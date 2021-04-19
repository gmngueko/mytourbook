/*******************************************************************************
 * Copyright (C) 2005, 2019 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.rawData;

import net.tourbook.Messages;
import net.tourbook.ui.ITourProvider2;
import net.tourbook.ui.ITourProviderByID;
import net.tourbook.ui.action.ActionComputeDistanceValuesFromGeoposition;
import net.tourbook.ui.action.ActionMultiplyCaloriesBy1000;
import net.tourbook.ui.action.ActionRetrieveWeatherData;
import net.tourbook.ui.action.ActionRetrieveWeatherOwmData;
import net.tourbook.ui.action.ActionSetTimeZone;
import net.tourbook.ui.action.SubMenu_Cadence;
import net.tourbook.ui.action.SubMenu_CustomTracks;
import net.tourbook.ui.action.SubMenu_Elevation;
import net.tourbook.ui.action.SubMenu_Weather;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SubMenu_AdjustTourValues extends Action implements IMenuCreator {

   private SubMenu_CustomTracks                       _subMenu_CustomTracks;

   private SubMenu_Cadence                            _subMenu_Cadence;
   private SubMenu_Elevation                          _subMenu_Elevation;
   private SubMenu_Weather                            _subMenu_Weather;

   private ActionComputeDistanceValuesFromGeoposition _action_ComputeDistanceValuesFromGeoposition;
   private ActionMultiplyCaloriesBy1000               _action_MultiplyCaloriesBy1000;
   private ActionSetTimeZone                          _action_SetTimeZone;

   /*
    * UI controls
    */
   private Menu _menu;

   public SubMenu_AdjustTourValues(final ITourProvider2 tourProvider, final ITourProviderByID tourProviderById) {

      super(Messages.Tour_Action_AdjustTourValues, AS_DROP_DOWN_MENU);

      setMenuCreator(this);

      _subMenu_CustomTracks = new SubMenu_CustomTracks(tourProvider);
      _subMenu_Cadence = new SubMenu_Cadence(tourProvider);
      _subMenu_Elevation = new SubMenu_Elevation(tourProvider, tourProviderById);
      _subMenu_Weather = new SubMenu_Weather(tourProvider);

      _action_ComputeDistanceValuesFromGeoposition = new ActionComputeDistanceValuesFromGeoposition(tourProvider);
      _action_MultiplyCaloriesBy1000 = new ActionMultiplyCaloriesBy1000(tourProvider);
      _action_SetTimeZone = new ActionSetTimeZone(tourProvider);
   }

   @Override
   public void dispose() {

      if (_menu != null) {
         _menu.dispose();
         _menu = null;
      }
   }

   private void fillMenu(final Menu menu) {

      new ActionContributionItem(_action_ComputeDistanceValuesFromGeoposition).fill(menu, -1);
      new ActionContributionItem(_action_MultiplyCaloriesBy1000).fill(menu, -1);
      new ActionContributionItem(_action_SetTimeZone).fill(menu, -1);

      new ActionContributionItem(_subMenu_CustomTracks).fill(menu, -1);
      new ActionContributionItem(_subMenu_Cadence).fill(menu, -1);
      new ActionContributionItem(_subMenu_Elevation).fill(menu, -1);
      new ActionContributionItem(_subMenu_Weather).fill(menu, -1);
   }

   public ActionRetrieveWeatherData getActionRetrieveWeatherData() {
      return _subMenu_Weather.getActionRetrieveWeatherData();
   }

   public ActionRetrieveWeatherOwmData getActionRetrieveWeatherOwmData() {
      return _subMenu_Weather.getActionRetrieveWeatherOwmData();
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
