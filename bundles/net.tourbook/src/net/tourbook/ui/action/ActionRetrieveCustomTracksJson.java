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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.tourbook.Messages;
import net.tourbook.common.UI;
import net.tourbook.data.CustomTrackDefinition;
import net.tourbook.data.TourData;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider2;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ActionRetrieveCustomTracksJson extends Action {
   //TODO replace with default preference
   public static final String   JSONPATH_GARMIN_SP02      = "/Spo2DailyAcclimation/payload/spo2ValuesArray"; //$NON-NLS-1$
   public static final String   JSONPATH_GARMIN_RESPRATE  = "/respirationValuesArray"; //$NON-NLS-1$
   public static final String   TRACKNAME_GARMIN_SP02     = "Garmin SPO2"; //$NON-NLS-1$
   public static final String   TRACKNAME_GARMIN_RESPRATE = "Garmin RespRate"; //$NON-NLS-1$
   public static final String   TRACKID_GARMIN_SP02       = "Garmin SPO2:12d76972-a064-4cf0-ac91-c023869b2bb3"; //$NON-NLS-1$
   public static final String   TRACKID_GARMIN_RESPRATE   = "Garmin RespRate:c0e4348f-2d8b-48cf-ab91-14d0e32c61d5"; //$NON-NLS-1$
   public static final String   TRACKUNIT_GARMIN_SP02     = "%"; //$NON-NLS-1$
   public static final String   TRACKUNIT_GARMIN_RESPRATE = "cpm"; //$NON-NLS-1$

   final static Charset         ENCODING_UTF8             = StandardCharsets.UTF_8;
   final static Charset         ENCODING_CP1252           = Charset.forName("CP1252"); //$NON-NLS-1$

   private final ITourProvider2 _tourProvider;
   private String _jsonPath  = JSONPATH_GARMIN_SP02;
   private String _trackName = TRACKNAME_GARMIN_SP02;
   private String _trackId   = TRACKID_GARMIN_SP02;
   private String _trackUnit = TRACKUNIT_GARMIN_SP02;

   private class CustomTracksJsonSettingsDialog extends TitleAreaDialog {

      private Text    jsonPath1Text;
      private Text    jsonPath2Text;

      private Text    trackName1Text;

      private Text    trackName2Text;

      private Text    trackId1Text;

      private Text    trackId2Text;

      private Text    trackUnit1Text;

      private Text    trackUnit2Text;
      private String  jsonPath1Value;

      private String  trackName1Value;

      private String  trackId1Value;

      private String  trackUnit1Value;

      private String  jsonPath2Value;
      private String  trackName2Value;

      private String  trackId2Value;

      private String  trackUnit2Value;

      private Button  buttonOpenFile;

      private Text    txtFileName;

      private Button  buttonPath1;
      private Button  buttonPath2;

      private boolean isPath1 = true;
      private String  jsonFileContent;

      public CustomTracksJsonSettingsDialog(final Shell parentShell) {
         super(parentShell);
      }

      @Override
      public void create() {
         super.create();
         setTitle(Messages.Dialog_RetrieveCustomTracksJson_Dialog_Title);
         setMessage(Messages.Dialog_RetrieveCustomTracksJson_Dialog_Message, IMessageProvider.INFORMATION);
         jsonPath1Value = JSONPATH_GARMIN_SP02;
         jsonPath1Text.setText(jsonPath1Value);
         jsonPath2Value = JSONPATH_GARMIN_RESPRATE;
         jsonPath2Text.setText(jsonPath2Value);
         trackName1Value = TRACKNAME_GARMIN_SP02;
         trackName1Text.setText(trackName1Value);
         trackName2Value = TRACKNAME_GARMIN_RESPRATE;
         trackName2Text.setText(trackName2Value);
         trackId1Value = TRACKID_GARMIN_SP02;
         trackId1Text.setText(trackId1Value);
         trackId2Value = TRACKID_GARMIN_RESPRATE;
         trackId2Text.setText(trackId2Value);
         trackUnit1Value = TRACKUNIT_GARMIN_SP02;
         trackUnit1Text.setText(trackUnit1Value);
         trackUnit2Value = TRACKUNIT_GARMIN_RESPRATE;
         trackUnit2Text.setText(trackUnit2Value);
      }

      @Override
      protected Control createDialogArea(final Composite parent) {
         final Composite area = (Composite) super.createDialogArea(parent);
         final Composite container = new Composite(area, SWT.NONE);
         container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         final GridLayout layout = new GridLayout(2, false);
         container.setLayout(layout);

         createJsonImport(container);
         createJsonPath1(container);
         createJsonPath2(container);

         return area;
      }

      private void createJsonImport(final Composite container) {

         final Group group1 = new Group(container, SWT.SHADOW_IN);
         group1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelRadioChoose);
         group1.setLayout(new RowLayout(SWT.HORIZONTAL));
         buttonPath1 = new Button(group1, SWT.RADIO);
         buttonPath1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelJsonPath1);
         buttonPath1.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
               final Button source = (Button) e.getSource();

               if (source.getSelection()) {
                  isPath1 = true;
               }
            }

         });
         buttonPath2 = new Button(group1, SWT.RADIO);
         buttonPath2.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelJsonPath2);
         buttonPath2.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
               final Button source = (Button) e.getSource();

               if (source.getSelection()) {
                  isPath1 = false;
               }
            }

         });

         final Label lbtDummy = new Label(container, SWT.NONE);
         lbtDummy.setText(UI.EMPTY_STRING);

         buttonOpenFile = new Button(container, SWT.NONE);
         buttonOpenFile.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelJsonOpenFile);
         buttonOpenFile.setToolTipText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelJsonOpenFile_ToolTip);
         buttonOpenFile.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event e) {
               switch (e.type) {
               case SWT.Selection:
                  final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                  final String[] filterNames = new String[] { "Txt Files", "All Files (*)" }; //$NON-NLS-1$ //$NON-NLS-2$
                  final String[] filterExtensions = new String[] { "*.json;*.txt", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
                  final String filterPath = ""; //$NON-NLS-1$
                  dialog.setFilterNames(filterNames);
                  dialog.setFilterExtensions(filterExtensions);
                  dialog.setFilterPath(filterPath);
                  txtFileName.setText(dialog.open());
                  final Path path = Paths.get(txtFileName.getText());
                  try {
                     final List<String> jsonDocument = Files.readAllLines(path, ENCODING_CP1252);
                     jsonFileContent = UI.EMPTY_STRING;
                     for (final String element : jsonDocument) {
                        jsonFileContent += element;
                     }
                  } catch (final IOException e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                     txtFileName.setText(dialog.open() + "=>Exception reading file!!!"); //$NON-NLS-1$
                  }
                  break;
               }
            }
         });
         final GridData dataFile = new GridData();
         dataFile.grabExcessHorizontalSpace = true;
         dataFile.horizontalAlignment = GridData.FILL;
         txtFileName = new Text(container, SWT.BORDER);
         txtFileName.setLayoutData(dataFile);
      }

      private void createJsonPath1(final Composite container) {
         final Label lbtPath1 = new Label(container, SWT.NONE);
         lbtPath1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelJsonPath1);

         final GridData dataPath1 = new GridData();
         dataPath1.grabExcessHorizontalSpace = true;
         dataPath1.horizontalAlignment = GridData.FILL;
         jsonPath1Text = new Text(container, SWT.BORDER);
         jsonPath1Text.setLayoutData(dataPath1);

         final Label lbtName1 = new Label(container, SWT.NONE);
         lbtName1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelTrackName1);

         final GridData dataName1 = new GridData();
         dataName1.grabExcessHorizontalSpace = true;
         dataName1.horizontalAlignment = GridData.FILL;
         trackName1Text = new Text(container, SWT.BORDER);
         trackName1Text.setLayoutData(dataName1);

         final Label lbtId1 = new Label(container, SWT.NONE);
         lbtId1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelTrackId1);

         final GridData dataId1 = new GridData();
         dataId1.grabExcessHorizontalSpace = true;
         dataId1.horizontalAlignment = GridData.FILL;
         trackId1Text = new Text(container, SWT.BORDER);
         trackId1Text.setLayoutData(dataId1);

         final Label lbtUnit1 = new Label(container, SWT.NONE);
         lbtUnit1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelTrackUnit1);

         final GridData dataUnit1 = new GridData();
         dataUnit1.grabExcessHorizontalSpace = true;
         dataUnit1.horizontalAlignment = GridData.FILL;
         trackUnit1Text = new Text(container, SWT.BORDER);
         trackUnit1Text.setLayoutData(dataUnit1);
      }

      private void createJsonPath2(final Composite container) {
         final Label lbtPath2 = new Label(container, SWT.NONE);
         lbtPath2.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelJsonPath2);

         final GridData dataPath2 = new GridData();
         dataPath2.grabExcessHorizontalSpace = true;
         dataPath2.horizontalAlignment = GridData.FILL;
         jsonPath2Text = new Text(container, SWT.BORDER);
         jsonPath2Text.setLayoutData(dataPath2);

         final Label lbtName2 = new Label(container, SWT.NONE);
         lbtName2.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelTrackName2);

         final GridData dataName2 = new GridData();
         dataName2.grabExcessHorizontalSpace = true;
         dataName2.horizontalAlignment = GridData.FILL;
         trackName2Text = new Text(container, SWT.BORDER);
         trackName2Text.setLayoutData(dataName2);

         final Label lbtId2 = new Label(container, SWT.NONE);
         lbtId2.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelTrackId2);

         final GridData dataId2 = new GridData();
         dataId2.grabExcessHorizontalSpace = true;
         dataId2.horizontalAlignment = GridData.FILL;
         trackId2Text = new Text(container, SWT.BORDER);
         trackId2Text.setLayoutData(dataId2);

         final Label lbtUnit1 = new Label(container, SWT.NONE);
         lbtUnit1.setText(Messages.Dialog_RetrieveCustomTracksJson_Dialog_LabelTrackUnit2);

         final GridData dataUnit2 = new GridData();
         dataUnit2.grabExcessHorizontalSpace = true;
         dataUnit2.horizontalAlignment = GridData.FILL;
         trackUnit2Text = new Text(container, SWT.BORDER);
         trackUnit2Text.setLayoutData(dataUnit2);
      }

      public String getJsonFileContent() {
         return jsonFileContent;
      }

      public String getJsonPath1Value() {
         return jsonPath1Value;
      }

      public String getJsonPath2Value() {
         return jsonPath2Value;
      }

      public String getTrackId1Value() {
         return trackId1Value;
      }

      public String getTrackId2Value() {
         return trackId2Value;
      }

      public String getTrackName1Value() {
         return trackName1Value;
      }

      public String getTrackName2Value() {
         return trackName2Value;
      }

      public String getTrackUnit1Value() {
         return trackUnit1Value;
      }

      public String getTrackUnit2Value() {
         return trackUnit2Value;
      }

      public boolean isPath1() {
         return isPath1;
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
         jsonPath1Value = jsonPath1Text.getText();
         jsonPath2Value = jsonPath2Text.getText();
         trackName1Value = trackName1Text.getText();
         trackName2Value = trackName2Text.getText();
         trackId1Value = trackId1Text.getText();
         trackId2Value = trackId2Text.getText();
         trackUnit1Value = trackUnit1Text.getText();
         trackUnit2Value = trackUnit2Text.getText();
      }

   }

   public class GarminData{
      TreeMap<Long, Integer> valueMap;
      String trackId;
      String trackName;
      String trackUnit;

      public Integer GetInterpolatedValue(final Long epochTime) {
         Integer value = null;
         if (valueMap != null && valueMap.size() > 0) {
            if (valueMap.containsKey(epochTime)) {
               return valueMap.get(epochTime);
            } else {
               final Entry<Long, Integer> entryLow = valueMap.lowerEntry(epochTime);
               final Entry<Long, Integer> entryHigh = valueMap.ceilingEntry(epochTime);
               if (entryLow != null && entryHigh != null) {
                  if (entryLow.getValue() == entryHigh.getValue()) {
                     return entryHigh.getValue();
                  } else {
                     final float weigthDifference = (epochTime - entryLow.getKey()) / (entryHigh.getKey() - entryLow.getKey());
                     final float interpolatedValue = weigthDifference * (entryHigh.getValue() - entryLow.getValue()) + entryLow.getValue();
                     value = Math.round(interpolatedValue);
                  }
               }
            }
         }
         return value;
      }
   }

   private class GarminEntry {
      long epochMilliseconds;
      int  value;            //spO2 or respiration rate
   }

   public ActionRetrieveCustomTracksJson(final ITourProvider2 tourProvider) {

      super(null, AS_PUSH_BUTTON);

      _tourProvider = tourProvider;

      setText(Messages.Tour_Action_RetrieveCustomTracksJsonData);
   }

   public boolean AddGarminCustomTracksToTour(final TourData tour, final GarminData garminData) {
      boolean isModified = false;

      //firt eliminate tours which start after the end of the track
      //or end before the start of the custom track
      if (garminData == null || garminData.valueMap == null || garminData.valueMap.isEmpty()) {
         return isModified;
      }
      if (tour.getTourStartTimeMS() > garminData.valueMap.lastKey()) {
         return isModified;
      }
      if (tour.getTourEndTimeMS() < garminData.valueMap.firstKey()) {
         return isModified;
      }

      //now add custom track value by interpolating if necessary
      //while looping on the tour timeserie entries
      final CustomTrackDefinition customTrackDefinition = new CustomTrackDefinition();
      customTrackDefinition.setId(garminData.trackId);
      customTrackDefinition.setName(garminData.trackName);
      customTrackDefinition.setUnit(garminData.trackUnit);
      if (!tour.customTracksDefinition.containsKey(customTrackDefinition.getId())) {
         tour.customTracksDefinition.put(customTrackDefinition.getId(), customTrackDefinition);
      }
      tour.setDataSeriesAppendDefinitions(customTrackDefinition);

      final int[] timeSerie = tour.timeSerie;
      final float[] newCustomTrackSerie = new float[timeSerie.length];
      for (int serieIndex = 0; serieIndex < timeSerie.length; serieIndex++) {
         final long timeIndex = tour.getTourStartTimeMS() + timeSerie[serieIndex] * 1000;
         final Integer interpolatedValue = garminData.GetInterpolatedValue(timeIndex);
         if (interpolatedValue == null) {
            newCustomTrackSerie[serieIndex] = 0;
         } else {
            newCustomTrackSerie[serieIndex] = interpolatedValue;
         }
      }
      //final HashMap<String, float[]> newCustomTracks = tour.getCustomTracks();
      //newCustomTracks.put(garminData.trackId, newCustomTrackSerie);
      //tour.setCustomTracks(newCustomTracks);
      tour.setCustomTracksSerie(garminData.trackId, newCustomTrackSerie);
      isModified = true;

      return isModified;
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

      final CustomTracksJsonSettingsDialog dialog = new CustomTracksJsonSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         if (dialog.isPath1()) {
            _trackId = dialog.getTrackId1Value();
            _trackName = dialog.getTrackName1Value();
            _jsonPath = dialog.getJsonPath1Value();
            _trackUnit = dialog.getTrackUnit1Value();
         } else {
            _trackId = dialog.getTrackId2Value();
            _trackName = dialog.getTrackName2Value();
            _jsonPath = dialog.getJsonPath2Value();
            _trackUnit = dialog.getTrackUnit2Value();
         }
      } else {
         return;
      }

      final TreeMap<Long, Integer> garminValueMap = new TreeMap<>();
      final GarminData garminData = new GarminData();
      garminData.valueMap = garminValueMap;
      garminData.trackId = _trackId;
      garminData.trackName = _trackName;
      garminData.trackUnit = _trackUnit;

      if (dialog.getJsonFileContent() != null && !dialog.getJsonFileContent().isBlank()) {
         final ObjectMapper objectMapper = new ObjectMapper();
         try {
            final JsonNode jsonMasterNode = objectMapper.readTree(dialog.getJsonFileContent());
            final ArrayNode jsonArrayNode = (ArrayNode) jsonMasterNode.at(_jsonPath);
            for (int i = 0; i < jsonArrayNode.size(); i++) {
               final ArrayNode arrayElement = (ArrayNode) jsonArrayNode.get(i);
               final GarminEntry entry = new GarminEntry();
               entry.epochMilliseconds = arrayElement.get(0).asLong();
               entry.value = arrayElement.get(1).asInt();
               garminValueMap.put(entry.epochMilliseconds, entry.value);
            }
         } catch (final JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      } else {
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {

               final boolean isDataRetrieved = AddGarminCustomTracksToTour(tour, garminData);

               if (isDataRetrieved) {
                  modifiedTours.add(tour);
               }
            }

            if (modifiedTours.size() > 0) {
               TourManager.saveModifiedTours(modifiedTours);
            } else {
               MessageDialog.openInformation(
                     shell,
                     Messages.Dialog_RetrieveCustomTracksJson_Dialog_Title,
                     Messages.Dialog_RetrieveCustomTracksJson_Dialog_NoJsonImport);
            }
         }
      });
   }
}
