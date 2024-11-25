/*******************************************************************************
 * Copyright (C) 2010, 2024 Wolfgang Schramm and Contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
package de.byteholder.geoclipse.map;

import de.byteholder.geoclipse.Messages;
import de.byteholder.geoclipse.map.event.TileEventId;
import de.byteholder.geoclipse.mapprovider.MP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.tourbook.common.UI;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.StringUtils;
import net.tourbook.common.util.Util;
import net.tourbook.web.WEB;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;

/**
 * This class loads the tile images. The run method is called from the executer in the thread queue.
 */
public class TileImageLoader implements Runnable {

   private static int                  _stackTraceCounter;

   private static List<FairUseLimiter> _allFairUseLimiter = new ArrayList<>();

   static {

      /**
       * Web Map Tile Service WMTS-BGDI
       * <p>
       * wmts.geo.admin.ch <br>
       * 600 Requests / Minute <br>
       * https://www.geo.admin.ch/de/allgemeine-nutzungsbedingungen-bgdi/#doc-1flo19d440<br>
       */
//    _allFairUseLimiter.add(new FairUseLimiter("wmts.geo.admin.ch", 600, 60));
      _allFairUseLimiter.add(new FairUseLimiter("wmts.geo.admin.ch", 60, 6)); //$NON-NLS-1$
   }

   public static class FairUseLimiter {

      String        mapProviderUrlPart;

      int           numMaxRequestsPerTimeSlice;
      int           timeSliceDurationInSec;

      AtomicLong    firstRequestTime     = new AtomicLong();
      AtomicInteger numTimeSliceRequests = new AtomicInteger();

      /**
       * @param mapProviderUrlPart
       * @param numMaxRequestsPerTimeSlice
       * @param timeSliceDurationInSec
       *           Number of seconds for one time slice
       */
      public FairUseLimiter(final String mapProviderUrlPart,
                            final int numMaxRequestsPerTimeSlice,
                            final int timeSliceDurationInSec) {

         this.mapProviderUrlPart = mapProviderUrlPart;

         this.numMaxRequestsPerTimeSlice = numMaxRequestsPerTimeSlice;
         this.timeSliceDurationInSec = timeSliceDurationInSec;
      }
   }

   /**
    * Loads a tile image from a map provider which is contained in the tile. Tiles are retrieved
    * from the tile waiting queue {@link MP#getTileWaitingQueue()}
    */
   public TileImageLoader() {}

   /**
    * Ensure fair use
    *
    * @param url
    *
    * @throws InterruptedException
    */
   private void ensureFairUse(final URL url) throws InterruptedException {

      for (final FairUseLimiter fairUseLimiter : _allFairUseLimiter) {

         final String mapProviderUrlPart = fairUseLimiter.mapProviderUrlPart;

         if (url.toString().contains(mapProviderUrlPart)) {

            // force fair use for this map provider

// SET_FORMATTING_OFF

            final long now_Sec = System.currentTimeMillis() / 1000;

            final long firstRequestTime_Sec        = fairUseLimiter.firstRequestTime.get();
            final int timeSliceDurationInSec       = fairUseLimiter.timeSliceDurationInSec;
            final int numMaxRequestsPerTimeSlice   = fairUseLimiter.numMaxRequestsPerTimeSlice;
            final int numTimeSliceRequests         = fairUseLimiter.numTimeSliceRequests.get();

            final long newRequestTimeDiff = (now_Sec - firstRequestTime_Sec) % timeSliceDurationInSec;

            final boolean isInCurrentTimeSlice     = now_Sec < firstRequestTime_Sec + timeSliceDurationInSec;
            final boolean isAboveMaxRequests       = numTimeSliceRequests + 1 > numMaxRequestsPerTimeSlice;

// SET_FORMATTING_ON

            if (isInCurrentTimeSlice && isAboveMaxRequests) {

               // wait until the next time slice

               final long nextTimeSlice_Sec = firstRequestTime_Sec + timeSliceDurationInSec;
               final long timeDiff_Sec = nextTimeSlice_Sec - now_Sec;

               System.out.println(UI.timeStamp() + " - %s - Fair use is sleeping for %d sec".formatted( //$NON-NLS-1$
                     mapProviderUrlPart,
                     timeDiff_Sec));

               Thread.sleep(timeDiff_Sec * 1000);

               // reset fair use timer + counter
               fairUseLimiter.firstRequestTime.set(System.currentTimeMillis() / 1000);
               fairUseLimiter.numTimeSliceRequests.set(1);

            } else if (isInCurrentTimeSlice == false) {

               // we are out of the current time slice -> reset time slice counter

               final long newRequestTime = now_Sec - newRequestTimeDiff;

//             System.out.println(UI.timeStamp() + " - %s - Fair use is resetting timer -%d sec".formatted( //$NON-NLS-1$
//                  mapProviderUrlPart,
//                  newRequestTimeDiff));

               fairUseLimiter.firstRequestTime.set(newRequestTime);
               fairUseLimiter.numTimeSliceRequests.set(1);

            } else {

               // we are in the current time slice and not above the limit

               fairUseLimiter.numTimeSliceRequests.incrementAndGet();

//             System.out.println(UI.timeStamp() + " - Map image loader: Num requests %d - %d sec".formatted(numRequests, newRequestTimeDiff)); //$NON-NLS-1$
            }
         }

         // there can be only one fair use limiter for a url

         break;
      }
   }

   private void finalizeTile(final Tile tile, final boolean isNotifyObserver) {

      final String tileKey = tile.getTileKey();

      if (tile.isLoadingError()) {

         // move tile from tile cache into the cache which contains tiles with errors

         MP.getErrorTiles().add(tileKey, tile);

         MP.getTileCache().remove(tileKey);
      }

      // set tile state, notify observer (Map is an observer)
      tile.setLoading(false);

      if (isNotifyObserver) {
         tile.callTileImageLoaderCallback();
      }

      MP.fireTileEvent(TileEventId.TILE_END_LOADING, tile);
   }

   /**
    * Get tile image from offline file, url or tile painter
    *
    * @param tile
    */
   private void getTileImage(final Tile tile) {

      String loadingError = null;
      boolean isNotifyObserver = true;
      boolean isParentFinal = false;

      Tile parentTile = null;

      try {

         boolean isSaveImage = false;
         boolean isLoadingImage = false;

         final MP mp = tile.getMP();
         final TileImageCache tileImageCache = mp.getTileImageCache();

         final boolean useOfflineImage = mp.isUseOfflineImage();

         ImageData tileImageData = null;
         Image tileOfflineImage = null;

         // load image from offline cache
         if (useOfflineImage) {
            tileOfflineImage = tileImageCache.getOfflineImage(tile);
         }

         if (tileOfflineImage == null) {

            // offline image is not available

            isSaveImage = true;

            final ITilePainter tilePainter = mp.getTilePainter();

            if (tilePainter instanceof ITilePainter) {

               // paint tile image

               tileImageData = paintTileImage(tile, tilePainter);

            } else {

               // load tile image from a url

               InputStream inputStream = null;

               try {

                  if (mp instanceof ITileLoader) {

                     /*
                      * get image from a tile loader (this feature is used to load images
                      * from a wms server)
                      */

                     try {
                        inputStream = ((ITileLoader) mp).getTileImageStream(tile);
                     } catch (final Exception e) {
                        loadingError = e.getMessage();
                        StatusUtil.log(loadingError, e);
                        throw e;
                     }

                  } else {

                     /*
                      * get image from a url (this was the behaviour before wms was
                      * supported)
                      */

                     final URL url;

                     try {

                        url = mp.getTileURLEncoded(tile);

                     } catch (final Exception e) {
                        loadingError = e.getMessage();
                        throw e;
                     }

                     ensureFairUse(url);

                     try {

                        final URLConnection connection = url.openConnection();

                        connection.setReadTimeout(5000);

                        final String userAgent = mp.getUserAgent();

                        if (StringUtils.hasContent(userAgent)) {

                           // OSM needs that a user agent is set
                           connection.setRequestProperty(WEB.HTTP_HEADER_USER_AGENT, userAgent);
                        }

                        inputStream = connection.getInputStream();

                     } catch (final FileNotFoundException e) {

                        loadingError = NLS.bind(Messages.Error_Loading_FileNotFoundException_DBG052, tile.getUrl(), e.getMessage());

                        // this is hidden because it can happen very often
                        // StatusUtil.log(IMAGE_HAS_LOADING_ERROR, e);
                        throw e;

                     } catch (final UnknownHostException e) {

                        loadingError = NLS.bind(Messages.Error_Loading_UnknownHostException_DBG053, tile.getUrl(), e.getMessage());

                        // this is hidden because it can happen very often
                        // StatusUtil.log(IMAGE_HAS_LOADING_ERROR, e);
                        throw e;

                     } catch (final Exception e) {

                        loadingError = NLS.bind(Messages.Error_Loading_FromUrl_DBG054, tile.getUrl(), e.getMessage());

                        // this is hidden because it can happen very often
                        // StatusUtil.log(IMAGE_HAS_LOADING_ERROR, e);
                        throw e;
                     }
                  }

                  isLoadingImage = true;
                  final ImageData[] loadedImageData = new ImageLoader().load(inputStream);

                  if (loadedImageData != null && loadedImageData.length > 0) {
                     tileImageData = loadedImageData[0];
                  }

               } catch (final Exception e) {

                  /*
                   * Exception occurs when loading the image, don't remove them from the
                   * loading list, so that the tiles don't get reloaded
                   */

                  if (isLoadingImage) {

                     /*
                      * Log only when images are loaded to debug this issue
                      * https://github.com/wolfgang-ch/mytourbook/issues/317
                      */

                     StatusUtil.logError("Cannot load image from: " + tile.getUrl());//$NON-NLS-1$
                  }

                  try {
                     if (inputStream != null) {

                        /*
                         * Print stack track otherwise many popups can occur
                         */

                        if (_stackTraceCounter++ > 1) {

                           e.printStackTrace();

                        } else {

                           // the stream can contain an error message from the wms server
                           StatusUtil.showStatus(Util.readContentFromStream(inputStream), e);
                        }

                        inputStream.close();

                        isSaveImage = false;
                     }
                  } catch (final IOException e1) {

                     if (_stackTraceCounter++ > 1) {

                        e.printStackTrace();

                     } else {

                        StatusUtil.log(e.getMessage(), e);
                     }
                  }

                  MP.fireTileEvent(TileEventId.TILE_ERROR_LOADING, tile);
               }
            }
         }

         /**
          * Tile image is loaded from a url or from an offline file, is painted or is not
          * available
          */

         boolean isSetupImage = true;
         boolean isChildError = false;

         // set tile where the tile image is stored
         Tile imageTile = tile;
         String imageTileKey = tile.getTileKey();

         if (tileOfflineImage == null && tileImageData == null) {

            // image data is empty, set error

            tile.setLoadingError(loadingError == null
                  ? Messages.Error_Loading_EmptyImageData_DBG051
                  : loadingError);

            isSetupImage = false;
         }

         parentTile = tile.getParentTile();
         if (parentTile != null) {

            /*
             * The current tile is a child of a parent tile, create the parent image with this
             * child image data
             */

            isNotifyObserver = false;

            if (tileOfflineImage != null) {

               tileImageData = tileOfflineImage.getImageData();

               // when image data is used, the image is not needed any more
               tileOfflineImage.dispose();
               tileOfflineImage = null;
            }

            // save child image
            if (tileImageData != null && isSaveImage) {
               tileImageCache.saveOfflineImage(tile, tileImageData, false);
            }

            // set image into child
            final ParentImageStatus parentImageStatus = tile.createParentImage(tileImageData);

            if (parentImageStatus == null) {

               // set error into parent tile
               parentTile.setLoadingError(Messages.Error_Loading_CannotCreateParent_DBG050);

            } else {

               // check if the parent image is created
               if (parentImageStatus.isImageFinal) {

                  // create image only when the parent is final

                  // set image data from parent
                  tileImageData = parentImageStatus.tileImageData;

                  // use parent tile to store the image
                  imageTile = parentTile;
                  imageTileKey = parentTile.getTileKey();

                  // parent is final
                  isParentFinal = true;
                  isSetupImage = true;
                  isSaveImage = parentImageStatus.isSaveImage;
                  isChildError = parentImageStatus.isChildError;

               } else {

                  /*
                   * disable image creation, the image is created only when the parent is
                   * final
                   */

                  isSetupImage = false;
               }
            }
         }

         /*
          * Create tile image
          */
         if (isSetupImage) {

            // create/save image
            final Image tileImage = tileImageCache.setupImage(
                  tileImageData,
                  tileOfflineImage,
                  imageTile,
                  imageTileKey,
                  isSaveImage,
                  isChildError);

            if (imageTile.setMapImage(tileImage) == false) {

               // set an error to prevent it loading a second time
               tile.setLoadingError(Messages.Error_Loading_ImageIsInvalid_DBG049);
            }
         }

      } catch (final Exception e) {

         // this should not happen
         StatusUtil.log(NLS.bind(Messages.Error_Loading_DefaultException_DBG048, tile.getTileKey()), e);

      } finally {

         finalizeTile(tile, isNotifyObserver);

         if (isParentFinal) {
            finalizeTile(parentTile, true);
         }
      }
   }

   /**
    * paint tile based on SRTM data
    */
   private ImageData paintTileImage(final Tile tile, final ITilePainter tilePainter) {

      MP.fireTileEvent(TileEventId.SRTM_PAINTING_START, tile);

      ImageData tileImageData = null;

      try {

         /*
          * create tile image data from RGB data
          */

         // create RGB data for the tile
         final int[][] rgbData = tilePainter.drawTile(tile);

         final int tileSize = rgbData[0].length;

         tileImageData = new ImageData(//
               tileSize,
               tileSize,
               24,
               new PaletteData(0xFF, 0xFF00, 0xFF0000));

         final byte[] pixelData = tileImageData.data;
         final int bytesPerLine = tileImageData.bytesPerLine;

         for (int drawX = 0; drawX < rgbData.length; drawX++) {

            final int xBytesPerLine = drawX * bytesPerLine;
            final int[] rgbX = rgbData[drawX];

            for (int drawY = 0; drawY < rgbX.length; drawY++) {

               final int dataIndex = xBytesPerLine + (drawY * 3);

               final int rgb = rgbX[drawY];

               final int blue = (byte) ((rgb & 0xFF0000) >> 16);
               final int green = (byte) ((rgb & 0xFF00) >> 8);
               final int red = (byte) ((rgb & 0xFF) >> 0);

               pixelData[dataIndex] = (byte) (blue & 0xff);
               pixelData[dataIndex + 1] = (byte) (green & 0xff);
               pixelData[dataIndex + 2] = (byte) (red & 0xff);
            }
         }

         MP.fireTileEvent(TileEventId.SRTM_PAINTING_END, tile);

      } catch (final Exception e) {

         tile.setLoadingError(Messages.Error_Loading_PaintingError_DBG045 + e.getMessage());

         MP.fireTileEvent(TileEventId.SRTM_PAINTING_ERROR, tile);

         StatusUtil.log(e.getMessage(), e);
      }

      return tileImageData;
   }

   @Override
   public void run() {

      /*
       * load/create tile image
       */
      // get tile from queue
      final LinkedBlockingDeque<Tile> tileWaitingQueue = MP.getTileWaitingQueue();

      final Tile tile = tileWaitingQueue.pollFirst();

      if (tile == null) {
         // it's possible that the waiting queue was reset
         return;
      }

      final MP mp = tile.getMP();
      final boolean isParentTile = mp instanceof ITileChildrenCreator;
      {
         // current tile is in the viewport of the map

         MP.fireTileEvent(TileEventId.TILE_START_LOADING, tile);

         if (isParentTile) {

            // current tile is a parent tile

            if (tile.isLoadingError()) {

               /*
                * parent tile has a loading error, this can happen when it contains no child
                * because it does not support the zooming level
                */

               finalizeTile(tile, true);

            } else if (tile.isOfflimeImageAvailable()) {

               // parent tile has no children which needs to be loaded, behave as a normal tile

               getTileImage(tile);

            } else {

               // a parent gets finalized when the last child is loaded
            }

         } else {

            // tile is 'normal' or a children

            getTileImage(tile);
         }
      }

      // loading has finished
      tile.setFuture(null);
   }
}
