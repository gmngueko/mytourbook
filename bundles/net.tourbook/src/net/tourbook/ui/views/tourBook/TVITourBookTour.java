/*******************************************************************************
 * Copyright (C) 2005, 2024 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.tourbook.common.util.TreeViewerItem;
import net.tourbook.database.TourDatabase;

public class TVITourBookTour extends TVITourBookItem implements Comparable<TVITourBookTour> {

   public long             tourId;

   /**
    * The default for tour type id is not 0 because 0 is a valid tour type id and would be used even
    * when tour type id is not set.
    */
   long                    tourTypeId = TourDatabase.ENTITY_IS_NOT_SAVED;

   long                    colDateTime_MS;
   String                  colDateTime_Text;

   long                    colStartDistance;
   short                   colTimeInterval;

   Set<Long>               sqlTagIds;
   Set<Long>               sqlMarkerIds;
   Set<Long>               sqlNutritionProductsIds;

   /**
    * Id's for the tags or <code>null</code> when tags are not available
    */
   private ArrayList<Long> _tagIds;

   /**
    * Id's for the markers or <code>null</code> when markers are not available
    */
   private ArrayList<Long> _markerIds;

   /**
    * Id's for the nutrition products or <code>null</code> when nutrition products are not available
    */
   private ArrayList<Long> _nutritionProductsIds;

   /**
    * @param view
    * @param parentItem
    */
   public TVITourBookTour(final TourBookView view, final TreeViewerItem parentItem) {

      super(view);

      setParentItem(parentItem);
   }

   @Override
   public void clearChildren() {

      // cleanup
      sqlTagIds = null;
      sqlMarkerIds = null;
      sqlNutritionProductsIds = null;

      _tagIds = null;
      _markerIds = null;
      _nutritionProductsIds = null;

      super.clearChildren();
   }

   @Override
   public int compareTo(final TVITourBookTour tviTour) {

      int compared = colDateTime_MS < tviTour.colDateTime_MS
            ? -1
            : colDateTime_MS == tviTour.colDateTime_MS
                  ? 0
                  : 1;

      // add additional comparing when both tours have the same date/time that the sorting is unique
      if (compared == 0) {

         compared = col_ImportFileName.compareTo(tviTour.col_ImportFileName);
      }

      return compared;
   }

   @Override
   protected void fetchChildren() {}

   public long getColumnStartDistance() {
      return colStartDistance;
   }

   public short getColumnTimeInterval() {
      return colTimeInterval;
   }

   public List<Long> getMarkerIds() {
      if (sqlMarkerIds != null && _markerIds == null) {
         _markerIds = new ArrayList<>(sqlMarkerIds);
      }
      return _markerIds;
   }

   public List<Long> getNutritionProductsIds() {

      if (sqlNutritionProductsIds != null && _nutritionProductsIds == null) {
         _nutritionProductsIds = new ArrayList<>(sqlNutritionProductsIds);
      }
      return _nutritionProductsIds;
   }

   public List<Long> getTagIds() {
      if (sqlTagIds != null && _tagIds == null) {
         _tagIds = new ArrayList<>(sqlTagIds);
      }
      return _tagIds;
   }

   @Override
   public Long getTourId() {
      return tourId;
   }

   /**
    * @return Returns the tour type id of the tour or {@link TourDatabase#ENTITY_IS_NOT_SAVED} when
    *         the tour type is not set.
    */
   public long getTourTypeId() {
      return tourTypeId;
   }

   /**
    * tour items do not have children
    */
   @Override
   public boolean hasChildren() {
      return false;
   }

   public void setMarkerIds(final Set<Long> markerIds) {
      sqlMarkerIds = markerIds;
   }

   public void setNutritionProductsIds(final Set<Long> nutritionProductsIds) {
      sqlNutritionProductsIds = nutritionProductsIds;
   }

   public void setTagIds(final Set<Long> tagIds) {
      sqlTagIds = tagIds;
   }

   @Override
   public String toString() {

      return NL

            + "TVITourBookTour" + NL //                           //$NON-NLS-1$

            + "[" + NL //                                         //$NON-NLS-1$

            + "colDateTimeText   = " + colDateTime_Text + NL //    //$NON-NLS-1$
//          + "colTourDateTime   = " + colTourDateTime + NL //     //$NON-NLS-1$
            + "colTourTitle      = " + colTourTitle + NL //        //$NON-NLS-1$

            + "]" + NL //                                         //$NON-NLS-1$
      ;
   }

}
