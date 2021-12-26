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

import de.byteholder.geoclipse.map.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.tourbook.Messages;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ActionRetrieveCustomTracksCsv extends Action {

   public static final String   TRACKNAME_STEPS_NEXT15MIN    = "Steps next 15min";                                            //$NON-NLS-1$
   public static final String   TRACKNAME_CALORIES_NEXT15MIN = "calories of next 15min";                                      //$NON-NLS-1$
   public static final String   TRACKNAME_DISTANCE_NEXT15MIN = "Distance next 15min";                                         //$NON-NLS-1$
   public static final String   TRACKNAME_HR3                = "Heart Rate 3";                                                //$NON-NLS-1$
   public static final String   TRACKID_STEPS_NEXT15MIN      = "Steps next 15min:334e8f39-9c27-45c2-b8dd-1a314783f568";       //$NON-NLS-1$
   public static final String   TRACKID_CALORIES_NEXT15MIN   = "calories of next 15min:62c0f33d-ee87-480b-ab42-25ea021c00a3"; //$NON-NLS-1$
   public static final String   TRACKID_DISTANCE_NEXT15MIN   = "Distance next 15min:6edf94af-e3b8-4058-986a-c6195d0a734e";    //$NON-NLS-1$
   public static final String   TRACKID_HR3                  = "Heart Rate 3:3e9b32c3-e84b-4dff-b152-e2317053364c";           //$NON-NLS-1$
   public static final String   TRACKUNIT_STEPS_NEXT15MIN    = "pas";                                                         //$NON-NLS-1$
   public static final String   TRACKUNIT_CALORIES_NEXT15MIN = "cal";                                                         //$NON-NLS-1$
   public static final String   TRACKUNIT_DISTANCE_NEXT15MIN = "km";                                                          //$NON-NLS-1$
   public static final String   TRACKUNIT_HR3                = "bpm";                                                         //$NON-NLS-1$
   public static final String   DIALOG_TITLE                 = "Retrieve Custom Tracks from CSV";                             //$NON-NLS-1$
   public static final String   DIALOG_OPEN_TITLE            = "Custom Tracks CSV Retrieval";                                 //$NON-NLS-1$
   public static final String   DIALOG_OPEN_MSG              = "No Tracks to import from CSV";                                //$NON-NLS-1$
   public static final String   DIALOG_MSG                   = "Put Retrieval Custom Tracks CSV";                             //$NON-NLS-1$
   public static final String   TRACKLIST_TITLE              = "List of CSV Tracks";                                          //$NON-NLS-1$
   public static final String   MENU_NAME                    = "&Import Custom Tracks from CSV...";                           //$NON-NLS-1$
   public static final String        COL0_NAME                    = "Selected";                                                    //$NON-NLS-1$
   public static final String        COL1_NAME                    = "CSV Object Type";                                             //$NON-NLS-1$
   public static final String        COL2_NAME                    = "Track Name";                                                  //$NON-NLS-1$
   public static final String        COL3_NAME                    = "Track Id";                                                    //$NON-NLS-1$
   public static final String        COL4_NAME                    = "Track Unit";                                                  //$NON-NLS-1$
   public static final String        COL5_NAME                    = "Start Date";                                                  //$NON-NLS-1$
   public static final String        COL6_NAME                    = "End Date";                                                    //$NON-NLS-1$
   public static final String   BUTTON_OPENFILE_NAME         = "Open CSV(Fitbit) File";                                       //$NON-NLS-1$
   public static final String   BUTTON_OPENFILE_TOOLTIP      = "Open and Parce CSV File(Fitbit)";                             //$NON-NLS-1$

   private TreeMap<Integer, CsvData> csvDataList                  = new TreeMap<>();

   private final ITourProvider2 _tourProvider;

   public class CsvData{
      TreeMap<Long, Float> valueMap;
      String trackId;
      String trackName;
      String trackUnit;
      Integer objType;

      public Float GetInterpolatedValue(final Long epochTime) {
         Float value = null;
         if (valueMap != null && valueMap.size() > 0) {
            if (valueMap.containsKey(epochTime)) {
               return valueMap.get(epochTime);
            } else {
               final Entry<Long, Float> entryLow = valueMap.lowerEntry(epochTime);
               final Entry<Long, Float> entryHigh = valueMap.ceilingEntry(epochTime);
               if (entryLow != null && entryHigh != null) {
                  if (entryLow.getValue() == entryHigh.getValue()) {
                     return entryHigh.getValue();
                  } else {
                     final float weigthDifference = (epochTime - entryLow.getKey()) / (entryHigh.getKey() - entryLow.getKey());
                     final float interpolatedValue = weigthDifference * (entryHigh.getValue() - entryLow.getValue()) + entryLow.getValue();
                     value = interpolatedValue;
                  }
               }
            }
         }
         return value;
      }
   }

   private class CsvEntry {
      long  epochMilliseconds;
      float value;            //value in csv
   }

   private class CustomTracksCsvSettingsDialog extends TitleAreaDialog {

      private Composite _containerTrackList;
      private Table     _tableTracks;
      private String[]  _titleTracks = { COL0_NAME, COL1_NAME, COL2_NAME, COL3_NAME, COL4_NAME, COL5_NAME, COL6_NAME };

      private Button    buttonOpenFile;
      private Text      txtFileName;
      private String    csvFileContent;
      private Text      txtHeader;

      public CustomTracksCsvSettingsDialog(final Shell parentShell) {
         super(parentShell);
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

            //show selected Maintenance events info in the UI
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

            buttonOpenFile = new Button(_containerTrackList, SWT.NONE);
            buttonOpenFile.setText(BUTTON_OPENFILE_NAME);
            buttonOpenFile.setToolTipText(BUTTON_OPENFILE_TOOLTIP);
            buttonOpenFile.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                     final String[] filterNames = new String[] { "Txt Files", "All Files (*)" }; //$NON-NLS-1$ //$NON-NLS-2$
                     final String[] filterExtensions = new String[] { "*.csv;*.txt", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
                     final String filterPath = ""; //$NON-NLS-1$
                     dialog.setFilterNames(filterNames);
                     dialog.setFilterExtensions(filterExtensions);
                     dialog.setFilterPath(filterPath);
                     txtFileName.setText(dialog.open());
                     final Path path = Paths.get(txtFileName.getText());
                     int lineNbr = 0;
                     try {
                        final List<String> csvDocument = Files.readAllLines(path);
                        csvFileContent = UI.EMPTY_STRING;

                        for (final String csvLine : csvDocument) {
                           if (lineNbr == 0) {
                              txtHeader.setText(csvLine);
                              lineNbr++;
                              continue;
                           }
                           csvFileContent += csvLine;
                           final List<String> result = new ArrayList<>(Arrays.asList(csvLine.split("\\s*;\\s*")));
                           final long epochMs = Long.parseLong(result.get(1));
                           final int objType = Integer.parseInt(result.get(3));
                           final float value = Float.parseFloat(result.get(2));
                           if (csvDataList.containsKey(objType)) {
                              csvDataList.get(objType).valueMap.put(epochMs, value);
                           } else {
                              final CsvData newData = new CsvData();
                              newData.objType = objType;
                              newData.valueMap = new TreeMap<>();
                              newData.valueMap.put(epochMs, value);
                              csvDataList.put(objType, newData);
                           }
                           lineNbr++;
                        }

                        final Set<Map.Entry<Integer, CsvData>> csvEentries = csvDataList.entrySet();

                        final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

                        //iterate entries using the forEach
                        csvEentries.forEach(entry -> {
                           //System.out.println(entry.getKey() + "->" + entry.getValue());
                           final TableItem itemEvent = new TableItem(_tableTracks, SWT.NONE);
                           itemEvent.setText(1, String.valueOf(entry.getValue().objType));

                           if (entry.getValue().valueMap.size() > 0) {
                              Calendar calEvent = Calendar.getInstance();
                              calEvent.setTimeInMillis(entry.getValue().valueMap.firstKey());
                              Date dateEvent = calEvent.getTime();
                              itemEvent.setText(5, formatDate.format(dateEvent));

                              calEvent = Calendar.getInstance();
                              calEvent.setTimeInMillis(entry.getValue().valueMap.lastKey());
                              dateEvent = calEvent.getTime();
                              itemEvent.setText(6, formatDate.format(dateEvent));
                           }
                           final TableEditor editor = new TableEditor(_tableTracks);
                           final Button button = new Button(_tableTracks, SWT.CHECK);
                           button.setData(entry.getValue());
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

                           final TableEditor editorId = new TableEditor(_tableTracks);
                           final Text textId = new Text(_tableTracks, SWT.SINGLE);
                           textId.pack();
                           //editorId.minimumWidth = textId.getSize().x;
                           editorId.horizontalAlignment = SWT.LEFT;
                           editorId.setEditor(textId, itemEvent, 3);
                           editorId.grabHorizontal = true;

                           if (entry.getValue().objType == 11) {
                              textId.setText(TRACKID_STEPS_NEXT15MIN);
                           } else if (entry.getValue().objType == 12) {
                              textId.setText(TRACKID_CALORIES_NEXT15MIN);
                           } else if (entry.getValue().objType == 15) {
                              textId.setText(TRACKID_DISTANCE_NEXT15MIN);
                           } else if (entry.getValue().objType == 18) {
                              textId.setText(TRACKID_HR3);
                           }
                        });

                     } catch (final IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        txtFileName.setText(dialog.open() + "=>Exception reading file!!!, line nbr:" + String.valueOf(lineNbr)); //$NON-NLS-1$
                     }
                     break;
                  }
               }
            });

            final GridData dataFile = new GridData();
            dataFile.grabExcessHorizontalSpace = true;
            dataFile.horizontalAlignment = GridData.FILL;
            txtFileName = new Text(_containerTrackList, SWT.BORDER);
            txtFileName.setLayoutData(dataFile);

            final GridData dataFileHeader = new GridData();
            dataFileHeader.grabExcessHorizontalSpace = true;
            dataFileHeader.horizontalAlignment = GridData.FILL;
            txtHeader = new Text(_containerTrackList, SWT.BORDER);
            txtHeader.setLayoutData(dataFileHeader);
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

   public ActionRetrieveCustomTracksCsv(final ITourProvider2 tourProvider) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider = tourProvider;

      setText(MENU_NAME);
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
               Messages.Dialog_RetrieveCustomTracksJson_Dialog_Title,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }

      final CustomTracksCsvSettingsDialog dialog = new CustomTracksCsvSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //TODO
      } else {
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {

               //TODO

               /*
                * if (isDataRetrieved) {
                * modifiedTours.add(tour);
                * }
                */
            }

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
