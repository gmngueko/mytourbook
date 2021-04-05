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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.ExtraData;
import net.tourbook.data.MaintenanceEvent;
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
   private Text                             _txtName;
   private Text                             _txtExtraHours;
   private Text                             _txtExtraMonths;

   private Text                             _txtEventNotes;
   private Text                             _txtEventCost;

   private org.eclipse.swt.widgets.DateTime _eventDate;

   private Button                           _buttonAddEvent;
   private Button                           _buttonDeleteEvents;

   private Composite                        _containerEventList;
   private Table                            _tableEvents;
   private String[]                         _titleEvents = { Messages.Dialog_TourTag_Label_TagMaintenance_EventSelected,
         Messages.Dialog_TourTag_Label_TagMaintenance_EventDate, Messages.Dialog_TourTag_Label_TagMaintenance_EventCost,
         Messages.Dialog_TourTag_Label_TagMaintenance_EventMeterUsed, Messages.Dialog_TourTag_Label_TagMaintenance_EventMeterUsedDifference,
         Messages.Dialog_TourTag_Label_TagMaintenance_EventTimeUsed, Messages.Dialog_TourTag_Label_TagMaintenance_EventTimeUsedDifference,
         Messages.Dialog_TourTag_Label_TagMaintenance_EventNotes };

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
      _containerEventList = new Composite(parent, SWT.BORDER);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerEventList);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_containerEventList);
      {
         final Label label = new Label(_containerEventList, SWT.NONE);
         label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventList);
         GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

         _tableEvents = new Table(_containerEventList, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
         _tableEvents.setLinesVisible(true);
         _tableEvents.setHeaderVisible(true);
         final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
         data.heightHint = 100;
         _tableEvents.setLayoutData(data);

         for (int i = 0; i < _titleEvents.length; i++) {
            final TableColumn column = new TableColumn(_tableEvents, SWT.NONE);
            column.setText(_titleEvents[i]);
            _tableEvents.getColumn(i).pack();
         }
         _containerEventList.pack();

         //show selected Maintenance events info in the UI
         //which makes re-create easy (select->delete->update -> create)
         _tableEvents.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event e) {
               final TableItem[] selection = _tableEvents.getSelection();
               if (selection.length > 0) {
                  final MaintenanceEvent selectEvent = (MaintenanceEvent) selection[0].getData();
                  _txtEventCost.setText(String.valueOf(selectEvent.cost));
                  _txtEventNotes.setText(selectEvent.notes);
                  final Calendar cal = Calendar.getInstance();
                  cal.setTimeInMillis(selectEvent.eventEpochTime * 1000);
                  _eventDate.setYear(cal.get(Calendar.YEAR));
                  _eventDate.setMonth(cal.get(Calendar.MONTH));
                  _eventDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
               }

            }
         });

         {
            //Delete Event Button
            _buttonDeleteEvents = new Button(_containerEventList, SWT.NONE);
            _buttonDeleteEvents.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventDelete);
            _buttonDeleteEvents.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     //System.out.println("Delete Maintenance Events Button pressed");
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
                     for (final Control control : _tableEvents.getChildren()) {
                        if (control instanceof Button) {
                           if (((Button) control).getSelection()) {
                              maintenance.removeEvent((Long) control.getData());
                              control.dispose();
                           }

                        }
                     }
                     restoreState();
                     break;
                  }
               }
            });
         }
         //TODO button to update selected Maintenance event to avoid to ahve to delete and re-create
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
                     //System.out.println("Add Maintenance Event Button pressed for:" + cal.toString() + ";" + _eventDate.toString());
                     final Calendar cal = Calendar.getInstance();
                     cal.set(_eventDate.getYear(),
                           _eventDate.getMonth(),
                           _eventDate.getDay(),
                           _eventDate.getHours(), //TODO set 0?
                           _eventDate.getMinutes());//TODO set to 0?
                     final MaintenanceEvent event = new MaintenanceEvent();
                     event.eventEpochTime = cal.getTimeInMillis() / 1000;
                     event.cost = 0;
                     if (_txtEventCost.getText().length() > 0) {
                        event.cost = Float.valueOf(_txtEventCost.getText());
                     }
                     event.notes = _txtEventNotes.getText();
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
                     maintenance.addEvent(event.eventEpochTime, event);
                     restoreState();
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
         _txtExtraMonths.setText(String.valueOf(maintenance.getExtraLifeMonthUsage()));
      }

      //set UI with Maintenance events
      if (extraData != null && extraData.getMaintenanceInfo() != null
            && extraData.getMaintenanceInfo().getSortedEventsMaintenance() != null) {
         final TreeMap<Long, MaintenanceEvent> eventsMap = extraData.getMaintenanceInfo().getSortedEventsMaintenance();
         _tableEvents.removeAll();
         for (final Control control : _tableEvents.getChildren()) {
            if (control instanceof Button) {
               control.dispose();
            }
         }
         final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
         for (final Map.Entry<Long, MaintenanceEvent> eventEntry : eventsMap.entrySet()) {
            final Long key = eventEntry.getKey();
            final MaintenanceEvent value = eventEntry.getValue();
            final TableItem itemEvent = new TableItem(_tableEvents, SWT.NONE);

            final Calendar calEvent = Calendar.getInstance();
            calEvent.setTimeInMillis(key * 1000);
            final Date dateEvent = calEvent.getTime();
            itemEvent.setText(1, formatDate.format(dateEvent));
            itemEvent.setData(value);

            itemEvent.setText(2, String.valueOf(value.cost));
            itemEvent.setText(3, String.valueOf(value.metersTotalUsed));
            itemEvent.setText(4, String.valueOf(0));//TODO: add
            itemEvent.setText(5,
                  ("" + value.secondsTotalUsed / 3600) + ':' + ((value.secondsTotalUsed / 60) % 60) + ':' + (value.secondsTotalUsed % 60));
            itemEvent.setText(6, "00:00:00");//TODO add
            itemEvent.setText(7, value.notes);

            final TableEditor editor = new TableEditor(_tableEvents);
            final Button button = new Button(_tableEvents, SWT.CHECK);
            button.setData(key);
            button.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(final SelectionEvent e) {
                  final Button button = (Button) e.widget;
                  if (button.getSelection()) {
                     //System.out.println(" Maintenance check for True:" + button.getData());
                  } else {
                     //System.out.println(" Maintenance check for False:" + button.getData());
                  }
               }
            });
            button.pack();
            editor.minimumWidth = button.getSize().x;
            editor.horizontalAlignment = SWT.LEFT;
            editor.setEditor(button, itemEvent, 0);
         }

         for (int i = 0; i < _titleEvents.length; i++) {
            _tableEvents.getColumn(i).pack();
         }
         _containerEventList.pack();
      }
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
         maintenance.setExtraLifeMonthUsage(0);
      } else {
         maintenance.setExtraLifeMonthUsage(Integer.valueOf(extraMonth));
      }
      //set Maintenance events from UI

   }
}
