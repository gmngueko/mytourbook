/*******************************************************************************
 * Copyright (C) 2022 Wolfgang Schramm and Contributors
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
package net.tourbook.device.mt;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.StringUtils;
import net.tourbook.common.util.Util;
import net.tourbook.data.DeviceSensor;
import net.tourbook.data.DeviceSensorValue;
import net.tourbook.data.SerieData;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.data.TourPhoto;
import net.tourbook.data.TourWayPoint;
import net.tourbook.database.TourDatabase;
import net.tourbook.importdata.ImportState_File;
import net.tourbook.importdata.ImportState_Process;
import net.tourbook.importdata.RawDataManager;
import net.tourbook.importdata.TourTypeWrapper;
import net.tourbook.importdata.TourbookDevice;
import net.tourbook.tour.TourLogManager;

/**
 * Import tours which are exported from MyTourbook
 */
public class MT_StAXHandler {

   private static final String              TAG_MT                        = "mt";           //$NON-NLS-1$
   private static final String              TAG_TOUR                      = "tour";         //$NON-NLS-1$

   private static final String              TAG_TOUR_MARKER               = "marker";       //$NON-NLS-1$
   private static final String              TAG_TOUR_PHOTO                = "photo";        //$NON-NLS-1$
   private static final String              TAG_TOUR_SENSOR_VALUE         = "sensorvalue";  //$NON-NLS-1$
   private static final String              TAG_TOUR_TYPE                 = "tourType";     //$NON-NLS-1$
   private static final String              TAG_TOUR_WAYPOINT             = "waypoint";     //$NON-NLS-1$

   private static final String              TAG_TOUR_ALL_MARKERS          = "markers";      //$NON-NLS-1$
   private static final String              TAG_TOUR_ALL_PHOTOS           = "photos";       //$NON-NLS-1$
   private static final String              TAG_TOUR_ALL_SENSOR_VALUES    = "sensorvalues"; //$NON-NLS-1$
   private static final String              TAG_TOUR_ALL_TAGS             = "tags";         //$NON-NLS-1$
   private static final String              TAG_TOUR_ALL_WAYPOINTS        = "waypoints";    //$NON-NLS-1$
   private static final String              TAG_TOUR_ALL_DATA_SERIES      = "dataseries";   //$NON-NLS-1$

   private static final String              SUB_TAG_NAME                  = "name";         //$NON-NLS-1$

   private static final String              ATTR_VALUES                   = "values";       //$NON-NLS-1$

   private Map<Long, TourData>              _alreadyImportedTours;
   private Map<Long, TourData>              _newlyImportedTours;

   private TourbookDevice                   _device;
   private String                           _importFilePath;

   private TourData                         _tourData;

   private ImportState_File                 _importState_File;
   private ImportState_Process              _importState_Process;

   private String                           _importedData_TourTypeName;
   private final Set<TourMarker>            _importedData_AllMarkers      = new HashSet<>();
   private final HashSet<TourPhoto>         _importedData_AllPhotos       = new HashSet<>();
   private final HashSet<DeviceSensorValue> _importedData_AllSensorValues = new HashSet<>();
   private final HashSet<String>            _importedData_AllTagNames     = new HashSet<>();
   private final Set<TourWayPoint>          _importedData_AllWayPoints    = new HashSet<>();

   /*
    * Imported data which must be set in the correct order
    */
   private ZonedDateTime _data_TourStartTime;
   private String        _data_TimeZoneId;
   private int           _data_TourComputedTime_Moving;
   private long          _data_TourDeviceTime_Elapsed;
   private long          _data_TourDeviceTime_Paused;
   private long          _data_TourDeviceTime_Recorded;
   private float         _data_TourAltDown;

   public MT_StAXHandler(final TourbookDevice deviceDataReader,
                         final String importFilePath,

                         final Map<Long, TourData> alreadyImportedTours,
                         final Map<Long, TourData> newlyImportedTours,

                         final ImportState_File importState_File,
                         final ImportState_Process importState_Process) throws XMLStreamException {

      _device = deviceDataReader;
      _importFilePath = importFilePath;

      _alreadyImportedTours = alreadyImportedTours;
      _newlyImportedTours = newlyImportedTours;

      _importState_File = importState_File;
      _importState_Process = importState_Process;

      parseXML(importFilePath);
   }

   public void dispose() {

   }

   private void finalizeTour() {

      // create tour id
      final String uniqueId = _device.createUniqueId(_tourData, Util.UNIQUE_ID_SUFFIX_MT);
      final Long tourId = _tourData.createTourId(uniqueId);

      // check if the tour is already imported
      if (_alreadyImportedTours.containsKey(tourId) == false) {

         // add new tour to other tours
         _newlyImportedTours.put(tourId, _tourData);

         // create additional data
//         _tourData.finalizeTour_TimerPauses(_pausedTime_Start, _pausedTime_End, null);
//         _tourData.setTourDeviceTime_Recorded(_tourData.getTourDeviceTime_Elapsed() - _tourData.getTourDeviceTime_Paused());
//
//         _tourData.computeAltitudeUpDown();
//         _tourData.computeTourMovingTime();
//         _tourData.computeComputedValues();

         finalizeTour_TourType();
         finalizeTour_Tags();
      }

      _tourData = null;

      _importState_File.isFileImportedWithValidData = true;
   }

   private void finalizeTour_Tags() {

      if (_importedData_AllTagNames.isEmpty()) {
         return;
      }

      final boolean isNewTourTag = RawDataManager.setTourTags(_tourData, _importedData_AllTagNames);

      if (isNewTourTag) {
         _importState_Process.isCreated_NewTag().set(true);
      }
   }

   private void finalizeTour_TourType() {

      if (_importedData_TourTypeName == null) {
         return;
      }

      final TourTypeWrapper tourTypeWrapper = RawDataManager.setTourType(_tourData, _importedData_TourTypeName);

      if (tourTypeWrapper != null && tourTypeWrapper.isNewTourType) {

         _importState_Process.isCreated_NewTourType().set(true);
      }
   }

   private DeviceSensor getSensor(final long sensorId) {

      final DeviceSensor deviceSensor = TourDatabase.getAllDeviceSensors_BySensorID().get(sensorId);

      if (deviceSensor == null) {

         TourLogManager.subLog_ERROR(String.format(
               "'%s' - A DeviceSensor is not available for sensorID=%d", //$NON-NLS-1$
               _importFilePath,
               sensorId));
      }

      return deviceSensor;
   }

   /**
    * Convert "values=..." into a string array
    *
    * @param startElement
    * @return
    */
   private String[] getValuesFromAttribute(final StartElement startElement) {

      final String attributeValue = startElement.getAttributeByName(new QName(ATTR_VALUES)).getValue();

      // remove leading/trailing square brackets "[...]"
      final String cleanedSerieValue = attributeValue.substring(1, attributeValue.length() - 1);

      /**
       * !!! TRAILING SPACE is IMPORTANT
       * <p>
       * otherwise a NumberFormatException occurs when a float value is parsed for a
       * long/int/short value, this can be WRONG
       */
      return cleanedSerieValue.split(", "); //$NON-NLS-1$
   }

   private boolean[] parseValues_Boolean(final StartElement startElement) {

      final String[] values = getValuesFromAttribute(startElement);

      final boolean[] result = new boolean[values.length];

      for (int serieIndex = 0; serieIndex < values.length; serieIndex++) {
         result[serieIndex] = Boolean.parseBoolean(values[serieIndex]);
      }

      return result;
   }

   private float[] parseValues_Float(final StartElement startElement) {

      final String[] values = getValuesFromAttribute(startElement);

      final float[] result = new float[values.length];

      for (int serieIndex = 0; serieIndex < values.length; serieIndex++) {
         result[serieIndex] = Float.parseFloat(values[serieIndex]);
      }

      return result;
   }

//   private int[] parseValues_Integer_AsStream(final StartElement startElement) {
//
//      final int[] array = Arrays.stream(getAttributeValues(startElement))
//
//            .map(String::trim)
//            .mapToInt(Integer::parseInt)
//            .toArray();
//
//      return array;
//   }

   private int[] parseValues_Integer(final StartElement startElement) {

      final String[] values = getValuesFromAttribute(startElement);

      final int[] result = new int[values.length];

      for (int serieIndex = 0; serieIndex < values.length; serieIndex++) {
         result[serieIndex] = Util.parseInt_0(values[serieIndex]);
      }

      return result;
   }

   private long[] parseValues_Long(final StartElement startElement) {

      final String[] values = getValuesFromAttribute(startElement);

      final long[] result = new long[values.length];

      for (int serieIndex = 0; serieIndex < values.length; serieIndex++) {
         result[serieIndex] = Util.parseLong_0(values[serieIndex]);
      }

      return result;
   }

   private short[] parseValues_Short(final StartElement startElement) {

      final String[] values = getValuesFromAttribute(startElement);

      final short[] result = new short[values.length];

      for (int serieIndex = 0; serieIndex < values.length; serieIndex++) {
         result[serieIndex] = Util.parseShort_0(values[serieIndex]);
      }

      return result;
   }

   private void parseXML(final String importFilePath) throws FactoryConfigurationError, XMLStreamException {

      final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      final XMLEventReader eventReader = inputFactory.createXMLEventReader(new StreamSource("file:" + importFilePath)); //$NON-NLS-1$

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            switch (elementName) {

            // <tour>
            case TAG_TOUR:
               parseXML_010_Tour(eventReader, startElement);
               break;

            // <tourType>
            case TAG_TOUR_TYPE:
               parseXML_020_TourType(eventReader);
               break;

            // <tags>
            case TAG_TOUR_ALL_TAGS:
               parseXML_030_Tags(eventReader);
               break;

            // <photos>
            case TAG_TOUR_ALL_PHOTOS:
               parseXML_040_Photos(eventReader);
               break;

            // <sensorvalues>
            case TAG_TOUR_ALL_SENSOR_VALUES:
               parseXML_050_SensorValues(eventReader);
               break;

            // <markers>
            case TAG_TOUR_ALL_MARKERS:
               parseXML_060_Markers(eventReader);
               break;

            // <waypoints>
            case TAG_TOUR_ALL_WAYPOINTS:
               parseXML_070_Waypoints(eventReader);
               break;

            // <dataseries>
            case TAG_TOUR_ALL_DATA_SERIES:
               parseXML_100_DataSeries(eventReader);
               break;

            }
         }

         if (xmlEvent.isEndElement()) {

            final String elementName = xmlEvent.asEndElement().getName().getLocalPart();

            switch (elementName) {

            case TAG_MT:
               finalizeTour();
               break;
            }
         }
      }
   }

   /**
    * Parse {@code <tour>...</tour>}
    *
    * @param eventReader
    * @param startElement_Parent
    * @throws XMLStreamException
    */
   private void parseXML_010_Tour(final XMLEventReader eventReader, final StartElement startElement_Parent) throws XMLStreamException {

      _tourData = new TourData();

      parseXML_012_Tour_Attributes(startElement_Parent);

// SET_FORMATTING_OFF

      /**
       * !!! VERY IMPORTANT !!!
       *
       * Set some data in the correct sort order
       */

      _tourData.setTimeZoneId(                  _data_TimeZoneId);
      _tourData.setTourComputedTime_Moving(     _data_TourComputedTime_Moving);
      _tourData.setTourDeviceTime_Paused(       _data_TourDeviceTime_Paused);
      _tourData.setTourDeviceTime_Recorded(     _data_TourDeviceTime_Recorded);

      _tourData.setTourStartTime(               _data_TourStartTime);
      _tourData.setTourDeviceTime_Elapsed(      _data_TourDeviceTime_Elapsed);

      _tourData.setTourAltDown(                 _data_TourAltDown);

// SET_FORMATTING_ON

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

// SET_FORMATTING_OFF

            switch (elementName) {

            case "importFileName":     _tourData.setTourImportFileName( eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "importFilePath":     _tourData.setTourImportFilePath( eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "tourDescription":    _tourData.setTourDescription(    eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "tourTitle":          _tourData.setTourTitle(          eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "tourStartPlace":     _tourData.setTourStartPlace(     eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "tourEndPlace":       _tourData.setTourEndPlace(       eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "weather":            _tourData.setWeather(            eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "weather_Clouds":     _tourData.setWeather_Clouds(     eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$

            }

// SET_FORMATTING_ON

         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </tour>
               break;
            }
         }
      }
   }

   private void parseXML_012_Tour_Attributes(final StartElement startElement) {

      startElement.getAttributes().forEachRemaining(attribute -> {

         final String value = attribute.getValue();

// SET_FORMATTING_OFF

         final String attributeName = attribute.getName().getLocalPart();

         switch (attributeName) {

         case "tourStartTime":                        _data_TourStartTime                           = ZonedDateTime.parse(value);   break; //$NON-NLS-1$
         case "timeZoneId":                           _data_TimeZoneId                              = value;                        break; //$NON-NLS-1$
         case "tourComputedTime_Moving":              _data_TourComputedTime_Moving                 = Util.parseInt_0(value);       break; //$NON-NLS-1$
         case "tourDeviceTime_Elapsed":               _data_TourDeviceTime_Elapsed                  = Util.parseLong_0(value);      break; //$NON-NLS-1$
         case "tourDeviceTime_Paused":                _data_TourDeviceTime_Paused                   = Util.parseLong_0(value);      break; //$NON-NLS-1$
         case "tourDeviceTime_Recorded":              _data_TourDeviceTime_Recorded                 = Util.parseLong_0(value);      break; //$NON-NLS-1$

         case "avgAltitudeChange":                    _tourData.setAvgAltitudeChange(                 Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "avgCadence":                           _tourData.setAvgCadence(                        Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "avgPulse":                             _tourData.setAvgPulse(                          Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "battery_Percentage_Start":             _tourData.setBattery_Percentage_Start(          Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "battery_Percentage_End":               _tourData.setBattery_Percentage_End(            Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "bodyFat":                              _tourData.setBodyFat(                           Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "bodyWeight":                           _tourData.setBodyWeight(                        Util.parseFloat_0(value));    break; //$NON-NLS-1$

         case "cadenceMultiplier":                    _tourData.setCadenceMultiplier(                 Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "cadenceZone_FastTime":                 _tourData.setCadenceZone_FastTime(              Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "cadenceZone_SlowTime":                 _tourData.setCadenceZone_SlowTime(              Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "cadenceZones_DelimiterValue":          _tourData.setCadenceZones_DelimiterValue(       Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "calories":                             _tourData.setCalories(                          Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "conconiDeflection":                    _tourData.setConconiDeflection(                 Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "dateTimeCreated":                      _tourData.setDateTimeCreated(                   TimeTools.createYMDhms_From_DateTime(ZonedDateTime.parse(value)));  break; //$NON-NLS-1$
         case "dateTimeModified":                     _tourData.setDateTimeModified(                  TimeTools.createYMDhms_From_DateTime(ZonedDateTime.parse(value)));  break; //$NON-NLS-1$

         case "devicePluginId":                       _tourData.setDeviceId(                          value);                       break; //$NON-NLS-1$
         case "deviceFirmwareVersion":                _tourData.setDeviceFirmwareVersion(             value);                       break; //$NON-NLS-1$
         case "deviceModeName":                       _tourData.setDeviceModeName(                    value);                       break; //$NON-NLS-1$
         case "devicePluginName":                     _tourData.setDeviceName(                        value);                       break; //$NON-NLS-1$
         case "deviceTimeInterval":                   _tourData.setDeviceTimeInterval(                Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "dpTolerance":                          _tourData.setDpTolerance(                       Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "frontShiftCount":                      _tourData.setFrontShiftCount(                   Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "hasGeoData":                           _tourData.setHasGeoData(                        Util.parseBoolean(value));    break; //$NON-NLS-1$

         case "hrZone0":                              _tourData.setHrZone0(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone1":                              _tourData.setHrZone1(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone2":                              _tourData.setHrZone2(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone3":                              _tourData.setHrZone3(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone4":                              _tourData.setHrZone4(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone5":                              _tourData.setHrZone5(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone6":                              _tourData.setHrZone6(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone7":                              _tourData.setHrZone7(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone8":                              _tourData.setHrZone8(                           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "hrZone9":                              _tourData.setHrZone9(                           Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "isDistanceFromSensor":                 _tourData.setIsDistanceFromSensor(              Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "isPowerSensorPresent":                 _tourData.setIsPowerSensorPresent(              Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "isPulseSensorPresent":                 _tourData.setIsPulseSensorPresent(              Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "isStrideSensorPresent":                _tourData.setIsStrideSensorPresent(             Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "isWeatherDataFromProvider":            _tourData.setIsWeatherDataFromProvider(         Util.parseBoolean(value));    break; //$NON-NLS-1$

         case "maxAltitude":                          _tourData.setMaxElevation(                      Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "maxPace":                              _tourData.setMaxPace(                           Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "maxPulse":                             _tourData.setMaxPulse(                          Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "maxSpeed":                             _tourData.setMaxSpeed(                          Util.parseFloat_0(value));    break; //$NON-NLS-1$

         case "mergedAltitudeOffset":                 _tourData.setMergedAltitudeOffset(              Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "mergedTourTimeOffset":                 _tourData.setMergedTourTimeOffset(              Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "numberOfHrZones":                      _tourData.setNumberOfHrZones(                   Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "numberOfPhotos":                       _tourData.setNumberOfPhotos(                    Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "numberOfTimeSlices":                   _tourData.setNumberOfTimeSlices(                Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "photoTimeAdjustment":                  _tourData.setPhotoTimeAdjustment(               Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "power_Avg":                            _tourData.setPower_Avg(                         Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "power_AvgLeftPedalSmoothness":         _tourData.setPower_AvgLeftPedalSmoothness(      Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "power_AvgLeftTorqueEffectiveness":     _tourData.setPower_AvgLeftTorqueEffectiveness(  Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "power_AvgRightPedalSmoothness":        _tourData.setPower_AvgRightPedalSmoothness(     Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "power_AvgRightTorqueEffectiveness":    _tourData.setPower_AvgRightTorqueEffectiveness( Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "power_FTP":                            _tourData.setPower_FTP(                         Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "power_IntensityFactor":                _tourData.setPower_IntensityFactor(             Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "power_Max":                            _tourData.setPower_Max(                         Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "power_Normalized":                     _tourData.setPower_Normalized(                  Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "power_PedalLeftRightBalance":          _tourData.setPower_PedalLeftRightBalance(       Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "power_TotalWork":                      _tourData.setPower_TotalWork(                   Util.parseLong_0(value));     break; //$NON-NLS-1$
         case "power_TrainingStressScore":            _tourData.setPower_TrainingStressScore(         Util.parseFloat_0(value));    break; //$NON-NLS-1$

         case "rearShiftCount":                       _tourData.setRearShiftCount(                    Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "restPulse":                            _tourData.setRestPulse(                         Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "runDyn_StanceTimeBalance_Avg":         _tourData.setRunDyn_StanceTimeBalance_Avg(      Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "runDyn_StanceTimeBalance_Max":         _tourData.setRunDyn_StanceTimeBalance_Max(      Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_StanceTimeBalance_Min":         _tourData.setRunDyn_StanceTimeBalance_Min(      Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_StanceTime_Avg":                _tourData.setRunDyn_StanceTime_Avg(             Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "runDyn_StanceTime_Max":                _tourData.setRunDyn_StanceTime_Max(             Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_StanceTime_Min":                _tourData.setRunDyn_StanceTime_Min(             Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_StepLength_Avg":                _tourData.setRunDyn_StepLength_Avg(             Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "runDyn_StepLength_Max":                _tourData.setRunDyn_StepLength_Max(             Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_StepLength_Min":                _tourData.setRunDyn_StepLength_Min(             Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_VerticalOscillation_Avg":       _tourData.setRunDyn_VerticalOscillation_Avg(    Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "runDyn_VerticalOscillation_Max":       _tourData.setRunDyn_VerticalOscillation_Max(    Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_VerticalOscillation_Min":       _tourData.setRunDyn_VerticalOscillation_Min(    Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_VerticalRatio_Avg":             _tourData.setRunDyn_VerticalRatio_Avg(          Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "runDyn_VerticalRatio_Max":             _tourData.setRunDyn_VerticalRatio_Max(          Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "runDyn_VerticalRatio_Min":             _tourData.setRunDyn_VerticalRatio_Min(          Util.parseShort_0(value));    break; //$NON-NLS-1$

         case "startAltitude":                        _tourData.setStartAltitude(                     Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "startDistance":                        _tourData.setStartDistance(                     Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "startPulse":                           _tourData.setStartPulse(                        Util.parseShort_0(value));    break; //$NON-NLS-1$

         case "surfing_IsMinDistance":                _tourData.setSurfing_IsMinDistance(             Util.parseBoolean(value));    break; //$NON-NLS-1$
         case "surfing_MinDistance":                  _tourData.setSurfing_MinDistance(               Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "surfing_MinSpeed_StartStop":           _tourData.setSurfing_MinSpeed_StartStop(        Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "surfing_MinSpeed_Surfing":             _tourData.setSurfing_MinSpeed_Surfing(          Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "surfing_MinTimeDuration":              _tourData.setSurfing_MinTimeDuration(           Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "surfing_NumberOfEvents":               _tourData.setSurfing_NumberOfEvents(            Util.parseShort_0(value));    break; //$NON-NLS-1$

         case "temperatureScale":                     _tourData.setTemperatureScale(                  Util.parseInt_0(value));      break; //$NON-NLS-1$

         case "tourAltDown":                          _data_TourAltDown                             = Util.parseFloat_0(value);     break; //$NON-NLS-1$
         case "tourAltUp":                            _tourData.setTourAltUp(                         Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "tourDistance":                         _tourData.setTourDistance(                      Util.parseFloat_0(value));    break; //$NON-NLS-1$

         case "training_TrainingEffect_Aerob":        _tourData.setTraining_TrainingEffect_Aerob(     Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "training_TrainingEffect_Anaerob":      _tourData.setTraining_TrainingEffect_Anaerob(   Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "training_TrainingPerformance":         _tourData.setTraining_TrainingPerformance(      Util.parseFloat_0(value));    break; //$NON-NLS-1$

         case "weather_Humidity":                     _tourData.setWeather_Humidity(                  Util.parseShort_0(value));    break; //$NON-NLS-1$
         case "weather_Precipitation":                _tourData.setWeather_Precipitation(             Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Pressure":                     _tourData.setWeather_Pressure(                  Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Snowfall":                     _tourData.setWeather_Snowfall(                  Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_Average":          _tourData.setWeather_Temperature_Average(       Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_Average_Device":   _tourData.setWeather_Temperature_Average_Device(Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_Max":              _tourData.setWeather_Temperature_Max(           Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_Max_Device":       _tourData.setWeather_Temperature_Max_Device(    Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_Min":              _tourData.setWeather_Temperature_Min(           Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_Min_Device":       _tourData.setWeather_Temperature_Min_Device(    Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Temperature_WindChill":        _tourData.setWeather_Temperature_WindChill(     Util.parseFloat_0(value));    break; //$NON-NLS-1$
         case "weather_Wind_Direction":               _tourData.setWeather_Wind_Direction(            Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "weather_Wind_Speed":                   _tourData.setWeather_Wind_Speed(                Util.parseInt_0(value));      break; //$NON-NLS-1$

// SET_FORMATTING_ON

         }
      });
   }

   /**
    * Parse {@code <tourType>...</tourType>}
    *
    * @param eventReader
    * @throws XMLStreamException
    */
   private void parseXML_020_TourType(final XMLEventReader eventReader) throws XMLStreamException {

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            if (SUB_TAG_NAME.equals(elementName)) {

               final String data = eventReader.nextEvent().asCharacters().getData();

               if (StringUtils.hasContent(data)) {
                  _importedData_TourTypeName = data;
               }
            }

         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_TYPE.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </tourType>
               break;
            }
         }
      }
   }

   /**
    * Parse {@code <tags>...</tags>}
    *
    * @param eventReader
    * @param startElement_
    * @throws XMLStreamException
    */
   private void parseXML_030_Tags(final XMLEventReader eventReader) throws XMLStreamException {

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            if (SUB_TAG_NAME.equals(elementName)) {

               final String data = eventReader.nextEvent().asCharacters().getData();

               if (StringUtils.hasContent(data)) {
                  _importedData_AllTagNames.add(data);
               }
            }
         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_ALL_TAGS.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </tags>
               break;
            }
         }
      }
   }

   /**
    * Parse {@code <photos>...</photos>}
    *
    * @param eventReader
    * @throws XMLStreamException
    */
   private void parseXML_040_Photos(final XMLEventReader eventReader) throws XMLStreamException {

      // loop: all <photo>
      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            if (TAG_TOUR_PHOTO.equals(elementName)) {

               // <photo>

               parseXML_041_Photo(eventReader, startElement);
            }
         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_ALL_PHOTOS.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </photos>
               break;
            }
         }
      }

      if (_importedData_AllPhotos.size() > 0) {
         _tourData.setTourPhotos(_importedData_AllPhotos);
      }
   }

   private void parseXML_041_Photo(final XMLEventReader eventReader,
                                   final StartElement startElement_Photo) throws XMLStreamException {

      final TourPhoto tourPhoto = new TourPhoto(_tourData);

      parseXML_042_Photo_Attributes(startElement_Photo, tourPhoto);

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

// SET_FORMATTING_OFF

            switch (elementName) {

            case "imageFilePathName":    tourPhoto.setFilePathName(eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$

            }

// SET_FORMATTING_ON

         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_PHOTO.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </photo>
               break;
            }
         }
      }

      _importedData_AllPhotos.add(tourPhoto);
   }

   private void parseXML_042_Photo_Attributes(final StartElement startElement, final TourPhoto tourPhoto) {

      startElement.getAttributes().forEachRemaining(attribute -> {

         final String value = attribute.getValue();

// SET_FORMATTING_OFF

         final String attributeName = attribute.getName().getLocalPart();

         switch (attributeName) {

         case "imageExifTime":         tourPhoto.setImageExifTime(         Util.parseLong_0(value));     break; //$NON-NLS-1$
         case "imageFileLastModified": tourPhoto.setImageFileLastModified( Util.parseLong_0(value));     break; //$NON-NLS-1$
         case "adjustedTime":          tourPhoto.setAdjustedTime(          Util.parseLong_0(value));     break; //$NON-NLS-1$
         case "isGeoFromPhoto":        tourPhoto.setIsGeoFrom(             Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "ratingStars":           tourPhoto.setRatingStars(           Util.parseInt_0(value));      break; //$NON-NLS-1$
         case "latitude":              tourPhoto.setLatitude(              Util.parseDouble_0(value));   break; //$NON-NLS-1$
         case "longitude":             tourPhoto.setLongitude(             Util.parseDouble_0(value));   break; //$NON-NLS-1$

// SET_FORMATTING_ON

         }

      });
   }

   /**
    * Parse {@code <sensorvalues>...</sensorvalues>}
    *
    * @param eventReader
    * @throws XMLStreamException
    */
   private void parseXML_050_SensorValues(final XMLEventReader eventReader) throws XMLStreamException {

      // loop: until </sensorvalues>
      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            if (TAG_TOUR_SENSOR_VALUE.equals(elementName)) {

               // <sensorvalue>

               parseXML_051_SensorValue(eventReader, startElement);
            }
         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_ALL_SENSOR_VALUES.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </sensorvalues>
               break;
            }
         }
      }

      if (_importedData_AllSensorValues.size() > 0) {

         _tourData.getDeviceSensorValues().addAll(_importedData_AllSensorValues);
      }
   }

   private void parseXML_051_SensorValue(final XMLEventReader eventReader,
                                         final StartElement startElement_SensorValue) throws XMLStreamException {

      final DeviceSensorValue sensorValue = new DeviceSensorValue(_tourData);

      parseXML_052_SensorValue_Attributes(startElement_SensorValue, sensorValue);

      _importedData_AllSensorValues.add(sensorValue);
   }

   private void parseXML_052_SensorValue_Attributes(final StartElement startElement, final DeviceSensorValue sensorValue) {

      startElement.getAttributes().forEachRemaining(attribute -> {

         final String value = attribute.getValue();

// SET_FORMATTING_OFF

         final String attributeName = attribute.getName().getLocalPart();

         switch (attributeName) {

         case "sensorId":              sensorValue.setDeviceSensor(getSensor(    Util.parseLong_0(value)));    break; //$NON-NLS-1$
         case "tourStartTime":         sensorValue.setTourTime_Start(            Util.parseLong_0(value));     break; //$NON-NLS-1$
         case "tourEndTime":           sensorValue.setTourTime_End(              Util.parseLong_0(value));     break; //$NON-NLS-1$
         case "batteryLevel_Start":    sensorValue.setBatteryLevel_Start(        Util.parseShort_n1(value));   break; //$NON-NLS-1$
         case "batteryLevel_End":      sensorValue.setBatteryLevel_End(          Util.parseShort_n1(value));   break; //$NON-NLS-1$
         case "batteryStatus_Start":   sensorValue.setBatteryStatus_Start(       Util.parseShort_n1(value));   break; //$NON-NLS-1$
         case "batteryStatus_End":     sensorValue.setBatteryStatus_End(         Util.parseShort_n1(value));   break; //$NON-NLS-1$
         case "batteryVoltage_Start":  sensorValue.setBatteryVoltage_Start(      Util.parseFloat_n1(value));   break; //$NON-NLS-1$
         case "batteryVoltage_End":    sensorValue.setBatteryVoltage_End(        Util.parseFloat_n1(value));   break; //$NON-NLS-1$

// SET_FORMATTING_ON

         }

      });
   }

   /**
    * Parse {@code <markers>...</markers>}
    *
    * @param eventReader
    * @throws XMLStreamException
    */
   private void parseXML_060_Markers(final XMLEventReader eventReader) throws XMLStreamException {

      // loop: until </markers>
      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            if (TAG_TOUR_MARKER.equals(elementName)) {

               // <marker>

               parseXML_061_Marker(eventReader, startElement);
            }
         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_ALL_MARKERS.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </markers>
               break;
            }
         }
      }

      if (_importedData_AllMarkers.size() > 0) {
         _tourData.setTourMarkers(_importedData_AllMarkers);
      }
   }

   private void parseXML_061_Marker(final XMLEventReader eventReader,
                                    final StartElement startElement_Marker) throws XMLStreamException {

      final TourMarker tourMarker = new TourMarker(_tourData);

      parseXML_062_Marker_Attributes(startElement_Marker, tourMarker);

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

// SET_FORMATTING_OFF

            switch (elementName) {

            case "label":        tourMarker.setLabel(       eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "description":  tourMarker.setDescription( eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "urlAddress":   tourMarker.setUrlAddress(  eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "urlText":      tourMarker.setUrlText(     eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$

            }

// SET_FORMATTING_ON

         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_MARKER.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </marker>
               break;
            }
         }
      }

      _importedData_AllMarkers.add(tourMarker);
   }

   private void parseXML_062_Marker_Attributes(final StartElement startElement, final TourMarker tourMarker) {

      startElement.getAttributes().forEachRemaining(attribute -> {

         final String value = attribute.getValue();

// SET_FORMATTING_OFF

         final String attributeName = attribute.getName().getLocalPart();

         switch (attributeName) {

         case "altitude":           tourMarker.setAltitude(          Util.parseFloat(     value, TourDatabase.DEFAULT_FLOAT));   break; //$NON-NLS-1$
         case "distance20":         tourMarker.setDistance(          Util.parseFloat_n1(  value));                               break; //$NON-NLS-1$
         case "latitude":           tourMarker.setLatitude(          Util.parseDouble(    value, TourDatabase.DEFAULT_DOUBLE));  break; //$NON-NLS-1$
         case "longitude":          tourMarker.setLongitude(         Util.parseDouble(    value, TourDatabase.DEFAULT_DOUBLE));  break; //$NON-NLS-1$
         case "labelXOffset":       tourMarker.setLabelXOffset(      Util.parseInt_0(     value));                               break; //$NON-NLS-1$
         case "labelYOffset":       tourMarker.setLabelYOffset(      Util.parseInt_0(     value));                               break; //$NON-NLS-1$
         case "serieIndex":         tourMarker.setSerieIndex(        Util.parseInt_0(     value));                               break; //$NON-NLS-1$
         case "time":               tourMarker.setTime(              Util.parseInt_n1(    value));                               break; //$NON-NLS-1$
         case "tourTime":           tourMarker.setTourTime(          Util.parseLong(      value));                               break; //$NON-NLS-1$
         case "type":               tourMarker.setType(              Util.parseInt_0(     value));                               break; //$NON-NLS-1$
         case "visualPosition":     tourMarker.setLabelPosition(     Util.parseInt_0(     value));                               break; //$NON-NLS-1$
         case "isMarkerVisible":    tourMarker.setIsMarkerVisible(   Util.parseInt(       value, 1));                            break; //$NON-NLS-1$

// SET_FORMATTING_ON

         }

      });
   }

   /**
    * Parse {@code <waypoints>...</waypoints>}
    *
    * @param eventReader
    * @throws XMLStreamException
    */
   private void parseXML_070_Waypoints(final XMLEventReader eventReader) throws XMLStreamException {

      // loop: until </waypoints>
      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

            if (TAG_TOUR_WAYPOINT.equals(elementName)) {

               // <waypoint>

               parseXML_071_Waypoint(eventReader, startElement);
            }
         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_ALL_WAYPOINTS.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </waypoints>
               break;
            }
         }
      }

      if (_importedData_AllWayPoints.size() > 0) {
         _tourData.getTourWayPoints().addAll(_importedData_AllWayPoints);
      }
   }

   private void parseXML_071_Waypoint(final XMLEventReader eventReader,
                                      final StartElement startElement_WayPoint) throws XMLStreamException {

      final TourWayPoint tourWayPoint = new TourWayPoint();

      parseXML_072_WayPoint_Attributes(startElement_WayPoint, tourWayPoint);

      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

// SET_FORMATTING_OFF

            switch (elementName) {

            case "category":     tourWayPoint.setCategory(     eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "comment":      tourWayPoint.setComment(      eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "description":  tourWayPoint.setDescription(  eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "name":         tourWayPoint.setName(         eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "symbol":       tourWayPoint.setSymbol(       eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "urlAddress":   tourWayPoint.setUrlAddress(   eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$
            case "urlText":      tourWayPoint.setUrlText(      eventReader.nextEvent().asCharacters().getData());    break; //$NON-NLS-1$

            }

// SET_FORMATTING_ON

         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_WAYPOINT.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </waypoint>
               break;
            }
         }
      }

      _importedData_AllWayPoints.add(tourWayPoint);
   }

   private void parseXML_072_WayPoint_Attributes(final StartElement startElement, final TourWayPoint tourWayPoint) {

      startElement.getAttributes().forEachRemaining(attribute -> {

         final String value = attribute.getValue();

// SET_FORMATTING_OFF

         final String attributeName = attribute.getName().getLocalPart();

         switch (attributeName) {

         case "altitude":     tourWayPoint.setAltitude(     Util.parseFloat(     value));  break; //$NON-NLS-1$
         case "latitude":     tourWayPoint.setLatitude(     Util.parseDouble(    value));  break; //$NON-NLS-1$
         case "longitude":    tourWayPoint.setLongitude(    Util.parseDouble(    value));  break; //$NON-NLS-1$
         case "time":         tourWayPoint.setTime(         Util.parseLong_0(    value));  break; //$NON-NLS-1$

// SET_FORMATTING_ON

         }

      });
   }

   /**
    * Parse {@code <waypoints>...</waypoints>}
    *
    * @param eventReader
    * @throws XMLStreamException
    */
   private void parseXML_100_DataSeries(final XMLEventReader eventReader) throws XMLStreamException {

      final SerieData serieData = new SerieData();

      // loop: until </dataseries>
      while (eventReader.hasNext()) {

         final XMLEvent xmlEvent = eventReader.nextEvent();

         if (xmlEvent.isStartElement()) {

            final StartElement startElement = xmlEvent.asStartElement();
            final String elementName = startElement.getName().getLocalPart();

// SET_FORMATTING_OFF

            switch (elementName) {

            case "serieTime":                         serieData.timeSerie                    = parseValues_Integer(  startElement);    break; //$NON-NLS-1$
            case "serieAltitude":                     serieData.altitudeSerie20              = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "serieCadence":                      serieData.cadenceSerie20               = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "serieDistance":                     serieData.distanceSerie20              = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "seriePulse":                        serieData.pulseSerie20                 = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "serieTemperature":                  serieData.temperatureSerie20           = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "seriePower":                        serieData.powerSerie20                 = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "serieSpeed":                        serieData.speedSerie20                 = parseValues_Float(    startElement);    break; //$NON-NLS-1$
            case "serieGears":                        serieData.gears                        = parseValues_Long(     startElement);    break; //$NON-NLS-1$
            case "serieLatitude":                     serieData.latitudeE6                   = parseValues_Integer(  startElement);    break; //$NON-NLS-1$
            case "serieLongitude":                    serieData.longitudeE6                  = parseValues_Integer(  startElement);    break; //$NON-NLS-1$
            case "seriePausedTime_Start":             serieData.pausedTime_Start             = parseValues_Long(     startElement);    break; //$NON-NLS-1$
            case "seriePausedTime_End":               serieData.pausedTime_End               = parseValues_Long(     startElement);    break; //$NON-NLS-1$
            case "seriePausedTime_Data":              serieData.pausedTime_Data              = parseValues_Long(     startElement);    break; //$NON-NLS-1$
            case "seriePulseTimes":                   serieData.pulseTimes                   = parseValues_Integer(  startElement);    break; //$NON-NLS-1$
            case "seriePulseTimes_TimeIndex":         serieData.pulseTime_TimeIndex          = parseValues_Integer(  startElement);    break; //$NON-NLS-1$
            case "serieRunDyn_StanceTime":            serieData.runDyn_StanceTime            = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieRunDyn_StanceTimeBalance":     serieData.runDyn_StanceTimeBalance     = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieRunDyn_StepLength":            serieData.runDyn_StepLength            = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieRunDyn_VerticalOscillation":   serieData.runDyn_VerticalOscillation   = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieRunDyn_VerticalRatio":         serieData.runDyn_VerticalRatio         = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieSwim_LengthType":              serieData.swim_LengthType              = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieSwim_Cadence":                 serieData.swim_Cadence                 = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieSwim_Strokes":                 serieData.swim_Strokes                 = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieSwim_StrokeStyle":             serieData.swim_StrokeStyle             = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieSwim_Time":                    serieData.swim_Time                    = parseValues_Integer(  startElement);    break; //$NON-NLS-1$
            case "serieVisiblePoints_Surfing":        serieData.visiblePoints_Surfing        = parseValues_Boolean(  startElement);    break; //$NON-NLS-1$
            case "serieBattery_Percentage":           serieData.battery_Percentage           = parseValues_Short(    startElement);    break; //$NON-NLS-1$
            case "serieBattery_Time":                 serieData.battery_Time                 = parseValues_Integer(  startElement);    break; //$NON-NLS-1$

            }

// SET_FORMATTING_ON

         }

         if (xmlEvent.isEndElement()) {

            if (TAG_TOUR_ALL_DATA_SERIES.equals(xmlEvent.asEndElement().getName().getLocalPart())) {

               // </dataseries...>
               break;
            }
         }
      }

      _tourData.setSerieData(serieData);
   }

}
