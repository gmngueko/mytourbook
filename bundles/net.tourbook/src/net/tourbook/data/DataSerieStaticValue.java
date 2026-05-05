package net.tourbook.data;

import java.util.HashMap;
import java.util.Map;

public class DataSerieStaticValue {
   //Name; Comment; Unit; RefId
   public static int            NameIdx                      = 0;
   public static int            CommentIdx                   = 1;
   public static int            UnitIdx                      = 2;
   public static int            UUIDIdx                      = 3;

   public static final String[] GRADE                        = { "Grade", "Grade", "%",
         "e3e1f807-ac01-4ff9-a05d-6ed114e85353" };

   public static final String[] PERFORMANCE_CONDITION        = { "Performance Condition", "Performance Condition", "",
         "181866cb-0d1c-4888-9a9f-951c1153179b" };
   public static final String[] STAMINA                      = { "Stamina", "Stamina", "%",
         "31c38bf1-e823-4077-bb46-5e96b02a7991" };
   public static final String[] STAMINA_POTENTIAL            = { "Stamina Potential", "Stamina Potential", "%",
         "bc3cd6d1-2c15-4ce2-87dc-251bf0da3170" };
   public static final String[] RESPIRATION_RATE             = { "Respiration Rate", "Respiration Rate", "brpm",
         "d2c81197-b152-4c85-abce-112e42ea5825" };
   public static final String[] GARMIN_SPO2     = { "Garmin SPO2", "Garmin spO2", "%", "12d76972-a064-4cf0-ac91-c023869b2bb3" };
   public static final String[] GARMIN_RESPRATE = { "Garmin RespRate", "Garmin Respiration Rate", "cpm", "c0e4348f-2d8b-48cf-ab91-14d0e32c61d5" };
   public static final String[] GEAR_RATIO      = { "Gear ratio", "", "", "d23af954-ed29-43ae-9206-305e30bc8be3" };

   public static final String[] LEFT_PEDAL_SMOOTHNESS     = { "Left Pedal Smoothness", "", "%", "8c08b148-e5a6-4903-8ab7-97ba3925b039" };
   public static final String[] LEFT_TORQUE_EFFECTIVENESS = { "Left Torque Effectiveness", "", "%", "98c6bf0b-7c3c-4f3a-8ddc-5427cf09cdc9" };

   public static final String[] PLATFORM_CENTER_OFFSET_LEFT = { "Platform center offset - Left", "", "mm", "bd76403c-634e-421a-ba38-8d166483e077" };
   public static final String[] PLATFORM_CENTER_OFFSET_RIGHT = { "Platform center offset - Right", "", "mm", "8f567038-9d9d-4c57-9e7c-5517c5357c9d" };

   public static final String[] POWER_LEFT_RIGHT_BALANCE     = { "Power balance", "Power balance", "% Left", "dffda4e1-a721-417a-9c50-11fee33bcda7" };

   public static final String[] POWER_PHASE_LEFT_END         = { "Power phase - Left - End", "", "°", "b688de7c-5266-460b-bb38-09b12c348f58" };
   public static final String[] POWER_PHASE_LEFT_START       = { "Power phase - Left - Start", "", "°", "35ed646a-e84f-47b4-8e76-43b84d5efdfb" };
   public static final String[] POWER_PHASE_RIGHT_END        = { "Power phase - Right - End", "", "°", "2cfc2594-0dc0-4e3c-bb38-3bf0eb8a9f48" };
   public static final String[] POWER_PHASE_RIGHT_START      = { "Power phase - Right - Start", "", "°", "26e23a22-7551-4aa9-9643-3cb0741bd82c" };

   public static final String[] POWER_PHASE_PEAK_LEFT_END         = { "Power phase peak - Left - End", "", "°", "a715420f-4372-4c57-8860-ccbf7ff9e9de" };
   public static final String[] POWER_PHASE_PEAK_LEFT_START       = { "Power phase peak - Left - Start", "", "°", "31205b13-d1fa-433a-9aa8-6ccda334eb48" };
   public static final String[] POWER_PHASE_PEAK_RIGHT_END         = { "Power phase peak - Right - End", "", "°", "ace25859-797f-4e42-aa22-73f4c78fe483" };
   public static final String[] POWER_PHASE_PEAK_RIGHT_START       = { "Power phase peak - Right - Start", "", "°", "acd57289-689b-4a47-bdb8-7f2c9cd150eb" };

   public static final String[] RIGHT_PEDAL_SMOOTHNESS       = { "Right Pedal Smoothness", "", "%", "86335f67-9f79-4080-bbe9-b8871136b207" };
   public static final String[] RIGHT_TORQUE_EFFECTIVENESS   = { "Right Torque Effectiveness", "", "%", "3fa6c311-f820-407b-aee8-bc6bb5b6ed4c" };

   public static final String[] GARMIN_FLOW = { "Garmin Flow", "Flow score estimates "
                                             + "how long distance wise a cyclist"
                                             + " deaccelerates over intervals where "
                                             + "deacceleration is unnecessary", "", "f190498b-dc57-46c2-93ba-4f2fe98d29f1" };
   public static final String[] GARMIN_GRIT = { "Garmin Grit", "Grit score estimates "
                                              + "how challenging a route could be "
                                              + "in terms of time spent "
                                              + "going over sharp turns or large grade slopes", "", "afb2e7e7-7da7-4b28-809f-ee23ba02b009" };

   public static final String[] RECORDING_DEVICE_BATTERY     = { "battery", "device battery", "%", "5596e3f5-c564-46d4-811d-396f69feee95" };

   public static final String[] TOTAL_HEMOGLOBIN_CONC = { "Total Hemoglobin Concentration", "Total Hemoglobin Concentration "
                                                                                          + "from Fit data", "g/dL", "c5e54c52-3cb7-4f26-bee5-66e7695d1f15" };
   public static final String[] SATURATED_HEMOGLOBIN = { "Saturated Hemoglobin Percent", "Saturated Hemoglobin Percent", "%", "f68155fa-1219-4c12-96eb-975bf2b37176" };

   /*
    * public static final String[] XX = { "Name", "Comment", "Unit", "UUID" };
    * public static final String[] XX = { "", "", "", "" };
    */

   private static final Map<String, String[]> DATASERIES_STATIC = new HashMap<>();

   public static final Map<String, String[]> getList() {
      DATASERIES_STATIC.clear();

      DATASERIES_STATIC.put(GRADE[UUIDIdx], GRADE);

      DATASERIES_STATIC.put(PERFORMANCE_CONDITION[UUIDIdx], PERFORMANCE_CONDITION);
      DATASERIES_STATIC.put(STAMINA[UUIDIdx], STAMINA);
      DATASERIES_STATIC.put(STAMINA_POTENTIAL[UUIDIdx], STAMINA_POTENTIAL);
      DATASERIES_STATIC.put(RESPIRATION_RATE[UUIDIdx], RESPIRATION_RATE);

      DATASERIES_STATIC.put(GARMIN_SPO2[UUIDIdx], GARMIN_SPO2);
      DATASERIES_STATIC.put(GARMIN_RESPRATE[UUIDIdx], GARMIN_RESPRATE);
      DATASERIES_STATIC.put(GEAR_RATIO[UUIDIdx], GEAR_RATIO);

      DATASERIES_STATIC.put(LEFT_PEDAL_SMOOTHNESS[UUIDIdx], LEFT_PEDAL_SMOOTHNESS);
      DATASERIES_STATIC.put(LEFT_TORQUE_EFFECTIVENESS[UUIDIdx], LEFT_TORQUE_EFFECTIVENESS);

      DATASERIES_STATIC.put(PLATFORM_CENTER_OFFSET_LEFT[UUIDIdx], PLATFORM_CENTER_OFFSET_LEFT);
      DATASERIES_STATIC.put(PLATFORM_CENTER_OFFSET_RIGHT[UUIDIdx], PLATFORM_CENTER_OFFSET_RIGHT);

      DATASERIES_STATIC.put(POWER_LEFT_RIGHT_BALANCE[UUIDIdx], POWER_LEFT_RIGHT_BALANCE);

      DATASERIES_STATIC.put(POWER_PHASE_LEFT_END[UUIDIdx], POWER_PHASE_LEFT_END);
      DATASERIES_STATIC.put(POWER_PHASE_LEFT_START[UUIDIdx], POWER_PHASE_LEFT_START);
      DATASERIES_STATIC.put(POWER_PHASE_RIGHT_END[UUIDIdx], POWER_PHASE_RIGHT_END);
      DATASERIES_STATIC.put(POWER_PHASE_RIGHT_START[UUIDIdx], POWER_PHASE_RIGHT_START);

      DATASERIES_STATIC.put(POWER_PHASE_PEAK_LEFT_END[UUIDIdx], POWER_PHASE_PEAK_LEFT_END);
      DATASERIES_STATIC.put(POWER_PHASE_PEAK_LEFT_START[UUIDIdx], POWER_PHASE_PEAK_LEFT_START);
      DATASERIES_STATIC.put(POWER_PHASE_PEAK_RIGHT_END[UUIDIdx], POWER_PHASE_PEAK_RIGHT_END);
      DATASERIES_STATIC.put(POWER_PHASE_PEAK_RIGHT_START[UUIDIdx], POWER_PHASE_PEAK_RIGHT_START);

      DATASERIES_STATIC.put(RIGHT_PEDAL_SMOOTHNESS[UUIDIdx], RIGHT_PEDAL_SMOOTHNESS);
      DATASERIES_STATIC.put(RIGHT_TORQUE_EFFECTIVENESS[UUIDIdx], RIGHT_TORQUE_EFFECTIVENESS);

      DATASERIES_STATIC.put(GARMIN_FLOW[UUIDIdx], GARMIN_FLOW);
      DATASERIES_STATIC.put(GARMIN_GRIT[UUIDIdx], GARMIN_GRIT);

      DATASERIES_STATIC.put(RECORDING_DEVICE_BATTERY[UUIDIdx], RECORDING_DEVICE_BATTERY);
      DATASERIES_STATIC.put(TOTAL_HEMOGLOBIN_CONC[UUIDIdx], TOTAL_HEMOGLOBIN_CONC);
      DATASERIES_STATIC.put(SATURATED_HEMOGLOBIN[UUIDIdx], SATURATED_HEMOGLOBIN);

      return DATASERIES_STATIC;
   }
}
