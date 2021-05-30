/*******************************************************************************
 * Copyright (C) 2005, 2021 Wolfgang Schramm and Contributors
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
package net.tourbook.device.csv.tours;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.data.TourData;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourType;
import net.tourbook.database.TourDatabase;
import net.tourbook.importdata.DeviceData;
import net.tourbook.importdata.SerialParameters;
import net.tourbook.importdata.TourbookDevice;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.preferences.TourTypeColorDefinition;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;

import org.eclipse.swt.widgets.Display;

public class CSVTourDataReader extends TourbookDevice {

   //
   // csv import data samples
   //
   // Date (yyyy-mm-dd); Time (hh-mm); Duration (sec); Paused Time (sec), Distance (m); Title; Comment; Tour Type; Tags;
   // 2008-09-02;08-20;1200;300;8500;zur Arbeit;kein Kommentar, siehe n�chste Tour;Rennvelo;Arbeitsfahrt am Abend, new tag
   // 2008-09-01;14-30;1500;20;6000;auf Fremersberg;;MTB;FB
   // 2008-08-28;18-00;780;120;12000;Feierabendrunde;;TestTourType;no tags

   private static final String TOUR_CSV_ID   =
         "Date (yyyy-mm-dd); Time (hh-mm); Duration (sec); Paused Time (sec), Distance (m); Title; Comment; Tour Type; Tags;"; //$NON-NLS-1$

   /**
    * This header is a modified header for {@link #TOUR_CSV_ID} with a semikolon instead of a komma
    * after
    * <p>
    * <i>Paused Time (sec);</i>
    */
   private static final String TOUR_CSV_ID_2 =
         "Date (yyyy-mm-dd); Time (hh-mm); Duration (sec); Paused Time (sec); Distance (m); Title; Comment; Tour Type; Tags;"; //$NON-NLS-1$
// private static final String   TOUR_CSV_ID       = "Date (yyyy-mm-dd); Time (hh-mm); Duration (sec); Paused Time (sec), Distance (m); Title; Comment; Tour Type; Tags;"; //$NON-NLS-1$

   /**
    *
    */
   private static final String TOUR_CSV_ID_3            = "Date (yyyy-mm-dd); Time (hh-mm); Duration (sec); Paused Time (sec);"                                            //$NON-NLS-1$
         + " Distance (m); Title; Comment; Tour Type; Tags;"                                                                                                               //$NON-NLS-1$
         + " Altitude Up (m); Altitude Down (m);";                                                                                                                         //$NON-NLS-1$

   private static final String TOUR_CSV_ID_4B           =
         "\"Date\";\"Weight [kg]\";\"BMI\";\"RestingHeartRatePerMinute\";\"MaximumHeartRatePerMinute\";\"SystolicBloodPressure\";\"DiastolicBloodPressure\";"              //$NON-NLS-1$
               + "\"CaloriesConsumed\";\"BodyFatPercentage\";\"Skinfold\";\"SleepHours\";\"SleepQuality\";"                                                                //$NON-NLS-1$
               + "\"Mood\";\"Sick\";\"SickText\";\"Injured\";\"InjuredText\";\"MissedWorkout\";\"MissedWorkoutText\";"                                                     //$NON-NLS-1$
               + "\"DiaryText\";\"#Sleep Resting 1\";\"#Sleep Resting 2\";\"#activity\";\"#Source1\";\"#Source2\";"                                                        //$NON-NLS-1$
               + "\"#Sleep Total 1\";\"#Sleep Deep 1\";\"#Sleep Paradoxal 1\";\"#Sleep Light 1\";\"#Awake 1\";"                                                            //$NON-NLS-1$
               + "\"#Sleep Unknown 1\";\"#Steps1\";\"#Pts1\";\"#Calories1\";\"#Distance1(m)\";\"#Calories2\";"                                                             //$NON-NLS-1$
               + "\"#Steps2\";\"#Sleep Total 2\";\"#Sleep Paradoxal 2\";\"#Sleep Deep 2\";\"#Sleep Light 2\";"                                                             //$NON-NLS-1$
               + "\"#Sleep Unknown 2\";\"#Awake 2\";\"#Distance2(m)\";\"#pompages\";\"#CalorieMotoACTV\";"                                                                 //$NON-NLS-1$
               + "\"#StepCountMotoACTV\";\"#Recovery time\";\"#HR Recovery Decrease [bpm]\";"                                                                              //$NON-NLS-1$
               + "\"#V02max Cycling\";\"#V02max Running\";\"#temperature_day\";\"#temperature_day_DB\";"                                                                   //$NON-NLS-1$
               + "\"#temperature_day_Condition\";\"#SleepHour_during_day\";\"#SleepScore(%)\";"                                                                            //$NON-NLS-1$
               + "\"#SleepRespRythm [cpm]\";\"#SleepMovement\";\"#SleepDelay\"";                                                                                           //$NON-NLS-1$

   private static final String TOUR_CSV_ID_4            =
         "\"Date\";\"Weight [kg]\";\"BMI\";\"RestingHeartRatePerMinute\";\"MaximumHeartRatePerMinute\";\"SystolicBloodPressure\";\"DiastolicBloodPressure\";"              //$NON-NLS-1$
               + "\"CaloriesConsumed\";\"BodyFatPercentage\";\"Skinfold\";\"SleepHours\";\"SleepQuality\";"                                                                //$NON-NLS-1$
               + "\"Mood\";\"Sick\";\"SickText\";\"Injured\";\"InjuredText\";\"MissedWorkout\";\"MissedWorkoutText\";"                                                     //$NON-NLS-1$
               + "\"DiaryText\";";                                                                                                                                         //$NON-NLS-1$

   private static final String ST3_TAG_DATE             = "\"Date\"";

   private static final String ST3_TAG_WEIGHT           = "\"Weight [kg]\"";

   private static final String ST3_TAG_BODYFAT          = "\"BodyFatPercentage\"";
   private static final String ST3_TAG_RESTHR           = "\"RestingHeartRatePerMinute\"";
   private static final String ST3_TAG_WEATHER          = "\"#temperature_day\"";
   private static final String ST3_TAG_WEATHER_DB       = "\"#temperature_day_DB\"";
   private static final String ST3_TAG_CALORIES1        = "\"#Calories1\"";
   private static final String ST3_TAG_CALORIES2        = "\"#Calories2\"";
   private static final String ST3_TAG_DISTANCE1        = "\"#Distance1(m)\"";
   private static final String ST3_TAG_DISTANCE2        = "\"#Distance2(m)\"";
   private static final String ST3_TAG_CALORIESMOTOACTV = "\"#CalorieMotoACTV\"";
   private static final String ST3_TAG_WEATHER_DAY_COND = "\"#temperature_day_Condition\"";
   private static final String ST3_TAG_SLEEP_AWAKE1     = "\"#Awake 1\"";
   private static final String ST3_TAG_SLEEP_AWAKE2     = "\"#Awake 2\"";
   private static final String ST3_TAG_SLEEP_TOTAL1     = "\"#Sleep Total 1\"";
   private static final String ST3_TAG_SLEEP_TOTAL2     = "\"#Sleep Total 2\"";
   private static final String ST3_TAG_SLEEP_DEEP1      = "\"#Sleep Deep 1\"";
   private static final String ST3_TAG_SLEEP_DEEP2      = "\"#Sleep Deep 2\"";
   private static final String ST3_TAG_SLEEP_REM1       = "\"#Sleep Paradoxal 1\"";
   private static final String ST3_TAG_SLEEP_REM2       = "\"#Sleep Paradoxal 2\"";
   private static final String ST3_TAG_SLEEP_LIGHT1     = "\"#Sleep Light 1\"";
   private static final String ST3_TAG_SLEEP_LIGHT2     = "\"#Sleep Light 2\"";
   private static final String ST3_TAG_SLEEP_UNKNOW1    = "\"#Sleep Unknown 1\"";
   private static final String ST3_TAG_SLEEP_UNKNOW2    = "\"#Sleep Unknown 2\"";
   private static final String ST3_TAG_SLEEP_REST1      = "\"#Sleep Resting 1\"";
   private static final String ST3_TAG_SLEEP_REST2      = "\"#Sleep Resting 2\"";
   private static final String CSV_TOKEN_SEPARATOR      = ";";                                                                                                             //$NON-NLS-1$
   private static final String CSV_TAG_SEPARATOR        = ",";                                                                                                             //$NON-NLS-1$

   private static final char   CSV_TOKEN_SEPARATOR_CHAR = ';';

   private boolean             _isId1;

   private boolean             _isId2;

   private boolean             _isId3;

   private boolean             _isId4;

   private class DateTimeData {
      public int year;
      public int month;
      public int day;
      public int hour;
      public int minute;
   }

   private class WeatherData {
      public Float minDegree       = null;
      public Float maxDegree       = null;
      public Float avgDegree       = null;
      public Float humidityPercent = null;
      public Float precipitationmm = null;
   }

   public CSVTourDataReader() {
      // plugin constructor
   }
   /**
    * provides a String representation of the given time
    *
    * @return {@code millis} in hh:mm:ss format
    */
   public static final String secondsToHHMMSS(final long secs) {
      return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
   }

   @Override
   public String buildFileNameFromRawData(final String rawDataFileName) {
      return null;
   }

   public ArrayList<Integer> buildST3DescFieldIdx(final String header) {
      final ArrayList<Integer> st3List = new ArrayList<>();
      final String[] allTokenH = header.split(CSV_TOKEN_SEPARATOR);
      for (int i = 0; i < allTokenH.length; i++) {
         if (allTokenH[i].compareTo(ST3_TAG_BODYFAT) != 0 &&
               allTokenH[i].compareTo(ST3_TAG_DATE) != 0 &&
               allTokenH[i].compareTo(ST3_TAG_RESTHR) != 0 &&
               allTokenH[i].compareTo(ST3_TAG_WEIGHT) != 0 &&
               allTokenH[i].compareTo(ST3_TAG_WEATHER_DAY_COND) != 0 &&
               allTokenH[i].compareTo(ST3_TAG_WEATHER) != 0 &&
               allTokenH[i].compareTo(ST3_TAG_WEATHER_DB) != 0) {
            st3List.add(i);
         }
      }
      return st3List;
   }

   public ArrayList<Integer> buildST3DescSleepFieldIdx(final String header) {
      final ArrayList<Integer> st3List = new ArrayList<>();
      final String[] allTokenH = header.split(CSV_TOKEN_SEPARATOR);
      for (int i = 0; i < allTokenH.length; i++) {
         if (allTokenH[i].compareTo(ST3_TAG_SLEEP_AWAKE1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_AWAKE2) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_TOTAL1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_TOTAL2) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_DEEP1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_DEEP2) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_REM1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_REM2) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_LIGHT1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_LIGHT2) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_UNKNOW1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_UNKNOW2) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_REST1) == 0 ||
               allTokenH[i].compareTo(ST3_TAG_SLEEP_REST2) == 0) {
            st3List.add(i);
         }
      }
      return st3List;
   }

   public Map<String, Integer> buildST3TagMap(final String header) {
      final Map<String, Integer> st3MapTag = new HashMap<>();

      final String[] allTokenH = header.split(CSV_TOKEN_SEPARATOR);
      for (int i = 0; i < allTokenH.length; i++) {
         st3MapTag.put(allTokenH[i], i);
      }

      return st3MapTag;
   }

   public Map<Integer, String> buildST3TagMapRev(final String header) {
      final Map<Integer, String> st3MapTag = new HashMap<>();

      final String[] allTokenH = header.split(CSV_TOKEN_SEPARATOR);
      for (int i = 0; i < allTokenH.length; i++) {
         st3MapTag.put(i, allTokenH[i]);
      }

      return st3MapTag;
   }

   @Override
   public boolean checkStartSequence(final int byteIndex, final int newByte) {
      return false;
   }

   @Override
   public String getDeviceModeName(final int profileId) {
      return null;
   }

   @Override
   public SerialParameters getPortParameters(final String portName) {
      return null;
   }

   @Override
   public int getStartSequenceSize() {
      return -1;
   }

   @Override
   public int getTransferDataSize() {
      return -1;
   }

   private boolean isFileValid(final String fileHeader) {

      _isId1 = fileHeader.startsWith(TOUR_CSV_ID);
      _isId2 = fileHeader.startsWith(TOUR_CSV_ID_2);
      _isId3 = fileHeader.startsWith(TOUR_CSV_ID_3);
      _isId4 = fileHeader.startsWith(TOUR_CSV_ID_4);

      if (_isId1 || _isId2 || _isId3 || _isId4) {
         return true;
      }

      return false;
   }

   private void parseDate(final DateTimeData dateTime, final String nextToken) {

      // Date (yyyy-mm-dd)
      dateTime.year = parseInteger(nextToken.substring(0, 4));
      dateTime.month = parseInteger(nextToken.substring(5, 7));
      dateTime.day = parseInteger(nextToken.substring(8, 10));
   }

   private void parseDateST3(final DateTimeData dateTime, final String nextToken) {
      // Date ("dd-mm-yy")
      //remove starting and ending " if any!!
      int offset = 1;
      if (nextToken.startsWith("\"")) {
         offset = 0;
      }
      dateTime.year = parseInteger(nextToken.substring(7 - offset, 9 - offset)) + 2000;
      dateTime.month = parseInteger(nextToken.substring(4 - offset, 6 - offset));
      dateTime.day = parseInteger(nextToken.substring(1 - offset, 3 - offset));

      dateTime.hour = 0;
      dateTime.minute = 0;

   }

   private float parseFloatST3(final String string) {//string assume to be starting with "\""

      try {
         int offset = 1;
         if (string.startsWith("\"")) {
            offset = 0;
         }
         return Float.parseFloat(string.substring(1 - offset, string.length() - 1 + offset).replace(',', '.'));
      } catch (final Exception e) {
         return (float) 0.0;
      }
   }

   /**
    * @param string
    * @return Returns parsed integer value or 0 when the string argument do not contain a valid
    *         value.
    */
   private int parseInteger(final String string) {

      try {
         return Integer.parseInt(string);
      } catch (final Exception e) {
         return 0;
      }
   }

   private int parseIntegerST3(final String stringInput) {//string must start with "\"" !!
      String string = "";
      if (!string.startsWith("\"")) {
         string = "\"" + stringInput + "\"";
      } else {
         string = stringInput;
      }

      try {
         return Integer.parseInt(string.substring(1, string.length() - 1));
      } catch (final Exception e) {
         return 0;
      }
   }

   /**
    * @param tourData
    * @param tagToken
    * @return <code>true</code> when a new tag is created
    */
   private boolean parseTags(final TourData tourData, final String tagToken) {

      boolean isNewTag = false;

      final StringTokenizer tokenizer = new StringTokenizer(tagToken, CSV_TAG_SEPARATOR);
      final Set<TourTag> tourTags = new HashSet<>();

      HashMap<Long, TourTag> tourTagMap = TourDatabase.getAllTourTags();
      TourTag[] allTourTags = tourTagMap.values().toArray(new TourTag[tourTagMap.size()]);

      try {

         String tagLabel;

         while ((tagLabel = tokenizer.nextToken()) != null) {

            tagLabel = tagLabel.trim();
            boolean isTagAvailable = false;

            for (final TourTag tourTag : allTourTags) {
               if (tourTag.getTagName().equals(tagLabel)) {

                  // existing tag is found

                  isTagAvailable = true;

                  tourTags.add(tourTag);
                  break;
               }
            }

            if (isTagAvailable == false) {

               // create a new tag

               final TourTag tourTag = new TourTag(tagLabel);
               tourTag.setRoot(true);

               // persist tag
               final TourTag savedTag = TourDatabase.saveEntity(
                     tourTag,
                     TourDatabase.ENTITY_IS_NOT_SAVED,
                     TourTag.class);

               if (savedTag != null) {

                  tourTags.add(savedTag);

                  // reload tour tag list

                  TourDatabase.clearTourTags();

                  tourTagMap = TourDatabase.getAllTourTags();
                  allTourTags = tourTagMap.values().toArray(new TourTag[tourTagMap.size()]);

                  isNewTag = true;
               }
            }
         }

      } catch (final NoSuchElementException e) {
         // no further tokens
      } finally {

         tourData.setTourTags(tourTags);
      }

      return isNewTag;

   }

   private void parseTime(final DateTimeData dateTime, final String nextToken) {

      // Time (hh-mm)
      dateTime.hour = parseInteger(nextToken.substring(0, 2));
      dateTime.minute = parseInteger(nextToken.substring(3, 5));
   }

   /**
    * @param tourData
    * @param parsedTourTypeLabel
    * @return <code>true</code> when a new {@link TourType} is created
    */
   private boolean parseTourType(final TourData tourData, final String parsedTourTypeLabel) {

      final ArrayList<TourType> tourTypeMap = TourDatabase.getAllTourTypes();
      TourType tourType = null;

      // find tour type in existing tour types
      for (final TourType mapTourType : tourTypeMap) {
         if (parsedTourTypeLabel.equalsIgnoreCase(mapTourType.getName())) {
            tourType = mapTourType;
            break;
         }
      }

      TourType newSavedTourType = null;

      if (tourType == null) {

         // create new tour type

         final TourType newTourType = new TourType(parsedTourTypeLabel);

         final TourTypeColorDefinition newColorDef = new TourTypeColorDefinition(
               newTourType,
               Long.toString(newTourType.getTypeId()),
               newTourType.getName());

         newTourType.setColor_Gradient_Bright(newColorDef.getGradientBright_Default());
         newTourType.setColor_Gradient_Dark(newColorDef.getGradientDark_Default());
         newTourType.setColor_Line(newColorDef.getLineColor_Default_Light(), newColorDef.getLineColor_Default_Dark());
         newTourType.setColor_Text(newColorDef.getTextColor_Default_Light(), newColorDef.getTextColor_Default_Dark());

         // save new entity
         newSavedTourType = TourDatabase.saveEntity(newTourType, newTourType.getTypeId(), TourType.class);
         if (newSavedTourType != null) {

            tourType = newSavedTourType;

            TourDatabase.clearTourTypes();
            TourManager.getInstance().clearTourDataCache();
         }
      }

      tourData.setTourType(tourType);

      return newSavedTourType != null;
   }

   private WeatherData parseWeatherST3(final String weatherInput) {//string must starts with " !!!
      final WeatherData res = new WeatherData();
      String weather = "";
      if (!weatherInput.startsWith("\"")) {
         weather = "\"" + weatherInput + "\"";
      } else {
         weather = weatherInput;
      }

      if (weather.contains("Humidity")) {
         //format: "4,4�/10,2�C-Humidity:80,9%"
         //or "-1,3�/5,4�C-Precipitation:3,49mm-Humidity:89,6%"
         int idxT1 = weather.indexOf('�');
         int idxT = weather.indexOf('/');
         int idxT2 = weather.indexOf('�', idxT1 + 1);
         if (idxT1 > 0 && idxT > 0 && idxT2 > 0) {
            res.minDegree = Float.parseFloat(weather.substring(1, idxT1).replace(',', '.'));
            res.maxDegree = Float.parseFloat(weather.substring(idxT + 1, idxT2).replace(',', '.'));
            res.avgDegree = (res.maxDegree + res.minDegree) / 2;
         }
         if (weather.contains("Precipitation")) {
            idxT = weather.indexOf("Precipitation");
            idxT1 = weather.indexOf(':', idxT);
            idxT2 = weather.indexOf('m', idxT1);
            if (idxT1 > 0 && idxT > 0 && idxT2 > 0) {
               res.precipitationmm = Float.parseFloat(weather.substring(idxT1 + 1, idxT2).replace(',', '.'));
            }
         }
         idxT = weather.indexOf("Humidity");
         idxT1 = weather.indexOf(':', idxT);
         idxT2 = weather.indexOf('%', idxT1);
         if (idxT1 > 0 && idxT > 0 && idxT2 > 0) {
            res.humidityPercent = Float.parseFloat(weather.substring(idxT1 + 1, idxT2).replace(',', '.'));
         }
      } else if (weather.contains("/")) {
         //"5�/10�C:pluies 0,3mm"
         final int idxT1 = weather.indexOf('�');
         final int idxT = weather.indexOf('/');
         final int idxT2 = weather.indexOf('�', idxT1 + 1);
         if (idxT1 > 0 && idxT > 0 && idxT2 > 0) {
            res.minDegree = Float.parseFloat(weather.substring(1, idxT1).replace(',', '.').trim());
            res.maxDegree = Float.parseFloat(weather.substring(idxT + 1, idxT2).replace(',', '.').trim());
            res.avgDegree = (res.maxDegree + res.minDegree) / 2;
         }
      } else if (weather.contains("�")) {
         final int idxT1 = weather.indexOf('�');
         if (idxT1 > 0) {
            res.avgDegree = Float.parseFloat(weather.substring(1, idxT1).replace(',', '.').trim());
         }
      }
      return res;
   }

   @Override
   public boolean processDeviceData(final String importFilePath,
                                    final DeviceData deviceData,
                                    final Map<Long, TourData> alreadyImportedTours,
                                    final Map<Long, TourData> newlyImportedTours,
                                    final boolean isReimport) {

      boolean returnValue = false;

      boolean isNewTag = false;
      boolean isNewTourType = false;

//      final CSVParser parser = new CSVParserBuilder().withSeparator(CSV_TOKEN_SEPARATOR_CHAR).build();
//      final Path myImportFilePath = Paths.get(importFilePath);
//      List<String[]> listCSVLinesToken = new ArrayList<>();
//      try (var bufferReader = Files.newBufferedReader(myImportFilePath, StandardCharsets.UTF_8);
//            var csvReader = new CSVReaderBuilder(bufferReader).withCSVParser(parser).build();) {
//
//         listCSVLinesToken = csvReader.readAll();
//         csvReader.close();
//      } catch (final Exception e) {
//
//         StatusUtil.log(e);
//
//         return false;
//
//      }

      String fileHeader = "";
      try (FileReader fileReader = new FileReader(importFilePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader)) {

         // check file header
         fileHeader = bufferedReader.readLine();
         if (isFileValid(fileHeader) == false) {
            return false;
         }
      } catch (final Exception e) {

         StatusUtil.log(e);

         return false;

      }

      final CSVParser parser = new CSVParserBuilder().withSeparator(CSV_TOKEN_SEPARATOR_CHAR).build();
      final Path myImportFilePath = Paths.get(importFilePath);
      List<String[]> listCSVLinesToken = new ArrayList<>();
      try (var bufferReader = Files.newBufferedReader(myImportFilePath, StandardCharsets.UTF_8);
            CSVReader csvReader = new CSVReaderBuilder(bufferReader).withCSVParser(parser).build();
      //CSVReader csvReader = new CSVReader(new FileReader(importFilePath), CSV_TOKEN_SEPARATOR_CHAR);
      ) {

         listCSVLinesToken = csvReader.readAll();
         csvReader.close();
//       StringTokenizer tokenizer;
         final String tokenLine;
         Map<String, Integer> St3TagMap = null;
         Map<Integer, String> St3TagMapRev = null;
         ArrayList<Integer> St3ListDesc = null;
         ArrayList<Integer> St3ListSleepDesc = null;
         if (_isId4) {
            St3TagMap = buildST3TagMap(fileHeader);
            St3TagMapRev = buildST3TagMapRev(fileHeader);
            St3ListDesc = buildST3DescFieldIdx(fileHeader);
            St3ListSleepDesc = buildST3DescSleepFieldIdx(fileHeader);
         }
         // read all tours, each line is one tour
         //while ((tokenLine = bufferedReader.readLine()) != null)
         //starting at 1!! to avoid header !!!
         for (int line = 1; line < listCSVLinesToken.size(); line++) {

            int distance = 0;
            int duration = 0;
            int pausedTime = 0;
            boolean isValidTour = false;

            final TourData tourData = new TourData();

            try {

               final DateTimeData dateTime = new DateTimeData();

               /*
                * The split method is used because the Tokenizer ignores empty tokens !!!
                */

               //final String[] allToken = tokenLine.split(CSV_TOKEN_SEPARATOR);
               final String[] allToken = listCSVLinesToken.get(line);

               if (!_isId4) {
                  parseDate(dateTime, allToken[0]);//                      1 Date (yyyy-mm-dd);
                  parseTime(dateTime, allToken[1]);//                      2 Time (hh-mm);
                  tourData.setTourStartTime(
                        dateTime.year,
                        dateTime.month,
                        dateTime.day,
                        dateTime.hour,
                        dateTime.minute,
                        0);
                  isValidTour = true;
                  duration = parseInteger(allToken[2]); //                 3 Duration (sec);
                  tourData.setTourDeviceTime_Elapsed(duration);

                  pausedTime = parseInteger(allToken[3]); //                  4 Paused Time (sec),
                  tourData.setTourDeviceTime_Paused(pausedTime);

                  final int recordedTime = Math.max(0, duration - pausedTime);
                  tourData.setTourDeviceTime_Recorded(recordedTime);
                  tourData.setTourComputedTime_Moving(recordedTime);

                  distance = parseInteger(allToken[4]);//                     5 Distance (m);
                  tourData.setTourDistance(distance);

                  tourData.setTourTitle(allToken[5]);//                    6 Title;
                  tourData.setTourDescription(allToken[6]);//                 7 Comment;

                  isNewTourType |= parseTourType(tourData, allToken[7]);//    8 Tour Type;
                  isNewTag |= parseTags(tourData, allToken[8]);//             9 Tags;

                  if (_isId3) {

                     tourData.setTourAltUp(parseInteger(allToken[9]));//         10 Altitude Up (m);
                     tourData.setTourAltDown(parseInteger(allToken[10]));//      11 Altitude Down (m);
                  }
               } else {
                  if (allToken[0].startsWith("##") || allToken[0].startsWith("\"##")) {
                     continue;//skip line starting with ##
                  }
                  Integer idx = St3TagMap.get(ST3_TAG_DATE);
                  if (idx == null) {
                     continue;
                  }
                  parseDateST3(dateTime, allToken[idx]);//                      1 Date (dd-mm-yy);
                  tourData.setTourStartTime(
                        dateTime.year,
                        dateTime.month,
                        dateTime.day,
                        dateTime.hour,
                        dateTime.minute,
                        0);
                  isValidTour = true;

                  idx = St3TagMap.get(ST3_TAG_WEIGHT);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     tourData.setBodyWeight(val);
                  }

                  idx = St3TagMap.get(ST3_TAG_BODYFAT);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     tourData.setBodyFat(val);
                  }

                  idx = St3TagMap.get(ST3_TAG_RESTHR);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     tourData.setRestPulse((int) val);
                  }

                  //Calories parsing
                  float valCal = 0.0f;
                  idx = St3TagMap.get(ST3_TAG_CALORIES1);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     valCal = val;
                  }
                  idx = St3TagMap.get(ST3_TAG_CALORIES2);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     if (val > valCal) {
                        valCal = val;
                     }
                  }
                  idx = St3TagMap.get(ST3_TAG_CALORIESMOTOACTV);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     if (val > valCal) {
                        valCal = val;
                     }
                  }
                  if (valCal > 0.0f) {
                     tourData.setCalories((int) valCal);
                  }

                  //distance parsing
                  valCal = 0.0f;
                  idx = St3TagMap.get(ST3_TAG_DISTANCE1);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     valCal = val;
                  }
                  idx = St3TagMap.get(ST3_TAG_DISTANCE2);
                  if (idx != null) {
                     final float val = parseFloatST3(allToken[idx]);
                     if (val > valCal) {
                        valCal = val;
                     }
                  }
                  if (valCal > 0.0f) {
                     tourData.setTourDistance(valCal);
                  }
                  //Weather parsing
                  idx = St3TagMap.get(ST3_TAG_WEATHER);
                  if (idx != null) {
                     tourData.setWeather(allToken[idx]);
                     final WeatherData res = parseWeatherST3(allToken[idx]);
                     if (res != null) {
                        if (res.avgDegree != null) {
                           tourData.setAvgTemperature(res.avgDegree);
                        }
                        if (res.minDegree != null) {
                           tourData.setWeather_Temperature_Min(res.minDegree);
                        }
                        if (res.maxDegree != null) {
                           tourData.setWeather_Temperature_Max(res.maxDegree);
                        }

                        if (res.humidityPercent != null) {
                           tourData.setWeather_Humidity((short) ((float) res.humidityPercent));
                        }
                        if (res.precipitationmm != null) {
                           tourData.setWeather_Precipitation(res.precipitationmm);
                        }
                     }
                  }

                  //custom data fields parsing
                  String tourDescr = "";
                  for (final Integer element : St3ListDesc) {
                     if (St3ListSleepDesc.contains(element)) {
                        final int valSleep = parseIntegerST3(allToken[element]);
                        if (valSleep > 0) {
                           final String sSleep = secondsToHHMMSS(valSleep);
                           tourDescr += St3TagMapRev.get(element) + ":\"" + sSleep + "\"\n";
                        } else {
                           tourDescr += St3TagMapRev.get(element) + ":" + allToken[element] + "\n";
                        }
                     } else {
                        tourDescr += St3TagMapRev.get(element) + ":" + allToken[element] + "\n";
                     }
                  }
                  tourData.setTourDescription(tourDescr);
                  tourData.setTourTitle("Daily History");

                  final String tourTypeLabel = "Daily-History";
                  isNewTourType |= parseTourType(tourData, tourTypeLabel);
               }
            } catch (final NoSuchElementException e) {
               // not all tokens are defined
            } finally {

               if (isValidTour) {
                  tourData.setImportFilePath(importFilePath);

                  tourData.setDeviceId(deviceId);
                  tourData.setDeviceName(visibleName);

                  // after all data are added, the tour id can be created
                  final String uniqueId = createUniqueId_Legacy(tourData, distance);
                  final Long tourId = tourData.createTourId(uniqueId);

                  // check if the tour is in the tour map
                  if (alreadyImportedTours.containsKey(tourId) == false) {

                     // add new tour to the map
                     newlyImportedTours.put(tourId, tourData);

                     returnValue = true;
                  }
               }
            }
         }

      } catch (final Exception e) {

         StatusUtil.log(e);

         return false;

      } finally {

         try {
            if (isNewTag) {

               // fire modify event

               Display.getDefault().syncExec(() -> TourManager.fireEvent(TourEventId.TAG_STRUCTURE_CHANGED));

            }

            if (isNewTourType) {

               // fire modify event

               Display.getDefault().syncExec(() -> TourbookPlugin
                     .getDefault()
                     .getPreferenceStore()
                     .setValue(ITourbookPreferences.TOUR_TYPE_LIST_IS_MODIFIED, Math.random()));

            }

         } catch (final Exception e) {
            StatusUtil.log(e);
         }
      }

      return returnValue;
   }

   /**
    * checks if the data file has a valid .crp data format
    *
    * @return true for a valid .crp data format
    */
   @Override
   public boolean validateRawData(final String fileName) {

      try (FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader)) {

         final String fileHeader = bufferedReader.readLine();
         if (fileHeader == null || isFileValid(fileHeader) == false) {
            return false;
         }

      } catch (final IOException e) {
         e.printStackTrace();
      }

      return true;
   }

}

