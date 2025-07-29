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
package net.tourbook.ui.views.tourBook;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * The action to delete a tour is displayed in a sub menu that it is not accidentally be run
 */
public class ActionDeleteTourMenu2 extends Action implements IMenuCreator {

   private Menu             _menu;

   private ActionDeleteTour _actionDeleteTour;

   public ActionDeleteTourMenu2(final TourBookView tourBookView) {

      super(Messages.Tour_Book_Action_delete_selected_tours_menu, AS_DROP_DOWN_MENU);

      setImageDescriptor(TourbookPlugin.getImageDescriptor(Images.App_Trash));

      setMenuCreator(this);

      _actionDeleteTour = new ActionDeleteTour(tourBookView);
   }

   @Override
   public void dispose() {
      if (_menu != null) {
         _menu.dispose();
         _menu = null;
      }
   }

   private void fillMenu(final Menu menu) {

      final ActionContributionItem item = new ActionContributionItem(_actionDeleteTour);
      item.fill(menu, -1);
   }

   @Override
   public Menu getMenu(final Control parent) {
      return null;
   }

   @Override
   public Menu getMenu(final Menu parent) {

      dispose();

      _menu = new Menu(parent);

      // Add listener to repopulate the menu each time
      _menu.addMenuListener(new MenuAdapter() {
         @Override
         public void menuShown(final MenuEvent e) {

            // dispose old menu items
            for (final MenuItem menuItem : ((Menu) e.widget).getItems()) {
               menuItem.dispose();
            }

            fillMenu(_menu);
         }
      });

      return _menu;
   }

}
