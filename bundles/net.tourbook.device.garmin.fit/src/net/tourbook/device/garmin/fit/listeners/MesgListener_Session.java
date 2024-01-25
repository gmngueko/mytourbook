/*******************************************************************************
 * Copyright (C) 2005, 2020 Wolfgang Schramm and Contributors
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
package net.tourbook.device.garmin.fit.listeners;

import com.garmin.fit.DateTime;
import com.garmin.fit.DeveloperField;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import com.garmin.fit.Sport;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.tourbook.common.UI;
import net.tourbook.common.time.DateUtil;
import net.tourbook.common.time.TimeTools;
import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;
import net.tourbook.data.CustomFieldValue;
import net.tourbook.data.TimeData;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.device.garmin.fit.FitData;
import net.tourbook.device.garmin.fit.FitDataReaderException;
import net.tourbook.importdata.RawDataManager;

import org.joda.time.Duration;

public class MesgListener_Session extends AbstractMesgListener implements SessionMesgListener {

   public MesgListener_Session(final FitData fitData) {
      super(fitData);
   }

   private CustomField addCustomField(final CustomField field) {

      if (field == null) {
         return null;
      }
      final CustomField customField;
      customField = RawDataManager.createCustomField(field.getFieldName(),
            field.getRefId(),
            field.getUnit(),
            field.getFieldType(),
            field.getDescription());

      final CustomField customFieldToCompare = new CustomField();
      customFieldToCompare.setFieldName(field.getFieldName());
      customFieldToCompare.setFieldType(customField.getFieldType());
      customFieldToCompare.setRefId(field.getRefId());
      if (customField.equals2(customFieldToCompare) == false) {
         //TODO might not be necessary to update CustomFields
         //existing CustomFields must sray the way they are
         //User might have modified on purpose !!!
         if (customField.getFieldId() == TourDatabase.ENTITY_IS_NOT_SAVED) {

            /*
             * Nothing to do, customField will be saved when a tour is saved which contains
             * this customField
             * in
             * net.tourbook.database.TourDatabase.checkUnsavedTransientInstances_CustomFields(
             * )
             */

         } else {

            /*
             * Notify post process to update the customField in the db
             */

            final ConcurrentHashMap<String, CustomField> allCustomFieldsToBeUpdated = fitData.getImportState_Process()
                  .getAllCustomFieldsToBeUpdated();

            allCustomFieldsToBeUpdated.put(customField.getRefId(), customField);
         }
      }

      return customField;
   }//end addCustomField

   private CustomFieldValue addCustomFieldValue(final Object fieldValue, final CustomField customField, final TourData tourData) {

      if (customField == null) {
         return null;
      }

      //second create CustomFieldValue's
      final CustomFieldValue customFieldValue = new CustomFieldValue(customField);
      CustomFieldType newCustomFieldType;

      final String customFieldName = customField.getFieldName();
      final String customFieldUnit = customField.getUnit();

      //String developperFieldString = "";

      /*
       * Set CustomFieldValue into tour data
       */
      customFieldValue.setTourStartTime(tourData.getTourStartTimeMS());
      customFieldValue.setTourEndTime(tourData.getTourEndTimeMS());
      customFieldValue.setTourData(tourData);

      //final Object fieldValue = devField.getValue();
      //developperFieldString += customFieldName + "[" + customFieldUnit + "] " + UI.SYMBOL_EQUAL;
      //developperFieldString += " " + "\"" + fieldValue + "\"";
      if (fieldValue instanceof Float) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((Float) fieldValue).floatValue());
      } else if (fieldValue instanceof Integer) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((Integer) fieldValue).floatValue());

      } else if (fieldValue instanceof Long) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((Long) fieldValue).floatValue());

      } else if (fieldValue instanceof String) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //TODO check if string contains a duration(hhh:mm:sec) or a datetime(YYYY-MM-DDThh:mm:ss)
         if (DateUtil.isValideDuration((String) fieldValue)) {
            try {
               final Duration duration = DateUtil.parseDuration((String) fieldValue);
               //customField.setFieldType(CustomFieldType.FIELD_DURATION);
               newCustomFieldType = CustomFieldType.FIELD_DURATION;
               customFieldValue.setValueFloat(((Long) duration.getStandardSeconds()).floatValue());
               customFieldValue.setValueString(null);
            } catch (final Exception e) {
               System.out.println(e.getMessage());
               //customField.setFieldType(CustomFieldType.FIELD_STRING);
               newCustomFieldType = CustomFieldType.FIELD_STRING;
               customFieldValue.setValueFloat(null);
               customFieldValue.setValueString(((String) fieldValue));
            }

         } else {
            //customField.setFieldType(CustomFieldType.FIELD_STRING);
            newCustomFieldType = CustomFieldType.FIELD_STRING;
            customFieldValue.setValueFloat(null);
            customFieldValue.setValueString(((String) fieldValue));

         }

      } else if (fieldValue instanceof Short) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((Short) fieldValue).floatValue());

      } else if (fieldValue instanceof Byte) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((Byte) fieldValue).floatValue());

      } else if (fieldValue instanceof Double) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((Double) fieldValue).floatValue());

      } else if (fieldValue instanceof BigInteger) {
         //developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
         //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
         newCustomFieldType = CustomFieldType.FIELD_NUMBER;
         customFieldValue.setValueString(null);
         customFieldValue.setValueFloat(((BigInteger) fieldValue).floatValue());

      } else {
         //developperFieldString += " {Unknown}";
         //customField.setFieldType(CustomFieldType.FIELD_STRING);
         newCustomFieldType = CustomFieldType.FIELD_STRING;
         customFieldValue.setValueFloat(null);
         customFieldValue.setValueString(((String) fieldValue));

      }
      if (customField.getFieldType().compareTo(CustomFieldType.NONE) == 0) {
         //this is a completly new customField
         customField.setFieldType(newCustomFieldType);
      } else {
         if (customField.getFieldType().compareTo(newCustomFieldType) != 0) {
            //create a new version with a new referenceid because the user already updated
            //the existing one with a different fieldType for good reason
            final CustomField customFieldCopy = RawDataManager.createCustomField(customFieldName,
                  "v2;" + customField.getRefId(), //$NON-NLS-1$
                  customFieldUnit,
                  newCustomFieldType,
                  customField.getDescriptionShort());
            customFieldValue.setCustomField(customFieldCopy);
         }
      }
      //developperFieldString += UI.NEW_LINE1;

      return customFieldValue;
   }

   @Override
   public void onMesg(final SessionMesg mesg) {

      fitData.setSessionIndex(mesg);

      final DateTime startTime = mesg.getStartTime();
      if (startTime == null) {
         throw new FitDataReaderException("Missing session start date"); //$NON-NLS-1$
      }

      final TourData tourData = fitData.getTourData();

// since FIT SDK > 12 the tour start time is different with the records, therefore the tour start time is set later
//
// !!!!
//      This problem is corrected in FIT SDK 14.10 but it took me several days to investigate it
//    and then came the idea to check for a new FIT SDK which solved this problem.
// !!!!

      final ZonedDateTime tourStartTime = TimeTools.getZonedDateTime(startTime.getDate().getTime());

      fitData.setSessionStartTime(tourStartTime);
      tourData.setTourStartTime(tourStartTime);

      final Sport sport = mesg.getSport();
      if (sport != null) {
         final String sportName = sport.name().trim();
         tourData.setDeviceModeName(sportName);
         fitData.setSportname(sportName);
      }

      final Short avgHeartRate = mesg.getAvgHeartRate();
      if (avgHeartRate != null) {
         tourData.setAvgPulse(avgHeartRate);
      }

      final Short avgCadence = mesg.getAvgCadence();
      if (avgCadence != null) {
         tourData.setAvgCadence(avgCadence);
      }

      final Integer totalCalories = mesg.getTotalCalories();
      if (totalCalories != null) {

         // convert kcal -> cal
         tourData.setCalories(totalCalories * 1000);
      }

      final Float totalDistance = mesg.getTotalDistance();
      if (totalDistance != null) {
         tourData.setTourDistance(Math.round(totalDistance));
      }

      final Integer totalAscent = mesg.getTotalAscent();
      if (totalAscent != null) {
         tourData.setTourAltUp(totalAscent);
      }

      final Integer totalDescent = mesg.getTotalDescent();
      if (totalDescent != null) {
         tourData.setTourAltDown(totalDescent);
      }

      final Float totalElapsedTime = mesg.getTotalElapsedTime();
      if (totalElapsedTime != null) {
         tourData.setTourDeviceTime_Elapsed(Math.round(totalElapsedTime));
      }

      final Float totalTimerTime = mesg.getTotalTimerTime();
      if (totalTimerTime != null) {

         int roundedTotalTimerTime = Math.round(totalTimerTime);
         tourData.setTourComputedTime_Moving(roundedTotalTimerTime);

         final long tourDeviceTimeElapsed = tourData.getTourDeviceTime_Elapsed();
         if (roundedTotalTimerTime > tourDeviceTimeElapsed) {
            roundedTotalTimerTime = (int) tourDeviceTimeElapsed;
         }

         tourData.setTourDeviceTime_Recorded(roundedTotalTimerTime);
         tourData.setTourDeviceTime_Paused(tourData.getTourDeviceTime_Elapsed() - tourData.getTourDeviceTime_Recorded());
      }

      // -----------------------POWER -----------------------
      final Integer avgPower = mesg.getAvgPower();
      if (avgPower != null) {
         tourData.setPower_Avg(avgPower);
      }

      Integer maxPower = mesg.getMaxPower();
      if (maxPower != null) {
         tourData.setPower_Max(maxPower);
      }

      // Looking if the power was retrieved from the developer fields
      if (mesg.getMaxPower() == null && mesg.getAvgPower() == null) {
         final ArrayList<Float> powerDataList = new ArrayList<>();
         for (final TimeData timeData : fitData.getAllTimeData()) {
            if (timeData.power != Float.MIN_VALUE) {
               powerDataList.add(timeData.power);
            }

            if (tourData.getPower_DataSource() == null && timeData.powerDataSource != null) {
               tourData.setPower_DataSource(timeData.powerDataSource);
            }
         }

         if (powerDataList.size() > 0) {

            fitData.isComputeAveragePower = true;

            maxPower = (int) powerDataList.stream().mapToDouble(Float::doubleValue).max().getAsDouble();
            tourData.setPower_Max(maxPower);

            tourData.setIsStrideSensorPresent(true);
         }
      }

      final Integer normalizedPower = mesg.getNormalizedPower();
      if (normalizedPower != null) {
         tourData.setPower_Normalized(normalizedPower);
      }

      final Integer leftRightBalance = mesg.getLeftRightBalance();
      if (leftRightBalance != null) {
         final int maskRight = com.garmin.fit.LeftRightBalance100.RIGHT;//0x8000;
         final int maskValue = com.garmin.fit.LeftRightBalance100.MASK;//0x3FFF;
         final int valueLeft = (100 - (leftRightBalance & maskValue) / 100);
         final int isRight = leftRightBalance & maskRight;
         if (isRight != 0) {
            tourData.setPower_PedalLeftRightBalance(valueLeft);
         }
      }

      final Float avgLeftTorqueEffectiveness = mesg.getAvgLeftTorqueEffectiveness();
      if (avgLeftTorqueEffectiveness != null) {
         tourData.setPower_AvgLeftTorqueEffectiveness(avgLeftTorqueEffectiveness);
      }

      final Float avgRightTorqueEffectiveness = mesg.getAvgRightTorqueEffectiveness();
      if (avgRightTorqueEffectiveness != null) {
         tourData.setPower_AvgRightTorqueEffectiveness(avgRightTorqueEffectiveness);
      }

      final Float avgLeftPedalSmoothness = mesg.getAvgLeftPedalSmoothness();
      if (avgLeftPedalSmoothness != null) {
         tourData.setPower_AvgLeftPedalSmoothness(avgLeftPedalSmoothness);
      }

      final Float avgRightPedalSmoothness = mesg.getAvgRightPedalSmoothness();
      if (avgRightPedalSmoothness != null) {
         tourData.setPower_AvgRightPedalSmoothness(avgRightPedalSmoothness);
      }

      final Long totalWork = mesg.getTotalWork();
      if (totalWork != null) {
         tourData.setPower_TotalWork(totalWork);
      }

      final Float trainingStressScore = mesg.getTrainingStressScore();
      if (trainingStressScore != null) {
         tourData.setPower_TrainingStressScore(trainingStressScore);
      }

      final Float intensityFactor = mesg.getIntensityFactor();
      if (intensityFactor != null) {
         tourData.setPower_IntensityFactor(intensityFactor);
      }

      final Integer ftp = mesg.getThresholdPower();
      if (ftp != null) {
         tourData.setPower_FTP(ftp);
      }

      // -----------------------TRAINING -----------------------

      final Float totalTrainingEffect = mesg.getTotalTrainingEffect();
      if (totalTrainingEffect != null) {
         tourData.setTraining_TrainingEffect_Aerob(totalTrainingEffect);
      }

      final Float totalAnaerobicTrainingEffect = mesg.getTotalAnaerobicTrainingEffect();
      if (totalAnaerobicTrainingEffect != null) {
         tourData.setTraining_TrainingEffect_Anaerob(totalAnaerobicTrainingEffect);
      }

      //---Martial Static CustomField--------
      final Set<CustomFieldValue> allTourData_StaticCustomFieldValues = new HashSet<>();
      //final HashMap<String, CustomField> allTourData_StaticCustomField = new HashMap<>();
      String staticFieldString = "";

      final String activityProfile = mesg.getSportProfileName();
      if (activityProfile != null) {
         final CustomField myField = CustomFieldStatic.getMap().get(CustomFieldStatic.KEY_ACTIVITY_PROFILE);
         final CustomField myFieldDb = addCustomField(myField);
         CustomFieldValue myFieldValue = null;

         if(myFieldDb != null) {
            myFieldValue = addCustomFieldValue(activityProfile, myFieldDb, tourData);
         }

         if (myFieldValue != null) {
            allTourData_StaticCustomFieldValues.add(myFieldValue);
            staticFieldString += "activityProfile" + "[" + myField.getUnit() + "] " + UI.SYMBOL_EQUAL;
            staticFieldString += " " + "\"" + myFieldValue.getValueString() + "\"";
            staticFieldString += " {" + activityProfile.getClass().getSimpleName() + "}" + UI.NEW_LINE1;
         }
      }

      final Integer standingCount = mesg.getStandCount();
      if (standingCount != null) {
         final CustomField myField = CustomFieldStatic.getMap().get(CustomFieldStatic.KEY_STANDING_COUNT);
         final CustomField myFieldDb = addCustomField(myField);
         CustomFieldValue myFieldValue = null;

         if (myFieldDb != null) {
            myFieldValue = addCustomFieldValue(standingCount, myFieldDb, tourData);
         }

         if (myFieldValue != null) {
            allTourData_StaticCustomFieldValues.add(myFieldValue);
            staticFieldString += "standingCount" + "[" + myField.getUnit() + "] " + UI.SYMBOL_EQUAL;
            staticFieldString += " " + "\"" + myFieldValue.getValueString() + "\"";
            staticFieldString += " {" + standingCount.getClass().getSimpleName() + "}" + UI.NEW_LINE1;
         }
      }

      //final Integer sweatLoss = mesg.getFieldIntegerValue(178);
      final Object sweatLossObj = mesg.getFieldValue(178);
      //if (sweatLoss != null) {
      if (sweatLossObj != null) {
         final CustomField myField = CustomFieldStatic.getMap().get(CustomFieldStatic.KEY_SWEAT_LOSS);
         final CustomField myFieldDb = addCustomField(myField);
         CustomFieldValue myFieldValue = null;

         if (myFieldDb != null) {
            myFieldValue = addCustomFieldValue(sweatLossObj, myFieldDb, tourData);
         }

         if (myFieldValue != null) {
            allTourData_StaticCustomFieldValues.add(myFieldValue);
            staticFieldString += "sweatLoss" + "[" + myField.getUnit() + "] " + UI.SYMBOL_EQUAL;
            staticFieldString += " " + "\"" + myFieldValue.getValue() + "\"";
            staticFieldString += " {" + sweatLossObj.getClass().getSimpleName() + "}" + UI.NEW_LINE1;
         }
      }

      // -----------------------Developper field-----------------
      //will be put into Custom Fields

      final Set<CustomFieldValue> allTourData_CustomFieldValues = tourData.getCustomFieldValues();

      allTourData_CustomFieldValues.clear();

      String developperFieldString = "";
      for (final DeveloperField devField : mesg.getDeveloperFields()) {

         final String fieldName = devField.getName();
         if (!devField.isDefined() || !devField.isValid() || fieldName == null) {
            continue;
         }

         final String customFieldName = fieldName;// + UI.SYMBOL_UNDERSCORE + devField.getAppUUID().toString();
         final String customFieldId = fieldName + UI.SYMBOL_SEMICOLON + devField.getAppUUID().toString();
         String customFieldUnit = UI.EMPTY_STRING;

         if (devField.getUnits() != null) {
            customFieldUnit = devField.getUnits();
         }

         final String description = "Garmin developper field";
         final CustomField customField;
         customField = RawDataManager.createCustomField(customFieldName,
               customFieldId,
               customFieldUnit,
               CustomFieldType.NONE,
               description);

         final CustomField customFieldToCompare = new CustomField();
         customFieldToCompare.setFieldName(customFieldName);
         customFieldToCompare.setFieldType(customField.getFieldType());
         customFieldToCompare.setRefId(customFieldId);
         if (customField.equals2(customFieldToCompare) == false) {
            //TODO might not be necessary to update CustomFields
            //existing CustomFields must sray the way they are
            //User might have modified on purpose !!!
            if (customField.getFieldId() == TourDatabase.ENTITY_IS_NOT_SAVED) {

               /*
                * Nothing to do, customField will be saved when a tour is saved which contains
                * this customField
                * in
                * net.tourbook.database.TourDatabase.checkUnsavedTransientInstances_CustomFields(
                * )
                */

            } else {

               /*
                * Notify post process to update the customField in the db
                */

               final ConcurrentHashMap<String, CustomField> allCustomFieldsToBeUpdated = fitData.getImportState_Process()
                     .getAllCustomFieldsToBeUpdated();

               allCustomFieldsToBeUpdated.put(customField.getRefId(), customField);
            }
         }

         //second create CustomFieldValue's
         final CustomFieldValue customFieldValue = new CustomFieldValue(customField);
         CustomFieldType newCustomFieldType;
         /*
          * Set CustomFieldValue into tour data
          */
         customFieldValue.setTourStartTime(tourData.getTourStartTimeMS());
         customFieldValue.setTourEndTime(tourData.getTourEndTimeMS());
         customFieldValue.setTourData(tourData);

         final Object fieldValue = devField.getValue();
         developperFieldString += customFieldName + "[" + customFieldUnit + "] " + UI.SYMBOL_EQUAL;
         developperFieldString += " " + "\"" + fieldValue + "\"";
         if (fieldValue instanceof Float) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((Float) fieldValue).floatValue());
         } else if (fieldValue instanceof Integer) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((Integer) fieldValue).floatValue());

         } else if (fieldValue instanceof Long) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((Long) fieldValue).floatValue());

         } else if (fieldValue instanceof String) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //TODO check if string contains a duration(hhh:mm:sec) or a datetime(YYYY-MM-DDThh:mm:ss)
            if (DateUtil.isValideDuration((String) fieldValue)) {
               try {
                  final Duration duration = DateUtil.parseDuration((String) fieldValue);
                  //customField.setFieldType(CustomFieldType.FIELD_DURATION);
                  newCustomFieldType = CustomFieldType.FIELD_DURATION;
                  customFieldValue.setValueFloat(((Long) duration.getStandardSeconds()).floatValue());
                  customFieldValue.setValueString(null);
               } catch (final Exception e) {
                  System.out.println(e.getMessage());
                  //customField.setFieldType(CustomFieldType.FIELD_STRING);
                  newCustomFieldType = CustomFieldType.FIELD_STRING;
                  customFieldValue.setValueFloat(null);
                  customFieldValue.setValueString(((String) fieldValue));
               }

            } else {
               //customField.setFieldType(CustomFieldType.FIELD_STRING);
               newCustomFieldType = CustomFieldType.FIELD_STRING;
               customFieldValue.setValueFloat(null);
               customFieldValue.setValueString(((String) fieldValue));

            }

         } else if (fieldValue instanceof Short) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((Short) fieldValue).floatValue());

         } else if (fieldValue instanceof Byte) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((Byte) fieldValue).floatValue());

         } else if (fieldValue instanceof Double) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((Double) fieldValue).floatValue());

         } else if (fieldValue instanceof BigInteger) {
            developperFieldString += " {" + fieldValue.getClass().getSimpleName() + "}";
            //customField.setFieldType(CustomFieldType.FIELD_NUMBER);
            newCustomFieldType = CustomFieldType.FIELD_NUMBER;
            customFieldValue.setValueString(null);
            customFieldValue.setValueFloat(((BigInteger) fieldValue).floatValue());

         } else {
            developperFieldString += " {Unknown}";
            //customField.setFieldType(CustomFieldType.FIELD_STRING);
            newCustomFieldType = CustomFieldType.FIELD_STRING;
            customFieldValue.setValueFloat(null);
            customFieldValue.setValueString(((String) fieldValue));

         }
         if (customField.getFieldType().compareTo(CustomFieldType.NONE) == 0) {
            //this is a completly new customField
            customField.setFieldType(newCustomFieldType);
         } else {
            if (customField.getFieldType().compareTo(newCustomFieldType) != 0) {
               //create a new version with a new referenceid because the user already updated
               //the existing one with a different fieldType for good reason
               final CustomField customFieldCopy = RawDataManager.createCustomField(customFieldName,
                     "v2;" + customFieldId, //$NON-NLS-1$
                     customFieldUnit,
                     newCustomFieldType,
                     description);
               customFieldValue.setCustomField(customFieldCopy);
            }
         }
         developperFieldString += UI.NEW_LINE1;

         allTourData_CustomFieldValues.add(customFieldValue);

      }
      if (!developperFieldString.isBlank()) {
         //add it to description
         developperFieldString = "Developper Field(s)\n====================\n" + developperFieldString; //$NON-NLS-1$
         final String note = tourData.getTourDescription();
         tourData.setTourDescription(note + UI.NEW_LINE2 + developperFieldString);
      }

      if (allTourData_StaticCustomFieldValues.size() > 0) {//add static custom field
         for (final CustomFieldValue field : allTourData_StaticCustomFieldValues) {
            allTourData_CustomFieldValues.add(field);
         }
      }

      if (!staticFieldString.isBlank()) {
         //add it to description
         staticFieldString = "Static Field(s)\n====================\n" + staticFieldString; //$NON-NLS-1$
         final String note = tourData.getTourDescription();
         tourData.setTourDescription(note + UI.NEW_LINE2 + staticFieldString);
      }

      fitData.onSetup_Session_20_Finalize();
   }

}
