package net.tourbook.device.garmin.fit.listeners;

import java.util.HashMap;

import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;

public class CustomFieldStatic {
   public static final String KEY_ACTIVITY_PROFILE     = "Activity Profile";
   public static final String KEY_RECOVERY_TIME        = "Recovery Time";
   public static final String KEY_HR_RECOVERY_DECREASE = "HR Recovery Decrease";

   public static final String KEY_R_R_AVG              = "Average R-R";
   public static final String KEY_R_R_MIN              = "Min R-R";
   public static final String KEY_R_R_MAX              = "Max R-R";
   public static final String KEY_R_R_STDDEV           = "R-R StdDev";
   public static final String KEY_PNN50                = "pNN50";
   public static final String KEY_RMSSD                = "RMSSD";

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

   public static final String KEY_STANDING_TIME                    = "Standing Time";
   public static final String KEY_STANDING_COUNT                   = "Standing Count";
   public static final String KEY_TOTAL_GRIT                       = "Total Grit";
   public static final String KEY_TOTAL_STROKES                    = "Total Strokes";
   public static final String KEY_SWEAT_LOSS                       = "Sweat Loss";
   public static final String KEY_RESTING_CALORIES                 = "Resting Calories";
   public static final String KEY_VO2MAX_HIGH                      = "VO2max High";
   public static final String KEY_VO2MAX_LOW                       = "VO2max Low";

   public static final String KEY_INTENSITY_MODERATE               = "Intensity Moderate";
   public static final String KEY_STAMINA_POTENTIAL_START          = "Stamina potential Start";
   public static final String KEY_STAMINA_POTENTIAL_END            = "Stamina potential End";
   public static final String KEY_STAMINA_MIN                      = "Stamina Minimum";

   private static HashMap<String, CustomField> map;

   static {
      map = new HashMap<>();
      CustomField field = new CustomField();
      field.setFieldName(KEY_ACTIVITY_PROFILE);
      field.setDescription("Garmin Activity Profile");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("6a5cef4a-c729-4f87-a442-8e3a6cfa3da9");
      field.setUnit("");
      map.put(KEY_ACTIVITY_PROFILE, field);

      field = new CustomField();
      field.setFieldName(KEY_RECOVERY_TIME);
      field.setDescription("Garmin Recovery Time");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("716ed908-3e19-4eb8-a933-429a921052b9");
      field.setUnit("");
      map.put(KEY_RECOVERY_TIME, field);

      field = new CustomField();
      field.setFieldName(KEY_HR_RECOVERY_DECREASE);
      field.setDescription("Garmin HR Recovery Decrease");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("5681a0f2-2601-46aa-a52b-4128af83d329");
      field.setUnit("bpm");
      map.put(KEY_HR_RECOVERY_DECREASE, field);

      field = new CustomField();
      field.setFieldName(KEY_STANDING_COUNT);
      field.setDescription("Garmin Standing Count (cycling)");
      field.setFieldType(CustomFieldType.NONE);
      field.setRefId("f507d70a-bd67-4f90-8451-3a9870f3101e");
      field.setUnit("");
      map.put(KEY_STANDING_COUNT, field);

   }

   public static HashMap<String, CustomField> getMap() {
      return map;
   }
}
