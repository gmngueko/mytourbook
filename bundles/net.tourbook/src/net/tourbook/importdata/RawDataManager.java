/*******************************************************************************
 * Copyright (C) 2005, 2021 Wolfgang Schramm and Contributors
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
package net.tourbook.importdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import net.tourbook.Messages;
import net.tourbook.application.PerspectiveFactoryRawData;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.FileSystemManager;
import net.tourbook.common.UI;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.FilesUtils;
import net.tourbook.common.util.ITourViewer3;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.Util;
import net.tourbook.common.widgets.ComboEnumEntry;
import net.tourbook.data.TourData;
import net.tourbook.data.TourPerson;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourType;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.CadenceMultiplier;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourLogManager;
import net.tourbook.tour.TourLogState;
import net.tourbook.tour.TourLogView;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.views.collateTours.CollatedToursView;
import net.tourbook.ui.views.rawData.RawDataView;
import net.tourbook.ui.views.tourBook.TourBookView;
import net.tourbook.ui.views.tourDataEditor.TourDataEditorView;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class RawDataManager {

// SET_FORMATTING_OFF

   private static final String   COLUMN_FACTORY_CATEGORY_MARKER              = net.tourbook.ui.Messages.ColumnFactory_Category_Marker;
   private static final String   COLUMN_FACTORY_GEAR_REAR_SHIFT_COUNT_LABEL  = net.tourbook.ui.Messages.ColumnFactory_GearRearShiftCount_Label;
   private static final String   COLUMN_FACTORY_GEAR_FRONT_SHIFT_COUNT_LABEL = net.tourbook.ui.Messages.ColumnFactory_GearFrontShiftCount_Label;
   private static final String   VALUE_UNIT_CADENCE                          = net.tourbook.ui.Messages.Value_Unit_Cadence;
   private static final String   VALUE_UNIT_CADENCE_SPM                      = net.tourbook.ui.Messages.Value_Unit_Cadence_Spm;
   private static final String   VALUE_UNIT_K_CALORIES                       = net.tourbook.ui.Messages.Value_Unit_KCalories;
   private static final String   VALUE_UNIT_PULSE                            = net.tourbook.ui.Messages.Value_Unit_Pulse;

   public static final String    LOG_IMPORT_DELETE_TOUR_FILE                 = Messages.Log_Import_DeleteTourFiles;
   public static final String    LOG_IMPORT_DELETE_TOUR_FILE_END             = Messages.Log_Import_DeleteTourFiles_End;
   private static final String   LOG_IMPORT_TOUR                             = Messages.Log_Import_Tour;
   public static final String    LOG_IMPORT_TOUR_IMPORTED                    = Messages.Log_Import_Tour_Imported;
   private static final String   LOG_IMPORT_TOUR_END                         = Messages.Log_Import_Tour_End;
   public static final String    LOG_IMPORT_TOURS_IMPORTED_FROM_FILE         = Messages.Log_Import_Tours_Imported_From_File;

   public static final String    LOG_DELETE_COMBINED_VALUES                  = NLS.bind(Messages.Log_ModifiedTour_Combined_Values, Messages.Log_Delete_Text);

   public static final String    LOG_DELETE_TOURVALUES_END                   = Messages.Log_Delete_TourValues_End;
   public static final String    LOG_MODIFIEDTOUR_OLD_DATA_VS_NEW_DATA       = Messages.Log_ModifiedTour_Old_Data_Vs_New_Data;

   public static final String    LOG_REIMPORT_PREVIOUS_FILES                 = Messages.Log_Reimport_PreviousFiles;
   public static final String    LOG_REIMPORT_END                            = Messages.Log_Reimport_PreviousFiles_End;
   public static final String    LOG_REIMPORT_COMBINED_VALUES                = NLS.bind(Messages.Log_ModifiedTour_Combined_Values, Messages.Log_Reimport_Text);
   private static final String   LOG_REIMPORT_MANUAL_TOUR                    = Messages.Log_Reimport_ManualTour;
   private static final String   LOG_REIMPORT_TOUR_SKIPPED                   = Messages.Log_Reimport_Tour_Skipped;

// SET_FORMATTING_ON

   private static final String           RAW_DATA_LAST_SELECTED_PATH           = "raw-data-view.last-selected-import-path";              //$NON-NLS-1$
   private static final String           TEMP_IMPORTED_FILE                    = "received-device-data.txt";                             //$NON-NLS-1$

   private static final String           FILE_EXTENSION_FIT                    = ".fit";                                                 //$NON-NLS-1$

   private static final IPreferenceStore _prefStore                            = TourbookPlugin.getPrefStore();
   private static final IDialogSettings  _stateRawDataView                     = TourbookPlugin.getState(RawDataView.ID);

   private static final String           INVALIDFILES_TO_IGNORE                = "invalidfiles_to_ignore.txt";                           //$NON-NLS-1$

   public static final int               ADJUST_IMPORT_YEAR_IS_DISABLED        = -1;

   static final ComboEnumEntry<?>[]      ALL_IMPORT_TOUR_TYPE_CONFIG;

   private static boolean                _importState_IsAutoOpenImportLog      = RawDataView.STATE_IS_AUTO_OPEN_IMPORT_LOG_VIEW_DEFAULT;
   private static boolean                _importState_IsIgnoreInvalidFile      = RawDataView.STATE_IS_IGNORE_INVALID_FILE_DEFAULT;
   private static boolean                _importState_IsSetBodyWeight          = RawDataView.STATE_IS_SET_BODY_WEIGHT_DEFAULT;
   private static CadenceMultiplier      _importState_DefaultCadenceMultiplier = (CadenceMultiplier) Util.getStateEnum(_stateRawDataView,
         RawDataView.STATE_DEFAULT_CADENCE_MULTIPLIER,
         RawDataView.STATE_DEFAULT_CADENCE_MULTIPLIER_DEFAULT);

   static {

      ALL_IMPORT_TOUR_TYPE_CONFIG = new ComboEnumEntry<?>[] {

            new ComboEnumEntry<>(Messages.Import_Data_TourTypeConfig_OneForAll, TourTypeConfig.TOUR_TYPE_CONFIG_ONE_FOR_ALL),
            new ComboEnumEntry<>(Messages.Import_Data_TourTypeConfig_BySpeed, TourTypeConfig.TOUR_TYPE_CONFIG_BY_SPEED)

      };
   }

   private static RawDataManager           _instance                           = null;

   private static ArrayList<String>        _invalidFilesList                   = new ArrayList<>();

   /**
    * Alternative filepaths from previous re-imported tours
    */
   private static HashSet<IPath>           _allPreviousReimportFolders         = new HashSet<>();
   private static IPath                    _previousReimportFolder;

   /**
    * Is <code>true</code> when currently a re-importing is running
    */
   private static boolean                  _isReimportingActive;

   /**
    * Is <code>true</code> when deleting values from tour(s) is happening
    */
   private static boolean                  _isDeleteValuesActive;

   /**
    * contains the device data imported from the device/file
    */
   private final DeviceData                _deviceData                         = new DeviceData();

   /**
    * Contains tours which are imported or received and displayed in the import view.
    */
   private final Map<Long, TourData>       _toursInImportView                  = new HashMap<>();

   /**
    * Contains tours which are imported from the last file name.
    */
   private final Map<Long, TourData>       _newlyImportedTours                 = new HashMap<>();

   private String                          _lastImportedFileName;

   /**
    * Contains the filenames for all imported files which are displayed in the import view
    */
   private final HashSet<String>           _importedFileNames                  = new HashSet<>();

   /**
    * Contains filenames which are not directly imported but is imported from other imported files
    */
   private final HashSet<String>           _importedFileNamesChildren          = new HashSet<>();
   //
   private boolean                         _isImported;
   private boolean                         _isImportCanceled;
   //
   private int                             _importState_ImportYear             = ADJUST_IMPORT_YEAR_IS_DISABLED;
   private boolean                         _importState_IsConvertWayPoints     = Util.getStateBoolean(_stateRawDataView,
         RawDataView.STATE_IS_CONVERT_WAYPOINTS,
         RawDataView.STATE_IS_CONVERT_WAYPOINTS_DEFAULT);
   private boolean                         _importState_IsCreateTourIdWithTime = RawDataView.STATE_IS_CREATE_TOUR_ID_WITH_TIME_DEFAULT;
   private boolean                         _importState_IsChecksumValidation   = RawDataView.STATE_IS_CHECKSUM_VALIDATION_DEFAULT;
   private boolean                         _importState_IsMergeTracks          = RawDataView.STATE_IS_MERGE_TRACKS_DEFAULT;
   //
   private List<TourbookDevice>            _devicesBySortPriority;
   private HashMap<String, TourbookDevice> _devicesByExtension;
   //
   private final ArrayList<TourType>       _tempTourTypes                      = new ArrayList<>();
   private final ArrayList<TourTag>        _tempTourTags                       = new ArrayList<>();

   /**
    * This is a wrapper to keep the {@link #isBackupImportFile} state.
    */
   private class ImportFile {

      IPath   filePath;
      boolean isBackupImportFile;

      public ImportFile(final org.eclipse.core.runtime.Path iPath) {
         filePath = iPath;
      }
   }

   public enum TourValueType {

      ENTIRE_TOUR, //
      ALL_TIME_SLICES, //

      TOUR_MARKER, //
      TOUR_CALORIES, //
      IMPORT_FILE_LOCATION, //

      TIME_SLICES_ELEVATION, //
      TIME_SLICES_CADENCE, //
      TIME_SLICES_GEAR, //
      TIME_SLICES_POWER_AND_SPEED, //
      TIME_SLICES_POWER_AND_PULSE, //
      TIME_SLICES_RUNNING_DYNAMICS, //
      TIME_SLICES_SWIMMING, //
      TIME_SLICES_TEMPERATURE, //
      TIME_SLICES_TRAINING, //
      TIME_SLICES_TIME, //
      TIME_SLICES_TIMER_PAUSES //
   }

   private RawDataManager() {}

   /**
    * Displays the differences of data before and after the tour modifications (re-import or
    * deletion)
    *
    * @param tourValueType
    *           A tour value that was modified (re-imported or deleted)
    * @param oldTourData
    *           The Tour before the modifications (re-import or deletion)
    * @param newTourData
    *           The Tour after the modifications (re-import or deletion)
    */
   public static void displayTourModifiedDataDifferences(final TourValueType tourValueType,
                                                         final TourData oldTourData,
                                                         final TourData newTourData) {
      final List<String> previousData = new ArrayList<>();
      final List<String> newData = new ArrayList<>();

      if (tourValueType == TourValueType.TIME_SLICES_ELEVATION ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         final String heightLabel = UI.UNIT_IS_ELEVATION_METER ? UI.UNIT_METER : UI.UNIT_HEIGHT_FT;

         final int oldAltitudeUp = Math.round(oldTourData.getTourAltUp() / UI.UNIT_VALUE_ELEVATION);
         final int oldAltitudeDown = Math.round(oldTourData.getTourAltDown() / UI.UNIT_VALUE_ELEVATION);
         previousData.add(
               UI.SYMBOL_PLUS + oldAltitudeUp + heightLabel + UI.SLASH_WITH_SPACE
                     + UI.DASH
                     + oldAltitudeDown
                     + heightLabel);

         final int newAltitudeUp = Math.round(newTourData.getTourAltUp() / UI.UNIT_VALUE_ELEVATION);
         final int newAltitudeDown = Math.round(newTourData.getTourAltDown() / UI.UNIT_VALUE_ELEVATION);
         newData.add(
               UI.SYMBOL_PLUS + newAltitudeUp + heightLabel + UI.SLASH_WITH_SPACE
                     + UI.DASH + newAltitudeDown
                     + heightLabel);
      }
      if (tourValueType == TourValueType.TIME_SLICES_TIMER_PAUSES ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(UI.format_hhh_mm_ss(oldTourData.getTourDeviceTime_Paused()));
         newData.add(UI.format_hhh_mm_ss(newTourData.getTourDeviceTime_Paused()));
      }
      if (tourValueType == TourValueType.TIME_SLICES_CADENCE ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(
               Math.round(oldTourData.getAvgCadence()) + (oldTourData.isCadenceSpm()
                     ? VALUE_UNIT_CADENCE_SPM
                     : VALUE_UNIT_CADENCE));
         newData.add(
               Math.round(newTourData.getAvgCadence()) + (newTourData.isCadenceSpm()
                     ? VALUE_UNIT_CADENCE_SPM
                     : VALUE_UNIT_CADENCE));
      }
      if (tourValueType == TourValueType.TIME_SLICES_GEAR ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(
               oldTourData.getFrontShiftCount() + UI.SPACE1 + COLUMN_FACTORY_GEAR_FRONT_SHIFT_COUNT_LABEL
                     + UI.COMMA_SPACE + oldTourData.getRearShiftCount() + UI.SPACE1 + COLUMN_FACTORY_GEAR_REAR_SHIFT_COUNT_LABEL);
         newData.add(
               newTourData.getFrontShiftCount() + UI.SPACE1 + COLUMN_FACTORY_GEAR_FRONT_SHIFT_COUNT_LABEL
                     + UI.COMMA_SPACE + newTourData.getRearShiftCount() + UI.SPACE1 + COLUMN_FACTORY_GEAR_REAR_SHIFT_COUNT_LABEL);
      }
      if (tourValueType == TourValueType.TIME_SLICES_POWER_AND_PULSE ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(
               Math.round(oldTourData.getPower_Avg()) + UI.UNIT_POWER_SHORT + UI.COMMA_SPACE
                     + Math.round(oldTourData.getAvgPulse()) + VALUE_UNIT_PULSE);
         newData.add(
               Math.round(newTourData.getPower_Avg()) + UI.UNIT_POWER_SHORT + UI.COMMA_SPACE
                     + Math.round(newTourData.getAvgPulse()) + VALUE_UNIT_PULSE);
      }
      if (tourValueType == TourValueType.TIME_SLICES_POWER_AND_SPEED ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(Math.round(oldTourData.getPower_Avg()) + UI.UNIT_POWER_SHORT);
         newData.add(Math.round(newTourData.getPower_Avg()) + UI.UNIT_POWER_SHORT);
      }
      if (tourValueType == TourValueType.TIME_SLICES_TEMPERATURE ||
            tourValueType == TourValueType.ALL_TIME_SLICES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         float avgTemperature = oldTourData.getAvgTemperature();
         if (!UI.UNIT_IS_TEMPERATURE_CELSIUS) {
            avgTemperature = avgTemperature
                  * UI.UNIT_FAHRENHEIT_MULTI
                  + UI.UNIT_FAHRENHEIT_ADD;
         }
         previousData.add(
               Math.round(avgTemperature) + (UI.UNIT_IS_TEMPERATURE_CELSIUS
                     ? UI.SYMBOL_TEMPERATURE_CELSIUS
                     : UI.SYMBOL_TEMPERATURE_FAHRENHEIT));

         avgTemperature = newTourData.getAvgTemperature();
         if (!UI.UNIT_IS_TEMPERATURE_CELSIUS) {
            avgTemperature = avgTemperature
                  * UI.UNIT_FAHRENHEIT_MULTI
                  + UI.UNIT_FAHRENHEIT_ADD;
         }
         newData.add(
               Math.round(avgTemperature) + (UI.UNIT_IS_TEMPERATURE_CELSIUS
                     ? UI.SYMBOL_TEMPERATURE_CELSIUS
                     : UI.SYMBOL_TEMPERATURE_FAHRENHEIT));
      }
      if (tourValueType == TourValueType.TOUR_MARKER ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(
               oldTourData.getTourMarkers().size() + UI.SPACE1 + COLUMN_FACTORY_CATEGORY_MARKER);
         newData.add(
               newTourData.getTourMarkers().size() + UI.SPACE1 + COLUMN_FACTORY_CATEGORY_MARKER);
      }
      if (tourValueType == TourValueType.TOUR_CALORIES ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(oldTourData.getCalories() / 1000f + VALUE_UNIT_K_CALORIES);
         newData.add(newTourData.getCalories() / 1000f + VALUE_UNIT_K_CALORIES);
      }
      if (tourValueType == TourValueType.IMPORT_FILE_LOCATION ||
            tourValueType == TourValueType.ENTIRE_TOUR) {

         previousData.add(oldTourData.getImportFilePathName());
         newData.add(newTourData.getImportFilePathName());
      }

      if (previousData.isEmpty() && newData.isEmpty()) {
         return;
      }

      for (int index = 0; index < previousData.size(); ++index) {
         TourLogManager.addSubLog(TourLogState.INFO,
               NLS.bind(
                     LOG_MODIFIEDTOUR_OLD_DATA_VS_NEW_DATA,
                     previousData.get(index),
                     newData.get(index)));
      }
   }

   public static boolean doesInvalidFileExist(final String fileName) {

      final ArrayList<String> invalidFilesList = readInvalidFilesToIgnoreFile();

      return invalidFilesList
            .stream()
            .anyMatch(invalidFilePath -> Paths.get(invalidFilePath).getFileName().toString().equals(fileName));
   }

   /**
    * @return Returns the cadence multiplier default value
    */
   public static CadenceMultiplier getCadenceMultiplierDefaultValue() {
      return _importState_DefaultCadenceMultiplier;
   }

   private static EasyConfig getEasyConfig() {
      return EasyImportManager.getInstance().getEasyConfig();
   }

   public static RawDataManager getInstance() {

      if (_instance == null) {
         _instance = new RawDataManager();
      }

      return _instance;
   }

   private static File getInvalidFilesToIgnoreFile() {
      final IPath stateLocation = Platform.getStateLocation(CommonActivator.getDefault().getBundle());
      return stateLocation.append(INVALIDFILES_TO_IGNORE).toFile();
   }

   /**
    * @return temporary directory where received data are stored temporarily
    */
   public static String getTempDir() {
      return TourbookPlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
   }

   public static boolean isAutoOpenImportLog() {
      return _importState_IsAutoOpenImportLog;
   }

   /**
    * @return Returns <code>true</code> when currently deleting values from tour(s)
    */
   public static boolean isDeleteValuesActive() {
      return _isDeleteValuesActive;
   }

   public static boolean isIgnoreInvalidFile() {
      return _importState_IsIgnoreInvalidFile;
   }

   /**
    * @return Returns <code>true</code> when currently a re-importing is running
    */
   public static boolean isReimportingActive() {
      return _isReimportingActive;
   }

   public static boolean isSetBodyWeight() {
      return _importState_IsSetBodyWeight;
   }

   private static ArrayList<String> readInvalidFilesToIgnoreFile() {
      final ArrayList<String> invalidFilesList = new ArrayList<>();

      final File invalidFilesToIgnoreFile = getInvalidFilesToIgnoreFile();
      if (!invalidFilesToIgnoreFile.exists()) {
         return invalidFilesList;
      }

      try (Scanner s = new Scanner(invalidFilesToIgnoreFile)) {
         while (s.hasNext()) {
            invalidFilesList.add(s.next());
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }

      return invalidFilesList;
   }

   /**
    * Writes the list of files to ignore into a text file.
    */
   private static void save_InvalidFilesToIgnore_InTxt() {

      final File file = getInvalidFilesToIgnoreFile();

      try {
         if (!file.exists()) {
            file.createNewFile();
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }

      try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, UI.UTF_8);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

         final ImportConfig importConfig = getEasyConfig().getActiveImportConfig();

         for (final String invalidFile : _invalidFilesList) {

            Path invalidFilePath = Paths.get(invalidFile);

            //If the invalid files are backed up and deleted from the device folder,
            //then we save their backup path and not their device path.
            if (importConfig.isCreateBackup && importConfig.isDeleteDeviceFiles) {
               invalidFilePath = Paths.get(importConfig.getBackupFolder(), Paths.get(invalidFile).getFileName().toString());
            }

            // We check if the file still exists (it could have been deleted recently)
            // and that it's not already in the text file
            if (Files.exists(invalidFilePath) && !doesInvalidFileExist(invalidFilePath.getFileName().toString())) {
               writer.write(invalidFilePath.toString());
               writer.newLine();
            }
         }

      } catch (final IOException e) {
         e.printStackTrace();
      }
   }

   public static void setIsDeleteValuesActive(final boolean isDeleteValuesActive) {
      _isDeleteValuesActive = isDeleteValuesActive;
   }

   public static void setIsReimportingActive(final boolean isReimportingActive) {
      _isReimportingActive = isReimportingActive;
   }

   public void actionImportFromDevice() {

      final DataTransferWizardDialog dialog = new DataTransferWizardDialog(//
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            new DataTransferWizard(),
            Messages.Import_Wizard_Dlg_title);

      if (dialog.open() == Window.OK) {
         showRawDataView();
      }
   }

   public void actionImportFromDeviceDirect() {

      final DataTransferWizard transferWizard = new DataTransferWizard();

      final WizardDialog dialog = new DataTransferWizardDialog(//
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            transferWizard,
            Messages.Import_Wizard_Dlg_title);

      // create the dialog and shell which is required in setAutoDownload()
      dialog.create();

      transferWizard.setAutoDownload();

      if (dialog.open() == Window.OK) {
         showRawDataView();
      }
   }

   /**
    * Import tours from files which are selected in a file selection dialog.
    */
   public void actionImportFromFile() {

      final List<TourbookDevice> deviceList = DeviceManager.getDeviceList();

      // create file filter list
      final int deviceLength = deviceList.size() + 1;
      final String[] filterExtensions = new String[deviceLength];
      final String[] filterNames = new String[deviceLength];

      int deviceIndex = 0;

      // add option to show all files
      filterExtensions[deviceIndex] = "*.*"; //$NON-NLS-1$
      filterNames[deviceIndex] = "*.*"; //$NON-NLS-1$

      deviceIndex++;

      // add option for every file extension
      for (final TourbookDevice device : deviceList) {
         filterExtensions[deviceIndex] = "*." + device.fileExtension; //$NON-NLS-1$
         filterNames[deviceIndex] = device.visibleName + (" (*." + device.fileExtension + UI.SYMBOL_BRACKET_RIGHT); //$NON-NLS-1$
         deviceIndex++;
      }

      final String lastSelectedPath = _prefStore.getString(RAW_DATA_LAST_SELECTED_PATH);

      // setup open dialog
      final FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), (SWT.OPEN | SWT.MULTI));
      fileDialog.setFilterExtensions(filterExtensions);
      fileDialog.setFilterNames(filterNames);
      fileDialog.setFilterPath(lastSelectedPath);

      // open file dialog
      final String firstFilePathName = fileDialog.open();

      // check if user canceled the dialog
      if (firstFilePathName == null) {
         return;
      }

      final Path firstFilePath = Paths.get(firstFilePathName);
      final String filePathFolder = firstFilePath.getParent().toString();

      // keep last selected path
      _prefStore.putValue(RAW_DATA_LAST_SELECTED_PATH, filePathFolder);

      final String[] selectedFileNames = fileDialog.getFileNames();

      final ArrayList<OSFile> osFiles = new ArrayList<>();

      for (final String fileName : selectedFileNames) {

         final Path filePath = Paths.get(filePathFolder, fileName);
         final OSFile osFile = new OSFile(filePath);

         osFiles.add(osFile);
      }

      if (_importState_IsAutoOpenImportLog) {
         TourLogManager.showLogView();
      }

      runImport(osFiles, false, null);
   }

   /**
    * Asks the user if the modification (re-import or deletion) of all the chosen data is desired.
    *
    * @param tourValueTypes
    *           A list of tour values to be modified (re-imported or deleted)
    * @param isReimport
    *           True if data needs to be re-imported, false if the data needs to be deleted
    * @return
    */
   public boolean actionModifyTourValues_10_Confirm(final List<TourValueType> tourValueTypes, final boolean isReimport) {

      final ArrayList<String> dataToModifyDetails = new ArrayList<>();

      for (final TourValueType tourValueType : tourValueTypes) {

         // Elevation
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_ELEVATION) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_AltitudeValues);
         }

         // Cadence
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_CADENCE) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_CadenceValues);
         }

         // Calories
         if (tourValueType == TourValueType.TOUR_CALORIES) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_Calories);
         }

         // Gear
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_GEAR) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_GearValues);
         }

         // Power
         if (tourValueType == TourValueType.ALL_TIME_SLICES
               || tourValueType == TourValueType.TIME_SLICES_POWER_AND_PULSE
               || tourValueType == TourValueType.TIME_SLICES_POWER_AND_SPEED) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_PowerValues);
         }

         // Pulse
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_POWER_AND_PULSE) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_PulseValues);
         }

         // Speed
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_POWER_AND_SPEED) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_SpeedValues);
         }

         // Running Dynamics
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_RUNNING_DYNAMICS) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_RunningDynamicsValues);
         }

         // Swimming
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_SWIMMING) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_SwimmingValues);
         }

         // Temperature
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_TEMPERATURE) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_TemperatureValues);
         }

         // Training
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_TRAINING) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_TrainingValues);
         }

         // Timer pauses
         if (tourValueType == TourValueType.ALL_TIME_SLICES || tourValueType == TourValueType.TIME_SLICES_TIMER_PAUSES) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_TourTimerPauses);
         }

         // Tour markers
         if (tourValueType == TourValueType.TOUR_MARKER) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_TourMarkers);
         }

         // Time data
         if (tourValueType == TourValueType.TIME_SLICES_TIME) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_Time);
         }

         // Import file location
         if (tourValueType == TourValueType.IMPORT_FILE_LOCATION) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_ImportFileLocation);
         }

         // ALL
         if (tourValueType == TourValueType.ALL_TIME_SLICES) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_TimeSlices);
         }

         // Entire Tour
         if (tourValueType == TourValueType.ENTIRE_TOUR) {
            dataToModifyDetails.add(Messages.Tour_Data_Text_EntireTour);
         }
      }

      String confirmMessage = isReimport
            ? Messages.Dialog_ReimportTours_Dialog_ConfirmReimportValues_Message
            : Messages.Dialog_DeleteTourValues_Dialog_ConfirmDeleteValues_Message;
      confirmMessage = NLS.bind(confirmMessage, String.join(UI.NEW_LINE1, dataToModifyDetails));

      if (actionModifyTourValues_12_Confirm_Dialog(
            isReimport
                  ? ITourbookPreferences.TOGGLE_STATE_REIMPORT_TOUR_VALUES
                  : ITourbookPreferences.TOGGLE_STATE_DELETE_TOUR_VALUES,
            isReimport
                  ? Messages.Dialog_ReimportData_Title
                  : Messages.Dialog_DeleteData_Title,
            confirmMessage)) {

         String logMessage = isReimport ? LOG_REIMPORT_COMBINED_VALUES : LOG_DELETE_COMBINED_VALUES;

         logMessage = logMessage.concat(String.join(UI.COMMA_SPACE, dataToModifyDetails));

         TourLogManager.addLog(
               TourLogState.DEFAULT,
               logMessage,
               TourLogView.CSS_LOG_TITLE);

         return true;
      }

      return false;
   }

   private boolean actionModifyTourValues_12_Confirm_Dialog(final String toggleState,
                                                            final String dialogTitle,
                                                            final String confirmMessage) {

      if (_prefStore.getBoolean(toggleState)) {

         return true;

      } else {

         final MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(
               Display.getCurrent().getActiveShell(),
               dialogTitle,
               confirmMessage,
               Messages.App_ToggleState_DoNotShowAgain,
               false, // toggle default state
               null,
               null);

         if (dialog.getReturnCode() == Window.OK) {
            _prefStore.setValue(toggleState, dialog.getToggleState());
            return true;
         }
      }

      return false;
   }

   /**
    * @param tourValueTypes
    *           A list of tour values to be re-imported
    * @param tourViewer
    *           Tour viewer containing the selected tours to be re-imported.
    * @param skipToursWithFileNotFound
    *           Indicates whether to re-import or not a tour for which the file is not found
    */
   public void actionReimportSelectedTours(final List<TourValueType> tourValueTypes,
                                           final ITourViewer3 tourViewer,
                                           final boolean skipToursWithFileNotFound) {

      final long start = System.currentTimeMillis();

      if (actionModifyTourValues_10_Confirm(tourValueTypes, true) == false) {
         return;
      }

      // get selected tour IDs

      final Object[] selectedItems = getTourViewerSelectedTourIds(tourViewer);

      if (selectedItems == null || selectedItems.length == 0) {

         MessageDialog.openInformation(Display.getDefault().getActiveShell(),
               Messages.Dialog_ReimportTours_Dialog_Title,
               Messages.Dialog_ModifyTours_Dialog_ToursAreNotSelected);

         return;
      }

      /*
       * convert selection to array
       */
      final Long[] selectedTourIds = new Long[selectedItems.length];
      for (int i = 0; i < selectedItems.length; i++) {
         selectedTourIds[i] = (Long) selectedItems[i];
      }

      setImportId();
      setImportCanceled(false);

      final IRunnableWithProgress importRunnable = new IRunnableWithProgress() {

         Display display = Display.getDefault();

         @Override
         public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            final ReImportStatus reImportStatus = new ReImportStatus();
            final boolean[] isUserAsked_ToCancelReImport = { false };

            final File[] reimportedFile = new File[1];
            int deleted = 0;
            final int numberOfTours = selectedTourIds.length;

            monitor.beginTask(Messages.Import_Data_Dialog_Reimport_Task, numberOfTours);

            // loop: all selected tours in the viewer
            for (final Long tourId : selectedTourIds) {

               if (monitor.isCanceled()) {
                  // stop re-importing but process re-imported tours
                  break;
               }

               monitor.worked(1);
               monitor.subTask(NLS.bind(
                     Messages.Import_Data_Dialog_Reimport_SubTask,
                     new Object[] { ++deleted, numberOfTours }));

               final TourData oldTourData = TourManager.getTour(tourId);

               if (oldTourData == null) {
                  continue;
               }

               reimportTour(tourValueTypes, oldTourData, reimportedFile, skipToursWithFileNotFound, reImportStatus);

               if (reImportStatus.isCanceled_ByUser_TheFileLocationDialog && isUserAsked_ToCancelReImport[0] == false
                     && skipToursWithFileNotFound == false) {

                  // user has canceled the re-import -> ask if the whole re-import should be canceled

                  final boolean[] isCancelReimport = { false };

                  display.syncExec(() -> {

                     if (MessageDialog.openQuestion(display.getActiveShell(),
                           Messages.Import_Data_Dialog_IsCancelReImport_Title,
                           Messages.Import_Data_Dialog_IsCancelReImport_Message)) {

                        isCancelReimport[0] = true;

                     } else {

                        isUserAsked_ToCancelReImport[0] = true;
                     }
                  });

                  if (isCancelReimport[0]) {
                     break;
                  }
               }
            }

            if (reImportStatus.isReImported) {

               updateTourData_InImportView_FromDb(monitor);

               // reselect tours, run in UI thread
               display.asyncExec(tourViewer::reloadViewer);
            }
         }
      };

      try {
         new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, importRunnable);
      } catch (final Exception e) {
         TourLogManager.logEx(e);
         Thread.currentThread().interrupt();
      } finally {

         final double time = (System.currentTimeMillis() - start) / 1000.0;
         TourLogManager.addLog(//
               TourLogState.DEFAULT,
               String.format(RawDataManager.LOG_REIMPORT_END, time));

      }
   }

   /**
    * @param tourData
    * @param skipToursWithFileNotFound
    *           Indicates whether to re-import or not a tour for which the file is not found
    * @param reImportStatus
    * @return Returns <code>null</code> when the user has canceled the file dialog.
    */
   private File actionReimportTour_20_GetImportFile(final TourData tourData,
                                                    final boolean skipToursWithFileNotFound,
                                                    final ReImportStatus reImportStatus) {

      final String[] reimportFilePathName = { null };

      Display.getDefault().syncExec(() -> {

         final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

         // get import file name which is kept in the tour
         final String savedImportFilePathName = tourData.getImportFilePathName();

         if (savedImportFilePathName == null) {

            // import filepath is not available

            // The user doesn't want to look for a new file path for the current tour
            if (skipToursWithFileNotFound) {

               reImportStatus.isCanceled_Auto_ImportFilePathIsEmpty = true;

               return;
            }

            // in older versions the file path name is not saved

            final String tourDateTimeShort = TourManager.getTourDateTimeShort(tourData);

            final boolean okPressed = MessageDialog.openConfirm(
                  activeShell,
                  NLS.bind(Messages.Import_Data_Dialog_Reimport_Title, tourDateTimeShort),
                  NLS.bind(Messages.Import_Data_Dialog_GetReimportedFilePath_Message,
                        tourDateTimeShort,
                        tourDateTimeShort));

            // The user doesn't want to look for a new file path for the current tour
            if (!okPressed) {

               reImportStatus.isCanceled_ByUser_TheFileLocationDialog = true;

               return;
            }

         } else {

            // import filepath is available

            // check import file
            final File importFile = new File(savedImportFilePathName);
            if (importFile.exists()) {

               reimportFilePathName[0] = savedImportFilePathName;

            } else {

               for (final IPath prefReimportFolder : _allPreviousReimportFolders) {

                  /*
                   * Try to use a folder from a previously re-imported tour
                   */

                  final String oldImportFileName = new org.eclipse.core.runtime.Path(savedImportFilePathName).lastSegment();
                  final IPath newImportFilePath = prefReimportFolder.append(oldImportFileName);

                  final String newImportFilePathName = newImportFilePath.toOSString();
                  final File newImportFile = new File(newImportFilePathName);
                  if (newImportFile.exists()) {

                     // re-import file exists in the same folder
                     reimportFilePathName[0] = newImportFilePathName;
                  }
               }

               if (reimportFilePathName[0] == null) {

                  //The user doesn't want to look for a new file path for the current tour.
                  if (skipToursWithFileNotFound) {

                     reImportStatus.isCanceled_Auto_TheFileLocationDialog = true;

                     return;
                  }

                  final boolean okPressed = MessageDialog.openQuestion(
                        activeShell,
                        Messages.Dialog_ReimportData_Title,
                        NLS.bind(
                              Messages.Import_Data_Dialog_GetAlternativePath_Message,
                              savedImportFilePathName));

                  // The user doesn't want to look for a new file path for the current tour
                  if (!okPressed) {

                     reImportStatus.isCanceled_ByUser_TheFileLocationDialog = true;

                     return;
                  }
               }
            }
         }

         if (reimportFilePathName[0] == null) {

            // create dialog title
            final String tourDateTime = tourData.getTourStartTime().format(TimeTools.Formatter_DateTime_ML);
            final String deviceName = tourData.getDeviceName();
            final String dataFormat = deviceName == null ? UI.EMPTY_STRING : deviceName;
            final String fileName = savedImportFilePathName == null ? UI.EMPTY_STRING : savedImportFilePathName;
            final String dialogTitle = String.format(Messages.Import_Data_Dialog_ReimportFile_Title,
                  tourDateTime,
                  fileName,
                  dataFormat);

            final FileDialog dialog = new FileDialog(activeShell, SWT.OPEN);
            dialog.setText(dialogTitle);

            if (savedImportFilePathName != null) {

               // select file location from the tour

               final IPath importFilePath = new org.eclipse.core.runtime.Path(savedImportFilePathName);
               final String importFileName = importFilePath.lastSegment();

               dialog.setFileName(importFileName);
               dialog.setFilterPath(savedImportFilePathName);

            } else if (_previousReimportFolder != null) {

               dialog.setFilterPath(_previousReimportFolder.toOSString());
            }

            reimportFilePathName[0] = dialog.open();
         }
      });

      if (reimportFilePathName[0] == null) {

         // user has canceled the file dialog
         return null;
      }

      /*
       * Keep selected file path which is used to re-import following tours from the same folder
       * that the user do not have to reselect again and again.
       */
      final IPath currentReimportFolder = new org.eclipse.core.runtime.Path(reimportFilePathName[0]).removeLastSegments(1);

      _previousReimportFolder = currentReimportFolder;
      _allPreviousReimportFolders.add(currentReimportFolder);

      return new File(reimportFilePathName[0]);
   }

   private boolean actionReimportTour_30(final List<TourValueType> tourValueTypes,
                                         final File reimportedFile,
                                         final TourData oldTourData) {

      boolean isTourReImported = false;

      final Long oldTourId = oldTourData.getTourId();
      final String reimportFileNamePath = reimportedFile.getAbsolutePath();

      /*
       * tour must be removed otherwise it would be recognized as a duplicate and therefore not
       * imported
       */
      final TourData oldTourDataInImportView = _toursInImportView.remove(oldTourId);

      if (importRawData(reimportedFile, null, false, null, false, true)) {

         /*
          * tour(s) could be re-imported from the file, check if it contains a valid tour
          */

         TourData clonedTourData = null;

         try {

            clonedTourData = (TourData) oldTourData.clone();

            // loop: For each tour value type, we save the associated data for future display
            //to compare with the new data
            for (final TourValueType tourValueType : tourValueTypes) {

               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.TOUR_MARKER) {

                  clonedTourData.setTourMarkers(new HashSet<>(oldTourData.getTourMarkers()));

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_CADENCE) {

                  clonedTourData.setAvgCadence(oldTourData.getAvgCadence());

                  clonedTourData.setCadenceMultiplier(oldTourData.getCadenceMultiplier());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_ELEVATION) {

                  clonedTourData.setTourAltDown(oldTourData.getTourAltDown());
                  clonedTourData.setTourAltUp(oldTourData.getTourAltUp());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_GEAR) {

                  clonedTourData.setFrontShiftCount(oldTourData.getFrontShiftCount());
                  clonedTourData.setRearShiftCount(oldTourData.getRearShiftCount());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_POWER_AND_PULSE) {

                  clonedTourData.setPower_Avg(oldTourData.getPower_Avg());
                  clonedTourData.setAvgPulse(oldTourData.getAvgPulse());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_POWER_AND_SPEED) {

                  clonedTourData.setPower_Avg(oldTourData.getPower_Avg());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_TEMPERATURE) {

                  clonedTourData.setAvgTemperature(oldTourData.getAvgTemperature());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.ALL_TIME_SLICES ||
                     tourValueType == TourValueType.TIME_SLICES_TIMER_PAUSES) {

                  clonedTourData.setTourDeviceTime_Paused(oldTourData.getTourDeviceTime_Paused());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.TOUR_CALORIES) {

                  clonedTourData.setCalories(oldTourData.getCalories());

               }
               if (tourValueType == TourValueType.ENTIRE_TOUR ||
                     tourValueType == TourValueType.IMPORT_FILE_LOCATION) {

                  clonedTourData.setImportFilePath(oldTourData.getImportFilePathName());

               }
            }
         } catch (final CloneNotSupportedException e) {
            StatusUtil.log(e);
         }

         TourData updatedTourData = actionReimportTour_40(tourValueTypes, reimportedFile, oldTourData);

         if (updatedTourData == null) {

            // error is already logged

         } else {

            isTourReImported = true;

            // set re-import file path as new location
            updatedTourData.setImportFilePath(reimportFileNamePath);

            // check if tour is saved
            final TourPerson tourPerson = oldTourData.getTourPerson();
            if (tourPerson != null) {

               // re-save tour when the re-imported tour was already saved

               updatedTourData.setTourPerson(tourPerson);

               /*
                * Save tour but don't fire a change event because the tour editor would set the tour
                * to dirty
                */
               final TourData savedTourData = TourManager.saveModifiedTour(updatedTourData, false);

               updatedTourData = savedTourData;
            }

            TourLogManager.addSubLog(TourLogState.IMPORT_OK,
                  NLS.bind(LOG_IMPORT_TOUR_IMPORTED,
                        updatedTourData.getTourStartTime().format(TimeTools.Formatter_DateTime_S),
                        reimportFileNamePath));

            // Print the old vs new data comparison
            for (final TourValueType tourValueType : tourValueTypes) {
               displayTourModifiedDataDifferences(tourValueType, clonedTourData, updatedTourData);
            }

            // check if tour is displayed in the import view
            if (oldTourDataInImportView != null) {

               // replace tour data in the import view

               _toursInImportView.put(updatedTourData.getTourId(), updatedTourData);
            }
         }

      } else {

         TourLogManager.addSubLog(TourLogState.IMPORT_ERROR, reimportFileNamePath);

         if (oldTourDataInImportView != null) {

            // re-attach removed tour

            _toursInImportView.put(oldTourId, oldTourDataInImportView);
         }
      }

      return isTourReImported;
   }

   /**
    * @param tourValueTypes
    *           A list of tour values to be re-imported
    * @param reImportedFile
    * @param oldTourData
    * @return Returns {@link TourData} with the re-imported time slices or <code>null</code> when an
    *         error occurred.
    */
   private TourData actionReimportTour_40(final List<TourValueType> tourValueTypes,
                                          final File reImportedFile,
                                          final TourData oldTourData) {

      TourLogManager.showLogView();

      final String oldTourDateTimeShort = TourManager.getTourDateTimeShort(oldTourData);
      String message = null;

      for (final TourData reimportedTourData : _newlyImportedTours.values()) {

         // skip tours which have a different tour start time
         final long reimportTourStartTime = reimportedTourData.getTourStartTimeMS();
         final long oldTourStartTime = oldTourData.getTourStartTimeMS();
         final long timeDiff = reimportTourStartTime > oldTourStartTime
               ? reimportTourStartTime - oldTourStartTime
               : oldTourStartTime - reimportTourStartTime;

         /**
          * Check time difference, this is VERY important because one .hac file contains multiple
          * tours !!!
          * <p>
          * It must be >60 seconds because db version < 7 (9.01) had no saved seconds !!!
          */
         if (timeDiff > 65_000
               // disabled for .fit files because they can have different tour start times (of some seconds)
               && !reImportedFile.getName().toLowerCase().endsWith(FILE_EXTENSION_FIT)) {

            continue;
         }

         if (oldTourData.timeSerie != null && reimportedTourData.timeSerie != null) {

            /*
             * Data series must have the same number of time slices, otherwise the markers can be
             * off the array bounds, this problem could be solved but takes time to do it.
             */
            final int oldLength = oldTourData.timeSerie.length;
            final int reimportedLength = reimportedTourData.timeSerie.length;

            if (oldLength != reimportedLength) {

               // log error
               message = NLS.bind(
                     Messages.Import_Data_Log_ReimportIsInvalid_WrongSliceNumbers,
                     new Object[] {
                           oldTourDateTimeShort,
                           reImportedFile.toString(),
                           oldLength,
                           reimportedLength });

               break;
            }
         }

         /*
          * Ensure that the re-imported tour has the same tour id
          */
         final long oldTourId = oldTourData.getTourId().longValue();
         final long reimportTourId = reimportedTourData.getTourId().longValue();

         if (oldTourId != reimportTourId) {

            message = NLS.bind(
                  Messages.Import_Data_Log_ReimportIsInvalid_DifferentTourId_Message,
                  new Object[] {
                        oldTourDateTimeShort,
                        reImportedFile.toString(),
                        oldTourId,
                        reimportTourId });

            break;
         }

         TourData newTourData = null;

         if (tourValueTypes.get(0) == TourValueType.ENTIRE_TOUR) {

            // replace complete tour

            TourManager.getInstance().removeTourFromCache(oldTourData.getTourId());

            newTourData = reimportedTourData;

            // keep body weight from old tour
            newTourData.setBodyWeight(oldTourData.getBodyWeight());

         } else {

            if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_CADENCE)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_ELEVATION)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_GEAR)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_POWER_AND_PULSE)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_POWER_AND_SPEED)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_RUNNING_DYNAMICS)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_SWIMMING)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_TEMPERATURE)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_TIMER_PAUSES)
                  || tourValueTypes.contains(TourValueType.TIME_SLICES_TRAINING)) {

               // replace part of the tour

               actionReimportTour_50_ReplacesValues(tourValueTypes, oldTourData, reimportedTourData);
            }

            if (tourValueTypes.contains(TourValueType.TOUR_CALORIES)) {

               oldTourData.setCalories(reimportedTourData.getCalories());
            }

            if (tourValueTypes.contains(TourValueType.TOUR_MARKER)) {

               oldTourData.setTourMarkers(reimportedTourData.getTourMarkers());
            }

            newTourData = oldTourData;
         }

         if (newTourData != null) {

            /*
             * Compute computed values
             */
            newTourData.clearComputedSeries();

            newTourData.computeAltitudeUpDown();
            newTourData.computeTourMovingTime();
            newTourData.computeComputedValues();

            // maintain list, that another call of this method do not find this tour again
            _newlyImportedTours.remove(oldTourData.getTourId());

            return newTourData;
         }
      }

      /*
       * A re-import failed, display an error message
       */
      if (message == null) {

         // undefined error
         TourLogManager.subLog_Error(NLS.bind(
               Messages.Import_Data_Log_ReimportIsInvalid_TourNotFoundInFile_Message,
               new Object[] {
                     oldTourDateTimeShort,
                     reImportedFile.toString() }));

      } else {
         TourLogManager.subLog_Error(message);
      }

      return null;
   }

   /**
    * Replace parts of the tour values.
    *
    * @param tourValueTypes
    * @param oldTourData
    * @param reimportedTourData
    */
   private void actionReimportTour_50_ReplacesValues(final List<TourValueType> tourValueTypes,
                                                     final TourData oldTourData,
                                                     final TourData reimportedTourData) {

      // Cadence
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_CADENCE)) {

         // re-import cadence/stride only
         oldTourData.setCadenceSerie(reimportedTourData.getCadenceSerie());
         oldTourData.setCadenceMultiplier(reimportedTourData.getCadenceMultiplier());
         oldTourData.setIsStrideSensorPresent(reimportedTourData.isStrideSensorPresent());
      }

      // Elevation
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_ELEVATION)) {

         // re-import altitude only
         oldTourData.altitudeSerie = reimportedTourData.altitudeSerie;
      }

      // Gear
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_GEAR)) {

         // re-import gear only
         oldTourData.gearSerie = reimportedTourData.gearSerie;
         oldTourData.setFrontShiftCount(reimportedTourData.getFrontShiftCount());
         oldTourData.setRearShiftCount(reimportedTourData.getRearShiftCount());
      }

      // Power
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES)
            || tourValueTypes.contains(TourValueType.TIME_SLICES_POWER_AND_PULSE)
            || tourValueTypes.contains(TourValueType.TIME_SLICES_POWER_AND_SPEED)) {

         // re-import power and speed only when it's from the device
         final boolean isDevicePower = reimportedTourData.isPowerSerieFromDevice();
         if (isDevicePower) {

            final float[] powerSerie = reimportedTourData.getPowerSerie();
            if (powerSerie != null) {
               oldTourData.setPowerSerie(powerSerie);
            }

//SET_FORMATTING_OFF

            oldTourData.setPower_Avg(                          reimportedTourData.getPower_Avg());
            oldTourData.setPower_Max(                          reimportedTourData.getPower_Max());
            oldTourData.setPower_Normalized(                   reimportedTourData.getPower_Normalized());
            oldTourData.setPower_FTP(                          reimportedTourData.getPower_FTP());

            oldTourData.setPower_TotalWork(                    reimportedTourData.getPower_TotalWork());
            oldTourData.setPower_TrainingStressScore(          reimportedTourData.getPower_TrainingStressScore());
            oldTourData.setPower_IntensityFactor(              reimportedTourData.getPower_IntensityFactor());

            oldTourData.setPower_PedalLeftRightBalance(        reimportedTourData.getPower_PedalLeftRightBalance());
            oldTourData.setPower_AvgLeftPedalSmoothness(       reimportedTourData.getPower_AvgLeftPedalSmoothness());
            oldTourData.setPower_AvgLeftTorqueEffectiveness(   reimportedTourData.getPower_AvgLeftTorqueEffectiveness());
            oldTourData.setPower_AvgRightPedalSmoothness(      reimportedTourData.getPower_AvgRightPedalSmoothness());
            oldTourData.setPower_AvgRightTorqueEffectiveness(  reimportedTourData.getPower_AvgRightTorqueEffectiveness());

//SET_FORMATTING_ON
         }
      }

      // Pulse
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_POWER_AND_PULSE)) {

         // re-import pulse

         oldTourData.pulseSerie = reimportedTourData.pulseSerie;

         oldTourData.pulseTime_Milliseconds = reimportedTourData.pulseTime_Milliseconds;
         oldTourData.pulseTime_TimeIndex = reimportedTourData.pulseTime_TimeIndex;
      }

      // Speed
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_POWER_AND_SPEED)) {

         // re-import speed

         final boolean isDeviceSpeed = reimportedTourData.isSpeedSerieFromDevice();
         if (isDeviceSpeed) {
            final float[] speedSerie = reimportedTourData.getSpeedSerieFromDevice();
            if (speedSerie != null) {
               oldTourData.setSpeedSerie(speedSerie);
            }
         }
      }

      // Running Dynamics
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_RUNNING_DYNAMICS)) {

         // re-import only running dynamics

         oldTourData.runDyn_StanceTime = reimportedTourData.runDyn_StanceTime;
         oldTourData.runDyn_StanceTimeBalance = reimportedTourData.runDyn_StanceTimeBalance;
         oldTourData.runDyn_StepLength = reimportedTourData.runDyn_StepLength;
         oldTourData.runDyn_VerticalOscillation = reimportedTourData.runDyn_VerticalOscillation;
         oldTourData.runDyn_VerticalRatio = reimportedTourData.runDyn_VerticalRatio;
      }

      // Swimming
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_SWIMMING)) {

         // re-import only swimming

         oldTourData.swim_LengthType = reimportedTourData.swim_LengthType;
         oldTourData.swim_Cadence = reimportedTourData.swim_Cadence;
         oldTourData.swim_Strokes = reimportedTourData.swim_Strokes;
         oldTourData.swim_StrokeStyle = reimportedTourData.swim_StrokeStyle;
         oldTourData.swim_Time = reimportedTourData.swim_Time;
      }

      // Temperature
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_TEMPERATURE)) {

         // re-import temperature only

         oldTourData.temperatureSerie = reimportedTourData.temperatureSerie;
      }

      // Training
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_TRAINING)) {

         // re-import training only

         oldTourData.setTraining_TrainingEffect_Aerob(reimportedTourData.getTraining_TrainingEffect_Aerob());
         oldTourData.setTraining_TrainingEffect_Anaerob(reimportedTourData.getTraining_TrainingEffect_Anaerob());
         oldTourData.setTraining_TrainingPerformance(reimportedTourData.getTraining_TrainingPerformance());
      }

      // Timer pauses
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES) || tourValueTypes.contains(TourValueType.TIME_SLICES_TIMER_PAUSES)) {

         // re-import pauses only

         oldTourData.setTourDeviceTime_Recorded(reimportedTourData.getTourDeviceTime_Recorded());

         long totalTourTimerPauses = 0;
         final long[] pausedTime_Start = reimportedTourData.getPausedTime_Start();
         if (pausedTime_Start != null && pausedTime_Start.length > 0) {
            final List<Long> listPausedTime_Start = Arrays.stream(pausedTime_Start).boxed().collect(Collectors.toList());
            final List<Long> listPausedTime_End = Arrays.stream(reimportedTourData.getPausedTime_End()).boxed().collect(Collectors.toList());
            oldTourData.finalizeTour_TimerPauses(listPausedTime_Start, listPausedTime_End);
         } else {
            oldTourData.setPausedTime_Start(reimportedTourData.getPausedTime_Start());
            oldTourData.setPausedTime_End(reimportedTourData.getPausedTime_End());
         }

         totalTourTimerPauses = reimportedTourData.getTourDeviceTime_Paused();

         oldTourData.setTourDeviceTime_Paused(totalTourTimerPauses);
      }

      // ALL
      if (tourValueTypes.contains(TourValueType.ALL_TIME_SLICES)) {

         // re-import all other data series

         // update device data
         oldTourData.setDeviceFirmwareVersion(reimportedTourData.getDeviceFirmwareVersion());
         oldTourData.setDeviceId(reimportedTourData.getDeviceId());
         oldTourData.setDeviceName(reimportedTourData.getDeviceName());

         oldTourData.distanceSerie = reimportedTourData.distanceSerie;
         oldTourData.latitudeSerie = reimportedTourData.latitudeSerie;
         oldTourData.longitudeSerie = reimportedTourData.longitudeSerie;
         oldTourData.timeSerie = reimportedTourData.timeSerie;

         oldTourData.computeGeo_Bounds();
      }
   }

   public void clearInvalidFilesList() {
      _invalidFilesList.clear();
   }

   public void deleteTourValues(final List<TourValueType> tourValueTypes, final ITourViewer3 tourViewer) {

      final long start = System.currentTimeMillis();

      if (!actionModifyTourValues_10_Confirm(tourValueTypes, false)) {
         return;
      }

      final Object[] selectedItems = getTourViewerSelectedTourIds(tourViewer);

      if (selectedItems == null || selectedItems.length == 0) {

         MessageDialog.openInformation(Display.getDefault().getActiveShell(),
               Messages.Dialog_DeleteTourValues_Dialog_Title,
               Messages.Dialog_ModifyTours_Dialog_ToursAreNotSelected);

         return;
      }

      /*
       * convert selection to array
       */
      final Long[] selectedTourIds = new Long[selectedItems.length];
      for (int i = 0; i < selectedItems.length; i++) {
         selectedTourIds[i] = (Long) selectedItems[i];
      }

      final IRunnableWithProgress importRunnable = new IRunnableWithProgress() {

         Display display = Display.getDefault();

         @Override
         public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            int imported = 0;
            final int importSize = selectedTourIds.length;

            monitor.beginTask(Messages.Import_Data_Dialog_DeleteTourValues_Task, importSize);

            // loop: all selected tours in the viewer
            for (final Long selectedTourId : selectedTourIds) {

               monitor.worked(1);
               monitor.subTask(NLS.bind(
                     Messages.Import_Data_Dialog_Reimport_SubTask,
                     new Object[] { ++imported, importSize }));

               final TourData tourData = TourManager.getTour(selectedTourId);

               if (tourData == null) {
                  continue;
               }

               deleteTourValuesFromTour(tourValueTypes, tourData);

               if (monitor.isCanceled()) {

                  // user has canceled the deletion -> ask if the whole deletion should be canceled

                  final boolean[] isCancelDeletion = { false };

                  display.syncExec(() -> {

                     if (MessageDialog.openQuestion(display.getActiveShell(),
                           Messages.Import_Data_Dialog_IsCancelTourValuesDeletion_Title,
                           Messages.Import_Data_Dialog_IsCancelTourValuesDeletion_Message)) {

                        isCancelDeletion[0] = true;

                     }
                  });

                  if (isCancelDeletion[0]) {
                     break;
                  }
               }
            }

            updateTourData_InImportView_FromDb(monitor);

            // reselect tours, run in UI thread
            display.asyncExec(tourViewer::reloadViewer);
         }
      };

      try {
         new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, importRunnable);
      } catch (final Exception e) {
         TourLogManager.logEx(e);
      } finally {

         final double time = (System.currentTimeMillis() - start) / 1000.0;
         TourLogManager.addLog(//
               TourLogState.DEFAULT,
               String.format(RawDataManager.LOG_DELETE_TOURVALUES_END, time));
      }
   }

   public void deleteTourValuesFromTour(final List<TourValueType> tourValueTypes,
                                        final TourData tourData) {

      final Long tourId = tourData.getTourId();

      /*
       * tour must be removed otherwise it would be recognized as a duplicate and therefore not
       * imported
       */
      final TourData oldTourDataInImportView = _toursInImportView.remove(tourId);

      TourData clonedTourData = null;
      try {

         clonedTourData = (TourData) tourData.clone();

         // loop: For each tour value type, we save the associated data for future display
         //to compare with the new data
         for (final TourValueType tourValueType : tourValueTypes) {

            switch (tourValueType) {

            case TIME_SLICES_TIME:
               for (int index = 0; index < tourData.timeSerie.length; ++index) {
                  tourData.timeSerie[index] = 0;
               }
               tourData.setTourDeviceTime_Elapsed(0);
               tourData.setPausedTime_Start(null);
               tourData.setPausedTime_End(null);
               tourData.setTourDeviceTime_Paused(0);
               tourData.setTourDeviceTime_Recorded(0);
               tourData.setTourComputedTime_Moving(0);
               tourData.clearComputedSeries();
               break;
            case TOUR_MARKER:
               clonedTourData.setTourMarkers(new HashSet<>(tourData.getTourMarkers()));

               tourData.setTourMarkers(new HashSet<>());
               break;

            case TIME_SLICES_CADENCE:
               clonedTourData.setCadenceSerie(tourData.getCadenceSerie());
               clonedTourData.setAvgCadence(tourData.getAvgCadence());
               clonedTourData.setCadenceMultiplier(tourData.getCadenceMultiplier());

               tourData.setCadenceSerie(null);
               tourData.setAvgCadence(0);
               tourData.setCadenceMultiplier(0);
               tourData.setCadenceZone_SlowTime(0);
               tourData.setCadenceZone_FastTime(0);
               break;

            case TIME_SLICES_ELEVATION:
               clonedTourData.setTourAltDown(tourData.getTourAltDown());
               clonedTourData.setTourAltUp(tourData.getTourAltUp());

               tourData.altitudeSerie = null;
               tourData.setTourAltUp(0);
               tourData.setTourAltDown(0);
               break;

            case TIME_SLICES_GEAR:
               clonedTourData.setFrontShiftCount(tourData.getFrontShiftCount());
               clonedTourData.setRearShiftCount(tourData.getRearShiftCount());

               tourData.setFrontShiftCount(0);
               tourData.setRearShiftCount(0);
               break;

            case TIME_SLICES_POWER_AND_PULSE:
               clonedTourData.setPower_Avg(tourData.getPower_Avg());
               clonedTourData.setAvgPulse(tourData.getAvgPulse());

               tourData.setPowerSerie(null);
               tourData.setPower_Avg(0);
               tourData.pulseSerie = null;
               tourData.pulseTime_Milliseconds = null;
               tourData.pulseTime_TimeIndex = null;
               tourData.setAvgPulse(0);
               break;

            case TIME_SLICES_POWER_AND_SPEED:
               clonedTourData.setPower_Avg(tourData.getPower_Avg());

               tourData.setPowerSerie(null);
               tourData.setPower_Avg(0);
               tourData.setSpeedSerie(null);
               break;

            case TIME_SLICES_TEMPERATURE:
               clonedTourData.setAvgTemperature(tourData.getAvgTemperature());

               tourData.temperatureSerie = null;
               tourData.setAvgTemperature(0);
               break;

            case TIME_SLICES_TIMER_PAUSES:
               clonedTourData.setTourDeviceTime_Paused(tourData.getTourDeviceTime_Paused());

               tourData.setPausedTime_Start(null);
               tourData.setPausedTime_End(null);
               tourData.setTourDeviceTime_Paused(0);
               tourData.setTourDeviceTime_Recorded(tourData.getTourDeviceTime_Elapsed());
               break;

            case TOUR_CALORIES:
               clonedTourData.setCalories(tourData.getCalories());

               tourData.setCalories(0);
               break;

            case IMPORT_FILE_LOCATION:
               clonedTourData.setImportFilePath(tourData.getImportFilePathName());
               break;

            case ALL_TIME_SLICES:
            case ENTIRE_TOUR:
            default:
               break;
            }
         }
      } catch (final CloneNotSupportedException e) {
         StatusUtil.log(e);
      }
      TourManager.saveModifiedTour(tourData, false);

      TourLogManager.showLogView();
      TourLogManager.addSubLog(TourLogState.IMPORT_OK,
            tourData.getTourStartTime().format(TimeTools.Formatter_DateTime_S));

      for (final TourValueType tourValueType : tourValueTypes) {
         displayTourModifiedDataDifferences(tourValueType, clonedTourData, tourData);
      }

      // check if tour is displayed in the import view
      if (oldTourDataInImportView != null) {

         // replace tour data in the import view

         _toursInImportView.put(tourData.getTourId(), tourData);
      }
   }

   public DeviceData getDeviceData() {
      return _deviceData;
   }

   private List<TourbookDevice> getDeviceListSortedByPriority() {

      if (_devicesBySortPriority == null) {

         _devicesBySortPriority = new ArrayList<>(DeviceManager.getDeviceList());

         // sort device list by sorting priority
         Collections.sort(_devicesBySortPriority, (tourbookDevice1, tourbookDevice2) -> {

            // 1. sort by priority
            final int sortByPrio = tourbookDevice1.extensionSortPriority - tourbookDevice2.extensionSortPriority;

            // 2. sort by name
            if (sortByPrio == 0) {
               return tourbookDevice1.deviceId.compareTo(tourbookDevice2.deviceId);
            }

            return sortByPrio;
         });

         _devicesByExtension = new HashMap<>();

         for (final TourbookDevice device : _devicesBySortPriority) {
            _devicesByExtension.put(device.fileExtension.toLowerCase(), device);
         }
      }

      return _devicesBySortPriority;
   }

   public HashSet<String> getImportedFiles() {
      return _importedFileNames;
   }

   /**
    * @return Returns an {@link ArrayList} containing the imported tours.
    */
   public ArrayList<TourData> getImportedTourList() {

      final Collection<TourData> importedToursCollection = _toursInImportView.values();
      final ArrayList<TourData> importedTours = new ArrayList<>(importedToursCollection);

      return importedTours;
   }

   /**
    * @return Returns all {@link TourData} which has been imported or received and are displayed in
    *         the import view, tour id is the key.
    */
   public Map<Long, TourData> getImportedTours() {
      return _toursInImportView;
   }

   /**
    * @return Returns the import year or <code>-1</code> when the year was not set
    */
   public int getImportYear() {
      return _importState_ImportYear;
   }

   public ArrayList<String> getInvalidFilesList() {
      return _invalidFilesList;
   }

   public ArrayList<TourTag> getTempTourTags() {
      return _tempTourTags;
   }

   public ArrayList<TourType> getTempTourTypes() {
      return _tempTourTypes;
   }

   private Object[] getTourViewerSelectedTourIds(final ITourViewer3 tourViewer) {

      Object[] selectedItems = null;
      if (tourViewer instanceof TourBookView) {
         selectedItems = (((TourBookView) tourViewer).getSelectedTourIDs()).toArray();
      } else if (tourViewer instanceof CollatedToursView) {
         selectedItems = (((CollatedToursView) tourViewer).getSelectedTourIDs()).toArray();
      } else if (tourViewer instanceof RawDataView) {
         selectedItems = (((RawDataView) tourViewer).getSelectedTourIDs()).toArray();
      }
      return selectedItems;
   }

   /**
    * Import a tour from a file, all imported tours can be retrieved with
    * {@link #getImportedTours()}
    *
    * @param importFile
    *           the file to be imported
    * @param destinationPath
    *           if not null copy the file to this path
    * @param buildNewFileNames
    *           if <code>true</code> create a new filename depending on the content of the file,
    *           keep old name if false
    * @param fileCollision
    *           behavior if destination file exists (ask if null)
    * @param isTourDisplayedInImportView
    *           When <code>true</code>, the newly imported tours are displayed in the import view,
    *           otherwise they are imported into {@link #_newlyImportedTours} but not displayed in
    *           the import view.
    * @return Returns <code>true</code> when the import was successfully
    */
   public boolean importRawData(final File importFile,
                                final String destinationPath,
                                final boolean buildNewFileNames,
                                final FileCollisionBehavior fileCollision,
                                final boolean isTourDisplayedInImportView,
                                final boolean isReimport) {

      final String importFilePathName = importFile.getAbsolutePath();
      final Display display = Display.getDefault();

      // check if importFile exist
      if (importFile.exists() == false) {

         display.syncExec(() -> {

            final Shell activeShell = display.getActiveShell();

            // during initialization there is no active shell
            if (activeShell != null) {

               MessageDialog.openError(
                     activeShell,
                     Messages.DataImport_Error_file_does_not_exist_title,
                     NLS.bind(Messages.DataImport_Error_file_does_not_exist_msg, importFilePathName));
            }
         });

         return false;
      }

      // find the file extension in the filename
      final int dotPos = importFilePathName.lastIndexOf(UI.SYMBOL_DOT);
      if (dotPos == -1) {
         return false;
      }
      final String fileExtension = importFilePathName.substring(dotPos + 1);

      final List<TourbookDevice> deviceList = getDeviceListSortedByPriority();

      _isImported = false;

      BusyIndicator.showWhile(null, () -> {

         boolean isDataImported = false;
         final ArrayList<String> additionalImportedFiles = new ArrayList<>();

         /*
          * try to import from all devices which have the defined extension
          */
         for (final TourbookDevice device1 : deviceList) {

            final String deviceFileExtension = device1.fileExtension;

            if (deviceFileExtension.equals(UI.SYMBOL_STAR) || deviceFileExtension.equalsIgnoreCase(fileExtension)) {

               // Check if the file we want to import requires confirmation and if yes, ask user
               if (device1.userConfirmationRequired()) {
                  display.syncExec(() -> {
                     final Shell activeShell = display.getActiveShell();
                     if (activeShell != null) {
                        if (MessageDialog.openConfirm(
                              Display.getCurrent().getActiveShell(),
                              NLS.bind(Messages.DataImport_ConfirmImport_title, device1.visibleName),
                              device1.userConfirmationMessage())) {
                           _isImportCanceled = false;
                        } else {
                           _isImportCanceled = true;
                        }
                     }
                  });
               }

               if (_isImportCanceled) {
                  _isImported = true; // don't display an error to the user
                  return;
               }

               // device file extension was found in the filename extension
               if (importRawData_10(
                     device1,
                     importFilePathName,
                     destinationPath,
                     buildNewFileNames,
                     fileCollision,
                     isTourDisplayedInImportView,
                     isReimport)) {

                  isDataImported = true;
                  _isImported = true;

                  final ArrayList<String> deviceImportedFiles = device1.getAdditionalImportedFiles();
                  if (deviceImportedFiles != null) {
                     additionalImportedFiles.addAll(deviceImportedFiles);
                  }

                  break;
               }

               if (_isImportCanceled) {
                  break;
               }
            }
         }

         if (isDataImported == false && !_isImportCanceled) {

            /*
             * when data has not imported yet, try all available devices without checking the
             * file extension
             */
            for (final TourbookDevice device2 : deviceList) {
               if (importRawData_10(
                     device2,
                     importFilePathName,
                     destinationPath,
                     buildNewFileNames,
                     fileCollision,
                     isTourDisplayedInImportView,
                     isReimport)) {

                  isDataImported = true;
                  _isImported = true;

                  final ArrayList<String> otherImportedFiles = device2.getAdditionalImportedFiles();
                  if (otherImportedFiles != null) {
                     additionalImportedFiles.addAll(otherImportedFiles);
                  }

                  break;
               }
            }
         }

         if (isDataImported) {

            _importedFileNames.add(_lastImportedFileName);

            if (!additionalImportedFiles.isEmpty()) {
               _importedFileNamesChildren.addAll(additionalImportedFiles);
            }
         }

         // cleanup
         additionalImportedFiles.clear();
      });

      return _isImported;
   }

   /**
    * import the raw data of the given file
    *
    * @param device
    *           the device which is able to process the data of the file
    * @param sourceFileName
    *           the file to be imported
    * @param destinationPath
    *           if not null copy the file to this path
    * @param buildNewFileName
    *           if true create a new filename depending on the content of the file, keep old name if
    *           false
    * @param fileCollision
    *           behavior if destination file exists (ask if null)
    * @param isTourDisplayedInImportView
    * @return Returns <code>true</code> when data has been imported.
    */
   private boolean importRawData_10(final TourbookDevice device,
                                    String sourceFileName,
                                    final String destinationPath,
                                    final boolean buildNewFileName,
                                    FileCollisionBehavior fileCollision,
                                    final boolean isTourDisplayedInImportView,
                                    final boolean isReimport) {

      if (fileCollision == null) {
         fileCollision = new FileCollisionBehavior();
      }

      _newlyImportedTours.clear();

      device.setIsChecksumValidation(_importState_IsChecksumValidation);

      if (device.validateRawData(sourceFileName)) {

         // file contains valid raw data for the raw data reader

         if (_importState_ImportYear != -1) {
            device.setImportYear(_importState_ImportYear);
         }

         device.setMergeTracks(_importState_IsMergeTracks);
         device.setCreateTourIdWithTime(_importState_IsCreateTourIdWithTime);
         device.setConvertWayPoints(_importState_IsConvertWayPoints);

         // copy file to destinationPath
         if (destinationPath != null) {

            final String newFileName = importRawData_20_CopyFile(
                  device,
                  sourceFileName,
                  destinationPath,
                  buildNewFileName,
                  fileCollision);

            if (newFileName == null) {
               return false;
            }

            sourceFileName = newFileName;
         }

         _lastImportedFileName = sourceFileName;

         boolean isImported = false;

         try {

            isImported = device.processDeviceData(
                  sourceFileName,
                  _deviceData,
                  _toursInImportView,
                  _newlyImportedTours,
                  isReimport);

         } catch (final Exception e) {
            TourLogManager.logEx(e);
         }

         if (isTourDisplayedInImportView) {
            _toursInImportView.putAll(_newlyImportedTours);
         }

         // keep tours in _newlyImportedTours because they are used when tours are re-imported

         return isImported;
      }

      return false;

   }

   private String importRawData_20_CopyFile(final TourbookDevice device,
                                            final String sourceFileName,
                                            final String destinationPath,
                                            final boolean buildNewFileName,
                                            final FileCollisionBehavior fileCollision) {

      String destFileName = new File(sourceFileName).getName();

      if (buildNewFileName) {

         destFileName = null;

         try {
            destFileName = device.buildFileNameFromRawData(sourceFileName);
         } catch (final Exception e) {
            TourLogManager.logEx(e);
         } finally {

            if (destFileName == null) {

               MessageDialog.openError(
                     Display.getDefault().getActiveShell(),
                     Messages.Import_Data_Error_CreatingFileName_Title,
                     NLS.bind(
                           Messages.Import_Data_Error_CreatingFileName_Message, //
                           new Object[] {
                                 sourceFileName,
                                 new org.eclipse.core.runtime.Path(destinationPath)
                                       .addTrailingSeparator()
                                       .toString(), TEMP_IMPORTED_FILE }));

               destFileName = TEMP_IMPORTED_FILE;
            }
         }
      }
      final File newFile = new File(
            (new org.eclipse.core.runtime.Path(destinationPath)
                  .addTrailingSeparator()
                  .toString() + destFileName));

      // get source file
      final File fileIn = new File(sourceFileName);

      // check if file already exist
      if (newFile.exists()) {

         // TODO allow user to rename the file

         boolean keepFile = false; // for MessageDialog result
         if (fileCollision.value == FileCollisionBehavior.ASK) {

            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            final MessageDialog messageDialog = new MessageDialog(
                  shell,
                  Messages.Import_Wizard_Message_Title,
                  null,
                  NLS.bind(Messages.Import_Wizard_Message_replace_existing_file, newFile),
                  MessageDialog.QUESTION,
                  new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.NO_TO_ALL_LABEL },
                  0);
            messageDialog.open();
            final int returnCode = messageDialog.getReturnCode();
            switch (returnCode) {

            case 1: // YES_TO_ALL
               fileCollision.value = FileCollisionBehavior.REPLACE;
               break;

            case 3: // NO_TO_ALL
               fileCollision.value = FileCollisionBehavior.KEEP;
            case 2: // NO
               keepFile = true;
               break;

            default:
               break;
            }
         }

         if (fileCollision.value == FileCollisionBehavior.KEEP || keepFile) {
            _isImportCanceled = true;
            fileIn.delete();
            return null;
         }
      }

      // copy source file into destination file
      try (FileInputStream inReader = new FileInputStream(fileIn);
            FileOutputStream outReader = new FileOutputStream(newFile)) {

         int c;

         while ((c = inReader.read()) != -1) {
            outReader.write(c);
         }

      } catch (final IOException e) {
         TourLogManager.logEx(e);
         return null;
      }

      // delete source file
      fileIn.delete();

      return newFile.getAbsolutePath();
   }

   /**
    * Re-imports a tour and specifically the data specified.
    *
    * @param tourValueTypes
    *           A list of tour values to be re-imported
    * @param tourData
    *           The tour to re-import
    * @param reimportedFile
    *           Indicates whether the tour to re-import is part of a multi-activity tour
    * @param skipToursWithFileNotFound
    *           Indicates whether to re-import or not a tour for which the file is not found
    * @param reImportStatus
    * @return
    *         True if the tour has been re-imported, false otherwise
    */
   public void reimportTour(final List<TourValueType> tourValueTypes,
                            final TourData tourData,
                            final File[] reimportedFile,
                            final boolean skipToursWithFileNotFound,
                            final ReImportStatus reImportStatus) {

      boolean isReImported = false;

      if (tourData.isManualTour()) {

         /**
          * Manually created tours cannot be re-imported, there is no import file path
          * <p>
          * It took a very long time (years) until this case was discovered
          */

         TourLogManager.addSubLog(TourLogState.INFO,
               NLS.bind(
                     LOG_REIMPORT_MANUAL_TOUR,
                     tourData.getTourStartTime().format(TimeTools.Formatter_DateTime_S)));

         reImportStatus.isReImported = isReImported;

         return;
      }

      boolean isTourReImportedFromSameFile = false;

      final File currentTourImportFile = actionReimportTour_20_GetImportFile(tourData, skipToursWithFileNotFound, reImportStatus);

      if (reimportedFile[0] != null && reimportedFile[0].equals(currentTourImportFile)
            && _newlyImportedTours.size() > 0) {

         // this case occurs when a file contains multiple tours

         if (actionReimportTour_30(tourValueTypes, reimportedFile[0], tourData)) {
            isReImported = true;
            isTourReImportedFromSameFile = true;
         }
      }

      if (isTourReImportedFromSameFile == false) {

         reimportedFile[0] = currentTourImportFile;

         if (reimportedFile[0] == null) {

            /*
             * User canceled file dialog -> continue with next file, it is possible that a
             * tour file could not be reselected because it is not available any more
             */
            String reason;
            if (reImportStatus.isCanceled_Auto_ImportFilePathIsEmpty) {
               reason = Messages.Log_Reimport_Tour_Skipped_FilePathIsEmpty;

            } else if (reImportStatus.isCanceled_Auto_TheFileLocationDialog) {
               reason = Messages.Log_Reimport_Tour_Skipped_FileLocationDialog_Auto;

            } else if (reImportStatus.isCanceled_ByUser_TheFileLocationDialog) {
               reason = Messages.Log_Reimport_Tour_Skipped_FileLocationDialog_ByUser;

            } else {
               reason = Messages.Log_Reimport_Tour_Skipped_OtherReasons;
            }

            TourLogManager.addSubLog(TourLogState.IMPORT_ERROR,
                  NLS.bind(LOG_REIMPORT_TOUR_SKIPPED,
                        tourData.getTourStartTime().format(TimeTools.Formatter_DateTime_S),
                        reason));

            reImportStatus.isReImported = isReImported;

            return;
         }

         // import file is available

         if (actionReimportTour_30(tourValueTypes, reimportedFile[0], tourData)) {
            isReImported = true;
         }
      }

      reImportStatus.isReImported = isReImported;
   }

   public void removeAllTours() {

      _toursInImportView.clear();

      _importedFileNames.clear();
      _importedFileNamesChildren.clear();

      _tempTourTags.clear();
      _tempTourTypes.clear();
   }

   public void removeTours(final TourData[] removedTours) {

      final HashSet<?> oldFileNames = (HashSet<?>) _importedFileNames.clone();

      for (final TourData tourData : removedTours) {

         final Long key = tourData.getTourId();

         if (_toursInImportView.containsKey(key)) {
            _toursInImportView.remove(key);
         }
      }

      /*
       * Check if all tours from a file are removed, when yes, remove file path that the file can
       * not be re-imported. When at least one tour is still used, all tours will be re-imported
       * because it's not yet saved which tours are removed from a file and which are not.
       */
      for (final Object item : oldFileNames) {
         if (item instanceof String) {

            final String oldFilePath = (String) item;
            boolean isNeeded = false;

            for (final TourData tourData : _toursInImportView.values()) {

               final String tourFilePathName = tourData.getImportFilePathName();

               if (tourFilePathName != null && tourFilePathName.equals(oldFilePath)) {
                  isNeeded = true;
                  break;
               }
            }

            if (isNeeded == false) {

               // file path is not needed any more
               _importedFileNames.remove(oldFilePath);
            }
         }
      }
   }

   public ImportRunState runImport(final ArrayList<OSFile> importFiles,
                                   final boolean isEasyImport,
                                   final String fileGlobPattern) {

      final ImportRunState importRunState = new ImportRunState();

      if (importFiles.isEmpty()) {
         return importRunState;
      }

      final long start = System.currentTimeMillis();

      /*
       * Log import
       */
      final String css = isEasyImport //
            ? UI.EMPTY_STRING
            : TourLogView.CSS_LOG_TITLE;

      final String message = isEasyImport //
            ? String.format(EasyImportManager.LOG_EASY_IMPORT_002_TOUR_FILES_START, fileGlobPattern)
            : RawDataManager.LOG_IMPORT_TOUR;

      TourLogManager.addLog(TourLogState.DEFAULT, message, css);

      // check if devices are loaded
      if (_devicesByExtension == null) {
         getDeviceListSortedByPriority();
      }

      final List<ImportFile> importFilePaths = new ArrayList<>();

      /*
       * Convert to IPath because NIO Path DO NOT SUPPORT EXTENSIONS :-(((
       */
      for (final OSFile osFile : importFiles) {

         final String absolutePath = osFile.getPath().toString();
         final org.eclipse.core.runtime.Path iPath = new org.eclipse.core.runtime.Path(absolutePath);

         final ImportFile importFile = new ImportFile(iPath);
         importFile.isBackupImportFile = osFile.isBackupImportFile;

         importFilePaths.add(importFile);
      }

      // resort files by extension priority
      Collections.sort(importFilePaths, (importFilePath1, importFilePath2) -> {

         final String file1Extension = importFilePath1.filePath.getFileExtension();
         final String file2Extension = importFilePath2.filePath.getFileExtension();

         if (file1Extension != null
               && file1Extension.length() > 0
               && file2Extension != null
               && file2Extension.length() > 0) {

            final TourbookDevice file1Device = _devicesByExtension.get(file1Extension.toLowerCase());
            final TourbookDevice file2Device = _devicesByExtension.get(file2Extension.toLowerCase());

            if (file1Device != null && file2Device != null) {
               return file1Device.extensionSortPriority - file2Device.extensionSortPriority;
            }
         }

         // sort invalid files to the end
         return Integer.MAX_VALUE;
      });

      setImportCanceled(false);

      final IRunnableWithProgress importRunnable = new IRunnableWithProgress() {

         @Override
         public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            int imported = 0;
            final int importSize = importFilePaths.size();

            monitor.beginTask(Messages.import_data_importTours_task, importSize);

            setImportId();

            int importCounter = 0;

            // loop: import all selected files
            for (final ImportFile filePath : importFilePaths) {

               if (monitor.isCanceled()) {

                  // stop importing but process imported tours

                  importRunState.isImportCanceled = true;

                  break;
               }

               final String osFilePath = filePath.filePath.toOSString();

               final String subTask = NLS.bind(
                     Messages.import_data_importTours_subTask, //
                     new Object[] { ++imported, importSize, osFilePath });

               monitor.worked(1);
               monitor.subTask(subTask);

               // ignore files which are imported as children from other imported files
               if (_importedFileNamesChildren.contains(osFilePath)) {
                  continue;
               }

               File importFile = new File(osFilePath);

               if (FileSystemManager.isFileFromTourBookFileSystem(osFilePath)) {
                  importFile = FileSystemManager.CopyLocally(osFilePath);

               }

               if (importRawData(importFile, null, false, null, true, false)) {

                  importCounter++;

                  // update state
                  for (final TourData importedTourData : _newlyImportedTours.values()) {

                     importedTourData.isBackupImportFile = filePath.isBackupImportFile;

                     TourLogManager.addSubLog(//
                           TourLogState.IMPORT_OK,
                           NLS.bind(LOG_IMPORT_TOUR_IMPORTED,
                                 importedTourData.getTourStartTime().format(TimeTools.Formatter_DateTime_S),
                                 osFilePath));
                  }

                  TourLogManager.addSubLog(//
                        TourLogState.INFO,
                        NLS.bind(LOG_IMPORT_TOURS_IMPORTED_FROM_FILE, _newlyImportedTours.size(), osFilePath));

               } else {

                  _invalidFilesList.add(osFilePath);

                  TourLogManager.addSubLog(TourLogState.IMPORT_ERROR, osFilePath);
               }

               if (FileSystemManager.isFileFromTourBookFileSystem(osFilePath)) {
                  // Delete the temporary created file
                  FilesUtils.deleteIfExists(importFile.toPath());
               }

            }

            save_InvalidFilesToIgnore_InTxt();

            if (importCounter > 0) {

               updateTourData_InImportView_FromDb(monitor);

               Display.getDefault().syncExec(() -> {

                  final RawDataView view = showRawDataView();

                  if (view != null) {
                     view.reloadViewer();

                     if (isEasyImport == false) {
                        // first tour is selected later
                        view.selectFirstTour();
                     }
                  }
               });
            }
         }
      };

      try {
         new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, importRunnable);
      } catch (final Exception e) {
         TourLogManager.logEx(e);
      }

      final double time = (System.currentTimeMillis() - start) / 1000.0;
      TourLogManager.addLog(
            TourLogState.DEFAULT,
            String.format(
                  isEasyImport
                        ? EasyImportManager.LOG_EASY_IMPORT_002_END
                        : RawDataManager.LOG_IMPORT_TOUR_END,
                  time));

      return importRunState;
   }

   public void setImportCanceled(final boolean importCanceled) {
      _isImportCanceled = importCanceled;
   }

   /**
    * Sets a unique id into the device data so that each import can be identified.
    */
   public void setImportId() {
      _deviceData.importId = System.currentTimeMillis();
   }

   public void setImportYear(final int year) {
      _importState_ImportYear = year;
   }

   public void setIsChecksumValidation(final boolean checked) {
      _importState_IsChecksumValidation = checked;
   }

   public void setMergeTracks(final boolean checked) {
      _importState_IsMergeTracks = checked;
   }

   public void setState_ConvertWayPoints(final boolean isConvertWayPoints) {
      _importState_IsConvertWayPoints = isConvertWayPoints;
   }

   public void setState_CreateTourIdWithTime(final boolean isActionChecked) {
      _importState_IsCreateTourIdWithTime = isActionChecked;
   }

   public void setState_DefaultCadenceMultiplier(final CadenceMultiplier defaultCadenceMultiplier) {
      _importState_DefaultCadenceMultiplier = defaultCadenceMultiplier;
   }

   public void setState_IsIgnoreInvalidFile(final boolean isIgnoreInvalidFile) {
      _importState_IsIgnoreInvalidFile = isIgnoreInvalidFile;
   }

   public void setState_IsOpenImportLogView(final boolean isOpenImportLog) {
      _importState_IsAutoOpenImportLog = isOpenImportLog;
   }

   public void setState_IsSetBodyWeight(final boolean isSetBodyWeight) {
      _importState_IsSetBodyWeight = isSetBodyWeight;
   }

   private RawDataView showRawDataView() {

      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

      try {

         final IViewPart rawDataView = window.getActivePage().findView(RawDataView.ID);

         if (rawDataView == null) {

            // show raw data perspective when raw data view is not visible
            workbench.showPerspective(PerspectiveFactoryRawData.PERSPECTIVE_ID, window);
         }

         // show raw data view
         return (RawDataView) Util.showView(RawDataView.ID, true);

      } catch (final WorkbenchException e) {
         TourLogManager.logEx(e);
      }
      return null;
   }

   /**
    * Update {@link TourData} from the database for all imported tours which are displayed in the
    * import view, a progress dialog is displayed.
    *
    * @param monitor
    */
   public void updateTourData_InImportView_FromDb(final IProgressMonitor monitor) {

      final int numImportTours = _toursInImportView.size();

      if (numImportTours == 0) {

         // nothing to do

      } else if (numImportTours < 3) {

         // don't show progress dialog
         updateTourData_InImportView_FromDb_Runnable(null);

      } else {

         if (monitor == null) {
            try {
               new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(
                     true,
                     false,
                     new IRunnableWithProgress() {

                        @Override
                        public void run(final IProgressMonitor monitor) throws InvocationTargetException,
                              InterruptedException {

                           updateTourData_InImportView_FromDb_Runnable(monitor);
                        }
                     });

            } catch (final InvocationTargetException | InterruptedException e) {
               TourLogManager.logEx(e);
            }
         } else {
            updateTourData_InImportView_FromDb_Runnable(monitor);
         }
      }
   }

   private void updateTourData_InImportView_FromDb_Runnable(final IProgressMonitor monitor) {

      int workedDone = 0;
      final int workedAll = _toursInImportView.size();

      if (monitor != null) {
         monitor.beginTask(Messages.import_data_updateDataFromDatabase_task, workedAll);
      }

      long editorTourId = -1;

      final TourDataEditorView tourDataEditor = TourManager.getTourDataEditor();
      if (tourDataEditor != null) {
         final TourData tourData = tourDataEditor.getTourData();
         if (tourData != null) {
            editorTourId = tourData.getTourId();
         }
      }

      for (final TourData importedTourData : _toursInImportView.values()) {

         if (monitor != null) {
            monitor.worked(1);
            monitor.subTask(NLS.bind(Messages.import_data_updateDataFromDatabase_subTask, workedDone++, workedAll));
         }

         if (importedTourData.isTourDeleted) {
            continue;
         }

         final Long tourId = importedTourData.getTourId();

         try {

            final TourData dbTourData = TourManager.getInstance().getTourDataFromDb(tourId);
            if (dbTourData != null) {

               /*
                * Imported tour is saved in the database, set transient fields.
                */

               // used to delete the device import file
               dbTourData.isTourFileDeleted = importedTourData.isTourFileDeleted;
               dbTourData.isTourFileMoved = importedTourData.isTourFileMoved;
               dbTourData.isBackupImportFile = importedTourData.isBackupImportFile;
               dbTourData.importFilePathOriginal = importedTourData.importFilePathOriginal;

               final Long dbTourId = dbTourData.getTourId();

               // replace existing tours but do not add new tours
               if (_toursInImportView.containsKey(dbTourId)) {

                  /*
                   * check if the tour editor contains this tour, this should not be necessary, just
                   * make sure the correct tour is used !!!
                   */
                  if (editorTourId == dbTourId) {
                     _toursInImportView.put(dbTourId, tourDataEditor.getTourData());
                  } else {
                     _toursInImportView.put(dbTourId, dbTourData);
                  }
               }
            }
         } catch (final Exception e) {
            TourLogManager.logEx(e);
         }
      }

      // prevent async error
      Display.getDefault().syncExec(() -> TourManager.fireEvent(TourEventId.CLEAR_DISPLAYED_TOUR, null, null));
   }

   /**
    * Updates the model with modified tours
    *
    * @param modifiedTours
    */
   public void updateTourDataModel(final ArrayList<TourData> modifiedTours) {

      for (final TourData tourData : modifiedTours) {
         if (tourData != null) {

            final Long tourId = tourData.getTourId();

            // replace existing tour do not add new tours
            if (_toursInImportView.containsKey(tourId)) {
               _toursInImportView.put(tourId, tourData);
            }
         }
      }
   }

}
