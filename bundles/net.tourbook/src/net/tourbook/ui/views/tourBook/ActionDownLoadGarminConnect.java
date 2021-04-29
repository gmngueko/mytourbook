/*******************************************************************************
 * Copyright (C) 2005, 2020 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourBook;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.util.ITourViewer3;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.ui.UI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

class ActionDownLoadGarminConnect extends Action {

   private static final String   STATE_SELECTED_PERSON = "selectedPerson";                              //$NON-NLS-1$

   private static final String   PATH_SCRIPT_DAY       = "C:\\Users\\gmngu\\Documents\\MyTourBook\\sel";

   private static final String   PATH_SCRIPT_DAY_SPLIT = "C:\\Users\\gmngu\\Documents\\MyTourBook\\fit";
   private static String         OS                    = System.getProperty("os.name").toLowerCase();
   private final IDialogSettings _state                = TourbookPlugin.getDefault()                    //
         .getDialogSettingsSection("DialogSelectPerson");                                               //$NON-NLS-1$

   private TourBookView          _tourBookView;

   private class dayTour {
      long     epoch;
      String   dayString;
      String   timeStartHHMM;
      String   cmd;
      TourData tour;
      boolean  isChecked = true;
   }

   private class DialogGarminConnectDownLoad extends TitleAreaDialog {
      private final ITourViewer3 _tourViewer;

      TreeMap<Long, dayTour>     mapOfMapToursForDay = new TreeMap<>();

      /*
       * UI controls
       */
      private Composite                        _parent;
      private Composite                        _dlgContainer;

      private org.eclipse.swt.widgets.DateTime _textStartDate;
      private org.eclipse.swt.widgets.DateTime _textEndDate;
      private Text                             _textPathFolder;

      private Text                             _textPathScriptDay;
      private Text                             _textPathScriptDaySplit;

      private Text                             _textResult;

      private Button                           _buttonChooseFolder;
      private Button                           _buttonDownLoadDay;
      private Button                           _buttonSplitDays;

      private Composite                        _containerTourList;
      private Table                            _tableTours;
      private String[]                         _titleTourList = { "Selected", "Tour Description", "Tour Person" };
      private Button                           _buttonAddTours;

      /**
       * @param parentShell
       */
      public DialogGarminConnectDownLoad(final Shell parentShell,
                                         final ITourViewer3 tourViewer) {

         super(parentShell);

         _tourViewer = tourViewer;
      }

      @Override
      public void create() {

         super.create();

         setTitle("Garmin Connect DownLoad Dialog Title");
         setMessage("Garmin Connect DownLoad Workout or Daily Activities");
         //default value
         _textPathScriptDay.setText(PATH_SCRIPT_DAY);
         _textPathScriptDaySplit.setText(PATH_SCRIPT_DAY_SPLIT);

         restoreState();
      }

      @Override
      protected final void createButtonsForButtonBar(final Composite parent) {

         super.createButtonsForButtonBar(parent);

         // set text for the OK button
         //getButton(IDialogConstants.OK_ID).setText(Messages.Dialog_ReimportTours_Button_ReImport);
      }

      @Override
      protected Control createDialogArea(final Composite parent) {

         _parent = parent;

         _dlgContainer = (Composite) super.createDialogArea(parent);

         createUI(_dlgContainer);

         return _dlgContainer;
      }

      private void createUI(final Composite parent) {
         final Composite container = new Composite(parent, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
         GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);

         {
            _buttonChooseFolder = new Button(container, SWT.NONE);
            _buttonChooseFolder.setText("Select Folder");
            _buttonChooseFolder.setToolTipText("Select Folder for the Action");
            _buttonChooseFolder.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final DirectoryDialog dialog = new DirectoryDialog(getShell());
                     final String filterPath = ""; //$NON-NLS-1$
                     dialog.setFilterPath(filterPath);
                     _textPathFolder.setText(dialog.open());

                     break;
                  }
               }
            });
            final GridData dataFile = new GridData();
            dataFile.grabExcessHorizontalSpace = true;
            dataFile.horizontalAlignment = GridData.FILL;
            _textPathFolder = new Text(container, SWT.BORDER);
            _textPathFolder.setLayoutData(dataFile);
         }
         {
            // Text: Sript Day Path

            final Label label = new Label(container, SWT.NONE);
            label.setText("script Day path:");
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _textPathScriptDay = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textPathScriptDay);
         }
         {
            // Text: Sript DaySplit Path

            final Label label = new Label(container, SWT.NONE);
            label.setText("script DaySplit path:");
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _textPathScriptDaySplit = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textPathScriptDaySplit);
         }
         {
            final Composite containerDate = new Composite(parent, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(containerDate);
            GridLayoutFactory.swtDefaults().numColumns(4).applyTo(containerDate);
            {
               // Text: start Date

               final Label label = new Label(containerDate, SWT.NONE);
               label.setText("start Date");
               GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

               _textStartDate = new org.eclipse.swt.widgets.DateTime(containerDate, SWT.DATE);
               GridDataFactory.fillDefaults().grab(true, false).applyTo(_textStartDate);
               final Calendar cal = Calendar.getInstance();
               cal.setTimeInMillis(System.currentTimeMillis());
               //init event date to today/now
               _textStartDate.setYear(cal.get(Calendar.YEAR));
               _textStartDate.setMonth(cal.get(Calendar.MONTH));
               _textStartDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
            }

            {
               // Text: start Date

               final Label label = new Label(containerDate, SWT.NONE);
               label.setText("end Date");
               GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

               _textEndDate = new org.eclipse.swt.widgets.DateTime(containerDate, SWT.DATE);
               GridDataFactory.fillDefaults().grab(true, false).applyTo(_textEndDate);
               final Calendar cal = Calendar.getInstance();
               cal.setTimeInMillis(System.currentTimeMillis());
               //init event date to today/now
               _textEndDate.setYear(cal.get(Calendar.YEAR));
               _textEndDate.setMonth(cal.get(Calendar.MONTH));
               _textEndDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
            }
         }
         //Table List of Events
         _containerTourList = new Composite(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerTourList);
         GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_containerTourList);

         {
            final Label label = new Label(_containerTourList, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_EventList);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
            final Color headerColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            _tableTours = new Table(_containerTourList, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
            _tableTours.setLinesVisible(true);
            _tableTours.setHeaderVisible(true);
            _tableTours.setHeaderBackground(headerColor);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.heightHint = 100;
            data.grabExcessHorizontalSpace = true;
            _tableTours.setLayoutData(data);

            for (int i = 0; i < _titleTourList.length; i++) {
               final TableColumn column = new TableColumn(_tableTours, SWT.RIGHT);
               column.setText(_titleTourList[i]);
               if (i > 0) {
                  _tableTours.getColumn(i).setAlignment(SWT.LEFT);
               }
               _tableTours.getColumn(i).pack();
            }
            _containerTourList.pack();

            {
               //Add Tours Button
               _buttonAddTours = new Button(_containerTourList, SWT.NONE);
               _buttonAddTours.setText("Add Tours in Selected Date Range");
               _buttonAddTours.addListener(SWT.Selection, new Listener() {
                  @Override
                  public void handleEvent(final Event e) {
                     switch (e.type) {
                     case SWT.Selection:
                        //clean table
                        _tableTours.removeAll();
                        for (final Control control : _tableTours.getChildren()) {
                           if (control instanceof Button) {
                              control.dispose();
                           }
                        }

                        //compute list of tours in the time range
                        final Calendar calStart = Calendar.getInstance();
                        calStart.set(_textStartDate.getYear(),
                              _textStartDate.getMonth(),
                              _textStartDate.getDay(),
                              0,
                              0);
                        final Calendar calEnd = Calendar.getInstance();
                        calEnd.set(_textEndDate.getYear(),
                              _textEndDate.getMonth(),
                              _textEndDate.getDay(),
                              0,
                              0);
                        final long personId = _state.getLong(STATE_SELECTED_PERSON);
                        final ArrayList<Long> tourIds;
                        if (personId != 0) {
                           tourIds = TourDatabase.getAllTourIds_BetweenTwoDates_ForPerson(calStart.getTimeInMillis(),
                                 calEnd.getTimeInMillis(),
                                 personId);
                        } else {
                           tourIds = TourDatabase.getAllTourIds_BetweenTwoDates(calStart.getTimeInMillis(),
                                 calEnd.getTimeInMillis());
                        }

                        mapOfMapToursForDay.clear();
                        for (final Long ids : tourIds) {
                           final TourData tourData = TourDatabase.getTourFromDb(ids);
                           final String patternDate = "yyyy-MM-dd";
                           final SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat(patternDate);
                           final String patternTime = "HH:mm";
                           final SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat(patternTime);
                           final Calendar calTourStart = Calendar.getInstance();
                           calTourStart.setTimeInMillis(tourData.getTourStartTimeMS());
                           final Date dateTourStart = calTourStart.getTime();
                           final dayTour dayTourEntry = new dayTour();
                           dayTourEntry.epoch = tourData.getTourStartTimeMS();
                           dayTourEntry.dayString = simpleDateFormatDate.format(dateTourStart);
                           dayTourEntry.tour = tourData;
                           dayTourEntry.timeStartHHMM = simpleDateFormatTime.format(dateTourStart);
                           final Calendar calTourEnd = Calendar.getInstance();
                           calTourEnd.setTimeInMillis(tourData.getTourEndTimeMS());
                           final Date dateTourEnd = calTourEnd.getTime();
                           String cmd = dayTourEntry.timeStartHHMM + ";";
                           cmd += simpleDateFormatTime.format(dateTourEnd);
                           String tourTypeName = "";
                           if (tourData.getTourType() != null) {
                              if (tourData.getTourType().getName().toLowerCase().contains("cycling")) {
                                 cmd += "=bike";
                              } else if (tourData.getTourType().getName().toLowerCase().contains("running")) {
                                 cmd += "=run";
                              } else if (tourData.getTourType().getName().toLowerCase().contains("fitness")) {
                                 cmd += "=bike";
                              }
                              tourTypeName = tourData.getTourType().getName();
                           }
                           dayTourEntry.cmd = cmd;
                           mapOfMapToursForDay.put(tourData.getTourStartTimeMS(), dayTourEntry);
                           //populate table
                           final TableItem itemTour = new TableItem(_tableTours, SWT.NONE);

                           itemTour.setText(1, String.valueOf(dayTourEntry.dayString + "/" + dayTourEntry.timeStartHHMM + "/" + tourTypeName));
                           itemTour.setText(2, tourData.getDataPerson().getName());
                           final TableEditor editor = new TableEditor(_tableTours);
                           final Button button = new Button(_tableTours, SWT.CHECK);
                           button.setData(dayTourEntry);
                           button.setSelection(true);
                           button.addSelectionListener(new SelectionAdapter() {
                              @Override
                              public void widgetSelected(final SelectionEvent e) {
                                 final Button button = (Button) e.widget;
                                 final dayTour entry = (dayTour) button.getData();
                                 entry.isChecked = button.getSelection();
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
                           editor.setEditor(button, itemTour, 0);
                        }

                        for (int i = 0; i < _titleTourList.length; i++) {
                           _tableTours.getColumn(i).pack();
                        }
                        //_containerTourList.pack();

                        break;
                     }
                  }
               });
            }
         }
         {
            final Composite containerButton = new Composite(parent, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(containerButton);
            GridLayoutFactory.swtDefaults().numColumns(2).applyTo(containerButton);
            {
               _buttonDownLoadDay = new Button(containerButton, SWT.NONE);
               _buttonDownLoadDay.setText("DownLoad Activities For Days");
               _buttonDownLoadDay.setToolTipText("DownLoad Activities for the selected Date span");
               _buttonDownLoadDay.addListener(SWT.Selection, new Listener() {
                  @Override
                  public void handleEvent(final Event e) {
                     switch (e.type) {
                     case SWT.Selection:
                        //call shell script
                        final String pattern = "yyyy-MM-dd";
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        final Calendar calStart = Calendar.getInstance();
                        calStart.set(_textStartDate.getYear(),
                              _textStartDate.getMonth(),
                              _textStartDate.getDay(),
                              0,
                              0);
                        final Calendar calEnd = Calendar.getInstance();
                        calEnd.set(_textEndDate.getYear(),
                              _textEndDate.getMonth(),
                              _textEndDate.getDay(),
                              0,
                              0);
                        final Date sDate = calStart.getTime();
                        String dayListChain = simpleDateFormat.format(sDate);
                        final Calendar nextDay = calStart.getInstance();
                        nextDay.add(Calendar.DAY_OF_MONTH, 1);
                        while (nextDay.before(calEnd)) {
                           dayListChain += "_" + simpleDateFormat.format(nextDay);
                           nextDay.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        _textResult.setText(UI.EMPTY_STRING);
                        try {
                           if (_textPathFolder.getText().isBlank()) {
                              _textResult.setText("Error: Download directory is empty !!!!");
                              return;
                           }
                           if (_textPathScriptDay.getText().isBlank()) {
                              _textResult.setText("Error: Script directory is empty !!!!");
                              return;
                           }
                           if (OS.contains("win")) {
                              Process process;
                              _textResult.setText("STARTED");
                              process = Runtime.getRuntime().exec("cmd.exe /c start /wait GarminDayAct.bat " + _textPathFolder.getText() + " "
                                    + dayListChain,
                                    null,
                                    new File(_textPathScriptDay.getText()));

                              final StringBuilder output = new StringBuilder();

                              final BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()));

                              String line;
                              while ((line = reader.readLine()) != null) {
                                 output.append(line + "\n");
                              }

                              final int exitVal = process.waitFor();
                              _textResult.setText("DONE with exit value=" + exitVal + UI.NEW_LINE2 + output);
                              if (exitVal == 0) {
                                 System.out.println("Success! Executing Script");
                                 System.out.println(output);
                              } else {
                                 System.out.println("Failure! Executing Script");
                                 System.out.println(output);
                                 _textResult.setText("Failure! Executing Script");
                                 return;
                              }
                           } else {
                              _textResult.setText("only windows supported");
                              return;
                           }

                        } catch (final IOException e1) {
                           // TODO Auto-generated catch block
                           e1.printStackTrace();
                           _textResult.setText("exception:" + e1.getMessage());
                        } catch (final InterruptedException e1) {
                           // TODO Auto-generated catch block
                           e1.printStackTrace();
                           _textResult.setText("interupt exception:" + e1.getMessage());
                        }

                        _textResult.setText("Day Download Done");
                        break;
                     }
                  }
               });
            }
            {
               _buttonSplitDays = new Button(containerButton, SWT.NONE);
               _buttonSplitDays.setText("Split each Days Per Activity");
               _buttonSplitDays.setToolTipText("Split Days per Selected Tours in List");
               _buttonSplitDays.addListener(SWT.Selection, new Listener() {
                  @Override
                  public void handleEvent(final Event e) {
                     switch (e.type) {
                     case SWT.Selection:
                        //TODO: call shell script
                        _textResult.setText(UI.EMPTY_STRING);
                        String finalResult = "";

                        if (_textPathFolder.getText().isBlank()) {
                           _textResult.setText("Error: Download directory is empty !!!!");
                           return;
                        }
                        if (_textPathScriptDaySplit.getText().isBlank()) {
                           _textResult.setText("Error: Day Split Script directory is empty !!!!");
                           return;
                        }
                        if (mapOfMapToursForDay.isEmpty()) {
                           _textResult.setText("Error: No Days Selected, First Select Days to Split!!");
                           return;
                        }

                        //pre-processing script
                        if (OS.contains("win")) {
                           Process process;
                           //_textResult.setText("STARTED");
                           try {
                              process = Runtime.getRuntime().exec("cmd.exe /c start /wait preProcessing.bat " + "\"" + _textPathFolder.getText()
                                    + FileSystems
                                          .getDefault().getSeparator() + "\"",
                                    null,
                                    new File(_textPathScriptDaySplit.getText()));
                              final int exitVal = process.waitFor();
                              if (exitVal == 0) {
                                 System.out.println("Success! Executing Split pre Script");
                                 finalResult += "Success! Executing Split pre Script";
                                 //System.out.println(output);
                              } else {
                                 System.out.println("Failure! Executing Split pre Script");
                                 _textResult.setText("Failure! Executing Split pre Script");
                                 //System.out.println(output);
                                 return;
                              }
                           } catch (final IOException e1) {
                              // TODO Auto-generated catch block
                              e1.printStackTrace();
                              _textResult.setText("exception:" + e1.getMessage());
                              return;
                           } catch (final InterruptedException e1) {
                              // TODO Auto-generated catch block
                              e1.printStackTrace();
                              _textResult.setText("InterruptedException:" + e1.getMessage());
                              return;
                           }
                        } else {
                           _textResult.setText("ONLY windows OS supported");
                           return;
                        }

                        //main split process
                        //first gather commands list
                        String curCmd = "";
                        String prevDay = "";
                        int cntEntry = 0;
                        final TreeMap<String, String> listCmdDay = new TreeMap<>();
                        for (final Entry<Long, dayTour> entry : mapOfMapToursForDay.entrySet()) {
                           cntEntry++;
                           if (!prevDay.isBlank() && !prevDay.equalsIgnoreCase(entry.getValue().dayString)) {
                              if (curCmd.isBlank()) {
                                 curCmd = "00:00;23:59";
                              }
                              listCmdDay.put(prevDay, curCmd);
                              prevDay = entry.getValue().dayString;
                              if (entry.getValue().isChecked) {
                                 curCmd = entry.getValue().cmd;
                              } else {
                                 curCmd = "";
                              }
                           } else if (prevDay.isBlank()) {
                              prevDay = entry.getValue().dayString;
                              curCmd = "";
                              if (entry.getValue().isChecked) {
                                 curCmd = entry.getValue().cmd;
                              }
                           } else {
                              if (entry.getValue().isChecked) {
                                 curCmd += ";" + entry.getValue().cmd;
                              }
                           }
                           if (cntEntry == mapOfMapToursForDay.size()) {
                              if (curCmd.isBlank()) {
                                 curCmd = "00:00;23:59";
                              }
                              listCmdDay.put(prevDay, curCmd);
                           }
                        }

                        //second execute script
                        for (final Entry<String, String> entryDay : listCmdDay.entrySet()) {
                           if (OS.contains("win")) {
                              Process process;
                              //_textResult.setText("STARTED");
                              try {
                                 process = Runtime.getRuntime().exec("cmd.exe /c start /wait FitDaySingle2CSV.bat " + "\"" + _textPathFolder.getText()
                                       + FileSystems
                                             .getDefault().getSeparator()
                                       + entryDay.getKey() + ".zip" + "\" "
                                       + "\"" + entryDay.getValue() + "\"",
                                       null,
                                       new File(_textPathScriptDaySplit.getText()));
                                 final int exitVal = process.waitFor();
                                 if (exitVal == 0) {
                                    System.out.println("Success! Executing Split Script");
                                    finalResult += UI.NEW_LINE + "Success! Executing Split Script:"
                                          + entryDay.getKey() + ".zip "
                                          + entryDay.getValue();
                                    //System.out.println(output);
                                 } else {
                                    System.out.println("Failure! Executing Split Script");
                                    _textResult.setText("Failure! Executing Split Script:" + entryDay.getKey());
                                    //System.out.println(output);
                                    return;
                                 }
                              } catch (final IOException e1) {
                                 // TODO Auto-generated catch block
                                 e1.printStackTrace();
                                 _textResult.setText("exception:" + e1.getMessage());
                                 return;
                              } catch (final InterruptedException e1) {
                                 // TODO Auto-generated catch block
                                 e1.printStackTrace();
                                 _textResult.setText("InterruptedException:" + e1.getMessage());
                                 return;
                              }
                           } else {
                              _textResult.setText("ONLY windows OS supported");
                              return;
                           }

                        }

                        //post processing
                        if (OS.contains("win")) {
                           Process process;
                           //_textResult.setText("STARTED");
                           try {
                              process = Runtime.getRuntime().exec("cmd.exe /c start /wait postProcessing.bat " + "\"" + _textPathFolder.getText()
                                    + FileSystems
                                          .getDefault().getSeparator() + "\"",
                                    null,
                                    new File(_textPathScriptDaySplit.getText()));
                              final int exitVal = process.waitFor();
                              if (exitVal == 0) {
                                 System.out.println("Success! Executing Split post Script");
                                 finalResult += UI.NEW_LINE + "Success! Executing Split post Script";
                                 //System.out.println(output);
                              } else {
                                 System.out.println("Failure! Executing Split post Script");
                                 _textResult.setText("Failure! Executing Split post Script");
                                 //System.out.println(output);
                                 return;
                              }
                           } catch (final IOException e1) {
                              // TODO Auto-generated catch block
                              e1.printStackTrace();
                              _textResult.setText("exception:" + e1.getMessage());
                              return;
                           } catch (final InterruptedException e1) {
                              // TODO Auto-generated catch block
                              e1.printStackTrace();
                              _textResult.setText("InterruptedException:" + e1.getMessage());
                              return;
                           }
                        } else {
                           _textResult.setText("ONLY windows OS supported");
                           return;
                        }

                        _textResult.setText(finalResult);
                        break;
                     }
                  }
               });
            }
            {
               // Text: Notes

               final Label label = new Label(containerButton, SWT.NONE);
               label.setText("Script Result:");
               GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);

               _textResult = new Text(containerButton, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
               GridDataFactory.fillDefaults()
                     .grab(true, true)
                     .hint(convertWidthInCharsToPixels(100), convertHeightInCharsToPixels(20))
                     .applyTo(_textResult);
            }
         }
      }

      @Override
      protected boolean isResizable() {
         return true;
      }

      @Override
      protected void okPressed() {
         saveState();
         super.okPressed();
      }

      private void restoreState() {

      }

      private void saveState() {

      }

   }

   /**
    * @param tourBookView
    */
   public ActionDownLoadGarminConnect(final TourBookView tourBookView) {

      super();

      _tourBookView = tourBookView;

      setText("Garmin Connect Download");

   }

   @Override
   public void run() {

      final Shell shell = Display.getCurrent().getActiveShell();

      final DialogGarminConnectDownLoad dialog = new DialogGarminConnectDownLoad(shell, _tourBookView);
      dialog.create();
      if (dialog.open() == Window.OK) {

      } else {
         return;
      }
   }
}
