/*******************************************************************************
 * Copyright (C) 2005, 2010  Wolfgang Schramm and Contributors
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
package net.tourbook.export;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.TourData;
import net.tourbook.extension.export.ExportTourExtension;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.FileCollisionBehavior;
import net.tourbook.ui.ImageComboLabel;
import net.tourbook.ui.UI;
import net.tourbook.util.StatusUtil;

import org.dinopolis.gpstool.gpsinput.garmin.GarminTrack;
import org.dinopolis.gpstool.gpsinput.garmin.GarminWaypoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.joda.time.DateTime;

public class DialogExportTour extends TitleAreaDialog {

	private static final String						UI_AVERAGE_SYMBOL			= "� ";										//$NON-NLS-1$

	private static final int						VERTICAL_SECTION_MARGIN		= 10;
	private static final int						SIZING_TEXT_FIELD_WIDTH		= 250;
	private static final int						COMBO_HISTORY_LENGTH		= 20;

	private static final String						STATE_IS_MERGE_ALL_TOURS	= "isMergeAllTours";							//$NON-NLS-1$
	private static final String						STATE_IS_EXPORT_TOUR_RANGE	= "isExportTourRange";							//$NON-NLS-1$
	private static final String						STATE_IS_EXPORT_MARKERS		= "isExportMarkers";							//$NON-NLS-1$
	private static final String						STATE_IS_EXPORT_NOTES		= "isExportNotes";								//$NON-NLS-1$

	private static final String						STATE_IS_CAMOUFLAGE_SPEED	= "isCamouflageSpeed";							//$NON-NLS-1$
	private static final String						STATE_CAMOUFLAGE_SPEED		= "camouflageSpeedValue";						//$NON-NLS-1$

	private static final String						STATE_EXPORT_PATH_NAME		= "exportPathName";							//$NON-NLS-1$
	private static final String						STATE_EXPORT_FILE_NAME		= "exportFileName";							//$NON-NLS-1$
	private static final String						STATE_IS_OVERWRITE_FILES	= "isOverwriteFiles";							//$NON-NLS-1$

	private static final DateFormat					_timeFormatter				= DateFormat
																						.getTimeInstance(DateFormat.MEDIUM);
	private static final NumberFormat				_numberFormatter			= NumberFormat.getNumberInstance();

	private static String							_dlgDefaultMessage;

	private final String							_formatTemplate;

	private final IDialogSettings					_state						= TourbookPlugin
																						.getDefault()
																						.getDialogSettingsSection(
																								"DialogExportTour");			//$NON-NLS-1$

	private final ExportTourExtension				_exportExtensionPoint;

	private final ArrayList<TourData>				_tourDataList;
	private final int								_tourStartIndex;
	private final int								_tourEndIndex;

	private Point									_shellDefaultSize;
	private Composite								_dlgContainer;

	private Button									_chkExportTourRange;
	private Button									_chkMergeAllTours;
	private Button									_chkExportMarkers;
	private Button									_chkExportNotes;

	private Button									_chkCamouflageSpeed;
	private Text									_txtCamouflageSpeed;
	private Label									_lblCoumouflageSpeedUnit;

	private Composite								_inputContainer;

	private Combo									_comboFile;
	private Combo									_comboPath;
	private Button									_btnSelectFile;
	private Button									_btnSelectDirectory;
	private Text									_txtFilePath;
	private Button									_chkOverwriteFiles;

	private ProgressIndicator						_progressIndicator;
	private ImageComboLabel							_lblExportedFilePath;

	private boolean									_isInit;

	private DateTime								_trackStartDateTime;

	/**
	 * Is <code>true</code> when multiple tours are selected and not merged into 1 file
	 */
	private boolean									_isMultipleTourAndMultipleFile;

	public DialogExportTour(final Shell parentShell,
							final ExportTourExtension exportExtensionPoint,
							final ArrayList<TourData> tourDataList,
							final int tourStartIndex,
							final int tourEndIndex,
							final String formatTemplate) {

		super(parentShell);

		int shellStyle = getShellStyle();

		shellStyle = //
		SWT.NONE //
				| SWT.TITLE
				| SWT.CLOSE
				| SWT.MIN
//				| SWT.MAX
				| SWT.RESIZE
				| SWT.NONE;

		// make dialog resizable
		setShellStyle(shellStyle);

		_exportExtensionPoint = exportExtensionPoint;
		_formatTemplate = formatTemplate;

		_tourDataList = tourDataList;
		_tourStartIndex = tourStartIndex;
		_tourEndIndex = tourEndIndex;

		_dlgDefaultMessage = NLS.bind(Messages.dialog_export_dialog_message, _exportExtensionPoint.getVisibleName());
	}


	/**
	 * @return Returns <code>true</code> when a part of a tour can be exported
	 */
	private boolean canExportTourPart() {
		return (_tourDataList.size() == 1) && (_tourStartIndex >= 0) && (_tourEndIndex > 0);
	}

	@Override
	public boolean close() {

		saveState();

		return super.close();
	}

	@Override
	protected void configureShell(final Shell shell) {

		super.configureShell(shell);

		shell.setText(Messages.dialog_export_shell_text);

		shell.addListener(SWT.Resize, new Listener() {
			public void handleEvent(final Event event) {

				// allow resizing the width but not the height

				if (_shellDefaultSize == null) {
					_shellDefaultSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				}

				final Point shellSize = shell.getSize();

				/*
				 * this is not working, the shell is flickering when the shell size is below min
				 * size and I found no way to prevent a resize :-(
				 */
//				if (shellSize.x < _shellDefaultSize.x) {
//					event.doit = false;
//				}

				shellSize.x = shellSize.x < _shellDefaultSize.x ? _shellDefaultSize.x : shellSize.x;
				shellSize.y = _shellDefaultSize.y;

				shell.setSize(shellSize);
			}
		});
	}

	@Override
	public void create() {

		super.create();

		setTitle(Messages.dialog_export_dialog_title);
		setMessage(_dlgDefaultMessage);

		_isInit = true;
		{
			restoreState();
		}
		_isInit = false;

//		validateFields();
		enableFields();
	}

	@Override
	protected final void createButtonsForButtonBar(final Composite parent) {

		super.createButtonsForButtonBar(parent);

		// set text for the OK button
		getButton(IDialogConstants.OK_ID).setText(Messages.dialog_export_btn_export);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {

		_dlgContainer = (Composite) super.createDialogArea(parent);

		createUI(_dlgContainer);

		return _dlgContainer;
	}

	private void createUI(final Composite parent) {

		_inputContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(_inputContainer);
		GridLayoutFactory.swtDefaults().margins(10, 5).applyTo(_inputContainer);
//		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		{
			createUI10Option(_inputContainer);
			createUI20Destination(_inputContainer);
		}
		createUI30Progress(parent);
	}

	private void createUI10Option(final Composite parent) {

		// container
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.dialog_export_group_options);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);
		{
			createUI12OptionCamouflageSpeed(group);
			createUI13OptionExportMarkers(group);
			createUI14OptionExportNotes(group);
			createUI15OptionMergeTours(group);
			createUI16OptionTourPart(group);
		}
	}

	private void createUI12OptionCamouflageSpeed(final Composite parent) {

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
		{
			/*
			 * checkbox: camouflage speed
			 */
			_chkCamouflageSpeed = new Button(container, SWT.CHECK);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(_chkCamouflageSpeed);
			_chkCamouflageSpeed.setText(Messages.dialog_export_chk_camouflageSpeed);
			_chkCamouflageSpeed.setToolTipText(Messages.dialog_export_chk_camouflageSpeed_tooltip);
			_chkCamouflageSpeed.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {

					validateFields();
					enableFields();

					if (_chkCamouflageSpeed.getSelection()) {
						_txtCamouflageSpeed.setFocus();
					}
				}
			});

			// text: speed
			_txtCamouflageSpeed = new Text(container, SWT.BORDER | SWT.TRAIL);
			_txtCamouflageSpeed.setToolTipText(Messages.dialog_export_chk_camouflageSpeedInput_tooltip);
			_txtCamouflageSpeed.addModifyListener(new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					validateFields();
					enableFields();
				}
			});
			_txtCamouflageSpeed.addListener(SWT.Verify, new Listener() {
				public void handleEvent(final Event e) {
					net.tourbook.util.UI.verifyIntegerInput(e, false);
				}
			});

			// label: unit
			_lblCoumouflageSpeedUnit = new Label(container, SWT.NONE);
			_lblCoumouflageSpeedUnit.setText(UI_AVERAGE_SYMBOL + UI.UNIT_LABEL_SPEED);
			GridDataFactory
					.fillDefaults()
					.grab(true, false)
					.align(SWT.BEGINNING, SWT.CENTER)
					.applyTo(_lblCoumouflageSpeedUnit);
		}
	}

	private void createUI13OptionExportMarkers(final Composite parent) {

		/*
		 * checkbox: export markers
		 */
		_chkExportMarkers = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(_chkExportMarkers);
		_chkExportMarkers.setText(Messages.dialog_export_chk_exportMarkers);
		_chkExportMarkers.setToolTipText(Messages.dialog_export_chk_exportMarkers_tooltip);
	}

	private void createUI14OptionExportNotes(final Composite parent) {

		/*
		 * checkbox: export notes
		 */
		_chkExportNotes = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(_chkExportNotes);
		_chkExportNotes.setText(Messages.dialog_export_chk_exportNotes);
		_chkExportNotes.setToolTipText(Messages.dialog_export_chk_exportNotes_tooltip);
	}

	private void createUI15OptionMergeTours(final Composite parent) {

		/*
		 * checkbox: merge all tours
		 */
		_chkMergeAllTours = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(_chkMergeAllTours);
		_chkMergeAllTours.setText(Messages.dialog_export_chk_mergeAllTours);
		_chkMergeAllTours.setToolTipText(Messages.dialog_export_chk_mergeAllTours_tooltip);
		_chkMergeAllTours.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				enableFields();
				setFileName();
			}
		});
	}

	private void createUI16OptionTourPart(final Composite parent) {

		/*
		 * checkbox: tour range
		 */
		String tourRangeUI = null;

		if ((_tourDataList.size() == 1) && (_tourStartIndex != -1) && (_tourEndIndex != -1)) {

			final TourData tourData = _tourDataList.get(0);
			final int[] timeSerie = tourData.timeSerie;
			if (timeSerie != null) {

				final int[] distanceSerie = tourData.distanceSerie;
				final boolean isDistance = distanceSerie != null;

				final int startTime = timeSerie[_tourStartIndex];
				final int endTime = timeSerie[_tourEndIndex];

				final DateTime dtTour = new DateTime(
						tourData.getStartYear(),
						tourData.getStartMonth(),
						tourData.getStartDay(),
						tourData.getStartHour(),
						tourData.getStartMinute(),
						tourData.getStartSecond(),
						0);

				final String uiStartTime = _timeFormatter.format(dtTour.plusSeconds(startTime).toDate());
				final String uiEndTime = _timeFormatter.format(dtTour.plusSeconds(endTime).toDate());

				if (isDistance) {

					_numberFormatter.setMinimumFractionDigits(3);
					_numberFormatter.setMaximumFractionDigits(3);

					tourRangeUI = NLS.bind(
							Messages.dialog_export_chk_tourRangeWithDistance,
							new Object[] {
									uiStartTime,
									uiEndTime,

									_numberFormatter.format(((float) distanceSerie[_tourStartIndex])
											/ 1000
											/ UI.UNIT_VALUE_DISTANCE),

									_numberFormatter.format(((float) distanceSerie[_tourEndIndex])
											/ 1000
											/ UI.UNIT_VALUE_DISTANCE),

									UI.UNIT_LABEL_DISTANCE,

									// adjust by 1 to corresponds to the number in the tour editor
									_tourStartIndex + 1,
									_tourEndIndex + 1 });

				} else {

					tourRangeUI = NLS.bind(Messages.dialog_export_chk_tourRangeWithoutDistance, new Object[] {
							uiStartTime,
							uiEndTime,
							_tourStartIndex + 1,
							_tourEndIndex + 1 });
				}
			}
		}

		_chkExportTourRange = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(_chkExportTourRange);

		_chkExportTourRange.setText(tourRangeUI != null ? tourRangeUI : Messages.dialog_export_chk_tourRangeDisabled);

		_chkExportTourRange.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				enableFields();
			}
		});
	}

	private void createUI20Destination(final Composite parent) {

		Label label;

		final ModifyListener filePathModifyListener = new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				validateFields();
			}
		};

		/*
		 * group: filename
		 */
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.dialog_export_group_exportFileName);
		GridDataFactory.fillDefaults().grab(true, false).indent(0, VERTICAL_SECTION_MARGIN).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(group);
		{
			/*
			 * label: filename
			 */
			label = new Label(group, SWT.NONE);
			label.setText(Messages.dialog_export_label_fileName);

			/*
			 * combo: path
			 */
			_comboFile = new Combo(group, SWT.SINGLE | SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(_comboFile);
			((GridData) _comboFile.getLayoutData()).widthHint = SIZING_TEXT_FIELD_WIDTH;
			_comboFile.setVisibleItemCount(20);
			_comboFile.addVerifyListener(net.tourbook.util.UI.verifyFilenameInput());
			_comboFile.addModifyListener(filePathModifyListener);
			_comboFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					validateFields();
				}
			});

			/*
			 * button: browse
			 */
			_btnSelectFile = new Button(group, SWT.PUSH);
			_btnSelectFile.setText(Messages.app_btn_browse);
			_btnSelectFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					onSelectBrowseFile();
					validateFields();
				}
			});
			setButtonLayoutData(_btnSelectFile);

			// -----------------------------------------------------------------------------

			/*
			 * label: path
			 */
			label = new Label(group, SWT.NONE);
			label.setText(Messages.dialog_export_label_exportFilePath);

			/*
			 * combo: path
			 */
			_comboPath = new Combo(group, SWT.SINGLE | SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(_comboPath);
			((GridData) _comboPath.getLayoutData()).widthHint = SIZING_TEXT_FIELD_WIDTH;
			_comboPath.setVisibleItemCount(20);
			_comboPath.addModifyListener(filePathModifyListener);
			_comboPath.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					validateFields();
				}
			});

			/*
			 * button: browse
			 */
			_btnSelectDirectory = new Button(group, SWT.PUSH);
			_btnSelectDirectory.setText(Messages.app_btn_browse);
			_btnSelectDirectory.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					onSelectBrowseDirectory();
					validateFields();
				}
			});
			setButtonLayoutData(_btnSelectDirectory);

			// -----------------------------------------------------------------------------

			/*
			 * label: file path
			 */
			label = new Label(group, SWT.NONE);
			label.setText(Messages.dialog_export_label_filePath);

			/*
			 * text: filename
			 */
			_txtFilePath = new Text(group, /* SWT.BORDER | */SWT.READ_ONLY);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(_txtFilePath);
			_txtFilePath.setToolTipText(Messages.dialog_export_txt_filePath_tooltip);
			_txtFilePath.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

			// -----------------------------------------------------------------------------

			/*
			 * checkbox: overwrite files
			 */
			_chkOverwriteFiles = new Button(group, SWT.CHECK);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).span(3, 1).applyTo(_chkOverwriteFiles);
			_chkOverwriteFiles.setText(Messages.dialog_export_chk_overwriteFiles);
			_chkOverwriteFiles.setToolTipText(Messages.dialog_export_chk_overwriteFiles_tooltip);
			
			customizeUI20Destination(group);
		}

	}

	private void createUI30Progress(final Composite parent) {

		final int selectedTours = _tourDataList.size();

		// hide progress bar when only one tour is exported
		if (selectedTours < 2) {
			return;
		}

		// container
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).indent(0, VERTICAL_SECTION_MARGIN).applyTo(container);
		GridLayoutFactory.swtDefaults().margins(10, 5).numColumns(1).applyTo(container);
		{
			/*
			 * progress indicator
			 */
			_progressIndicator = new ProgressIndicator(container, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(_progressIndicator);

			/*
			 * label: exported filename
			 */
			_lblExportedFilePath = new ImageComboLabel(container, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(_lblExportedFilePath);
		}
	}

	/**
	 * Override in subclass to add fields to the filename group of the dialog.
	 * 
	 * @param group Fields group 
	 * 
	 * @see #createUI20Destination(Composite)
	 */
	protected void customizeUI20Destination(Group group) {
	}

	protected void doExport() throws IOException {

		// disable button's
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		getButton(IDialogConstants.CANCEL_ID).setEnabled(false);

		String completeFilePath = _txtFilePath.getText();

		final boolean isOverwriteFiles = _chkOverwriteFiles.getSelection();
		final boolean isCamouflageSpeed = _chkCamouflageSpeed.getSelection();
		final float[] camouflageSpeed = new float[1];
		try {
			camouflageSpeed[0] = Float.parseFloat(_txtCamouflageSpeed.getText());
		} catch (final NumberFormatException e) {
			camouflageSpeed[0] = 0.1F;
		}
		camouflageSpeed[0] *= UI.UNIT_VALUE_DISTANCE / 3.6f;

		final ArrayList<GarminTrack> trackList = new ArrayList<GarminTrack>();
		final ArrayList<GarminWaypoint> wayPointList = new ArrayList<GarminWaypoint>();

		final FileCollisionBehavior fileCollisionBehaviour = new FileCollisionBehavior();

		if (_tourDataList.size() == 1) {

			// export one tour

			final TourData tourData = _tourDataList.get(0);

			final GarminLap tourLap = getLap(tourData, _chkExportNotes.getSelection());

			final GarminTrack track = getTrack(
					tourData,
					TourManager.getTourDateTime(tourData),
					isCamouflageSpeed,
					camouflageSpeed[0]);

			if (track != null) {
				trackList.add(track);
			}

			if (_chkExportMarkers.getSelection()) {
				// get markers when this option is checked
				final TourData2GarminTrack converter = new TourData2GarminTrack();
				converter.getWaypoints(wayPointList, tourData);
			}

			doExportTour(tourLap, trackList, wayPointList, completeFilePath, fileCollisionBehaviour, isOverwriteFiles);

		} else {

			/*
			 * export multiple tours
			 */

			if (_chkMergeAllTours.getSelection()) {

				/*
				 * merge all tours into one
				 */

				_trackStartDateTime = TourManager.getTourDateTime(_tourDataList.get(0));
				DateTime trackDateTime;

				final GarminLap tourLap = new GarminLap();

				// create tracklist and lap
				for (final TourData tourData : _tourDataList) {

					mergeLap(tourLap, tourData, _chkExportNotes.getSelection());

					if (isCamouflageSpeed) {
						trackDateTime = _trackStartDateTime;
					} else {
						trackDateTime = TourManager.getTourDateTime(tourData);
					}

					final GarminTrack track = getTrack(tourData, trackDateTime, isCamouflageSpeed, camouflageSpeed[0]);
					if (track != null) {
						trackList.add(track);
					}
				}

				doExportTour(
						tourLap,
						trackList,
						wayPointList,
						completeFilePath,
						fileCollisionBehaviour,
						isOverwriteFiles);

			} else {

				/*
				 * export each tour separately
				 */

				final String exportPathName = getExportPathName();
				final boolean addNotes = _chkExportNotes.getSelection();
				_progressIndicator.beginTask(_tourDataList.size());

				final Job exportJob = new Job("export files") { //$NON-NLS-1$
					@Override
					public IStatus run(final IProgressMonitor monitor) {

						monitor.beginTask(UI.EMPTY_STRING, _tourDataList.size());
						final IPath exportFilePath = new Path(exportPathName).addTrailingSeparator();

						for (final TourData tourData : _tourDataList) {

							// get filepath
							final IPath filePath = exportFilePath
									.append(UI.format_yyyymmdd_hhmmss(tourData))
									.addFileExtension(_exportExtensionPoint.getFileExtension());

							final GarminLap tourLap = getLap(tourData, addNotes);

							// create tracklist
							trackList.clear();
							final GarminTrack track = getTrack(
									tourData,
									TourManager.getTourDateTime(tourData),
									isCamouflageSpeed,
									camouflageSpeed[0]);

							if (track != null) {
								trackList.add(track);

								/*
								 * update dialog progress monitor
								 */
								Display.getDefault().syncExec(new Runnable() {
									public void run() {

										// display exported filepath
										_lblExportedFilePath.setText(NLS.bind(
												Messages.dialog_export_lbl_exportFilePath,
												filePath.toOSString()));

										// !!! force label update !!!
										_lblExportedFilePath.update();

										_progressIndicator.worked(1);
									}
								});

								try {
									doExportTour(
											tourLap,
											trackList,
											wayPointList,
											filePath.toOSString(),
											fileCollisionBehaviour,
											isOverwriteFiles);
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}

							// check if overwrite dialog was canceled
							if (fileCollisionBehaviour.value == FileCollisionBehavior.DIALOG_IS_CANCELED) {
								break;
							}
						}

						return Status.OK_STATUS;
					}
				};

				exportJob.schedule();
				try {
					exportJob.join();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void doExportTour(	final GarminLap lap,
								final ArrayList<GarminTrack> tList,
								final ArrayList<GarminWaypoint> wayPointList,
								final String exportFileName,
								final FileCollisionBehavior fileCollisionBehaviour,
								final boolean isOverwriteFiles) throws IOException {

		final VelocityTourExporter tourExporter = new VelocityTourExporter(_formatTemplate);

		boolean isOverwrite = true;
		final File exportFile = new File(exportFileName);
		if (exportFile.exists()) {
			if (isOverwriteFiles) {
				// overwrite is enabled in the UI
			} else {
				isOverwrite = UI.confirmOverwrite(fileCollisionBehaviour, exportFile);
			}
		}

		if (isOverwrite) {

			try {
				tourExporter.doExportTour(lap, tList, wayPointList, exportFileName);
			} catch (final Exception e) {
				StatusUtil.showStatus(e);
			}
		}
	}

	private void enableExportButton(final boolean isEnabled) {
		final Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(isEnabled);
		}
	}

	private void enableFields() {

		final boolean isMultipleTours = _tourDataList.size() > 1;
		final boolean isCamouflageTime = _chkCamouflageSpeed.getSelection();
		final boolean isMergeTour = _chkMergeAllTours.getSelection();

		_chkMergeAllTours.setEnabled(isMultipleTours);
		// when disabled, uncheck it
		if (_chkMergeAllTours.isEnabled() == false) {
			_chkMergeAllTours.setSelection(false);
		}
		_isMultipleTourAndMultipleFile = _tourDataList.size() > 1 && _chkMergeAllTours.getSelection() == false;

		_comboFile.setEnabled(isMultipleTours == false || isMergeTour);
		_btnSelectFile.setEnabled(isMultipleTours == false || isMergeTour);

		_txtCamouflageSpeed.setEnabled(isCamouflageTime);
		_lblCoumouflageSpeedUnit.setEnabled(isCamouflageTime);

		_chkExportTourRange.setEnabled(canExportTourPart());
		// when disabled, uncheck it
		if (_chkExportTourRange.isEnabled() == false) {
			_chkExportTourRange.setSelection(false);
		}

		setFileName();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		// keep window size and position
		return _state;
	}

	protected ExportTourExtension getExportExtensionPoint() {
		return _exportExtensionPoint;
	}

	protected String getExportFileName() {
		return _comboFile.getText().trim();
	}

	protected String getExportPathName() {
		return _comboPath.getText().trim();
	}

	protected ArrayList<TourData> getTourDataList() {
		return _tourDataList;
	}

	private GarminLap getLap(final TourData tourData, final boolean addNotes) {

		final TourData2GarminTrack converter = new TourData2GarminTrack();
		return converter.getLap(tourData, addNotes);
	}

	private GarminTrack getTrack(	final TourData tourData,
									final DateTime trackDateTime,
									final boolean isCamouflageSpeed,
									final float camouflageSpeed) {

		final TourData2GarminTrack converter = new TourData2GarminTrack(
				isExportTourPart(),
				_tourStartIndex,
				_tourEndIndex);

		GarminTrack track = converter.getTrack(tourData, trackDateTime, isCamouflageSpeed, camouflageSpeed);

		// keep last date/time for the next merged tour
		_trackStartDateTime = converter.getLastTrackDateTime();

		return track;
	}

	private String[] getUniqueItems(final String[] pathItems, final String currentItem) {

		final ArrayList<String> pathList = new ArrayList<String>();

		pathList.add(currentItem);

		for (final String pathItem : pathItems) {

			// ignore duplicate entries
			if (currentItem.equals(pathItem) == false) {
				pathList.add(pathItem);
			}

			if (pathList.size() >= COMBO_HISTORY_LENGTH) {
				break;
			}
		}

		return pathList.toArray(new String[pathList.size()]);
	}

	/**
	 * @return Return <code>true</code> when a part of a tour can be exported
	 */
	private boolean isExportTourPart() {

		final boolean[] result = new boolean[1];

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				result[0] = _chkExportTourRange.getSelection()
						&& (_tourDataList.size() == 1)
						&& (_tourStartIndex != -1)
						&& (_tourEndIndex != -1);
			}
		});

		return result[0];
	}

	/**
	 * @return <code>true</code> when multiple tours are selected and not merged into 1 file
	 */
	public boolean isMultipleTourAndMultipleFile() {
		return _isMultipleTourAndMultipleFile;
	}

	private void mergeLap(final GarminLap tourLap, final TourData tourData, final boolean mergeNotes) {

		int calories = tourLap.getCalories();
		calories += tourData.getCalories();
		tourLap.setCalories(calories);

		if (mergeNotes) {
			final String notes = tourData.getTourDescription();
			if ((notes != null) && (notes.length() > 0)) {
				final String lapNotes = tourLap.getNotes();

				if (lapNotes == null) {
					tourLap.setNotes(notes);
				} else {
					tourLap.setNotes(lapNotes + "\n" + notes); //$NON-NLS-1$
				}
			}
		}

	}

	@Override
	protected void okPressed() {

		UI.disableAllControls(_inputContainer);

		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				try {
					doExport();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});

		super.okPressed();
	}

	private void onSelectBrowseDirectory() {

		final DirectoryDialog dialog = new DirectoryDialog(_dlgContainer.getShell(), SWT.SAVE);
		dialog.setText(Messages.dialog_export_dir_dialog_text);
		dialog.setMessage(Messages.dialog_export_dir_dialog_message);

		dialog.setFilterPath(getExportPathName());

		final String selectedDirectoryName = dialog.open();

		if (selectedDirectoryName != null) {
			setErrorMessage(null);
			_comboPath.setText(selectedDirectoryName);
		}
	}

	private void onSelectBrowseFile() {

		final String fileExtension = _exportExtensionPoint.getFileExtension();

		final FileDialog dialog = new FileDialog(_dlgContainer.getShell(), SWT.SAVE);
		dialog.setText(Messages.dialog_export_file_dialog_text);

		dialog.setFilterPath(getExportPathName());
		dialog.setFilterExtensions(new String[] { fileExtension });
		dialog.setFileName("*." + fileExtension);//$NON-NLS-1$

		final String selectedFilePath = dialog.open();

		if (selectedFilePath != null) {
			setErrorMessage(null);
			_comboFile.setText(new Path(selectedFilePath).toFile().getName());
		}
	}

	private void restoreState() {

		_chkMergeAllTours.setSelection(_state.getBoolean(STATE_IS_MERGE_ALL_TOURS));
		_chkExportTourRange.setSelection(_state.getBoolean(STATE_IS_EXPORT_TOUR_RANGE));
		_chkExportMarkers.setSelection(_state.getBoolean(STATE_IS_EXPORT_MARKERS));
		_chkExportNotes.setSelection(_state.getBoolean(STATE_IS_EXPORT_NOTES));

		// camouflage speed
		_chkCamouflageSpeed.setSelection(_state.getBoolean(STATE_IS_CAMOUFLAGE_SPEED));
		final String camouflageSpeed = _state.get(STATE_CAMOUFLAGE_SPEED);
		_txtCamouflageSpeed.setText(camouflageSpeed == null ? "10" : camouflageSpeed);//$NON-NLS-1$
		_txtCamouflageSpeed.selectAll();

		// export file/path
		UI.restoreCombo(_comboFile, _state.getArray(STATE_EXPORT_FILE_NAME));
		UI.restoreCombo(_comboPath, _state.getArray(STATE_EXPORT_PATH_NAME));
		_chkOverwriteFiles.setSelection(_state.getBoolean(STATE_IS_OVERWRITE_FILES));
	}

	private void saveState() {

		// export file/path
		if (validateFilePath()) {
			_state.put(STATE_EXPORT_PATH_NAME, getUniqueItems(_comboPath.getItems(), getExportPathName()));
			_state.put(STATE_EXPORT_FILE_NAME, getUniqueItems(_comboFile.getItems(), getExportFileName()));
		}

		// merge all tours
		if (_tourDataList.size() > 1) {
			_state.put(STATE_IS_MERGE_ALL_TOURS, _chkMergeAllTours.getSelection());
		}

		// export tour part
		if (canExportTourPart()) {
			_state.put(STATE_IS_EXPORT_TOUR_RANGE, _chkExportTourRange.getSelection());
		}

		// camouflage speed
		_state.put(STATE_IS_CAMOUFLAGE_SPEED, _chkCamouflageSpeed.getSelection());
		_state.put(STATE_CAMOUFLAGE_SPEED, _txtCamouflageSpeed.getText());

		_state.put(STATE_IS_OVERWRITE_FILES, _chkOverwriteFiles.getSelection());
		_state.put(STATE_IS_EXPORT_MARKERS, _chkExportMarkers.getSelection());
		_state.put(STATE_IS_EXPORT_NOTES, _chkExportNotes.getSelection());
	}

	protected void setDlgDefaultMessage(String message) {
		_dlgDefaultMessage = message;
	}

	private void setError(final String message) {
		setErrorMessage(message);
		enableExportButton(false);
	}

	/**
	 * Set filename with the first tour date/time, when tour is merged "<#default>" is displayed
	 */
	private void setFileName() {

		// search for the first tour
		TourData minTourData = null;
		final long minTourMillis = 0;

		for (final TourData tourData : _tourDataList) {
			final DateTime checkingTourDate = TourManager.getTourDateTime(tourData);

			if (minTourData == null) {
				minTourData = tourData;
			} else {

				final long tourMillis = checkingTourDate.getMillis();
				if (tourMillis < minTourMillis) {
					minTourData = tourData;
				}
			}
		}

		if (_isMultipleTourAndMultipleFile) {

			// use default file name for each exported tour

			_comboFile.setText(Messages.dialog_export_label_DefaultFileName);

		} else if ((_tourDataList.size() == 1) && (_tourStartIndex != -1) && (_tourEndIndex != -1)) {

			// display the start date/time

			final DateTime dtTour = new DateTime(
					minTourData.getStartYear(),
					minTourData.getStartMonth(),
					minTourData.getStartDay(),
					minTourData.getStartHour(),
					minTourData.getStartMinute(),
					minTourData.getStartSecond(),
					0);

			// adjust start time
			final int startTime = minTourData.timeSerie[_tourStartIndex];
			final DateTime tourTime = dtTour.plusSeconds(startTime);

			_comboFile.setText(UI.format_yyyymmdd_hhmmss(
					tourTime.getYear(),
					tourTime.getMonthOfYear(),
					tourTime.getDayOfMonth(),
					tourTime.getHourOfDay(),
					tourTime.getMinuteOfHour(),
					tourTime.getSecondOfMinute()));
		} else {

			// display the tour date/time

			_comboFile.setText(UI.format_yyyymmdd_hhmmss(minTourData));
		}
	}

	private void validateFields() {

		if (_isInit) {
			return;
		}

		/*
		 * validate fields
		 */

		if (validateFilePath() == false) {
			return;
		}

		// speed value
		final boolean isEqualizeTimeEnabled = _chkCamouflageSpeed.getSelection();
		if (isEqualizeTimeEnabled) {

			if (net.tourbook.util.UI.verifyIntegerValue(_txtCamouflageSpeed.getText()) == false) {
				setError(Messages.dialog_export_error_camouflageSpeedIsInvalid);
				_txtCamouflageSpeed.setFocus();
				return;
			}
		}

		setErrorMessage(null);
		enableExportButton(true);
	}

	protected boolean validateFilePath() {
		return validateFilePath(_txtFilePath, _exportExtensionPoint.getFileExtension());
	}

	protected boolean validateFilePath(Text fieldFilePath, String fileExtension) {

		// check path
		IPath filePath = new Path(getExportPathName());
		if (new File(filePath.toOSString()).exists() == false) {

			// invalid path
			setError(NLS.bind(Messages.dialog_export_msg_pathIsNotAvailable, filePath.toOSString()));
			return false;
		}

		boolean returnValue = false;

		if (_isMultipleTourAndMultipleFile) {

			// only the path is checked, the file name is created automatically for each exported tour

			setMessage(_dlgDefaultMessage);

			// build file path with extension
			filePath = filePath
					.addTrailingSeparator()
					.append(Messages.dialog_export_label_DefaultFileName)
					.addFileExtension(fileExtension);

			returnValue = true;

		} else {

			String fileName = getExportFileName();

			// remove extentions
			final int extPos = fileName.indexOf('.');
			if (extPos != -1) {
				fileName = fileName.substring(0, extPos);
			}

			// build file path with extension
			filePath = filePath
					.addTrailingSeparator()
					.append(fileName)
					.addFileExtension(fileExtension);

			final File newFile = new File(filePath.toOSString());

			if ((fileName.length() == 0) || newFile.isDirectory()) {

				// invalid filename

				setError(Messages.dialog_export_msg_fileNameIsInvalid);

			} else if (newFile.exists()) {

				// file already exists

				setMessage(
						NLS.bind(Messages.dialog_export_msg_fileAlreadyExists, filePath.toOSString()),
						IMessageProvider.WARNING);
				returnValue = true;

			} else {

				setMessage(_dlgDefaultMessage);

				try {
					final boolean isFileCreated = newFile.createNewFile();

					// name is correct

					if (isFileCreated) {
						// delete file because the file is created for checking validity
						newFile.delete();
					}
					returnValue = true;

				} catch (final IOException ioe) {
					setError(Messages.dialog_export_msg_fileNameIsInvalid);
				}

			}
		}

		fieldFilePath.setText(filePath.toOSString());

		return returnValue;
	}
}
