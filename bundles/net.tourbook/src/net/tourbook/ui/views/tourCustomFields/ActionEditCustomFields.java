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
import java.util.TreeMap;

import net.tourbook.Images;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ActionEditCustomFields extends Action {
   public static final String          DIALOG_TITLE               = "Edit Custom CustomField's"; //$NON-NLS-1$
   public static final String          DIALOG_MSG                 = "Edit Custom CustomField's"; //$NON-NLS-1$
   public static final String          FIELDSLIST_TITLE            = "List of All CustomField's"; //$NON-NLS-1$
   public static final String          DIALOG_OPEN_TITLE          = "CustomField's Edit";        //$NON-NLS-1$
   public static final String          DIALOG_OPEN_MSG            = "No CustomField's to Edit";  //$NON-NLS-1$
   public static final String          MENU_NAME                  = "&Edit Tours CustomField's"; //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_NAME     = "List CustomField's";        //$NON-NLS-1$
   public static final String          BUTTON_LOADTRACKS_TOOLTIP  = "List All CustomField's";    //$NON-NLS-1$
   //
   public static final String          COLI_NAME                 = "#";                                         //$NON-NLS-1$
   public static final String          COL0_NAME                 = "Delete";                                    //$NON-NLS-1$
   public static final String          COL1_NAME                 = "Name";                                      //$NON-NLS-1$
   public static final String          COL2_NAME                 = "Unit";                                      //$NON-NLS-1$
   public static final String          COL3_NAME                 = "Type";                                      //$NON-NLS-1$
   public static final String          COL4_NAME                 = "Desc.";                                      //$NON-NLS-1$
   public static final String          COL5_NAME                 = "Count";                                     //$NON-NLS-1$
   public static final String          COL6_NAME                 = "RefId";                          //$NON-NLS-1$
   public static final String          COL7_NAME                 = "Update";                         //$NON-NLS-1$
   //
   public static final String          CHECKBOX_DELETE_TAG       = "CHECK_DEL_BOX";                             //$NON-NLS-1$
   public static final String          CHECKBOX_UPDATE_TAG       = "CHECK_UPD_BOX";                             //$NON-NLS-1$
   public static final String          TEXTBOX_UNIT_TAG          = "TEXTBOX_UNIT";                              //$NON-NLS-1$
   public static final String          TEXTBOX_NAME_TAG          = "TEXTBOX_NAME";                   //$NON-NLS-1$
   public static final String          TEXTBOX_DESC_TAG          = "TEXTBOX_DESC";                   //$NON-NLS-1$
   public static final String          TEXTBOX_TYPE_TAG          = "TEXTBOX_TYPE";                   //$NON-NLS-1$
   public static final String          DROPDOWN_REFID_TAG        = "DROPDOWN_REFID";                      //$NON-NLS-1$
   public static final String          DROPDOWN_ITEM_REFID_TAG   = "DROPDOWN_REFID";                      //$NON-NLS-1$

   private TreeMap<String, DataToEditEntry> _dataEditList                = new TreeMap<>();
   private ArrayList<CustomField>               _allCustomFields            = null;
   private HashMap<String, CustomFieldViewItem> _allCustomFieldView_ByRefId = null;

   private ITourProvider2              _tourProvider             = null;
   private ITourProvider               _tourProvider1            = null;

   private boolean       _isSaveTour;

   private class CustomFieldsEditSettingsDialog extends TitleAreaDialog {

      private Composite _containerFieldsList;
      private Table     _tableOfCustomFields;
      private String[]  _titleofTable = { COLI_NAME, COL0_NAME, COL1_NAME, COL2_NAME,
                                         COL3_NAME, COL4_NAME, COL5_NAME,
                                         COL6_NAME, COL7_NAME };

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
            _tableOfCustomFields = new Table(_containerFieldsList, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
            _tableOfCustomFields.setLinesVisible(true);
            _tableOfCustomFields.setHeaderVisible(true);
            _tableOfCustomFields.setHeaderBackground(headerColor);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.heightHint = 100;
            _tableOfCustomFields.setLayoutData(data);

            for (int i = 0; i < _titleofTable.length; i++) {
               final TableColumn column = new TableColumn(_tableOfCustomFields, SWT.RIGHT);
               column.setText(_titleofTable[i]);
               _tableOfCustomFields.getColumn(i).pack();
            }
            _containerFieldsList.pack();

            //show selected custom tracks list for selected tours info in the UI
            //which makes re-create easy (select->delete->update -> create)
            _tableOfCustomFields.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  final TableItem[] selection = _tableOfCustomFields.getSelection();
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
                     TourDatabase.clearCustomFields();
                     final ArrayList<CustomField> allTourCustomFields = _allCustomFields = TourDatabase.getAllCustomFields();
                     _allCustomFieldView_ByRefId = TourDatabase.getAllCustomFieldsView_ByRefId();
                     if (allTourCustomFields != null && !allTourCustomFields.isEmpty()) {
                        for (final CustomField customFieldEntry : allTourCustomFields) {
                           if (!_dataEditList.containsKey(customFieldEntry.getRefId())) {
                              final DataToEditEntry newEntry = new DataToEditEntry();
                              newEntry.customField = customFieldEntry;
                              newEntry.name = customFieldEntry.getFieldName();
                              newEntry.unit = customFieldEntry.getUnit();
                              newEntry.refid = customFieldEntry.getRefId();
                              newEntry.customFieldType = customFieldEntry.getFieldType();
                              newEntry.description = customFieldEntry.getDescription();
                              newEntry.count = _allCustomFieldView_ByRefId.get(customFieldEntry.getRefId()).getColTourCounter();
                              _dataEditList.put(customFieldEntry.getRefId(), newEntry);
                              }
                           }
                        }

                     //now add item to table list
                     final ArrayList<DataToEditEntry> listSerie = new ArrayList<>(_dataEditList.values());
                     listSerie.sort(Comparator.naturalOrder());
                     int cnt = 1;
                     for (final DataToEditEntry trackListEntry : listSerie) {
                        final TableItem itemEvent = new TableItem(_tableOfCustomFields, SWT.NONE);
                        itemEvent.setText(0, String.valueOf(cnt++));

                        itemEvent.setText(2, trackListEntry.name);
                        final TableEditor editorName = new TableEditor(_tableOfCustomFields);
                        final Text textName = new Text(_tableOfCustomFields, SWT.SINGLE);
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
                              final DataToEditEntry dataToEditEntry = (DataToEditEntry) ((Text) e.widget).getData();
                              dataToEditEntry.name = valueTxt;
                           }
                        });

                        itemEvent.setText(3, trackListEntry.unit);
                        final TableEditor editorUnit = new TableEditor(_tableOfCustomFields);
                        final Text textUnit = new Text(_tableOfCustomFields, SWT.SINGLE);
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
                              final DataToEditEntry dataToEditEntry = (DataToEditEntry) ((Text) e.widget).getData();
                              dataToEditEntry.unit = valueTxt;
                           }
                        });

                        final TableEditor editorType = new TableEditor(_tableOfCustomFields);
                        final ToolBar toolBarNew = new ToolBar(_tableOfCustomFields, SWT.BORDER | SWT.VERTICAL);
                        toolBarNew.setData(trackListEntry);
                        final ToolItem itemNew = new ToolItem(toolBarNew, SWT.DROP_DOWN);
                        itemEvent.setData(DROPDOWN_REFID_TAG, toolBarNew);
                        itemEvent.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);
                        //buttonUpdate.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);

                        itemNew.setText(trackListEntry.customFieldType.name());
                        final DropdownSelectionListener listenerNew = new DropdownSelectionListener(itemNew);
                        for (final CustomFieldType fieldType : CustomFieldType.values()) {
                           listenerNew.add(fieldType, trackListEntry);
                        }
                        itemNew.addSelectionListener(listenerNew);
                        toolBarNew.pack();
                        editorType.minimumWidth = toolBarNew.getSize().x;
                        editorType.minimumHeight = toolBarNew.getSize().y;
                        editorType.horizontalAlignment = SWT.LEFT;
                        editorType.grabHorizontal = true;
                        editorType.grabVertical = true;
                        editorType.setEditor(toolBarNew, itemEvent, 4);

                        itemEvent.setText(5, trackListEntry.description);
                        final TableEditor editorDescription = new TableEditor(_tableOfCustomFields);
                        final Text textDescription = new Text(_tableOfCustomFields, SWT.SINGLE);
                        itemEvent.setData(TEXTBOX_UNIT_TAG, textDescription);
                        textDescription.setData(trackListEntry);
                        textDescription.setText(trackListEntry.description);
                        textDescription.pack();
                        editorDescription.horizontalAlignment = SWT.LEFT;
                        editorDescription.setEditor(textDescription, itemEvent, 5);
                        editorDescription.grabHorizontal = true;
                        textDescription.addVerifyListener(new VerifyListener() {
                           @Override
                           public void verifyText(final VerifyEvent e) {
                              /* Notice how we combine the old and new below */
                              final String currentText = ((Text) e.widget).getText();
                              final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                              final DataToEditEntry dataToEditEntry = (DataToEditEntry) ((Text) e.widget).getData();
                              dataToEditEntry.description = valueTxt;
                           }
                        });

                        itemEvent.setText(6, String.valueOf(trackListEntry.count));
                        itemEvent.setText(7, String.valueOf(trackListEntry.refid));

                        final TableEditor editor = new TableEditor(_tableOfCustomFields);
                        final Button buttonDelete = new Button(_tableOfCustomFields, SWT.CHECK);
                        buttonDelete.setData(trackListEntry);
                        itemEvent.setData(CHECKBOX_DELETE_TAG, buttonDelete);

                        buttonDelete.addSelectionListener(new SelectionAdapter() {
                           @Override
                           public void widgetSelected(final SelectionEvent e) {
                              final Button button = (Button) e.widget;
                              final Object data = button.getData();
                              if (data != null) {
                                 final DataToEditEntry cData = (DataToEditEntry) data;
                                 if (button.getSelection()) {
                                    //System.out.println(" Maintenance check for True:" + button.getData());
                                    if (cData.count > 0) {
                                       button.setSelection(false);
                                       MessageDialog.openInformation(
                                             shell,
                                             DIALOG_OPEN_TITLE,
                                             "Can't delete CustomFields with Tours still using it!!!");
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

                        final TableEditor editorUpd = new TableEditor(_tableOfCustomFields);
                        final Button buttonUpdate = new Button(_tableOfCustomFields, SWT.CHECK);
                        buttonUpdate.setData(trackListEntry);
                        itemEvent.setData(CHECKBOX_UPDATE_TAG, buttonUpdate);

                        buttonUpdate.addSelectionListener(new SelectionAdapter() {
                           @Override
                           public void widgetSelected(final SelectionEvent e) {
                              final Button button = (Button) e.widget;
                              final Object data = button.getData();
                              if (data != null) {
                                 final DataToEditEntry cData = (DataToEditEntry) data;
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

   public class DataToEditEntry implements Comparable<Object> {
      CustomField customField;
      String  name;
      String  refid;
      String  unit;
      String  description;
      CustomFieldType customFieldType;
      long    count     = 0;
      Boolean isDeleted = false;
      Boolean isUpdated = false;

      @Override
      public int compareTo(final Object o) {
         if (o instanceof DataToEditEntry) {
            final DataToEditEntry trackEntryToCompare = (DataToEditEntry) o;
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

   class DropdownSelectionListener extends SelectionAdapter {
      private ToolItem dropdown;

      private Menu     menu;

      public DropdownSelectionListener(final ToolItem dropdown) {
         this.dropdown = dropdown;
         menu = new Menu(dropdown.getParent().getShell());
      }

      public void add(final CustomFieldType item) {
         final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
         menuItem.setText(item.name());
         menuItem.setData(item);
         menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
               final MenuItem selected = (MenuItem) event.widget;
               final CustomFieldType custFieldType = (CustomFieldType) selected.getData();
               dropdown.setText(custFieldType.name());
               dropdown.setToolTipText(custFieldType.toString());
               dropdown.setData(custFieldType);
            }
         });
      }

      public void add(final CustomFieldType item, final DataToEditEntry data) {
         final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
         menuItem.setText(item.name());
         menuItem.setData("Data", data);
         menuItem.setData("Type", item);
         menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
               final MenuItem selected = (MenuItem) event.widget;
               final DataToEditEntry custField = (DataToEditEntry) selected.getData("Data");
               final CustomFieldType custFieldType = (CustomFieldType) selected.getData("Type");
               dropdown.setText(selected.getText());
               dropdown.setToolTipText(custField.toString());
               custField.customFieldType = custFieldType;
               dropdown.setData(custField);
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

   public ActionEditCustomFields(final ITourProvider tourProvider, final boolean isSaveTour) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider1 = tourProvider;

      _isSaveTour = isSaveTour;

      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16));
      setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16_Disabled));

      setEnabled(false);

      setText(MENU_NAME);
   }

   public ActionEditCustomFields(final ITourProvider2 tourProvider) {

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
   private static void fireCustomFieldChangeEvent() {

      TourManager.fireEvent(TourEventId.CUSTOMFIELDS_IS_MODIFIED);
   }

   public boolean editCustomTracksfromTour(final ArrayList<CustomField> modifiedCustomField,
                                           final ArrayList<CustomField> deletedCustomField,
                                           final TreeMap<String, DataToEditEntry> fieldMapList) {
      boolean isModified = false;
      if (_allCustomFields != null && !_allCustomFields.isEmpty()) {
         final Iterator<CustomField> iteratorCustTrackDef = _allCustomFields.iterator();
         while (iteratorCustTrackDef.hasNext()) {
            final CustomField customField = iteratorCustTrackDef.next();
            if (fieldMapList.containsKey(customField.getRefId())) {
               if (fieldMapList.get(customField.getRefId()).isDeleted) {
                  deletedCustomField.add(customField);
                  isModified = true;
               } else if (fieldMapList.get(customField.getRefId()).isUpdated) {
                  customField.setUnit(fieldMapList.get(customField.getRefId()).unit);
                  customField.setFieldName(fieldMapList.get(customField.getRefId()).name);
                  customField.setDescription(fieldMapList.get(customField.getRefId()).description);
                  customField.setFieldType(fieldMapList.get(customField.getRefId()).customFieldType);
                  modifiedCustomField.add(customField);
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
      TourDatabase.clearCustomFields();
      _allCustomFields = TourDatabase.getAllCustomFields();
      _allCustomFieldView_ByRefId = TourDatabase.getAllCustomFieldsView_ByRefId();

      final Shell shell = Display.getCurrent().getActiveShell();

      if (_allCustomFields == null || _allCustomFields.isEmpty()) {

         // No CustomFields
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "No CustomField's Available");//$NON-NLS-1$

         return;
      }

      _dataEditList.clear();
      final CustomFieldsEditSettingsDialog dialog = new CustomFieldsEditSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //retrieve UI data before execution of custom trak loading below
         //put mapping of objet type to custom tracks
         if (_dataEditList == null || _dataEditList.isEmpty()) {
            System.out.println("ActionEditCustomFields: No Custom CustomFields to Edit!!"); //$NON-NLS-1$
            return;
         }
      } else {
         _dataEditList.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<CustomField> deletedCustomFields = new ArrayList<>();
            final ArrayList<CustomField> modifiedCustomFields = new ArrayList<>();

            final boolean isDataRetrieved = editCustomTracksfromTour(modifiedCustomFields, deletedCustomFields, _dataEditList);

               if (isDataRetrieved) {
               //modifiedTours.add(tour);
               }

            _dataEditList.clear();

            if (modifiedCustomFields.size() > 0 || deletedCustomFields.size() > 0) {
               if (_isSaveTour) {
                  //TourManager.saveModifiedTours(modifiedTours);
                  TourDatabase.updateAndDeleteCustomField(modifiedCustomFields, deletedCustomFields);
                  fireCustomFieldChangeEvent();
               }else {

                  /*
                   * don't save the tour's, just update the tour's in data editor
                   */
                  fireCustomFieldChangeEvent();
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
