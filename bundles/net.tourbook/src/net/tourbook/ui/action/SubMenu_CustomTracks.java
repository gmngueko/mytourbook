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
package net.tourbook.ui.action;

import net.tourbook.Messages;
import net.tourbook.ui.ITourProvider2;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 */
public class SubMenu_CustomTracks extends Action implements IMenuCreator {

   private Menu                           _menu;

   private ActionRetrieveCustomTracksJson _action_RetrieveCustomTracksJsonData;
   private ActionRetrieveCustomTracksCsv  _action_RetrieveCustomTracksCsvData;
   private ActionEditCustomTracks       _action_EditCustomTracksData;

   private ITourProvider2                 _tourProvider;

   public SubMenu_CustomTracks(final ITourProvider2 tourViewer) {

      super(Messages.Tour_Action_Custom_Tracks, AS_DROP_DOWN_MENU);

      setMenuCreator(this);

      _tourProvider = tourViewer;

      _action_RetrieveCustomTracksJsonData = new ActionRetrieveCustomTracksJson(_tourProvider);
      _action_RetrieveCustomTracksCsvData = new ActionRetrieveCustomTracksCsv(_tourProvider);
      _action_EditCustomTracksData = new ActionEditCustomTracks(_tourProvider);
   }

   @Override
   public void dispose() {

      if (_menu != null) {
         _menu.dispose();
         _menu = null;
      }
   }

   private void fillMenu(final Menu menu) {

      new ActionContributionItem(_action_RetrieveCustomTracksJsonData).fill(menu, -1);
      new ActionContributionItem(_action_RetrieveCustomTracksCsvData).fill(menu, -1);
      new ActionContributionItem(_action_EditCustomTracksData).fill(menu, -1);
   }


   public ActionEditCustomTracks getActionDeleteCustomTracksData() {
      return _action_EditCustomTracksData;
   }

   public ActionRetrieveCustomTracksCsv getActionRetrieveCustomTracksCsvData() {
      return _action_RetrieveCustomTracksCsvData;
   }

   public ActionRetrieveCustomTracksJson getActionRetrieveCustomTracksJsonData() {
      return _action_RetrieveCustomTracksJsonData;
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
