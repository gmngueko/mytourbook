/*******************************************************************************
 * Copyright (C) 2005, 2022 Wolfgang Schramm and Contributors
 * Copyright (C) 2018, 2021 Thomas Theussing
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

/*
 * Original: org.oscim.layers.PathLayer
 */
package net.tourbook.map25.layer.tourtrack;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import net.tourbook.common.color.ColorUtil;
import net.tourbook.map25.Map25ConfigManager;
import net.tourbook.map25.layer.tourtrack.Map25TrackConfig.LineColorMode;
import net.tourbook.map25.renderer.BucketRendererMT;
import net.tourbook.map25.renderer.LineBucketMT;
import net.tourbook.map25.renderer.RenderBucketMT;
import net.tourbook.map25.renderer.RenderBucketsAllMT;

import org.oscim.backend.canvas.Paint.Cap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.layers.Layer;
//import org.oscim.layers.vector.
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.theme.styles.LineStyle;
import org.oscim.utils.FastMath;
import org.oscim.utils.async.SimpleWorker;
import org.oscim.utils.geom.LineClipper;

/**
 * This class draws a path line in given color or texture.
 * <p>
 * Example to handle track hover/selection
 * org.oscim.layers.marker.ItemizedLayer.activateSelectedItems(MotionEvent, ActiveItem)
 * <p>
 * Original code: org.oscim.layers.PathLayer
 */
public class TourLayer extends Layer {

   private static final int RENDERING_DELAY = 0;

   /**
    * Stores points, converted to the map projection.
    */
   private GeoPoint[]       _allGeoPoints;
   private TIntArrayList    _allTourStarts;

   private boolean          _isUpdatePoints;

   /**
    * Line style
    */
   private LineStyle        _lineStyle;

   /*
    * Track config values
    */
   private int                _config_LineColorMode;

   private final Worker       _simpleWorker;

   private boolean            _isUpdateLayer;
   private int[]              _allGeoPointColors;

   private RenderBucketsAllMT _currentTaskRenderBuckets;

   private final class TourRenderer extends BucketRendererMT {

      private int __oldX         = -1;
      private int __oldY         = -1;
      private int __oldZoomScale = -1;

      @Override
      public synchronized void update(final GLViewport viewport) {

         if (isEnabled() == false) {
            return;
         }

         final int currentZoomScale = 1 << viewport.pos.zoomLevel;
         final int currentX = (int) (viewport.pos.x * currentZoomScale);
         final int currentY = (int) (viewport.pos.y * currentZoomScale);

         // update layers when map moved by at least one tile
         if (currentX != __oldX || currentY != __oldY || currentZoomScale != __oldZoomScale || _isUpdateLayer) {

            /*
             * It took me many days to find this solution that a newly selected tour is
             * displayed after the map position was moved/tilt/rotated. It works but I don't
             * know exacly why.
             */
            if (_isUpdateLayer) {
               _simpleWorker.cancel(true);
            }

            _isUpdateLayer = false;

            _simpleWorker.submit(RENDERING_DELAY);

            __oldX = currentX;
            __oldY = currentY;
            __oldZoomScale = currentZoomScale;
         }

         final TourRenderTask workerTask = _simpleWorker.poll();

         if (workerTask == null) {

            // task is done -> nothing to do
            return;
         }

         // keep position to render relative to current state
         mMapPosition.copy(workerTask.__mapPos);

         // compile new layers
         final RenderBucketMT firstChainedBucket = workerTask.__allRenderBuckets.get();
         allBuckets.set(firstChainedBucket);

         compile();
      }
   }

   private final static class TourRenderTask {

      RenderBucketsAllMT __allRenderBuckets = new RenderBucketsAllMT();
      MapPosition        __mapPos           = new MapPosition();
   }

   final class Worker extends SimpleWorker<TourRenderTask> {

      private static final int  MIN_DIST               = 3;

      /**
       * Visible pixel of a line/tour, all other pixels are clipped with {@link #__lineClipper}
       */
      // limit coords
      private static final int  MAX_VISIBLE_PIXEL      = 2048;

      /**
       * Pre-projected points
       * <p>
       * Is projecting -180°...180° => 0...1 by using the {@link MercatorProjection}
       */
      private double[]          __projectedPoints      = new double[2];

      /**
       * Points which are projected (0...1) and then scaled to pixel
       */
      private float[]           __pixelPoints;

      /**
       * One {@link #__pixelPointColors2} has two {@link #__pixelPoints}, 2 == Half of items
       */
      private int[]             __pixelPointColors2;

      /**
       * Contains the x/y projected pixels where direction arrows are painted
       */
      private TFloatArrayList   __pixelDirectionArrows = new TFloatArrayList();

      /**
       * Is clipping line positions between
       * <p>
       * - {@link #MAX_VISIBLE_PIXEL} (-2048) ... <br>
       * + {@link #MAX_VISIBLE_PIXEL} (+2048)
       */
      private final LineClipper __lineClipper;

      private int               __numGeoPoints;

      public Worker(final Map map) {

         super(map, 50, new TourRenderTask(), new TourRenderTask());

         __lineClipper = new LineClipper(

               -MAX_VISIBLE_PIXEL,
               -MAX_VISIBLE_PIXEL,
               MAX_VISIBLE_PIXEL,
               MAX_VISIBLE_PIXEL);

         __pixelPoints = new float[0];
         __pixelPointColors2 = new int[0];
         __pixelDirectionArrows.clearQuick();
      }

      /**
       * Adds a point (2 points: x,y) which are in the range of the {@link #__lineClipper},
       * -2048...+2048
       *
       * @param points
       * @param pointIndex
       * @param x
       * @param y
       * @return
       */
      private int addPoint(final float[] points, int pointIndex, final int x, final int y) {

         points[pointIndex++] = x;
         points[pointIndex++] = y;

         return pointIndex;
      }

      @Override
      public void cleanup(final TourRenderTask task) {

         task.__allRenderBuckets.clear();
      }

      @Override
      public boolean doWork(final TourRenderTask task) {

         int numGeoPoints = __numGeoPoints;

         if (_isUpdatePoints) {

            synchronized (_allGeoPoints) {

               _isUpdatePoints = false;
               __numGeoPoints = numGeoPoints = _allGeoPoints.length;

               double[] projectedPoints = __projectedPoints;

               if (numGeoPoints * 2 >= projectedPoints.length) {

                  projectedPoints = __projectedPoints = new double[numGeoPoints * 2];

                  __pixelPoints = new float[numGeoPoints * 2];
                  __pixelPointColors2 = new int[numGeoPoints];
                  __pixelDirectionArrows.clearQuick();
               }

               for (int pointIndex = 0; pointIndex < numGeoPoints; pointIndex++) {
                  MercatorProjection.project(_allGeoPoints[pointIndex], projectedPoints, pointIndex);
               }
            }
         }

         _currentTaskRenderBuckets = task.__allRenderBuckets;

         if (numGeoPoints == 0) {

            if (task.__allRenderBuckets.get() != null) {

               task.__allRenderBuckets.clear();

               mMap.render();
            }

            return true;
         }

         doWork_Rendering(task, numGeoPoints);

         // trigger redraw to let renderer fetch the result.
         mMap.render();

         return true;
      }

      private void doWork_Rendering(final TourRenderTask task, final int numPoints) {

         final Map25TrackConfig trackConfig = Map25ConfigManager.getActiveTourTrackConfig();

         final LineBucketMT lineBucket = getLineBucket(task.__allRenderBuckets);

         final MapPosition mapPos = task.__mapPos;
         mMap.getMapPosition(mapPos);

         final int zoomlevel = mapPos.zoomLevel;
         mapPos.scale = 1 << zoomlevel;

         // current map positions 0...1
         final double currentMapPosX = mapPos.x; // 0...1, lat == 0 -> 0.5
         final double currentMapPosY = mapPos.y; // 0...1, lon == 0 -> 0.5

         // number of x/y pixels for the whole map at the current zoom level
         final double maxMapPixel = Tile.SIZE * mapPos.scale;
         final int maxMapPixel2 = Tile.SIZE << (zoomlevel - 1);

         // flip around dateline
         int flip = 0;

         int pixelX = (int) ((__projectedPoints[0] - currentMapPosX) * maxMapPixel);
         int pixelY = (int) ((__projectedPoints[1] - currentMapPosY) * maxMapPixel);

         if (pixelX > maxMapPixel2) {

            pixelX -= maxMapPixel2 * 2;
            flip = -1;

         } else if (pixelX < -maxMapPixel2) {

            pixelX += maxMapPixel2 * 2;
            flip = 1;
         }

         // setup first tour index
         int tourIndex = 0;
         int nextTourStartIndex = getNextTourStartIndex(tourIndex);

         // setup tour clipper
         __lineClipper.clipStart(pixelX, pixelY);

         final float[] pixelPoints = __pixelPoints;
         final int[] pixelPointColors2 = __pixelPointColors2;

         __pixelDirectionArrows.clearQuick();
         final TFloatArrayList allDirectionArrowPixel = __pixelDirectionArrows;

         // set first point / color / direction arrow
         int pixelPointIndex = addPoint(pixelPoints, 0, pixelX, pixelY);
         pixelPointColors2[0] = _allGeoPointColors[0];
         allDirectionArrowPixel.add(pixelX);
         allDirectionArrowPixel.add(pixelY);

         float prevX = pixelX;
         float prevY = pixelY;
         float prevXArrow = pixelX;
         float prevYArrow = pixelY;

         float[] segment = null;

         for (int projectedPointIndex = 2; projectedPointIndex < numPoints * 2; projectedPointIndex += 2) {

            // convert projected points 0...1 into map pixel
            pixelX = (int) ((__projectedPoints[projectedPointIndex + 0] - currentMapPosX) * maxMapPixel);
            pixelY = (int) ((__projectedPoints[projectedPointIndex + 1] - currentMapPosY) * maxMapPixel);

            int flipDirection = 0;

            if (pixelX > maxMapPixel2) {

               pixelX -= maxMapPixel2 * 2;
               flipDirection = -1;

            } else if (pixelX < -maxMapPixel2) {

               pixelX += maxMapPixel2 * 2;
               flipDirection = 1;
            }

            if (flip != flipDirection) {

               flip = flipDirection;

               if (pixelPointIndex > 2) {
                  lineBucket.addLine(pixelPoints, pixelPointIndex, false, pixelPointColors2);
               }

               __lineClipper.clipStart(pixelX, pixelY);

               pixelPointIndex = addPoint(pixelPoints, 0, pixelX, pixelY);
               pixelPointColors2[0] = _allGeoPointColors[projectedPointIndex / 2];

               continue;
            }

            // ckeck if a new tour starts
            if (projectedPointIndex >= nextTourStartIndex) {

               // finish last tour (copied from flip code)
               if (pixelPointIndex > 2) {
                  lineBucket.addLine(pixelPoints, pixelPointIndex, false, pixelPointColors2);
               }

               // setup next tour
               nextTourStartIndex = getNextTourStartIndex(++tourIndex);

               __lineClipper.clipStart(pixelX, pixelY);
               pixelPointIndex = addPoint(pixelPoints, 0, pixelX, pixelY);
               pixelPointColors2[0] = _allGeoPointColors[projectedPointIndex / 2];

               continue;
            }

            final int clipperCode = __lineClipper.clipNext(pixelX, pixelY);

            if (clipperCode != LineClipper.INSIDE) {

               /*
                * Point is outside clipper
                */

               if (pixelPointIndex > 2) {
                  lineBucket.addLine(pixelPoints, pixelPointIndex, false, pixelPointColors2);
               }

               if (clipperCode == LineClipper.INTERSECTION) {

                  // add line segment
                  segment = __lineClipper.getLine(segment, 0);
                  lineBucket.addLine(segment, 4, false, pixelPointColors2);

                  // the prev point is the real point not the clipped point
                  // prevX = __lineClipper.outX2;
                  // prevY = __lineClipper.outY2;
                  prevX = pixelX;
                  prevY = pixelY;
               }

               pixelPointIndex = 0;

               // if the end point is inside, add it
               if (__lineClipper.getPrevOutcode() == LineClipper.INSIDE) {

                  pixelPoints[pixelPointIndex++] = prevX;
                  pixelPoints[pixelPointIndex++] = prevY;

                  pixelPointColors2[(pixelPointIndex - 1) / 2] = _allGeoPointColors[projectedPointIndex / 2];
               }

               continue;
            }

            /*
             * Point is inside clipper
             */

            final float diffX = pixelX - prevX;
            final float diffY = pixelY - prevY;

            if (pixelPointIndex == 0 || FastMath.absMaxCmp(diffX, diffY, MIN_DIST)) {

               // point > min distance == 3

               pixelPoints[pixelPointIndex++] = prevX = pixelX;
               pixelPoints[pixelPointIndex++] = prevY = pixelY;

               pixelPointColors2[(pixelPointIndex - 1) / 2] = _allGeoPointColors[projectedPointIndex / 2];
            }

            final float diffXArrow = pixelX - prevXArrow;
            final float diffYArrow = pixelY - prevYArrow;

            if (projectedPointIndex == 0 || FastMath.absMaxCmp(diffXArrow, diffYArrow, trackConfig.arrow_MinimumDistance)) {

               // point > min distance

               prevXArrow = pixelX;
               prevYArrow = pixelY;

               allDirectionArrowPixel.add(pixelX);
               allDirectionArrowPixel.add(pixelY);
            }
         }

         if (pixelPointIndex > 2) {
            lineBucket.addLine(pixelPoints, pixelPointIndex, false, pixelPointColors2);
         }

         if (trackConfig.isShowDirectionArrow) {
            lineBucket.createDirectionArrowVertices(__pixelDirectionArrows);
         }
      }

      private int getNextTourStartIndex(final int tourIndex) {

         if (_allTourStarts.size() > tourIndex + 1) {
            return _allTourStarts.get(tourIndex + 1) * 2;
         } else {
            return Integer.MAX_VALUE;
         }
      }

   }

   public TourLayer(final Map map) {

      super(map);

      _lineStyle = createLineStyle();

      _allGeoPoints = new GeoPoint[] {};
      _allTourStarts = new TIntArrayList();

      mRenderer = new TourRenderer();
      _simpleWorker = new Worker(map);
   }

   private LineStyle createLineStyle() {

      final Map25TrackConfig trackConfig = Map25ConfigManager.getActiveTourTrackConfig();

      _config_LineColorMode = trackConfig.lineColorMode == LineColorMode.SOLID

            // solid color
            ? 0

            // gradient color
            : 1;

      final int lineColor = ColorUtil.getARGB(trackConfig.lineColor, trackConfig.lineOpacity);

      final int trackVerticalOffset = trackConfig.isTrackVerticalOffset
            ? trackConfig.trackVerticalOffset
            : 0;

      final LineStyle style = LineStyle.builder()

            .strokeWidth(trackConfig.lineWidth)

            .color(lineColor)

//          .cap(Cap.BUTT)
//          .cap(Cap.SQUARE)
            .cap(Cap.ROUND)

            // I don't know how outline is working
            // .isOutline(true)

            // "u_height" is above the ground -> this is the z axis
            .heightOffset(trackVerticalOffset)

            // VERY IMPORTANT: Set fixed=true, otherwise the line width
            // will jump when the zoom-level is changed !!!
            .fixed(true)

//          .blur(trackConfig.testValue / 100.0f)

            .build();

      return style;
   }

   /**
    * Update linestyle in the bucket
    *
    * @param allRenderBuckets
    * @return
    */
   private LineBucketMT getLineBucket(final RenderBucketsAllMT allRenderBuckets) {

      LineBucketMT lineBucket;

      lineBucket = allRenderBuckets.getLineBucket(0);

// SET_FORMATTING_OFF

      lineBucket.lineStyle       = _lineStyle;
      lineBucket.lineColorMode   = _config_LineColorMode;

// SET_FORMATTING_ON

      return lineBucket;
   }

   public void onModifyConfig(final boolean isVerticesModified) {

      _lineStyle = createLineStyle();

      if (isVerticesModified) {

         // vertices structure is modified -> recreate vertices

         _simpleWorker.submit(RENDERING_DELAY);

      } else {

         // do a fast update

         getLineBucket(_currentTaskRenderBuckets);

         mMap.render();
      }
   }

   public void setPoints(final GeoPoint[] allGeoPoints, final int[] allGeoPointColors, final TIntArrayList allTourStarts) {

      synchronized (_allGeoPoints) {

         _allGeoPoints = allGeoPoints;
         _allGeoPointColors = allGeoPointColors;

         _allTourStarts.clear();
         _allTourStarts.addAll(allTourStarts);
      }

      _simpleWorker.cancel(true);

      _isUpdatePoints = true;
      _isUpdateLayer = true;
   }
}
