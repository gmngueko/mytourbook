/*******************************************************************************
 * Copyright (C) 2021 Gervais-Martial Ngueko
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

import java.io.Serializable;

import net.tourbook.common.UI;

public class MaintenanceEvent implements Cloneable, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = -3716703585072557410L;

   public long               eventEpochTime   = 0;
   public float              cost             = 0;
   public String             notes            = UI.EMPTY_STRING;
   public float              metersTotalUsed  = 0;
   public long               secondsTotalUsed = 0;
   public int                numTours         = 0;
}
