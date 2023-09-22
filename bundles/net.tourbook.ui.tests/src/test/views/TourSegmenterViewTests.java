/*******************************************************************************
 * Copyright (C) 2022, 2023 Frédéric Bard
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
package views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.tourbook.Messages;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.Test;

import utils.UITest;
import utils.Utils;

public class TourSegmenterViewTests extends UITest {

   private SWTBotView getTourSegmenterView() {

      Utils.showViewFromMenu(bot, Utils.TOOLS, Utils.VIEW_NAME_TOURSEGMENTER);
      return Utils.showView(bot, Utils.VIEW_NAME_TOURSEGMENTER);
   }

   @Test
   void testSegmenterAlgorithms_All() {

      Utils.showView(bot, "Tour Chart"); //$NON-NLS-1$

      final SWTBotTreeItem tour = bot.tree().getTreeItem("2015   1").expand() //$NON-NLS-1$
            .getNode("May   1").expand().select().getNode("31").select(); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull(tour);

      SWTBot tourSegmenterViewBot = getTourSegmenterView().bot();
      SWTBotTable tableSegments = tourSegmenterViewBot.table();
      SWTBotCombo segmenterMethodCombo = tourSegmenterViewBot.comboBox(0);

      segmenterMethodCombo.setSelection(Messages.tour_segmenter_type_byAltitude);
      assertEquals("0:06", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.Tour_Segmenter_Type_ByAltitude_Merged);
      assertEquals("0:17", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.Tour_Segmenter_Type_ByAltitude_Marker);
      assertEquals("4:56", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.tour_segmenter_type_byMarker);
      assertEquals("4:56", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.tour_segmenter_type_byDistance);
      assertEquals("0:07", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.Tour_Segmenter_Type_ByBreakTime);
      assertEquals("1:06", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.tour_segmenter_type_byPower);
      assertEquals(">0", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.tour_segmenter_type_byPulse);
      assertEquals(">0", tableSegments.cell(0, 0)); //$NON-NLS-1$

      segmenterMethodCombo.setSelection(Messages.tour_segmenter_type_byComputedAltiUpDown);
      assertEquals("0:07", tableSegments.cell(0, 0)); //$NON-NLS-1$

      //Change the measurement system to imperial
      Utils.changeMeasurementSystem(bot, net.tourbook.common.Messages.Measurement_System_Profile_Imperial);

      bot.sleep(5000);

      //Change back the measurement system to metric
      Utils.changeMeasurementSystem(bot, net.tourbook.common.Messages.Measurement_System_Profile_Metric);

      bot.sleep(5000);

      final SWTBotView tourSegmenterView = getTourSegmenterView();
      tourSegmenterViewBot = tourSegmenterView.bot();
      tableSegments = tourSegmenterViewBot.table();

      segmenterMethodCombo = tourSegmenterViewBot.comboBox(0);
      segmenterMethodCombo.setSelection(Messages.Tour_Segmenter_Type_Surfing);
      assertEquals("0:17", tableSegments.cell(0, 0)); //$NON-NLS-1$

      tourSegmenterView.close();
   }

   @Test
   void testSelectSegments() {

      Utils.showView(bot, "Tour Chart"); //$NON-NLS-1$

      final SWTBotTreeItem tour = bot.tree().getTreeItem("2015   1").expand() //$NON-NLS-1$
            .getNode("May   1").expand().select().getNode("31").select(); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull(tour);

      Utils.showViewFromMenu(bot, Utils.TOOLS, Utils.VIEW_NAME_TOURSEGMENTER);
      final SWTBotView tourSegmenterView = Utils.showView(bot, Utils.VIEW_NAME_TOURSEGMENTER);

      final SWTBotTable tableSegments = tourSegmenterView.bot().table();
      tableSegments.select(0);
      tableSegments.select(1);

      tourSegmenterView.close();
   }
}
