/*******************************************************************************
 * Copyright (C) 2023, 2025 Wolfgang Schramm and Contributors
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.tourbook.common.UI;
import net.tourbook.database.TourDatabase;

/**
 */
@Entity
public class DeviceSensorValue implements Serializable {

   private static final long    serialVersionUID     = 1L;

   private static final char    NL                   = UI.NEW_LINE;

   /**
    * Create a unique id to identify imported sensor values
    */
   private static AtomicInteger _createCounter       = new AtomicInteger();

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long                 sensorValueId        = TourDatabase.ENTITY_IS_NOT_SAVED;

   /**
    * Tour start time
    */
   private long                 tourStartTime;
   private long                 tourEndTime;

   /**
    * Battery level: 0...100%
    */
   private short                batteryLevel_Start   = -1;
   private short                batteryLevel_End     = -1;

   /**
    * Defined in {@link com.garmin.fit.BatteryStatus}
    */
   private short                batteryStatus_Start  = -1;
   private short                batteryStatus_End    = -1;

   private float                batteryVoltage_Start = -1;
   private float                batteryVoltage_End   = -1;

   @ManyToOne(optional = false)
   private DeviceSensor         deviceSensor;

   @ManyToOne(optional = false)
   private TourData             tourData;

   /**
    * Is used to identify a device by it's device index according to the FIT "device index" field
    */
   @Transient
   private int                  _deviceIndex         = -1;

   @Transient
   private long                 _createId            = 0;

   /**
    * This constructor is needed for Hibernate
    */
   public DeviceSensorValue() {}

   public DeviceSensorValue(final DeviceSensor sensor) {

      _createId = _createCounter.incrementAndGet();

      deviceSensor = sensor;
   }

   /**
    * Used for MT import/export
    *
    * @param tourData
    */
   public DeviceSensorValue(final TourData tourData) {

      this.tourData = tourData;

      _createId = _createCounter.incrementAndGet();
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

      final DeviceSensorValue other = (DeviceSensorValue) obj;

      if (_createId == 0) {

         // sensor value is from the database
         if (sensorValueId != other.sensorValueId) {
            return false;
         }

      } else {

         // sensor value is create
         if (_createId != other._createId) {
            return false;
         }
      }

      return true;
   }

   public float getBatteryLevel_End() {
      return batteryLevel_End;
   }

   public float getBatteryLevel_Start() {
      return batteryLevel_Start;
   }

   public float getBatteryStatus_End() {
      return batteryStatus_End;
   }

   public float getBatteryStatus_Start() {
      return batteryStatus_Start;
   }

   public float getBatteryVoltage_End() {
      return batteryVoltage_End;
   }

   public float getBatteryVoltage_Start() {
      return batteryVoltage_Start;
   }

   /**
    * @return Returns the device index or -1 when the index is not yet set
    */
   public int getDeviceIndex() {
      return _deviceIndex;
   }

   public DeviceSensor getDeviceSensor() {
      return deviceSensor;
   }

   /**
    * @return Returns the primary key for a {@link DeviceSensorValue} entity
    */
   public long getSensorValueId() {
      return sensorValueId;
   }

   public long getTourEndTime() {
      return tourEndTime;
   }

   public long getTourStartTime() {
      return tourStartTime;
   }

   @Override
   public int hashCode() {
      return Objects.hash(sensorValueId);
   }

   /**
    * @return Returns <code>true</code> when any values are available
    */
   public boolean isDataAvailable() {

      if (batteryLevel_Start != -1 || batteryLevel_End != -1
            || batteryStatus_Start != -1 || batteryStatus_End != -1
            || batteryVoltage_Start != -1 || batteryVoltage_End != -1) {

         return true;
      }

      return false;
   }

   /**
    * Used for MT import/export
    *
    * @param batteryLevel_End
    */
   public void setBatteryLevel_End(final short batteryLevel_End) {
      this.batteryLevel_End = batteryLevel_End;
   }

   /**
    * Used for MT import/export
    *
    * @param batteryLevel_Start
    */
   public void setBatteryLevel_Start(final short batteryLevel_Start) {
      this.batteryLevel_Start = batteryLevel_Start;
   }

   /**
    * Used for MT import/export
    *
    * @param batteryStatus_End
    */
   public void setBatteryStatus_End(final short batteryStatus_End) {
      this.batteryStatus_End = batteryStatus_End;
   }

   /**
    * Used for MT import/export
    *
    * @param batteryStatus_Start
    */
   public void setBatteryStatus_Start(final short batteryStatus_Start) {
      this.batteryStatus_Start = batteryStatus_Start;
   }

   /**
    * Used for MT import/export
    *
    * @param batteryVoltage_End
    */
   public void setBatteryVoltage_End(final float batteryVoltage_End) {
      this.batteryVoltage_End = batteryVoltage_End;
   }

   /**
    * Used for MT import/export
    *
    * @param batteryVoltage_Start
    */
   public void setBatteryVoltage_Start(final float batteryVoltage_Start) {
      this.batteryVoltage_Start = batteryVoltage_Start;
   }

   public void setDeviceIndex(final int deviceIndex) {
      _deviceIndex = deviceIndex;
   }

   public void setDeviceSensor(final DeviceSensor deviceSensor) {
      this.deviceSensor = deviceSensor;
   }

   public void setTourData(final TourData tourData) {
      this.tourData = tourData;
   }

   public void setTourTime_End(final long tourTime_End) {
      this.tourEndTime = tourTime_End;
   }

   public void setTourTime_Start(final long tourTime_Start) {
      this.tourStartTime = tourTime_Start;
   }

   public void setupDeepClone(final TourData tourDataFromClone) {

      _createId = _createCounter.incrementAndGet();

      sensorValueId = TourDatabase.ENTITY_IS_NOT_SAVED;

      tourData = tourDataFromClone;
   }

   /**
    * This method is called in the MT UI in the "Tour Data" view
    */
   @Override
   public String toString() {

      return "DeviceSensorValue" + NL //                                            //$NON-NLS-1$

            + "   sensorValueId        = " + sensorValueId + NL //                  //$NON-NLS-1$
            + "   batteryLevel_Start   = " + batteryLevel_Start + NL //             //$NON-NLS-1$
            + "   batteryLevel_End     = " + batteryLevel_End + NL //               //$NON-NLS-1$
            + "   batteryStatus_Start  = " + batteryStatus_Start + NL //             //$NON-NLS-1$
            + "   batteryStatus_End    = " + batteryStatus_End + NL //              //$NON-NLS-1$
            + "   batteryVoltage_Start = " + batteryVoltage_Start + NL //           //$NON-NLS-1$
            + "   batteryVoltage_End   = " + batteryVoltage_End + NL //             //$NON-NLS-1$
            + "   deviceSensor         = " + deviceSensor + NL //                   //$NON-NLS-1$
//          + "   tourData             = " + tourData + NL //                       //$NON-NLS-1$

      ;
   }

}
