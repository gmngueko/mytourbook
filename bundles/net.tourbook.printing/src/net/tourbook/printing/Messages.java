/*******************************************************************************
 * Copyright (C) 2005, 2022 Wolfgang Schramm and Contributors
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
package net.tourbook.printing;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

   private static final String BUNDLE_NAME = "net.tourbook.printing.messages"; //$NON-NLS-1$

   public static String        Dialog_Print_Btn_Print;
   public static String        Dialog_Print_Chk_OpenFile;
   public static String        Dialog_Print_Chk_OpenFile_Tooltip;
   public static String        Dialog_Print_Chk_OverwriteFiles;
   public static String        Dialog_Print_Chk_OverwriteFiles_Tooltip;
   public static String        Dialog_Print_Chk_PrintMarkers;
   public static String        Dialog_Print_Chk_PrintMarkers_Tooltip;
   public static String        Dialog_Print_Chk_PrintNotes;
   public static String        Dialog_Print_Chk_PrintNotes_Tooltip;
   public static String        Dialog_Print_Dialog_Message;
   public static String        Dialog_Print_Dialog_Title;
   public static String        Dialog_Print_Dir_Dialog_Message;
   public static String        Dialog_Print_Dir_Dialog_Text;
   public static String        Dialog_Print_Error_Message;
   public static String        Dialog_Print_Error_Title;
   public static String        Dialog_Print_File_Dialog_Text;
   public static String        Dialog_Print_Group_Options;
   public static String        Dialog_Print_Group_Paper;
   public static String        Dialog_Print_Group_PdfFileName;
   public static String        Dialog_Print_Label_FileName;
   public static String        Dialog_Print_Label_FilePath;
   public static String        Dialog_Print_Label_Paper_Orientation;
   public static String        Dialog_Print_Label_Paper_Orientation_Portrait;
   public static String        Dialog_Print_Label_Paper_Orientation_Landscape;
   public static String        Dialog_Print_Label_Paper_Size;
   public static String        Dialog_Print_Label_Paper_Size_A4;
   public static String        Dialog_Print_Label_Paper_Size_Letter;
   public static String        Dialog_Print_Label_PrintFilePath;
   public static String        Dialog_Print_Lbl_PdfFilePath;
   public static String        Dialog_Print_Msg_FileAlreadyExists;
   public static String        Dialog_Print_Msg_FileNameIsInvalid;
   public static String        Dialog_Print_Msg_PathIsNotAvailable;
   public static String        Dialog_Print_Shell_Text;
   public static String        Dialog_Print_Txt_FilePath_Tooltip;

   public static String        Tour_Print_Altitude;
   public static String        Tour_Print_Average_Cadence;
   public static String        Tour_Print_Average_Pulse;
   public static String        Tour_Print_Calories;
   public static String        Tour_Print_Distance;
   public static String        Tour_Print_End_Location;
   public static String        Tour_Print_Highest_Altitude;
   public static String        Tour_Print_Maximum_Pulse;
   public static String        Tour_Print_Maximum_Speed;
   public static String        Tour_Print_Meters_Down;
   public static String        Tour_Print_Meters_Up;
   public static String        Tour_Print_No_Markers_Found;
   public static String        Tour_Print_PageTitle;
   public static String        Tour_Print_Personal;
   public static String        Tour_Print_Rest_Pulse;
   public static String        Tour_Print_Start;
   public static String        Tour_Print_Start_Location;
   public static String        Tour_Print_Time_Distance_Speed;
   public static String        Tour_Print_Tour;
   public static String        Tour_Print_Tour_Moving_Time;
   public static String        Tour_Print_Tour_Pausing_Time;
   public static String        Tour_Print_Tour_Time;
   public static String        Tour_Print_Tour_Markers;

   public static String        app_btn_browse;

   static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {}
}
