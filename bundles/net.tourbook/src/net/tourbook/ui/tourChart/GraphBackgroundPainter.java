/*******************************************************************************
 * Copyright (C) 2005, 2021 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.tourChart;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.GraphDrawingData;
import net.tourbook.chart.IFillPainter;
import net.tourbook.common.swimming.SwimStroke;
import net.tourbook.common.swimming.SwimStrokeManager;
import net.tourbook.data.HrZoneContext;
import net.tourbook.data.TourData;
import net.tourbook.data.TourPerson;
import net.tourbook.data.TourPersonHRZone;
import net.tourbook.tour.TourManager;
import net.tourbook.training.TrainingManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Draws background color into the graph, e.g. HR zone, swim style
 */
public class GraphBackgroundPainter implements IFillPainter {

   private Color[]               _hrZone_Colors;
   private HashMap<Short, Color> _strokeStyle_Colors;

   private void createColors_HrZone(final GC gcGraph, final TourPerson tourPerson) {

      final Device display = gcGraph.getDevice();
      final List<TourPersonHRZone> personHrZones = tourPerson.getHrZonesSorted();

      _hrZone_Colors = new Color[personHrZones.size()];

      for (int colorIndex = 0; colorIndex < personHrZones.size(); colorIndex++) {

         final TourPersonHRZone hrZone = personHrZones.get(colorIndex);
         final RGB rgb = hrZone.getColor();

         _hrZone_Colors[colorIndex] = new Color(display, rgb);
      }
   }

   private void createColors_SwimStyle(final GC gcGraph) {

      final Device display = gcGraph.getDevice();

      _strokeStyle_Colors = new HashMap<>();

      for (final Entry<SwimStroke, RGB> swimStrokeItem : SwimStrokeManager.getSwimStroke_RGB().entrySet()) {
         _strokeStyle_Colors.put(swimStrokeItem.getKey().getValue(), new Color(display, swimStrokeItem.getValue()));
      }
   }

   @Override
   public void draw(final GC gcGraph,
                    final GraphDrawingData graphDrawingData,
                    final Chart chart,
                    final long[] devXPositions,
                    final int valueIndexFirstPoint,
                    final int valueIndexLastPoint) {

      final ChartDataModel dataModel = chart.getChartDataModel();

      final TourData tourData = (TourData) dataModel.getCustomData(TourManager.CUSTOM_DATA_TOUR_DATA);
      final TourChartConfiguration tcc = (TourChartConfiguration) dataModel.getCustomData(TourManager.CUSTOM_DATA_TOUR_CHART_CONFIGURATION);

      final boolean useGraphBgStyle_HrZone = tcc.isBackgroundStyle_HrZone();
      final boolean useGraphBgStyle_SwimStyle = tcc.isBackgroundStyle_SwimmingStyle();

      if (useGraphBgStyle_HrZone) {

         final TourPerson tourPerson = tourData.getDataPerson();
         if (tourPerson == null) {
            return;
         }

         final int numberOfHrZones = tourData.getNumberOfHrZones();
         if (numberOfHrZones == 0) {
            return;
         }

         if (tourData.pulseSerie == null) {
            return;
         }

         createColors_HrZone(gcGraph, tourPerson);

      } else if (useGraphBgStyle_SwimStyle) {

         createColors_SwimStyle(gcGraph);
      }

      boolean isGradient = false;
      boolean isWhite = false;
      boolean isBgColor = false;

      switch (tcc.graphBackground_Style) {

      case GRAPH_COLOR_TOP:

         isGradient = true;
         isWhite = false;
         isBgColor = true;
         break;

      case NO_GRADIENT:

         isGradient = false;
         isWhite = false;
         isBgColor = true;
         break;

      case WHITE_BOTTOM:

         isGradient = true;
         isWhite = true;
         isBgColor = false;
         break;

      case WHITE_TOP:

         isGradient = true;
         isWhite = true;
         isBgColor = true;
         break;
      }

      if (isWhite) {
         gcGraph.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
      }

      final int devCanvasHeight = graphDrawingData.devGraphHeight;

      final long devXPrev = devXPositions[valueIndexFirstPoint];
      long devXStart = devXPositions[valueIndexFirstPoint];

      if (useGraphBgStyle_HrZone) {

         final float[] pulseSerie = tourData.pulseSerie;

         final HrZoneContext hrZoneContext = tourData.getHrZoneContext();
         int prevZoneIndex = TrainingManager.getZoneIndex(hrZoneContext, pulseSerie[valueIndexFirstPoint]);

         for (int valueIndex = valueIndexFirstPoint + 1; valueIndex <= valueIndexLastPoint; valueIndex++) {

            final long devXCurrent = devXPositions[valueIndex];
            final boolean isLastIndex = valueIndex == valueIndexLastPoint;

            // ignore same position even when the HR zone has changed
            if (devXCurrent == devXPrev && isLastIndex == false) {
               continue;
            }

            // check if zone has changed
            final int zoneIndex = TrainingManager.getZoneIndex(hrZoneContext, pulseSerie[valueIndex]);
            if (zoneIndex == prevZoneIndex && isLastIndex == false) {
               continue;
            }

            final int devWidth = (int) (devXCurrent - devXStart);
            final Color color = _hrZone_Colors[prevZoneIndex];

            if (isBgColor) {
               gcGraph.setBackground(color);
            } else {
               gcGraph.setForeground(color);
            }

            if (isGradient) {
               gcGraph.fillGradientRectangle((int) devXStart, 0, devWidth, devCanvasHeight, true);
            } else {
               gcGraph.fillRectangle((int) devXStart, 0, devWidth, devCanvasHeight);
            }

            // set start for the next HR zone
            devXStart = devXCurrent;
            prevZoneIndex = zoneIndex;
         }

         // dispose colors
         for (final Color color : _hrZone_Colors) {
            color.dispose();
         }

      } else if (useGraphBgStyle_SwimStyle) {

         final float[] allStrokeStyles = tourData.getSwim_StrokeStyle();
         short prevStrokeStyle = (short) allStrokeStyles[valueIndexFirstPoint];

         for (int valueIndex = valueIndexFirstPoint + 1; valueIndex <= valueIndexLastPoint; valueIndex++) {

            final long devXCurrent = devXPositions[valueIndex];
            final boolean isLastIndex = valueIndex == valueIndexLastPoint;

            // ignore same position
            if (devXCurrent == devXPrev && isLastIndex == false) {
               continue;
            }

            // check if stroke style has changed
            final short currentStrokeStyle = (short) allStrokeStyles[valueIndex];
            if (currentStrokeStyle == prevStrokeStyle && isLastIndex == false) {
               continue;
            }

            final int devWidth = (int) (devXCurrent - devXStart);
            final Color color = _strokeStyle_Colors.get(prevStrokeStyle);

            if (color != null) {

               /*
                * Color could be null when there is no stroke during the rest time -> nothing will
                * be painted to make the heartrate more visible
                */

               if (isBgColor) {
                  gcGraph.setBackground(color);
               } else {
                  gcGraph.setForeground(color);
               }

               if (isGradient) {
                  gcGraph.fillGradientRectangle((int) devXStart, 0, devWidth, devCanvasHeight, true);
               } else {
                  gcGraph.fillRectangle((int) devXStart, 0, devWidth, devCanvasHeight);
               }
            }

            // set start for the next HR zone
            devXStart = devXCurrent;
            prevStrokeStyle = currentStrokeStyle;
         }

         // dispose colors
         for (final Color color : _strokeStyle_Colors.values()) {
            color.dispose();
         }
      }
   }

}
