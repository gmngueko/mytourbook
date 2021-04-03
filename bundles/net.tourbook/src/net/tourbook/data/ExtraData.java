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

/*
 * Extra additional data for TourTag
 * data like Maintenace info, etc...
 * those data are non searchable just extra informations
 */
public class ExtraData implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 7303438653005185156L;

   private TourTagMaintenance maintenanceInfo;

   public TourTagMaintenance getMaintenanceInfo() {
      return maintenanceInfo;
   }

   public void setMaintenanceInfo(final TourTagMaintenance maintenanceInfo) {
      this.maintenanceInfo = maintenanceInfo;
   }

}
