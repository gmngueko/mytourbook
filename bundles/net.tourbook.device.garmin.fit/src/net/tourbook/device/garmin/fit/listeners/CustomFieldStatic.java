package net.tourbook.device.garmin.fit.listeners;

import java.util.HashMap;

import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;

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

   public static final String KEY_PCO_LEFT_AVG         = "PCO Left Avg";
   public static final String KEY_PCO_RIGHT_AVG        = "PCO Right Avg";
   public static final String KEY_POWER_PHASE_RIGHT_START_AVG      = "Power Phase Right Start Avg";
   public static final String KEY_POWER_PHASE_RIGHT_END_AVG        = "Power Phase Right End Avg";
   public static final String KEY_POWER_PHASE_PEAK_RIGHT_START_AVG = "Power Phase Peak Right Start Avg";
   public static final String KEY_POWER_PHASE_PEAK_RIGHT_END_AVG   = "Power Phase Peak Right End Avg";
   public static final String KEY_POWER_PHASE_LEFT_START_AVG       = "Power Phase Left Start Avg";
   public static final String KEY_POWER_PHASE_LEFT_END_AVG         = "Power Phase Left End Avg";
   public static final String KEY_POWER_PHASE_PEAK_LEFT_START_AVG  = "Power Phase Peak Left Start Avg";
   public static final String KEY_POWER_PHASE_PEAK_LEFT_END_AVG    = "Power Phase Peak Left End Avg";

   public static final String                  KEY_STANDING_TIME                    = "Standing Time (fit)";
   public static final String                  KEY_STANDING_COUNT                   = "Standing Count (fit)";
   public static final String                  KEY_TOTAL_GRIT                       = "Total Grit (fit)";
   public static final String                  KEY_TOTAL_FLOW                       = "Total Flow (fit)";
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
      //mapFitKey.put(KEY_ACTIVITY_PROFILE, 5);

      field = new CustomField();
      field.setFieldName(KEY_RECOVERY_TIME);
      field.setDescription("Garmin Recovery Time");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("716ed908-3e19-4eb8-a933-429a921052b9");
      field.setUnit("");
      map.put(KEY_RECOVERY_TIME, field);
      //mapKey.put(KEY_RECOVERY_TIME, );

      field = new CustomField();
      field.setFieldName(KEY_HR_RECOVERY_DECREASE);
      field.setDescription("Garmin HR Recovery Decrease");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("5681a0f2-2601-46aa-a52b-4128af83d329");
      field.setUnit("bpm");
      map.put(KEY_HR_RECOVERY_DECREASE, field);
      mapFitKey.put(KEY_HR_RECOVERY_DECREASE, 202);

      field = new CustomField();
      field.setFieldName(KEY_STANDING_COUNT);
      field.setDescription("Garmin Standing Count (cycling)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f507d70a-bd67-4f90-8451-3a9870f3101e");
      field.setUnit("");
      map.put(KEY_STANDING_COUNT, field);
      mapFitKey.put(KEY_STANDING_COUNT, 113);

      field = new CustomField();
      field.setFieldName(KEY_STANDING_TIME);
      field.setDescription("Garmin Standing Time (cycling)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("a724c61b-040e-4c79-ade3-3722d53be649");
      field.setUnit("");
      map.put(KEY_STANDING_TIME, field);
      mapFitKey.put(KEY_STANDING_TIME, 112);

      field = new CustomField();
      field.setFieldName(KEY_TOTAL_GRIT);
      field.setDescription("Garmin Total Grit (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("2679ce1f-783b-48d9-9c34-a4e82e49e6f2");
      field.setUnit("kGrit");
      map.put(KEY_TOTAL_GRIT, field);
      mapFitKey.put(KEY_TOTAL_GRIT, 181);

      field = new CustomField();
      field.setFieldName(KEY_TOTAL_FLOW);
      field.setDescription("Garmin Total Flow (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("4711e2ab-1367-4ea3-a0e7-1b5ee141e216");
      field.setUnit("Flow");
      map.put(KEY_TOTAL_FLOW, field);
      mapFitKey.put(KEY_TOTAL_FLOW, 182);

      field = new CustomField();
      field.setFieldName(KEY_JUMP_COUNT);
      field.setDescription("Garmin Number Of Jumps (MTB)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("00575577-6482-4971-b8a5-0ce1f53672e0");
      field.setUnit("");
      map.put(KEY_JUMP_COUNT, field);
      mapFitKey.put(KEY_JUMP_COUNT, 183);

      field = new CustomField();
      field.setFieldName(KEY_TOTAL_STROKES);
      field.setDescription("Garmin Total Strokes/Cycles/Strides");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("41ca5f0c-cd1b-4a9a-a65d-bf6f7133c0f4");
      field.setUnit("");
      map.put(KEY_TOTAL_STROKES, field);
      mapFitKey.put(KEY_TOTAL_STROKES, 10);

      field = new CustomField();
      field.setFieldName(KEY_SWEAT_LOSS);
      field.setDescription("Garmin Sweat Loss");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("898edbe0-d429-46c2-9b69-d3bcf2ae52a9");
      field.setUnit("ml");
      map.put(KEY_SWEAT_LOSS, field);
      mapFitKey.put(KEY_SWEAT_LOSS, 178);

      field = new CustomField();
      field.setFieldName(KEY_RESTING_CALORIES);
      field.setDescription("Garmin Number of Calories at Rest");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f9c9dc74-bdcc-4ce6-ac75-184d146a6752");
      field.setUnit("kcal");
      map.put(KEY_RESTING_CALORIES, field);
      mapFitKey.put(KEY_RESTING_CALORIES, 196);

      field = new CustomField();
      field.setFieldName(KEY_INTENSITY_MODERATE);
      field.setDescription("Garmin Intensity Moderate");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("6acec2a5-a488-4add-a824-265a970013e1");
      field.setUnit("min");
      map.put(KEY_INTENSITY_MODERATE, field);
      mapFitKey.put(KEY_INTENSITY_MODERATE, 212);

      field = new CustomField();
      field.setFieldName(KEY_STAMINA_POTENTIAL_START);
      field.setDescription("Garmin Stamina Potential at the Begining");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("3d7f4d1e-e4a3-4c07-9a85-7ab8b1cf5f5a");
      field.setUnit("%");
      map.put(KEY_STAMINA_POTENTIAL_START, field);
      mapFitKey.put(KEY_STAMINA_POTENTIAL_START, 205);

      field = new CustomField();
      field.setFieldName(KEY_STAMINA_POTENTIAL_END);
      field.setDescription("Garmin Stamina Potential at the End");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("12e5b08b-c046-4d38-bccf-e6f9945bcc2e");
      field.setUnit("%");
      map.put(KEY_STAMINA_POTENTIAL_END, field);
      mapFitKey.put(KEY_STAMINA_POTENTIAL_END, 206);

      field = new CustomField();
      field.setFieldName(KEY_STAMINA_MIN);
      field.setDescription("Garmin Stamina Minimum During Activity");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("bd2ab323-d6de-4a1b-a3c1-d52a8dc3823b");
      field.setUnit("%");
      map.put(KEY_STAMINA_MIN, field);
      mapFitKey.put(KEY_STAMINA_MIN, 207);

   }

   public static HashMap<String, Integer> getFitMap() {
      return mapFitKey;
   }

   public static HashMap<String, CustomField> getMap() {
      return map;
   }
}
