/*******************************************************************************
 * Copyright (C) 2018, 2023 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.geoCompare;

import static org.eclipse.swt.events.ControlListener.controlResizedAdapter;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.SelectionChartInfo;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.CommonImages;
import net.tourbook.common.UI;
import net.tourbook.common.color.ThemeUtil;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.tooltip.ActionToolbarSlideoutAdv;
import net.tourbook.common.tooltip.AdvancedSlideout;
import net.tourbook.common.tooltip.IOpeningDialog;
import net.tourbook.common.tooltip.OpenDialogManager;
import net.tourbook.common.ui.SelectionCellLabelProvider;
import net.tourbook.common.util.ColumnDefinition;
import net.tourbook.common.util.ColumnManager;
import net.tourbook.common.util.ColumnProfile;
import net.tourbook.common.util.IContextMenuProvider;
import net.tourbook.common.util.ITourViewer;
import net.tourbook.common.util.PostSelectionProvider;
import net.tourbook.common.util.TableColumnDefinition;
import net.tourbook.common.util.Util;
import net.tourbook.data.NormalizedGeoData;
import net.tourbook.data.TourData;
import net.tourbook.data.TourReference;
import net.tourbook.data.TourType;
import net.tourbook.importdata.RawDataManager;
import net.tourbook.map2.view.SelectionMapSelection;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.SelectionDeletedTours;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.tourType.TourTypeImage;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.tourChart.TourChart;
import net.tourbook.ui.views.referenceTour.CompareConfig;
import net.tourbook.ui.views.referenceTour.ComparedTourChartView;
import net.tourbook.ui.views.referenceTour.ReferenceTourChartView;
import net.tourbook.ui.views.referenceTour.ReferenceTourManager;
import net.tourbook.ui.views.referenceTour.SelectionReferenceTourView;
import net.tourbook.ui.views.referenceTour.TVIElevationCompareResult_ComparedTour;
import net.tourbook.ui.views.referenceTour.TVIRefTour_ComparedTour;

import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class GeoCompareView extends ViewPart implements ITourViewer, IGeoCompareListener {

   public static final String            ID                                 = "net.tourbook.ui.views.geoCompare.GeoCompareView"; //$NON-NLS-1$

   private static final Color            COLOR_COMPARING_TOURS              = new Color(255, 87, 87);

   private static final int              DELAY_BEFORE_STARTING_COMPARE      = 100;
   private static final int              UI_UPDATE_INTERVAL                 = 500;

   static final String                   STATE_DISTANCE_INTERVAL            = "STATE_DISTANCE_INTERVAL";                         //$NON-NLS-1$
   static final String                   STATE_GEO_ACCURACY                 = "STATE_GEO_ACCURACY";                              //$NON-NLS-1$
   private static final String           STATE_GEO_FILTER_GEO_DIFFERENCE    = "STATE_GEO_FILTER_GEO_DIFFERENCE";                 //$NON-NLS-1$
   private static final String           STATE_GEO_FILTER_SEQUENCE_FILTER   = "STATE_GEO_FILTER_SEQUENCE_FILTER";                //$NON-NLS-1$
   private static final String           STATE_IS_GEO_FILTER_GEO_DIFFERENCE = "STATE_IS_GEO_FILTER_GEO_DIFFERENCE";              //$NON-NLS-1$
   private static final String           STATE_IS_GEO_FILTER_MAX_RESULTS    = "STATE_IS_GEO_FILTER_MAX_RESULTS";                 //$NON-NLS-1$
   private static final String           STATE_IS_USE_APP_FILTER            = "STATE_IS_USE_APP_FILTER";                         //$NON-NLS-1$

   private static final String           STATE_SORT_COLUMN_DIRECTION        = "STATE_SORT_COLUMN_DIRECTION";                     //$NON-NLS-1$
   private static final String           STATE_SORT_COLUMN_ID               = "STATE_SORT_COLUMN_ID";                            //$NON-NLS-1$

   static final int                      DEFAULT_DISTANCE_INTERVAL          = 100;
   static final int                      DEFAULT_GEO_ACCURACY               = 10_000;

   private static final String           COLUMN_AVG_PACE                    = "avgPace";                                         //$NON-NLS-1$
   private static final String           COLUMN_AVG_PULSE                   = "avgPulse";                                        //$NON-NLS-1$
   private static final String           COLUMN_AVG_SPEED                   = "avgSpeed";                                        //$NON-NLS-1$
   private static final String           COLUMN_GEO_DIFF                    = "geoDiff";                                         //$NON-NLS-1$
   private static final String           COLUMN_GEO_DIFF_RELATIVE           = "geoDiffRelative";                                 //$NON-NLS-1$
   private static final String           COLUMN_SEQUENCE                    = "sequence";                                        //$NON-NLS-1$
   private static final String           COLUMN_TOUR_START_DATE             = "tourStartDate";                                   //$NON-NLS-1$
   private static final String           COLUMN_TOUR_TITLE                  = "tourTitle";                                       //$NON-NLS-1$

   private static final IDialogSettings  _state                             = TourbookPlugin.getState(ID);
   private static final IPreferenceStore _prefStore                         = TourbookPlugin.getPrefStore();
   private static final IPreferenceStore _prefStore_Common                  = CommonActivator.getPrefStore();

   static {

      /**
       * This may also fix https://github.com/wolfgang-ch/mytourbook/issues/265 issue but trying
       * first with an comparator fix.
       * <code>
       *
       * java.lang.IllegalArgumentException: Comparison method violates its general contract!
       *
       * Workaround for comparator violation:
       * Set system property -Djava.util.Arrays.useLegacyMergeSort=true
       * this: net.tourbook.ui.views.geoCompare.GeoCompareView$CompareResultComparator
       * comparator: null
       * array:
       * GeoPartComparerItem [tourId=20186282328431, geoPartItem=GeoPartItem [executorId=9, ]]
       * GeoPartComparerItem [tourId=20188197129091, geoPartItem=GeoPartItem [executorId=9, ]]
       * GeoPartComparerItem [tourId=20201261206965, geoPartItem=GeoPartItem [executorId=9, ]]
       * GeoPartComparerItem [tourId=20201269477015, geoPartItem=GeoPartItem [executorId=9, ]]
       *
       * </code>
       */

//      System.setProperty("java.util.Arrays.useLegacyMergeSort", "true"); //$NON-NLS-1$
   }

   private static final String       FORMAT_START_END = "%d…%d";                         //$NON-NLS-1$

   private static final NumberFormat _nf0             = NumberFormat.getNumberInstance();
   private static final NumberFormat _nf1             = NumberFormat.getNumberInstance();

   static {

      _nf0.setMinimumFractionDigits(0);
      _nf0.setMaximumFractionDigits(0);
      _nf1.setMinimumFractionDigits(1);
      _nf1.setMaximumFractionDigits(1);
   }

   private SelectionListener       _columnSortListener;
   private SelectionListener       _compareSelectionListener;
   private MouseWheelListener      _compareMouseWheelListener;
   private IPartListener2          _partListener;
   private IPropertyChangeListener _prefChangeListener;
   private IPropertyChangeListener _prefChangeListener_Common;
   private ISelectionListener      _postSelectionListener;
   private ITourEventListener      _tourEventListener;

   private PostSelectionProvider   _postSelectionProvider;

// private int                       _lastSelectionHash;

   private AtomicInteger                   _workedTours                    = new AtomicInteger();
   private AtomicInteger                   _runningId                      = new AtomicInteger();

   private long                            _workerExecutorId;

   private boolean                         _isInUpdate;
   private long                            _lastUIUpdate;
   private boolean                         _isInSelection;
   private boolean                         _isComparedTourPinned;

   /**
    * Items which are displayed in the tour viewer
    */
   private List<GeoComparedTour>           _allGeoComparedTours            = new ArrayList<>();

   /**
    * All tours which are sorted in the geo compare viewer
    */
   private GeoComparedTour[]               _allSortedAndFiltered_GeoComparedTours;

   private GeoComparedTour                 _selectedGeoComparedTour;

   private int                             _compareData_FirstIndex;
   private int                             _compareData_LastIndex;
   private int[]                           _compareData_GeoGrid;
   private boolean                         _compareData_IsUseAppFilter;
   private int                             _compareData_NumGeoPartTours;
   private GeoCompareData                  _compareData_CurrentGeoCompareData;
   private long                            _compareData_RefId;
   private TourData                        _compareData_TourData;
   private long                            _compareData_TourId             = -1;
   private String                          _compareData_TourTitle;

   private int                             _compareData_DistanceInterval;
   private int                             _compareData_GeoAccuracy;

   private long                            _maxMinDiff;

   private int                             _lastCompare_DistanceInterval;
   private int                             _lastCompare_GeoAccuracy;
   private boolean                         _lastCompare_IsUseAppFilter;

   private GeoCompareViewer                _geoCompareViewer;
   private GeoCompareComparator            _geoCompareComparator           = new GeoCompareComparator();
   private IContextMenuProvider            _tableViewerContextMenuProvider = new TableContextMenuProvider();
   private ColumnManager                   _columnManager;
   private MenuManager                     _viewerMenuManager;

   private TableColumnDefinition           _colDef_TourTypeImage;
   private int                             _columnIndex_TourTypeImage      = -1;
   private int                             _columnWidth_TourTypeImage;

   private boolean                         _isGeoFilter_GeoDifference;
   private boolean                         _isGeoFilter_MaxResults;
   private int                             _geoFilter_GeoDifference;
   private int                             _geoFilter_MaxResults;

   private OpenDialogManager               _openDlgMgr                     = new OpenDialogManager();
   private SlideoutGeoCompareOptions       _slideoutGeoCompareOptions;
   private GeoCompareState                 _slideoutGeoCompareState        = new GeoCompareState();

   private PixelConverter                  _pc;

   private ActionAppTourFilter             _actionAppTourFilter;
   private ActionGeoCompareOptions         _actionGeoCompareOptions;
   private ActionHideToursBelow            _actionHideToursBelow;
   private ActionOnOff                     _actionOnOff;
   private ActionPinTourWhichIsCompared    _actionPinTourWhichIsCompared;
   private ActionSelectTourWhichIsCompared _actionSelectTourWhichIsCompared;

   /*
    * UI controls
    */
   private Display   _display;

   private Composite _parent;
   private Composite _viewerContainer;

   private PageBook  _pageBook;
   private Composite _pageCompareResult;
   private Composite _pageMultipleTours;
   private Composite _pageSelectTourWithGeoData;

   private Button    _chkGeoFilter_GeoDiff;
   private Button    _chkGeoFilter_MaxResults;

   private Label     _lblCompareStatus_Icon;
   private Label     _lblCompareStatus_Message;
   private Label     _lblGeoFilter_GeoDifference_Unit;
   private Label     _lblGeoFilter_MaxResults_Unit;
   private Label     _lblNumGeoGrids;
   private Label     _lblNumGeoGrids_Value;
   private Label     _lblNumSlices;
   private Label     _lblNumSlices_Value;
   private Label     _lblNumTours;
   private Label     _lblNumTours_Value;
   private Label     _lblStartEndIndex;
   private Label     _lblStartEndIndex_Value;
   private Label     _lblTitle;

   private Spinner   _spinnerGeoFilter_GeoDifference;
   private Spinner   _spinnerGeoFilter_MaxResults;

   private Menu      _tableContextMenu;

   private Image     _imageCompareType_GeoCompare  = TourbookPlugin.getImageDescriptor(Images.TourCompare_GeoCompare_RefTour).createImage();
   private Image     _imageCompareType_RefTour     = TourbookPlugin.getImageDescriptor(Images.RefTour).createImage();
   private Image     _imageCompareType_PlaceHolder = TourbookPlugin.getImageDescriptor(Images.App_EmptyIcon_Placeholder).createImage();
   private Image     _imageOptions_Enabled         = CommonActivator.getThemedImageDescriptor(CommonImages.TourOptions).createImage();
   private Image     _imageOptions_Disabled        = CommonActivator.getThemedImageDescriptor(CommonImages.TourOptions_Disabled).createImage();

   private CLabel    _iconCompareType;

   private class ActionAppTourFilter extends Action {

      public ActionAppTourFilter() {

         super(null, AS_CHECK_BOX);

         setToolTipText(Messages.GeoCompare_View_Action_AppFilter_Tooltip);

         setImageDescriptor(CommonActivator.getThemedImageDescriptor(CommonImages.App_Filter));
         setDisabledImageDescriptor(CommonActivator.getThemedImageDescriptor(CommonImages.App_Filter_Disabled));
      }

      @Override
      public void run() {
         onAction_AppFilter(isChecked());
      }
   }

   private class ActionGeoCompareOptions extends ActionToolbarSlideoutAdv {

      public ActionGeoCompareOptions() {

         /*
          * !!! Needed to create images, otherwise they are disposed sometimes and the action
          * is not displayed in the toolbar, very strange, in other views it works without creating
          * images !!!
          */

         super(_imageOptions_Enabled, _imageOptions_Disabled);
      }

      @Override
      protected AdvancedSlideout createSlideout(final ToolItem toolItem) {

         _slideoutGeoCompareOptions = new SlideoutGeoCompareOptions(toolItem, _state, GeoCompareView.this);

         return _slideoutGeoCompareOptions;
      }
   }

   /**
    * Action: Hide tours below
    */
   private class ActionHideToursBelow extends Action {

      public ActionHideToursBelow() {

         super(Messages.GeoCompare_View_Action_HideToursBelow, AS_PUSH_BUTTON);

         setToolTipText(Messages.GeoCompare_View_Action_HideToursBelow_Tooltip);
      }

      @Override
      public void run() {
         onAction_HideToursBelow();
      }
   }

   private class ActionOnOff extends Action {

      private ImageDescriptor _imageDescriptor_AppOn  = CommonActivator.getThemedImageDescriptor(CommonImages.App_Turn_On);
      private ImageDescriptor _imageDescriptor_AppOff = CommonActivator.getThemedImageDescriptor(CommonImages.App_Turn_Off);

      public ActionOnOff() {

         super(null, AS_CHECK_BOX);

         setToolTipText(Messages.GeoCompare_View_Action_OnOff_Tooltip);

         setImageDescriptor(_imageDescriptor_AppOn);
      }

      @Override
      public void run() {
         onAction_OnOff(isChecked());
      }

      private void setIcon(final boolean isSelected) {

         // switch icon
         if (isSelected) {
            setImageDescriptor(_imageDescriptor_AppOn);
         } else {
            setImageDescriptor(_imageDescriptor_AppOff);
         }
      }
   }

   /**
    * Action: Pin tour which is compared
    */
   private class ActionPinTourWhichIsCompared extends Action {

      public ActionPinTourWhichIsCompared() {

         super(UI.EMPTY_STRING, AS_CHECK_BOX);

         setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.GeoCompare_PinComparedTour));
         setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.GeoCompare_PinComparedTour_Disabled));

         setToolTipText(Messages.GeoCompare_View_Action_PinTourWhichIsCompared_Tooltip);
      }

      @Override
      public void run() {
         onAction_PinTourWhichIsCompared(isChecked());
      }
   }

   /**
    * Action: Select tour which is compared
    */
   private class ActionSelectTourWhichIsCompared extends Action {

      public ActionSelectTourWhichIsCompared() {

         super(UI.EMPTY_STRING, AS_PUSH_BUTTON);

         setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.GeoCompare_SelectComparedTour));
         setDisabledImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.GeoCompare_SelectComparedTour_Disabled));

         setToolTipText(Messages.GeoCompare_View_Action_SelectTourWhichIsCompared_Tooltip);
      }

      @Override
      public void run() {
         onAction_SelectTourWhichIsCompared();
      }
   }

   private class CompareResultProvider implements IStructuredContentProvider {

      @Override
      public void dispose() {}

      @Override
      public Object[] getElements(final Object inputElement) {
         return _allGeoComparedTours.toArray();
      }

      @Override
      public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
   }

   private class GeoCompareComparator extends ViewerComparator {

      private static final int ASCENDING       = 0;
      private static final int DESCENDING      = 1;

      private String           __sortColumnId  = COLUMN_GEO_DIFF;
      private int              __sortDirection = ASCENDING;

      @Override
      public int compare(final Viewer viewer, final Object o1, final Object o2) {

         final GeoComparedTour geoTour1 = (GeoComparedTour) o1;
         final GeoComparedTour geoTour2 = (GeoComparedTour) o2;

         double rc = 0;

         // Determine which column and do the appropriate sort
         switch (__sortColumnId) {

         case COLUMN_GEO_DIFF:
         case COLUMN_GEO_DIFF_RELATIVE:

            final long minDiffValue1 = geoTour1.minDiffValue;
            final long minDiffValue2 = geoTour2.minDiffValue;

            if (minDiffValue1 == minDiffValue2) {

               // prevent java.lang.IllegalArgumentException: Comparison method violates its general contract!

               rc = 0;

            } else if (minDiffValue1 >= 0 && minDiffValue2 >= 0) {

               rc = minDiffValue1 - minDiffValue2;

            } else if (minDiffValue1 >= 0) {

               rc = -Integer.MAX_VALUE;

            } else if (minDiffValue2 >= 0) {

               rc = Integer.MAX_VALUE;

            } else {

               rc = minDiffValue1 - minDiffValue2;
            }

            break;

         case COLUMN_TOUR_START_DATE:

            // sorting by date is computed below

            break;

         case COLUMN_AVG_PULSE:
            rc = geoTour1.avgPulse - geoTour2.avgPulse;
            break;

         case COLUMN_AVG_PACE:
            rc = geoTour1.avgPace - geoTour2.avgPace;
            break;

         case COLUMN_AVG_SPEED:
            rc = geoTour1.avgSpeed - geoTour2.avgSpeed;
            break;

         case COLUMN_TOUR_TITLE:
            rc = geoTour1.tourTitle.compareTo(geoTour2.tourTitle);
            break;

         case TableColumnFactory.ALTITUDE_ELEVATION_TOTAL_GAIN_ID:
            rc = geoTour1.elevationGain - geoTour2.elevationGain;
            break;

         case TableColumnFactory.ALTITUDE_ELEVATION_TOTAL_LOSS_ID:
            rc = geoTour1.elevationLoss - geoTour2.elevationLoss;
            break;

         case TableColumnFactory.MOTION_ALTIMETER_ID:
            rc = geoTour1.avgAltimeter - geoTour2.avgAltimeter;
            break;

         case TableColumnFactory.MOTION_DISTANCE_ID:
            rc = geoTour1.distance - geoTour2.distance;
            break;

         case TableColumnFactory.TIME__COMPUTED_MOVING_TIME_ID:
            rc = geoTour1.movingTime - geoTour2.movingTime;
            break;

         case TableColumnFactory.TIME__DEVICE_RECORDED_TIME_ID:
            rc = geoTour1.recordedTime - geoTour2.recordedTime;
            break;

         case TableColumnFactory.TIME__DEVICE_ELAPSED_TIME_ID:
            rc = geoTour1.elapsedTime - geoTour2.elapsedTime;
            break;

         }

         if (rc == 0) {
            rc = geoTour1.tourStartTimeMS - geoTour2.tourStartTimeMS;
         }

         // if descending order, flip the direction
         if (__sortDirection == DESCENDING) {
            rc = -rc;
         }

         /*
          * MUST return 1 or -1 otherwise long values are not sorted correctly.
          */
         return rc > 0
               ? 1
               : rc < 0
                     ? -1
                     : 0;
      }

      @Override
      public boolean isSorterProperty(final Object element, final String property) {

         // force resorting when a name is renamed
         return true;
      }

      public void setSortColumn(final Widget widget) {

         final ColumnDefinition columnDefinition = (ColumnDefinition) widget.getData();
         final String columnId = columnDefinition.getColumnId();

         if (columnId.equals(__sortColumnId)) {

            // Same column as last sort: Toggle the direction

            if (__sortDirection == DESCENDING) {

               // reset sorting to default

               __sortColumnId = COLUMN_GEO_DIFF;
               __sortDirection = ASCENDING;

            } else {

               __sortDirection = 1 - __sortDirection;
            }

         } else {

            // New column: Do an ascent sorting

            __sortColumnId = columnId;
            __sortDirection = ASCENDING;
         }

         updateUI_SetSortDirection(__sortColumnId, __sortDirection);
      }
   }

   public class GeoCompareViewer extends TableViewer {

      public GeoCompareViewer(final Table table) {

         super(table);
      }

      @Override
      protected Object[] getSortedChildren(final Object parent) {

         // keep sorted tours
         final Object[] allSortedAndFiltered_GeoComparedTours = super.getSortedChildren(parent);

         // cast array
         _allSortedAndFiltered_GeoComparedTours = Arrays.copyOf(

               allSortedAndFiltered_GeoComparedTours,
               allSortedAndFiltered_GeoComparedTours.length,

               // this is the trick to cast an Object[]
               GeoComparedTour[].class);

         final int numFilterVisible = _allSortedAndFiltered_GeoComparedTours.length;

         _lblCompareStatus_Message.setText(String.format(
               Messages.GeoCompare_View_State_TourFilter,
               numFilterVisible,
               _slideoutGeoCompareState.numTours - numFilterVisible));

         _lblCompareStatus_Icon.setText(UI.SPACE4);
         _lblCompareStatus_Icon.setBackground(UI.IS_DARK_THEME
               ? ThemeUtil.getDefaultBackgroundColor_Table()
               : ThemeUtil.getDefaultBackgroundColor_Shell());

         return allSortedAndFiltered_GeoComparedTours;
      }

   }

   private enum InvalidData {

      NoGeoData, MultipleTours
   }

   public class TableContextMenuProvider implements IContextMenuProvider {

      @Override
      public void disposeContextMenu() {

         if (_tableContextMenu != null) {
            _tableContextMenu.dispose();
         }
      }

      @Override
      public Menu getContextMenu() {
         return _tableContextMenu;
      }

      @Override
      public Menu recreateContextMenu() {

         disposeContextMenu();

         _tableContextMenu = createUI_92_CreateViewerContextMenu();

         return _tableContextMenu;
      }

   }

   private void addPartListener() {

      _partListener = new IPartListener2() {

         @Override
         public void partActivated(final IWorkbenchPartReference partRef) {}

         @Override
         public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

         @Override
         public void partClosed(final IWorkbenchPartReference partRef) {

            if (partRef.getPart(false) == GeoCompareView.this) {
               setState_StopComparing();
            }
         }

         @Override
         public void partDeactivated(final IWorkbenchPartReference partRef) {}

         @Override
         public void partHidden(final IWorkbenchPartReference partRef) {}

         @Override
         public void partInputChanged(final IWorkbenchPartReference partRef) {}

         @Override
         public void partOpened(final IWorkbenchPartReference partRef) {}

         @Override
         public void partVisible(final IWorkbenchPartReference partRef) {}
      };

      getViewSite().getPage().addPartListener(_partListener);
   }

   private void addPrefListener() {

      _prefChangeListener = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ITourbookPreferences.APP_DATA_FILTER_IS_MODIFIED)) {

            recompareTours();

         } else if (property.equals(ITourbookPreferences.GRAPH_COLORS_HAS_CHANGED)) {

            // map options can have be changed
            _slideoutGeoCompareOptions.restoreState();
         }
      };

      _prefChangeListener_Common = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ICommonPreferences.MEASUREMENT_SYSTEM)) {

            // measurement system has changed

            _columnManager.saveState(_state);
            _columnManager.clearColumns();
            defineAllColumns();

            _geoCompareViewer = (GeoCompareViewer) recreateViewer(_geoCompareViewer);
         }
      };

      // register the listener
      _prefStore.addPropertyChangeListener(_prefChangeListener);
      _prefStore_Common.addPropertyChangeListener(_prefChangeListener_Common);
   }

   /**
    * Listen for events when a tour is selected
    */
   private void addSelectionListener() {

      _postSelectionListener = (part, selection) -> {

         if (isIgnorePart(part, selection)) {
            return;
         }

         onSelectionChanged(selection);
      };

      getSite().getPage().addPostSelectionListener(_postSelectionListener);
   }

   private void addTourEventListener() {

      _tourEventListener = (part, eventId, eventData) -> {

         if (isIgnorePart(part, null)) {
            return;
         }

         if ((eventId == TourEventId.TOUR_SELECTION) && eventData instanceof ISelection) {

            onSelectionChanged((ISelection) eventData);

         } else if (eventId == TourEventId.SLIDER_POSITION_CHANGED && eventData instanceof ISelection) {

            onSelectionChanged((ISelection) eventData);

         } else if (eventId == TourEventId.MAP_SELECTION && eventData instanceof SelectionMapSelection) {

            onSelection_MapSelection((SelectionMapSelection) eventData);
         }
      };

      TourManager.getInstance().addTourEventListener(_tourEventListener);
   }

   private void clearView(final InvalidData invalidData) {

      setState_StopComparing();

      switch (invalidData) {

      case MultipleTours:
         _pageBook.showPage(_pageMultipleTours);
         break;

      case NoGeoData:
      default:
         showInvalidPage();
         break;
      }
   }

   /**
    * Close all opened dialogs except the opening dialog.
    *
    * @param openingDialog
    */
   public void closeOpenedDialogs(final IOpeningDialog openingDialog) {
      _openDlgMgr.closeOpenedDialogs(openingDialog);
   }

   /**
    * @param tourData
    * @param leftIndex
    * @param rightIndex
    * @param refId
    *           Reference tour id or <code>-1</code> when not available
    */
   private void compare_10_Compare(final TourData tourData,
                                   final int leftIndex,
                                   final int rightIndex,
                                   final long refId) {

      if (GeoCompareManager.isGeoComparingOn() == false) {

         // geo comparing is OFF

         return;
      }

      /*
       * !!! This must be very early because the comparison is running with a delay which could
       * cause that an old is running !!!
       */
      final int runnableRunningId = _runningId.incrementAndGet();

      /*
       * Show no data page
       */
      final double[] latSerie = tourData.latitudeSerie;
      if (latSerie == null) {

         clearView(InvalidData.NoGeoData);

         return;
      }

      /*
       * Validate first/last indices
       */
      int firstIndex = leftIndex < rightIndex ? leftIndex : rightIndex;
      int lastIndex = leftIndex > rightIndex ? leftIndex : rightIndex;
      if (firstIndex < 0) {
         firstIndex = 0;
      }
      if (lastIndex > latSerie.length) {
         lastIndex = latSerie.length;
      }

      final long tourId = tourData.getTourId();

      // skip same data and continue current comparison
      if (_compareData_TourId == tourId
            && _compareData_FirstIndex == leftIndex
            && _compareData_LastIndex == rightIndex

            && _lastCompare_GeoAccuracy == _compareData_GeoAccuracy
            && _lastCompare_DistanceInterval == _compareData_DistanceInterval
            && _lastCompare_IsUseAppFilter == _compareData_IsUseAppFilter) {

         /*
          * Ensure that the compare result is displayed otherwise it would be hidden when a
          * none geo tour was seleced before
          */
         _pageBook.showPage(_pageCompareResult);

         return;
      }

      /*
       * Keep index AFTER checking otherwise wrong values are used in the runnable !!!
       */
      final int compareFirstIndex = firstIndex;
      final int compareLastIndex = lastIndex;

      /*
       * New data should be compared
       */
      GeoPartTourLoader.stopLoading(_compareData_CurrentGeoCompareData);

      updateUI_GeoCompareData(null);

      // delay tour comparator, moving the slider can occur very often
      _display.timerExec(DELAY_BEFORE_STARTING_COMPARE, new Runnable() {

         private int __runningId = runnableRunningId;

         @Override
         public void run() {

            if (_parent.isDisposed()) {
               return;
            }

            final int currentId = _runningId.get();

            if (__runningId != currentId) {

               // a newer runnable is created

               return;
            }

            _compareData_TourId = tourId;
            _compareData_TourData = tourData;
            _compareData_RefId = refId;
            _compareData_FirstIndex = compareFirstIndex;
            _compareData_LastIndex = compareLastIndex;

            compare_20_SetupComparing();
         }
      });
   }

   private void compare_20_SetupComparing() {

      // 1. get geo grid from lat/lon first/last index
      _compareData_GeoGrid = _compareData_TourData.computeGeo_Grid(
            _compareData_FirstIndex,
            _compareData_LastIndex);

      if (_compareData_GeoGrid == null) {

         showInvalidPage();

         return;
      }

      _pageBook.showPage(_pageCompareResult);

      /*
       * Update UI
       */
      _allGeoComparedTours.clear();

      // reset max diff
      _maxMinDiff = -1;

      updateUI_Viewer();

      updateUI_State_Progress(-1, -1);

      _compareData_TourTitle = TourManager.getTourTitleDetailed(_compareData_TourData);

      _iconCompareType.setImage(_imageCompareType_PlaceHolder);

      // update UI before starting the comparison
      _display.asyncExec(() -> compare_30_StartComparing());
   }

   /**
    * Start comparing with data from geo compare fields
    */
   private void compare_30_StartComparing() {

      /*
       * Create geo data which should be compared
       */
      final NormalizedGeoData normalizedGeoData = _compareData_TourData.computeGeo_NormalizeLatLon(
            _compareData_FirstIndex,
            _compareData_LastIndex,
            _compareData_GeoAccuracy,
            _compareData_DistanceInterval);

      // load tour id's in the geo parts
      final GeoCompareData newGeoCompareData = GeoPartTourLoader.loadToursFromGeoParts(
            _compareData_TourId,
            _compareData_TourTitle,
            _compareData_GeoGrid,
            normalizedGeoData,
            _compareData_IsUseAppFilter,
            _compareData_CurrentGeoCompareData,
            this);

      newGeoCompareData.refId = _compareData_RefId;

      _compareData_CurrentGeoCompareData = newGeoCompareData;

      /*
       * Set slideout info
       */
      _slideoutGeoCompareState = new GeoCompareState();

      _slideoutGeoCompareState.firstIndex = _compareData_FirstIndex;
      _slideoutGeoCompareState.lastIndex = _compareData_LastIndex;
      _slideoutGeoCompareState.numGeoGrids = _compareData_GeoGrid.length;
      _slideoutGeoCompareState.normalizedDistance = normalizedGeoData.normalizedDistance;

      _slideoutGeoCompareOptions.updateUI_StateValues(_slideoutGeoCompareState);
      updateUI_StateValues();

   }

   void compare_40_CompareTours(final GeoCompareData geoCompareData) {

      _compareData_NumGeoPartTours = geoCompareData.tourIds.length;

      if (_compareData_NumGeoPartTours == 0) {

         // update UI
         _display.asyncExec(() -> {

            if (_parent.isDisposed()) {
               return;
            }

            // this can happen when the tour filter is active and no tours are found -> show empty result

            _allGeoComparedTours.clear();

            updateUI_State_Progress(0, 0);
            updateUI_Viewer();

            updateUI_GeoCompareData(geoCompareData);
         });

         return;
      }

      final long[] workerExecutorId = { 0 };

      _workedTours.set(0);

      _workerExecutorId = geoCompareData.executorId;
      workerExecutorId[0] = _workerExecutorId;

      GeoCompareManager.compareGeoTours(geoCompareData, this);

      // update UI
      _display.asyncExec(() -> {

         if (_parent.isDisposed()) {
            return;
         }

         if (workerExecutorId[0] != _workerExecutorId) {

            // skip old tasks

            return;
         }

         updateUI_GeoCompareData(geoCompareData);
      });

   }

   void compare_50_OneTourIsCompared(final GeoCompareData geoCompareData) {

      if (geoCompareData == null
            || geoCompareData.isCanceled
            || geoCompareData.executorId != _workerExecutorId) {

         return;
      }

      final long now = System.currentTimeMillis();
      final int numWorkedTours = _workedTours.incrementAndGet();

      // update UI not too often
      if (now - _lastUIUpdate < UI_UPDATE_INTERVAL && numWorkedTours != _compareData_NumGeoPartTours) {

         return;
      }

      // reset update time
      _lastUIUpdate = now;

      // use a copy of the currently updated data
      _allGeoComparedTours = new ArrayList<>(geoCompareData.allGeoComparedTours);

      // get previous selected item
      final GeoComparedTour[] reselectedItem = { null };
      if (geoCompareData.isReselectedInUI == false) {

         geoCompareData.isReselectedInUI = true;

         if (_selectedGeoComparedTour != null) {

            for (final GeoComparedTour reselectComparerItem : _allGeoComparedTours) {

               if (reselectComparerItem.tourId == _selectedGeoComparedTour.tourId) {
                  reselectedItem[0] = reselectComparerItem;
               }
            }
         }
      }

      _display.asyncExec(() -> {

         if (_parent.isDisposed()) {
            return;
         }

         updateUI_State_Progress(numWorkedTours, _compareData_NumGeoPartTours);

         if (numWorkedTours == _compareData_NumGeoPartTours) {

            // all is compared

            compare_60_AllToursAreCompared(geoCompareData);

            // fire geo compare data
            GeoCompareManager.fireEvent(
                  GeoCompareEventId.TOUR_IS_GEO_COMPARED,
                  geoCompareData,
                  GeoCompareView.this);

            updateUI_Viewer();

            // reselect previous selection
            if (reselectedItem[0] != null) {

               _geoCompareViewer.setSelection(new StructuredSelection(reselectedItem), true);
               _geoCompareViewer.getTable().showSelection();

//					// focus can have changed when resorted, set focus to the selected item
//					int selectedIndex = 0;
//					final Table table = _profileViewer.getTable();
//					final TableItem[] items = table.getItems();
//					for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
//
//						final TableItem tableItem = items[itemIndex];
//
//						if (tableItem.getData() == selectedProfile) {
//							selectedIndex = itemIndex;
//						}
//					}
//					table.setSelection(selectedIndex);
//					table.showSelection();
            }
         }
      });
   }

   private void compare_60_AllToursAreCompared(final GeoCompareData geoCompareData) {

// SET_FORMATTING_OFF

      /*
       * Keep state after compare is done
       */
      _lastCompare_DistanceInterval          = _compareData_DistanceInterval;
      _lastCompare_GeoAccuracy               = _compareData_GeoAccuracy;
      _lastCompare_IsUseAppFilter            = _compareData_IsUseAppFilter;

// SET_FORMATTING_ON

      /*
       * Get max of the minDiff value
       */
      _maxMinDiff = 0;
      for (final GeoComparedTour comparerItem : geoCompareData.allGeoComparedTours) {

         if (comparerItem.minDiffValue > _maxMinDiff) {
            _maxMinDiff = comparerItem.minDiffValue;
         }
      }

      filterGeoCompareItems(geoCompareData);

      // make sure the selection is visible
      _geoCompareViewer.getTable().showSelection();
   }

   public void compareRefTour(final long refId) {

      final CompareConfig tourCompareConfig = ReferenceTourManager.getTourCompareConfig(refId);

      if (tourCompareConfig == null) {
         return;
      }

      final TourData tourData = tourCompareConfig.getRefTourData();

      if (tourData != null) {

         final TourReference refTour = tourCompareConfig.getRefTour();

         /*
          * Convert real ref tour into a geo compare ref tour that the behaviour is the same,
          * however this will disable features in the tour compare chart but this is already very
          * complex.
          */

         final long geoCompareRefId = ReferenceTourManager.createGeoCompareRefTour_FromNative(refTour);

         compare_10_Compare(
               tourData,
               refTour.getStartValueIndex(),
               refTour.getEndValueIndex(),
               geoCompareRefId);
      }
   }

   private void createActions() {

// SET_FORMATTING_OFF

      _actionAppTourFilter             = new ActionAppTourFilter();
      _actionGeoCompareOptions         = new ActionGeoCompareOptions();
      _actionHideToursBelow            = new ActionHideToursBelow();
      _actionOnOff                     = new ActionOnOff();
      _actionPinTourWhichIsCompared    = new ActionPinTourWhichIsCompared();
      _actionSelectTourWhichIsCompared = new ActionSelectTourWhichIsCompared();

// SET_FORMATTING_ON
   }

   @Override
   public void createPartControl(final Composite parent) {

      initUI(parent);
      restoreState_BeforeUI();

      // define all columns for the viewer
      _columnManager = new ColumnManager(this, _state);
      defineAllColumns();

      createActions();
      createUI(parent);

      fillToolbar();

      addPartListener();
      addPrefListener();
      addTourEventListener();
      addSelectionListener();
      GeoCompareManager.addGeoCompareEventListener(this);

      // set selection provider
      getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider(ID));

      restoreState();
      restoreSelection();
   }

   private void createUI(final Composite parent) {

      _pageBook = new PageBook(parent, SWT.NONE);

// SET_FORMATTING_OFF

      _pageMultipleTours         = UI.createUI_PageNoData(_pageBook, Messages.GeoCompare_View_PageText_MultipleToursNotSupported);
      _pageSelectTourWithGeoData = UI.createUI_PageNoData(_pageBook, Messages.GeoCompare_View_PageText_SelectTourWithGeoData);

// SET_FORMATTING_ON

      _pageCompareResult = new Composite(_pageBook, SWT.NONE);
      GridLayoutFactory.fillDefaults().applyTo(_pageCompareResult);
      {
         final Composite container = new Composite(_pageCompareResult, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
         GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(container);
         {
            createUI_10_CompareData(container);

            _viewerContainer = new Composite(container, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(_viewerContainer);
            GridLayoutFactory.fillDefaults().applyTo(_viewerContainer);
            {
               createUI_80_TableViewer(_viewerContainer);
            }
         }
      }
   }

   private void createUI_10_CompareData(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.swtDefaults().numColumns(2).spacing(40, 5).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_YELLOW);
      {
         createUI_20_Title(container);

         createUI_30_Col1_GeoFilter(container);
         createUI_32_Col2_Info(container);
      }
   }

   private void createUI_20_Title(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults()
//            .grab(true, false)
            .span(2, 1)
            .applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_GREEN);
      {
         {
            /*
             * Image: Compare type
             */
            _iconCompareType = new CLabel(container, SWT.NONE);
            _iconCompareType.setImage(_imageCompareType_PlaceHolder);
//            _iconCompareType.setBackground(UI.SYS_COLOR_BLUE);
            GridDataFactory.fillDefaults()

                  // adjust to lower checkbox
                  .indent(-3, 0)
                  .applyTo(_iconCompareType);

         }
         {
            /*
             * Title
             */
            _lblTitle = new Label(container, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, false)
                  .align(SWT.FILL, SWT.CENTER)
                  .applyTo(_lblTitle);
//            _lblTitle.setBackground(UI.SYS_COLOR_MAGENTA);
         }
         {
            /*
             * Action: Select tour which is compared
             */
            final ToolBar toolbar = new ToolBar(container, SWT.FLAT);

            final ToolBarManager tbm = new ToolBarManager(toolbar);

            tbm.add(_actionSelectTourWhichIsCompared);
            tbm.add(_actionPinTourWhichIsCompared);

            tbm.update(true);
         }
      }
   }

   private void createUI_30_Col1_GeoFilter(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults()
//            .grab(true, true)
//            .align(SWT.FILL, SWT.BEGINNING)
            .applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_CYAN);
      {

         {
            /*
             * Checkbox: Relative geographic differences filter
             */
            {
               // Checkbox
               _chkGeoFilter_GeoDiff = new Button(container, SWT.CHECK);
               _chkGeoFilter_GeoDiff.setText(Messages.GeoCompare_View_Checkbox_GeoDifferenceFilter);
               _chkGeoFilter_GeoDiff.setToolTipText(Messages.GeoCompare_View_Checkbox_GeoDifferenceFilter_Tooltip);
               _chkGeoFilter_GeoDiff.addSelectionListener(_compareSelectionListener);
            }
            {
               // Spinner
               _spinnerGeoFilter_GeoDifference = new Spinner(container, SWT.BORDER);
               _spinnerGeoFilter_GeoDifference.setMinimum(0);
               _spinnerGeoFilter_GeoDifference.setMaximum(100);
               _spinnerGeoFilter_GeoDifference.setPageIncrement(10);
               _spinnerGeoFilter_GeoDifference.addSelectionListener(_compareSelectionListener);
               _spinnerGeoFilter_GeoDifference.addMouseWheelListener(_compareMouseWheelListener);
               GridDataFactory.fillDefaults().applyTo(_spinnerGeoFilter_GeoDifference);

            }
            {
               // %
               _lblGeoFilter_GeoDifference_Unit = new Label(container, SWT.NONE);
               _lblGeoFilter_GeoDifference_Unit.setText(UI.UNIT_PERCENT);

            }
         }
         {
            /*
             * Checkbox: Maximum results filter
             */
            {
               // Checkbox
               _chkGeoFilter_MaxResults = new Button(container, SWT.CHECK);
               _chkGeoFilter_MaxResults.setText(Messages.GeoCompare_View_Checkbox_MaxResultsFilter);
               _chkGeoFilter_MaxResults.setToolTipText(Messages.GeoCompare_View_Checkbox_MaxResultsFilter_Tooltip);
               _chkGeoFilter_MaxResults.addSelectionListener(_compareSelectionListener);
            }
            {
               // Spinner
               _spinnerGeoFilter_MaxResults = new Spinner(container, SWT.BORDER);
               _spinnerGeoFilter_MaxResults.setMinimum(0);
               _spinnerGeoFilter_MaxResults.setMaximum(100_000);
               _spinnerGeoFilter_MaxResults.setPageIncrement(10);
               _spinnerGeoFilter_MaxResults.addSelectionListener(_compareSelectionListener);
               _spinnerGeoFilter_MaxResults.addMouseWheelListener(_compareMouseWheelListener);
            }
            {
               // #
               _lblGeoFilter_MaxResults_Unit = new Label(container, SWT.NONE);
               _lblGeoFilter_MaxResults_Unit.setText(UI.SYMBOL_NUMBER_SIGN);
            }
         }
         final Composite stateContainer = new Composite(container, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, true).span(3, 1).align(SWT.FILL, SWT.END).applyTo(stateContainer);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(stateContainer);
//         stateContainer.setBackground(UI.SYS_COLOR_MAGENTA);
         {
            /*
             * Status color
             */
            _lblCompareStatus_Icon = new Label(stateContainer, SWT.NONE);
            _lblCompareStatus_Icon.setText(UI.SPACE4);

            /*
             * Label: Status message
             */
            _lblCompareStatus_Message = new Label(stateContainer, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, false)
//                  .align(SWT.FILL, SWT.END)
                  .applyTo(_lblCompareStatus_Message);
         }
      }
   }

   private void createUI_32_Col2_Info(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults()
            .grab(true, false)
            .applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_GREEN);

      {
         {
            /*
             * Number of tours
             */
            {
               _lblNumTours = new Label(container, SWT.NONE);
               _lblNumTours.setText(Messages.GeoCompare_View_Label_PossibleTours);

            }
            {
               _lblNumTours_Value = new Label(container, SWT.NONE);
               _lblNumTours_Value.setText(UI.EMPTY_STRING);
               GridDataFactory.fillDefaults().grab(true, false).applyTo(_lblNumTours_Value);
//               _lblNumTours.setBackground(UI.SYS_COLOR_CYAN);
            }
         }
         {
            /*
             * Number of geo parts
             */
            {
               _lblNumGeoGrids = new Label(container, SWT.NONE);
               _lblNumGeoGrids.setText(Messages.GeoCompare_View_Label_GeoParts);
               _lblNumGeoGrids.setToolTipText(Messages.GeoCompare_View_Label_GeoParts_Tooltip);

            }
            {
               _lblNumGeoGrids_Value = new Label(container, SWT.NONE);
               _lblNumGeoGrids_Value.setText(UI.EMPTY_STRING);
               GridDataFactory.fillDefaults().grab(true, false).applyTo(_lblNumGeoGrids_Value);
            }
         }
         {
            /*
             * Number of time slices
             */
            {
               _lblNumSlices = new Label(container, SWT.NONE);
               _lblNumSlices.setText(Messages.GeoCompare_View_Label_TimeSlices);

            }
            {
               _lblNumSlices_Value = new Label(container, SWT.NONE);
               _lblNumSlices_Value.setText(UI.EMPTY_STRING);
               GridDataFactory.fillDefaults().grab(true, false).applyTo(_lblNumSlices_Value);
            }
         }
         {
            /*
             * Start/end index
             */
            {
               _lblStartEndIndex = new Label(container, SWT.NONE);
               _lblStartEndIndex.setText(Messages.GeoCompare_View_Label_StartEndPosition);

            }
            {
               _lblStartEndIndex_Value = new Label(container, SWT.NONE);
               _lblStartEndIndex_Value.setText(UI.EMPTY_STRING);
               GridDataFactory.fillDefaults().grab(true, false).applyTo(_lblStartEndIndex_Value);
//               _lblStartEndIndex.setBackground(UI.SYS_COLOR_CYAN);
            }
         }
      }
   }

   private void createUI_80_TableViewer(final Composite parent) {

      /*
       * Create table
       */
      final Table table = new Table(parent, SWT.FULL_SELECTION /* | SWT.MULTI /* | SWT.BORDER */);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

      table.setHeaderVisible(true);
      table.setLinesVisible(false);

      /*
       * It took a while that the correct listener is set and also the checked item is fired and not
       * the wrong selection.
       */
      table.addListener(SWT.Selection, event -> onGeoPart_Select());

      /*
       * create table viewer
       */
      _geoCompareViewer = new GeoCompareViewer(table);

      _columnManager.createColumns(_geoCompareViewer);

      _geoCompareViewer.setUseHashlookup(true);
      _geoCompareViewer.setContentProvider(new CompareResultProvider());
      _geoCompareViewer.setComparator(_geoCompareComparator);

      _geoCompareViewer.addSelectionChangedListener(selectionChangedEvent -> onSelect_ComparerItem(selectionChangedEvent));

      updateUI_SetSortDirection(
            _geoCompareComparator.__sortColumnId,
            _geoCompareComparator.__sortDirection);

      createUI_82_ColumnImages(table);

      createUI_90_ContextMenu();
   }

   private void createUI_82_ColumnImages(final Table table) {

      boolean isColumnVisible = false;
      final ControlListener controlResizedAdapter = controlResizedAdapter(controlEvent -> onResize_SetWidthForImageColumn());

      // update column index which is needed for repainting
      final ColumnProfile activeProfile = _columnManager.getActiveProfile();
      _columnIndex_TourTypeImage = activeProfile.getColumnIndex(_colDef_TourTypeImage.getColumnId());

      final int numColumns = table.getColumns().length;

      // add column resize listener
      if (_columnIndex_TourTypeImage >= 0 && _columnIndex_TourTypeImage < numColumns) {

         isColumnVisible = true;
         table.getColumn(_columnIndex_TourTypeImage).addControlListener(controlResizedAdapter);
      }

      // add table listener
      if (isColumnVisible) {

         /*
          * NOTE: MeasureItem, PaintItem and EraseItem are called repeatedly. Therefore, it is
          * critical for performance that these methods be as efficient as possible.
          */
         final Listener paintListener = event -> {

            if (event.type == SWT.PaintItem) {

               onPaintViewer(event);
            }
         };

         table.addControlListener(controlResizedAdapter);
         table.addListener(SWT.PaintItem, paintListener);
      }
   }

   /**
    * Create the view context menus
    */
   private void createUI_90_ContextMenu() {

      _tableContextMenu = createUI_92_CreateViewerContextMenu();

      final Table table = _geoCompareViewer.getTable();

      _columnManager.createHeaderContextMenu(table, _tableViewerContextMenuProvider);
   }

   /**
    * create the views context menu
    *
    * @return
    */
   private Menu createUI_92_CreateViewerContextMenu() {

      _viewerMenuManager = new MenuManager();
      _viewerMenuManager.setRemoveAllWhenShown(true);
      _viewerMenuManager.addMenuListener(new IMenuListener() {
         @Override
         public void menuAboutToShow(final IMenuManager manager) {
            fillContextMenu(manager);
         }
      });

      final Table table = _geoCompareViewer.getTable();
      final Menu tableContextMenu = _viewerMenuManager.createContextMenu(table);

      return tableContextMenu;
   }

   private void defineAllColumns() {

      defineColumn_00_SequenceNumber();

      defineColumn_GeoDiff_Absolute();
      defineColumn_GeoDiff_Relative();
      defineColumn_Elevation_ElevationGain();
      defineColumn_Elevation_ElevationLoss();
      defineColumn_Time_TourStartDate();
      defineColumn_Tour_Type();
      defineColumn_Motion_AvgPace();
      defineColumn_Motion_AvgSpeed();
      defineColumn_Motion_Altimeter();
      defineColumn_Motion_Distance();

      defineColumn_Body_AvgPulse();

      defineColumn_Time_ElapsedTime();
      defineColumn_Time_RecordedTime();
      defineColumn_Time_MovingTime();

      defineColumn_Tour_Title();
   }

   private void defineColumn_00_SequenceNumber() {

      final ColumnDefinition colDef = new TableColumnDefinition(_columnManager, COLUMN_SEQUENCE, SWT.TRAIL);

      colDef.setColumnLabel(Messages.GeoCompare_View_Column_SequenceNumber_Label);
      colDef.setColumnHeaderText(Messages.GeoCompare_View_Column_SequenceNumber_Header);
      colDef.setColumnHeaderToolTipText(Messages.GeoCompare_View_Column_SequenceNumber_Label);

      colDef.setIsDefaultColumn();
      colDef.setDefaultColumnWidth(_pc.convertWidthInCharsToPixels(6));

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final int indexOf = _geoCompareViewer.getTable().indexOf((TableItem) cell.getItem());

            cell.setText(Integer.toString(indexOf + 1));
         }
      });

   }

   /**
    * Column: Average pulse
    */
   private void defineColumn_Body_AvgPulse() {

      final ColumnDefinition colDef = TableColumnFactory.BODY_AVG_PULSE.createColumn(_columnManager, _pc);

      // overwrite column id to identify the column when table is sorted
      colDef.setColumnId(COLUMN_AVG_PULSE);
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            colDef.printDetailValue(cell, item.avgPulse);
         }
      });
   }

   /**
    * Column: Elevation gain
    */
   private void defineColumn_Elevation_ElevationGain() {

      final ColumnDefinition colDef = TableColumnFactory.ALTITUDE_ELEVATION_TOTAL_GAIN.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);
      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final float value = item.elevationGain;
            if (value == 0) {

               cell.setText(UI.EMPTY_STRING);

            } else {

               cell.setText(_nf0.format(value));
            }
         }
      });
   }

   /**
    * Column: Elevation loss
    */
   private void defineColumn_Elevation_ElevationLoss() {

      final ColumnDefinition colDef = TableColumnFactory.ALTITUDE_ELEVATION_TOTAL_LOSS.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);
      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final float value = item.elevationLoss;
            if (value == 0) {

               cell.setText(UI.EMPTY_STRING);

            } else {

               cell.setText(_nf0.format(value));
            }
         }
      });
   }

   /**
    * Column: Geo Diff
    */
   private void defineColumn_GeoDiff_Absolute() {

      final ColumnDefinition colDef = new TableColumnDefinition(_columnManager, COLUMN_GEO_DIFF, SWT.TRAIL);

      colDef.setColumnLabel(Messages.GeoCompare_View_Column_GeoDiff_Label);
      colDef.setColumnHeaderText(Messages.GeoCompare_View_Column_GeoDiff_Header);
      colDef.setColumnHeaderToolTipText(Messages.GeoCompare_View_Column_GeoDiff_Tooltip);

      colDef.setDefaultColumnWidth(_pc.convertWidthInCharsToPixels(10));

      colDef.setIsDefaultColumn();
      colDef.setCanModifyVisibility(false);
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();
            final long minDiffValue = item.minDiffValue;

            final String minDiffText = minDiffValue == -2
                  ? UI.ELLIPSIS
                  : minDiffValue == -1
                        ? Messages.App_Label_NotAvailable_Shortcut
                        : Long.toString(minDiffValue);

            cell.setText(minDiffText);
         }
      });
   }

   /**
    * Column: Geo Diff
    */
   private void defineColumn_GeoDiff_Relative() {

      final ColumnDefinition colDef = new TableColumnDefinition(_columnManager, COLUMN_GEO_DIFF_RELATIVE, SWT.TRAIL);

      colDef.setColumnLabel(Messages.GeoCompare_View_Column_GeoDiff_Relative_Label);
      colDef.setColumnHeaderText(Messages.GeoCompare_View_Column_GeoDiff_Relative_Header);
      colDef.setColumnHeaderToolTipText(Messages.GeoCompare_View_Column_GeoDiff_Relative_Tooltip);

      colDef.setIsDefaultColumn();
      colDef.setDefaultColumnWidth(_pc.convertWidthInCharsToPixels(10));
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();
            final long minDiffValue = item.minDiffValue;

            final String minDiffText = minDiffValue == -2 || _maxMinDiff == -1
                  ? UI.ELLIPSIS
                  : minDiffValue == -1
                        ? Messages.App_Label_NotAvailable_Shortcut
                        : _nf1.format((float) minDiffValue / _maxMinDiff * 100);

            cell.setText(minDiffText);
         }
      });
   }

   /**
    * Column: Vertical speed (VAM average ascent speed)
    */
   private void defineColumn_Motion_Altimeter() {

      final ColumnDefinition colDef = TableColumnFactory.MOTION_ALTIMETER.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final float value = item.avgAltimeter;

            colDef.printDetailValue(cell, value);
         }
      });
   }

   /**
    * Column: Motion - Avg pace min/km - min/mi
    */
   private void defineColumn_Motion_AvgPace() {

      final ColumnDefinition colDef = TableColumnFactory.MOTION_AVG_PACE.createColumn(_columnManager, _pc);

      // overwrite column id to identify the column when table is sorted
      colDef.setColumnId(COLUMN_AVG_PACE);
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final float avgpace = item.avgPace * UI.UNIT_VALUE_DISTANCE;

            if (avgpace == 0) {
               cell.setText(UI.EMPTY_STRING);
            } else {
               cell.setText(UI.format_mm_ss((long) avgpace));
            }
         }
      });
   }

   /**
    * Column: Motion - Avg speed km/h - mph
    */
   private void defineColumn_Motion_AvgSpeed() {

      final ColumnDefinition colDef = TableColumnFactory.MOTION_AVG_SPEED.createColumn(_columnManager, _pc);

      // overwrite column id to identify the column when table is sorted
      colDef.setColumnId(COLUMN_AVG_SPEED);
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final double speed = item.avgSpeed / UI.UNIT_VALUE_DISTANCE;

            colDef.printDetailValue(cell, speed);
         }
      });
   }

   /**
    * Column: Distance
    */
   private void defineColumn_Motion_Distance() {

      final ColumnDefinition colDef = TableColumnFactory.MOTION_DISTANCE.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final double value = item.distance
                  / 1000.0
                  / UI.UNIT_VALUE_DISTANCE;

            colDef.printDetailValue(cell, value);
         }
      });
   }

   /**
    * Column: Elapsed time (h)
    */
   private void defineColumn_Time_ElapsedTime() {

      final ColumnDefinition colDef = TableColumnFactory.TIME__DEVICE_ELAPSED_TIME.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final long value = item.elapsedTime;

            colDef.printLongValue(cell, value, true);
         }
      });
   }

   /**
    * Column: Moving time (h)
    */
   private void defineColumn_Time_MovingTime() {

      final ColumnDefinition colDef = TableColumnFactory.TIME__COMPUTED_MOVING_TIME.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final long value = item.movingTime;

            colDef.printLongValue(cell, value, true);
         }
      });
   }

   /**
    * Column: Time - Recorded time (h)
    */
   private void defineColumn_Time_RecordedTime() {

      final ColumnDefinition colDef = TableColumnFactory.TIME__DEVICE_RECORDED_TIME.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);
      colDef.setIsDefaultColumn();

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final long value = item.recordedTime;

            colDef.printLongValue(cell, value, true);
         }
      });
   }

   /**
    * Column: Tour start date
    */
   private void defineColumn_Time_TourStartDate() {

      final ColumnDefinition colDef = TableColumnFactory.TIME_TOUR_START_DATE.createColumn(_columnManager, _pc);

      // overwrite column id to identify the column when table is sorted
      colDef.setColumnId(COLUMN_TOUR_START_DATE);
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            final ZonedDateTime tourStartTime = item.tourStartTime;

            cell.setText(tourStartTime == null
                  ? UI.EMPTY_STRING
                  : tourStartTime.format(TimeTools.Formatter_Date_S));
         }
      });
   }

   /**
    * Column: Tour title
    */
   private void defineColumn_Tour_Title() {

      final ColumnDefinition colDef = TableColumnFactory.TOUR_TITLE.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setColumnId(COLUMN_TOUR_TITLE);

      colDef.setLabelProvider(new SelectionCellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final GeoComparedTour item = (GeoComparedTour) cell.getElement();

            cell.setText(item.tourTitle);
         }
      });
   }

   /**
    * Column: Tour type image
    */
   private void defineColumn_Tour_Type() {

      _colDef_TourTypeImage = TableColumnFactory.TOUR_TYPE.createColumn(_columnManager, _pc);
      _colDef_TourTypeImage.setIsDefaultColumn();
      _colDef_TourTypeImage.setLabelProvider(new SelectionCellLabelProvider() {

         // !!! When using cell.setImage() then it is not centered !!!
         // !!! Set dummy label provider, otherwise an error occures !!!
         @Override
         public void update(final ViewerCell cell) {}
      });
   }

   @Override
   public void dispose() {

      UI.disposeResource(_imageCompareType_GeoCompare);
      UI.disposeResource(_imageCompareType_PlaceHolder);
      UI.disposeResource(_imageCompareType_RefTour);
      UI.disposeResource(_imageOptions_Enabled);
      UI.disposeResource(_imageOptions_Disabled);

      getSite().getPage().removePostSelectionListener(_postSelectionListener);
      getSite().getPage().removePartListener(_partListener);

      TourManager.getInstance().removeTourEventListener(_tourEventListener);
      GeoCompareManager.removeGeoCompareListener(this);

      _prefStore.removePropertyChangeListener(_prefChangeListener);
      _prefStore_Common.removePropertyChangeListener(_prefChangeListener_Common);

      super.dispose();
   }

   private void enableActions() {

      final boolean isTourSelected = _selectedGeoComparedTour != null;

      _actionHideToursBelow.setEnabled(isTourSelected);
   }

   private void enableControls() {

// SET_FORMATTING_OFF

      final boolean isGeoCompareON     = GeoCompareManager.isGeoComparingOn();

      _actionAppTourFilter                .setEnabled(isGeoCompareON);
      _actionPinTourWhichIsCompared       .setEnabled(isGeoCompareON);
      _actionSelectTourWhichIsCompared    .setEnabled(isGeoCompareON);

      _chkGeoFilter_GeoDiff               .setEnabled(isGeoCompareON);
      _chkGeoFilter_MaxResults            .setEnabled(isGeoCompareON);

      _lblCompareStatus_Message           .setEnabled(isGeoCompareON);
      _lblGeoFilter_GeoDifference_Unit    .setEnabled(isGeoCompareON && _isGeoFilter_GeoDifference);
      _lblGeoFilter_MaxResults_Unit       .setEnabled(isGeoCompareON && _isGeoFilter_MaxResults);
      _lblNumGeoGrids                     .setEnabled(isGeoCompareON);
      _lblNumGeoGrids_Value               .setEnabled(isGeoCompareON);
      _lblNumSlices                       .setEnabled(isGeoCompareON);
      _lblNumSlices_Value                 .setEnabled(isGeoCompareON);
      _lblNumTours                        .setEnabled(isGeoCompareON);
      _lblNumTours_Value                  .setEnabled(isGeoCompareON);
      _lblStartEndIndex                   .setEnabled(isGeoCompareON);
      _lblStartEndIndex_Value             .setEnabled(isGeoCompareON);
      _lblTitle                           .setEnabled(isGeoCompareON);

      _spinnerGeoFilter_GeoDifference     .setEnabled(isGeoCompareON && _isGeoFilter_GeoDifference);
      _spinnerGeoFilter_MaxResults        .setEnabled(isGeoCompareON && _isGeoFilter_MaxResults);

      _geoCompareViewer.getTable()        .setEnabled(isGeoCompareON);

// SET_FORMATTING_ON
   }

   private void fillContextMenu(final IMenuManager menuMgr) {

      menuMgr.add(_actionHideToursBelow);

      enableActions();
   }

   private void fillToolbar() {

      final IToolBarManager toolbarMrg = getViewSite().getActionBars().getToolBarManager();

      toolbarMrg.add(_actionOnOff);
      toolbarMrg.add(_actionAppTourFilter);
      toolbarMrg.add(_actionGeoCompareOptions);

      toolbarMrg.update(true);
   }

   /**
    * Filter compare result
    *
    * @param geoCompareData
    */
   private void filterGeoCompareItems(final GeoCompareData geoCompareData) {

      // first display all tours
      _allGeoComparedTours = geoCompareData.allGeoComparedTours;
      geoCompareData.allGeoComparedTours_Filtered = _allGeoComparedTours;

      if (_isGeoFilter_GeoDifference) {

         final List<GeoComparedTour> filteredComparedTours = new ArrayList<>(

               geoCompareData.allGeoComparedTours
                     .stream()
                     .filter(geoComparedTour -> isInGeoDiffFilter(geoComparedTour.minDiffValue))
                     .collect(Collectors.toList())

         );

         _allGeoComparedTours = filteredComparedTours;
         geoCompareData.allGeoComparedTours_Filtered = filteredComparedTours;
      }

      if (_isGeoFilter_MaxResults) {

         if (_allGeoComparedTours.size() > _geoFilter_MaxResults) {

            // sort by geo diff
            _allGeoComparedTours.sort((final GeoComparedTour tour1, final GeoComparedTour tour2) -> {

               final long value1 = tour1.minDiffValue;
               final long value2 = tour2.minDiffValue;

               return (value1 < value2) ? -1 : ((value1 == value2) ? 0 : 1);
            });

            _allGeoComparedTours = _allGeoComparedTours.subList(0, _geoFilter_MaxResults);
            geoCompareData.allGeoComparedTours_Filtered = _allGeoComparedTours;
         }
      }
   }

   @Override
   public void geoCompareEvent(final IWorkbenchPart part, final GeoCompareEventId eventId, final Object eventData) {

      if (part == GeoCompareView.this) {
         return;
      }

      switch (eventId) {
      case TOUR_IS_GEO_COMPARED:
         break;

      case SET_COMPARING_ON:
      case SET_COMPARING_OFF:

         onAction_OnOff(eventId == GeoCompareEventId.SET_COMPARING_ON);

         break;
      }
   }

   @Override
   public ColumnManager getColumnManager() {
      return _columnManager;
   }

   GeoCompareState getSlideoutState() {
      return _slideoutGeoCompareState;
   }

   /**
    * @param sortColumnId
    * @return Returns the column widget by it's column id, when column id is not found then the
    *         first column is returned.
    */
   private TableColumn getSortColumn(final String sortColumnId) {

      final TableColumn[] allColumns = _geoCompareViewer.getTable().getColumns();

      for (final TableColumn column : allColumns) {

         final String columnId = ((ColumnDefinition) column.getData()).getColumnId();

         if (columnId.equals(sortColumnId)) {
            return column;
         }
      }

      return allColumns[0];
   }

   @Override
   public ColumnViewer getViewer() {
      return _geoCompareViewer;
   }

   private StructuredSelection getViewerSelection() {

      return (StructuredSelection) _geoCompareViewer.getSelection();
   }

   private void initUI(final Composite parent) {

      _parent = parent;
      _display = parent.getDisplay();

      _pc = new PixelConverter(parent);

      _columnSortListener = widgetSelectedAdapter(selectionEvent -> onSelect_SortColumn(selectionEvent));

      _compareSelectionListener = widgetSelectedAdapter(selectionEvent -> onSelect_CompareParameter());
      _compareMouseWheelListener = mouseEvent -> {
         UI.adjustSpinnerValueOnMouseScroll(mouseEvent, 10);
         onSelect_CompareParameter();
      };
   }

   private boolean isIgnorePart(final IWorkbenchPart part, final ISelection selection) {

      // ignore own part
      if (part == GeoCompareView.this) {
         return true;
      }

      // ignore other parts to prevent geo part comparing !!!
      if (part instanceof ReferenceTourChartView) {
         return true;
      }

      /*
       * Allow selection of GeoComparedTour which is used to navigate compared tours in the
       * "Compared Tour" view
       */
      if (part instanceof ComparedTourChartView) {

         if (selection instanceof StructuredSelection) {

            final Object firstElement = ((StructuredSelection) selection).getFirstElement();
            if (firstElement instanceof GeoComparedTour) {

               return false;
            }
         }

         return true;
      }

      return false;
   }

   /**
    * Evaluates whether a given minDiffValue is between 0 and the current geo
    * relative difference filter value, false otherwise.
    *
    * @param minDiffValue
    *           A given value
    * @return true if the minDiffValue is between 0 and the current geo
    *         relative difference filter value, false otherwise.
    */
   private boolean isInGeoDiffFilter(final long minDiffValue) {

      final float relativeDiff = (float) minDiffValue / _maxMinDiff * 100;

      final float relativeDifference = _geoFilter_GeoDifference == 0

            // allow values which are near 0
            ? 0.5f

            : _geoFilter_GeoDifference;

      return relativeDiff >= 0 && relativeDiff <= relativeDifference;
   }

   public Object navigateTour(final boolean isNextTour) {

      if (_selectedGeoComparedTour == null
            || _allSortedAndFiltered_GeoComparedTours == null
            || _allSortedAndFiltered_GeoComparedTours.length == 0) {

         return null;
      }

      final int numTours = _allSortedAndFiltered_GeoComparedTours.length;

      if (numTours < 2) {

         // there is nothing which can be navigated

         return _allSortedAndFiltered_GeoComparedTours[0];
      }

      for (int tourIndex = 0; tourIndex < numTours; tourIndex++) {

         final GeoComparedTour geoComparedTour = _allSortedAndFiltered_GeoComparedTours[tourIndex];

         if (geoComparedTour == _selectedGeoComparedTour) {

            if (isNextTour) {

               // navigate next tour

               final int nextIndex = tourIndex + 1;

               if (nextIndex >= numTours) {

                  // navigate to the first tour

                  return _allSortedAndFiltered_GeoComparedTours[0];

               } else {

                  // navigate to the next tour

                  return _allSortedAndFiltered_GeoComparedTours[nextIndex];
               }

            } else {

               // navigate previous tour

               final int previousIndex = tourIndex - 1;

               if (previousIndex < 0) {

                  // navigate to the last tour

                  return _allSortedAndFiltered_GeoComparedTours[numTours - 1];

               } else {

                  // navigate to the previous tour

                  return _allSortedAndFiltered_GeoComparedTours[previousIndex];
               }
            }
         }
      }

      return null;
   }

   private void onAction_AppFilter(final boolean isSelected) {

      _compareData_IsUseAppFilter = isSelected;

      recompareTours();
   }

   private void onAction_HideToursBelow() {

      final Table table = _geoCompareViewer.getTable();
      final int selectionIndex = table.getSelectionIndex();

      if (selectionIndex == -1) {
         return;
      }

      // update tour max filter
      _isGeoFilter_MaxResults = true;
      _chkGeoFilter_MaxResults.setSelection(true);
      _spinnerGeoFilter_MaxResults.setSelection(selectionIndex);

      saveState();
      enableControls();

      // get remaining tours
      final GeoComparedTour[] remainingTours = Arrays.copyOf(_allSortedAndFiltered_GeoComparedTours, selectionIndex);
      _allGeoComparedTours = new ArrayList<>();
      _allGeoComparedTours.addAll(Arrays.asList(remainingTours));

      final GeoCompareData geoCompareData = _compareData_CurrentGeoCompareData;

      // update filtered data that the year statistic shows the correct values
      geoCompareData.allGeoComparedTours_Filtered = _allGeoComparedTours;

      updateUI_Viewer();

      // fire geo compare data
      GeoCompareManager.fireEvent(
            GeoCompareEventId.TOUR_IS_GEO_COMPARED,
            geoCompareData,
            GeoCompareView.this);
   }

   /**
    * @param isOn
    *           Turn comparing ON/OFF
    * @param isDoRecomparing
    */
   private void onAction_OnOff(final boolean isOn) {

      _actionOnOff.setChecked(isOn);
      _actionOnOff.setIcon(isOn);

      GeoCompareManager.setGeoComparing(isOn, this);

      if (isOn) {

         // enable comparing

         // fire currently selected tour that other views can recognize it

         final StructuredSelection viewerSelection = getViewerSelection();

         if (viewerSelection.isEmpty()) {

            onAction_SelectTourWhichIsCompared();

         } else {

            _postSelectionProvider.setSelection(viewerSelection);
         }

      } else {

         // cancel comparing

         setState_StopComparing();
         updateUI_State_CancelComparing();
      }

      enableControls();
   }

   private void onAction_PinTourWhichIsCompared(final boolean isChecked) {

      _isComparedTourPinned = isChecked;
   }

   private void onAction_SelectTourWhichIsCompared() {

      for (final GeoComparedTour comparedTour : _allGeoComparedTours) {

         if (comparedTour.tourId == _compareData_TourId) {

            _geoCompareViewer.setSelection(new StructuredSelection(comparedTour), true);
            _geoCompareViewer.getTable().showSelection();

            return;
         }
      }
   }

   void onChangeCompareParameter() {

// SET_FORMATTING_OFF

      _compareData_GeoAccuracy      = Util.getStateInt(_state, GeoCompareView.STATE_GEO_ACCURACY,      GeoCompareView.DEFAULT_GEO_ACCURACY);
      _compareData_DistanceInterval = Util.getStateInt(_state, GeoCompareView.STATE_DISTANCE_INTERVAL, GeoCompareView.DEFAULT_DISTANCE_INTERVAL);

// SET_FORMATTING_ON

      if (_lastCompare_GeoAccuracy != _compareData_GeoAccuracy
            || _lastCompare_DistanceInterval != _compareData_DistanceInterval) {

         // geo compare options are modified

         recompareTours();
      }
   }

   private void onGeoPart_Select() {

      if (_isInUpdate) {
         return;
      }
   }

   private void onPaintViewer(final Event event) {

      // paint column image

      final int columnIndex = event.index;

      if (columnIndex == _columnIndex_TourTypeImage) {

         onPaintViewer_TourTypeImage(event);
      }
   }

   private void onPaintViewer_TourTypeImage(final Event event) {

      final Object itemData = event.item.getData();

      if (itemData instanceof GeoComparedTour) {

         final GeoComparedTour compareItem = (GeoComparedTour) itemData;
         final TourType tourType = compareItem.tourType;

         if (tourType != null) {

            final long tourTypeId = tourType.getTypeId();
            final Image image = TourTypeImage.getTourTypeImage(tourTypeId);

            if (image != null) {

               UI.paintImageCentered(event, image, _columnWidth_TourTypeImage);
            }
         }
      }
   }

   private void onResize_SetWidthForImageColumn() {

      if (_colDef_TourTypeImage != null) {

         final TableColumn tableColumn = _colDef_TourTypeImage.getTableColumn();

         if (tableColumn != null && tableColumn.isDisposed() == false) {

            _columnWidth_TourTypeImage = tableColumn.getWidth();
         }
      }
   }

   private void onSelect_CompareParameter() {

      _isGeoFilter_GeoDifference = _chkGeoFilter_GeoDiff.getSelection();
      _isGeoFilter_MaxResults = _chkGeoFilter_MaxResults.getSelection();
      _geoFilter_GeoDifference = _spinnerGeoFilter_GeoDifference.getSelection();
      _geoFilter_MaxResults = _spinnerGeoFilter_MaxResults.getSelection();

      saveState();

      enableControls();

      // update UI immediately
      _display.asyncExec(() -> {

         if (_parent.isDisposed()) {
            return;
         }

         // update compare result
         filterGeoCompareItems(_compareData_CurrentGeoCompareData);

         updateUI_Viewer();

         // fire geo compare data
         GeoCompareManager.fireEvent(
               GeoCompareEventId.TOUR_IS_GEO_COMPARED,
               _compareData_CurrentGeoCompareData,
               GeoCompareView.this);
      });
   }

   private void onSelect_ComparerItem(final SelectionChangedEvent event) {

      if (_isInSelection) {
         return;
      }

      final ISelection selection = event.getSelection();
      final Object firstElement = ((StructuredSelection) selection).getFirstElement();

      _selectedGeoComparedTour = (GeoComparedTour) firstElement;

      // fire selection for the selected geo part tour
      _postSelectionProvider.setSelection(selection);
   }

   private void onSelect_SortColumn(final SelectionEvent e) {

      _viewerContainer.setRedraw(false);
      {
         // keep selection
         final ISelection selectionBackup = getViewerSelection();
         {
            // update viewer with new sorting
            _geoCompareComparator.setSortColumn(e.widget);
            _geoCompareViewer.refresh();
         }
         updateUI_SelectCompareItem(selectionBackup);
      }
      _viewerContainer.setRedraw(true);
   }

   private void onSelection_MapSelection(final SelectionMapSelection mapSelection) {

      final TourData tourData = TourManager.getTour(mapSelection.getTourId());

      if (tourData != null) {

         final int valueIndex1 = mapSelection.getValueIndex1();
         final int valueIndex2 = mapSelection.getValueIndex2();

         final long geoCompareRefId = ReferenceTourManager.createGeoCompareRefTour_Virtual(
               tourData,
               valueIndex1,
               valueIndex2);

         compare_10_Compare(
               tourData,
               valueIndex1,
               valueIndex2,
               geoCompareRefId);
      }
   }

   private void onSelectionChanged(final ISelection selection) {

      if (selection == null
            || GeoCompareManager.isGeoComparingOn() == false) {

         return;
      }

// !!! THIS IS NOT WORKING !!!
//
//      final int selectionHash = selection.hashCode();
//      if (_lastSelectionHash == selectionHash) {
//
//         /*
//          * Last selection has not changed, this can occur when the app lost the focus and got the
//          * focus again.
//          */
//         return;
//      }
//
//      _lastSelectionHash = selectionHash;

      if (selection instanceof SelectionChartInfo) {

         final SelectionChartInfo chartInfo = (SelectionChartInfo) selection;

         if (GeoCompareManager.isGeoComparingOn() == false) {
            return;
         }

         TourData tourData = null;

         final Chart chart = chartInfo.getChart();
         if (chart instanceof TourChart) {

            final TourChart tourChart = (TourChart) chart;
            tourData = tourChart.getTourData();
         }

         if (tourData != null && tourData.isMultipleTours()) {

            // multiple tours are selected

         } else {

            // use old behaviour

            final ChartDataModel chartDataModel = chartInfo.chartDataModel;
            if (chartDataModel != null) {

               final Object tourId = chartDataModel.getCustomData(Chart.CUSTOM_DATA_TOUR_ID);
               if (tourId instanceof Long) {

                  tourData = TourManager.getInstance().getTourData((Long) tourId);
                  if (tourData == null) {

                     // tour is not in the database, try to get it from the raw data manager

                     final Map<Long, TourData> rawData = RawDataManager.getInstance().getImportedTours();
                     tourData = rawData.get(tourId);
                  }
               }
            }
         }

         if (tourData != null) {

            if (tourData.isMultipleTours()) {

               // multiple tours are not supported

               clearView(InvalidData.MultipleTours);

            } else {

               if (_isComparedTourPinned

                     // allow modifications of the same tour
                     && tourData != _compareData_TourData) {

                  // ignore tour

               } else {

                  final int leftSliderValuesIndex = chartInfo.leftSliderValuesIndex;
                  final int rightSliderValuesIndex = chartInfo.rightSliderValuesIndex;

                  final long geoCompareRefId = ReferenceTourManager.createGeoCompareRefTour_Virtual(
                        tourData,
                        leftSliderValuesIndex,
                        rightSliderValuesIndex);

                  compare_10_Compare(
                        tourData,
                        leftSliderValuesIndex,
                        rightSliderValuesIndex,
                        geoCompareRefId);
               }
            }
         }

      } else if (selection instanceof SelectionChartXSliderPosition) {

         final SelectionChartXSliderPosition xSliderPos = (SelectionChartXSliderPosition) selection;
         final Chart chart = xSliderPos.getChart();
         if (chart == null) {
            return;
         }

         final ChartDataModel chartDataModel = chart.getChartDataModel();

         final Object tourId = chartDataModel.getCustomData(Chart.CUSTOM_DATA_TOUR_ID);
         if (tourId instanceof Long) {

            final TourData tourData = TourManager.getInstance().getTourData((Long) tourId);
            if (tourData != null) {

               if (tourData.isMultipleTours()) {

                  // multiple tours are not supported

                  clearView(InvalidData.MultipleTours);

               } else {

                  final int leftSliderValueIndex = xSliderPos.getLeftSliderValueIndex();
                  int rightSliderValueIndex = xSliderPos.getRightSliderValueIndex();

                  rightSliderValueIndex =
                        rightSliderValueIndex == SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION
                              ? leftSliderValueIndex
                              : rightSliderValueIndex;

                  final long geoCompareRefId = ReferenceTourManager.createGeoCompareRefTour_Virtual(
                        tourData,
                        leftSliderValueIndex,
                        rightSliderValueIndex);

                  compare_10_Compare(
                        tourData,
                        leftSliderValueIndex,
                        rightSliderValueIndex,
                        geoCompareRefId);
               }
            }
         }

      } else if (selection instanceof SelectionReferenceTourView) {

         final SelectionReferenceTourView selectionReferenceTourView = (SelectionReferenceTourView) selection;
         final Long refId = selectionReferenceTourView.getRefId();

         if (_isComparedTourPinned

               // allow modifications of the same tour
               && refId != _compareData_RefId) {

            // ignore tour

         } else {

            compareRefTour(refId);
         }

      } else if (selection instanceof StructuredSelection) {

         final Object firstElement = ((StructuredSelection) selection).getFirstElement();

         if (firstElement instanceof TVIRefTour_ComparedTour) {

            final TVIRefTour_ComparedTour comparedTour = (TVIRefTour_ComparedTour) firstElement;
            final GeoComparedTour geoCompareTour = comparedTour.getGeoCompareTour();

            if (geoCompareTour != null) {

               selectGeoComparedTour(geoCompareTour);

            } else {

               compareRefTour(comparedTour.getRefId());
            }

         } else if (firstElement instanceof GeoComparedTour) {

            final GeoComparedTour geoCompareTour = (GeoComparedTour) firstElement;

            selectGeoComparedTour(geoCompareTour);

         } else if (firstElement instanceof TVIElevationCompareResult_ComparedTour) {

            compareRefTour(((TVIElevationCompareResult_ComparedTour) firstElement).refTour.refId);
         }

      } else if (selection instanceof SelectionDeletedTours) {

         clearView(InvalidData.NoGeoData);
      }

      enableControls();
   }

   private void recompareTours() {

      if (_compareData_GeoGrid == null) {

         _pageBook.showPage(_pageSelectTourWithGeoData);

      } else if (GeoCompareManager.isGeoComparingOn()) {

         compare_20_SetupComparing();
      }
   }

   @Override
   public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

      _viewerContainer.setRedraw(false);
      {
         _geoCompareViewer.getTable().dispose();

         createUI_80_TableViewer(_viewerContainer);
         _viewerContainer.layout();

         // update the viewer
         reloadViewer();
      }
      _viewerContainer.setRedraw(true);

      return _geoCompareViewer;
   }

   @Override
   public void reloadViewer() {

      updateUI_Viewer();
   }

   private void restoreSelection() {

      // try to use selection from selection service
      onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

      if (_compareData_TourData == null) {

         showInvalidPage();
      }
   }

   private void restoreState() {

      final boolean isGeoCompareOn = GeoCompareManager.isGeoComparingOn();

// SET_FORMATTING_OFF

      _compareData_IsUseAppFilter      = Util.getStateBoolean(_state,   STATE_IS_USE_APP_FILTER,               true);

      _compareData_DistanceInterval    = Util.getStateInt(_state,       STATE_DISTANCE_INTERVAL,               DEFAULT_DISTANCE_INTERVAL);
      _compareData_GeoAccuracy         = Util.getStateInt(_state,       STATE_GEO_ACCURACY,                    DEFAULT_GEO_ACCURACY);

      _isGeoFilter_GeoDifference       = Util.getStateBoolean(_state,   STATE_IS_GEO_FILTER_GEO_DIFFERENCE,    false);
      _isGeoFilter_MaxResults          = Util.getStateBoolean(_state,   STATE_IS_GEO_FILTER_MAX_RESULTS,       false);
      _geoFilter_GeoDifference         = Util.getStateInt(_state,       STATE_GEO_FILTER_GEO_DIFFERENCE,       50);
      _geoFilter_MaxResults            = Util.getStateInt(_state,       STATE_GEO_FILTER_SEQUENCE_FILTER,      100);

      _actionOnOff                        .setChecked(isGeoCompareOn);
      _actionOnOff                        .setIcon(isGeoCompareOn);
      _actionAppTourFilter                .setChecked(_compareData_IsUseAppFilter);

      _chkGeoFilter_GeoDiff               .setSelection(_isGeoFilter_GeoDifference);
      _chkGeoFilter_MaxResults            .setSelection(_isGeoFilter_MaxResults);
      _spinnerGeoFilter_GeoDifference     .setSelection(_geoFilter_GeoDifference);
      _spinnerGeoFilter_MaxResults        .setSelection(_geoFilter_MaxResults);

      enableControls();
   }

   private void restoreState_BeforeUI() {

      // sorting
      final String sortColumnId = Util.getStateString(_state,  STATE_SORT_COLUMN_ID,         COLUMN_GEO_DIFF);
      final int sortDirection   = Util.getStateInt(_state,     STATE_SORT_COLUMN_DIRECTION,  GeoCompareComparator.ASCENDING);

      // update comparator
      _geoCompareComparator.__sortColumnId = sortColumnId;
      _geoCompareComparator.__sortDirection = sortDirection;
   }

   @PersistState
   private void saveState() {

      _state.put(STATE_IS_USE_APP_FILTER,                _compareData_IsUseAppFilter);

      _state.put(STATE_IS_GEO_FILTER_GEO_DIFFERENCE,     _isGeoFilter_GeoDifference);
      _state.put(STATE_IS_GEO_FILTER_MAX_RESULTS,        _isGeoFilter_MaxResults);
      _state.put(STATE_GEO_FILTER_GEO_DIFFERENCE,        _geoFilter_GeoDifference);
      _state.put(STATE_GEO_FILTER_SEQUENCE_FILTER,       _geoFilter_MaxResults);

      _state.put(STATE_SORT_COLUMN_ID,                   _geoCompareComparator.__sortColumnId);
      _state.put(STATE_SORT_COLUMN_DIRECTION,            _geoCompareComparator.__sortDirection);

      _columnManager.saveState(_state);

// SET_FORMATTING_ON
   }

   private void selectGeoComparedTour(final GeoComparedTour geoCompareTour) {

      _isInSelection = true;
      {
         _geoCompareViewer.setSelection(new StructuredSelection(geoCompareTour));

         // make the selection visible, table is scrolled when needed
         final Table table = _geoCompareViewer.getTable();
         table.setSelection(table.getSelectionIndex());

         _selectedGeoComparedTour = geoCompareTour;
      }
      _isInSelection = false;
   }

   @Override
   public void setFocus() {

      _geoCompareViewer.getTable().setFocus();
   }

   private void setState_StopComparing() {

      GeoPartTourLoader.stopLoading(_compareData_CurrentGeoCompareData);
   }

   private void showInvalidPage() {

      _pageBook.showPage(_pageSelectTourWithGeoData);
   }

   @Override
   public void updateColumnHeader(final ColumnDefinition colDef) {}

   /**
    * @param geoCompareData
    *           Can be <code>null</code> to reset the UI
    */
   private void updateUI_GeoCompareData(final GeoCompareData geoCompareData) {

      if (geoCompareData == null) {

         _lblTitle.setText(UI.EMPTY_STRING);

         _iconCompareType.setImage(_imageCompareType_PlaceHolder);
         _iconCompareType.setToolTipText(UI.EMPTY_STRING);

         _slideoutGeoCompareState.isReset = true;

      } else {

         _lblTitle.setText(_compareData_TourTitle);

         if (ReferenceTourManager.isFromNativeRefTour()) {

            _iconCompareType.setImage(_imageCompareType_RefTour);
            _iconCompareType.setToolTipText(Messages.GeoCompare_View_Icon_CompareType_RefTour_Tooltip);

         } else {

            _iconCompareType.setImage(_imageCompareType_GeoCompare);
            _iconCompareType.setToolTipText(Messages.GeoCompare_View_Icon_CompareType_GeoTour_Tooltip);
         }

         _slideoutGeoCompareState.numTours = geoCompareData.tourIds.length;
      }

      _slideoutGeoCompareOptions.updateUI_StateValues(_slideoutGeoCompareState);

      updateUI_StateValues();
   }

   /**
    * Select and reveal a compare item item.
    *
    * @param selection
    */
   private void updateUI_SelectCompareItem(final ISelection selection) {

      _isInUpdate = true;
      {
         _geoCompareViewer.setSelection(selection, true);

         _geoCompareViewer.getTable().showSelection();
      }
      _isInUpdate = false;
   }

   /**
    * Set the sort column direction indicator for a column.
    *
    * @param sortColumnId
    * @param isAscendingSort
    */
   private void updateUI_SetSortDirection(final String sortColumnId, final int sortDirection) {

      final Table table = _geoCompareViewer.getTable();
      final TableColumn tc = getSortColumn(sortColumnId);

      table.setSortColumn(tc);
      table.setSortDirection(sortDirection == GeoCompareComparator.ASCENDING ? SWT.UP : SWT.DOWN);
   }

   private void updateUI_State_CancelComparing() {

      _lblCompareStatus_Message.setText(Messages.GeoCompare_View_State_ComparingIsCanceled);

      _lblCompareStatus_Icon.setText(UI.SPACE4);
      _lblCompareStatus_Icon.setBackground(UI.IS_DARK_THEME
            ? ThemeUtil.getDefaultBackgroundColor_Table()
            : ThemeUtil.getDefaultBackgroundColor_Shell());
   }

   private void updateUI_State_Progress(final int workedTours, final int numTours) {

      if (workedTours == -1 && numTours == -1) {

         // start comparing

         _lblCompareStatus_Message.setText(Messages.GeoCompare_View_State_StartComparing);

         _lblCompareStatus_Icon.setText(UI.SPACE4);
         _lblCompareStatus_Icon.setBackground(UI.SYS_COLOR_GREEN);

      } else if (workedTours == numTours) {

         // comparing is done

         _lblCompareStatus_Message.setText(String.format(Messages.GeoCompare_View_State_CompareResult, numTours));

         _lblCompareStatus_Icon.setText(UI.SYMBOL_HEAVY_CHECK_MARK);
         _lblCompareStatus_Icon.setBackground(UI.IS_DARK_THEME
               ? ThemeUtil.getDefaultBackgroundColor_Table()
               : ThemeUtil.getDefaultBackgroundColor_Shell());

      } else {

         // comparing is in progress

         _lblCompareStatus_Message.setText(String.format(Messages.GeoCompare_View_State_ComparingTours, workedTours, numTours));

         _lblCompareStatus_Icon.setText(UI.SPACE4);
         _lblCompareStatus_Icon.setBackground(COLOR_COMPARING_TOURS);
      }
   }

   private void updateUI_StateValues() {

      if (_slideoutGeoCompareState.isReset) {

         _lblNumGeoGrids_Value.setText(UI.EMPTY_STRING);
         _lblNumSlices_Value.setText(UI.EMPTY_STRING);
         _lblNumTours_Value.setText(UI.EMPTY_STRING);
         _lblStartEndIndex_Value.setText(UI.EMPTY_STRING);

      } else {

         _lblNumGeoGrids_Value.setText(Integer.toString(_slideoutGeoCompareState.numGeoGrids));
         _lblNumSlices_Value.setText(Integer.toString(_slideoutGeoCompareState.lastIndex - _slideoutGeoCompareState.firstIndex));
         _lblNumTours_Value.setText(Integer.toString(_slideoutGeoCompareState.numTours));
         _lblStartEndIndex_Value.setText(String.format(FORMAT_START_END, _slideoutGeoCompareState.firstIndex, _slideoutGeoCompareState.lastIndex));
      }
   }

   private void updateUI_Viewer() {

      // reset current selection
      _selectedGeoComparedTour = null;

      _geoCompareViewer.setInput(new Object[0]);
   }
}
