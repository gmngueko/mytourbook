/*******************************************************************************
 * Copyright (C) 2021 Gervais-Martial Ngueko
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
package net.tourbook.tag;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.ExtraData;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourTagMaintenance;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog to modify Maintenace a {@link TourTag}
 */
public class Dialog_TourTag_Maintenance extends TitleAreaDialog {

   private static final String   ID     = "net.tourbook.tag.Dialog_TourTag_Maintenance"; //$NON-NLS-1$

   private final IDialogSettings _state = TourbookPlugin.getState(ID);

   private String                _dlgMessage;

   private TourTag               _tourTag_Original;
   private TourTag               _tourTag_Clone;

   /*
    * UI controls
    */
   //private Text _txtNotes;
   private Text _txtName;
   private Text _txtExtraHours;
   private Text _txtExtraMonths;

   public Dialog_TourTag_Maintenance(final Shell parentShell, final String dlgMessage, final TourTag tourTag) {

      super(parentShell);

      _dlgMessage = dlgMessage;

      _tourTag_Original = tourTag;
      _tourTag_Clone = tourTag.clone();

      // make dialog resizable
      setShellStyle(getShellStyle() | SWT.RESIZE);
   }

   @Override
   protected void configureShell(final Shell shell) {

      super.configureShell(shell);

      // set window title
      shell.setText(Messages.Dialog_TourTag_Title);
   }

   @Override
   public void create() {

      super.create();

      setTitle(Messages.Dialog_TourTag_MaintenanceTag_Title);
      setMessage(_dlgMessage);
   }

   @Override
   protected final void createButtonsForButtonBar(final Composite parent) {

      super.createButtonsForButtonBar(parent);

      // OK -> Save
      getButton(IDialogConstants.OK_ID).setText(Messages.app_action_save);
   }

   @Override
   protected Control createDialogArea(final Composite parent) {

      final Composite dlgContainer = (Composite) super.createDialogArea(parent);

      createUI(dlgContainer);

      restoreState();

      _txtName.selectAll();
      _txtName.setFocus();

      return dlgContainer;
   }

   /**
    * create the drop down menus, this must be created after the parent control is created
    */

   private void createUI(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);
      {
         {
            // Text: Name

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagName);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtName = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtName);
         }
         {
            // Text: ExtraHours

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_ExtraHour);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtExtraHours = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtExtraHours);
            _txtExtraHours.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final int value = Integer.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals("")) {
                        e.doit = false;
                     }
                  }
               }
            });
         }
         {
            // Text: ExtraMonths

            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Dialog_TourTag_Label_TagMaintenance_ExtraMonth);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);

            _txtExtraMonths = new Text(container, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(_txtExtraMonths);

            _txtExtraMonths.addVerifyListener(new VerifyListener() {
               @Override
               public void verifyText(final VerifyEvent e) {
                  /* Notice how we combine the old and new below */
                  final String currentText = ((Text) e.widget).getText();
                  final String valueTxt = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
                  try {
                     final int value = Integer.valueOf(valueTxt);
                     if (value < 0) {
                        e.doit = false;
                     }
                  } catch (final NumberFormatException ex) {
                     if (!valueTxt.equals("")) {
                        e.doit = false;
                     }
                  }
               }
            });
         }
         /*
          * {
          * // Text: Notes
          * final Label label = new Label(container, SWT.NONE);
          * label.setText(Messages.Dialog_TourTag_Label_Notes);
          * GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);
          * _txtNotes = new Text(container, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL |
          * SWT.H_SCROLL);
          * GridDataFactory.fillDefaults()
          * .grab(true, true)
          * .hint(convertWidthInCharsToPixels(100), convertHeightInCharsToPixels(20))
          * .applyTo(_txtNotes);
          * }
          */
      }
   }

   @Override
   protected IDialogSettings getDialogBoundsSettings() {

      // keep window size and position
      return _state;
   }

   @Override
   protected void okPressed() {

      // set model from UI
      saveState();

      if (_tourTag_Clone.isValidForSave() == false) {
         return;
      }

      // update original model
      _tourTag_Original.updateFromModified(_tourTag_Clone);

      super.okPressed();
   }

   private void restoreState() {

      _txtName.setText(_tourTag_Clone.getTagName());
      final ExtraData extraData = _tourTag_Clone.getExtraData();
      if (extraData != null && extraData.getMaintenanceInfo() != null) {
         final TourTagMaintenance maintenance = extraData.getMaintenanceInfo();
         _txtExtraHours.setText(String.valueOf(maintenance.getExtraHourUsed()));
         _txtExtraMonths.setText(String.valueOf(maintenance.getExtraMonthUsage()));
      }
      //_txtNotes.setText(_tourTag_Clone.getNotes());
   }

   private void saveState() {

      //_tourTag_Clone.setNotes(_txtNotes.getText());
      _tourTag_Clone.setTagName(_txtName.getText());
      ExtraData extraData = _tourTag_Clone.getExtraData();
      if (extraData == null) {
         extraData = new ExtraData();
         _tourTag_Clone.setExtraData(extraData);
      }
      TourTagMaintenance maintenance = extraData.getMaintenanceInfo();
      if (maintenance == null) {
         maintenance = new TourTagMaintenance();
         extraData.setMaintenanceInfo(maintenance);
      }

      final String extraHour = _txtExtraHours.getText().strip();
      if (extraHour.isEmpty()) {
         maintenance.setExtraHourUsed(0);
      } else {
         maintenance.setExtraHourUsed(Integer.valueOf(extraHour));
      }

      final String extraMonth = _txtExtraMonths.getText().strip();
      if (extraMonth.isEmpty()) {
         maintenance.setExtraMonthUsage(0);
      } else {
         maintenance.setExtraMonthUsage(Integer.valueOf(extraMonth));
      }

   }
}
