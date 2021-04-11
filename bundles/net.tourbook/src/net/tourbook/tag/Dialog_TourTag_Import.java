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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.ExtraData;
import net.tourbook.data.MaintenanceEvent;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourTagMaintenance;
import net.tourbook.database.TourDatabase;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog to modify a {@link TourTag}
 */
public class Dialog_TourTag_Import extends TitleAreaDialog {

   private static final String      ID                           = "net.tourbook.tag.Dialog_TourTag_Import"; //$NON-NLS-1$

   private static final char        CSV_TOKEN_SEPARATOR_CHAR     = ';';

   private static final String      CSV_COLUMN_ID                = "Id";
   private static final String      CSV_COLUMN_NAME              = "Name";
   private static final String      CSV_COLUMN_MODEL             = "Model";
   private static final String      CSV_COLUMN_BRAND             = "Brand";
   private static final String      CSV_COLUMN_DATEPURCHASED     = "DatePurchased";
   private static final String      CSV_COLUMN_TYPE              = "Type";
   private static final String      CSV_COLUMN_EXPECTEDLIFEKM    = "ExpectedLifeKilometers";
   private static final String      CSV_COLUMN_EXPECTEDLIFEHOURS = "ExpectedLifeHours";
   private static final String      CSV_COLUMN_EXTRAKMUSED       = "ExtraKilometersUsed";
   private static final String      CSV_COLUMN_WEIGHTKG          = "WeightKilograms";
   private static final String      CSV_COLUMN_PURCHASELOCATION  = "PurchaseLocation";
   private static final String      CSV_COLUMN_PURCHASEPRICE     = "PurchasePrice";
   private static final String      CSV_COLUMN_NOTES             = "Notes";
   private static final String      CSV_COLUMN_INUSE             = "InUse";
   private static final String      CSV_COLUMN_MAINTENANCE       = "Maintenance";

   private final IDialogSettings    _state                       = TourbookPlugin.getState(ID);

   private String                   _dlgMessage;

   private HashMap<Long, TourTag>   _allTourTags                 = new HashMap<>();
   private ArrayList<CsvTag>        _allCsvTags                  = new ArrayList<>();
   private HashMap<String, CsvTag>  _mapIdCsvTags                = new HashMap<>();
   private HashMap<String, TourTag> _mapIdTourTags               = new HashMap<>();

   private HashMap<String, Integer> _mapHeaderIndex              = new HashMap<>();

   /*
    * UI controls
    */
   private Text   _txtFileName;
   private Button _buttonOpenFile;
   private Text   _txtFileInfo;

   static public class CsvMaintenanceEvent {
      public String  MyDateTime;
      public String  MyNotes;
      public Float   MyCost;
      public Boolean MyIsScheduledMaintainance;
      public String  MyTimeSpanUsed;
      public Float   MyMetersUsed;
      public Long    MyDateTimeEpoch;
   }

   static public class CsvMaintenanceInfo {

      public Boolean MyMonitorEndOfLife;

      public String  MyScheduleNotes;
      public String  MyScheduleDistanceMeters;
      public String  MyScheduleTimeSpan;
      public String  MyScheduleMonths;
      public Float   MyExtraHoursUsed;
      public Float   MyExtraMonthsUsed;

      //public ObjectNode MyMaintainanceEvents;
      public JsonNode MyMaintainanceEvents;
   }

   public class CsvTag {
      public String                   name;
      public String                   notes;
      public String                   id;
      public String                   maintenance;
      public Float                    expectedLifeKilometers;
      public Float                    expectedLifeHours;
      public Float                    extraKilometersUsed;
      public Float                    weightKilograms;

      public String                   purchasePrice;
      public String                   purchaseLocation;
      public long                     datePurchased           = 0;

      public String                   brand;
      public String                   model;
      public Integer                  inUse;
      public String                   type;

      public Float                    scheduleDistanceMeters  = null;
      public Integer                  scheduleTimeSpanSeconds = null;
      public Float                    scheduleLifeMonths      = null;

      CsvMaintenanceInfo              maintenanceInfo;
      TreeMap<Long, MaintenanceEvent> maintenanceEvents       = new TreeMap<>();
   }

   public Dialog_TourTag_Import(final Shell parentShell, final String dlgMessage) {

      super(parentShell);

      _dlgMessage = dlgMessage;

      // make dialog resizable
      setShellStyle(getShellStyle() | SWT.RESIZE);
   }

   /*
    * parse input csv file
    * @return the List<String[]> for each line in the file
    */
   public static List<String[]> parseCSVFile(final String importFilePath) {
      final CSVParser parser = new CSVParserBuilder().withSeparator(CSV_TOKEN_SEPARATOR_CHAR).build();
      final Path myImportFilePath = Paths.get(importFilePath);
      List<String[]> listCSVLinesToken = new ArrayList<>();
      try (var bufferReader = Files.newBufferedReader(myImportFilePath, StandardCharsets.UTF_8);
            CSVReader csvReader = new CSVReaderBuilder(bufferReader).withCSVParser(parser).build();) {

         listCSVLinesToken = csvReader.readAll();
         csvReader.close();
      } catch (final IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (final CsvException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return listCSVLinesToken;
   }

   /*
    * parse input timespan string e.g: "12.5:00:15.25"
    * @return the Integer number of seconds corresponding to the span or null
    */
   public static Integer parseTimeSpan(final String timespanString) {
      if (timespanString == null || timespanString.isBlank()) {
         return null;
      }
      Integer timeSpanSeconds = 0;

      final String[] listElement = timespanString.split(":");
      if (listElement.length != 3) {
         return null;
      }

      try {
         //parse day.hours index [0]
         final String[] daysHours = listElement[0].split(".");
         if (daysHours.length == 2) {
            timeSpanSeconds = (Integer.valueOf(daysHours[0]) * 24 + Integer.valueOf(daysHours[1])) * 3600;
         } else if (daysHours.length == 1) {
            timeSpanSeconds = (Integer.valueOf(daysHours[0])) * 3600;
         } else {
            return null;
         }
         //parse minutes index [1]
         timeSpanSeconds += (Integer.valueOf(listElement[1])) * 60;
         //parse seconds index [2]
         float seconds = Float.parseFloat(listElement[2]);
         seconds = Math.round(seconds);
         timeSpanSeconds += (int) seconds;
      } catch (final Exception exc) {
         exc.printStackTrace();
         timeSpanSeconds = null;
      }
      return timeSpanSeconds;
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

      setTitle(Messages.Dialog_TourTag_ImportTag_Title);
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

      _txtFileName.selectAll();
      _txtFileName.setFocus();

      return dlgContainer;
   }

   /**
    * create the drop down menus, this must be created after the parent control is created
    */

   private void createUI(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);
      {
         {
            // Text: FileName

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_ImportTag_FileName);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtFileName = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtFileName);
         }
         {
            // Text: FileInfo

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_ImportTag_FileInfo);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtFileInfo = new Text(container, SWT.BORDER);
            _txtFileInfo.setEditable(false);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtFileInfo);
         }
         {
            // Button: OpenFile
            _buttonOpenFile = new Button(container, SWT.NONE);
            _buttonOpenFile.setText(Messages.Dialog_TourTag_Label_ImportTag_ChoseFile);
            _buttonOpenFile.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                     final String[] filterNames = new String[] { "Txt Files", "All Files (*)" };
                     final String[] filterExtensions = new String[] { "*.csv;*.txt", "*" };
                     final String filterPath = "";
                     dialog.setFilterNames(filterNames);
                     dialog.setFilterExtensions(filterExtensions);
                     dialog.setFilterPath(filterPath);
                     _txtFileName.setText(dialog.open());
                     parseCsvTags(_txtFileName.getText());
                     break;
                  }
               }
            });
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

      super.okPressed();
   }

   private void parseCsvTags(final String csvFile) {
      final List<String[]> csvParseResult = parseCSVFile(csvFile);

      String info = "nbr Entries:" + (csvParseResult.size() - 1) + ";";
      int numFaultLines = 0;

      if (csvParseResult.size() > 1) {
         //Map header to index
         String[] allToken = csvParseResult.get(0);
         for (int idx = 0; idx < allToken.length; idx++) {
            _mapHeaderIndex.put(allToken[idx], idx);
         }

         //parse each line to get tourtags informations
         for (int line = 1; line < csvParseResult.size(); line++) {
            Integer idx = _mapHeaderIndex.get(CSV_COLUMN_ID);
            if (idx == null) {
               numFaultLines++;
               continue;
            }

            allToken = csvParseResult.get(line);
            final String lineId = allToken[idx];
            idx = _mapHeaderIndex.get(CSV_COLUMN_NAME);
            if (idx == null) {
               numFaultLines++;
               continue;
            }
            final String lineName = allToken[idx];
            final CsvTag newTag = new CsvTag();
            newTag.id = lineId;
            newTag.name = lineName;
            idx = _mapHeaderIndex.get(CSV_COLUMN_NOTES);
            if (idx != null) {
               newTag.notes = allToken[idx];
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_EXTRAKMUSED);
            if (idx != null) {
               newTag.extraKilometersUsed = Float.valueOf(allToken[idx]);
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_BRAND);
            if (idx != null) {
               newTag.brand = allToken[idx];
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_DATEPURCHASED);
            if (idx != null) {
               if (!allToken[idx].isBlank() && !allToken[idx].startsWith("000")) {
                  final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                  try {
                     final Date date = formatter.parse(allToken[idx].replaceAll("Z$", "+0000"));
                     newTag.datePurchased = date.getTime() / 1000;
                  } catch (final ParseException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               }
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_EXPECTEDLIFEHOURS);
            if (idx != null) {
               if (allToken[idx].isBlank()) {
                  newTag.expectedLifeHours = null;
               } else {
                  newTag.expectedLifeHours = Float.valueOf(allToken[idx]);
               }
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_EXPECTEDLIFEKM);
            if (idx != null) {
               if (allToken[idx].isBlank()) {
                  newTag.expectedLifeKilometers = null;
               } else {
                  newTag.expectedLifeKilometers = Float.valueOf(allToken[idx]);
               }
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_EXTRAKMUSED);
            if (idx != null) {
               if (allToken[idx].isBlank()) {
                  newTag.extraKilometersUsed = (float) 0;
               } else {
                  newTag.extraKilometersUsed = Float.valueOf(allToken[idx]);
               }
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_INUSE);
            if (idx != null) {
               if (allToken[idx].isBlank()) {
                  newTag.inUse = 0;
               } else {
                  newTag.inUse = Integer.valueOf(allToken[idx]);
               }
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_MODEL);
            if (idx != null) {
               newTag.model = allToken[idx];
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_PURCHASELOCATION);
            if (idx != null) {
               newTag.purchaseLocation = allToken[idx];
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_PURCHASEPRICE);
            if (idx != null) {
               newTag.purchasePrice = allToken[idx];
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_WEIGHTKG);
            if (idx != null) {
               if (allToken[idx].isBlank()) {
                  newTag.weightKilograms = null;
               } else {
                  newTag.weightKilograms = Float.valueOf(allToken[idx]);
               }
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_TYPE);
            if (idx != null) {
               newTag.type = allToken[idx];
            }

            idx = _mapHeaderIndex.get(CSV_COLUMN_MAINTENANCE);
            if (idx != null) {
               newTag.maintenance = allToken[idx];
            }

            //parse maintenance json string
            if (newTag.maintenance != null && newTag.maintenance.length() > 0) {
               final ObjectMapper mapper = new ObjectMapper();
               try {
                  final CsvMaintenanceInfo csvMaintenanceInfo = mapper.readValue(newTag.maintenance, new TypeReference<CsvMaintenanceInfo>() {});
                  newTag.maintenanceInfo = csvMaintenanceInfo;

                  try {
                     newTag.scheduleDistanceMeters = Float.parseFloat(csvMaintenanceInfo.MyScheduleDistanceMeters);
                  } catch (final Exception exc) {
                     //TODO
                     exc.printStackTrace();
                     newTag.scheduleDistanceMeters = null;
                  }
                  try {
                     newTag.scheduleLifeMonths = Float.parseFloat(csvMaintenanceInfo.MyScheduleMonths);
                  } catch (final Exception exc) {
                     //TODO
                     exc.printStackTrace();
                     newTag.scheduleLifeMonths = null;
                  }
                  newTag.scheduleTimeSpanSeconds = parseTimeSpan(csvMaintenanceInfo.MyScheduleTimeSpan);

                  final Iterator<Entry<String, JsonNode>> nodes = csvMaintenanceInfo.MyMaintainanceEvents.fields();
                  while (nodes.hasNext()) {
                     final Map.Entry<String, JsonNode> entry = nodes.next();
                     if (entry.getValue().toString().isBlank()) {
                        continue;
                     }
                     final CsvMaintenanceEvent csvMaintenanceEvent = mapper.readValue(entry.getValue().toString(),
                           new TypeReference<CsvMaintenanceEvent>() {});
                     final MaintenanceEvent event = new MaintenanceEvent();
                     event.cost = csvMaintenanceEvent.MyCost;
                     event.eventEpochTime = csvMaintenanceEvent.MyDateTimeEpoch;
                     event.notes = csvMaintenanceEvent.MyNotes;
                     newTag.maintenanceEvents.put(event.eventEpochTime, event);

                     //System.out.println("key --> " + entry.getKey() + " value-->" + entry.getValue());
                  }
               } catch (final JsonMappingException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               } catch (final JsonProcessingException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            _allCsvTags.add(newTag);
            if (lineId != null && lineId.length() > 0) {
               _mapIdCsvTags.put(lineId, newTag);
            }
         }

         info += "numFaultyLines:" + numFaultLines;

         //Update existing Tags with csv info
         _allTourTags = TourDatabase.getAllTourTags();
         int numFoundbyST3Id = 0;
         for (final Map.Entry<Long, TourTag> entry : _allTourTags.entrySet()) {
            final Long key = entry.getKey();
            final TourTag value = entry.getValue();
            //search by ST3 Id
            CsvTag foundCsvTag = null;
            for (final CsvTag csvTag : _allCsvTags) {
               if (value.getNotes() != null && value.getNotes().contains(csvTag.id)) {
                  foundCsvTag = csvTag;
               }
            }
            if (foundCsvTag != null) {
               ExtraData extraData = value.getExtraData();
               if (extraData == null) {
                  extraData = new ExtraData();
                  value.setExtraData(extraData);
               }
               TourTagMaintenance maintenance = extraData.getMaintenanceInfo();
               if (maintenance == null) {
                  maintenance = new TourTagMaintenance();
                  extraData.setMaintenanceInfo(maintenance);
               }
               maintenance.setBrand(foundCsvTag.brand);
               maintenance.setExpectedLifeHours(foundCsvTag.expectedLifeHours);
               maintenance.setExpectedLifeKilometers(foundCsvTag.expectedLifeKilometers);
               maintenance.setExtraHourUsed(foundCsvTag.maintenanceInfo.MyExtraHoursUsed.intValue());
               maintenance.setExtraLifeMonthUsage(foundCsvTag.maintenanceInfo.MyExtraMonthsUsed.intValue());
               maintenance.setModel(foundCsvTag.model);
               maintenance.setPurchaseDateEpochSeconds(foundCsvTag.datePurchased);
               maintenance.setPurchaseLocation(foundCsvTag.purchaseLocation);
               maintenance.setPurchasePrice(foundCsvTag.purchasePrice);
               maintenance.setScheduleDistanceMeters(foundCsvTag.scheduleDistanceMeters);
               maintenance.setScheduleLifeMonths(foundCsvTag.scheduleLifeMonths);
               maintenance.setScheduleTimeSpanSeconds(foundCsvTag.scheduleTimeSpanSeconds);
               maintenance.setType(foundCsvTag.type);
               maintenance.setWeightKilograms(foundCsvTag.weightKilograms);

               maintenance.setSortedEventsMaintenance(foundCsvTag.maintenanceEvents);

               numFoundbyST3Id++;
               //save tag to DB
               TourDatabase.saveEntity(value, value.getTagId(), TourTag.class);

            } else {
               //TODO search by name
            }

         }
         info += "; ST3Id Found:" + numFoundbyST3Id;
      }

      _txtFileInfo.setText(info);
   }

   private void restoreState() {

   }

   private void saveState() {

   }
}
