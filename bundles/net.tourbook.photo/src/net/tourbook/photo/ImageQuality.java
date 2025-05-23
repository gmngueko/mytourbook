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
package net.tourbook.photo;

public enum ImageQuality {

   /**
    * This image has EXIF thumb size/quality
    */
   THUMB,

   /**
    * Image is in HQ (high quality) size
    */
   HQ,

   /**
    * Thumb image is in HQ (high quality) size, it is used in e.g. 2D map
    */
   THUMB_HQ,

   /**
    * Thumb image is in HQ (high quality) size and adjusted, e.g. cropped or tonality modified, it
    * is used in e.g. 2D map
    */
   THUMB_HQ_ADJUSTED,

   /**
    * Image in original size
    */
   ORIGINAL,
}
