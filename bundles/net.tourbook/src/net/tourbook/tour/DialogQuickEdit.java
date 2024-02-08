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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

import net.sf.swtaddons.autocomplete.combo.AutocompleteComboInput;
import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.OtherMessages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.tooltip.ActionToolbarSlideout;
import net.tourbook.common.tooltip.IOpeningDialog;
import net.tourbook.common.tooltip.OpenDialogManager;
import net.tourbook.common.tooltip.ToolbarSlideout;
import net.tourbook.common.util.Util;
import net.tourbook.common.weather.IWeather;
import net.tourbook.data.TourData;
import net.tourbook.data.TourLocation;
import net.tourbook.database.TourDatabase;
import net.tourbook.tour.location.ITourLocationConsumer;
import net.tourbook.tour.location.TourLocationData;
import net.tourbook.tour.location.TourLocationManager;
import net.tourbook.ui.views.tourDataEditor.TourDataEditorView;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class DialogQuickEdit extends TitleAreaDialog implements ITourLocationConsumer {

   private static final String           STATE_ID                  = "net.tourbook.tour.DialogQuickEdit";           //$NON-NLS-1$

   private static final IDialogSettings  _state                    = TourbookPlugin.getState(STATE_ID);
   private static final IDialogSettings  _state_TourDataEditorView = TourbookPlugin.getState(TourDataEditorView.ID);

   private final TourData                _tourData;
   private PixelConverter                _pc;

   /**
    * Contains the controls which are displayed in the first column, these controls are used to get
    * the maximum width and set the first column within the different section to the same width
    */
   private final ArrayList<Control>      _firstColumnControls      = new ArrayList<>();
   private final ArrayList<Control>      _secondColumnControls     = new ArrayList<>();

   private int                           _hintDefaultSpinnerWidth;
   private int                           _numLines_WeatherDescription;

   private boolean                       _isUpdateUI;
   private boolean                       _isTemperatureManuallyModified;
   private boolean                       _isWindSpeedManuallyModified;
   private int[]                         _unitValueWindSpeed;
   private float                         _unitValueDistance;

   private ActionTourLocation            _actionStartLocation;
   private ActionTourLocation            _actionEndLocation;
   private ActionSlideout_WeatherOptions _actionSlideout_WeatherOptions;

   private MouseWheelListener            _mouseWheelListener;

   private ToolBarManager                _toolbarManager_EndLocation;
   private ToolBarManager                _toolbarManager_StartLocation;
   private ToolBarManager                _toolbarManager_WeatherOptions;

   private OpenDialogManager             _openDlgMgr               = new OpenDialogManager();

   private GridDataFactory               _gridData_GrabHorizontal_CenterVertical;

   /*
    * UI controls
    */
   private Composite              _parent;
   private Composite              _tourContainer;

   private FormToolkit            _tk;
   private Form                   _formContainer;

   private CLabel                 _lblWeather_CloudIcon;

   private Combo                  _comboLocation_Start;
   private Combo                  _comboLocation_End;
   private Combo                  _comboTitle;
   private Combo                  _comboWeather_Clouds;
   private Combo                  _comboWeather_Wind_DirectionText;
   private Combo                  _comboWeather_Wind_SpeedText;

   private Spinner                _spinBodyWeight;
   private Spinner                _spinFTP;
   private Spinner                _spinRestPulse;
   private Spinner                _spinCalories;
   private Spinner                _spinWeather_Temperature_Average;
   private Spinner                _spinWeather_Wind_DirectionValue;
   private Spinner                _spinWeather_Wind_SpeedValue;

   private Text                   _txtTourDescription;
   private Text                   _txtWeatherDescription;
   private Text                   _txtWeather_Temperature_Average_Device;

   private AutocompleteComboInput _autocomplete_Location_End;
   private AutocompleteComboInput _autocomplete_Location_Start;
   private AutocompleteComboInput _autocomplete_Title;

   private Boolean                _tourLocation_HoverState;

   private class ActionSlideout_WeatherOptions extends ActionToolbarSlideout {

      @Override
      protected ToolbarSlideout createSlideout(final ToolBar toolbar) {

         return new SlideoutQuickEditor_WeatherOptions(_parent, toolbar, DialogQuickEdit.this);
      }

      @Override
      protected void onBeforeOpenSlideout() {
         closeOpenedDialogs(this);
      }
   }

   public DialogQuickEdit(final Shell parentShell, final TourData tourData) {

      super(parentShell);

      // make dialog resizable
      setShellStyle(getShellStyle() | SWT.RESIZE);

      setDefaultImage(TourbookPlugin.getImageDescriptor(Images.App_Edit).createImage());

      _tourData = tourData;

   }

   /**
    * Close all opened dialogs except the opening dialog.
    *
    * @param openingDialog
    */
   private void closeOpenedDialogs(final IOpeningDialog openingDialog) {

      _openDlgMgr.closeOpenedDialogs(openingDialog);
   }

   @Override
   public void closeOtherSlideouts(final ContributionItem requestForOpeningContribItem) {

      if (requestForOpeningContribItem.equals(_actionEndLocation)) {

         // close start location slideout

         _actionStartLocation.closeSlideout();

      } else {

         // close end location slideout

         _actionEndLocation.closeSlideout();
      }
   }

   @Override
   protected void configureShell(final Shell shell) {

      super.configureShell(shell);

      shell.setText(Messages.dialog_quick_edit_dialog_title);

      shell.addDisposeListener(disposeEvent -> onDispose());
   }

   @Override
   public void create() {

      super.create();

      setTitle(Messages.dialog_quick_edit_dialog_area_title);

      final ZonedDateTime tourStart = _tourData.getTourStartTime();

      setMessage(tourStart.format(TimeTools.Formatter_Date_F)
            + UI.SPACE2
            + tourStart.format(TimeTools.Formatter_Time_S));
   }

   private void createActions() {

      _actionStartLocation = new ActionTourLocation(this, true, STATE_ID);
      _actionEndLocation = new ActionTourLocation(this, false, STATE_ID);

      _actionStartLocation.setupTourData(_tourData);
      _actionEndLocation.setupTourData(_tourData);

      _actionSlideout_WeatherOptions = new ActionSlideout_WeatherOptions();
   }

   @Override
   protected final void createButtonsForButtonBar(final Composite parent) {

      super.createButtonsForButtonBar(parent);

      final String okText = net.tourbook.ui.UI.convertOKtoSaveUpdateButton(_tourData);

      getButton(IDialogConstants.OK_ID).setText(okText);
   }

   @Override
   protected Control createDialogArea(final Composite parent) {

      createActions();

      initUI();

      restoreState_BeforeUI();

      final Composite uiContainer = (Composite) super.createDialogArea(parent);

      createUI(uiContainer);

      updateUIFromModel();

      // prevent flickering
      parent.setRedraw(false);

      parent.getDisplay().asyncExec(() -> {

         // compute width for all controls and equalize column width for the different sections
         _tourContainer.layout(true, true);
         UI.setEqualizeColumWidths(_firstColumnControls);
         UI.setEqualizeColumWidths(_secondColumnControls);

         _tourContainer.layout(true, true);

         // !!! MUST BE DONE VERY LATE, OTHERWISE THERE ARE ISSUES !!!  ?????????? need to be checked
         _toolbarManager_WeatherOptions.update(true);

         parent.setRedraw(true);

         enableControls();

         resoreState();

         setFocus();
      });

      return uiContainer;
   }

   private void createUI(final Composite parent) {

      _parent = parent;

      _pc = new PixelConverter(parent);

      _hintDefaultSpinnerWidth = UI.IS_LINUX
            ? SWT.DEFAULT
            : _pc.convertWidthInCharsToPixels(UI.IS_OSX
                  ? 14
                  : 7);

      _unitValueDistance = UI.UNIT_VALUE_DISTANCE;
      _unitValueWindSpeed = IWeather.getAllWindSpeeds();

      _tk = new FormToolkit(parent.getDisplay());

      _formContainer = _tk.createForm(parent);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(_formContainer);

      _tk.decorateFormHeading(_formContainer);
      _tk.setBorderStyle(SWT.BORDER);

      _tourContainer = _formContainer.getBody();
      GridLayoutFactory.swtDefaults().applyTo(_tourContainer);
//      tourContainer.setBackground(UI.SYS_COLOR_GREEN);
      {
         createUI_110_Tour(_tourContainer);
         createUI_SectionSeparator(_tourContainer);

         createUI_130_Personal(_tourContainer);
         createUI_SectionSeparator(_tourContainer);

         createUI_140_Weather(_tourContainer);
      }

      final Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
   }

   private void createUI_110_Tour(final Composite parent) {

      final int defaultTextWidth = _pc.convertWidthInCharsToPixels(40);

      /*
       * Title section
       */
      final Composite sectionContainer = createUI_Section(parent, Messages.tour_editor_section_tour, true);
      GridLayoutFactory.fillDefaults().numColumns(2).applyTo(sectionContainer);
      {
         /*
          * Title
          */

         final Label label = _tk.createLabel(sectionContainer, Messages.tour_editor_label_tour_title);
         _firstColumnControls.add(label);

         // combo: tour title with history
         _comboTitle = new Combo(sectionContainer, SWT.BORDER | SWT.FLAT);
         _comboTitle.setText(UI.EMPTY_STRING);

         _tk.adapt(_comboTitle, true, false);
         GridDataFactory.fillDefaults()
               .grab(true, false)
               .hint(defaultTextWidth, SWT.DEFAULT)
               .applyTo(_comboTitle);

         // fill combobox
         final ConcurrentSkipListSet<String> dbTitles = TourDatabase.getCachedFields_AllTourTitles();
         for (final String title : dbTitles) {
            _comboTitle.add(title);
         }

         _autocomplete_Title = new AutocompleteComboInput(_comboTitle);
      }
      {
         /*
          * Description
          */
         final Label label = _tk.createLabel(sectionContainer, Messages.tour_editor_label_description);
         GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);
         _firstColumnControls.add(label);

         _txtTourDescription = _tk.createText(
               sectionContainer,
               UI.EMPTY_STRING,
               SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL//
         );

         // this is used as default, when the dialog is resized then the description field is also resized
         final int descriptionHeight = _pc.convertHeightInCharsToPixels(5);

         GridDataFactory.fillDefaults()
               .grab(true, true)
               //
               // SWT.DEFAULT causes lot's of problems with the layout therefore the hint is set
               //
               .hint(defaultTextWidth, descriptionHeight)
               .applyTo(_txtTourDescription);
      }
      {
         /*
          * Start location
          */
         final Composite container = new Composite(sectionContainer, SWT.NONE);
         GridDataFactory.fillDefaults().applyTo(container);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
//         container.setBackground(UI.SYS_COLOR_RED);
         {
            {
               // label

               final Label label = new Label(container, SWT.NONE);
               label.setText(Messages.Tour_Editor_Label_Location_Start);

               _tk.adapt(label, true, true);
               _gridData_GrabHorizontal_CenterVertical.applyTo(label);
            }
            {
               // download action

               final ToolBar toolbar = new ToolBar(container, SWT.FLAT);

               _toolbarManager_StartLocation = new ToolBarManager(toolbar);
               _toolbarManager_StartLocation.add(_actionStartLocation);
               _toolbarManager_StartLocation.update(true);
            }

            _firstColumnControls.add(container);

            {
               // autocomplete combo

               _comboLocation_Start = new Combo(sectionContainer, SWT.BORDER | SWT.FLAT);
               _comboLocation_Start.setText(UI.EMPTY_STRING);

               _tk.adapt(_comboLocation_Start, true, false);
               GridDataFactory.fillDefaults()
                     .grab(true, false)
                     .hint(defaultTextWidth, SWT.DEFAULT)
                     .applyTo(_comboLocation_Start);

               // fill combobox
               final ConcurrentSkipListSet<String> arr = TourDatabase.getCachedFields_AllTourPlaceStarts();
               for (final String string : arr) {
                  if (string != null) {
                     _comboLocation_Start.add(string);
                  }
               }

               _autocomplete_Location_Start = new AutocompleteComboInput(_comboLocation_Start);
            }
         }
      }
      {
         /*
          * End location
          */

         final Composite container = new Composite(sectionContainer, SWT.NONE);
         GridDataFactory.fillDefaults().applyTo(container);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
//         container.setBackground(UI.SYS_COLOR_DARK_GREEN);
         {
            {
               // label

               final Label label = new Label(container, SWT.NONE);
               label.setText(Messages.Tour_Editor_Label_Location_End);

               _tk.adapt(label, true, true);
               _gridData_GrabHorizontal_CenterVertical.applyTo(label);
            }
            {
               // download action

               final ToolBar toolbar = new ToolBar(container, SWT.FLAT);

               _toolbarManager_EndLocation = new ToolBarManager(toolbar);
               _toolbarManager_EndLocation.add(_actionEndLocation);
               _toolbarManager_EndLocation.update(true);
            }

            _firstColumnControls.add(container);
         }
         {
            // autocomplete combo

            _comboLocation_End = new Combo(sectionContainer, SWT.BORDER | SWT.FLAT);
            _comboLocation_End.setText(UI.EMPTY_STRING);

            _tk.adapt(_comboLocation_End, true, false);
            GridDataFactory.fillDefaults()
                  .grab(true, false)
                  .hint(defaultTextWidth, SWT.DEFAULT)
                  .applyTo(_comboLocation_End);

            // fill combobox
            final ConcurrentSkipListSet<String> arr = TourDatabase.getCachedFields_AllTourPlaceEnds();
            for (final String string : arr) {
               if (string != null) {
                  _comboLocation_End.add(string);
               }
            }

            _autocomplete_Location_End = new AutocompleteComboInput(_comboLocation_End);
         }
      }
   }

   private void createUI_130_Personal(final Composite parent) {

      final Composite section = createUI_Section(parent, Messages.tour_editor_section_personal, false);
      GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(section);
      GridLayoutFactory.fillDefaults()
            .numColumns(2)
            .spacing(20, 5)
            .applyTo(section);
//      section.setBackground(UI.SYS_COLOR_MAGENTA);
      {
         createUI_132_Personal_Col1(section);
         createUI_134_Personal_Col2(section);
      }
   }

   /**
    * 1. column
    */
   private void createUI_132_Personal_Col1(final Composite section) {

      final Composite container = _tk.createComposite(section);
      GridDataFactory.fillDefaults().applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_GREEN);
      {
         {
            /*
             * calories
             */

            // label
            final Label label = _tk.createLabel(container, Messages.tour_editor_label_tour_calories);
            _firstColumnControls.add(label);

            // spinner
            _spinCalories = new Spinner(container, SWT.BORDER);
            _spinCalories.setMinimum(0);
            _spinCalories.setMaximum(1_000_000_000);
            _spinCalories.setDigits(3);
            _spinCalories.addMouseWheelListener(_mouseWheelListener);

            GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(_spinCalories);

            // label: kcal
            _tk.createLabel(container, OtherMessages.VALUE_UNIT_K_CALORIES);
         }
         {
            /*
             * rest pulse
             */

            // label: Rest pulse
            final Label label = _tk.createLabel(container, Messages.tour_editor_label_rest_pulse);
            label.setToolTipText(Messages.tour_editor_label_rest_pulse_Tooltip);
            _firstColumnControls.add(label);

            // spinner
            _spinRestPulse = new Spinner(container, SWT.BORDER);
            _spinRestPulse.setMinimum(0);
            _spinRestPulse.setMaximum(200);
            _spinRestPulse.setToolTipText(Messages.tour_editor_label_rest_pulse_Tooltip);
            _spinRestPulse.addMouseWheelListener(_mouseWheelListener);

            GridDataFactory.fillDefaults()
                  .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                  .align(SWT.FILL, SWT.CENTER)
                  .applyTo(_spinRestPulse);

            // label: bpm
            _tk.createLabel(container, OtherMessages.GRAPH_LABEL_HEARTBEAT_UNIT);
         }
      }
   }

   /**
    * 2. column
    */
   private void createUI_134_Personal_Col2(final Composite section) {

      final Composite container = _tk.createComposite(section);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_YELLOW);
      {
         {
            {
               /*
                * Body weight
                */
               // label: Weight
               final Label label = _tk.createLabel(container, Messages.Tour_Editor_Label_BodyWeight);
               label.setToolTipText(Messages.Tour_Editor_Label_BodyWeight_Tooltip);
               _secondColumnControls.add(label);

               // spinner: weight
               _spinBodyWeight = new Spinner(container, SWT.BORDER);
               _spinBodyWeight.setDigits(1);
               _spinBodyWeight.setMinimum(0);
               _spinBodyWeight.setMaximum(6614); // 300.0 kg, 661.4 lbs
               _spinBodyWeight.addMouseWheelListener(_mouseWheelListener);

               GridDataFactory.fillDefaults()
                     .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                     .align(SWT.BEGINNING, SWT.CENTER)
                     .applyTo(_spinBodyWeight);

               // label: unit
               _tk.createLabel(container, UI.UNIT_LABEL_WEIGHT);
            }

            {
               /*
                * FTP - Functional Threshold Power
                */
               // label: FTP
               final Label label = _tk.createLabel(container, Messages.Tour_Editor_Label_FTP);
               label.setToolTipText(Messages.Tour_Editor_Label_FTP_Tooltip);
               _secondColumnControls.add(label);

               // spinner: FTP
               _spinFTP = new Spinner(container, SWT.BORDER);
               _spinFTP.setMinimum(0);
               _spinFTP.setMaximum(10000);
               _spinFTP.addMouseWheelListener(_mouseWheelListener);

               GridDataFactory.fillDefaults()
                     .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                     .align(SWT.BEGINNING, SWT.CENTER)
                     .applyTo(_spinFTP);

               // spacer
               _tk.createLabel(container, UI.EMPTY_STRING);
            }
         }
      }
   }

   private void createUI_140_Weather(final Composite parent) {

      final Composite section = createUI_Section(parent, Messages.tour_editor_section_weather, false);
      GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(section);
      GridLayoutFactory.fillDefaults()
            .numColumns(2)
            .spacing(20, 5)
            .applyTo(section);
//      section.setBackground(UI.SYS_COLOR_CYAN);
      {
         createUI_141_Weather_Description(section);
         createUI_142_Weather_Wind_Temperature(section);
         createUI_144_Weather_Cloud(section);
      }
   }

   private void createUI_141_Weather_Description(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
      {
         /*
          * Weather description
          */
         final Label label = _tk.createLabel(container, Messages.Tour_Editor_Label_Weather);
         GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);
         _firstColumnControls.add(label);

         _txtWeatherDescription = _tk.createText(
               container,
               UI.EMPTY_STRING,
               SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL//
         );

         GridDataFactory.fillDefaults()
               .grab(true, true)
               //
               // SWT.DEFAULT causes lot's of problems with the layout therefore the hint is set
               //
               .hint(_pc.convertWidthInCharsToPixels(80), _pc.convertHeightInCharsToPixels(_numLines_WeatherDescription))
               .applyTo(_txtWeatherDescription);
      }
   }

   private void createUI_142_Weather_Wind_Temperature(final Composite parent) {

      final Composite container = _tk.createComposite(parent);
      GridDataFactory.fillDefaults().span(2, 1).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(6).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_RED);
      {
         {
            /*
             * wind speed
             */

            // label
            Label label = _tk.createLabel(container, Messages.tour_editor_label_wind_speed);
            label.setToolTipText(Messages.tour_editor_label_wind_speed_Tooltip);
            _firstColumnControls.add(label);

            // spinner
            _spinWeather_Wind_SpeedValue = new Spinner(container, SWT.BORDER);
            _spinWeather_Wind_SpeedValue.setMinimum(0);
            _spinWeather_Wind_SpeedValue.setMaximum(120);
            _spinWeather_Wind_SpeedValue.setToolTipText(Messages.tour_editor_label_wind_speed_Tooltip);

            _spinWeather_Wind_SpeedValue.addModifyListener(modifyEvent -> {
               if (_isUpdateUI) {
                  return;
               }
               onSelect_WindSpeed_Value();
            });

            _spinWeather_Wind_SpeedValue.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> {
               if (_isUpdateUI) {
                  return;
               }
               onSelect_WindSpeed_Value();
            }));

            _spinWeather_Wind_SpeedValue.addMouseWheelListener(mouseEvent -> {
               Util.adjustSpinnerValueOnMouseScroll(mouseEvent);
               if (_isUpdateUI) {
                  return;
               }
               onSelect_WindSpeed_Value();
            });

            GridDataFactory.fillDefaults()
                  .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                  .align(SWT.BEGINNING, SWT.CENTER)
                  .applyTo(_spinWeather_Wind_SpeedValue);

            // label: km/h, mi/h
            label = _tk.createLabel(container, UI.UNIT_LABEL_SPEED);

            // combo: wind speed with text
            _comboWeather_Wind_SpeedText = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
            _comboWeather_Wind_SpeedText.setToolTipText(Messages.tour_editor_label_wind_speed_Tooltip);
            _comboWeather_Wind_SpeedText.setVisibleItemCount(20);
            _comboWeather_Wind_SpeedText.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> {
               if (_isUpdateUI) {
                  return;
               }
               onSelect_WindSpeed_Text();
            }));

            _tk.adapt(_comboWeather_Wind_SpeedText, true, false);
            GridDataFactory.fillDefaults()
                  .align(SWT.BEGINNING, SWT.FILL)
                  .indent(10, 0)
                  .span(2, 1)
                  .applyTo(_comboWeather_Wind_SpeedText);

            // fill combobox
            for (final String speedText : IWeather.windSpeedText) {
               _comboWeather_Wind_SpeedText.add(speedText);
            }
            {
               /*
                * Options slideout
                */
               final ToolBar toolbar = new ToolBar(container, SWT.FLAT);

               _tk.adapt(toolbar, true, false);
               GridDataFactory.fillDefaults()
                     .grab(true, false)
                     .align(SWT.END, SWT.BEGINNING)
                     .applyTo(toolbar);

               _toolbarManager_WeatherOptions = new ToolBarManager(toolbar);
               _toolbarManager_WeatherOptions.add(_actionSlideout_WeatherOptions);
            }
         }
         {
            /*
             * Wind direction
             */

            // label
            final Label label = _tk.createLabel(container, Messages.tour_editor_label_wind_direction);
            label.setToolTipText(Messages.tour_editor_label_wind_direction_Tooltip);
            _firstColumnControls.add(label);

            // combo: wind direction text
            _comboWeather_Wind_DirectionText = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
            _comboWeather_Wind_DirectionText.setToolTipText(Messages.tour_editor_label_WindDirectionNESW_Tooltip);
            _comboWeather_Wind_DirectionText.setVisibleItemCount(16);
            _comboWeather_Wind_DirectionText.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> {
               if (_isUpdateUI) {
                  return;
               }
               TourDataEditorView.onSelect_WindDirection_Text(_spinWeather_Wind_DirectionValue, _comboWeather_Wind_DirectionText);
            }));

            _tk.adapt(_comboWeather_Wind_DirectionText, true, false);
            GridDataFactory.fillDefaults()
                  .align(SWT.FILL, SWT.FILL)
                  .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                  .applyTo(_comboWeather_Wind_DirectionText);

            // fill combobox
            for (final String fComboCloudsUIValue : IWeather.windDirectionText) {
               _comboWeather_Wind_DirectionText.add(fComboCloudsUIValue);
            }

            // spacer
            new Label(container, SWT.NONE);

            // spinner: wind direction value
            _spinWeather_Wind_DirectionValue = new Spinner(container, SWT.BORDER);
            _spinWeather_Wind_DirectionValue.setMinimum(-1);
            _spinWeather_Wind_DirectionValue.setMaximum(3600);
            _spinWeather_Wind_DirectionValue.setDigits(1);
            _spinWeather_Wind_DirectionValue.setToolTipText(Messages.tour_editor_label_wind_direction_Tooltip);

            _spinWeather_Wind_DirectionValue.addModifyListener(modifyEvent -> {
               if (_isUpdateUI) {
                  return;
               }
               TourDataEditorView.onSelect_WindDirection_Value(_spinWeather_Wind_DirectionValue, _comboWeather_Wind_DirectionText);
            });

            _spinWeather_Wind_DirectionValue.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> {
               if (_isUpdateUI) {
                  return;
               }
               TourDataEditorView.onSelect_WindDirection_Value(_spinWeather_Wind_DirectionValue, _comboWeather_Wind_DirectionText);
            }));

            _spinWeather_Wind_DirectionValue.addMouseWheelListener(mouseEvent -> {
               Util.adjustSpinnerValueOnMouseScroll(mouseEvent);
               if (_isUpdateUI) {
                  return;
               }
               TourDataEditorView.onSelect_WindDirection_Value(_spinWeather_Wind_DirectionValue, _comboWeather_Wind_DirectionText);
            });

            GridDataFactory.fillDefaults()
                  .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                  .indent(10, 0)
                  .align(SWT.BEGINNING, SWT.CENTER)
                  .applyTo(_spinWeather_Wind_DirectionValue);

            // label: direction unit = degree
            _tk.createLabel(container, Messages.Tour_Editor_Label_WindDirection_Unit);

            UI.createSpacer_Horizontal(container, 1);
         }
         {
            /*
             * Average temperatures
             */
            {
               /*
                * Manual/provider
                */

               // label
               Label label = _tk.createLabel(container, Messages.Tour_Editor_Label_Temperature);
               label.setToolTipText(Messages.Tour_Editor_Label_Temperature_Tooltip);
               _firstColumnControls.add(label);

               // Spinner: Average temperature
               _spinWeather_Temperature_Average = new Spinner(container, SWT.BORDER);
               _spinWeather_Temperature_Average.setToolTipText(Messages.Tour_Editor_Label_Temperature_Avg_Tooltip);

               // the min/max temperature has a large range because Fahrenheit has bigger values than Celsius
               _spinWeather_Temperature_Average.setMinimum(-600);
               _spinWeather_Temperature_Average.setMaximum(1500);

               _spinWeather_Temperature_Average.addModifyListener(modifyEvent -> {
                  if (_isUpdateUI) {
                     return;
                  }
                  _isTemperatureManuallyModified = true;
               });
               _spinWeather_Temperature_Average.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> {
                  if (_isUpdateUI) {
                     return;
                  }
                  _isTemperatureManuallyModified = true;
               }));
               _spinWeather_Temperature_Average.addMouseWheelListener(mouseEvent -> {
                  Util.adjustSpinnerValueOnMouseScroll(mouseEvent);
                  if (_isUpdateUI) {
                     return;
                  }
                  _isTemperatureManuallyModified = true;
               });

               GridDataFactory.fillDefaults()
                     .align(SWT.BEGINNING, SWT.CENTER)
                     .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                     .applyTo(_spinWeather_Temperature_Average);

               // label: celsius, fahrenheit
               label = _tk.createLabel(container, UI.UNIT_LABEL_TEMPERATURE);
               label.setToolTipText(Messages.Tour_Editor_Label_Temperature_Avg_Tooltip);
            }
            {
               /*
                * Device
                */
               final Composite temperatureFromDeviceContainer = new Composite(container, SWT.NONE);
               GridDataFactory.fillDefaults().span(2, 1).applyTo(temperatureFromDeviceContainer);
               GridLayoutFactory.fillDefaults().numColumns(2).applyTo(temperatureFromDeviceContainer);
               {
                  // Average temperature measured from device
                  _txtWeather_Temperature_Average_Device = new Text(temperatureFromDeviceContainer, SWT.BORDER | SWT.READ_ONLY);
                  _txtWeather_Temperature_Average_Device.setToolTipText(Messages.Tour_Editor_Label_Temperature_Avg_Device_Tooltip);

                  _tk.adapt(_txtWeather_Temperature_Average_Device, true, false);
                  GridDataFactory.fillDefaults()
                        .hint(_hintDefaultSpinnerWidth, SWT.DEFAULT)
                        .indent(10, 0)
                        .align(SWT.END, SWT.CENTER)
                        .applyTo(_txtWeather_Temperature_Average_Device);

                  // label: celsius, fahrenheit
                  final Label label = _tk.createLabel(temperatureFromDeviceContainer, UI.UNIT_LABEL_TEMPERATURE);
                  label.setToolTipText(Messages.Tour_Editor_Label_Temperature_Avg_Device_Tooltip);
               }
            }
         }
      }
   }

   /**
    * Weather: Cloud
    */
   private void createUI_144_Weather_Cloud(final Composite parent) {

      final Composite container = _tk.createComposite(parent);
      GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_BLUE);
      {
         /*
          * Clouds label + icon
          */
         final Composite labelAndIconContainer = new Composite(container, SWT.NONE);
         GridDataFactory.fillDefaults().applyTo(labelAndIconContainer);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(labelAndIconContainer);
//         labelAndIconContainer.setBackground(UI.SYS_COLOR_GREEN);
         {
            // label: clouds
            final Label label = _tk.createLabel(labelAndIconContainer, Messages.tour_editor_label_clouds);
            label.setToolTipText(Messages.tour_editor_label_clouds_Tooltip);
            _gridData_GrabHorizontal_CenterVertical.applyTo(label);

            // icon: clouds
            _lblWeather_CloudIcon = new CLabel(labelAndIconContainer, SWT.NONE);
            GridDataFactory.fillDefaults()
                  .align(SWT.END, SWT.FILL)
                  .applyTo(_lblWeather_CloudIcon);

            _firstColumnControls.add(labelAndIconContainer);
         }
         {
            // combo: clouds
            _comboWeather_Clouds = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
            _comboWeather_Clouds.setToolTipText(Messages.tour_editor_label_clouds_Tooltip);
            _comboWeather_Clouds.setVisibleItemCount(10);
            _comboWeather_Clouds.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> displayCloudIcon()));

            _tk.adapt(_comboWeather_Clouds, true, false);
            GridDataFactory.fillDefaults().span(2, 1).applyTo(_comboWeather_Clouds);

            // fill combobox
            for (final String cloudText : IWeather.cloudText) {
               _comboWeather_Clouds.add(cloudText);
            }

            // force the icon to be displayed to ensure the width is correctly set when the size is computed
            _isUpdateUI = true;
            {
               _comboWeather_Clouds.select(0);
               displayCloudIcon();
            }
            _isUpdateUI = false;
         }
      }
   }

   private Composite createUI_Section(final Composite parent, final String title, final boolean isGrabVertical) {

      final Section section = _tk.createSection(parent, Section.TITLE_BAR);

      section.setText(title);
      GridDataFactory.fillDefaults()
            .grab(true, isGrabVertical)
            .applyTo(section);

      final Composite sectionContainer = _tk.createComposite(section);
      section.setClient(sectionContainer);

      return sectionContainer;
   }

   /**
    * @param parent
    * @param title
    * @param isGrabVertical
    *
    * @return
    */
   private void createUI_SectionSeparator(final Composite parent) {

      final Composite sep = _tk.createComposite(parent);

      GridDataFactory.fillDefaults()
            .span(2, 1)
            .hint(SWT.DEFAULT, 5)
            .applyTo(sep);
   }

   @Override
   public void defaultProfileIsUpdated() {

      // update action tooltip which displays the current profile
      _actionStartLocation.updateUI_ToolItem();
      _actionEndLocation.updateUI_ToolItem();
   }

   private void displayCloudIcon() {

      final int selectionIndex = _comboWeather_Clouds.getSelectionIndex();

      final String cloudKey = IWeather.cloudIcon[selectionIndex];
      final Image cloudIcon = UI.IMAGE_REGISTRY.get(cloudKey);

      _lblWeather_CloudIcon.setImage(cloudIcon);
   }

   @Override
   public void downloadAndSetTourEndLocation() {

      TourLocationData endLocationData = _tourData.tourLocationData_End;

      if (endLocationData == null) {

         final int lastIndex = _tourData.latitudeSerie.length - 1;

         final TourLocationData retrievedLocationData = TourLocationManager.getLocationData(
               _tourData.latitudeSerie[lastIndex],
               _tourData.longitudeSerie[lastIndex],
               null,
               TourLocationManager.getProfileZoomlevel());

         if (retrievedLocationData == null) {
            return;
         }

         _tourData.setTourLocationEnd(retrievedLocationData.tourLocation);
         endLocationData = _tourData.tourLocationData_End = retrievedLocationData;

         // show different action image
         _actionEndLocation.setHasLocationData(true);
         _toolbarManager_EndLocation.update(true);
      }

      _comboLocation_End.setText(TourLocationManager.createLocationDisplayName(endLocationData.tourLocation));

      enableControls();
   }

   @Override
   public void downloadAndSetTourStartLocation() {

      TourLocationData startLocationData = _tourData.tourLocationData_Start;

      if (startLocationData == null) {

         final TourLocationData retrievedLocationData = TourLocationManager.getLocationData(
               _tourData.latitudeSerie[0],
               _tourData.longitudeSerie[0],
               null,
               TourLocationManager.getProfileZoomlevel());

         if (retrievedLocationData == null) {
            return;
         }

         _tourData.setTourLocationStart(retrievedLocationData.tourLocation);
         startLocationData = _tourData.tourLocationData_Start = retrievedLocationData;

         // show different action image
         _actionStartLocation.setHasLocationData(true);
         _toolbarManager_StartLocation.update(true);
      }

      _comboLocation_Start.setText(TourLocationManager.createLocationDisplayName(startLocationData.tourLocation));

      enableControls();
   }

   private void enableControls() {

      final boolean hasGeoData = _tourData.latitudeSerie != null && _tourData.latitudeSerie.length > 0;

      _actionStartLocation.setEnabled(hasGeoData);
      _actionEndLocation.setEnabled(hasGeoData);

      _spinWeather_Wind_DirectionValue.setEnabled(_comboWeather_Wind_DirectionText.getSelectionIndex() > 0);
   }

   @Override
   protected IDialogSettings getDialogBoundsSettings() {

      // keep window size and position
      return _state;
//      return null;
   }

   private int getWindSpeedTextIndex(final int speed) {

      // set speed to max index value
      int speedValueIndex = _unitValueWindSpeed.length - 1;

      for (int speedIndex = 0; speedIndex < _unitValueWindSpeed.length; speedIndex++) {

         final int speedMaxValue = _unitValueWindSpeed[speedIndex];

         if (speed <= speedMaxValue) {
            speedValueIndex = speedIndex;
            break;
         }
      }

      return speedValueIndex;
   }

   private void initUI() {

      _gridData_GrabHorizontal_CenterVertical = GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER);

      _mouseWheelListener = mouseEvent -> Util.adjustSpinnerValueOnMouseScroll(mouseEvent);
   }

   @Override
   protected void okPressed() {

      updateModelFromUI();

      if (_tourData.isValidForSave() == false) {
         // data are not valid to be saved which is done in the action which opened this dialog
         return;
      }

      super.okPressed();
   }

   private void onDispose() {

      final IDialogSettings state = TourDataEditorView.getState();

// SET_FORMATTING_OFF

      _autocomplete_Title           .saveState(state, TourDataEditorView.STATE_AUTOCOMPLETE_POPUP_HEIGHT_TITLE);
      _autocomplete_Location_Start  .saveState(state, TourDataEditorView.STATE_AUTOCOMPLETE_POPUP_HEIGHT_LOCATION_START);
      _autocomplete_Location_End    .saveState(state, TourDataEditorView.STATE_AUTOCOMPLETE_POPUP_HEIGHT_LOCATION_END);

// SET_FORMATTING_ON

      if (_tk != null) {
         _tk.dispose();
      }

      _firstColumnControls.clear();
      _secondColumnControls.clear();
   }

   private void onSelect_WindSpeed_Text() {

      _isWindSpeedManuallyModified = true;

      final int selectedIndex = _comboWeather_Wind_SpeedText.getSelectionIndex();
      final int speed = _unitValueWindSpeed[selectedIndex];

      final boolean isBackup = _isUpdateUI;
      _isUpdateUI = true;
      {
         _spinWeather_Wind_SpeedValue.setSelection(speed);
      }
      _isUpdateUI = isBackup;
   }

   private void onSelect_WindSpeed_Value() {

      _isWindSpeedManuallyModified = true;

      final int windSpeed = _spinWeather_Wind_SpeedValue.getSelection();

      final boolean isBackup = _isUpdateUI;
      _isUpdateUI = true;
      {
         _comboWeather_Wind_SpeedText.select(getWindSpeedTextIndex(windSpeed));
      }
      _isUpdateUI = isBackup;
   }

   private void resoreState() {

      final IDialogSettings state = TourDataEditorView.getState();

// SET_FORMATTING_OFF

      _autocomplete_Title           .restoreState(state, TourDataEditorView.STATE_AUTOCOMPLETE_POPUP_HEIGHT_TITLE);
      _autocomplete_Location_Start  .restoreState(state, TourDataEditorView.STATE_AUTOCOMPLETE_POPUP_HEIGHT_LOCATION_START);
      _autocomplete_Location_End    .restoreState(state, TourDataEditorView.STATE_AUTOCOMPLETE_POPUP_HEIGHT_LOCATION_END);

// SET_FORMATTING_ON
   }

   private void restoreState_BeforeUI() {

      _numLines_WeatherDescription = Util.getStateInt(_state_TourDataEditorView,
            TourDataEditorView.STATE_WEATHERDESCRIPTION_NUMBER_OF_LINES,
            TourDataEditorView.STATE_WEATHERDESCRIPTION_NUMBER_OF_LINES_DEFAULT);
   }

   /**
    * Set focus when dialog is opened
    */
   private void setFocus() {

      if (_tourLocation_HoverState == null) {

         _comboTitle.setFocus();

      } else if (_tourLocation_HoverState) {

         _comboLocation_Start.setFocus();

      } else {

         _comboLocation_End.setFocus();
      }
   }

   @Override
   public void setTourEndLocation(final String endLocation) {

      _comboLocation_End.setText(endLocation);
   }

   /**
    * @param tourLocation_HoverState
    *
    *           Is <code>true</code> when start location is hovered, <code>false</code> when
    *           endlocation is hovered, <code>null</code> when a location is not hovered
    */
   public void setTourLocationFocus(final Boolean tourLocation_HoverState) {

      _tourLocation_HoverState = tourLocation_HoverState;
   }

   @Override
   public void setTourStartLocation(final String startLocation) {

      _comboLocation_Start.setText(startLocation);
   }

   /**
    * update tourdata from the fields
    */
   private void updateModelFromUI() {

      _tourData.setTourTitle(_comboTitle.getText().trim());
      _tourData.setTourDescription(_txtTourDescription.getText().trim());

      _tourData.setTourStartPlace(_comboLocation_Start.getText().trim());
      _tourData.setTourEndPlace(_comboLocation_End.getText().trim());

      final float bodyWeight = UI.convertBodyWeightToMetric(_spinBodyWeight.getSelection());
      _tourData.setBodyWeight(bodyWeight / 10.0f);
      _tourData.setPower_FTP(_spinFTP.getSelection());
      _tourData.setRestPulse(_spinRestPulse.getSelection());
      _tourData.setCalories(_spinCalories.getSelection());

      _tourData.setWeather_Wind_Direction((int) (_spinWeather_Wind_DirectionValue.getSelection() / 10.0f));
      final int weatherWindDirection = _comboWeather_Wind_DirectionText.getSelectionIndex() == 0
            ? -1
            : (int) (_spinWeather_Wind_DirectionValue.getSelection() / 10.0f);
      _tourData.setWeather_Wind_Direction(weatherWindDirection);

      if (_isWindSpeedManuallyModified) {
         /*
          * update the speed only when it was modified because when the measurement is changed
          * when the tour is being modified then the computation of the speed value can cause
          * rounding errors
          */
         _tourData.setWeather_Wind_Speed((int) (_spinWeather_Wind_SpeedValue.getSelection() * _unitValueDistance));
      }

      final int cloudIndex = _comboWeather_Clouds.getSelectionIndex();
      String cloudValue = IWeather.cloudIcon[cloudIndex];
      if (cloudValue.equals(UI.IMAGE_EMPTY_16)) {
         // replace invalid cloud key
         cloudValue = UI.EMPTY_STRING;
      }
      _tourData.setWeather_Clouds(cloudValue);
      _tourData.setWeather(_txtWeatherDescription.getText().trim());

      if (_isTemperatureManuallyModified) {

         final float temperature = (float) _spinWeather_Temperature_Average.getSelection() / 10;

         _tourData.setWeather_Temperature_Average(UI.convertTemperatureToMetric(temperature));
      }

   }

   void updateUI_DescriptionNumLines(final int numLines_WeatherDescription) {

      if (_numLines_WeatherDescription == numLines_WeatherDescription) {

         // nothing has changed
         return;
      }

      _numLines_WeatherDescription = numLines_WeatherDescription;

      // update layout
      final GridData gd = (GridData) _txtWeatherDescription.getLayoutData();
      gd.heightHint = _pc.convertHeightInCharsToPixels(numLines_WeatherDescription);

      final Composite tourContainer = _formContainer.getBody();
      tourContainer.layout(true, true);
   }

   private void updateUIFromModel() {

      _isUpdateUI = true;
      {
         /*
          * Tour/event
          */
         // set field content
         _comboTitle.setText(_tourData.getTourTitle());
         _txtTourDescription.setText(_tourData.getTourDescription());

         /*
          * Tour location
          */
         _comboLocation_Start.setText(_tourData.getTourStartPlace());
         _comboLocation_End.setText(_tourData.getTourEndPlace());

         final TourLocation tourLocationStart = _tourData.getTourLocationStart();
         final TourLocation tourLocationEnd = _tourData.getTourLocationEnd();

         if (tourLocationStart != null) {
            _actionStartLocation.setHasLocationData(true);
            _toolbarManager_StartLocation.update(true);
         }
         if (tourLocationEnd != null) {
            _actionEndLocation.setHasLocationData(true);
            _toolbarManager_EndLocation.update(true);
         }

         /*
          * Personal details
          */
         final float bodyWeight = UI.convertBodyWeightFromMetric(_tourData.getBodyWeight());
         _spinBodyWeight.setSelection(Math.round(bodyWeight * 10));
         _spinFTP.setSelection(_tourData.getPower_FTP());
         _spinRestPulse.setSelection(_tourData.getRestPulse());
         _spinCalories.setSelection(_tourData.getCalories());

         /*
          * Wind properties
          */
         _txtWeatherDescription.setText(_tourData.getWeather());

         // wind direction
         final int weatherWindDirection = _tourData.getWeather_Wind_Direction();
         if (weatherWindDirection == -1) {
            _spinWeather_Wind_DirectionValue.setSelection(0);
            _comboWeather_Wind_DirectionText.select(0);
         } else {
            final int weatherWindDirectionDegree = weatherWindDirection * 10;
            _spinWeather_Wind_DirectionValue.setSelection(weatherWindDirectionDegree);
            _comboWeather_Wind_DirectionText.select(UI.getCardinalDirectionTextIndex((int) (weatherWindDirectionDegree / 10.0f)));
         }

         // wind speed
         final int windSpeed = _tourData.getWeather_Wind_Speed();
         final int speed = (int) (windSpeed / _unitValueDistance);
         _spinWeather_Wind_SpeedValue.setSelection(speed);
         _comboWeather_Wind_SpeedText.select(getWindSpeedTextIndex(speed));

         // weather clouds
         _comboWeather_Clouds.select(_tourData.getWeatherIndex());

         // icon must be displayed after the combobox entry is selected
         displayCloudIcon();

         /*
          * Avg temperature
          */
         final boolean isTourTemperatureValid = _tourData.getWeather_Temperature_Average() != 0 ||
               _tourData.getWeather_Temperature_Max() != 0 ||
               _tourData.getWeather_Temperature_Min() != 0 ||
               _tourData.isWeatherDataFromProvider();
         final float avgTemperature =
               UI.convertTemperatureFromMetric(_tourData.getWeather_Temperature_Average());

         _spinWeather_Temperature_Average.setDigits(1);
         int avgTemperatureValue = 0;
         if (isTourTemperatureValid) {
            avgTemperatureValue = Math.round(avgTemperature * 10);

         }
         _spinWeather_Temperature_Average.setSelection(avgTemperatureValue);

         /*
          * Avg temperature from Device
          */
         final boolean isTourTemperatureDeviceValid = _tourData.temperatureSerie != null && _tourData.temperatureSerie.length > 0;
         final float avgTemperature_Device = UI.convertTemperatureFromMetric(_tourData.getWeather_Temperature_Average_Device());
         _txtWeather_Temperature_Average_Device.setText(isTourTemperatureDeviceValid
               ? String.valueOf(Math.round(avgTemperature_Device * 10.0) / 10.0)
               : UI.EMPTY_STRING);
      }
      _isUpdateUI = false;
   }
}
