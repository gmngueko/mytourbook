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
package net.tourbook.ui.action;

import java.util.ArrayList;

import net.tourbook.Messages;
import net.tourbook.common.action.ActionOpenPrefDialog;
import net.tourbook.data.TourData;
import net.tourbook.data.TourType;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.PrefPageTourType_Definitions;
import net.tourbook.tour.TourManager;
import net.tourbook.tour.TourTypeMenuManager;
import net.tourbook.tourType.TourTypeImage;
import net.tourbook.ui.ITourProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class ActionSetTourTypeMenu extends Action implements IMenuCreator {

   private Menu  _menu;

   ITourProvider _tourProvider;

   private static class Action_SetTourType extends Action {

      private TourType      _tourType;
      private ITourProvider _tourProvider;

      private boolean       _isSaveTour;

      /**
       * @param tourType
       * @param tourProvider
       * @param isSaveTour
       *           when <code>true</code> the tour will be saved and a
       *           {@link TourManager#TOUR_CHANGED} event is fired, otherwise the
       *           {@link TourData} from the tour provider is only updated
       * @param isChecked
       */
      public Action_SetTourType(final TourType tourType,
                                final ITourProvider tourProvider,
                                final boolean isSaveTour,
                                final boolean isChecked) {

         super(tourType.getName(), AS_CHECK_BOX);

         if (isChecked == false) {

            // show image when tour type can be selected, disabled images look ugly on win
            final Image tourTypeImage = TourTypeImage.getTourTypeImage(tourType.getTypeId());
            setImageDescriptor(ImageDescriptor.createFromImage(tourTypeImage));
         }

         setChecked(isChecked);
         setEnabled(isChecked == false);

         _tourType = tourType;
         _tourProvider = tourProvider;
         _isSaveTour = isSaveTour;
      }

      @Override
      public void run() {

         final TourTypeMenuManager tourTypeMenuManager = new TourTypeMenuManager(_tourProvider);

         tourTypeMenuManager.setTourTypeIntoTour(_tourType, _isSaveTour);
      }
   }

   public ActionSetTourTypeMenu(final ITourProvider tourProvider) {

      super(Messages.App_Action_set_tour_type, AS_DROP_DOWN_MENU);

      setMenuCreator(this);

      _tourProvider = tourProvider;
   }

   /**
    * Adds all tour types to the menu manager
    *
    * @param menuMgr
    * @param tourProvider
    * @param isSaveTour
    *           when <code>true</code> the tour will be saved and a
    *           {@link TourManager#TOUR_CHANGED} event is fired, otherwise {@link TourData} from
    *           the tour provider is only modified
    */
   public static void fillMenu(final IMenuManager menuMgr,
                               final ITourProvider tourProvider,
                               final boolean isSaveTour) {

      // get tours which tour type should be changed
      final ArrayList<TourData> selectedTours = tourProvider.getSelectedTours();
      if (selectedTours == null) {
         return;
      }

      // get tour type which will be checked in the menu
      TourType checkedTourType = null;
      if (selectedTours.size() == 1) {
         checkedTourType = selectedTours.get(0).getTourType();
      }

      // add all tour types to the menu
      final ArrayList<TourType> tourTypes = TourDatabase.getAllTourTypes();

      for (final TourType tourType : tourTypes) {

         boolean isChecked = false;
         if (checkedTourType != null && checkedTourType.getTypeId() == tourType.getTypeId()) {
            isChecked = true;
         }

         final Action_SetTourType action = new Action_SetTourType(tourType, tourProvider, isSaveTour, isChecked);

         menuMgr.add(action);
      }

      /*
       * Add action to setup the tour type
       */
      menuMgr.add(new Separator());

      menuMgr.add(new ActionOpenPrefDialog(
            Messages.action_tourType_modify_tourTypes,
            PrefPageTourType_Definitions.ID));
   }

   private void addActionToMenu(final Action action, final Menu menu) {

      final ActionContributionItem item = new ActionContributionItem(action);
      item.fill(menu, -1);
   }

   @Override
   public void dispose() {
      if (_menu != null) {
         _menu.dispose();
         _menu = null;
      }
   }

   private void fillMenu(final Menu menu) {

      // get tours which tour type should be changed
      final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();
      if (selectedTours == null) {
         return;
      }

      // get tour type which will be checked in the menu
      TourType checkedTourType = null;
      if (selectedTours.size() == 1) {
         checkedTourType = selectedTours.get(0).getTourType();
      }

      // add all tour types to the menu
      final ArrayList<TourType> tourTypes = TourDatabase.getAllTourTypes();

      for (final TourType tourType : tourTypes) {

         boolean isChecked = false;
         if (checkedTourType != null && checkedTourType.getTypeId() == tourType.getTypeId()) {
            isChecked = true;
         }

         addActionToMenu(new Action_SetTourType(tourType, _tourProvider, true, isChecked), menu);
      }

      /*
       * Add action to setup the tour type
       */
      new MenuItem(menu, SWT.SEPARATOR);

      addActionToMenu(
            new ActionOpenPrefDialog(
                  Messages.action_tourType_modify_tourTypes,
                  PrefPageTourType_Definitions.ID),
            menu);

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
