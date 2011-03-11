/*******************************************************************************
 * Copyright (C) 2005, 2010  Wolfgang Schramm and Contributors
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import net.tourbook.Messages;
import net.tourbook.algorithm.DPPoint;
import net.tourbook.algorithm.DouglasPeuckerSimplifier;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.data.AltitudeUpDownSegment;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.data.TourSegment;
import net.tourbook.database.MyTourbookException;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.preferences.PrefPageComputedValues;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.SelectionDeletedTours;
import net.tourbook.tour.SelectionTourData;
import net.tourbook.tour.SelectionTourId;
import net.tourbook.tour.SelectionTourIds;
import net.tourbook.tour.TourEditor;
import net.tourbook.tour.TourEvent;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ImageComboLabel;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.UI;
import net.tourbook.ui.action.ActionModifyColumns;
import net.tourbook.ui.tourChart.TourChart;
import net.tourbook.ui.tourChart.TourChartView;
import net.tourbook.util.ArrayListToArray;
import net.tourbook.util.ColumnDefinition;
import net.tourbook.util.ColumnManager;
import net.tourbook.util.ITourViewer;
import net.tourbook.util.PixelConverter;
import net.tourbook.util.PostSelectionProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 *
 */
public class TourSegmenterView extends ViewPart implements ITourViewer {

	private static final int								SEGMENTER_REQUIRES_ALTITUDE			= 0x01;
	private static final int								SEGMENTER_REQUIRES_DISTANCE			= 0x02;
	private static final int								SEGMENTER_REQUIRES_PULSE			= 0x04;
	private static final int								SEGMENTER_REQUIRES_MARKER			= 0x08;

	private static final int								MAX_DISTANCE_SCALE_MILE				= 80;
	private static final int								MAX_DISTANCE_SCALE_METRIC			= 100;

	public static final String								ID									= "net.tourbook.views.TourSegmenter";	//$NON-NLS-1$

	private static final String								STATE_SELECTED_SEGMENTER_INDEX		= "selectedSegmenterIndex";			//$NON-NLS-1$
	private static final String								STATE_SELECTED_MIN_ALTITUDE_INDEX	= "selectedMinAltitude";				//$NON-NLS-1$
	private static final String								STATE_SELECTED_DISTANCE				= "selectedDistance";					//$NON-NLS-1$
	private static final int								COLUMN_DEFAULT						= 0;									// sort by time
	private static final int								COLUMN_SPEED						= 10;
	private static final int								COLUMN_PACE							= 20;
	private static final int								COLUMN_GRADIENT						= 30;
	private static final int								COLUMN_PULSE						= 40;

	private final IPreferenceStore							_prefStore							= TourbookPlugin
																										.getDefault()
																										.getPreferenceStore();
	private final IDialogSettings							_state								= TourbookPlugin
																										.getDefault()
																										.getDialogSettingsSection(
																												ID);
	private ColumnManager									_columnManager;

	private TourData										_tourData;

	private int												_dpTolerance;
	private int												_savedDpTolerance					= -1;

	private PostSelectionProvider							_postSelectionProvider;

	private ISelectionListener								_postSelectionListener;
	private IPartListener2									_partListener;
	private IPropertyChangeListener							_prefChangeListener;
	private ITourEventListener								_tourEventListener;

	private final NumberFormat								_nf_0_0								= NumberFormat
																										.getNumberInstance();
	private final NumberFormat								_nf_1_0								= NumberFormat
																										.getNumberInstance();
	private final NumberFormat								_nf_1_1								= NumberFormat
																										.getNumberInstance();
	private final NumberFormat								_nf_3_3								= NumberFormat
																										.getNumberInstance();
	{
		_nf_0_0.setMinimumFractionDigits(0);
		_nf_0_0.setMaximumFractionDigits(0);

		_nf_1_0.setMinimumFractionDigits(1);
		_nf_1_0.setMaximumFractionDigits(0);

		_nf_1_1.setMinimumFractionDigits(1);
		_nf_1_1.setMaximumFractionDigits(1);

		_nf_3_3.setMinimumFractionDigits(3);
		_nf_3_3.setMaximumFractionDigits(3);
	}

	private int												_maxDistanceScale;
	private int												_scaleDistancePage;

	private boolean											_isShowSegmentsInChart;
	private boolean											_isTourDirty						= false;
	private boolean											_isSaving;

	/**
	 * when <code>true</code>, the tour dirty flag is disabled to load data into the fields
	 */
	private boolean											_isDirtyDisabled					= false;

	private boolean											_isClearView;
	private int												_altitudeUp;
	private int												_altitudeDown;

	/**
	 * contains all segmenters
	 */
	private static HashMap<SegmenterType, TourSegmenter>	_allTourSegmenter;

	private ArrayList<TourSegmenter>						_availableSegmenter					= new ArrayList<TourSegmenter>();

	/**
	 * segmenter type which the user has selected
	 */
	private SegmenterType									_userSelectedSegmenterType;

	/*
	 * UI controls
	 */

	private PageBook										_pageBook;
	private Composite										_pageSegmenter;
	private Label											_pageNoData;

	private PageBook										_pageBookSegmenter;
	private Composite										_pageSegTypeDP;
	private Composite										_pageSegTypeByMarker;
	private Composite										_pageSegTypeByDistance;
	private Composite										_pageSegTypeByComputedAltiUpDown;

	private TableViewer										_segmentViewer;

	private Scale											_scaleDistance;
	private Label											_lblDistanceValue;

	private Composite										_viewerContainer;

	private Scale											_scaleTolerance;
	private Label											_lblToleranceValue;
	private ImageComboLabel									_lblTitle;
	private Combo											_cboSegmenterType;
	private Label											_lblAltitudeUp;

	private Button											_btnSaveTour;
	private Combo											_cboMinAltitude;
	private Label											_lblMinAltitude;

	private ActionShowSegments								_actionShowSegments;

	/**
	 * {@link TourChart} contains the chart for the tour, this is necessary to move the slider in
	 * the chart to a selected segment
	 */
	private TourChart										_tourChart;

	private class ActionShowSegments extends Action {

		public ActionShowSegments() {

			super(Messages.App_Action_open_tour_segmenter, SWT.TOGGLE);
			setToolTipText(Messages.App_Action_open_tour_segmenter_tooltip);

			setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__tour_segmenter));
		}

		@Override
		public void run() {
			_isShowSegmentsInChart = !_isShowSegmentsInChart;
			fireSegmentLayerChanged();
		}
	}

	public static enum SegmenterType {
		ByAltitudeWithDP, //
		ByPulseWithDP, //
		ByMarker, //
		ByDistance, //
		ByComputedAltiUpDown
	}

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap
	 * existing objects in adapters or simply return objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore it and always show the same content (like Task
	 * List, for example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {

		public ViewContentProvider() {}

		public void dispose() {}

		public Object[] getElements(final Object parent) {
			if (_tourData == null) {
				return new Object[0];
			} else {

				final Object[] tourSegments = _tourData.createTourSegments();

				updateUIAltitude();

				return tourSegments;
			}
		}

		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {}
	}

	private static class ViewSorter extends ViewerSorter {

		// private static final int ASCENDING = 0;

		private static final int	DESCENDING	= 1;

		private int					column;

		private int					direction;

		/**
		 * Compares the object for sorting
		 */
		@Override
		public int compare(final Viewer viewer, final Object obj1, final Object obj2) {

			final TourSegment segment1 = ((TourSegment) obj1);
			final TourSegment segment2 = ((TourSegment) obj2);

			int rc = 0;

			// Determine which column and do the appropriate sort
			switch (column) {
			case COLUMN_DEFAULT:
				rc = segment1.serieIndexStart - segment2.serieIndexStart;
				if (direction == DESCENDING) {
					rc = -rc;
				}
				break;

			case COLUMN_SPEED:
				rc = (int) ((segment1.speed - segment2.speed) * 100);
				break;

			case COLUMN_PACE:
				rc = ((segment1.pace - segment2.pace) * 100);
				break;

			case COLUMN_PULSE:
				rc = segment1.pulse - segment2.pulse;
				break;

			case COLUMN_GRADIENT:
				rc = (int) ((segment1.gradient - segment2.gradient) * 100);
				break;
			}

			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}

			return rc;
		}

		/**
		 * Does the sort. If it's a different column from the previous sort, do an ascending sort.
		 * If it's the same column as the last sort, toggle the sort direction.
		 * 
		 * @param column
		 */
		public void setSortColumn(final int column) {

			if (column == this.column) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an descending sort
				this.column = column;
				direction = DESCENDING;
			}
		}
	}

	/**
	 * Constructor
	 */
	public TourSegmenterView() {
		super();
	}

	private void addPartListener() {

		// set the part listener
		_partListener = new IPartListener2() {
			public void partActivated(final IWorkbenchPartReference partRef) {}

			public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

			public void partClosed(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourSegmenterView.this) {
					saveTour();
					saveState();
					hideTourSegmentsInChart();
				}
			}

			public void partDeactivated(final IWorkbenchPartReference partRef) {}

			public void partHidden(final IWorkbenchPartReference partRef) {}

			public void partInputChanged(final IWorkbenchPartReference partRef) {}

			public void partOpened(final IWorkbenchPartReference partRef) {}

			public void partVisible(final IWorkbenchPartReference partRef) {}
		};

		getSite().getPage().addPartListener(_partListener);
	}

	private void addPrefListener() {

		_prefChangeListener = new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {

				final String property = event.getProperty();

				if (property.equals(ITourbookPreferences.MEASUREMENT_SYSTEM)) {

					// measurement system has changed

					UI.updateUnits();

					/*
					 * update viewer
					 */
					_columnManager.saveState(_state);
					_columnManager.clearColumns();
					defineAllColumns(_viewerContainer);

					recreateViewer(null);

					/*
					 * update distance scale
					 */
					setMaxDistanceScale();
					_scaleDistance.setMaximum(_maxDistanceScale);
					_scaleDistance.setPageIncrement(_scaleDistancePage);
					updateUIDistance();

					/*
					 * update min altitude
					 */
					_lblMinAltitude.setText(UI.UNIT_LABEL_DISTANCE);
					_lblMinAltitude.pack(true);
					updateUIMinAltitude();

					createSegments();

				} else if (property.equals(ITourbookPreferences.VIEW_LAYOUT_CHANGED)) {

					_segmentViewer.getTable().setLinesVisible(
							_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

					_segmentViewer.refresh();

					/*
					 * the tree must be redrawn because the styled text does not show with the new
					 * color
					 */
					_segmentViewer.getTable().redraw();
				}
			}
		};

		_prefStore.addPropertyChangeListener(_prefChangeListener);
	}

	private void addSelectionListener() {

		_postSelectionListener = new ISelectionListener() {
			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {

				if (part == TourSegmenterView.this) {
					return;
				}

				onSelectionChanged(selection);
			}
		};

		getSite().getPage().addPostSelectionListener(_postSelectionListener);
	}

	private void addTourEventListener() {

		_tourEventListener = new ITourEventListener() {
			public void tourChanged(final IWorkbenchPart part, final TourEventId eventId, final Object eventData) {

				if (_tourData == null || part == TourSegmenterView.this) {
					return;
				}

				if (eventId == TourEventId.TOUR_CHANGED && eventData instanceof TourEvent) {

					final TourEvent tourEvent = (TourEvent) eventData;
					final ArrayList<TourData> modifiedTours = tourEvent.getModifiedTours();

					if (modifiedTours == null || modifiedTours.size() == 0) {
						return;
					}

					final TourData modifiedTourData = modifiedTours.get(0);
					final long viewTourId = _tourData.getTourId();

					if (modifiedTourData.getTourId() == viewTourId) {

						// update existing tour

						if (checkDataValidation(modifiedTourData)) {

							if (tourEvent.isReverted) {

								/*
								 * tour is reverted, saving existing tour is not necessary, just
								 * update the tour
								 */
								setTour(modifiedTourData, true);

							} else {

								createSegments();
								reloadViewer();
							}
						}

					} else {

						// display new tour

						onSelectionChanged(new SelectionTourData(null, modifiedTourData));
					}

					// removed old tour data from the selection provider
					_postSelectionProvider.clearSelection();

				} else if (eventId == TourEventId.CLEAR_DISPLAYED_TOUR) {

					clearView();

				} else if (eventId == TourEventId.UPDATE_UI) {

					// check if a tour must be updated

					if (_tourData == null) {
						return;
					}

					final Long tourId = _tourData.getTourId();

					// update ui
					if (UI.containsTourId(eventData, tourId) != null) {

						setTour(TourManager.getInstance().getTourData(tourId), true);
					}
				}
			}
		};

		TourManager.getInstance().addTourEventListener(_tourEventListener);
	}

	/**
	 * check if data for the segmenter is valid
	 */
	private boolean checkDataValidation(final TourData tourData) {

		/*
		 * tourdata and time serie are necessary to create any segment
		 */
		if (tourData == null || tourData.timeSerie == null || tourData.timeSerie.length < 2) {

			clearView();

			return false;
		}

		if (checkSegmenterData(tourData) == 0) {

			clearView();

			return false;
		}

		_pageBook.showPage(_pageSegmenter);

		return true;
	}

	private int checkSegmenterData(final TourData tourData) {

		final int[] altitudeSerie = tourData.altitudeSerie;
		final int[] metricDistanceSerie = tourData.getMetricDistanceSerie();
		final int[] pulseSerie = tourData.pulseSerie;
		final Set<TourMarker> markerSerie = tourData.getTourMarkers();

		int checkedSegmenterData = 0;

		checkedSegmenterData |= altitudeSerie != null && altitudeSerie.length > 1 ? //
				SEGMENTER_REQUIRES_ALTITUDE
				: 0;

		checkedSegmenterData |= metricDistanceSerie != null && metricDistanceSerie.length > 1
				? SEGMENTER_REQUIRES_DISTANCE
				: 0;

		checkedSegmenterData |= pulseSerie != null && pulseSerie.length > 1 ? //
				SEGMENTER_REQUIRES_PULSE
				: 0;

		checkedSegmenterData |= markerSerie != null && markerSerie.size() > 0 ? //
				SEGMENTER_REQUIRES_MARKER
				: 0;

		return checkedSegmenterData;
	}

	private void clearView() {

		_pageBook.showPage(_pageNoData);

		_tourData = null;
		_tourChart = null;

		_isClearView = true;
	}

	private void createActions() {

		_actionShowSegments = new ActionShowSegments();
		final ActionModifyColumns actionModifyColumns = new ActionModifyColumns(this);

		/*
		 * fill view menu
		 */
		final IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		menuMgr.add(actionModifyColumns);

		/*
		 * fill view toolbar
		 */
		final IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(_actionShowSegments);
	}

	private void createAllTourSegmenter() {

		if (_allTourSegmenter != null) {
			// segmenter are already created
			return;
		}

		_allTourSegmenter = new HashMap<SegmenterType, TourSegmenter>();

		_allTourSegmenter.put(SegmenterType.ByAltitudeWithDP, //
				new TourSegmenter(
						SegmenterType.ByAltitudeWithDP,
						Messages.tour_segmenter_type_byAltitude,
						SEGMENTER_REQUIRES_ALTITUDE | SEGMENTER_REQUIRES_DISTANCE));

		_allTourSegmenter.put(SegmenterType.ByDistance, //
				new TourSegmenter(SegmenterType.ByDistance, //
						Messages.tour_segmenter_type_byDistance, //
						SEGMENTER_REQUIRES_DISTANCE));

		_allTourSegmenter.put(SegmenterType.ByComputedAltiUpDown, //
				new TourSegmenter(
						SegmenterType.ByComputedAltiUpDown,
						Messages.tour_segmenter_type_byComputedAltiUpDown,
						SEGMENTER_REQUIRES_ALTITUDE));

		_allTourSegmenter.put(SegmenterType.ByPulseWithDP, //
				new TourSegmenter(
						SegmenterType.ByPulseWithDP,
						Messages.tour_segmenter_type_byPulse,
						SEGMENTER_REQUIRES_PULSE));

		_allTourSegmenter.put(SegmenterType.ByMarker, //
				new TourSegmenter(
						SegmenterType.ByMarker,
						Messages.tour_segmenter_type_byMarker,
						SEGMENTER_REQUIRES_MARKER));
	}

	@Override
	public void createPartControl(final Composite parent) {

		createAllTourSegmenter();
		setMaxDistanceScale();

		// define all columns
		_columnManager = new ColumnManager(this, _state);
		defineAllColumns(parent);

		createUI(parent);
		createActions();

		addSelectionListener();
		addPartListener();
		addPrefListener();
		addTourEventListener();

		// tell the site that this view is a selection provider
		getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider());

		// set default value, show segments in opened charts
		_isShowSegmentsInChart = true;
		_actionShowSegments.setChecked(_isShowSegmentsInChart);

		restoreState();

		// update viewer with current selection
		onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

		// when previous onSelectionChanged did not display a tour, get tour from tour manager
		if (_tourData == null) {
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {

					final ArrayList<TourData> selectedTours = TourManager.getSelectedTours();

					if (selectedTours != null && selectedTours.size() > 0) {
						onSelectionChanged(new SelectionTourData(null, selectedTours.get(0)));
					}
				}
			});
		}

	}

	/**
	 * create points for the simplifier from distance and altitude
	 */
	private void createSegments() {

		if (_tourData == null) {
			_pageBook.showPage(_pageNoData);
			return;
		}

		// disable computed altitude
		_tourData.segmentSerieComputedAltitudeDiff = null;

		final TourSegmenter selectedSegmenter = getSelectedSegmenter();
		if (selectedSegmenter == null) {
			clearView();
			return;
		}

		final SegmenterType selectedSegmenterType = selectedSegmenter.segmenterType;

		if (selectedSegmenterType == SegmenterType.ByAltitudeWithDP) {

			createSegmentsByAltitudeWithDP();

		} else if (selectedSegmenterType == SegmenterType.ByPulseWithDP) {

			createSegmentsByPulseWithDP();

		} else if (selectedSegmenterType == SegmenterType.ByDistance) {

			createSegmentsByDistance();

		} else if (selectedSegmenterType == SegmenterType.ByMarker) {

			createSegmentsByMarker();

		} else if (selectedSegmenterType == SegmenterType.ByComputedAltiUpDown) {

			createSegmentsByAltiUpDown();
		}

		// update table and create the tour segments in tour data
		reloadViewer();

		fireSegmentLayerChanged();
	}

	/**
	 * create Douglas-Peucker segments from distance and altitude
	 */
	private void createSegmentsByAltitudeWithDP() {

		final int[] distanceSerie = _tourData.getMetricDistanceSerie();
		final int[] altitudeSerie = _tourData.altitudeSerie;

		// convert data series into points
		final DPPoint graphPoints[] = new DPPoint[distanceSerie.length];
		for (int serieIndex = 0; serieIndex < graphPoints.length; serieIndex++) {
			graphPoints[serieIndex] = new DPPoint(distanceSerie[serieIndex], altitudeSerie[serieIndex], serieIndex);
		}

		final Object[] simplePoints = new DouglasPeuckerSimplifier(_dpTolerance, graphPoints).simplify();

		/*
		 * copie the data index for the simplified points into the tour data
		 */

		final int[] segmentSerieIndex = _tourData.segmentSerieIndex = new int[simplePoints.length];

		for (int iPoint = 0; iPoint < simplePoints.length; iPoint++) {
			final DPPoint point = (DPPoint) simplePoints[iPoint];
			segmentSerieIndex[iPoint] = point.serieIndex;
		}
	}

	private void createSegmentsByAltiUpDown() {

		final int selectedMinAltiDiff = PrefPageComputedValues.ALTITUDE_MINIMUM[_cboMinAltitude.getSelectionIndex()];
		final ArrayList<AltitudeUpDownSegment> tourSegements = new ArrayList<AltitudeUpDownSegment>();

		// create segment when the altitude up/down is changing
		_tourData.computeAltitudeUpDown(tourSegements, selectedMinAltiDiff);

		// convert segment list into array
		int serieIndex = 0;
		final int segmentLength = tourSegements.size();
		final int[] segmentSerieIndex = _tourData.segmentSerieIndex = new int[segmentLength];
		final int[] altitudeDiff = _tourData.segmentSerieComputedAltitudeDiff = new int[segmentLength];

		for (final AltitudeUpDownSegment altitudeUpDownSegment : tourSegements) {

			segmentSerieIndex[serieIndex] = altitudeUpDownSegment.serieIndex;
			altitudeDiff[serieIndex] = altitudeUpDownSegment.computedAltitudeDiff;

			serieIndex++;
		}
	}

	private void createSegmentsByDistance() {

		final int[] distanceSerie = _tourData.getMetricDistanceSerie();
		final int lastDistanceSerieIndex = distanceSerie.length - 1;

		final float segmentDistance = getDistance();
		final ArrayList<Integer> segmentSerieIndex = new ArrayList<Integer>();

		// set first segment start
		segmentSerieIndex.add(0);

		float nextSegmentDistance = segmentDistance;

		for (int distanceIndex = 0; distanceIndex < distanceSerie.length; distanceIndex++) {
			final int distance = distanceSerie[distanceIndex];

			if (distance >= nextSegmentDistance) {

				segmentSerieIndex.add(distanceIndex);

				// set minimum distance for the next segment
				nextSegmentDistance += segmentDistance;
			}
		}

		final int serieSize = segmentSerieIndex.size();

		// ensure the last segment ends at the end of the tour
		if (serieSize == 1 || //

				// ensure the last index is not duplicated
				segmentSerieIndex.get(serieSize - 1) != lastDistanceSerieIndex) {

			segmentSerieIndex.add(lastDistanceSerieIndex);
		}

		_tourData.segmentSerieIndex = ArrayListToArray.toInt(segmentSerieIndex);
	}

	private void createSegmentsByMarker() {

		final int[] timeSerie = _tourData.timeSerie;
		final Set<TourMarker> tourMarkers = _tourData.getTourMarkers();

		// sort markers by time - they are unsorted
		final ArrayList<TourMarker> markerList = new ArrayList<TourMarker>(tourMarkers);
		Collections.sort(markerList, new Comparator<TourMarker>() {
			public int compare(final TourMarker tm1, final TourMarker tm2) {
				return tm1.getSerieIndex() - tm2.getSerieIndex();
			}
		});

		final ArrayList<Integer> segmentSerieIndex = new ArrayList<Integer>();

		// set first segment at tour start
		segmentSerieIndex.add(0);

		// create segment for each marker
		for (final TourMarker tourMarker : markerList) {
			segmentSerieIndex.add(tourMarker.getSerieIndex());
		}

		// add segment end at the tour end
		segmentSerieIndex.add(timeSerie.length - 1);

		_tourData.segmentSerieIndex = ArrayListToArray.toInt(segmentSerieIndex);
	}

	/**
	 * create Douglas-Peucker segments from time and pulse
	 */
	private void createSegmentsByPulseWithDP() {

		final int[] timeSerie = _tourData.timeSerie;
		final int[] pulseSerie = _tourData.pulseSerie;

		if (pulseSerie == null || pulseSerie.length < 2) {
			_tourData.segmentSerieIndex = null;
			return;
		}

		// convert data series into points
		final DPPoint graphPoints[] = new DPPoint[timeSerie.length];
		for (int serieIndex = 0; serieIndex < graphPoints.length; serieIndex++) {
			graphPoints[serieIndex] = new DPPoint(timeSerie[serieIndex], pulseSerie[serieIndex], serieIndex);
		}

		final DouglasPeuckerSimplifier dpSimplifier = new DouglasPeuckerSimplifier(_dpTolerance, graphPoints);
		final Object[] simplePoints = dpSimplifier.simplify();

		/*
		 * copie the data index for the simplified points into the tour data
		 */
		final int[] segmentSerieIndex = _tourData.segmentSerieIndex = new int[simplePoints.length];

		for (int iPoint = 0; iPoint < simplePoints.length; iPoint++) {
			final DPPoint point = (DPPoint) simplePoints[iPoint];
			segmentSerieIndex[iPoint] = point.serieIndex;
		}
	}

	private void createSegmentViewer(final Composite parent) {

		final Table table = new Table(parent, //
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI /* | SWT.BORDER */);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		_segmentViewer = new TableViewer(table);
		_columnManager.createColumns(_segmentViewer);

		_segmentViewer.setContentProvider(new ViewContentProvider());
		_segmentViewer.setSorter(new ViewSorter());

		_segmentViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {

				final StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection != null) {

					/*
					 * select the chart sliders according to the selected segment(s)
					 */

					final Object[] segments = selection.toArray();

					if (segments.length > 0) {

						if (_tourChart == null) {
							_tourChart = getActiveTourChart(_tourData);
						}

						final SelectionChartXSliderPosition selectionSliderPosition = //
						new SelectionChartXSliderPosition(
								_tourChart,
								((TourSegment) (segments[0])).serieIndexStart,
								((TourSegment) (segments[segments.length - 1])).serieIndexEnd);

						_postSelectionProvider.setSelection(selectionSliderPosition);
					}
				}
			}
		});
	}

	private void createUI(final Composite parent) {

		_pageBook = new PageBook(parent, SWT.NONE);

		_pageNoData = new Label(_pageBook, SWT.WRAP);
		_pageNoData.setText(Messages.Tour_Segmenter_Label_no_chart);

		_pageSegmenter = new Composite(_pageBook, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(_pageSegmenter);

		_pageBook.showPage(_pageNoData);

		createUIHeader(_pageSegmenter);
		createUIViewer(_pageSegmenter);
	}

	private void createUIHeader(final Composite parent) {

		final PixelConverter pc = new PixelConverter(parent);

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(3, 3, 3, 2).applyTo(container);
//		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		{
			// tour title
			_lblTitle = new ImageComboLabel(container, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(_lblTitle);

			// label: create segments with
			final Label label = new Label(container, SWT.NONE);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
			label.setText(Messages.tour_segmenter_label_createSegmentsWith);

			// combo: segmenter type
			_cboSegmenterType = new Combo(container, SWT.READ_ONLY);
			_cboSegmenterType.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					onSelectSegmenterType(true);
				}
			});
			/*
			 * fill the segmenter list that the next layout shows the combo that all segmenter names
			 * are visible
			 */
			for (final TourSegmenter segmenter : _allTourSegmenter.values()) {
				_cboSegmenterType.add(segmenter.name);
			}
			final Point size = _cboSegmenterType.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			GridDataFactory.fillDefaults().hint(size).applyTo(_cboSegmenterType);

			// tour/computed altitude
			final Composite altitudeContainer = new Composite(container, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(altitudeContainer);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(altitudeContainer);
			{
				// label: computed altitude up
				_lblAltitudeUp = new Label(altitudeContainer, SWT.TRAIL);
				GridDataFactory.fillDefaults()//
						.align(SWT.END, SWT.CENTER)
						.grab(true, false)
						.hint(pc.convertWidthInCharsToPixels(18), SWT.DEFAULT)
						.applyTo(_lblAltitudeUp);
				_lblAltitudeUp.setToolTipText(Messages.tour_segmenter_label_tourAltitude_tooltip);

				// button: update tour
				_btnSaveTour = new Button(altitudeContainer, SWT.NONE);
				GridDataFactory.fillDefaults().indent(5, 0).applyTo(_btnSaveTour);
				_btnSaveTour.setText(Messages.tour_segmenter_button_updateAltitude);
				_btnSaveTour.setToolTipText(Messages.tour_segmenter_button_updateAltitude_tooltip);
				_btnSaveTour.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						onSaveTourAltitude();
					}
				});
			}

			// pagebook: segmenter type
			_pageBookSegmenter = new PageBook(container, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(_pageBookSegmenter);
			{
				_pageSegTypeDP = createUISegmenterDP(_pageBookSegmenter);
				_pageSegTypeByMarker = createUISegmenterByMarker(_pageBookSegmenter);
				_pageSegTypeByDistance = createUISegmenterByDistance(_pageBookSegmenter);
				_pageSegTypeByComputedAltiUpDown = createUISegmenterByAltiUpDown(_pageBookSegmenter);
			}
		}

	}

	private Composite createUISegmenterByAltiUpDown(final Composite parent) {

//		final PixelConverter pc = new PixelConverter(parent);

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
		{
			// label: min alti diff
			final Label label = new Label(container, SWT.NONE);
			label.setText(Messages.tour_segmenter_segType_byUpDownAlti_label);

			// combo: min altitude
			_cboMinAltitude = new Combo(container, SWT.READ_ONLY);
			_cboMinAltitude.setVisibleItemCount(20);
			_cboMinAltitude.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					createSegments();
				}
			});

			// label: unit
			_lblMinAltitude = new Label(container, SWT.NONE);
			_lblMinAltitude.setText(UI.UNIT_LABEL_ALTITUDE);
		}

		return container;
	}

	private Composite createUISegmenterByDistance(final Composite parent) {

		final PixelConverter pc = new PixelConverter(parent);

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
		{

			// label: distance
			final Label label = new Label(container, SWT.NONE);
			label.setText(Messages.tour_segmenter_segType_byDistance_label);

			// scale: distance
			_scaleDistance = new Scale(container, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(_scaleDistance);
			_scaleDistance.setMaximum(_maxDistanceScale);
			_scaleDistance.setPageIncrement(_scaleDistancePage);
			_scaleDistance.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					onChangedDistance();
				}
			});

			// text: distance value
			_lblDistanceValue = new Label(container, SWT.TRAIL);
			_lblDistanceValue.setText(Messages.tour_segmenter_segType_byDistance_defaultDistance);
			GridDataFactory.fillDefaults()//
					.align(SWT.FILL, SWT.CENTER)
					.hint(pc.convertWidthInCharsToPixels(8), SWT.DEFAULT)
					.applyTo(_lblDistanceValue);
		}

		return container;
	}

	private Composite createUISegmenterByMarker(final Composite parent) {

		/*
		 * display NONE, this is not easy to do - or I didn't find an easier way
		 */
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().applyTo(container);
		{
			final Canvas canvas = new Canvas(container, SWT.NONE);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).hint(1, 1).applyTo(canvas);
		}

		return container;
	}

	private Composite createUISegmenterDP(final Composite parent) {

		final PixelConverter pc = new PixelConverter(parent);

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
		{

			// label: tolerance
			final Label label = new Label(container, SWT.NONE);
			label.setText(Messages.Tour_Segmenter_Label_tolerance);

			// scale: tolerance
			_scaleTolerance = new Scale(container, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(_scaleTolerance);
			_scaleTolerance.setMaximum(100);
			_scaleTolerance.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					onChangedTolerance(getDPTolerance());
					setTourDirty();
				}
			});

			// text: tolerance value
			_lblToleranceValue = new Label(container, SWT.TRAIL);
			_lblToleranceValue.setText(Messages.Tour_Segmenter_Label_default_tolerance);
			GridDataFactory
					.fillDefaults()
					.align(SWT.FILL, SWT.CENTER)
					.hint(pc.convertWidthInCharsToPixels(4), SWT.DEFAULT)
					.applyTo(_lblToleranceValue);
		}

		return container;
	}

	private void createUIViewer(final Composite parent) {

		_viewerContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(_viewerContainer);
		GridLayoutFactory.fillDefaults().applyTo(_viewerContainer);

		createSegmentViewer(_viewerContainer);
	}

	private void defineAllColumns(final Composite parent) {

		final PixelConverter pc = new PixelConverter(parent);

		final SelectionAdapter defaultListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				((ViewSorter) _segmentViewer.getSorter()).setSortColumn(COLUMN_DEFAULT);
				_segmentViewer.refresh();
			}
		};

		defineColumnRecordingTimeTotal(pc, defaultListener);

		defineColumnDistanceTotal(pc, defaultListener);
		defineColumnDistance(pc, defaultListener);

		defineColumnRecordingTime(pc, defaultListener);
		defineColumnDrivingTime(pc, defaultListener);
		defineColumnPausedTime(pc, defaultListener);

		defineColumnAltitudeDiffSegmentBorder(pc, defaultListener);
		defineColumnAltitudeDiffSegmentComputed(pc, defaultListener);
		defineColumnAltitudeUpSummarizedComputed(pc, defaultListener);
		defineColumnAltitudeDownSummarizedComputed(pc, defaultListener);

		defineColumnGradient(pc);

		defineColumnAltitudeUpHour(pc, defaultListener);
		defineColumnAltitudeDownHour(pc, defaultListener);

		defineColumnAvgSpeed(pc);
		defineColumnAvgPace(pc);
		defineColumnAvgPaceDifference(pc);
		defineColumnAvgPulse(pc);
		defineColumnAvgPulseDifference(pc);

		defineColumnAltitudeUpSummarizedBorder(pc, defaultListener);
		defineColumnAltitudeDownSummarizedBorder(pc, defaultListener);
	}

	/**
	 * column: altitude diff segment border (m/ft)
	 */
	private void defineColumnAltitudeDiffSegmentBorder(	final PixelConverter pc,
														final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_DIFF_SEGMENT_BORDER.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				final int altitudeDiff = segment.altitudeDiffSegmentBorder;

				cell.setText(_nf_0_0.format(altitudeDiff / UI.UNIT_VALUE_ALTITUDE));
				setCellColor(cell, altitudeDiff);
			}
		});
	}

	/**
	 * column: computed altitude diff (m/ft)
	 */
	private void defineColumnAltitudeDiffSegmentComputed(	final PixelConverter pc,
															final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_DIFF_SEGMENT_COMPUTED.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				final int altitudeDiff = segment.altitudeDiffSegmentComputed;

				cell.setText(_nf_0_0.format(altitudeDiff / UI.UNIT_VALUE_ALTITUDE));
				setCellColor(cell, altitudeDiff);
			}
		});
	}

	/**
	 * column: altitude down m/h
	 */
	private void defineColumnAltitudeDownHour(	final PixelConverter pc,
												final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_DOWN_H.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();
				if (segment.drivingTime == 0) {
					cell.setText(UI.EMPTY_STRING);
				} else {
					final float result = (segment.altitudeDownHour / UI.UNIT_VALUE_ALTITUDE)
							/ segment.drivingTime
							* 3600;
					if (result == 0) {
						cell.setText(UI.EMPTY_STRING);
					} else {
						cell.setText(_nf_1_0.format(result));
					}
				}
			}
		});
	}

	/**
	 * column: total altitude down (m/ft)
	 */
	private void defineColumnAltitudeDownSummarizedBorder(	final PixelConverter pc,
															final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_DOWN_SUMMARIZED_BORDER.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_0_0.format(segment.altitudeDownSummarizedBorder / UI.UNIT_VALUE_ALTITUDE));
			}
		});
	}

	/**
	 * column: total altitude down (m/ft)
	 */
	private void defineColumnAltitudeDownSummarizedComputed(final PixelConverter pc,
															final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_DOWN_SUMMARIZED_COMPUTED.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_0_0.format(segment.altitudeDownSummarizedComputed / UI.UNIT_VALUE_ALTITUDE));
			}
		});
	}

	/**
	 * column: altitude up m/h
	 */
	private void defineColumnAltitudeUpHour(final PixelConverter pc,
											final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_UP_H.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();
				if (segment.drivingTime == 0) {
					cell.setText(UI.EMPTY_STRING);
				} else {
					final float result = (segment.altitudeUpHour / UI.UNIT_VALUE_ALTITUDE) / segment.drivingTime * 3600;
					if (result == 0) {
						cell.setText(UI.EMPTY_STRING);
					} else {
						cell.setText(_nf_1_0.format(result));
					}
				}
			}
		});
	}

	/**
	 * column: total altitude up (m/ft)
	 */
	private void defineColumnAltitudeUpSummarizedBorder(final PixelConverter pc,
														final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_UP_SUMMARIZED_BORDER.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_0_0.format(segment.altitudeUpSummarizedBorder / UI.UNIT_VALUE_ALTITUDE));
			}
		});
	}

	/**
	 * column: total altitude up (m/ft)
	 */
	private void defineColumnAltitudeUpSummarizedComputed(	final PixelConverter pc,
															final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.ALTITUDE_UP_SUMMARIZED_COMPUTED.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_0_0.format(segment.altitudeUpSummarizedComputed / UI.UNIT_VALUE_ALTITUDE));
			}
		});
	}

	/**
	 * column: average pace
	 */
	private void defineColumnAvgPace(final PixelConverter pc) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.AVG_PACE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				((ViewSorter) _segmentViewer.getSorter()).setSortColumn(COLUMN_SPEED);
				_segmentViewer.refresh();
			}
		});
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();
				cell.setText(UI.format_mm_ss(segment.pace));
			}
		});
	}

	/**
	 * column: pace difference
	 */
	private void defineColumnAvgPaceDifference(final PixelConverter pc) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.AVG_PACE_DIFFERENCE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();
				cell.setText(UI.format_mm_ss(segment.paceDiff));
			}
		});
	}

	/**
	 * column: average pulse
	 */
	private void defineColumnAvgPulse(final PixelConverter pc) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.AVG_PULSE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				((ViewSorter) _segmentViewer.getSorter()).setSortColumn(COLUMN_PULSE);
				_segmentViewer.refresh();
			}
		});
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final int pulse = ((TourSegment) cell.getElement()).pulse;

				if (pulse == 0) {
					cell.setText(UI.EMPTY_STRING);
				} else {
					cell.setText(Integer.toString(pulse));
				}
			}
		});
	}

	/**
	 * column: pulse difference
	 */
	private void defineColumnAvgPulseDifference(final PixelConverter pc) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.AVG_PULSE_DIFFERENCE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final int pulseDiff = ((TourSegment) cell.getElement()).pulseDiff;

				if (pulseDiff == Integer.MIN_VALUE) {
					cell.setText(UI.EMPTY_STRING);
				} else if (pulseDiff == 0) {
					cell.setText(UI.DASH);
				} else {
					cell.setText(Integer.toString(pulseDiff));
				}
			}
		});
	}

	/**
	 * column: average speed
	 */
	private void defineColumnAvgSpeed(final PixelConverter pc) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.AVG_SPEED.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				((ViewSorter) _segmentViewer.getSorter()).setSortColumn(COLUMN_SPEED);
				_segmentViewer.refresh();
			}
		});
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();
				cell.setText(_nf_1_1.format(segment.speed));
			}
		});
	}

	/**
	 * column: distance (km/mile)
	 */
	private void defineColumnDistance(final PixelConverter pc, final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.DISTANCE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_3_3.format((segment.distanceDiff) / (1000 * UI.UNIT_VALUE_DISTANCE)));
			}
		});
	}

	/**
	 * column: TOTAL distance (km/mile)
	 */
	private void defineColumnDistanceTotal(	final PixelConverter pc,
											final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.DISTANCE_TOTAL.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_3_3.format((segment.distanceTotal) / (1000 * UI.UNIT_VALUE_DISTANCE)));
			}
		});
	}

	/**
	 * column: driving time
	 */
	private void defineColumnDrivingTime(final PixelConverter pc, final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.DRIVING_TIME.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final TourSegment segment = (TourSegment) cell.getElement();
				cell.setText(UI.format_hh_mm_ss(segment.drivingTime));
			}
		});
	}

	/**
	 * column: gradient
	 */
	private void defineColumnGradient(final PixelConverter pc) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.GRADIENT.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				((ViewSorter) _segmentViewer.getSorter()).setSortColumn(COLUMN_GRADIENT);
				_segmentViewer.refresh();
			}
		});
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourSegment segment = (TourSegment) cell.getElement();

				cell.setText(_nf_1_1.format(segment.gradient));
			}
		});
	}

	/**
	 * column: break time
	 */
	private void defineColumnPausedTime(final PixelConverter pc, final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.PAUSED_TIME.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final TourSegment segment = (TourSegment) cell.getElement();
				final int breakTime = segment.breakTime;
				if (breakTime == 0) {
					cell.setText(UI.EMPTY_STRING);
				} else {
					cell.setText(UI.format_hh_mm_ss(breakTime));
				}
			}
		});
	}

	/**
	 * column: recording time
	 */
	private void defineColumnRecordingTime(	final PixelConverter pc,
											final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.RECORDING_TIME.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final TourSegment segment = (TourSegment) cell.getElement();
				cell.setText(UI.format_hh_mm_ss(segment.recordingTime));
			}
		});
	}

	/**
	 * column: TOTAL recording time
	 */
	private void defineColumnRecordingTimeTotal(final PixelConverter pc,
												final SelectionAdapter defaultColumnSelectionListener) {

		final ColumnDefinition colDef;

		colDef = TableColumnFactory.RECORDING_TIME_TOTAL.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.addSelectionListener(defaultColumnSelectionListener);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final TourSegment segment = (TourSegment) cell.getElement();
				cell.setText(UI.format_hh_mm_ss(segment.timeTotal));
			}
		});
	}

	@Override
	public void dispose() {

		final IWorkbenchPage wbPage = getSite().getPage();
		wbPage.removePostSelectionListener(_postSelectionListener);
		wbPage.removePartListener(_partListener);

		_prefStore.removePropertyChangeListener(_prefChangeListener);
		TourManager.getInstance().removeTourEventListener(_tourEventListener);

		super.dispose();
	}

	/**
	 * notify listeners to show/hide the segments
	 */
	private void fireSegmentLayerChanged() {

		// show/hide the segments in the chart
		TourManager.fireEvent(TourEventId.SEGMENT_LAYER_CHANGED, _isShowSegmentsInChart, TourSegmenterView.this);
	}

	/**
	 * try to get the tour chart and/or editor from the active part
	 * 
	 * @param tourData
	 * @return Returns the {@link TourChart} for the requested {@link TourData}
	 */
	private TourChart getActiveTourChart(final TourData tourData) {

		// get tour chart from the active editor part
		for (final IWorkbenchWindow wbWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (final IWorkbenchPage wbPage : wbWindow.getPages()) {

				final IEditorPart activeEditor = wbPage.getActiveEditor();
				if (activeEditor instanceof TourEditor) {

					/*
					 * check if the tour data in the editor is the same
					 */
					final TourChart tourChart = ((TourEditor) activeEditor).getTourChart();
					final TourData tourChartTourData = tourChart.getTourData();
					if (tourChartTourData == tourData) {

						try {
							TourManager.checkTourData(tourData, tourChartTourData);
						} catch (final MyTourbookException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		// get tour chart from the tour chart view
		for (final IWorkbenchWindow wbWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (final IWorkbenchPage wbPage : wbWindow.getPages()) {

				final IViewReference viewRef = wbPage.findViewReference(TourChartView.ID);
				if (viewRef != null) {

					final IViewPart view = viewRef.getView(false);
					if (view instanceof TourChartView) {

						final TourChartView tourChartView = ((TourChartView) view);

						/*
						 * check if the tour data in the tour chart is the same
						 */
						final TourChart tourChart = tourChartView.getTourChart();
						final TourData tourChartTourData = tourChart.getTourData();
						if (tourChartTourData == tourData) {
							try {
								TourManager.checkTourData(tourData, tourChartTourData);
							} catch (final MyTourbookException e) {
								e.printStackTrace();
							}

							return tourChart;
						}
					}
				}
			}
		}

		return null;
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public Object getAdapter(final Class adapter) {
//
//		if (adapter == ColumnViewer.class) {
//			return fSegmentViewer;
//		}
//
//		return Platform.getAdapterManager().getAdapter(this, adapter);
//	}

	public ColumnManager getColumnManager() {
		return _columnManager;
	}

	/**
	 * @return Returns distance in meters from the scale control
	 */
	private float getDistance() {

		final float selectedDistance = _scaleDistance.getSelection();
		float scaleDistance;

		if (UI.UNIT_VALUE_DISTANCE == UI.UNIT_MILE) {

			// miles are displayed

			scaleDistance = (selectedDistance) * 1000 / 8;

			if (scaleDistance == 0) {
				scaleDistance = 1000 / 8;
			}

			// convert mile -> meters
			scaleDistance *= UI.UNIT_MILE;

		} else {

			// meters are displayed

			scaleDistance = selectedDistance * MAX_DISTANCE_SCALE_METRIC;

			// ensure the distance in not below 100m
			if (scaleDistance < 100) {
				scaleDistance = 100;
			}
		}

		return scaleDistance;
	}

	private int getDPTolerance() {
		return (int) ((Math.pow(_scaleTolerance.getSelection(), 2.05)) / 50.0);
	}

	private TourSegmenter getSelectedSegmenter() {

		if (_availableSegmenter.size() == 0) {
			return null;
		}

		final int selectedIndex = _cboSegmenterType.getSelectionIndex();

		if (selectedIndex != -1) {
			return _availableSegmenter.get(selectedIndex);
		}

		// should not happen
		return null;
	}

	public ColumnViewer getViewer() {
		return _segmentViewer;
	}

	/**
	 * hides the tour segments
	 */
	private void hideTourSegmentsInChart() {

		_isShowSegmentsInChart = false;
		_actionShowSegments.setChecked(_isShowSegmentsInChart);

		fireSegmentLayerChanged();
	}

	private void onChangedDistance() {

		updateUIDistance();

		createSegments();
	}

	private void onChangedTolerance(final int dpTolerance/* , final boolean forceRecalc */) {

		// update label in the ui
		_lblToleranceValue.setText(Integer.toString(dpTolerance));

		if (_tourData == null || (_dpTolerance == dpTolerance /* && forceRecalc == false */)) {
			return;
		}

		_dpTolerance = dpTolerance;

		// update tolerance into the tour data
		_tourData.setDpTolerance((short) dpTolerance);

		createSegments();
	}

	private void onSaveTourAltitude() {

		_tourData.setTourAltUp(_altitudeUp);
		_tourData.setTourAltDown(_altitudeDown);

		_isTourDirty = true;

		_tourData = saveTour();
	}

	/**
	 * handle a tour selection event
	 * 
	 * @param selection
	 */
	private void onSelectionChanged(final ISelection selection) {

		_isClearView = false;

		if (_isSaving) {
			return;
		}

		/*
		 * run selection async because a tour could be modified and needs to be saved, modifications
		 * are not reported to the tour data editor, saving needs also to be asynch with the tour
		 * data editor
		 */
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {

				// check if view is disposed
				if (_pageBook.isDisposed() || _isClearView) {
					return;
				}

				TourData nextTourData = null;
				TourChart nextTourChart = null;

				if (selection instanceof SelectionTourData) {

					final SelectionTourData selectionTourData = (SelectionTourData) selection;

					nextTourData = selectionTourData.getTourData();
					nextTourChart = selectionTourData.getTourChart();

				} else if (selection instanceof SelectionTourId) {

					final SelectionTourId tourIdSelection = (SelectionTourId) selection;

					if (_tourData != null) {
						if (_tourData.getTourId().equals(tourIdSelection.getTourId())) {
							// don't reload the same tour
							return;
						}
					}

					nextTourData = TourManager.getInstance().getTourData(tourIdSelection.getTourId());

				} else if (selection instanceof SelectionTourIds) {

					final ArrayList<Long> tourIds = ((SelectionTourIds) selection).getTourIds();
					if (tourIds != null && tourIds.size() > 0) {

						final Long tourId = tourIds.get(0);

						if (_tourData != null) {
							if (_tourData.getTourId().equals(tourId)) {
								// don't reload the same tour
								return;
							}
						}

						nextTourData = TourManager.getInstance().getTourData(tourId);
					}

				} else if (selection instanceof SelectionDeletedTours) {

					clearView();

				} else {
					return;
				}

				if (checkDataValidation(nextTourData) == false) {
					return;
				}

				/*
				 * save previous tour when a new tour is selected
				 */
				if (_tourData != null && _tourData.getTourId() == nextTourData.getTourId()) {

					// nothing to do, it's the same tour

				} else {

					final TourData savedTour = saveTour();
					if (savedTour != null) {

						/*
						 * when a tour is saved, the change notification is not fired because
						 * another tour is already selected, but to update the tour in a TourViewer,
						 * a change nofification must be fired afterwords
						 */
//				Display.getCurrent().asyncExec(new Runnable() {
//					public void run() {
//						TourManager.fireEvent(TourEventId.TOUR_CHANGED,
//								new TourEvent(savedTour),
//								TourSegmenterView.this);
//					}
//				});
					}

					if (nextTourChart == null) {
						nextTourChart = getActiveTourChart(nextTourData);
					}

					_tourChart = nextTourChart;

					setTour(nextTourData, false);
				}
			}
		});
	}

	private void onSelectSegmenterType(final boolean isUserSelected) {

		final TourSegmenter selectedSegmenter = getSelectedSegmenter();
		if (selectedSegmenter == null) {
			clearView();
			return;
		}

		final SegmenterType selectedSegmenterType = selectedSegmenter.segmenterType;

		/*
		 * keep segmenter type which the user selected, try to reselect this segmenter when a new
		 * tour is displayed
		 */
		if (isUserSelected) {
			_userSelectedSegmenterType = selectedSegmenterType;
		}

		if (selectedSegmenterType == SegmenterType.ByAltitudeWithDP || //
				selectedSegmenterType == SegmenterType.ByPulseWithDP) {

			_pageBookSegmenter.showPage(_pageSegTypeDP);

		} else if (selectedSegmenterType == SegmenterType.ByMarker) {

			_pageBookSegmenter.showPage(_pageSegTypeByMarker);

		} else if (selectedSegmenterType == SegmenterType.ByDistance) {

			_pageBookSegmenter.showPage(_pageSegTypeByDistance);

		} else if (selectedSegmenterType == SegmenterType.ByComputedAltiUpDown) {

			_pageBookSegmenter.showPage(_pageSegTypeByComputedAltiUpDown);
		}

		_pageSegmenter.layout();

		createSegments();
	}

	public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

		_viewerContainer.setRedraw(false);
		{
			_segmentViewer.getTable().dispose();

			createSegmentViewer(_viewerContainer);
			_viewerContainer.layout();

			// update the viewer
			reloadViewer();
		}
		_viewerContainer.setRedraw(true);

		return _segmentViewer;
	}

	public void reloadViewer() {
		// force input to be reloaded
		_segmentViewer.setInput(new Object[0]);
	}

	private void restoreState() {

		// selected segmenter
		int segmenterIndex = -1;
		try {
			segmenterIndex = _state.getInt(STATE_SELECTED_SEGMENTER_INDEX);
		} catch (final NumberFormatException e) {}
		if (segmenterIndex < 0) {
			segmenterIndex = 0;
		}
		_cboSegmenterType.select(segmenterIndex);

		// selected distance
		int stateDistance = 10;
		try {
			stateDistance = _state.getInt(STATE_SELECTED_DISTANCE);
		} catch (final NumberFormatException e) {}
		_scaleDistance.setSelection(stateDistance);
		updateUIDistance();

		// selected min altitude
		int minAltitudeIndex = PrefPageComputedValues.DEFAULT_MIN_ALTITUDE_INDEX;
		try {
			minAltitudeIndex = _state.getInt(STATE_SELECTED_MIN_ALTITUDE_INDEX);
		} catch (final NumberFormatException e) {}
		if (minAltitudeIndex < 0) {
			minAltitudeIndex = PrefPageComputedValues.DEFAULT_MIN_ALTITUDE_INDEX;
		}
		if (_cboMinAltitude.getItemCount() == 0) {
			updateUIMinAltitude();
		}

		_cboMinAltitude.select(minAltitudeIndex);
		updateUIMinAltitude();
	}

	private void saveState() {

		_columnManager.saveState(_state);

		_state.put(STATE_SELECTED_SEGMENTER_INDEX, _cboSegmenterType.getSelectionIndex());
		_state.put(STATE_SELECTED_MIN_ALTITUDE_INDEX, _cboMinAltitude.getSelectionIndex());
		_state.put(STATE_SELECTED_DISTANCE, _scaleDistance.getSelection());
	}

	private TourData saveTour() {

		if (_isTourDirty == false || _tourData == null || _savedDpTolerance == -1) {
			// nothing to do
			return null;
		}

		TourData savedTour;
		_isSaving = true;
		{
			savedTour = TourManager.saveModifiedTour(_tourData);
		}
		_isSaving = false;

		_isTourDirty = false;

		return savedTour;
	}

	private void setCellColor(final ViewerCell cell, final int altiDiff) {

		if (altiDiff == 0) {
			cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		} else if (altiDiff > 0) {
			cell.setBackground(JFaceResources.getColorRegistry().get(UI.VIEW_COLOR_BG_SEGMENTER_UP));
		} else if (altiDiff < 0) {
			cell.setBackground(JFaceResources.getColorRegistry().get(UI.VIEW_COLOR_BG_SEGMENTER_DOWN));
		}
	}

	@Override
	public void setFocus() {
		_scaleTolerance.setFocus();
	}

	private void setMaxDistanceScale() {

		if (UI.UNIT_VALUE_DISTANCE == UI.UNIT_MILE) {

			// imperial

			_maxDistanceScale = MAX_DISTANCE_SCALE_MILE;
			_scaleDistancePage = 8;

		} else {

			// metric

			_maxDistanceScale = MAX_DISTANCE_SCALE_METRIC;
			_scaleDistancePage = 10;
		}
	}

	/**
	 * Sets the tour for the segmenter
	 * 
	 * @param tourData
	 * @param forceUpdate
	 */
	private void setTour(final TourData tourData, final boolean forceUpdate) {

		if (tourData == null || (forceUpdate == false && tourData == _tourData)) {
			return;
		}

		_isDirtyDisabled = true;
		{
			_tourData = tourData;

			_pageBook.showPage(_pageSegmenter);

			// update tour title
			_lblTitle.setText(TourManager.getTourTitleDetailed(_tourData));

			// keep original dp tolerance
			_savedDpTolerance = _dpTolerance = _tourData.getDpTolerance();

			// update segmenter values, the factor is defined by experimentals
			final float factor = 1 / 2.05f;
			final double tolerance = Math.pow(_dpTolerance * 50, factor);

			_scaleTolerance.setSelection((int) tolerance);
			_lblToleranceValue.setText(Integer.toString(_tourData.getDpTolerance()));

			_btnSaveTour.setEnabled(_tourData.getTourPerson() != null);
		}
		_isDirtyDisabled = false;

		updateUISegmenterSelector();
		onSelectSegmenterType(false);
	}

	/**
	 * when dp tolerance was changed set the tour dirty
	 */
	private void setTourDirty() {

		if (_isDirtyDisabled) {
			return;
		}

		if (_tourData != null && _savedDpTolerance != _tourData.getDpTolerance()) {
			_isTourDirty = true;
		}
	}

	/**
	 * update ascending altitude computed value
	 */
	private void updateUIAltitude() {

		final TourSegmenter selectedSegmenter = getSelectedSegmenter();
		if (selectedSegmenter == null) {
			clearView();
			return;
		}

		final SegmenterType selectedSegmenterType = selectedSegmenter.segmenterType;
		int[] altitudeSegments;
		if (selectedSegmenterType == SegmenterType.ByComputedAltiUpDown) {
			altitudeSegments = _tourData.segmentSerieComputedAltitudeDiff;
		} else {
			altitudeSegments = _tourData.segmentSerieAltitudeDiff;
		}

		if (altitudeSegments == null) {
			_lblAltitudeUp.setText(UI.EMPTY_STRING);
			return;
		}

		// compute total alti up/down from the segments
		_altitudeUp = 0;
		_altitudeDown = 0;

		for (final int altitude : altitudeSegments) {
			if (altitude > 0) {
				_altitudeUp += altitude;
			} else {
				_altitudeDown += altitude;
			}
		}

		final StringBuilder sb = new StringBuilder();
		sb.append(Integer.toString((int) (_altitudeUp / UI.UNIT_VALUE_ALTITUDE)));
		sb.append(UI.SLASH_WITH_SPACE);
		sb.append(Integer.toString((int) (_tourData.getTourAltUp() / UI.UNIT_VALUE_ALTITUDE)));
		sb.append(UI.SPACE);
		sb.append(UI.UNIT_LABEL_ALTITUDE);

		_lblAltitudeUp.setText(sb.toString());
	}

	private void updateUIDistance() {

		float scaleDistance = getDistance() / UI.UNIT_VALUE_DISTANCE;

		if (UI.UNIT_VALUE_DISTANCE == UI.UNIT_MILE) {

			// imperial

			scaleDistance /= 1000;

			final int distanceInt = (int) scaleDistance;
			final float distanceFract = scaleDistance - distanceInt;

			// create distance for imperials which shows the fraction with 1/8, 1/4, 3/8 ...
			final StringBuilder sb = new StringBuilder();
			if (distanceInt > 0) {
				sb.append(Integer.toString(distanceInt));
				sb.append(UI.SPACE);
			}

			if (Math.abs(distanceFract - 0.125f) <= 0.01) {
				sb.append("1/8"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			} else if (Math.abs(distanceFract - 0.25f) <= 0.01) {
				sb.append("1/4"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			} else if (Math.abs(distanceFract - 0.375) <= 0.01) {
				sb.append("3/8"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			} else if (Math.abs(distanceFract - 0.5f) <= 0.01) {
				sb.append("1/2"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			} else if (Math.abs(distanceFract - 0.625) <= 0.01) {
				sb.append("5/8"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			} else if (Math.abs(distanceFract - 0.75f) <= 0.01) {
				sb.append("3/4"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			} else if (Math.abs(distanceFract - 0.875) <= 0.01) {
				sb.append("7/8"); //$NON-NLS-1$
				sb.append(UI.SPACE);
			}

			sb.append(UI.UNIT_LABEL_DISTANCE);

			// update UI
			_lblDistanceValue.setText(sb.toString());

		} else {

			// metric

			// format distance
			String distanceText;
			final float selectedDistance = scaleDistance / 1000;
			if (selectedDistance >= 10) {
				distanceText = _nf_0_0.format(selectedDistance);
			} else {
				distanceText = _nf_1_1.format(selectedDistance);
			}

			// update UI
			_lblDistanceValue.setText(distanceText + UI.SPACE + UI.UNIT_LABEL_DISTANCE);
		}
	}

	private void updateUIMinAltitude() {

		// preserve selection
		int prevSelection = _cboMinAltitude.getSelectionIndex();
		if (prevSelection < 0) {
			prevSelection = PrefPageComputedValues.DEFAULT_MIN_ALTITUDE_INDEX;
		}

		_cboMinAltitude.removeAll();

		for (final int minAlti : PrefPageComputedValues.ALTITUDE_MINIMUM) {
			_cboMinAltitude.add(Integer.toString((int) (minAlti / UI.UNIT_VALUE_ALTITUDE)));
		}

		_cboMinAltitude.select(prevSelection);
	}

	private void updateUISegmenterSelector() {

		final TourSegmenter currentSegmenter = getSelectedSegmenter();
		final int availableSegmenterData = checkSegmenterData(_tourData);

		// get all segmenters which can segment current tour
		_availableSegmenter.clear();
		for (final TourSegmenter tourSegmenter : _allTourSegmenter.values()) {

			final int requiredDataSeries = tourSegmenter.requiredDataSeries;

			if ((availableSegmenterData & requiredDataSeries) == requiredDataSeries) {
				_availableSegmenter.add(tourSegmenter);
			}
		}

		// sort by name
		Collections.sort(_availableSegmenter);

		/*
		 * fill list box
		 */
		int segmenterIndex = 0;
		int previousSegmenterIndex = -1;
		int userSelectedSegmenterIndex = -1;

		_cboSegmenterType.removeAll();
		for (final TourSegmenter tourSegmenter : _availableSegmenter) {

			_cboSegmenterType.add(tourSegmenter.name);

			if (tourSegmenter.segmenterType == _userSelectedSegmenterType) {
				userSelectedSegmenterIndex = segmenterIndex;
			}
			if (tourSegmenter == currentSegmenter) {
				previousSegmenterIndex = segmenterIndex;
			}

			segmenterIndex++;
		}

		// reselect previous segmenter
		if (userSelectedSegmenterIndex != -1) {
			_cboSegmenterType.select(userSelectedSegmenterIndex);
		} else {
			if (previousSegmenterIndex != -1) {
				_cboSegmenterType.select(previousSegmenterIndex);
			} else {
				_cboSegmenterType.select(0);
			}
		}
	}
}
