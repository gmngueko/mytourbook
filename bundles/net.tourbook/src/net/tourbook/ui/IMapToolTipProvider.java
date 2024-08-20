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

import de.byteholder.geoclipse.mapprovider.MP;

import net.tourbook.common.util.HoveredAreaContext;

import org.eclipse.swt.graphics.Rectangle;

public interface IMapToolTipProvider {

   /**
    * @param mouseMovePositionX
    * @param mouseMovePositionY
    * @param worldPixelTopLeftViewport
    * @param mp
    * @param mapZoomLevel
    * @param tilePixelSize
    * @param isTourPaintMethodEnhanced
    * @param requestedObject
    *           Requested object which should be found in the hovered tile or <code>null</code>
    *           when an object not requested.
    *
    * @return Returns a hovered area context or <code>null</code> when an area is not hovered.
    */
   HoveredAreaContext getHoveredContext(int mouseMovePositionX,
                                        int mouseMovePositionY,
                                        Rectangle worldPixelTopLeftViewport,
                                        MP mp,
                                        int mapZoomLevel,
                                        int tilePixelSize,
                                        Object requestedObject);
}
