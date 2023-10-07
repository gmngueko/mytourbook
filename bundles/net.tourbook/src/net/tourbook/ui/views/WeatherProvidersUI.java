/*******************************************************************************
 * Copyright (C) 2022, 2023 Frédéric Bard
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.preferences.ITourbookPreferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.PageBook;

public class WeatherProvidersUI {

   private static final IPreferenceStore  _prefStore            = TourbookPlugin.getPrefStore();

   private static final WeatherProvider[] ALL_WEATHER_PROVIDER  = {

         new WeatherProvider(
               IWeatherProvider.Pref_Weather_Provider_None,
               Messages.Pref_Weather_Provider_None),

         new WeatherProvider(
               IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAP_ID,
               IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAP_ID),

         new WeatherProvider(
               IWeatherProvider.WEATHER_PROVIDER_WEATHERAPI_ID,
               IWeatherProvider.WEATHER_PROVIDER_WEATHERAPI_NAME),

         new WeatherProvider(
               IWeatherProvider.WEATHER_PROVIDER_WORLDWEATHERONLINE_ID,
               IWeatherProvider.WEATHER_PROVIDER_WORLDWEATHERONLINE_NAME),

         new WeatherProvider(
               IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAPCUSTOM,
               IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAPCUSTOM)

   };

   private List<IWeatherProvider>         _weatherProviders     = List.of(
         new WeatherProvider_None(),
         new WeatherProvider_OpenWeatherMap(),
         new WeatherProvider_WeatherApi(),
         new WeatherProvider_WorldWeatherOnline(),
         new WeatherProvider_OpenWeatherMapCustom());

   private List<Composite>                _weatherProviderPages = new ArrayList<>();

   private boolean                        _isUpdateUI;

   //private IWeatherProvider               _weatherProvider_OpenWeatherMapCustom = new WeatherProvider_OpenWeatherMapCustom();

   /*
    * UI controls
    */
   private Composite   _uiContainer;
   private PageBook    _pagebookWeatherProvider;
   private FormToolkit _formToolkit;

   //private Composite   _pageOpenWeatherMapCustomUI;

   private Button      _chkDisplayFullLog;
   private Button      _chkSaveLogInTourWeatherDescription;

   private Button      _rdoWeatherDescription_Replace;
   private Button      _rdoWeatherDescription_Append;

   private Combo       _comboWeatherProvider;

   public WeatherProvidersUI() {}

   public static WeatherProvider getCurrentWeatherProvider() {

      final String currentId = _prefStore.getString(ITourbookPreferences.WEATHER_WEATHER_PROVIDER_ID);

      WeatherProvider currentWeatherProvider = null;

      for (final WeatherProvider weatherProvider : ALL_WEATHER_PROVIDER) {

         if (weatherProvider.weatherProviderId.equals(currentId)) {
            currentWeatherProvider = weatherProvider;
            break;
         }
      }

      if (currentWeatherProvider == null) {
         currentWeatherProvider = ALL_WEATHER_PROVIDER[0];
      }

      return currentWeatherProvider;
   }

   public void createUI(final Composite parent) {

      initUI(parent);

      createUI_10(parent);

      setupUI();

      restoreState();
      updateUI();

      enableActions();
   }

   private void createUI_10(final Composite parent) {

      _uiContainer = _formToolkit.createComposite(parent);
      GridDataFactory.fillDefaults()
            .grab(true, true)
            .applyTo(_uiContainer);
      GridLayoutFactory.fillDefaults().numColumns(1).applyTo(_uiContainer);
      {
         createUI_10_WeatherProvider();
         createUI_20_WeatherProvider_PageBook();
         createUI_30_WeatherProvider_MainPreferences();
      }
   }

   private void createUI_10_WeatherProvider() {

      final Composite container = _formToolkit.createComposite(_uiContainer);
      GridDataFactory.fillDefaults().grab(false, false).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(0, 0, 0, 5).applyTo(container);
      {
         /*
          * Label: weather provider
          */
         final Label label = _formToolkit.createLabel(
               container,
               Messages.Pref_Weather_Label_WeatherProvider);
         GridDataFactory.fillDefaults()
               .align(SWT.FILL, SWT.CENTER)
               .applyTo(label);

         /*
          * Combo: weather provider
          */
         _comboWeatherProvider = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
         _comboWeatherProvider.setVisibleItemCount(10);
         _comboWeatherProvider.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> {
            if (_isUpdateUI) {
               return;
            }
            onSelectWeatherProvider();
         }));
         GridDataFactory.fillDefaults()
               .indent(20, 0)
               .applyTo(_comboWeatherProvider);
      }
   }

   private void createUI_20_WeatherProvider_PageBook() {

      /*
       * Pagebook: Weather provider
       */
      _pagebookWeatherProvider = new PageBook(_uiContainer, SWT.NONE);
      GridDataFactory.fillDefaults()
            .grab(true, false)
            .span(2, 1)
            .applyTo(_pagebookWeatherProvider);
      {
         _weatherProviders.forEach(weatherProvider -> _weatherProviderPages.add(weatherProvider.createUI(
               this,
               _pagebookWeatherProvider,
               _formToolkit)));

//         _pageOpenWeatherMapCustomUI = _weatherProvider_OpenWeatherMapCustom.createUI(
//               this,
//               _pagebookWeatherProvider,
//               _formToolkit);

      }
   }

   private void createUI_30_WeatherProvider_MainPreferences() {

      final Composite container = new Composite(_uiContainer, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
      GridLayoutFactory.fillDefaults().applyTo(container);
      {
         // Checkbox: Save the weather log in the tour weather's description
         _chkSaveLogInTourWeatherDescription = new Button(container, SWT.CHECK);
         _chkSaveLogInTourWeatherDescription.setText(Messages.Pref_Weather_Check_SaveLogInTourWeatherDescription);
         _chkSaveLogInTourWeatherDescription.setToolTipText(Messages.Pref_Weather_Check_SaveLogInTourWeatherDescription_Tooltip);
         _chkSaveLogInTourWeatherDescription.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> enableActions()));
         GridDataFactory.fillDefaults().applyTo(_chkSaveLogInTourWeatherDescription);

         final Composite containerWeatherDescription = new Composite(container, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, false)
               .indent(16, 0)
               .applyTo(containerWeatherDescription);
         GridLayoutFactory.fillDefaults().numColumns(1).applyTo(containerWeatherDescription);
         {
            _rdoWeatherDescription_Replace = new Button(containerWeatherDescription, SWT.RADIO);
            _rdoWeatherDescription_Replace.setText(Messages.Pref_Weather_Radio_WeatherDescription_Replace);

            _rdoWeatherDescription_Append = new Button(containerWeatherDescription, SWT.RADIO);
            _rdoWeatherDescription_Append.setText(Messages.Pref_Weather_Radio_WeatherDescription_Append);
         }

         // Checkbox: Display the full log
         _chkDisplayFullLog = new Button(container, SWT.CHECK);
         _chkDisplayFullLog.setText(Messages.Pref_Weather_Check_DisplayFullLog);
         _chkDisplayFullLog.setToolTipText(Messages.Pref_Weather_Check_DisplayFullLog_Tooltip);
         GridDataFactory.fillDefaults().applyTo(_chkDisplayFullLog);
      }
   }

   public void dispose() {

      //_weatherProvider_OpenWeatherMapCustom.dispose();
      _weatherProviders.forEach(IWeatherProvider::dispose);

      _formToolkit.dispose();
   }

   private void enableActions() {

      final boolean isSaveLog = _chkSaveLogInTourWeatherDescription.getSelection();

      _rdoWeatherDescription_Append.setEnabled(isSaveLog);
      _rdoWeatherDescription_Replace.setEnabled(isSaveLog);
   }

   private WeatherProvider getSelectedWeatherProvider() {
      return ALL_WEATHER_PROVIDER[_comboWeatherProvider.getSelectionIndex()];
   }

   private void initUI(final Composite parent) {

      if (_formToolkit != null) {
         return;
      }

      // it could be already created
      _formToolkit = new FormToolkit(parent.getDisplay());
      _formToolkit.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
   }

   private void onSelectWeatherProvider() {

      updateUI();

      saveVendorsState();
   }

   public void performDefaults() {

      _isUpdateUI = true;
      {

// SET_FORMATTING_OFF

         final boolean isDisplayFullLog            = _prefStore.getDefaultBoolean(ITourbookPreferences.WEATHER_DISPLAY_FULL_LOG);
         final boolean isLogWeatherDescription     = _prefStore.getDefaultBoolean(ITourbookPreferences.WEATHER_SAVE_LOG_IN_TOUR_WEATHER_DESCRIPTION);
         final boolean isAppendWeatherDescription  = _prefStore.getDefaultBoolean(ITourbookPreferences.WEATHER_IS_APPEND_WEATHER_DESCRIPTION);
         final String weatherProviderId            = _prefStore.getDefaultString (ITourbookPreferences.WEATHER_WEATHER_PROVIDER_ID);

         _chkDisplayFullLog                  .setSelection(isDisplayFullLog);
         _chkSaveLogInTourWeatherDescription .setSelection(isLogWeatherDescription);

         _rdoWeatherDescription_Append       .setSelection(isAppendWeatherDescription);
         _rdoWeatherDescription_Replace      .setSelection(isAppendWeatherDescription == false);

// SET_FORMATTING_ON

         selectWeatherProvider(weatherProviderId);

         updateUI();

         _weatherProviders.forEach(weatherProvider -> weatherProvider.performDefaults());

         enableActions();
      }
      _isUpdateUI = false;
   }

   private void restoreState() {

      _isUpdateUI = true;
      {

// SET_FORMATTING_OFF

         final boolean isDisplayFullLog            = _prefStore.getBoolean(ITourbookPreferences.WEATHER_DISPLAY_FULL_LOG);
         final boolean isLogWeatherDescription     = _prefStore.getBoolean(ITourbookPreferences.WEATHER_SAVE_LOG_IN_TOUR_WEATHER_DESCRIPTION);
         final boolean isAppendWeatherDescription  = _prefStore.getBoolean(ITourbookPreferences.WEATHER_IS_APPEND_WEATHER_DESCRIPTION);
         final String weatherProviderId            = _prefStore.getString (ITourbookPreferences.WEATHER_WEATHER_PROVIDER_ID);

         // Weather provider
         selectWeatherProvider(weatherProviderId);

         _chkDisplayFullLog                  .setSelection(isDisplayFullLog);
         _chkSaveLogInTourWeatherDescription .setSelection(isLogWeatherDescription);

         _rdoWeatherDescription_Append       .setSelection(isAppendWeatherDescription);
         _rdoWeatherDescription_Replace      .setSelection(isAppendWeatherDescription == false);

// SET_FORMATTING_ON

         enableActions();
      }
      _isUpdateUI = false;
   }

   /**
    * Update new values in the preference store
    */
   public void saveState() {

      _prefStore.setValue(ITourbookPreferences.WEATHER_WEATHER_PROVIDER_ID,
            getSelectedWeatherProvider().weatherProviderId);

      saveVendorsState();
   }

   /**
    * Update new values in the preference store
    */
   public void saveVendorsState() {

      _prefStore.setValue(ITourbookPreferences.WEATHER_DISPLAY_FULL_LOG,
            _chkDisplayFullLog.getSelection());

      //_weatherProvider_OpenWeatherMapCustom.saveState();
      _prefStore.setValue(ITourbookPreferences.WEATHER_SAVE_LOG_IN_TOUR_WEATHER_DESCRIPTION,
            _chkSaveLogInTourWeatherDescription.getSelection());

      _prefStore.setValue(ITourbookPreferences.WEATHER_IS_APPEND_WEATHER_DESCRIPTION,
            _rdoWeatherDescription_Append.getSelection());

      _weatherProviders.forEach(IWeatherProvider::saveState);
   }

   private void selectWeatherProvider(final String prefWeatherProviderId) {

      int prefWeatherProviderIndex = -1;
      for (int weatherProviderIndex = 0; weatherProviderIndex < ALL_WEATHER_PROVIDER.length; weatherProviderIndex++) {

         if (ALL_WEATHER_PROVIDER[weatherProviderIndex].weatherProviderId.equals(prefWeatherProviderId)) {
            prefWeatherProviderIndex = weatherProviderIndex;
            break;
         }
      }

      if (prefWeatherProviderIndex == -1) {

         prefWeatherProviderIndex = 0;
      }

      _comboWeatherProvider.select(prefWeatherProviderIndex);
   }

   private void setupUI() {

      _isUpdateUI = true;
      {
         /*
          * Fillup weather provider combo
          */
         Arrays.asList(ALL_WEATHER_PROVIDER).forEach(

               weatherProvider -> _comboWeatherProvider.add(weatherProvider.uiText));

         _comboWeatherProvider.select(0);
      }
      _isUpdateUI = false;
   }

   private void updateUI() {

      final String selectedWeatherProvider = getSelectedWeatherProvider().weatherProviderId;
      boolean areMainPreferencesVisible = true;

      Control selectedWeatherProviderPage = null;

      if (selectedWeatherProvider.equals(IWeatherProvider.Pref_Weather_Provider_None)) {

         selectedWeatherProviderPage = _weatherProviderPages.get(0);
         areMainPreferencesVisible = false;

      } else if (selectedWeatherProvider.equals(IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAP_ID)) {

         selectedWeatherProviderPage = _weatherProviderPages.get(1);

      } else if (selectedWeatherProvider.equals(IWeatherProvider.WEATHER_PROVIDER_WEATHERAPI_ID)) {

         selectedWeatherProviderPage = _weatherProviderPages.get(2);

      } else if (selectedWeatherProvider.equals(IWeatherProvider.WEATHER_PROVIDER_WORLDWEATHERONLINE_ID)) {

         selectedWeatherProviderPage = _weatherProviderPages.get(3);

      } else if (selectedWeatherProvider.equals(IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAPCUSTOM)) {

         //_pagebookWeatherProvider.showPage(_pageOpenWeatherMapCustomUI);
         selectedWeatherProviderPage = _weatherProviderPages.get(4);
      }

      _pagebookWeatherProvider.showPage(selectedWeatherProviderPage);

      _chkSaveLogInTourWeatherDescription.setVisible(areMainPreferencesVisible);
      _chkDisplayFullLog.setVisible(areMainPreferencesVisible);

      _rdoWeatherDescription_Append.setVisible(areMainPreferencesVisible);
      _rdoWeatherDescription_Replace.setVisible(areMainPreferencesVisible);

      UI.updateScrolledContent(_uiContainer);
   }
}
