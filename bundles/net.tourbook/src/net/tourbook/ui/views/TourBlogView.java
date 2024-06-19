/*******************************************************************************
 * Copyright (C) 2005, 2024 Wolfgang Schramm and Contributors
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

import static org.eclipse.swt.browser.LocationListener.changingAdapter;
import static org.eclipse.swt.browser.ProgressListener.completedAdapter;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

import java.io.File;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.OtherMessages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.UI;
import net.tourbook.common.color.ThemeUtil;
import net.tourbook.common.formatter.FormatManager;
import net.tourbook.common.formatter.ValueFormatter_Number_1_0;
import net.tourbook.common.formatter.ValueFormatter_Number_1_1;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.tooltip.ActionToolbarSlideout;
import net.tourbook.common.tooltip.ToolbarSlideout;
import net.tourbook.common.util.CSS;
import net.tourbook.common.util.PostSelectionProvider;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.Util;
import net.tourbook.data.TourBeverageContainer;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.data.TourNutritionProduct;
import net.tourbook.data.TourTag;
import net.tourbook.database.TourDatabase;
import net.tourbook.nutrition.NutritionUtils;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tag.TagManager;
import net.tourbook.tour.DialogMarker;
import net.tourbook.tour.DialogQuickEdit;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.SelectionDeletedTours;
import net.tourbook.tour.SelectionTourData;
import net.tourbook.tour.SelectionTourId;
import net.tourbook.tour.SelectionTourIds;
import net.tourbook.tour.SelectionTourMarker;
import net.tourbook.tour.TourEvent;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.tourChart.TourChart;
import net.tourbook.ui.views.referenceTour.SelectionReferenceTourView;
import net.tourbook.ui.views.referenceTour.TVIElevationCompareResult_ComparedTour;
import net.tourbook.ui.views.referenceTour.TVIRefTour_ComparedTour;
import net.tourbook.ui.views.referenceTour.TVIRefTour_RefTourItem;
import net.tourbook.weather.WeatherUtils;
import net.tourbook.web.WEB;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class TourBlogView extends ViewPart {

   static final String         ID                                              = "net.tourbook.ui.views.TourBlogView";      //$NON-NLS-1$

   private static final String NL                                              = UI.NEW_LINE1;

   private static final String SPACER                                          = "<div>&nbsp;</div>";                       //$NON-NLS-1$

   private static final String TOUR_BLOG_CSS                                   = "/tourbook/resources/tour-blog.css";       //$NON-NLS-1$

   static final String         STATE_IS_DRAW_MARKER_WITH_DEFAULT_COLOR         = "STATE_IS_DRAW_MARKER_WITH_DEFAULT_COLOR"; //$NON-NLS-1$
   static final boolean        STATE_IS_DRAW_MARKER_WITH_DEFAULT_COLOR_DEFAULT = false;
   static final String         STATE_IS_SHOW_HIDDEN_MARKER                     = "STATE_IS_SHOW_HIDDEN_MARKER";             //$NON-NLS-1$
   static final boolean        STATE_IS_SHOW_HIDDEN_MARKER_DEFAULT             = true;
   static final String         STATE_IS_SHOW_TOUR_MARKERS                      = "STATE_IS_SHOW_TOUR_MARKERS";              //$NON-NLS-1$
   static final boolean        STATE_IS_SHOW_TOUR_MARKERS_DEFAULT              = true;
   static final String         STATE_IS_SHOW_TOUR_TAGS                         = "STATE_IS_SHOW_TOUR_TAGS";                 //$NON-NLS-1$
   static final boolean        STATE_IS_SHOW_TOUR_TAGS_DEFAULT                 = true;

   private static final String EXTERNAL_LINK_URL                               = "http";                                    //$NON-NLS-1$
   private static final String HREF_TOKEN                                      = "#";                                       //$NON-NLS-1$
   private static final String PAGE_ABOUT_BLANK                                = "about:blank";                             //$NON-NLS-1$

   /**
    * This is necessary otherwise XULrunner in Linux do not fire a location change event.
    */
   private static final String HTTP_DUMMY                                      = "http://dummy";                            //$NON-NLS-1$

   private static final String ACTION_EDIT_TOUR                                = "EditTour";                                //$NON-NLS-1$
   private static final String ACTION_EDIT_MARKER                              = "EditMarker";                              //$NON-NLS-1$
   private static final String ACTION_HIDE_MARKER                              = "HideMarker";                              //$NON-NLS-1$
   private static final String ACTION_OPEN_MARKER                              = "OpenMarker";                              //$NON-NLS-1$
   private static final String ACTION_SHOW_MARKER                              = "ShowMarker";                              //$NON-NLS-1$

   private static String       HREF_EDIT_TOUR;
   private static String       HREF_EDIT_MARKER;
   private static String       HREF_HIDE_MARKER;
   private static String       HREF_OPEN_MARKER;
   private static String       HREF_SHOW_MARKER;

   static {

      HREF_EDIT_TOUR = HREF_TOKEN + ACTION_EDIT_TOUR;

      HREF_EDIT_MARKER = HREF_TOKEN + ACTION_EDIT_MARKER + HREF_TOKEN;
      HREF_HIDE_MARKER = HREF_TOKEN + ACTION_HIDE_MARKER + HREF_TOKEN;
      HREF_OPEN_MARKER = HREF_TOKEN + ACTION_OPEN_MARKER + HREF_TOKEN;
      HREF_SHOW_MARKER = HREF_TOKEN + ACTION_SHOW_MARKER + HREF_TOKEN;
   }
   private static final String           HREF_MARKER_ITEM  = "#MarkerItem";                   //$NON-NLS-1$
   private static final IPreferenceStore _prefStore        = TourbookPlugin.getPrefStore();
   private static final IPreferenceStore _prefStore_Common = CommonActivator.getPrefStore();
   private static final IDialogSettings  _state            = TourbookPlugin.getState(ID);
   private static final IDialogSettings  _state_WEB        = WEB.getState();

   private static final NumberFormat     _nf2              = NumberFormat.getNumberInstance();
   {
      _nf2.setMinimumFractionDigits(0);
      _nf2.setMaximumFractionDigits(2);
   }

   private PostSelectionProvider   _postSelectionProvider;
   private ISelectionListener      _postSelectionListener;
   private IPropertyChangeListener _prefChangeListener;
   private IPropertyChangeListener _prefChangeListener_Common;
   private ITourEventListener      _tourEventListener;

   private TourData                _tourData;

   private String                  _htmlCss;

   private String                  _imageUrl_ActionEdit;
   private String                  _imageUrl_ActionHideMarker;
   private String                  _imageUrl_ActionShowMarker;

   private String                  _cssMarker_DefaultColor;
   private String                  _cssMarker_DeviceColor;
   private String                  _cssMarker_HiddenColor;

   private boolean                 _isDrawWithDefaultColor;
   private boolean                 _isShowHiddenMarker;
   private boolean                 _isShowTourMarkers;

   private Long                    _reloadedTourMarkerId;

   private ActionTourBlogOptions   _actionTourBlogOptions;

   /*
    * UI controls
    */
   private PageBook  _pageBook;

   private Composite _pageNoBrowser;
   private Composite _pageNoData;
   private Composite _pageContent;
   private Composite _parent;

   private Browser   _browser;
   private TourChart _tourChart;
   private Text      _txtNoBrowser;

   private class ActionTourBlogOptions extends ActionToolbarSlideout {

      @Override
      protected ToolbarSlideout createSlideout(final ToolBar toolbar) {

         return new SlideoutTourBlogOptions(_parent, toolbar, TourBlogView.this, _state);
      }
   }

   private void addPrefListener() {

      _prefChangeListener = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ITourbookPreferences.GRAPH_MARKER_IS_MODIFIED)) {

            updateUI();

         } else if (property.equals(ITourbookPreferences.NUTRITION_BEVERAGECONTAINERS_HAVE_CHANGED) ||
               property.equals(ITourbookPreferences.NUTRITION_IGNORE_FIRST_HOUR)) {

            reloadTourData();
         }
      };

      _prefChangeListener_Common = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ICommonPreferences.MEASUREMENT_SYSTEM)) {

            // measurement system has changed

            updateUI();
         }
      };

      _prefStore.addPropertyChangeListener(_prefChangeListener);
      _prefStore_Common.addPropertyChangeListener(_prefChangeListener_Common);
   }

   /**
    * listen for events when a tour is selected
    */
   private void addSelectionListener() {

      _postSelectionListener = (workbenchPart, selection) -> {
         if (workbenchPart == TourBlogView.this) {
            return;
         }
         onSelectionChanged(selection);
      };
      getSite().getPage().addPostSelectionListener(_postSelectionListener);
   }

   private void addTourEventListener() {

      _tourEventListener = (workbenchPart, eventId, eventData) -> {

         if (workbenchPart == TourBlogView.this) {
            return;
         }

         if ((eventId == TourEventId.TOUR_CHANGED) && (eventData instanceof final TourEvent tourEventData)) {

            final List<TourData> modifiedTours = tourEventData.getModifiedTours();
            if (modifiedTours != null) {

               // update modified tour

               if (_tourData == null) {
                  return;
               }

               final long viewTourId = _tourData.getTourId();

               for (final TourData tourData : modifiedTours) {
                  if (tourData.getTourId() == viewTourId) {

                     // get modified tour
                     _tourData = tourData;

                     // removed old tour data from the selection provider
                     _postSelectionProvider.clearSelection();

                     updateUI();

                     // nothing more to do, the view contains only one tour
                     return;
                  }
               }
            }

         } else if (eventId == TourEventId.CLEAR_DISPLAYED_TOUR) {

            clearView();

         } else if ((eventId == TourEventId.TOUR_SELECTION) && eventData instanceof final ISelection selection) {

            onSelectionChanged(selection);

         } else if (eventId == TourEventId.MARKER_SELECTION) {

            if (eventData instanceof final SelectionTourMarker selectionTourMarker) {

               final TourData tourData = selectionTourMarker.getTourData();

               if (tourData != _tourData) {

                  _tourData = tourData;

                  updateUI();
               }
            }
         } else if (eventId == TourEventId.TAG_CONTENT_CHANGED ||
               eventId == TourEventId.TAG_STRUCTURE_CHANGED) {

            reloadTourData();
         }
      };

      TourManager.getInstance().addTourEventListener(_tourEventListener);
   }

   private String buildDescription(String tourDescription) {

      // Using the option {@link UrlDetectorOptions#JAVASCRIPT} because it encompasses
      // {@link UrlDetectorOptions#QUOTE_MATCH},
      // {@link UrlDetectorOptions#SINGLE_QUOTE_MATCH} and
      // {@link UrlDetectorOptions#BRACKET_MATCH}

      final UrlDetector parser = new UrlDetector(tourDescription, UrlDetectorOptions.JAVASCRIPT);
      final List<Url> detectedUrls = parser.detect();

      if (!detectedUrls.isEmpty()) {

         // Because the tour description can contain similar URLs, this can create
         // an issue when URLs can be replaced several times when replacing original
         // text to formatted URLs.
         // To avoid this, original URLs are first replaced by a unique random string
         // and then this string is replaced by the formatted URL
         final HashMap<String, Url> detectedUrlMap = new HashMap<>();
         for (final Url detectedUrl : detectedUrls) {

            final String randomString = RandomStringUtils.random(10, true, true);
            detectedUrlMap.put(randomString, detectedUrl);

            final String originalUrl = detectedUrl.getOriginalUrl();
            tourDescription = StringUtils.replaceOnce(tourDescription, originalUrl, randomString);
         }

         for (final Map.Entry<String, Url> set : detectedUrlMap.entrySet()) {

            final String fullUrl = set.getValue().getFullUrl();
            tourDescription = tourDescription.replace(set.getKey(), "<a href=\"" + fullUrl + "\">" + fullUrl + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         }
      }

      if (UI.IS_SCRAMBLE_DATA) {
         tourDescription = UI.scrambleText(tourDescription);
      }

      return "<p class='description'>" + WEB.convertHTML_LineBreaks(tourDescription) + "</p>" + NL; //$NON-NLS-1$ //$NON-NLS-2$
   }

   private String buildNutritionSection(final Set<TourNutritionProduct> tourNutritionProducts, final boolean addSpacer) {

      final StringBuilder sb = new StringBuilder();

      if (addSpacer) {
         sb.append(SPACER);
      }

      sb.append("<div class='title'>" + Messages.Tour_Blog_Section_Nutrition + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$

      // AVERAGES
      sb.append(NL + "<u>" + Messages.Tour_Blog_Label_Nutrition_Averages + "</u>" + NL); //$NON-NLS-1$ //$NON-NLS-2$

      sb.append(NL);
      sb.append("<table>"); //$NON-NLS-1$

      // Average fluids per hour
      final String averageFluidPerHour = NutritionUtils.computeAverageFluidsPerHour(_tourData);
      if (net.tourbook.common.util.StringUtils.hasContent(averageFluidPerHour)) {

         sb.append(buildTableRow(Messages.Tour_Nutrition_Label_Fluids,
               averageFluidPerHour,
               UI.UNIT_FLUIDS_L + UI.SLASH + UI.UNIT_LABEL_TIME));
      }

      // Average calories per hour
      final String averageCaloriesPerHour = NutritionUtils.computeAverageCaloriesPerHour(_tourData);
      if (net.tourbook.common.util.StringUtils.hasContent(averageCaloriesPerHour)) {

         sb.append(buildTableRow(Messages.Tour_Nutrition_Label_Calories,
               averageCaloriesPerHour,
               OtherMessages.VALUE_UNIT_K_CALORIES + UI.SLASH + UI.UNIT_LABEL_TIME));
      }

      // Average sodium per L
      final String averageSodiumPerLiter = NutritionUtils.computeAverageSodiumPerLiter(_tourData);
      if (net.tourbook.common.util.StringUtils.hasContent(averageSodiumPerLiter)) {

         sb.append(buildTableRow(Messages.Tour_Nutrition_Label_Sodium,
               averageSodiumPerLiter,
               UI.UNIT_WEIGHT_MG + UI.SLASH + UI.UNIT_FLUIDS_L));
      }

      sb.append("</table>"); //$NON-NLS-1$

      // Split the tour nutrition products into 2 lists: beverages and foods
      final List<TourNutritionProduct> beverages = new ArrayList<>();
      final List<TourNutritionProduct> foods = new ArrayList<>();
      for (final TourNutritionProduct tourNutritionProduct : tourNutritionProducts) {

         if (tourNutritionProduct.isBeverage() || tourNutritionProduct.getTourBeverageContainer() != null) {
            beverages.add(tourNutritionProduct);
         } else {
            foods.add(tourNutritionProduct);
         }
      }

      if (beverages.size() > 0) {
         // BEVERAGES

         sb.append(NL);

         sb.append(NL + "<u>" + Messages.Tour_Blog_Label_Nutrition_Beverages + "</u>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
         beverages.stream().forEach(product -> {

            sb.append(NL);
            final TourBeverageContainer tourBeverageContainer = product.getTourBeverageContainer();

            if (tourBeverageContainer == null) {

               sb.append(String.format("%s %s (%s %s)", //$NON-NLS-1$
                     _nf2.format(product.getConsumedQuantity()),
                     product.getName(),
                     _nf2.format(product.getBeverageQuantity()),
                     UI.UNIT_FLUIDS_ML));

            } else {

               sb.append(String.format("%s %s (%s %s)", //$NON-NLS-1$
                     _nf2.format(product.getContainersConsumed()) + UI.SPACE1 + NutritionUtils.buildTourBeverageContainerName(
                           tourBeverageContainer),
                     product.getName(),
                     _nf2.format(product.getContainersConsumed() * tourBeverageContainer.getCapacity()),
                     UI.UNIT_FLUIDS_L));

            }
         });

         sb.append(NL);
      }
      if (foods.size() > 0) {
         // FOODS

         sb.append(NL);

         sb.append(NL + "<u>" + Messages.Tour_Blog_Label_Nutrition_Foods + "</u>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
         foods.stream().forEach(product -> {

            sb.append(NL);
            sb.append(_nf2.format(product.getConsumedQuantity()) + UI.SPACE1 + product.getName());
         });

         sb.append(NL);
      }

      String nutritionSectionString = WEB.convertHTML_LineBreaks(sb.toString());

      if (UI.IS_SCRAMBLE_DATA) {
         nutritionSectionString = UI.scrambleText(nutritionSectionString);
      }

      return nutritionSectionString;
   }

   private String buildTableRow(final String label, final String average, final String unit) {

      final StringBuilder sb = new StringBuilder();

      sb.append("<tr>"); //$NON-NLS-1$
      sb.append(String.format("<td>%s</td>", label)); //$NON-NLS-1$
      sb.append("<td>&rarr;</td>"); //$NON-NLS-1$
      sb.append(String.format("<td>%s %s</td>", average, unit)); //$NON-NLS-1$
      sb.append("</tr>"); //$NON-NLS-1$

      return sb.toString();
   }

   private String buildTagsSection(final Set<TourTag> tourTags, final boolean addSpacer) {

      final boolean showTourTags = Util.getStateBoolean(_state, TourBlogView.STATE_IS_SHOW_TOUR_TAGS, TourBlogView.STATE_IS_SHOW_TOUR_TAGS_DEFAULT);
      if (!showTourTags) {
         return UI.EMPTY_STRING;
      }

      final StringBuilder sb = new StringBuilder();

      if (addSpacer) {
         sb.append(SPACER);
      }

      sb.append("<div class='title'>" + Messages.Tour_Blog_Section_Tags + "</div>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append("<table><tr>"); //$NON-NLS-1$

      final Map<Long, String> tourTagsAccumulatedValues = TagManager.fetchTourTagsAccumulatedValues();
      for (final TourTag tag : tourTags) {

         sb.append("<td>"); //$NON-NLS-1$

         final Image tagImage = TagManager.getTagImage(tag);
         if (tagImage != null) {

            final String imageBase64 = Util.imageToBase64(tagImage);
            sb.append("<img src=\"data:image/png;base64," + imageBase64 + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
         }
         final String tagText = NL + tag.getTagName() + NL + "<i>" + tourTagsAccumulatedValues.get(tag.getTagId()) + "</i>"; //$NON-NLS-1$ //$NON-NLS-2$

         sb.append(tagText);

         sb.append("</td>"); //$NON-NLS-1$
      }

      sb.append("</tr></table>"); //$NON-NLS-1$

      String tagsSectionString = WEB.convertHTML_LineBreaks(sb.toString());

      if (UI.IS_SCRAMBLE_DATA) {
         tagsSectionString = UI.scrambleText(tagsSectionString);
      }

      return tagsSectionString;
   }

   private String buildTourSummary() {

      final StringBuilder sb = new StringBuilder();

      // Distance
      final float tourDistance = _tourData.getTourDistance();
      if (tourDistance > 0) {
         sb.append("&#128207;" + UI.SPACE1); //$NON-NLS-1$
         final float distance = tourDistance / UI.UNIT_VALUE_DISTANCE;
         sb.append(FormatManager.formatDistance(distance / 1000.0));
         sb.append(UI.SPACE1 + UI.UNIT_LABEL_DISTANCE + UI.SPACE3);
      }

      // Time
      final long tourRecordedTime = _tourData.getTourDeviceTime_Recorded();
      if (tourRecordedTime > 0) {
         sb.append("&#9201;" + UI.SPACE1); //$NON-NLS-1$
         sb.append(FormatManager.formatRecordedTime(_tourData.getTourDeviceTime_Recorded()));
         sb.append(UI.SPACE3);
      }

      // Elevation gain
      final float elevationGain = _tourData.getTourAltUp() / UI.UNIT_VALUE_ELEVATION;
      if (elevationGain > 0) {
         sb.append("&#8599;" + UI.SPACE1); //$NON-NLS-1$
         sb.append(FormatManager.formatElevation(elevationGain));
         sb.append(UI.SPACE1 + UI.UNIT_LABEL_ELEVATION + UI.SPACE3);
      }

      // Elevation loss
      final float elevationLoss = _tourData.getTourAltDown() / UI.UNIT_VALUE_ELEVATION;
      if (elevationLoss > 0) {
         sb.append("&#8600;" + UI.SPACE1); //$NON-NLS-1$
         sb.append(FormatManager.formatElevation(elevationLoss));
         sb.append(UI.SPACE1 + UI.UNIT_LABEL_ELEVATION + UI.SPACE3);
      }

      // Calories
      final float calories = _tourData.getCalories() / 1000f;
      if (calories > 0) {
         sb.append("&#128293;" + UI.SPACE1); //$NON-NLS-1$
         sb.append(new ValueFormatter_Number_1_0().printDouble(calories));
         sb.append(UI.SPACE1 + OtherMessages.VALUE_UNIT_K_CALORIES + UI.SPACE3);
      }

      // Body weight
      final float bodyWeight = _tourData.getBodyWeight() * UI.UNIT_VALUE_WEIGHT;
      if (bodyWeight > 0) {
         sb.append("&#9878;" + UI.SPACE1); //$NON-NLS-1$
         sb.append(new ValueFormatter_Number_1_1().printDouble(bodyWeight));
         sb.append(UI.SPACE1 + UI.UNIT_LABEL_WEIGHT + UI.SPACE3);
      }

      // Body fat
      final float bodyFat = _tourData.getBodyFat();
      if (bodyFat > 0) {
         sb.append(new ValueFormatter_Number_1_1().printDouble(bodyFat));
         sb.append(UI.SPACE1 + UI.SYMBOL_PERCENTAGE + UI.SPACE3);
      }

      return sb.toString();
   }

   private String buildWeatherSection(String tourWeather, final boolean addSpacer) {

      final StringBuilder sb = new StringBuilder();

      if (addSpacer) {
         // write spacer
         sb.append(SPACER);
      }

      if (UI.IS_SCRAMBLE_DATA) {
         tourWeather = UI.scrambleText(tourWeather);
      }

      sb.append("<div class='title'>" + Messages.Tour_Blog_Section_Weather + "</div>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append("<p class='description'>" + WEB.convertHTML_LineBreaks(tourWeather) + "</p>" + NL); //$NON-NLS-1$ //$NON-NLS-2$

      return sb.toString();
   }

   private void clearView() {

      _tourData = null;

      // removed old tour data from the selection provider
      _postSelectionProvider.clearSelection();

      showInvalidPage();
   }

   private String create_10_Head() {

      // set body size
      final int bodyFontSize = Util.getStateInt(_state_WEB, WEB.STATE_BODY_FONT_SIZE, WEB.STATE_BODY_FONT_SIZE_DEFAULT);
      String htmlCss = _htmlCss.replace(WEB.STATE_BODY_FONT_SIZE_CSS_REPLACEMENT_TAG, Integer.toString(bodyFontSize));

      /*
       * Replace theme tags
       */

// SET_FORMATTING_OFF

      htmlCss = htmlCss.replace(WEB.CSS_TAG__BODY__COLOR,                        UI.IS_DARK_THEME ? "ddd" : "333");        //$NON-NLS-1$ //$NON-NLS-2$
      htmlCss = htmlCss.replace(WEB.CSS_TAG__BODY__BACKGROUND_COLOR,             UI.IS_DARK_THEME ? "333" : "fff");        //$NON-NLS-1$ //$NON-NLS-2$

      htmlCss = htmlCss.replace(WEB.CSS_TAG__A_LINK__COLOR,                      UI.IS_DARK_THEME ? "D6FF6F" : "3B9529");  //$NON-NLS-1$ //$NON-NLS-2$
      htmlCss = htmlCss.replace(WEB.CSS_TAG__A_VISITED__COLOR,                   UI.IS_DARK_THEME ? "7E9543" : "DE559D");  //$NON-NLS-1$ //$NON-NLS-2$

      htmlCss = htmlCss.replace(WEB.CSS_TAG__ACTION_CONTAINER__BACKGROUND_COLOR, UI.IS_DARK_THEME ? "444" : "f8f8f8");     //$NON-NLS-1$ //$NON-NLS-2$

// SET_FORMATTING_ON

      if (UI.IS_DARK_THEME) {

         // show dark scrollbar
         htmlCss = htmlCss.replace(WEB.CSS_TAG__BODY_SCROLLBAR, WEB.CSS_CONTENT__BODY_SCROLLBAR__DARK);
      }

      final String html = UI.EMPTY_STRING

            + "   <meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />" + NL //$NON-NLS-1$
            + "   <meta http-equiv='X-UA-Compatible' content='IE=edge' />" + NL //$NON-NLS-1$
            + htmlCss
            + NL;

      return html;
   }

   private String create_20_Body() {

      final StringBuilder sb = new StringBuilder();

      create_22_BlogHeader(sb);
      create_24_Tour(sb);

      if (_isShowTourMarkers) {

         final Set<TourMarker> tourMarkers = _tourData.getTourMarkers();
         final List<TourMarker> allMarker = new ArrayList<>(tourMarkers);
         Collections.sort(allMarker);

         for (final TourMarker tourMarker : allMarker) {

            // check if marker is hidden and should not be displayed
            if (tourMarker.isMarkerVisible() == false && _isShowHiddenMarker == false) {
               continue;
            }

            sb.append("<div class='blog-item'>"); //$NON-NLS-1$
            sb.append("<div class='action-hover-container'>" + NL); //$NON-NLS-1$
            {
               create_30_Marker(sb, tourMarker);
               create_32_MarkerUrl(sb, tourMarker);
            }
            sb.append("</div>" + NL); //$NON-NLS-1$
            sb.append("</div>" + NL); //$NON-NLS-1$
         }
      }

      return sb.toString();
   }

   private void create_22_BlogHeader(final StringBuilder sb) {

      /*
       * Date/Time header
       */
      final long elapsedTime = _tourData.getTourDeviceTime_Elapsed();

      final ZonedDateTime dtTourStart = _tourData.getTourStartTime();
      final ZonedDateTime dtTourEnd = dtTourStart.plusSeconds(elapsedTime);

      final String date = dtTourStart.format(TimeTools.Formatter_Date_F);

      final String time = String.format("%s - %s", //$NON-NLS-1$
            dtTourStart.format(TimeTools.Formatter_Time_M),
            dtTourEnd.format(TimeTools.Formatter_Time_M));

      sb.append("<div class='date'>" + date + "</div>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append("<div class='time'>" + time + "</div>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append("<div style='clear: both;'></div>" + NL); //$NON-NLS-1$
   }

   private void create_24_Tour(final StringBuilder sb) {

      final boolean isSaveWeatherLogInWeatherDescription = _prefStore.getBoolean(ITourbookPreferences.WEATHER_SAVE_LOG_IN_TOUR_WEATHER_DESCRIPTION);

      String tourTitle = _tourData.getTourTitle();
      final String tourDescription = _tourData.getTourDescription();
      final Set<TourNutritionProduct> tourNutritionProducts = _tourData.getTourNutritionProducts();
      final Set<TourTag> tourTags = _tourData.getTourTags();
      String tourSummary = buildTourSummary();
      final String tourWeather = WeatherUtils.buildWeatherDataString(_tourData,
            true, // isdisplayMaximumMinimumTemperature
            true, // isDisplayPressure
            isSaveWeatherLogInWeatherDescription // isWeatherDataSeparatorNewLine
      );

      final boolean isDescription = tourDescription.length() > 0;
      final boolean isNutrition = tourNutritionProducts.size() > 0;
      final boolean isTitle = tourTitle.length() > 0;
      final boolean isTourSummary = tourSummary.length() > 0;
      final boolean isTourTags = tourTags.size() > 0;
      final boolean isWeather = tourWeather.length() > 0;

      if (isDescription || isTourSummary || isTitle || isWeather || isTourTags || isNutrition) {

         sb.append("<div class='action-hover-container' style='margin-top:15px; margin-bottom: 5px;'>" + NL); //$NON-NLS-1$
         {

            if (UI.IS_SCRAMBLE_DATA) {
               tourSummary = UI.scrambleText(tourSummary);
            }
            sb.append(tourSummary);
            sb.append(SPACER);

            sb.append("<div class='blog-item'>"); //$NON-NLS-1$
            {
               if (UI.IS_SCRAMBLE_DATA) {
                  tourTitle = UI.scrambleText(tourTitle);
               }

               /*
                * Edit action
                */
               final String hoverEdit = WEB.escapeSingleQuote(NLS.bind(Messages.Tour_Blog_Action_EditTour_Tooltip, tourTitle));
               final String hrefEditTour = HTTP_DUMMY + HREF_EDIT_TOUR;

               sb.append(UI.EMPTY_STRING +

                     ("<div class='action-container'>" //                           //$NON-NLS-1$
                           + ("<a class='action' style='background: url(" //        //$NON-NLS-1$
                                 + _imageUrl_ActionEdit
                                 + ") no-repeat;'" //                               //$NON-NLS-1$
                                 + " href='" + hrefEditTour + "'" //                //$NON-NLS-1$ //$NON-NLS-2$
                                 + " title='" + hoverEdit + "'" //                  //$NON-NLS-1$ //$NON-NLS-2$
                                 + ">" //                                           //$NON-NLS-1$
                                 + "</a>") //                                       //$NON-NLS-1$
                           + "   </div>" + NL)); //                                   //$NON-NLS-1$

               /*
                * Tour title
                */
               if (isTitle) {

                  sb.append("<span class='blog-title'>" + tourTitle + "</span>" + NL); //$NON-NLS-1$ //$NON-NLS-2$
               }

               /*
                * Description
                */
               if (isDescription) {

                  sb.append(buildDescription(tourDescription));
               }

               /*
                * Weather
                */
               if (isWeather) {

                  sb.append(buildWeatherSection(tourWeather, isDescription));
               }

               /*
                * Nutrition
                */
               if (isNutrition) {

                  sb.append(buildNutritionSection(tourNutritionProducts, isDescription || isWeather));
               }

               /*
                * Tags
                */
               if (isTourTags) {

                  sb.append(buildTagsSection(tourTags, isDescription || isWeather || isNutrition));
               }
            }
            sb.append("</div>" + NL); //$NON-NLS-1$
         }
         sb.append("</div>" + NL); //$NON-NLS-1$

      } else {

         // there is no tour header, set some spacing

         sb.append("<div style='margin-top:20px;'></div>" + NL); //$NON-NLS-1$
      }
   }

   /**
    * Label
    */
   private void create_30_Marker(final StringBuilder sb, final TourMarker tourMarker) {

      final long markerId = tourMarker.getMarkerId();
      String markerLabel = tourMarker.getLabel();

      if (UI.IS_SCRAMBLE_DATA) {
         markerLabel = UI.scrambleText(markerLabel);
      }

      final String hrefOpenMarker = HTTP_DUMMY + HREF_OPEN_MARKER + markerId;
      final String hrefEditMarker = HTTP_DUMMY + HREF_EDIT_MARKER + markerId;
      final String hrefHideMarker = HTTP_DUMMY + HREF_HIDE_MARKER + markerId;
      final String hrefShowMarker = HTTP_DUMMY + HREF_SHOW_MARKER + markerId;

      final String hoverEditMarker = WEB.escapeSingleQuote(NLS.bind(Messages.Tour_Blog_Action_EditMarker_Tooltip, markerLabel));
      final String hoverHideMarker = WEB.escapeSingleQuote(NLS.bind(Messages.Tour_Blog_Action_HideMarker_Tooltip, markerLabel));
      final String hoverOpenMarker = WEB.escapeSingleQuote(NLS.bind(Messages.Tour_Blog_Action_OpenMarker_Tooltip, markerLabel));
      final String hoverShowMarker = WEB.escapeSingleQuote(NLS.bind(Messages.Tour_Blog_Action_ShowMarker_Tooltip, markerLabel));

      /*
       * get color by priority
       */
      String cssMarkerColor;

      if (_isDrawWithDefaultColor) {

         // force default color
         cssMarkerColor = _cssMarker_DefaultColor;

      } else if (tourMarker.isMarkerVisible() == false) {

         // show hidden color
         cssMarkerColor = _cssMarker_HiddenColor;

      } else if (tourMarker.isDeviceMarker()) {

         // show with device color
         cssMarkerColor = _cssMarker_DeviceColor;

      } else {

         cssMarkerColor = _cssMarker_DefaultColor;
      }

      final String htmlMarkerStyle = " style='color:" + cssMarkerColor + "'"; //$NON-NLS-1$ //$NON-NLS-2$

      final String htmlActionShowHideMarker = tourMarker.isMarkerVisible() //
            ? createHtml_Action(hrefHideMarker, hoverHideMarker, _imageUrl_ActionHideMarker)
            : createHtml_Action(hrefShowMarker, hoverShowMarker, _imageUrl_ActionShowMarker);

      final String htmlActionContainer = UI.EMPTY_STRING //
            + "<div class='action-container'>" //$NON-NLS-1$
            + ("<table><tbody><tr>") //$NON-NLS-1$
            + ("<td>" + htmlActionShowHideMarker + "</td>") //$NON-NLS-1$ //$NON-NLS-2$
            + ("<td>" + createHtml_Action(hrefEditMarker, hoverEditMarker, _imageUrl_ActionEdit) + "</td>") //$NON-NLS-1$ //$NON-NLS-2$
            + "</tr></tbody></table>" // //$NON-NLS-1$
            + "</div>" + NL; //$NON-NLS-1$

      sb.append("<div class='title'>" + NL //$NON-NLS-1$

            + htmlActionContainer

            + ("<a class='label-text'" //$NON-NLS-1$
                  + htmlMarkerStyle
                  + (" href='" + hrefOpenMarker + "'") //$NON-NLS-1$ //$NON-NLS-2$
                  + (" name='" + createHtml_MarkerName(markerId) + "'") //$NON-NLS-1$ //$NON-NLS-2$
                  + (" title='" + hoverOpenMarker + "'") //$NON-NLS-1$ //$NON-NLS-2$
                  + ">" + markerLabel + "</a>" + NL) //$NON-NLS-1$ //$NON-NLS-2$

            + "</div>" + NL); //$NON-NLS-1$
      /*
       * Description
       */
      final String description = tourMarker.getDescription();
      String descriptionWithLineBreaks = WEB.convertHTML_LineBreaks(description);

      if (UI.IS_SCRAMBLE_DATA) {
         descriptionWithLineBreaks = UI.scrambleText(descriptionWithLineBreaks);
      }

      sb.append("<a class='label-text' href='" + hrefOpenMarker + "' title='" + hoverOpenMarker + "'>" + NL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      sb.append("   <p class='description'" + htmlMarkerStyle + ">" + descriptionWithLineBreaks + "</p>" + NL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      sb.append("</a>" + NL); //$NON-NLS-1$
   }

   /**
    * Url
    */
   private void create_32_MarkerUrl(final StringBuilder sb, final TourMarker tourMarker) {

      final String urlText = tourMarker.getUrlText();
      final String urlAddress = tourMarker.getUrlAddress();
      final boolean isText = urlText.length() > 0;
      final boolean isAddress = urlAddress.length() > 0;

      if (isText || isAddress) {

         String linkText;

         if (isAddress == false) {

            // only text is in the link -> this is not a internet address but create a link of it

            linkText = "<a href='" + urlText + "' title='" + urlText + "'>" + urlText + "</a>" + NL; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

         } else if (isText == false) {

            linkText = "<a href='" + urlAddress + "' title='" + urlAddress + "'>" + urlAddress + "</a>" + NL; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

         } else {

            linkText = "<a href='" + urlAddress + "' title='" + urlAddress + "'>" + urlText + "</a>" + NL; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
         }

         sb.append(linkText);
      }
   }

   private void createActions() {

      _actionTourBlogOptions = new ActionTourBlogOptions();

      fillActionBars();
   }

   private String createHtml_Action(final String hrefMarker, final String hoverMarker, final String backgroundImage) {

      return "<a class='action'" // //$NON-NLS-1$
            + " style='background-image: url(" + backgroundImage + ");'" //$NON-NLS-1$ //$NON-NLS-2$
            + " href='" + hrefMarker + "'" //$NON-NLS-1$ //$NON-NLS-2$
            + " title='" + hoverMarker + "'" //$NON-NLS-1$ //$NON-NLS-2$
            + ">" //$NON-NLS-1$
            + "</a>"; //$NON-NLS-1$
   }

   private String createHtml_MarkerName(final long markerId) {

      return HREF_MARKER_ITEM + markerId;
   }

   @Override
   public void createPartControl(final Composite parent) {

      _parent = parent;

      initUI();

      createUI(parent);
      createActions();

      addSelectionListener();
      addTourEventListener();
      addPrefListener();

      showInvalidPage();

      // this part is a selection provider
      getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider(ID));

      // show markers from last selection
      onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

      if (_tourData == null) {
         showTourFromTourProvider();
      }
   }

   private void createUI(final Composite parent) {

      _pageBook = new PageBook(parent, SWT.NONE);

      _pageNoData = UI.createUI_PageNoData(_pageBook, Messages.UI_Label_no_chart_is_selected);

      _pageNoBrowser = new Composite(_pageBook, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(_pageNoBrowser);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_pageNoBrowser);
      _pageNoBrowser.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      {
         _txtNoBrowser = new Text(_pageNoBrowser, SWT.WRAP | SWT.READ_ONLY);
         GridDataFactory.fillDefaults()//
               .grab(true, true)
               .align(SWT.FILL, SWT.BEGINNING)
               .applyTo(_txtNoBrowser);
         _txtNoBrowser.setText(Messages.UI_Label_BrowserCannotBeCreated);
      }

      _pageContent = new Composite(_pageBook, SWT.NONE);
      GridLayoutFactory.fillDefaults().applyTo(_pageContent);
      {
         createUI_10_Browser(_pageContent);
      }
   }

   private void createUI_10_Browser(final Composite parent) {

      try {

         try {

            // use default browser
            _browser = new Browser(parent, SWT.NONE);

         } catch (final Exception e) {

            // use WebKit browser for Linux when default browser fails
            _browser = new Browser(parent, SWT.WEBKIT);
         }

         GridDataFactory.fillDefaults().grab(true, true).applyTo(_browser);

         _browser.addLocationListener(changingAdapter(locationEvent -> onBrowserLocationChanging(locationEvent)));

         _browser.addProgressListener(completedAdapter(progressEvent -> onBrowserCompleted()));

      } catch (final SWTError e) {

         _txtNoBrowser.setText(NLS.bind(Messages.UI_Label_BrowserCannotBeCreated_Error, e.getMessage()));
      }
   }

   @Override
   public void dispose() {

      TourManager.getInstance().removeTourEventListener(_tourEventListener);

      getSite().getPage().removePostSelectionListener(_postSelectionListener);

      _prefStore.removePropertyChangeListener(_prefChangeListener);
      _prefStore_Common.removePropertyChangeListener(_prefChangeListener_Common);

      super.dispose();
   }

   private void fillActionBars() {

      /*
       * Fill view toolbar
       */
      final IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();

      tbm.add(_actionTourBlogOptions);
   }

   private void fireMarkerPosition(final StructuredSelection selection) {

      final Object[] selectedMarker = selection.toArray();

      if (selectedMarker.length > 0) {

         final ArrayList<TourMarker> allTourMarker = new ArrayList<>();

         for (final Object object : selectedMarker) {
            allTourMarker.add((TourMarker) object);
         }

         _postSelectionProvider.setSelection(new SelectionTourMarker(_tourData, allTourMarker));
      }
   }

   private void hrefActionEditMarker(final TourMarker selectedTourMarker) {

      if (_tourData.isManualTour()) {
         // a manually created tour do not have time slices -> no markers
         return;
      }

      final DialogMarker markerDialog = new DialogMarker(
            Display.getCurrent().getActiveShell(),
            _tourData,
            selectedTourMarker);

      if (markerDialog.open() == Window.OK) {
         saveModifiedTour();
      }
   }

   private void hrefActionEditTour() {

      if (new DialogQuickEdit(//
            Display.getCurrent().getActiveShell(),
            _tourData).open() == Window.OK) {

         saveModifiedTour();
      }
   }

   private void hrefActionHideMarker(final TourMarker selectedTourMarker) {

      selectedTourMarker.setMarkerVisible(false);

      prepareBrowserReload(selectedTourMarker);

      saveModifiedTour();
   }

   /**
    * Fire a selection for the selected marker(s).
    */
   private void hrefActionOpenMarker(final StructuredSelection selection) {

      // a chart must be available
      if (_tourChart == null) {

         final TourChart tourChart = TourManager.getInstance().getActiveTourChart();

         if ((tourChart == null) || tourChart.isDisposed()) {

            fireMarkerPosition(selection);

            return;

         } else {
            _tourChart = tourChart;
         }
      }

      final Object[] selectedMarker = selection.toArray();

      if (selectedMarker.length > 1) {

         // two or more markers are selected

         _postSelectionProvider.setSelection(new SelectionChartXSliderPosition(
               _tourChart,
               ((TourMarker) selectedMarker[0]).getSerieIndex(),
               ((TourMarker) selectedMarker[selectedMarker.length - 1]).getSerieIndex()));

      } else if (selectedMarker.length > 0) {

         // one marker is selected

         _postSelectionProvider.setSelection(new SelectionChartXSliderPosition(
               _tourChart,
               ((TourMarker) selectedMarker[0]).getSerieIndex(),
               SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION));
      }
   }

   private void hrefActionShowMarker(final TourMarker selectedTourMarker) {

      selectedTourMarker.setMarkerVisible(true);

      prepareBrowserReload(selectedTourMarker);

      saveModifiedTour();
   }

   private void initUI() {

      try {

         /*
          * load css from file
          */
         final File cssFile = WEB.getFile(TOUR_BLOG_CSS);
         final String cssContent = Util.readContentFromFile(cssFile.getAbsolutePath());

         _htmlCss = "<style>" + cssContent + "</style>"; //$NON-NLS-1$ //$NON-NLS-2$

         /*
          * set image urls
          */
         _imageUrl_ActionEdit = net.tourbook.ui.UI.getIconUrl(ThemeUtil.getThemedImageName(Images.App_Edit));
         _imageUrl_ActionHideMarker = net.tourbook.ui.UI.getIconUrl(ThemeUtil.getThemedImageName(Images.App_Hide));
         _imageUrl_ActionShowMarker = net.tourbook.ui.UI.getIconUrl(ThemeUtil.getThemedImageName(Images.App_Show));

      } catch (final Exception e) {
         StatusUtil.showStatus(e);
      }

   }

   private void onBrowserCompleted() {

      if (_reloadedTourMarkerId == null) {
         return;
      }

      // get local copy
      final long reloadedTourMarkerId = _reloadedTourMarkerId;

      /*
       * This must be run async otherwise an endless loop will happen
       */
      _browser.getDisplay().asyncExec(() -> {

         final String href = "location.href='" + createHtml_MarkerName(reloadedTourMarkerId) + "'"; //$NON-NLS-1$ //$NON-NLS-2$

         _browser.execute(href);
      });

      _reloadedTourMarkerId = null;
   }

   private void onBrowserLocationChanging(final LocationEvent event) {

      final String location = event.location;

      if (location.contains(HREF_MARKER_ITEM)) {

         /*
          * Page is reloaded and is scrolled to the tour marker where the last tour marker action is
          * done.
          */

         _browser.setRedraw(true);

         return;
      }

      final String[] locationParts = location.split(HREF_TOKEN);

      if (locationParts.length == 3) {

         // a tour marker id is selected, fire tour marker selection

         try {

            /**
             * Split location<br>
             * Part 1: location, e.g. "about"<br>
             * Part 2: action<br>
             * Part 3: markerID
             */
            final String markerIdText = locationParts[2];
            final long markerId = Long.parseLong(markerIdText);

            // get tour marker by id
            TourMarker hrefTourMarker = null;
            for (final TourMarker tourMarker : _tourData.getTourMarkers()) {
               if (tourMarker.getMarkerId() == markerId) {
                  hrefTourMarker = tourMarker;
                  break;
               }
            }

            final String action = locationParts[1];

            if (hrefTourMarker != null) {

               switch (action) {
               case ACTION_EDIT_MARKER:
                  hrefActionEditMarker(hrefTourMarker);
                  break;

               case ACTION_HIDE_MARKER:
                  hrefActionHideMarker(hrefTourMarker);
                  break;

               case ACTION_OPEN_MARKER:
                  hrefActionOpenMarker(new StructuredSelection(hrefTourMarker));
                  break;

               case ACTION_SHOW_MARKER:
                  hrefActionShowMarker(hrefTourMarker);
                  break;
               }
            }

         } catch (final Exception e) {
            // ignore
         }

      } else if (locationParts.length == 2) {

         final String action = locationParts[1];

         switch (action) {
         case ACTION_EDIT_TOUR:
            hrefActionEditTour();
            break;
         }

      } else if (location.startsWith(HTTP_DUMMY) == false && location.startsWith(EXTERNAL_LINK_URL)) {

         // open link in the external browser

         // check if this is a valid web url and not any other protocol
         WEB.openUrl(location);
      }

      if (location.equals(PAGE_ABOUT_BLANK) == false) {

         // about:blank is the initial page

         event.doit = false;
      }
   }

   private void onSelectionChanged(final ISelection selection) {

      long tourId = TourDatabase.ENTITY_IS_NOT_SAVED;

      if (selection instanceof final SelectionTourData tourDataSelection) {

         // a tour was selected, get the chart and update the marker viewer

         _tourData = tourDataSelection.getTourData();

         if (_tourData == null) {
            _tourChart = null;
         } else {
            _tourChart = tourDataSelection.getTourChart();
            tourId = _tourData.getTourId();
         }

      } else if (selection instanceof final SelectionTourId selectionTourId) {

         _tourChart = null;
         tourId = selectionTourId.getTourId();

      } else if (selection instanceof final SelectionTourIds selectionTourIds) {

         final List<Long> tourIds = selectionTourIds.getTourIds();
         if (CollectionUtils.isNotEmpty(tourIds)) {
            _tourChart = null;
            tourId = tourIds.get(0);
         }

      } else if (selection instanceof final SelectionReferenceTourView tourCatalogSelection) {

         final TVIRefTour_RefTourItem refItem = tourCatalogSelection.getRefItem();
         if (refItem != null) {
            _tourChart = null;
            tourId = refItem.getTourId();
         }

      } else if (selection instanceof final StructuredSelection structuredSelection) {

         _tourChart = null;
         final Object firstElement = structuredSelection.getFirstElement();
         if (firstElement instanceof final TVIRefTour_ComparedTour tviRefTour_ComparedTour) {
            tourId = tviRefTour_ComparedTour.getTourId();
         } else if (firstElement instanceof final TVIElevationCompareResult_ComparedTour tviElevationCompareResult_ComparedTour) {
            tourId = tviElevationCompareResult_ComparedTour.getTourId();
         }

      } else if (selection instanceof SelectionDeletedTours) {

         clearView();
      }

      if (tourId >= TourDatabase.ENTITY_IS_NOT_SAVED) {

         final TourData tourData = TourManager.getInstance().getTourData(tourId);
         if (tourData != null) {
            _tourData = tourData;
         }
      }

      final boolean isTourAvailable = (tourId >= 0) && (_tourData != null);
      if (isTourAvailable && _browser != null) {

         updateUI();

      } else if (_tourData == null) {

         clearView();
      }
   }

   /**
    * Keeps the current browser scroll position.
    *
    * @param tourMarker
    */
   private void prepareBrowserReload(final TourMarker tourMarker) {

      _reloadedTourMarkerId = tourMarker.getMarkerId();

      _browser.setRedraw(false);
   }

   private void reloadTourData() {

      if (_tourData == null) {
         return;
      }

      final Long tourId = _tourData.getTourId();
      final TourManager tourManager = TourManager.getInstance();

      tourManager.removeTourFromCache(tourId);

      _tourData = tourManager.getTourDataFromDb(tourId);

      // removed old tour data from the selection provider
      _postSelectionProvider.clearSelection();

      updateUI();
   }

   private void saveModifiedTour() {

      /*
       * Run async because a tour save will fire a tour change event.
       */
      _parent.getDisplay().asyncExec(() -> TourManager.saveModifiedTour(_tourData));
   }

   @Override
   public void setFocus() {

   }

   private void showInvalidPage() {

      _pageBook.showPage(_browser == null ? _pageNoBrowser : _pageNoData);
   }

   private void showTourFromTourProvider() {

      showInvalidPage();

      // a tour is not displayed, find a tour provider which provides a tour
      Display.getCurrent().asyncExec(() -> {

         // validate widget
         if (_pageBook.isDisposed()) {
            return;
         }

         /*
          * check if tour was set from a selection provider
          */
         if (_tourData != null) {
            return;
         }

         final List<TourData> selectedTours = TourManager.getSelectedTours();

         if (CollectionUtils.isNotEmpty(selectedTours)) {

            onSelectionChanged(new SelectionTourData(selectedTours.get(0)));
         }
      });
   }

   /**
    * Update the UI from {@link #_tourData}.
    */
   void updateUI() {

      if (_tourData == null || _browser == null) {
         return;
      }

      _pageBook.showPage(_pageContent);

      _isDrawWithDefaultColor = _state.getBoolean(STATE_IS_DRAW_MARKER_WITH_DEFAULT_COLOR);
      _isShowHiddenMarker = _state.getBoolean(STATE_IS_SHOW_HIDDEN_MARKER);
      _isShowTourMarkers = _state.getBoolean(STATE_IS_SHOW_TOUR_MARKERS);

      final String graphMarker_ColorDefault = UI.IS_DARK_THEME
            ? ITourbookPreferences.GRAPH_MARKER_COLOR_DEFAULT_DARK
            : ITourbookPreferences.GRAPH_MARKER_COLOR_DEFAULT;

      final String graphMarker_ColorHidden = UI.IS_DARK_THEME
            ? ITourbookPreferences.GRAPH_MARKER_COLOR_HIDDEN_DARK
            : ITourbookPreferences.GRAPH_MARKER_COLOR_HIDDEN;

      final String graphMarker_ColorDevice = UI.IS_DARK_THEME
            ? ITourbookPreferences.GRAPH_MARKER_COLOR_DEVICE_DARK
            : ITourbookPreferences.GRAPH_MARKER_COLOR_DEVICE;

      _cssMarker_DefaultColor = CSS.color(PreferenceConverter.getColor(_prefStore, graphMarker_ColorDefault));
      _cssMarker_HiddenColor = CSS.color(PreferenceConverter.getColor(_prefStore, graphMarker_ColorHidden));
      _cssMarker_DeviceColor = CSS.color(PreferenceConverter.getColor(_prefStore, graphMarker_ColorDevice));

//      Force Internet Explorer to not use compatibility mode. Internet Explorer believes that websites under
//      several domains (including "ibm.com") require compatibility mode. You may see your web application run
//      normally under "localhost", but then fail when hosted under another domain (e.g.: "ibm.com").
//      Setting "IE=Edge" will force the latest standards mode for the version of Internet Explorer being used.
//      This is supported for Internet Explorer 8 and later. You can also ease your testing efforts by forcing
//      specific versions of Internet Explorer to render using the standards mode of previous versions. This
//      prevents you from exploiting the latest features, but may offer you compatibility and stability. Lookup
//      the online documentation for the "X-UA-Compatible" META tag to find which value is right for you.

      final String html = UI.EMPTY_STRING
            + "<!DOCTYPE html>" + NL // ensure that IE is using the newest version and not the quirk mode //$NON-NLS-1$
            + "<html style='height: 100%; width: 100%; margin: 0px; padding: 0px;'>" + NL //$NON-NLS-1$
            + "<head>" + NL + create_10_Head() + NL + "</head>" + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "<body>" + NL + create_20_Body() + NL + "</body>" + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "</html>"; //$NON-NLS-1$

      _browser.setRedraw(true);
      _browser.setText(html);
   }
}
