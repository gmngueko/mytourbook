/*******************************************************************************
 * Copyright (C) 2005, 2020 Wolfgang Schramm and Contributors
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
package net.tourbook.sign;

import org.eclipse.jface.action.Action;

public class ActionRenameSign_DISABLED extends Action {

//	private ITourViewer	fTourViewer;
//
//	public ActionRenameSign(final ITourViewer tourViewer) {
//
//		super(Messages.action_tag_rename_tag, AS_PUSH_BUTTON);
//
//		fTourViewer = tourViewer;
//	}
//
//	private static boolean updateCategory(final long id, final String tagName) {
//
//		final EntityManager em = TourDatabase.getInstance().getEntityManager();
//
//		boolean isSaved = false;
//		final EntityTransaction ts = em.getTransaction();
//
//		try {
//
//			ts.begin();
//			{
//				final TourTagCategory categoryInDb = em.find(TourTagCategory.class, id);
//				if (categoryInDb != null) {
//
//					categoryInDb.setName(tagName);
//					em.merge(categoryInDb);
//				}
//			}
//			ts.commit();
//
//		} catch (final Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (ts.isActive()) {
//				ts.rollback();
//			} else {
//				isSaved = true;
//			}
//			em.close();
//		}
//
//		if (isSaved == false) {
//			MessageDialog.openError(Display.getCurrent().getActiveShell(),//
//					"Error", "Error occured when saving an entity"); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//
//		return isSaved;
//	}
//
//	private static boolean updateTag(final long id, final String tagName) {
//
//		final EntityManager em = TourDatabase.getInstance().getEntityManager();
//
//		boolean isSaved = false;
//		final EntityTransaction ts = em.getTransaction();
//
//		try {
//
//			ts.begin();
//			{
//				final TourTag tagInDb = em.find(TourTag.class, id);
//				if (tagInDb != null) {
//
//					tagInDb.setTagName(tagName);
//					em.merge(tagInDb);
//				}
//			}
//			ts.commit();
//
//		} catch (final Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (ts.isActive()) {
//				ts.rollback();
//			} else {
//				isSaved = true;
//			}
//			em.close();
//		}
//
//		if (isSaved == false) {
//			MessageDialog.openError(Display.getCurrent().getActiveShell(),//
//					"Error", "Error occured when saving an entity"); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//
//		return isSaved;
//	}
//
//	/**
//	 * Rename selected tag/category
//	 */
//	@Override
//	public void run() {
//
//		final StructuredSelection selection = (StructuredSelection) fTourViewer.getViewer().getSelection();
//		final Object firstElement = selection.getFirstElement();
//
//		String name = UI.EMPTY_STRING;
//		String dlgTitle = UI.EMPTY_STRING;
//		String dlgMessage = UI.EMPTY_STRING;
//
//		if (firstElement instanceof TVITagView_Tag) {
//
//			name = ((TVITagView_Tag) firstElement).getName();
//			dlgTitle = Messages.action_tag_dlg_rename_title;
//			dlgMessage = Messages.action_tag_dlg_rename_message;
//
//		} else if (firstElement instanceof TVITagView_TagCategory) {
//
//			name = ((TVITagView_TagCategory) firstElement).getName();
//			dlgTitle = Messages.action_tagcategory_dlg_rename_title;
//			dlgMessage = Messages.action_tagcategory_dlg_rename_message;
//		} else {
//			return;
//		}
//
//		final InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(),
//				dlgTitle,
//				dlgMessage,
//				name,
//				null);
//
//		inputDialog.open();
//
//		if (inputDialog.getReturnCode() != Window.OK) {
//			return;
//		}
//
//		final String newName = inputDialog.getValue().trim();
//		if (name.equals(newName)) {
//			// name was not changed
//			return;
//		}
//
//		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
//			public void run() {
//
//				if (firstElement instanceof TVITagView_Tag) {
//
//					// save tag
//
//					final TVITagView_Tag tourTagItem = ((TVITagView_Tag) firstElement);
//
//					// update model
//					updateTag(tourTagItem.getTagId(), newName);
//
//					// update view
//					fTourViewer.getViewer().update(tourTagItem, null);
//
//				} else if (firstElement instanceof TVITagView_TagCategory) {
//
//					// save category
//
//					final TVITagView_TagCategory tourCategoryItem = ((TVITagView_TagCategory) firstElement);
//
//					// update model
//					updateCategory(tourCategoryItem.getCategoryId(), newName);
//
//					// update view
//					fTourViewer.getViewer().update(tourCategoryItem, null);
//
//				}
//
//				// remove old tags from internal list
//				TourDatabase.clearTourTags();
//				SignMenuManager.updateRecentTagNames();
//
//				TourManager.getInstance().clearTourDataCache();
//
//				// fire modify event
//				TourManager.fireEvent(TourEventId.TAG_STRUCTURE_CHANGED);
//			}
//		});
//	}
}
