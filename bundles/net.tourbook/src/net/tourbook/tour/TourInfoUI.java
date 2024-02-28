/*******************************************************************************
 * Copyright (C) 2005, 2024 Wolfgang Schramm and Contributors
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
package net.tourbook.tour;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.tourbook.OtherMessages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.CommonImages;
import net.tourbook.common.UI;
import net.tourbook.common.font.MTFont;
import net.tourbook.common.formatter.FormatManager;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.time.TourDateTime;
import net.tourbook.common.util.IToolTipProvider;
import net.tourbook.common.util.StringUtils;
import net.tourbook.common.util.Util;
import net.tourbook.common.weather.IWeather;
import net.tourbook.data.DeviceSensor;
import net.tourbook.data.DeviceSensorValue;
import net.tourbook.data.TourData;
import net.tourbook.data.TourPersonHRZone;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourType;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.preferences.PrefPageAppearanceDisplayFormat;
import net.tourbook.statistic.StatisticView;
import net.tourbook.tag.TagManager;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.Messages;
import net.tourbook.ui.action.ActionTourToolTip_EditQuick;
import net.tourbook.ui.action.ActionTourToolTip_EditTour;
import net.tourbook.ui.action.Action_ToolTip_EditPreferences;
import net.tourbook.ui.views.sensors.BatteryStatus;
import net.tourbook.ui.views.sensors.SelectionRecordingDeviceBattery;
import net.tourbook.ui.views.sensors.SelectionSensor;
import net.tourbook.ui.views.sensors.SensorChartView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPart;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public class TourInfoUI {

   private static final String            ID                            = "net.tourbook.tour.TourInfoUI";       //$NON-NLS-1$

   private static final String            STATE_UI_WIDTH_SIZE_INDEX     = "STATE_UI_WIDTH_SIZE_INDEX";          //$NON-NLS-1$

   private static final String            STATE_UI_WIDTH_SMALL          = "STATE_UI_WIDTH_SMALL";               //$NON-NLS-1$
   private static final String            STATE_UI_WIDTH_MEDIUM         = "STATE_UI_WIDTH_MEDIUM";              //$NON-NLS-1$
   private static final String            STATE_UI_WIDTH_LARGE          = "STATE_UI_WIDTH_LARGE";               //$NON-NLS-1$
   private static final int               STATE_UI_WIDTH_SMALL_DEFAULT  = 600;
   private static final int               STATE_UI_WIDTH_MEDIUM_DEFAULT = 800;
   private static final int               STATE_UI_WIDTH_LARGE_DEFAULT  = 1000;

   private static final int               STATE_UI_WIDTH_MIN            = 100;
   private static final int               STATE_UI_WIDTH_MAX            = 3000;

   private static final int               SHELL_MARGIN                  = 5;
   private static final int               MAX_DATA_WIDTH                = 300;

   private static final String            BATTERY_FORMAT                = "... %d %%";                          //$NON-NLS-1$
   private static final String            GEAR_SHIFT_FORMAT             = "%d / %d";                            //$NON-NLS-1$

   private static final IPreferenceStore  _prefStoreCommon              = CommonActivator.getPrefStore();

   private static final DateTimeFormatter _dtHistoryFormatter           = DateTimeFormatter.ofLocalizedDateTime(
         FormatStyle.FULL,
         FormatStyle.MEDIUM);

   private static PeriodType              _tourPeriodTemplate           = PeriodType.yearMonthDayTime()

         // hide these components
         // .withMinutesRemoved()

         .withSecondsRemoved()
         .withMillisRemoved()
//
   ;

   private static final IPreferenceStore  _prefStore                    = TourbookPlugin.getPrefStore();
   private static final IDialogSettings   _state                        = TourbookPlugin.getState(ID);

   private final NumberFormat             _nf0                          = NumberFormat.getNumberInstance();
   private final NumberFormat             _nf1                          = NumberFormat.getInstance();
   private final NumberFormat             _nf2                          = NumberFormat.getInstance();
   private final NumberFormat             _nf3                          = NumberFormat.getInstance();

   {
      _nf0.setMinimumFractionDigits(0);
      _nf0.setMaximumFractionDigits(0);

      _nf1.setMinimumFractionDigits(1);
      _nf1.setMaximumFractionDigits(1);

      _nf2.setMinimumFractionDigits(2);
      _nf2.setMaximumFractionDigits(2);

      _nf3.setMinimumFractionDigits(3);
      _nf3.setMaximumFractionDigits(3);
   }

   private boolean        _hasRecordingDeviceBattery;
   private boolean        _hasGears;
   private boolean        _hasRunDyn;
   private boolean        _hasSensorValues;
   private boolean        _hasTags;
   private boolean        _hasTourType;

   private boolean        _hasTourDescription;
   private boolean        _hasLocationStart;
   private boolean        _hasLocationEnd;
   private boolean        _hasWeatherDescription;

   private int            _uiWidth_Pixel;
   private int            _uiWidth_SizeIndex;

   private int            _descriptionLineCount;
   private int            _descriptionScroll_Lines = 15;
   private int            _descriptionScroll_Height;

   /**
    * Part which fired an event
    */
   private IWorkbenchPart _part;

   /*
    * Actions
    */
   private ActionCloseTooltip             _actionCloseTooltip;
   private ActionTourToolTip_EditTour     _actionEditTour;
   private ActionTourToolTip_EditQuick    _actionEditQuick;
   private Action_ToolTip_EditPreferences _actionPrefDialog;

   private boolean                        _isActionsVisible = false;

   /**
    * When <code>true</code> then the tour info is embedded in a view and do not need the toolbar to
    * close the tooltip.
    */
   private boolean                        _isUIEmbedded;

   /**
    * Tour which is displayed in the tool tip
    */
   private TourData                       _tourData;

   private ArrayList<DeviceSensorValue>   _allSensorValuesWithData;

   private String                         _noTourTooltip    = Messages.Tour_Tooltip_Label_NoTour;

   private PixelConverter                 _pc;

//   private FocusListener                  _keepOpenListener;

   /*
    * Fields which are optionally displayed when they are not null
    */
   private ZonedDateTime    _uiDtCreated;
   private ZonedDateTime    _uiDtModified;
   private String           _uiTourTypeName;

   private IToolTipProvider _tourToolTipProvider;
   private ITourProvider    _tourProvider;

   /*
    * UI resources
    */
   private Color _bgColor;
   private Color _fgColor;

   private Font  _boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);

   /*
    * UI controls
    */
   private Composite        _parent;
   private Composite        _ttContainer;
   private Composite        _lowerPartContainer;

   private Text             _txtDescription;
   private Text             _txtLocationEnd;
   private Text             _txtLocationStart;
   private Text             _txtWeather;

   private CLabel           _lblClouds;
   private CLabel           _lblTourType_Image;

   private Label            _lblAirQuality;
   private Label            _lblAltimeter_Up;
   private Label            _lblAltimeter_Up_Unit;
   private Label            _lblAltimeter_Down;
   private Label            _lblAltimeter_Down_Unit;
   private Label            _lblAvgCadence;
   private Label            _lblAvgCadenceUnit;
   private Label            _lblAvgElevation;
   private Label            _lblAvgElevationUnit;
   private Label            _lblAvgElevationChange;
   private Label            _lblAvgElevationChange_Unit;
   private Label            _lblAvgPace;
   private Label            _lblAvgPaceUnit;
   private Label            _lblAvg_Power;
   private Label            _lblAvg_PowerUnit;
   private Label            _lblAvgPulse;
   private Label            _lblAvgPulseUnit;
   private Label            _lblAvgSpeed;
   private Label            _lblAvgSpeedUnit;
   private Label            _lblBattery_Spacer;
   private Label            _lblBattery_Start;
   private Label            _lblBattery_End;
   private Label            _lblBodyWeight;
   private Label            _lblBreakTime;
   private Label            _lblBreakTime_Unit;
   private Label            _lblCalories;
   private Label            _lblCloudsUnit;
   private Label            _lblDate;
   private Label            _lblDateTimeCreatedValue;
   private Label            _lblDateTimeModifiedValue;
   private Label            _lblDateTimeModified;
   private Label            _lblDescription;
   private Label            _lblDistance;
   private Label            _lblDistance_Unit;
   private Label            _lblElevationUp;
   private Label            _lblElevationUp_Unit;
   private Label            _lblElevationDown;
   private Label            _lblElevationDown_Unit;
   private Label            _lblGear;
   private Label            _lblGear_Spacer;
   private Label            _lblGear_GearShifts;
   private Label            _lblGear_GearShifts_Spacer;
   private Label            _lblMaxElevation;
   private Label            _lblMaxElevation_Unit;
   private Label            _lblMaxPace;
   private Label            _lblMaxPace_Unit;
   private Label            _lblMaxPulse;
   private Label            _lblMaxPulse_Unit;
   private Label            _lblMaxSpeed;
   private Label            _lblMaxSpeed_Unit;
   private Label            _lblMovingTime;
   private Label            _lblMovingTime_Unit;
   private Label            _lblElapsedTime;
   private Label            _lblElapsedTime_Unit;
   private Label            _lblLocationStart;
   private Label            _lblLocationEnd;
   private Label            _lblPausedTime;
   private Label            _lblPausedTime_Unit;
   private Label            _lblRecordedTime;
   private Label            _lblRecordedTime_Unit;
   private Label            _lblRestPulse;
   private Label            _lblTemperature_Part1;
   private Label            _lblTemperature_Part2;
   private Label            _lblTimeZone_Value;
   private Label            _lblTimeZoneDifference;
   private Label            _lblTimeZoneDifference_Value;
   private Label            _lblTitle;
   private Label            _lblTourTags;
   private Label            _lblTourTags_Value;
   private Label            _lblTourType;
   private Label            _lblTourType_Value;
   private Label            _lblWeather;
   private Label            _lblWindSpeed;
   private Label            _lblWindSpeedUnit;
   private Label            _lblWindDirection;
   private Label            _lblWindDirectionUnit;

   private Label            _lblRunDyn_StanceTime_Min;
   private Label            _lblRunDyn_StanceTime_Min_Unit;
   private Label            _lblRunDyn_StanceTime_Max;
   private Label            _lblRunDyn_StanceTime_Max_Unit;
   private Label            _lblRunDyn_StanceTime_Avg;
   private Label            _lblRunDyn_StanceTime_Avg_Unit;
   private Label            _lblRunDyn_StanceTimeBalance_Min;
   private Label            _lblRunDyn_StanceTimeBalance_Min_Unit;
   private Label            _lblRunDyn_StanceTimeBalance_Max;
   private Label            _lblRunDyn_StanceTimeBalance_Max_Unit;
   private Label            _lblRunDyn_StanceTimeBalance_Avg;
   private Label            _lblRunDyn_StanceTimeBalance_Avg_Unit;
   private Label            _lblRunDyn_StepLength_Min;
   private Label            _lblRunDyn_StepLength_Min_Unit;
   private Label            _lblRunDyn_StepLength_Max;
   private Label            _lblRunDyn_StepLength_Max_Unit;
   private Label            _lblRunDyn_StepLength_Avg;
   private Label            _lblRunDyn_StepLength_Avg_Unit;
   private Label            _lblRunDyn_VerticalOscillation_Min;
   private Label            _lblRunDyn_VerticalOscillation_Min_Unit;
   private Label            _lblRunDyn_VerticalOscillation_Max;
   private Label            _lblRunDyn_VerticalOscillation_Max_Unit;
   private Label            _lblRunDyn_VerticalOscillation_Avg;
   private Label            _lblRunDyn_VerticalOscillation_Avg_Unit;
   private Label            _lblRunDyn_VerticalRatio_Min;
   private Label            _lblRunDyn_VerticalRatio_Min_Unit;
   private Label            _lblRunDyn_VerticalRatio_Max;
   private Label            _lblRunDyn_VerticalRatio_Max_Unit;
   private Label            _lblRunDyn_VerticalRatio_Avg;
   private Label            _lblRunDyn_VerticalRatio_Avg_Unit;
   //
   private Label            _lblVerticalSpeed_Distance_Header;
   private Label            _lblVerticalSpeed_Distance_Flat;
   private Label            _lblVerticalSpeed_Distance_Gain;
   private Label            _lblVerticalSpeed_Distance_Loss;
   //
   private Label            _lblVerticalSpeed_Distance_Relative_Header;
   private Label            _lblVerticalSpeed_Distance_Relative_Flat;
   private Label            _lblVerticalSpeed_Distance_Relative_Gain;
   private Label            _lblVerticalSpeed_Distance_Relative_Loss;
   //
   private Label            _lblVerticalSpeed_Elevation_Header;
   private Label            _lblVerticalSpeed_Elevation_Gain;
   private Label            _lblVerticalSpeed_Elevation_Loss;
   //
   private Label            _lblVerticalSpeed_Speed_Header;
   private Label            _lblVerticalSpeed_Speed_Flat;
   private Label            _lblVerticalSpeed_Speed_Gain;
   private Label            _lblVerticalSpeed_Speed_Loss;
   //
   private Label            _lblVerticalSpeed_Time_Header;
   private Label            _lblVerticalSpeed_Time_Flat;
   private Label            _lblVerticalSpeed_Time_Gain;
   private Label            _lblVerticalSpeed_Time_Loss;
   //
   private Label            _lblVerticalSpeed_Time_Relative_Header;
   private Label            _lblVerticalSpeed_Time_Relative_Flat;
   private Label            _lblVerticalSpeed_Time_Relative_Gain;
   private Label            _lblVerticalSpeed_Time_Relative_Loss;

   private Link             _linkBattery;
   private ArrayList<Link>  _allSensorValue_Link;

   private ArrayList<Label> _allSensorValue_Level;
   private ArrayList<Label> _allSensorValue_Status;
   private ArrayList<Label> _allSensorValue_Voltage;

   private Combo            _comboUIWidth_Size;

   private Spinner          _spinnerUIWidth_Pixel;

   private class ActionCloseTooltip extends Action {

      public ActionCloseTooltip() {

         super(null, IAction.AS_PUSH_BUTTON);

         setToolTipText(OtherMessages.APP_ACTION_CLOSE_TOOLTIP);
         setImageDescriptor(CommonActivator.getThemedImageDescriptor(CommonImages.App_Close));
      }

      @Override
      public void run() {
         _tourToolTipProvider.hideToolTip();
      }
   }

   /**
    * Run tour action quick edit.
    */
   public void actionQuickEditTour() {

      _actionEditQuick.run();
   }

   /**
    * @param parent
    * @param tourData
    * @param tourToolTipProvider
    * @param tourProvider
    *
    * @return Returns the content area control
    */
   public Composite createContentArea(final Composite parent,
                                      final TourData tourData,
                                      final IToolTipProvider tourToolTipProvider,
                                      final ITourProvider tourProvider) {

      _parent = parent;
      _tourData = tourData;
      _tourToolTipProvider = tourToolTipProvider;
      _tourProvider = tourProvider;

      final Display display = parent.getDisplay();

      _bgColor = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
      _fgColor = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);

      final Set<TourTag> tourTags = _tourData.getTourTags();
      // date/time created/modified
      _uiDtCreated = _tourData.getDateTimeCreated();
      _uiDtModified = _tourData.getDateTimeModified();

      final TourType tourType = _tourData.getTourType();
      _uiTourTypeName = tourType == null
            ? null
            : TourDatabase.getTourTypeName(tourType.getTypeId());

// SET_FORMATTING_OFF

      _hasGears                     = _tourData.getFrontShiftCount() > 0 || _tourData.getRearShiftCount() > 0;
      _hasRecordingDeviceBattery    = tourData.getBattery_Percentage_Start() != -1;
      _hasRunDyn                    = _tourData.isRunDynAvailable();
      _hasTags                      = tourTags != null && tourTags.size() > 0;
      _hasTourType                  = tourType != null;
      _hasSensorValues              = _tourData.getDeviceSensorValues().size() > 0;

      _hasLocationEnd               = StringUtils.hasContent(_tourData.getTourEndPlace());
      _hasLocationStart             = StringUtils.hasContent(_tourData.getTourStartPlace());
      _hasTourDescription           = StringUtils.hasContent(_tourData.getTourDescription());
      _hasWeatherDescription        = _tourData.getWeather().length() > 0;

// SET_FORMATTING_ON

      restoreState_BeforeUI(parent);
      initUI(parent);

      final Composite container = createUI(parent);

// this do not help to remove flickering, first an empty tooltip window is displayed then also it's content
//      _ttContainer.setRedraw(false);

      fillUI();

      restoreState();

      updateUI();
      updateUI_Layout();

      enableControls();

//      _ttContainer.setRedraw(true);

      return container;
   }

   private Composite createUI(final Composite parent) {

      final Point defaultSpacing = LayoutConstants.getSpacing();
      final int columnSpacing = defaultSpacing.x + 30;

      /*
       * shell container is necessary because the margins of the inner container will hide the
       * tooltip when the mouse is hovered, which is not as it should be.
       */
      final Composite shellContainer = new Composite(parent, SWT.NONE);
      shellContainer.setForeground(_fgColor);
      shellContainer.setBackground(_bgColor);
      GridLayoutFactory.fillDefaults().applyTo(shellContainer);
      {
         _ttContainer = new Composite(shellContainer, SWT.NONE);
         _ttContainer.setForeground(_fgColor);
         _ttContainer.setBackground(_bgColor);
         GridLayoutFactory.fillDefaults()
               .margins(SHELL_MARGIN, SHELL_MARGIN)
               .applyTo(_ttContainer);
//         _ttContainer.setBackground(UI.SYS_COLOR_GREEN);
         {
            createUI_10_UpperPart(_ttContainer);

            final Composite container = new Composite(_ttContainer, SWT.NONE);
            container.setBackground(_bgColor);
            GridDataFactory.fillDefaults().applyTo(container);
//            container.setBackground(UI.SYS_COLOR_CYAN);

            if (_hasRunDyn) {

               GridLayoutFactory.fillDefaults()
                     .numColumns(3)
                     .spacing(columnSpacing, defaultSpacing.y)
                     .applyTo(container);
               {
                  createUI_30_Column_1(container);
                  createUI_40_Column_2(container);
                  createUI_50_Column_3(container);
               }

            } else {

               GridLayoutFactory.fillDefaults()
                     .numColumns(2)
                     .spacing(columnSpacing, defaultSpacing.y)
                     .applyTo(container);
               {
                  createUI_30_Column_1(container);
                  createUI_40_Column_2(container);
               }
            }

            createUI_90_LowerPart(_ttContainer);
            createUI_99_CreateModifyTime(_ttContainer);
         }
      }

      return shellContainer;
   }

   private void createUI_10_UpperPart(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      container.setForeground(_fgColor);
      container.setBackground(_bgColor);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults()
            .numColumns(3)
            .applyTo(container);
      {
         {
            /*
             * Tour type
             */
            if (_uiTourTypeName != null) {

               _lblTourType_Image = new CLabel(container, SWT.NONE);
               _lblTourType_Image.setForeground(_fgColor);
               _lblTourType_Image.setBackground(_bgColor);
               GridDataFactory.swtDefaults()
                     .align(SWT.BEGINNING, SWT.BEGINNING)
                     .applyTo(_lblTourType_Image);
            }
         }
         {
            /*
             * Title
             */
            _lblTitle = new Label(container, SWT.LEAD | SWT.WRAP);
            _lblTitle.setForeground(_fgColor);
            _lblTitle.setBackground(_bgColor);
            GridDataFactory.fillDefaults()
                  .hint(MAX_DATA_WIDTH, SWT.DEFAULT)
                  .grab(true, false)
                  .align(SWT.FILL, SWT.CENTER)
                  .applyTo(_lblTitle);
            MTFont.setBannerFont(_lblTitle);
         }
         {
            /*
             * Action toolbar in the top right corner
             */
            createUI_12_Toolbar(container);
         }

         // LINE 2

         {
            /*
             * Date
             */
            _lblDate = createUI_LabelValue(container, SWT.LEAD);
            GridDataFactory.fillDefaults().span(3, 1).applyTo(_lblDate);
         }
      }
   }

   private void createUI_12_Toolbar(final Composite container) {

      if (_isUIEmbedded) {

         // spacer
         new Label(container, SWT.NONE);

      } else {

         /*
          * Create toolbar
          */
         final ToolBar toolbar = new ToolBar(container, SWT.FLAT);
         toolbar.setForeground(_fgColor);
         toolbar.setBackground(_bgColor);
         GridDataFactory.fillDefaults().applyTo(toolbar);

         final ToolBarManager tbm = new ToolBarManager(toolbar);

         /*
          * Fill toolbar
          */
         if (_isActionsVisible) {

            _actionEditTour = new ActionTourToolTip_EditTour(_tourToolTipProvider, _tourProvider);
            _actionEditQuick = new ActionTourToolTip_EditQuick(_tourToolTipProvider, _tourProvider);

            final Integer selectedTabFolder = Integer.valueOf(0);

            _actionPrefDialog = new Action_ToolTip_EditPreferences(_tourToolTipProvider,
                  Messages.Tour_Tooltip_Action_EditFormatPreferences,
                  PrefPageAppearanceDisplayFormat.ID,
                  selectedTabFolder);

            tbm.add(_actionEditTour);
            tbm.add(_actionEditQuick);
            tbm.add(_actionPrefDialog);
         }

         /**
          * The close action is ALWAYS visible, sometimes there is a bug that the tooltip do not
          * automatically close when hovering out.
          */
         _actionCloseTooltip = new ActionCloseTooltip();
         tbm.add(_actionCloseTooltip);

         tbm.update(true);
      }
   }

   private void createUI_30_Column_1(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      container.setForeground(_fgColor);
      container.setBackground(_bgColor);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).spacing(5, 0).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_MAGENTA);
      {
         createUI_32_Time(container);
         createUI_34_DistanceElevation(container);
         createUI_36_Misc(container);
         createUI_37_HeartRateZones(container);

         // gear data
         _lblGear_Spacer = createUI_Spacer(container);
         createUI_38_Gears(container);
      }
   }

   private void createUI_32_Time(final Composite container) {

      {
         /*
          * Elapsed time
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_ElapsedTime);

         _lblElapsedTime = createUI_LabelValue(container, SWT.TRAIL);
         _lblElapsedTime_Unit = createUI_Label(container, Messages.Tour_Tooltip_Label_Hour);

         // force this column to take the rest of the space
         GridDataFactory.fillDefaults().grab(true, false).applyTo(_lblElapsedTime_Unit);
      }

      {
         /*
          * Recorded time
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_RecordedTime);

         _lblRecordedTime = createUI_LabelValue(container, SWT.TRAIL);
         _lblRecordedTime_Unit = createUI_Label(container, Messages.Tour_Tooltip_Label_Hour);
      }

      {
         /*
          * Paused time
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_PausedTime);

         _lblPausedTime = createUI_LabelValue(container, SWT.TRAIL);
         _lblPausedTime_Unit = createUI_Label(container, Messages.Tour_Tooltip_Label_Hour);
      }

      {
         /*
          * Moving time
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_MovingTime);

         _lblMovingTime = createUI_LabelValue(container, SWT.TRAIL);
         _lblMovingTime_Unit = createUI_Label(container, Messages.Tour_Tooltip_Label_Hour);
      }

      {
         /*
          * Break time
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_BreakTime);

         _lblBreakTime = createUI_LabelValue(container, SWT.TRAIL);
         _lblBreakTime_Unit = createUI_Label(container, Messages.Tour_Tooltip_Label_Hour);
      }

      if (isSimpleTour()) {

         createUI_Spacer(container);

         {
            /*
             * Timezone
             */
            createUI_Label(container, Messages.Tour_Tooltip_Label_TimeZone);

            _lblTimeZone_Value = createUI_LabelValue(container, SWT.LEAD);
            GridDataFactory.fillDefaults().span(2, 1).applyTo(_lblTimeZone_Value);
         }
         {
            /*
             * Timezone difference
             */

            _lblTimeZoneDifference = createUI_Label(container, Messages.Tour_Tooltip_Label_TimeZoneDifference);

            // set layout that the decoration is correctly layouted
//          GridDataFactory.fillDefaults().applyTo(_lblTimeZoneDifference);

            _lblTimeZoneDifference_Value = createUI_LabelValue(container, SWT.TRAIL);

            // hour
            createUI_Label(container, Messages.Tour_Tooltip_Label_Hour);
         }
      }
   }

   private void createUI_34_DistanceElevation(final Composite container) {

      createUI_Spacer(container);

      /*
       * Distance
       */
      createUI_Label(container, Messages.Tour_Tooltip_Label_Distance);

      _lblDistance = createUI_LabelValue(container, SWT.TRAIL);
      _lblDistance_Unit = createUI_LabelValue(container, SWT.LEAD);

      /*
       * Elevation up
       */
      createUI_Label(container, Messages.Tour_Tooltip_Label_AltitudeUp);

      _lblElevationUp = createUI_LabelValue(container, SWT.TRAIL);
      _lblElevationUp_Unit = createUI_LabelValue(container, SWT.LEAD);

      /*
       * Elevation down
       */
      createUI_Label(container, Messages.Tour_Tooltip_Label_AltitudeDown);

      _lblElevationDown = createUI_LabelValue(container, SWT.TRAIL);
      _lblElevationDown_Unit = createUI_LabelValue(container, SWT.LEAD);

      /*
       * Average elevation change
       */
      createUI_Label(container, Messages.Tour_Tooltip_Label_AvgElevationChange);

      _lblAvgElevationChange = createUI_LabelValue(container, SWT.TRAIL);
      _lblAvgElevationChange_Unit = createUI_LabelValue(container, SWT.LEAD);

      createUI_Spacer(container);
   }

   private void createUI_36_Misc(final Composite container) {

      {
         /*
          * calories
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_Calories);

         _lblCalories = createUI_LabelValue(container, SWT.TRAIL);

         createUI_Label(container, Messages.Value_Unit_KCalories);
      }

      {
         /*
          * rest pulse
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_RestPulse);

         _lblRestPulse = createUI_LabelValue(container, SWT.TRAIL);

         createUI_Label(container, Messages.Value_Unit_Pulse);
      }
      {
         /*
          * Body weight
          */
         createUI_Label(container, Messages.Tour_Tooltip_Label_BodyWeight);

         _lblBodyWeight = createUI_LabelValue(container, SWT.TRAIL);

         createUI_Label(container, UI.UNIT_LABEL_WEIGHT);
      }
   }

   private void createUI_37_HeartRateZones(final Composite container) {

      createUI_Spacer(container);

      if (_tourData.getNumberOfHrZones() == 0) {
         return;
      }

      final List<TourPersonHRZone> tourPersonHrZones = _tourData.getDataPerson().getHrZonesSorted();

      final long movingTime = _tourData.getTourComputedTime_Moving();

      final int numHrZones = tourPersonHrZones.size();
      final int[] timeInHrZones = _tourData.getHrZones();

      for (int hrZonedIndex = numHrZones - 1; hrZonedIndex >= 0; --hrZonedIndex) {

         final TourPersonHRZone currentHrZone = tourPersonHrZones.get(hrZonedIndex);

         final int timeInTimeZone = timeInHrZones[hrZonedIndex];
         final float zonePercentage = timeInTimeZone * 100f / movingTime;

         final String zonePercentageTimeText = String.valueOf(Math.round(zonePercentage))
               + UI.SPACE
               + UI.SYMBOL_PERCENTAGE;

         final String lblTimeText = FormatManager.formatRecordedTime(timeInTimeZone)
               + UI.SPACE
               + Messages.Tour_Tooltip_Label_Hour;

         // label: HR zone
         createUI_Label(container, currentHrZone.getNameShort());

         // label: nn %
         final Label lblPercentage = createUI_LabelValue(container, SWT.TRAIL);
         lblPercentage.setText(timeInTimeZone > 0 ? zonePercentageTimeText : UI.EMPTY_STRING);

         // label: hh:mm:ss
         final Label lblTime = createUI_LabelValue(container, SWT.LEAD);
         lblTime.setText(timeInTimeZone > 0 ? lblTimeText : UI.EMPTY_STRING);
      }
   }

   private void createUI_38_Gears(final Composite parent) {

      /*
       * Front/rear gear shifts
       */
      _lblGear = createUI_Label(parent, Messages.Tour_Tooltip_Label_GearShifts);

      _lblGear_GearShifts = createUI_LabelValue(parent, SWT.TRAIL);
      _lblGear_GearShifts_Spacer = createUI_LabelValue(parent, SWT.LEAD);
   }

   private void createUI_40_Column_2(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      container.setForeground(_fgColor);
      container.setBackground(_bgColor);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).spacing(5, 0).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_BLUE);
      {
         createUI_42_Avg(container);

         createUI_Spacer(container);
         createUI_43_Max(container);

         createUI_Spacer(container);
         createUI_44_HillSpeed(container);

         createUI_Spacer(container);
         createUI_47_Weather(container);

         _lblBattery_Spacer = createUI_Spacer(container);
         createUI_48_Battery(container);
      }
   }

   private void createUI_42_Avg(final Composite parent) {

      /*
       * avg pulse
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AvgPulse);

      _lblAvgPulse = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAvgPulseUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * avg speed
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AvgSpeed);

      _lblAvgSpeed = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAvgSpeedUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * avg pace
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AvgPace);

      _lblAvgPace = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAvgPaceUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * avg cadence
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AvgCadence);

      _lblAvgCadence = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAvgCadenceUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * avg power
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AvgPower);

      _lblAvg_Power = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAvg_PowerUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * Average elevation
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AvgElevation);

      _lblAvgElevation = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAvgElevationUnit = createUI_LabelValue(parent, SWT.LEAD);
   }

   private void createUI_43_Max(final Composite parent) {

      /*
       * max pulse
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_MaxPulse);

      _lblMaxPulse = createUI_LabelValue(parent, SWT.TRAIL);
      _lblMaxPulse_Unit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * max speed
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_MaxSpeed);

      _lblMaxSpeed = createUI_LabelValue(parent, SWT.TRAIL);
      _lblMaxSpeed_Unit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * max pace
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_MaxPace);

      _lblMaxPace = createUI_LabelValue(parent, SWT.TRAIL);
      _lblMaxPace_Unit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * max altitude
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_MaxAltitude);

      _lblMaxElevation = createUI_LabelValue(parent, SWT.TRAIL);
      _lblMaxElevation_Unit = createUI_LabelValue(parent, SWT.LEAD);
   }

   private void createUI_44_HillSpeed(final Composite parent) {

      /*
       * Altimeter up
       */
      createUI_Label(parent, Messages.Segmenter_Tooltip_Label_Altimeter + UI.SPACE + UI.SYMBOL_ARROW_UP);

      _lblAltimeter_Up = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAltimeter_Up_Unit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * Altimeter down
       */
      createUI_Label(parent, Messages.Segmenter_Tooltip_Label_Altimeter + UI.SPACE + UI.SYMBOL_ARROW_DOWN);

      _lblAltimeter_Down = createUI_LabelValue(parent, SWT.TRAIL);
      _lblAltimeter_Down_Unit = createUI_LabelValue(parent, SWT.LEAD);

      createUI_Spacer(parent);

      /*
       * Vertical speed
       */
      final int columnSpacing = 15;

      final GridDataFactory gd = GridDataFactory.fillDefaults().grab(true, false);

      final Composite speedContainer = new Composite(parent, SWT.NONE);
      speedContainer.setForeground(_fgColor);
      speedContainer.setBackground(_bgColor);
//    speedContainer.setBackground(UI.SYS_COLOR_GREEN);
      GridDataFactory.fillDefaults().span(3, 1).applyTo(speedContainer);
      GridLayoutFactory.fillDefaults()
            .numColumns(7)
            .spacing(columnSpacing, 0)
            .applyTo(speedContainer);

      {
         {
            /*
             * Vertical speed: Header
             */
            final Label label = createUI_Label(speedContainer, UI.SPACE1);
            gd.applyTo(label);

            // elevation
            _lblVerticalSpeed_Elevation_Header = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Elevation_Header);

            // distance
            _lblVerticalSpeed_Distance_Header = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Header);

            _lblVerticalSpeed_Distance_Relative_Header = createUI_Label_Trailing(speedContainer, UI.SYMBOL_PERCENTAGE);
            gd.applyTo(_lblVerticalSpeed_Distance_Relative_Header);

            // time
            _lblVerticalSpeed_Time_Header = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Header);

            _lblVerticalSpeed_Time_Relative_Header = createUI_Label_Trailing(speedContainer, UI.SYMBOL_PERCENTAGE);
            gd.applyTo(_lblVerticalSpeed_Time_Relative_Header);

            // speed
            _lblVerticalSpeed_Speed_Header = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Speed_Header);
         }
         {
            /*
             * Vertical speed: Flat
             */
            final Label label = createUI_Label(speedContainer, OtherMessages.TOUR_SEGMENTER_LABEL_VERTICAL_SPEED_FLAT);
            gd.applyTo(label);

            // a flat elevation does not make sense
            final Label labelElevation_Flat = createUI_Label_Trailing(speedContainer, UI.SPACE1);
            gd.applyTo(labelElevation_Flat);

            // distance
            _lblVerticalSpeed_Distance_Flat = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Flat);

            // distance relative
            _lblVerticalSpeed_Distance_Relative_Flat = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Relative_Flat);

            // time
            _lblVerticalSpeed_Time_Flat = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Flat);

            // time relative
            _lblVerticalSpeed_Time_Relative_Flat = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Relative_Flat);

            // speed
            _lblVerticalSpeed_Speed_Flat = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Speed_Flat);
         }
         {
            /*
             * Vertical speed: Ascent
             */
            final Label label = createUI_Label(speedContainer, OtherMessages.TOUR_SEGMENTER_LABEL_VERTICAL_SPEED_ASCENT);
            gd.applyTo(label);

            // elevation
            _lblVerticalSpeed_Elevation_Gain = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Elevation_Gain);

            // distance
            _lblVerticalSpeed_Distance_Gain = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Gain);

            // distance relative
            _lblVerticalSpeed_Distance_Relative_Gain = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Relative_Gain);

            // time
            _lblVerticalSpeed_Time_Gain = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Gain);

            // time relative
            _lblVerticalSpeed_Time_Relative_Gain = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Relative_Gain);

            // speed
            _lblVerticalSpeed_Speed_Gain = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Speed_Gain);
         }
         {
            /*
             * Vertical speed: Descent
             */
            final Label label = createUI_Label(speedContainer, OtherMessages.TOUR_SEGMENTER_LABEL_VERTICAL_SPEED_DESCENT);
            gd.applyTo(label);

            // elevation
            _lblVerticalSpeed_Elevation_Loss = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Elevation_Loss);

            // distance
            _lblVerticalSpeed_Distance_Loss = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Loss);

            // distance relative
            _lblVerticalSpeed_Distance_Relative_Loss = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Distance_Relative_Loss);

            // time
            _lblVerticalSpeed_Time_Loss = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Loss);

            // time relative
            _lblVerticalSpeed_Time_Relative_Loss = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Time_Relative_Loss);

            // speed
            _lblVerticalSpeed_Speed_Loss = createUI_Label_Trailing(speedContainer, UI.EMPTY_STRING);
            gd.applyTo(_lblVerticalSpeed_Speed_Loss);
         }
      }
   }

   private void createUI_47_Weather(final Composite parent) {

      /*
       * Clouds
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_Clouds);

      // Icon: clouds
      _lblClouds = new CLabel(parent, SWT.TRAIL);
      _lblClouds.setForeground(_fgColor);
      _lblClouds.setBackground(_bgColor);
      GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).applyTo(_lblClouds);

      // text: clouds
      _lblCloudsUnit = createUI_LabelValue(parent, SWT.LEAD);
      GridDataFactory.swtDefaults().applyTo(_lblCloudsUnit);

      /*
       * Temperature
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_Temperature);

      _lblTemperature_Part1 = createUI_LabelValue(parent, SWT.TRAIL);
      _lblTemperature_Part2 = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * Wind speed
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_WindSpeed);

      _lblWindSpeed = createUI_LabelValue(parent, SWT.TRAIL);
      _lblWindSpeedUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * Wind direction
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_WindDirection);

      _lblWindDirection = createUI_LabelValue(parent, SWT.TRAIL);
      _lblWindDirectionUnit = createUI_LabelValue(parent, SWT.LEAD);

      /*
       * Air Quality
       */
      createUI_Label(parent, Messages.Tour_Tooltip_Label_AirQuality);
      UI.createSpacer_Horizontal(parent);

      _lblAirQuality = createUI_LabelValue(parent, SWT.LEAD);
      final GridData gd = (GridData) _lblAirQuality.getLayoutData();
      gd.horizontalAlignment = SWT.BEGINNING; // do not fill the cell with the background color
   }

   private void createUI_48_Battery(final Composite parent) {

      {
         /*
          * Device battery, e.g. 88...56 %
          */
         _linkBattery = createUI_Link(parent, Messages.Tour_Tooltip_Label_Battery);
         _linkBattery.setToolTipText(Messages.Tour_Tooltip_Label_Battery_Tooltip);
         _linkBattery.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onSelect_Battery()));

         _lblBattery_Start = createUI_LabelValue(parent, SWT.TRAIL);
         _lblBattery_Start.setToolTipText(Messages.Tour_Tooltip_Label_Battery_Tooltip);

         _lblBattery_End = createUI_LabelValue(parent, SWT.LEAD);
         _lblBattery_End.setToolTipText(Messages.Tour_Tooltip_Label_Battery_Tooltip);
      }
   }

   private void createUI_50_Column_3(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      container.setForeground(_fgColor);
      container.setBackground(_bgColor);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).spacing(5, 0).applyTo(container);
//      container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
      {
         createUI_52_RunDyn(container);
      }
   }

   private void createUI_52_RunDyn(final Composite parent) {

      {
         /*
          * Stance time
          */

         {
            /*
             * Min
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StanceTime_Min);

            _lblRunDyn_StanceTime_Min = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StanceTime_Min_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Max
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StanceTime_Max);

            _lblRunDyn_StanceTime_Max = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StanceTime_Max_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Avg
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StanceTime_Avg);

            _lblRunDyn_StanceTime_Avg = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StanceTime_Avg_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
      }

      createUI_Spacer(parent);

      {
         /*
          * Stance Time Balance
          */

         {
            /*
             * Min
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StanceTimeBalance_Min);

            _lblRunDyn_StanceTimeBalance_Min = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StanceTimeBalance_Min_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Max
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StanceTimeBalance_Max);

            _lblRunDyn_StanceTimeBalance_Max = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StanceTimeBalance_Max_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Avg
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StanceTimeBalance_Avg);

            _lblRunDyn_StanceTimeBalance_Avg = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StanceTimeBalance_Avg_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
      }

      createUI_Spacer(parent);

      {
         /*
          * Step Length
          */

         {
            /*
             * Min
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StepLength_Min);

            _lblRunDyn_StepLength_Min = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StepLength_Min_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Max
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StepLength_Max);

            _lblRunDyn_StepLength_Max = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StepLength_Max_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Avg
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_StepLength_Avg);

            _lblRunDyn_StepLength_Avg = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_StepLength_Avg_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
      }

      createUI_Spacer(parent);

      {
         /*
          * Vertical Oscillation
          */

         {
            /*
             * Min
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_VerticalOscillation_Min);

            _lblRunDyn_VerticalOscillation_Min = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_VerticalOscillation_Min_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Max
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_VerticalOscillation_Max);

            _lblRunDyn_VerticalOscillation_Max = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_VerticalOscillation_Max_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Avg
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_VerticalOscillation_Avg);

            _lblRunDyn_VerticalOscillation_Avg = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_VerticalOscillation_Avg_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
      }

      createUI_Spacer(parent);

      {
         /*
          * Vertical Ratio
          */

         {
            /*
             * Min
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_VerticalRatio_Min);

            _lblRunDyn_VerticalRatio_Min = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_VerticalRatio_Min_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Max
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_VerticalRatio_Max);

            _lblRunDyn_VerticalRatio_Max = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_VerticalRatio_Max_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
         {
            /*
             * Avg
             */
            createUI_Label(parent, Messages.Tour_Tooltip_Label_RunDyn_VerticalRatio_Avg);

            _lblRunDyn_VerticalRatio_Avg = createUI_LabelValue(parent, SWT.TRAIL);
            _lblRunDyn_VerticalRatio_Avg_Unit = createUI_LabelValue(parent, SWT.LEAD);
         }
      }
   }

   private void createUI_90_LowerPart(final Composite parent) {

      final int numColumns = 4;

      _lowerPartContainer = new Composite(parent, SWT.NONE);
      _lowerPartContainer.setForeground(_fgColor);
      _lowerPartContainer.setBackground(_bgColor);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(_lowerPartContainer);
      GridLayoutFactory.fillDefaults().numColumns(numColumns).spacing(16, 0).applyTo(_lowerPartContainer);
//      _lowerPartContainer.setBackground(UI.SYS_COLOR_CYAN);
      {

         createUI_92_SensorValues(_lowerPartContainer);

         {
            /*
             * Tour type
             */
            _lblTourType = createUI_Label(_lowerPartContainer, Messages.Tour_Tooltip_Label_TourType);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
                  .indent(0, 5)
                  .applyTo(_lblTourType);

            _lblTourType_Value = createUI_LabelValue(_lowerPartContainer, SWT.LEAD | SWT.WRAP);
            GridDataFactory.fillDefaults()
                  .span(numColumns - 1, 1)
//                  .grab(true, false)
//                  .hint(MAX_DATA_WIDTH, SWT.DEFAULT)
                  .indent(0, 5)
                  .applyTo(_lblTourType_Value);
         }
         {
            /*
             * Tags
             */
            _lblTourTags = createUI_Label(_lowerPartContainer, Messages.Tour_Tooltip_Label_Tags);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(_lblTourTags);

            _lblTourTags_Value = createUI_LabelValue(_lowerPartContainer, SWT.LEAD | SWT.WRAP);
            GridDataFactory.fillDefaults()
                  .span(numColumns - 1, 1)
//                  .grab(true, false)
//                  .hint(MAX_DATA_WIDTH, SWT.DEFAULT)
                  .applyTo(_lblTourTags_Value);
         }
         {
            /*
             * Weather
             */
            _lblWeather = createUI_Label(_lowerPartContainer, Messages.Tour_Tooltip_Label_Weather);
            _lblWeather.setFont(_boldFont);
            GridDataFactory.fillDefaults()
                  .span(numColumns, 1)
                  .indent(0, 10)
                  .applyTo(_lblWeather);

            _txtWeather = new Text(_lowerPartContainer, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
            _txtWeather.setForeground(_fgColor);
            _txtWeather.setBackground(_bgColor);
            GridDataFactory.fillDefaults().span(numColumns, 1).applyTo(_txtWeather);
         }
         {
            /*
             * Description
             */

            // label
            _lblDescription = createUI_Label(_lowerPartContainer, Messages.Tour_Tooltip_Label_Description);
            _lblDescription.setFont(_boldFont);
            GridDataFactory.fillDefaults()
                  .span(numColumns, 1)
                  .indent(0, 10)
                  .applyTo(_lblDescription);

            // text field
            int style = SWT.WRAP | SWT.MULTI | SWT.READ_ONLY;
            _descriptionLineCount = Util.countCharacter(_tourData.getTourDescription(), '\n');
            if (_descriptionLineCount > _descriptionScroll_Lines) {
               style |= SWT.V_SCROLL;
            }

            _txtDescription = new Text(_lowerPartContainer, style);
            _txtDescription.setForeground(_fgColor);
            _txtDescription.setBackground(_bgColor);

            GridDataFactory.fillDefaults().span(numColumns, 1).applyTo(_txtDescription);
            if (_descriptionLineCount > _descriptionScroll_Lines) {
               final GridData gd = (GridData) _txtDescription.getLayoutData();
               gd.heightHint = _descriptionScroll_Height;
            }
         }
         {
            /*
             * Start location
             */
            _lblLocationStart = createUI_Label(_lowerPartContainer, Messages.Tour_Tooltip_Label_LocationStart);
            _lblLocationStart.setFont(_boldFont);
            GridDataFactory.fillDefaults()
                  .span(numColumns, 1)
                  .indent(0, 10)
                  .applyTo(_lblLocationStart);

            _txtLocationStart = new Text(_lowerPartContainer, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
            _txtLocationStart.setForeground(_fgColor);
            _txtLocationStart.setBackground(_bgColor);
            GridDataFactory.fillDefaults().span(numColumns, 1).applyTo(_txtLocationStart);
         }
         {
            /*
             * End location
             */
            _lblLocationEnd = createUI_Label(_lowerPartContainer, Messages.Tour_Tooltip_Label_LocationEnd);
            _lblLocationEnd.setFont(_boldFont);
            GridDataFactory.fillDefaults()
                  .span(numColumns, 1)
                  .indent(0, 10)
                  .applyTo(_lblLocationEnd);

            _txtLocationEnd = new Text(_lowerPartContainer, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
            _txtLocationEnd.setForeground(_fgColor);
            _txtLocationEnd.setBackground(_bgColor);
            GridDataFactory.fillDefaults().span(numColumns, 1).applyTo(_txtLocationEnd);
         }
      }
   }

   private void createUI_92_SensorValues(final Composite parent) {

      /*
       * Setup sensor value data BEFORE returning, otherwise old data could cause widget dispose
       * exceptions because this instance is reused
       */
      _allSensorValuesWithData = new ArrayList<>();
      _allSensorValue_Link = new ArrayList<>();

      _allSensorValue_Level = new ArrayList<>();
      _allSensorValue_Status = new ArrayList<>();
      _allSensorValue_Voltage = new ArrayList<>();

      final Set<DeviceSensorValue> allSensorValues = _tourData.getDeviceSensorValues();
      if (allSensorValues.isEmpty()) {
         return;
      }

      // sort by sensor label
      final ArrayList<DeviceSensorValue> allSortedSensorValues = new ArrayList<>(allSensorValues);
      Collections.sort(allSortedSensorValues, (sensorValue1, sensorValue2) -> {

         return sensorValue1.getDeviceSensor().getLabel().compareTo(
               sensorValue2.getDeviceSensor().getLabel());
      });

      for (final DeviceSensorValue sensorValue : allSortedSensorValues) {

         if (sensorValue.isDataAvailable() == false) {
            continue;
         }

         final DeviceSensor sensor = sensorValue.getDeviceSensor();

         _allSensorValuesWithData.add(sensorValue);

         // sensor label/link
         final Link link = createUI_Link(parent, sensor.getLabel());
         link.setData(sensor);
         link.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onSelect_Sensor(selectionEvent)));
         _allSensorValue_Link.add(link);

         _allSensorValue_Level.add(createUI_LabelValue(parent, SWT.LEAD));
         _allSensorValue_Voltage.add(createUI_LabelValue(parent, SWT.LEAD));
         _allSensorValue_Status.add(createUI_LabelValue(parent, SWT.LEAD));
      }
   }

   private void createUI_99_CreateModifyTime(final Composite parent) {

      final boolean hasDescription = _hasTourDescription || _hasWeatherDescription || _hasLocationStart || _hasLocationEnd;

      final boolean isShowUIWidthControls = hasDescription

            // hide when embedded
            && _isUIEmbedded == false;

      final int numColumns = isShowUIWidthControls ? 3 : 2;

      final Composite container = new Composite(parent, SWT.NONE);
      container.setForeground(_fgColor);
      container.setBackground(_bgColor);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults()
            .numColumns(numColumns)
            .spacing(20, 5)
            .applyTo(container);
//      container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
      {
         {
            /*
             * Date/time created
             */
            final Composite containerCreated = new Composite(container, SWT.NONE);
            containerCreated.setForeground(_fgColor);
            containerCreated.setBackground(_bgColor);
            GridDataFactory.fillDefaults()
                  .grab(true, false)
                  .align(SWT.FILL, SWT.CENTER)
                  .applyTo(containerCreated);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(containerCreated);
            {
               createUI_Label(containerCreated, Messages.Tour_Tooltip_Label_DateTimeCreated);

               _lblDateTimeCreatedValue = createUI_LabelValue(containerCreated, SWT.LEAD);
               GridDataFactory.fillDefaults().applyTo(_lblDateTimeCreatedValue);
            }
         }
         {
            /*
             * Date/time modified
             */
            final Composite containerModified = new Composite(container, SWT.NONE);
            containerModified.setForeground(_fgColor);
            containerModified.setBackground(_bgColor);
            GridDataFactory.fillDefaults()
                  .grab(true, false)
                  .align(SWT.FILL, SWT.CENTER)
                  .applyTo(containerModified);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(containerModified);
            {
               _lblDateTimeModified = createUI_Label(containerModified, Messages.Tour_Tooltip_Label_DateTimeModified);
               GridDataFactory.fillDefaults()
                     .grab(true, false)
                     .align(SWT.END, SWT.FILL)
                     .applyTo(_lblDateTimeModified);

               _lblDateTimeModifiedValue = createUI_LabelValue(containerModified, SWT.TRAIL);
               GridDataFactory.fillDefaults()
                     .align(SWT.END, SWT.FILL)
                     .applyTo(_lblDateTimeModifiedValue);
            }
         }

         if (isShowUIWidthControls) {

            final Composite containerTextWidth = new Composite(container, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(containerTextWidth);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(containerTextWidth);
            {
               {
                  // Combo: Mouse wheel incrementer
                  _comboUIWidth_Size = new Combo(containerTextWidth, SWT.READ_ONLY | SWT.BORDER);
                  _comboUIWidth_Size.setVisibleItemCount(10);
                  _comboUIWidth_Size.setToolTipText(Messages.Tour_Tooltip_Combo_UIWidthSize_Tooltip);
                  _comboUIWidth_Size.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onSelect_UIWidth_1_Size()));
//                  _comboUIWidth_Size.addFocusListener(_keepOpenListener);

                  GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(_comboUIWidth_Size);
               }
               {
                  /*
                   * Text width in pixel
                   */
                  _spinnerUIWidth_Pixel = new Spinner(containerTextWidth, SWT.BORDER);
                  _spinnerUIWidth_Pixel.setMinimum(STATE_UI_WIDTH_MIN);
                  _spinnerUIWidth_Pixel.setMaximum(STATE_UI_WIDTH_MAX);
                  _spinnerUIWidth_Pixel.setIncrement(10);
                  _spinnerUIWidth_Pixel.setPageIncrement(50);
                  _spinnerUIWidth_Pixel.setToolTipText(Messages.Tour_Tooltip_Spinner_TextWidth_Tooltip);
                  _spinnerUIWidth_Pixel.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onSelect_UIWidth_2_Value()));
                  _spinnerUIWidth_Pixel.addMouseWheelListener(mouseEvent -> {

                     UI.adjustSpinnerValueOnMouseScroll(mouseEvent, 10);
                     onSelect_UIWidth_2_Value();
                  });

                  GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(_spinnerUIWidth_Pixel);
               }
            }
         }
      }
   }

   private Label createUI_Label(final Composite parent, final String labelText) {

      final Label label = new Label(parent, SWT.NONE);
      label.setForeground(_fgColor);
      label.setBackground(_bgColor);

      if (labelText != null) {
         label.setText(labelText);
      }

      return label;
   }

   private Label createUI_Label_Trailing(final Composite parent, final String labelText) {

      final Label label = new Label(parent, SWT.TRAIL);
      label.setForeground(_fgColor);
      label.setBackground(_bgColor);

      if (labelText != null) {
         label.setText(labelText);
      }

      return label;
   }

   private Label createUI_LabelValue(final Composite parent, final int style) {

      final Label label = new Label(parent, style);
      label.setForeground(_fgColor);
      label.setBackground(_bgColor);
      GridDataFactory.fillDefaults().applyTo(label);

      return label;
   }

   private Link createUI_Link(final Composite parent, final String linkText) {

      final Link link = new Link(parent, SWT.NONE);
      link.setText(UI.LINK_TAG_START + linkText + UI.LINK_TAG_END);
      link.setForeground(_fgColor);
      link.setBackground(_bgColor);

      return link;
   }

   public Composite createUI_NoData(final Composite parent) {

      final Display display = parent.getDisplay();

      final Color bgColor = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
      final Color fgColor = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);

      /*
       * shell container is necessary because the margins of the inner container will hide the
       * tooltip when the mouse is hovered, which is not as it should be.
       */
      final Composite shellContainer = new Composite(parent, SWT.NONE);
      shellContainer.setForeground(fgColor);
      shellContainer.setBackground(bgColor);
      GridLayoutFactory.fillDefaults().applyTo(shellContainer);
      {

         final Composite container = new Composite(shellContainer, SWT.NONE);
         container.setForeground(fgColor);
         container.setBackground(bgColor);
         GridLayoutFactory.fillDefaults()
               .margins(SHELL_MARGIN, SHELL_MARGIN)
               .applyTo(container);
         {
            final Label label = new Label(container, SWT.NONE);
            label.setText(_noTourTooltip);
            label.setForeground(fgColor);
            label.setBackground(bgColor);
         }
      }

      return shellContainer;
   }

   private Label createUI_Spacer(final Composite container) {

      // spacer
      final Label label = createUI_Label(container, null);
      GridDataFactory.fillDefaults().span(3, 1).applyTo(label);

      return label;
   }

   public void dispose() {

   }

   private void enableControls() {

      if (_isActionsVisible == false) {
         return;
      }

      final boolean isTourSaved = _tourData.isTourSaved();

      _actionEditQuick.setEnabled(isTourSaved);
      _actionEditTour.setEnabled(true);
   }

   private void fillUI() {

      if (_comboUIWidth_Size != null && _comboUIWidth_Size.isDisposed() == false) {

         _comboUIWidth_Size.add(OtherMessages.APP_SIZE_SMALL);
         _comboUIWidth_Size.add(OtherMessages.APP_SIZE_MEDIUM);
         _comboUIWidth_Size.add(OtherMessages.APP_SIZE_LARGE);
      }
   }

   private int getSelectedUIWidthSizeIndex() {

      final int selectionIndex = _comboUIWidth_Size.getSelectionIndex();

      return selectionIndex < 0
            ? 0
            : selectionIndex;
   }

   private int getUIWidthFromParent(final Composite parent) {

      return parent.getBounds().width

            // needs an offset otherwise it would grow endlessly
            - (UI.IS_4K_DISPLAY ? 60 : 30);
   }

   private int getWindSpeedTextIndex(final int speed) {

      final int[] unitValueWindSpeed = IWeather.getAllWindSpeeds();

      // set speed to max index value
      int speedValueIndex = unitValueWindSpeed.length - 1;

      for (int speedIndex = 0; speedIndex < unitValueWindSpeed.length; speedIndex++) {

         final int speedMaxValue = unitValueWindSpeed[speedIndex];

         if (speed <= speedMaxValue) {
            speedValueIndex = speedIndex;
            break;
         }
      }

      return speedValueIndex;
   }

   private void initUI(final Composite parent) {

      _pc = new PixelConverter(parent);

// DISABLED because this UI is opened in different tooltips

//      _keepOpenListener = new FocusListener() {
//
//         @Override
//         public void focusGained(final FocusEvent e) {
//
//            /*
//             * This will fix the problem that when the list of a combobox is displayed, then the
//             * slideout will disappear :-(((
//             */
//
////            final Shell shell = _parent.getShell();
//
////            if (shell instanceof AdvancedSlideoutShell) {
////               final AdvancedSlideoutShell new_name = (AdvancedSlideoutShell) shell;
////
////            }
//
////            _tooltipShell.setIsAnotherDialogOpened(true);
//         }
//
//         @Override
//         public void focusLost(final FocusEvent e) {
////            _tooltipShell.setIsAnotherDialogOpened(false);
//         }
//      };

      _descriptionScroll_Height = _pc.convertHeightInCharsToPixels(_descriptionScroll_Lines);

      if (_isUIEmbedded) {

         parent.addControlListener(ControlListener.controlResizedAdapter(controlEvent -> {

            _uiWidth_Pixel = getUIWidthFromParent(parent);

            updateUI();
         }));
      }
   }

   private boolean isSimpleTour() {

      final long elapsedTime = _tourData.getTourDeviceTime_Elapsed();

      final boolean isShortTour = elapsedTime < UI.DAY_IN_SECONDS;
      final boolean isSingleTour = !_tourData.isMultipleTours();

      return isShortTour || isSingleTour;
   }

   /**
    * Show tour in battery SoC statistic
    */
   private void onSelect_Battery() {

      Util.showView(StatisticView.ID, false);

      TourManager.fireEventWithCustomData(

            TourEventId.SELECTION_RECORDING_DEVICE_BATTERY,
            new SelectionRecordingDeviceBattery(_tourData.getTourId(), _tourData.getStartYear()),
            null);
   }

   /**
    * Show sensor in the sensor chart, e.g. to visualize the voltage or level over time
    *
    * @param selectionEvent
    */
   private void onSelect_Sensor(final SelectionEvent selectionEvent) {

      final Object linkData = selectionEvent.widget.getData();
      if (linkData instanceof final DeviceSensor deviceSensor) {

         Util.showView(SensorChartView.ID, false);

         TourManager.fireEventWithCustomData(

               TourEventId.SELECTION_SENSOR,
               new SelectionSensor(deviceSensor, _tourData.getTourId()),
               _part);
      }
   }

   private void onSelect_UIWidth_1_Size() {

      final int selectedUIWidthSizeIndex = getSelectedUIWidthSizeIndex();

      // save selected size
      _state.put(STATE_UI_WIDTH_SIZE_INDEX, selectedUIWidthSizeIndex);

      // set size from state
      switch (getSelectedUIWidthSizeIndex()) {
      case 1 -> _uiWidth_Pixel = Util.getStateInt(_state, STATE_UI_WIDTH_MEDIUM, STATE_UI_WIDTH_MEDIUM_DEFAULT);
      case 2 -> _uiWidth_Pixel = Util.getStateInt(_state, STATE_UI_WIDTH_LARGE, STATE_UI_WIDTH_LARGE_DEFAULT);
      default -> _uiWidth_Pixel = Util.getStateInt(_state, STATE_UI_WIDTH_SMALL, STATE_UI_WIDTH_SMALL_DEFAULT);
      }

      // update UI
      _spinnerUIWidth_Pixel.setSelection(_uiWidth_Pixel);

      onSelect_UIWidth_9_UpdateUI();
   }

   private void onSelect_UIWidth_2_Value() {

      // get width
      _uiWidth_Pixel = _spinnerUIWidth_Pixel.getSelection();

      // save state for the selected size
      switch (getSelectedUIWidthSizeIndex()) {
      case 1 -> _state.put(STATE_UI_WIDTH_MEDIUM, _uiWidth_Pixel);
      case 2 -> _state.put(STATE_UI_WIDTH_LARGE, _uiWidth_Pixel);
      default -> _state.put(STATE_UI_WIDTH_SMALL, _uiWidth_Pixel);
      }

      // update UI
      onSelect_UIWidth_9_UpdateUI();
   }

   private void onSelect_UIWidth_9_UpdateUI() {

      updateUI();
      updateUI_Layout();

      final Shell parentShell = _parent.getShell();
      final Shell appShell = TourbookPlugin.getAppShell();

      if (parentShell == appShell) {

         _parent.layout(true, true);

      } else {

         // tour info is within a tooltip

         parentShell.pack(true);
      }
   }

   private void restoreState() {

      if (_spinnerUIWidth_Pixel != null && _spinnerUIWidth_Pixel.isDisposed() == false) {

         _spinnerUIWidth_Pixel.setSelection(_uiWidth_Pixel);

         _comboUIWidth_Size.select(_uiWidth_SizeIndex);
      }
   }

   private void restoreState_BeforeUI(final Composite parent) {

      if (_isUIEmbedded) {

         _uiWidth_Pixel = getUIWidthFromParent(parent);

      } else {

         _uiWidth_SizeIndex = Util.getStateInt(_state, STATE_UI_WIDTH_SIZE_INDEX, 0);

         switch (_uiWidth_SizeIndex) {

         case 1 -> _uiWidth_Pixel = Util.getStateInt(_state,
               STATE_UI_WIDTH_MEDIUM,
               STATE_UI_WIDTH_MEDIUM_DEFAULT,
               STATE_UI_WIDTH_MIN,
               STATE_UI_WIDTH_MAX);

         case 2 -> _uiWidth_Pixel = Util.getStateInt(_state,
               STATE_UI_WIDTH_LARGE,
               STATE_UI_WIDTH_LARGE_DEFAULT,
               STATE_UI_WIDTH_MIN,
               STATE_UI_WIDTH_MAX);

         default -> _uiWidth_Pixel = Util.getStateInt(_state,
               STATE_UI_WIDTH_SMALL,
               STATE_UI_WIDTH_SMALL_DEFAULT,
               STATE_UI_WIDTH_MIN,
               STATE_UI_WIDTH_MAX);
         }

      }
   }

   /**
    * Enable/disable tour edit actions, actions are disabled by default
    *
    * @param isEnabled
    */
   public void setActionsEnabled(final boolean isEnabled) {
      _isActionsVisible = isEnabled;
   }

   /**
    * @param isUIEmbedded
    *           When <code>true</code> then the tour info is embedded in a view and do not need the
    *           toolbar
    *           to close the tooltip.
    */
   public void setIsUIEmbedded(final boolean isUIEmbedded) {
      _isUIEmbedded = isUIEmbedded;
   }

   /**
    * Set text for the tooltip which is displayed when a tour is not hovered.
    *
    * @param noTourTooltip
    */
   public void setNoTourTooltip(final String noTourTooltip) {
      _noTourTooltip = noTourTooltip;
   }

   public void setPart(final IWorkbenchPart part) {
      _part = part;
   }

   private void updateUI() {

      /*
       * Upper/lower part
       */
      if (_lblTourType_Image != null && _lblTourType_Image.isDisposed() == false) {
         _lblTourType_Image.setToolTipText(_uiTourTypeName);
         net.tourbook.ui.UI.updateUI_TourType(_tourData, _lblTourType_Image, false);
      }

      String tourTitle = _tourData.getTourTitle();
      if (tourTitle == null || tourTitle.trim().length() == 0) {

         if (_uiTourTypeName == null) {
            tourTitle = Messages.Tour_Tooltip_Label_DefaultTitle;
         } else {
            tourTitle = _uiTourTypeName;
         }
      }
      _lblTitle.setText(tourTitle);

      /*
       * Lower part container contains sensor values, weather, tour type, tags and description
       */
      UI.showHideControl(_lowerPartContainer,

            _hasSensorValues
                  || _hasWeatherDescription
                  || _hasTourType
                  || _hasTags
                  || _hasTourDescription
                  || _hasLocationStart
                  || _hasLocationEnd);

      /*
       * Weather description
       */
      if (_hasWeatherDescription) {
         _txtWeather.setText(_tourData.getWeather());
      }
      UI.showHideControl(_lblWeather, _hasWeatherDescription);
      UI.showHideControl(_txtWeather, _hasWeatherDescription, _uiWidth_Pixel);

      /*
       * Tour type
       */
      if (_hasTourType) {
         _lblTourType_Value.setText(_tourData.getTourType().getName());
      }
      UI.showHideControl(_lblTourType, _hasTourType);
      UI.showHideControl(_lblTourType_Value, _hasTourType);

      /*
       * Tags
       */
      if (_hasTags) {
         TagManager.updateUI_Tags(_tourData, _lblTourTags_Value, true);
      }
      UI.showHideControl(_lblTourTags, _hasTags);
      UI.showHideControl(_lblTourTags_Value, _hasTags);

      /*
       * Tour description
       */
      if (_hasTourDescription) {
         _txtDescription.setText(_tourData.getTourDescription());
      }
      UI.showHideControl(_lblDescription, _hasTourDescription);

      if (_descriptionLineCount > _descriptionScroll_Lines) {
         // show with vertical scrollbar
         UI.showHideControl(_txtDescription, _hasTourDescription, _uiWidth_Pixel, _descriptionScroll_Height);
      } else {
         // vertical scrollbar is not necessary
         UI.showHideControl(_txtDescription, _hasTourDescription, _uiWidth_Pixel);
      }

      /*
       * Start/end location
       */
      if (_hasLocationStart) {
         _txtLocationStart.setText(_tourData.getTourStartPlace());
      }
      UI.showHideControl(_lblLocationStart, _hasLocationStart);
      UI.showHideControl(_txtLocationStart, _hasLocationStart, _uiWidth_Pixel);

      if (_hasLocationEnd) {
         _txtLocationEnd.setText(_tourData.getTourEndPlace());
      }
      UI.showHideControl(_lblLocationEnd, _hasLocationEnd);
      UI.showHideControl(_txtLocationEnd, _hasLocationEnd, _uiWidth_Pixel);

      /*
       * Column: Left
       */
      final long elapsedTime = _tourData.getTourDeviceTime_Elapsed();
      final long recordedTime = _tourData.getTourDeviceTime_Recorded();
      final long pausedTime = _tourData.getTourDeviceTime_Paused();
      final long movingTime = _tourData.getTourComputedTime_Moving();
      final long breakTime = elapsedTime - movingTime;

      final ZonedDateTime zdtTourStart = _tourData.getTourStartTime();
      final ZonedDateTime zdtTourEnd = zdtTourStart.plusSeconds(elapsedTime);

      if (isSimpleTour()) {

         // < 1 day

         _lblDate.setText(String.format(
               Messages.Tour_Tooltip_Format_DateWeekTime,
               zdtTourStart.format(TimeTools.Formatter_Date_F),
               zdtTourStart.format(TimeTools.Formatter_Time_M),
               zdtTourEnd.format(TimeTools.Formatter_Time_M),
               zdtTourStart.get(TimeTools.calendarWeek.weekOfWeekBasedYear())

         ));

         // show units only when data are available
         _lblElapsedTime_Unit.setVisible(elapsedTime > 0);
         _lblRecordedTime_Unit.setVisible(recordedTime > 0);
         _lblPausedTime_Unit.setVisible(pausedTime > 0);
         _lblMovingTime_Unit.setVisible(movingTime > 0);
         _lblBreakTime_Unit.setVisible(breakTime > 0);

         _lblElapsedTime.setText(FormatManager.formatElapsedTime(elapsedTime));
         _lblRecordedTime.setText(FormatManager.formatRecordedTime(recordedTime));
         _lblPausedTime.setText(FormatManager.formatPausedTime(pausedTime));
         _lblMovingTime.setText(FormatManager.formatMovingTime(movingTime));
         _lblBreakTime.setText(FormatManager.formatBreakTime(breakTime));

         /*
          * Time zone
          */
         final String tourTimeZoneId = _tourData.getTimeZoneId();
         final TourDateTime tourDateTime = _tourData.getTourDateTime();
         _lblTimeZone_Value.setText(tourTimeZoneId == null ? UI.EMPTY_STRING : tourTimeZoneId);
         _lblTimeZoneDifference_Value.setText(tourDateTime.timeZoneOffsetLabel);

         // set tooltip text
         final String defaultTimeZoneId = _prefStoreCommon.getString(ICommonPreferences.TIME_ZONE_LOCAL_ID);
         final String timeZoneTooltip = NLS.bind(
               Messages.ColumnFactory_TimeZoneDifference_Tooltip,
               defaultTimeZoneId);

         _lblTimeZoneDifference.setToolTipText(timeZoneTooltip);
         _lblTimeZoneDifference_Value.setToolTipText(timeZoneTooltip);

      } else {

         // > 1 day

         _lblDate.setText(String.format(
               Messages.Tour_Tooltip_Format_HistoryDateTime,
               zdtTourStart.format(_dtHistoryFormatter),
               zdtTourEnd.format(_dtHistoryFormatter)));

         // hide labels, they are displayed with the period values
         _lblElapsedTime_Unit.setVisible(false);
         _lblRecordedTime_Unit.setVisible(false);
         _lblPausedTime_Unit.setVisible(false);
         _lblMovingTime_Unit.setVisible(false);
         _lblBreakTime_Unit.setVisible(false);

         final Period elapsedPeriod = new Period(
               _tourData.getTourStartTimeMS(),
               _tourData.getTourEndTimeMS(),
               _tourPeriodTemplate);
         final Period recordedPeriod = new Period(0, recordedTime * 1000, _tourPeriodTemplate);
         final Period pausedPeriod = new Period(0, pausedTime * 1000, _tourPeriodTemplate);
         final Period movingPeriod = new Period(0, movingTime * 1000, _tourPeriodTemplate);
         final Period breakPeriod = new Period(0, breakTime * 1000, _tourPeriodTemplate);

         _lblElapsedTime.setText(elapsedPeriod.toString(UI.DEFAULT_DURATION_FORMATTER_SHORT));
         _lblRecordedTime.setText(recordedPeriod.toString(UI.DEFAULT_DURATION_FORMATTER_SHORT));
         _lblPausedTime.setText(pausedPeriod.toString(UI.DEFAULT_DURATION_FORMATTER_SHORT));
         _lblMovingTime.setText(movingPeriod.toString(UI.DEFAULT_DURATION_FORMATTER_SHORT));
         _lblBreakTime.setText(breakPeriod.toString(UI.DEFAULT_DURATION_FORMATTER_SHORT));
      }

      /*
       * Weather
       */
      final int windSpeed = (int) (_tourData.getWeather_Wind_Speed() / UI.UNIT_VALUE_DISTANCE);
      final int weatherWindDirectionDegree = _tourData.getWeather_Wind_Direction();
      if (windSpeed > 0 && weatherWindDirectionDegree != -1) {

         // Wind speed
         _lblWindSpeed.setText(Integer.toString(windSpeed));
         _lblWindSpeedUnit.setText(
               String.format(
                     Messages.Tour_Tooltip_Format_WindSpeedUnit,
                     UI.UNIT_LABEL_SPEED,
                     IWeather.windSpeedTextShort[getWindSpeedTextIndex(windSpeed)]));

         // Wind direction
         _lblWindDirection.setText(Integer.toString(weatherWindDirectionDegree));
         _lblWindDirectionUnit.setText(String.format(
               Messages.Tour_Tooltip_Format_WindDirectionUnit,
               UI.getCardinalDirectionText(weatherWindDirectionDegree)));
      }

      /*
       * Air Quality
       */
      final int airQualityTextIndex = _tourData.getWeather_AirQuality_TextIndex();
      if (airQualityTextIndex > 0) {

         _lblAirQuality.setText(UI.SPACE + IWeather.AIR_QUALITY_TEXT[airQualityTextIndex] + UI.SPACE);

         final int colorIndex = airQualityTextIndex * 2;

         // run async otherwise in the dark mode the colors are not displayed
         _parent.getDisplay().asyncExec(() -> {

            if (_parent.isDisposed()) {
               return;
            }

            if (UI.IS_DARK_THEME) {

               _lblAirQuality.setForeground(IWeather.AIR_QUALITY_COLORS_DARK_THEME[colorIndex]);
               _lblAirQuality.setBackground(IWeather.AIR_QUALITY_COLORS_DARK_THEME[colorIndex + 1]);

            } else {

               _lblAirQuality.setForeground(IWeather.AIR_QUALITY_COLORS_BRIGHT_THEME[colorIndex]);
               _lblAirQuality.setBackground(IWeather.AIR_QUALITY_COLORS_BRIGHT_THEME[colorIndex + 1]);
            }
         });
      }

      /*
       * Average temperature
       */
      final float temperature_NoDevice = _tourData.getWeather_Temperature_Average();
      final float temperature_FromDevice = _tourData.getWeather_Temperature_Average_Device();

      final float convertedTemperature_NoDevice = UI.convertTemperatureFromMetric(temperature_NoDevice);
      final float convertedTemperature_FromDevice = UI.convertTemperatureFromMetric(temperature_FromDevice);

      final String formattedTemperature_NoDevice = _tourData.isMultipleTours()
            ? FormatManager.formatTemperature_Summary(convertedTemperature_NoDevice)
            : FormatManager.formatTemperature(convertedTemperature_NoDevice);

      final String formattedTemperature_FromDevice = _tourData.isMultipleTours()
            ? FormatManager.formatTemperature_Summary(convertedTemperature_FromDevice)
            : FormatManager.formatTemperature(convertedTemperature_FromDevice);

      final boolean isTemperature_NoDevice = temperature_NoDevice > 0 || _tourData.isWeatherDataFromProvider();
      final boolean isTemperature_FromDevice = _tourData.temperatureSerie != null && _tourData.temperatureSerie.length > 0;

      String part1Text = UI.EMPTY_STRING;
      String part2Text = UI.EMPTY_STRING;
      String part1Tooltip = UI.EMPTY_STRING;
      String part2Tooltip = UI.EMPTY_STRING;

      if (isTemperature_NoDevice && isTemperature_FromDevice) {

         // both values are available

         part1Text = formattedTemperature_NoDevice + UI.SPACE + UI.UNIT_LABEL_TEMPERATURE;
         part2Text = formattedTemperature_FromDevice + UI.SPACE + UI.UNIT_LABEL_TEMPERATURE;

         part1Tooltip = Messages.Tour_Tooltip_Label_AvgTemperature_NoDevice;
         part2Tooltip = Messages.Tour_Tooltip_Label_AvgTemperature_FromDevice;

      } else if (isTemperature_NoDevice) {

         // values only from provider or manual

         part1Text = formattedTemperature_NoDevice;
         part2Text = UI.UNIT_LABEL_TEMPERATURE;

         part1Tooltip = Messages.Tour_Tooltip_Label_AvgTemperature_NoDevice;
         part2Tooltip = Messages.Tour_Tooltip_Label_AvgTemperature_NoDevice;

      } else if (isTemperature_FromDevice) {

         // values only from device

         part1Text = formattedTemperature_FromDevice;
         part2Text = UI.UNIT_LABEL_TEMPERATURE;

         part1Tooltip = Messages.Tour_Tooltip_Label_AvgTemperature_FromDevice;
         part2Tooltip = Messages.Tour_Tooltip_Label_AvgTemperature_FromDevice;
      }

      _lblTemperature_Part1.setText(part1Text);
      _lblTemperature_Part1.setToolTipText(part1Tooltip);

      _lblTemperature_Part2.setText(part2Text);
      _lblTemperature_Part2.setToolTipText(part2Tooltip);

      // weather clouds
      final int weatherIndex = _tourData.getWeatherIndex();
      final String cloudText = IWeather.CLOUD_TEXT[weatherIndex];
      final String cloudImageName = IWeather.CLOUD_ICON[weatherIndex];

      _lblClouds.setImage(UI.IMAGE_REGISTRY.get(cloudImageName));
      _lblCloudsUnit.setText(cloudText.equals(IWeather.cloudIsNotDefined) ? UI.EMPTY_STRING : cloudText);

// SET_FORMATTING_OFF

      /*
       * Column: Distance/elevation
       */
      final float distance = _tourData.getTourDistance() / UI.UNIT_VALUE_DISTANCE;

      _lblDistance               .setText(FormatManager.formatDistance(distance / 1000.0));
      _lblDistance_Unit          .setText(UI.UNIT_LABEL_DISTANCE);

      _lblElevationUp            .setText(Integer.toString((int) (_tourData.getTourAltUp() / UI.UNIT_VALUE_ELEVATION)));
      _lblElevationUp_Unit       .setText(UI.UNIT_LABEL_ELEVATION);

      _lblElevationDown          .setText(Integer.toString(-(int) (_tourData.getTourAltDown() / UI.UNIT_VALUE_ELEVATION)));
      _lblElevationDown_Unit     .setText(UI.UNIT_LABEL_ELEVATION);

      final int averageElevationChange = Math.round(UI.convertAverageElevationChangeFromMetric(_tourData.getAvgAltitudeChange()));
      _lblAvgElevationChange     .setText(Integer.toString(averageElevationChange));
      _lblAvgElevationChange_Unit.setText(UI.UNIT_LABEL_ELEVATION + UI.SLASH + UI.UNIT_LABEL_DISTANCE);

      // ensure that data are available
      _tourData.computeVerticalSpeed();

      final int   vertSpeed_TimeFlat      = _tourData.verticalSpeed_Flat_Time;
      final int   vertSpeed_TimeGain      = _tourData.verticalSpeed_Up_Time;
      final int   vertSpeed_TimeLoss      = _tourData.verticalSpeed_Down_Time;

      final float vertSpeed_DistanceFlat  = _tourData.verticalSpeed_Flat_Distance;
      final float vertSpeed_DistanceGain  = _tourData.verticalSpeed_Up_Distance;
      final float vertSpeed_DistanceLoss  = _tourData.verticalSpeed_Down_Distance;

      final float vertSpeed_ElevationGain = _tourData.verticalSpeed_Up_Elevation;
      final float vertSpeed_ElevationLoss = _tourData.verticalSpeed_Down_Elevation;

      final float verticalSpeed_Flat = vertSpeed_TimeFlat == 0 ? 0 : 3.6f * vertSpeed_DistanceFlat / vertSpeed_TimeFlat;
      final float verticalSpeed_Gain = vertSpeed_TimeGain == 0 ? 0 : 3.6f * vertSpeed_DistanceGain / vertSpeed_TimeGain;
      final float verticalSpeed_Loss = vertSpeed_TimeLoss == 0 ? 0 : 3.6f * vertSpeed_DistanceLoss / vertSpeed_TimeLoss;

      final float sumTime     = vertSpeed_TimeFlat       + vertSpeed_TimeGain       + vertSpeed_TimeLoss *1f;
      final float sumDistance = vertSpeed_DistanceFlat   + vertSpeed_DistanceGain   + vertSpeed_DistanceLoss;

      final float altimeter_Gain = vertSpeed_ElevationGain / vertSpeed_TimeGain * 3600;
      final float altimeter_Loss = vertSpeed_ElevationLoss / vertSpeed_TimeLoss * 3600;

      final float prefFlatGainLoss_DPTolerance  = _prefStore.getFloat(ITourbookPreferences.FLAT_GAIN_LOSS_DP_TOLERANCE);
      final float prefFlatGainLoss_FlatGradient = _prefStore.getFloat(ITourbookPreferences.FLAT_GAIN_LOSS_FLAT_GRADIENT);

      final String tooltip = Messages.Tour_Tooltip_FlatGainLoss_Tooltip.formatted(
            _nf2.format(prefFlatGainLoss_DPTolerance),
            _nf1.format(prefFlatGainLoss_FlatGradient));

      _lblVerticalSpeed_Time_Header             .setText(UI.UNIT_LABEL_TIME);
      _lblVerticalSpeed_Time_Flat               .setText(FormatManager.formatMovingTime(vertSpeed_TimeFlat, false, true));
      _lblVerticalSpeed_Time_Gain               .setText(FormatManager.formatMovingTime(vertSpeed_TimeGain, false, true));
      _lblVerticalSpeed_Time_Loss               .setText(FormatManager.formatMovingTime(vertSpeed_TimeLoss, false, true));

      _lblVerticalSpeed_Time_Relative_Flat      .setText(FormatManager.formatRelative((double)vertSpeed_TimeFlat / sumTime * 100f));
      _lblVerticalSpeed_Time_Relative_Gain      .setText(FormatManager.formatRelative((double)vertSpeed_TimeGain / sumTime * 100f));
      _lblVerticalSpeed_Time_Relative_Loss      .setText(FormatManager.formatRelative((double)vertSpeed_TimeLoss / sumTime * 100f));

      _lblVerticalSpeed_Distance_Header         .setText(UI.UNIT_LABEL_DISTANCE);
      _lblVerticalSpeed_Distance_Flat           .setText(FormatManager.formatDistance(vertSpeed_DistanceFlat / 1000 / UI.UNIT_VALUE_DISTANCE));
      _lblVerticalSpeed_Distance_Gain           .setText(FormatManager.formatDistance(vertSpeed_DistanceGain / 1000 / UI.UNIT_VALUE_DISTANCE));
      _lblVerticalSpeed_Distance_Loss           .setText(FormatManager.formatDistance(vertSpeed_DistanceLoss / 1000 / UI.UNIT_VALUE_DISTANCE));

      _lblVerticalSpeed_Distance_Relative_Flat  .setText(FormatManager.formatRelative(vertSpeed_DistanceFlat / sumDistance * 100));
      _lblVerticalSpeed_Distance_Relative_Gain  .setText(FormatManager.formatRelative(vertSpeed_DistanceGain / sumDistance * 100));
      _lblVerticalSpeed_Distance_Relative_Loss  .setText(FormatManager.formatRelative(vertSpeed_DistanceLoss / sumDistance * 100));

      _lblVerticalSpeed_Elevation_Header        .setText(UI.UNIT_LABEL_ELEVATION);
      _lblVerticalSpeed_Elevation_Gain          .setText(FormatManager.formatElevation(vertSpeed_ElevationGain / UI.UNIT_VALUE_ELEVATION));
      _lblVerticalSpeed_Elevation_Loss          .setText(FormatManager.formatElevation(-vertSpeed_ElevationLoss / UI.UNIT_VALUE_ELEVATION));

      _lblVerticalSpeed_Speed_Header            .setText(UI.UNIT_LABEL_SPEED);
      _lblVerticalSpeed_Speed_Flat              .setText(FormatManager.formatSpeed(verticalSpeed_Flat / UI.UNIT_VALUE_DISTANCE));
      _lblVerticalSpeed_Speed_Gain              .setText(FormatManager.formatSpeed(verticalSpeed_Gain / UI.UNIT_VALUE_DISTANCE));
      _lblVerticalSpeed_Speed_Loss              .setText(FormatManager.formatSpeed(verticalSpeed_Loss / UI.UNIT_VALUE_DISTANCE));

      _lblVerticalSpeed_Time_Header             .setToolTipText(tooltip);
      _lblVerticalSpeed_Time_Flat               .setToolTipText(tooltip);
      _lblVerticalSpeed_Time_Gain               .setToolTipText(tooltip);
      _lblVerticalSpeed_Time_Loss               .setToolTipText(tooltip);

      _lblVerticalSpeed_Time_Relative_Flat      .setToolTipText(tooltip);
      _lblVerticalSpeed_Time_Relative_Gain      .setToolTipText(tooltip);
      _lblVerticalSpeed_Time_Relative_Loss      .setToolTipText(tooltip);

      _lblVerticalSpeed_Distance_Header         .setToolTipText(tooltip);
      _lblVerticalSpeed_Distance_Flat           .setToolTipText(tooltip);
      _lblVerticalSpeed_Distance_Gain           .setToolTipText(tooltip);
      _lblVerticalSpeed_Distance_Loss           .setToolTipText(tooltip);

      _lblVerticalSpeed_Distance_Relative_Flat  .setToolTipText(tooltip);
      _lblVerticalSpeed_Distance_Relative_Gain  .setToolTipText(tooltip);
      _lblVerticalSpeed_Distance_Relative_Loss  .setToolTipText(tooltip);

      _lblVerticalSpeed_Elevation_Header        .setToolTipText(tooltip);
      _lblVerticalSpeed_Elevation_Gain          .setToolTipText(tooltip);
      _lblVerticalSpeed_Elevation_Loss          .setToolTipText(tooltip);

      _lblVerticalSpeed_Speed_Header            .setToolTipText(tooltip);
      _lblVerticalSpeed_Speed_Flat              .setToolTipText(tooltip);
      _lblVerticalSpeed_Speed_Gain              .setToolTipText(tooltip);
      _lblVerticalSpeed_Speed_Loss              .setToolTipText(tooltip);

      _lblAltimeter_Up        .setText(Integer.toString((int) (altimeter_Gain + .5)));
      _lblAltimeter_Down      .setText(Integer.toString((int) (altimeter_Loss + .5)));

      _lblAltimeter_Up_Unit   .setText(UI.UNIT_LABEL_ELEVATION + Messages.ColumnFactory_hour);
      _lblAltimeter_Down_Unit .setText(UI.UNIT_LABEL_ELEVATION + Messages.ColumnFactory_hour);

// SET_FORMATTING_ON

      // SET_FORMATTING_ON

      /*
       * Column: Right
       */
      final boolean isPaceAndSpeedFromRecordedTime = _prefStore.getBoolean(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME);
      final long time = isPaceAndSpeedFromRecordedTime ? recordedTime : movingTime;
      final float avgSpeed = time == 0 ? 0 : 3.6f * distance / time;
      _lblAvgSpeed.setText(FormatManager.formatSpeed(avgSpeed));
      _lblAvgSpeedUnit.setText(UI.UNIT_LABEL_SPEED);

      final int pace = (int) (distance == 0 ? 0 : (time * 1000 / distance));
      _lblAvgPace.setText(String.format(Messages.Tour_Tooltip_Format_Pace,
            pace / 60,
            pace % 60));
      _lblAvgPaceUnit.setText(UI.UNIT_LABEL_PACE);

      // avg pulse
      final double avgPulse = _tourData.getAvgPulse();
      _lblAvgPulse.setText(FormatManager.formatPulse(avgPulse));
      _lblAvgPulseUnit.setText(Messages.Value_Unit_Pulse);

      // avg cadence
      final double avgCadence = _tourData.getAvgCadence() * _tourData.getCadenceMultiplier();
      _lblAvgCadence.setText(FormatManager.formatCadence(avgCadence));
      _lblAvgCadenceUnit.setText(_tourData.isCadenceSpm()
            ? Messages.Value_Unit_Cadence_Spm
            : Messages.Value_Unit_Cadence);

      // avg power
      final double avgPower = _tourData.getPower_Avg();
      _lblAvg_Power.setText(FormatManager.formatPower(avgPower));
      _lblAvg_PowerUnit.setText(UI.UNIT_POWER);

      // Average elevation
      final double avgElevation = _tourData.getElevation_Avg();
      _lblAvgElevation.setText(Integer.toString((int) avgElevation));
      _lblAvgElevationUnit.setText(UI.UNIT_LABEL_ELEVATION);

      // calories
      final double calories = _tourData.getCalories();
      _lblCalories.setText(FormatManager.formatNumber_0(calories / 1000));

      // body
      final float bodyWeight = UI.convertBodyWeightFromMetric(_tourData.getBodyWeight());
      _lblRestPulse.setText(Integer.toString(_tourData.getRestPulse()));
      _lblBodyWeight.setText(_nf1.format(bodyWeight));

// SET_FORMATTING_OFF

      /*
       * Max values
       */
      _lblMaxElevation        .setText(Integer.toString((int) (_tourData.getMaxAltitude() / UI.UNIT_VALUE_ELEVATION)));
      _lblMaxElevation_Unit   .setText(UI.UNIT_LABEL_ELEVATION);

      _lblMaxPulse            .setText(FormatManager.formatPulse(_tourData.getMaxPulse()));
      _lblMaxPulse_Unit       .setText(Messages.Value_Unit_Pulse);

      _lblMaxPace             .setText(UI.format_mm_ss((long) (_tourData.getMaxPace() * UI.UNIT_VALUE_DISTANCE)));
      _lblMaxPace_Unit        .setText(UI.UNIT_LABEL_PACE);

      _lblMaxSpeed            .setText(FormatManager.formatSpeed(_tourData.getMaxSpeed() / UI.UNIT_VALUE_DISTANCE));
      _lblMaxSpeed_Unit       .setText(UI.UNIT_LABEL_SPEED);

      /*
       * Gears
       */
      if (_hasGears) {

         _lblGear_GearShifts.setText(String.format(GEAR_SHIFT_FORMAT,
               _tourData.getFrontShiftCount(),
               _tourData.getRearShiftCount()));
      }

      UI.showHideControl(_lblGear_Spacer,             _hasGears);
      UI.showHideControl(_lblGear,                    _hasGears);
      UI.showHideControl(_lblGear_GearShifts,         _hasGears);
      UI.showHideControl(_lblGear_GearShifts_Spacer,  _hasGears);

      /*
       * Battery
       */
      if (_hasRecordingDeviceBattery) {
         _lblBattery_Start.setText(Short.toString(_tourData.getBattery_Percentage_Start()));
         _lblBattery_End.setText(String.format(BATTERY_FORMAT, _tourData.getBattery_Percentage_End()));
      }

      UI.showHideControl(_linkBattery,       _hasRecordingDeviceBattery);
      UI.showHideControl(_lblBattery_Spacer, _hasRecordingDeviceBattery);
      UI.showHideControl(_lblBattery_Start,  _hasRecordingDeviceBattery);
      UI.showHideControl(_lblBattery_End,    _hasRecordingDeviceBattery);

// SET_FORMATTING_ON

      /*
       * Sensor
       */
      updateUI_SensorValues();

      /*
       * Date/time
       */

      // date/time created
      if (_uiDtCreated != null) {

         _lblDateTimeCreatedValue.setText(_uiDtCreated == null
               ? UI.EMPTY_STRING
               : _uiDtCreated.format(TimeTools.Formatter_DateTime_M));
      }

      // date/time modified
      if (_uiDtModified != null) {

         _lblDateTimeModifiedValue.setText(_uiDtModified == null
               ? UI.EMPTY_STRING
               : _uiDtModified.format(TimeTools.Formatter_DateTime_M));
      }

      /*
       * Running Dynamics
       */
      if (_hasRunDyn) {

         final float mmOrInch = UI.UNIT_VALUE_DISTANCE_MM_OR_INCH;

         _lblRunDyn_StanceTime_Min.setText(Integer.toString(_tourData.getRunDyn_StanceTime_Min()));
         _lblRunDyn_StanceTime_Min_Unit.setText(UI.UNIT_MS);
         _lblRunDyn_StanceTime_Max.setText(Integer.toString(_tourData.getRunDyn_StanceTime_Max()));
         _lblRunDyn_StanceTime_Max_Unit.setText(UI.UNIT_MS);
         _lblRunDyn_StanceTime_Avg.setText(_nf0.format(_tourData.getRunDyn_StanceTime_Avg()));
         _lblRunDyn_StanceTime_Avg_Unit.setText(UI.UNIT_MS);

         _lblRunDyn_StanceTimeBalance_Min.setText(_nf1.format(_tourData.getRunDyn_StanceTimeBalance_Min()));
         _lblRunDyn_StanceTimeBalance_Min_Unit.setText(UI.SYMBOL_PERCENTAGE);
         _lblRunDyn_StanceTimeBalance_Max.setText(_nf1.format(_tourData.getRunDyn_StanceTimeBalance_Max()));
         _lblRunDyn_StanceTimeBalance_Max_Unit.setText(UI.SYMBOL_PERCENTAGE);
         _lblRunDyn_StanceTimeBalance_Avg.setText(_nf1.format(_tourData.getRunDyn_StanceTimeBalance_Avg()));
         _lblRunDyn_StanceTimeBalance_Avg_Unit.setText(UI.SYMBOL_PERCENTAGE);

         if (UI.UNIT_IS_DISTANCE_KILOMETER) {

            _lblRunDyn_StepLength_Min.setText(_nf0.format(_tourData.getRunDyn_StepLength_Min() * mmOrInch));
            _lblRunDyn_StepLength_Max.setText(_nf0.format(_tourData.getRunDyn_StepLength_Max() * mmOrInch));
            _lblRunDyn_StepLength_Avg.setText(_nf0.format(_tourData.getRunDyn_StepLength_Avg() * mmOrInch));

            _lblRunDyn_VerticalOscillation_Min.setText(_nf0.format(_tourData.getRunDyn_VerticalOscillation_Min() * mmOrInch));
            _lblRunDyn_VerticalOscillation_Max.setText(_nf0.format(_tourData.getRunDyn_VerticalOscillation_Max() * mmOrInch));
            _lblRunDyn_VerticalOscillation_Avg.setText(_nf0.format(_tourData.getRunDyn_VerticalOscillation_Avg() * mmOrInch));

         } else {

            // imperial has 1 more digit

            _lblRunDyn_StepLength_Min.setText(_nf1.format(_tourData.getRunDyn_StepLength_Min() * mmOrInch));
            _lblRunDyn_StepLength_Max.setText(_nf1.format(_tourData.getRunDyn_StepLength_Max() * mmOrInch));
            _lblRunDyn_StepLength_Avg.setText(_nf1.format(_tourData.getRunDyn_StepLength_Avg() * mmOrInch));

            _lblRunDyn_VerticalOscillation_Min.setText(_nf1.format(_tourData.getRunDyn_VerticalOscillation_Min() * mmOrInch));
            _lblRunDyn_VerticalOscillation_Max.setText(_nf1.format(_tourData.getRunDyn_VerticalOscillation_Max() * mmOrInch));
            _lblRunDyn_VerticalOscillation_Avg.setText(_nf1.format(_tourData.getRunDyn_VerticalOscillation_Avg() * mmOrInch));
         }

         _lblRunDyn_StepLength_Min_Unit.setText(UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);
         _lblRunDyn_StepLength_Max_Unit.setText(UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);
         _lblRunDyn_StepLength_Avg_Unit.setText(UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

         _lblRunDyn_VerticalOscillation_Min_Unit.setText(UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);
         _lblRunDyn_VerticalOscillation_Max_Unit.setText(UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);
         _lblRunDyn_VerticalOscillation_Avg_Unit.setText(UI.UNIT_LABEL_DISTANCE_MM_OR_INCH);

         _lblRunDyn_VerticalRatio_Min.setText(_nf1.format(_tourData.getRunDyn_VerticalRatio_Min()));
         _lblRunDyn_VerticalRatio_Min_Unit.setText(UI.SYMBOL_PERCENTAGE);
         _lblRunDyn_VerticalRatio_Max.setText(_nf1.format(_tourData.getRunDyn_VerticalRatio_Max()));
         _lblRunDyn_VerticalRatio_Max_Unit.setText(UI.SYMBOL_PERCENTAGE);
         _lblRunDyn_VerticalRatio_Avg.setText(_nf1.format(_tourData.getRunDyn_VerticalRatio_Avg()));
         _lblRunDyn_VerticalRatio_Avg_Unit.setText(UI.SYMBOL_PERCENTAGE);
      }
   }

   private void updateUI_Layout() {

      // compute width for all controls and equalize column width for the different sections

      _ttContainer.layout(true, true);
   }

   private void updateUI_SensorValues() {

      if (_allSensorValuesWithData == null) {
         return;
      }

      for (int sensorValueIndex = 0; sensorValueIndex < _allSensorValuesWithData.size(); sensorValueIndex++) {

         final DeviceSensorValue sensorValue = _allSensorValuesWithData.get(sensorValueIndex);

         final Label lblLevel = _allSensorValue_Level.get(sensorValueIndex);
         final Label lblStatus = _allSensorValue_Status.get(sensorValueIndex);
         final Label lblVoltage = _allSensorValue_Voltage.get(sensorValueIndex);

         final float batteryLevel_Start = sensorValue.getBatteryLevel_Start();
         final float batteryLevel_End = sensorValue.getBatteryLevel_End();
         final float batteryStatus_Start = sensorValue.getBatteryStatus_Start();
         final float batteryStatus_End = sensorValue.getBatteryStatus_End();
         final float batteryVoltage_Start = sensorValue.getBatteryVoltage_Start();
         final float batteryVoltage_End = sensorValue.getBatteryVoltage_End();

         final boolean isBatteryLevel = batteryLevel_Start != -1 || batteryLevel_End != -1;
         final boolean isBatteryStatus = batteryStatus_Start != -1 || batteryStatus_End != -1;
         final boolean isBatteryVoltage = batteryVoltage_Start != -1 || batteryVoltage_End != -1;

         if (isBatteryLevel) {

            // 77 ... 51 %

            String batteryLevel = batteryLevel_Start == batteryLevel_End

                  // don't repeat the same level
                  ? _nf0.format(batteryLevel_Start)

                  : _nf0.format(batteryLevel_Start) + UI.ELLIPSIS_WITH_SPACE + _nf0.format(batteryLevel_End);

            // add unit
            batteryLevel += UI.SPACE + UI.SYMBOL_PERCENTAGE;

            lblLevel.setText(batteryLevel);
            lblLevel.setToolTipText(Messages.Tour_Tooltip_Label_BatteryLevel_Tooltip);
         }

         if (isBatteryStatus) {

            final String statusStart_Name = BatteryStatus.getLabelFromValue((short) batteryStatus_Start);

            final String batteryStatus = batteryStatus_Start == batteryStatus_End

                  // don't repeat the same status
                  ? statusStart_Name

                  : statusStart_Name + UI.ELLIPSIS_WITH_SPACE + BatteryStatus.getLabelFromValue((short) batteryStatus_End);

            lblStatus.setText(batteryStatus);
            lblStatus.setToolTipText(Messages.Tour_Tooltip_Label_BatteryStatus_Tooltip);
         }

         if (isBatteryVoltage) {

            String batteryVoltage = batteryVoltage_Start == batteryVoltage_End

                  // don't repeat the same level
                  ? _nf2.format(batteryVoltage_Start)

                  : _nf2.format(batteryVoltage_Start) + UI.ELLIPSIS_WITH_SPACE + _nf2.format(batteryVoltage_End);

            // add unit
            batteryVoltage += UI.SPACE + UI.UNIT_VOLT;

            lblVoltage.setText(batteryVoltage);
            lblVoltage.setToolTipText(Messages.Tour_Tooltip_Label_BatteryVoltage_Tooltip);
         }

      }
   }

}
