package net.tourbook.data;

import java.util.Arrays;

public class TourDataCompute {

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
}
