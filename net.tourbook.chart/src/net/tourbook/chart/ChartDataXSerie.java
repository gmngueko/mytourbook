/*******************************************************************************
 * Copyright (C) 2005, 2011  Wolfgang Schramm and Contributors
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

/**
 * Author: Wolfgang Schramm Created: 21.06.2005
 */
package net.tourbook.chart;

/**
 * Contains data values for the x-axis
 */
public class ChartDataXSerie extends ChartDataSerie {

	/**
	 * start value for the serie data, this is use to set the start point for time data to the
	 * starting time
	 */
	private int				_startValue				= 0;

	/**
	 * index in the x-data at which the graph is painted in the marker color, <code>-1</code>
	 * disables the synch marker
	 */
	private int				_synchMarkerStartIndex	= -1;

	/**
	 * index in the x-data at which the graph is stoped to painted in the marker color
	 */
	private int				_synchMarkerEndIndex	= -1;

	/**
	 * Range marker shows an area with a different color in the graph
	 */
	private int[]			_rangeMarkerStartIndex;
	private int[]			_rangeMarkerEndIndex;

	/**
	 * Segment contains information to show statistics for several years
	 */
	private ChartSegments	_chartSegments;

	/**
	 * Scaling for the x-axis which is computed with {@link Math#pow(double, double)} when this
	 * value is <code>!= 1</code>
	 */
	private double			_scalingFactor			= 1;
	private double			_scalingMaxValue		= 1;

	public ChartDataXSerie(final int values[]) {
		setMinMaxValues(new int[][] { values });
	}

	public ChartDataXSerie(final int values[][]) {
		setMinMaxValues(values);
	}

	public ChartSegments getChartSegments() {
		return _chartSegments;
	}

	/**
	 * @return Returns scaling for the x-axis which is computed with Math.pow(double, double). This
	 *         scaling is disabled when <code>1</code> is returned.
	 */
	public double getScalingFactor() {
		return _scalingFactor;
	}

	public int[] getRangeMarkerEndIndex() {
		return _rangeMarkerEndIndex;
	}

	public int[] getRangeMarkerStartIndex() {
		return _rangeMarkerStartIndex;
	}

	public double getScalingMaxValue() {
		return _scalingMaxValue;
	}

	/**
	 * @return Returns the startValue.
	 */
	public int getStartValue() {
		return _startValue;
	}

	/**
	 * @return Returns the xMarkerEndIndex.
	 */
	public int getSynchMarkerEndIndex() {
		return _synchMarkerEndIndex;
	}

	/**
	 * @return Returns the xMarkerStartIndex or <code>-1</code> when the x-marker is not displayed
	 */
	public int getSynchMarkerStartIndex() {
		return _synchMarkerStartIndex;
	}

	public void setChartSegments(final ChartSegments chartSegments) {
		_chartSegments = chartSegments;
	}

	@Override
	void setMinMaxValues(final int[][] lowValues, final int[][] highValues) {}

	/**
	 * Range markers are an area in the graph which will be displayed in a different color
	 * 
	 * @param rangeMarkerStartIndex
	 * @param rangeMarkerEndIndex
	 */
	public void setRangeMarkers(final int[] rangeMarkerStartIndex, final int[] rangeMarkerEndIndex) {
		_rangeMarkerStartIndex = rangeMarkerStartIndex;
		_rangeMarkerEndIndex = rangeMarkerEndIndex;
	}

	public void setScalingFactors(final double scalingFactor, final double scalingMaxValue) {
		_scalingFactor = scalingFactor;
		_scalingMaxValue = scalingMaxValue;
	}

	/**
	 * @param startValue
	 *            The startValue to set.
	 */
	public void setStartValue(final int startValue) {
		this._startValue = startValue;
	}

	/**
	 * set the start/end value index for the marker which is displayed in a different color, by
	 * default the synch marker is disabled
	 * 
	 * @param startIndex
	 * @param endIndex
	 */
	public void setSynchMarkerValueIndex(final int startIndex, final int endIndex) {
		_synchMarkerStartIndex = startIndex;
		_synchMarkerEndIndex = endIndex;
	}

	@Override
	public String toString() {
		return "[ChartDataXSerie]";//$NON-NLS-1$
	}
}
