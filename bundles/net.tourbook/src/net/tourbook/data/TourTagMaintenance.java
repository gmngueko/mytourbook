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

public class TourTagMaintenance implements Serializable {

   /**
    *
    */
   private static final long                serialVersionUID = 314756931007979108L;

   private int                              extraHourUsed;
   private int                              extraMonthUsage;

   private TreeMap<Long, MaintenanceEvent> sorted           = new TreeMap<>();

   public class MaintenanceEvent implements Serializable {

      /**
       *
       */
      private static final long serialVersionUID = -3716703585072557410L;

      public long               eventEpochTime;
      public float              cost;
      public String             notes;
      public float              metersTotalUsed;
      public long               secondsTotalUsed;
   }

   public void addEvent(final Long utcTime, final MaintenanceEvent newEvent) {
      sorted.put(utcTime, newEvent);
   }

   public int getExtraHourUsed() {
      return extraHourUsed;
   }

   public int getExtraMonthUsage() {
      return extraMonthUsage;
   }

   public TreeMap<Long, MaintenanceEvent> getSorted() {
      return sorted;
   }

   public void setExtraHourUsed(final int extraHourUsed) {
      this.extraHourUsed = extraHourUsed;
   }

   public void setExtraMonthUsage(final int extraMonthUsage) {
      this.extraMonthUsage = extraMonthUsage;
   }

   public void setSorted(final TreeMap<Long, MaintenanceEvent> sorted) {
      this.sorted = sorted;
   }
}
