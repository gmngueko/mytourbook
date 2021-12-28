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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.tourbook.Messages;
import net.tourbook.data.CustomTrackDefinition;
import net.tourbook.data.TourData;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider2;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ActionDeleteCustomTracks extends Action {
   public static final String          DIALOG_TITLE              = "Delete Custom Tracks from Tours";           //$NON-NLS-1$
   public static final String          DIALOG_MSG                = "Delete Custom Tracks";                      //$NON-NLS-1$
   public static final String          TRACKLIST_TITLE           = "List of Custom Tracks";                     //$NON-NLS-1$
   public static final String          DIALOG_OPEN_TITLE         = "Custom Tracks Delete";                      //$NON-NLS-1$
   public static final String          DIALOG_OPEN_MSG           = "No Tracks to delete";                       //$NON-NLS-1$
   public static final String          MENU_NAME                 = "&Delete Custom Tracks from tours...";       //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_NAME    = "List Custom Tracks";                        //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_TOOLTIP = "List Custom Tracks from selected tours..."; //$NON-NLS-1$
   public static final String          COL0_NAME                 = "Selected";                                  //$NON-NLS-1$
   public static final String          COL1_NAME                 = "Name";                                      //$NON-NLS-1$
   public static final String          COL2_NAME                 = "Count";                                     //$NON-NLS-1$
   public static final String          CHECKBOX_TAG              = "CHECK_BOX";                                 //$NON-NLS-1$

   private TreeMap<String, TrackEntry> trackList         = new TreeMap<>();

   private final ITourProvider2 _tourProvider;

   private class CustomTracksDeleteSettingsDialog extends TitleAreaDialog {

      private Composite _containerTrackList;
      private Table     _tableTracks;
      private String[]  _titleTracks = { COL0_NAME, COL1_NAME, COL2_NAME };

      private Button    buttonLoadTracks;

      private Shell     shell;

      public CustomTracksDeleteSettingsDialog(final Shell parentShell) {
         super(parentShell);
         shell = parentShell;
      }

      @Override
      public void create() {
         super.create();
         setTitle(DIALOG_TITLE);
         setMessage(DIALOG_MSG, IMessageProvider.INFORMATION);
      }

      @Override
      protected Control createDialogArea(final Composite parent) {
         final Composite area = (Composite) super.createDialogArea(parent);
         final Composite container = new Composite(area, SWT.NONE);
         container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         final GridLayout layout = new GridLayout(1, false);
         container.setLayout(layout);

         //Table List of Events
         _containerTrackList = new Composite(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerTrackList);
         GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_containerTrackList);
         {
            final Label label = new Label(_containerTrackList, SWT.NONE);
            label.setText(TRACKLIST_TITLE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
            final Color headerColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            _tableTracks = new Table(_containerTrackList, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
            _tableTracks.setLinesVisible(true);
            _tableTracks.setHeaderVisible(true);
            _tableTracks.setHeaderBackground(headerColor);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.heightHint = 100;
            _tableTracks.setLayoutData(data);

            for (int i = 0; i < _titleTracks.length; i++) {
               final TableColumn column = new TableColumn(_tableTracks, SWT.RIGHT);
               column.setText(_titleTracks[i]);
               _tableTracks.getColumn(i).pack();
            }
            _containerTrackList.pack();

            //show selected custom tracks list for selected tours info in the UI
            //which makes re-create easy (select->delete->update -> create)
            _tableTracks.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  final TableItem[] selection = _tableTracks.getSelection();
                  if (selection.length > 0) {
                     //TODO
                  }

               }
            });

            buttonLoadTracks = new Button(_containerTrackList, SWT.NONE);
            buttonLoadTracks.setText(BUTTON_LOADTRACKS_NAME);
            buttonLoadTracks.setToolTipText(BUTTON_LOADTRACKS_TOOLTIP);
            buttonLoadTracks.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();
                     if (selectedTours == null || selectedTours.isEmpty()) {

                        // a tour is not selected
                        MessageDialog.openInformation(
                              shell,
                              DIALOG_OPEN_TITLE,
                              Messages.UI_Label_TourIsNotSelected);

                        return;
                     }
                     //Build list of track name and count
                     for (final TourData tour : selectedTours) {
                        if (tour.customTracksDefinition != null && !tour.customTracksDefinition.isEmpty()) {
                           for (final Entry<String, CustomTrackDefinition> custTrackDefEntry : tour.customTracksDefinition.entrySet()) {
                              if (trackList.containsKey(custTrackDefEntry.getValue().getName())) {
                                 trackList.get(custTrackDefEntry.getValue().getName()).count += 1;
                              } else {
                                 final TrackEntry newEntry = new TrackEntry();
                                 newEntry.name = custTrackDefEntry.getValue().getName();
                                 newEntry.count = 1;
                                 trackList.put(custTrackDefEntry.getValue().getName(), newEntry);
                              }
                           }
                        }
                     }

                     //now add item to table list
                     for (final Entry<String, TrackEntry> trackListEntry : trackList.entrySet()) {
                        final TableItem itemEvent = new TableItem(_tableTracks, SWT.NONE);
                        itemEvent.setText(1, trackListEntry.getKey());
                        itemEvent.setText(2, String.valueOf(trackListEntry.getValue().count));
                        final TableEditor editor = new TableEditor(_tableTracks);
                        final Button buttonCheck = new Button(_tableTracks, SWT.CHECK);
                        buttonCheck.setData(trackListEntry.getValue());
                        itemEvent.setData(CHECKBOX_TAG, buttonCheck);

                        buttonCheck.addSelectionListener(new SelectionAdapter() {
                           @Override
                           public void widgetSelected(final SelectionEvent e) {
                              final Button button = (Button) e.widget;
                              final Object data = button.getData();
                              if (data != null) {
                                 final TrackEntry cData = (TrackEntry) data;
                                 if (button.getSelection()) {
                                    //System.out.println(" Maintenance check for True:" + button.getData());
                                    cData.isSelected = true;
                                 } else {
                                    //System.out.println(" Maintenance check for False:" + button.getData());
                                    cData.isSelected = false;
                                 }
                              }
                           }
                        });
                        buttonCheck.pack();
                        editor.minimumWidth = buttonCheck.getSize().x;
                        editor.horizontalAlignment = SWT.LEFT;
                        editor.setEditor(buttonCheck, itemEvent, 0);
                     }
                     break;
                  }
               }
            });
         }

         return area;
      }

      @Override
      protected boolean isResizable() {
         return true;
      }

      @Override
      protected void okPressed() {
         saveInput();
         super.okPressed();
      }

      // save content of the Text fields because they get disposed
      // as soon as the Dialog closes
      private void saveInput() {

      }

   }

   public class TrackEntry {
      String name;
      int    count = 0;
      Boolean isSelected = false;
   }

   public ActionDeleteCustomTracks(final ITourProvider2 tourProvider) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider = tourProvider;

      setText(MENU_NAME);
   }

   public boolean DeleteCustomTracksfromTour(final TourData tour, final TreeMap<String, TrackEntry> trackList2) {
      boolean isModified = false;
      if (tour.customTracksDefinition != null && !tour.customTracksDefinition.isEmpty()) {
         final Iterator<Entry<String, CustomTrackDefinition>> iteratorCustTrackDef = tour.customTracksDefinition.entrySet().iterator();
         while (iteratorCustTrackDef.hasNext()) {
            final Map.Entry<String, CustomTrackDefinition> pairCustTrackDef = iteratorCustTrackDef.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            if (trackList2.containsKey(pairCustTrackDef.getValue().getName())) {
               if (trackList2.get(pairCustTrackDef.getValue().getName()).isSelected) {
                  tour.clear_CustomTracks(pairCustTrackDef.getValue().getId());
                  iteratorCustTrackDef.remove(); // avoids a ConcurrentModificationException
                  isModified = true;
               }
            }
         }
      }
      return isModified;
   }

   @Override
   public void run() {

      // check if the tour editor contains a modified tour
      if (TourManager.isTourEditorModified()) {
         return;
      }

      final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();

      final Shell shell = Display.getCurrent().getActiveShell();
      if (selectedTours == null || selectedTours.isEmpty()) {

         // a tour is not selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }
      trackList.clear();
      final CustomTracksDeleteSettingsDialog dialog = new CustomTracksDeleteSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //retrieve UI data before execution of custom trak loading below
         //put mapping of objet type to custom tracks
         if (trackList == null || trackList.isEmpty()) {
            System.out.println("ActionDeleteCustomTracks: No Custom Tracks to delete!!");
            return;
         }
      } else {
         trackList.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {

               final boolean isDataRetrieved = DeleteCustomTracksfromTour(tour, trackList);

               if (isDataRetrieved) {
                  modifiedTours.add(tour);
               }
            }

            trackList.clear();

            if (modifiedTours.size() > 0) {
               TourManager.saveModifiedTours(modifiedTours);
            } else {
               MessageDialog.openInformation(
                     shell,
                     DIALOG_OPEN_TITLE,
                     DIALOG_OPEN_MSG);
            }
         }
      });
   }
}
