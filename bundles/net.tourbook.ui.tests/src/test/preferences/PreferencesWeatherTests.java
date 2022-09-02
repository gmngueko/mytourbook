/*******************************************************************************
 * Copyright (C) 2022 Frédéric Bard
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
package preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.tourbook.Messages;
import net.tourbook.ui.views.IWeatherProvider;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.jupiter.api.Test;

import utils.UITest;

public class PreferencesWeatherTests extends UITest {

   private void testVendorConnection(final String vendorName) {

      bot.comboBox().setSelection(vendorName);
      bot.button(Messages.Pref_Weather_Button_TestHTTPConnection).click();

      final SWTBotShell shell = bot.shell(Messages.Pref_Weather_CheckHTTPConnection_Message);
      final String message = NLS.bind(
            Messages.Pref_Weather_CheckHTTPConnection_OK_Message,
            vendorName);
      assertEquals(message, shell.bot().label(message).getText());

      bot.button("OK").click(); //$NON-NLS-1$
   }

   @Test
   void testVendorConnections() {

      bot.toolbarButtonWithTooltip("Preferences (Ctrl+Shift+P)").click(); //$NON-NLS-1$
      bot.tree().getTreeItem("Weather").select(); //$NON-NLS-1$

      bot.comboBox().setSelection(0);

      //OpenWeatherMap
      testVendorConnection(IWeatherProvider.WEATHER_PROVIDER_OPENWEATHERMAP_ID);

      //Weather API
      testVendorConnection(IWeatherProvider.WEATHER_PROVIDER_WEATHERAPI_NAME);

      bot.button("Apply and Close").click(); //$NON-NLS-1$
   }

}
