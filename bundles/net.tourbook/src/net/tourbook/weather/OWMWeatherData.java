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

import net.tourbook.common.weather.IWeather;
import net.tourbook.weather.OWMResults.OWMWeather_Main_Map;

/**
 * Class to store data from the Open Weather Map API.
 * Documentation : https://openweathermap.org/guide
 */
public class OWMWeatherData {

   public float[] OWM_Temperature_Serie;
   public float[] OWM_Temperature_Feel_Serie;
   public float[] OWM_Clouds_Serie;
   public float[] OWM_Dew_Points_Serie;
   public float[] OWM_Humidity_Serie;
   public float[] OWM_Precipitation_Serie;
   public float[] OWM_Snow_Serie;
   public float[] OWM_Pressure_Serie;
   public float[] OWM_UV_Index_Serie;
   public float[] OWM_Visibility_Serie;
   public float[] OWM_Wind_Direction_Serie;
   public float[] OWM_Wind_Gust_Serie;
   public float[] OWM_Wind_Speed_Serie;

   boolean        OWM_Temperature_Present      = false;
   boolean        OWM_Temperature_Feel_Present = false;
   boolean        OWM_Clouds_Present           = false;
   boolean        OWM_Dew_Points_Present       = false;
   boolean        OWM_Humidity_Present         = false;
   boolean        OWM_Precipitation_Present    = false;
   boolean        OWM_Snow_Present             = false;
   boolean        OWM_Pressure_Present         = false;
   boolean        OWM_UV_Index_Present         = false;
   boolean        OWM_Visibility_Present       = false;
   boolean        OWM_Wind_Direction_Present   = false;
   boolean        OWM_Wind_Gust_Present        = false;
   boolean        OWM_Wind_Speed_Present       = false;

   Map<String, OWMWeather_Main_Map> OWM_Weather_Map              = new HashMap<>();

   private int    maxTemperature;
   private int    minTemperature;
   private int    averageTemperature;
   private int    WindDirection;
   private int    WindSpeed;

   /**
    * Precipitation in millimeters
    */
   private float  precipitation;

   private String WeatherDescription;
   private String WeatherType;

   /**
    * Humidity in percentage (%)
    */
   private int    averageHumidity;

   /**
    * Atmospheric pressure in millibars (mb)
    */
   private int    averagePressure;

   private int    windChill;

   public int getAverageHumidity() {
      return averageHumidity;
   }

   public int getAveragePressure() {
      return averagePressure;
   }

   public float getPrecipitation() {
      return precipitation;
   }

   public int getTemperatureAverage() {
      return averageTemperature;
   }

   public int getTemperatureMax() {
      return maxTemperature;
   }

   public int getTemperatureMin() {
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

   public void setAveragePressure(final int averagePressure) {
      this.averagePressure = averagePressure;
   }

   public void setPrecipitation(final float precipitation) {
      this.precipitation = precipitation;
   }

   public void setTemperatureAverage(final int temperatureAverage) {
      averageTemperature = temperatureAverage;
   }

   public void setTemperatureMax(final int temperatureMax) {
      maxTemperature = temperatureMax;
   }

   public void setTemperatureMin(final int temperatureMin) {
      minTemperature = temperatureMin;
   }

   public void setWeatherDescription(final String weatherDescription) {
      WeatherDescription = weatherDescription;
   }

   public void setWeatherType(final String weatherCode) {

      // Codes : http://www.worldweatheronline.com/feed/wwoConditionCodes.xml

      switch (weatherCode) {
      case "122": //$NON-NLS-1$
         WeatherType = IWeather.WEATHER_ID_OVERCAST;
         break;
      case "113": //$NON-NLS-1$
         WeatherType = IWeather.WEATHER_ID_CLEAR;
         break;
      case "116": //$NON-NLS-1$
         WeatherType = IWeather.WEATHER_ID_PART_CLOUDS;
         break;
      //case "200":
      //    WeatherType = IWeather.WEATHER_ID_LIGHTNING;
      //   break;
      case "293": //$NON-NLS-1$
      case "296": //$NON-NLS-1$
      case "299": //$NON-NLS-1$
      case "302": //$NON-NLS-1$
      case "305": //$NON-NLS-1$
      case "308": //$NON-NLS-1$
      case "356": //$NON-NLS-1$
      case "359": //$NON-NLS-1$
         WeatherType = IWeather.WEATHER_ID_RAIN;
         break;
      case "332": //$NON-NLS-1$
      case "335": //$NON-NLS-1$
      case "329": //$NON-NLS-1$
      case "326": //$NON-NLS-1$
      case "323": //$NON-NLS-1$
      case "320": //$NON-NLS-1$
         WeatherType = IWeather.WEATHER_ID_SNOW;
         break;
      case "200": //$NON-NLS-1$
         WeatherType = IWeather.WEATHER_ID_SEVERE_WEATHER_ALERT;
         break;
      case "353": //$NON-NLS-1$
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
