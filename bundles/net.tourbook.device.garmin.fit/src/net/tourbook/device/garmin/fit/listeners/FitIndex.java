package net.tourbook.device.garmin.fit.listeners;

public class FitIndex {
   public Integer fieldNbr = null;
   public Integer arrayIdx = null;

   public FitIndex(final Integer fitNbr, final Integer arrayIndex) {
      fieldNbr = fitNbr;
      arrayIdx = arrayIndex;
   }
}

