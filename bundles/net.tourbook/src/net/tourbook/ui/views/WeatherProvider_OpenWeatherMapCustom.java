/*******************************************************************************
 * Copyright (C) 2022 Gervais-Martial Ngueko
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
package net.tourbook.ui.views;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.util.StringUtils;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.TourLogManager;
import net.tourbook.tour.TourLogState;
import net.tourbook.weather.HistoricalWeatherOwmRetriever;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class WeatherProvider_OpenWeatherMapCustom implements IWeatherProvider {

   private final IPreferenceStore _prefStore = TourbookPlugin.getPrefStore();

   /*
    * UI controls
    */
   private Button _btnTestConnectionOwm;

   private Label  _labelApiOwmKey;
   private Text   _textApiOwmKey;

   public WeatherProvider_OpenWeatherMapCustom() {}

   @Override
   public Composite createUI(final WeatherProvidersUI weatherProvidersUI, final Composite parent, final FormToolkit formToolkit) {
      // TODO Auto-generated method stub
      final int defaultHIndent = 16;

      final Composite container = formToolkit.createComposite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.fillDefaults().applyTo(container);
      {
         /*
          * OWM API key
          */
         // label
         _labelApiOwmKey = new Label(container, SWT.WRAP);
         _labelApiOwmKey.setText(Messages.Pref_WeatherOwm_Label_ApiKey);
         GridDataFactory.fillDefaults()
               .indent(defaultHIndent, 0)
               .align(SWT.FILL, SWT.CENTER)
               .applyTo(_labelApiOwmKey);

         // text
         _textApiOwmKey = new Text(container, SWT.BORDER);
         _textApiOwmKey.setToolTipText(Messages.Pref_WeatherOwm_Label_ApiKey_Tooltip);
         _textApiOwmKey.addModifyListener(event -> onModifyApiKey());
         GridDataFactory.fillDefaults()
               .grab(true, false)
               .applyTo(_textApiOwmKey);

      }
      {
         /*
          * Button: test connection OWM
          */
         _btnTestConnectionOwm = new Button(container, SWT.NONE);
         _btnTestConnectionOwm.setText(Messages.Pref_WeatherOwm_Button_TestHTTPConnection);
         _btnTestConnectionOwm.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
               onCheckConnectionOwm();
            }
         });
         GridDataFactory.fillDefaults()
               .indent(defaultHIndent, 0)
               .align(SWT.BEGINNING, SWT.FILL)
               .span(2, 1)
               .applyTo(_btnTestConnectionOwm);
      }

      restoreState();
      enableControls();

      return container;
   }

   @Override
   public void dispose() {
   }

   private void enableControls() {
      _labelApiOwmKey.setEnabled(true);
      _textApiOwmKey.setEnabled(true);
      onModifyApiKey();
   }

   /**
    * This method ensures the connection to the OWM API can be made successfully.
    */
   private void onCheckConnectionOwm() {

      final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {

            try {

               final String apiUrl = HistoricalWeatherOwmRetriever.getTestApiUrl();
               final HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl + _textApiOwmKey
                     .getText()))
                     .GET()
                     .build();

               final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

               final int statusCode = response.statusCode();
               final String responseMessage = response.body();

               final String message = statusCode == HttpURLConnection.HTTP_OK
                     ? NLS.bind(Messages.Pref_Weather_CheckHTTPConnection_OK_Message, apiUrl)
                     : NLS.bind(
                           Messages.Pref_Weather_CheckHTTPConnection_FAILED_Message,
                           new Object[] {
                                 apiUrl,
                                 statusCode,
                                 responseMessage });

               if (statusCode == HttpURLConnection.HTTP_OK) {
                  TourLogManager.addSubLog(TourLogState.OK, "TEST OWM Connection result=>\n" + responseMessage);
               } else {
                  TourLogManager.addSubLog(TourLogState.ERROR, "TEST OWM Connection result=>\n" + responseMessage);
               }
               MessageDialog.openInformation(
                     Display.getCurrent().getActiveShell(),
                     Messages.Pref_Weather_CheckHTTPConnection_Message,
                     message);

            } catch (final IOException | InterruptedException e) {
               e.printStackTrace();
            }
         }
      });
   }

   private void onModifyApiKey() {
      _btnTestConnectionOwm.setEnabled(StringUtils.hasContent(_textApiOwmKey.getText()));
   }

   @Override
   public void performDefaults() {
      _textApiOwmKey.setText(_prefStore.getDefaultString(ITourbookPreferences.WEATHER_OWM_API_KEY));

   }

   private void restoreState() {
      _textApiOwmKey.setText(_prefStore.getString(ITourbookPreferences.WEATHER_OWM_API_KEY));
   }

   @Override
   public void saveState() {
      _prefStore.setValue(ITourbookPreferences.WEATHER_OWM_API_KEY, _textApiOwmKey.getText());

   }

}
