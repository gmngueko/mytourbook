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
package net.tourbook.tag;

import java.util.Calendar;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.ExtraData;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourTagMaintenance;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.joda.time.DateTime;

/**
 * Dialog to modify Maintenace a {@link TourTag}
 */
public class Dialog_TourTag_Maintenance extends TitleAreaDialog {

   private static final String   ID     = "net.tourbook.tag.Dialog_TourTag_Maintenance"; //$NON-NLS-1$

   private final IDialogSettings _state = TourbookPlugin.getState(ID);

   private String                _dlgMessage;

   private TourTag               _tourTag_Original;
   private TourTag               _tourTag_Clone;

   /*
    * UI controls
    */
   //private Text _txtNotes;
   private Text _txtName;
   private Text _txtExtraHours;
   private Text _txtExtraMonths;

   private Text _txtEventNotes;
   private Text _txtEventCost;

   private org.eclipse.swt.widgets.DateTime _eventDate;

   private Button _buttonAddEvent;
   private Button _buttonDeleteEvents;

   public Dialog_TourTag_Maintenance(final Shell parentShell, final String dlgMessage, final TourTag tourTag) {

      super(parentShell);

      _dlgMessage = dlgMessage;

      _tourTag_Original = tourTag;
      _tourTag_Clone = tourTag.clone();

      // make dialog resizable
      setShellStyle(getShellStyle() | SWT.RESIZE);
   }

   @Override
   protected void configureShell(final Shell shell) {

      super.configureShell(shell);

      // set window title
      shell.setText(Messages.Dialog_TourTag_Title);
   }

   @Override
   public void create() {

      super.create();

      setTitle(Messages.Dialog_TourTag_MaintenanceTag_Title);
      setMessage(_dlgMessage);
   }

   @Override
   protected final void createButtonsForButtonBar(final Composite parent) {

      super.createButtonsForButtonBar(parent);

      // OK -> Save
      getButton(IDialogConstants.OK_ID).setText(Messages.app_action_save);
   }

   @Override
   protected Control createDialogArea(final Composite parent) {

      final Composite dlgContainer = (Composite) super.createDialogArea(parent);

      createUI(dlgContainer);

      restoreState();

      _txtName.selectAll();
      _txtName.setFocus();

      return dlgContainer;
   }

   /**
    * create the drop down menus, this must be created after the parent control is created
    */

   private void createUI(final Composite parent) {

      //Global Maintenance info
      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);
      {
         {
            // Text: Name

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagName);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtName = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtName);
         }
         {
            // Text: ExtraHours

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_ExtraHour);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtExtraHours = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtExtraHours);
            _txtExtraHours.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final int value = Integer.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals("")) {
                        e.doit = false;
                     }
                  }
               }
            });
         }
         {
            // Text: ExtraMonths

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_ExtraMonth);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtExtraMonths = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtExtraMonths);

            _txtExtraMonths.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final int value = Integer.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals("")) {
                        e.doit = false;
                     }
                  }
               }
            });
         }
      }
      //Table List of Events
      final Composite containerEventList = new Composite(parent, SWT.BORDER);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(containerEventList);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(containerEventList);
      {
         final Label label = new Label(containerEventList, SWT.NONE);
         label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventList);
         GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

         final Table table = new Table(containerEventList, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
         table.setLinesVisible(true);
         table.setHeaderVisible(true);
         final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
         data.heightHint = 100;
         table.setLayoutData(data);

         final String[] titles = { Messages.Dialog_TourTag_Label_TagMaintenance_EventSelected,
               Messages.Dialog_TourTag_Label_TagMaintenance_EventDate, Messages.Dialog_TourTag_Label_TagMaintenance_EventCost,
               Messages.Dialog_TourTag_Label_TagMaintenance_EventMeterUsed, Messages.Dialog_TourTag_Label_TagMaintenance_EventMeterUsedDifference,
               Messages.Dialog_TourTag_Label_TagMaintenance_EventTimeUsed, Messages.Dialog_TourTag_Label_TagMaintenance_EventTimeUsedDifference,
               Messages.Dialog_TourTag_Label_TagMaintenance_EventNotes };
         for (int i = 0; i < titles.length; i++) {
            final TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(titles[i]);
            table.getColumn(i).pack();
         }

         for (int i = 0; i <= 10; i++) {
            final TableItem item = new TableItem(table, SWT.NONE);
            item.setText(1, DateTime.now().plusDays(i).toString());
            item.setText(2, String.valueOf(153.7 + i));
            item.setText(3, String.valueOf(200 + i));
            item.setText(4, String.valueOf(100 + i));
            item.setText(5, "100:00:00");
            item.setText(6, "50:00:00");
            item.setText(7, "Test Notes.....");
            final TableEditor editor = new TableEditor(table);
            final Button button = new Button(table, SWT.CHECK);
            button.setData(i);
            button.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(final SelectionEvent e) {
                  final Button button = (Button) e.widget;
                  if (button.getSelection()) {
                     System.out.println(" Maintenance check for True:" + button.getData());
                  } else {
                     System.out.println(" Maintenance check for False:" + button.getData());
                  }
               }
            });
            button.pack();
            editor.minimumWidth = button.getSize().x;
            editor.horizontalAlignment = SWT.LEFT;
            editor.setEditor(button, item, 0);
         }

         for (int i = 0; i < titles.length; i++) {
            table.getColumn(i).pack();
         }
         containerEventList.pack();

         {
            //Add Event Button
            _buttonDeleteEvents = new Button(containerEventList, SWT.NONE);
            _buttonDeleteEvents.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventDelete);
            _buttonDeleteEvents.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     System.out.println("Delete Maintenance Events Button pressed");
                     break;
                  }
               }
            });
         }
      }
      //Specific Event Maintenance info to Add
      final Composite containerEvent = new Composite(parent, SWT.BORDER);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(containerEvent);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(containerEvent);
      {
         {
            //Add Event Button
            _buttonAddEvent = new Button(containerEvent, SWT.NONE);
            _buttonAddEvent.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventAdd);
            _buttonAddEvent.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final Calendar cal = Calendar.getInstance();
                     cal.set(_eventDate.getYear(),
                           _eventDate.getMonth(),
                           _eventDate.getDay(),
                           _eventDate.getHours(),
                           _eventDate.getMinutes());
                     System.out.println("Add Maintenance Event Button pressed for:" + cal.toString() + ";" + _eventDate.toString());
                     break;
                  }
               }
            });
         }
         final Composite containerEventDateCost = new Composite(containerEvent, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(containerEventDateCost);
         GridLayoutFactory.swtDefaults().numColumns(4).applyTo(containerEventDateCost);
         {
            // Text: Event Cost

            final Label label = new Label(containerEventDateCost, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventCost);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtEventCost = new Text(containerEventDateCost, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtEventCost);

            _txtEventCost.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals("")) {
                        e.doit = false;
                     }
                  }
               }
            });
         }
         {
            // Text: Maintenance Event Date

            final Label label = new Label(containerEventDateCost, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventDate);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _eventDate = new org.eclipse.swt.widgets.DateTime(containerEventDateCost, SWT.DATE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_eventDate);
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            //init event date to today/now
            _eventDate.setYear(cal.get(Calendar.YEAR));
            _eventDate.setMonth(cal.get(Calendar.MONTH));
            _eventDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
         }
         final Composite containerEventNotes = new Composite(containerEvent, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(containerEventNotes);
         GridLayoutFactory.swtDefaults().numColumns(2).applyTo(containerEventNotes);
         {
            // Text: Event Notes
            final Label label = new Label(containerEventNotes, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventNotes);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);
            _txtEventNotes = new Text(containerEventNotes,
                  SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL |
                        SWT.H_SCROLL);
            GridDataFactory.fillDefaults()
                  .grab(true, true)
                  .hint(convertWidthInCharsToPixels(100), convertHeightInCharsToPixels(20) / 4)
                  .applyTo(_txtEventNotes);
         }

      }
   }

   @Override
   protected IDialogSettings getDialogBoundsSettings() {

      // keep window size and position
      return _state;
   }

   @Override
   protected void okPressed() {

      // set model from UI
      saveState();

      if (_tourTag_Clone.isValidForSave() == false) {
         return;
      }

      // update original model
      _tourTag_Original.updateFromModified(_tourTag_Clone);

      super.okPressed();
   }

   private void restoreState() {

      _txtName.setText(_tourTag_Clone.getTagName());
      final ExtraData extraData = _tourTag_Clone.getExtraData();
      if (extraData != null && extraData.getMaintenanceInfo() != null) {
         final TourTagMaintenance maintenance = extraData.getMaintenanceInfo();
         _txtExtraHours.setText(String.valueOf(maintenance.getExtraHourUsed()));
         _txtExtraMonths.setText(String.valueOf(maintenance.getExtraMonthUsage()));
      }
      //_txtNotes.setText(_tourTag_Clone.getNotes());
   }

   private void saveState() {

      //_tourTag_Clone.setNotes(_txtNotes.getText());
      _tourTag_Clone.setTagName(_txtName.getText());
      ExtraData extraData = _tourTag_Clone.getExtraData();
      if (extraData == null) {
         extraData = new ExtraData();
         _tourTag_Clone.setExtraData(extraData);
      }
      TourTagMaintenance maintenance = extraData.getMaintenanceInfo();
      if (maintenance == null) {
         maintenance = new TourTagMaintenance();
         extraData.setMaintenanceInfo(maintenance);
      }

      final String extraHour = _txtExtraHours.getText().strip();
      if (extraHour.isEmpty()) {
         maintenance.setExtraHourUsed(0);
      } else {
         maintenance.setExtraHourUsed(Integer.valueOf(extraHour));
      }

      final String extraMonth = _txtExtraMonths.getText().strip();
      if (extraMonth.isEmpty()) {
         maintenance.setExtraMonthUsage(0);
      } else {
         maintenance.setExtraMonthUsage(Integer.valueOf(extraMonth));
      }

   }
}
