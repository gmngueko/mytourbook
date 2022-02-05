/*******************************************************************************
 * Copyright (C) 2022 Gervais-Martial Ngueko and Contributors
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
package net.tourbook.ui.views.tourCustomFields;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;

import org.eclipse.jface.preference.IPreferenceStore;

public class CustomFieldViewItem {

   static final String SQL_SUM_COLUMNS;

   static {

      SQL_SUM_COLUMNS = UI.EMPTY_STRING

            + "SUM(tourDistance)," //              0   //$NON-NLS-1$
            + "SUM(TourDeviceTime_Elapsed)," //    1   //$NON-NLS-1$
            + "SUM(tourComputedTime_Moving)," //   2   //$NON-NLS-1$
            + "SUM(tourAltUp)," //                 3   //$NON-NLS-1$
            + "SUM(tourAltDown)," //               4   //$NON-NLS-1$

            + "MAX(maxPulse)," //                  5   //$NON-NLS-1$
            + "MAX(maxAltitude)," //               6   //$NON-NLS-1$
            + "MAX(maxSpeed)," //                  7   //$NON-NLS-1$

            + "AVG( CASE WHEN AVGPULSE = 0         THEN NULL ELSE AVGPULSE END)," //                                    8   //$NON-NLS-1$
            + "AVG( CASE WHEN AVGCADENCE = 0       THEN NULL ELSE AVGCADENCE END )," //                                 9   //$NON-NLS-1$
            + "AVG( CASE WHEN AvgTemperature = 0   THEN NULL ELSE DOUBLE(AvgTemperature) / TemperatureScale END )," //  10   //$NON-NLS-1$

            + "SUM(TourDeviceTime_Recorded)," //    11   //$NON-NLS-1$

            + "MAX(power_Max)," //                  12   //$NON-NLS-1$
            + "AVG( CASE WHEN POWER_AVG = 0         THEN NULL ELSE POWER_AVG END)," //                                    13   //$NON-NLS-1$
            + "AVG( CASE WHEN POWER_NORMALIZED = 0         THEN NULL ELSE POWER_NORMALIZED END)," //                                    14   //$NON-NLS-1$

            + "AVG(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN NULL ELSE valueFloat END)," //                  15   //$NON-NLS-1$
            + "MIN(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN NULL ELSE valueFloat END)," //               16   //$NON-NLS-1$
            + "MAX(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN NULL ELSE valueFloat END)," //                  17   //$NON-NLS-1$
            + "SUM(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN NULL ELSE valueFloat END)," //                  18   //$NON-NLS-1$

            + "AVG(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN INTEGER(valueString) ELSE NULL END)," //                  19   //$NON-NLS-1$
            + "MIN(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN INTEGER(valueString) ELSE NULL END)," //               20   //$NON-NLS-1$
            + "MAX(CASE WHEN FIELDTYPE = 'FIELD_DATE'  THEN INTEGER(valueString) ELSE NULL END)," //                  21   //$NON-NLS-1$

            // tour counter
            + "SUM(1)" //                          22   //$NON-NLS-1$
      ;

   }
   protected final IPreferenceStore _prefStore = TourbookPlugin.getPrefStore();

   String                           refId;

   long                             colDistance;

   long                             colElapsedTime;

   long                             colRecordedTime;
   long                             colMovingTime;
   long                             colPausedTime;
   long                             colAltitudeUp;

   long                             colAltitudeDown;
   float                            colMaxSpeed;

   long                             colMaxPulse;
   long                             colMaxAltitude;
   float                            colAvgSpeed;

   float                            colAvgPace;
   float                            colAvgPulse;

   float                            colAvgCadence;
   float                            colAvgTemperature;
   long                             colMaxPower;

   float                            colAvgPower;
   float                            colAvgPowerNormalized;

   float                            colAvgValueFloat;
   float                            colMinValueFloat;
   float                            colMaxValueFloat;
   float                            colSumValueFloat;

   long                             colAvgValueDate;
   long                             colMinValueDate;

   long                             colMaxValueDate;

   long                             colTourCounter;

   public CustomFieldViewItem() {
      super();
   }

   public long getColAltitudeDown() {
      return colAltitudeDown;
   }

   public long getColAltitudeUp() {
      return colAltitudeUp;
   }

   public float getColAvgCadence() {
      return colAvgCadence;
   }
   public float getColAvgPace() {
      return colAvgPace;
   }

   public float getColAvgPower() {
      return colAvgPower;
   }

   public float getColAvgPowerNormalized() {
      return colAvgPowerNormalized;
   }

   public float getColAvgPulse() {
      return colAvgPulse;
   }

   public float getColAvgSpeed() {
      return colAvgSpeed;
   }

   public float getColAvgTemperature() {
      return colAvgTemperature;
   }

   public long getColAvgValueDate() {
      return colAvgValueDate;
   }
   public float getColAvgValueFloat() {
      return colAvgValueFloat;
   }
   public long getColDistance() {
      return colDistance;
   }

   public long getColElapsedTime() {
      return colElapsedTime;
   }

   public long getColMaxAltitude() {
      return colMaxAltitude;
   }


   public long getColMaxPower() {
      return colMaxPower;
   }

   public long getColMaxPulse() {
      return colMaxPulse;
   }

   public float getColMaxSpeed() {
      return colMaxSpeed;
   }

   public long getColMaxValueDate() {
      return colMaxValueDate;
   }

   public float getColMaxValueFloat() {
      return colMaxValueFloat;
   }

   public long getColMinValueDate() {
      return colMinValueDate;
   }

   public float getColMinValueFloat() {
      return colMinValueFloat;
   }

   public long getColMovingTime() {
      return colMovingTime;
   }

   public long getColPausedTime() {
      return colPausedTime;
   }

   public long getColRecordedTime() {
      return colRecordedTime;
   }

   public float getColSumValueFloat() {
      return colSumValueFloat;
   }

   public long getColTourCounter() {
      return colTourCounter;
   }

   public String getDateValue(final CustomField customField, final Long inputValue) {
      String value ="";

      if (customField.getFieldType().compareTo(CustomFieldType.FIELD_DATE) == 0) {
         try {
            final Long epochMilli = Long.valueOf(inputValue) * 1000;
            final ZoneId zoneId = ZoneId.systemDefault();

            ZonedDateTime zonedDateTime;
            final Instant startInstant = Instant.ofEpochMilli(epochMilli);
            zonedDateTime = ZonedDateTime.ofInstant(startInstant, zoneId);

            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss XXX");
            value = zonedDateTime.format(formatter);
         } catch (final Exception e) {
            //fallback
            System.out.println(e.getMessage());
            value = "Error";
         }

      } else {
         value = "Error";
      }
      return value;
   }

   public String getRefId() {
      return refId;
   }

   public String getValueAsString(final CustomField customField, final Float inputValue, final String inputString) {
      String value = "";
      if (inputString != null) {
         value = inputString;
      } else if (inputValue != null) {
         if (customField.getFieldType().compareTo(CustomFieldType.FIELD_DURATION) == 0) {
            final Long sec = inputValue.longValue();
            final long second = sec % 60;
            long minute = sec / 60;
            if (minute >= 60) {
               final long hour = minute / 60;
               minute %= 60;
               value = String.format("%02d:%02d:%02d", hour, minute, second);
            } else {
               final long hour = 0;
               value = String.format("%02d:%02d:%02d", hour, minute, second);
            }
         } else if (customField.getFieldType().compareTo(CustomFieldType.FIELD_STRING) == 0) {
            value = "N/A";
         } else {
            value = String.valueOf(inputValue);
         }
      }
      return value;
   }

   /**
    * Read sum totals from the database for the dataSerieItem
    *
    * @param customFieldItem
    */
   public void readDataSerieTotals(final CustomField customFieldItem) {

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         //final SQLFilter sqlFilter = new SQLFilter();

         /*
          * get tags
          */
         final String sql = UI.EMPTY_STRING
               //
               + ("SELECT " + SQL_SUM_COLUMNS) //$NON-NLS-1$
               + (" FROM " + TourDatabase.TABLE_CUSTOM_FIELD_VALUE + " jtblFieldValue") //$NON-NLS-1$ //$NON-NLS-2$

               // get data for a tour
               + (" LEFT OUTER JOIN " + TourDatabase.TABLE_TOUR_DATA + " TourData ON ") //$NON-NLS-1$ //$NON-NLS-2$
               + (" jtblFieldValue.TourData_tourId = TourData.tourId") //$NON-NLS-1$

               // get data for a customField
               + (" LEFT OUTER JOIN " + TourDatabase.TABLE_CUSTOM_FIELD + " CustomField ON ") //$NON-NLS-1$ //$NON-NLS-2$
               + (" jtblFieldValue.CUSTOMFIELD_FieldID = CustomField.FieldID") //$NON-NLS-1$

               + " WHERE jtblFieldValue.CUSTOMFIELD_FieldID = ?" //$NON-NLS-1$
         ;//+ sqlFilter.getWhereClause();

         final PreparedStatement statement = conn.prepareStatement(sql);
         statement.setLong(1, customFieldItem.getFieldId());
         //sqlFilter.setParameters(statement, 2);

         refId = customFieldItem.getRefId();

         final ResultSet result = statement.executeQuery();
         while (result.next()) {
            this.readSumColumnData(result, 1);
         }

      } catch (final SQLException e) {
         net.tourbook.ui.UI.showSQLException(e);
      }
   }

   void readDefaultColumnData(final ResultSet result, final int startIndex) throws SQLException {

      colDistance = result.getLong(startIndex + 0);

      colElapsedTime = result.getLong(startIndex + 1);
      colMovingTime = result.getLong(startIndex + 2);
      colPausedTime = colElapsedTime - colMovingTime;

      colAltitudeUp = result.getLong(startIndex + 3);
      colAltitudeDown = result.getLong(startIndex + 4);

      colMaxPulse = result.getLong(startIndex + 5);
      colMaxAltitude = result.getLong(startIndex + 6);
      colMaxSpeed = result.getFloat(startIndex + 7);

      colAvgPulse = result.getFloat(startIndex + 8);
      colAvgCadence = result.getFloat(startIndex + 9);
      colAvgTemperature = result.getFloat(startIndex + 10);

      colRecordedTime = result.getLong(startIndex + 11);

      colAvgPower = result.getFloat(startIndex + 13);
      colMaxPower = result.getLong(startIndex + 12);
      colAvgPowerNormalized = result.getFloat(startIndex + 14);

      final boolean isPaceAndSpeedFromRecordedTime = _prefStore.getBoolean(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME);
      final long time = isPaceAndSpeedFromRecordedTime ? colRecordedTime : colMovingTime;
      // prevent divide by 0
      colAvgSpeed = (time == 0 ? 0 : 3.6f * colDistance / time);
      colAvgPace = colDistance == 0 ? 0 : time * 1000f / colDistance;

      colAvgValueFloat = result.getFloat(startIndex + 15);
      colMinValueFloat = result.getFloat(startIndex + 16);
      colMaxValueFloat = result.getFloat(startIndex + 17);
      colSumValueFloat = result.getFloat(startIndex + 18);

      colAvgValueDate = result.getLong(startIndex + 19);
      colMinValueDate = result.getLong(startIndex + 20);
      colMaxValueDate = result.getLong(startIndex + 21);

      if (UI.IS_SCRAMBLE_DATA) {

         colDistance = UI.scrambleNumbers(colDistance);

         colElapsedTime = UI.scrambleNumbers(colElapsedTime);
         colRecordedTime = UI.scrambleNumbers(colRecordedTime);
         colMovingTime = UI.scrambleNumbers(colMovingTime);
         colPausedTime = UI.scrambleNumbers(colPausedTime);

         colAltitudeUp = UI.scrambleNumbers(colAltitudeUp);
         colAltitudeDown = UI.scrambleNumbers(colAltitudeDown);

         colMaxPulse = UI.scrambleNumbers(colMaxPulse);
         colMaxAltitude = UI.scrambleNumbers(colMaxAltitude);
         colMaxSpeed = UI.scrambleNumbers(colMaxSpeed);

         colAvgPulse = UI.scrambleNumbers(colAvgPulse);
         colAvgCadence = UI.scrambleNumbers(colAvgCadence);
         colAvgTemperature = UI.scrambleNumbers(colAvgTemperature);

         colAvgSpeed = UI.scrambleNumbers(colAvgSpeed);
         colAvgPace = UI.scrambleNumbers(colAvgPace);

         colAvgValueFloat = UI.scrambleNumbers(colAvgValueFloat);
         colMinValueFloat = UI.scrambleNumbers(colMinValueFloat);
         colMaxValueFloat = UI.scrambleNumbers(colMaxValueFloat);
         colSumValueFloat = UI.scrambleNumbers(colSumValueFloat);

         colAvgValueDate = UI.scrambleNumbers(colAvgValueDate);
         colMinValueDate = UI.scrambleNumbers(colMinValueDate);
         colMaxValueDate = UI.scrambleNumbers(colMaxValueDate);

      }
   }

   public void readSumColumnData(final ResultSet result, final int startIndex) throws SQLException {

      readDefaultColumnData(result, startIndex);

      colTourCounter = result.getLong(startIndex + 22);
   }

   public void setColAltitudeDown(final long colAltitudeDown) {
      this.colAltitudeDown = colAltitudeDown;
   }

   public void setColAltitudeUp(final long colAltitudeUp) {
      this.colAltitudeUp = colAltitudeUp;
   }

   public void setColAvgCadence(final float colAvgCadence) {
      this.colAvgCadence = colAvgCadence;
   }

   public void setColAvgPace(final float colAvgPace) {
      this.colAvgPace = colAvgPace;
   }

   public void setColAvgPower(final float colAvgPower) {
      this.colAvgPower = colAvgPower;
   }

   public void setColAvgPowerNormalized(final float colAvgPowerNormalized) {
      this.colAvgPowerNormalized = colAvgPowerNormalized;
   }

   public void setColAvgPulse(final float colAvgPulse) {
      this.colAvgPulse = colAvgPulse;
   }

   public void setColAvgSpeed(final float colAvgSpeed) {
      this.colAvgSpeed = colAvgSpeed;
   }

   public void setColAvgTemperature(final float colAvgTemperature) {
      this.colAvgTemperature = colAvgTemperature;
   }

   public void setColAvgValueDate(final long colAvgValueDate) {
      this.colAvgValueDate = colAvgValueDate;
   }

   public void setColAvgValueFloat(final float colAvgValueFloat) {
      this.colAvgValueFloat = colAvgValueFloat;
   }

   public void setColDistance(final long colDistance) {
      this.colDistance = colDistance;
   }

   public void setColElapsedTime(final long colElapsedTime) {
      this.colElapsedTime = colElapsedTime;
   }

   public void setColMaxAltitude(final long colMaxAltitude) {
      this.colMaxAltitude = colMaxAltitude;
   }

   public void setColMaxPower(final long colMaxPower) {
      this.colMaxPower = colMaxPower;
   }

   public void setColMaxPulse(final long colMaxPulse) {
      this.colMaxPulse = colMaxPulse;
   }

   public void setColMaxSpeed(final float colMaxSpeed) {
      this.colMaxSpeed = colMaxSpeed;
   }

   public void setColMaxValueDate(final long colMaxValueDate) {
      this.colMaxValueDate = colMaxValueDate;
   }

   public void setColMaxValueFloat(final float colMaxValueFloat) {
      this.colMaxValueFloat = colMaxValueFloat;
   }

   public void setColMinValueDate(final long colMinValueDate) {
      this.colMinValueDate = colMinValueDate;
   }

   public void setColMinValueFloat(final float colMinValueFloat) {
      this.colMinValueFloat = colMinValueFloat;
   }

   public void setColMovingTime(final long colMovingTime) {
      this.colMovingTime = colMovingTime;
   }

   public void setColPausedTime(final long colPausedTime) {
      this.colPausedTime = colPausedTime;
   }

   public void setColRecordedTime(final long colRecordedTime) {
      this.colRecordedTime = colRecordedTime;
   }

   public void setColSumValueFloat(final float colSumValueFloat) {
      this.colSumValueFloat = colSumValueFloat;
   }

   public void setColTourCounter(final long colTourCounter) {
      this.colTourCounter = colTourCounter;
   }

   public void setRefId(final String refId) {
      this.refId = refId;
   }

}
