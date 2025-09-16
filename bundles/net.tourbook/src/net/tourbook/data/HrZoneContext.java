/*******************************************************************************
 * Copyright (C) 2005, 2025 Wolfgang Schramm and Contributors
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

public class HrZoneContext implements Serializable {

   private static final long serialVersionUID = 1L;

   /**
    * Age in years
    */
   public int                age;
   public int                hrMax;

   public float[]            zoneMinBpm;
   public float[]            zoneMaxBpm;

   /**
    * Set HR zones, age and max HR
    *
    * @param zoneMinBpm
    * @param zoneMaxBpm
    * @param age
    * @param hrMax
    */
   public HrZoneContext(final float[] zoneMinBpm,
                        final float[] zoneMaxBpm,
                        final int age,
                        final int hrMax) {

      this.zoneMinBpm = zoneMinBpm;
      this.zoneMaxBpm = zoneMaxBpm;

      this.age = age;
      this.hrMax = hrMax;
   }

}
