/*******************************************************************************
 * Copyright (C) 2005, 2019 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tagging;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import net.tourbook.common.UI;
import net.tourbook.common.time.TimeTools;
import net.tourbook.database.TourDatabase;

public class TVITagView_Tour extends TVITagViewItem {

   public static final String SQL_TOUR_COLUMNS = UI.EMPTY_STRING

         + "startYear,"                                         //	0 //$NON-NLS-1$
         + "startMonth,"                                        //	1 //$NON-NLS-1$
         + "startDay,"                                          //	2 //$NON-NLS-1$

         + "tourTitle,"                                         //	3 //$NON-NLS-1$
         + "tourType_typeId,"                                   //	4 //$NON-NLS-1$
         + "deviceTimeInterval,"                                //	5 //$NON-NLS-1$
         + "startDistance,"                                     //	6 //$NON-NLS-1$

         + SQL_SUM_COLUMNS_TOUR;                                //	7

   long                       tourId;

   ZonedDateTime              tourDate;

   int                        tourYear;
   int                        tourMonth;
   int                        tourDay;

   String                     tourTitle;
   long                       tourTypeId;

   ArrayList<Long>            tagIds;

   public long                deviceStartDistance;
   public short               deviceTimeInterval;

   public TVITagView_Tour(final TVITagViewItem parentItem) {
      setParentItem(parentItem);
   }

   @Override
   public boolean equals(final Object obj) {

      if (obj == this) {
         return true;
      }

      return false;
   }

   @Override
   protected void fetchChildren() {}

   public void getTourColumnData(final ResultSet result, final Object resultTagId, final int startIndex)
         throws SQLException {

      tourYear = result.getInt(startIndex + 0);
      tourMonth = result.getInt(startIndex + 1);
      tourDay = result.getInt(startIndex + 2);

      tourDate = ZonedDateTime.of(tourYear, tourMonth, tourDay, 0, 0, 0, 0, TimeTools.getDefaultTimeZone());

      tourTitle = result.getString(startIndex + 3);

      final Object resultTourTypeId = result.getObject(startIndex + 4);
      tourTypeId = (resultTourTypeId == null ? TourDatabase.ENTITY_IS_NOT_SAVED : (Long) resultTourTypeId);

      deviceTimeInterval = result.getShort(startIndex + 5);
      deviceStartDistance = result.getLong(startIndex + 6);

      if (UI.IS_SCRAMBLE_DATA) {
         tourTitle = UI.scrambleText(tourTitle);
      }

      readDefaultColumnData(result, startIndex + 7);

      if (resultTagId instanceof Long) {
         tagIds = new ArrayList<>();
         tagIds.add((Long) resultTagId);
      }
   }

   public long getTourId() {
      return tourId;
   }

   @Override
   public boolean hasChildren() {
      return false;
   }
}
