/*******************************************************************************
 * Copyright (C) 2018, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.tourChart;

/**
 * Fill graph background with color from this source.
 */
public enum GraphBackgroundSource {

   DEFAULT, // defined in the preferences Appearance/Colors

   HR_ZONE, //
   SWIMMING_STYLE, //

   /**
    * This is displayed when values are missing (interpolated), e.g. when there was not always a geo
    * position in the tour saved
    */
   INTERPOLATED_VALUES, //

}
