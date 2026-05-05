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

import net.tourbook.common.UI;

public class TourTagMaintenance implements Cloneable, Serializable {

   /**
    *
    */
   private static final long                serialVersionUID = 314756931007979108L;


   private int                             extraHourUsed           = 0;
   private int                             extraLifeMonthUsage         = 0;

   private Float                           expectedLifeKilometers   = null;
   private Float                           expectedLifeHours        = null;

   private String                          purchasePrice            = UI.EMPTY_STRING;
   private String                          purchaseLocation         = UI.EMPTY_STRING;
   private long                            purchaseDateEpochSeconds = 0;

   private Float                           scheduleDistanceMeters   = null;
   private Integer                         scheduleTimeSpanSeconds  = null;
   private Float                           scheduleLifeMonths       = null;

   private String                          brand                    = UI.EMPTY_STRING;
   private String                          model                    = UI.EMPTY_STRING;
   private String                          type                     = UI.EMPTY_STRING;
   private Float                           weightKilograms          = null;

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

   public String getBrand() {
      return brand;
   }

   public Float getExpectedLifeHours() {
      return expectedLifeHours;
   }

   public Float getExpectedLifeKilometers() {
      return expectedLifeKilometers;
   }

   public int getExtraHourUsed() {
      return extraHourUsed;
   }

   public int getExtraLifeMonthUsage() {
      return extraLifeMonthUsage;
   }

   public String getModel() {
      return model;
   }

   public long getPurchaseDateEpochSeconds() {
      return purchaseDateEpochSeconds;
   }

   public String getPurchaseLocation() {
      return purchaseLocation;
   }

   public String getPurchasePrice() {
      return purchasePrice;
   }

   public Float getScheduleDistanceMeters() {
      return scheduleDistanceMeters;
   }

   public Float getScheduleLifeMonths() {
      return scheduleLifeMonths;
   }

   public Integer getScheduleTimeSpanSeconds() {
      return scheduleTimeSpanSeconds;
   }

   public TreeMap<Long, MaintenanceEvent> getSortedEventsMaintenance() {
      return sortedEventsMaintenance;
   }

   public String getType() {
      return type;
   }

   public Float getWeightKilograms() {
      return weightKilograms;
   }

   public MaintenanceEvent removeEvent(final Long utcTime) {
      return sortedEventsMaintenance.remove(utcTime);
   }

   public void setBrand(final String brand) {
      this.brand = brand;
   }

   public void setExpectedLifeHours(final Float expectedLifeHours) {
      this.expectedLifeHours = expectedLifeHours;
   }

   public void setExpectedLifeKilometers(final Float expectedLifeKilometers) {
      this.expectedLifeKilometers = expectedLifeKilometers;
   }

   public void setExtraHourUsed(final int extraHourUsed) {
      this.extraHourUsed = extraHourUsed;
   }

   public void setExtraLifeMonthUsage(final int extraMonthUsage) {
      this.extraLifeMonthUsage = extraMonthUsage;
   }

   public void setModel(final String model) {
      this.model = model;
   }

   public void setPurchaseDateEpochSeconds(final long purchaseDateEpochSeconds) {
      this.purchaseDateEpochSeconds = purchaseDateEpochSeconds;
   }

   public void setPurchaseLocation(final String purchaseLocation) {
      this.purchaseLocation = purchaseLocation;
   }

   public void setPurchasePrice(final String purchasePrice) {
      this.purchasePrice = purchasePrice;
   }

   public void setScheduleDistanceMeters(final Float scheduleDistanceMeters) {
      this.scheduleDistanceMeters = scheduleDistanceMeters;
   }

   public void setScheduleLifeMonths(final Float scheduleLifeMonths) {
      this.scheduleLifeMonths = scheduleLifeMonths;
   }

   public void setScheduleTimeSpanSeconds(final Integer scheduleTimeSpanSeconds) {
      this.scheduleTimeSpanSeconds = scheduleTimeSpanSeconds;
   }

   public void setSortedEventsMaintenance(final TreeMap<Long, MaintenanceEvent> sorted) {
      this.sortedEventsMaintenance = sorted;
   }

   public void setType(final String type) {
      this.type = type;
   }

   public void setWeightKilograms(final Float weightKilograms) {
      this.weightKilograms = weightKilograms;
   }
}
