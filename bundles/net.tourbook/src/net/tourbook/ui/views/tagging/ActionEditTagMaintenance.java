/*******************************************************************************
 * Copyright (C) 2021 Wolfgang Gerais-Martial Ngueko
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
package net.tourbook.ui.views.tagging;

import java.util.HashMap;

import net.tourbook.Messages;
import net.tourbook.common.util.ITourViewer;
import net.tourbook.data.TourTag;
import net.tourbook.database.TourDatabase;
import net.tourbook.tag.Dialog_TourTag_Maintenance;
import net.tourbook.tag.TagMenuManager;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.UI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Action to edit maintennace {@link TourTag}
 */
public class ActionEditTagMaintenance extends Action {

   private ITourViewer _tourViewer;

   public ActionEditTagMaintenance(final ITourViewer tourViewer) {

      super(Messages.Action_Tag_EditMaintenance, AS_PUSH_BUTTON);

      _tourViewer = tourViewer;
   }

   void editTag(final Object viewerCellData) {

      String dlgMessage = UI.EMPTY_STRING;

      final TourTag[] finalTourTag = { null };

      /*
       * Open dialog
       */
      if (viewerCellData instanceof TVITaggingView_Tag) {

         final TVITaggingView_Tag tourTagItem = ((TVITaggingView_Tag) viewerCellData);

         final HashMap<Long, TourTag> allTourTags = TourDatabase.getAllTourTags();

         final TourTag tourTag = finalTourTag[0] = allTourTags.get(tourTagItem.getTagId());

         dlgMessage = NLS.bind(Messages.Dialog_TourTag_MaintenanceTag_Message, tourTag.getTagName());

         if (new Dialog_TourTag_Maintenance(
               Display.getCurrent().getActiveShell(),
               dlgMessage,
               tourTag).open() != Window.OK) {

            return;
         }

      } else {

         return;
      }

      /*
       * Update UI/model
       */
      BusyIndicator.showWhile(Display.getCurrent(), () -> {

         final ColumnViewer tagViewer = _tourViewer.getViewer();

         if (viewerCellData instanceof TVITaggingView_Tag) {

            // update model
            final TourTag tourTag = finalTourTag[0];
            TourDatabase.saveEntity(tourTag, tourTag.getTagId(), TourTag.class);

            // update UI
            final TVITaggingView_Tag tourTagItem = ((TVITaggingView_Tag) viewerCellData);
            tagViewer.update(tourTagItem, null);

         }
         // remove old tags from internal list
         TourDatabase.clearTourTags();
         TagMenuManager.updateRecentTagNames();

         TourManager.getInstance().clearTourDataCache();

         // fire modify event
         TourManager.fireEvent(TourEventId.TAG_STRUCTURE_CHANGED);
      });
   }

   /**
    * Edit selected tag/category
    */
   @Override
   public void run() {

      final ColumnViewer tagViewer = _tourViewer.getViewer();

      final Object firstElement = tagViewer.getStructuredSelection().getFirstElement();

      editTag(firstElement);
   }
}
