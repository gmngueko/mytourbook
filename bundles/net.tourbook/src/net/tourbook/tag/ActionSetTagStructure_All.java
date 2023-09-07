/*******************************************************************************
 * Copyright (C) 2005, 2023 Wolfgang Schramm and Contributors
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
package net.tourbook.tag;

import java.util.HashMap;

import javax.persistence.EntityManager;

import net.tourbook.Messages;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.data.TourTag;
import net.tourbook.database.TourDatabase;
import net.tourbook.tour.TourManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class ActionSetTagStructure_All extends Action implements IMenuCreator {

   private Menu _menu;

   private class ActionSetTagStructure extends Action {

      private int __expandType;

      private ActionSetTagStructure(final int expandType, final String name) {

         super(name, AS_CHECK_BOX);
         __expandType = expandType;
      }

      @Override
      public void run() {

         // check if the tour editor contains a modified tour
         if (TourManager.isTourEditorModified()) {
            return;
         }

         if (MessageDialog.openConfirm(
               Display.getCurrent().getActiveShell(),
               Messages.action_tag_set_all_confirm_title,
               Messages.action_tag_set_all_confirm_message) == false) {

            return;
         }

         final Runnable runnable = new Runnable() {

            private boolean _isModified;

            @Override
            public void run() {

               final EntityManager em = TourDatabase.getInstance().getEntityManager();
               try {

                  /*
                   * Update all tags which have not the current expand type
                   */

                  final HashMap<Long, TourTag> allTourTags = TourDatabase.getAllTourTags();
                  for (final TourTag tourTag : allTourTags.values()) {

                     if (tourTag.getExpandType() != __expandType) {

                        // set new expand type

                        final Long tagId = tourTag.getTagId();
                        final TourTag tagInDb = em.find(TourTag.class, tagId);

                        if (tagInDb != null) {

                           tagInDb.setExpandType(__expandType);

                           final TourTag savedEntity = TourDatabase.saveEntity(tagInDb, tagId, TourTag.class);

                           if (savedEntity != null) {
                              _isModified = true;
                           }
                        }
                     }
                  }

               } catch (final Exception e) {

                  StatusUtil.log(e);

               } finally {

                  em.close();
               }

               if (_isModified) {
                  TagManager.clearAllTagResourcesAndFireModifyEvent();
               }
            }
         };
         BusyIndicator.showWhile(Display.getCurrent(), runnable);
      }
   }

   public ActionSetTagStructure_All() {

      super(Messages.action_tag_set_all_tag_structures, AS_DROP_DOWN_MENU);

      setMenuCreator(this);
   }

   private void addActionToMenu(final Action action) {

      final ActionContributionItem item = new ActionContributionItem(action);
      item.fill(_menu, -1);
   }

   @Override
   public void dispose() {
      if (_menu != null) {
         _menu.dispose();
         _menu = null;
      }
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

            final Menu menu = (Menu) e.widget;

            // dispose old items
            for (final MenuItem menuItem : menu.getItems()) {
               menuItem.dispose();
            }

            /*
             * create all expand types
             */
            int typeIndex = 0;
            for (final int expandType : TagManager.EXPAND_TYPES) {

               final ActionSetTagStructure actionTagStructure = new ActionSetTagStructure(
                     expandType,
                     TagManager.EXPAND_TYPE_NAMES[typeIndex++]);

               addActionToMenu(actionTagStructure);
            }
         }
      });

      return _menu;
   }

}
