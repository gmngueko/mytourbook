/*******************************************************************************
 * Copyright (C) 2020, 2024 Frédéric Bard
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
package net.tourbook.cloud.strava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

import net.tourbook.cloud.Activator;
import net.tourbook.cloud.CloudImages;
import net.tourbook.cloud.Messages;
import net.tourbook.cloud.Preferences;
import net.tourbook.cloud.oauth2.MultiPartBodyPublisher;
import net.tourbook.cloud.oauth2.OAuth2Constants;
import net.tourbook.cloud.oauth2.OAuth2Utils;
import net.tourbook.common.UI;
import net.tourbook.common.util.FileUtils;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.StringUtils;
import net.tourbook.data.TourData;
import net.tourbook.export.TourExporter;
import net.tourbook.extension.upload.TourbookCloudUploader;
import net.tourbook.tour.TourLogManager;
import net.tourbook.tour.TourManager;
import net.tourbook.weather.WeatherUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.json.JSONObject;

public class StravaUploader extends TourbookCloudUploader {

   private static final String     STRAVA_BASE_URL   = "https://www.strava.com/api/v3";            //$NON-NLS-1$

   private static IPreferenceStore _prefStore        = Activator.getDefault().getPreferenceStore();
   private static TourExporter     _tourExporter     = new TourExporter("fit");                    //$NON-NLS-1$

   private static String           CLOUD_UPLOADER_ID = "Strava";                                   //$NON-NLS-1$

   // Source : https://developers.strava.com/docs/reference/#api-models-ActivityType
   private static final List<String> StravaManualActivityTypes = List.of(
         "InlineSkate", //$NON-NLS-1$
         "Kayaking", //$NON-NLS-1$
         "Kitesurf", //$NON-NLS-1$
         "NordicSki", //$NON-NLS-1$
         "Ride", //$NON-NLS-1$
         "RockClimbing", //$NON-NLS-1$
         "RollerSki", //$NON-NLS-1$
         "Rowing", //$NON-NLS-1$
         "Run", //$NON-NLS-1$
         "Sail", //$NON-NLS-1$
         "Skateboard", //$NON-NLS-1$
         "Snowboard", //$NON-NLS-1$
         "Snowshoe", //$NON-NLS-1$
         "Soccer", //$NON-NLS-1$
         "StairStepper", //$NON-NLS-1$
         "StandUpPaddling", //$NON-NLS-1$
         "Surfing", //$NON-NLS-1$
         "Swim", //$NON-NLS-1$
         "Velomobile", //$NON-NLS-1$
         "VirtualRide", //$NON-NLS-1$
         "VirtualRun", //$NON-NLS-1$
         "Walk", //$NON-NLS-1$
         "WeightTraining", //$NON-NLS-1$
         "Wheelchair", //$NON-NLS-1$
         "Windsurf", //$NON-NLS-1$
         "Workout", //$NON-NLS-1$
         "Yoga"); //$NON-NLS-1$

   public StravaUploader() {

      super(CLOUD_UPLOADER_ID,
            Messages.VendorName_Strava,
            Activator.getImageDescriptor(CloudImages.Cloud_Strava_Logo));
   }

   private static ActivityUpload convertResponseToUpload(final HttpResponse<String> response, final String tourDate) {

      ActivityUpload activityUpload = new ActivityUpload();

      final String responseBody = response.body();
      if (response.statusCode() == HttpURLConnection.HTTP_CREATED && StringUtils.hasContent(responseBody)) {

         final ObjectMapper mapper = new ObjectMapper();
         try {
            activityUpload = mapper.readValue(responseBody, ActivityUpload.class);
         } catch (final JsonProcessingException e) {
            StatusUtil.log(e);
         }
      } else {
         activityUpload.setError(responseBody);
      }

      activityUpload.setTourDate(tourDate);

      return activityUpload;
   }

   static StravaTokens getTokens(final String authorizationCode, final boolean isRefreshToken, final String refreshToken) {

      final String responseBody = OAuth2Utils.getTokens(
            authorizationCode,
            isRefreshToken,
            refreshToken,
            OAuth2Utils.createOAuthPasseurUri("/strava/token")); //$NON-NLS-1$

      StravaTokens stravaTokens = null;
      try {
         stravaTokens = new ObjectMapper().readValue(responseBody, StravaTokens.class);
      } catch (final IllegalArgumentException | JsonProcessingException e) {
         StatusUtil.log(e);
      }

      return stravaTokens;
   }

   private static String gzipFile(final String file) {

      final String compressedFilePath = file + ".gz"; //$NON-NLS-1$

      try (final FileInputStream fileInputStream = new FileInputStream(file);
            final FileOutputStream fileOutputStream = new FileOutputStream(compressedFilePath);
            final GZIPOutputStream gzipOS = new GZIPOutputStream(fileOutputStream)) {

         final byte[] buffer = new byte[1024];
         int length;
         while ((length = fileInputStream.read(buffer)) != -1) {
            gzipOS.write(buffer, 0, length);
         }
      } catch (final IOException e) {
         StatusUtil.log(e);
         return UI.EMPTY_STRING;
      }

      return compressedFilePath;
   }

   private String buildFormattedDescription(final TourData tourData) {

      final StringBuilder description = new StringBuilder();
      if (_prefStore.getBoolean(Preferences.STRAVA_SENDDESCRIPTION)) {

         description.append(tourData.getTourDescription());
      }
      if (_prefStore.getBoolean(Preferences.STRAVA_SENDWEATHERDATA_IN_DESCRIPTION)) {

         if (StringUtils.hasContent(description.toString())) {
            description.append(UI.SYSTEM_NEW_LINE);
         }
         String weatherData = WeatherUtils.buildWeatherDataString(tourData, false, false, false);
         if (StringUtils.hasContent(description.toString())) {
            weatherData = UI.NEW_LINE1 + weatherData;
         }
         description.append(weatherData);
      }

      return description.toString();
   }

   private String buildFormattedTitle(final TourData tourData) {

      String title = tourData.getTourTitle();

      if (_prefStore.getBoolean(Preferences.STRAVA_ADDWEATHERICON_IN_TITLE)) {
         title += WeatherUtils.getWeatherIcon(tourData.getWeatherIndex());
      }
      return title;
   }

   private void createCompressedFitTourFile(final IProgressMonitor monitor,
                                            final Map<String, TourData> toursWithTimeSeries,
                                            final TourData tourData) {

      final String absoluteTourFilePath = FileUtils.createTemporaryFile(
            String.valueOf(tourData.getTourId()),
            "fit"); //$NON-NLS-1$

      final String exportedFitGzFile = exportFitGzFile(tourData, absoluteTourFilePath);
      if (StringUtils.hasContent(exportedFitGzFile)) {

         toursWithTimeSeries.put(exportedFitGzFile, tourData);
      }

      FileUtils.deleteIfExists(Paths.get(absoluteTourFilePath));

      monitor.worked(1);
   }

   private void deleteTemporaryTourFiles(final Map<String, TourData> tourFiles) {

      tourFiles.keySet().forEach(tourFilePath -> FileUtils.deleteIfExists(Paths.get(
            tourFilePath)));
   }

   private String exportFitGzFile(final TourData tourData, final String absoluteTourFilePath) {

      _tourExporter.useTourData(tourData).export(absoluteTourFilePath);

      return gzipFile(absoluteTourFilePath);
   }

   private String getAccessToken() {
      return _prefStore.getString(Preferences.STRAVA_ACCESSTOKEN);
   }

   private long getAccessTokenExpirationDate() {
      return _prefStore.getLong(Preferences.STRAVA_ACCESSTOKEN_EXPIRES_AT);
   }

   private String getRefreshToken() {
      return _prefStore.getString(Preferences.STRAVA_REFRESHTOKEN);
   }

   private boolean getValidTokens() {

      if (OAuth2Utils.isAccessTokenValid(getAccessTokenExpirationDate())) {
         return true;
      }

      final StravaTokens newTokens = getTokens(UI.EMPTY_STRING, true, getRefreshToken());

      boolean isTokenValid = false;
      if (newTokens != null) {
         setAccessTokenExpirationDate(newTokens.getExpires_at());
         setRefreshToken(newTokens.getRefresh_token());
         setAccessToken(newTokens.getAccess_token());
         isTokenValid = true;
      }

      return isTokenValid;
   }

   @Override
   protected boolean isReady() {
      return StringUtils.hasContent(getAccessToken()) && StringUtils.hasContent(getRefreshToken());
   }

   private boolean logUploadResult(final ActivityUpload activityUpload) {

      boolean isTourUploaded = false;

      if (StringUtils.hasContent(activityUpload.getError())) {

         TourLogManager.log_ERROR(NLS.bind(
               Messages.Log_UploadToursToStrava_004_UploadError,
               activityUpload.getTourDate(),
               activityUpload.getError()));

      } else {

         isTourUploaded = true;

         if (StringUtils.hasContent(activityUpload.getName())) {

            TourLogManager.log_OK(NLS.bind(
                  Messages.Log_UploadToursToStrava_003_ActivityLink,
                  activityUpload.getTourDate(),
                  activityUpload.getId()));
         } else {

            TourLogManager.log_OK(NLS.bind(
                  Messages.Log_UploadToursToStrava_003_UploadStatus,
                  new Object[] {
                        activityUpload.getTourDate(),
                        activityUpload.getId(),
                        activityUpload.getStatus() }));
         }
      }

      return isTourUploaded;
   }

   private String mapTourType(final TourData manualTour) {

      final String tourTypeName = manualTour.getTourType() != null
            ? manualTour.getTourType().getName().trim()
            : UI.EMPTY_STRING;

      return StravaManualActivityTypes.stream().filter(
            stravaActivityType -> tourTypeName.toLowerCase().startsWith(stravaActivityType.toLowerCase()))
            .findFirst()
            .orElse(StravaManualActivityTypes.get(4));
   }

   private void processManualTour(final IProgressMonitor monitor,
                                  final TourData tourData,
                                  final Map<TourData, String> manualTours) {

      if (StringUtils.isNullOrEmpty(tourData.getTourTitle())) {

         final String tourDate = TourManager.getTourDateTimeShort(tourData);

         TourLogManager.log_ERROR(NLS.bind(Messages.Log_UploadToursToStrava_002_NoTourTitle, tourDate));
         monitor.worked(2);

      } else {

         final String stravaActivityName = mapTourType(tourData);

         manualTours.put(tourData, stravaActivityName);
         monitor.worked(1);
      }
   }

   private void processTours(final List<TourData> selectedTours,
                             final IProgressMonitor monitor,
                             final Map<String, TourData> toursWithTimeSeries,
                             final Map<TourData, String> manualTours) {

      for (final TourData tourData : selectedTours) {

         if (monitor.isCanceled()) {
            return;
         }

         if (tourData.timeSerie == null || tourData.timeSerie.length == 0) {

            processManualTour(monitor, tourData, manualTours);
         } else {

            createCompressedFitTourFile(monitor, toursWithTimeSeries, tourData);
         }
      }
   }

   private CompletableFuture<ActivityUpload> sendAsyncRequest(final TourData tour, final HttpRequest request) {

      final String tourDate = TourManager.getTourDateTimeShort(tour);

      final CompletableFuture<ActivityUpload> activityUpload = OAuth2Utils.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(name -> convertResponseToUpload(name, tourDate))
            .exceptionally(e -> {
               final ActivityUpload errorUpload = new ActivityUpload();
               errorUpload.setTourDate(tourDate);
               errorUpload.setError(e.getMessage());
               return errorUpload;
            });

      return activityUpload;
   }

   private void setAccessToken(final String accessToken) {
      _prefStore.setValue(Preferences.STRAVA_ACCESSTOKEN, accessToken);
   }

   private void setAccessTokenExpirationDate(final long expireAt) {
      _prefStore.setValue(Preferences.STRAVA_ACCESSTOKEN_EXPIRES_AT, expireAt);
   }

   private void setRefreshToken(final String refreshToken) {
      _prefStore.setValue(Preferences.STRAVA_REFRESHTOKEN, refreshToken);
   }

   /**
    * https://developers.strava.com/playground/#/Uploads/createUpload
    *
    * @param compressedTourAbsoluteFilePath
    * @param tourData
    *
    * @return
    */
   private CompletableFuture<ActivityUpload> uploadFile(final String compressedTourAbsoluteFilePath, final TourData tourData) {

      final String title = buildFormattedTitle(tourData);

      final MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
            .addPart("data_type", "fit.gz") //$NON-NLS-1$ //$NON-NLS-2$
            .addPart("name", title) //$NON-NLS-1$
            .addPart("file", Paths.get(compressedTourAbsoluteFilePath)); //$NON-NLS-1$

      final String description = buildFormattedDescription(tourData);
      publisher.addPart("description", description); //$NON-NLS-1$

      final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(STRAVA_BASE_URL + "/uploads")) //$NON-NLS-1$
            .header(OAuth2Constants.AUTHORIZATION, OAuth2Constants.BEARER + getAccessToken())
            .header(OAuth2Constants.CONTENT_TYPE, "multipart/form-data; boundary=" + publisher.getBoundary()) //$NON-NLS-1$
            .timeout(Duration.ofMinutes(5))
            .POST(publisher.build())
            .build();

      return sendAsyncRequest(tourData, request);
   }

   /**
    * https://developers.strava.com/playground/#/Activities/createActivity
    *
    * @param manualTourToUpload
    *
    * @return
    */
   private CompletableFuture<ActivityUpload> uploadManualTour(final Entry<TourData, String> manualTourToUpload) {

      final TourData tourData = manualTourToUpload.getKey();

      final boolean isTrainerActivity = tourData.getTourType() != null &&
            tourData.getTourType().getName().trim().equalsIgnoreCase("trainer"); //$NON-NLS-1$

      final String title = buildFormattedTitle(tourData);

      final JSONObject body = new JSONObject();
      body.put("name", title); //$NON-NLS-1$
      body.put("type", manualTourToUpload.getValue()); //$NON-NLS-1$
      body.put("start_date_local", tourData.getTourStartTime().format(DateTimeFormatter.ISO_DATE_TIME)); //$NON-NLS-1$
      body.put("elapsed_time", tourData.getTourDeviceTime_Elapsed()); //$NON-NLS-1$
      body.put("distance", tourData.getTourDistance()); //$NON-NLS-1$
      body.put("trainer", (isTrainerActivity ? "1" : "0")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      final String description = buildFormattedDescription(tourData);
      body.put("description", description); //$NON-NLS-1$

      final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(STRAVA_BASE_URL + "/activities")) //$NON-NLS-1$
            .header(OAuth2Constants.AUTHORIZATION, OAuth2Constants.BEARER + getAccessToken())
            .header(OAuth2Constants.CONTENT_TYPE, "application/json") //$NON-NLS-1$
            .timeout(Duration.ofMinutes(5))
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

      return sendAsyncRequest(tourData, request);
   }

   @Override
   public void uploadTours(final List<TourData> selectedTours) {

      final int numberOfTours = selectedTours.size();
      final int[] numberOfUploadedTours = new int[1];
      final String[] notificationText = new String[1];

      final Job job = new Job(NLS.bind(Messages.Dialog_UploadToursToStrava_Task,
            numberOfTours,
            _prefStore.getString(Preferences.STRAVA_ATHLETEFULLNAME))) {

         @Override
         public IStatus run(final IProgressMonitor monitor) {

            monitor.beginTask(UI.EMPTY_STRING, numberOfTours * 2);

            if (!getValidTokens()) {
               Display.getDefault().asyncExec(() -> TourLogManager.log_ERROR(Messages.Log_CloudAction_InvalidTokens));
               notificationText[0] = Messages.Log_CloudAction_InvalidTokens;
               return Status.CANCEL_STATUS;
            }

            monitor.subTask(NLS.bind(Messages.Dialog_UploadToursToStrava_SubTask, UI.SYMBOL_HOURGLASS_WITH_FLOWING_SAND, UI.EMPTY_STRING));

            final Map<String, TourData> toursWithTimeSeries = new HashMap<>();
            final Map<TourData, String> manualTours = new HashMap<>();
            processTours(selectedTours, monitor, toursWithTimeSeries, manualTours);

            if (monitor.isCanceled()) {
               deleteTemporaryTourFiles(toursWithTimeSeries);
            }

            monitor.subTask(NLS.bind(Messages.Dialog_UploadToursToStrava_SubTask,
                  UI.SYMBOL_WHITE_HEAVY_CHECK_MARK,
                  UI.SYMBOL_HOURGLASS_WITH_FLOWING_SAND));

            numberOfUploadedTours[0] = uploadTours(toursWithTimeSeries, manualTours, monitor);

            monitor.worked(toursWithTimeSeries.size() + manualTours.size());

            monitor.subTask(NLS.bind(Messages.Dialog_UploadToursToStrava_SubTask,
                  UI.SYMBOL_WHITE_HEAVY_CHECK_MARK,
                  UI.SYMBOL_WHITE_HEAVY_CHECK_MARK));

            monitor.done();

            return Status.OK_STATUS;
         }
      };

      final long start = System.currentTimeMillis();

      TourLogManager.log_TITLE(NLS.bind(Messages.Log_UploadToursToStrava_001_Start, numberOfTours));

      job.setPriority(Job.INTERACTIVE);
      job.schedule();

      job.addJobChangeListener(new JobChangeAdapter() {
         @Override
         public void done(final IJobChangeEvent event) {

            if (!PlatformUI.isWorkbenchRunning()) {
               return;
            }

            final String infoText = event.getResult().isOK()
                  ? NLS.bind(Messages.Dialog_UploadToursToStrava_Message,
                        numberOfUploadedTours[0],
                        numberOfTours - numberOfUploadedTours[0])
                  : notificationText[0];

            PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {

               TourLogManager.log_TITLE(String.format(Messages.Log_CloudAction_End, (System.currentTimeMillis() - start) / 1000.0));

               UI.openNotificationPopup(Messages.Dialog_UploadToursToStrava_Title,
                     Activator.getImageDescriptor(CloudImages.Cloud_Strava_Logo),
                     infoText);
            });
         }
      });
   }

   private int uploadTours(final Map<String, TourData> toursWithTimeSeries,
                           final Map<TourData, String> manualTours,
                           final IProgressMonitor monitor) {

      final List<CompletableFuture<ActivityUpload>> activityUploads = new ArrayList<>();

      for (final Map.Entry<String, TourData> tourToUpload : toursWithTimeSeries.entrySet()) {

         if (monitor.isCanceled()) {
            return 0;
         }

         final String compressedTourAbsoluteFilePath = tourToUpload.getKey();
         final TourData tourData = tourToUpload.getValue();

         activityUploads.add(uploadFile(compressedTourAbsoluteFilePath, tourData));
      }

      for (final Map.Entry<TourData, String> manualTourToUpload : manualTours.entrySet()) {

         if (monitor.isCanceled()) {
            return 0;
         }

         activityUploads.add(uploadManualTour(manualTourToUpload));
      }

      final int[] numberOfUploadedTours = new int[1];
      activityUploads.stream().map(completableFuture -> completableFuture.join()).forEach(activityUpload -> {
         if (monitor.isCanceled()) {
            return;
         } else if (logUploadResult(activityUpload)) {
            ++numberOfUploadedTours[0];
         }
      });

      deleteTemporaryTourFiles(toursWithTimeSeries);

      return numberOfUploadedTours[0];
   }
}
