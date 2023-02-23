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
package net.tourbook.ui.action;

import java.util.ArrayList;

import net.tourbook.Messages;
import net.tourbook.data.TourData;
import net.tourbook.database.PersonManager;
import net.tourbook.tour.DialogJoinTours;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class ActionJoinTours extends Action {

	private final ITourProvider	_tourProvider;

	/**
	 * @param tourProvider
	 */
	public ActionJoinTours(final ITourProvider tourProvider) {

		_tourProvider = tourProvider;

		setText(Messages.App_Action_JoinTours);
	}

	/**
	 * Checks if the tour data can be joined. At least, the data series for time, distance or
	 * latitude must be available. All tours must have data for the required data series.
	 *
	 * @return Return <code>null</code> when data are OK or an error message.
	 */
	private String checkTourData() {

		boolean isFirstTour = true;

		boolean isTime = false;
		boolean isDistance = false;
		boolean isLat = false;
		boolean isPowerFromDevice = false;
		boolean isSpeedFromDevice = false;

		for (final TourData tourData : _tourProvider.getSelectedTours()) {

			final int[] tourTimeSerie = tourData.timeSerie;
			final float[] tourDistanceSerie = tourData.distanceSerie;
			final double[] tourLatitudeSerie = tourData.latitudeSerie;

			final boolean isTourTime = (tourTimeSerie != null) && (tourTimeSerie.length > 0);
			final boolean isTourDistance = (tourDistanceSerie != null) && (tourDistanceSerie.length > 0);
			final boolean isTourLat = (tourLatitudeSerie != null) && (tourLatitudeSerie.length > 0);

			if (isFirstTour) {

				isFirstTour = false;

				isTime = isTourTime;
				isDistance = isTourDistance;
				isLat = isTourLat;
				isPowerFromDevice = tourData.isPowerSerieFromDevice();
				isSpeedFromDevice = tourData.isSpeedSerieFromDevice();

			} else {

				/*
				 * check if the data series are compatible which is true when each tour contains
				 * time, distance or latitude
				 */
				// check time
				if (isTime != isTourTime) {
					return NLS.bind(//
							Messages.Dialog_JoinTours_InvalidData,
							Messages.Dialog_JoinTours_InvalidData_Time);
				}

				// check distance
				if (isDistance != isTourDistance) {
					return NLS.bind(
							Messages.Dialog_JoinTours_InvalidData,
							Messages.Dialog_JoinTours_InvalidData_Distance);
				}

				// check latitude
				if (isLat != isTourLat) {
					return NLS.bind(
							Messages.Dialog_JoinTours_InvalidData,
							Messages.Dialog_JoinTours_InvalidData_Latitude);
				}

				// check power
				if (isPowerFromDevice != tourData.isPowerSerieFromDevice()) {
					return Messages.Dialog_JoinTours_InvalidData_Power;
				}

				// check speed
				if (isSpeedFromDevice != tourData.isSpeedSerieFromDevice()) {
					return Messages.Dialog_JoinTours_InvalidData_Speed;
				}
			}

			if (isTime == false && isDistance == false && isLat == false) {
				return Messages.Dialog_JoinTours_InvalidData_RequiredDataSeries;
			}
		}

		return null;
	}

	@Override
	public void run() {

		// check if the tour editor contains a modified tour
		if (TourManager.isTourEditorModified()) {
			return;
		}

		// get selected tour, make sure at least two tours are selected
		final ArrayList<TourData> selectedTours = _tourProvider.getSelectedTours();
		if (selectedTours == null || selectedTours.size() < 2) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), //
					Messages.Dialog_JoinTours_InvalidData_DlgTitle,
					Messages.Dialog_JoinTours_InvalidData_InvalidTours);
			return;
		}

		// check person
		if (PersonManager.isPersonAvailable() == false) {
			return;
		}

		// check tour data
		final String checkMessage = checkTourData();

		if (checkMessage != null) {
			showMessage(checkMessage);
		} else {
         new DialogJoinTours(Display.getCurrent().getActiveShell(), _tourProvider, selectedTours).open();
		}
	}

	private void showMessage(final String checkedMessage) {
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(), //
				Messages.Dialog_JoinTours_InvalidData_DlgTitle,
				NLS.bind(Messages.Dialog_JoinTours_InvalidData_DlgMessage, checkedMessage));
	}
}
