/*******************************************************************************
 * Copyright (C) 2005, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.preferences;

import java.util.ArrayList;
import java.util.HashMap;

import net.tourbook.Messages;
import net.tourbook.OtherMessages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.chart.ChartActivator;
import net.tourbook.chart.MouseWheel2KeyTranslation;
import net.tourbook.chart.MouseWheelMode;
import net.tourbook.chart.preferences.IChartPreferences;
import net.tourbook.common.UI;
import net.tourbook.common.util.StringToArrayConverter;
import net.tourbook.common.util.Util;
import net.tourbook.tour.TourManager;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PrefPageAppearanceTourChart extends PreferencePage implements IWorkbenchPreferencePage {

   public static final String      ID                                        = "net.tourbook.preferences.PrefPageChartGraphs"; //$NON-NLS-1$

   private static final String     STATE_PREF_PAGE_CHART_GRAPHS_SELECTED_TAB = "PrefPage.ChartGraphs.SelectedTab";             //$NON-NLS-1$

   private final IPreferenceStore  _prefStore                                = TourbookPlugin.getPrefStore();
   private final IPreferenceStore  _prefStore_Chart                          = ChartActivator.getPrefStore();

   private HashMap<Integer, Graph> _graphMap;
   private ArrayList<Graph>        _graphList;
   private ArrayList<Graph>        _viewerGraphs;

   private SelectionAdapter        _defaultSelectionListener;

   /*
    * UI controls
    */
   private CTabFolder          _tabFolder;
   private CTabItem            _tab1_Graphs;
   private CTabItem            _tab2_Options;

   private CheckboxTableViewer _graphCheckboxList;

   private Button              _btnDown;
   private Button              _btnUp;

   private Button              _chkLiveUpdate;
   private Button              _chkMoveSlidersWhenZoomed;
   private Button              _chkShowStartTime;
   private Button              _chkZoomToSlider;

   private Button              _rdoMouseKey_UpLeft;
   private Button              _rdoMouseKey_UpRight;
   private Button              _rdoMouseMode_Slider;
   private Button              _rdoMouseMode_Zoom;
   private Button              _rdoShow_Distance;
   private Button              _rdoShow_Time;

   private static class Graph {

      boolean __isChecked = false;

      int     __graphId;

      String  __graphLabel;
      String  __graphLabelPrefix;

      public Graph(final int graphId, final String graphLabel) {

         __graphId = graphId;
         __graphLabel = graphLabel;
      }

      public Graph(final int graphId, final String graphLabel, final String graphLabelPrefix) {

         __graphId = graphId;
         __graphLabel = graphLabel;
         __graphLabelPrefix = graphLabelPrefix;
      }
   }

   @Override
   protected Control createContents(final Composite parent) {

      initUI();

      final Control ui = createUI(parent);

      restoreState();

      validateInput();

      enableActions();
      enableUpDownActions();

      return ui;
   }

   private Composite createUI(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
      {
         _tabFolder = new CTabFolder(container, SWT.NONE);
         _tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         {
            _tab1_Graphs = new CTabItem(_tabFolder, SWT.NONE);
            _tab1_Graphs.setText(Messages.Pref_Graphs_Tab_graph_defaults);
            _tab1_Graphs.setControl(createUI_10_Tab_1_Graphs(_tabFolder));

            _tab2_Options = new CTabItem(_tabFolder, SWT.NONE);
            _tab2_Options.setText(Messages.Pref_Graphs_Tab_zoom_options);
            _tab2_Options.setControl(createUI_50_Tab_2_Options(_tabFolder));
         }

         createUI_99_LiveUpdate(container);
      }

      return container;
   }

   private Control createUI_10_Tab_1_Graphs(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridLayoutFactory.swtDefaults().applyTo(container);
      {
         createUI_12_DefaultGraphs(container);
      }

      return container;
   }

   private void createUI_12_DefaultGraphs(final Composite parent) {

      // group: default graphs
      final Group group = new Group(parent, SWT.NONE);
      group.setText(Messages.Pref_Graphs_Label_select_graph);
      group.setToolTipText(Messages.Pref_Graphs_Label_select_graph_tooltip);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      GridLayoutFactory.swtDefaults().applyTo(group);
      {
         /*
          * label: select info
          */
         final Label label = new Label(group, SWT.WRAP);
         label.setText(Messages.Pref_Graphs_Label_select_graph_tooltip);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

         /*
          * graph container
          */
         final Composite graphContainer = new Composite(group, SWT.NONE);
         GridDataFactory.fillDefaults().indent(0, 10).applyTo(graphContainer);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(graphContainer);
//         graphContainer.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
         {
            createUI_13_GraphCheckBoxList(graphContainer);
            createUI_14_GraphActions(graphContainer);
         }

      }

      createUI_15_GraphOptions(parent);
   }

   private void createUI_13_GraphCheckBoxList(final Composite parent) {

      _graphCheckboxList = CheckboxTableViewer.newCheckList(
            parent,
            SWT.SINGLE | SWT.TOP /* | SWT.BORDER */);
//      _graphCheckboxList.getTable().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

      _graphCheckboxList.setContentProvider(new IStructuredContentProvider() {
         @Override
         public void dispose() {}

         @Override
         public Object[] getElements(final Object inputElement) {
            return _viewerGraphs.toArray();
         }

         @Override
         public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
      });

      _graphCheckboxList.setLabelProvider(new LabelProvider() {
         @Override
         public String getText(final Object element) {

            final Graph graph = (Graph) element;

            return graph.__graphLabelPrefix == null
                  ? graph.__graphLabel
                  : graph.__graphLabelPrefix + UI.SPACE3 + UI.SYMBOL_GREATER_THAN + UI.SPACE3 + graph.__graphLabel;

         }
      });

      _graphCheckboxList.addCheckStateListener(checkStateChangedEvent -> {

         // keep the checked status
         final Graph item = (Graph) checkStateChangedEvent.getElement();
         item.__isChecked = checkStateChangedEvent.getChecked();

         // select the checked item
         _graphCheckboxList.setSelection(new StructuredSelection(item));

         validateInput();
      });

      _graphCheckboxList.addSelectionChangedListener(selectionChangedEvent -> {
         enableUpDownActions();
         doLiveUpdate();
      });

//      final Table table = _graphCheckboxList.getTable();
//      table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
   }

   private void createUI_14_GraphActions(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(container);
      GridLayoutFactory.fillDefaults().applyTo(container);
      {
         /*
          * button: up
          */
         _btnUp = new Button(container, SWT.NONE);
         _btnUp.setText(Messages.Pref_Graphs_Button_up);
         _btnUp.setEnabled(false);
         _btnUp.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {}

            @Override
            public void widgetSelected(final SelectionEvent e) {
               moveSelectionUp();
               enableUpDownActions();
               doLiveUpdate();
            }
         });
         setButtonLayoutData(_btnUp);

         /*
          * button: down
          */
         _btnDown = new Button(container, SWT.NONE);
         _btnDown.setText(Messages.Pref_Graphs_Button_down);
         _btnDown.setEnabled(false);
         _btnDown.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {}

            @Override
            public void widgetSelected(final SelectionEvent e) {
               moveSelectionDown();
               enableUpDownActions();
               doLiveUpdate();
            }
         });
         setButtonLayoutData(_btnDown);
      }
   }

   private void createUI_15_GraphOptions(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
      {

      }
   }

   /**
    * Tab: Options
    *
    * @param parent
    *
    * @return
    */
   private Control createUI_50_Tab_2_Options(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridLayoutFactory.swtDefaults().applyTo(container);
      {
         createUI_52_XAxisUnits(container);
         createUI_54_MouseMode(container);
         createUI_56_ZoomOptions(container);
         createUI_58_MouseKeyTranslation(container);
      }

      return container;
   }

   private void createUI_52_XAxisUnits(final Composite parent) {

      // group: units for the x-axis
      final Group group = new Group(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText(Messages.Pref_Graphs_Group_units_for_xaxis);
      GridLayoutFactory.swtDefaults().applyTo(group);
      {
         /*
          * radio: distance
          */
         _rdoShow_Distance = new Button(group, SWT.RADIO);
         _rdoShow_Distance.setText(Messages.Pref_Graphs_Radio_show_distance);
         _rdoShow_Distance.addSelectionListener(_defaultSelectionListener);

         /*
          * radio: time
          */
         _rdoShow_Time = new Button(group, SWT.RADIO);
         _rdoShow_Time.setText(Messages.Pref_Graphs_Radio_show_time);
         _rdoShow_Time.addSelectionListener(_defaultSelectionListener);

         // checkbox: start time
         _chkShowStartTime = new Button(group, SWT.CHECK);
         _chkShowStartTime.setText(Messages.Pref_Graphs_Check_show_start_time);
         _chkShowStartTime.addSelectionListener(_defaultSelectionListener);
         GridDataFactory.fillDefaults().indent(UI.FORM_FIRST_COLUMN_INDENT, 0).applyTo(_chkShowStartTime);
      }
   }

   private void createUI_54_MouseMode(final Composite parent) {

      final Group group = new Group(parent, SWT.NONE);
      group.setText(Messages.Pref_Graphs_Group_mouse_mode);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      GridLayoutFactory.swtDefaults().applyTo(group);
      {
         // radio: zoom features
         _rdoMouseMode_Zoom = new Button(group, SWT.RADIO);
         _rdoMouseMode_Zoom.setText(Messages.Pref_Graphs_Radio_mouse_mode_zoom);
         _rdoMouseMode_Zoom.addSelectionListener(_defaultSelectionListener);

         // radio: slider features
         _rdoMouseMode_Slider = new Button(group, SWT.RADIO);
         _rdoMouseMode_Slider.setText(Messages.Pref_Graphs_Radio_mouse_mode_slider);
         _rdoMouseMode_Slider.addSelectionListener(_defaultSelectionListener);
      }
   }

   /**
    * Group: zoom options
    */
   private void createUI_56_ZoomOptions(final Composite parent) {

      final Group groupZoomOptions = new Group(parent, SWT.NONE);
      groupZoomOptions.setText(Messages.Pref_Graphs_Group_zoom_options);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(groupZoomOptions);
      GridLayoutFactory.swtDefaults().applyTo(groupZoomOptions);
      {
         /*
          * checkbox: auto zoom to moved slider
          */
         _chkZoomToSlider = new Button(groupZoomOptions, SWT.CHECK);
         _chkZoomToSlider.setText(Messages.Pref_Graphs_Check_autozoom);
         _chkZoomToSlider.addSelectionListener(_defaultSelectionListener);

         /*
          * checkbox: move sliders to border when zoomed
          */
         _chkMoveSlidersWhenZoomed = new Button(groupZoomOptions, SWT.CHECK);
         _chkMoveSlidersWhenZoomed.setText(Messages.Pref_Graphs_move_sliders_when_zoomed);
         _chkMoveSlidersWhenZoomed.addSelectionListener(_defaultSelectionListener);
      }
   }

   private void createUI_58_MouseKeyTranslation(final Composite parent) {

      final Group group = new Group(parent, SWT.NONE);
      group.setText(Messages.Pref_Graphs_Group_MouseKeyTranslation);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      GridLayoutFactory.swtDefaults().applyTo(group);
      {
         // radio: up left
         _rdoMouseKey_UpLeft = new Button(group, SWT.RADIO);
         _rdoMouseKey_UpLeft.setText(Messages.Pref_Graphs_Radio_MouseKey_UpLeft);
         _rdoMouseKey_UpLeft.setToolTipText(Messages.Pref_Graphs_Radio_MouseKey_UpLeft_Tooltip);
         _rdoMouseKey_UpLeft.addSelectionListener(_defaultSelectionListener);

         // radio: up right
         _rdoMouseKey_UpRight = new Button(group, SWT.RADIO);
         _rdoMouseKey_UpRight.setText(Messages.Pref_Graphs_Radio_MouseKey_UpRight);
         _rdoMouseKey_UpRight.setToolTipText(Messages.Pref_Graphs_Radio_MouseKey_UpRight_Tooltip);
         _rdoMouseKey_UpRight.addSelectionListener(_defaultSelectionListener);
      }
   }

   private void createUI_99_LiveUpdate(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
      {
         /*
          * Checkbox: live update
          */
         _chkLiveUpdate = new Button(container, SWT.CHECK);
         _chkLiveUpdate.setText(Messages.Pref_LiveUpdate_Checkbox);
         _chkLiveUpdate.setToolTipText(Messages.Pref_LiveUpdate_Checkbox_Tooltip);
         _chkLiveUpdate.addSelectionListener(_defaultSelectionListener);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(_chkLiveUpdate);
      }
   }

   private void doLiveUpdate() {

      if (_chkLiveUpdate.getSelection()) {
         performApply();
      }
   }

   private void enableActions() {

      _chkShowStartTime.setEnabled(_rdoShow_Time.getSelection());
   }

   private void enableControls() {

   }

   /**
    * check if the up/down button are enabled
    */

   private void enableUpDownActions() {

      final Table table = _graphCheckboxList.getTable();
      final TableItem[] items = table.getSelection();

      final boolean validSelection = items != null && items.length > 0;
      boolean enableUp = validSelection;
      boolean enableDown = validSelection;

      if (validSelection) {
         final int[] indices = table.getSelectionIndices();
         final int max = table.getItemCount();
         enableUp = indices[0] != 0;
         enableDown = indices[indices.length - 1] < max - 1;
      }

      _btnUp.setEnabled(enableUp);
      _btnDown.setEnabled(enableDown);
   }

   private Enum<MouseWheel2KeyTranslation> getPrefMouseKeyTranslation(final boolean isDefault) {

      final String prefMouseKey = isDefault

            ? _prefStore_Chart.getDefaultString(IChartPreferences.GRAPH_MOUSE_KEY_TRANSLATION)
            : _prefStore_Chart.getString(IChartPreferences.GRAPH_MOUSE_KEY_TRANSLATION);

      final Enum<MouseWheel2KeyTranslation> mouseKey = Util.getEnumValue(prefMouseKey, MouseWheel2KeyTranslation.Up_Left);

      return mouseKey;
   }

   /*
    * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
    */
   @Override
   public void init(final IWorkbench workbench) {
      setPreferenceStore(_prefStore);
   }

   private void initUI() {

      _defaultSelectionListener = new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            onSelection();
         }
      };

// SET_FORMATTING_OFF

      // create a map and list with all available graphs
      final Graph graph_Altitude                      = new Graph(TourManager.GRAPH_ALTITUDE,                     OtherMessages.GRAPH_LABEL_ALTITUDE);
      final Graph graph_Speed                         = new Graph(TourManager.GRAPH_SPEED,                        OtherMessages.GRAPH_LABEL_SPEED);
      final Graph graph_Pace                          = new Graph(TourManager.GRAPH_PACE,                         OtherMessages.GRAPH_LABEL_PACE);
      final Graph graph_Power                         = new Graph(TourManager.GRAPH_POWER,                        OtherMessages.GRAPH_LABEL_POWER);
      final Graph graph_Pulse                         = new Graph(TourManager.GRAPH_PULSE,                        OtherMessages.GRAPH_LABEL_HEARTBEAT);
      final Graph graph_Temperature                   = new Graph(TourManager.GRAPH_TEMPERATURE,                  OtherMessages.GRAPH_LABEL_TEMPERATURE);
      final Graph graph_Cadence                       = new Graph(TourManager.GRAPH_CADENCE,                      OtherMessages.GRAPH_LABEL_CADENCE);
      final Graph graph_Gears                         = new Graph(TourManager.GRAPH_GEARS,                        OtherMessages.GRAPH_LABEL_GEAR_RATIO);
      final Graph graph_Altimeter                     = new Graph(TourManager.GRAPH_ALTIMETER,                    OtherMessages.GRAPH_LABEL_ALTIMETER);
      final Graph graph_Gradient                      = new Graph(TourManager.GRAPH_GRADIENT,                     OtherMessages.GRAPH_LABEL_GRADIENT);

      final Graph graph_Radar_PassedVehicles          = new Graph(TourManager.GRAPH_RADAR_PASSED_VEHICLES,        OtherMessages.GRAPH_LABEL_RADAR_PASSED_VEHICLES,            OtherMessages.GRAPH_LABEL_PREFIX_RADAR);
      final Graph graph_Radar_DistanceToVehicle       = new Graph(TourManager.GRAPH_RADAR_DISTANCE_TO_VEHICLE,    OtherMessages.GRAPH_LABEL_RADAR_DISTANCE_TO_VEHICLE,        OtherMessages.GRAPH_LABEL_PREFIX_RADAR);
      final Graph graph_Radar_PassingSpeed_Absolute   = new Graph(TourManager.GRAPH_RADAR_PASSING_SPEED_ABSOLUTE, OtherMessages.GRAPH_LABEL_RADAR_PASSING_SPEED_ABSOLUTE,     OtherMessages.GRAPH_LABEL_PREFIX_RADAR);
      final Graph graph_Radar_PassingSpeed_Relative   = new Graph(TourManager.GRAPH_RADAR_PASSING_SPEED_RELATIVE, OtherMessages.GRAPH_LABEL_RADAR_PASSING_SPEED_RELATIVE,     OtherMessages.GRAPH_LABEL_PREFIX_RADAR);

      final Graph graph_RunDyn_StanceTime             = new Graph(TourManager.GRAPH_RUN_DYN_STANCE_TIME,          OtherMessages.GRAPH_LABEL_RUN_DYN_STANCE_TIME,              OtherMessages.GRAPH_LABEL_PREFIX_RUNNING_DYNAMICS);
      final Graph graph_RunDyn_StanceTimeBalance      = new Graph(TourManager.GRAPH_RUN_DYN_STANCE_TIME_BALANCED, OtherMessages.GRAPH_LABEL_RUN_DYN_STANCE_TIME_BALANCE,      OtherMessages.GRAPH_LABEL_PREFIX_RUNNING_DYNAMICS);
      final Graph graph_RunDyn_StepLength             = new Graph(TourManager.GRAPH_RUN_DYN_STEP_LENGTH,          OtherMessages.GRAPH_LABEL_RUN_DYN_STEP_LENGTH,              OtherMessages.GRAPH_LABEL_PREFIX_RUNNING_DYNAMICS);
      final Graph graph_RunDyn_VerticalOscillation    = new Graph(TourManager.GRAPH_RUN_DYN_VERTICAL_OSCILLATION, OtherMessages.GRAPH_LABEL_RUN_DYN_VERTICAL_OSCILLATION,     OtherMessages.GRAPH_LABEL_PREFIX_RUNNING_DYNAMICS);
      final Graph graph_RunDyn_VerticalRatio          = new Graph(TourManager.GRAPH_RUN_DYN_VERTICAL_RATIO,       OtherMessages.GRAPH_LABEL_RUN_DYN_VERTICAL_RATIO,           OtherMessages.GRAPH_LABEL_PREFIX_RUNNING_DYNAMICS);

      final Graph graph_Swim_Strokes                  = new Graph(TourManager.GRAPH_SWIM_STROKES,                 OtherMessages.GRAPH_LABEL_SWIM_STROKES,                     OtherMessages.GRAPH_LABEL_PREFIX_SWIMMING);
      final Graph graph_Swim_Swolf                    = new Graph(TourManager.GRAPH_SWIM_SWOLF,                   OtherMessages.GRAPH_LABEL_SWIM_SWOLF,                       OtherMessages.GRAPH_LABEL_PREFIX_SWIMMING);

      final Graph graph_Training_Effect_Aerob         = new Graph(TourManager.GRAPH_TRAINING_EFFECT_AEROB,        OtherMessages.GRAPH_LABEL_TRAINING_EFFECT_AEROB,            OtherMessages.GRAPH_LABEL_PREFIX_TRAINING);
      final Graph graph_Training_Effect_Anaerob       = new Graph(TourManager.GRAPH_TRAINING_EFFECT_ANAEROB,      OtherMessages.GRAPH_LABEL_TRAINING_EFFECT_ANAEROB,          OtherMessages.GRAPH_LABEL_PREFIX_TRAINING);
      final Graph graph_Training_Performance          = new Graph(TourManager.GRAPH_TRAINING_PERFORMANCE,         OtherMessages.GRAPH_LABEL_TRAINING_PERFORMANCE,             OtherMessages.GRAPH_LABEL_PREFIX_TRAINING);

      final Graph graph_TourCompareResult             = new Graph(TourManager.GRAPH_TOUR_COMPARE,                 OtherMessages.GRAPH_LABEL_TOUR_COMPARE);
      final Graph graph_TourCompareReferenceTour      = new Graph(TourManager.GRAPH_TOUR_COMPARE_REF_TOUR,        OtherMessages.GRAPH_LABEL_TOUR_COMPARE_REFERENCE_TOUR);

      _graphMap = new HashMap<>();

      _graphMap.put(TourManager.GRAPH_ALTITUDE,                      graph_Altitude);
      _graphMap.put(TourManager.GRAPH_SPEED,                         graph_Speed);
      _graphMap.put(TourManager.GRAPH_PACE,                          graph_Pace);
      _graphMap.put(TourManager.GRAPH_POWER,                         graph_Power);
      _graphMap.put(TourManager.GRAPH_PULSE,                         graph_Pulse);
      _graphMap.put(TourManager.GRAPH_TEMPERATURE,                   graph_Temperature);
      _graphMap.put(TourManager.GRAPH_CADENCE,                       graph_Cadence);
      _graphMap.put(TourManager.GRAPH_GEARS,                         graph_Gears);
      _graphMap.put(TourManager.GRAPH_ALTIMETER,                     graph_Altimeter);
      _graphMap.put(TourManager.GRAPH_GRADIENT,                      graph_Gradient);

      _graphMap.put(TourManager.GRAPH_RADAR_PASSED_VEHICLES,         graph_Radar_PassedVehicles);
      _graphMap.put(TourManager.GRAPH_RADAR_DISTANCE_TO_VEHICLE,     graph_Radar_DistanceToVehicle);
      _graphMap.put(TourManager.GRAPH_RADAR_PASSING_SPEED_ABSOLUTE,  graph_Radar_PassingSpeed_Absolute);
      _graphMap.put(TourManager.GRAPH_RADAR_PASSING_SPEED_RELATIVE,  graph_Radar_PassingSpeed_Relative);

      _graphMap.put(TourManager.GRAPH_RUN_DYN_STANCE_TIME,           graph_RunDyn_StanceTime);
      _graphMap.put(TourManager.GRAPH_RUN_DYN_STANCE_TIME_BALANCED,  graph_RunDyn_StanceTimeBalance);
      _graphMap.put(TourManager.GRAPH_RUN_DYN_STEP_LENGTH,           graph_RunDyn_StepLength);
      _graphMap.put(TourManager.GRAPH_RUN_DYN_VERTICAL_OSCILLATION,  graph_RunDyn_VerticalOscillation);
      _graphMap.put(TourManager.GRAPH_RUN_DYN_VERTICAL_RATIO,        graph_RunDyn_VerticalRatio);

      _graphMap.put(TourManager.GRAPH_SWIM_STROKES,                  graph_Swim_Strokes);
      _graphMap.put(TourManager.GRAPH_SWIM_SWOLF,                    graph_Swim_Swolf);

      _graphMap.put(TourManager.GRAPH_TRAINING_EFFECT_AEROB,         graph_Training_Effect_Aerob);
      _graphMap.put(TourManager.GRAPH_TRAINING_EFFECT_ANAEROB,       graph_Training_Effect_Anaerob);
      _graphMap.put(TourManager.GRAPH_TRAINING_PERFORMANCE,          graph_Training_Performance);

      _graphMap.put(TourManager.GRAPH_TOUR_COMPARE,                  graph_TourCompareResult);
      _graphMap.put(TourManager.GRAPH_TOUR_COMPARE_REF_TOUR,         graph_TourCompareReferenceTour);

// SET_FORMATTING_ON

      _graphList = new ArrayList<>();

      _graphList.add(graph_Altitude);
      _graphList.add(graph_Speed);
      _graphList.add(graph_Pace);
      _graphList.add(graph_Power);
      _graphList.add(graph_Pulse);
      _graphList.add(graph_Temperature);
      _graphList.add(graph_Cadence);
      _graphList.add(graph_Gears);
      _graphList.add(graph_Altimeter);
      _graphList.add(graph_Gradient);

      _graphList.add(graph_Radar_PassedVehicles);
      _graphList.add(graph_Radar_DistanceToVehicle);
      _graphList.add(graph_Radar_PassingSpeed_Absolute);
      _graphList.add(graph_Radar_PassingSpeed_Relative);

      _graphList.add(graph_RunDyn_StanceTime);
      _graphList.add(graph_RunDyn_StanceTimeBalance);
      _graphList.add(graph_RunDyn_StepLength);
      _graphList.add(graph_RunDyn_VerticalOscillation);
      _graphList.add(graph_RunDyn_VerticalRatio);

      _graphList.add(graph_Swim_Strokes);
      _graphList.add(graph_Swim_Swolf);

      _graphList.add(graph_Training_Effect_Aerob);
      _graphList.add(graph_Training_Effect_Anaerob);
      _graphList.add(graph_Training_Performance);

      _graphList.add(graph_TourCompareResult);
      _graphList.add(graph_TourCompareReferenceTour);
   }

   /**
    * Moves an entry in the table to the given index.
    */
   private void move(final TableItem item, final int index) {

      this.setValid(true);
      final Graph graph = (Graph) item.getData();
      item.dispose();

      _graphCheckboxList.insert(graph, index);
      _graphCheckboxList.setChecked(graph, graph.__isChecked);
   }

   /**
    * Move the current selection in the build list down.
    */
   private void moveSelectionDown() {

      final Table table = _graphCheckboxList.getTable();
      final int[] indices = table.getSelectionIndices();
      if (indices.length < 1) {
         return;
      }

      final int[] newSelection = new int[indices.length];
      final int max = table.getItemCount() - 1;

      for (int i = indices.length - 1; i >= 0; i--) {
         final int index = indices[i];
         if (index < max) {
            move(table.getItem(index), index + 1);
            newSelection[i] = index + 1;
         }
      }
      table.setSelection(newSelection);
   }

   /**
    * Move the current selection in the build list up.
    */
   private void moveSelectionUp() {

      final Table table = _graphCheckboxList.getTable();
      final int[] indices = table.getSelectionIndices();
      final int[] newSelection = new int[indices.length];

      for (int i = 0; i < indices.length; i++) {
         final int index = indices[i];
         if (index > 0) {
            move(table.getItem(index), index - 1);
            newSelection[i] = index - 1;
         }
      }
      table.setSelection(newSelection);
   }

   @Override
   public boolean okToLeave() {

      saveState_UI();

      return super.okToLeave();
   }

   private void onSelection() {

      enableActions();
      enableControls();

      doLiveUpdate();
   }

   @Override
   public boolean performCancel() {

      saveState_UI();

      return super.performCancel();
   }

   @Override
   protected void performDefaults() {

      /*
       * perform defaults for the currently selected tab
       */
      final CTabItem selectedTab = _tabFolder.getItem(_tabFolder.getSelectionIndex());

      if (selectedTab == _tab2_Options) {

         if (getPrefMouseKeyTranslation(true).equals(MouseWheel2KeyTranslation.Up_Left)) {

            _rdoMouseKey_UpLeft.setSelection(true);
            _rdoMouseKey_UpRight.setSelection(false);

         } else {

            _rdoMouseKey_UpLeft.setSelection(false);
            _rdoMouseKey_UpRight.setSelection(true);
         }
      }

      // live update
      _chkLiveUpdate.setSelection(_prefStore.getDefaultBoolean(ITourbookPreferences.GRAPH_PREF_PAGE_IS_TOUR_CHART_LIVE_UPDATE));

      super.performDefaults();

      onSelection();
   }

   @Override
   public boolean performOk() {

      saveState();

      return super.performOk();
   }

   private void restoreState() {

      restoreState_Tab_1_Graphs();
      restoreState_Tab_2_Options();

      // selected tab
      _tabFolder.setSelection(_prefStore.getInt(STATE_PREF_PAGE_CHART_GRAPHS_SELECTED_TAB));

      // live update
      _chkLiveUpdate.setSelection(_prefStore.getBoolean(ITourbookPreferences.GRAPH_PREF_PAGE_IS_TOUR_CHART_LIVE_UPDATE));

      enableControls();
   }

   private void restoreState_Tab_1_Graphs() {

      restoreState_Tab_1_Graphs_Graphs();
   }

   private void restoreState_Tab_1_Graphs_Graphs() {

      /*
       * Create a list with all available graphs
       */
      final String[] prefAllGraphIds = StringToArrayConverter.convertStringToArray(_prefStore.getString(ITourbookPreferences.GRAPH_ALL));

      _viewerGraphs = new ArrayList<>();

      // put all graphs in the viewer which are defined in the prefs
      for (final String graphIdKey : prefAllGraphIds) {

         final int graphId = Integer.parseInt(graphIdKey);

         if (_graphMap.containsKey(graphId)) {
            _viewerGraphs.add(_graphMap.get(graphId));
         }
      }

      // make sure that all available graphs are in the viewer
      for (final Graph graph : _graphList) {
         if (!_viewerGraphs.contains(graph)) {
            _viewerGraphs.add(graph);
         }
      }

      _graphCheckboxList.setInput(this);

      final String[] prefVisibleIds = StringToArrayConverter.convertStringToArray(
            _prefStore.getString(ITourbookPreferences.GRAPH_VISIBLE));

      // check all graphs which are defined in the prefs
      final ArrayList<Graph> checkedGraphs = new ArrayList<>();
      for (final Graph graph : _viewerGraphs) {
         final int graphId = graph.__graphId;
         for (final String prefId : prefVisibleIds) {
            if (graphId == Integer.parseInt(prefId)) {
               graph.__isChecked = true;
               checkedGraphs.add(graph);
            }
         }
      }

      _graphCheckboxList.setCheckedElements(checkedGraphs.toArray());
   }

   private void restoreState_Tab_2_Options() {

      /*
       * X-Axis units
       */
      if (_prefStore.getString(ITourbookPreferences.GRAPH_X_AXIS).equals(TourManager.X_AXIS_TIME)) {
         _rdoShow_Time.setSelection(true);
      } else {
         _rdoShow_Distance.setSelection(true);
      }

      _chkShowStartTime.setSelection(_prefStore.getBoolean(ITourbookPreferences.GRAPH_X_AXIS_STARTTIME));

      /*
       * Mouse wheel mode
       */
      _chkZoomToSlider.setSelection(_prefStore.getBoolean(ITourbookPreferences.GRAPH_ZOOM_AUTO_ZOOM_TO_SLIDER));
      _chkMoveSlidersWhenZoomed.setSelection(_prefStore.getBoolean(ITourbookPreferences.GRAPH_MOVE_SLIDERS_WHEN_ZOOMED));

      // zoom options
      final String prefMouseWheelMode = _prefStore.getString(ITourbookPreferences.GRAPH_MOUSE_MODE);
      final Enum<MouseWheelMode> mouseWheelMode = Util.getEnumValue(prefMouseWheelMode, MouseWheelMode.Zoom);

      if (mouseWheelMode.equals(MouseWheelMode.Selection)) {
         _rdoMouseMode_Slider.setSelection(true);
      } else {
         _rdoMouseMode_Zoom.setSelection(true);
      }

      // mouse key
      if (getPrefMouseKeyTranslation(false).equals(MouseWheel2KeyTranslation.Up_Left)) {

         _rdoMouseKey_UpLeft.setSelection(true);
         _rdoMouseKey_UpRight.setSelection(false);

      } else {

         _rdoMouseKey_UpLeft.setSelection(false);
         _rdoMouseKey_UpRight.setSelection(true);
      }
   }

   private void saveState() {

      saveState_Tab_1_Graphs();
      saveState_Tab_2_Options();

      // live update
      _prefStore.setValue(ITourbookPreferences.GRAPH_PREF_PAGE_IS_TOUR_CHART_LIVE_UPDATE, _chkLiveUpdate.getSelection());
   }

   private void saveState_Tab_1_Graphs() {

      saveState_Tab_1_Graphs_Graphs();
   }

   /**
    * get the graph id's from the preferences and check the graphs in the list
    */
   private void saveState_Tab_1_Graphs_Graphs() {

      // convert the array with the graph objects into a string which is store
      // in the prefs
      final Object[] graphs = _graphCheckboxList.getCheckedElements();
      final String[] prefGraphsChecked = new String[graphs.length];
      for (int graphIndex = 0; graphIndex < graphs.length; graphIndex++) {
         final Graph graph = (Graph) graphs[graphIndex];
         prefGraphsChecked[graphIndex] = Integer.toString(graph.__graphId);
      }
      _prefStore.setValue(
            ITourbookPreferences.GRAPH_VISIBLE,
            StringToArrayConverter.convertArrayToString(prefGraphsChecked));

      // convert the array of all table items into a string which is store in
      // the prefs
      final TableItem[] items = _graphCheckboxList.getTable().getItems();
      final String[] prefGraphs = new String[items.length];
      for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
         prefGraphs[itemIndex] = Integer.toString(((Graph) items[itemIndex].getData()).__graphId);
      }

      _prefStore.setValue(ITourbookPreferences.GRAPH_ALL, StringToArrayConverter.convertArrayToString(prefGraphs));
   }

   private void saveState_Tab_2_Options() {

      // x-axis units
      if (_rdoShow_Time.getSelection()) {
         _prefStore.setValue(ITourbookPreferences.GRAPH_X_AXIS, TourManager.X_AXIS_TIME);
      } else {
         _prefStore.setValue(ITourbookPreferences.GRAPH_X_AXIS, TourManager.X_AXIS_DISTANCE);
      }
      _prefStore.setValue(ITourbookPreferences.GRAPH_X_AXIS_STARTTIME, _chkShowStartTime.getSelection());

      // mouse wheel mode
      if (_rdoMouseMode_Slider.getSelection()) {
         _prefStore.setValue(ITourbookPreferences.GRAPH_MOUSE_MODE, MouseWheelMode.Selection.name());
      } else {
         _prefStore.setValue(ITourbookPreferences.GRAPH_MOUSE_MODE, MouseWheelMode.Zoom.name());
      }

      // zoom options
      _prefStore.setValue(ITourbookPreferences.GRAPH_ZOOM_AUTO_ZOOM_TO_SLIDER, _chkZoomToSlider.getSelection());
      _prefStore.setValue(ITourbookPreferences.GRAPH_MOVE_SLIDERS_WHEN_ZOOMED, _chkMoveSlidersWhenZoomed.getSelection());

      // mouse key translation
      if (_rdoMouseKey_UpLeft.getSelection()) {
         _prefStore_Chart.setValue(IChartPreferences.GRAPH_MOUSE_KEY_TRANSLATION, MouseWheel2KeyTranslation.Up_Left.name());
      } else {
         _prefStore_Chart.setValue(IChartPreferences.GRAPH_MOUSE_KEY_TRANSLATION, MouseWheel2KeyTranslation.Up_Right.name());
      }
   }

   private void saveState_UI() {

      if (_tabFolder == null) {
         // this happened
         return;
      }

      // keep selected tab
      _prefStore.setValue(STATE_PREF_PAGE_CHART_GRAPHS_SELECTED_TAB, _tabFolder.getSelectionIndex());
   }

   /**
    * check the fields if they are valid
    */
   private void validateInput() {

      if (_graphCheckboxList.getCheckedElements().length == 0) {

         setErrorMessage(Messages.Pref_Graphs_Error_one_graph_must_be_selected);
         setValid(false);

      } else {

         setErrorMessage(null);
         setValid(true);
      }
   }

}
