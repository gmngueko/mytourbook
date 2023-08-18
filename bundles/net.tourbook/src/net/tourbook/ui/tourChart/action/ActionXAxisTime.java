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
package net.tourbook.ui.tourChart.action;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.ui.tourChart.TourChart;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;

public class ActionXAxisTime extends Action {

   private TourChart _tourChart;

   public ActionXAxisTime(final TourChart tourChart) {

      super(UI.SPACE1, AS_RADIO_BUTTON);

      _tourChart = tourChart;

      final String obsolete1 = Messages.Tour_Action_show_time_on_x_axis;
      final String obsolete2 = Messages.Tour_Action_show_time_on_x_axis_tooltip;

      setToolTipText(Messages.Tour_Action_ShowTimeOnXAxis_Tooltip);
      setImageDescriptor(TourbookPlugin.getThemedImageDescriptor(Images.XAxis_ShowTime));

      setChecked(tourChart.getTourChartConfig().isShowTimeOnXAxis);
   }

   @Override
   public void runWithEvent(final Event event) {

      _tourChart.actionXAxisTime(event, isChecked());
   }

}
