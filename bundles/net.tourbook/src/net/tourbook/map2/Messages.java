/*******************************************************************************
 * Copyright (C) 2005, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.map2;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

   private static final String BUNDLE_NAME = "net.tourbook.map2.messages"; //$NON-NLS-1$

   public static String        App_Action_Apply;

   public static String        Clipboard_Content_MapLocation;

   public static String        Default_Label_NewTourMarker;

   public static String        graph_label_gradient_unit;
   public static String        graph_label_heartbeat_unit;

   /*
    * These image names are translatable, any other images are constants and NOT translated
    */
   public static String Image_Action_ShowSliderInMap;
   public static String Image_Action_ShowSliderInMap_Left;
   public static String Image_Action_ShowStartEndInMap;

   public static String Image_Map_MarkerSliderLeft;
   public static String Image_Map_MarkerSliderRight;
   public static String Image_Map_TourEndMarker;
   public static String Image_Map_TourStartMarker;

   public static String LegendColor_Dialog_Check_LiveUpdate;
   public static String LegendColor_Dialog_Check_LiveUpdate_Tooltip;

   public static String legendcolor_dialog_chk_max_value_text;
   public static String legendcolor_dialog_chk_max_value_tooltip;
   public static String legendcolor_dialog_chk_min_value_text;
   public static String legendcolor_dialog_chk_min_value_tooltip;
   public static String legendcolor_dialog_error_max_greater_min;
   public static String legendcolor_dialog_group_minmax_brightness;
   public static String legendcolor_dialog_group_minmax_value;
   public static String legendcolor_dialog_max_brightness_label;
   public static String legendcolor_dialog_max_brightness_tooltip;
   public static String legendcolor_dialog_min_brightness_label;
   public static String legendcolor_dialog_min_brightness_tooltip;
   public static String legendcolor_dialog_title;
   public static String legendcolor_dialog_title_message;
   public static String legendcolor_dialog_title_name;
   public static String legendcolor_dialog_txt_max_value;
   public static String legendcolor_dialog_txt_min_value;

   public static String map_action_reload_map;
   public static String map_action_save_default_position;
   public static String map_action_set_default_position;

   public static String Map_Action_AutoSelectPhoto;
   public static String Map_Action_CenterMapToMapPointPosition;
   public static String Map_Action_CopyLocation;
   public static String Map_Action_CopyTonality;
   public static String Map_Action_CopyTonality_StatusLine;
   public static String Map_Action_CopyTonality_Tooltip;
   public static String Map_Action_CreateTourMarkerFromMap;
   public static String Map_Action_DeselectPhoto;
   public static String Map_Action_DeselectPhoto_Tooltip;
   public static String Map_Action_EditPhotoLabel;
   public static String Map_Action_EditPhotoLabel_Dialog_Message;
   public static String Map_Action_EditPhotoLabel_Dialog_Title;
   public static String Map_Action_EditTourMarker;
   public static String Map_Action_Export_Map_Clipboard_Copied_Info;
   public static String Map_Action_Export_Map_View;
   public static String Map_Action_Export_Map_View_Clipboard;
   public static String Map_Action_Export_Map_View_Image;
   public static String Map_Action_Export_Map_View_Image_Tooltip;
   public static String Map_Action_ExternalApp_DoubleClickStart;
   public static String Map_Action_ExternalApp_OpenPhotoImage;
   public static String Map_Action_ExternalApp_Setup;
   public static String Map_Action_GeoMarkerPosition_RemoveAll;
   public static String Map_Action_GeoMarkerPosition_RemoveAll_Tooltip;
   public static String Map_Action_GeoMarkerPosition_SetIntoMarker;
   public static String Map_Action_GeoMarkerPosition_SetIntoMarker_Tooltip;
   public static String Map_Action_GeoPositions_Set;
   public static String Map_Action_GeoPositions_SetInto;
   public static String Map_Action_GeoPositions_Set_End;
   public static String Map_Action_GeoPositions_Set_Start;
   public static String Map_Action_GeoPositions_Tooltip2;
   public static String Map_Action_GotoLocation;
   public static String Map_Action_LookupCommonLocation;
   public static String Map_Action_ManageMapProviders;
   public static String Map_Action_PasteTonality;
   public static String Map_Action_PasteTonality_Tooltip;
   public static String Map_Action_PhotoFilter2_Tooltip;
   public static String Map_Action_POI;
   public static String Map_Action_POI_Tooltip;
   public static String Map_Action_ReplacePhotoGeoPosition;
   public static String Map_Action_ReplacePhotoGeoPosition_Tooltip;
   public static String Map_Action_SearchTourByLocation;
   public static String Map_Action_SearchTourByLocation_Tooltip;
   public static String Map_Action_ShowAllFilteredPhotos_Tooltip;
   public static String Map_Action_ShowEarthMap;
   public static String Map_Action_ShowMapPoints_Tooltip;
   public static String Map_Action_ShowOnlyThisTour;
   public static String Map_Action_ShowPhotos_Tooltip;
   public static String Map_Action_ShowPhotoAnnotations;
   public static String Map_Action_ShowPhotoHistogram;
   public static String Map_Action_ShowPhotoImage;
   public static String Map_Action_ShowPhotoLabel;
   public static String Map_Action_ShowPhotoRating;
   public static String Map_Action_ShowTourInfoInMap;
   public static String Map_Action_ShowTourWeatherInMap;
   public static String Map_Action_ShowValuePoint;
   public static String Map_Action_SynchWith_TourLocations;
   public static String Map_Action_SynchWith_TourPosition;
   public static String Map_Action_SynchWith_ValuePoint;
   public static String Map_Action_SynchWithSlider_Centered;
   public static String Map_Action_SyncPhotoWithMap_Tooltip;
   public static String Map_Action_Zoom_CenteredBy_Map_Tooltip;
   public static String Map_Action_Zoom_CenteredBy_Mouse_Tooltip;
   public static String Map_Action_Zoom_CenteredBy_Tour_Tooltip;
   public static String Map_Action_ZoomInToTheMapPointPosition;

   public static String Map_Label_ValuePoint_Title;

   public static String Map_POI_MapLocation;

   public static String Map_Properties_ShowGeoGrid;
   public static String Map_Properties_ShowTileBorder;
   public static String Map_Properties_ShowTileInfo;

   public static String map_action_show_legend_in_map;
   public static String map_action_show_scale_in_map;
   public static String map_action_show_slider_in_legend;
   public static String map_action_show_slider_in_map;
   public static String map_action_show_start_finish_in_map;
   public static String map_action_show_tour_in_map;
   public static String map_action_synch_with_tour;
   public static String map_action_tour_color_altitude_tooltip;
   public static String map_action_tour_color_gradient_tooltip;
   public static String map_action_tour_color_pace_tooltip;
   public static String map_action_tour_color_power_tooltip;
   public static String map_action_tour_color_pulse_tooltip;
   public static String map_action_tour_color_speed_tooltip;
   public static String map_action_zoom_in;
   public static String map_action_zoom_level_centered_tour;
   public static String map_action_zoom_level_default;
   public static String map_action_zoom_level_x_value;
   public static String map_action_zoom_out;
   public static String map_action_zoom_show_entire_tour;

   public static String map_dlg_dim_warning_message;
   public static String map_dlg_dim_warning_title;
   public static String map_dlg_dim_warning_toggle_message;

   public static String statusLine_mapInfo_data;
   public static String statusLine_mapInfo_defaultText;
   public static String statusLine_mapInfo_pattern;
   public static String statusLine_mapInfo_tooltip;

   public static String Tour_Action_RunDyn_StepLength_Tooltip;
   public static String Tour_Action_ShowHrZones_Tooltip;

   public static String Dialog_ExportImage_Group_Image;
   public static String Dialog_ExportImage_Label_ImageFormat;
   public static String Dialog_ExportImage_Label_ImageQuality;
   public static String Dialog_ExportImage_Label_ImageQuality_Tooltip;
   public static String Dialog_ExportImage_Title;
   public static String Dialog_ExportImage_Message;

   public static String StatusLine_Message_CopiedLatitudeLongitude;

   static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {}
}
