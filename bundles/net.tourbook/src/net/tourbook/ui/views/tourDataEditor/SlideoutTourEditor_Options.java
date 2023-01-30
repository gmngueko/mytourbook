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
package net.tourbook.ui.views.tourDataEditor;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.action.ActionResetToDefaults;
import net.tourbook.common.action.IActionResetToDefault;
import net.tourbook.common.color.IColorSelectorListener;
import net.tourbook.common.font.MTFont;
import net.tourbook.common.tooltip.ToolbarSlideout;
import net.tourbook.common.util.Util;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Slideout for the tour data editor options.
 */
public class SlideoutTourEditor_Options extends ToolbarSlideout implements IColorSelectorListener, IActionResetToDefault {

   private final IDialogSettings _state = TourbookPlugin.getState(TourDataEditorView.ID);

   private TourDataEditorView    _tourEditorView;

   private ActionResetToDefaults _actionRestoreDefaults;

   private SelectionListener     _defaultSelectionListener;

   private PixelConverter        _pc;

   private int                   _hintValueFieldWidth;

   /*
    * UI controls
    */
   private Composite _shellContainer;

   private Button    _chkDelete_KeepDistance;
   private Button    _chkDelete_KeepTime;
   private Button    _chkRecomputeElevation;

   private Spinner   _spinnerLatLonDigits;
   private Spinner   _spinnerTourDescriptionNumLines;
   private Spinner   _spinnerWeatherDescriptionNumLines;

   public SlideoutTourEditor_Options(final Control ownerControl,
                                     final ToolBar toolBar,
                                     final TourDataEditorView tourEditorView) {

      super(ownerControl, toolBar);

      _tourEditorView = tourEditorView;
   }

   @Override
   public void colorDialogOpened(final boolean isDialogOpened) {

      setIsAnotherDialogOpened(isDialogOpened);
   }

   private void createActions() {

      /*
       * Action: Restore default
       */
      _actionRestoreDefaults = new ActionResetToDefaults(this);
   }

   @Override
   protected Composite createToolTipContentArea(final Composite parent) {

      initUI(parent);

      createActions();

      final Composite ui = createUI(parent);

      restoreState();

      return ui;
   }

   private Composite createUI(final Composite parent) {

      _shellContainer = new Composite(parent, SWT.NONE);
      GridLayoutFactory.swtDefaults().applyTo(_shellContainer);
      {
         final Composite container = new Composite(_shellContainer, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
//			container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
         {
            createUI_10_Title(container);
            createUI_12_Actions(container);

            createUI_20_Options(container);
         }
      }

      return _shellContainer;
   }

   private void createUI_10_Title(final Composite parent) {

      /*
       * Label: Slideout title
       */
      final Label label = new Label(parent, SWT.NONE);
      label.setText(Messages.Slideout_TourEditor_Label_Title);
      GridDataFactory.fillDefaults().applyTo(label);
      MTFont.setBannerFont(label);
   }

   private void createUI_12_Actions(final Composite parent) {

      final ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
      GridDataFactory.fillDefaults()
            .grab(true, false)
            .align(SWT.END, SWT.BEGINNING)
            .applyTo(toolbar);

      final ToolBarManager tbm = new ToolBarManager(toolbar);

      tbm.add(_actionRestoreDefaults);

      tbm.update(true);
   }

   private void createUI_20_Options(final Composite parent) {

      final GridDataFactory spinnerGridData = GridDataFactory.fillDefaults()
            .hint(_hintValueFieldWidth, SWT.DEFAULT)
            .align(SWT.END, SWT.CENTER);
      {
         /*
          * Number of lines for the tour's description
          */

         // label
         final Label label = new Label(parent, SWT.NONE);
         label.setText(Messages.pref_tour_editor_description_height);
         label.setToolTipText(Messages.pref_tour_editor_description_height_tooltip);
         GridDataFactory.fillDefaults()
               .align(SWT.FILL, SWT.CENTER)
               .applyTo(label);

         // spinner
         _spinnerTourDescriptionNumLines = new Spinner(parent, SWT.BORDER);
         _spinnerTourDescriptionNumLines.setMinimum(1);
         _spinnerTourDescriptionNumLines.setMaximum(100);
         _spinnerTourDescriptionNumLines.addSelectionListener(widgetSelectedAdapter(selectionEvent -> onSelect_NumDescriptionLines()));
         _spinnerTourDescriptionNumLines.addMouseWheelListener(mouseEvent -> {
            UI.adjustSpinnerValueOnMouseScroll(mouseEvent);
            onSelect_NumDescriptionLines();
         });
         spinnerGridData.applyTo(_spinnerTourDescriptionNumLines);
      }
      {
         /*
          * Number of lines for the weather's description
          */

         // label
         final Label label = new Label(parent, SWT.NONE);
         label.setText(Messages.Slideout_TourEditor_Label_WeatherDescription_Height);
         label.setToolTipText(Messages.Slideout_TourEditor_Label_WeatherDescription_Height_Tooltip);
         GridDataFactory.fillDefaults()
               .align(SWT.FILL, SWT.CENTER)
               .applyTo(label);

         // spinner
         _spinnerWeatherDescriptionNumLines = new Spinner(parent, SWT.BORDER);
         _spinnerWeatherDescriptionNumLines.setMinimum(1);
         _spinnerWeatherDescriptionNumLines.setMaximum(100);
         _spinnerWeatherDescriptionNumLines.addSelectionListener(widgetSelectedAdapter(selectionEvent -> onSelect_NumDescriptionLines()));
         _spinnerWeatherDescriptionNumLines.addMouseWheelListener(mouseEvent -> {
            UI.adjustSpinnerValueOnMouseScroll(mouseEvent);
            onSelect_NumDescriptionLines();
         });
         spinnerGridData.applyTo(_spinnerWeatherDescriptionNumLines);
      }
      {
         /*
          * Lat/lon digits
          */

         // label
         final Label label = new Label(parent, SWT.NONE);
         label.setText(Messages.Slideout_TourEditor_Label_LatLonDigits);
         label.setToolTipText(Messages.Slideout_TourEditor_Label_LatLonDigits_Tooltip);
         GridDataFactory.fillDefaults()
               .align(SWT.FILL, SWT.CENTER)
               .applyTo(label);

         // spinner
         _spinnerLatLonDigits = new Spinner(parent, SWT.BORDER);
         _spinnerLatLonDigits.setMinimum(0);
         _spinnerLatLonDigits.setMaximum(20);
         _spinnerLatLonDigits.addSelectionListener(widgetSelectedAdapter(selectionEvent -> onSelect_LatLonDigits()));
         _spinnerLatLonDigits.addMouseWheelListener(mouseEvent -> {
            UI.adjustSpinnerValueOnMouseScroll(mouseEvent);
            onSelect_LatLonDigits();
         });
         spinnerGridData.applyTo(_spinnerLatLonDigits);
      }
      {
         /*
          * DEL key actions
          */
         final Composite container = new Composite(parent, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(container);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
         {

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Slideout_TourEditor_Label_DeleteTimeSlices);
            GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

            // checkbox: Keep time
            _chkDelete_KeepTime = new Button(container, SWT.CHECK);
            _chkDelete_KeepTime.setText(Messages.Slideout_TourEditor_Checkbox_KeepTime);
            _chkDelete_KeepTime.setToolTipText(Messages.Slideout_TourEditor_Checkbox_KeepTime_Tooltip);
            _chkDelete_KeepTime.addSelectionListener(_defaultSelectionListener);
            GridDataFactory.fillDefaults().indent(16, 0).applyTo(_chkDelete_KeepTime);

            // radio: solid
            _chkDelete_KeepDistance = new Button(container, SWT.CHECK);
            _chkDelete_KeepDistance.setText(Messages.Slideout_TourEditor_Checkbox_KeepDistance);
            _chkDelete_KeepDistance.setToolTipText(Messages.Slideout_TourEditor_Checkbox_KeepDistance_Tooltip);
            _chkDelete_KeepDistance.addSelectionListener(_defaultSelectionListener);
         }
      }
      {
         /*
          * Recompute elevation up/down when saved
          */
         _chkRecomputeElevation = new Button(parent, SWT.CHECK);
         _chkRecomputeElevation.setText(Messages.Slideout_TourEditor_Checkbox_RecomputeElevationUpDown);
         _chkRecomputeElevation.setToolTipText(Messages.Slideout_TourEditor_Checkbox_RecomputeElevationUpDown_Tooltip);
         _chkRecomputeElevation.addSelectionListener(_defaultSelectionListener);
         GridDataFactory.fillDefaults().span(2, 1).applyTo(_chkRecomputeElevation);
      }
   }

   private void initUI(final Composite parent) {

      _pc = new PixelConverter(parent);

      _hintValueFieldWidth = _pc.convertWidthInCharsToPixels(3);

      _defaultSelectionListener = widgetSelectedAdapter(selectionEvent -> onChangeUI());
   }

   private void onChangeUI() {

      saveState();
   }

   private void onSelect_LatLonDigits() {

      final int latLonDigits = _spinnerLatLonDigits.getSelection();

      _state.put(TourDataEditorView.STATE_LAT_LON_DIGITS, latLonDigits);

      _tourEditorView.updateUI_LatLonDigits(latLonDigits);
   }

   private void onSelect_NumDescriptionLines() {

      final int tourDescriptionNumberOfLines = _spinnerTourDescriptionNumLines.getSelection();
      final int weatherDescriptionNumberOfLines = _spinnerWeatherDescriptionNumLines.getSelection();

      _state.put(TourDataEditorView.STATE_DESCRIPTION_NUMBER_OF_LINES, tourDescriptionNumberOfLines);
      _state.put(TourDataEditorView.STATE_WEATHERDESCRIPTION_NUMBER_OF_LINES, weatherDescriptionNumberOfLines);

      _tourEditorView.updateUI_DescriptionNumLines(tourDescriptionNumberOfLines, weatherDescriptionNumberOfLines);
   }

   @Override
   public void resetToDefaults() {

// SET_FORMATTING_OFF

      final int descriptionNumberOfLines        = TourDataEditorView.STATE_DESCRIPTION_NUMBER_OF_LINES_DEFAULT;
      final int latLonDigits                    = TourDataEditorView.STATE_LAT_LON_DIGITS_DEFAULT;
      final int weatherDescriptionNumberOfLines = TourDataEditorView.STATE_WEATHERDESCRIPTION_NUMBER_OF_LINES_DEFAULT;

      final boolean isDeleteKeepDistance        = TourDataEditorView.STATE_IS_DELETE_KEEP_DISTANCE_DEFAULT;
      final boolean isDeleteKeepTime            = TourDataEditorView.STATE_IS_DELETE_KEEP_TIME_DEFAULT;
      final boolean isRecomputeElevation        = TourDataEditorView.STATE_IS_RECOMPUTE_ELEVATION_UP_DOWN_DEFAULT;

      // update model
      _state.put(TourDataEditorView.STATE_IS_DELETE_KEEP_DISTANCE,         isDeleteKeepDistance);
      _state.put(TourDataEditorView.STATE_IS_DELETE_KEEP_TIME,             isDeleteKeepTime);
      _state.put(TourDataEditorView.STATE_IS_RECOMPUTE_ELEVATION_UP_DOWN,  isRecomputeElevation);
      _state.put(TourDataEditorView.STATE_DESCRIPTION_NUMBER_OF_LINES,     descriptionNumberOfLines);
      _state.put(TourDataEditorView.STATE_LAT_LON_DIGITS,                  latLonDigits);

      // update UI
      _chkDelete_KeepDistance             .setSelection(isDeleteKeepDistance);
      _chkDelete_KeepTime                 .setSelection(isDeleteKeepTime);
      _chkRecomputeElevation              .setSelection(isRecomputeElevation);
      _spinnerLatLonDigits                .setSelection(latLonDigits);
      _spinnerTourDescriptionNumLines     .setSelection(descriptionNumberOfLines);
      _spinnerWeatherDescriptionNumLines  .setSelection(weatherDescriptionNumberOfLines);

// SET_FORMATTING_ON

      _tourEditorView.updateUI_DescriptionNumLines(descriptionNumberOfLines, weatherDescriptionNumberOfLines);
      _tourEditorView.updateUI_LatLonDigits(latLonDigits);
   }

   private void restoreState() {

      _chkDelete_KeepDistance.setSelection(Util.getStateBoolean(_state,
            TourDataEditorView.STATE_IS_DELETE_KEEP_DISTANCE,
            TourDataEditorView.STATE_IS_DELETE_KEEP_DISTANCE_DEFAULT));

      _chkDelete_KeepTime.setSelection(Util.getStateBoolean(_state,
            TourDataEditorView.STATE_IS_DELETE_KEEP_TIME,
            TourDataEditorView.STATE_IS_DELETE_KEEP_TIME_DEFAULT));

      _chkRecomputeElevation.setSelection(Util.getStateBoolean(_state,
            TourDataEditorView.STATE_IS_RECOMPUTE_ELEVATION_UP_DOWN,
            TourDataEditorView.STATE_IS_RECOMPUTE_ELEVATION_UP_DOWN_DEFAULT));

      _spinnerLatLonDigits.setSelection(Util.getStateInt(_state,
            TourDataEditorView.STATE_LAT_LON_DIGITS,
            TourDataEditorView.STATE_LAT_LON_DIGITS_DEFAULT));

      _spinnerTourDescriptionNumLines.setSelection(Util.getStateInt(_state,
            TourDataEditorView.STATE_DESCRIPTION_NUMBER_OF_LINES,
            TourDataEditorView.STATE_DESCRIPTION_NUMBER_OF_LINES_DEFAULT));

      _spinnerWeatherDescriptionNumLines.setSelection(Util.getStateInt(_state,
            TourDataEditorView.STATE_WEATHERDESCRIPTION_NUMBER_OF_LINES,
            TourDataEditorView.STATE_WEATHERDESCRIPTION_NUMBER_OF_LINES_DEFAULT));
   }

   private void saveState() {

      _state.put(TourDataEditorView.STATE_IS_DELETE_KEEP_DISTANCE, _chkDelete_KeepDistance.getSelection());
      _state.put(TourDataEditorView.STATE_IS_DELETE_KEEP_TIME, _chkDelete_KeepTime.getSelection());
      _state.put(TourDataEditorView.STATE_IS_RECOMPUTE_ELEVATION_UP_DOWN, _chkRecomputeElevation.getSelection());
   }

}
