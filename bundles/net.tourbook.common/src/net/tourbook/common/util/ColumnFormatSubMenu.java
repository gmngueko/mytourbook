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
package net.tourbook.common.util;

import net.tourbook.common.Messages;
import net.tourbook.common.formatter.FormatManager;
import net.tourbook.common.formatter.ValueFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 */
public class ColumnFormatSubMenu extends Action implements IMenuCreator {

   private Menu             _contextMenu;
   private ColumnManager    _columnManager;

   private ColumnDefinition _colDef;

   /**
    *
    */
   private class ActionAvailableFormats extends Action implements IMenuCreator {

      private Menu    __formatsMenu;
      private boolean __isDetailFormat;

      public ActionAvailableFormats(final String text, final boolean isDetailFormat) {

         super(text, AS_DROP_DOWN_MENU);

         __isDetailFormat = isDetailFormat;

         setMenuCreator(this);
      }

      @Override
      public void dispose() {

         if (__formatsMenu != null) {
            __formatsMenu.dispose();
            __formatsMenu = null;
         }
      }

      @Override
      public Menu getMenu(final Control parent) {
         return null;
      }

      @Override
      public Menu getMenu(final Menu parent) {

         dispose();

         __formatsMenu = new Menu(parent);

         // Add listener to repopulate the menu each time
         __formatsMenu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(final MenuEvent e) {

               final Menu menu = (Menu) e.widget;

               // dispose old items
               final MenuItem[] items = menu.getItems();
               for (final MenuItem item : items) {
                  item.dispose();
               }

               // add actions
               createFormatActions(__formatsMenu, __isDetailFormat);
            }
         });

         return __formatsMenu;
      }
   }

   private class ActionColumnFormat extends Action {

      private ColumnDefinition __colDef;
      private boolean          __isDetail;
      private ValueFormat      __valueFormat;

      public ActionColumnFormat(final ColumnDefinition colDef,
                                final ValueFormat valueFormat,
                                final boolean isDetailFormat) {

         super(null, AS_CHECK_BOX);

         setText(FormatManager.getValueFormatterName(valueFormat));

         __colDef = colDef;
         __valueFormat = valueFormat;
         __isDetail = isDetailFormat;
      }

      @Override
      public void run() {

         _columnManager.action_SetValueFormatter(__colDef, __valueFormat, __isDetail);
      }
   }

   public ColumnFormatSubMenu(final Menu contextMenu, final ColumnDefinition colDef, final ColumnManager columnManager) {

      _contextMenu = contextMenu;
      _columnManager = columnManager;

      _colDef = colDef;

      final ValueFormat valueFormat_Category = colDef.getValueFormat_Category();
      final ValueFormat valueFormat_Detail = colDef.getValueFormat_Detail();

      if (valueFormat_Category == null && valueFormat_Detail != null) {

         // only the detail (tour) can be formatted

         final String actionDetailText = NLS.bind(
               Messages.Action_ColumnManager_ValueFormatter_Tour2,
               FormatManager.getValueFormatterName(valueFormat_Detail));

         addActionToMenu(_contextMenu, new ActionAvailableFormats(actionDetailText, true));

      } else {

         if (valueFormat_Category != null) {

            final String actionCategoryText = NLS.bind(
                  Messages.Action_ColumnManager_ValueFormatter_Category2,
                  FormatManager.getValueFormatterName(valueFormat_Category));

            addActionToMenu(_contextMenu, new ActionAvailableFormats(actionCategoryText, false));
         }

         if (valueFormat_Detail != null) {

            final String actionDetailText = NLS.bind(
                  Messages.Action_ColumnManager_ValueFormatter_Detail2,
                  FormatManager.getValueFormatterName(valueFormat_Detail));

            addActionToMenu(_contextMenu, new ActionAvailableFormats(actionDetailText, true));
         }
      }
   }

   private void addActionToMenu(final Menu menu, final Action action) {

      final ActionContributionItem item = new ActionContributionItem(action);
      item.fill(menu, -1);
   }

   private void createFormatActions(final Menu menu, final boolean isDetailFormat) {

      /*
       * Create action for each format
       */
      for (final ValueFormat valueFormat : _colDef.getAvailableFormatter()) {

         final ActionColumnFormat action = new ActionColumnFormat(_colDef, valueFormat, isDetailFormat);

         final ValueFormat currentFormat = isDetailFormat //
               ? _colDef.getValueFormat_Detail()
               : _colDef.getValueFormat_Category();

         final boolean isCurrentFormat = currentFormat == valueFormat;

         // check current format
         action.setChecked(isCurrentFormat);

         // disable current format
         action.setEnabled(isCurrentFormat == false);

         addActionToMenu(menu, action);
      }
   }

   @Override
   public void dispose() {

      if (_contextMenu != null) {
         _contextMenu.dispose();
         _contextMenu = null;
      }
   }

   @Override
   public Menu getMenu(final Control parent) {
      return null;
   }

   @Override
   public Menu getMenu(final Menu parent) {
      return null;
   }

}
