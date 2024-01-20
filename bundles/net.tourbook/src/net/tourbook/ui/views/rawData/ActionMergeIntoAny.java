/*******************************************************************************
 * Copyright (C) 2024  Gervais-Martial Ngueko
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

package net.tourbook.ui.views.rawData;

import net.tourbook.data.TourData;
import net.tourbook.ui.UI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Merge a tour into another tour
 */
public class ActionMergeIntoAny extends Action {

	private TourData	_fromTourData;
	private TourData	_intoTourData;

	private RawDataView	_rawDataView;

   public ActionMergeIntoAny(final TourData mergeFromTour, final RawDataView rawDataView) {

		super(UI.EMPTY_STRING, AS_CHECK_BOX);

		_fromTourData = mergeFromTour;
      //_intoTourData = mergeIntoTour;
		_rawDataView = rawDataView;

		/*
		 * set menu text
		 */
      //final long start = mergeIntoTour.getTourStartTimeMS();
      final long start = mergeFromTour.getTourStartTimeMS();

		final StringBuilder sb = new StringBuilder().append(UI.EMPTY_STRING)//
            .append("Merging Any Tour with")
            .append(UI.COLON_SPACE)
            .append(UI.getFormatterDateShort().format(start))
				.append(UI.DASH_WITH_DOUBLE_SPACE)
				.append(UI.getFormatterTimeShort().format(start))
				.append(UI.DASH_WITH_DOUBLE_SPACE)
            .append(mergeFromTour.getDeviceName());
      //.append(mergeIntoTour.getDeviceName());

		setText(sb.toString());

		// show database icon
      //setImageDescriptor(ImageDescriptor.createFromImage(_rawDataView.getStateImage_Db(_intoTourData)));
      setImageDescriptor(ImageDescriptor.createFromImage(_rawDataView.getStateImage_Db(_fromTourData)));

      setChecked(true);

		// check menu item when the from tour is merge into the into tour
      /*
       * final Long mergeIntoTourId = mergeFromTour.getMergeTargetTourId();
       * if (mergeIntoTourId != null && mergeIntoTourId.equals(mergeIntoTour.getTourId())) {
       * setChecked(true);
       * }
       */
	}

	@Override
	public void run() {
      //_rawDataView.actionMergeTours(_fromTourData, _intoTourData);
      final MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.ABORT | SWT.RETRY | SWT.IGNORE);

      messageBox.setText("Warning");
      messageBox.setMessage("Save the changes before exiting?");
      final int buttonID = messageBox.open();
      //to get tourids for day of tour to merge
      //TourDatabase.getAllTourIds_BetweenTwoDates(buttonID, buttonID)
      //to get the tourData
      //final TourData tourDataInDb = tourManager.getTourData(tourData.getTourId());
	}

}
