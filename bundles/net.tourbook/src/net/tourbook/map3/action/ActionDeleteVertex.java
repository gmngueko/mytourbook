/*******************************************************************************
 * Copyright (C) 2005, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.map3.action;

import net.tourbook.Images;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.map3.Messages;
import net.tourbook.map3.ui.DialogMap3ColorEditor;

import org.eclipse.jface.action.Action;

public class ActionDeleteVertex extends Action {

   private DialogMap3ColorEditor _dialogMap3ColorEditor;
   private int                   _vertexIndex;

   public ActionDeleteVertex(final DialogMap3ColorEditor dialogMap3ColorEditor) {

      super(null, AS_PUSH_BUTTON);

      _dialogMap3ColorEditor = dialogMap3ColorEditor;

      setText(Messages.Map3Color_Dialog_Action_RemoveVertex_Tooltip);
      setToolTipText(Messages.Map3Color_Dialog_Action_RemoveVertex_Tooltip);

      setImageDescriptor(TourbookPlugin.getImageDescriptor(Images.App_Trash));
   }

   @Override
   public void run() {

      _dialogMap3ColorEditor.actionRemoveVertex(_vertexIndex);
   }

   public void setData(final String key, final int vertexIndex) {

      _vertexIndex = vertexIndex;
   }
}
