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
package net.tourbook.device.garmin;

public interface IPreferences {

   String IS_COMPUTE_PAUSED_TIME           = "IS_COMPUTE_PAUSED_TIME";       //$NON-NLS-1$
   String IS_IGNORE_SPEED_VALUES           = "IS_IGNORE_SPEED_VALUES";       //$NON-NLS-1$
   String IS_IMPORT_INTO_DESCRIPTION_FIELD = "isImportIntoDescriptionField"; //$NON-NLS-1$
   String IS_IMPORT_INTO_TITLE_FIELD       = "isImportIntoTitleField";       //$NON-NLS-1$
   String IS_TITLE_IMPORT_ALL              = "isTitleImportAll";             //$NON-NLS-1$
   String NUMBER_OF_TITLE_CHARACTERS       = "numberOfTitleCharacters";      //$NON-NLS-1$
}
