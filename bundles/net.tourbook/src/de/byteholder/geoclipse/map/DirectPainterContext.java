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
package de.byteholder.geoclipse.map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class DirectPainterContext {

   /**
    * GC in the onPaint event
    */
   public GC        gc;

   /**
    * Contains the client area of the map without trimmings, this rectangle has the width and
    * height of the map image
    *
    */
   public Rectangle clientArea;

   /**
    * Viewport for the current map image in world pixel
    */
   public Rectangle mapViewport;
}
