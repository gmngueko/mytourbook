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

import java.util.ArrayList;

import net.tourbook.Messages;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ActionRetrieveWeatherOwmData extends Action {
   //TODO replace with default preference
   public static final int      DEFAULT_INTERVAL = 1200;
   private static final double  DEFAULT_LATITUDE  = 50.85;
   private static final double  DEFAULT_LONGITUDE = 4.42;

   private final ITourProvider2 _tourProvider;
   private int                  intervalSeconds  = DEFAULT_INTERVAL;
   private double               defaultOWNLatitude  = DEFAULT_LATITUDE;
   private double               defaultOWNLongitude = DEFAULT_LONGITUDE;

   private class OwmSettingsDialog extends TitleAreaDialog {

      private Text   intervalText;
      private Text   latitudeText;
      private Text   longitudeText;

      private String intervalValue;
      private String latitudeValue;
      private String longitudeValue;

      public OwmSettingsDialog(final Shell parentShell) {
         super(parentShell);
      }

      public void create(final int interval, final double latitude, final double longitude) {
         super.create();
         setTitle(Messages.Dialog_RetrieveWeatherOwm_Dialog_Title);
         setMessage(Messages.Dialog_RetrieveWeatherOwm_Dialog_Message, IMessageProvider.INFORMATION);
         intervalValue = String.valueOf(interval);
         intervalText.setText(intervalValue);
         latitudeValue = String.valueOf(latitude);
         latitudeText.setText(latitudeValue);
         longitudeValue = String.valueOf(longitude);
         longitudeText.setText(longitudeValue);
      }

      @Override
      protected Control createDialogArea(final Composite parent) {
         final Composite area = (Composite) super.createDialogArea(parent);
         final Composite container = new Composite(area, SWT.NONE);
         container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         final GridLayout layout = new GridLayout(2, false);
         container.setLayout(layout);

         createInterval(container);
         createLatitude(container);
         createLongitude(container);

         return area;
      }

      private void createInterval(final Composite container) {
         final Label lbtInterval = new Label(container, SWT.NONE);
         lbtInterval.setText(Messages.Dialog_RetrieveWeatherOwm_Dialog_LabelInterval);

         final GridData dataInterval = new GridData();
         dataInterval.grabExcessHorizontalSpace = true;
         dataInterval.horizontalAlignment = GridData.FILL;
         intervalText = new Text(container, SWT.BORDER);
         intervalText.setLayoutData(dataInterval);
         intervalText.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(final Event e) {
               final String string = e.text;
               final char[] chars = new char[string.length()];
               string.getChars(0, chars.length, chars, 0);
               for (final char element : chars) {
                  if (!('0' <= element && element <= '9')) {
                     e.doit = false;
                     return;
                  }
               }
            }
         });
      }

      private void createLatitude(final Composite container) {
         final Label lbtLatitude = new Label(container, SWT.NONE);
         lbtLatitude.setText(Messages.Dialog_RetrieveWeatherOwm_Dialog_LabelLatitude);

         final GridData dataLatitudeGrid = new GridData();
         dataLatitudeGrid.grabExcessHorizontalSpace = true;
         dataLatitudeGrid.horizontalAlignment = GridData.FILL;
         latitudeText = new Text(container, SWT.BORDER);
         latitudeText.setLayoutData(dataLatitudeGrid);

         latitudeText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(final VerifyEvent e) {

               // get old text and create new text by using the VerifyEvent.text
               final Text text = (Text) e.getSource();

               // get old text and create new text by using the VerifyEvent.text
               final String oldS = text.getText();
               final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

               boolean isFloat = true;
               try {
                  Double.parseDouble(newS);
               } catch (final NumberFormatException ex) {
                  isFloat = false;
               }

               if (!isFloat) {
                  e.doit = false;
               }
            }
         });

      }

      private void createLongitude(final Composite container) {
         final Label lbtLongitude = new Label(container, SWT.NONE);
         lbtLongitude.setText(Messages.Dialog_RetrieveWeatherOwm_Dialog_LabelLongitude);

         final GridData dataLongitudeGrid = new GridData();
         dataLongitudeGrid.grabExcessHorizontalSpace = true;
         dataLongitudeGrid.horizontalAlignment = GridData.FILL;
         longitudeText = new Text(container, SWT.BORDER);
         longitudeText.setLayoutData(dataLongitudeGrid);
         longitudeText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(final VerifyEvent e) {

               // get old text and create new text by using the VerifyEvent.text
               final Text text = (Text) e.getSource();

               // get old text and create new text by using the VerifyEvent.text
               final String oldS = text.getText();
               final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

               boolean isFloat = true;
               try {
                  Double.parseDouble(newS);
               } catch (final NumberFormatException ex) {
                  isFloat = false;
               }

               if (!isFloat) {
                  e.doit = false;
               }
            }
         });
      }

      public String getInterval() {
         return intervalValue;
      }

      public String getLatitude() {
         return latitudeValue;
      }

      public String getLongitude() {
         return longitudeValue;
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
         intervalValue = intervalText.getText();
         latitudeValue = latitudeText.getText();
         longitudeValue = longitudeText.getText();
      }
   }

   public ActionRetrieveWeatherOwmData(final ITourProvider2 tourProvider) {

      super(null, AS_PUSH_BUTTON);

      _tourProvider = tourProvider;

      setText(Messages.Tour_Action_RetrieveWeatherOwmData);
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
               Messages.Dialog_RetrieveWeatherOwm_Dialog_Title,
               Messages.UI_Label_TourIsNotSelected);

         return;
      }

      final OwmSettingsDialog dialog = new OwmSettingsDialog(shell);
      dialog.create(intervalSeconds, defaultOWNLatitude, defaultOWNLongitude);
      if (dialog.open() == Window.OK) {
         try {
            intervalSeconds = Integer.valueOf(dialog.getInterval());
         } catch (final Exception exc) {
            MessageDialog.openInformation(
                  shell,
                  Messages.Dialog_RetrieveWeatherOwm_Dialog_Title,
                  Messages.Dialog_RetrieveWeatherOwm_Dialog_InvalidInterval);
            return;
         }
         try {
            defaultOWNLatitude = Double.valueOf(dialog.getLatitude());
         } catch (final Exception exc) {
            MessageDialog.openInformation(
                  shell,
                  Messages.Dialog_RetrieveWeatherOwm_Dialog_Title,
                  Messages.Dialog_RetrieveWeatherOwm_Dialog_InvalidLatitude);
            return;
         }
         try {
            defaultOWNLongitude = Double.valueOf(dialog.getLongitude());
         } catch (final Exception exc) {
            MessageDialog.openInformation(
                  shell,
                  Messages.Dialog_RetrieveWeatherOwm_Dialog_Title,
                  Messages.Dialog_RetrieveWeatherOwm_Dialog_InvalidLongitude);
            return;
         }
      } else {
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            final ArrayList<TourData> modifiedTours = new ArrayList<>();

            for (final TourData tour : selectedTours) {

               final boolean isDataRetrieved = TourManager.retrieveWeatherOwmData(tour, intervalSeconds, defaultOWNLatitude, defaultOWNLongitude);

               if (isDataRetrieved) {
                  modifiedTours.add(tour);
               }
            }

            if (modifiedTours.size() > 0) {
               TourManager.saveModifiedTours(modifiedTours);
            } else {
               MessageDialog.openInformation(
                     shell,
                     Messages.Dialog_RetrieveWeatherOwm_Dialog_Title,
                     Messages.Dialog_RetrieveWeatherOwm_Label_WeatherDataNotRetrieved);
            }
         }
      });
   }
}
