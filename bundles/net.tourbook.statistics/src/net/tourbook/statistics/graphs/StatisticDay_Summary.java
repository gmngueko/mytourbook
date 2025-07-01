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

import net.tourbook.application.TourbookPlugin;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartType;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.statistic.ChartOptions_DaySummary;
import net.tourbook.statistic.SlideoutStatisticOptions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

public class StatisticDay_Summary extends StatisticDay {

   private final IPreferenceStore  _prefStore = TourbookPlugin.getPrefStore();
   private IPropertyChangeListener _statDay_PrefChangeListener;

   private boolean                 _isShowAvgPace;
   private boolean                 _isShowAvgSpeed;
   private boolean                 _isShowDistance;
   private boolean                 _isShowDuration;
   private boolean                 _isShowElevationDown;
   private boolean                 _isShowElevationUp;

   private void addPrefListener(final Composite container) {

      // create pref listener
      _statDay_PrefChangeListener = new IPropertyChangeListener() {
         @Override
         public void propertyChange(final PropertyChangeEvent event) {

            final String property = event.getProperty();

            // observe which data are displayed
            if (false
                  || property.equals(ITourbookPreferences.STAT_DAY_IS_SHOW_AVG_PACE)
                  || property.equals(ITourbookPreferences.STAT_DAY_IS_SHOW_AVG_SPEED)
                  || property.equals(ITourbookPreferences.STAT_DAY_IS_SHOW_DISTANCE)
                  || property.equals(ITourbookPreferences.STAT_DAY_IS_SHOW_DURATION)
                  || property.equals(ITourbookPreferences.STAT_DAY_IS_SHOW_ELEVATION_DOWN)
                  || property.equals(ITourbookPreferences.STAT_DAY_IS_SHOW_ELEVATION_UP)

                  || property.equals(ITourbookPreferences.STAT_DAY_DURATION_TIME)

            ) {

               if (property.equals(ITourbookPreferences.STAT_DAY_DURATION_TIME)) {

                  _isDuration_ReloadData = true;
               }

               // get the changed preferences
               getPreferences();

               // update chart
               preferencesHasChanged();
            }
         }
      };

      // add pref listener
      _prefStore.addPropertyChangeListener(_statDay_PrefChangeListener);

      // remove pref listener
      container.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(final DisposeEvent e) {
            _prefStore.removePropertyChangeListener(_statDay_PrefChangeListener);
         }
      });
   }

   @Override
   public void createStatisticUI(final Composite parent, final IViewSite viewSite) {

      super.createStatisticUI(parent, viewSite);

      addPrefListener(parent);
      getPreferences();
   }

   @Override
   ChartDataModel getChartDataModel() {

      final ChartDataModel chartDataModel = new ChartDataModel(ChartType.BAR);

      createXDataDay(chartDataModel);

      if (_isShowDistance) {
         createYDataDistance(chartDataModel);
      }

      if (_isShowElevationUp) {
         createYDataElevationUp(chartDataModel);
      }
      if (_isShowElevationDown) {
         createYDataElevationDown(chartDataModel);
      }

      if (_isShowDuration) {
         createYDataDuration(chartDataModel);
      }

      if (_isShowAvgPace) {
         createYDataAvgPace(chartDataModel);
      }

      if (_isShowAvgSpeed) {
         createYDataAvgSpeed(chartDataModel);
      }

      return chartDataModel;
   }

   @Override
   protected String getGridPrefPrefix() {
      return GRID_DAY_SUMMARY;
   }

   @Override
   protected String getLayoutPrefPrefix() {
      return LAYOUT_DAY_SUMMARY;
   }

   private void getPreferences() {

      _isShowAvgPace = _prefStore.getBoolean(ITourbookPreferences.STAT_DAY_IS_SHOW_AVG_PACE);
      _isShowAvgSpeed = _prefStore.getBoolean(ITourbookPreferences.STAT_DAY_IS_SHOW_AVG_SPEED);
      _isShowDistance = _prefStore.getBoolean(ITourbookPreferences.STAT_DAY_IS_SHOW_DISTANCE);
      _isShowDuration = _prefStore.getBoolean(ITourbookPreferences.STAT_DAY_IS_SHOW_DURATION);
      _isShowElevationUp = _prefStore.getBoolean(ITourbookPreferences.STAT_DAY_IS_SHOW_ELEVATION_UP);
      _isShowElevationDown = _prefStore.getBoolean(ITourbookPreferences.STAT_DAY_IS_SHOW_ELEVATION_DOWN);
   }

   @Override
   protected void setupStatisticSlideout(final SlideoutStatisticOptions slideout) {

      slideout.setStatisticOptions(new ChartOptions_DaySummary());
   }

}
