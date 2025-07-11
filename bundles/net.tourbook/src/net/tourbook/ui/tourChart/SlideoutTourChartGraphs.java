/*******************************************************************************
 * Copyright (C) 2005, 2023 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.tourChart;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.action.ActionOpenPrefDialog;
import net.tourbook.common.action.ActionResetToDefaults;
import net.tourbook.common.action.IActionResetToDefault;
import net.tourbook.common.font.MTFont;
import net.tourbook.common.tooltip.ToolbarSlideout;
import net.tourbook.common.util.StringToArrayConverter;
import net.tourbook.common.util.Util;
import net.tourbook.data.CustomTrackDefinition;
import net.tourbook.data.TourData;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.preferences.PrefPageAppearanceTourChart;
import net.tourbook.tour.TourManager;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Slideout to select visible graphs
 */
public class SlideoutTourChartGraphs extends ToolbarSlideout implements IActionResetToDefault {

   private static final String  GRAPH_LABEL_ALTITUDE                   = net.tourbook.common.Messages.Graph_Label_Altitude;
   public static final String   GRAPH_LABEL_HEARTBEAT                  = net.tourbook.common.Messages.Graph_Label_Heartbeat;
   public static final String   GRAPH_LABEL_SPEED                      = net.tourbook.common.Messages.Graph_Label_Speed;
   public static final String   GRAPH_LABEL_SPEED_INTERVAL             = net.tourbook.common.Messages.Graph_Label_Speed_Interval;
   public static final String   GRAPH_LABEL_SPEED_SUMMARIZED           = net.tourbook.common.Messages.Graph_Label_Speed_Summarized;
   public static final String   GRAPH_LABEL_PACE                       = net.tourbook.common.Messages.Graph_Label_Pace;
   public static final String   GRAPH_LABEL_PACE_INTERVAL              = net.tourbook.common.Messages.Graph_Label_Pace_Interval;
   public static final String   GRAPH_LABEL_PACE_SUMMARIZED            = net.tourbook.common.Messages.Graph_Label_Pace_Summarized;
   public static final String   GRAPH_LABEL_POWER                      = net.tourbook.common.Messages.Graph_Label_Power;
   public static final String   GRAPH_LABEL_TEMPERATURE                = net.tourbook.common.Messages.Graph_Label_Temperature;
   public static final String   GRAPH_LABEL_GRADIENT                   = net.tourbook.common.Messages.Graph_Label_Gradient;
   public static final String   GRAPH_LABEL_ALTIMETER                  = net.tourbook.common.Messages.Graph_Label_Altimeter;
   public static final String   GRAPH_LABEL_CADENCE                    = net.tourbook.common.Messages.Graph_Label_Cadence;
   public static final String   GRAPH_LABEL_GEARS                      = net.tourbook.common.Messages.Graph_Label_Gears;
   public static final String   GRAPH_LABEL_RUNDYN_STANCETIME          = net.tourbook.common.Messages.Graph_Label_RunDyn_StanceTime;
   public static final String   GRAPH_LABEL_RUNDYN_STANCETIMEBALANCE   = net.tourbook.common.Messages.Graph_Label_RunDyn_StanceTimeBalance;
   public static final String   GRAPH_LABEL_RUNDYN_STEPLENGTH          = net.tourbook.common.Messages.Graph_Label_RunDyn_StepLength;
   public static final String   GRAPH_LABEL_RUNDYN_VERTICALOSCILLATION = net.tourbook.common.Messages.Graph_Label_RunDyn_VerticalOscillation;
   public static final String   GRAPH_LABEL_RUNDYN_VERTICALRATIO       = net.tourbook.common.Messages.Graph_Label_RunDyn_VerticalRatio;
   public static final String   GRAPH_LABEL_SWIM_STROKES               = net.tourbook.common.Messages.Graph_Label_Swim_Strokes;
   public static final String   GRAPH_LABEL_SWIM_SWOLF                 = net.tourbook.common.Messages.Graph_Label_Swim_Swolf;

   private static final int       GRID_TOOLBAR_SLIDEOUT_NB_COLUMN        = 22;

   private final IPreferenceStore _prefStore = TourbookPlugin.getPrefStore();
   private IDialogSettings        _state;

   private ActionOpenPrefDialog   _actionPrefDialog;
   private ActionResetToDefaults  _actionRestoreDefaults;

   private SelectionListener      _defaultSelectionListener;

   /*
    * UI controls
    */
   private TourChart _tourChart;

   private Button    _chkShowInChartToolbar_Altimeter;
   private Button    _chkShowInChartToolbar_Altimeter_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Cadence;
   private Button    _chkShowInChartToolbar_Cadence_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Elevation;
   private Button    _chkShowInChartToolbar_Elevation_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Gears;
   private Button    _chkShowInChartToolbar_Gears_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Gradient;
   private Button    _chkShowInChartToolbar_Gradient_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Pace;
   private Button    _chkShowInChartToolbar_Pace_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Pace_Interval;
   private Button    _chkShowInChartToolbar_Pace_Interval_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Pace_Summarized;
   private Button    _chkShowInChartToolbar_Pace_Summarized_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Power;
   private Button    _chkShowInChartToolbar_Power_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Pulse;
   private Button    _chkShowInChartToolbar_Pulse_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Temperature;
   private Button    _chkShowInChartToolbar_Temperature_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Speed;
   private Button    _chkShowInChartToolbar_Speed_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Speed_Interval;
   private Button    _chkShowInChartToolbar_Speed_Interval_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Speed_Summarized;
   private Button    _chkShowInChartToolbar_Speed_Summarized_DefaultWhenOpened;

   private Button    _chkShowInChartToolbar_RunDyn_StanceTime;
   private Button    _chkShowInChartToolbar_RunDyn_StanceTime_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_RunDyn_StanceTimeBalance;
   private Button    _chkShowInChartToolbar_RunDyn_StanceTimeBalance_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_RunDyn_StepLength;
   private Button    _chkShowInChartToolbar_RunDyn_StepLength_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_RunDyn_VerticalOscillation;
   private Button    _chkShowInChartToolbar_RunDyn_VerticalOscillation_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_RunDyn_VerticalRatio;
   private Button    _chkShowInChartToolbar_RunDyn_VerticalRatio_DefaultWhenOpened;

   private Button    _chkShowInChartToolbar_Swim_Strokes;
   private Button    _chkShowInChartToolbar_Swim_Strokes_DefaultWhenOpened;
   private Button    _chkShowInChartToolbar_Swim_Swolf;
   private Button    _chkShowInChartToolbar_Swim_Swolf_DefaultWhenOpened;

   private HashMap<String, Button> _chkShowInChartToolbar_Custom_Tracks = new HashMap<>();
   private Composite               _container;
   private Composite               _containerLevel1;

   public SlideoutTourChartGraphs(final Control ownerControl,
                                  final ToolBar toolBar,
                                  final TourChart tourChart,
                                  final IDialogSettings state) {

      super(ownerControl, toolBar);

      _tourChart = tourChart;
      _state = state;
   }

   private void createActions() {

      _actionRestoreDefaults = new ActionResetToDefaults(this);

      _actionPrefDialog = new ActionOpenPrefDialog(
            Messages.Tour_Action_EditChartPreferences,
            PrefPageAppearanceTourChart.ID);

      _actionPrefDialog.closeThisTooltip(this);
      _actionPrefDialog.setShell(_tourChart.getShell());
   }

   @Override
   protected Composite createToolTipContentArea(final Composite parent) {

      initUI();

      createActions();

      final Composite ui = createUI(parent);

      restoreState();

      return ui;
   }
   private Composite createUI(final Composite parent) {

      final Composite shellContainer = new Composite(parent, SWT.NONE);
      GridLayoutFactory.swtDefaults().applyTo(shellContainer);
      {
         final Composite container = new Composite(shellContainer, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
         GridLayoutFactory
               .fillDefaults()//
               .numColumns(2)
               .applyTo(container);
//       container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
         {
            createUI_10_Title(container);
            createUI_12_Actions(container);
         }
         createUI_20_Graphs(shellContainer);
      }

      return shellContainer;
   }

   private void createUI_10_Title(final Composite parent) {

      /*
       * Label: Slideout title
       */
      final Label label = new Label(parent, SWT.NONE);
      GridDataFactory.fillDefaults().applyTo(label);
      label.setText(Messages.Slideout_TourChartGraph_Label_Title);
      MTFont.setBannerFont(label);
   }

   private void createUI_12_Actions(final Composite parent) {

      final ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
      GridDataFactory
            .fillDefaults()//
            .grab(true, false)
            .align(SWT.END, SWT.BEGINNING)
            .applyTo(toolbar);

      final ToolBarManager tbm = new ToolBarManager(toolbar);

      tbm.add(_actionRestoreDefaults);
      tbm.add(_actionPrefDialog);

      tbm.update(true);
   }

   private void createUI_20_Graphs(final Composite parent) {
      HashMap<String, float[]> customTracks = null;
      final TourData tourData = TourManager.getInstance().getActiveTourChart().getTourData();
      if (tourData != null) {
         customTracks = tourData.getCustomTracks();
      }

      int numCustomTracks = 0;
      final HashMap<String, CustomTrackDefinition> customTrackDefinitions =
            tourData.getCustomTracksDefinition();
      ArrayList<CustomTrackDefinition> listCustomTrackDefinition = null;
      if (customTrackDefinitions != null && customTrackDefinitions.size() > 0 && customTracks != null && customTracks.size() > 0) {
         listCustomTrackDefinition = new ArrayList<>(customTrackDefinitions.values());
         for (final CustomTrackDefinition customTrackDefinition : listCustomTrackDefinition) {
            final String customTrackDefinitionId = customTrackDefinition.getId();
            if (customTracks.get(customTrackDefinitionId) == null || customTracks.get(customTrackDefinitionId).length == 0) {
               continue;
            }
            numCustomTracks++;

         }
         java.util.Collections.sort(listCustomTrackDefinition);
      }

      if (numCustomTracks > 0) {
         /*
          * TOUR contains Custom Tracks
          */

         int numColums = numCustomTracks / GRID_TOOLBAR_SLIDEOUT_NB_COLUMN;
         if ((numCustomTracks % GRID_TOOLBAR_SLIDEOUT_NB_COLUMN) != 0) {
            numColums++;
         }

         _containerLevel1 = new Composite(parent, SWT.NONE);
         _container = new Composite(_containerLevel1, SWT.NONE);

         GridDataFactory.fillDefaults().grab(true, false).applyTo(_container);

         GridLayoutFactory.fillDefaults().numColumns(3).applyTo(_container);

         GridDataFactory.fillDefaults().grab(true, false).applyTo(_containerLevel1);

         GridLayoutFactory.fillDefaults().numColumns(numColums + 1).applyTo(_containerLevel1);

         final Composite[] _containerCustomTracks = new Composite[numColums];
         for (int index = 0; index < numColums; index++) {
            _containerCustomTracks[index] = new Composite(_containerLevel1, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_containerCustomTracks[index]);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(_containerCustomTracks[index]);
         }

         boolean altitudeEnabled = true;
         if (tourData != null && tourData.getAltitudeSmoothedSerie(true) == null) {
            altitudeEnabled = false;
         }
         _chkShowInChartToolbar_Elevation_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_ALTITUDE,
               GRAPH_LABEL_ALTITUDE,
               Images.Graph_Elevation,
               //Images.Graph_Elevation_Disabled,
               altitudeEnabled);
         _chkShowInChartToolbar_Elevation = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_ALTITUDE);

         boolean heartrateEnabled = true;
         if (tourData != null && tourData.getPulse_SmoothedSerie() == null) {
            heartrateEnabled = false;
         }
         _chkShowInChartToolbar_Pulse_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_PULSE,
               GRAPH_LABEL_HEARTBEAT,
               Images.Graph_Heartbeat,
               //Images.Graph_Heartbeat_Disabled,
               heartrateEnabled);
         _chkShowInChartToolbar_Pulse = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_HEARTBEAT);

         boolean speedEnabled = true;
         if (tourData != null && tourData.getSpeedSerie() == null) {
            speedEnabled = false;
         }
         _chkShowInChartToolbar_Speed_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_SPEED,
               GRAPH_LABEL_SPEED,
               Images.Graph_Speed,
               //Images.Graph_Speed_Disabled,
               speedEnabled);
         _chkShowInChartToolbar_Speed = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_SPEED);

         boolean speedIntervalEnabled = true;
         if (tourData != null && tourData.getSpeedSerie_Interval() == null) {
            speedIntervalEnabled = false;
         }
         _chkShowInChartToolbar_Speed_Interval_DefaultWhenOpened      = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_SPEED_INTERVAL,
               GRAPH_LABEL_SPEED_INTERVAL,
               Images.Graph_Speed_Interval,
               //Images.Graph_Speed_Interval_Disabled,
               speedIntervalEnabled);
         _chkShowInChartToolbar_Speed_Interval = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_SPEED_INTERVAL);

         boolean speedSummarizedEnabled = true;
         if (tourData != null && tourData.getSpeedSerie_Summarized() == null) {
            speedSummarizedEnabled = false;
         }
         _chkShowInChartToolbar_Speed_Summarized_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_SPEED_SUMMARIZED,
               GRAPH_LABEL_SPEED_SUMMARIZED,
               Images.Graph_Speed_Summarized,
               //Images.Graph_Speed_Summarized_Disabled,
               speedSummarizedEnabled);
         _chkShowInChartToolbar_Speed_Summarized = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_SPEED_SUMMARIZED);

         boolean paceEnabled = true;
         if (tourData != null && tourData.getPaceSerieSeconds() == null) {
            paceEnabled = false;
         }
         _chkShowInChartToolbar_Pace_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_PACE,
               GRAPH_LABEL_PACE,
               Images.Graph_Pace,
               //Images.Graph_Pace_Disabled,
               paceEnabled);
         _chkShowInChartToolbar_Pace = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_PACE);

         boolean paceIntervalEnabled = true;
         if (tourData != null && tourData.getPaceSerie_Interval_Seconds() == null) {
            paceIntervalEnabled = false;
         }
         _chkShowInChartToolbar_Pace_Interval_DefaultWhenOpened      = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_PACE_INTERVAL,
               GRAPH_LABEL_PACE_INTERVAL,
               Images.Graph_Pace_Interval,
               //Images.Graph_Pace_Interval_Disabled,
               paceIntervalEnabled);
         _chkShowInChartToolbar_Pace_Interval = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_PACE_INTERVAL);

         boolean paceSummarizedEnabled = true;
         if (tourData != null && tourData.getPaceSerie_Summarized_Seconds() == null) {
            paceSummarizedEnabled = false;
         }
         _chkShowInChartToolbar_Pace_Summarized_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_PACE_SUMMARIZED,
               GRAPH_LABEL_PACE_SUMMARIZED,
               Images.Graph_Pace_Summarized,
               //Images.Graph_Pace_Summarized_Disabled,
               paceSummarizedEnabled);
         _chkShowInChartToolbar_Pace_Summarized = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_PACE_SUMMARIZED);

         boolean powerEnabled = true;
         if (tourData != null && tourData.getPowerSerie() == null) {
            powerEnabled = false;
         }
         _chkShowInChartToolbar_Power_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_POWER,
               GRAPH_LABEL_POWER,
               Images.Graph_Power,
               //Images.Graph_Power_Disabled,
               powerEnabled);
         _chkShowInChartToolbar_Power = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_POWER);

         boolean temperatureEnabled = true;
         if (tourData != null && tourData.getTemperatureSerie() == null) {
            temperatureEnabled = false;
         }
         _chkShowInChartToolbar_Temperature_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_TEMPERATURE,
               GRAPH_LABEL_TEMPERATURE,
               Images.Graph_Temperature,
               //Images.Graph_Temperature_Disabled,
               temperatureEnabled);
         _chkShowInChartToolbar_Temperature = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_TEMPERATURE);

         boolean gradientEnabled = true;
         if (tourData != null && tourData.gradientSerie == null) {
            gradientEnabled = false;
         }
         _chkShowInChartToolbar_Gradient_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_GRADIENT,
               GRAPH_LABEL_GRADIENT,
               Images.Graph_Gradient,
               //Images.Graph_Gradient_Disabled,
               gradientEnabled);
         _chkShowInChartToolbar_Gradient = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_GRADIENT);

         boolean altimeterEnabled = true;
         if (tourData != null && tourData.getAltimeterSerie() == null) {
            altimeterEnabled = false;
         }
         _chkShowInChartToolbar_Altimeter_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_ALTIMETER,
               GRAPH_LABEL_ALTIMETER,
               Images.Graph_Altimeter,
               //Images.Graph_Altimeter_Disabled,
               altimeterEnabled);
         _chkShowInChartToolbar_Altimeter = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_ALTIMETER);

         boolean cadenceEnabled = true;
         if (tourData != null && tourData.getCadenceSerieWithMuliplier() == null) {
            cadenceEnabled = false;
         }
         _chkShowInChartToolbar_Cadence_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_CADENCE,
               GRAPH_LABEL_CADENCE,
               Images.Graph_Cadence,
               //Images.Graph_Cadence_Disabled,
               cadenceEnabled);
         _chkShowInChartToolbar_Cadence = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_CADENCE);

         boolean gearsEnabled = true;
         if (tourData != null && tourData.getGears() == null) {
            gearsEnabled = false;
         }
         _chkShowInChartToolbar_Gears_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_GEARS,
               GRAPH_LABEL_GEARS,
               Images.Graph_Gears,
               //Images.Graph_Gears_Disabled,
               gearsEnabled);
         _chkShowInChartToolbar_Gears = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_GEARS);

         boolean runDynStanceTimeEnabled = true;
         if (tourData != null && tourData.getRunDyn_StanceTime() == null) {
            runDynStanceTimeEnabled = false;
         }
         _chkShowInChartToolbar_RunDyn_StanceTime_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_RUN_DYN_STANCE_TIME,
               GRAPH_LABEL_RUNDYN_STANCETIME,
               Images.Graph_RunDyn_StanceTime,
               //Images.Graph_RunDyn_StanceTime_Disabled,
               runDynStanceTimeEnabled);
         _chkShowInChartToolbar_RunDyn_StanceTime = createUI_GraphCheckbox_CustomTracks(_container,
               GRAPH_LABEL_RUNDYN_STANCETIME);

         boolean runDynStanceTimeBalanceEnabled = true;
         if (tourData != null && tourData.getRunDyn_StanceTimeBalance() == null) {
            runDynStanceTimeBalanceEnabled = false;
         }
         _chkShowInChartToolbar_RunDyn_StanceTimeBalance_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_RUN_DYN_STANCE_TIME_BALANCED,
               GRAPH_LABEL_RUNDYN_STANCETIMEBALANCE,
               Images.Graph_RunDyn_StanceTimeBalance,
               //Images.Graph_RunDyn_StanceTimeBalance_Disabled,
               runDynStanceTimeBalanceEnabled);
         _chkShowInChartToolbar_RunDyn_StanceTimeBalance = createUI_GraphCheckbox_CustomTracks(_container,
               GRAPH_LABEL_RUNDYN_STANCETIMEBALANCE);

         boolean runDynStepLengthEnabled = true;
         if (tourData != null && tourData.getRunDyn_StepLength() == null) {
            runDynStepLengthEnabled = false;
         }
         _chkShowInChartToolbar_RunDyn_StepLength_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_RUN_DYN_STEP_LENGTH,
               GRAPH_LABEL_RUNDYN_STEPLENGTH,
               Images.Graph_RunDyn_StepLength,
               //Images.Graph_RunDyn_StepLength_Disabled,
               runDynStepLengthEnabled);
         _chkShowInChartToolbar_RunDyn_StepLength = createUI_GraphCheckbox_CustomTracks(_container,
               GRAPH_LABEL_RUNDYN_STEPLENGTH);

         boolean runDynVerticalOscEnabled = true;
         if (tourData != null && tourData.getRunDyn_VerticalOscillation() == null) {
            runDynVerticalOscEnabled = false;
         }
         _chkShowInChartToolbar_RunDyn_VerticalOscillation_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_RUN_DYN_VERTICAL_OSCILLATION,
               GRAPH_LABEL_RUNDYN_VERTICALOSCILLATION,
               Images.Graph_RunDyn_VerticalOscillation,
               //Images.Graph_RunDyn_VerticalOscillation_Disabled,
               runDynVerticalOscEnabled);
         _chkShowInChartToolbar_RunDyn_VerticalOscillation = createUI_GraphCheckbox_CustomTracks(_container,
               GRAPH_LABEL_RUNDYN_VERTICALOSCILLATION);

         boolean runDynVerticalRatioEnabled = true;
         if (tourData != null && tourData.getRunDyn_VerticalRatio() == null) {
            runDynVerticalRatioEnabled = false;
         }
         _chkShowInChartToolbar_RunDyn_VerticalRatio_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_RUN_DYN_VERTICAL_RATIO,
               GRAPH_LABEL_RUNDYN_VERTICALRATIO,
               Images.Graph_RunDyn_VerticalRatio,
               //Images.Graph_RunDyn_VerticalRatio_Disabled,
               runDynVerticalRatioEnabled);
         _chkShowInChartToolbar_RunDyn_VerticalRatio = createUI_GraphCheckbox_CustomTracks(_container,
               GRAPH_LABEL_RUNDYN_VERTICALRATIO);

         boolean swimStrokesEnabled = true;
         if (tourData != null && tourData.getSwim_Strokes() == null) {
            swimStrokesEnabled = false;
         }
         _chkShowInChartToolbar_Swim_Strokes_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_SWIM_STROKES,
               GRAPH_LABEL_SWIM_STROKES,
               Images.Graph_Swim_Strokes,
               //Images.Graph_Swim_Strokes_Disabled,
               swimStrokesEnabled);
         _chkShowInChartToolbar_Swim_Strokes = createUI_GraphCheckbox_CustomTracks(_container,
               GRAPH_LABEL_SWIM_STROKES);

         boolean swimSwolfEnabled = true;
         if (tourData != null && tourData.getSwim_Swolf() == null) {
            swimSwolfEnabled = false;
         }
         _chkShowInChartToolbar_Swim_Swolf_DefaultWhenOpened = createUI_DefaulWhenOpened_CustomTracks(_container);
         createUI_GraphAction_CustomTracks_StandardTrackswState(_container,
               TourManager.GRAPH_SWIM_SWOLF,
               GRAPH_LABEL_SWIM_SWOLF,
               Images.Graph_Swim_Swolf,
               //Images.Graph_Swim_Swolf_Disabled,
               swimSwolfEnabled);
         _chkShowInChartToolbar_Swim_Swolf = createUI_GraphCheckbox_CustomTracks(_container, GRAPH_LABEL_SWIM_SWOLF);

         {
            for (final Map.Entry<String, Button> mapButtonEntry : _chkShowInChartToolbar_Custom_Tracks.entrySet()) {
               if (mapButtonEntry.getValue() != null && !mapButtonEntry.getValue().isDisposed()) {
                  mapButtonEntry.getValue().dispose();
               }
            }
            _chkShowInChartToolbar_Custom_Tracks.clear();

            int numDisplayCustomTracks = 0;

            for (int indexAlphabetical = 0; indexAlphabetical < listCustomTrackDefinition.size(); indexAlphabetical++) {
               if (TourManager.MAX_VISIBLE_CUSTOM_TRACKS_DEBUG) {
                  if (numDisplayCustomTracks >= TourManager.MAX_VISIBLE_CUSTOM_TRACKS) {
                     break;
                  }
               }
               final String customTrackDefinitionId = listCustomTrackDefinition.get(indexAlphabetical).getId();
               if (customTracks.get(customTrackDefinitionId) == null || customTracks.get(customTrackDefinitionId).length == 0) {
                  continue;
               }
               final Composite _containerCurrent = _containerCustomTracks[numDisplayCustomTracks / GRID_TOOLBAR_SLIDEOUT_NB_COLUMN];
               final CustomTrackDefinition customTracksDefinition = listCustomTrackDefinition.get(indexAlphabetical);
               final String toolTip = customTracksDefinition.getName();

               createUI_GraphAction_CustomTracks_wText(_containerCurrent, TourManager.GRAPH_CUSTOM_TRACKS + indexAlphabetical, toolTip);

               final Button chkShowInChartToolbar_Custom_Track = createUI_GraphCheckbox_CustomTracks(_containerCurrent, toolTip);
               _chkShowInChartToolbar_Custom_Tracks.put(customTrackDefinitionId, chkShowInChartToolbar_Custom_Track);

               numDisplayCustomTracks++;
            }

         }

      } else {
         /*
          * Tour doesn't contain Custom Tracks
          */
         final Composite container = new Composite(parent, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
         GridLayoutFactory.fillDefaults().numColumns(GRID_TOOLBAR_SLIDEOUT_NB_COLUMN).applyTo(container);
//    container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
         {
            {
               // SET_FORMATTING_OFF
               /*
                * Checkbox: Selected graphs will be displayed when a tour is opened
                */
               final Label label = new Label(container, SWT.NONE);
               label.setText(Messages.Slideout_TourChartGraph_Label_DefaultWhenOpened);

               _chkShowInChartToolbar_Elevation_DefaultWhenOpened          = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Pulse_DefaultWhenOpened              = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Speed_DefaultWhenOpened              = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Speed_Interval_DefaultWhenOpened     = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Speed_Summarized_DefaultWhenOpened   = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Pace_DefaultWhenOpened               = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Pace_Interval_DefaultWhenOpened      = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Pace_Summarized_DefaultWhenOpened    = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Power_DefaultWhenOpened              = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Temperature_DefaultWhenOpened       = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Gradient_DefaultWhenOpened           = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Altimeter_DefaultWhenOpened          = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Cadence_DefaultWhenOpened            = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Gears_DefaultWhenOpened              = createUI_DefaulWhenOpened(container);

               _chkShowInChartToolbar_RunDyn_StanceTime_DefaultWhenOpened           = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_RunDyn_StanceTimeBalance_DefaultWhenOpened    = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_RunDyn_StepLength_DefaultWhenOpened           = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_RunDyn_VerticalOscillation_DefaultWhenOpened  = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_RunDyn_VerticalRatio_DefaultWhenOpened        = createUI_DefaulWhenOpened(container);

               _chkShowInChartToolbar_Swim_Strokes_DefaultWhenOpened    = createUI_DefaulWhenOpened(container);
               _chkShowInChartToolbar_Swim_Swolf_DefaultWhenOpened      = createUI_DefaulWhenOpened(container);

               // SET_FORMATTING_ON
            }
            {
               /*
                * Actions: chart graphs
                */
               final Label label = new Label(container, SWT.NONE);
               label.setText(Messages.Slideout_TourChartGraph_Label_ShowGraph);

               createUI_GraphAction(container, TourManager.GRAPH_ALTITUDE);
               createUI_GraphAction(container, TourManager.GRAPH_PULSE);
               createUI_GraphAction(container, TourManager.GRAPH_SPEED);
               createUI_GraphAction(container, TourManager.GRAPH_SPEED_INTERVAL);
               createUI_GraphAction(container, TourManager.GRAPH_SPEED_SUMMARIZED);
               createUI_GraphAction(container, TourManager.GRAPH_PACE);
               createUI_GraphAction(container, TourManager.GRAPH_PACE_INTERVAL);
               createUI_GraphAction(container, TourManager.GRAPH_PACE_SUMMARIZED);
               createUI_GraphAction(container, TourManager.GRAPH_POWER);
               createUI_GraphAction(container, TourManager.GRAPH_TEMPERATURE);
               createUI_GraphAction(container, TourManager.GRAPH_GRADIENT);
               createUI_GraphAction(container, TourManager.GRAPH_ALTIMETER);
               createUI_GraphAction(container, TourManager.GRAPH_CADENCE);
               createUI_GraphAction(container, TourManager.GRAPH_GEARS);

               createUI_GraphAction(container, TourManager.GRAPH_RUN_DYN_STANCE_TIME);
               createUI_GraphAction(container, TourManager.GRAPH_RUN_DYN_STANCE_TIME_BALANCED);
               createUI_GraphAction(container, TourManager.GRAPH_RUN_DYN_STEP_LENGTH);
               createUI_GraphAction(container, TourManager.GRAPH_RUN_DYN_VERTICAL_OSCILLATION);
               createUI_GraphAction(container, TourManager.GRAPH_RUN_DYN_VERTICAL_RATIO);

               createUI_GraphAction(container, TourManager.GRAPH_SWIM_STROKES);
               createUI_GraphAction(container, TourManager.GRAPH_SWIM_SWOLF);
            }
            {
               // SET_FORMATTING_OFF
               /*
                * Checkbox: Show/hide action in the chart toolbar
                */
               final Label label = new Label(container, SWT.NONE);
               label.setText(Messages.Slideout_TourChartGraph_Label_ShowActionInToolbar);

               _chkShowInChartToolbar_Elevation          = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Pulse              = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Speed              = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Speed_Interval     = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Speed_Summarized   = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Pace               = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Pace_Interval      = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Pace_Summarized    = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Power              = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Temperature       = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Gradient           = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Altimeter          = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Cadence            = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Gears              = createUI_ShowActionInToolbar(container);

               _chkShowInChartToolbar_RunDyn_StanceTime              = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_RunDyn_StanceTimeBalance       = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_RunDyn_StepLength              = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_RunDyn_VerticalOscillation     = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_RunDyn_VerticalRatio           = createUI_ShowActionInToolbar(container);

               _chkShowInChartToolbar_Swim_Strokes    = createUI_ShowActionInToolbar(container);
               _chkShowInChartToolbar_Swim_Swolf      = createUI_ShowActionInToolbar(container);
            }
            // SET_FORMATTING_ON
         }
      }
   }

   private Button createUI_DefaulWhenOpened(final Composite parent) {

      final Button checkbox = new Button(parent, SWT.CHECK);

      checkbox.setToolTipText(Messages.Slideout_TourChartGraph_Checkbox_DefaultWhenOpened_Tooltip);
      checkbox.addSelectionListener(_defaultSelectionListener);

      GridDataFactory
            .fillDefaults()
            .grab(true, false)
            .align(SWT.CENTER, SWT.FILL)
            .applyTo(checkbox);

      return checkbox;
   }

   private Button createUI_DefaulWhenOpened_CustomTracks(final Composite parent) {

      final Button checkbox = new Button(parent, SWT.CHECK);

      checkbox.setToolTipText(Messages.Slideout_TourChartGraph_Checkbox_DefaultWhenOpened_Tooltip);
      checkbox.addSelectionListener(_defaultSelectionListener);
      checkbox.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));

      GridDataFactory
            .fillDefaults()
            .grab(true, false)
            .align(SWT.CENTER, SWT.FILL)
            .applyTo(checkbox);

      return checkbox;
   }

   private void createUI_GraphAction(final Composite parent, final int graphId) {

      final ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
      final ToolBarManager tbm = new ToolBarManager(toolbar);

      tbm.add(_tourChart.getGraphAction(graphId));

      tbm.update(true);
   }

   private Button createUI_GraphAction_CustomTracks_StandardTrackswState(final Composite parent,
                                                                         final int graphId,
                                                                         final String text,
                                                                         final String image,
                                                                         //final String imageDisabled,
                                                                         final boolean isEnabled) {

      //final Button btngraph = new Button(parent, SWT.PUSH);
      final Button btngraph = new Button(parent, SWT.TOGGLE);//no need of disabled image since it is a toggle button

      btngraph.setEnabled(isEnabled);
      btngraph.setVisible(true);
      //if (isEnabled) {
         btngraph.setImage(TourbookPlugin.getImageDescriptor(image).createImage());
      //} else {
      //btngraph.setImage(TourbookPlugin.getImageDescriptor(imageDisabled).createImage());
      //}
      //btngraph.setText(text);
      btngraph.setToolTipText(Messages.Slideout_TourChartGraph_Checkbox_ShowHideGraph_Tooltip_Custom_Tracks + UI.SYMBOL_COLON + UI.SPACE + text);
      GridDataFactory
            .fillDefaults()
            .grab(true, false)
            .align(SWT.LEFT, SWT.FILL)
            .applyTo(btngraph);

      btngraph.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(final SelectionEvent e) {
            _tourChart.getGraphAction(graphId).run();
         }

      });

      return btngraph;
   }

   private Button createUI_GraphAction_CustomTracks_wText(final Composite parent, final int graphId, final String text) {

      //final Button btngraph = new Button(parent, SWT.PUSH);
      final Button btngraph = new Button(parent, SWT.TOGGLE);

      btngraph.setEnabled(true);
      btngraph.setVisible(true);
      btngraph.setImage(TourbookPlugin.getImageDescriptor(Images.Graph_Custom_Tracks).createImage());
      GridDataFactory
            .fillDefaults()
            .grab(true, false)
            .align(SWT.LEFT, SWT.FILL)
            .applyTo(btngraph);
      btngraph.setToolTipText(Messages.Slideout_TourChartGraph_Checkbox_ShowHideGraph_Tooltip_Custom_Tracks + UI.SYMBOL_COLON + UI.SPACE + text);

      btngraph.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(final SelectionEvent e) {
            _tourChart.getGraphAction(graphId).run();
         }
      });

      return btngraph;
   }

   private Button createUI_GraphCheckbox_CustomTracks(final Composite parent, final String toolTip) {

      final Button checkbox = new Button(parent, SWT.CHECK);

      checkbox.setText(toolTip);
      checkbox.setToolTipText(Messages.Slideout_TourChartGraph_Checkbox_ShowInChartToolbar_Tooltip + UI.SYMBOL_COLON + UI.SPACE + toolTip);
      checkbox.addSelectionListener(_defaultSelectionListener);

      GridDataFactory
            .fillDefaults()
            .grab(true, false)
            .align(SWT.LEFT, SWT.FILL)
            .applyTo(checkbox);

      return checkbox;
   }

   private Button createUI_ShowActionInToolbar(final Composite parent) {

      final Button checkbox = new Button(parent, SWT.CHECK);

      checkbox.setToolTipText(Messages.Slideout_TourChartGraph_Checkbox_ShowInChartToolbar_Tooltip);
      checkbox.addSelectionListener(_defaultSelectionListener);

      GridDataFactory
            .fillDefaults()
            .grab(true, false)
            .align(SWT.CENTER, SWT.FILL)
            .applyTo(checkbox);

      return checkbox;
   }

   private void initUI() {

      _defaultSelectionListener = widgetSelectedAdapter(selectionEvent -> onChangeUI());
   }

   @Override
   protected boolean isCenterHorizontal() {
      return true;
   }

   private void onChangeUI() {

      saveState();

      // update chart toolbar
      _tourChart.updateGraphToolbar();
   }

   @Override
   protected void onDispose() {

   }

   @Override
   public void resetToDefaults() {

// SET_FORMATTING_OFF

		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTITUDE, 								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTITUDE_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTIMETER, 							TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTIMETER_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_CADENCE, 								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_CADENCE_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GEARS, 									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GEARS_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GRADIENT, 								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GRADIENT_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_POWER, 									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_POWER_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PULSE, 									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PULSE_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_TEMPERATURE,							TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_TEMPERATURE_DEFAULT);

		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE, 									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_INTERVAL, 					   TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_INTERVAL_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_SUMMARIZED, 					TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_SUMMARIZED_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED, 									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_INTERVAL, 						TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_INTERVAL);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_SUMMARIZED, 					TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_SUMMARIZED_DEFAULT);

		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME, 				TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_BALANCED, 	TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_BALANCED_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STEP_LENGTH, 				TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STEP_LENGTH_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_OSCILLATION, 	TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_OSCILLATION_DEFAULT);
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_RATIO, 			TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_RATIO_DEFAULT);

      _state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_STROKES,                   TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_STROKES_DEFAULT);
      _state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_SWOLF,                     TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_SWOLF_DEFAULT);

// SET_FORMATTING_ON

      // update UI
      restoreState();

      // update chart toolbar
      _tourChart.updateGraphToolbar();
   }

   private void restoreState() {

// SET_FORMATTING_OFF

		_chkShowInChartToolbar_Elevation.setSelection(							Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTITUDE,								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTITUDE_DEFAULT));
		_chkShowInChartToolbar_Altimeter.setSelection(							Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTIMETER,								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTIMETER_DEFAULT));
		_chkShowInChartToolbar_Cadence.setSelection(								Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_CADENCE,								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_CADENCE_DEFAULT));
		_chkShowInChartToolbar_Gears.setSelection(								Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GEARS,									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GEARS_DEFAULT));
		_chkShowInChartToolbar_Gradient.setSelection(							Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GRADIENT,								TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GRADIENT_DEFAULT));
		_chkShowInChartToolbar_Power.setSelection(								Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_POWER,									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_POWER_DEFAULT));
		_chkShowInChartToolbar_Pulse.setSelection(								Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PULSE,									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PULSE_DEFAULT));
		_chkShowInChartToolbar_Temperature.setSelection(						Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_TEMPERATURE,							TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_TEMPERATURE_DEFAULT));

		_chkShowInChartToolbar_Pace.setSelection(									Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE,									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_DEFAULT));
		_chkShowInChartToolbar_Pace_Interval.setSelection(					   Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_INTERVAL,						TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_INTERVAL_DEFAULT));
		_chkShowInChartToolbar_Pace_Summarized.setSelection(					Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_SUMMARIZED,						TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_SUMMARIZED_DEFAULT));
		_chkShowInChartToolbar_Speed.setSelection(								Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED,									TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_DEFAULT));
		_chkShowInChartToolbar_Speed_Interval.setSelection(					Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_INTERVAL,						TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_INTERVAL_DEFAULT));
		_chkShowInChartToolbar_Speed_Summarized.setSelection(					Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_SUMMARIZED,					TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_SUMMARIZED_DEFAULT));

		_chkShowInChartToolbar_RunDyn_StanceTime.setSelection(				Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME,				TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_DEFAULT));
		_chkShowInChartToolbar_RunDyn_StanceTimeBalance.setSelection(		Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_BALANCED, 	TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_BALANCED_DEFAULT));
		_chkShowInChartToolbar_RunDyn_StepLength.setSelection(				Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STEP_LENGTH, 				TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STEP_LENGTH_DEFAULT));
		_chkShowInChartToolbar_RunDyn_VerticalOscillation.setSelection(	Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_OSCILLATION, 	TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_OSCILLATION_DEFAULT));
		_chkShowInChartToolbar_RunDyn_VerticalRatio.setSelection(			Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_RATIO, 			TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_RATIO_DEFAULT));

      _chkShowInChartToolbar_Swim_Strokes.setSelection(                 Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_STROKES,                   TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_STROKES_DEFAULT));
      _chkShowInChartToolbar_Swim_Swolf.setSelection(                   Util.getStateBoolean(_state, TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_SWOLF,                     TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_SWOLF_DEFAULT));

// SET_FORMATTING_ON
      final TourData tourData = TourManager.getInstance().getActiveTourChart().getTourData();
      int numDisplayCustomTracks = 0;
      final HashMap<String, CustomTrackDefinition> customTracksDefinitions = tourData.getCustomTracksDefinition();
      final ArrayList<CustomTrackDefinition> listCustomTrackDefinition = new ArrayList<>(customTracksDefinitions.values());
      java.util.Collections.sort(listCustomTrackDefinition);

      final HashMap<String, Boolean> state_CustomTracks = TourManager.getInstance().getActiveTourChart().get_state_CustomTracksToolBarChart();
      for (final CustomTrackDefinition customTrackDefinition : listCustomTrackDefinition) {
         if (TourManager.MAX_VISIBLE_CUSTOM_TRACKS_DEBUG) {
            if (numDisplayCustomTracks >= TourManager.MAX_VISIBLE_CUSTOM_TRACKS) {
               break;
            }
         }
         final Button chkShowInChartToolbar_Custom_Track = _chkShowInChartToolbar_Custom_Tracks.get(customTrackDefinition.getId());

         if (chkShowInChartToolbar_Custom_Track != null && state_CustomTracks != null) {
            final Boolean state = state_CustomTracks.getOrDefault(customTrackDefinition.getId(), false);
            chkShowInChartToolbar_Custom_Track.setSelection(state);
         }

         numDisplayCustomTracks++;
      }

      final String[] allVisibleIds = StringToArrayConverter.convertStringToArray(
            _prefStore.getString(ITourbookPreferences.GRAPH_VISIBLE));

      for (final String graphId : allVisibleIds) {

         final int graphIdValue = Integer.parseInt(graphId);

         if (graphIdValue == TourManager.GRAPH_ALTITUDE) {
            _chkShowInChartToolbar_Elevation_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_ALTIMETER) {
            _chkShowInChartToolbar_Altimeter_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_CADENCE) {
            _chkShowInChartToolbar_Cadence_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_GEARS) {
            _chkShowInChartToolbar_Gears_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_GRADIENT) {
            _chkShowInChartToolbar_Gradient_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_PACE) {
            _chkShowInChartToolbar_Pace_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_PACE_INTERVAL) {
            _chkShowInChartToolbar_Pace_Interval_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_PACE_SUMMARIZED) {
            _chkShowInChartToolbar_Pace_Summarized_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_POWER) {
            _chkShowInChartToolbar_Power_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_PULSE) {
            _chkShowInChartToolbar_Pulse_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_SPEED) {
            _chkShowInChartToolbar_Speed_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_SPEED_INTERVAL) {
            _chkShowInChartToolbar_Speed_Interval_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_SPEED_SUMMARIZED) {
            _chkShowInChartToolbar_Speed_Summarized_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_TEMPERATURE) {
            _chkShowInChartToolbar_Temperature_DefaultWhenOpened.setSelection(true);
         }

         /*
          * Running Dynamics
          */
         if (graphIdValue == TourManager.GRAPH_RUN_DYN_STANCE_TIME) {
            _chkShowInChartToolbar_RunDyn_StanceTime_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_RUN_DYN_STANCE_TIME_BALANCED) {
            _chkShowInChartToolbar_RunDyn_StanceTimeBalance_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_RUN_DYN_STEP_LENGTH) {
            _chkShowInChartToolbar_RunDyn_StepLength_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_RUN_DYN_VERTICAL_OSCILLATION) {
            _chkShowInChartToolbar_RunDyn_VerticalOscillation_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_RUN_DYN_VERTICAL_RATIO) {
            _chkShowInChartToolbar_RunDyn_VerticalRatio_DefaultWhenOpened.setSelection(true);
         }

         /*
          * Swimming
          */
         if (graphIdValue == TourManager.GRAPH_SWIM_STROKES) {
            _chkShowInChartToolbar_Swim_Strokes_DefaultWhenOpened.setSelection(true);
         }

         if (graphIdValue == TourManager.GRAPH_SWIM_SWOLF) {
            _chkShowInChartToolbar_Swim_Swolf_DefaultWhenOpened.setSelection(true);
         }
      }
   }

   private void saveState() {

// SET_FORMATTING_OFF

		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTITUDE, 								 _chkShowInChartToolbar_Elevation.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_ALTIMETER, 							 _chkShowInChartToolbar_Altimeter.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_CADENCE, 								 _chkShowInChartToolbar_Cadence.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GEARS, 									 _chkShowInChartToolbar_Gears.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_GRADIENT, 								 _chkShowInChartToolbar_Gradient.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE, 									 _chkShowInChartToolbar_Pace.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_INTERVAL, 					    _chkShowInChartToolbar_Pace_Interval.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PACE_SUMMARIZED, 					 _chkShowInChartToolbar_Pace_Summarized.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_POWER, 									 _chkShowInChartToolbar_Power.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_PULSE, 									 _chkShowInChartToolbar_Pulse.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED, 									 _chkShowInChartToolbar_Speed.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_INTERVAL, 						 _chkShowInChartToolbar_Speed_Interval.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SPEED_SUMMARIZED, 					 _chkShowInChartToolbar_Speed_Summarized.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_TEMPERATURE,							 _chkShowInChartToolbar_Temperature.getSelection());

		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME, 				 _chkShowInChartToolbar_RunDyn_StanceTime.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STANCE_TIME_BALANCED, 	 _chkShowInChartToolbar_RunDyn_StanceTimeBalance.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_STEP_LENGTH, 				 _chkShowInChartToolbar_RunDyn_StepLength.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_OSCILLATION, 	 _chkShowInChartToolbar_RunDyn_VerticalOscillation.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_RUN_DYN_VERTICAL_RATIO, 			 _chkShowInChartToolbar_RunDyn_VerticalRatio.getSelection());

		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_STROKES,				 			 _chkShowInChartToolbar_Swim_Strokes.getSelection());
		_state.put(TourChart.STATE_IS_SHOW_IN_CHART_TOOLBAR_SWIM_SWOLF,				 			 _chkShowInChartToolbar_Swim_Swolf.getSelection());

// SET_FORMATTING_ON

      final TourData tourData = TourManager.getInstance().getActiveTourChart().getTourData();
      int numDisplayCustomTracks = 0;
      final HashMap<String, CustomTrackDefinition> customTracksDefinitions =
            tourData.getCustomTracksDefinition();
      final ArrayList<CustomTrackDefinition> listCustomTrackDefinition = new ArrayList<>(customTracksDefinitions.values());
      java.util.Collections.sort(listCustomTrackDefinition);

      final LinkedHashMap<String, Boolean> state_CustomTracks = TourManager.getInstance().getActiveTourChart().get_state_CustomTracksToolBarChart();

      for (final CustomTrackDefinition customTrackDefinition : listCustomTrackDefinition) {
         if (TourManager.MAX_VISIBLE_CUSTOM_TRACKS_DEBUG) {
            if (numDisplayCustomTracks >= TourManager.MAX_VISIBLE_CUSTOM_TRACKS) {
               break;
            }
         }
         final Button chkShowInChartToolbar_Custom_Track = _chkShowInChartToolbar_Custom_Tracks.get(customTrackDefinition.getId());
         if (chkShowInChartToolbar_Custom_Track != null && state_CustomTracks != null) {
            state_CustomTracks.put(customTrackDefinition.getId(), chkShowInChartToolbar_Custom_Track.getSelection());
         }

         numDisplayCustomTracks++;
      }

      final ArrayList<String> allDefaultGraphs = new ArrayList<>();

      /*
       * Add all visible graphs in the chart
       */
      if (_chkShowInChartToolbar_Elevation_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_ALTITUDE));
      }

      if (_chkShowInChartToolbar_Altimeter_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_ALTIMETER));
      }

      if (_chkShowInChartToolbar_Cadence_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_CADENCE));
      }

      if (_chkShowInChartToolbar_Gears_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_GEARS));
      }

      if (_chkShowInChartToolbar_Gradient_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_GRADIENT));
      }

      if (_chkShowInChartToolbar_Pace_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_PACE));
      }

      if (_chkShowInChartToolbar_Pace_Interval_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_PACE_INTERVAL));
      }

      if (_chkShowInChartToolbar_Pace_Summarized_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_PACE_SUMMARIZED));
      }

      if (_chkShowInChartToolbar_Power_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_POWER));
      }

      if (_chkShowInChartToolbar_Pulse_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_PULSE));
      }

      if (_chkShowInChartToolbar_Speed_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_SPEED));
      }

      if (_chkShowInChartToolbar_Speed_Interval_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_SPEED_INTERVAL));
      }

      if (_chkShowInChartToolbar_Speed_Summarized_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_SPEED_SUMMARIZED));
      }

      if (_chkShowInChartToolbar_Temperature_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_TEMPERATURE));
      }

      /*
       * Running Dynamics
       */
      if (_chkShowInChartToolbar_RunDyn_StanceTime_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_RUN_DYN_STANCE_TIME));
      }

      if (_chkShowInChartToolbar_RunDyn_StanceTimeBalance_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_RUN_DYN_STANCE_TIME_BALANCED));
      }

      if (_chkShowInChartToolbar_RunDyn_StepLength_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_RUN_DYN_STEP_LENGTH));
      }

      if (_chkShowInChartToolbar_RunDyn_VerticalOscillation_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_RUN_DYN_VERTICAL_OSCILLATION));
      }

      if (_chkShowInChartToolbar_RunDyn_VerticalRatio_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_RUN_DYN_VERTICAL_RATIO));
      }

      /*
       * Swimming
       */

      if (_chkShowInChartToolbar_Swim_Strokes_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_SWIM_STROKES));
      }
      if (_chkShowInChartToolbar_Swim_Swolf_DefaultWhenOpened.getSelection()) {
         allDefaultGraphs.add(Integer.toString(TourManager.GRAPH_SWIM_SWOLF));
      }

      _prefStore.setValue(
            ITourbookPreferences.GRAPH_VISIBLE,
            StringToArrayConverter.convertArrayToString(allDefaultGraphs.toArray(new String[allDefaultGraphs.size()])));
   }
}
