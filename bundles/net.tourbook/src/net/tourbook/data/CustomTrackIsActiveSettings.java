/*******************************************************************************
 * Copyright (C) 2022 Gervais-Martial Ngueko
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

public class CustomTrackIsActiveSettings implements Serializable{

   /**
    *
    */
   private static final long serialVersionUID  = -6831787741415837672L;

   public float              minDistanceMeter  = 0;

   public float              minSpeedKmPerHour = 0;
   public float              minCadenceRpm     = 0;
   public float              minPowerWatt      = 0;
   public void copy(final CustomTrackIsActiveSettings newSettings) {
      minDistanceMeter = newSettings.minDistanceMeter;
      minSpeedKmPerHour = newSettings.minSpeedKmPerHour;
      minCadenceRpm = newSettings.minCadenceRpm;
      minPowerWatt = newSettings.minPowerWatt;
   }

   public String getIsActive(final float avgPower, final float avgSpeed, final float avgCadence, final float distance) {
      String result = "";

      if (minDistanceMeter == 0 && minSpeedKmPerHour == 0 && minCadenceRpm == 0 && minPowerWatt == 0) {
         result = "N/A";
      } else {
         if (minCadenceRpm > 0 && avgCadence < minCadenceRpm) {
            result = "false";
         } else if (minSpeedKmPerHour > 0 && avgSpeed < minSpeedKmPerHour) {
            result = "false";
         } else if (minPowerWatt > 0 && avgPower < minPowerWatt) {
            result = "false";
         } else if (minDistanceMeter > 0 && distance < minDistanceMeter) {
            result = "false";
         } else {
            result = "true";
         }
      }
      return result;
   }

   @Override
   public String toString() {
      return "CustomTrackIsActiveSettings [minDistanceMeter=" + minDistanceMeter + ", minSpeedKmPerHour=" + minSpeedKmPerHour + ", minCadenceRpm="
            + minCadenceRpm + ", minPowerWatt=" + minPowerWatt + "]";
   }
}
