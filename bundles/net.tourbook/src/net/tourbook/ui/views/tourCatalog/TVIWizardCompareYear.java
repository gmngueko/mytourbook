/*******************************************************************************
 * Copyright (C) 2005, 2020 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourCatalog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.TreeViewerItem;
import net.tourbook.database.TourDatabase;

public class TVIWizardCompareYear extends TVIWizardCompareItem {

   int tourYear;

   TVIWizardCompareYear(final TVIWizardCompareItem parentItem) {
      setParentItem(parentItem);
   }

   @Override
   protected void fetchChildren() {

      final ArrayList<TreeViewerItem> children = new ArrayList<TreeViewerItem>();
      setChildren(children);

      final StringBuilder sb = new StringBuilder();

      sb.append("SELECT"); //$NON-NLS-1$

      sb.append(" startYear, "); //$NON-NLS-1$
      sb.append(" startMonth "); //$NON-NLS-1$

      sb.append(" FROM " + TourDatabase.TABLE_TOUR_DATA); //$NON-NLS-1$

      sb.append(" WHERE startYear=?"); //$NON-NLS-1$

      sb.append(" GROUP BY startYear, startMonth"); //$NON-NLS-1$
      sb.append(" ORDER BY startMonth"); //$NON-NLS-1$

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         final PreparedStatement statement = conn.prepareStatement(sb.toString());
         statement.setInt(1, tourYear);

         final ResultSet result = statement.executeQuery();
         while (result.next()) {

            final TVIWizardCompareMonth monthItem = new TVIWizardCompareMonth(this);
            children.add(monthItem);

            final int dbYear = result.getInt(1);
            final int dbMonth = result.getInt(2);

            monthItem.treeColumn = calendar8//
                  .withYear(dbYear)
                  .withMonth(dbMonth)
                  .format(TimeTools.Formatter_Month);

            monthItem.tourYear = dbYear;
            monthItem.tourMonth = dbMonth;
         }

      } catch (final SQLException e) {
         net.tourbook.ui.UI.showSQLException(e);
      }
   }
}
