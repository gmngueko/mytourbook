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
package net.tourbook.tour;

import net.tourbook.chart.SelectionChartInfo;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.data.TourData;
import net.tourbook.data.TourLocation;
import net.tourbook.tag.ChangedTags;
import net.tourbook.ui.tourChart.HoveredValueData;
import net.tourbook.ui.views.sensors.SelectionRecordingDeviceBattery;
import net.tourbook.ui.views.sensors.SelectionSensor;
import net.tourbook.ui.views.tourDataEditor.TourDataEditorView;

import org.eclipse.jface.viewers.ISelection;

public enum TourEventId {

   /**
    * Tours has been modified, event data contains the modified tours {@link TourData}
    */
   TOUR_CHANGED,

   /**
    * All computed data for all tours are modified
    */
   ALL_TOURS_ARE_MODIFIED,

   /**
    * {@link TourData} has been modified, the UI must be updated by reloading {@link TourData}
    * <p>
    * When this event is fired, {@link TourDataEditorView} must <b>NOT</b> be dirty, this can be
    * checked with the method {@link TourManager#isTourEditorModified(TourData)}, it must return
    * <code>false</code>.
    * <p>
    * When a tour is modified, the event {@link #TOUR_CHANGED} must be fired
    */
   UPDATE_UI,

   /**
    * properties of the tour chart has been changed
    */
   TOUR_CHART_PROPERTY_IS_MODIFIED,

   /**
    *
    */
   SEGMENT_LAYER_CHANGED,

   /**
    * a reference tour is created
    */
   REFERENCE_TOUR_IS_CREATED,

   /**
    *
    */
   REFERENCE_TOUR_CHANGED,

   /**
    *
    */
   COMPARE_TOUR_CHANGED,

   /**
    * Tags for a tour has been modified. The property data contains an object {@link ChangedTags}
    * which contains the tags and the modified tours
    */
   NOTIFY_TAG_VIEW,

   /**
    * structure of the tags changed, this includes add/remove of tags and categories and
    * tag/category renaming
    */
   TAG_STRUCTURE_CHANGED,

   /**
    * Tag content has changed, e.g. tag image size
    */
   TAG_CONTENT_CHANGED,

   /**
    * Sliders in the tour chart moved. Property data contains {@link SelectionChartInfo} or
    * {@link SelectionChartXSliderPosition} with the position of the sliders.
    */
   SLIDER_POSITION_CHANGED,

   /**
    * remove the tour which is currently displayed because the tour is removed from a view or a view
    * is closed which provided the tour
    */
   CLEAR_DISPLAYED_TOUR,

   /**
    * This event is fired when mouse is hovering a value point, event data contains
    * {@link HoveredValueData}
    */
   HOVERED_VALUE_POSITION,

   /**
    * Event data contain the selected tours.
    */
   TOUR_SELECTION,

   /**
    * A tour marker is selected, event data contain a {@link SelectionTourMarker}.
    */
   MARKER_SELECTION,

   /**
    * A tour pause is selected, event data contain a {@link SelectionTourPause}.
    */
   PAUSE_SELECTION,

   /**
    * A sensor is selected, event data contains a {@link SelectionSensor}
    */
   SELECTION_SENSOR,

   /**
    * A sensor is selected, event data contains a {@link SelectionRecordingDeviceBattery}
    */
   SELECTION_RECORDING_DEVICE_BATTERY,

   /**
    * Something is selected in the map, event data contains a {@link ISelection}
    */
   MAP_SELECTION,

   /**
    * Show geo grid in the map, event data contains {@link TourGeoFilterItem}
    */
   MAP_SHOW_GEO_GRID,

   /**
    * This event is fired when a statistic is updated, the event data contains the
    * {@link Selection_StatisticValues}.
    */
   STATISTIC_VALUES,

   /**
    * Common locations are selected, event data contains a list with all {@link TourLocation}
    */
   COMMON_LOCATION_SELECTION,

   /**
    * Tour locations are selected, event data contains a list with all {@link TourLocation}
    */
   TOUR_LOCATION_SELECTION,

   /**
    * Fulltext was searched, the event data is containing a list with all tour ID's
    */
   FULLTEXT_SEARCH_TOURS,

}
