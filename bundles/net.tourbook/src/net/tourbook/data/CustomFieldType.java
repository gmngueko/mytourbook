/*******************************************************************************
 * Copyright (C) 2022 Gervais-Martial Ngueko
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
package net.tourbook.data;

public enum CustomFieldType {
   /**
    * A sensor type is not defined
    */
   NONE,

   FIELD_NUMBER, //a float number
   FIELD_DURATION, //duration in seconds
   FIELD_DATE, //timestamp in seconds since epoch in a string
   FIELD_STRING, //a random text
}