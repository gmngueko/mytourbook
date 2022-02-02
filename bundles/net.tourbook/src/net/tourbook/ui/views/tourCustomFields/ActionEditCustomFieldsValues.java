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
package net.tourbook.ui.views.tourCustomFields;

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
import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;
import net.tourbook.data.CustomFieldValue;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

public class ActionEditCustomFieldsValues extends Action {
   public static final String          DIALOG_TITLE               = "Edit Tours CustomFields Values";                  //$NON-NLS-1$
   public static final String          DIALOG_MSG                 = "Edit CustomFields Values";                        //$NON-NLS-1$
   public static final String          FIELDSLIST_TITLE            = "List of CustomFields Values";                     //$NON-NLS-1$
   public static final String          DIALOG_OPEN_TITLE          = "CustomFields Values Edit";                        //$NON-NLS-1$
   public static final String          DIALOG_OPEN_MSG            = "No CustomFields Values to edit";                  //$NON-NLS-1$
   public static final String          MENU_NAME                  = "&Edit Tours CustomFields Values...";              //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_NAME     = "List CustomFields Values";                        //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_TOOLTIP  = "List CustomFields Values from selected tours..."; //$NON-NLS-1$
   public static final String          COL0_NAME                 = "#";                                         //$NON-NLS-1$
   public static final String          COL1_NAME                 = "Delete";                                    //$NON-NLS-1$
   public static final String          COL2_NAME                 = "Name";                                      //$NON-NLS-1$
   public static final String          COL3_NAME                 = "Value";                                      //$NON-NLS-1$
   public static final String          COL4_NAME                 = "Unit";                                      //$NON-NLS-1$
   public static final String          COL5_NAME                 = "Count";                                     //$NON-NLS-1$
   public static final String          COL6_NAME                 = "Type";                                      //$NON-NLS-1$
   public static final String          COL7_NAME                 = "RefId";                                     //$NON-NLS-1$
   public static final String          COL8_NAME                 = "Update";                                    //$NON-NLS-1$
   public static final String          CHECKBOX_DELETE_TAG       = "CHECK_DEL_BOX";                             //$NON-NLS-1$
   public static final String          CHECKBOX_UPDATE_TAG       = "CHECK_UPD_BOX";                             //$NON-NLS-1$
   public static final String          TEXTBOX_UNIT_TAG          = "TEXTBOX_UNIT";                              //$NON-NLS-1$
   public static final String          DROPDOWN_REFID_TAG        = "DROPDOWN_REFID";                            //$NON-NLS-1$
   public static final String          DROPDOWN_ITEM_REFID_TAG   = "DROPDOWN_REFID";                            //$NON-NLS-1$
   public static final String          NO_SELECTION_STRING       = "No Selection";                              //$NON-NLS-1$

   private TreeMap<String, TrackEntry> _data2EditList                 = new TreeMap<>();
   private ArrayList<CustomField>       _allCustomFields             = null;
   private ArrayList<CustomFieldValue>       _selectedTourCustomFields    = new ArrayList<>();
   private HashMap<String, CustomFieldValue> _selectedTourCustomFieldsMap = new HashMap<>();
   private ArrayList<String>           _updatedTourCustomFieldsId   = new ArrayList<>();

   private ITourProvider2              _tourProvider             = null;
   private ITourProvider               _tourProvider1            = null;

   private boolean       _isSaveTour;

   private class CustomFieldsEditSettingsDialog extends TitleAreaDialog {

      private Composite _containerFieldsList;
      private Table     _tableFields;
      private String[]  _titleTracks = { COL0_NAME, COL1_NAME, COL2_NAME, COL3_NAME, COL4_NAME,
                                          COL5_NAME, COL6_NAME, COL7_NAME, COL8_NAME };

      private Button    buttonLoadFields;

      private Shell     shell;

      public CustomFieldsEditSettingsDialog(final Shell parentShell) {
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
         _containerFieldsList = new Composite(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerFieldsList);
         GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_containerFieldsList);
         {
            final Label label = new Label(_containerFieldsList, SWT.NONE);
            label.setText(FIELDSLIST_TITLE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
            final Color headerColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            _tableFields = new Table(_containerFieldsList, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
            _tableFields.setLinesVisible(true);
            _tableFields.setHeaderVisible(true);
            _tableFields.setHeaderBackground(headerColor);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.heightHint = 100;
            _tableFields.setLayoutData(data);

            for (int i = 0; i < _titleTracks.length; i++) {
               final TableColumn column = new TableColumn(_tableFields, SWT.RIGHT);
               column.setText(_titleTracks[i]);
               _tableFields.getColumn(i).pack();
            }
            _containerFieldsList.pack();

            //show selected custom tracks list for selected tours info in the UI
            //which makes re-create easy (select->delete->update -> create)
            _tableFields.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  final TableItem[] selection = _tableFields.getSelection();
                  if (selection.length > 0) {
                     //TODO
                  }

               }
            });

            buttonLoadFields = new Button(_containerFieldsList, SWT.NONE);
            buttonLoadFields.setText(BUTTON_LOADTRACKS_NAME);
            buttonLoadFields.setToolTipText(BUTTON_LOADTRACKS_TOOLTIP);
            buttonLoadFields.addListener(SWT.Selection, new Listener() {
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
                     _selectedTourCustomFields.clear();
                     _selectedTourCustomFieldsMap.clear();
                     _updatedTourCustomFieldsId.clear();
                     //Build list of track RefId, count and total size
                     for (final TourData tour : selectedTours) {
                        final Set<CustomFieldValue> allTourCustomFieldValues = tour.getCustomFieldValues();
                        if (allTourCustomFieldValues != null && !allTourCustomFieldValues.isEmpty()) {

                           for (final CustomFieldValue tourCustomFieldValue : allTourCustomFieldValues) {
                              if (_data2EditList.containsKey(tourCustomFieldValue.getCustomField().getRefId())) {
                                 _data2EditList.get(tourCustomFieldValue.getCustomField().getRefId()).count += 1;
                              } else {
                                 _selectedTourCustomFields.add(tourCustomFieldValue);
                                 _selectedTourCustomFieldsMap.put(tourCustomFieldValue.getCustomField().getRefId(), tourCustomFieldValue);
                                 final TrackEntry newEntry = new TrackEntry();
                                 newEntry.count = 1;
                                 newEntry.name = tourCustomFieldValue.getCustomField().getFieldName();
                                 newEntry.unit = tourCustomFieldValue.getCustomField().getUnit();
                                 newEntry.refid = tourCustomFieldValue.getCustomField().getRefId();
                                 newEntry.customFieldType = tourCustomFieldValue.getCustomField().getFieldType();
                                 newEntry.customFieldValue = tourCustomFieldValue;
                                 newEntry.valueFloat = tourCustomFieldValue.getValueFloat();
                                 newEntry.valueString = tourCustomFieldValue.getValueString();
                                 _data2EditList.put(tourCustomFieldValue.getCustomField().getRefId(), newEntry);
                              }
                           }
                        }
                     }

                     //now add item to table list
                     final ArrayList<TrackEntry> listSerie = new ArrayList<>(_data2EditList.values());
                     listSerie.sort(Comparator.naturalOrder());

                     int cnt = 1;
                     for (final TrackEntry trackListEntry : listSerie) {
                        final TableItem itemEvent = new TableItem(_tableFields, SWT.NONE);
                        itemEvent.setText(0, String.valueOf(cnt++));
                        itemEvent.setText(2, trackListEntry.name);

                        final TableEditor editorValue = new TableEditor(_tableFields);
                        final Text textValue = new Text(_tableFields, SWT.SINGLE);
                        itemEvent.setData(TEXTBOX_UNIT_TAG, textValue);
                        textValue.setData(trackListEntry);
                        textValue.setText(trackListEntry.customFieldValue.getValue());
                        textValue.pack();
                        editorValue.horizontalAlignment = SWT.LEFT;
                        editorValue.setEditor(textValue, itemEvent, 3);
                        editorValue.grabHorizontal = true;
                        textValue.addVerifyListener(new VerifyListener() {
                           @Override
                           public void verifyText(final VerifyEvent e) {
                              /* Notice how we combine the old and new below */
                              final String currentText = ((Text) e.widget).getText();
                              final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                              final TrackEntry trackEntry = (TrackEntry) ((Text) e.widget).getData();
                              e.doit = trackEntry.setValue(valueTxt) ;//if invalid entry refuse
                           }
                        });

                        itemEvent.setText(4, trackListEntry.unit);
                        itemEvent.setText(5, String.valueOf(trackListEntry.count));
                        itemEvent.setText(6, trackListEntry.customFieldType.name());
                        itemEvent.setText(7, String.valueOf(trackListEntry.refid));

                        final TableEditor editor = new TableEditor(_tableFields);
                        final Button buttonDelete = new Button(_tableFields, SWT.CHECK);
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

                        final TableEditor editorUpd = new TableEditor(_tableFields);
                        final Button buttonUpdate = new Button(_tableFields, SWT.CHECK);
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
                        editorUpd.setEditor(buttonUpdate, itemEvent, 8);

//                        final TableEditor editorNew = new TableEditor(_tableFields);
//                        final ToolBar toolBarNew = new ToolBar(_tableFields, SWT.BORDER | SWT.VERTICAL);
//                        toolBarNew.setData(trackListEntry);
//                        final ToolItem itemNew = new ToolItem(toolBarNew, SWT.DROP_DOWN);
//                        itemEvent.setData(DROPDOWN_REFID_TAG, toolBarNew);
//                        itemEvent.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);
//                        buttonUpdate.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);
//
//                        itemNew.setText(NO_SELECTION_STRING);
//                        final DropdownSelectionListener listenerNew = new DropdownSelectionListener(itemNew);
//                        for (final CustomField dataSerie : _allCustomFields) {
//                           if (dataSerie.getRefId().compareTo(trackListEntry.refid) != 0) {
//                              listenerNew.add(dataSerie);
//                           }
//                        }
//                        itemNew.addSelectionListener(listenerNew);
//                        toolBarNew.pack();
//                        editorNew.minimumWidth = toolBarNew.getSize().x;
//                        editorNew.minimumHeight = toolBarNew.getSize().y;
//                        editorNew.horizontalAlignment = SWT.LEFT;
//                        editorNew.grabHorizontal = true;
//                        editorNew.setEditor(toolBarNew, itemEvent, 8);

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
      CustomFieldValue customFieldValue;
      String      valueString;
      Float       valueFloat;
      CustomFieldType customFieldType;
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

      public boolean setValue(final String value) {
         if (customFieldValue != null) {
            final Boolean retValue = customFieldValue.setValueTemp(value);
            if (retValue) {
               this.valueFloat = customFieldValue.getValueFloatTemp();
               this.valueString = customFieldValue.getValueStringTemp();
            }
            return retValue;
         }
         return false;//if not OK
      }
   }

   public ActionEditCustomFieldsValues(final ITourProvider tourProvider, final boolean isSaveTour) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider1 = tourProvider;

      _isSaveTour = isSaveTour;

      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16));
      setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16_Disabled));

      setEnabled(isSaveTour);

      setText(MENU_NAME);
   }

   public ActionEditCustomFieldsValues(final ITourProvider2 tourProvider) {

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

   public boolean editCustomFieldsfromTour(final TourData tour, final TreeMap<String, TrackEntry> trackList2) {
      boolean isModified = false;
      if (_selectedTourCustomFields != null && !_selectedTourCustomFields.isEmpty()) {
         final Iterator<CustomFieldValue> iteratorCustTrackDef = _selectedTourCustomFields.iterator();
         while (iteratorCustTrackDef.hasNext()) {
            final CustomFieldValue customFieldValue = iteratorCustTrackDef.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            if (trackList2.containsKey(customFieldValue.getCustomField().getRefId())) {
               if (trackList2.get(customFieldValue.getCustomField().getRefId()).isDeleted) {
                  tour.getCustomFieldValues().remove(customFieldValue);
                  isModified = true;
               } else if (trackList2.get(customFieldValue.getCustomField().getRefId()).isUpdated) {
                  customFieldValue.setValueFloat(trackList2.get(customFieldValue.getCustomField().getRefId()).valueFloat);
                  customFieldValue.setValueString(trackList2.get(customFieldValue.getCustomField().getRefId()).valueString);
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
      _allCustomFields = TourDatabase.getAllCustomFields();
      final Shell shell = Display.getCurrent().getActiveShell();
      if (selectedTours == null || selectedTours.isEmpty()) {

         // a tour is not selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }

      if (selectedTours.size() > 1) {

         // a tour is not selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "Can Edit Values for only one Tour !!");//$NON-NLS-1$

         return;
      }

      if (_allCustomFields == null || _allCustomFields.isEmpty()) {

         // No DataSeries
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "No CustomField's Available");//$NON-NLS-1$

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

      _data2EditList.clear();
      final CustomFieldsEditSettingsDialog dialog = new CustomFieldsEditSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //retrieve UI data before execution of custom trak loading below
         //put mapping of objet type to custom tracks
         if (_data2EditList == null || _data2EditList.isEmpty()) {
            System.out.println("ActionEditCustomTracks: No Custom Fields to Edit!!"); //$NON-NLS-1$
            return;
         }
      } else {
         _data2EditList.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {

               final boolean isDataRetrieved = editCustomFieldsfromTour(tour, _data2EditList);

               if (isDataRetrieved) {
                  modifiedTours.add(tour);
               }
            }

            _data2EditList.clear();

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
