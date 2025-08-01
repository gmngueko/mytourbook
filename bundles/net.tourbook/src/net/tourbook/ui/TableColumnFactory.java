/*******************************************************************************
 * Copyright (C) 2005, 2024 Wolfgang Schramm and Contributors
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
package net.tourbook.ui;

import net.tourbook.OtherMessages;
import net.tourbook.common.UI;
import net.tourbook.common.formatter.ValueFormat;
import net.tourbook.common.formatter.ValueFormatSet;
import net.tourbook.common.util.ColumnManager;
import net.tourbook.common.util.TableColumnDefinition;
import net.tourbook.data.CustomTrackDefinition;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;

public abstract class TableColumnFactory {

   public static final TableColumnFactory ALTITUDE_ALTITUDE;
   public static final TableColumnFactory ALTITUDE_AVG_CHANGE;
   public static final String             ALTITUDE_AVG_CHANGE_ID                             = "ALTITUDE_AVG_CHANGE";                             //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_DIFF_SEGMENT_BORDER;
   public static final TableColumnFactory ALTITUDE_DIFF_SEGMENT_COMPUTED;
   public static final TableColumnFactory ALTITUDE_ELEVATION_UP;
   public static final TableColumnFactory ALTITUDE_ELEVATION_DOWN;
   public static final TableColumnFactory ALTITUDE_ELEVATION_DIFF;
   public static final String             ALTITUDE_ELEVATION_DIFF_ID                         = "ALTITUDE_ELEVATION_DIFF_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_ELEVATION_GAIN;
   public static final String             ALTITUDE_ELEVATION_GAIN_ID                         = "ALTITUDE_ELEVATION_GAIN_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_ELEVATION_GAIN_DIFF;
   public static final String             ALTITUDE_ELEVATION_GAIN_DIFF_ID                    = "ALTITUDE_ELEVATION_GAIN_DIFF_ID";                 //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_ELEVATION_LOSS;
   public static final String             ALTITUDE_ELEVATION_LOSS_ID                         = "ALTITUDE_ELEVATION_LOSS_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_ELEVATION_LOSS_DIFF;
   public static final String             ALTITUDE_ELEVATION_LOSS_DIFF_ID                    = "ALTITUDE_ELEVATION_LOSS_DIFF_ID";                 //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_ELEVATION_SEGMENT_UP;
   public static final TableColumnFactory ALTITUDE_ELEVATION_SEGMENT_DOWN;
   public static final TableColumnFactory ALTITUDE_GRADIENT;
   public static final String             ALTITUDE_GRADIENT_ID                               = "ALTITUDE_GRADIENT";                               //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_GRADIENT_AVG;
   public static final TableColumnFactory ALTITUDE_MAX;
   public static final String             ALTITUDE_MAX_ID                                    = "ALTITUDE_MAX";                                    //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_SUMMARIZED_BORDER_DOWN;
   public static final String             ALTITUDE_SUMMARIZED_BORDER_DOWN_ID                 = "ALTITUDE_SUMMARIZED_BORDER_DOWN";                 //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_SUMMARIZED_BORDER_UP;
   public static final String             ALTITUDE_SUMMARIZED_BORDER_UP_ID                   = "ALTITUDE_SUMMARIZED_BORDER_UP";                   //$NON-NLS-1$
   public static final TableColumnFactory ALTITUDE_SUMMARIZED_COMPUTED_DOWN;
   public static final TableColumnFactory ALTITUDE_SUMMARIZED_COMPUTED_UP;

   public static final TableColumnFactory BODY_AVG_PULSE;
   public static final String             BODY_AVG_PULSE_ID                                  = "BODY_AVG_PULSE";                                  //$NON-NLS-1$
   public static final TableColumnFactory BODY_AVG_PULSE_DIFFERENCE;
   public static final TableColumnFactory BODY_CALORIES;
   public static final String             BODY_CALORIES_ID                                   = "BODY_CALORIES";                                   //$NON-NLS-1$
   public static final TableColumnFactory BODY_PERSON;
   public static final String             BODY_PERSON_ID                                     = "BODY_PERSON";                                     //$NON-NLS-1$
   public static final TableColumnFactory BODY_PULSE;
   public static final TableColumnFactory BODY_PULSE_MAX;
   public static final String             BODY_PULSE_MAX_ID                                  = "BODY_PULSE_MAX";                                  //$NON-NLS-1$
   public static final TableColumnFactory BODY_PULSE_RR_AVG_BPM;
   public static final String             BODY_PULSE_RR_AVG_BPM_ID                           = "BODY_PULSE_RR_AVG_BPM";                           //$NON-NLS-1$
   public static final TableColumnFactory BODY_PULSE_RR_INDEX;
   public static final String             BODY_PULSE_RR_INDEX_ID                             = "BODY_PULSE_RR_INDEX";                             //$NON-NLS-1$
   public static final TableColumnFactory BODY_PULSE_RR_INTERVALS;
   public static final String             BODY_PULSE_RR_INTERVALS_ID                         = "BODY_PULSE_RR_INTERVALS";                         //$NON-NLS-1$
   public static final TableColumnFactory BODY_RESTPULSE;
   public static final String             BODY_RESTPULSE_ID                                  = "BODY_RESTPULSE";                                  //$NON-NLS-1$
   public static final TableColumnFactory BODY_WEIGHT;
   public static final String             BODY_WEIGHT_ID                                     = "BODY_WEIGHT";                                     //$NON-NLS-1$

   public static final TableColumnFactory CUSTOM_TRACKS_TIME_SLICES;

   public static final TableColumnFactory DATA_DP_TOLERANCE;
   public static final String             DATA_DP_TOLERANCE_ID                               = "DATA_DP_TOLERANCE";                               //$NON-NLS-1$
   public static final TableColumnFactory DATA_FIRST_COLUMN;
   public static final String             DATA_FIRST_COLUMN_ID                               = "DATA_FIRST_COLUMN";                               //$NON-NLS-1$
   public static final TableColumnFactory DATA_HAS_GEO_DATA;
   public static final String             DATA_HAS_GEO_DATA_ID                               = "DATA_HAS_GEO_DATA";                               //$NON-NLS-1$
   public static final TableColumnFactory DATA_IMPORT_FILE_NAME;
   public static final String             DATA_IMPORT_FILE_NAME_ID                           = "DATA_IMPORT_FILE_NAME";                           //$NON-NLS-1$
   public static final TableColumnFactory DATA_IMPORT_FILE_PATH;
   public static final String             DATA_IMPORT_FILE_PATH_ID                           = "DATA_IMPORT_FILE_PATH";                           //$NON-NLS-1$
   public static final TableColumnFactory DATA_NUM_TIME_SLICES;
   public static final String             DATA_NUM_TIME_SLICES_ID                            = "DATA_NUM_TIME_SLICES";                            //$NON-NLS-1$
   public static final TableColumnFactory DATA_SERIE_START_END_INDEX;
   public static final String             DATA_SERIE_START_END_INDEX_ID                      = "DATA_SERIE_START_END_INDEX";                      //$NON-NLS-1$
   public static final TableColumnFactory DATA_SEQUENCE;
   public static final String             DATA_SEQUENCE_ID                                   = "DATA_SEQUENCE";                                   //$NON-NLS-1$
   public static final TableColumnFactory DATA_TIME_INTERVAL;
   public static final String             DATA_TIME_INTERVAL_ID                              = "DATA_TIME_INTERVAL";                              //$NON-NLS-1$
   public static final TableColumnFactory DATA_TOUR_ID;
   public static final String             DATA_TOUR_ID_ID                                    = "DATA_TOUR_ID";                                    //$NON-NLS-1$

   public static final TableColumnFactory DEVICE_BATTERY_SOC_END;
   public static final String             DEVICE_BATTERY_SOC_END_ID                          = "DEVICE_BATTERY_SOC_END_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory DEVICE_BATTERY_SOC_START;
   public static final String             DEVICE_BATTERY_SOC_START_ID                        = "DEVICE_BATTERY_SOC_START_ID";                     //$NON-NLS-1$
   public static final TableColumnFactory DEVICE_DISTANCE;
   public static final String             DEVICE_DISTANCE_ID                                 = "DEVICE_DISTANCE";                                 //$NON-NLS-1$
   public static final TableColumnFactory DEVICE_NAME;
   public static final String             DEVICE_NAME_ID                                     = "DEVICE_NAME";                                     //$NON-NLS-1$
   public static final TableColumnFactory DEVICE_PROFILE;

   public static final TableColumnFactory LOCATION_DATA_ID;
   public static final String             LOCATION_DATA_ID_ID                                = "LOCATION_DATA_ID_ID";                             //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_DATA_APPLIED_NAME;
   public static final String             LOCATION_DATA_APPLIED_NAME_ID                      = "LOCATION_DATA_APPLIED_NAME_ID";                   //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_DATA_LAST_MODIFIED;
   public static final String             LOCATION_DATA_LAST_MODIFIED_ID                     = "LOCATION_DATA_LAST_MODIFIED_ID";                  //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_BOUNDING_BOX_HEIGHT;
   public static final String             LOCATION_GEO_BOUNDING_BOX_HEIGHT_ID                = "LOCATION_GEO_BOUNDING_BOX_HEIGHT_ID";             //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_BOUNDING_BOX_WIDTH;
   public static final String             LOCATION_GEO_BOUNDING_BOX_WIDTH_ID                 = "LOCATION_GEO_BOUNDING_BOX_WIDTH_ID";              //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_IS_RESIZED_BOUNDING_BOX;
   public static final String             LOCATION_GEO_IS_RESIZED_BOUNDING_BOX_ID            = "LOCATION_GEO_IS_RESIZED_BOUNDING_BOX_ID";         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_LATITUDE;
   public static final String             LOCATION_GEO_LATITUDE_ID                           = "LOCATION_GEO_LATITUDE_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_LATITUDE_DIFF;
   public static final String             LOCATION_GEO_LATITUDE_DIFF_ID                      = "LOCATION_GEO_LATITUDE_DIFF_ID";                   //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_LONGITUDE;
   public static final String             LOCATION_GEO_LONGITUDE_ID                          = "LOCATION_GEO_LONGITUDE_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_LONGITUDE_DIFF;
   public static final String             LOCATION_GEO_LONGITUDE_DIFF_ID                     = "LOCATION_GEO_LONGITUDE_DIFF_ID";                  //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_GEO_ZOOMLEVEL;
   public static final String             LOCATION_GEO_ZOOMLEVEL_ID                          = "LOCATION_GEO_ZOOMLEVEL_ID";                       //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_TOUR_USAGE;
   public static final String             LOCATION_TOUR_USAGE_ID                             = "LOCATION_TOUR_USAGE";                             //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_TOUR_USAGE_START_LOCATIONS;
   public static final String             LOCATION_TOUR_USAGE_START_LOCATIONS_ID             = "LOCATION_TOUR_USAGE_START_LOCATIONS_ID";          //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_TOUR_USAGE_END_LOCATIONS;
   public static final String             LOCATION_TOUR_USAGE_END_LOCATIONS_ID               = "LOCATION_TOUR_USAGE_END_LOCATIONS_ID";            //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_DisplayName;
   public static final String             LOCATION_PART_DisplayName_ID                       = "LOCATION_PART_DisplayName_ID";                    //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Name;
   public static final String             LOCATION_PART_Name_ID                              = "LOCATION_PART_Name";                              //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Continent;
   public static final String             LOCATION_PART_Continent_ID                         = "LOCATION_PART_Continent_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Country;
   public static final String             LOCATION_PART_Country_ID                           = "LOCATION_PART_Country_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_CountryCode;
   public static final String             LOCATION_PART_CountryCode_ID                       = "LOCATION_PART_CountryCode_ID";                    //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Region;
   public static final String             LOCATION_PART_Region_ID                            = "LOCATION_PART_Region_ID";                         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_State;
   public static final String             LOCATION_PART_State_ID                             = "LOCATION_PART_State_ID";                          //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_StateDistrict;
   public static final String             LOCATION_PART_StateDistrict_ID                     = "LOCATION_PART_StateDistrict_ID";                  //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_County;
   public static final String             LOCATION_PART_County_ID                            = "LOCATION_PART_County_ID";                         //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Municipality;
   public static final String             LOCATION_PART_Municipality_ID                      = "LOCATION_PART_Municipality_ID";                   //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_City;
   public static final String             LOCATION_PART_City_ID                              = "LOCATION_PART_City_ID";                           //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Town;
   public static final String             LOCATION_PART_Town_ID                              = "LOCATION_PART_Town_ID";                           //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Village;
   public static final String             LOCATION_PART_Village_ID                           = "LOCATION_PART_Village_ID";                        //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_CityDistrict;
   public static final String             LOCATION_PART_CityDistrict_ID                      = "LOCATION_PART_CityDistrict_ID";                   //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_District;
   public static final String             LOCATION_PART_District_ID                          = "LOCATION_PART_District_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Borough;
   public static final String             LOCATION_PART_Borough_ID                           = "LOCATION_PART_Borough_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Suburb;
   public static final String             LOCATION_PART_Suburb_ID                            = "LOCATION_PART_Suburb_ID";                         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Subdivision;
   public static final String             LOCATION_PART_Subdivision_ID                       = "LOCATION_PART_Subdivision_ID";                    //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Hamlet;
   public static final String             LOCATION_PART_Hamlet_ID                            = "LOCATION_PART_Hamlet_ID";                         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Croft;
   public static final String             LOCATION_PART_Croft_ID                             = "LOCATION_PART_Croft_ID";                          //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_IsolatedDwelling;
   public static final String             LOCATION_PART_IsolatedDwelling_ID                  = "LOCATION_PART_IsolatedDwelling_ID";               //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Neighbourhood;
   public static final String             LOCATION_PART_Neighbourhood_ID                     = "LOCATION_PART_Neighbourhood_ID";                  //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Allotments;
   public static final String             LOCATION_PART_Allotments_ID                        = "LOCATION_PART_Allotments_ID";                     //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Quarter;
   public static final String             LOCATION_PART_Quarter_ID                           = "LOCATION_PART_Quarter_ID";                        //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_CityBlock;
   public static final String             LOCATION_PART_CityBlock_ID                         = "LOCATION_PART_CityBlock_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Residential;
   public static final String             LOCATION_PART_Residential_ID                       = "LOCATION_PART_Residential_ID";                    //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Farm;
   public static final String             LOCATION_PART_Farm_ID                              = "LOCATION_PART_Farm_ID";                           //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Farmyard;
   public static final String             LOCATION_PART_Farmyard_ID                          = "LOCATION_PART_Farmyard_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Industrial;
   public static final String             LOCATION_PART_Industrial_ID                        = "LOCATION_PART_Industrial_ID";                     //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Commercial;
   public static final String             LOCATION_PART_Commercial_ID                        = "LOCATION_PART_Commercial_ID";                     //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Retail;
   public static final String             LOCATION_PART_Retail_ID                            = "LOCATION_PART_Retail_ID";                         //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Road;
   public static final String             LOCATION_PART_Road_ID                              = "LOCATION_PART_Road_ID";                           //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_HouseName;
   public static final String             LOCATION_PART_HouseName_ID                         = "LOCATION_PART_HouseName_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_HouseNumber;
   public static final String             LOCATION_PART_HouseNumber_ID                       = "LOCATION_PART_HouseNumber_ID";                    //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Aerialway;
   public static final String             LOCATION_PART_Aerialway_ID                         = "LOCATION_PART_Aerialway_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Aeroway;
   public static final String             LOCATION_PART_Aeroway_ID                           = "LOCATION_PART_Aeroway_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Amenity;
   public static final String             LOCATION_PART_Amenity_ID                           = "LOCATION_PART_Amenity_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Boundary;
   public static final String             LOCATION_PART_Boundary_ID                          = "LOCATION_PART_Boundary_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Bridge;
   public static final String             LOCATION_PART_Bridge_ID                            = "LOCATION_PART_Bridge_ID";                         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Club;
   public static final String             LOCATION_PART_Club_ID                              = "LOCATION_PART_Club_ID";                           //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Craft;
   public static final String             LOCATION_PART_Craft_ID                             = "LOCATION_PART_Craft_ID";                          //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Emergency;
   public static final String             LOCATION_PART_Emergency_ID                         = "LOCATION_PART_Emergency_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Historic;
   public static final String             LOCATION_PART_Historic_ID                          = "LOCATION_PART_Historic_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Landuse;
   public static final String             LOCATION_PART_Landuse_ID                           = "LOCATION_PART_Landuse_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Leisure;
   public static final String             LOCATION_PART_Leisure_ID                           = "LOCATION_PART_Leisure_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_ManMade;
   public static final String             LOCATION_PART_ManMade_ID                           = "LOCATION_PART_ManMade_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Military;
   public static final String             LOCATION_PART_Military_ID                          = "LOCATION_PART_Military_ID";                       //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_MountainPass;
   public static final String             LOCATION_PART_MountainPass_ID                      = "LOCATION_PART_MountainPass_ID";                   //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Natural;
   public static final String             LOCATION_PART_Natural_ID                           = "LOCATION_PART_Natural_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Office;
   public static final String             LOCATION_PART_Office_ID                            = "LOCATION_PART_Office_ID";                         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Place;
   public static final String             LOCATION_PART_Place_ID                             = "LOCATION_PART_Place_ID";                          //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Railway;
   public static final String             LOCATION_PART_Railway_ID                           = "LOCATION_PART_Railway_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Shop;
   public static final String             LOCATION_PART_Shop_ID                              = "LOCATION_PART_Shop_ID";                           //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Tourism;
   public static final String             LOCATION_PART_Tourism_ID                           = "LOCATION_PART_Tourism_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Tunnel;
   public static final String             LOCATION_PART_Tunnel_ID                            = "LOCATION_PART_Tunnel_ID";                         //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_Waterway;
   public static final String             LOCATION_PART_Waterway_ID                          = "LOCATION_PART_Waterway_ID";                       //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_Postcode;
   public static final String             LOCATION_PART_Postcode_ID                          = "LOCATION_PART_Postcode_ID";                       //$NON-NLS-1$

   public static final TableColumnFactory LOCATION_PART_SettlementSmall;
   public static final String             LOCATION_PART_SettlementSmall_ID                   = "LOCATION_PART_SettlementSmall_ID";                //$NON-NLS-1$
   public static final TableColumnFactory LOCATION_PART_SettlementLarge;
   public static final String             LOCATION_PART_SettlementLarge_ID                   = "LOCATION_PART_SettlementLarge_ID";                //$NON-NLS-1$

   public static final TableColumnFactory MARKER_ALTITUDE_ELEVATION_GAIN_DELTA;
   public static final TableColumnFactory MARKER_ALTITUDE_ELEVATION_LOSS_DELTA;
   public static final TableColumnFactory MARKER_MAP_VISIBLE;
   public static final TableColumnFactory MARKER_SERIE_INDEX;
   public static final TableColumnFactory MARKER_TIME_DELTA;
   public static final TableColumnFactory MARKER_URL;
   public static final String             MARKER_URL_ID                                      = "MARKER_URL_ID";                                   //$NON-NLS-1$
   public static final TableColumnFactory MARKER_TYPE;
   public static final String             MARKER_TYPE_ID                                     = "MARKER_TYPE_ID";                                  //$NON-NLS-1$

   public static final TableColumnFactory MOTION_ALTIMETER;
   public static final String             MOTION_ALTIMETER_ID                                = "MOTION_ALTIMETER";                                //$NON-NLS-1$
   public static final TableColumnFactory MOTION_AVG_PACE;
   public static final String             MOTION_AVG_PACE_ID                                 = "MOTION_AVG_PACE";                                 //$NON-NLS-1$
   public static final TableColumnFactory MOTION_AVG_PACE_DIFFERENCE;
   public static final TableColumnFactory MOTION_AVG_SPEED;
   public static final String             MOTION_AVG_SPEED_ID                                = "MOTION_AVG_SPEED";                                //$NON-NLS-1$
   public static final TableColumnFactory MOTION_DISTANCE;
   public static final String             MOTION_DISTANCE_ID                                 = "MOTION_DISTANCE";                                 //$NON-NLS-1$
   public static final TableColumnFactory MOTION_DISTANCE_DELTA;
   public static final String             MOTION_DISTANCE_DELTA_ID                           = "MOTION_DISTANCE_DELTA_ID";                        //$NON-NLS-1$
   public static final TableColumnFactory MOTION_DISTANCE_DIFF;
   public static final TableColumnFactory MOTION_DISTANCE_TOTAL;
   public static final TableColumnFactory MOTION_LATITUDE;
   public static final TableColumnFactory MOTION_LONGITUDE;
   public static final TableColumnFactory MOTION_MAX_SPEED;
   public static final String             MOTION_MAX_SPEED_ID                                = "MOTION_MAX_SPEED";                                //$NON-NLS-1$
   public static final TableColumnFactory MOTION_PACE;
   public static final TableColumnFactory MOTION_SPEED;
   public static final TableColumnFactory MOTION_SPEED_DIFF;

   public static final TableColumnFactory NUTRITION_NUM_PRODUCTS;
   public static final String             NUTRITION_NUM_PRODUCTS_ID                          = "NUTRITION_NUM_PRODUCTS";                          //$NON-NLS-1$

   public static final TableColumnFactory PHOTO_FILE_PATH;
   public static final TableColumnFactory PHOTO_NUMBER_OF_GPS_PHOTOS;
   public static final TableColumnFactory PHOTO_NUMBER_OF_NO_GPS_PHOTOS;
   public static final TableColumnFactory PHOTO_NUMBER_OF_PHOTOS;
   public static final TableColumnFactory PHOTO_TIME_ADJUSTMENT;
   public static final TableColumnFactory PHOTO_TIME_ADJUSTMENT_ALL;
   public static final TableColumnFactory PHOTO_TOUR_CAMERA;

   public static final TableColumnFactory POWER_AVG;
   public static final String             POWER_AVG_ID                                       = "POWER_AVG";                                       //$NON-NLS-1$
   public static final TableColumnFactory POWER_MAX;
   public static final String             POWER_MAX_ID                                       = "POWER_MAX";                                       //$NON-NLS-1$
   public static final TableColumnFactory POWER_NORMALIZED;
   public static final String             POWER_NORMALIZED_ID                                = "POWER_NORMALIZED";                                //$NON-NLS-1$
   public static final TableColumnFactory POWER_TIME_SLICE;
   public static final TableColumnFactory POWER_TOTAL_WORK;
   public static final String             POWER_TOTAL_WORK_ID                                = "POWER_TOTAL_WORK";                                //$NON-NLS-1$

   public static final TableColumnFactory POWERTRAIN_AVG_CADENCE;
   public static final String             POWERTRAIN_AVG_CADENCE_ID                          = "POWERTRAIN_AVG_CADENCE";                          //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_AVG_LEFT_PEDAL_SMOOTHNESS;
   public static final String             POWERTRAIN_AVG_LEFT_PEDAL_SMOOTHNESS_ID            = "POWERTRAIN_AVG_LEFT_PEDAL_SMOOTHNESS";            //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_AVG_RIGHT_PEDAL_SMOOTHNESS;
   public static final String             POWERTRAIN_AVG_RIGHT_PEDAL_SMOOTHNESS_ID           = "POWERTRAIN_AVG_RIGHT_PEDAL_SMOOTHNESS";           //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_AVG_LEFT_TORQUE_EFFECTIVENESS;
   public static final String             POWERTRAIN_AVG_LEFT_TORQUE_EFFECTIVENESS_ID        = "POWERTRAIN_AVG_LEFT_TORQUE_EFFECTIVENESS";        //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_AVG_RIGHT_TORQUE_EFFECTIVENESS;
   public static final String             POWERTRAIN_AVG_RIGHT_TORQUE_EFFECTIVENESS_ID       = "POWERTRAIN_AVG_RIGHT_TORQUE_EFFECTIVENESS";       //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_CADENCE_TIME_SLICE;
   public static final TableColumnFactory POWERTRAIN_CADENCE_MULTIPLIER;
   public static final String             POWERTRAIN_CADENCE_MULTIPLIER_ID                   = "POWERTRAIN_CADENCE_MULTIPLIER";                   //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_GEAR_RATIO_TIME_SLICE;
   public static final TableColumnFactory POWERTRAIN_GEAR_TEETH;
   public static final TableColumnFactory POWERTRAIN_GEAR_FRONT_SHIFT_COUNT;
   public static final String             POWERTRAIN_GEAR_FRONT_SHIFT_COUNT_ID               = "POWERTRAIN_GEAR_FRONT_SHIFT_COUNT";               //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_GEAR_REAR_SHIFT_COUNT;
   public static final String             POWERTRAIN_GEAR_REAR_SHIFT_COUNT_ID                = "POWERTRAIN_GEAR_REAR_SHIFT_COUNT";                //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_PEDAL_LEFT_RIGHT_BALANCE;
   public static final String             POWERTRAIN_PEDAL_LEFT_RIGHT_BALANCE_ID             = "POWERTRAIN_PEDAL_LEFT_RIGHT_BALANCE";             //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_SLOW_VS_FAST_CADENCE_ZONES_DELIMITER;
   public static final String             POWERTRAIN_SLOW_VS_FAST_CADENCE_ZONES_DELIMITER_ID = "POWERTRAIN_SLOW_VS_FAST_CADENCE_ZONES_DELIMITER"; //$NON-NLS-1$
   public static final TableColumnFactory POWERTRAIN_SLOW_VS_FAST_CADENCE_PERCENTAGES;
   public static final String             POWERTRAIN_SLOW_VS_FAST_CADENCE_PERCENTAGES_ID     = "POWERTRAIN_SLOW_VS_FAST_CADENCE_PERCENTAGES";     //$NON-NLS-1$

   public static final TableColumnFactory RUN_DYN_STANCE_TIME_AVG;
   public static final String             RUN_DYN_STANCE_TIME_AVG_ID                         = "RUN_DYN_STANCE_TIME_AVG";                         //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_STANCE_TIME_MIN;
   public static final String             RUN_DYN_STANCE_TIME_MIN_ID                         = "RUN_DYN_STANCE_TIME_MIN";                         //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_STANCE_TIME_MAX;
   public static final String             RUN_DYN_STANCE_TIME_MAX_ID                         = "RUN_DYN_STANCE_TIME_MAX";                         //$NON-NLS-1$

   public static final TableColumnFactory RUN_DYN_STANCE_TIME_BALANCE_AVG;
   public static final String             RUN_DYN_STANCE_TIME_BALANCE_AVG_ID                 = "RUN_DYN_STANCE_TIME_BALANCE_AVG";                 //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_STANCE_TIME_BALANCE_MIN;
   public static final String             RUN_DYN_STANCE_TIME_BALANCE_MIN_ID                 = "RUN_DYN_STANCE_TIME_BALANCE_MIN";                 //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_STANCE_TIME_BALANCE_MAX;
   public static final String             RUN_DYN_STANCE_TIME_BALANCE_MAX_ID                 = "RUN_DYN_STANCE_TIME_BALANCE_MAX";                 //$NON-NLS-1$

   public static final TableColumnFactory RUN_DYN_STEP_LENGTH_AVG;
   public static final String             RUN_DYN_STEP_LENGTH_AVG_ID                         = "RUN_DYN_STEP_LENGTH_AVG";                         //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_STEP_LENGTH_MIN;
   public static final String             RUN_DYN_STEP_LENGTH_MIN_ID                         = "RUN_DYN_STEP_LENGTH_MIN";                         //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_STEP_LENGTH_MAX;
   public static final String             RUN_DYN_STEP_LENGTH_MAX_ID                         = "RUN_DYN_STEP_LENGTH_MAX";                         //$NON-NLS-1$

   public static final TableColumnFactory RUN_DYN_VERTICAL_OSCILLATION_AVG;
   public static final String             RUN_DYN_VERTICAL_OSCILLATION_AVG_ID                = "RUN_DYN_VERTICAL_OSCILLATION_AVG";                //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_VERTICAL_OSCILLATION_MIN;
   public static final String             RUN_DYN_VERTICAL_OSCILLATION_MIN_ID                = "RUN_DYN_VERTICAL_OSCILLATION_MIN";                //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_VERTICAL_OSCILLATION_MAX;
   public static final String             RUN_DYN_VERTICAL_OSCILLATION_MAX_ID                = "RUN_DYN_VERTICAL_OSCILLATION_MAX";                //$NON-NLS-1$

   public static final TableColumnFactory RUN_DYN_VERTICAL_RATIO_AVG;
   public static final String             RUN_DYN_VERTICAL_RATIO_AVG_ID                      = "RUN_DYN_VERTICAL_RATIO_AVG";                      //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_VERTICAL_RATIO_MIN;
   public static final String             RUN_DYN_VERTICAL_RATIO_MIN_ID                      = "RUN_DYN_VERTICAL_RATIO_MIN";                      //$NON-NLS-1$
   public static final TableColumnFactory RUN_DYN_VERTICAL_RATIO_MAX;
   public static final String             RUN_DYN_VERTICAL_RATIO_MAX_ID                      = "RUN_DYN_VERTICAL_RATIO_MAX";                      //$NON-NLS-1$

   public static final TableColumnFactory SENSOR_NAME;
   public static final String             SENSOR_NAME_ID                                     = "SENSOR_NAME";                                     //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_NAME_KEY;
   public static final String             SENSOR_NAME_KEY_ID                                 = "SENSOR_NAME_KEY";                                 //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_DESCRIPTION;
   public static final String             SENSOR_DESCRIPTION_ID                              = "SENSOR_DESCRIPTION";                              //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_MANUFACTURER_NAME;
   public static final String             SENSOR_MANUFACTURER_NAME_ID                        = "SENSOR_MANUFACTURER_NAME";                        //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_MANUFACTURER_NUMBER;
   public static final String             SENSOR_MANUFACTURER_NUMBER_ID                      = "SENSOR_MANUFACTURER_NUMBER";                      //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_PRODUCT_NAME;
   public static final String             SENSOR_PRODUCT_NAME_ID                             = "SENSOR_PRODUCT_NAME";                             //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_PRODUCT_NUMBER;
   public static final String             SENSOR_PRODUCT_NUMBER_ID                           = "SENSOR_PRODUCT_NUMBER";                           //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_SERIAL_NUMBER;
   public static final String             SENSOR_SERIAL_NUMBER_ID                            = "SENSOR_SERIAL_NUMBER";                            //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_STATE_BATTERY_LEVEL;
   public static final String             SENSOR_STATE_BATTERY_LEVEL_ID                      = "SENSOR_STATE_BATTERY_LEVEL";                      //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_STATE_BATTERY_STATUS;
   public static final String             SENSOR_STATE_BATTERY_STATUS_ID                     = "SENSOR_STATE_BATTERY_STATUS";                     //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_STATE_BATTERY_VOLTAGE;
   public static final String             SENSOR_STATE_BATTERY_VOLTAGE_ID                    = "SENSOR_STATE_BATTERY_VOLTAGE";                    //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_TIME_FIRST_USED;
   public static final String             SENSOR_TIME_FIRST_USED_ID                          = "SENSOR_TIME_FIRST_USED";                          //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_TIME_LAST_USED;
   public static final String             SENSOR_TIME_LAST_USED_ID                           = "SENSOR_TIME_LAST_USED";                           //$NON-NLS-1$
   public static final TableColumnFactory SENSOR_TYPE;
   public static final String             SENSOR_TYPE_ID                                     = "SENSOR_TYPE";                                     //$NON-NLS-1$

   public static final TableColumnFactory STATE_DB_STATUS;
   public static final TableColumnFactory STATE_IMPORT_STATE;

   public static final TableColumnFactory SURFING_MIN_DISTANCE;
   public static final String             SURFING_MIN_DISTANCE_ID                            = "SURFING_MIN_DISTANCE";                            //$NON-NLS-1$
   public static final TableColumnFactory SURFING_MIN_SPEED_START_STOP;
   public static final String             SURFING_MIN_SPEED_START_STOP_ID                    = "SURFING_MIN_SPEED_START_STOP";                    //$NON-NLS-1$
   public static final TableColumnFactory SURFING_MIN_SPEED_SURFING;
   public static final String             SURFING_MIN_SPEED_SURFING_ID                       = "SURFING_MIN_SPEED_SURFING";                       //$NON-NLS-1$
   public static final TableColumnFactory SURFING_MIN_TIME_DURATION;
   public static final String             SURFING_MIN_TIME_DURATION_ID                       = "SURFING_MIN_TIME_DURATION";                       //$NON-NLS-1$
   public static final TableColumnFactory SURFING_NUMBER_OF_EVENTS;
   public static final String             SURFING_NUMBER_OF_EVENTS_ID                        = "SURFING_NUMBER_OF_EVENTS";                        //$NON-NLS-1$

   public static final TableColumnFactory SWIM_LENGTH_TYPE;
   public static final String             SWIM_LENGTH_TYPE_ID                                = "SWIM_LENGTH_TYPE";                                //$NON-NLS-1$
   public static final TableColumnFactory SWIM_STROKE_RATE;
   public static final TableColumnFactory SWIM_STROKES_PER_LENGTH;
   public static final TableColumnFactory SWIM_STROKE_STYLE;

   public static final TableColumnFactory SWIM__TIME_TOUR_TIME_DIFF;
   public static final TableColumnFactory SWIM__TIME_TOUR_TIME_HH_MM_SS;
   public static final TableColumnFactory SWIM__TIME_TOUR_TIME;
   public static final TableColumnFactory SWIM__TIME_TOUR_TIME_OF_DAY_HH_MM_SS;

   public static final TableColumnFactory TIME__DEVICE_ELAPSED_TIME;
   public static final String             TIME__DEVICE_ELAPSED_TIME_ID                       = "TIME__DEVICE_ELAPSED_TIME";                       //$NON-NLS-1$
   public static final TableColumnFactory TIME__DEVICE_ELAPSED_TIME_TOTAL;
   public static final TableColumnFactory TIME__DEVICE_RECORDED_TIME;
   public static final String             TIME__DEVICE_RECORDED_TIME_ID                      = "TIME__DEVICE_RECORDED_TIME";                      //$NON-NLS-1$
   public static final TableColumnFactory TIME__DEVICE_PAUSED_TIME;
   public static final String             TIME__DEVICE_PAUSED_TIME_ID                        = "TIME__DEVICE_PAUSED_TIME";                        //$NON-NLS-1$
   public static final TableColumnFactory TIME__COMPUTED_MOVING_TIME;
   public static final String             TIME__COMPUTED_MOVING_TIME_ID                      = "TIME__COMPUTED_MOVING_TIME";                      //$NON-NLS-1$
   public static final TableColumnFactory TIME__COMPUTED_BREAK_TIME;
   public static final String             TIME__COMPUTED_BREAK_TIME_ID                       = "TIME__COMPUTED_BREAK_TIME";                       //$NON-NLS-1$
   public static final TableColumnFactory TIME__COMPUTED_BREAK_TIME_RELATIVE;
   public static final String             TIME__COMPUTED_BREAK_TIME_RELATIVE_ID              = "TIME__COMPUTED_BREAK_TIME_RELATIVE";              //$NON-NLS-1$

   public static final TableColumnFactory TIME_IS_BREAK_TIME;
   public static final TableColumnFactory TIME_IS_PAUSED_TIME;
   public static final TableColumnFactory TIME_DATE;
   public static final String             TIME_DATE_ID                                       = "TIME_DATE";                                       //$NON-NLS-1$
   public static final TableColumnFactory TIME_TIME_ZONE;
   public static final String             TIME_TIME_ZONE_ID                                  = "TIME_TIME_ZONE";                                  //$NON-NLS-1$
   public static final TableColumnFactory TIME_TIME_ZONE_DIFFERENCE;
   public static final String             TIME_TIME_ZONE_DIFFERENCE_ID                       = "TIME_TIME_ZONE_DIFFERENCE";                       //$NON-NLS-1$
   public static final TableColumnFactory TIME_TOUR_TIME_DIFF;
   public static final TableColumnFactory TIME_TOUR_TIME_HH_MM_SS;
   public static final TableColumnFactory TIME_TOUR_TIME;
   public static final TableColumnFactory TIME_TOUR_TIME_OF_DAY_HH_MM_SS;
   public static final TableColumnFactory TIME_TOUR_DATE;
   public static final TableColumnFactory TIME_TOUR_DURATION_TIME;
   public static final TableColumnFactory TIME_TOUR_START_TIME;
   public static final String             TIME_TOUR_START_TIME_ID                            = "TIME_TOUR_START_TIME";                            //$NON-NLS-1$
   public static final TableColumnFactory TIME_TOUR_END_TIME;
   public static final String             TIME_TOUR_END_TIME_ID                              = "TIME_TOUR_END_TIME";                              //$NON-NLS-1$
   public static final TableColumnFactory TIME_TOUR_START_DATE;
   public static final TableColumnFactory TIME_TOUR_END_DATE;
   public static final TableColumnFactory TIME_WEEK_DAY;
   public static final String             TIME_WEEK_DAY_ID                                   = "TIME_WEEK_DAY";                                   //$NON-NLS-1$
   public static final TableColumnFactory TIME_WEEK_NO;
   public static final String             TIME_WEEK_NO_ID                                    = "TIME_WEEK_NO";                                    //$NON-NLS-1$
   public static final TableColumnFactory TIME_WEEKYEAR;
   public static final String             TIME_WEEKYEAR_ID                                   = "TIME_WEEKYEAR";                                   //$NON-NLS-1$

   public static final TableColumnFactory TOUR_DESCRIPTION;
   public static final String             TOUR_DESCRIPTION_ID                                = "TOUR_DESCRIPTION";                                //$NON-NLS-1$
   public static final TableColumnFactory TOUR_LOCATION_START;
   public static final String             TOUR_LOCATION_START_ID                             = "TOUR_LOCATION_START";                             //$NON-NLS-1$
   public static final TableColumnFactory TOUR_LOCATION_END;
   public static final String             TOUR_LOCATION_END_ID                               = "TOUR_LOCATION_END";                               //$NON-NLS-1$
   public static final TableColumnFactory TOUR_LOCATION_ID_START;
   public static final String             TOUR_LOCATION_ID_START_ID                          = "TOUR_LOCATION_ID_START";                          //$NON-NLS-1$
   public static final TableColumnFactory TOUR_LOCATION_ID_END;
   public static final String             TOUR_LOCATION_ID_END_ID                            = "TOUR_LOCATION_ID_END";                            //$NON-NLS-1$
   public static final TableColumnFactory TOUR_MARKER;
   public static final TableColumnFactory TOUR_NUM_MARKERS;
   public static final String             TOUR_NUM_MARKERS_ID                                = "TOUR_NUM_MARKERS";                                //$NON-NLS-1$
   public static final TableColumnFactory TOUR_NUM_PHOTOS;
   public static final String             TOUR_NUM_PHOTOS_ID                                 = "TOUR_NUM_PHOTOS";                                 //$NON-NLS-1$
   public static final TableColumnFactory TOUR_POSITIONED_PHOTO;
   public static final String             TOUR_POSITIONED_PHOTO_ID                           = "TOUR_POSITIONED_PHOTO";                           //$NON-NLS-1$
   public static final TableColumnFactory TOUR_TAGS;
   public static final String             TOUR_TAGS_ID                                       = "TOUR_TAGS";                                       //$NON-NLS-1$
   public static final TableColumnFactory TOUR_TITLE;
   public static final String             TOUR_TITLE_ID                                      = "TOUR_TITLE";                                      //$NON-NLS-1$
   public static final TableColumnFactory TOUR_TYPE;
   public static final String             TOUR_TYPE_ID                                       = "TOUR_TYPE";                                       //$NON-NLS-1$
   public static final TableColumnFactory TOUR_TYPE_TEXT;
   public static final String             TOUR_TYPE_TEXT_ID                                  = "TOUR_TYPE_TEXT";                                  //$NON-NLS-1$

   public static final TableColumnFactory TRAINING_EFFECT_AEROB;
   public static final String             TRAINING_EFFECT_AEROB_ID                           = "TRAINING_TRAINING_EFFECT_AEROB";                  //$NON-NLS-1$
   public static final TableColumnFactory TRAINING_EFFECT_ANAEROB;
   public static final String             TRAINING_EFFECT_ANAEROB_ID                         = "TRAINING_EFFECT_ANAEROB";                         //$NON-NLS-1$
   public static final TableColumnFactory TRAINING_FTP;
   public static final String             TRAINING_FTP_ID                                    = "TRAINING_FTP";                                    //$NON-NLS-1$
   public static final TableColumnFactory TRAINING_INTENSITY_FACTOR;
   public static final String             TRAINING_INTENSITY_FACTOR_ID                       = "TRAINING_INTENSITY_FACTOR";                       //$NON-NLS-1$
   public static final TableColumnFactory TRAINING_PERFORMANCE_LEVEL;
   public static final String             TRAINING_PERFORMANCE_LEVEL_ID                      = "TRAINING_PERFORMANCE_LEVEL";                      //$NON-NLS-1$
   public static final TableColumnFactory TRAINING_POWER_TO_WEIGHT;
   public static final String             TRAINING_POWER_TO_WEIGHT_ID                        = "TRAINING_POWER_TO_WEIGHT";                        //$NON-NLS-1$
   public static final TableColumnFactory TRAINING_STRESS_SCORE;
   public static final String             TRAINING_STRESS_SCORE_ID                           = "TRAINING_STRESS_SCORE";                           //$NON-NLS-1$

   public static final TableColumnFactory WAYPOINT_ALTITUDE;
   public static final TableColumnFactory WAYPOINT_CATEGORY;
   public static final TableColumnFactory WAYPOINT_COMMENT;
   public static final TableColumnFactory WAYPOINT_DATE;
   public static final TableColumnFactory WAYPOINT_DESCRIPTION;
   public static final TableColumnFactory WAYPOINT_ID;
   public static final TableColumnFactory WAYPOINT_NAME;
   public static final TableColumnFactory WAYPOINT_SYMBOL;
   public static final TableColumnFactory WAYPOINT_TIME;

   public static final TableColumnFactory WEATHER_AIR_QUALITY;
   public static final String             WEATHER_AIR_QUALITY_ID                             = "WEATHER_AIR_QUALITY";                             //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_CLOUDS;
   public static final String             WEATHER_CLOUDS_ID                                  = "WEATHER_CLOUDS";                                  //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_AVG;
   public static final TableColumnFactory WEATHER_TEMPERATURE_AVG_COMBINED;
   public static final String             WEATHER_TEMPERATURE_AVG_COMBINED_ID                = "WEATHER_TEMPERATURE_AVG_COMBINED_ID";             //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_AVG_DEVICE;
   public static final String             WEATHER_TEMPERATURE_AVG_DEVICE_ID                  = "WEATHER_TEMPERATURE_AVG_DEVICE";                  //$NON-NLS-1$
   public static final String             WEATHER_TEMPERATURE_AVG_ID                         = "WEATHER_TEMPERATURE_AVG_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_MIN;
   public static final String             WEATHER_TEMPERATURE_MIN_ID                         = "WEATHER_TEMPERATURE_MIN_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_MIN_COMBINED;
   public static final String             WEATHER_TEMPERATURE_MIN_COMBINED_ID                = "WEATHER_TEMPERATURE_MIN_COMBINED_ID";             //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_MIN_DEVICE;
   public static final String             WEATHER_TEMPERATURE_MIN_DEVICE_ID                  = "WEATHER_TEMPERATURE_MIN_DEVICE";                  //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_MAX;
   public static final String             WEATHER_TEMPERATURE_MAX_ID                         = "WEATHER_TEMPERATURE_MAX_ID";                      //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_MAX_COMBINED;
   public static final String             WEATHER_TEMPERATURE_MAX_COMBINED_ID                = "WEATHER_TEMPERATURE_MAX_COMBINED_ID";             //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_MAX_DEVICE;
   public static final String             WEATHER_TEMPERATURE_MAX_DEVICE_ID                  = "WEATHER_TEMPERATURE_MAX_DEVICE";                  //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_TEMPERATURE_TIME_SLICE;
   public static final TableColumnFactory WEATHER_WIND_DIR;
   public static final String             WEATHER_WIND_DIRECTION_ID                          = "WEATHER_WIND_DIR";                                //$NON-NLS-1$
   public static final TableColumnFactory WEATHER_WIND_SPEED;
   public static final String             WEATHER_WIND_SPEED_ID                              = "WEATHER_WIND_SPEED";                              //$NON-NLS-1$

// SET_FORMATTING_OFF

   static {

      /*
       * Elevation
       */

      ALTITUDE_ALTITUDE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_ALTITUDE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_ELEVATION);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_ELEVATION);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      ALTITUDE_AVG_CHANGE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_AVG_CHANGE_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_AVERAGE + UI.SPACE + UI.UNIT_LABEL_ELEVATION + "/" + UI.UNIT_LABEL_DISTANCE; //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_AvgChange_Label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_AvgChange_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      ALTITUDE_DIFF_SEGMENT_BORDER = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE
                  + UI.UNIT_LABEL_ELEVATION
                  + UI.SYMBOL_DOUBLE_VERTICAL;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_DIFF_SEGMENT_BORDER", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_difference_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_difference_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_0,
                  columnManager);

            return colDef;
         }
      };

      ALTITUDE_DIFF_SEGMENT_COMPUTED = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE
                  + UI.UNIT_LABEL_ELEVATION
                  + UI.SYMBOL_DOUBLE_HORIZONTAL;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_DIFF_SEGMENT_COMPUTED", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_difference_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_computed_difference_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_0,
                  columnManager);

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_DOWN = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_ELEVATION_DOWN", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.UNIT_LABEL_ELEVATION
                  + Messages.ColumnFactory_hour
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_DOWN;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_down_h_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_down_h_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_UP = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_ELEVATION_UP", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.UNIT_LABEL_ELEVATION
                  + Messages.ColumnFactory_hour
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_up_h_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_up_h_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_SEGMENT_DOWN = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_ELEVATION_SEGMENT_DOWN", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_DOWN;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Segment_Descent_Label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Segment_Descent_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_SEGMENT_UP = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_ELEVATION_SEGMENT_UP", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_UP;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Segment_Ascent_Label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Segment_Ascent_Tooltip);
            colDef.setColumnUnit(unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_ELEVATION_DIFF_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_UP_DOWN_II;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_Diff_Tooltip);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_Diff_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_GAIN = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_ELEVATION_GAIN_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_UP;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_Gain_Tooltip);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_Gain_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_GAIN_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_ELEVATION_GAIN_DIFF_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_UP;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_GainDiff_Tooltip);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_GainDiff_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_LOSS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_ELEVATION_LOSS_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_DOWN;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_Loss_Tooltip);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_Loss_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      ALTITUDE_ELEVATION_LOSS_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_ELEVATION_LOSS_DIFF_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_DOWN;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_LossDiff_Tooltip);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_LossDiff_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      ALTITUDE_GRADIENT = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_GRADIENT_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_gradient_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_gradient);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_gradient_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_gradient);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      ALTITUDE_GRADIENT_AVG = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_GRADIENT_AVG", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_gradient_label);
            colDef.setColumnHeaderText(         UI.SYMBOL_AVERAGE_WITH_SPACE + Messages.ColumnFactory_gradient);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_gradient_tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_AVERAGE_WITH_SPACE + Messages.ColumnFactory_avg_gradient);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      ALTITUDE_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_MAX_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_MAX + UI.UNIT_LABEL_ELEVATION;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_max_altitude_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_max_altitude_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      ALTITUDE_SUMMARIZED_BORDER_DOWN = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_SUMMARIZED_BORDER_DOWN_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE
                  + UI.UNIT_LABEL_ELEVATION
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_DOWN;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_down_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_down_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      ALTITUDE_SUMMARIZED_BORDER_UP = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, ALTITUDE_SUMMARIZED_BORDER_UP_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE
                  + UI.UNIT_LABEL_ELEVATION
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_up_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_up_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      ALTITUDE_SUMMARIZED_COMPUTED_DOWN = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE
                  + UI.UNIT_LABEL_ELEVATION
                  + UI.SYMBOL_DOUBLE_HORIZONTAL
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_DOWN;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_SUMMARIZED_COMPUTED_DOWN", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_down_computed_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_down_computed_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      ALTITUDE_SUMMARIZED_COMPUTED_UP = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE
                  + UI.UNIT_LABEL_ELEVATION
                  + UI.SYMBOL_DOUBLE_HORIZONTAL
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "ALTITUDE_SUMMARIZED_COMPUTED_UP", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_altitude_up_computed_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_altitude_up_computed_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      /*
       * Body
       */

      BODY_AVG_PULSE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_AVG_PULSE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_pulse_label);
            colDef.setColumnHeaderText(         UI.SYMBOL_AVERAGE_WITH_SPACE + Messages.ColumnFactory_pulse);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_pulse_tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_AVERAGE_WITH_SPACE + Messages.ColumnFactory_pulse);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      BODY_AVG_PULSE_DIFFERENCE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "BODY_AVG_PULSE_DIFFERENCE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_pulse_difference_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_pulse_difference);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_pulse_difference_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_pulse_difference);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      BODY_CALORIES = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_CALORIES_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_calories_label);
            colDef.setColumnHeaderText(         Messages.Value_Unit_KCalories);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_calories_tooltip);
            colDef.setColumnUnit(               Messages.Value_Unit_KCalories);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      BODY_PERSON = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_PERSON_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourPerson);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourPerson);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourPerson_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      BODY_PULSE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "BODY_PULSE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_pulse_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_pulse);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_pulse_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_pulse);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      BODY_PULSE_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_PULSE_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_max_pulse_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_max_pulse);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_max_pulse_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_max_pulse);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      BODY_PULSE_RR_AVG_BPM = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_PULSE_RR_AVG_BPM_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_Pulse_RR_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Pulse_RR);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Pulse_RR_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_pulse);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      BODY_PULSE_RR_INDEX = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_PULSE_RR_INDEX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_Pulse_RR_Index);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Pulse_RR_Index);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Pulse_RR_Index_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_MS);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      BODY_PULSE_RR_INTERVALS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_PULSE_RR_INTERVALS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_Pulse_RR_Intervals);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Pulse_RR_Intervals);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Pulse_RR_Intervals_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_MS);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      BODY_RESTPULSE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_RESTPULSE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_restpulse_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_restpulse);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_restpulse_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_restpulse);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(7));

            return colDef;
         }
      };

      BODY_WEIGHT = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, BODY_WEIGHT_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Body);

            colDef.setColumnLabel(              Messages.ColumnFactory_BodyWeight_Label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_WEIGHT);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_BodyWeight_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_WEIGHT);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(7));

            return colDef;
         }
      };

      /*
       * Data
       */

      DATA_DP_TOLERANCE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_DP_TOLERANCE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_DPTolerance_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_DPTolerance_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_DPTolerance_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));

            return colDef;
         }
      };

      DATA_FIRST_COLUMN = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_FIRST_COLUMN_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setDefaultColumnWidth(0);

            return colDef;
         }
      };

      DATA_HAS_GEO_DATA = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                  final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_HAS_GEO_DATA_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_HasGeoData_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_HasGeoData_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_HasGeoData_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      DATA_IMPORT_FILE_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_IMPORT_FILE_NAME_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_import_filename_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_import_filename);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_import_filename_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      DATA_IMPORT_FILE_PATH = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_IMPORT_FILE_PATH_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_import_filepath_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_import_filepath);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_import_filepath_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      DATA_NUM_TIME_SLICES = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_NUM_TIME_SLICES_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_NumberOfTimeSlices_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_NumberOfTimeSlices_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_NumberOfTimeSlices_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      DATA_SERIE_START_END_INDEX = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_SERIE_START_END_INDEX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_SerieStartEndIndex_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_SerieStartEndIndex);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_SerieStartEndIndex_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      DATA_SEQUENCE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_SEQUENCE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_sequence_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_sequence);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      DATA_TIME_INTERVAL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DATA_TIME_INTERVAL_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_time_interval_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_time_interval);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_time_interval_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_time_interval);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      DATA_TOUR_ID = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                  final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "DATA_TOUR_ID", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourId);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourId);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(16));

            return colDef;
         }
      };
      /*
       * Device
       */

      DEVICE_BATTERY_SOC_END = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DEVICE_BATTERY_SOC_END_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Device);

            colDef.setColumnLabel(              Messages.ColumnFactory_Device_BatterySoC_End_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Device_BatterySoC_End_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Device_BatterySoC_End_Header_Tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(4));

            return colDef;
         }
      };

      DEVICE_BATTERY_SOC_START = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DEVICE_BATTERY_SOC_START_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Device);

            colDef.setColumnLabel(              Messages.ColumnFactory_Device_BatterySoC_Start_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Device_BatterySoC_Start_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Device_BatterySoC_Start_Header_Tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(4));

            return colDef;
         }
      };

      DEVICE_DISTANCE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DEVICE_DISTANCE_ID, SWT.TRAIL);

            final String unit = UI.UNIT_LABEL_DISTANCE + " * 1000"; //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Device);

            colDef.setColumnLabel(              Messages.ColumnFactory_device_start_distance_label);
            colDef.setColumnHeaderText(         unit);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_device_start_distance_tooltip);
            colDef.setColumnUnit(               unit);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(13));

            return colDef;
         }
      };

      DEVICE_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, DEVICE_NAME_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Device);

            colDef.setColumnLabel(              Messages.ColumnFactory_device_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_device);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_device_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      DEVICE_PROFILE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "DEVICE_PROFILE", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Device);

            colDef.setColumnLabel(              Messages.ColumnFactory_profile_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_profile);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_profile_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };


      /*
       * Location
       */

      LOCATION_DATA_ID = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_DATA_ID_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Data_LocationID_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Data_LocationID_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Data_LocationID_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_DATA_APPLIED_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_DATA_APPLIED_NAME_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_AddressName);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Data_AppliedName_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Data_AppliedName_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Data_AppliedName_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_DATA_LAST_MODIFIED = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_DATA_LAST_MODIFIED_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Data_LastModified_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Data_LastModified_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Data_LastModified_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_GEO_BOUNDING_BOX_WIDTH = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_BOUNDING_BOX_WIDTH_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Geo_BoundingBox_Width_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Geo_BoundingBox_Width_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_BoundingBox_Width_Tooltip.formatted(UI.UNIT_LABEL_DISTANCE_M_OR_YD));
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_M_OR_YD);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_GEO_BOUNDING_BOX_HEIGHT = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_BOUNDING_BOX_HEIGHT_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Geo_BoundingBox_Height_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Geo_BoundingBox_Height_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_BoundingBox_Height_Tooltip.formatted(UI.UNIT_LABEL_DISTANCE_M_OR_YD));
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_M_OR_YD);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_GEO_IS_RESIZED_BOUNDING_BOX = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_IS_RESIZED_BOUNDING_BOX_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Geo_IsResizedBoundingBox_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Geo_IsResizedBoundingBox_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_IsResizedBoundingBox_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(4));

            return colDef;
         }
      };

      LOCATION_GEO_LATITUDE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_LATITUDE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnName(               Messages.ColumnFactory_Location_Geo_Latitude);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_Latitude_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      LOCATION_GEO_LONGITUDE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_LONGITUDE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnName(               Messages.ColumnFactory_Location_Geo_Longitude);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_Longitude_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      LOCATION_GEO_LATITUDE_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_LATITUDE_DIFF_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Geo_LatitudeDiff_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Geo_LatitudeDiff_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_LatitudeDiff_Tooltip.formatted(UI.UNIT_LABEL_DISTANCE_M_OR_YD));
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_M_OR_YD);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_GEO_LONGITUDE_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_LONGITUDE_DIFF_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Geo_LongitudeDiff_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Geo_LongitudeDiff_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_LongitudeDiff_Tooltip.formatted(UI.UNIT_LABEL_DISTANCE_M_OR_YD));
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_M_OR_YD);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_GEO_ZOOMLEVEL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_GEO_ZOOMLEVEL_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Geo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Geo_Zoomlevel_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Geo_Zoomlevel_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Geo_Zoomlevel_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_TOUR_USAGE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_TOUR_USAGE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnName(               Messages.ColumnFactory_Location_TourUsage);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_TourUsage_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_TOUR_USAGE_START_LOCATIONS= new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_TOUR_USAGE_START_LOCATIONS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnName(               Messages.ColumnFactory_Location_TourUsage_StartLocations);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_TourUsage_StartLocations_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      LOCATION_TOUR_USAGE_END_LOCATIONS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, LOCATION_TOUR_USAGE_END_LOCATIONS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnName(               Messages.ColumnFactory_Location_TourUsage_EndLocations);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_TourUsage_EndLocations_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      final String categoryName     = Messages.ColumnFactory_Category_AddressName;
      final String categoryCountry  = Messages.ColumnFactory_Category_AddressCountry;
      final String categoryState    = Messages.ColumnFactory_Category_AddressState;
      final String categoryCity     = Messages.ColumnFactory_Category_AddressCity;
      final String categoryRoad     = Messages.ColumnFactory_Category_AddressRoad;
      final String categoryArea1    = Messages.ColumnFactory_Category_AddressArea1;
      final String categoryArea2    = Messages.ColumnFactory_Category_AddressArea2;
      final String categoryArea3    = Messages.ColumnFactory_Category_AddressArea3;
      final String categoryArea4    = Messages.ColumnFactory_Category_AddressArea4;
      final String categoryOther    = Messages.ColumnFactory_Category_AddressOther;

      LOCATION_PART_DisplayName      = createColumn_Address(      categoryName,     LOCATION_PART_DisplayName_ID,        Messages.Tour_Location_Part_OsmDefaultName);
      LOCATION_PART_Name             = createColumn_Address(      categoryName,     LOCATION_PART_Name_ID,               Messages.Tour_Location_Part_OsmName);

      LOCATION_PART_Country          = createColumn_Address(      categoryCountry,  LOCATION_PART_Country_ID,            Messages.Tour_Location_Part_Country);
      LOCATION_PART_CountryCode      = createColumn_Address(      categoryCountry,  LOCATION_PART_CountryCode_ID,        Messages.Tour_Location_Part_CountryCode);
      LOCATION_PART_Continent        = createColumn_Address(      categoryCountry,  LOCATION_PART_Continent_ID,          Messages.Tour_Location_Part_Continent);

      LOCATION_PART_Region           = createColumn_Address(      categoryState,    LOCATION_PART_Region_ID,             Messages.Tour_Location_Part_Region);
      LOCATION_PART_State            = createColumn_Address(      categoryState,    LOCATION_PART_State_ID,              Messages.Tour_Location_Part_State);
      LOCATION_PART_StateDistrict    = createColumn_Address(      categoryState,    LOCATION_PART_StateDistrict_ID,      Messages.Tour_Location_Part_StateDistrict);
      LOCATION_PART_County           = createColumn_Address(      categoryState,    LOCATION_PART_County_ID,             Messages.Tour_Location_Part_County);

      LOCATION_PART_Municipality     = createColumn_Address(      categoryCity,     LOCATION_PART_Municipality_ID,       Messages.Tour_Location_Part_Municipality);
      LOCATION_PART_City             = createColumn_Address(      categoryCity,     LOCATION_PART_City_ID,               Messages.Tour_Location_Part_City);
      LOCATION_PART_Town             = createColumn_Address(      categoryCity,     LOCATION_PART_Town_ID,               Messages.Tour_Location_Part_Town);
      LOCATION_PART_Village          = createColumn_Address(      categoryCity,     LOCATION_PART_Village_ID,            Messages.Tour_Location_Part_Village);
      LOCATION_PART_Postcode         = createColumn_Address_Trail(categoryCity,     LOCATION_PART_Postcode_ID,           Messages.Tour_Location_Part_Postcode);

      LOCATION_PART_Road             = createColumn_Address(      categoryRoad,     LOCATION_PART_Road_ID,               Messages.Tour_Location_Part_Road);
      LOCATION_PART_HouseName        = createColumn_Address(      categoryRoad,     LOCATION_PART_HouseName_ID,          Messages.Tour_Location_Part_HouseName);
      LOCATION_PART_HouseNumber      = createColumn_Address_Trail(categoryRoad,     LOCATION_PART_HouseNumber_ID,        Messages.Tour_Location_Part_HouseNumber);

      LOCATION_PART_CityDistrict     = createColumn_Address(      categoryArea1,    LOCATION_PART_CityDistrict_ID,       Messages.Tour_Location_Part_CityDistrict);
      LOCATION_PART_District         = createColumn_Address(      categoryArea1,    LOCATION_PART_District_ID,           Messages.Tour_Location_Part_District);
      LOCATION_PART_Borough          = createColumn_Address(      categoryArea1,    LOCATION_PART_Borough_ID,            Messages.Tour_Location_Part_Borough);
      LOCATION_PART_Suburb           = createColumn_Address(      categoryArea1,    LOCATION_PART_Suburb_ID,             Messages.Tour_Location_Part_Suburb);
      LOCATION_PART_Subdivision      = createColumn_Address(      categoryArea1,    LOCATION_PART_Subdivision_ID,        Messages.Tour_Location_Part_Subdivision);

      LOCATION_PART_Hamlet           = createColumn_Address(      categoryArea2,    LOCATION_PART_Hamlet_ID,             Messages.Tour_Location_Part_Hamlet);
      LOCATION_PART_Croft            = createColumn_Address(      categoryArea2,    LOCATION_PART_Croft_ID,              Messages.Tour_Location_Part_Croft);
      LOCATION_PART_IsolatedDwelling = createColumn_Address(      categoryArea2,    LOCATION_PART_IsolatedDwelling_ID,   Messages.Tour_Location_Part_IsolatedDwelling);

      LOCATION_PART_Neighbourhood    = createColumn_Address(      categoryArea3,    LOCATION_PART_Neighbourhood_ID,      Messages.Tour_Location_Part_Neighbourhood);
      LOCATION_PART_Allotments       = createColumn_Address(      categoryArea3,    LOCATION_PART_Allotments_ID,         Messages.Tour_Location_Part_Allotments);
      LOCATION_PART_Quarter          = createColumn_Address(      categoryArea3,    LOCATION_PART_Quarter_ID,            Messages.Tour_Location_Part_Quarter);

      LOCATION_PART_CityBlock        = createColumn_Address(      categoryArea4,    LOCATION_PART_CityBlock_ID,          Messages.Tour_Location_Part_CityBlock);
      LOCATION_PART_Residential      = createColumn_Address(      categoryArea4,    LOCATION_PART_Residential_ID,        Messages.Tour_Location_Part_Residential);
      LOCATION_PART_Farm             = createColumn_Address(      categoryArea4,    LOCATION_PART_Farm_ID,               Messages.Tour_Location_Part_Farm);
      LOCATION_PART_Farmyard         = createColumn_Address(      categoryArea4,    LOCATION_PART_Farmyard_ID,           Messages.Tour_Location_Part_Farmyard);
      LOCATION_PART_Industrial       = createColumn_Address(      categoryArea4,    LOCATION_PART_Industrial_ID,         Messages.Tour_Location_Part_Industrial);
      LOCATION_PART_Commercial       = createColumn_Address(      categoryArea4,    LOCATION_PART_Commercial_ID,         Messages.Tour_Location_Part_Commercial);
      LOCATION_PART_Retail           = createColumn_Address(      categoryArea4,    LOCATION_PART_Retail_ID,             Messages.Tour_Location_Part_Retail);

      LOCATION_PART_Aerialway        = createColumn_Address(      categoryOther,    LOCATION_PART_Aerialway_ID,          Messages.Tour_Location_Part_Aerialway);
      LOCATION_PART_Aeroway          = createColumn_Address(      categoryOther,    LOCATION_PART_Aeroway_ID,            Messages.Tour_Location_Part_Aeroway);
      LOCATION_PART_Amenity          = createColumn_Address(      categoryOther,    LOCATION_PART_Amenity_ID,            Messages.Tour_Location_Part_Amenity);
      LOCATION_PART_Boundary         = createColumn_Address(      categoryOther,    LOCATION_PART_Boundary_ID,           Messages.Tour_Location_Part_Boundary);
      LOCATION_PART_Bridge           = createColumn_Address(      categoryOther,    LOCATION_PART_Bridge_ID,             Messages.Tour_Location_Part_Bridge);
      LOCATION_PART_Club             = createColumn_Address(      categoryOther,    LOCATION_PART_Club_ID,               Messages.Tour_Location_Part_Club);
      LOCATION_PART_Craft            = createColumn_Address(      categoryOther,    LOCATION_PART_Craft_ID,              Messages.Tour_Location_Part_Craft);
      LOCATION_PART_Emergency        = createColumn_Address(      categoryOther,    LOCATION_PART_Emergency_ID,          Messages.Tour_Location_Part_Emergency);
      LOCATION_PART_Historic         = createColumn_Address(      categoryOther,    LOCATION_PART_Historic_ID,           Messages.Tour_Location_Part_Historic);
      LOCATION_PART_Landuse          = createColumn_Address(      categoryOther,    LOCATION_PART_Landuse_ID,            Messages.Tour_Location_Part_Landuse);
      LOCATION_PART_Leisure          = createColumn_Address(      categoryOther,    LOCATION_PART_Leisure_ID,            Messages.Tour_Location_Part_Leisure);
      LOCATION_PART_ManMade          = createColumn_Address(      categoryOther,    LOCATION_PART_ManMade_ID,            Messages.Tour_Location_Part_ManMade);
      LOCATION_PART_Military         = createColumn_Address(      categoryOther,    LOCATION_PART_Military_ID,           Messages.Tour_Location_Part_Military);
      LOCATION_PART_MountainPass     = createColumn_Address(      categoryOther,    LOCATION_PART_MountainPass_ID,       Messages.Tour_Location_Part_MountainPass);
      LOCATION_PART_Natural          = createColumn_Address(      categoryOther,    LOCATION_PART_Natural_ID,            Messages.Tour_Location_Part_Natural);
      LOCATION_PART_Office           = createColumn_Address(      categoryOther,    LOCATION_PART_Office_ID,             Messages.Tour_Location_Part_Office);
      LOCATION_PART_Place            = createColumn_Address(      categoryOther,    LOCATION_PART_Place_ID,              Messages.Tour_Location_Part_Place);
      LOCATION_PART_Railway          = createColumn_Address(      categoryOther,    LOCATION_PART_Railway_ID,            Messages.Tour_Location_Part_Railway);
      LOCATION_PART_Shop             = createColumn_Address(      categoryOther,    LOCATION_PART_Shop_ID,               Messages.Tour_Location_Part_Shop);
      LOCATION_PART_Tourism          = createColumn_Address(      categoryOther,    LOCATION_PART_Tourism_ID,            Messages.Tour_Location_Part_Tourism);
      LOCATION_PART_Tunnel           = createColumn_Address(      categoryOther,    LOCATION_PART_Tunnel_ID,             Messages.Tour_Location_Part_Tunnel);
      LOCATION_PART_Waterway         = createColumn_Address(      categoryOther,    LOCATION_PART_Waterway_ID,           Messages.Tour_Location_Part_Waterway);

      LOCATION_PART_SettlementSmall  = createColumn_Address(      categoryCity,     LOCATION_PART_SettlementSmall_ID,    Messages.Tour_Location_Part_SettlementSmall);
      LOCATION_PART_SettlementLarge  = createColumn_Address(      categoryCity,     LOCATION_PART_SettlementLarge_ID,    Messages.Tour_Location_Part_SettlementLarge);

      /*
       * Marker
       */

      MARKER_ALTITUDE_ELEVATION_GAIN_DELTA = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MARKER_ALTITUDE_ELEVATION_GAIN_DELTA", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_UP;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_GainDelta_Label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_GainDelta_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      MARKER_ALTITUDE_ELEVATION_LOSS_DELTA = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MARKER_ALTITUDE_ELEVATION_LOSS_DELTA", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_ELEVATION + UI.SPACE + UI.SYMBOL_ARROW_DOWN;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Elevation_LossDelta_Label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Elevation_LossDelta_Tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      MARKER_MAP_VISIBLE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(8);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MARKER_MAP_VISIBLE", SWT.CENTER); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Marker);

            colDef.setColumnLabel(              Messages.Tour_Marker_Column_IsVisible);
            colDef.setColumnHeaderText(         Messages.Tour_Marker_Column_IsVisible);
            colDef.setColumnHeaderToolTipText(  Messages.Tour_Marker_Column_IsVisibleNoEdit_Tooltip);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      MARKER_SERIE_INDEX = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MARKER_SERIE_INDEX", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_SerieIndex_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_SerieIndex);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_SerieIndex_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      MARKER_TIME_DELTA = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MARKER_TIME_DELTA", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TimeDelta_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TimeDelta_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TimeDelta_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_TIME);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      MARKER_TYPE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MARKER_TYPE_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Marker);

            colDef.setColumnLabel(              Messages.ColumnFactory_MarkerType_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_MarkerType_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_MarkerType_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(25));

            return colDef;
         }
      };

      MARKER_URL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MARKER_URL_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Marker);

            colDef.setColumnLabel(              Messages.ColumnFactory_Url_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Url_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Url_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(25));

            return colDef;
         }
      };

      /*
       * Motion
       */

      MOTION_ALTIMETER = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MOTION_ALTIMETER_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_Motion_Altimeter);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_ALTIMETER);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Motion_Altimeter_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_ALTIMETER);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_0,
                  columnManager);

            return colDef;
         }
      };

      MOTION_AVG_PACE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MOTION_AVG_PACE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_pace_label);
            colDef.setColumnHeaderText(         UI.SYMBOL_AVERAGE_WITH_SPACE + UI.UNIT_LABEL_PACE);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_pace_tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_AVERAGE_WITH_SPACE + UI.UNIT_LABEL_PACE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      MOTION_AVG_PACE_DIFFERENCE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_AVG_PACE_DIFFERENCE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_pace_difference_label);
            colDef.setColumnHeaderText(         UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_PACE);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_pace_difference_tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_DIFFERENCE_WITH_SPACE + UI.UNIT_LABEL_PACE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      MOTION_AVG_SPEED = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MOTION_AVG_SPEED_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_speed_label);
            colDef.setColumnHeaderText(         UI.SYMBOL_AVERAGE_WITH_SPACE + UI.UNIT_LABEL_SPEED);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_speed_tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_AVERAGE_WITH_SPACE + UI.UNIT_LABEL_SPEED);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      MOTION_DISTANCE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MOTION_DISTANCE_ID, SWT.TRAIL);

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(11);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_distance_label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_DISTANCE);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_distance_tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_3,
                  columnManager);

            return colDef;
         }
      };

      MOTION_DISTANCE_DELTA = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String deltaDistance = UI.SYMBOL_DIFFERENCE_WITH_SPACE
                  + net.tourbook.common.UI.UNIT_LABEL_DISTANCE;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MOTION_DISTANCE_DELTA_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_DistanceDelta_Label);
            colDef.setColumnHeaderText(         deltaDistance);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_DistanceDelta_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_3,
                  columnManager);

            return colDef;
         }
      };

      MOTION_DISTANCE_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String header = Messages.ColumnFactory_Diff_Header + UI.SPACE + UI.UNIT_LABEL_DISTANCE;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_DISTANCE_DIFF", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourDistanceDiff_Label);
            colDef.setColumnHeaderText(         header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourDistanceDiff_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      MOTION_DISTANCE_TOTAL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_DISTANCE_TOTAL", SWT.TRAIL); //$NON-NLS-1$

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(11);
            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE + UI.UNIT_LABEL_DISTANCE;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_distanceTotal_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_distanceTotal_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_3,
                  columnManager);

            return colDef;
         }
      };

      MOTION_LATITUDE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_LATITUDE", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_latitude_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_latitude);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_latitude_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            return colDef;
         }
      };

      MOTION_LONGITUDE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_LONGITUDE", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_longitude_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_longitude);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_longitude_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            return colDef;
         }
      };

      MOTION_MAX_SPEED = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, MOTION_MAX_SPEED_ID, SWT.TRAIL);

            final String unitLabel = UI.SYMBOL_MAX + UI.UNIT_LABEL_SPEED;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_max_speed_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_max_speed_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      MOTION_PACE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_PACE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_pace_label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_PACE);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_pace_tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_PACE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      MOTION_SPEED = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_SPEED", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_speed_label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_SPEED);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_speed_tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_SPEED);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(ValueFormatSet.Number, ValueFormat.NUMBER_1_1, columnManager);

            return colDef;
         }
      };

      MOTION_SPEED_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String header = Messages.ColumnFactory_Diff_Header + UI.SPACE + UI.UNIT_LABEL_SPEED;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "MOTION_SPEED_DIFF", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Motion);

            colDef.setColumnLabel(              Messages.ColumnFactory_SpeedDiff_Label);
            colDef.setColumnHeaderText(         header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_SpeedDiff_Tooltip);
            colDef.setColumnUnit(               header);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      /*
       * Nutrition
       */

      NUTRITION_NUM_PRODUCTS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, NUTRITION_NUM_PRODUCTS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Nutrition);

            colDef.setColumnLabel(              Messages.ColumnFactory_Nutrition_NumberOfProducts_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Nutrition_NumberOfProducts_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Nutrition_NumberOfProducts_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));

            return colDef;
         }
      };

      /*
       * Photo
       */

      PHOTO_FILE_PATH = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_FILE_PATH", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_FilePath_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_FilePath_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_FilePath_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      PHOTO_NUMBER_OF_GPS_PHOTOS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_NUMBER_OF_GPS_PHOTOS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_NumberOfGPSPhotos_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_NumberOfGPSPhotos_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_NumberOfGPSPhotos_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      PHOTO_NUMBER_OF_NO_GPS_PHOTOS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_NUMBER_OF_NO_GPS_PHOTOS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_NumberOfNoGPSPhotos_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_NumberOfNoGPSPhotos_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_NumberOfNoGPSPhotos_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      PHOTO_NUMBER_OF_PHOTOS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_NUMBER_OF_PHOTOS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_NumberOfTourPhotos_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_NumberOfTourPhotos_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_NumberOfTourPhotos_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      PHOTO_TIME_ADJUSTMENT = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_TIME_ADJUSTMENT", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_TimeAdjustment_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_TimeAdjustment_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_TimeAdjustment_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11)); // 9 ... 54

            return colDef;
         }
      };

      PHOTO_TIME_ADJUSTMENT_ALL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_TIME_ADJUSTMENT_ALL", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_TimeAdjustmentAll_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_TimeAdjustmentAll_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_TimeAdjustmentAll_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      PHOTO_TOUR_CAMERA = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "PHOTO_TOUR_CAMERA", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Photo);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_TourCamera_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_TourCamera_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_TourCamera_Label_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      /*
       * Power
       */

      POWER_AVG = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWER_AVG_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Power);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_Avg_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_Avg_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_Avg_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_power);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_0,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWER_MAX = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWER_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Power);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_Max_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_Max_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_Max_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_power);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_0,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWER_NORMALIZED = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWER_NORMALIZED_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Power);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_Normalized_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_Normalized_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_Normalized_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_power);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));

            return colDef;
         }
      };

      POWER_TIME_SLICE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "POWER_TIME_SLICE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Power);

            colDef.setColumnLabel(              Messages.ColumnFactory_power_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_power);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_power_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_power);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      POWER_TOTAL_WORK = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWER_TOTAL_WORK_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Power);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_TotalWork_Tooltip);
            colDef.setColumnHeaderText(         UI.UNIT_JOULE_MEGA);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_TotalWork_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_JOULE_MEGA);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_2,
                  ValueFormat.NUMBER_1_3,
                  columnManager);

            return colDef;
         }
      };

      /*
       * Powertrain
       */

      POWERTRAIN_AVG_CADENCE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_AVG_CADENCE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_avg_cadence_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_avg_cadence);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_avg_cadence_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_avg_cadence);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWERTRAIN_CADENCE_TIME_SLICE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "POWERTRAIN_CADENCE_TIME_SLICE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_cadence_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_cadence);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_cadence_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_cadence);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      POWERTRAIN_GEAR_RATIO_TIME_SLICE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "POWERTRAIN_GEAR_RATIO_TIME_SLICE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_GearRatio_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_GearRatio_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_GearRatio_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      POWERTRAIN_GEAR_TEETH = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "POWERTRAIN_GEAR_TEETH", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_GearTeeth_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_GearTeeth_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_GearTeeth_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      POWERTRAIN_AVG_LEFT_PEDAL_SMOOTHNESS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_AVG_LEFT_PEDAL_SMOOTHNESS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_AvgLeftPedalSmoothness_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_AvgLeftPedalSmoothness_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_AvgLeftPedalSmoothness_Tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWERTRAIN_AVG_RIGHT_PEDAL_SMOOTHNESS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_AVG_RIGHT_PEDAL_SMOOTHNESS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_AvgRightPedalSmoothness_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_AvgRightPedalSmoothness_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_AvgRightPedalSmoothness_Tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWERTRAIN_AVG_LEFT_TORQUE_EFFECTIVENESS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_AVG_LEFT_TORQUE_EFFECTIVENESS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_AvgLeftTorqueEffectiveness_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_AvgLeftTorqueEffectiveness_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_AvgLeftTorqueEffectiveness_Tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWERTRAIN_AVG_RIGHT_TORQUE_EFFECTIVENESS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_AVG_RIGHT_TORQUE_EFFECTIVENESS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_AvgRightTorqueEffectiveness_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_AvgRightTorqueEffectiveness_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_AvgRightTorqueEffectiveness_Tooltip);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      POWERTRAIN_CADENCE_MULTIPLIER = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_CADENCE_MULTIPLIER_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_CadenceMultiplier_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_CadenceMultiplier_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_CadenceMultiplier_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_CadenceMultiplier_Unit);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(4));

            return colDef;
         }
      };

      POWERTRAIN_GEAR_FRONT_SHIFT_COUNT = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_GEAR_FRONT_SHIFT_COUNT_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_GearFrontShiftCount_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_GearFrontShiftCount_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_GearFrontShiftCount_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      POWERTRAIN_GEAR_REAR_SHIFT_COUNT = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_GEAR_REAR_SHIFT_COUNT_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_GearRearShiftCount_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_GearRearShiftCount_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_GearRearShiftCount_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      POWERTRAIN_PEDAL_LEFT_RIGHT_BALANCE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_PEDAL_LEFT_RIGHT_BALANCE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_LeftRightBalance_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_LeftRightBalance_Header2);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_LeftRightBalance_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            return colDef;
         }
      };

      POWERTRAIN_SLOW_VS_FAST_CADENCE_ZONES_DELIMITER = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager,
                  POWERTRAIN_SLOW_VS_FAST_CADENCE_ZONES_DELIMITER_ID,
                  SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_SlowVsFast_CadenceZonesDelimiter_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_SlowVsFast_CadenceZonesDelimiter_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_SlowVsFast_CadenceZonesDelimiter_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_Power_SlowVsFast_CadenceZonesDelimiter_Header);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      POWERTRAIN_SLOW_VS_FAST_CADENCE_PERCENTAGES = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, POWERTRAIN_SLOW_VS_FAST_CADENCE_PERCENTAGES_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Powertrain);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_SlowVsFast_CadencePercentages_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_SlowVsFast_CadencePercentages_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_SlowVsFast_CadencePercentages_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_Power_SlowVsFast_CadencePercentages_Header);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      /*
       * Running dynamics
       */

      RUN_DYN_STANCE_TIME_MIN = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STANCE_TIME_MIN_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StanceTime_Min);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_RunDyn_StanceTime_Min_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StanceTime_Min);
            colDef.setColumnUnit(               UI.UNIT_MS);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      RUN_DYN_STANCE_TIME_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STANCE_TIME_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StanceTime_Max);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_RunDyn_StanceTime_Max_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StanceTime_Max);
            colDef.setColumnUnit(               UI.UNIT_MS);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      RUN_DYN_STANCE_TIME_AVG = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STANCE_TIME_AVG_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StanceTime_Avg);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_RunDyn_StanceTime_Avg_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StanceTime_Avg);
            colDef.setColumnUnit(               UI.UNIT_MS);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      RUN_DYN_STANCE_TIME_BALANCE_MIN = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STANCE_TIME_BALANCE_MIN_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StanceTimeBalance_Min);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_RunDyn_StanceTimeBalance_Min_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StanceTimeBalance_Min);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  null,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      RUN_DYN_STANCE_TIME_BALANCE_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STANCE_TIME_BALANCE_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StanceTimeBalance_Max);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_RunDyn_StanceTimeBalance_Max_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StanceTimeBalance_Max);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  null,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      RUN_DYN_STANCE_TIME_BALANCE_AVG = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STANCE_TIME_BALANCE_AVG_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StanceTimeBalance_Avg);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_RunDyn_StanceTimeBalance_Avg_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StanceTimeBalance_Avg);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  null,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      RUN_DYN_STEP_LENGTH_MIN = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_MIN
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_LEFT_RIGHT
                  + UI.SPACE
                  + UI.UNIT_LABEL_DISTANCE_MM_OR_INCH;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STEP_LENGTH_MIN_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StepLength_Min);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StepLength_Min);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            if (UI.UNIT_IS_DISTANCE_KILOMETER == false) {

               // imperial has 1 more digit

               colDef.setValueFormats(
                     ValueFormatSet.Number,
                     null,
                     ValueFormat.NUMBER_1_1,
                     columnManager);
            }

            return colDef;
         }
      };

      RUN_DYN_STEP_LENGTH_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_MAX
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_LEFT_RIGHT
                  + UI.SPACE
                  + UI.UNIT_LABEL_DISTANCE_MM_OR_INCH;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STEP_LENGTH_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StepLength_Max);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StepLength_Max);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            if (UI.UNIT_IS_DISTANCE_KILOMETER == false) {

               // imperial has 1 more digit

               colDef.setValueFormats(
                     ValueFormatSet.Number,
                     null,
                     ValueFormat.NUMBER_1_1,
                     columnManager);
            }

            return colDef;
         }
      };

      RUN_DYN_STEP_LENGTH_AVG = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_AVERAGE
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_LEFT_RIGHT
                  + UI.SPACE
                  + UI.UNIT_LABEL_DISTANCE_MM_OR_INCH;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_STEP_LENGTH_AVG_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_StepLength_Avg);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_StepLength_Avg);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            if (UI.UNIT_IS_DISTANCE_KILOMETER == false) {

               // imperial has 1 more digit

               colDef.setValueFormats(
                     ValueFormatSet.Number,
                     null,
                     ValueFormat.NUMBER_1_1,
                     columnManager);
            }

            return colDef;
         }
      };

      RUN_DYN_VERTICAL_OSCILLATION_MIN = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_MIN
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP_DOWN
                  + UI.SPACE
                  + UI.UNIT_LABEL_DISTANCE_MM_OR_INCH;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_VERTICAL_OSCILLATION_MIN_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_VerticalOscillation_Min);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_VerticalOscillation_Min);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            if (UI.UNIT_IS_DISTANCE_KILOMETER == false) {

               // imperial has 1 more digit

               colDef.setValueFormats(
                     ValueFormatSet.Number,
                     null,
                     ValueFormat.NUMBER_1_1,
                     columnManager);
            }

            return colDef;
         }
      };

      RUN_DYN_VERTICAL_OSCILLATION_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_MAX
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP_DOWN
                  + UI.SPACE
                  + UI.UNIT_LABEL_DISTANCE_MM_OR_INCH;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_VERTICAL_OSCILLATION_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_VerticalOscillation_Max);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_VerticalOscillation_Max);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            if (UI.UNIT_IS_DISTANCE_KILOMETER == false) {

               // imperial has 1 more digit

               colDef.setValueFormats(
                     ValueFormatSet.Number,
                     null,
                     ValueFormat.NUMBER_1_1,
                     columnManager);
            }

            return colDef;
         }
      };

      RUN_DYN_VERTICAL_OSCILLATION_AVG = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_AVERAGE
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP_DOWN
                  + UI.SPACE
                  + UI.UNIT_LABEL_DISTANCE_MM_OR_INCH;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_VERTICAL_OSCILLATION_AVG_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_VerticalOscillation_Avg);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_VerticalOscillation_Avg);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            if (UI.UNIT_IS_DISTANCE_KILOMETER == false) {

               // imperial has 1 more digit

               colDef.setValueFormats(
                     ValueFormatSet.Number,
                     null,
                     ValueFormat.NUMBER_1_1,
                     columnManager);
            }

            return colDef;
         }
      };

      RUN_DYN_VERTICAL_RATIO_MIN = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_MIN
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP_DOWN
                  + UI.SPACE
                  + UI.SYMBOL_PERCENTAGE;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_VERTICAL_RATIO_MIN_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_VerticalRatio_Min);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_VerticalRatio_Min);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  null,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      RUN_DYN_VERTICAL_RATIO_MAX = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_MAX
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP_DOWN
                  + UI.SPACE
                  + UI.SYMBOL_PERCENTAGE;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_VERTICAL_RATIO_MAX_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_VerticalRatio_Max);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_VerticalRatio_Max);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  null,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      RUN_DYN_VERTICAL_RATIO_AVG = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final String headerLabel = UI.EMPTY_STRING
                  + UI.SYMBOL_AVERAGE
                  + UI.SPACE
                  + UI.SYMBOL_ARROW_UP_DOWN
                  + UI.SPACE
                  + UI.SYMBOL_PERCENTAGE;

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, RUN_DYN_VERTICAL_RATIO_AVG_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_RunDyn);

            colDef.setColumnLabel(              Messages.ColumnFactory_RunDyn_VerticalRatio_Avg);
            colDef.setColumnHeaderText(         headerLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_RunDyn_VerticalRatio_Avg);
            colDef.setColumnUnit(               UI.SYMBOL_PERCENTAGE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  null,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      /*
       * Sensor
       */

      SENSOR_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_NAME_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_Name);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_Name);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_Name_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_NAME_KEY = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_NAME_KEY_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_NameKey);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_NameKey);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_NameKey_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_DESCRIPTION = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_DESCRIPTION_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_Description);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_Description);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_MANUFACTURER_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_MANUFACTURER_NAME_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_ManufacturerName);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_ManufacturerName);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_MANUFACTURER_NUMBER = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_MANUFACTURER_NUMBER_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_ManufacturerNumber);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_ManufacturerNumber_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_ManufacturerNumber);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_PRODUCT_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_PRODUCT_NAME_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_ProductName);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_ProductName);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_PRODUCT_NUMBER = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_PRODUCT_NUMBER_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_ProductNumber);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_ProductNumber_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_ProductNumber);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_SERIAL_NUMBER = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_SERIAL_NUMBER_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_SerialNumber);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_SerialNumber_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_SerialNumber);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_STATE_BATTERY_LEVEL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_STATE_BATTERY_LEVEL_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_BatteryState_Level_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_BatteryState_Level_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_BatteryState_Level_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_STATE_BATTERY_STATUS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_STATE_BATTERY_STATUS_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_BatteryState_Status_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_BatteryState_Status_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_BatteryState_Status_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_STATE_BATTERY_VOLTAGE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_STATE_BATTERY_VOLTAGE_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_BatteryState_Voltage_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_BatteryState_Voltage_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_BatteryState_Voltage_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_TIME_FIRST_USED = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_TIME_FIRST_USED_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_Time_FirstUsed);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_Time_FirstUsed);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_TIME_LAST_USED= new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_TIME_LAST_USED_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_Time_LastUsed);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_Time_LastUsed);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SENSOR_TYPE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SENSOR_TYPE_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Sensor);

            colDef.setColumnLabel(              Messages.ColumnFactory_Sensor_Type);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Sensor_Type);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Sensor_Type_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      /*
       * States
       */

      STATE_DB_STATUS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "STATE_DB_STATUS", SWT.CENTER); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_State);

            colDef.setColumnLabel(              Messages.ColumnFactory_db_status_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_db_status_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_db_status_tooltip);

            colDef.setDefaultColumnWidth(20);

            return colDef;
         }
      };

      STATE_IMPORT_STATE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "STATE_IMPORT_STATE", SWT.CENTER); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_State);

            colDef.setColumnLabel(              Messages.ColumnFactory_ImportStatus_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_ImportStatus_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_ImportStatus_Tooltip);

            colDef.setDefaultColumnWidth(20);

            return colDef;
         }
      };

      /*
       * Surfing
       */

      SURFING_MIN_DISTANCE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SURFING_MIN_DISTANCE_ID, SWT.TRAIL);

            final String unitLabel = UI.SPACE1 + UI.UNIT_LABEL_DISTANCE_M_OR_YD;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Surfing);

            colDef.setColumnLabel(              Messages.ColumnFactory_Surfing_MinDistance_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Surfing_MinDistance_Header + unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Surfing_MinDistance_Label);
            colDef.setColumnUnit(               UI.UNIT_LABEL_DISTANCE_M_OR_YD);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SURFING_MIN_SPEED_START_STOP = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SURFING_MIN_SPEED_START_STOP_ID, SWT.TRAIL);

            final String unitLabel = UI.SPACE1 + UI.UNIT_LABEL_SPEED;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Surfing);

            colDef.setColumnLabel(              Messages.ColumnFactory_Surfing_MinSpeed_StartStop_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Surfing_MinSpeed_StartStop_Header + unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Surfing_MinSpeed_StartStop_Label);
            colDef.setColumnUnit(               UI.UNIT_LABEL_SPEED);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            return colDef;
         }
      };

      SURFING_MIN_SPEED_SURFING = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SURFING_MIN_SPEED_SURFING_ID, SWT.TRAIL);

            final String unitLabel = UI.SPACE1 + UI.UNIT_LABEL_SPEED;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Surfing);

            colDef.setColumnLabel(              Messages.ColumnFactory_Surfing_MinSpeed_Surfing_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Surfing_MinSpeed_Surfing_Header + unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Surfing_MinSpeed_Surfing_Label);
            colDef.setColumnUnit(               UI.UNIT_LABEL_SPEED);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SURFING_MIN_TIME_DURATION = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SURFING_MIN_TIME_DURATION_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Surfing);

            colDef.setColumnLabel(              Messages.ColumnFactory_Surfing_MinTimeDuration_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Surfing_MinTimeDuration_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Surfing_MinTimeDuration_Label);
            colDef.setColumnUnit(               OtherMessages.APP_UNIT_SECONDS_SMALL);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SURFING_NUMBER_OF_EVENTS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SURFING_NUMBER_OF_EVENTS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Surfing);

            colDef.setColumnLabel(              Messages.ColumnFactory_Surfing_NumberOfEvents_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Surfing_NumberOfEvents_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Surfing_NumberOfEvents_Label);
            colDef.setColumnUnit(               UI.SYMBOL_NUMBER_SIGN);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      /*
       * Swimming
       */
      SWIM_LENGTH_TYPE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, SWIM_LENGTH_TYPE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Swimming);

            colDef.setColumnLabel(              Messages.ColumnFactory_Swim_LengthType_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Swim_LengthType_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Swim_LengthType_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(4));

            return colDef;
         }
      };

      SWIM_STROKE_RATE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__SWIM_STROKE_RATE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Swimming);

            colDef.setColumnLabel(              Messages.ColumnFactory_Swim_StrokeRate_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Swim_StrokeRate_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Swim_StrokeRate_Tooltip);
//				colDef.setColumnUnit(               Messages.ColumnFactory_Swim_Cadence_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      SWIM_STROKES_PER_LENGTH = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__SWIM_STROKES_PER_LENGTH", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Swimming);

            colDef.setColumnLabel(              Messages.ColumnFactory_Swim_StrokesPerLength_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Swim_StrokesPerLength_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Swim_StrokesPerLength_Tooltip);
//				colDef.setColumnUnit(               Messages.ColumnFactory_Swim_Strokes_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      SWIM_STROKE_STYLE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__SWIM_STROKE_STYLE", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Swimming);

            colDef.setColumnLabel(              Messages.ColumnFactory_Swim_StrokeStyle_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Swim_StrokeStyle_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Swim_StrokeStyle_Tooltip);
//				colDef.setColumnUnit(               Messages.ColumnFactory_Swim_StrokeStyle_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

            return colDef;
         }
      };

      SWIM__TIME_TOUR_TIME_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__TIME_TOUR_TIME_DIFF", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourTimeDiff_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourTimeDiff_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourTimeDiff_Tooltip_2);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SWIM__TIME_TOUR_TIME_HH_MM_SS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(12);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__TIME_TOUR_TIME_HH_MM_SS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_time_label_hhmmss);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_time_label_hhmmss);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_time_tooltip_hhmmss);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time_hhmmss);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      SWIM__TIME_TOUR_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__TIME_TOUR_TIME", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      SWIM__TIME_TOUR_TIME_OF_DAY_HH_MM_SS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(12);
            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "SWIM__TIME_TOUR_TIME_OF_DAY_HH_MM_SS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_Tour_DayTime);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Tour_DayTime);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Tour_DayTime_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time_hhmmss);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      /*
       * Time
       */

      TIME_IS_BREAK_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_IS_BREAK_TIME", SWT.CENTER); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_BreakTime_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_BreakTime_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_BreakTime_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(3));

            return colDef;
         }
      };

      TIME_IS_PAUSED_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_IS_PAUSED_TIME", SWT.CENTER); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_Time_PausedTime_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Time_PausedTime_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Time_PausedTime_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(3));

            return colDef;
         }
      };

      TIME_DATE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_DATE_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_date_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_date);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_date_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(18));

            return colDef;
         }
      };

      TIME_TIME_ZONE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_TIME_ZONE_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TimeZone_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TimeZone_Header);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(25));

            return colDef;
         }
      };

      TIME_TIME_ZONE_DIFFERENCE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_TIME_ZONE_DIFFERENCE_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TimeZoneDifference_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TimeZoneDifference_Header);

// !!! THIS MUST BE SET IN THE VIEW TO SET THE CORRECT DEFAULT TIME ZONE !!!
//				colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TimeZone_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      TIME_TOUR_TIME_DIFF = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_TIME_DIFF", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourTimeDiff_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourTimeDiff_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourTimeDiff_Tooltip_2);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      TIME_TOUR_TIME_HH_MM_SS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(12);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_TIME_HH_MM_SS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_time_label_hhmmss);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_time_label_hhmmss);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_time_tooltip_hhmmss);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time_hhmmss);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      TIME_TOUR_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_TIME", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      TIME_TOUR_TIME_OF_DAY_HH_MM_SS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(12);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_TIME_OF_DAY_HH_MM_SS", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_Tour_DayTime);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Tour_DayTime);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Tour_DayTime_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_tour_time_hhmmss);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      TIME__COMPUTED_BREAK_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME__COMPUTED_BREAK_TIME_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_break_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_break_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_break_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_break_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Time,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME__COMPUTED_MOVING_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME__COMPUTED_MOVING_TIME_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_moving_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_moving_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_moving_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_moving_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Time,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME__DEVICE_PAUSED_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME__DEVICE_PAUSED_TIME_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_paused_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_paused_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_paused_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_paused_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Time,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME__COMPUTED_BREAK_TIME_RELATIVE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME__COMPUTED_BREAK_TIME_RELATIVE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_break_time_relative_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_break_relative_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_break_time_relative_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_break_relative_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      TIME__DEVICE_RECORDED_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME__DEVICE_RECORDED_TIME_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_recorded_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_recorded_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_recorded_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_recorded_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Time,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME__DEVICE_ELAPSED_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME__DEVICE_ELAPSED_TIME_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_elapsed_time_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_elapsed_time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_elapsed_time_tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_elapsed_time);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Time,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME__DEVICE_ELAPSED_TIME_TOTAL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME__DEVICE_ELAPSED_TIME_TOTAL", SWT.TRAIL); //$NON-NLS-1$

            final String unitLabel = UI.SYMBOL_SUM_WITH_SPACE + Messages.ColumnFactory_elapsed_time;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_elapsed_timeTotal_label);
            colDef.setColumnHeaderText(         unitLabel);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_elapsed_timeTotal_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
            colDef.setValueFormats(
                  ValueFormatSet.Time,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME_TOUR_DATE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_DATE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnHeaderText(         Messages.ColumnFactory_date);
            colDef.setColumnLabel(              Messages.ColumnFactory_date_label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

            return colDef;
         }
      };

      TIME_TOUR_DURATION_TIME = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_DURATION_TIME", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourDurationTime_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourDurationTime_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourDurationTime_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(15));

            return colDef;
         }
      };

      TIME_TOUR_START_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_TOUR_START_TIME_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourStartTime_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourStartTime_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourStartTime_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            colDef.setValueFormats(
                  ValueFormatSet.Time_mmss,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME_TOUR_END_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_END_TIME", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourEndTime_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourEndTime_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourEndTime_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            colDef.setValueFormats(
                  ValueFormatSet.Time_mmss,
                  ValueFormat.TIME_HH_MM,
                  columnManager);

            return colDef;
         }
      };

      TIME_TOUR_START_DATE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_START_DATE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourStartDate_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourStartDate_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourStartDate_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            return colDef;
         }
      };

      TIME_TOUR_END_DATE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TIME_TOUR_END_DATE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourEndDate_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourEndDate_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourEndDate_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            return colDef;
         }
      };

      TIME_WEEK_DAY = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_WEEK_DAY_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_Tour_WeekDay_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Tour_WeekDay_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Tour_WeekDay_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));

            return colDef;
         }
      };

      TIME_WEEK_NO = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_WEEK_NO_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_week_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_week_header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_week_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(7));

            return colDef;
         }
      };

      TIME_WEEKYEAR = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TIME_WEEKYEAR_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourWeekYear_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourWeekYear_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourWeekYear_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(14));

            return colDef;
         }
      };

      /*
       * Tour
       */

      TOUR_DESCRIPTION = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_DESCRIPTION_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_TourDescription_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourDescription_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_TourDescription_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      TOUR_MARKER = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "TOUR_MARKER", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_marker_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_marker_label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_marker_label_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));
            colDef.setColumnWeightData(new ColumnWeightData(100, true));

            return colDef;
         }
      };

      TOUR_LOCATION_START = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_LOCATION_START_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_Tour_LocationStart_Title);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Tour_LocationStart_Title);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Tour_LocationStart_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      TOUR_LOCATION_END = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_LOCATION_END_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_Tour_LocationEnd_Title);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Tour_LocationEnd_Title);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Tour_LocationEnd_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      TOUR_LOCATION_ID_START = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_LOCATION_ID_START_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Tour_LocationID_Start);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Tour_LocationID_Start_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Tour_LocationID_Start);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      TOUR_LOCATION_ID_END = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_LOCATION_ID_END_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Data);

            colDef.setColumnLabel(              Messages.ColumnFactory_Location_Tour_LocationID_End);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Location_Tour_LocationID_End_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Location_Tour_LocationID_End);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      TOUR_NUM_MARKERS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_NUM_MARKERS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_marker_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_marker_header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_marker_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));

            return colDef;
         }
      };

      TOUR_NUM_PHOTOS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_NUM_PHOTOS_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_NumberOfPhotos_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_NumberOfPhotos_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_NumberOfPhotos_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      TOUR_POSITIONED_PHOTO = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_POSITIONED_PHOTO_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_Photo_Positioned_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Photo_Positioned_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Photo_Positioned_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

            return colDef;
         }
      };

      TOUR_TAGS = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_TAGS_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_tag_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_tag_label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_tag_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };

      TOUR_TITLE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_TITLE_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_title_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_title);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_title_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(25));

            return colDef;
         }
      };

      TOUR_TYPE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_TYPE_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnLabel(              Messages.ColumnFactory_tour_type_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_tour_type_header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_tour_type_tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));

            return colDef;
         }
      };

      TOUR_TYPE_TEXT = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TOUR_TYPE_TEXT_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Tour);

            colDef.setColumnHeaderText(         Messages.ColumnFactory_TourTypeText_Header);
            colDef.setColumnLabel(              Messages.ColumnFactory_TourTypeText_Label);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(18));

            return colDef;
         }
      };

      /*
       * Training
       */

      TRAINING_EFFECT_AEROB = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_EFFECT_AEROB_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Training_TrainingEffect_Aerob_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Training_TrainingEffect_Aerob_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Training_TrainingEffect_Aerob_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      TRAINING_EFFECT_ANAEROB = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_EFFECT_ANAEROB_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Training_TrainingEffect_Anaerob_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Training_TrainingEffect_Anaerob_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Training_TrainingEffect_Anaerob_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      TRAINING_FTP = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_FTP_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_FTP_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_FTP_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_FTP_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_power);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));

            return colDef;
         }
      };

      TRAINING_INTENSITY_FACTOR = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_INTENSITY_FACTOR_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_IntensityFactor_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_IntensityFactor_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_IntensityFactor_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_2,
                  ValueFormat.NUMBER_1_2,
                  columnManager);

            return colDef;
         }
      };

      TRAINING_PERFORMANCE_LEVEL = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_PERFORMANCE_LEVEL_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Training_TrainingPerformance_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Training_TrainingPerformance_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Training_TrainingPerformance_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_2,
                  ValueFormat.NUMBER_1_2,
                  columnManager);

            return colDef;
         }
      };

      TRAINING_POWER_TO_WEIGHT = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_POWER_TO_WEIGHT_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_PowerToWeight_Tooltip);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_PowerToWeight_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_PowerToWeight_Tooltip);
            colDef.setColumnUnit(               UI.UNIT_POWER_TO_WEIGHT_RATIO);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_2,
                  ValueFormat.NUMBER_1_2,
                  columnManager);

            return colDef;
         }
      };

      TRAINING_STRESS_SCORE = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, TRAINING_STRESS_SCORE_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Training);

            colDef.setColumnLabel(              Messages.ColumnFactory_Power_TrainingStressScore_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Power_TrainingStressScore_Header);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Power_TrainingStressScore_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(5));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };

      /*
       * Waypoint
       */

      WAYPOINT_ALTITUDE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(10);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_ALTITUDE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Altitude);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Altitude_Label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_ELEVATION);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Waypoint_Altitude_Label);
            colDef.setColumnUnit(               UI.UNIT_LABEL_ELEVATION);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_CATEGORY = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(30);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_CATEGORY", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Waypoint);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Category);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Category);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_COMMENT = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(30);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_COMMENT", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Waypoint);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Comment);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Comment);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_DATE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(15);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_DATE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Date);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Date);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Waypoint_Date_Tooltip);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_DESCRIPTION = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(30);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_DESCRIPTION", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Waypoint);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Description);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Description);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_ID = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_ID", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Waypoint);

            colDef.setColumnLabel(              Messages.ColumnFactory_Id_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Id_Label);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Id_Tooltip);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      WAYPOINT_NAME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(30);
            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_NAME", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Waypoint);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Name);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Name);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_SYMBOL = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(30);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_SYMBOL", SWT.LEAD); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Waypoint);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Symbol);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Symbol);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      WAYPOINT_TIME = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final int pixelWidth = pixelConverter.convertWidthInCharsToPixels(15);

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WAYPOINT_TIME", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Time);

            colDef.setColumnLabel(              Messages.ColumnFactory_Waypoint_Time);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_Waypoint_Time);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_Waypoint_Time_Tooltip);
            colDef.setColumnUnit(               Messages.ColumnFactory_Waypoint_Time_Unit);

            colDef.setDefaultColumnWidth(pixelWidth);
            colDef.setColumnWeightData(new ColumnPixelData(pixelWidth, true));

            return colDef;
         }
      };

      /*
       * Weather
       */

      WEATHER_AIR_QUALITY = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, WEATHER_AIR_QUALITY_ID, SWT.LEAD);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Weather);

            colDef.setColumnLabel(              Messages.ColumnFactory_AirQuality_Label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_AirQuality);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_AirQuality_Label);

            colDef.setDefaultColumnWidth(       pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };

      WEATHER_CLOUDS = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, WEATHER_CLOUDS_ID, SWT.CENTER);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Weather);

            colDef.setColumnLabel(              Messages.ColumnFactory_clouds_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_clouds);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_clouds_tooltip);

            colDef.setDefaultColumnWidth(25);

            return colDef;
         }
      };

      WEATHER_TEMPERATURE_AVG = createColumn_Temperature(
            WEATHER_TEMPERATURE_AVG_ID,
            UI.SYMBOL_AVERAGE,
            Messages.ColumnFactory_Temperature_Avg_Label,
            Messages.ColumnFactory_Temperature_Avg_Tooltip);

      WEATHER_TEMPERATURE_AVG_COMBINED = createColumn_Temperature(
            WEATHER_TEMPERATURE_AVG_COMBINED_ID,
            UI.SYMBOL_AVERAGE,
            Messages.ColumnFactory_Temperature_Avg_Combined_Label,
            Messages.ColumnFactory_Temperature_Avg_Combined_Tooltip);

      WEATHER_TEMPERATURE_AVG_DEVICE = createColumn_Temperature(
            WEATHER_TEMPERATURE_AVG_DEVICE_ID,
            UI.SYMBOL_AVERAGE,
            Messages.ColumnFactory_Temperature_Avg_Device_Label,
            Messages.ColumnFactory_Temperature_Avg_Device_Tooltip);

      WEATHER_TEMPERATURE_MIN = createColumn_Temperature(
            WEATHER_TEMPERATURE_MIN_ID,
            UI.SYMBOL_MIN,
            Messages.ColumnFactory_Temperature_Min_Label,
            Messages.ColumnFactory_Temperature_Min_Tooltip);

      WEATHER_TEMPERATURE_MIN_COMBINED = createColumn_Temperature(
            WEATHER_TEMPERATURE_MIN_COMBINED_ID,
            UI.SYMBOL_MIN,
            Messages.ColumnFactory_Temperature_Min_Combined_Label,
            Messages.ColumnFactory_Temperature_Min_Combined_Tooltip);

      WEATHER_TEMPERATURE_MIN_DEVICE = createColumn_Temperature(
            WEATHER_TEMPERATURE_MIN_DEVICE_ID,
            UI.SYMBOL_MIN,
            Messages.ColumnFactory_Temperature_Min_Device_Label,
            Messages.ColumnFactory_Temperature_Min_Device_Tooltip);

      WEATHER_TEMPERATURE_MAX = createColumn_Temperature(
            WEATHER_TEMPERATURE_MAX_ID,
            UI.SYMBOL_MAX,
            Messages.ColumnFactory_Temperature_Max_Label,
            Messages.ColumnFactory_Temperature_Max_Tooltip);

      WEATHER_TEMPERATURE_MAX_COMBINED = createColumn_Temperature(
            WEATHER_TEMPERATURE_MAX_COMBINED_ID,
            UI.SYMBOL_MAX,
            Messages.ColumnFactory_Temperature_Max_Combined_Label,
            Messages.ColumnFactory_Temperature_Max_Combined_Tooltip);

      WEATHER_TEMPERATURE_MAX_DEVICE = createColumn_Temperature(
            WEATHER_TEMPERATURE_MAX_DEVICE_ID,
            UI.SYMBOL_MAX,
            Messages.ColumnFactory_Temperature_Max_Device_Label,
            Messages.ColumnFactory_Temperature_Max_Device_Tooltip);

      WEATHER_TEMPERATURE_TIME_SLICE = new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "WEATHER_TEMPERATURE_TIME_SLICE", SWT.TRAIL); //$NON-NLS-1$

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Weather);

            colDef.setColumnLabel(              Messages.ColumnFactory_temperature_label);
            colDef.setColumnHeaderText(         UI.UNIT_LABEL_TEMPERATURE);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_temperature_tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_TEMPERATURE);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
            colDef.setValueFormats(ValueFormatSet.Number, ValueFormat.NUMBER_1_1, columnManager);

            return colDef;
         }
      };

      WEATHER_WIND_DIR = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, WEATHER_WIND_DIRECTION_ID, SWT.TRAIL);

            final String unitLabel = UI.UNIT_LABEL_DIRECTION;

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Weather);

            colDef.setColumnLabel(              Messages.ColumnFactory_wind_dir_label);
            colDef.setColumnHeaderText(         Messages.ColumnFactory_wind_dir);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_wind_dir_tooltip);
            colDef.setColumnUnit(               unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(7));

            return colDef;
         }
      };

      WEATHER_WIND_SPEED = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, WEATHER_WIND_SPEED_ID, SWT.TRAIL);

            colDef.setColumnCategory(           Messages.ColumnFactory_Category_Weather);

            colDef.setColumnLabel(              Messages.ColumnFactory_wind_speed_label);
            colDef.setColumnHeaderText(         UI.SYMBOL_WIND_WITH_SPACE + UI.UNIT_LABEL_SPEED);
            colDef.setColumnHeaderToolTipText(  Messages.ColumnFactory_wind_speed_tooltip);
            colDef.setColumnUnit(               UI.UNIT_LABEL_SPEED);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(11));

            return colDef;
         }
      };


      CUSTOM_TRACKS_TIME_SLICES = new TableColumnFactory() {
         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {
            //it is not used for custom tracks
            return null;
         }

         @Override
         public TableColumnDefinition createColumnCustomTrack(final ColumnManager columnManager,
                                                            final PixelConverter pixelConverter,
                                                            final CustomTrackDefinition customTrackDefinition) {

            final String colId = ColumnManager.CUSTOM_TRACKS_TIME_SLICES_ID + "_" + customTrackDefinition.getId();
            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, colId, SWT.TRAIL);

            colDef.setColumnCategory(Messages.ColumnFactory_Category_Custom_Tracks);

            colDef.setColumnLabel(customTrackDefinition.getName());
            String colHead = customTrackDefinition.getName();
            if(colHead.length() > 11) {
               colHead = customTrackDefinition.getName().substring(0, 11) + "...";
            }

            colDef.setColumnHeaderText(colHead + "[" + customTrackDefinition.getUnit() + "]");
            colDef.setColumnUnit(customTrackDefinition.getUnit());
            colDef.setColumnHeaderToolTipText(customTrackDefinition.getName());

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));
            colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(6));

            return colDef;
         }
      };
   }


// SET_FORMATTING_ON

   private static TableColumnFactory createColumn_Address(final String category,
                                                          final String columnID,
                                                          final String columnLabel) {

      return new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, columnID, SWT.LEAD);

            colDef.setColumnCategory(category);

            colDef.setColumnLabel(columnLabel);
            colDef.setColumnHeaderText(columnLabel);
            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));

            return colDef;
         }
      };
   }

   private static TableColumnFactory createColumn_Address_Trail(final String category,
                                                                final String columnID,
                                                                final String columnLabel) {

      return new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, columnID, SWT.TRAIL);

            colDef.setColumnCategory(category);

            colDef.setColumnLabel(columnLabel);
            colDef.setColumnHeaderText(columnLabel);
            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

            return colDef;
         }
      };
   }

   private static TableColumnFactory createColumn_Temperature(final String columnId,
                                                              final String symbol,
                                                              final String columnLabel,
                                                              final String columnHeaderToolTipText) {

      return new TableColumnFactory() {

         @Override
         public TableColumnDefinition createColumn(final ColumnManager columnManager,
                                                   final PixelConverter pixelConverter) {

            final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, columnId, SWT.TRAIL);

            final String unitLabel = symbol + UI.SPACE + UI.UNIT_LABEL_TEMPERATURE;

            colDef.setColumnCategory(Messages.ColumnFactory_Category_Weather);

            colDef.setColumnLabel(columnLabel);
            colDef.setColumnHeaderText(unitLabel);
            colDef.setColumnHeaderToolTipText(columnHeaderToolTipText);
            colDef.setColumnUnit(unitLabel);

            colDef.setDefaultColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
            colDef.setValueFormats(
                  ValueFormatSet.Number,
                  ValueFormat.NUMBER_1_1,
                  ValueFormat.NUMBER_1_1,
                  columnManager);

            return colDef;
         }
      };
   }

   /**
    * @param columnManager
    * @param pixelConverter
    *
    * @return Returns a {@link TableColumnDefinition}
    */
   public abstract TableColumnDefinition createColumn(ColumnManager columnManager, PixelConverter pixelConverter);

   public TableColumnDefinition createColumnCustomTrack(final ColumnManager columnManager,
                                                      final PixelConverter pixelConverter,
                                                      final CustomTrackDefinition custTD) {
      //default implementation doesn't do anything !!
      return null;
   }
}
