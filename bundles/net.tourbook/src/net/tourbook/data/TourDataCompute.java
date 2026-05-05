package net.tourbook.data;

import java.util.ArrayList;
import java.util.Arrays;

public class TourDataCompute {
   public static final String CLASS_NAME_LOG = "TourDataCompute";
   public static final String MALIK_RULE     = "malik";
   public static final String KAMATH_RULE    = "kamath";
   public static final String KARLSSON_RULE  = "karlsson";
   public static final String ACAR_RULE      = "acar";
   public static final String CUSTOM_RULE    = "custom";

   public static class Compute_Rmssd_Pnn50_Result {
      Double rmssd = Double.NaN;
      Double pnn50 = Double.NaN;
   }

   public static class Remove_Outlier_Result {
      int      outlier_count = 0;
      Double[] nn_intervals  = null;
   }

   public static Remove_Outlier_Result _remove_outlier_acar(
                                                            final Double[] rr_intervals,
                                                            Double custom_rule) {
      //def _remove_outlier_acar(rr_intervals: List[float], custom_rule=0.2) -> Tuple[list, int]:
      final Remove_Outlier_Result result = new Remove_Outlier_Result();
      if(custom_rule == null) {
         custom_rule = 0.2;
      }

      //      """
      //      RR-intervals differing by more than the 20 % of the mean of last 9 RrIntervals
      //      are removed.
      //
      //      Parameters
      //      ---------
      //      rr_intervals : list
      //          list of RR-intervals
      //      custom_rule : int
      //          percentage criteria of difference with mean of  9 previous RR-intervals at
      //          which we consider that RR-interval is abnormal. By default, set to 20 %
      //
      //      Returns
      //      ---------
      //      nn_intervals : list
      //          list of NN Interval
      //
      //      References
      //      ----------
      //      .. [8] Automatic ectopic beat elimination in short-term heart rate variability measurements \
      //      Acar B., Irina S., Hemingway H., Malik M.
      //      """

      final ArrayList<Double> nn_intervals = new ArrayList<>();
      result.outlier_count = 0;

      for (int i = 0; i < rr_intervals.length; i++) {
         final Double rr_interval = rr_intervals[i];
         if (i < 9) {
            nn_intervals.add(rr_interval);
            continue;
         }

         Double acar_rule_elt = Double.NaN;
         int count = 0;
         Double sum = 0.0;
         final int lenNN = nn_intervals.size();
         for (int j = lenNN - 9; j < lenNN; j++) {//sum of last 9 entries in nn_intervals
            if (!nn_intervals.get(j).isNaN()) {
               sum += nn_intervals.get(j);
               count++;
            }
         }
         if (count > 0) {
            acar_rule_elt = sum / count;
            if (Math.abs(acar_rule_elt - rr_interval) < custom_rule * acar_rule_elt) {
               nn_intervals.add(rr_interval);
            } else {// it as an outlier
               nn_intervals.add(Double.NaN);
               result.outlier_count += 1;
            }
         } else {//average itself is NaN so treat it as an outlier
            nn_intervals.add(Double.NaN);
            result.outlier_count += 1;
         }
      }

      Double[] resArray = new Double[nn_intervals.size()];
      resArray = nn_intervals.toArray(resArray);
      result.nn_intervals = resArray;
      return result;
   }

   public static Remove_Outlier_Result _remove_outlier_karlsson(
                                                                final Double[] rr_intervals,
                                                                Double removing_rule) {
      //def _remove_outlier_karlsson(rr_intervals: List[float], removing_rule: float = 0.2) -> Tuple[list, int]:
      //      """
      //      RR-intervals differing by more than the 20 % of the mean of previous and next RR-interval
      //      are removed.
      //
      //      Parameters
      //      ---------
      //      rr_intervals : list
      //          list of RR-intervals
      //      removing_rule : float
      //          Percentage of difference between the absolute mean of previous and next RR-interval at which \
      //      to consider the beat as abnormal.
      //
      //      Returns
      //      ---------
      //      nn_intervals : list
      //          list of NN Interval
      //
      //      References
      //      ----------
      //      .. [7]  Automatic filtering of outliers in RR-intervals before analysis of heart rate \
      //      variability in Holter recordings: a comparison with carefully edited data - Marcus Karlsson, \
      //      Rolf Hörnsten, Annika Rydberg and Urban Wiklund
      //      """

      final Remove_Outlier_Result result = new Remove_Outlier_Result();
      if (removing_rule == null) {
         removing_rule = 0.2;
      }

      final ArrayList<Double> nn_intervals = new ArrayList<>();
      result.outlier_count = 0;

      for (int i = 0; i < rr_intervals.length; i++) {
         if (i == rr_intervals.length - 2) {
            nn_intervals.add(rr_intervals[i + 1]);
            break;
         }
         final Double mean_prev_next_rri = (rr_intervals[i] + rr_intervals[i + 2]) / 2;

         if (Math.abs(mean_prev_next_rri - rr_intervals[i + 1]) < removing_rule * mean_prev_next_rri) {
            nn_intervals.add(rr_intervals[i + 1]);
         } else {
            nn_intervals.add(Double.NaN);
            result.outlier_count += 1;
         }
      }

      return result;
   }

   public static Compute_Rmssd_Pnn50_Result calculateRMSSD(final Double[] nn_intervals) {
      Compute_Rmssd_Pnn50_Result result = new Compute_Rmssd_Pnn50_Result();

      if (nn_intervals.length > 1) {
         double num1 = nn_intervals[0];
         double num2 = 0.0;
         int num3 = 0;
         for (int index = 1; index < nn_intervals.length; ++index) {
            if (nn_intervals[index] == null) {
               continue;
            }
            final double num4 = nn_intervals[index] - num1;
            num2 += num4 * num4;
            num1 = nn_intervals[index];
            if (num4 > 50.0 || num4 < -50.0) {
               ++num3;
            }
         }

         result = new Compute_Rmssd_Pnn50_Result();
         result.rmssd = Math.sqrt(num2 / (nn_intervals.length - 1));
         result.pnn50 = 1f * (double) num3 / (nn_intervals.length - 1);
      }

      return result;
   }

   public static Compute_Rmssd_Pnn50_Result calculate_RMSSD_PNN50(final int[] values) {
      Compute_Rmssd_Pnn50_Result result = new Compute_Rmssd_Pnn50_Result();

      if (values.length > 1) {
         float currentInterval = values[0];
         double mssd = 0.0;
         int nn50 = 0;
         for (int index = 1; index < values.length; ++index) {
            final double diffWithPrevValue = (double) values[index] - (double) currentInterval;
            mssd += diffWithPrevValue * diffWithPrevValue;
            currentInterval = values[index];
            if (diffWithPrevValue > 50.0 || diffWithPrevValue < -50.0) {
               ++nn50;
            }
         }

         result = new Compute_Rmssd_Pnn50_Result();
         result.rmssd = Math.sqrt(mssd / (values.length - 1));
         result.pnn50 = 1f * (double) nn50 / (values.length - 1);
      }

      return result;
   }

   public static double calculateStandardDeviation(final Double[] nn_intervals) {

      if (nn_intervals == null || nn_intervals.length == 0) {
         return Double.NaN;
      }
      // get the sum of array
      double sum = 0.0;
      for (final Double nn_interval : nn_intervals) {
         if (nn_interval == null) {
            continue;
         }
         sum += nn_interval;
      }

      // get the mean of array
      final int length = nn_intervals.length;
      final double mean = sum / length;

      // calculate the standard deviation
      double standardDeviation = 0.0;
      for (final Double nn_interval : nn_intervals) {
         if (nn_interval == null) {
            continue;
         }
         standardDeviation += Math.pow(nn_interval - mean, 2);
      }

      return Math.sqrt(standardDeviation / length);
   }

   public static double calculateStandardDeviation(final int[] array) {

      if (array == null || array.length == 0) {
         return Double.NaN;
      }
      // get the sum of array
      double sum = 0.0;
      for (final double i : array) {
         sum += i;
      }

      // get the mean of array
      final int length = array.length;
      final double mean = sum / length;

      // calculate the standard deviation
      double standardDeviation = 0.0;
      for (final double num : array) {
         standardDeviation += Math.pow(num - mean, 2);
      }

      return Math.sqrt(standardDeviation / length);
   }

   public static void computeHRVData(final TourData tourData) {
      if (tourData == null || tourData.pulseTime_Milliseconds == null || tourData.pulseTime_Milliseconds.length == 0) {
         return;
      }

      tourData.rrAvg_ms_Raw = Arrays.stream(tourData.pulseTime_Milliseconds).average().orElse(Double.NaN);
      tourData.rrMax_ms_Raw = Arrays.stream(tourData.pulseTime_Milliseconds).max().orElse(-1);
      tourData.rrMin_ms_Raw = Arrays.stream(tourData.pulseTime_Milliseconds).min().orElse(-1);
      final int[] values = tourData.pulseTime_Milliseconds;
      if (values.length > 1) {
         //         float num1 = values[0];
         //         double num2 = 0.0;
         //         int num3 = 0;
         //         for (int index = 1; index < values.length; ++index) {
         //            final double num4 = (double) values[index] - (double) num1;
         //            num2 += num4 * num4;
         //            num1 = values[index];
         //            if (num4 > 50.0 || num4 < -50.0) {
         //               ++num3;
         //            }
         //         }
         final Compute_Rmssd_Pnn50_Result result = calculate_RMSSD_PNN50(values);
         tourData.rmssd_ms_Raw = result.rmssd;// (double) Math.sqrt(num2 / (values.length - 1));
         tourData.pnn50_Raw = result.pnn50;// 1f * (double) num3 / (values.length - 1);
         tourData.rrStdDev_ms_Raw = calculateStandardDeviation(tourData.pulseTime_Milliseconds);
      }

      //cleaned
      final Double[] rr_intervals = new Double[tourData.pulseTime_Milliseconds.length];
      Double[] nn_intervals = null;
      final Integer low_rri = 300;
      final Integer high_rri = 2000;
      final String limit_area = "";
      final String limit_direction = "forward";
      final String interpolation_method = "linear";
      final String ectopic_beats_removal_method = KAMATH_RULE;
      for (int i = 0; i < tourData.pulseTime_Milliseconds.length; i++) {
         rr_intervals[i] = Double.valueOf(tourData.pulseTime_Milliseconds[i]);
      }
      nn_intervals = get_nn_intervals(rr_intervals,
            low_rri,
            high_rri,
            limit_area,
            limit_direction,
            interpolation_method,
            ectopic_beats_removal_method,
            true);
      if (nn_intervals != null) {
         Double sum = 0.0;
         int count = 0;
         tourData.rrMin_ms = -1;
         tourData.rrMax_ms = -1;
         for (final Double nn_interval : nn_intervals) {
            if (nn_interval != null && !nn_interval.isNaN()) {
               sum += nn_interval;
               count++;
               if (tourData.rrMax_ms == -1) {
                  tourData.rrMax_ms = nn_interval.intValue();
               } else if (nn_interval > tourData.rrMax_ms) {
                  tourData.rrMax_ms = nn_interval.intValue();
               }
               if (tourData.rrMin_ms == -1) {
                  tourData.rrMin_ms = nn_interval.intValue();
               } else if (nn_interval < tourData.rrMin_ms) {
                  tourData.rrMin_ms = nn_interval.intValue();
               }
            }
         }
         if (count == 0) {
            tourData.rrAvg_ms = Double.NaN;
         } else {
            tourData.rrAvg_ms = sum / count;
         }

         if (nn_intervals.length > 1) {
            final Compute_Rmssd_Pnn50_Result result = calculateRMSSD(nn_intervals);
            tourData.rmssd_ms = result.rmssd;// (double) Math.sqrt(num2 / (values.length - 1));
            tourData.pnn50 = result.pnn50;// 1f * (double) num3 / (values.length - 1);
            tourData.rrStdDev_ms = calculateStandardDeviation(nn_intervals);
         } else {
            System.out.println("!!!!!!ERROR nn_intervals is <= 1!!!!!");
         }
      } else {
         System.out.println("!!!!!!ERROR computing nn_intervals out of givne rr_inetrvals!!!!!");
      }
   }

   public static Double[] get_nn_intervals(final Double[] rr_intervals,
                                           Integer low_rri,
                                           Integer high_rri,
                                           String limit_area,
                                           String limit_direction,
                                           String interpolation_method,
                                           String ectopic_beats_removal_method,
                                           final boolean verbose) {
      //   def get_nn_intervals(rr_intervals: List[float], low_rri: int = 300, high_rri: int = 2000,
      //         limit_area: str = None, limit_direction: str = "forward",
      //         interpolation_method: str = "linear", ectopic_beats_removal_method: str = KAMATH_RULE,
      //         verbose: bool = True) -> List[float]:

      //      """
      //      Function that computes NN Intervals from RR-intervals.
      //
      //      Parameters
      //      ---------
      //      rr_intervals : list
      //          RrIntervals list.
      //      interpolation_method : str
      //          Method used to interpolate Nan values of series.
      //      ectopic_beats_removal_method : str
      //          method to use to clean outlier. malik, kamath, karlsson, acar or custom.
      //      low_rri : int
      //          lowest RrInterval to be considered plausible.
      //      high_rri : int
      //          highest RrInterval to be considered plausible.
      //      limit_area: str
      //          If limit is specified, consecutive NaNs will be filled with this restriction.
      //      limit_direction: str
      //          If limit is specified, consecutive NaNs will be filled in this direction.
      //      verbose : bool
      //          Print information about deleted outliers.
      //
      //      Returns
      //      ---------
      //      interpolated_nn_intervals : list
      //          list of NN Interval interpolated
      //      """

      if (low_rri == null) {
         low_rri = 300;
      }
      if (high_rri == null) {
         high_rri = 2000;
      }
      if (limit_direction == null) {
         limit_direction = "forward";
      }
      if (limit_area == null) {
         limit_area = "";
      }

      if (interpolation_method == null) {
         interpolation_method = "linear";
      }
      if (ectopic_beats_removal_method == null) {
         ectopic_beats_removal_method = KAMATH_RULE;
      }

      Double[] interpolated_nn_intervals = null;
      final Double[] rr_intervals_cleaned;
      final Double[] interpolated_rr_intervals;
      final Remove_Outlier_Result nn_intervals;

      rr_intervals_cleaned = remove_outliers(rr_intervals, verbose, low_rri, high_rri);
      interpolated_rr_intervals = interpolate_nan_values(rr_intervals_cleaned,
            interpolation_method,
            limit_area,
            limit_direction,
            null);
      try {
         nn_intervals = remove_ectopic_beats(interpolated_rr_intervals,
               ectopic_beats_removal_method,
               null,
               verbose);
      } catch (final Exception e) {
         e.printStackTrace();
         return null;
      }
      interpolated_nn_intervals = interpolate_nan_values(nn_intervals.nn_intervals,
            interpolation_method,
            limit_area,
            limit_direction,
            null);

      return interpolated_nn_intervals;
   }

   public static Double[] interpolate_nan_values(final Double[] rr_intervals,
                                                 final String interpolation_method,
                                                 final String limit_area,
                                                 final String limit_direction,
                                                 final Double limit) {
      //def interpolate_nan_values(rr_intervals: list,
      //interpolation_method: str = "linear",
      //limit_area: str = None,
      //limit_direction: str = "forward",
      //limit=None,) -> list:
      Double[] resultArray;

      //      """
      //      Function that interpolate Nan values with linear interpolation
      //
      //      Parameters
      //      ---------
      //      rr_intervals : list
      //          RrIntervals list.
      //      interpolation_method : str
      //          Method used to interpolate Nan values of series.
      //      limit_area: str
      //          If limit is specified, consecutive NaNs will be filled with this restriction.
      //      limit_direction: str
      //          If limit is specified, consecutive NaNs will be filled in this direction.
      //      limit: int
      //          TODO
      //      Returns
      //      ---------
      //      interpolated_rr_intervals : list
      //          new list with outliers replaced by interpolated values.
      //      """

      try {
         resultArray = interpolate_nan_values_linear(rr_intervals,
               limit,
               limit_area,
               limit_direction);
      } catch (final Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         resultArray = null;
      }

      return resultArray;
   }

   public static Double[] interpolate_nan_values_linear(final Double[] values,
                                                        final Double limit,
                                                        final String limit_area,
                                                        final String limit_direction) throws Exception {

      final Double[] resultArray = new Double[values.length];
      int startIdx = 0;
      for (final Double value : values) {
         if (value.isNaN()) {
            startIdx++;
         } else {
            break;//found the frist non NaN value end here
         }
      }

      if (startIdx >= values.length) {
         throw new Exception("array contains only Double.Nan values");
      }

      for (int idx = 0; idx <= startIdx; idx++) {//fill first Nan value with first non NaN value
         resultArray[idx] = values[startIdx];
      }

      int endIdx = values.length -1;
      for (int idx = values.length -1; idx > -1; idx--) {
         if (values[idx].isNaN()) {
            endIdx--;
         } else {
            break;//found the last non NaN value end here
         }
      }

      if (endIdx < 0) {
         throw new Exception("array contains only Double.Nan values");
      }

      for (int idx = values.length - 1; idx >= endIdx; idx--) {//fill last Nan value with first non NaN value
         resultArray[idx] = values[endIdx];
      }

      //linear interpolate remaining NaN and supposing equal spacing for the data
      int lastGoodIdx = startIdx;
      for (int idx = startIdx + 1; idx < endIdx; idx++) {
         if (!values[idx].isNaN()) {//current index is not a Nan so check the previous one
            resultArray[idx] = values[idx];
            if (values[idx - 1].isNaN()) {//need to interpolate all the previous NaN, since the current index is not a NaN
               final Double increment = (values[idx] - values[lastGoodIdx]) / (idx - lastGoodIdx);
               lastGoodIdx = idx;
               for (int j = lastGoodIdx + 1; j < idx; j++) {
                  resultArray[j] = resultArray[j - 1] + increment;
               }
            }
         }
      }

      return resultArray;
   }

   public static boolean is_outlier(final Double rr_interval,
                                    final Double next_rr_interval,
                                    String method,
                                    Double custom_rule) {
      //def is_outlier(rr_interval: int, next_rr_interval: float, method: str = "malik",
      //      custom_rule: float = 0.2) -> bool:
      if (custom_rule == null) {
         custom_rule = 0.2;
      }

      if (method == null) {
         method = "malik";
      }

      //      """
      //      Test if the rr_interval is an outlier
      //
      //      Parameters
      //      ----------
      //      rr_interval : int
      //          RrInterval
      //      next_rr_interval : int
      //          consecutive RrInterval
      //      method : str
      //          method to use to clean outlier. malik, kamath, karlsson, acar or custom
      //      custom_rule : int
      //          percentage criteria of difference with previous RR-interval at which we consider
      //          that it is abnormal
      //
      //      Returns
      //      ----------
      //      outlier : bool
      //          True if RrInterval is valid, False if not
      //      """
      boolean outlier = false;

      if (method.compareToIgnoreCase(MALIK_RULE) == 0) {
         outlier = Math.abs(rr_interval - next_rr_interval) <= 0.2 * rr_interval ? true : false;
      } else if (method.compareToIgnoreCase(KAMATH_RULE) == 0) {
         outlier = (0 <= (next_rr_interval - rr_interval) && (next_rr_interval - rr_interval) <= 0.325 * rr_interval) || (0 <= (rr_interval
               - next_rr_interval) && (rr_interval - next_rr_interval) <= 0.245 * rr_interval) ? true : false;
      } else {
         outlier = Math.abs(rr_interval - next_rr_interval) <= custom_rule * rr_interval ? true : false;
      }

      return outlier;
   }

   public static Remove_Outlier_Result remove_ectopic_beats(final Double[] rr_intervals,
                                                            String method,
                                                            Double custom_removing_rule,
                                                            final boolean verbose) throws Exception {
      // def remove_ectopic_beats(rr_intervals: List[float], method: str = "malik",
      // custom_removing_rule: float = 0.2, verbose: bool = True) -> list:
      final ArrayList<Double> nn_intervals = new ArrayList<>();
      if (method == null) {
         method = MALIK_RULE;
      }
      if (custom_removing_rule == null) {
         custom_removing_rule = 0.2;
      }
      Remove_Outlier_Result result = new Remove_Outlier_Result();
      result.outlier_count = 0;
      //      """
      //      RR-intervals differing by more than the removing_rule from the one proceeding it are removed.
      //
      //      Parameters
      //      ---------
      //      rr_intervals : list
      //          list of RR-intervals
      //      method : str
      //          method to use to clean outlier. malik, kamath, karlsson, acar or custom.
      //      custom_removing_rule : int
      //          Percentage criteria of difference with previous RR-interval at which we consider
      //          that it is abnormal. If method is set to Karlsson, it is the percentage of difference
      //          between the absolute mean of previous and next RR-interval at which  to consider the beat
      //          as abnormal.
      //      verbose : bool
      //          Print information about ectopic beats.
      //
      //      Returns
      //      ---------
      //      nn_intervals : list
      //          list of NN Interval
      //      outlier_count : int
      //          Count of outlier detected in RR-interval list
      //
      //      References
      //      ----------
      //      .. [5] Kamath M.V., Fallen E.L.: Correction of the Heart Rate Variability Signal for Ectopics \
      //      and Miss- ing Beats, In: Malik M., Camm A.J.
      //
      //      .. [6] Geometric Methods for Heart Rate Variability Assessment - Malik M et al
      //      """

      if (method.compareToIgnoreCase(KARLSSON_RULE) == 0) {
         result = _remove_outlier_karlsson(rr_intervals, custom_removing_rule);
      } else if (method.compareToIgnoreCase(ACAR_RULE) == 0) {
         final Double rule = null;
         result = _remove_outlier_acar(rr_intervals, rule);
      } else if (method.compareToIgnoreCase(KAMATH_RULE) == 0 ||
            method.compareToIgnoreCase(MALIK_RULE) == 0 ||
            method.compareToIgnoreCase(CUSTOM_RULE) == 0) {

         //# set first element in list
         result.outlier_count = 0;
         boolean previous_outlier = false;
         nn_intervals.add(rr_intervals[0]);
         for (int i = 0; i < rr_intervals.length - 1; i++) {
            final Double rr_interval = rr_intervals[i];

            if (previous_outlier) {
               nn_intervals.add(rr_intervals[i + 1]);
               previous_outlier = false;
               continue;
            }

            if (is_outlier(rr_interval, rr_intervals[i + 1], method, custom_removing_rule)) {
               nn_intervals.add(rr_intervals[i + 1]);
            } else {
               nn_intervals.add(Double.NaN);
               result.outlier_count += 1;
               previous_outlier = true;
            }
         }

         Double[] resultArray = new Double[nn_intervals.size()];
         resultArray = nn_intervals.toArray(resultArray);
         result.nn_intervals = resultArray;

      } else {
         System.out.println("Not a valid method. Please choose between malik, kamath, karlsson, acar."
               + "You can also choose your own removing critera with custom_rule parameter.");
         throw new Exception("Not a valid 'method' input parameter, should be one of: malik, kamath, karlsson, acar, custom");
      }

      if (verbose) {
         System.out.println(result.outlier_count + " ectopic beat(s) have been deleted with " + method + " rule.");
      }

      return result;
   }

   public static Double[] remove_outliers(final Double[] rr_intervals, final boolean verbose, Integer low_rri, final Integer high_rri) {

      if (low_rri == null) {
         low_rri = 300;
      }
      if (high_rri == null) {
         low_rri = 2000;
      }
      //   def remove_outliers(rr_intervals: List[float], verbose: bool = True, low_rri: int = 300,
      //         high_rri: int = 2000) -> list:
      //"""
      //Function that replace RR-interval outlier by nan.
      //
      //Parameters
      //---------
      //rr_intervals : list
      //raw signal extracted.
      //low_rri : int
      //lowest RrInterval to be considered plausible.
      //high_rri : int
      //highest RrInterval to be considered plausible.
      //verbose : bool
      //Print information about deleted outliers.
      //
      //Returns
      //---------
      //rr_intervals_cleaned : list
      //list of RR-intervals without outliers
      //
      //References
      //----------
      //.. [1] O. Inbar, A. Oten, M. Scheinowitz, A. Rotstein, R. Dlin, R.Casaburi. Normal \
      //cardiopulmonary responses during incremental exercise in 20-70-yr-old men.
      //
      //.. [2] W. C. Miller, J. P. Wallace, K. E. Eggert. Predicting max HR and the HR-VO2 relationship\
      //for exercise prescription in obesity.
      //
      //.. [3] H. Tanaka, K. D. Monahan, D. R. Seals. Age-predictedmaximal heart rate revisited.
      //
      //.. [4] M. Gulati, L. J. Shaw, R. A. Thisted, H. R. Black, C. N. B.Merz, M. F. Arnsdorf. Heart \
      //rate response to exercise stress testing in asymptomatic women.
      //"""
      final Double[] rr_intervals_cleaned = new Double[rr_intervals.length];
      int nan_count = 0;
      final ArrayList<Double> outliers_list = new ArrayList<>();

      //# Conversion RrInterval to Heart rate ==> rri (ms) =  1000 / (bpm / 60)
      //# rri 2000 => bpm 30 / rri 300 => bpm 200
      //rr_intervals_cleaned = [rri if high_rri >= rri >= low_rri else np.nan for rri in rr_intervals]

      //if verbose:
      //outliers_list = []
      //for rri in rr_intervals:
      // if high_rri >= rri >= low_rri:
      //     pass
      // else:
      //     outliers_list.append(rri)
      //
      //nan_count = sum(np.isnan(rr_intervals_cleaned))
      //if nan_count == 0:
      // print("{} outlier(s) have been deleted.".format(nan_count))
      //else:
      // print("{} outlier(s) have been deleted.".format(nan_count))
      // print("The outlier(s) value(s) are : {}".format(outliers_list))
      for (int idx = 0; idx < rr_intervals.length; idx++) {

         if (rr_intervals[idx] >= low_rri && rr_intervals[idx] <= high_rri) {
            rr_intervals_cleaned[idx] = rr_intervals[idx];
         } else {
            rr_intervals_cleaned[idx] = Double.NaN;
            nan_count++;
            outliers_list.add(rr_intervals[idx]);
         }
      }

      if (nan_count == 0) {
         System.out.println(CLASS_NAME_LOG + " No outliers to be deleted");
      } else {
         System.out.println(CLASS_NAME_LOG + "Nbr of outliers deleted" + nan_count);
      }

      return rr_intervals_cleaned;
   }
}
