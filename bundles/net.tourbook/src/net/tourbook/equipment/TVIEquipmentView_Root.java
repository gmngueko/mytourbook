/*******************************************************************************
 * Copyright (C) 2025, 2026 Wolfgang Schramm and Contributors
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
package net.tourbook.equipment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.tourbook.common.UI;
import net.tourbook.data.Equipment;
import net.tourbook.database.TourDatabase;

import org.eclipse.jface.viewers.TreeViewer;

public class TVIEquipmentView_Root extends TVIEquipmentView_Item {

   /**
    * @param equipmentViewer
    * @param isShowTours
    *           When <code>true</code> then the years/months and tours are displayed, otherwise
    *           then just the equipment structure is displayed, e.g. in the equipment tour filter
    */
   public TVIEquipmentView_Root(final TreeViewer equipmentViewer, final EquipmentViewerType equipmentType) {

      super(equipmentViewer, equipmentType);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void fetchChildren() {

      final Map<Long, TVIEquipmentView_Equipment> allEquipmentItems = new HashMap<>();

      final EntityManager em = TourDatabase.getInstance().getEntityManager();
      {
         if (em == null) {
            return;
         }

         final boolean isFilterEnabled = EquipmentManager.isEquipmentFilterEnabled();
         final int equipmentFilter_Retired = EquipmentManager.getEquipmentFilter_Retired();

         final boolean eqFilter_IsRetired = equipmentFilter_Retired == EquipmentManager.FILTER_RETIRED_IS_RETIRED;
         final boolean eqFilter_IsNotRetired = equipmentFilter_Retired == EquipmentManager.FILTER_RETIRED_IS_NOT_RETIRED;

         final boolean useFilter = isFilterEnabled && (eqFilter_IsRetired || eqFilter_IsNotRetired);

         String sqlFilter = UI.EMPTY_STRING;

         if (useFilter) {

//            if (eqFilter_IsRetired) {
//
               sqlFilter = " WHERE IsCollate = ? AND IsAutoRetired = ?";
//
//            } else {
//
//               sqlFilter = " WHERE IsCollate = ? OR (IsCollate = ? AND IsAutoRetired = ?)";
//            }
         }

         final String sql = UI.EMPTY_STRING

               + "SELECT" + NL //                                                      //$NON-NLS-1$
               + " Equipment" + NL //                                                  //$NON-NLS-1$
               + " FROM " + Equipment.class.getSimpleName() + " AS Equipment" + NL//   //$NON-NLS-1$ //$NON-NLS-2$

               + sqlFilter;

         final Query query = em.createQuery(sql);

         if (useFilter) {

            boolean isRetired = false;

            if (eqFilter_IsRetired) {

               isRetired = true;
            }

            /**
             * Parameter 1 is needed to fix: java.sql.SQLSyntaxErrorException: Comparisons between
             * 'BOOLEAN' and 'INTEGER' are not supported. Types must be comparable. String types
             * must also have matching collation. If collation does not match, a possible solution
             * is to cast operands to force them to the default collation (e.g. SELECT tablename
             * FROM sys.systables WHERE CAST(tablename AS VARCHAR(128)) = 'T1')
             */
            query.setParameter(1, true);
            query.setParameter(2, isRetired);
         }

         final TreeViewer equipmentViewer = getEquipmentViewer();
         final List<Equipment> allEquipments = query.getResultList();

         /*
          * Create all equipment top items
          */
         for (final Equipment equipment : allEquipments) {

            final TVIEquipmentView_Equipment equipmentItem = new TVIEquipmentView_Equipment(

                  equipmentViewer,
                  equipment,
                  getViewerType());

            addChild(equipmentItem);

            allEquipmentItems.put(equipment.getEquipmentId(), equipmentItem);
         }
      }
      em.close();

      loadSummarizedValues_Equipment(allEquipmentItems);
   }
}
