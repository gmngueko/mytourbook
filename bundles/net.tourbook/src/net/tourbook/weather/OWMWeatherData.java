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
      if (OWM_Pressure_Present)
      {
          if (summary.length() > 0) {
            summary += "; "; //$NON-NLS-1$
         }
          summary += "Pressure:"; //$NON-NLS-1$
          summary += String.format("%d", averagePressure) + "mb"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (OWM_Humidity_Present)
      {
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
