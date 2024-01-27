package net.tourbook.device.garmin.fit.listeners;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.tourbook.common.time.DateUtil;
import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;
import net.tourbook.data.CustomFieldValue;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.device.garmin.fit.FitData;
import net.tourbook.importdata.RawDataManager;

import org.joda.time.Duration;

public class CustomFieldStatic {

   public static final String                  KEY_ACTIVITY_PROFILE                 = "Activity Profile (fit)";

   public static final String                  KEY_RECOVERY_TIME                    = "Recovery Time (fit)";
   public static final String                  KEY_HR_RECOVERY_DECREASE             = "HR Recovery Decrease (fit)";

   public static final String                  KEY_R_R_AVG                          = "Average R-R (fit)";
   public static final String                  KEY_R_R_MIN                          = "Min R-R (fit)";
   public static final String                  KEY_R_R_MAX                          = "Max R-R (fit)";
   public static final String                  KEY_R_R_STDDEV                       = "R-R StdDev (fit)";
   public static final String                  KEY_PNN50                            = "pNN50 (fit)";
   public static final String                  KEY_RMSSD                            = "RMSSD (fit)";

   public static final String                  KEY_PCO_LEFT_AVG                     = "PCO Left Avg";
   public static final String                  KEY_PCO_RIGHT_AVG                    = "PCO Right Avg";
   public static final String                  KEY_POWER_STANDING_MAX               = "Power Standing Max";
   public static final String                  KEY_POWER_SEATED_MAX                 = "Power Seated Max";
   public static final String                  KEY_POWER_STANDING_AVG               = "Power Standing Avg";
   public static final String                  KEY_POWER_SEATED_AVG                 = "Power Seated Avg";
   public static final String                  KEY_CADENCE_STANDING_MAX             = "Cadence Standing Max";
   public static final String                  KEY_CADENCE_SEATED_MAX               = "Cadence Seated Max";
   public static final String                  KEY_CADENCE_STANDING_AVG             = "Cadence Standing Avg";
   public static final String                  KEY_CADENCE_SEATED_AVG               = "Cadence Seated Avg";
   public static final String                  KEY_POWER_PHASE_RIGHT_START_AVG      = "Power Phase Right Start Avg";
   public static final String                  KEY_POWER_PHASE_RIGHT_END_AVG        = "Power Phase Right End Avg";
   public static final String                  KEY_POWER_PHASE_PEAK_RIGHT_START_AVG = "Power Phase Peak Right Start Avg";
   public static final String                  KEY_POWER_PHASE_PEAK_RIGHT_END_AVG   = "Power Phase Peak Right End Avg";
   public static final String                  KEY_POWER_PHASE_LEFT_START_AVG       = "Power Phase Left Start Avg";
   public static final String                  KEY_POWER_PHASE_LEFT_END_AVG         = "Power Phase Left End Avg";
   public static final String                  KEY_POWER_PHASE_PEAK_LEFT_START_AVG  = "Power Phase Peak Left Start Avg";
   public static final String                  KEY_POWER_PHASE_PEAK_LEFT_END_AVG    = "Power Phase Peak Left End Avg";
   public static final String                  KEY_STROKE_COUNT                     = "Stroke Counts (fit)";

   public static final String                  KEY_STANDING_TIME                    = "Standing Time (fit)";

   public static final String                  KEY_STANDING_COUNT                   = "Standing Count (fit)";
   public static final String                  KEY_TOTAL_GRIT                       = "Total Grit (fit)";
   public static final String                  KEY_TOTAL_FLOW                       = "Total Flow (fit)";
   public static final String                  KEY_AVG_GRIT                         = "Avg Grit (fit)";
   public static final String                  KEY_AVG_FLOW                         = "Avg Flow (fit)";
   public static final String                  KEY_JUMP_COUNT                       = "Jump Count (fit)";
   public static final String                  KEY_TOTAL_STROKES                    = "Total Strokes (fit)";
   public static final String                  KEY_SWEAT_LOSS                       = "Sweat Loss (fit)";
   public static final String                  KEY_RESTING_CALORIES                 = "Resting Calories (fit)";
   public static final String                  KEY_VO2MAX_HIGH                      = "VO2max High (fit)";
   public static final String                  KEY_VO2MAX_LOW                       = "VO2max Low (fit)";
   public static final String                  KEY_INTENSITY_MODERATE               = "Intensity Moderate (fit)";

   public static final String                  KEY_STAMINA_POTENTIAL_START          = "Stamina potential Start (fit)";
   public static final String                  KEY_STAMINA_POTENTIAL_END            = "Stamina potential End (fit)";
   public static final String                  KEY_STAMINA_MIN                      = "Stamina Minimum (fit)";

   private static final HashMap<String, CustomField> map                                  = new HashMap<>();
   private static final HashMap<String, FitIndex>    mapFitIndex                          = new HashMap<>();

   private static final HashMap<String, Integer>     mapFitKey                               = new HashMap<>();
   static {
      //map = new HashMap<>();
      //mapKey = new HashMap<>();


      CustomField field = new CustomField();
      field.setFieldName(KEY_ACTIVITY_PROFILE);
      field.setDescription("Garmin Activity Profile");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("6a5cef4a-c729-4f87-a442-8e3a6cfa3da9");
      field.setUnit("");
      map.put(KEY_ACTIVITY_PROFILE, field);
      mapFitKey.put(KEY_ACTIVITY_PROFILE, 110);
      mapFitIndex.put(KEY_ACTIVITY_PROFILE, new FitIndex(110, null));
      //mapFitKey.put(KEY_ACTIVITY_PROFILE, 5);

      //-------------Cycling Position Dynamics------------
      field = new CustomField();
      field.setFieldName(KEY_PCO_LEFT_AVG);
      field.setDescription("Garmin Average Left Pedal Platform Center Offset");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("515db71d-0027-4ddf-ab3e-91b6f8a16873");
      field.setUnit("mm");
      map.put(KEY_PCO_LEFT_AVG, field);
      mapFitIndex.put(KEY_PCO_LEFT_AVG, new FitIndex(114, null));
      mapFitKey.put(KEY_PCO_LEFT_AVG, 114);

      field = new CustomField();
      field.setFieldName(KEY_PCO_RIGHT_AVG);
      field.setDescription("Garmin Average Right Pedal Platform Center Offset");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("b876aaf4-eed6-4e12-8d72-3f0076fdefe5");
      field.setUnit("mm");
      map.put(KEY_PCO_RIGHT_AVG, field);
      mapFitIndex.put(KEY_PCO_RIGHT_AVG, new FitIndex(115, null));
      mapFitKey.put(KEY_PCO_RIGHT_AVG, 115);

      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_LEFT_START_AVG);
      field.setDescription("Garmin Average Left Power Phase Start");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("b8e69703-30e0-4db8-bf51-935138000d1e");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_LEFT_START_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_LEFT_START_AVG, new FitIndex(116, 0));
      //LEFT PHASE
      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_LEFT_END_AVG);
      field.setDescription("Garmin Average Left Power Phase End");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("3b2f4a0c-0856-4b0b-8f1c-f7bd63697a04");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_LEFT_END_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_LEFT_END_AVG, new FitIndex(116, 1));

      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_PEAK_LEFT_START_AVG);
      field.setDescription("Garmin Average Left Power Phase Peak Start");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("544cb8ee-2f48-47b2-bd38-8f372d4b8c18");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_PEAK_LEFT_START_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_PEAK_LEFT_START_AVG, new FitIndex(117, 0));

      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_PEAK_LEFT_END_AVG);
      field.setDescription("Garmin Average Left Power Phase Peak End");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("c66a669b-4c7e-48f3-adf3-189f481252a4");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_PEAK_LEFT_END_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_PEAK_LEFT_END_AVG, new FitIndex(117, 1));

      //RIGHT PHASE
      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_RIGHT_START_AVG);
      field.setDescription("Garmin Average Right Power Phase Start");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("ef52a26d-4ab7-4acc-8db0-cd40b9f3065d");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_RIGHT_START_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_RIGHT_START_AVG, new FitIndex(118, 0));

      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_RIGHT_END_AVG);
      field.setDescription("Garmin Average Right Power Phase End");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f1361e59-aa64-4a13-80d7-9239d51865be");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_RIGHT_END_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_RIGHT_END_AVG, new FitIndex(118, 1));

      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_PEAK_RIGHT_START_AVG);
      field.setDescription("Garmin Average Right Power Phase Peak Start");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("29bbfe74-13ed-45ed-b3e4-a03c934ce508");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_PEAK_RIGHT_START_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_PEAK_RIGHT_START_AVG, new FitIndex(119, 0));

      field = new CustomField();
      field.setFieldName(KEY_POWER_PHASE_PEAK_RIGHT_END_AVG);
      field.setDescription("Garmin Average Right Power Phase Peak End");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("e618e08e-df59-40fe-89e3-daea65b0bfbe");
      field.setUnit("°");
      map.put(KEY_POWER_PHASE_PEAK_RIGHT_END_AVG, field);
      mapFitIndex.put(KEY_POWER_PHASE_PEAK_RIGHT_END_AVG, new FitIndex(119, 1));

      //---------------------------------------------------
      //-------------Cycling Power Dynamics------------
      field = new CustomField();
      field.setFieldName(KEY_POWER_SEATED_AVG);
      field.setDescription("Garmin Average Power Seated");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("0dcac865-f330-4fa9-9be6-8020963b7a4e");
      field.setUnit("watts");
      map.put(KEY_POWER_SEATED_AVG, field);
      mapFitIndex.put(KEY_POWER_SEATED_AVG, new FitIndex(120, 0));

      field = new CustomField();
      field.setFieldName(KEY_POWER_STANDING_AVG);
      field.setDescription("Garmin Average Power Standing");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("ee1035f8-075d-4ab8-bde5-05bdcab810ef");
      field.setUnit("watts");
      map.put(KEY_POWER_STANDING_AVG, field);
      mapFitIndex.put(KEY_POWER_STANDING_AVG, new FitIndex(120, 1));

      field = new CustomField();
      field.setFieldName(KEY_POWER_SEATED_MAX);
      field.setDescription("Garmin Average Power Seated");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("115d64a3-bb05-4566-aafb-074d136e3ed3");
      field.setUnit("watts");
      map.put(KEY_POWER_SEATED_MAX, field);
      mapFitIndex.put(KEY_POWER_SEATED_MAX, new FitIndex(121, 0));

      field = new CustomField();
      field.setFieldName(KEY_POWER_STANDING_MAX);
      field.setDescription("Garmin Average Power Standing");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("61e922ca-94df-43a6-9a09-46b80956814e");
      field.setUnit("watts");
      map.put(KEY_POWER_STANDING_MAX, field);
      mapFitIndex.put(KEY_POWER_STANDING_MAX, new FitIndex(121, 1));

      //---------------------------------------------------
      //-------------Cycling Cadence Dynamics------------
      field = new CustomField();
      field.setFieldName(KEY_CADENCE_SEATED_AVG);
      field.setDescription("Garmin Average Cadence Seated");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("5b85257d-61e7-441d-b436-37875a99dec9");
      field.setUnit("rpm");
      map.put(KEY_CADENCE_SEATED_AVG, field);
      mapFitIndex.put(KEY_CADENCE_SEATED_AVG, new FitIndex(122, 0));

      field = new CustomField();
      field.setFieldName(KEY_CADENCE_STANDING_AVG);
      field.setDescription("Garmin Average Cadence Standing");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("4ebc2c23-79b0-4398-8f23-dd2d386583e4");
      field.setUnit("rpm");
      map.put(KEY_CADENCE_STANDING_AVG, field);
      mapFitIndex.put(KEY_CADENCE_STANDING_AVG, new FitIndex(122, 1));

      field = new CustomField();
      field.setFieldName(KEY_CADENCE_SEATED_MAX);
      field.setDescription("Garmin Average Cadence Seated");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("bec41ff0-ac8a-4fb0-943a-457017f30960");
      field.setUnit("rpm");
      map.put(KEY_CADENCE_SEATED_MAX, field);
      mapFitIndex.put(KEY_CADENCE_SEATED_MAX, new FitIndex(123, 0));

      field = new CustomField();
      field.setFieldName(KEY_CADENCE_STANDING_MAX);
      field.setDescription("Garmin Average Cadence Standing");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("22907b17-ea79-4996-b4a0-e6fe25319557");
      field.setUnit("rpm");
      map.put(KEY_CADENCE_STANDING_MAX, field);
      mapFitIndex.put(KEY_CADENCE_STANDING_MAX, new FitIndex(123, 1));

      //---------------------------------------------------

      field = new CustomField();
      field.setFieldName(KEY_RECOVERY_TIME);
      field.setDescription("Garmin Recovery Time");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("716ed908-3e19-4eb8-a933-429a921052b9");
      field.setUnit("");
      map.put(KEY_RECOVERY_TIME, field);
      //mapFitIndex.put(KEY_RECOVERY_TIME, new FitIndex(, null));
      //mapKey.put(KEY_RECOVERY_TIME, );

      field = new CustomField();
      field.setFieldName(KEY_HR_RECOVERY_DECREASE);
      field.setDescription("Garmin HR Recovery Decrease");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("5681a0f2-2601-46aa-a52b-4128af83d329");
      field.setUnit("bpm");
      map.put(KEY_HR_RECOVERY_DECREASE, field);
      mapFitIndex.put(KEY_HR_RECOVERY_DECREASE, new FitIndex(202, null));
      mapFitKey.put(KEY_HR_RECOVERY_DECREASE, 202);

      field = new CustomField();
      field.setFieldName(KEY_STROKE_COUNT);
      field.setDescription("Garmin Stroke Counts");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("de6b84ea-9a09-4d2f-adb8-77d174860cef");
      field.setUnit("counts");
      map.put(KEY_STROKE_COUNT, field);
      mapFitIndex.put(KEY_STROKE_COUNT, new FitIndex(85, null));
      mapFitKey.put(KEY_STROKE_COUNT, 85);


      field = new CustomField();
      field.setFieldName(KEY_STANDING_COUNT);
      field.setDescription("Garmin Standing Count (cycling)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f507d70a-bd67-4f90-8451-3a9870f3101e");
      field.setUnit("");
      map.put(KEY_STANDING_COUNT, field);
      mapFitIndex.put(KEY_STANDING_COUNT, new FitIndex(113, null));
      mapFitKey.put(KEY_STANDING_COUNT, 113);

      field = new CustomField();
      field.setFieldName(KEY_STANDING_TIME);
      field.setDescription("Garmin Standing Time (cycling)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("a724c61b-040e-4c79-ade3-3722d53be649");
      field.setUnit("s");
      map.put(KEY_STANDING_TIME, field);
      mapFitIndex.put(KEY_STANDING_TIME, new FitIndex(112, null));
      mapFitKey.put(KEY_STANDING_TIME, 112);

      field = new CustomField();
      field.setFieldName(KEY_TOTAL_GRIT);
      field.setDescription("Garmin Total Grit (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("2679ce1f-783b-48d9-9c34-a4e82e49e6f2");
      field.setUnit("kGrit");
      map.put(KEY_TOTAL_GRIT, field);
      mapFitIndex.put(KEY_TOTAL_GRIT, new FitIndex(181, null));
      mapFitKey.put(KEY_TOTAL_GRIT, 181);

      field = new CustomField();
      field.setFieldName(KEY_TOTAL_FLOW);
      field.setDescription("Garmin Total Flow (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("4711e2ab-1367-4ea3-a0e7-1b5ee141e216");
      field.setUnit("Flow");
      map.put(KEY_TOTAL_FLOW, field);
      mapFitIndex.put(KEY_TOTAL_FLOW, new FitIndex(182, null));
      mapFitKey.put(KEY_TOTAL_FLOW, 182);

      field = new CustomField();
      field.setFieldName(KEY_AVG_GRIT);
      field.setDescription("Garmin Average Grit (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("aa78a07b-082a-46aa-a2c5-7656ae9cab50");
      field.setUnit("kGrit");
      map.put(KEY_AVG_GRIT, field);
      mapFitIndex.put(KEY_AVG_GRIT, new FitIndex(186, null));
      mapFitKey.put(KEY_AVG_GRIT, 186);

      field = new CustomField();
      field.setFieldName(KEY_AVG_FLOW);
      field.setDescription("Garmin Average Flow (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("af0d5e2b-1a41-40a7-ba37-e9bbece6916d");
      field.setUnit("Flow");
      map.put(KEY_AVG_FLOW, field);
      mapFitIndex.put(KEY_AVG_FLOW, new FitIndex(187, null));
      mapFitKey.put(KEY_AVG_FLOW, 187);

      field = new CustomField();
      field.setFieldName(KEY_JUMP_COUNT);
      field.setDescription("Garmin Number Of Jumps (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("00575577-6482-4971-b8a5-0ce1f53672e0");
      field.setUnit("");
      map.put(KEY_JUMP_COUNT, field);
      mapFitIndex.put(KEY_JUMP_COUNT, new FitIndex(183, null));
      mapFitKey.put(KEY_JUMP_COUNT, 183);

      field = new CustomField();
      field.setFieldName(KEY_TOTAL_STROKES);
      field.setDescription("Garmin Total Strokes/Cycles/Strides");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("41ca5f0c-cd1b-4a9a-a65d-bf6f7133c0f4");
      field.setUnit("");
      map.put(KEY_TOTAL_STROKES, field);
      mapFitIndex.put(KEY_TOTAL_STROKES, new FitIndex(10, null));
      mapFitKey.put(KEY_TOTAL_STROKES, 10);

      field = new CustomField();
      field.setFieldName(KEY_SWEAT_LOSS);
      field.setDescription("Garmin Sweat Loss");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("898edbe0-d429-46c2-9b69-d3bcf2ae52a9");
      field.setUnit("ml");
      map.put(KEY_SWEAT_LOSS, field);
      mapFitIndex.put(KEY_SWEAT_LOSS, new FitIndex(178, null));
      mapFitKey.put(KEY_SWEAT_LOSS, 178);

      field = new CustomField();
      field.setFieldName(KEY_RESTING_CALORIES);
      field.setDescription("Garmin Number of Calories at Rest");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f9c9dc74-bdcc-4ce6-ac75-184d146a6752");
      field.setUnit("kcal");
      map.put(KEY_RESTING_CALORIES, field);
      mapFitIndex.put(KEY_RESTING_CALORIES, new FitIndex(196, null));
      mapFitKey.put(KEY_RESTING_CALORIES, 196);

      field = new CustomField();
      field.setFieldName(KEY_INTENSITY_MODERATE);
      field.setDescription("Garmin Intensity Moderate");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("6acec2a5-a488-4add-a824-265a970013e1");
      field.setUnit("min");
      map.put(KEY_INTENSITY_MODERATE, field);
      mapFitIndex.put(KEY_INTENSITY_MODERATE, new FitIndex(212, null));
      mapFitKey.put(KEY_INTENSITY_MODERATE, 212);

      field = new CustomField();
      field.setFieldName(KEY_STAMINA_POTENTIAL_START);
      field.setDescription("Garmin Stamina Potential at the Begining");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("3d7f4d1e-e4a3-4c07-9a85-7ab8b1cf5f5a");
      field.setUnit("%");
      map.put(KEY_STAMINA_POTENTIAL_START, field);
      mapFitIndex.put(KEY_STAMINA_POTENTIAL_START, new FitIndex(205, null));
      mapFitKey.put(KEY_STAMINA_POTENTIAL_START, 205);

      field = new CustomField();
      field.setFieldName(KEY_STAMINA_POTENTIAL_END);
      field.setDescription("Garmin Stamina Potential at the End");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("12e5b08b-c046-4d38-bccf-e6f9945bcc2e");
      field.setUnit("%");
      map.put(KEY_STAMINA_POTENTIAL_END, field);
      mapFitIndex.put(KEY_STAMINA_POTENTIAL_END, new FitIndex(206, null));
      mapFitKey.put(KEY_STAMINA_POTENTIAL_END, 206);

      field = new CustomField();
      field.setFieldName(KEY_STAMINA_MIN);
      field.setDescription("Garmin Stamina Minimum During Activity");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("bd2ab323-d6de-4a1b-a3c1-d52a8dc3823b");
      field.setUnit("%");
      map.put(KEY_STAMINA_MIN, field);
      mapFitIndex.put(KEY_STAMINA_MIN, new FitIndex(207, null));
      mapFitKey.put(KEY_STAMINA_MIN, 207);

      field = new CustomField();
      field.setFieldName(KEY_VO2MAX_HIGH);
      field.setDescription("Garmin VO2Max upper limit estimate");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f5aee6c8-78d4-43eb-8ad8-1bbd604616be");
      field.setUnit("");
      map.put(KEY_VO2MAX_HIGH, field);

      field = new CustomField();
      field.setFieldName(KEY_VO2MAX_LOW);
      field.setDescription("Garmin VO2Max lower limit estimate");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("a8e27bf8-df62-4bc1-8e93-ff6604af31e5");
      field.setUnit("");
      map.put(KEY_VO2MAX_LOW, field);

   }

   public static CustomField addCustomField(final CustomField field, final FitData fitData) {

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

   public static CustomFieldValue addCustomFieldValue(final Object fieldValue,
                                                      final CustomField customField,
                                                      final TourData tourData,
                                                      final FitData fitData) {

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

   public static HashMap<String, FitIndex> getFitIndexMap() {
      return mapFitIndex;
   }

   public static HashMap<String, Integer> getFitMap() {
      return mapFitKey;
   }

   public static HashMap<String, CustomField> getMap() {
      return map;
   }
}
