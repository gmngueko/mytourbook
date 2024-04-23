/*******************************************************************************
 * Copyright (C) 2019, 2024 Frédéric Bard
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
package net.tourbook.weather.worldweatheronline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import net.tourbook.common.UI;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.StringUtils;

/**
 * A Java representation of a World Weather Online query result "hourly" element.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Hourly(@JsonProperty("UTCdate") String utcDate,
                     @JsonProperty("UTCtime") String utcTime,
                     String windspeedKmph,
                     String winddirDegree,
                     List<ValueResult> weatherDesc,
                     String weatherCode,
                     /**
                      * Feels like temperature in degrees Celsius (windchill)
                      */
                     @JsonProperty("FeelsLikeC") String feelsLikeC,
                     /**
                      * Temperature in degrees Celsius
                      */
                     String tempC,
                     /**
                      * Atmospheric pressure in millibars (mb)
                      */
                     String pressure,
                     /**
                      * Humidity in percentage (%)
                      */
                     int humidity,
                     /**
                      * Precipitation in millimeters
                      */
                     String precipMM) {

   public long getEpochSeconds() {

      if (StringUtils.isNullOrEmpty(utcTime)) {
         return 0;
      }

      final int timeHours = utcTime.equals("0") || utcTime.length() < 2//$NON-NLS-1$
            ? 0
            : Integer.parseInt(utcTime.substring(0, utcTime.length() - 2));
      final String dateTime = utcDate +
            UI.SPACE +
            String.format("%02d", timeHours) + //$NON-NLS-1$
            UI.SYMBOL_COLON +
            "00"; //$NON-NLS-1$
      final ZonedDateTime zonedDateTime = LocalDateTime.parse(
            dateTime,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) //$NON-NLS-1$
            .atZone(TimeTools.UTC);

      final long timeEpochSeconds = zonedDateTime.toInstant().toEpochMilli() / 1000;

      return timeEpochSeconds;
   }

   public int getFeelsLikeC() {
      return Integer.parseInt(feelsLikeC);
   }

   public float getPrecipMM() {
      return Float.parseFloat(precipMM);
   }

   public int getPressure() {
      return Integer.parseInt(pressure);
   }

   public int getTempC() {
      return Integer.parseInt(tempC);
   }

   public String getWeatherDescription() {
      return weatherDesc().get(0).value();
   }

   public int getWinddirDegree() {
      return Integer.parseInt(winddirDegree);
   }

   public int getWindspeedKmph() {
      return Integer.parseInt(windspeedKmph);
   }
}
