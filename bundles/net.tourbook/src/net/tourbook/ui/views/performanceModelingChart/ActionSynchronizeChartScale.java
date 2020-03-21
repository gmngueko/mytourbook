/*******************************************************************************
 * Copyright (C) 2020 Frédéric Bard and Contributors
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
package net.tourbook.ui.views.performanceModelingChart;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.ui.UI;

import org.eclipse.jface.action.Action;

public class ActionSynchronizeChartScale extends Action {

   private PerformanceModelingChartView _performanceModelingChartView;

   public ActionSynchronizeChartScale(final PerformanceModelingChartView performanceModelingChartView) {

		super(UI.EMPTY_STRING, AS_CHECK_BOX);

      _performanceModelingChartView = performanceModelingChartView;

		setToolTipText(Messages.Training_View_Action_SynchChartScale);

		setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__synch_statistics));
		setDisabledImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__synch_statistics_Disabled));
	}

	@Override
	public void run() {
      _performanceModelingChartView.actionSynchChartScale();
	}
}
