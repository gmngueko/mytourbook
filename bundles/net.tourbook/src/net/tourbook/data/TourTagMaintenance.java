/*******************************************************************************
 * Copyright (C) 2021 Gervais-Martial Ngueko
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
package net.tourbook.data;

import java.io.Serializable;
import java.util.TreeMap;

import net.tourbook.ui.UI;

public class TourTagMaintenance implements Cloneable, Serializable {

   /**
    *
    */
   private static final long                serialVersionUID = 314756931007979108L;

   private int                             extraHourUsed           = 0;
   private int                             extraMonthUsage         = 0;

   private TreeMap<Long, MaintenanceEvent> sortedEventsMaintenance           = new TreeMap<>();

   public class MaintenanceEvent implements Cloneable, Serializable {

      /**
       *
       */
      private static final long serialVersionUID = -3716703585072557410L;

      public long               eventEpochTime   = 0;
      public float              cost             = 0;
      public String             notes            = UI.EMPTY_STRING;
      public float              metersTotalUsed  = 0;
      public long               secondsTotalUsed = 0;
   }

   public void addEvent(final Long utcTime, final MaintenanceEvent newEvent) {
      sortedEventsMaintenance.put(utcTime, newEvent);
   }

   @Override
   public TourTagMaintenance clone() {

      TourTagMaintenance newTourTagMaintenance = null;

      try {
         newTourTagMaintenance = (TourTagMaintenance) super.clone();
         newTourTagMaintenance.sortedEventsMaintenance = new TreeMap<>();
         newTourTagMaintenance.sortedEventsMaintenance.putAll(sortedEventsMaintenance);
      } catch (final CloneNotSupportedException e) {
         e.printStackTrace();
      }

      return newTourTagMaintenance;
   }

   public int getExtraHourUsed() {
      return extraHourUsed;
   }

   public int getExtraMonthUsage() {
      return extraMonthUsage;
   }

   public TreeMap<Long, MaintenanceEvent> getSortedEventsMaintenance() {
      return sortedEventsMaintenance;
   }

   public void setExtraHourUsed(final int extraHourUsed) {
      this.extraHourUsed = extraHourUsed;
   }

   public void setExtraMonthUsage(final int extraMonthUsage) {
      this.extraMonthUsage = extraMonthUsage;
   }

   public void setSortedEventsMaintenance(final TreeMap<Long, MaintenanceEvent> sorted) {
      this.sortedEventsMaintenance = sorted;
   }
}
