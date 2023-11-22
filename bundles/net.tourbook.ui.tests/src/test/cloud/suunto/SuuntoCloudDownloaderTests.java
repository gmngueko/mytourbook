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
package cloud.suunto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pgssoft.httpclient.HttpClientMock;

import java.nio.file.Paths;
import java.util.List;

import net.tourbook.cloud.Activator;
import net.tourbook.cloud.Preferences;
import net.tourbook.cloud.oauth2.OAuth2Utils;
import net.tourbook.cloud.suunto.SuuntoCloudDownloader;
import net.tourbook.common.UI;
import net.tourbook.common.util.FileUtils;
import net.tourbook.extension.download.CloudDownloaderManager;
import net.tourbook.tour.TourLogManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import utils.UITest;
import utils.Utils;

public class SuuntoCloudDownloaderTests extends UITest {

   private static final String           OAUTH_PASSEUR_APP_URL_TOKEN = OAuth2Utils.createOAuthPasseurUri("/suunto/token").toString();   //$NON-NLS-1$
   private static final String           CLOUD_FILES_PATH            = Utils.WORKING_DIRECTORY + "\\src\\test\\cloud\\suunto\\files\\"; //$NON-NLS-1$
   private static final String           VALID_TOKEN_RESPONSE        = Utils.readFileContent(CLOUD_FILES_PATH
         + "Token-Response.json");                                                                                                      //$NON-NLS-1$

   private static final IPreferenceStore _prefStore                  = Activator.getDefault().getPreferenceStore();
   static Object                         initialHttpClient;
   static HttpClientMock                 httpClientMock;
   static SuuntoCloudDownloader          suuntoCloudDownloader;

   @AfterAll
   static void cleanUp() {

      Utils.setHttpClient(initialHttpClient);
   }

   @BeforeAll
   static void initAll() {

      //We set the Suunto account information, otherwise the download can't
      //happen as the context menu will be grayed out.
      _prefStore.setValue(
            Preferences.getSuuntoWorkoutDownloadFolder_Active_Person_String(),
            "./"); //$NON-NLS-1$
      _prefStore.setValue(
            Preferences.getSuuntoWorkoutFilterStartDate_Active_Person_String(),
            "1293840000000"); //$NON-NLS-1$
      _prefStore.setValue(
            Preferences.getSuuntoWorkoutFilterEndDate_Active_Person_String(),
            "1295049600000"); //$NON-NLS-1$
      _prefStore.setValue(
            Preferences.getSuuntoUseWorkoutFilterStartDate_Active_Person_String(),
            true);
      _prefStore.setValue(
            Preferences.getSuuntoUseWorkoutFilterEndDate_Active_Person_String(),
            true);
      _prefStore.setValue(Preferences.SUUNTO_FILENAME_COMPONENTS,
            "{YEAR}{MONTH}{DAY}{USER_TEXT:-}{HOUR}{USER_TEXT:h}{MINUTE}{USER_TEXT:-}{SUUNTO_FILE_NAME}{USER_TEXT:-}{WORKOUT_ID}{USER_TEXT:-}{ACTIVITY_TYPE}{FIT_EXTENSION}"); //$NON-NLS-1$
      _prefStore.setValue(Preferences.getPerson_SuuntoAccessToken_String("0"), "access_token"); //$NON-NLS-1$ //$NON-NLS-2$
      _prefStore.setValue(Preferences.getPerson_SuuntoRefreshToken_String("0"), "refresh_token"); //$NON-NLS-1$ //$NON-NLS-2$
      //We set the access token issue date time in the past to trigger the retrieval
      //of a new token.
      _prefStore.setValue(
            Preferences.getSuuntoAccessTokenIssueDateTime_Active_Person_String(),
            "973701086000"); //$NON-NLS-1$

      initialHttpClient = Utils.getInitialHttpClient();
      httpClientMock = Utils.initializeHttpClientMock();

      suuntoCloudDownloader = (SuuntoCloudDownloader) CloudDownloaderManager.getCloudDownloaderList().stream()
            .filter(tourCloudDownloader -> tourCloudDownloader.getId().equals("SUUNTO")) //$NON-NLS-1$
            .findAny()
            .orElse(null);
   }

   @BeforeEach
   void setUp() {
      Utils.clearTourLogView(bot);
   }

   @AfterEach
   void tearDown() {
      Utils.clearTourLogView(bot);
   }

   @Test
   void testTourDownload() {

      // Arrange
      httpClientMock.onPost(
            OAUTH_PASSEUR_APP_URL_TOKEN)
            .doReturn(VALID_TOKEN_RESPONSE)
            .withStatus(201);

      final String workoutsResponse = Utils.readFileContent(CLOUD_FILES_PATH
            + "Workouts-Response.json"); //$NON-NLS-1$
      httpClientMock.onGet(
            OAuth2Utils.createOAuthPasseurUri("/suunto/workouts?since=1293840000000&until=1295049600000").toString()) //$NON-NLS-1$
            .doReturn(workoutsResponse)
            .withStatus(200);

      final String filename = "2011-01-13.fit"; //$NON-NLS-1$
      httpClientMock.onGet(
            OAuth2Utils.createOAuthPasseurUri("/suunto/workout/exportFit?workoutKey=601227a563c46e612c20b579").toString()) //$NON-NLS-1$
            .doReturn(UI.EMPTY_STRING)
            .withHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            .withStatus(200);

      // Act
      suuntoCloudDownloader.downloadTours();

      bot.sleep(5000);

      // Assert
      httpClientMock.verify().get(OAuth2Utils.createOAuthPasseurUri("/suunto/workouts?since=1293840000000&until=1295049600000").toString()).called(); //$NON-NLS-1$
      httpClientMock.verify().get(OAuth2Utils.createOAuthPasseurUri("/suunto/workout/exportFit?workoutKey=601227a563c46e612c20b579").toString()) //$NON-NLS-1$
            .called();

      List<?> logs = TourLogManager.getLogs();
      assertTrue(logs.stream().map(log -> log.toString()).anyMatch(log -> log.contains(
            "601227a563c46e612c20b579 -> Workout Downloaded to the file:"))); //$NON-NLS-1$

      final String downloadedFilename = "20110112-19h02-2011-01-13-601227a563c46e612c20b579-running.fit"; //$NON-NLS-1$
      assertTrue(logs.stream().map(log -> log.toString()).anyMatch(log -> log.contains(
            downloadedFilename)));

      // Act
      // Attempting to download again
      suuntoCloudDownloader.downloadTours();

      bot.sleep(5000);

      // Assert
      logs = TourLogManager.getLogs();
      assertTrue(logs.stream().map(log -> log.toString()).anyMatch(log -> log.contains(
            "601227a563c46e612c20b579 -> The following file already exists at the location:"))); //$NON-NLS-1$

      FileUtils.deleteIfExists(Paths.get(downloadedFilename));
   }

   @Test
   void tourDownload_TokenRetrieval_NullResponse() {

      // Arrange
      httpClientMock.onPost(
            OAUTH_PASSEUR_APP_URL_TOKEN)
            .doReturn(UI.EMPTY_STRING)
            .withStatus(201);

      // Act
      suuntoCloudDownloader.downloadTours();

      bot.sleep(5000);

      // Assert
      httpClientMock.verify().post(OAUTH_PASSEUR_APP_URL_TOKEN).called();

      final List<?> logs = TourLogManager.getLogs();
      assertTrue(logs.stream().map(log -> log.toString()).anyMatch(log -> log.contains(
            "Action aborted due to invalid tokens"))); //$NON-NLS-1$
   }
}
