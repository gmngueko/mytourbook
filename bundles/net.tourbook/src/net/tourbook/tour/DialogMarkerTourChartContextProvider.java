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
package net.tourbook.tour;

import net.tourbook.Messages;
import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartXSlider;
import net.tourbook.chart.IChartContextProvider;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.ui.tourChart.ITourMarkerUpdater;
import net.tourbook.ui.tourChart.TourChart;
import net.tourbook.ui.tourChart.action.ActionCreateMarkerFromSlider;
import net.tourbook.ui.tourChart.action.ActionCreateMarkerFromValuePoint;
import net.tourbook.ui.tourChart.action.ActionDeleteMarker;
import net.tourbook.ui.tourChart.action.ActionSetMarkerLabelPositionMenu;
import net.tourbook.ui.tourChart.action.ActionSetMarkerVisible;
import net.tourbook.ui.tourChart.action.IMarkerReceiver;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;

class DialogMarkerTourChartContextProvider implements IChartContextProvider, IMarkerReceiver {

   private final DialogMarker               _markerDialog;

   private ActionDeleteMarker               _actionDeleteMarker;
   private ActionCreateMarkerFromSlider     _actionCreateMarkerFromSlider;
   private ActionCreateMarkerFromSlider     _actionCreateMarkerFromSliderLeft;
   private ActionCreateMarkerFromSlider     _actionCreateMarkerFromSliderRight;
   private ActionCreateMarkerFromValuePoint _actionCreateMarkerFromValuePoint;
   private ActionSetMarkerVisible           _actionSetMarkerVisible;
   private ActionSetMarkerLabelPositionMenu _actionSetMarkerPosition;
//	private ActionSetMarkerImageMenu			_actionSetMarkerImageMenu;

   private ChartXSlider _leftSlider;
   private ChartXSlider _rightSlider;

   /**
    * @param markerDialog
    */
   DialogMarkerTourChartContextProvider(final DialogMarker markerDialog) {

      _markerDialog = markerDialog;

      createActions();
   }

   @Override
   public void addTourMarker(final TourMarker tourMarker) {
      _markerDialog.addTourMarker(tourMarker);
   }

   private void createActions() {

      final ITourMarkerUpdater tourMarkerUpdater = _markerDialog.getTourChart();

      _actionCreateMarkerFromSlider = new ActionCreateMarkerFromSlider(
            this,
            Messages.RefTour_Action_CreateMarker,
            true);

      _actionCreateMarkerFromSliderLeft = new ActionCreateMarkerFromSlider(
            this,
            Messages.RefTour_Action_CreateLeftMarker,
            true);

      _actionCreateMarkerFromSliderRight = new ActionCreateMarkerFromSlider(
            this,
            Messages.RefTour_Action_CreateRightMarker,
            false);

      _actionCreateMarkerFromValuePoint = new ActionCreateMarkerFromValuePoint(
            this,
            Messages.RefTour_Action_CreateMarker);

      _actionCreateMarkerFromSlider.setMarkerReceiver(this);
      _actionCreateMarkerFromSliderLeft.setMarkerReceiver(this);
      _actionCreateMarkerFromSliderRight.setMarkerReceiver(this);
      _actionCreateMarkerFromValuePoint.setMarkerReceiver(this);

      _actionDeleteMarker = new ActionDeleteMarker(_markerDialog.getTourChart());
      _actionSetMarkerVisible = new ActionSetMarkerVisible(tourMarkerUpdater);
      _actionSetMarkerPosition = new ActionSetMarkerLabelPositionMenu(tourMarkerUpdater);
//		_actionSetMarkerImageMenu = new ActionSetMarkerImageMenu(tourMarkerUpdater);
   }

   @Override
   public void fillBarChartContextMenu(final IMenuManager menuMgr,
                                       final int hoveredBarSerieIndex,
                                       final int hoveredBarValueIndex) {}

   @Override
   public void fillContextMenu(final IMenuManager menuMgr,
                               final int mouseDownDevPositionX,
                               final int mouseDownDevPositionY) {

      final TourChart tourChart = _markerDialog.getTourChart();
      final TourData tourData = tourChart.getTourData();

      // action: create marker at the value position
      final int vpIndex = tourChart.getHoveredValuePointIndex();
      if (vpIndex != -1) {

         // a value point is hovered

         _actionCreateMarkerFromValuePoint.setValuePointIndex(vpIndex);

         menuMgr.add(_actionCreateMarkerFromValuePoint);
      }

      final TourMarker tourMarker = tourChart.getHoveredTourMarker();
      if (tourMarker != null) {

         _actionDeleteMarker.setTourMarker(tourMarker, false);
//			_actionSetMarkerImageMenu.setTourMarker(tourMarker);
         _actionSetMarkerPosition.setTourMarker(tourMarker);
         _actionSetMarkerVisible.setTourMarker(tourMarker, !tourMarker.isMarkerVisible());

         menuMgr.add(_actionSetMarkerPosition);
//			menuMgr.add(_actionSetMarkerImageMenu);

//			menuMgr.add(new Separator());
         menuMgr.add(_actionSetMarkerVisible);
         menuMgr.add(_actionDeleteMarker);
      }

      /*
       * Enable action
       */
      final boolean isTourSaved = tourData != null && tourData.getTourPerson() != null;

      _actionCreateMarkerFromValuePoint.setEnabled(isTourSaved);
   }

   @Override
   public void fillXSliderContextMenu(final IMenuManager menuMgr,
                                      final ChartXSlider leftSlider,
                                      final ChartXSlider rightSlider) {

      _leftSlider = leftSlider;
      _rightSlider = rightSlider;

      if (leftSlider != null || rightSlider != null) {

         // marker actions
         if (leftSlider != null && rightSlider == null) {
//				menuMgr.add(_actionCreateMarkerFromSlider);
         } else {
            menuMgr.add(_actionCreateMarkerFromSliderLeft);
            menuMgr.add(_actionCreateMarkerFromSliderRight);
         }
      }
   }

   @Override
   public Chart getChart() {
      return _markerDialog.getTourChart();
   }

   @Override
   public ChartXSlider getLeftSlider() {
      return _leftSlider;
   }

   @Override
   public ChartXSlider getRightSlider() {
      return _rightSlider;
   }

   @Override
   public void onHideContextMenu(final MenuEvent menuEvent, final Control menuParentControl) {}

   @Override
   public void onShowContextMenu(final MenuEvent menuEvent, final Control menuParentControl) {}

   @Override
   public boolean showOnlySliderContextMenu() {
      return false;
   }

}
