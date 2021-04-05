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

public class TourTagMaintenance implements Cloneable, Serializable {

   /**
    *
    */
   private static final long                serialVersionUID = 314756931007979108L;

   private int                             extraHourUsed           = 0;
   private int                             extraLifeMonthUsage         = 0;

   private TreeMap<Long, MaintenanceEvent> sortedEventsMaintenance           = new TreeMap<>();

   public MaintenanceEvent addEvent(final Long utcTime, final MaintenanceEvent newEvent) {
      return sortedEventsMaintenance.put(utcTime, newEvent);
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

   public int getExtraLifeMonthUsage() {
      return extraLifeMonthUsage;
   }

   public TreeMap<Long, MaintenanceEvent> getSortedEventsMaintenance() {
      return sortedEventsMaintenance;
   }

   public MaintenanceEvent removeEvent(final Long utcTime) {
      return sortedEventsMaintenance.remove(utcTime);
   }

   public void setExtraHourUsed(final int extraHourUsed) {
      this.extraHourUsed = extraHourUsed;
   }

   public void setExtraLifeMonthUsage(final int extraMonthUsage) {
      this.extraLifeMonthUsage = extraMonthUsage;
   }

   public void setSortedEventsMaintenance(final TreeMap<Long, MaintenanceEvent> sorted) {
      this.sortedEventsMaintenance = sorted;
   }
}
