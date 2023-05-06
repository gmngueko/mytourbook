/*******************************************************************************
 * Copyright (C) 2005, 2023 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourCatalog;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.OtherMessages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartDataXSerie;
import net.tourbook.chart.ChartDataYSerie;
import net.tourbook.chart.ChartStatisticSegments;
import net.tourbook.chart.ChartTitleSegmentConfig;
import net.tourbook.chart.ChartType;
import net.tourbook.chart.IChartInfoProvider;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.CommonImages;
import net.tourbook.common.UI;
import net.tourbook.common.color.GraphColorManager;
import net.tourbook.common.color.ThemeUtil;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.tooltip.ActionToolbarSlideout;
import net.tourbook.common.tooltip.ToolbarSlideout;
import net.tourbook.common.util.ArrayListToArray;
import net.tourbook.common.util.IToolTipProvider;
import net.tourbook.common.util.PostSelectionProvider;
import net.tourbook.common.util.TreeViewerItem;
import net.tourbook.common.util.Util;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourInfoIconToolTipProvider;
import net.tourbook.tour.TourManager;

import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class RefTour_YearStatistic_View extends ViewPart {

   public static final String  ID                            = "net.tourbook.views.tourCatalog.yearStatisticView"; //$NON-NLS-1$

   private static final String STATE_IS_SHOW_ALL_VALUES      = "STATE_IS_SHOW_ALL_VALUES";                         //$NON-NLS-1$
   private static final String STATE_IS_SYNC_MIN_MAX_VALUES  = "STATE_IS_SYNC_MIN_MAX_VALUES";                     //$NON-NLS-1$
   private static final String STATE_NUMBER_OF_VISIBLE_YEARS = "STATE_NUMBER_OF_VISIBLE_YEARS";                    //$NON-NLS-1$

   static final String         STATE_SHOW_AVG_PULSE          = "STATE_SHOW_AVG_PULSE";                             //$NON-NLS-1$
   static final String         STATE_SHOW_MAX_PULSE          = "STATE_SHOW_MAX_PULSE";                             //$NON-NLS-1$
   static final String         STATE_SHOW_AVG_SPEED          = "STATE_SHOW_AVG_SPEED";                             //$NON-NLS-1$
   static final String         STATE_SHOW_AVG_ALTIMETER      = "STATE_SHOW_AVG_ALTIMETER";                         //$NON-NLS-1$

// SET_FORMATTING_OFF

   private static final String   PREF_PREFIX                         = "GRID_REF_TOUR_YEAR_STATISTIC__";                   //$NON-NLS-1$

   private static final String   GRID_IS_SHOW_VERTICAL_GRIDLINES     = PREF_PREFIX + ITourbookPreferences.CHART_GRID_IS_SHOW_VERTICAL_GRIDLINES;
   private static final String   GRID_IS_SHOW_HORIZONTAL_GRIDLINES   = PREF_PREFIX + ITourbookPreferences.CHART_GRID_IS_SHOW_HORIZONTAL_GRIDLINES;
   private static final String   GRID_VERTICAL_DISTANCE              = PREF_PREFIX + ITourbookPreferences.CHART_GRID_VERTICAL_DISTANCE;
   private static final String   GRID_HORIZONTAL_DISTANCE            = PREF_PREFIX + ITourbookPreferences.CHART_GRID_HORIZONTAL_DISTANCE;

// SET_FORMATTING_ON

   private static final boolean          IS_OSX            = net.tourbook.common.UI.IS_OSX;
   private static final boolean          IS_LINUX          = net.tourbook.common.UI.IS_LINUX;

   private static final IDialogSettings  _state            = TourbookPlugin.getState(ID);
   private static final IPreferenceStore _prefStore        = TourbookPlugin.getPrefStore();
   private static final IPreferenceStore _prefStore_Common = CommonActivator.getPrefStore();

   private static final NumberFormat     _nf1              = NumberFormat.getNumberInstance();
   {
      _nf1.setMinimumFractionDigits(1);
      _nf1.setMaximumFractionDigits(1);
   }

   private IPropertyChangeListener _prefChangeListener;
   private IPropertyChangeListener _prefChangeListener_Common;
   private ISelectionListener      _postSelectionListener;
   private PostSelectionProvider   _postSelectionProvider;

   private int                     _numVisibleYears;
   private int[]                   _allVisibleYears;
   private int[]                   _allNumberOfDaysInYear;

   /**
    * This is the last year (on the right side) which is displayed in the statistics
    */
   private int                     _lastVisibleYear    = TimeTools.now().getYear();

   /**
    * Contains all years which the user can select as start year in the combo box
    */
   private ArrayList<Integer>      _allSelectableYears = new ArrayList<>();

   /*
    * Statistic values for all visible years
    */
   private ArrayList<TVICatalogComparedTour> _statValues_AllTours     = new ArrayList<>();
   private ArrayList<Integer>                _statValues_DOYValues    = new ArrayList<>();

   private ArrayList<Float>                  _statValues_AvgAltimeter = new ArrayList<>();
   private ArrayList<Float>                  _statValues_AvgPulse     = new ArrayList<>();
   private ArrayList<Float>                  _statValues_AvgSpeed     = new ArrayList<>();
   private ArrayList<Float>                  _statValues_MaxPulse     = new ArrayList<>();

   /**
    * Reference tour item for which the statistic is displayed. This statistic can display only
    * compared tours for ONE reference tour.
    */
   private TVICatalogRefTourItem             _currentRefItem;

   /**
    * Selection which is thrown by the year statistic
    */
   private StructuredSelection               _currentSelection;

   private ITourEventListener                _tourEventListener;

   private boolean                           _isShowAllValues;
   private boolean                           _isSynchMinMaxValue;

   /**
    * Contains the index in {@link #_statValues_AllTours} for the currently selected tour.
    */
   private int                               _selectedTourIndex;

   private ActionCopyValuesIntoClipboard     _actionCopyValuesIntoClipboard;
   private ActionShowAllValues               _actionShowAllValues;
   private ActionSyncMinMaxValues            _actionSyncMinMaxValues;
   private ActionYearStatisticOptions        _actionYearStatOptions;

   private YearStatisticTourToolTip          _tourToolTip;
   private TourInfoIconToolTipProvider       _tourInfoToolTipProvider = new TourInfoIconToolTipProvider();

   private PixelConverter                    _pc;

   private YearContributionItem              _yearSelector;

   /*
    * UI controls
    */
   private PageBook  _pageBook;
   private Composite _pageChart;
   private Composite _pageNoChart;

   private Chart     _yearChart;

   private Label     _lblRefTourTitle;

   private class ActionCopyValuesIntoClipboard extends Action {

      private ActionCopyValuesIntoClipboard() {

         super(UI.EMPTY_STRING, AS_PUSH_BUTTON);

         setToolTipText(Messages.Tour_StatisticValues_Action_CopyIntoClipboard_Tooltip);

         setImageDescriptor(CommonActivator.getThemedImageDescriptor(CommonImages.App_Copy));
         setDisabledImageDescriptor(CommonActivator.getThemedImageDescriptor(CommonImages.App_Copy_Disabled));
      }

      @Override
      public void run() {

         onAction_CopyIntoClipboard();
      }
   }

   private class ActionShowAllValues extends Action {

      public ActionShowAllValues() {

         super(UI.EMPTY_STRING, AS_CHECK_BOX);

         setToolTipText(Messages.Year_Statistic_Action_ShowAllValues_Tooltip);

         setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.RefTour_Statistic_Show_All_Values));
      }

      @Override
      public void run() {

         onAction_ShowAllValues(isChecked());
      }
   }

   private class ActionSyncMinMaxValues extends Action {

      public ActionSyncMinMaxValues() {

         super(UI.EMPTY_STRING, AS_CHECK_BOX);

         setToolTipText(Messages.tourCatalog_view_action_synch_chart_years_tooltip);

         setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.SyncStatistics));
      }

      @Override
      public void run() {

         onAction_SyncMinMaxValues(isChecked());
      }
   }

   private class ActionYearStatisticOptions extends ActionToolbarSlideout {

      @Override
      protected ToolbarSlideout createSlideout(final ToolBar toolbar) {

         return new SlideoutYearStatisticOptions(RefTour_YearStatistic_View.this, _pageBook, toolbar, PREF_PREFIX, _state);
      }
   }

   public RefTour_YearStatistic_View() {}

   private void addPrefListener() {

      _prefChangeListener = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(GRID_HORIZONTAL_DISTANCE)
               || property.equals(GRID_VERTICAL_DISTANCE)
               || property.equals(GRID_IS_SHOW_HORIZONTAL_GRIDLINES)
               || property.equals(GRID_IS_SHOW_VERTICAL_GRIDLINES)) {

            updateUI_YearChart(false);
         }
      };

      _prefChangeListener_Common = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ICommonPreferences.MEASUREMENT_SYSTEM)) {

            // measurement system has changed -> recreate the chart

            _yearChart.dispose();
            createUI_30_Chart(_pageChart);

            _pageChart.layout();

            updateUI_YearChart(false);
         }
      };

      _prefStore.addPropertyChangeListener(_prefChangeListener);
      _prefStore_Common.addPropertyChangeListener(_prefChangeListener_Common);
   }

   /**
    * Listen for events when a tour is selected
    */
   private void addSelectionListener() {

      _postSelectionListener = (part, selection) -> {

         // prevent to listen to a selection which is originated by this year chart

         if (selection != _currentSelection) {
            onSelectionChanged(selection);
         }
      };

      getSite().getPage().addPostSelectionListener(_postSelectionListener);
   }

   private void addTourEventListener() {

      _tourEventListener = new ITourEventListener() {
         @Override
         public void tourChanged(final IWorkbenchPart part,
                                 final TourEventId propertyId,
                                 final Object propertyData) {

            if (propertyId == TourEventId.COMPARE_TOUR_CHANGED
                  && propertyData instanceof TourPropertyCompareTourChanged) {

               final TourPropertyCompareTourChanged compareTourProperty = (TourPropertyCompareTourChanged) propertyData;

               if (compareTourProperty.isDataSaved) {
                  updateUI_YearChart(false);
               }
            }
         }
      };

      TourManager.getInstance().addTourEventListener(_tourEventListener);
   }

   /**
    * Set/restore min/max values.
    *
    * @param yData
    * @param minMaxValues
    */
   private void adjustMinMaxValues(final ChartDataYSerie yData, final float[] minMaxValues) {

      final float dataMinValue = (float) yData.getVisibleMinValue();
      final float dataMaxValue = (float) yData.getVisibleMaxValue();

      if (_isSynchMinMaxValue) {

         if (minMaxValues[0] == Float.MIN_VALUE
               || minMaxValues[1] == Float.MAX_VALUE) {

            // min/max values have not yet been set

            /*
             * Set the min value 10% below the computed so that the lowest value is not at the
             * bottom
             */
            yData.setVisibleMinValue(dataMinValue);
            yData.setVisibleMaxValue(dataMaxValue);

            minMaxValues[0] = dataMinValue;
            minMaxValues[1] = dataMaxValue;

         } else {

            // set min/max to previous values

            yData.setVisibleMinValue(minMaxValues[0]);
            yData.setVisibleMaxValue(minMaxValues[1]);
         }

      } else {

         // set min/max to the data min/max values

         yData.setVisibleMinValue(dataMinValue);
         yData.setVisibleMaxValue(dataMaxValue);
      }
   }

   private void createActions() {

      _actionCopyValuesIntoClipboard = new ActionCopyValuesIntoClipboard();
      _actionShowAllValues = new ActionShowAllValues();
      _actionSyncMinMaxValues = new ActionSyncMinMaxValues();
      _actionYearStatOptions = new ActionYearStatisticOptions();

      fillActionBars();
   }

   /**
    * Create segments for the chart
    */
   private ChartStatisticSegments createChartSegments() {

      final double[] segmentStart = new double[_numVisibleYears];
      final double[] segmentEnd = new double[_numVisibleYears];
      final String[] segmentTitle = new String[_numVisibleYears];

      final int firstYear = getFirstVisibleYear();
      int yearDaysSum = 0;

      // create segments for each year
      for (int yearDayIndex = 0; yearDayIndex < _allNumberOfDaysInYear.length; yearDayIndex++) {

         final int yearDays = _allNumberOfDaysInYear[yearDayIndex];

         segmentStart[yearDayIndex] = yearDaysSum;
         segmentEnd[yearDayIndex] = yearDaysSum + yearDays - 1;
         segmentTitle[yearDayIndex] = Integer.toString(firstYear + yearDayIndex);

         yearDaysSum += yearDays;
      }

      final ChartStatisticSegments chartSegments = new ChartStatisticSegments();
      chartSegments.segmentStartValue = segmentStart;
      chartSegments.segmentEndValue = segmentEnd;
      chartSegments.segmentTitle = segmentTitle;

      chartSegments.years = _allVisibleYears;
      chartSegments.yearDays = _allNumberOfDaysInYear;
      chartSegments.allValues = yearDaysSum;

      return chartSegments;
   }

   @Override
   public void createPartControl(final Composite parent) {

      initUI(parent);
      createUI(parent);

      addSelectionListener();
      addTourEventListener();
      addPrefListener();

      createActions();

      // set selection provider
      getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider(ID));

      _pageBook.showPage(_pageNoChart);

      restoreState();

      // restore selection
      onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

      parent.getDisplay().asyncExec(() -> enableControls());
   }

   /**
    * All items from the ref tour are from type {@link TVICatalogComparedTour}
    *
    * @param firstVisibleYear
    * @param isShowLatestYear
    */
   private void createStatisticData_WithoutYearCategories(int firstVisibleYear,
                                                          final boolean isShowLatestYear) {

      final Object[] allItems = _currentRefItem.getFetchedChildrenAsArray();

      if (_isShowAllValues
            && allItems != null
            && allItems.length > 0
            && allItems[0] instanceof TVICatalogComparedTour
            && allItems[allItems.length - 1] instanceof TVICatalogComparedTour) {

         final int firstYear = ((TVICatalogComparedTour) allItems[0]).year;
         final int lastYear = ((TVICatalogComparedTour) allItems[allItems.length - 1]).year;

         firstVisibleYear = firstYear;
         _lastVisibleYear = lastYear;

         _numVisibleYears = lastYear - firstYear + 1;
         _yearSelector.spinnerNumberOfVisibleYears.setSelection(_numVisibleYears);

         // update year data
         setYearData();
      }

      // loop: all tours
      for (final Object item : allItems) {

         if (item instanceof TVICatalogComparedTour) {

            final TVICatalogComparedTour tourItem = (TVICatalogComparedTour) item;

            final int tourYear = tourItem.year;

            if (tourYear >= firstVisibleYear && tourYear <= _lastVisibleYear) {

               final LocalDate tourDate = tourItem.tourDate;

               _statValues_AllTours.add(tourItem);
               _statValues_DOYValues.add(getYearDOYs(tourDate.getYear()) + tourDate.getDayOfYear() - 1);

               _statValues_AvgAltimeter.add(tourItem.getAvgAltimeter());
               _statValues_AvgPulse.add(tourItem.getAvgPulse());
               _statValues_MaxPulse.add(tourItem.getMaxPulse());
               _statValues_AvgSpeed.add(tourItem.getTourSpeed() / UI.UNIT_VALUE_DISTANCE);
            }
         }
      }
   }

   /**
    * All items from the ref tour are from type {@link TVICatalogYearItem}
    *
    * @param firstVisibleYear
    * @param isShowLatestYear
    */
   private void createStatisticData_WithYearCategories(int firstVisibleYear,
                                                       final boolean isShowLatestYear) {

      final Object[] allItems = _currentRefItem.getFetchedChildrenAsArray();

      if (allItems != null
            && allItems.length > 0
            && allItems[0] instanceof TVICatalogYearItem
            && allItems[allItems.length - 1] instanceof TVICatalogYearItem) {

         final int firstYear = ((TVICatalogYearItem) allItems[0]).year;
         final int lastYear = ((TVICatalogYearItem) allItems[allItems.length - 1]).year;

         if (_isShowAllValues) {

            firstVisibleYear = firstYear;
            _lastVisibleYear = lastYear;

            _numVisibleYears = lastYear - firstYear + 1;
            _yearSelector.spinnerNumberOfVisibleYears.setSelection(_numVisibleYears);

            // update year data
            setYearData();

         } else if (isShowLatestYear) {

            // get the last year when it's forced

            /*
             * Use current years when the new items are in the current range, otherwise adjust the
             * years
             */
            if (lastYear <= _lastVisibleYear && firstYear >= _lastVisibleYear - _numVisibleYears) {

               // new years are within the current year range

            } else {

               // overwrite last year
               _lastVisibleYear = lastYear;
            }
         }
      }

      /*
       * Create data for all years
       */
      for (final Object yearItemObj : allItems) {

         if (yearItemObj instanceof TVICatalogYearItem) {

            final TVICatalogYearItem yearItem = (TVICatalogYearItem) yearItemObj;

            // check if the year can be displayed

            final int yearItemYear = yearItem.year;

            if (yearItemYear >= firstVisibleYear && yearItemYear <= _lastVisibleYear) {

               // loop: all tours
               final Object[] allTourItems = yearItem.getFetchedChildrenAsArray();

               for (final Object tourItemObj : allTourItems) {

                  if (tourItemObj instanceof TVICatalogComparedTour) {

                     final TVICatalogComparedTour tourItem = (TVICatalogComparedTour) tourItemObj;

                     final LocalDate tourDate = tourItem.tourDate;

                     _statValues_AllTours.add(tourItem);
                     _statValues_DOYValues.add(getYearDOYs(tourDate.getYear()) + tourDate.getDayOfYear() - 1);

                     _statValues_AvgAltimeter.add(tourItem.getAvgAltimeter());
                     _statValues_AvgPulse.add(tourItem.getAvgPulse());
                     _statValues_MaxPulse.add(tourItem.getMaxPulse());
                     _statValues_AvgSpeed.add(tourItem.getTourSpeed() / UI.UNIT_VALUE_DISTANCE);
                  }
               }
            }
         }
      }
   }

   /**
    * @param toolTipProvider
    * @param parent
    * @param serieIndex
    * @param valueIndex
    * @param frequencyStatistic
    */
   private void createToolTipUI(final IToolTipProvider toolTipProvider,
                                final Composite parent,
                                int valueIndex) {

      if (valueIndex >= _statValues_DOYValues.size()) {
         valueIndex -= _statValues_DOYValues.size();
      }

      if (_statValues_DOYValues == null || valueIndex >= _statValues_DOYValues.size()) {
         return;
      }

      /*
       * Get day/month/year
       */
      final int firstYear = getFirstVisibleYear();
      final int tourDOY = _statValues_DOYValues.get(valueIndex);

      final ZonedDateTime tourDate = ZonedDateTime
            .of(firstYear, 1, 1, 0, 0, 0, 1, TimeTools.getDefaultTimeZone())
            .plusDays(tourDOY);
      final String title = tourDate.format(TimeTools.Formatter_Date_F);

      new RefTour_YearStatistic_TooltipUI().createContentArea(

            parent,
            toolTipProvider,

            title,

            _statValues_AvgAltimeter.get(valueIndex),
            _statValues_AvgPulse.get(valueIndex),
            _statValues_MaxPulse.get(valueIndex),
            _statValues_AvgSpeed.get(valueIndex));
   }

   private void createUI(final Composite parent) {

      _pageBook = new PageBook(parent, SWT.NONE);

      _pageNoChart = UI.createUI_PageNoData(_pageBook, Messages.tourCatalog_view_label_year_not_selected);

      _pageChart = createUI_10_PageYearChart(_pageBook);
   }

   private Composite createUI_10_PageYearChart(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
      GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(1).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_YELLOW);
      {
         createUI_20_Title(container);
         createUI_30_Chart(container);
      }

      return container;
   }

   /**
    * Title
    */
   private void createUI_20_Title(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults()
            .grab(true, false)
            .align(SWT.FILL, SWT.FILL)
            .applyTo(container);
      GridLayoutFactory.fillDefaults()
            .numColumns(1)
            .margins(3, 3)
            .applyTo(container);
//      container.setBackground(UI.SYS_COLOR_GREEN);
      container.setBackground(ThemeUtil.getDefaultBackgroundColor_Table());
      {
         {
            /*
             * Ref tour title
             */
            _lblRefTourTitle = new Label(container, SWT.NONE);
            GridDataFactory.fillDefaults()
                  .grab(true, true)
                  .align(SWT.CENTER, SWT.CENTER)
                  .applyTo(_lblRefTourTitle);
//            _lblRefTourTitle.setBackground(UI.SYS_COLOR_RED);
         }
      }
   }

   /**
    * Year chart
    */
   private void createUI_30_Chart(final Composite parent) {

      _yearChart = new Chart(parent, SWT.NONE);
      _yearChart.addBarSelectionListener((serieIndex, valueIndex) -> onSelect_ComparedTour(valueIndex));
      GridDataFactory.fillDefaults().grab(true, true).applyTo(_yearChart);

      // set tour info icon into the left axis
      _tourToolTip = new YearStatisticTourToolTip(_yearChart.getToolTipControl());
      _tourToolTip.addToolTipProvider(_tourInfoToolTipProvider);
      _tourToolTip.addHideListener(event -> {

         // hide hovered image
         _yearChart.getToolTipControl().afterHideToolTip();
      });

      _yearChart.setTourInfoIconToolTipProvider(_tourInfoToolTipProvider);
      _tourInfoToolTipProvider.setActionsEnabled(true);

   }

   @Override
   public void dispose() {

      getSite().getPage().removePostSelectionListener(_postSelectionListener);
      TourManager.getInstance().removeTourEventListener(_tourEventListener);

      _prefStore.removePropertyChangeListener(_prefChangeListener);
      _prefStore_Common.removePropertyChangeListener(_prefChangeListener_Common);

      super.dispose();
   }

   private void enableControls() {

      final boolean canSelectYears = _isShowAllValues == false;

      _yearSelector.comboLastVisibleYear.setEnabled(canSelectYears);
      _yearSelector.spinnerNumberOfVisibleYears.setEnabled(canSelectYears);
   }

   private void fillActionBars() {

      /*
       * Fill view toolbar
       */
      final IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();

      _yearSelector = new YearContributionItem(this);

      tbm.add(_yearSelector);

      tbm.add(_actionShowAllValues);
      tbm.add(_actionSyncMinMaxValues);
//    tbm.add(_actionCopyStatValuesIntoClipboard);
      tbm.add(_actionYearStatOptions);

      tbm.update(true);
   }

   private int getFirstVisibleYear() {

      return _lastVisibleYear - _numVisibleYears + 1;
   }

   /**
    * @param currentYear
    * @param numberOfYears
    * @return Returns the number of days between {@link #fLastYear} and currentYear
    */
   int getYearDOYs(final int selectedYear) {

      int yearDOYs = 0;
      int yearIndex = 0;

      final int firstVisibleYear = getFirstVisibleYear();

      for (int currentYear = firstVisibleYear; currentYear < selectedYear; currentYear++) {

         if (currentYear == selectedYear) {
            return yearDOYs;
         }

         yearDOYs += _allNumberOfDaysInYear[yearIndex];

         yearIndex++;
      }

      return yearDOYs;
   }

   private void initUI(final Composite parent) {

      _pc = new PixelConverter(parent);
   }

   /**
    * get numbers for each year <br>
    * <br>
    * all years into {@link #fYears} <br>
    * number of day's into {@link #_allNumberOfDaysInYear} <br>
    * number of week's into {@link #fYearWeeks}
    */
   void initYearNumbers() {

   }

   TVICatalogComparedTour navigateTour(final boolean isNextTour) {

      final int numTours = _statValues_AllTours.size();

      if (numTours < 2) {
         return null;
      }

      int navIndex;
      if (isNextTour) {

         // get nexttour

         if (_selectedTourIndex >= numTours - 1) {

            navIndex = 0;

         } else {

            navIndex = _selectedTourIndex + 1;
         }

      } else {

         // get previous tour

         if (_selectedTourIndex <= 0) {

            navIndex = numTours - 1;

         } else {

            navIndex = _selectedTourIndex - 1;
         }
      }

      return _statValues_AllTours.get(navIndex);
   }

   private void onAction_CopyIntoClipboard() {
      // TODO Auto-generated method stub

   }

   private void onAction_ShowAllValues(final boolean isShowAllValues) {

      _isShowAllValues = isShowAllValues;

      updateUI_YearChart(false);

      enableControls();
   }

   private void onAction_SyncMinMaxValues(final boolean isSyncMinMaxValue) {

      _isSynchMinMaxValue = isSyncMinMaxValue;

      updateUI_YearChart(false);
   }

   /**
    * A compared tour is selected in the chart
    *
    * @param valueIndex
    */
   private void onSelect_ComparedTour(final int valueIndex) {

      if (_statValues_AllTours.isEmpty()) {

         _tourInfoToolTipProvider.setTourId(-1);
         return;
      }

      // ensure list size
      _selectedTourIndex = Math.min(valueIndex, _statValues_AllTours.size() - 1);

      // select tour in the tour viewer and show tour in compared tour char
      final TVICatalogComparedTour tourCatalogComparedTour = _statValues_AllTours.get(_selectedTourIndex);
      _currentSelection = new StructuredSelection(tourCatalogComparedTour);
      _postSelectionProvider.setSelection(_currentSelection);

      _tourInfoToolTipProvider.setTourId(tourCatalogComparedTour.getTourId());
   }

   void onSelect_LastVisibleYear() {

      // get last visible year
      _lastVisibleYear = _allSelectableYears.get(_yearSelector.comboLastVisibleYear.getSelectionIndex());

      // update year data
      setYearData();

      updateUI_YearChart(false);
   }

   /**
    * Update statistic by setting number of visible years
    */
   void onSelect_NumberOfVisibleYears() {

      // get selected tour
      long selectedTourId = 0;

      if (_statValues_AllTours.isEmpty()) {
         selectedTourId = -1;

      } else {

         final int selectedTourIndex = Math.min(_selectedTourIndex, _statValues_AllTours.size() - 1);
         selectedTourId = _statValues_AllTours.get(selectedTourIndex).getTourId();
      }

      _numVisibleYears = _yearSelector.spinnerNumberOfVisibleYears.getSelection();

      setYearData();

      updateUI_YearChart(false);

      // reselect last selected tour
      selectTourInYearChart(selectedTourId);
   }

   private void onSelectionChanged(final ISelection selection) {

      if (selection instanceof SelectionTourCatalogView) {

         final SelectionTourCatalogView tourCatalogItem = (SelectionTourCatalogView) selection;

         final TVICatalogRefTourItem refItem = tourCatalogItem.getRefItem();
         if (refItem != null) {

            // reference tour is selected

            _currentRefItem = refItem;

            updateUI_YearChart(true);

         } else {

            // show statistic for a specific year

            final TVICatalogYearItem yearItem = tourCatalogItem.getYearItem();
            if (yearItem != null) {

               _currentRefItem = yearItem.getRefItem();

               // overwrite last year
               _lastVisibleYear = yearItem.year;

               // update year data
               setYearData();

               updateUI_YearChart(false);
            }
         }

         // select tour in the statistic
         final Long compTourId = tourCatalogItem.getCompTourId();
         if (compTourId != null) {

            selectTourInYearChart(compTourId);

         } else {

            // select first tour for the youngest year
            int yearIndex = 0;
            for (final TVICatalogComparedTour tourItem : _statValues_AllTours) {

               if (tourItem.tourDate.getYear() == _lastVisibleYear) {
                  break;
               }

               yearIndex++;
            }

            final int allTourSize = _statValues_AllTours.size();

            if (allTourSize > 0 && yearIndex < allTourSize) {
               selectTourInYearChart(_statValues_AllTours.get(yearIndex).getTourId());
            }
         }

      } else if (selection instanceof StructuredSelection) {

         final StructuredSelection structuredSelection = (StructuredSelection) selection;

         if (structuredSelection.size() > 0) {

            final Object firstElement = structuredSelection.getFirstElement();

            if (firstElement instanceof TVICatalogComparedTour) {

               final TVICatalogComparedTour compareItem = (TVICatalogComparedTour) firstElement;

               // get year item
               final TreeViewerItem compareParentItem = compareItem.getParentItem();
               if (compareParentItem instanceof TVICatalogYearItem) {

                  final TVICatalogYearItem yearItem = (TVICatalogYearItem) compareParentItem;

                  // get ref tour item
                  final TreeViewerItem yearParentItem = yearItem.getParentItem();
                  if (yearParentItem instanceof TVICatalogRefTourItem) {

                     final TVICatalogRefTourItem refTourItem = (TVICatalogRefTourItem) yearParentItem;

                     final long refId = refTourItem.refId;

                     if (_currentRefItem == null) {

                        // create new ref item for the ref tour
                        _currentRefItem = TourCompareManager.createCatalogRefItem(refId);

                        updateUI_YearChart(false);

                     } else {

                        if (_currentRefItem.refId != refId) {

                           // the current statistic do not show the ref tour for the compared tour
                           // -> show also the ref tour

                           _currentRefItem = refTourItem;

                           updateUI_YearChart(false);
                        }
                     }
                  }
               }

               // select tour in the year chart
               final Long tourId = compareItem.getTourId();
               if (tourId != null) {
                  selectTourInYearChart(tourId);
               }

            } else if (firstElement instanceof TVICompareResultComparedTour) {

               final TVICompareResultComparedTour compareResult = (TVICompareResultComparedTour) firstElement;

               final Long tourId = compareResult.getTourId();
               if (tourId != null) {

                  final RefTourItem refTour = compareResult.refTour;

                  final long refId = refTour.refId;
                  if (_currentRefItem == null || _currentRefItem.refId != refId) {

                     // the current statistic do not show the ref tour for the compared tour
                     // -> first show the ref tour

                     // create new ref item for the ref tour
                     _currentRefItem = TourCompareManager.createCatalogRefItem(refId);

                     updateUI_YearChart(false);
                  }

                  selectTourInYearChart(tourId);
               }
            }
         }

      } else if (selection instanceof SelectionRemovedComparedTours) {

         final SelectionRemovedComparedTours removedCompTours = (SelectionRemovedComparedTours) selection;

         final ArrayList<ElevationCompareResult> allCompareResults = removedCompTours.removedComparedTours;

         if (allCompareResults.size() > 0) {

            // create new ref item which do not contain the removed compared tours

            final ElevationCompareResult compareResult = allCompareResults.get(0);
            final long refTourId = compareResult.refTourId;

            _currentRefItem = TourCompareManager.createCatalogRefItem(refTourId);

            updateUI_YearChart(false);
         }

      } else if (selection instanceof SelectionPersistedCompareResults) {

         final SelectionPersistedCompareResults savedCompTours = (SelectionPersistedCompareResults) selection;

         final ArrayList<TVICompareResultComparedTour> persistedCompareResults = savedCompTours.persistedCompareResults;

         if (persistedCompareResults.size() > 0) {

            // create new ref item which contains the newly persisted compared tours

            final TVICompareResultComparedTour tviCompareResultComparedTour = persistedCompareResults.get(0);
            final RefTourItem refTour = tviCompareResultComparedTour.refTour;
            final long savedRefId = refTour.refId;

            _currentRefItem = TourCompareManager.createCatalogRefItem(savedRefId);

            updateUI_YearChart(false);
         }
      }
   }

   private void restoreState() {

      _isShowAllValues = Util.getStateBoolean(_state, STATE_IS_SHOW_ALL_VALUES, false);
      _isSynchMinMaxValue = Util.getStateBoolean(_state, STATE_IS_SYNC_MIN_MAX_VALUES, false);

      _actionShowAllValues.setChecked(_isShowAllValues);
      _actionSyncMinMaxValues.setChecked(_isSynchMinMaxValue);

      final int numVisibleYears = Util.getStateInt(_state, RefTour_YearStatistic_View.STATE_NUMBER_OF_VISIBLE_YEARS, 3);
      _numVisibleYears = numVisibleYears;
      _yearSelector.spinnerNumberOfVisibleYears.setSelection(numVisibleYears);

      setYearData();
   }

   @PersistState
   private void saveState() {

      _state.put(STATE_IS_SHOW_ALL_VALUES, _isShowAllValues);
      _state.put(STATE_IS_SYNC_MIN_MAX_VALUES, _isSynchMinMaxValue);
      _state.put(STATE_NUMBER_OF_VISIBLE_YEARS, _numVisibleYears);
   }

   /**
    * select the tour in the year map chart
    *
    * @param tourIdToSelect
    *           tour id which should be selected
    */
   private void selectTourInYearChart(final long tourIdToSelect) {

      if (_statValues_AllTours.isEmpty()) {

         _tourInfoToolTipProvider.setTourId(-1);

         return;
      }

      final int tourLength = _statValues_AllTours.size();
      final boolean[] selectedTours = new boolean[tourLength];
      boolean isTourSelected = false;

      for (int tourIndex = 0; tourIndex < tourLength; tourIndex++) {

         final TVICatalogComparedTour comparedItem = _statValues_AllTours.get(tourIndex);

         if (comparedItem.getTourId() == tourIdToSelect) {

            selectedTours[tourIndex] = true;
            isTourSelected = true;
         }
      }

      if (isTourSelected == false && selectedTours.length > 0) {

         // a tour is not selected, select first tour

// disable it can be confusing when the wrong tour is selected
//         selectedTours[0] = true;
      }

      _yearChart.setSelectedBars(selectedTours);
   }

   @Override
   public void setFocus() {

      _yearChart.setFocus();
   }

   /**
    * Get data for each displayed year
    */
   private void setYearData() {

      _allVisibleYears = new int[_numVisibleYears];
      _allNumberOfDaysInYear = new int[_numVisibleYears];

      final int firstYear = getFirstVisibleYear();

      int yearIndex = 0;
      for (int currentYear = firstYear; currentYear <= _lastVisibleYear; currentYear++) {

         _allVisibleYears[yearIndex] = currentYear;
         _allNumberOfDaysInYear[yearIndex] = TimeTools.getNumberOfDaysWithYear(currentYear);

         yearIndex++;
      }
   }

   /**
    * Show statistic for several years
    *
    * @param isShowLatestYear
    *           Shows the latest year and the years before
    */
   void updateUI_YearChart(final boolean isShowLatestYear) {

      if (_currentRefItem == null) {

         _pageBook.showPage(_pageNoChart);

         return;
      }

      _pageBook.showPage(_pageChart);

      /*
       * Reset statistic values
       */
      _statValues_AllTours.clear();
      _statValues_DOYValues.clear();

      _statValues_AvgAltimeter.clear();
      _statValues_AvgPulse.clear();
      _statValues_AvgSpeed.clear();
      _statValues_MaxPulse.clear();

      final int firstVisibleYear = getFirstVisibleYear();

      if (TourCompareManager.getReferenceTour_ViewLayout() == TourCompareManager.REF_TOUR_VIEW_LAYOUT_WITH_YEAR_CATEGORIES) {

         // compared tours are displayed with year categories

         createStatisticData_WithYearCategories(firstVisibleYear, isShowLatestYear);

      } else {

         // compared tours are displayed without year categories

         createStatisticData_WithoutYearCategories(firstVisibleYear, isShowLatestYear);
      }

      final ChartDataModel chartModel = new ChartDataModel(ChartType.BAR);

      final ChartDataXSerie xData = new ChartDataXSerie(ArrayListToArray.integerToDouble(_statValues_DOYValues));
      xData.setAxisUnit(ChartDataXSerie.X_AXIS_UNIT_DAY);
      xData.setChartSegments(createChartSegments());
      chartModel.setXData(xData);

      /**
       * Speed
       */
      if (Util.getStateBoolean(_state, STATE_SHOW_AVG_SPEED, true)) {

         // set the bar low/high data
         final ChartDataYSerie yDataSpeed = new ChartDataYSerie(
               ChartType.BAR,
               ArrayListToArray.toFloat(_statValues_AvgSpeed),
               true);

         final float[] minMaxValues = _currentRefItem.avgSpeed_MinMax;
         yDataSpeed.setSliderMinMaxValue(minMaxValues);
         adjustMinMaxValues(yDataSpeed, minMaxValues);

         TourManager.setBarColors(yDataSpeed, GraphColorManager.PREF_GRAPH_SPEED);
         TourManager.setGraphColors(yDataSpeed, GraphColorManager.PREF_GRAPH_SPEED);

         yDataSpeed.setYTitle(Messages.tourCatalog_view_label_year_chart_title);
         yDataSpeed.setUnitLabel(UI.UNIT_LABEL_SPEED);
         yDataSpeed.setShowYSlider(true);

         /*
          * ensure that painting of the bar is started at the bottom and not at the visible min
          * which is above the bottom !!!
          */
         yDataSpeed.setGraphFillMethod(ChartDataYSerie.BAR_DRAW_METHOD_BOTTOM);

         chartModel.addYData(yDataSpeed);
      }

      /**
       * Altimeter (VAM)
       */
      if (Util.getStateBoolean(_state, STATE_SHOW_AVG_ALTIMETER, true)) {

         // set the bar low/high data
         final ChartDataYSerie yDataAltimeter = new ChartDataYSerie(
               ChartType.BAR,
               ArrayListToArray.toFloat(_statValues_AvgAltimeter),
               true);

         final float[] minMaxValues = _currentRefItem.avgAltimeter_MinMax;
         yDataAltimeter.setSliderMinMaxValue(minMaxValues);
         adjustMinMaxValues(yDataAltimeter, minMaxValues);

         TourManager.setBarColors(yDataAltimeter, GraphColorManager.PREF_GRAPH_ALTIMETER);
         TourManager.setGraphColors(yDataAltimeter, GraphColorManager.PREF_GRAPH_ALTIMETER);

         yDataAltimeter.setYTitle(OtherMessages.GRAPH_LABEL_ALTIMETER);
         yDataAltimeter.setUnitLabel(UI.UNIT_LABEL_ALTIMETER);
         yDataAltimeter.setShowYSlider(true);

         /*
          * ensure that painting of the bar is started at the bottom and not at the visible min
          * which is above the bottom !!!
          */
         yDataAltimeter.setGraphFillMethod(ChartDataYSerie.BAR_DRAW_METHOD_BOTTOM);

         chartModel.addYData(yDataAltimeter);
      }

      /**
       * Avg Pulse
       */
      if (Util.getStateBoolean(_state, STATE_SHOW_AVG_PULSE, true)) {

         // set the bar low/high data
         final ChartDataYSerie yDataAvgPulse = new ChartDataYSerie(
               ChartType.BAR,
               ArrayListToArray.toFloat(_statValues_AvgPulse),
               true);

         final float[] minMaxValues = _currentRefItem.avgPulse_MinMax;
         yDataAvgPulse.setSliderMinMaxValue(minMaxValues);
         adjustMinMaxValues(yDataAvgPulse, minMaxValues);

         TourManager.setBarColors(yDataAvgPulse, GraphColorManager.PREF_GRAPH_HEARTBEAT);
         TourManager.setGraphColors(yDataAvgPulse, GraphColorManager.PREF_GRAPH_HEARTBEAT);

         yDataAvgPulse.setYTitle(OtherMessages.GRAPH_LABEL_HEARTBEAT);
         yDataAvgPulse.setUnitLabel(OtherMessages.GRAPH_LABEL_HEARTBEAT_UNIT);
         yDataAvgPulse.setShowYSlider(true);

         /*
          * ensure that painting of the bar is started at the bottom and not at the visible min
          * which is above the bottom !!!
          */
         yDataAvgPulse.setGraphFillMethod(ChartDataYSerie.BAR_DRAW_METHOD_BOTTOM);

         chartModel.addYData(yDataAvgPulse);
      }

      /**
       * Max Pulse
       */
      if (Util.getStateBoolean(_state, STATE_SHOW_MAX_PULSE, true)) {

         // set the bar low/high data
         final ChartDataYSerie yDataMaxPulse = new ChartDataYSerie(
               ChartType.BAR,
               ArrayListToArray.toFloat(_statValues_MaxPulse),
               true);

         final float[] minMaxValues = _currentRefItem.maxPulse_MinMax;
         yDataMaxPulse.setSliderMinMaxValue(minMaxValues);
         adjustMinMaxValues(yDataMaxPulse, minMaxValues);

         TourManager.setBarColors(yDataMaxPulse, GraphColorManager.PREF_GRAPH_HEARTBEAT);
         TourManager.setGraphColors(yDataMaxPulse, GraphColorManager.PREF_GRAPH_HEARTBEAT);

         yDataMaxPulse.setYTitle(OtherMessages.GRAPH_LABEL_HEARTBEAT_MAX);
         yDataMaxPulse.setUnitLabel(OtherMessages.GRAPH_LABEL_HEARTBEAT_UNIT);
         yDataMaxPulse.setShowYSlider(true);

         /*
          * Ensure that painting of the bar is started at the bottom and not at the visible min
          * which is above the bottom !!!
          */
         yDataMaxPulse.setGraphFillMethod(ChartDataYSerie.BAR_DRAW_METHOD_BOTTOM);

         chartModel.addYData(yDataMaxPulse);
      }

      /**
       * Setup UI
       */
      // set tool tip info
      chartModel.setCustomData(ChartDataModel.BAR_TOOLTIP_INFO_PROVIDER, new IChartInfoProvider() {

         @Override
         public void createToolTipUI(final IToolTipProvider toolTipProvider, final Composite parent, final int serieIndex, final int valueIndex) {

            RefTour_YearStatistic_View.this.createToolTipUI(toolTipProvider, parent, valueIndex);
         }
      });

      net.tourbook.ui.UI.updateChartProperties(_yearChart, PREF_PREFIX);

      final ChartTitleSegmentConfig ctsConfig = _yearChart.getChartTitleSegmentConfig();
      ctsConfig.isShowSegmentTitle = true;

      // show the data in the chart
      _yearChart.updateChart(chartModel, false, false);

      /*
       * Update start year combo box
       */
      _yearSelector.comboLastVisibleYear.removeAll();
      _allSelectableYears.clear();

      for (int year = firstVisibleYear - 1; year <= _lastVisibleYear + _numVisibleYears; year++) {

         _allSelectableYears.add(year);
         _yearSelector.comboLastVisibleYear.add(Integer.toString(year));
      }

      _yearSelector.comboLastVisibleYear.select(_numVisibleYears);

      _lblRefTourTitle.setText(_currentRefItem.label);

      // layout is needed otherwise the horizontal centered text is not displayed
      _lblRefTourTitle.getParent().layout(true, true);
   }
}
