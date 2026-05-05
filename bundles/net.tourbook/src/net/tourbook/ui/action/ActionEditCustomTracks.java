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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.data.DataSerie;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.tour.TourEvent;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.ITourProvider2;
import net.tourbook.ui.views.tourDataEditor.TourDataEditorView;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ActionEditCustomTracks extends Action {
   public static final String          DIALOG_TITLE              = "Edit Tours Custom Tracks";                  //$NON-NLS-1$
   public static final String          DIALOG_MSG                = "Edit Custom Tracks";                        //$NON-NLS-1$
   public static final String          TRACKLIST_TITLE           = "List of Custom Tracks";                     //$NON-NLS-1$
   public static final String          DIALOG_OPEN_TITLE         = "Custom Tracks Edit";                        //$NON-NLS-1$
   public static final String          DIALOG_OPEN_MSG           = "No Tracks to edit";                         //$NON-NLS-1$
   public static final String          MENU_NAME                 = "&Edit Tours Custom Tracks...";              //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_NAME    = "List Custom Tracks";                        //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_TOOLTIP = "List Custom Tracks from selected tours..."; //$NON-NLS-1$
   public static final String          COL0_NAME                 = "#";                                         //$NON-NLS-1$
   public static final String          COL1_NAME                 = "Delete";                                    //$NON-NLS-1$
   public static final String          COL2_NAME                 = "Name";                                      //$NON-NLS-1$
   public static final String          COL3_NAME                 = "Unit";                                      //$NON-NLS-1$
   public static final String          COL4_NAME                 = "Count";                                     //$NON-NLS-1$
   public static final String          COL5_NAME                 = "Size";                                      //$NON-NLS-1$
   public static final String          COL6_NAME                 = "RefId";                                     //$NON-NLS-1$
   public static final String          COL7_NAME                 = "Update";                                    //$NON-NLS-1$
   public static final String          COL8_NAME                 = "New RefId";                                 //$NON-NLS-1$
   public static final String          CHECKBOX_DELETE_TAG       = "CHECK_DEL_BOX";                             //$NON-NLS-1$
   public static final String          CHECKBOX_UPDATE_TAG       = "CHECK_UPD_BOX";                             //$NON-NLS-1$
   public static final String          TEXTBOX_UNIT_TAG          = "TEXTBOX_UNIT";                              //$NON-NLS-1$
   public static final String          DROPDOWN_REFID_TAG        = "DROPDOWN_REFID";                            //$NON-NLS-1$
   public static final String          DROPDOWN_ITEM_REFID_TAG   = "DROPDOWN_REFID";                            //$NON-NLS-1$
   public static final String          NO_SELECTION_STRING       = "No Selection";                              //$NON-NLS-1$

   private TreeMap<String, TrackEntry> _trackList                 = new TreeMap<>();
   private ArrayList<DataSerie>        _allDataSeries             = null;
   private ArrayList<DataSerie>        _selectedTourDataSeries    = new ArrayList<>();
   private HashMap<String, DataSerie>  _selectedTourDataSeriesMap = new HashMap<>();
   private ArrayList<String>           _updatedTourDataSeriesId   = new ArrayList<>();

   private ITourProvider2              _tourProvider             = null;
   private ITourProvider               _tourProvider1            = null;

   private boolean       _isSaveTour;

   private class CustomTracksEditSettingsDialog extends TitleAreaDialog {

      private Composite _containerTrackList;
      private Table     _tableTracks;
      private String[]  _titleTracks = { COL0_NAME, COL1_NAME, COL2_NAME, COL3_NAME, COL4_NAME, COL5_NAME, COL6_NAME, COL7_NAME, COL8_NAME };

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
                     final ArrayList<TourData> selectedTours = _tourProvider != null ? _tourProvider.getSelectedTours() : _tourProvider1
                           .getSelectedTours();
                     if (selectedTours == null || selectedTours.isEmpty()) {

                        // a tour is not selected
                        MessageDialog.openInformation(
                              shell,
                              DIALOG_OPEN_TITLE,
                              Messages.UI_Label_TourIsNotSelected);

                        return;
                     }
                     _selectedTourDataSeries.clear();
                     _selectedTourDataSeriesMap.clear();
                     _updatedTourDataSeriesId.clear();
                     //Build list of track RefId, count and total size
                     for (final TourData tour : selectedTours) {
                        final Set<DataSerie> allTourDataSerie = tour.getDataSeries();
                        if (allTourDataSerie != null && !allTourDataSerie.isEmpty()) {

                           for (final DataSerie tourDataSerie : allTourDataSerie) {
                              if (_trackList.containsKey(tourDataSerie.getRefId())) {
                                 _trackList.get(tourDataSerie.getRefId()).count += 1;
                                 final float[] custValues = tour.getCustomTracks(tourDataSerie.getRefId());
                                 final int custSize = custValues == null ? 0 : custValues.length;
                                 _trackList.get(tourDataSerie.getRefId()).size += custSize;
                              } else {
                                 _selectedTourDataSeries.add(tourDataSerie);
                                 _selectedTourDataSeriesMap.put(tourDataSerie.getRefId(), tourDataSerie);
                                 final TrackEntry newEntry = new TrackEntry();
                                 newEntry.name = tourDataSerie.getName();
                                 newEntry.unit = tourDataSerie.getUnit();
                                 newEntry.refid = tourDataSerie.getRefId();
                                 newEntry.count = 1;
                                 final float[] custValues = tour.getCustomTracks(tourDataSerie.getRefId());
                                 final int custSize = custValues == null ? 0 : custValues.length;
                                 newEntry.size = custSize;
                                 _trackList.put(tourDataSerie.getRefId(), newEntry);
                              }
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

                        itemEvent.setText(3, trackListEntry.unit);
//                        final TableEditor editorUnit = new TableEditor(_tableTracks);
//                        final Text textUnit = new Text(_tableTracks, SWT.SINGLE);
//                        itemEvent.setData(TEXTBOX_UNIT_TAG, textUnit);
//                        textUnit.setData(trackListEntry.getValue());
//                        textUnit.setText(trackListEntry.getValue().unit);
//                        textUnit.pack();
//                        editorUnit.horizontalAlignment = SWT.LEFT;
//                        editorUnit.setEditor(textUnit, itemEvent, 2);
//                        editorUnit.grabHorizontal = true;
//                        textUnit.addVerifyListener(new VerifyListener() {
//                           @Override
//                           public void verifyText(final VerifyEvent e) {
//                              /* Notice how we combine the old and new below */
//                              final String currentText = ((Text) e.widget).getText();
//                              final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
//                              final TrackEntry trackEntry = (TrackEntry) ((Text) e.widget).getData();
//                              trackEntry.unit = valueTxt;
//                           }
//                        });

                        itemEvent.setText(4, String.valueOf(trackListEntry.count));
                        itemEvent.setText(5, String.valueOf(trackListEntry.size));
                        itemEvent.setText(6, String.valueOf(trackListEntry.refid));

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
                                 final ToolItem itemNew = (ToolItem) button.getData(DROPDOWN_ITEM_REFID_TAG);
                                 final DataSerie selectedSerie = (DataSerie) itemNew.getData();
                                 if (button.getSelection()) {
                                    //System.out.println(" Maintenance check for True:" + button.getData());
                                    if (itemNew.getText().compareTo(NO_SELECTION_STRING) == 0) {
                                       button.setSelection(false);
                                       cData.isUpdated = false;
                                       MessageDialog.openInformation(
                                             shell,
                                             DIALOG_OPEN_TITLE,
                                             "Cannot Update an Empty Selection, please Select a DataSerie");//$NON-NLS-1$
                                       return;
                                    }
                                    if (_updatedTourDataSeriesId.contains(selectedSerie.getRefId())) {
                                       button.setSelection(false);
                                       cData.isUpdated = false;
                                       MessageDialog.openInformation(
                                             shell,
                                             DIALOG_OPEN_TITLE,
                                             "Cannot Update because Selected DataSerie is already used in another Update!!");//$NON-NLS-1$
                                       return;
                                    }
                                    if (_selectedTourDataSeriesMap.containsKey(selectedSerie.getRefId())) {
                                       button.setSelection(false);
                                       cData.isUpdated = false;
                                       MessageDialog.openInformation(
                                             shell,
                                             DIALOG_OPEN_TITLE,
                                             "Cannot Update because Selected DataSerie is already present in the current Tour's!!");//$NON-NLS-1$
                                       return;
                                    }
                                    _updatedTourDataSeriesId.add(selectedSerie.getRefId());
                                    cData.isUpdated = true;
                                    cData.refidNew = selectedSerie.getRefId();
                                 } else {
                                    //System.out.println(" Maintenance check for False:" + button.getData());
                                    cData.isUpdated = false;
                                    cData.refidNew = null;
                                    _updatedTourDataSeriesId.remove(selectedSerie.getRefId());
                                 }
                              }
                           }
                        });
                        buttonUpdate.pack();
                        editorUpd.minimumWidth = buttonUpdate.getSize().x;
                        editorUpd.horizontalAlignment = SWT.LEFT;
                        editorUpd.setEditor(buttonUpdate, itemEvent, 7);

                        final TableEditor editorNew = new TableEditor(_tableTracks);
                        final ToolBar toolBarNew = new ToolBar(_tableTracks, SWT.BORDER | SWT.VERTICAL);
                        toolBarNew.setData(trackListEntry);
                        final ToolItem itemNew = new ToolItem(toolBarNew, SWT.DROP_DOWN);
                        itemEvent.setData(DROPDOWN_REFID_TAG, toolBarNew);
                        itemEvent.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);
                        buttonUpdate.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);

                        itemNew.setText(NO_SELECTION_STRING);
                        final DropdownSelectionListener listenerNew = new DropdownSelectionListener(itemNew);
                        for (final DataSerie dataSerie : _allDataSeries) {
                           if (dataSerie.getRefId().compareTo(trackListEntry.refid) != 0) {
                              listenerNew.add(dataSerie);
                           }
                        }
                        itemNew.addSelectionListener(listenerNew);
                        toolBarNew.pack();
                        editorNew.minimumWidth = toolBarNew.getSize().x;
                        editorNew.minimumHeight = toolBarNew.getSize().y;
                        editorNew.horizontalAlignment = SWT.LEFT;
                        editorNew.grabHorizontal = true;
                        editorNew.setEditor(toolBarNew, itemEvent, 8);

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

   class DropdownSelectionListener extends SelectionAdapter {
      private ToolItem dropdown;

      private Menu     menu;

      public DropdownSelectionListener(final ToolItem dropdown) {
         this.dropdown = dropdown;
         menu = new Menu(dropdown.getParent().getShell());
      }

      public void add(final DataSerie item) {
         final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
         menuItem.setText(item.toStringShort());
         menuItem.setToolTipText(item.toStringShortRefId());
         menuItem.setData(item);
         menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
               final MenuItem selected = (MenuItem) event.widget;
               final DataSerie dataSerie = (DataSerie) selected.getData();
               dropdown.setText(dataSerie.getName());
               dropdown.setToolTipText(dataSerie.toStringShortRefId());
               dropdown.setData(dataSerie);
            }
         });
      }

      public void add(final String item) {
         final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
         menuItem.setText(item);
         menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
               final MenuItem selected = (MenuItem) event.widget;
               dropdown.setText(selected.getText());
            }
         });
      }

      @Override
      public void widgetSelected(final SelectionEvent event) {
         if (event.detail == SWT.ARROW) {
            final ToolItem item = (ToolItem) event.widget;
            final Rectangle rect = item.getBounds();
            final Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
            menu.setLocation(pt.x, pt.y + rect.height);
            menu.setVisible(true);
         } else {
            System.out.println(dropdown.getText() + " Pressed");
         }
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
      String  refidNew  = null;

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

   public ActionEditCustomTracks(final ITourProvider tourProvider, final boolean isSaveTour) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider1 = tourProvider;

      _isSaveTour = isSaveTour;

      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Graph_Custom_Tracks));
      setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Graph_Custom_Tracks_Disabled));

      setEnabled(isSaveTour);

      setText(MENU_NAME);
   }

   public ActionEditCustomTracks(final ITourProvider2 tourProvider) {

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
   private static void fireTourChangeEvent(final TourData tourData) {

      TourManager.fireEvent(TourEventId.TOUR_CHANGED, new TourEvent(tourData));
   }

   public boolean editCustomTracksfromTour(final TourData tour, final TreeMap<String, TrackEntry> trackList2) {
      boolean isModified = false;
      if (_selectedTourDataSeries != null && !_selectedTourDataSeries.isEmpty()) {
         final Iterator<DataSerie> iteratorCustTrackDef = _selectedTourDataSeries.iterator();
         while (iteratorCustTrackDef.hasNext()) {
            final DataSerie dataSerie = iteratorCustTrackDef.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            if (trackList2.containsKey(dataSerie.getRefId())) {
               if (trackList2.get(dataSerie.getRefId()).isDeleted) {
                  tour.clear_CustomTracks(dataSerie.getRefId());
                  isModified = true;
               } else if (trackList2.get(dataSerie.getRefId()).isUpdated && trackList2.get(dataSerie.getRefId()).refidNew != null) {
                  tour.update_CustomTracks_DataSerie(trackList2.get(dataSerie.getRefId()).refid, trackList2.get(dataSerie.getRefId()).refidNew);
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

      final ArrayList<TourData> selectedTours = _tourProvider != null ? _tourProvider.getSelectedTours() : _tourProvider1.getSelectedTours();
      _allDataSeries = TourDatabase.getAllDataSeries();
      final Shell shell = Display.getCurrent().getActiveShell();
      if (selectedTours == null || selectedTours.isEmpty()) {

         // a tour is not selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }

      if (_allDataSeries == null || _allDataSeries.isEmpty()) {

         // No DataSeries
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "No DataSerie's Available");//$NON-NLS-1$

         return;
      }

      Boolean isManual = false;
      for (final TourData tourData : selectedTours) {
         if (tourData.isManualTour()) {
            isManual = true;
            break;
         }
      }
      if (isManual) {
         // a manual tour is selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "Cannot operate on Manual Tour's !!!"); //$NON-NLS-1$
         return;
      }

      _trackList.clear();
      final CustomTracksEditSettingsDialog dialog = new CustomTracksEditSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //retrieve UI data before execution of custom trak loading below
         //put mapping of objet type to custom tracks
         if (_trackList == null || _trackList.isEmpty()) {
            System.out.println("ActionEditCustomTracks: No Custom Tracks to Edit!!"); //$NON-NLS-1$
            return;
         }
      } else {
         _trackList.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {

               final boolean isDataRetrieved = editCustomTracksfromTour(tour, _trackList);

               if (isDataRetrieved) {
                  modifiedTours.add(tour);
               }
            }

            _trackList.clear();

            if (modifiedTours.size() > 0) {
               if (_isSaveTour) {
                  TourManager.saveModifiedTours(modifiedTours);
               }else {

                  /*
                   * don't save the tour's, just update the tour's in data editor
                   */
                  final TourDataEditorView tourDataEditor = TourManager.getTourDataEditor();
                  if (tourDataEditor != null) {
                     for(final TourData tourData:modifiedTours) {
                        tourDataEditor.updateUI(tourData, true);
                        fireTourChangeEvent(tourData);
                     }
                  }
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
