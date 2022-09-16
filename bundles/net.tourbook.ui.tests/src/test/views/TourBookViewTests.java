/*******************************************************************************
 * Copyright (C) 2022 Frédéric Bard
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import net.tourbook.Messages;
import net.tourbook.tour.TourLogManager;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.Test;

import utils.UITest;
import utils.Utils;

public class TourBookViewTests extends UITest {

   /**
    * This test doesn't work because SWTBot doesn't support native dialogs
    * https://wiki.eclipse.org/SWTBot/FAQ#How_do_I_use_SWTBot_to_test_native_dialogs_.28File_Dialogs.2C_Color_Dialogs.2C_etc.29.3F
    */
//   @Test
//   void testExportTourBookView() {
//
//      Utils.showTourBookView(bot);
//
//      final SWTBotTreeItem tour = Utils.getTour(bot);
//
//      tour.contextMenu(Messages.App_Action_ExportViewCSV).click();
//
//      bot.button("Save").click();
//
//      final Path csvFilePath = Paths.get(Utils.workingDirectory, "TourBook_2022-08-30_21-39-05.csv");
//      assertTrue(Files.exists(csvFilePath));
//
//      FilesUtils.deleteIfExists(csvFilePath);
//      assertTrue(!Files.exists(csvFilePath));
//   }

   @Test
   void testComputeTourDistance() {

      Utils.showTourBookView(bot);

      //Check the original distance
      SWTBotTreeItem tour = Utils.getTour(bot);
      assertEquals("0.542", tour.cell(tourBookView_Distance_Column_Index)); //$NON-NLS-1$

      //Compute the tour distance
      tour.contextMenu(Messages.Tour_Action_AdjustTourValues).menu(Messages.TourEditor_Action_ComputeDistanceValuesFromGeoPosition).click();
      bot.button("OK").click(); //$NON-NLS-1$

      //Check the new computed distance
      tour = Utils.getTour(bot);
      assertEquals("0.551", tour.cell(tourBookView_Distance_Column_Index)); //$NON-NLS-1$
   }

   @Test
   void testDuplicateAndDeleteTour() {

      Utils.showTourBookView(bot);

      bot.viewByTitle("Tour Book").show();

      // Get a tour that can be duplicated
      SWTBotTreeItem tour = bot.tree().getTreeItem("2014   1").expand() //$NON-NLS-1$
            .getNode("Jan   1").expand().select().getNode("1").select(); //$NON-NLS-1$ //$NON-NLS-2$

      // Duplicate the tour
      tour.contextMenu("Duplicate Tour...").click();

      // Set a different date than today's date
      bot.dateTime(0).setDate(new Date(1420117200000L));
      // Set a different time than the current's time
      bot.dateTime(1).setDate(new Date(1420117200000L));

      //Save the tour
      bot.toolbarButtonWithTooltip("Save modified tour (Ctrl+S)").click();

      tour = bot.tree().getTreeItem("2015   2").expand() //$NON-NLS-1$
            .getNode("Jan   1").expand().select().getNode("1").select(); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull(tour);

      //Delete the tour
      tour.contextMenu(Messages.Tour_Book_Action_delete_selected_tours_menu).menu(Messages.Tour_Book_Action_delete_selected_tours_menu).menu(
            Messages.Tour_Book_Action_delete_selected_tours).click();
      bot.button("OK").click(); //$NON-NLS-1$
      bot.button("OK").click(); //$NON-NLS-1$

      final List<?> logs = TourLogManager.getLogs();
      assertTrue(logs.stream().map(Object::toString).anyMatch(log -> log.contains(
            "1/1/15, 1:00 PM")));//$NON-NLS-1$

      //Check that the tour was successfully deleted
      final SWTBotTreeItem[] allItems = bot.tree().getAllItems();
      assertEquals("2015   1", allItems[2].getText());
   }

   @Test
   void testMultiplyTourCalories() {

      Utils.showTourBookView(bot);

      //Select a tour that contains a calories value
      SWTBotTreeItem tour = bot.tree().getTreeItem("2020   3").expand() //$NON-NLS-1$
            .getNode("May   2").expand().select().getNode("23").select(); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull(tour);

      //Check the original calories value
      assertEquals("1,073", tour.cell(tourBookView_Temperature_Column_Index)); //$NON-NLS-1$

      //Multiply the calories by 1000
      tour.contextMenu(Messages.Tour_Action_AdjustTourValues).menu(Messages.Tour_Action_MultiplyCaloriesBy1000).click();
      bot.button(Messages.Tour_Action_MultiplyCaloriesBy1000_Apply).click();

      //Check the new calories value
      tour = bot.tree().getTreeItem("2020   3").expand() //$NON-NLS-1$
            .getNode("May   2").expand().select().getNode("23").select(); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull(tour);
      assertEquals("1,073,000", tour.cell(tourBookView_Temperature_Column_Index)); //$NON-NLS-1$
   }
}
