/* Copyright (C) 2021 Gervais-Martial Ngueko
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
package net.tourbook.weather.openweathermapCustom;

public class OWMWindCompute {
   public Float tail   = null;//+tail wind; -headwind [cos(wind°-heading°)*wind speed]
   public Float cross  = null;//+crosswind East; -crosswind West [sin(wind°-heading°)*wind speed]
   public Float gpsDir = null;//GPS direction 0 to 360°

   public static OWMWindCompute computeWind(final float lati1,
                                            final float longi1,
                                            final float lati2,
                                            final float longi2,
                                            final float windDir,
                                            final float windSpeed) {
      final OWMWindCompute res = new OWMWindCompute();
      final double windDirRad = Math.PI * windDir / 180.0;
      final double lati1Rad = Math.PI * lati1 / 180.0;
      final double lati2Rad = Math.PI * lati2 / 180.0;
      final double longi1Rad = Math.PI * longi1 / 180.0;
      final double longi2Rad = Math.PI * longi2 / 180.0;

      final double y = Math.sin(longi2Rad - longi1Rad) * Math.cos(lati2Rad);
      final double x = Math.cos(lati1Rad) * Math.sin(lati2Rad) -
            Math.cos(lati2Rad) * Math.sin(lati1Rad) * Math.cos(longi2Rad - longi1Rad);
      final double θ = Math.atan2(y, x);
      final double brng = (θ * 180 / Math.PI + 360) % 360;

      res.tail = (float) (windSpeed * Math.cos(windDirRad - θ));
      res.cross = (float) (windSpeed * Math.sin(windDirRad - θ));
      res.gpsDir = (float) Math.round(brng);

      return res;
   }
}
