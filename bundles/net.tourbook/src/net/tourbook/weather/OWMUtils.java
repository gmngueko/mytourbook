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
package net.tourbook.weather;

public class OWMUtils {
   public static String OWM_Temperature_UUID         = "068d3ba6-4acc-48ba-a370-6164af242b78";                                                      //$NON-NLS-1$
   public static String OWM_Temperature_Name         = "OWM Temperature";                                                                           //$NON-NLS-1$
   public static String OWM_Temperature_Unit         = "°C";                                                                                        //$NON-NLS-1$
   public static String OWM_Temperature_Comment      = "Open Weather Map Temperature";                                                              //$NON-NLS-1$
   public static String OWM_Temperature_Feel_UUID    = "81691be7-3bd3-458b-a775-8420f55fb388";                                                      //$NON-NLS-1$
   public static String OWM_Temperature_Feel_Name    = "OWM Temperature Feel";                                                                      //$NON-NLS-1$
   public static String OWM_Temperature_Feel_Unit    = "°C";                                                                                        //$NON-NLS-1$
   public static String OWM_Temperature_Feel_Comment = "Open Weather Map Temperature Feel Like";                                                    //$NON-NLS-1$
   public static String OWM_Clouds_UUID              = "5d33342c-c165-4836-813b-440f738468c5";                                                      //$NON-NLS-1$
   public static String OWM_Clouds_Name              = "OWM Clouds";                                                                                //$NON-NLS-1$
   public static String OWM_Clouds_Unit              = "%";                                                                                         //$NON-NLS-1$
   public static String OWM_Clouds_Comment           = "Open Weather Map Cloudiness";                                                               //$NON-NLS-1$
   public static String OWM_Dew_Point_UUID           = "39101d1c-6798-4b2b-b76e-88e2d4cddb68";                                                      //$NON-NLS-1$
   public static String OWM_Dew_Point_Name           = "OWM Dew Point";                                                                             //$NON-NLS-1$
   public static String OWM_Dew_Point_Unit           = "°C";                                                                                        //$NON-NLS-1$
   public static String OWM_Dew_Point_Comment        =
         "OWM Atmospheric temperature (varying according to pressure and humidity) below which water droplets begin to condense and dew can form."; //$NON-NLS-1$
   public static String OWM_Humidity_UUID            = "5f632c2a-8485-4aa4-88cc-4e2ee57f8052";                                                      //$NON-NLS-1$
   public static String OWM_Humidity_Name            = "OWM Humidity";                                                                              //$NON-NLS-1$
   public static String OWM_Humidity_Unit            = "%";                                                                                         //$NON-NLS-1$
   public static String OWM_Humidity_Comment         = "Open Weather Map Humidity";                                                                 //$NON-NLS-1$
   public static String OWM_Snow_Name                = "OWM Snow";                                                                                  //$NON-NLS-1$
   public static String OWM_Snow_Unit                = "mm/h";                                                                                      //$NON-NLS-1$
   public static String OWM_Snow_Comment             = "Open Weather Map Snow fall mm";                                                             //$NON-NLS-1$
   public static String OWM_Snow_UUID                = "902bc3a0-72e1-42e8-b030-660995ec3dde";                                                      //"bcbba2ea-aab5-4942-a706-8d3f10f0e743"; //$NON-NLS-1$
   public static String OWM_Precipitation_UUID       = "c74a6603-1876-4ec8-9da4-197604e4d3ae";                                                      //$NON-NLS-1$
   public static String OWM_Precipitation_Name       = "OWM Precipitation";                                                                         //$NON-NLS-1$
   public static String OWM_Precipitation_Unit       = "mm/h";                                                                                      //$NON-NLS-1$
   public static String OWM_Precipitation_Comment    = "Open Weather Map Precipitation (Rain) mm";                                                  //$NON-NLS-1$
   public static String OWM_Pressure_UUID            = "54fc167e-dfec-46f0-ae8a-d265ef083042";                                                      //$NON-NLS-1$
   public static String OWM_Pressure_Name            = "OWM Pressure";                                                                              //$NON-NLS-1$
   public static String OWM_Pressure_Unit            = "hPa";                                                                                       //$NON-NLS-1$
   public static String OWM_Pressure_Comment         = "Open Weather Map Atmospheric pressure on the sea level";                                    //$NON-NLS-1$
   public static String OWM_UV_Index_UUID            = "d2aee82d-f4e5-4622-8dd0-bbbfe30a05ea";                                                      //$NON-NLS-1$
   public static String OWM_UV_Index_Name            = "OWM UV Index";                                                                              //$NON-NLS-1$
   public static String OWM_UV_Index_Unit            = "";                                                                                          //$NON-NLS-1$
   public static String OWM_Visibility_UUID          = "b3fc36df-0509-42ee-9b29-3366b437b4df";                                                      //$NON-NLS-1$
   public static String OWM_Visibility_Name          = "OWM Visibility";                                                                            //$NON-NLS-1$
   public static String OWM_Visibility_Unit          = "m";                                                                                         //$NON-NLS-1$
   public static String OWM_Visibility_Comment       = "Open Weather Map Average visibility";                                                       //$NON-NLS-1$
   public static String OWM_Wind_Direction_UUID      = "4a1a7700-7db1-4f95-a895-8d9f72d95b2e";                                                      //$NON-NLS-1$
   public static String OWM_Wind_Direction_Name      = "OWM Wind Direction";                                                                        //$NON-NLS-1$
   public static String OWM_Wind_Direction_Unit      = "°";                                                                                         //$NON-NLS-1$
   public static String OWM_Wind_Direction_Comment   = "Open Weather Map Wind direction";                                                           //$NON-NLS-1$
   public static String OWM_Wind_Gust_UUID           = "31200332-40b9-43d7-927e-fc8b0fe9e2dd";                                                      //$NON-NLS-1$
   public static String OWM_Wind_Gust_Name           = "OWM Wind Gust";                                                                             //$NON-NLS-1$
   public static String OWM_Wind_Gust_Unit           = "m/s";                                                                                       //$NON-NLS-1$
   public static String OWM_Wind_Gust_Comment        = "Open Weather Map Wind Gust";                                                                //$NON-NLS-1$
   public static String OWM_Wind_Speed_UUID          = "a5f58241-2a30-409a-a740-8ad06ee2cc15";                                                      //$NON-NLS-1$
   public static String OWM_Wind_Speed_Name          = "OWM Wind Speed";                                                                            //$NON-NLS-1$
   public static String OWM_Wind_Speed_Unit          = "m/s";                                                                                       //$NON-NLS-1$
   public static String OWM_Wind_Speed_Comment       = "Open Weather Map Wind Speed";                                                               //$NON-NLS-1$
   public static String OWM_Wind_Tail_UUID           = "fd18a4bd-49f3-409c-b1e2-a0256be7c862";                                                      //$NON-NLS-1$
   public static String OWM_Wind_Tail_Name           = "OWM TailWind Speed";                                                                        //$NON-NLS-1$
   public static String OWM_Wind_Tail_Unit           = "m/s";                                                                                       //$NON-NLS-1$
   public static String OWM_Wind_Tail_Comment        = "Tail Wind Speed using OWM data";                                                            //$NON-NLS-1$
   public static String OWM_Wind_Cross_UUID          = "33e0e489-2811-4223-9725-760af08d1e21";                                                      //$NON-NLS-1$
   public static String OWM_Wind_Cross_Name          = "OWM CrossWind Speed";                                                                       //$NON-NLS-1$
   public static String OWM_Wind_Cross_Unit          = "m/s";                                                                                       //$NON-NLS-1$
   public static String OWM_Wind_Cross_Comment       = "Cross Wind Speed using OWM data";                                                           //$NON-NLS-1$
   public static String OWM_GPS_Direction_UUID       = "ec14691d-7071-4584-bf34-1083ea3d99ef";                                                      //$NON-NLS-1$
   public static String OWM_GPS_Direction_Name       = "OWM GPS Direction";                                                                         //$NON-NLS-1$
   public static String OWM_GPS_Direction_Unit       = "°";                                                                                         //$NON-NLS-1$
   public static String OWM_GPS_Direction_Comment    = "Calculated GPS Direction";                                                                  //$NON-NLS-1$

   public static String Sensor_Temperature_Name      = "Sensor Temperature";                                                                        //$NON-NLS-1$
   public static String Sensor_Temperature_UUID      = "98f3fa2a-f488-4b82-8875-69e2ec23f068";                                                      //$NON-NLS-1$
   public static String Sensor_Temperature_Unit      = "°C";                                                                                        //$NON-NLS-1$
   public static String Sensor_Temperature_Comment   = "Temperature before OWM retrieval";                                                          //$NON-NLS-1$
   //
   public static int    TailWind_Delta_seconds       = 10;                                                                                          //10 sec

}
