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
import java.util.HashMap;
import java.util.UUID;

import net.tourbook.Images;
import net.tourbook.application.TourbookPlugin;
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

public class ActionAddCustomFields extends Action {
   public static final String           DIALOG_TITLE              = "Create CustomField's";                //$NON-NLS-1$
   public static final String           DIALOG_MSG                = "Add CustomField's";                   //$NON-NLS-1$
   public static final String           TRACKLIST_TITLE           = "List of Added CustomField's";         //$NON-NLS-1$
   public static final String           DIALOG_OPEN_TITLE         = "CustomFields Creation";               //$NON-NLS-1$
   public static final String           DIALOG_OPEN_MSG           = "No CustomFields to Add";              //$NON-NLS-1$
   public static final String           MENU_NAME                 = "&Add Tours CustomField's";            //$NON-NLS-1$
   public static final String           BUTTON_LOADTRACKS_NAME    = "Add CustomFields row";                //$NON-NLS-1$
   public static final String           BUTTON_LOADTRACKS_TOOLTIP = "Add a new CustomFields to be edited"; //$NON-NLS-1$
   public static final String           COL0_NAME                 = "Name";                                //$NON-NLS-1$
   public static final String           COL1_NAME                 = "Unit";                                //$NON-NLS-1$
   public static final String           COL2_NAME                 = "Type";                                //$NON-NLS-1$
   public static final String           COL3_NAME                 = "Desc.";                               //$NON-NLS-1$
   public static final String           COL4_NAME                 = "RefId";                               //$NON-NLS-1$

   public static final String           TEXTBOX_UNIT_TAG          = "TEXTBOX_UNIT";                        //$NON-NLS-1$
   public static final String           TEXTBOX_NAME_TAG          = "TEXTBOX_NAME";                        //$NON-NLS-1$
   public static final String           TEXTBOX_REFID_TAG         = "TEXTBOX_REFID";                       //$NON-NLS-1$
   public static final String           TEXTBOX_DESC_TAG          = "TEXTBOX_DESC";                        //$NON-NLS-1$
   public static final String           TEXTBOX_TYPE_TAG          = "TEXTBOX_TYPE";                        //$NON-NLS-1$
   public static final String           DROPDOWN_REFID_TAG        = "DROPDOWN_REFID";                      //$NON-NLS-1$
   public static final String           DROPDOWN_ITEM_REFID_TAG   = "DROPDOWN_REFID_ITEM";                 //$NON-NLS-1$

   private HashMap<String, CustomField> _addCustomField_ByRefId   = new HashMap<>();

   private ITourProvider2              _tourProvider             = null;
   private ITourProvider               _tourProvider1            = null;

   private boolean       _isSaveTour;

   private class CustomFieldsEditSettingsDialog extends TitleAreaDialog {

      private Composite _containerFieldList;
      private Table     _tableFields;
      private String[]  _titleFields = { COL0_NAME, COL1_NAME, COL2_NAME, COL3_NAME, COL4_NAME };

      private Button    buttonAddFields;

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
         _containerFieldList = new Composite(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerFieldList);
         GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_containerFieldList);
         {
            final Label label = new Label(_containerFieldList, SWT.NONE);
            label.setText(TRACKLIST_TITLE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
            final Color headerColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            _tableFields = new Table(_containerFieldList, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
            _tableFields.setLinesVisible(true);
            _tableFields.setHeaderVisible(true);
            _tableFields.setHeaderBackground(headerColor);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.heightHint = 100;
            _tableFields.setLayoutData(data);

            for (int i = 0; i < _titleFields.length; i++) {
               final TableColumn column = new TableColumn(_tableFields, SWT.RIGHT);
               column.setText(_titleFields[i]);
               _tableFields.getColumn(i).pack();
            }
            _containerFieldList.pack();

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

            buttonAddFields = new Button(_containerFieldList, SWT.NONE);
            buttonAddFields.setText(BUTTON_LOADTRACKS_NAME);
            buttonAddFields.setToolTipText(BUTTON_LOADTRACKS_TOOLTIP);
            buttonAddFields.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     //Build list of track RefId, count and total size
                     final UUID uuid = UUID.randomUUID();
                     final CustomField newCustomField = new CustomField("", "", uuid.toString(), CustomFieldType.NONE, "");
                     _addCustomField_ByRefId.put(newCustomField.getRefId(), newCustomField);

                     //now add item to table list
                     final TableItem itemEvent = new TableItem(_tableFields, SWT.NONE);

                     itemEvent.setText(0, newCustomField.getFieldName());
                     final TableEditor editorName = new TableEditor(_tableFields);
                     final Text textName = new Text(_tableFields, SWT.SINGLE);
                     itemEvent.setData(TEXTBOX_NAME_TAG, textName);
                     textName.setData(newCustomField);
                     textName.setText(newCustomField.getFieldName());
                     textName.pack();
                     editorName.horizontalAlignment = SWT.LEFT;
                     editorName.setEditor(textName, itemEvent, 0);
                     editorName.grabHorizontal = true;
                     textName.addVerifyListener(new VerifyListener() {
                        @Override
                        public void verifyText(final VerifyEvent e) {
                           /* Notice how we combine the old and new below */
                           final String currentText = ((Text) e.widget).getText();
                           final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                           final CustomField newField = (CustomField) ((Text) e.widget).getData();
                           newField.setFieldName(valueTxt);
                        }
                     });

                     itemEvent.setText(1, newCustomField.getUnit());
                     final TableEditor editorUnit = new TableEditor(_tableFields);
                     final Text textUnit = new Text(_tableFields, SWT.SINGLE);
                     itemEvent.setData(TEXTBOX_UNIT_TAG, textUnit);
                     textUnit.setData(newCustomField);
                     textUnit.setText(newCustomField.getUnit());
                     textUnit.pack();
                     editorUnit.horizontalAlignment = SWT.LEFT;
                     editorUnit.setEditor(textUnit, itemEvent, 1);
                     editorUnit.grabHorizontal = true;
                     textUnit.addVerifyListener(new VerifyListener() {
                        @Override
                        public void verifyText(final VerifyEvent e) {
                           /* Notice how we combine the old and new below */
                           final String currentText = ((Text) e.widget).getText();
                           final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                           final CustomField newField = (CustomField) ((Text) e.widget).getData();
                           newField.setUnit(valueTxt);
                        }
                     });

                     final TableEditor editorType = new TableEditor(_tableFields);
                     final ToolBar toolBarNew = new ToolBar(_tableFields, SWT.BORDER | SWT.VERTICAL);
                     toolBarNew.setData(newCustomField);
                     final ToolItem itemNew = new ToolItem(toolBarNew, SWT.DROP_DOWN);
                     itemEvent.setData(DROPDOWN_REFID_TAG, toolBarNew);
                     itemEvent.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);
                     //buttonUpdate.setData(DROPDOWN_ITEM_REFID_TAG, itemNew);

                     itemNew.setText(CustomFieldType.NONE.name());
                     final DropdownSelectionListener listenerNew = new DropdownSelectionListener(itemNew);
                     for (final CustomFieldType fieldType : CustomFieldType.values()) {
                        listenerNew.add(fieldType, newCustomField);
                     }
                     itemNew.addSelectionListener(listenerNew);
                     toolBarNew.pack();
                     editorType.minimumWidth = toolBarNew.getSize().x;
                     editorType.minimumHeight = toolBarNew.getSize().y;
                     editorType.horizontalAlignment = SWT.LEFT;
                     editorType.grabHorizontal = true;
                     editorType.grabVertical = true;
                     editorType.setEditor(toolBarNew, itemEvent, 2);

                     itemEvent.setText(3, newCustomField.getDescription());
                     final TableEditor editorDescription = new TableEditor(_tableFields);
                     final Text textDescription = new Text(_tableFields, SWT.SINGLE);
                     itemEvent.setData(TEXTBOX_DESC_TAG, textDescription);
                     textDescription.setData(newCustomField);
                     textDescription.setText(newCustomField.getDescription());
                     textDescription.pack();
                     editorDescription.horizontalAlignment = SWT.LEFT;
                     editorDescription.setEditor(textDescription, itemEvent, 3);
                     editorDescription.grabHorizontal = true;
                     textDescription.addVerifyListener(new VerifyListener() {
                        @Override
                        public void verifyText(final VerifyEvent e) {
                           /* Notice how we combine the old and new below */
                           final String currentText = ((Text) e.widget).getText();
                           final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                           final CustomField newField = (CustomField) ((Text) e.widget).getData();
                           newField.setDescription(valueTxt);
                        }
                     });

                     itemEvent.setText(4, newCustomField.getRefId());
                     final TableEditor editorRefId = new TableEditor(_tableFields);
                     final Text textRefId = new Text(_tableFields, SWT.SINGLE);
                     itemEvent.setData(TEXTBOX_REFID_TAG, textRefId);
                     textRefId.setData(newCustomField);
                     textRefId.setText(newCustomField.getRefId());
                     textRefId.pack();
                     editorRefId.horizontalAlignment = SWT.LEFT;
                     editorRefId.setEditor(textRefId, itemEvent, 4);
                     editorRefId.grabHorizontal = true;
                     textRefId.addVerifyListener(new VerifyListener() {
                        @Override
                        public void verifyText(final VerifyEvent e) {
                           /* Notice how we combine the old and new below */
                           final String currentText = ((Text) e.widget).getText();
                           final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                           final CustomField newField = (CustomField) ((Text) e.widget).getData();
                           newField.setRefId(valueTxt);
                        }
                     });

//                     final TableEditor editor = new TableEditor(_tableFields);
//                     final Button buttonDelete = new Button(_tableFields, SWT.CHECK);
//                     buttonDelete.setData(trackListEntry);
//                     itemEvent.setData(CHECKBOX_DELETE_TAG, buttonDelete);
//
//                     buttonDelete.addSelectionListener(new SelectionAdapter() {
//                        @Override
//                        public void widgetSelected(final SelectionEvent e) {
//                           final Button button = (Button) e.widget;
//                           final Object data = button.getData();
//                           if (data != null) {
//                              final TrackEntry cData = (TrackEntry) data;
//                              if (button.getSelection()) {
//                                 //System.out.println(" Maintenance check for True:" + button.getData());
//                                 if (cData.count > 0) {
//                                    button.setSelection(false);
//                                    MessageDialog.openInformation(
//                                          shell,
//                                          DIALOG_OPEN_TITLE,
//                                          "Can't delete DataSeries with Tours still using it!!!");
//                                    return;
//                                 }
//                                 cData.isDeleted = true;
//                              } else {
//                                 //System.out.println(" Maintenance check for False:" + button.getData());
//                                 cData.isDeleted = false;
//                              }
//                           }
//                        }
//                     });
//                     buttonDelete.pack();
//                     editor.minimumWidth = buttonDelete.getSize().x;
//                     editor.horizontalAlignment = SWT.LEFT;
//                     editor.setEditor(buttonDelete, itemEvent, 1);

//                     final TableEditor editorUpd = new TableEditor(_tableFields);
//                     final Button buttonUpdate = new Button(_tableFields, SWT.CHECK);
//                     buttonUpdate.setData(trackListEntry);
//                     itemEvent.setData(CHECKBOX_UPDATE_TAG, buttonUpdate);
//
//                     buttonUpdate.addSelectionListener(new SelectionAdapter() {
//                        @Override
//                        public void widgetSelected(final SelectionEvent e) {
//                           final Button button = (Button) e.widget;
//                           final Object data = button.getData();
//                           if (data != null) {
//                              final TrackEntry cData = (TrackEntry) data;
//                              if (button.getSelection()) {
//                                 //System.out.println(" Maintenance check for True:" + button.getData());
//                                 cData.isUpdated = true;
//                              } else {
//                                 //System.out.println(" Maintenance check for False:" + button.getData());
//                                 cData.isUpdated = false;
//                              }
//                           }
//                        }
//                     });
//                     buttonUpdate.pack();
//                     editorUpd.minimumWidth = buttonUpdate.getSize().x;
//                     editorUpd.horizontalAlignment = SWT.LEFT;
//                     editorUpd.setEditor(buttonUpdate, itemEvent, 6);

                     _tableFields.pack();
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

      public void add(final CustomFieldType item, final CustomField data) {
         final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
         menuItem.setText(item.name());
         menuItem.setData("Data", data); //$NON-NLS-1$
         menuItem.setData("Type", item); //$NON-NLS-1$
         menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
               final MenuItem selected = (MenuItem) event.widget;
               final CustomField custField = (CustomField) selected.getData("Data"); //$NON-NLS-1$
               final CustomFieldType custFieldType = (CustomFieldType) selected.getData("Type"); //$NON-NLS-1$
               dropdown.setText(selected.getText());
               dropdown.setToolTipText(custField.toString());
               custField.setFieldType(custFieldType);
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


   public ActionAddCustomFields(final ITourProvider tourProvider, final boolean isSaveTour) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider1 = tourProvider;

      _isSaveTour = isSaveTour;

      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16));
      setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16_Disabled));

      setEnabled(false);

      setText(MENU_NAME);
   }

   public ActionAddCustomFields(final ITourProvider2 tourProvider) {

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

   public boolean filterAddedCustomFields(final ArrayList<CustomField> addedCustomField,
                                          final HashMap<String, CustomField> fieldMap) {
      boolean isModified = false;
      for (final CustomField customField : fieldMap.values()) {
         if (customField.getFieldName().isBlank() || customField.getRefId().isBlank()
               || customField.getFieldType().compareTo(CustomFieldType.NONE) == 0) {
            continue;
         }
         //TODO: check =refid for collision
         isModified = true;
         addedCustomField.add(customField);
      }
      return isModified;
   }

   @Override
   public void run() {

      // check if the tour editor contains a modified tour
      if (TourManager.isTourEditorModified()) {
         return;
      }

      final Shell shell = Display.getCurrent().getActiveShell();


      _addCustomField_ByRefId.clear();
      final CustomFieldsEditSettingsDialog dialog = new CustomFieldsEditSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         if (_addCustomField_ByRefId == null || _addCustomField_ByRefId.isEmpty()) {
            System.out.println("ActionAddCustomFields: No CustomFields to Add!!"); //$NON-NLS-1$
            return;
         }
      } else {
         _addCustomField_ByRefId.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<CustomField> addCustomField = new ArrayList<>();

            filterAddedCustomFields(addCustomField, _addCustomField_ByRefId);

            _addCustomField_ByRefId.clear();

            if (addCustomField.size() > 0) {
               if (_isSaveTour) {
                  TourDatabase.addCustomFields(addCustomField);
                  fireCustomFieldChangeEvent();
               }else {

                  /*
                   * don't save the tour's, just update the tour's in data editor
                   */
                  fireCustomFieldChangeEvent();
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
