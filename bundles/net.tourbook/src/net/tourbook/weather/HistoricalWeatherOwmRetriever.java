/*******************************************************************************
 * Copyright (C) 2019, 2020 Frédéric Bard
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
package net.tourbook.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.Util;
import net.tourbook.data.TourData;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.ui.Messages;
import net.tourbook.ui.views.calendar.CalendarProfile;
import net.tourbook.weather.OWMResults.OWMResponse;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * A class that retrieves, for a given track, the historical weather data.
 */
public class HistoricalWeatherOwmRetriever {

   private static final String  SYS_PROP__LOG_WEATHER_DATA = "logWeatherData";                                                      //$NON-NLS-1$
   private static final boolean _isLogWeatherData          = System.getProperty(SYS_PROP__LOG_WEATHER_DATA) != null;

   private static final String  DEFAULT_UNIT               = "metric";
   private static final String  DEFAULT_LATITUDE           = "50.85";
   private static final String  DEFAULT_LONGITUDE          = "4.42";

   private static HttpClient    httpClient                 = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

   static {

      if (_isLogWeatherData) {

         Util.logSystemProperty_IsEnabled(CalendarProfile.class,
               SYS_PROP__LOG_WEATHER_DATA,
               "OWM Weather data are logged"); //$NON-NLS-1$
      }
   }

   private static final String    baseApiUrl         = "http://api.openweathermap.org/data/2.5/onecall/timemachine"; //$NON-NLS-1$
   private static final String    unitParameter      = "?units=";                                                    //$NON-NLS-1$
   private static final String    keyParameter       = "&appid=";                                                    //$NON-NLS-1$
   private static final String    latitudeParameter  = "&lat=";                                                      //$NON-NLS-1$
   private static final String    longitudeParameter = "&lon=";                                                      //$NON-NLS-1$
   private static final String    utcParameter       = "&dt=";                                                       //$NON-NLS-1$

   private TourData               tour;
   private LatLng                 searchAreaCenter;
   private String                 startDate;
   private String                 startTime;

   private String                 endTime;

   private long                   utcStartTime;

   private OWMWeatherData         historicalWeatherData;

   private final IPreferenceStore _prefStore         = TourbookPlugin.getPrefStore();

   public HistoricalWeatherOwmRetriever() {}

   /*
    * @param tour
    * The tour for which we need to retrieve the weather data.
    */
   public HistoricalWeatherOwmRetriever(final TourData tour) {

      this.tour = tour;

      determineWeatherSearchArea();
      startDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(tour.getTourStartTime()); //$NON-NLS-1$

      final double roundedStartTime = tour.getTourStartTime().getHour();
      startTime = (int) roundedStartTime + "00"; //$NON-NLS-1$
      utcStartTime = tour.getTourStartTime().toEpochSecond();

      int roundedEndHour = Instant.ofEpochMilli(tour.getTourEndTimeMS()).atZone(tour.getTimeZoneIdWithDefault()).getHour();
      final int roundedEndMinutes = Instant.ofEpochMilli(tour.getTourEndTimeMS()).atZone(tour.getTimeZoneIdWithDefault()).getMinute();
      if (roundedEndMinutes >= 30) {
         ++roundedEndHour;
      }
      endTime = roundedEndHour + "00"; //$NON-NLS-1$

      historicalWeatherData = new OWMWeatherData();
      historicalWeatherData.initWeatherSeries(tour.timeSerie.length);
   }

   public static String getApiUrl() {
      return baseApiUrl + keyParameter;
   }

   public static String getBaseApiUrl() {
      return baseApiUrl;
   }

   public static String getTestApiUrl() {
      String testUrl = baseApiUrl + unitParameter + DEFAULT_UNIT;
      testUrl += latitudeParameter + DEFAULT_LATITUDE;
      testUrl += longitudeParameter + DEFAULT_LONGITUDE;
      final long utcNow = System.currentTimeMillis() / 1000 - 3600;
      testUrl += utcParameter + utcNow;
      return testUrl + keyParameter;
   }

   /**
    * Determines the geographic area covered by a GPS track. The goal is to
    * encompass most of the track to search a weather station as close as possible
    * to the overall course and not just to a specific point.
    */
   private void determineWeatherSearchArea() {
      // Looking for the farthest point of the track
      double maxDistance = Double.MIN_VALUE;
      LatLng furthestPoint = null;
      final LatLng startPoint = new LatLng(tour.latitudeSerie[0], tour.longitudeSerie[0]);
      for (int index = 1; index < tour.latitudeSerie.length && index < tour.longitudeSerie.length; ++index) {
         final LatLng currentPoint = new LatLng(tour.latitudeSerie[index], tour.longitudeSerie[index]);

         final double distanceFromStart = LatLngTool.distance(startPoint, currentPoint, LengthUnit.METER);

         if (distanceFromStart > maxDistance) {
            maxDistance = distanceFromStart;
            furthestPoint = currentPoint;
         }
      }

      final double distanceFromStart = LatLngTool.distance(startPoint, furthestPoint, LengthUnit.METER);
      final double bearingBetweenPoint = LatLngTool.initialBearing(startPoint, furthestPoint);

      // We find the center of the circle formed by the starting point and the farthest point
      searchAreaCenter = LatLngTool.travel(startPoint, bearingBetweenPoint, distanceFromStart / 2, LengthUnit.METER);
   }

   public OWMWeatherData getHistoricalWeatherData() {
      return historicalWeatherData;
   }

   /**
    * Parses a JSON weather data object into a WeatherData object.
    *
    * @param weatherDataResponse
    *           A string containing a historical weather data JSON object.
    * @return The parsed weather data.
    */
   private OWMWeatherData parseWeatherData(final String weatherDataResponse) {

      if (_isLogWeatherData) {

         final long elapsedTime = tour.getTourDeviceTime_Elapsed();
         final ZonedDateTime zdtTourStart = tour.getTourStartTime();
         final ZonedDateTime zdtTourEnd = zdtTourStart.plusSeconds(elapsedTime);
         final String tourTitle = tour.getTourTitle();

         System.out.println();

         if (tourTitle.length() > 0) {
            System.out.println(tourTitle);
         }

         System.out.println(String.format(Messages.Tour_Tooltip_Format_DateWeekTime,
               zdtTourStart.format(TimeTools.Formatter_Date_F),
               zdtTourStart.format(TimeTools.Formatter_Time_M),
               zdtTourEnd.format(TimeTools.Formatter_Time_M),
               zdtTourStart.get(TimeTools.calendarWeek.weekOfWeekBasedYear())));

         System.out.println(weatherDataResponse);
      }

      final OWMWeatherData weatherData = new OWMWeatherData();
      try {
         final ObjectMapper mapper = new ObjectMapper();
         final String weatherResults = mapper.readValue(weatherDataResponse, JsonNode.class)
               .get("data") //$NON-NLS-1$
               .get("weather") //$NON-NLS-1$
               .get(0)
               .get("hourly") //$NON-NLS-1$
               .toString();

         final List<WWOHourlyResults> rawWeatherData = mapper.readValue(weatherResults, new TypeReference<List<WWOHourlyResults>>() {});

         boolean isTourStartData = false;
         boolean isTourEndData = false;
         int numHourlyDatasets = 0;
         int sumHumidity = 0;
         int sumPressure = 0;
         float sumPrecipitation = 0f;
         int sumWindChill = 0;
         int sumWindDirection = 0;
         int sumWindSpeed = 0;
         int sumTemperature = 0;
         int maxTemperature = Integer.MIN_VALUE;
         int minTemperature = Integer.MAX_VALUE;

         for (final WWOHourlyResults hourlyData : rawWeatherData) {
            // Within the hourly data, find the times that corresponds to the tour time
            // and extract all the weather data.
            if (hourlyData.gettime().equals(startTime)) {
               isTourStartData = true;
               weatherData.setWeatherDescription(hourlyData.getWeatherDescription());
               weatherData.setWeatherType(hourlyData.getWeatherCode());
            }
            if (hourlyData.gettime().equals(endTime)) {
               isTourEndData = true;
            }

            if (isTourStartData || isTourEndData) {
               sumWindDirection += hourlyData.getWinddirDegree();
               sumWindSpeed += hourlyData.getWindspeedKmph();
               sumHumidity += hourlyData.getHumidity();
               sumPrecipitation += hourlyData.getPrecipMM();
               sumPressure += hourlyData.getPressure();
               sumWindChill += hourlyData.getFeelsLikeC();
               sumTemperature += hourlyData.getTempC();

               if (hourlyData.getTempC() < minTemperature) {
                  minTemperature = hourlyData.getTempC();
               }

               if (hourlyData.getTempC() > maxTemperature) {
                  maxTemperature = hourlyData.getTempC();
               }

               ++numHourlyDatasets;
               if (isTourEndData) {
                  break;
               }
            }
         }

         weatherData.setWindDirection((int) Math.ceil((double) sumWindDirection / (double) numHourlyDatasets));
         weatherData.setWindSpeed((int) Math.ceil((double) sumWindSpeed / (double) numHourlyDatasets));
         weatherData.setTemperatureMax(maxTemperature);
         weatherData.setTemperatureMin(minTemperature);
         weatherData.setTemperatureAverage((int) Math.ceil((double) sumTemperature / (double) numHourlyDatasets));
         weatherData.setWindChill((int) Math.ceil((double) sumWindChill / (double) numHourlyDatasets));
         weatherData.setAverageHumidity((int) Math.ceil((double) sumHumidity / (double) numHourlyDatasets));
         weatherData.setAveragePressure((int) Math.ceil((double) sumPressure / (double) numHourlyDatasets));
         weatherData.setPrecipitation(sumPrecipitation);

      } catch (final Exception e) {
         StatusUtil.log(
               "WeatherHistoryRetriever.parseWeatherData : Error while parsing the historical weather JSON object :" //$NON-NLS-1$
                     + weatherDataResponse + "\n" + e.getMessage()); //$NON-NLS-1$
         return null;
      }

      return weatherData;
   }

   /**
    * Retrieves the historical weather data
    *
    * @return The weather data, if found.
    */
   public HistoricalWeatherOwmRetriever retrieveHistoricalWeatherData(final int intervalSeconds) {

      if (tour == null) {
         return null;
      }


      final int sumHumidity = 0;
      final int numHumidityDatasets = 0;

      final int sumPressure = 0;
      final int numPressureDatasets = 0;

      final float sumPrecipitation = 0f;
      final int sumWindChill = 0;

      final int sumWindDirection = 0;
      final int numWindDirectionDatasets = 0;

      final float sumWindSpeed = 0;
      final int numWindSpeedDatasets = 0;

      float sumTemperature = 0;
      int numTemperatureDatasets = 0;
      float maxTemperature = Float.MIN_VALUE;
      float minTemperature = Float.MAX_VALUE;

      int jumpIndex = 0;
      for (int serieIndex = 0; serieIndex < tour.timeSerie.length; serieIndex++) {
         if (tour.timeSerie[serieIndex] / intervalSeconds >= jumpIndex || serieIndex == (tour.timeSerie.length - 1)) {
            jumpIndex++;
            final long utcForWeather = utcStartTime + tour.timeSerie[serieIndex];
            final String rawWeatherData = sendWeatherApiRequest(tour.latitudeSerie[serieIndex], tour.longitudeSerie[serieIndex], utcForWeather);
            if (!rawWeatherData.contains("current")) { //$NON-NLS-1$
               return null;
            }

            try {
               final ObjectMapper mapper = new ObjectMapper();
               final OWMResponse rawWeatherResponse = mapper.readValue(rawWeatherData, new TypeReference<OWMResponse>() {});

               if (rawWeatherResponse != null && rawWeatherResponse.current != null) {
                  if (rawWeatherResponse.current.temp != null) {
                     historicalWeatherData.OWM_Temperature_Present = true;
                     historicalWeatherData.OWM_Temperature_Serie[serieIndex] = rawWeatherResponse.current.temp;
                     sumTemperature += rawWeatherResponse.current.temp;

                     if (rawWeatherResponse.current.temp < minTemperature) {
                        minTemperature = rawWeatherResponse.current.temp;
                     }

                     if (rawWeatherResponse.current.temp > maxTemperature) {
                        maxTemperature = rawWeatherResponse.current.temp;
                     }
                     numTemperatureDatasets++;
                  }
               }
            } catch (final Exception e) {
               StatusUtil.log(
                     "OWMWeatherHistoryRetriever.parseWeatherData : Error while parsing the historical weather JSON object :" //$NON-NLS-1$
                           + rawWeatherData + "\n" + e.getMessage()); //$NON-NLS-1$
               return null;
            }
         }
      }

      return this;
   }

   /**
    * Processes a query against the weather API.
    *
    * @return The result of the weather API query.
    */
   private String sendWeatherApiRequest(final double latitudeSerie, final double longitudeSerie, final long utcTime) {

      String weatherRequestWithParameters = baseApiUrl;

      weatherRequestWithParameters += unitParameter + DEFAULT_UNIT;
      weatherRequestWithParameters += latitudeParameter + String.format("%.2f", latitudeSerie);
      weatherRequestWithParameters += longitudeParameter + String.format("%.2f", longitudeSerie);
      weatherRequestWithParameters += utcParameter + utcTime;
      weatherRequestWithParameters += keyParameter + _prefStore.getString(ITourbookPreferences.WEATHER_OWM_API_KEY);

      //tp=1 : Specifies the weather forecast time interval in hours. Here, every 1 hour

      String weatherHistory = UI.EMPTY_STRING;
      try {
         // NOTE :
         // This error below keeps popping up RANDOMLY and as of today, I haven't found a solution:
         // java.lang.NoClassDefFoundError: Could not initialize class sun.security.ssl.SSLContextImpl$CustomizedTLSContext
         // 2019/06/20 : To avoid this issue, we are using the HTTP address of WWO and not the HTTPS.

         final HttpRequest request = HttpRequest.newBuilder(URI.create(weatherRequestWithParameters)).GET().build();

         final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

         weatherHistory = response.body();

      } catch (final Exception ex) {
         StatusUtil.log(
               "WeatherHistoryRetriever.processRequest : Error while executing the historical weather request with the parameters " //$NON-NLS-1$
                     + weatherRequestWithParameters + "\n" + ex.getMessage()); //$NON-NLS-1$
         return UI.EMPTY_STRING;
      }

      return weatherHistory;
   }

}
