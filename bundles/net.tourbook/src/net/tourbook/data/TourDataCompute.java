package net.tourbook.data;

import java.util.ArrayList;
import java.util.Arrays;

public class TourDataCompute {
   public static final String CLASS_NAME_LOG = "TourDataCompute";
   public static final String MALIK_RULE     = "malik";
   public static final String KAMATH_RULE    = "kamath";

   public static double calculateStandardDeviation(final double[] array) {

      if(array == null || array.length == 0) {
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

      tourData.rrAvg_ms = Arrays.stream(tourData.pulseTime_Milliseconds).average().orElse(Double.NaN);
      tourData.rrMax_ms = Arrays.stream(tourData.pulseTime_Milliseconds).max().orElse(-1);
      tourData.rrMin_ms = Arrays.stream(tourData.pulseTime_Milliseconds).min().orElse(-1);
      final int[] values = tourData.pulseTime_Milliseconds;
      if (values.length > 1) {
         float num1 = values[0];
         double num2 = 0.0;
         int num3 = 0;
         for (int index = 1; index < values.length; ++index) {
            final double num4 = (double) values[index] - (double) num1;
            num2 += num4 * num4;
            num1 = values[index];
            if (num4 > 50.0 || num4 < -50.0) {
               ++num3;
            }
         }
         tourData.rmssd_ms = (double) Math.sqrt(num2 / (values.length - 1));
         tourData.pnn50 = 1f * (double) num3 / (values.length - 1);
         tourData.rrStdDev_ms = calculateStandardDeviation(tourData.pulseTime_Milliseconds);
      }
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
