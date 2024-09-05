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
package net.tourbook.tourType;

public class TourTypeImageConfig {

   public int            imageScaling = TourTypeManager.DEFAULT_IMAGE_SCALING;

   public TourTypeColor  borderColor  = TourTypeManager.DEFAULT_BORDER_COLOR;
   public TourTypeBorder borderLayout = TourTypeManager.DEFAULT_BORDER_LAYOUT;
   public int            borderWidth  = TourTypeManager.DEFAULT_BORDER_WIDTH;

   public TourTypeColor  imageColor1  = TourTypeManager.DEFAULT_IMAGE_COLOR1;
   public TourTypeColor  imageColor2  = TourTypeManager.DEFAULT_IMAGE_COLOR2;
   public TourTypeLayout imageLayout  = TourTypeManager.DEFAULT_IMAGE_LAYOUT;
}
