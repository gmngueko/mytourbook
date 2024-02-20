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

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.data.CustomField;
import net.tourbook.data.CustomFieldType;
import net.tourbook.data.CustomFieldValue;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ActionAddCustomFieldsValues extends Action {
   public static final String           DIALOG_TITLE              = "Create CustomField Value";                  //$NON-NLS-1$
   public static final String           DIALOG_MSG                = "Add Value's";                               //$NON-NLS-1$
   public static final String           TRACKLIST_TITLE           = "List of Added CustomField Value's";         //$NON-NLS-1$
   public static final String           DIALOG_OPEN_TITLE         = "CustomField Value Creation";                //$NON-NLS-1$
   public static final String           DIALOG_OPEN_MSG           = "No CustomField Values to Add";              //$NON-NLS-1$
   public static final String           MENU_NAME                 = "&Add Tours CustomField Value's";            //$NON-NLS-1$
   public static final String           BUTTON_ADDFIELD_NAME      = "Add Values row";                            //$NON-NLS-1$
   public static final String           BUTTON_ADDFIELD_TOOLTIP   = "Add a new CustomField Values to be edited"; //$NON-NLS-1$

   public static final String           COL0_NAME                 = "Name";                                      //$NON-NLS-1$
   public static final String           COL1_NAME                 = "Value";                                     //$NON-NLS-1$
   public static final String           COL2_NAME                 = "Unit";                                      //$NON-NLS-1$
   public static final String           COL3_NAME                 = "Type.";                                     //$NON-NLS-1$
   public static final String           COL4_NAME                 = "CustomField";                               //$NON-NLS-1$

   public static final String           TEXTBOX_UNIT_TAG              = "TEXTBOX_UNIT";                              //$NON-NLS-1$
   public static final String           TEXTBOX_NAME_TAG              = "TEXTBOX_NAME";                              //$NON-NLS-1$
   public static final String           TEXTBOX_VALUE_TAG             = "TEXTBOX_VALUE";                             //$NON-NLS-1$
   public static final String           TEXTBOX_CUSTOMFIELD_TAG       = "TEXTBOX_CUSTOMFIELD";                       //$NON-NLS-1$
   public static final String           TEXTBOX_TYPE_TAG              = "TEXTBOX_TYPE";                              //$NON-NLS-1$
   public static final String           DROPDOWN_CUSTOMFIELD_TAG      = "DROPDOWN_CUSTOMFIELD";                      //$NON-NLS-1$
   public static final String           DROPDOWN_ITEM_CUSTOMFIELD_TAG = "DROPDOWN_CUSTOMFIELD_ITEM";                 //$NON-NLS-1$

   private HashMap<String, CustomField> _usableCustomField_ByRefId   = new HashMap<>();
   private HashMap<String, CustomFieldValue> _addedCustomFieldValue_ByRefId = new HashMap<>();

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

         //Table List of Fields
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
            buttonAddFields.setText(BUTTON_ADDFIELD_NAME);
            buttonAddFields.setToolTipText(BUTTON_ADDFIELD_TOOLTIP);
            buttonAddFields.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     //Build list of track RefId, count and total size
                     //now add item to table list
                     final CustomFieldValue newCustomFieldValue = new CustomFieldValue(null, null, null);
                     final TableItem itemEvent = new TableItem(_tableFields, SWT.NONE);

                     itemEvent.setText(0, UI.EMPTY_STRING);//name

                     itemEvent.setText(1, UI.EMPTY_STRING);//value
                     final TableEditor editorValue = new TableEditor(_tableFields);
                     final Text textValue = new Text(_tableFields, SWT.SINGLE);
                     itemEvent.setData(TEXTBOX_VALUE_TAG, textValue);
                     textValue.setData(newCustomFieldValue);
                     textValue.setText(UI.EMPTY_STRING);
                     textValue.pack();
                     editorValue.horizontalAlignment = SWT.LEFT;
                     editorValue.setEditor(textValue, itemEvent, 1);
                     editorValue.grabHorizontal = true;
                     textValue.addVerifyListener(new VerifyListener() {
                        @Override
                        public void verifyText(final VerifyEvent e) {
                           /* Notice how we combine the old and new below */
                           final String currentText = ((Text) e.widget).getText();
                           final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                           final CustomFieldValue newField = (CustomFieldValue) ((Text) e.widget).getData();
                           if (newField.getCustomField() == null) {
                              e.doit = false;
                           } else {
                              e.doit = newField.setValue(valueTxt);
                           }
                        }
                     });

                     itemEvent.setText(2, UI.EMPTY_STRING);//unit
                     itemEvent.setText(3, UI.EMPTY_STRING);//type

                     final TableEditor editorType = new TableEditor(_tableFields);
                     final ToolBar toolBarNew = new ToolBar(_tableFields, SWT.BORDER | SWT.VERTICAL);
                     toolBarNew.setData(newCustomFieldValue);
                     final ToolItem toolItemNew = new ToolItem(toolBarNew, SWT.DROP_DOWN);
                     itemEvent.setData(DROPDOWN_CUSTOMFIELD_TAG, toolBarNew);
                     itemEvent.setData(DROPDOWN_ITEM_CUSTOMFIELD_TAG, toolItemNew);

                     toolItemNew.setText(CustomFieldType.NONE.name());
                     final DropdownSelectionListener listenerNew = new DropdownSelectionListener(toolItemNew);
                     for (final CustomField fieldType : _usableCustomField_ByRefId.values()) {
                        listenerNew.add(fieldType, newCustomFieldValue, itemEvent);
                     }
                     toolItemNew.addSelectionListener(listenerNew);
                     toolBarNew.pack();
                     editorType.minimumWidth = toolBarNew.getSize().x;
                     editorType.minimumHeight = toolBarNew.getSize().y;
                     editorType.horizontalAlignment = SWT.LEFT;
                     editorType.grabHorizontal = true;
                     editorType.grabVertical = true;
                     editorType.setEditor(toolBarNew, itemEvent, 4);

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

      public void add(final CustomField item, final CustomFieldValue data, final TableItem itemEvent) {
         final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
         menuItem.setText(item.toStringShort());
         menuItem.setToolTipText(item.toString());
         menuItem.setData("Data", data); //$NON-NLS-1$
         menuItem.setData("Item", item); //$NON-NLS-1$
         menuItem.setData("TableItem", itemEvent); //$NON-NLS-1$
         menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
               final MenuItem selected = (MenuItem) event.widget;
               final CustomFieldValue custFieldValue = (CustomFieldValue) selected.getData("Data"); //$NON-NLS-1$
               final CustomField custFieldType = (CustomField) selected.getData("Item"); //$NON-NLS-1$
               final TableItem itemEvent = (TableItem) selected.getData("TableItem"); //$NON-NLS-1$
               if (_addedCustomFieldValue_ByRefId.containsKey(custFieldType.getRefId())) {
                  //customField already present skip
                  return;
               }
               _addedCustomFieldValue_ByRefId.put(custFieldType.getRefId(), custFieldValue);
               itemEvent.setText(0, custFieldType.getFieldName());//name
               itemEvent.setText(2, custFieldType.getUnit());//unit
               itemEvent.setText(3, custFieldType.getFieldType().name());//type
               dropdown.setText(selected.getText());
               dropdown.setToolTipText(custFieldType.toString());
               custFieldValue.setCustomField(custFieldType);
               dropdown.setData(custFieldValue);
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


   public ActionAddCustomFieldsValues(final ITourProvider tourProvider, final boolean isSaveTour) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider1 = tourProvider;

      _isSaveTour = isSaveTour;

      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16));
      setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.Custom_Field_16_Disabled));

      setEnabled(false);

      setText(MENU_NAME);
   }

   public ActionAddCustomFieldsValues(final ITourProvider2 tourProvider) {

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


   @Override
   public void run() {

      // check if the tour editor contains a modified tour
      if (TourManager.isTourEditorModified()) {
         return;
      }

      final Shell shell = Display.getCurrent().getActiveShell();

      final ArrayList<TourData> selectedTours = _tourProvider != null ? _tourProvider.getSelectedTours() : _tourProvider1.getSelectedTours();

      if (selectedTours == null || selectedTours.isEmpty()) {

         // a tour is not selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }

      if (selectedTours.size() != 1) {

         // a tour is not selected
         MessageDialog.openInformation(
               shell,
               DIALOG_OPEN_TITLE,
               "Can only add CustomField Values on One Tour!!!");//$NON-NLS-1$

         return;
      }

      _usableCustomField_ByRefId.clear();
      _addedCustomFieldValue_ByRefId.clear();

      final HashMap<String, CustomField> allCustomFields_ByRefId = TourDatabase.getAllCustomFields_ByRefId();
      final HashMap<String, CustomField> tourCustomFields_byRefId = selectedTours.get(0).getCustomFields_ByRefID();
      for (final CustomField custField : allCustomFields_ByRefId.values()) {
         if (!tourCustomFields_byRefId.containsKey(custField.getRefId())) {
            _usableCustomField_ByRefId.put(custField.getRefId(), custField);
         }
      }

      if (_usableCustomField_ByRefId == null || _usableCustomField_ByRefId.isEmpty()) {

         // No CustomField Values
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

      final CustomFieldsEditSettingsDialog dialog = new CustomFieldsEditSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         if (_addedCustomFieldValue_ByRefId == null || _addedCustomFieldValue_ByRefId.isEmpty()) {
            System.out.println("ActionAddCustomFieldValues: No CustomFields Values to Add!!"); //$NON-NLS-1$
            return;
         }
      } else {
         _usableCustomField_ByRefId.clear();
         _addedCustomFieldValue_ByRefId.clear();
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            if (_addedCustomFieldValue_ByRefId.size() > 0) {
               for (final CustomFieldValue fieldValue : _addedCustomFieldValue_ByRefId.values()) {
                  fieldValue.setTourData(selectedTours.get(0));
               }

               selectedTours.get(0).getCustomFieldValues().addAll(_addedCustomFieldValue_ByRefId.values());
               modifiedTours.add(selectedTours.get(0));
               if (_isSaveTour) {
                  TourManager.saveModifiedTours(modifiedTours);
                  //fireTourChangeEvent(tourData);
               }else {

                  /*
                   * don't save the tour's, just update the tour's in data editor
                   */
                  final TourDataEditorView tourDataEditor = TourManager.getTourDataEditor();
                  if (tourDataEditor != null) {
                     for (final TourData tourData : modifiedTours) {
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
