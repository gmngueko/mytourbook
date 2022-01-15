/*******************************************************************************
 * Copyright (C) 2021 Gervais-Martial Ngueko
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

import java.util.HashMap;
import java.util.Map;

import net.tourbook.common.UI;
import net.tourbook.common.weather.IWeather;
import net.tourbook.data.CustomTrackDefinition;
import net.tourbook.data.TourData;
import net.tourbook.weather.OWMResults.OWMWeather_Main_Map;

/**
 * Class to store data from the Open Weather Map API.
 * Documentation : https://openweathermap.org/guide
 */
public class OWMWeatherData {

   public float[]                   OWM_Temperature_Serie;
   public float[]                   OWM_Temperature_Feel_Serie;

   public float[]                   OWM_Clouds_Serie;
   public float[]                   OWM_Dew_Points_Serie;
   public float[]                   OWM_Humidity_Serie;
   public float[]                   OWM_Precipitation_Serie;
   public float[]                   OWM_Snow_Serie;
   public float[]                   OWM_Pressure_Serie;
   public float[]                   OWM_UV_Index_Serie;
   public float[]                   OWM_Visibility_Serie;
   public float[]                   OWM_Wind_Direction_Serie;
   public float[]                   OWM_Wind_Gust_Serie;
   public float[]                   OWM_Wind_Speed_Serie;

   public float[]                   OWM_Wind_Tail_Serie;
   public float[]                   OWM_Wind_Cross_Serie;
   public float[]                   OWM_GPS_Direction_Serie;

   boolean                          OWM_Temperature_Present      = false;
   boolean                          OWM_Temperature_Feel_Present = false;

   boolean                          OWM_Clouds_Present           = false;
   boolean                          OWM_Dew_Points_Present       = false;
   boolean                          OWM_Humidity_Present         = false;
   boolean                          OWM_Precipitation_Present    = false;
   boolean                          OWM_Snow_Present             = false;
   boolean                          OWM_Pressure_Present         = false;
   boolean                          OWM_UV_Index_Present         = false;
   boolean                          OWM_Visibility_Present       = false;
   boolean                          OWM_Wind_Direction_Present   = false;
   boolean                          OWM_Wind_Gust_Present        = false;
   boolean                          OWM_Wind_Speed_Present       = false;

   boolean                          OWM_Wind_Tail_Present        = false;
   boolean                          OWM_Wind_Cross_Present       = false;
   boolean                          OWM_GPS_Direction_Present    = false;

   Map<String, OWMWeather_Main_Map> OWM_Weather_Map              = new HashMap<>();

   int                              intervalRetrievalSeconds     = -1;

   private float                    maxTemperature;
   private float                    minTemperature;
   private float                    averageTemperature;

   private int                      WindDirection;
   private int                      WindSpeed;
   /**
    * Precipitation in millimeters
    */
   private float                    precipitation;
   private float                    averagePrecipitationPerHour;

   /**
    * Snow Precipitation in millimeters
    */
   private float                    precipitationSnow;
   private float                    averagePrecipitationSnowPerHour;

   private String                   WeatherDescription;
   private String                   WeatherType                  = IWeather.cloudIsNotDefined;//default weather is undefined

   /**
    * Humidity in percentage (%)
    */
   private int                      averageHumidity;
   /**
    * Atmospheric pressure in millibars (mb)
    */
   private int                      averagePressure;

   private int                      windChill;

   public String computeWeatherSummaryDescription() {
      String summary = UI.EMPTY_STRING;

      if (OWM_Temperature_Present) {

         summary += "Min./Max./Avg.:"; //$NON-NLS-1$
         summary += String.format("%.1f", minTemperature) + "°C/"; //$NON-NLS-1$ //$NON-NLS-2$
         summary += String.format("%.1f", maxTemperature) + "°C/"; //$NON-NLS-1$ //$NON-NLS-2$
         summary += String.format("%.1f", averageTemperature) + "°C"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (OWM_Pressure_Present) {
         if (summary.length() > 0) {
            summary += "; "; //$NON-NLS-1$
         }
         summary += "Pressure:"; //$NON-NLS-1$
         summary += String.format("%d", averagePressure) + "hPa"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (OWM_Humidity_Present) {
         if (summary.length() > 0) {
            summary += "; "; //$NON-NLS-1$
         }
         summary += "Humidity:"; //$NON-NLS-1$
         summary += String.format("%d", averageHumidity) + "%"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (OWM_Wind_Speed_Present) {
         if (summary.length() > 0) {
            summary += "; "; //$NON-NLS-1$
         }
         summary += "Wind Speed:"; //$NON-NLS-1$
         summary += String.format("%.1f", (float) (3.6 * WindSpeed)) + "K/h"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (OWM_Snow_Present) {
         if (summary.length() > 0) {
            summary += "; "; //$NON-NLS-1$
         }
         summary += "Snow(Avg.1h/Tot.):"; //$NON-NLS-1$
         summary += String.format("%.2f", averagePrecipitationSnowPerHour) + "mm/"; //$NON-NLS-1$ //$NON-NLS-2$
         summary += String.format("%.2f", precipitationSnow) + "mm"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (OWM_Precipitation_Present) {
         if (summary.length() > 0) {
            summary += "; "; //$NON-NLS-1$
         }
         summary += "Rain(Avg.1h/Tot.):"; //$NON-NLS-1$
         summary += String.format("%.2f", averagePrecipitationPerHour) + "mm/"; //$NON-NLS-1$ //$NON-NLS-2$
         summary += String.format("%.2f", precipitation) + "mm"; //$NON-NLS-1$ //$NON-NLS-2$
      }

      return summary;
   }

   public void computeWindTour(final TourData tour, final int intervalSeconds) {
      OWMWindCompute windResult;

      if (tour.latitudeSerie == null || tour.longitudeSerie == null || !OWM_Wind_Direction_Present || !OWM_Wind_Speed_Present) {
         return;
      }
      if (tour.timeSerie[tour.timeSerie.length - 1] < (2 * OWMUtils.TailWind_Delta_seconds)) {
         return;//tour duration too short
      }
      if (tour.timeSerie.length < 3) {
         return;//not enough data
      }
      if (intervalSeconds <= 0) {
         return;
      }

      OWM_Wind_Tail_Serie = new float[tour.timeSerie.length];
      OWM_Wind_Cross_Serie = new float[tour.timeSerie.length];
      OWM_GPS_Direction_Serie = new float[tour.timeSerie.length];

      OWM_Wind_Tail_Present = true;
      OWM_Wind_Cross_Present = true;
      OWM_GPS_Direction_Present = true;

      int jumpIndex = 0;
      for (int serieIndex = 0; serieIndex < tour.timeSerie.length; serieIndex++) {
         if (tour.timeSerie[serieIndex] / intervalSeconds >= jumpIndex || serieIndex == (tour.timeSerie.length - 1)) {
            jumpIndex++;

            if ((serieIndex >= tour.latitudeSerie.length) || (serieIndex >= tour.longitudeSerie.length)) {
               continue;
            }

            final float windDir = OWM_Wind_Direction_Serie[serieIndex];
            int serieIndex2 = serieIndex + 1;
            final float windSpeed = OWM_Wind_Speed_Serie[serieIndex];

            if (serieIndex != (tour.timeSerie.length - 1)) {
               for (int j = serieIndex + 2; j < tour.timeSerie.length; j++) {
                  if ((tour.timeSerie[j] - tour.timeSerie[serieIndex]) > OWMUtils.TailWind_Delta_seconds) {
                     break;
                  } else {
                     serieIndex2 = j;
                  }
               }
            } else {
               serieIndex2 = serieIndex - 1;
               for (int j = serieIndex - 2; j < tour.timeSerie.length; j--) {
                  if ((tour.timeSerie[serieIndex] - tour.timeSerie[j]) > OWMUtils.TailWind_Delta_seconds) {
                     break;
                  } else {
                     serieIndex2 = j;
                  }
               }
            }

            float latituteStart = (float) tour.latitudeSerie[serieIndex];
            float latituteEnd = (float) tour.latitudeSerie[serieIndex2];
            float longitudeStart = (float) tour.longitudeSerie[serieIndex];
            float longitudeEnd = (float) tour.longitudeSerie[serieIndex2];
            if (serieIndex == (tour.timeSerie.length - 1)) {
               latituteStart = (float) tour.latitudeSerie[serieIndex2];
               latituteEnd = (float) tour.latitudeSerie[serieIndex];
               longitudeStart = (float) tour.longitudeSerie[serieIndex2];
               longitudeEnd = (float) tour.longitudeSerie[serieIndex];
            }

            windResult = OWMWindCompute.computeWind(latituteStart, longitudeStart, latituteEnd, longitudeEnd, windDir, windSpeed);
            OWM_Wind_Tail_Serie[serieIndex] = windResult.tail;
            OWM_Wind_Cross_Serie[serieIndex] = windResult.cross;
            OWM_GPS_Direction_Serie[serieIndex] = windResult.gpsDir;
         }
      }
   }

   public int getAverageHumidity() {
      return averageHumidity;
   }

   public float getAveragePrecipitationPerHour() {
      return averagePrecipitationPerHour;
   }

   public float getAveragePrecipitationSnowPerHour() {
      return averagePrecipitationSnowPerHour;
   }

   public int getAveragePressure() {
      return averagePressure;
   }

   public float getPrecipitation() {
      return precipitation;
   }

   public float getPrecipitationSnow() {
      return precipitationSnow;
   }

   public float getTemperatureAverage() {
      return averageTemperature;
   }

   public float getTemperatureMax() {
      return maxTemperature;
   }

   public float getTemperatureMin() {
      return minTemperature;
   }

   public String getWeatherDescription() {
      return WeatherDescription;
   }

   public String getWeatherType() {
      return WeatherType;
   }

   public int getWindChill() {
      return windChill;
   }

   public int getWindDirection() {
      return WindDirection;
   }

   public int getWindSpeed() {
      return WindSpeed;
   }

   public void initWeatherSeries(final int lenght) {
      OWM_Temperature_Serie = new float[lenght];
      OWM_Temperature_Feel_Serie = new float[lenght];
      OWM_Clouds_Serie = new float[lenght];
      OWM_Dew_Points_Serie = new float[lenght];
      OWM_Humidity_Serie = new float[lenght];
      OWM_Precipitation_Serie = new float[lenght];
      OWM_Snow_Serie = new float[lenght];
      OWM_Pressure_Serie = new float[lenght];
      OWM_UV_Index_Serie = new float[lenght];
      OWM_Visibility_Serie = new float[lenght];
      OWM_Wind_Direction_Serie = new float[lenght];
      OWM_Wind_Gust_Serie = new float[lenght];
      OWM_Wind_Speed_Serie = new float[lenght];
   }

   public void setAverageHumidity(final int averageHumidity) {
      this.averageHumidity = averageHumidity;
   }

   public void setAveragePrecipitationPerHour(final float averagePrecipitationPerHour) {
      this.averagePrecipitationPerHour = averagePrecipitationPerHour;
   }

   public void setAveragePrecipitationSnowPerHour(final float averagePrecipitationSnowPerHour) {
      this.averagePrecipitationSnowPerHour = averagePrecipitationSnowPerHour;
   }

   public void setAveragePressure(final int averagePressure) {
      this.averagePressure = averagePressure;
   }

   public void setCustomTracks(final TourData tourData) {
      final HashMap<String, float[]> newCustTracks = tourData.getCustomTracksInit();
      String customTrackId;
      /*
       * if (newCustTracks == null) {
       * newCustTracks = new HashMap<>();
       * }
       */
      if (tourData.customTracksDefinition == null) {
         tourData.customTracksDefinition = new HashMap<>();
      }

      if (OWM_Temperature_Present) {
         customTrackId = OWMUtils.OWM_Temperature_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Temperature_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Temperature_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Temperature_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Temperature_Serie);
      }
      if (OWM_Temperature_Feel_Present) {
         customTrackId = OWMUtils.OWM_Temperature_Feel_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Temperature_Feel_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Temperature_Feel_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Temperature_Feel_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Temperature_Feel_Serie);
      }
      if (OWM_Clouds_Present) {
         customTrackId = OWMUtils.OWM_Clouds_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Clouds_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Clouds_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Clouds_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Clouds_Serie);
      }
      if (OWM_Dew_Points_Present) {
         customTrackId = OWMUtils.OWM_Dew_Point_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Dew_Point_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Dew_Point_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Dew_Point_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Dew_Points_Serie);
      }
      if (OWM_Humidity_Present) {
         customTrackId = OWMUtils.OWM_Humidity_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Humidity_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Humidity_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Humidity_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Humidity_Serie);
      }
      if (OWM_Precipitation_Present) {
         customTrackId = OWMUtils.OWM_Precipitation_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Precipitation_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Precipitation_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Precipitation_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Precipitation_Serie);
      }
      if (OWM_Snow_Present) {
         customTrackId = OWMUtils.OWM_Snow_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Snow_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Snow_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Snow_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Snow_Serie);
      }
      if (OWM_Pressure_Present) {
         customTrackId = OWMUtils.OWM_Pressure_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Pressure_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Pressure_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Pressure_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Pressure_Serie);
      }
      if (OWM_UV_Index_Present) {
         customTrackId = OWMUtils.OWM_UV_Index_Name + UI.SYMBOL_COLON + OWMUtils.OWM_UV_Index_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_UV_Index_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_UV_Index_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_UV_Index_Serie);
      }
      if (OWM_Visibility_Present) {
         customTrackId = OWMUtils.OWM_Visibility_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Visibility_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Visibility_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Visibility_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Visibility_Serie);
      }
      if (OWM_Wind_Direction_Present) {
         customTrackId = OWMUtils.OWM_Wind_Direction_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Wind_Direction_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Wind_Direction_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Wind_Direction_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Wind_Direction_Serie);
      }
      if (OWM_Wind_Gust_Present) {
         customTrackId = OWMUtils.OWM_Wind_Gust_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Wind_Gust_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Wind_Gust_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Wind_Gust_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Wind_Gust_Serie);
      }
      if (OWM_Wind_Speed_Present) {
         customTrackId = OWMUtils.OWM_Wind_Speed_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Wind_Speed_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Wind_Speed_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Wind_Speed_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Wind_Speed_Serie);
      }

      if (OWM_Wind_Tail_Present) {
         customTrackId = OWMUtils.OWM_Wind_Tail_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Wind_Tail_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Wind_Tail_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Wind_Tail_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Wind_Tail_Serie);
      }
      if (OWM_Wind_Cross_Present) {
         customTrackId = OWMUtils.OWM_Wind_Cross_Name + UI.SYMBOL_COLON + OWMUtils.OWM_Wind_Cross_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_Wind_Cross_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_Wind_Cross_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_Wind_Cross_Serie);
      }
      if (OWM_GPS_Direction_Present) {
         customTrackId = OWMUtils.OWM_GPS_Direction_Name + UI.SYMBOL_COLON + OWMUtils.OWM_GPS_Direction_UUID;
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.OWM_GPS_Direction_Name);
         customTrackDefinition.setUnit(OWMUtils.OWM_GPS_Direction_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         newCustTracks.put(customTrackId, OWM_GPS_Direction_Serie);
      }

      customTrackId = OWMUtils.Sensor_Temperature_Name + UI.SYMBOL_COLON + OWMUtils.Sensor_Temperature_UUID;
      if (OWM_Temperature_Present && !tourData.customTracksDefinition.containsKey(customTrackId) && tourData.temperatureSerie != null) {
         final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
         customTrackDefinition.setRefId(customTrackId);
         customTrackDefinition.setName(OWMUtils.Sensor_Temperature_Name);
         customTrackDefinition.setUnit(OWMUtils.Sensor_Temperature_Unit);
         tourData.customTracksDefinition.put(customTrackId, customTrackDefinition);
         final float[] newTemperature = new float[tourData.temperatureSerie.length];
         System.arraycopy(tourData.temperatureSerie, 0, newTemperature, 0, tourData.temperatureSerie.length);
         newCustTracks.put(customTrackId, newTemperature);
      }
      if (OWM_Temperature_Present) {
         //replace current temperature series with OWM temperature
         tourData.temperatureSerie = new float[OWM_Temperature_Serie.length];
         System.arraycopy(OWM_Temperature_Serie, 0, tourData.temperatureSerie, 0, OWM_Temperature_Serie.length);
      }

      //tourData.setCustomTracks(newCustTracks);
   }

   public void setPrecipitation(final float precipitation) {
      this.precipitation = precipitation;
   }

   public void setPrecipitationSnow(final float precipitationSnow) {
      this.precipitationSnow = precipitationSnow;
   }

   public void setTemperatureAverage(final float temperatureAverage) {
      averageTemperature = temperatureAverage;
   }

   public void setTemperatureMax(final float temperatureMax) {
      maxTemperature = temperatureMax;
   }

   public void setTemperatureMin(final float temperatureMin) {
      minTemperature = temperatureMin;
   }

   public void setWeatherDescription(final String weatherDescription) {
      WeatherDescription = weatherDescription;
   }

   public void setWeatherType(final int weatherCode) {

      // Codes : https://openweathermap.org/weather-conditions

      switch (weatherCode) {
      case 803:
      case 804:
         WeatherType = IWeather.WEATHER_ID_OVERCAST;
         break;
      case 800:
         WeatherType = IWeather.WEATHER_ID_CLEAR;
         break;
      case 701:
      case 721:
      case 741:
      case 801:
      case 802:
         WeatherType = IWeather.WEATHER_ID_PART_CLOUDS;
         break;
      case 200://Thunderstorm
      case 201:
      case 202:
      case 210:
      case 211:
      case 212:
      case 221:
      case 230:
      case 231:
      case 232:
         WeatherType = IWeather.WEATHER_ID_LIGHTNING;
         break;
      case 300://Drizzle
      case 301:
      case 302:
      case 310:
      case 311:
      case 312:
      case 313:
      case 314:
      case 321:
      case 500://Rain
      case 501:
      case 502:
      case 503:
      case 504:
      case 511:
         WeatherType = IWeather.WEATHER_ID_RAIN;
         break;
      case 600:
      case 601:
      case 602:
      case 611:
      case 612:
      case 613:
      case 615:
      case 616:
      case 620:
      case 621:
      case 622:
         WeatherType = IWeather.WEATHER_ID_SNOW;
         break;
      case 711:
      case 731:
      case 762:
      case 781:
         WeatherType = IWeather.WEATHER_ID_SEVERE_WEATHER_ALERT;
         break;
      case 520://light intensity shower rain
      case 521:
      case 522:
      case 531:
         WeatherType = IWeather.WEATHER_ID_SCATTERED_SHOWERS;
         break;
      default:
         WeatherType = IWeather.cloudIsNotDefined;
         break;
      }
   }

   public void setWindChill(final int windChill) {
      this.windChill = windChill;
   }

   public void setWindDirection(final int windDirection) {
      WindDirection = windDirection;
   }

   public void setWindSpeed(final int windSpeed) {
      WindSpeed = windSpeed;
   }
}
