/*******************************************************************************
 * Copyright (C) 2005, 2019 Wolfgang Schramm and Contributors
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
package net.tourbook.map2.action;

import de.byteholder.geoclipse.mapprovider.MP;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.tooltip.ActionToolbarSlideoutAdv;
import net.tourbook.common.tooltip.AdvancedSlideout;
import net.tourbook.common.tooltip.SlideoutLocation;
import net.tourbook.map2.Messages;
import net.tourbook.map2.view.Map2View;
import net.tourbook.map2.view.Slideout_Map2_MapProvider;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.ToolItem;

public class ActionMap2_MapProvider extends ActionToolbarSlideoutAdv {

   private static final ImageDescriptor _actionImageDescriptor = TourbookPlugin.getImageDescriptor(Messages.image_action_change_tile_factory);

   private IDialogSettings              _state;

   private Slideout_Map2_MapProvider    _slideoutMap2MapProvider;

   private Map2View                     _map2View;

   public ActionMap2_MapProvider(final Map2View map2View, final IDialogSettings state) {

      super(_actionImageDescriptor, _actionImageDescriptor);

      _map2View = map2View;
      _state = state;

   }

   @Override
   protected AdvancedSlideout createSlideout(final ToolItem toolItem) {

      _slideoutMap2MapProvider = new Slideout_Map2_MapProvider(toolItem, _map2View, _state);
      _slideoutMap2MapProvider.setSlideoutLocation(SlideoutLocation.BELOW_RIGHT);

      return _slideoutMap2MapProvider;
   }

   public MP getSelectedMapProvider() {

      return _slideoutMap2MapProvider.getSelectedMapProvider();
   }

   @Override
   protected void onBeforeOpenSlideout() {

      _map2View.closeOpenedDialogs(this); 
   }

   public void selectMapProvider(final String mapProviderID) {

      _slideoutMap2MapProvider.selectMapProvider(mapProviderID);
   }

}