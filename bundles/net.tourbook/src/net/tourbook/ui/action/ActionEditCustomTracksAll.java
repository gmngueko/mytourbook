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
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import net.tourbook.Images;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.data.DataSerie;
import net.tourbook.database.TourDatabase;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.swt.widgets.Text;

public class ActionEditCustomTracksAll extends Action {
   public static final String          DIALOG_TITLE              = "Edit All Custom DataSerie's";    //$NON-NLS-1$
   public static final String          DIALOG_MSG                = "Edit All Custom DataSerie's";    //$NON-NLS-1$
   public static final String          TRACKLIST_TITLE           = "List of All Custom DataSerie's"; //$NON-NLS-1$
   public static final String          DIALOG_OPEN_TITLE         = "DataSeries Delete";              //$NON-NLS-1$
   public static final String          DIALOG_OPEN_MSG           = "No Tracks to delete";                       //$NON-NLS-1$
   public static final String          MENU_NAME                 = "&Edit Tours Custom DataSerie's"; //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_NAME    = "List Custom DataSerie's";        //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_TOOLTIP = "List All Custom DataSerie's";    //$NON-NLS-1$
   public static final String          COLI_NAME                 = "#";                                         //$NON-NLS-1$
   public static final String          COL0_NAME                 = "Delete";                                    //$NON-NLS-1$
   public static final String          COL1_NAME                 = "Name";                                      //$NON-NLS-1$
   public static final String          COL2_NAME                 = "Unit";                                      //$NON-NLS-1$
   public static final String          COL3_NAME                 = "Count";                                     //$NON-NLS-1$
//   public static final String          COL4_NAME                 = "Size";                                      //$NON-NLS-1$
   public static final String          COL4_NAME                 = "RefId";                          //$NON-NLS-1$
   public static final String          COL5_NAME                 = "Update";                         //$NON-NLS-1$
   public static final String          CHECKBOX_DELETE_TAG       = "CHECK_DEL_BOX";                             //$NON-NLS-1$
   public static final String          CHECKBOX_UPDATE_TAG       = "CHECK_UPD_BOX";                             //$NON-NLS-1$
   public static final String          TEXTBOX_UNIT_TAG          = "TEXTBOX_UNIT";                              //$NON-NLS-1$
   public static final String          TEXTBOX_NAME_TAG          = "TEXTBOX_NAME";                   //$NON-NLS-1$

   private TreeMap<String, TrackEntry> _trackList                = new TreeMap<>();
   private ArrayList<DataSerie>        _allDataSeries            = null;

   private ITourProvider2              _tourProvider             = null;
   private ITourProvider               _tourProvider1            = null;

   private boolean       _isSaveTour;

   private class CustomTracksEditSettingsDialog extends TitleAreaDialog {

      private Composite _containerTrackList;
      private Table     _tableTracks;
      private String[]  _titleTracks = { COLI_NAME, COL0_NAME, COL1_NAME, COL2_NAME, COL3_NAME, COL4_NAME, COL5_NAME };

      private Button    buttonLoadTracks;

      private Shell     shell;

      public CustomTracksEditSettingsDialog(final Shell parentShell) {
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
//                     final ArrayList<TourData> selectedTours = _tourProvider != null ? _tourProvider.getSelectedTours() : _tourProvider1
//                           .getSelectedTours();
//                     if (selectedTours == null || selectedTours.isEmpty()) {
//
//                        // a tour is not selected
//                        MessageDialog.openInformation(
//                              shell,
//                              DIALOG_OPEN_TITLE,
//                              Messages.UI_Label_TourIsNotSelected);
//
//                        return;
//                     }
                     //Build list of track RefId, count and total size
                     TourDatabase.clearDataSeries();
                     final ArrayList<DataSerie> allTourDataSerie = _allDataSeries = TourDatabase.getAllDataSeriesWithTourData();
                        if (allTourDataSerie != null && !allTourDataSerie.isEmpty()) {
                        for (final DataSerie dataSerieEntry : allTourDataSerie) {
                           if (!_trackList.containsKey(dataSerieEntry.getRefId())) {
                                 //if (tour.customTracksDefinition.containsKey(custTrackDefEntry.getRefId())) {
                                    final TrackEntry newEntry = new TrackEntry();
                              newEntry.name = dataSerieEntry.getName();
                              newEntry.unit = dataSerieEntry.getUnit();
                              newEntry.refid = dataSerieEntry.getRefId();
                              newEntry.count = dataSerieEntry.getTourData() == null ? 0 : dataSerieEntry.getTourData().size();
                                    final int custSize = -1;//custValues == null ? 0 : custValues.length;
                                    newEntry.size = custSize;
                              _trackList.put(dataSerieEntry.getRefId(), newEntry);
                                 //}
                              }
                           }
                        }

                     //now add item to table list
                     final ArrayList<TrackEntry> listSerie = new ArrayList<>(_trackList.values());
                     listSerie.sort(Comparator.naturalOrder());
                     int cnt = 1;
                     for (final TrackEntry trackListEntry : listSerie) {
                        final TableItem itemEvent = new TableItem(_tableTracks, SWT.NONE);
                        itemEvent.setText(0, String.valueOf(cnt++));

                        itemEvent.setText(2, trackListEntry.name);
                        final TableEditor editorName = new TableEditor(_tableTracks);
                        final Text textName = new Text(_tableTracks, SWT.SINGLE);
                        itemEvent.setData(TEXTBOX_NAME_TAG, textName);
                        textName.setData(trackListEntry);
                        textName.setText(trackListEntry.name);
                        textName.pack();
                        editorName.horizontalAlignment = SWT.LEFT;
                        editorName.setEditor(textName, itemEvent, 2);
                        editorName.grabHorizontal = true;
                        textName.addVerifyListener(new VerifyListener() {
                           @Override
                           public void verifyText(final VerifyEvent e) {
                              /* Notice how we combine the old and new below */
                              final String currentText = ((Text) e.widget).getText();
                              final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                              final TrackEntry trackEntry = (TrackEntry) ((Text) e.widget).getData();
                              trackEntry.name = valueTxt;
                           }
                        });

                        itemEvent.setText(3, trackListEntry.unit);
                        final TableEditor editorUnit = new TableEditor(_tableTracks);
                        final Text textUnit = new Text(_tableTracks, SWT.SINGLE);
                        itemEvent.setData(TEXTBOX_UNIT_TAG, textUnit);
                        textUnit.setData(trackListEntry);
                        textUnit.setText(trackListEntry.unit);
                        textUnit.pack();
                        editorUnit.horizontalAlignment = SWT.LEFT;
                        editorUnit.setEditor(textUnit, itemEvent, 3);
                        editorUnit.grabHorizontal = true;
                        textUnit.addVerifyListener(new VerifyListener() {
                           @Override
                           public void verifyText(final VerifyEvent e) {
                              /* Notice how we combine the old and new below */
                              final String currentText = ((Text) e.widget).getText();
                              final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                              final TrackEntry trackEntry = (TrackEntry) ((Text) e.widget).getData();
                              trackEntry.unit = valueTxt;
                           }
                        });

                        itemEvent.setText(4, String.valueOf(trackListEntry.count));
//                        itemEvent.setText(5, String.valueOf(trackListEntry.getValue().size));
                        itemEvent.setText(5, String.valueOf(trackListEntry.refid));

                        final TableEditor editor = new TableEditor(_tableTracks);
                        final Button buttonDelete = new Button(_tableTracks, SWT.CHECK);
                        buttonDelete.setData(trackListEntry);
                        itemEvent.setData(CHECKBOX_DELETE_TAG, buttonDelete);

                        buttonDelete.addSelectionListener(new SelectionAdapter() {
                           @Override
                           public void widgetSelected(final SelectionEvent e) {
                              final Button button = (Button) e.widget;
                              final Object data = button.getData();
                              if (data != null) {
                                 final TrackEntry cData = (TrackEntry) data;
                                 if (button.getSelection()) {
                                    //System.out.println(" Maintenance check for True:" + button.getData());
                                    if (cData.count > 0) {
                                       button.setSelection(false);
                                       MessageDialog.openInformation(
                                             shell,
                                             DIALOG_OPEN_TITLE,
                                             "Can't delete DataSeries with Tours still using it!!!");
                                       return;
                                    }
                                    cData.isDeleted = true;
                                 } else {
                                    //System.out.println(" Maintenance check for False:" + button.getData());
                                    cData.isDeleted = false;
                                 }
                              }
                           }
                        });
                        buttonDelete.pack();
                        editor.minimumWidth = buttonDelete.getSize().x;
                        editor.horizontalAlignment = SWT.LEFT;
                        editor.setEditor(buttonDelete, itemEvent, 1);

                        final TableEditor editorUpd = new TableEditor(_tableTracks);
                        final Button buttonUpdate = new Button(_tableTracks, SWT.CHECK);
                        buttonUpdate.setData(trackListEntry);
                        itemEvent.setData(CHECKBOX_UPDATE_TAG, buttonUpdate);

                        buttonUpdate.addSelectionListener(new SelectionAdapter() {
                           @Override
                           public void widgetSelected(final SelectionEvent e) {
                              final Button button = (Button) e.widget;
                              final Object data = button.getData();
                              if (data != null) {
                                 final TrackEntry cData = (TrackEntry) data;
                                 if (button.getSelection()) {
                                    //System.out.println(" Maintenance check for True:" + button.getData());
                                    cData.isUpdated = true;
                                 } else {
                                    //System.out.println(" Maintenance check for False:" + button.getData());
                                    cData.isUpdated = false;
                                 }
                              }
                           }
                        });
                        buttonUpdate.pack();
                        editorUpd.minimumWidth = buttonUpdate.getSize().x;
                        editorUpd.horizontalAlignment = SWT.LEFT;
                        editorUpd.setEditor(buttonUpdate, itemEvent, 6);
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

   public class TrackEntry implements Comparable<Object> {
      String name;
      String  refid;
      String  unit;
      int    count = 0;
      int     size       = 0;
      Boolean isDeleted = false;
      Boolean isUpdated = false;

      @Override
      public int compareTo(final Object o) {
         if (o instanceof TrackEntry) {
            final TrackEntry trackEntryToCompare = (TrackEntry) o;
            if (this.name == null) {
               this.name = UI.EMPTY_STRING;
            }
            if (trackEntryToCompare.name == null) {
               trackEntryToCompare.name = UI.EMPTY_STRING;
            }
            return this.name.compareTo(trackEntryToCompare.name);
         }
         return 0;
      }
   }

   public ActionEditCustomTracksAll(final ITourProvider tourProvider, final boolean isSaveTour) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider1 = tourProvider;

      _isSaveTour = isSaveTour;

      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Graph_Custom_Tracks));
      setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Graph_Custom_Tracks_Disabled));

      setEnabled(false);

      setText(MENU_NAME);
   }

   public ActionEditCustomTracksAll(final ITourProvider2 tourProvider) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider = tourProvider;

      _isSaveTour = true;

      setText(MENU_NAME);
   }

   /**
    * This event must be event when the dialog is canceled because the original tour custom tracks are
    * replaced with the backedup custom tracks.
    * <p>
    * Views which contain the original {@link CustomTracks}'s need to know that the list has changed
    * otherwise custom tracks actions do fail, e.g. Set custom tracks Hidden in tour chart view.
    *
    * @param tourData
    */
   private static void fireDataSerieChangeEvent() {

      TourManager.fireEvent(TourEventId.DATA_SERIE_IS_MODIFIED);
   }

   public boolean editCustomTracksfromTour(final ArrayList<DataSerie> modifiedDataSerie,
                                           final ArrayList<DataSerie> deletedDataSerie,
                                           final TreeMap<String, TrackEntry> trackList2) {
      boolean isModified = false;
      if (_allDataSeries != null && !_allDataSeries.isEmpty()) {
         final Iterator<DataSerie> iteratorCustTrackDef = _allDataSeries.iterator();
         while (iteratorCustTrackDef.hasNext()) {
            final DataSerie dataSerie = iteratorCustTrackDef.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            if (trackList2.containsKey(dataSerie.getRefId())) {
               if (trackList2.get(dataSerie.getRefId()).isDeleted) {
                  deletedDataSerie.add(dataSerie);
                  //iteratorCustTrackDef.remove(); // avoids a ConcurrentModificationException
                  isModified = true;
               } else if (trackList2.get(dataSerie.getRefId()).isUpdated) {
                  dataSerie.setUnit(trackList2.get(dataSerie.getRefId()).unit);
                  dataSerie.setName(trackList2.get(dataSerie.getRefId()).name);
                  modifiedDataSerie.add(dataSerie);
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

      //final ArrayList<TourData> selectedTours = _tourProvider != null ? _tourProvider.getSelectedTours() : _tourProvider1.getSelectedTours();
      TourDatabase.clearDataSeries();
      _allDataSeries = TourDatabase.getAllDataSeries();
      final Shell shell = Display.getCurrent().getActiveShell();
//      if (selectedTours == null || selectedTours.isEmpty()) {
//
//         // a tour is not selected
//         MessageDialog.openInformation(
//               shell,
//               DIALOG_OPEN_TITLE,
//               Messages.UI_Label_TourIsNotSelected);
//
//         return;
//      }

      if (_allDataSeries == null || _allDataSeries.isEmpty()) {

         // No DataSeries
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "No DataSerie's Available");//$NON-NLS-1$

         return;
      }

      _trackList.clear();
      final CustomTracksEditSettingsDialog dialog = new CustomTracksEditSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //retrieve UI data before execution of custom trak loading below
         //put mapping of objet type to custom tracks
         if (_trackList == null || _trackList.isEmpty()) {
            System.out.println("ActionEditCustomTracks: No Custom DataSeries to Edit!!"); //$NON-NLS-1$
            return;
         }
      } else {
         _trackList.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<DataSerie> deletedDataSerie = new ArrayList<>();
            final ArrayList<DataSerie> modifiedDataSerie = new ArrayList<>();

            final boolean isDataRetrieved = editCustomTracksfromTour(modifiedDataSerie, deletedDataSerie, _trackList);

               if (isDataRetrieved) {
               //modifiedTours.add(tour);
               }

            _trackList.clear();

            if (modifiedDataSerie.size() > 0 || deletedDataSerie.size() > 0) {
               if (_isSaveTour) {
                  //TourManager.saveModifiedTours(modifiedTours);
                  TourDatabase.updateAndDeleteDataSerie(modifiedDataSerie, deletedDataSerie);
                  fireDataSerieChangeEvent();
               }else {

                  /*
                   * don't save the tour's, just update the tour's in data editor
                   */
                  fireDataSerieChangeEvent();
//                  final TourDataEditorView tourDataEditor = TourManager.getTourDataEditor();
//                  if (tourDataEditor != null) {
//                     for(final TourData tourData:modifiedTours) {
//                        tourDataEditor.updateUI(tourData, true);
//                        fireTourChangeEvent();
//                     }
//                  }
               }
            } else {
               MessageDialog.openInformation(
                     shell,
                     DIALOG_OPEN_TITLE,
                     DIALOG_OPEN_MSG);
            }
         }
      });
   }

   public void setEnabled(final Boolean isEnabled) {
      _isSaveTour = isEnabled;
   }
}
