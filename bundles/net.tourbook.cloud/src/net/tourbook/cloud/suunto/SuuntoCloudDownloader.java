/*******************************************************************************
 * Copyright (C) 2021 Frédéric Bard
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
package net.tourbook.cloud.suunto;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.tourbook.cloud.Activator;
import net.tourbook.cloud.Preferences;
import net.tourbook.cloud.oauth2.OAuth2Constants;
import net.tourbook.cloud.suunto.workouts.Payload;
import net.tourbook.cloud.suunto.workouts.Workouts;
import net.tourbook.common.UI;
import net.tourbook.common.util.SQL;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.StringUtils;
import net.tourbook.database.TourDatabase;
import net.tourbook.extension.download.TourbookCloudDownloader;
import net.tourbook.tour.TourLogManager;
import net.tourbook.tour.TourLogState;

import org.apache.http.HttpHeaders;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class SuuntoCloudDownloader extends TourbookCloudDownloader {

   // SET_FORMATTING_OFF
   private static final String   ICON_CHECK                    = net.tourbook.cloud.Messages.Icon_Check;
   private static final String   ICON_HOURGLASS                = net.tourbook.cloud.Messages.Icon_Hourglass;
   private static final String   LOG_CLOUDACTION_END           = net.tourbook.cloud.Messages.Log_CloudAction_End;
   private static final String   LOG_CLOUDACTION_INVALIDTOKENS = net.tourbook.cloud.Messages.Log_CloudAction_InvalidTokens;
   // SET_FORMATTING_ON

   private static HttpClient       _httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(5)).build();
   private static IPreferenceStore _prefStore  = Activator.getDefault().getPreferenceStore();
   private static int[]            _numberOfDownloadedTours;
   private int[]                   _numberOfAvailableTours;

   public SuuntoCloudDownloader() {

      super("SUUNTO", //$NON-NLS-1$
            Messages.VendorName_Suunto_Workouts,
            Messages.Suunto_Workouts_Description,
            Activator.getImageAbsoluteFilePath(Messages.Image__SuuntoApp_Icon));
   }

   private static void logDownloadResult(final WorkoutDownload workoutDownload) {

      if (workoutDownload.isSuccessfullyDownloaded()) {

         ++_numberOfDownloadedTours[0];

         TourLogManager.addLog(TourLogState.IMPORT_OK,
               NLS.bind(Messages.Log_DownloadWorkoutsToSuunto_005_DownloadStatus,
                     workoutDownload.getWorkoutKey(),
                     workoutDownload.getAbsoluteFilePath()));
      } else {
         TourLogManager.logError(workoutDownload.getError());
      }
   }

   private CompletableFuture<WorkoutDownload> downloadFile(final String workoutKey) {

      final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OAuth2Constants.HEROKU_APP_URL + "/suunto/workout/exportFit?workoutKey=" + workoutKey))//$NON-NLS-1$
            .header(HttpHeaders.AUTHORIZATION, OAuth2Constants.BEARER + getAccessToken())
            .GET()
            .build();

      return sendAsyncRequest(workoutKey, request);
   }

   private void downloadFiles(final List<Payload> newWorkouts) {

      final List<CompletableFuture<WorkoutDownload>> workoutDownloads = new ArrayList<>();

      newWorkouts.stream().forEach(newWorkout -> workoutDownloads.add(downloadFile(newWorkout.workoutKey)));

      workoutDownloads.stream().map(CompletableFuture::join).forEach(SuuntoCloudDownloader::logDownloadResult);

   }

   @Override
   public void downloadTours() {

      if (!isReady()) {

         final int returnResult = PreferencesUtil.createPreferenceDialogOn(
               Display.getCurrent().getActiveShell(),
               PrefPageSuunto.ID,
               null,
               null).open();

         if (returnResult != 0) {// The OK button was not clicked or if the configuration is still not ready
            return;
         }
      }

      _numberOfAvailableTours = new int[1];
      _numberOfDownloadedTours = new int[1];

      final IRunnableWithProgress runnable = new IRunnableWithProgress() {

         @Override
         public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            monitor.beginTask(Messages.DownloadWorkoutsFromSuunto_Task, 2);

            monitor.subTask(Messages.ValidatingSuuntoTokens_SubTask);

            if (!SuuntoTokensRetrievalHandler.getValidTokens()) {
               TourLogManager.logError(LOG_CLOUDACTION_INVALIDTOKENS);
               return;
            }

            if (StringUtils.isNullOrEmpty(getDownloadFolder())) {
               TourLogManager.logError(Messages.Log_DownloadWorkoutsToSuunto_004_NoSpecifiedFolder);
               return;
            }

            monitor.subTask(NLS.bind(Messages.DownloadWorkoutsFromSuunto_SubTask,
                  new Object[] {
                        ICON_HOURGLASS,
                        UI.EMPTY_STRING,
                        UI.EMPTY_STRING }));

            //Get the list of workouts
            final Workouts workouts = retrieveWorkoutsList();
            if (workouts.payload.size() == 0) {
               TourLogManager.logInfo(Messages.Log_DownloadWorkoutsToSuunto_002_NewWorkoutsNotFound);
               return;
            }

            final List<Long> tourStartTimes = retrieveAllTourStartTimes();

            //Identifying the workouts that have not yet been imported in the tour database
            final List<Payload> newWorkouts = workouts.payload.stream()
                  .filter(suuntoWorkout -> !tourStartTimes.contains(suuntoWorkout.startTime / 1000L * 1000L))
                  .collect(Collectors.toList());

            final int numNewWorkouts = newWorkouts.size();
            if (numNewWorkouts == 0) {
               TourLogManager.logInfo(Messages.Log_DownloadWorkoutsToSuunto_003_AllWorkoutsAlreadyExist);
               return;
            }

            _numberOfAvailableTours[0] = numNewWorkouts;

            monitor.worked(1);

            monitor.subTask(NLS.bind(Messages.DownloadWorkoutsFromSuunto_SubTask,
                  new Object[] {
                        ICON_CHECK,
                        _numberOfAvailableTours[0],
                        ICON_HOURGLASS }));

            downloadFiles(newWorkouts);

            monitor.worked(1);
         }

      };

      try {
         final long start = System.currentTimeMillis();

         TourLogManager.showLogView();
         TourLogManager.logTitle(Messages.Log_DownloadWorkoutsToSuunto_001_Start);

         new ProgressMonitorDialog(Display.getCurrent().getActiveShell()).run(true, false, runnable);

         TourLogManager.logTitle(String.format(LOG_CLOUDACTION_END, (System.currentTimeMillis() - start) / 1000.0));

         MessageDialog.openInformation(
               Display.getDefault().getActiveShell(),
               Messages.Dialog_WorkoutsDownload_Summary,
               NLS.bind(Messages.Dialog_WorkoutsDownload_Message,
                     _numberOfDownloadedTours[0],
                     _numberOfAvailableTours[0] - _numberOfDownloadedTours[0]));

      } catch (final InvocationTargetException | InterruptedException e) {
         StatusUtil.log(e);
         Thread.currentThread().interrupt();
      }

   }

   private String getAccessToken() {
      return _prefStore.getString(Preferences.SUUNTO_ACCESSTOKEN);
   }

   private String getDownloadFolder() {
      return _prefStore.getString(Preferences.SUUNTO_WORKOUT_DOWNLOAD_FOLDER);
   }

   private String getRefreshToken() {
      return _prefStore.getString(Preferences.SUUNTO_REFRESHTOKEN);
   }

   @Override
   protected boolean isReady() {
      return StringUtils.hasContent(getAccessToken()) && StringUtils.hasContent(getRefreshToken()) &&
            StringUtils.hasContent(getDownloadFolder());
   }

   private List<Long> retrieveAllTourStartTimes() {

      final List<Long> tourStartTimes = new ArrayList<>();
      try (Connection conn = TourDatabase.getInstance().getConnection();
            Statement stmt = conn.createStatement()) {

         final String sqlQuery = "SELECT tourStartTime FROM " + TourDatabase.TABLE_TOUR_DATA; //$NON-NLS-1$

         final ResultSet result = stmt.executeQuery(sqlQuery);

         while (result.next()) {

            tourStartTimes.add(result.getLong(1));
         }

      } catch (final SQLException e) {
         SQL.showException(e);
      }
      return tourStartTimes;
   }

   private Workouts retrieveWorkoutsList() {

      final var sinceDateFilter = _prefStore.getLong(Preferences.SUUNTO_WORKOUT_FILTER_SINCE_DATE);
      final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OAuth2Constants.HEROKU_APP_URL + "/suunto/workouts?since=" + sinceDateFilter))//$NON-NLS-1$
            .header(HttpHeaders.AUTHORIZATION, OAuth2Constants.BEARER + getAccessToken())
            .GET()
            .build();

      try {
         final HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

         if (response.statusCode() == HttpURLConnection.HTTP_OK && StringUtils.hasContent(response.body())) {

            return new ObjectMapper().readValue(response.body(), Workouts.class);
         }
      } catch (IOException | InterruptedException e) {
         StatusUtil.log(e);
         Thread.currentThread().interrupt();
      }

      return new Workouts();
   }

   private CompletableFuture<WorkoutDownload> sendAsyncRequest(final String workoutKey, final HttpRequest request) {

      final CompletableFuture<WorkoutDownload> workoutDownload = _httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
            .thenApply(response -> writeFileToFolder(workoutKey, response))
            .exceptionally(e -> {
               final WorkoutDownload erroneousDownload = new WorkoutDownload(workoutKey);
               erroneousDownload.setError(NLS.bind(Messages.Log_DownloadWorkoutsToSuunto_007_Error,
                     erroneousDownload.getWorkoutKey(),
                     e.getMessage()));
               erroneousDownload.setSuccessfullyDownloaded(false);
               return erroneousDownload;
            });

      return workoutDownload;
   }

   private WorkoutDownload writeFileToFolder(final String workoutKey, final HttpResponse<InputStream> response) {

      final WorkoutDownload workoutDownload = new WorkoutDownload(workoutKey);

      final Optional<String> contentDisposition = response.headers().firstValue("Content-Disposition"); //$NON-NLS-1$

      String fileName = UI.EMPTY_STRING;
      if (contentDisposition.isPresent()) {
         fileName = contentDisposition.get().replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
      }

      final Path filePath = Paths.get(_prefStore.getString(Preferences.SUUNTO_WORKOUT_DOWNLOAD_FOLDER), StringUtils.sanitizeFileName(fileName));
      workoutDownload.setAbsoluteFilePath(filePath.toAbsolutePath().toString());

      if (filePath.toFile().exists()) {

         workoutDownload.setError(NLS.bind(Messages.Log_DownloadWorkoutsToSuunto_006_FileAlreadyExists,
               workoutDownload.getWorkoutKey(),
               filePath.toAbsolutePath().toString()));
         return workoutDownload;
      }

      try (InputStream inputStream = response.body();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {

         int inByte;
         while ((inByte = inputStream.read()) != -1) {
            fileOutputStream.write(inByte);
         }

      } catch (final IOException e) {
         StatusUtil.log(e);
         workoutDownload.setError(e.getMessage());
         return workoutDownload;
      }

      workoutDownload.setSuccessfullyDownloaded(true);

      return workoutDownload;
   }

}