package net.tourbook.ui.action;

import de.byteholder.geoclipse.map.UI;

import java.util.ArrayList;

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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
   public static final String   DIALOG_TITLE        = "Compute Custom Laps from Tours";     //$NON-NLS-1$
   public static final String   DIALOG_MSG          = "Compute Custom Laps";                //$NON-NLS-1$
   public static final String   DIALOG_OPEN_TITLE   = "Custom Laps Compute";                //$NON-NLS-1$
   public static final String   DIALOG_OPEN_MSG     = "No Tracks to delete";                //$NON-NLS-1$
   public static final String   MENU_NAME           = "&Compute Custom Laps from tours..."; //$NON-NLS-1$
   public static final String   LABEL_MIN_SPEED       = "Min Speed(k/h) :";                   //$NON-NLS-1$
   public static final String   LABEL_MIN_SPEED_LAP   = "Min Lap Speed(k/h) :";               //$NON-NLS-1$
   public static final String   LABEL_MIN_POWER       = "Min Power(w) :";                     //$NON-NLS-1$
   public static final String   LABEL_MIN_POWER_LAP   = "Min Lap Speed(w) :";                 //$NON-NLS-1$
   public static final String   LABEL_MIN_CADENCE     = "Min Cadence(rpm) :";                 //$NON-NLS-1$
   public static final String   LABEL_MIN_DURATION     = "Min Duration(sec) :";                //$NON-NLS-1$
   public static final String   LABEL_MIN_CADENCE_LAP = "Min Lap Cadence(rpm) :";             //$NON-NLS-1$
   public static final String   LABEL_MIN_DISTANCE_LAP = "Min Lap Distance(m) :";              //$NON-NLS-1$
   public static final String   BUTTON_COMPUTE_LAPS    = "Compute Laps";                       //$NON-NLS-1$
   public static final String   BUTTON_COMPUTE_LAPS_TOOLTIP = "Compute Laps with above settings";   //$NON-NLS-1$
   public static final String   GROUP_THRESHOLD             = "Lap Computation";                    //$NON-NLS-1$

   private final ITourProvider2 _tourProvider;

   private class CustomLapsComputeSettingsDialog extends TitleAreaDialog {

      private Group     _containerThresholdCompute;
      private Composite _containerLapTagging;
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

      private Shell shell;

      public CustomLapsComputeSettingsDialog(final Shell parentShell) {
         super(parentShell);
         shell = parentShell;
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
         {
            //minimum speed
            final Label labelSpeed = new Label(_containerThresholdCompute, SWT.NONE);
            labelSpeed.setText(LABEL_MIN_SPEED);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelSpeed);

            _textMinSpeed = new Text(_containerThresholdCompute, SWT.BORDER);
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
         }

         _containerLapTagging = new Composite(parent, SWT.BORDER);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(_containerLapTagging);
         GridLayoutFactory.swtDefaults().numColumns(2).applyTo(_containerLapTagging);
         {
            final Label label = new Label(_containerLapTagging, SWT.NONE);
            label.setText(LABEL_MIN_SPEED_LAP);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _textMinSpeedLap = new Text(_containerLapTagging, SWT.BORDER);
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

   public ActionComputeCustomLaps(final ITourProvider2 tourProvider) {

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
               DIALOG_OPEN_TITLE,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }

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
