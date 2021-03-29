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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.Util;
import net.tourbook.data.TourData;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.ui.views.calendar.CalendarProfile;
import net.tourbook.weather.OWMResults.OWMResponse;
import net.tourbook.weather.OWMResults.OWMResponse_Weather;
import net.tourbook.weather.OWMResults.OWMWeather_Description_Map;
import net.tourbook.weather.OWMResults.OWMWeather_Main_Map;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * A class that retrieves, for a given track, the historical weather data.
 */
public class HistoricalWeatherOwmRetriever {

   private static final String  SYS_PROP__LOG_WEATHER_DATA = "logWeatherData";                                                      //$NON-NLS-1$
   private static final boolean _isLogWeatherData          = System.getProperty(SYS_PROP__LOG_WEATHER_DATA) != null;

   private static final String  DEFAULT_UNIT               = "metric"; //$NON-NLS-1$
   private static final String  DEFAULT_LATITUDE           = "50.85"; //$NON-NLS-1$
   private static final String  DEFAULT_LONGITUDE          = "4.42"; //$NON-NLS-1$

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

//      if (tour.latitudeSerie != null && tour.longitudeSerie != null) {
//         determineWeatherSearchArea();
//      }
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

   /*
    * Compute numerical integration
    * mainly used to compute total precipitation
    * */
   public static float numericalIntegrationOfSerie(final float[] serieData,
                                                   final int[] serieTime,
                                                   final double scaleFactor,
                                                   final int intervalSeconds) {
      float result = 0;
      int jumpIndex = 0;
      int indexCurrent = 0;
      int indexPrev = 0;
      for (int serieIndex = 0; serieIndex < serieTime.length; serieIndex++) {
         if (serieTime[serieIndex] / intervalSeconds >= jumpIndex || serieIndex == (serieTime.length - 1)) {
            if (jumpIndex == 0) {
               indexCurrent = serieIndex;
               jumpIndex++;
               continue;
            }
            jumpIndex++;
            indexPrev = indexCurrent;
            indexCurrent = serieIndex;
            result += (serieTime[indexCurrent] - serieTime[indexPrev]) * (serieData[indexCurrent] + serieData[indexPrev]) / (2);
         }
      }
      return (float) (result * scaleFactor);
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
    * Retrieves the historical weather data
    * @param defaultOWNLongitude
    * @param defaultOWNLatitude
    *
    * @return The weather data, if found.
    */
   public HistoricalWeatherOwmRetriever retrieveHistoricalWeatherData(final int intervalSeconds, final double defaultOWNLatitude, final double defaultOWNLongitude) {

      if (tour == null) {
         return null;
      }


      int sumHumidity = 0;
      int numHumidityDatasets = 0;

      int sumPressure = 0;
      int numPressureDatasets = 0;

      float sumPrecipitation = 0f;
      int numPrecipitationDatasets = 0;

      float sumSnow = 0f;
      int numSnowDatasets = 0;

      int sumWindChill = 0;
      int numWindChillDatasets = 0;

      int sumWindDirection = 0;
      int numWindDirectionDatasets = 0;

      float sumWindSpeed = 0;
      int numWindSpeedDatasets = 0;

      float sumTemperature = 0;
      int numTemperatureDatasets = 0;
      float maxTemperature = Float.MIN_VALUE;
      float minTemperature = Float.MAX_VALUE;

      historicalWeatherData.intervalRetrievalSeconds = intervalSeconds;

      int jumpIndex = 0;
      for (int serieIndex = 0; serieIndex < tour.timeSerie.length; serieIndex++) {
         if (tour.timeSerie[serieIndex] / intervalSeconds >= jumpIndex || serieIndex == (tour.timeSerie.length - 1)) {
            jumpIndex++;
            final long utcForWeather = utcStartTime + tour.timeSerie[serieIndex];
            final String rawWeatherData;
            if (tour.latitudeSerie != null && tour.longitudeSerie != null) {
               rawWeatherData = sendWeatherApiRequest(tour.latitudeSerie[serieIndex], tour.longitudeSerie[serieIndex], utcForWeather);
            } else {
               rawWeatherData = sendWeatherApiRequest(defaultOWNLatitude, defaultOWNLongitude, utcForWeather);
            }

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
                  if (rawWeatherResponse.current.feels_like != null) {
                     historicalWeatherData.OWM_Temperature_Feel_Present = true;
                     historicalWeatherData.OWM_Temperature_Feel_Serie[serieIndex] = rawWeatherResponse.current.feels_like;
                     sumWindChill += rawWeatherResponse.current.feels_like;
                     numWindChillDatasets++;
                  }
                  if (rawWeatherResponse.current.wind_speed != null) {
                     historicalWeatherData.OWM_Wind_Speed_Present = true;
                     historicalWeatherData.OWM_Wind_Speed_Serie[serieIndex] = rawWeatherResponse.current.wind_speed;
                     sumWindSpeed += rawWeatherResponse.current.wind_speed;
                     numWindSpeedDatasets++;
                  }
                  if (rawWeatherResponse.current.wind_deg != null) {
                     historicalWeatherData.OWM_Wind_Direction_Present = true;
                     historicalWeatherData.OWM_Wind_Direction_Serie[serieIndex] = rawWeatherResponse.current.wind_deg;
                     sumWindDirection += rawWeatherResponse.current.wind_deg;
                     numWindDirectionDatasets++;
                  }
                  if (rawWeatherResponse.current.humidity != null) {
                     historicalWeatherData.OWM_Humidity_Present = true;
                     historicalWeatherData.OWM_Humidity_Serie[serieIndex] = rawWeatherResponse.current.humidity;
                     sumHumidity += rawWeatherResponse.current.humidity;
                     numHumidityDatasets++;
                  }
                  if (rawWeatherResponse.current.pressure != null) {
                     historicalWeatherData.OWM_Pressure_Present = true;
                     historicalWeatherData.OWM_Pressure_Serie[serieIndex] = rawWeatherResponse.current.pressure;
                     sumPressure += rawWeatherResponse.current.pressure;
                     numPressureDatasets++;
                  }

                  //rain
                  if (rawWeatherResponse.current.rain != null && rawWeatherResponse.current.rain.h1 != null) {
                     historicalWeatherData.OWM_Precipitation_Serie[serieIndex] = rawWeatherResponse.current.rain.h1;
                  } else if (rawWeatherResponse.current.rain != null && rawWeatherResponse.current.rain.h3 != null) {
                     historicalWeatherData.OWM_Precipitation_Serie[serieIndex] = rawWeatherResponse.current.rain.h3 / 3;
                  } else {
                     historicalWeatherData.OWM_Precipitation_Serie[serieIndex] = 0;
                  }
                  //rain is alway accounted for
                  historicalWeatherData.OWM_Precipitation_Present = true;
                  numPrecipitationDatasets++;
                  sumPrecipitation += historicalWeatherData.OWM_Precipitation_Serie[serieIndex];

                  if (rawWeatherResponse.current.clouds != null) {
                     historicalWeatherData.OWM_Clouds_Present = true;
                     historicalWeatherData.OWM_Clouds_Serie[serieIndex] = rawWeatherResponse.current.clouds;
                  }
                  if (rawWeatherResponse.current.dew_point != null) {
                     historicalWeatherData.OWM_Dew_Points_Present = true;
                     historicalWeatherData.OWM_Dew_Points_Serie[serieIndex] = rawWeatherResponse.current.dew_point;
                  }
                  if (rawWeatherResponse.current.uvi != null) {
                     historicalWeatherData.OWM_UV_Index_Present = true;
                     historicalWeatherData.OWM_UV_Index_Serie[serieIndex] = rawWeatherResponse.current.uvi;
                  }
                  if (rawWeatherResponse.current.visibility != null) {
                     historicalWeatherData.OWM_Visibility_Present = true;
                     historicalWeatherData.OWM_Visibility_Serie[serieIndex] = rawWeatherResponse.current.visibility;
                  }

                  //snow is always accounted for
                  historicalWeatherData.OWM_Snow_Present = true;
                  if (rawWeatherResponse.current.snow != null && rawWeatherResponse.current.snow.h1 != null) {
                     historicalWeatherData.OWM_Snow_Serie[serieIndex] = rawWeatherResponse.current.snow.h1;
                  } else if (rawWeatherResponse.current.snow != null && rawWeatherResponse.current.snow.h3 != null) {
                     historicalWeatherData.OWM_Snow_Serie[serieIndex] = rawWeatherResponse.current.snow.h3 / 3;
                  } else {
                     historicalWeatherData.OWM_Snow_Serie[serieIndex] = 0;
                  }
                  numSnowDatasets++;
                  sumSnow += historicalWeatherData.OWM_Snow_Serie[serieIndex];

                  //wind gust always accounted for
                  historicalWeatherData.OWM_Wind_Gust_Present = true;
                  if (rawWeatherResponse.current.wind_gust != null) {
                     historicalWeatherData.OWM_Wind_Gust_Serie[serieIndex] = rawWeatherResponse.current.wind_gust;
                  } else {
                     historicalWeatherData.OWM_Wind_Gust_Serie[serieIndex] = 0;
                  }

                  if (rawWeatherResponse.current.weather != null && rawWeatherResponse.current.weather.length > 0) {
                     final OWMResponse_Weather item = rawWeatherResponse.current.weather[0];
                     if (item.main != null) {
                        OWMWeather_Main_Map mainW = null;
                        if (historicalWeatherData.OWM_Weather_Map.containsKey(item.main)) {
                           mainW = historicalWeatherData.OWM_Weather_Map.get(item.main);
                           mainW.weight += 1;
                           String descM = "null"; //$NON-NLS-1$
                           if (item.description != null) {
                              descM = item.description;
                           }
                           if (mainW.descriptionsMap.containsKey(descM)) {
                              final OWMWeather_Description_Map descW = mainW.descriptionsMap.get(descM);
                              descW.weight += 1;
                           } else {
                              final OWMWeather_Description_Map descW = new OWMWeather_Description_Map();
                              descW.description = descM;
                              descW.id = item.id;
                              descW.weight += 1;
                              mainW.descriptionsMap.put(descW.description, descW);
                           }
                        } else {
                           mainW = new OWMWeather_Main_Map();

                           mainW.main = item.main;
                           mainW.weight += 1;
                           String descM = "null"; //$NON-NLS-1$
                           if (item.description != null) {
                              descM = item.description;
                           }
                           if (mainW.descriptionsMap.containsKey(descM)) {
                              final OWMWeather_Description_Map descW = mainW.descriptionsMap.get(descM);
                              descW.weight += 1;
                           } else {
                              final OWMWeather_Description_Map descW = new OWMWeather_Description_Map();
                              descW.description = descM;
                              descW.id = item.id;
                              descW.weight += 1;
                              mainW.descriptionsMap.put(descW.description, descW);
                           }
                           historicalWeatherData.OWM_Weather_Map.put(mainW.main, mainW);
                        }
                     }
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

      if (numWindDirectionDatasets > 0) {
         historicalWeatherData.setWindDirection((int) Math.ceil((double) sumWindDirection / (double) numWindDirectionDatasets));
      }
      if (numWindSpeedDatasets > 0) {
         historicalWeatherData.setWindSpeed((int) Math.ceil((double) sumWindSpeed / (double) numWindSpeedDatasets));
      }
      historicalWeatherData.setTemperatureMax(maxTemperature);
      historicalWeatherData.setTemperatureMin(minTemperature);
      if (numTemperatureDatasets > 0) {
         historicalWeatherData.setTemperatureAverage(sumTemperature / numTemperatureDatasets);
      }
      if (numWindChillDatasets > 0) {
         historicalWeatherData.setWindChill((int) Math.ceil((double) sumWindChill / (double) numWindChillDatasets));
      }
      if (numHumidityDatasets > 0) {
         historicalWeatherData.setAverageHumidity((int) Math.ceil((double) sumHumidity / (double) numHumidityDatasets));
      }
      if (numPressureDatasets > 0) {
         historicalWeatherData.setAveragePressure((int) Math.ceil((double) sumPressure / (double) numPressureDatasets));
      }
      //historicalWeatherData.setPrecipitation(sumPrecipitation);

      //compute weight percent
      int weigthSum = 0;
      for (final Map.Entry<String, OWMWeather_Main_Map> item : historicalWeatherData.OWM_Weather_Map.entrySet()) {
         weigthSum += item.getValue().weight;
         int weigthSum2 = 0;
         for (final Map.Entry<String, OWMWeather_Description_Map> item2 : item.getValue().descriptionsMap.entrySet()) {
            weigthSum2 += item2.getValue().weight;
         }
         if (weigthSum2 > 0) {
            for (final Map.Entry<String, OWMWeather_Description_Map> item2 : item.getValue().descriptionsMap.entrySet()) {
               item2.getValue().weightPercent = (float) Math.round((double) item2.getValue().weight * 100.0 / weigthSum2);
            }
         }
      }
      if (weigthSum > 0)
      {
         for (final Map.Entry<String, OWMWeather_Main_Map> item : historicalWeatherData.OWM_Weather_Map.entrySet())
          {
              item.getValue().weightPercent = (float)Math.round((double)item.getValue().weight *100.0 / weigthSum);
          }
      }

      //sort the json
      final List<OWMWeather_Main_Map> WeatherCond = new ArrayList<>();
      for (final Map.Entry<String, OWMWeather_Main_Map> item : historicalWeatherData.OWM_Weather_Map.entrySet())
      {
          WeatherCond.add(item.getValue());
          for (final Map.Entry<String, OWMWeather_Description_Map> item2 : item.getValue().descriptionsMap.entrySet())
          {
              item.getValue().descriptions.add(item2.getValue());
          }
          Collections.sort(item.getValue().descriptions, new OWMResults.SortOWMWeather_Description_Mapbyweight());
      }
      Collections.sort(WeatherCond, new OWMResults.SortOWMWeather_Main_Mapbyweight());

      //compute weather type (condition)
      if(WeatherCond != null && WeatherCond.size() > 0) {
         if (WeatherCond.get(0) != null && WeatherCond.get(0).descriptions != null
               && WeatherCond.get(0).descriptions.get(0) != null
               && WeatherCond.get(0).descriptions.get(0).id != null) {
            historicalWeatherData.setWeatherType(WeatherCond.get(0).descriptions.get(0).id);
         }
      }

      //compute precipitation total
      float totalPrecipitation = 0;
      float avgPrecipitationPerHour = 0;
      if (sumPrecipitation > 0 && numPrecipitationDatasets > 0) {
         totalPrecipitation = numericalIntegrationOfSerie(historicalWeatherData.OWM_Precipitation_Serie,
               tour.timeSerie,
               1.0 / 3600.0,
               intervalSeconds);
         avgPrecipitationPerHour = sumPrecipitation / numPrecipitationDatasets;
      }
      historicalWeatherData.setPrecipitation(totalPrecipitation);
      historicalWeatherData.setAveragePrecipitationPerHour(avgPrecipitationPerHour);

      //compute precipitation Snow total
      float totalPrecipitationSnow = 0;
      float avgPrecipitationSnowPerHour = 0;
      if (sumSnow > 0 && numSnowDatasets > 0) {
         totalPrecipitationSnow = numericalIntegrationOfSerie(historicalWeatherData.OWM_Snow_Serie,
               tour.timeSerie,
               1.0 / 3600.0,
               intervalSeconds);
         avgPrecipitationSnowPerHour = sumSnow / numSnowDatasets;
      }
      historicalWeatherData.setPrecipitationSnow(totalPrecipitationSnow);
      historicalWeatherData.setAveragePrecipitationSnowPerHour(avgPrecipitationSnowPerHour);

      //TODO compute head/tail/cross wind GPS direction

      //add summary weather info in description ???
      final ObjectMapper mapper = new ObjectMapper();
      try {
         final String weatherNote = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(WeatherCond);
         String weatherSummary = historicalWeatherData.computeWeatherSummaryDescription() + UI.NEW_LINE1;
         weatherSummary += weatherNote;
         weatherSummary += UI.NEW_LINE1 + "OWM retrieval interval(seconds):" + intervalSeconds; //$NON-NLS-1$
         historicalWeatherData.setWeatherDescription(weatherSummary);
      } catch (final JsonProcessingException e) {
         StatusUtil.log(
               "OWMWeatherHistoryRetriever.parseWeatherData : Error while pretty printing weather condition notes :" //$NON-NLS-1$
                     + "\n" + e.getMessage()); //$NON-NLS-1$
         return null;
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
      weatherRequestWithParameters += latitudeParameter + String.format("%.2f", latitudeSerie); //$NON-NLS-1$
      weatherRequestWithParameters += longitudeParameter + String.format("%.2f", longitudeSerie); //$NON-NLS-1$
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
