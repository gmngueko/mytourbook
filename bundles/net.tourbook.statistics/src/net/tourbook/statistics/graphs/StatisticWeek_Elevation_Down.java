/*******************************************************************************
 * Copyright (C) 2005, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.statistics.graphs;

import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartType;

public class StatisticWeek_Elevation_Down extends StatisticWeek {

	@Override
	ChartDataModel getChartDataModel() {

		final ChartDataModel chartDataModel = new ChartDataModel(ChartType.BAR);

		createXData_Week(chartDataModel);
      createYData_ElevationDown(chartDataModel);

		return chartDataModel;
	}

	@Override
	protected String getGridPrefPrefix() {

      return GRID_WEEK_ELEVATION_DOWN;
	}

   @Override
   protected String getLayoutPrefPrefix() {
      return LAYOUT_WEEK_ELEVATION_DOWN;
   }
}
