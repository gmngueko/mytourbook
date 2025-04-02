/*******************************************************************************
 * Copyright (C) 2015, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.importdata;

import java.util.ArrayList;

import net.tourbook.common.UI;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.data.TourType;
import net.tourbook.tag.TagGroup;
import net.tourbook.tag.TagGroupManager;
import net.tourbook.tour.CadenceMultiplier;
import net.tourbook.tour.location.TourLocationProfile;

public class ImportLauncher implements Cloneable {

   private static long             _idCreator;

   public String                   description                   = UI.EMPTY_STRING;
   public String                   name                          = UI.EMPTY_STRING;

   /**
    * When <code>null</code> then the tour type is not set.
    */
   public Enum<TourTypeConfig>     tourTypeConfig;

   public TourType                 oneTourType;
   public CadenceMultiplier        oneTourTypeCadence;

   public ArrayList<SpeedTourType> speedTourTypes                = new ArrayList<>();

   /** Contains the image hash or 0 when an image is not displayed. */
   public int                      imageHash;

   public int                      imageWidth;

   /**
    * Show/hide this launcher in the dashboard.
    */
   public boolean                  isShowInDashboard             = true;

   /**
    * When <code>true</code>, assigns a type to the tour.
    */
   public boolean                  isSetTourType;

   /**
    * When <code>true</code> save the tour for the active person.
    */
   public boolean                  isSaveTour;

   /**
    * When <code>true</code> then the text of the last marker is set.
    */
   public boolean                  isSetLastMarker;

   /**
    * When <code>true</code> then a marker will be removed when it is located at the 2nd last time
    * slice
    */
   public boolean                  isRemove2ndLastTimeSliceMarker;

   /**
    * Last marker distance in meters.
    */
   public int                      lastMarkerDistance;

   public String                   lastMarkerText                = UI.EMPTY_STRING;

   /**
    * When <code>true</code> then the tour start temperature is adjusted.
    */
   public boolean                  isAdjustTemperature;

   /**
    * When <code>true</code>, the weather data is saved in the tour.
    */
   public boolean                  isRetrieveWeatherData;

   /**
    * Duration in seconds during which the temperature is adjusted.
    */
   public int                      temperatureAdjustmentDuration = EasyConfig.TEMPERATURE_ADJUSTMENT_DURATION_DEFAULT;

   /**
    * Temperature adjustment will be performed when the tour average temperature is below this
    * value.
    */
   public float                    tourAvgTemperature            = EasyConfig.TEMPERATURE_AVG_TEMPERATURE_DEFAULT;

   private long                    _id;

   /**
    * When <code>true</code> then elevation from the first time slice is replaced with the value of
    * the 2nd time slice
    * <p>
    * This fixes an issue after updating the Garmin Edge 1030 firmware version to 12.20, sometimes
    * it has total wrong elevation value for the first time slice
    */
   public boolean                  isReplaceFirstTimeSliceElevation;

   /**
    * When <code>true</code> then the elevation up/down totals are computed from SRTM data when
    * available
    */
   public boolean                  isReplaceElevationFromSRTM;

   /**
    * When <code>true</code> then tour start/end locations are retrieved and set into the tour
    */
   public boolean                  isRetrieveTourLocation;

   /**
    * This tour location profile is used to set the tour location values
    */
   public TourLocationProfile      tourLocationProfile;

   /**
    * When <code>true</code> then all tags in a groups are set into the tour
    */
   public boolean                  isSetTourTagGroup;

   /**
    * ID of the {@link TagGroup}
    */
   public String                   tourTagGroupID;

   public ImportLauncher() {

      _id = ++_idCreator;
   }

   @Override
   protected ImportLauncher clone() {

      ImportLauncher clonedObject = null;

      try {

         clonedObject = (ImportLauncher) super.clone();

         clonedObject._id = ++_idCreator;

         clonedObject.speedTourTypes = new ArrayList<>();

         for (final SpeedTourType speedVertex : speedTourTypes) {
            clonedObject.speedTourTypes.add(speedVertex.clone());
         }

      } catch (final CloneNotSupportedException e) {
         StatusUtil.log(e);
      }

      return clonedObject;
   }

   @Override
   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final ImportLauncher other = (ImportLauncher) obj;
      if (_id != other._id) {
         return false;
      }
      return true;
   }

   /**
    * @return Returns a unique id for this import tile.
    */
   public long getId() {
      return _id;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (_id ^ (_id >>> 32));
      return result;
   }

   /**
    * @return Returns <code>true</code> when tags are set into the tour
    */
   public boolean isSetTags() {

      final TagGroup tagGroup = TagGroupManager.getTagGroup(tourTagGroupID);

      if (tagGroup != null && isSetTourTagGroup) {

         return tagGroup.tourTags.size() > 0;
      }

      return false;
   }

   /**
    * Setup data for the tour type config image.
    */
   void setupItemImage() {

      if (TourTypeConfig.TOUR_TYPE_CONFIG_BY_SPEED.equals(tourTypeConfig)) {

         final int numVertices = speedTourTypes.size();

         imageHash = speedTourTypes.hashCode();
         imageWidth = numVertices * TourType.TOUR_TYPE_IMAGE_SIZE;

      } else if (TourTypeConfig.TOUR_TYPE_CONFIG_ONE_FOR_ALL.equals(tourTypeConfig)) {

         if (oneTourType == null) {

            imageHash = 0;
            imageWidth = 0;

         } else {

            imageHash = oneTourType.hashCode();
            imageWidth = TourType.TOUR_TYPE_IMAGE_SIZE;
         }

      } else {

         // this is the default, no image

         imageHash = 0;
         imageWidth = 0;
      }
   }

   @Override
   public String toString() {
      return "DeviceImportLauncher [" //$NON-NLS-1$
            //
            + ("name=" + name + ", ") //$NON-NLS-1$ //$NON-NLS-2$
//            + ("speedTourTypes=" + speedTourTypes + ", ") //$NON-NLS-1$ //$NON-NLS-2$
//            + ("tourTypeConfig=" + tourTypeConfig + ", ") //$NON-NLS-1$ //$NON-NLS-2$
            + ("lastMarkerDistance=" + lastMarkerDistance + ", ") //$NON-NLS-1$ //$NON-NLS-2$

            + "]"; //$NON-NLS-1$
   }
}
