package net.tourbook.ui.action;

import de.byteholder.geoclipse.map.UI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.tourbook.Messages;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider2;
import net.tourbook.ui.tourChart.ChartLabelMarker;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ActionComputeCustomLaps extends Action {
   public static final String   DIALOG_TITLE                = "Compute Custom Laps from Tours";                //$NON-NLS-1$
   public static final String   DIALOG_MSG                  = "Compute Custom Laps";                           //$NON-NLS-1$
   public static final String   DIALOG_OPEN_TITLE           = "Custom Laps Compute";                           //$NON-NLS-1$
   public static final String   DIALOG_OPEN_MSG             = "No Tracks to Compute Rest Laps";                //$NON-NLS-1$
   public static final String   MENU_NAME                   = "&Compute Custom Laps from tours...";            //$NON-NLS-1$
   public static final String   LABEL_MIN_SPEED             = "Min Speed[K/h] :";                              //$NON-NLS-1$
   public static final String   LABEL_MIN_SPEED_LAP         = "Min Lap Speed[K/h] :";                          //$NON-NLS-1$
   public static final String   LABEL_MIN_POWER             = "Min Power[W] :";                                //$NON-NLS-1$
   public static final String   LABEL_MIN_POWER_LAP         = "Min Lap Power[W] :";                            //$NON-NLS-1$
   public static final String   LABEL_MIN_CADENCE           = "Min Cadence[rpm] :";                            //$NON-NLS-1$
   public static final String   LABEL_MIN_DURATION          = "Min Duration[sec] :";                           //$NON-NLS-1$
   public static final String   LABEL_MIN_CADENCE_LAP       = "Min Lap Cadence[rpm] :";                        //$NON-NLS-1$
   public static final String   LABEL_MIN_DISTANCE_LAP      = "Min Lap Distance[m] :";                         //$NON-NLS-1$
   public static final String   BUTTON_COMPUTE_LAPS         = "Compute Laps";                                  //$NON-NLS-1$
   public static final String   BUTTON_COMPUTE_LAPS_TOOLTIP = "Compute Laps with above settings";              //$NON-NLS-1$
   public static final String   GROUP_THRESHOLD             = "Laps Computation";                              //$NON-NLS-1$
   public static final String   BUTTON_SET_LAPS             = "Set Rest Laps Tag";                             //$NON-NLS-1$
   public static final String   BUTTON_SET_LAPS_TOOLTIP     = "Set if Laps is a Rest based on above settings"; //$NON-NLS-1$
   public static final String   BUTTON_SET_OLD_LAPS         = "Set Rest Orig.Laps Tag";                        //$NON-NLS-1$
   public static final String   BUTTON_SET_OLD_LAPS_TOOLTIP = "Set if Original Laps is a Rest based on above settings"; //$NON-NLS-1$
   public static final String   GROUP_TAGGING               = "Laps Tagging";                                  //$NON-NLS-1$
   public static final String   LAP_LABEL_PREFIX            = "auto-";                                         //$NON-NLS-1$
   public static final String   TAG_AUTO_REST_LAP           = "Auto_Rest_Calc";                                //$NON-NLS-1$
   public static final String   TAG_AUTO_REST_LAP2          = "Auto Rest Calc";                                //$NON-NLS-1$
   public static final String   TAG_REST_LAP                = "Rest";                                          //$NON-NLS-1$
   public static final String   BUTTON_DEL_PREV_CUSTMARKER  = "Del Old Custom";                                //$NON-NLS-1$
   public static final String   BUTTON_DEL_PREV_ALLMARKER   = "Del All Markers";                               //$NON-NLS-1$
   public static final String   BUTTON_DEL_PREV_TOOLTIP     = "Delete Old Custom Markers";                     //$NON-NLS-1$
   public static final String   BUTTON_DEL_ALLMARKER_TOOLTIP= "Delete All Old Markers";                        //$NON-NLS-1$

   public static final int            INIT_LAP_DURATION_SEC       = 5;
   public static final float          INIT_LAP_POWER_WATT         = 60;
   public static final int            INIT_LAP_CADENCE            = 30;
   public static final float          INIT_LAP_SPEED              = 0;
   public static final float          INIT_LAP_DISTANCE           = 0;

   private final ITourProvider2 _tourProvider;

   private TreeMap<Long, TourMarkerInfo> lapTreeList                 = new TreeMap<>();

   private class CustomLapsComputeSettingsDialog extends TitleAreaDialog {

      private Group     _containerThresholdCompute;
      private Group     _containerLapTagging;
      private Text      _textMinSpeed;
      private Text      _textMinSpeedLap;
      private Text      _textMinPower;
      private Text      _textMinPowerLap;
      private Text      _textMinCadence;
      private Text      _textMinCadenceLap;
      private Text      _textMinDuration;
      private Text      _textMinDistanceLap;
      private Button    _buttonComputeLaps;
      private Text      _textLapsComputeLog;
      private Button    _buttonSetLaps;
      private Button _buttonSetOrigLaps;
      private Text      _textLapsSetLog;
      private Button    _buttonMarkerDelPrevCustom;
      private Button    _buttonMarkerDelPrevAll;

      //private Shell shell;

      public CustomLapsComputeSettingsDialog(final Shell parentShell) {
         super(parentShell);
         //shell = parentShell;
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


         _containerThresholdCompute = new Group(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerThresholdCompute);
         GridLayoutFactory.swtDefaults().numColumns(2).applyTo(_containerThresholdCompute);
         _containerThresholdCompute.setText(GROUP_THRESHOLD);
         _containerThresholdCompute.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
         final FontData[] fD = _containerThresholdCompute.getFont().getFontData();
         fD[0].setHeight(12);
         fD[0].setStyle(SWT.BOLD);
         _containerThresholdCompute.setFont(new Font(Display.getCurrent(), fD[0]));
         {
            //minimum speed
            final Label labelSpeed = new Label(_containerThresholdCompute, SWT.NONE);
            labelSpeed.setText(LABEL_MIN_SPEED);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelSpeed);

            _textMinSpeed = new Text(_containerThresholdCompute, SWT.BORDER);
            _textMinSpeed.setText(String.valueOf(INIT_LAP_SPEED));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinSpeed);
            _textMinSpeed.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            final Label labelPower = new Label(_containerThresholdCompute, SWT.NONE);
            labelPower.setText(LABEL_MIN_POWER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelPower);

            _textMinPower = new Text(_containerThresholdCompute, SWT.BORDER);
            _textMinPower.setText(String.valueOf(INIT_LAP_POWER_WATT));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinPower);
            _textMinPower.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            final Label labelCadence = new Label(_containerThresholdCompute, SWT.NONE);
            labelCadence.setText(LABEL_MIN_CADENCE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelCadence);

            _textMinCadence = new Text(_containerThresholdCompute, SWT.BORDER);
            _textMinCadence.setText(String.valueOf(INIT_LAP_CADENCE));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinCadence);
            _textMinCadence.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            final Label labelDuration = new Label(_containerThresholdCompute, SWT.NONE);
            labelDuration.setText(LABEL_MIN_DURATION);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelDuration);

            _textMinDuration = new Text(_containerThresholdCompute, SWT.BORDER);
            _textMinDuration.setText(String.valueOf(INIT_LAP_DURATION_SEC));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinDuration);
            _textMinDuration.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Integer value = Integer.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            _buttonComputeLaps = new Button(_containerThresholdCompute, SWT.NONE);
            _buttonComputeLaps.setText(BUTTON_COMPUTE_LAPS);
            _buttonComputeLaps.setToolTipText(BUTTON_COMPUTE_LAPS_TOOLTIP);
            _buttonComputeLaps.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();
                     final float minSpeed = Float.parseFloat(_textMinSpeed.getText());
                     final float minCad = Float.parseFloat(_textMinCadence.getText());
                     final float minPower = Float.parseFloat(_textMinPower.getText());
                     final int minDuration = Integer.parseInt(_textMinDuration.getText());
                     if (minPower == 0 && minCad == 0 && minSpeed == 0) {
                        //cannot compute anything returning
                        _textLapsComputeLog.append("At least one Threshold value must be bigger then zero!!!!" + UI.NEW_LINE);
                        return;
                     }
                     ClearData();

                     for (final TourData tour : selectedTours) {
                        final TourMarkerInfo tourMarkerInfo = new TourMarkerInfo();
                        final List<Integer> tourLaps = new ArrayList<>();
                        tourMarkerInfo.lapSerieIndex = tourLaps;

                        //final float[] distanceSerie = tour.distanceSerie;
                        final float[] powerSerie = tour.getPowerSerie();
                        final float[] speedSerie = tour.getSpeedSerie();
                        final float[] cadenceSerie = tour.getCadenceSerie();
                        final int[] timeSerie = tour.timeSerie;

                        int currentState = 0;
                        int previousState = 0;
                        int prevIndex = 0;


                        // get serie index
                        for (int serieIndex = 0; serieIndex < timeSerie.length; serieIndex++) {

                           if (serieIndex == 0) {
                              //compute initial state
                              if (minPower > 0 && powerSerie[serieIndex] < minPower) {
                                 currentState = 0;
                              } else if (minCad > 0 && cadenceSerie[serieIndex] < minCad) {
                                 currentState = 0;
                              } else if (minSpeed > 0 && speedSerie[serieIndex] < minSpeed) {
                                 currentState = 0;
                              } else {
                                 currentState = 1;
                              }

                              previousState = currentState;
                           } else {
                              if (minPower > 0 && powerSerie[serieIndex] < minPower) {
                                 currentState = 0;
                              } else if (minCad > 0 && cadenceSerie[serieIndex] < minCad) {
                                 currentState = 0;
                              } else if (minSpeed > 0 && speedSerie[serieIndex] < minSpeed) {
                                 currentState = 0;
                              } else {
                                 currentState = 1;
                              }

                              final int durationLap = serieIndex - prevIndex;
                              if (minDuration > 0) {
                                 if (durationLap > minDuration &&
                                       currentState != previousState) {
                                 tourLaps.add(serieIndex);
                                 previousState = currentState;
                                 prevIndex = serieIndex;
                                 }
                              } else {
                                 if (currentState != previousState) {
                                    tourLaps.add(serieIndex);
                                    previousState = currentState;
                                    prevIndex = serieIndex;
                                 }
                              }
                           }
                        }
                        String lapLog10 = "'idx' start at  1!! " + UI.NEW_LINE + "[";
                        for (int idx = 0; idx < tourLaps.size() && idx < 10; idx++) {
                           final int serieIdx = tourLaps.get(idx);
                           lapLog10 += "idx=" + (serieIdx + 1) + "at sec=" + timeSerie[serieIdx] + ",";
                        }
                        lapLog10 += "...]" + UI.NEW_LINE + "total detected laps"
                              + net.tourbook.common.UI.SYMBOL_COLON + tourLaps.size();

                        lapTreeList.put(tour.getTourId(), tourMarkerInfo);
                        _textLapsComputeLog.append("id=" + tour.getTourId() + " - " + tour.getTourStartTime() + net.tourbook.common.UI.SYMBOL_COLON
                              + UI.NEW_LINE
                              + lapLog10 + UI.NEW_LINE);
                     }
                     break;
                  }
               }
            });

            _textLapsComputeLog = new Text(_containerThresholdCompute, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            //GridDataFactory.fillDefaults().grab(true, false).applyTo(_textLapsComputeLog);
            final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.heightHint = 5 * _textLapsComputeLog.getLineHeight();
            _textLapsComputeLog.setLayoutData(gridData);
         } //end compute section

         _containerLapTagging = new Group(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerLapTagging);
         GridLayoutFactory.swtDefaults().numColumns(2).applyTo(_containerLapTagging);
         _containerLapTagging.setText(GROUP_TAGGING);
         _containerLapTagging.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
         final FontData[] fD2 = _containerLapTagging.getFont().getFontData();
         fD2[0].setHeight(12);
         fD2[0].setStyle(SWT.BOLD);
         _containerLapTagging.setFont(new Font(Display.getCurrent(), fD2[0]));
         {
            final Label labelSpeed = new Label(_containerLapTagging, SWT.NONE);
            labelSpeed.setText(LABEL_MIN_SPEED_LAP);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelSpeed);

            _textMinSpeedLap = new Text(_containerLapTagging, SWT.BORDER);
            _textMinSpeedLap.setText(String.valueOf(INIT_LAP_SPEED));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinSpeedLap);
            _textMinSpeedLap.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            final Label labelPower = new Label(_containerLapTagging, SWT.NONE);
            labelPower.setText(LABEL_MIN_POWER_LAP);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelPower);

            _textMinPowerLap = new Text(_containerLapTagging, SWT.BORDER);
            _textMinPowerLap.setText(String.valueOf(INIT_LAP_POWER_WATT));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinPowerLap);
            _textMinPowerLap.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            final Label labelCadence = new Label(_containerLapTagging, SWT.NONE);
            labelCadence.setText(LABEL_MIN_CADENCE_LAP);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelCadence);

            _textMinCadenceLap = new Text(_containerLapTagging, SWT.BORDER);
            _textMinCadenceLap.setText(String.valueOf(INIT_LAP_CADENCE));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinCadenceLap);
            _textMinCadenceLap.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Integer value = Integer.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            final Label labelDistance = new Label(_containerLapTagging, SWT.NONE);
            labelDistance.setText(LABEL_MIN_DISTANCE_LAP);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelDistance);

            _textMinDistanceLap = new Text(_containerLapTagging, SWT.BORDER);
            _textMinDistanceLap.setText(String.valueOf(INIT_LAP_DISTANCE));
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_textMinDistanceLap);
            _textMinDistanceLap.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final Float value = Float.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals(UI.EMPTY_STRING)) {
                        e.doit = false;
                     }
                  }
               }
            });

            _buttonMarkerDelPrevCustom = new Button(_containerLapTagging, SWT.CHECK);
            _buttonMarkerDelPrevCustom.setText(BUTTON_DEL_PREV_CUSTMARKER);
            _buttonMarkerDelPrevCustom.setToolTipText(BUTTON_DEL_PREV_TOOLTIP);

            _buttonMarkerDelPrevAll = new Button(_containerLapTagging, SWT.CHECK);
            _buttonMarkerDelPrevAll.setText(BUTTON_DEL_PREV_ALLMARKER);
            _buttonMarkerDelPrevAll.setToolTipText(BUTTON_DEL_ALLMARKER_TOOLTIP);

            final Composite groupButtonsSet = new Composite(_containerLapTagging, SWT.NONE);
            GridDataFactory.fillDefaults().grab(false, false).applyTo(groupButtonsSet);
            GridLayoutFactory.swtDefaults().numColumns(1).applyTo(groupButtonsSet);

            _buttonSetLaps = new Button(groupButtonsSet, SWT.NONE);
            _buttonSetLaps.setText(BUTTON_SET_LAPS);
            _buttonSetLaps.setToolTipText(BUTTON_SET_LAPS_TOOLTIP);
            _buttonSetLaps.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();
                     final float minSpeed = Float.parseFloat(_textMinSpeedLap.getText());
                     final float minCad = Float.parseFloat(_textMinCadenceLap.getText());
                     final float minPower = Float.parseFloat(_textMinPowerLap.getText());
                     final float minDistance = Float.parseFloat(_textMinDistanceLap.getText());
                     if (minPower == 0 && minCad == 0 && minSpeed == 0 && minDistance == 0) {
                        //cannot compute anything returning
                        _textLapsSetLog.append("At least one Tagging Threshold value must be bigger then zero!!!!" + UI.NEW_LINE);
                        return;
                     }

                     if (_buttonMarkerDelPrevCustom.getSelection()) {
                        _textLapsSetLog.append("Previous 'Custom Computed Markers' will be deleted!!!!" + UI.NEW_LINE);

                     } else {
                        _textLapsSetLog.append("Previous 'Custom Computed Markers' will NOT be deleted!!" + UI.NEW_LINE);
                     }

                     if (_buttonMarkerDelPrevAll.getSelection()) {
                        _textLapsSetLog.append("All Previous Markers(Computed or not!) will be deleted!!!!" + UI.NEW_LINE);

                     } else {
                        _textLapsSetLog.append("All Previous Markers(Computed or not!) will NOT be deleted!!" + UI.NEW_LINE);
                     }

                     for (final TourData tour : selectedTours) {
                        final TourMarkerInfo tourMarkerInfo = lapTreeList.get(tour.getTourId());
                        if (tourMarkerInfo == null) {
                           continue;
                        }
                        final List<Integer> tourLapSerieIndex = tourMarkerInfo.lapSerieIndex;
                        if (tourLapSerieIndex == null) {
                           continue;
                        }

                        final ArrayList<TourMarker> listOriginalTourMarkers = tour.getTourMarkersSorted();
                        final TreeMap<Integer, TourMarker> calcMarkers = new TreeMap<>();
                        tourMarkerInfo.calcLapMarkers = calcMarkers;

                        if (!_buttonMarkerDelPrevAll.getSelection() && !_buttonMarkerDelPrevCustom.getSelection()) {
                           for (final TourMarker tourMarker : listOriginalTourMarkers) {
                              calcMarkers.put(tourMarker.getSerieIndex(), tourMarker);
                           }
                        } else if (!_buttonMarkerDelPrevAll.getSelection() && _buttonMarkerDelPrevCustom.getSelection()) {
                           for (final TourMarker tourMarker : listOriginalTourMarkers) {
                              final String desc = tourMarker.getDescription();
                              if (!desc.contains(TAG_AUTO_REST_LAP) &&
                                    !desc.contains(TAG_AUTO_REST_LAP2)) {
                                 calcMarkers.put(tourMarker.getSerieIndex(), tourMarker);
                              }
                           }
                        }

                        //tag the computed laps and add them to the overall list/tree
                        //of Markers to be set/put back for the tour
                        int lapCounter = 1;
                        for (final Integer serieIndex : tourLapSerieIndex) {
                           final TourMarker tourMarker = createTourMarker(tour,
                                 LAP_LABEL_PREFIX + Integer.toString(lapCounter),
                                 serieIndex,
                                 serieIndex);
                           String description = "";
                           description += TAG_AUTO_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON;
                           description += net.tourbook.common.UI.SYMBOL_SEMICOLON;
                           tourMarker.setDescription(description);
                           calcMarkers.put(serieIndex, tourMarker);
                           lapCounter++;
                        }
                        //now compute test on Rest Lap on all Markers
                        final float[] distanceSerie = tour.distanceSerie;
                        final float[] cadenceSerie = tour.getCadenceSerie();
                        final float[] powerSerie = tour.getPowerSerie();
                        //final float[] speedSerie = tour.getSpeedSerie();
                        final int[] timeSerie = tour.timeSerie;
                        String logfirst10 = "[";

                        // Get a set of the entries
                        final Set<Entry<Integer, TourMarker>> setCalcMarkers = calcMarkers.entrySet();

                        // Get an iterator
                        final Iterator<Entry<Integer, TourMarker>> itCalcMarkers = setCalcMarkers.iterator();
                        int lapCount = 0;
                        int lastSerieIndex = 0;
                        while (itCalcMarkers.hasNext()) {
                           final Entry<Integer, TourMarker> calcMarker = itCalcMarkers.next();
                           //System.out.print("Key is: "+me.getKey() + " & ");
                           //System.out.println("Value is: "+me.getValue());
                           final int serieIndex = calcMarker.getKey();
                           if (serieIndex > 0) {
                              float distanceLap = 0;
                              float speedLap = 0;
                              float cadenceLap = 0;
                              float powerLap = 0;

                              distanceLap = distanceSerie[serieIndex] - distanceSerie[lastSerieIndex];
                              speedLap = (float) (3.6) * distanceLap / (timeSerie[serieIndex] - timeSerie[lastSerieIndex]);
                              cadenceLap = computeAverageOnInterval(cadenceSerie, lastSerieIndex, serieIndex);
                              powerLap = computeAverageOnInterval(powerSerie, lastSerieIndex, serieIndex);
                              //test if rest or not based on input in dialog
                              Boolean isRest = false;
                              if (minDistance > 0 && distanceLap < minDistance) {
                                 isRest = true;
                              } else if (minSpeed > 0 && speedLap < minSpeed) {
                                 isRest = true;
                              } else if (minPower > 0 && powerLap < minPower) {
                                 isRest = true;
                              } else if (minCad > 0 && cadenceLap < minCad) {
                                 isRest = true;
                              }
                              final String descriptionOld = calcMarker.getValue().getDescription();
                              final String restTrue = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                    + Boolean.toString(true) + net.tourbook.common.UI.SYMBOL_SEMICOLON;
                              final String restFalse = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                    + Boolean.toString(false) + net.tourbook.common.UI.SYMBOL_SEMICOLON;
                              String descriptionNew = "";
                              if (descriptionOld.contains(restFalse)) {
                                 descriptionNew = descriptionOld.replace(restFalse, "");
                                 descriptionNew = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                       + Boolean.toString(isRest) + net.tourbook.common.UI.SYMBOL_SEMICOLON
                                       + descriptionNew;
                              } else if (descriptionOld.contains(restTrue)) {
                                 descriptionNew = descriptionOld.replace(restTrue, "");
                                 descriptionNew = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                       + Boolean.toString(isRest) + net.tourbook.common.UI.SYMBOL_SEMICOLON
                                       + descriptionNew;
                              } else {
                                 descriptionNew = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                       + Boolean.toString(isRest) + net.tourbook.common.UI.SYMBOL_SEMICOLON
                                       + descriptionOld;
                              }
                              calcMarker.getValue().setDescription(descriptionNew);
                              lastSerieIndex = serieIndex;
                              lapCount++;
                              if (isRest) {
                                 tourMarkerInfo.restLap++;
                              } else {
                                 tourMarkerInfo.nonRestLap++;
                              }
                              if (lapCount < 10) {
                                 logfirst10 += Boolean.toString(isRest) + ",";
                              } else if (lapCount == 10) {
                                 logfirst10 += Boolean.toString(isRest) + ",...]";
                              }
                           }
                        }
                        tourMarkerInfo.lapCount = lapCount;
                        _textLapsSetLog.append(tour.getTourStartTime() + "laps:" + tourMarkerInfo.lapCount
                              + ";rest:" + tourMarkerInfo.restLap + ";non-rest:" + tourMarkerInfo.nonRestLap
                              + net.tourbook.common.UI.NEW_LINE);

                        _textLapsSetLog.append(logfirst10 + net.tourbook.common.UI.NEW_LINE);
                     }

                     break;
                  }
               }
            });

            _buttonSetOrigLaps = new Button(groupButtonsSet, SWT.NONE);
            _buttonSetOrigLaps.setText(BUTTON_SET_OLD_LAPS);
            _buttonSetOrigLaps.setToolTipText(BUTTON_SET_OLD_LAPS_TOOLTIP);
            _buttonSetOrigLaps.addListener(SWT.Selection, new Listener() {
               @Override
               public void handleEvent(final Event e) {
                  switch (e.type) {
                  case SWT.Selection:
                     final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();
                     final float minSpeed = Float.parseFloat(_textMinSpeedLap.getText());
                     final float minCad = Float.parseFloat(_textMinCadenceLap.getText());
                     final float minPower = Float.parseFloat(_textMinPowerLap.getText());
                     final float minDistance = Float.parseFloat(_textMinDistanceLap.getText());
                     if (minPower == 0 && minCad == 0 && minSpeed == 0 && minDistance == 0) {
                        //cannot compute anything returning
                        _textLapsSetLog.append("At least one Tagging Threshold value must be bigger then zero!!!!" + UI.NEW_LINE);
                        return;
                     }

                     _textLapsSetLog.append("Set Rest Tag on Original laps(not computed)!!" + UI.NEW_LINE);
                     ClearData();
                     String logfirst10 = "[";

                     for (final TourData tour : selectedTours) {
                        final TourMarkerInfo tourMarkerInfo = new TourMarkerInfo();
                        lapTreeList.put(tour.getTourId(), tourMarkerInfo);

                        final ArrayList<TourMarker> listOriginalTourMarkers = tour.getTourMarkersSorted();
                        final TreeMap<Integer, TourMarker> calcMarkers = new TreeMap<>();
                        tourMarkerInfo.calcLapMarkers = calcMarkers;

                        for (final TourMarker tourMarker : listOriginalTourMarkers) {
                           calcMarkers.put(tourMarker.getSerieIndex(), tourMarker);
                        }

                        //now compute test on Rest Lap on all Markers
                        final float[] distanceSerie = tour.distanceSerie;
                        final float[] cadenceSerie = tour.getCadenceSerie();
                        final float[] powerSerie = tour.getPowerSerie();
                        //final float[] speedSerie = tour.getSpeedSerie();
                        final int[] timeSerie = tour.timeSerie;

                        // Get a set of the entries
                        final Set<Entry<Integer, TourMarker>> setCalcMarkers = calcMarkers.entrySet();

                        // Get an iterator
                        final Iterator<Entry<Integer, TourMarker>> itCalcMarkers = setCalcMarkers.iterator();
                        int lapCount = 0;
                        int lastSerieIndex = 0;
                        while (itCalcMarkers.hasNext()) {
                           final Entry<Integer, TourMarker> calcMarker = itCalcMarkers.next();
                           //System.out.print("Key is: "+me.getKey() + " & ");
                           //System.out.println("Value is: "+me.getValue());
                           final int serieIndex = calcMarker.getKey();
                           if (serieIndex > 0) {
                              float distanceLap = 0;
                              float speedLap = 0;
                              float cadenceLap = 0;
                              float powerLap = 0;

                              distanceLap = distanceSerie[serieIndex] - distanceSerie[lastSerieIndex];
                              speedLap = (float) (3.6) * distanceLap / (timeSerie[serieIndex] - timeSerie[lastSerieIndex]);
                              cadenceLap = computeAverageOnInterval(cadenceSerie, lastSerieIndex, serieIndex);
                              powerLap = computeAverageOnInterval(powerSerie, lastSerieIndex, serieIndex);
                              //test if rest or not based on input in dialog
                              Boolean isRest = false;
                              if (minDistance > 0 && distanceLap < minDistance) {
                                 isRest = true;
                              } else if (minSpeed > 0 && speedLap < minSpeed) {
                                 isRest = true;
                              } else if (minPower > 0 && powerLap < minPower) {
                                 isRest = true;
                              } else if (minCad > 0 && cadenceLap < minCad) {
                                 isRest = true;
                              }
                              final String descriptionOld = calcMarker.getValue().getDescription();
                              final String restTrue = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                    + Boolean.toString(true) + net.tourbook.common.UI.SYMBOL_SEMICOLON;
                              final String restFalse = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                    + Boolean.toString(false) + net.tourbook.common.UI.SYMBOL_SEMICOLON;
                              String descriptionNew = "";
                              if (descriptionOld.contains(restFalse)) {
                                 descriptionNew = descriptionOld.replace(restFalse, "");
                                 descriptionNew = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                       + Boolean.toString(isRest) + net.tourbook.common.UI.SYMBOL_SEMICOLON
                                       + descriptionNew;
                              } else if (descriptionOld.contains(restTrue)) {
                                 descriptionNew = descriptionOld.replace(restTrue, "");
                                 descriptionNew = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                       + Boolean.toString(isRest) + net.tourbook.common.UI.SYMBOL_SEMICOLON
                                       + descriptionNew;
                              } else {
                                 descriptionNew = TAG_REST_LAP + net.tourbook.common.UI.SYMBOL_COLON
                                       + Boolean.toString(isRest) + net.tourbook.common.UI.SYMBOL_SEMICOLON
                                       + descriptionOld;
                              }
                              calcMarker.getValue().setDescription(descriptionNew);
                              lastSerieIndex = serieIndex;
                              lapCount++;
                              if (isRest) {
                                 tourMarkerInfo.restLap++;
                              } else {
                                 tourMarkerInfo.nonRestLap++;
                              }

                              if (lapCount < 10) {
                                 logfirst10 += Boolean.toString(isRest) + ",";
                              } else if (lapCount == 10) {
                                 logfirst10 += Boolean.toString(isRest) + ",...]";
                              }
                           }
                        }
                        tourMarkerInfo.lapCount = lapCount;
                        _textLapsSetLog.append(tour.getTourStartTime() + "laps:" + tourMarkerInfo.lapCount
                              + ";rest:" + tourMarkerInfo.restLap + ";non-rest:" + tourMarkerInfo.nonRestLap
                              + net.tourbook.common.UI.NEW_LINE);

                        _textLapsSetLog.append(logfirst10 + net.tourbook.common.UI.NEW_LINE);
                     }

                     break;
                  }
               }
            });

            _textLapsSetLog = new Text(_containerLapTagging, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            //GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textMinSpeed);
            //GridDataFactory.fillDefaults().grab(true, false).applyTo(_textLapsComputeLog);
            final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.heightHint = 5 * _textLapsSetLog.getLineHeight();
            _textLapsSetLog.setLayoutData(gridData);
         } //end tagging section

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

   public class TourMarkerInfo {
      List<Integer> lapSerieIndex;
      TreeMap<Integer, TourMarker> calcLapMarkers;
      int                          lapCount   = 0;
      int                          restLap    = 0;
      int                          nonRestLap = 0;
   }

   public ActionComputeCustomLaps(final ITourProvider2 tourProvider) {

      super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);

      _tourProvider = tourProvider;

      setText(MENU_NAME);
   }

   public void ClearData() {
      for (final Entry<Long, TourMarkerInfo> treeListEntry : lapTreeList.entrySet()) {
         if (treeListEntry.getValue() != null) {
            if (treeListEntry.getValue().lapSerieIndex != null) {
               treeListEntry.getValue().lapSerieIndex.clear();
            }
            if (treeListEntry.getValue().calcLapMarkers != null) {
               treeListEntry.getValue().calcLapMarkers.clear();
            }
         }
      }
      lapTreeList.clear();
   }

   private float computeAverageOnInterval(final float[] dataSerie, final int startIdx, final int endIdx) {
      float returnValue = 0;
      int count = 0;
      int idxStart = startIdx;
      if (startIdx < 0) {
         idxStart = 0;
      }
      for (int idx = idxStart; idx < endIdx && idx < dataSerie.length; idx++) {
         returnValue += dataSerie[idx];
         count++;
      }
      returnValue /= count;
      return returnValue;
   }

   private TourMarker createTourMarker(final TourData tourData,
                                       final String label,
                                       final int lapRelativeTime,
                                       final int serieIndex) {

      final float[] altitudeSerie = tourData.altitudeSerie;
      final float[] distanceSerie = tourData.distanceSerie;
      final double[] latitudeSerie = tourData.latitudeSerie;
      final double[] longitudeSerie = tourData.longitudeSerie;

      final TourMarker tourMarker = new TourMarker(tourData, ChartLabelMarker.MARKER_TYPE_CUSTOM);

      tourMarker.setLabel(label);
      tourMarker.setSerieIndex(serieIndex);
      tourMarker.setTime(lapRelativeTime, tourData.getTourStartTimeMS() + (lapRelativeTime * 1000));

      if (distanceSerie != null) {
         tourMarker.setDistance(distanceSerie[serieIndex]);
      }

      if (altitudeSerie != null) {
         tourMarker.setAltitude(altitudeSerie[serieIndex]);
      }

      if (latitudeSerie != null) {
         tourMarker.setGeoPosition(latitudeSerie[serieIndex], longitudeSerie[serieIndex]);
      }
      return tourMarker;
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
               DIALOG_OPEN_TITLE,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }
      ClearData();
      final CustomLapsComputeSettingsDialog dialog = new CustomLapsComputeSettingsDialog(shell);
      dialog.create();
      if (dialog.open() == Window.OK) {
         //retrieve UI data before execution of custom trak loading below
         //put mapping of objet type to custom tracks

      } else {
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {
               final TourMarkerInfo tourMarkerInfo = lapTreeList.get(tour.getTourId());
               if (tourMarkerInfo != null && tourMarkerInfo.calcLapMarkers != null && !tourMarkerInfo.calcLapMarkers.isEmpty()) {
                  final Set<TourMarker> tourMarkersNew = new HashSet<>();
                  for (final TourMarker tourmarker : tourMarkerInfo.calcLapMarkers.values()) {
                     tourMarkersNew.add(tourmarker);
                  }
                  tour.setTourMarkers(tourMarkersNew);
                  modifiedTours.add(tour);
               }
               /*
                * final boolean isDataRetrieved = DeleteCustomTracksfromTour(tour, trackList);
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
