/*******************************************************************************
 * Copyright (C) 2026 Wolfgang Schramm and Contributors
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
package net.tourbook.equipment;

import net.tourbook.common.UI;
import net.tourbook.common.tooltip.AdvancedSlideout;
import net.tourbook.common.util.Util;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Equipment filter slideout
 */
public class SlideoutEquipmentFilter extends AdvancedSlideout {

   private ToolItem _toolItem;

   /*
    * UI controls
    */
   private Composite       _shellContainer;

   private Button          _rdoFilter_Equipment_Retired_Is;
   private Button          _rdoFilter_Equipment_Retired_IsNot;
   private Button          _rdoFilter_Equipment_Retired_Ignore;
   private Button          _rdoFilter_Equipment_Tours_With;
   private Button          _rdoFilter_Equipment_Tours_Without;
   private Button          _rdoFilter_Equipment_Tours_Ignore;

   private EquipmentView   _equipmentView;

   private IDialogSettings _state;

   public SlideoutEquipmentFilter(final ToolItem toolItem,
                                  final IDialogSettings state,
                                  final IDialogSettings stateSlideout,
                                  final EquipmentView equipmentView) {

      super(toolItem.getParent(), stateSlideout, new int[] { 220, 100, 220, 100 });

      _toolItem = toolItem;
      _state = state;
      _equipmentView = equipmentView;

      setTitleText("Equipment Filter");

      // prevent that the opened slideout is partly hidden
      setIsForceBoundsToBeInsideOfViewport(true);
   }

   @Override
   protected void createSlideoutContent(final Composite parent) {

      initUI(parent);

      createUI(parent);

      restoreState();
   }

   private Composite createUI(final Composite parent) {

      _shellContainer = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults()
            .margins(0, 0)
            .applyTo(_shellContainer);
      {
         UI.createLabel(_shellContainer, "Filter out equipment depending on");

         createUI_10_Filter(_shellContainer);
      }

      return _shellContainer;
   }

   private void createUI_10_Filter(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
//      container.setBackground(UI.SYS_COLOR_BLUE);
      {
         {
            final Label label = UI.createLabel(container, "Contained tours");
            GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(label);

            final Composite containerTours = new Composite(container, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(containerTours);
            GridLayoutFactory.fillDefaults().numColumns(1).applyTo(containerTours);
            {
               {
                  _rdoFilter_Equipment_Tours_Ignore = new Button(containerTours, SWT.RADIO);
                  _rdoFilter_Equipment_Tours_Ignore.setText("Ignore &tours");
                  _rdoFilter_Equipment_Tours_Ignore.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onModified()));
               }
               {
                  _rdoFilter_Equipment_Tours_With = new Button(containerTours, SWT.RADIO);
                  _rdoFilter_Equipment_Tours_With.setText("Equipment &with tours");
                  _rdoFilter_Equipment_Tours_With.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onModified()));
               }
               {
                  _rdoFilter_Equipment_Tours_Without = new Button(containerTours, SWT.RADIO);
                  _rdoFilter_Equipment_Tours_Without.setText("Equipment with&out tours");
                  _rdoFilter_Equipment_Tours_Without.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onModified()));
               }
            }
         }
         {
            final Label label = UI.createLabel(container, "Its retirement");
            GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(label);

            final Composite containerRetired = new Composite(container, SWT.NONE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(containerRetired);
            GridLayoutFactory.fillDefaults().numColumns(1).applyTo(containerRetired);
            {
               {
                  _rdoFilter_Equipment_Retired_Ignore = new Button(containerRetired, SWT.RADIO);
                  _rdoFilter_Equipment_Retired_Ignore.setText("&Ignore retirement");
                  _rdoFilter_Equipment_Retired_Ignore.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onModified()));
               }
               {
                  _rdoFilter_Equipment_Retired_IsNot = new Button(containerRetired, SWT.RADIO);
                  _rdoFilter_Equipment_Retired_IsNot.setText("&Active equipment");
                  _rdoFilter_Equipment_Retired_IsNot.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onModified()));
               }
               {
                  _rdoFilter_Equipment_Retired_Is = new Button(containerRetired, SWT.RADIO);
                  _rdoFilter_Equipment_Retired_Is.setText("&Retired equipment");
                  _rdoFilter_Equipment_Retired_Is.addSelectionListener(SelectionListener.widgetSelectedAdapter(selectionEvent -> onModified()));
               }
            }
         }
      }
   }

   @Override
   protected Rectangle getParentBounds() {

      final Rectangle itemBounds = _toolItem.getBounds();
      final Point itemDisplayPosition = _toolItem.getParent().toDisplay(itemBounds.x, itemBounds.y);

      itemBounds.x = itemDisplayPosition.x;
      itemBounds.y = itemDisplayPosition.y;

      return itemBounds;
   }

   private void initUI(final Composite parent) {

   }

   @Override
   protected void onFocus() {

      _rdoFilter_Equipment_Tours_Ignore.setFocus();
   }

   private void onModified() {

      final int isContainsTours =

            _rdoFilter_Equipment_Tours_With.getSelection() ? 1
                  : _rdoFilter_Equipment_Tours_Without.getSelection() ? 2

                        // ignore
                        : 0;

      final int isRetired =

            _rdoFilter_Equipment_Retired_Is.getSelection() ? 1
                  : _rdoFilter_Equipment_Retired_IsNot.getSelection() ? 2

                        // ignore
                        : 0;

      _state.put(EquipmentView.STATE_EQUIPMENT_FILTER_IS_CONTAINS_TOURS, isContainsTours);
      _state.put(EquipmentView.STATE_EQUIPMENT_FILTER_IS_RETIRED, isRetired);

      _shellContainer.getDisplay().asyncExec(() -> _equipmentView.updateEquipmentFilter_FromSlideout());
   }

   @Override
   protected Point onResize(final int newContentWidth, final int newContentHeight) {

      if (_shellContainer.isDisposed()) {

         // this happened during debugging

         return null;
      }

      // there is no need to resize this dialog
      final Point defaultSize = _shellContainer.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

      return defaultSize;
   }

   private void restoreState() {

      final int isWithTours = Util.getStateInt(_state,
            EquipmentView.STATE_EQUIPMENT_FILTER_IS_CONTAINS_TOURS,
            EquipmentView.STATE_EQUIPMENT_FILTER_IS_CONTAINS_TOURS_DEFAULT);

      final int isRetired = Util.getStateInt(_state,
            EquipmentView.STATE_EQUIPMENT_FILTER_IS_RETIRED,
            EquipmentView.STATE_EQUIPMENT_FILTER_IS_RETIRED_DEFAULT);

// SET_FORMATTING_OFF

      _rdoFilter_Equipment_Tours_Ignore   .setSelection(isWithTours == 0);
      _rdoFilter_Equipment_Tours_With     .setSelection(isWithTours == 1);
      _rdoFilter_Equipment_Tours_Without  .setSelection(isWithTours == 2);

      _rdoFilter_Equipment_Retired_Ignore .setSelection(isRetired == 0);
      _rdoFilter_Equipment_Retired_Is     .setSelection(isRetired == 1);
      _rdoFilter_Equipment_Retired_IsNot  .setSelection(isRetired == 2);

// SET_FORMATTING_ON
   }

   @Override
   public void saveState() {

      // save slideout position/size
      super.saveState();
   }

}
