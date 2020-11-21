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

public class ActionMenuSetAllSignStructures_DISABLED /* extends Action implements IMenuCreator */ {

//	private Menu		fMenu;
//
//	private TaggingView	fTagView;
//
//	private class ActionSetTagStructure extends Action {
//
//		private int	fExpandType;
//
//		private ActionSetTagStructure(final int expandType, final String name) {
//
//			super(name, AS_CHECK_BOX);
//			fExpandType = expandType;
//		}
//
//		@Override
//		public void run() {
//
//			if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
//					Messages.action_tag_set_all_confirm_title,
//					Messages.action_tag_set_all_confirm_message) == false) {
//				return;
//			}
//
//			final Runnable runnable = new Runnable() {
//
//				public void run() {
//
//					final EntityManager em = TourDatabase.getInstance().getEntityManager();
//					try {
//
//						/*
//						 * update all tags which has not the current expand type
//						 */
//
//						final HashMap<Long, TourTag> allTourTags = TourDatabase.getAllTourTags();
//						for (final TourTag tourTag : allTourTags.values()) {
//
//							if (tourTag.getExpandType() != fExpandType) {
//
//								// set new expand type
//
//								final Long tagId = tourTag.getTagId();
//								final TourTag tagInDb = em.find(TourTag.class, tagId);
//								if (tagInDb != null) {
//
//									tagInDb.setExpandType(fExpandType);
//
//									final TourTag savedEntity = TourDatabase.saveEntity(tagInDb,
//											tagId,
//											TourTag.class,
//											em);
//
//									if (savedEntity != null) {
//
//										// set entity from the database into the all tag list
//
//										allTourTags.put(tagId, savedEntity);
//									}
//								}
//							}
//						}
//
//					} catch (final Exception e) {
//						e.printStackTrace();
//					} finally {
//
//						em.close();
//					}
//
//					fTagView.reloadViewer();
//				}
//
//			};
//			BusyIndicator.showWhile(Display.getCurrent(), runnable);
//		}
//	}
//
//	public ActionMenuSetAllSignStructures(final TaggingView tagView) {
//
//		super(Messages.action_tag_set_all_tag_structures, AS_DROP_DOWN_MENU);
//		setMenuCreator(this);
//
//		fTagView = tagView;
//	}
//
//	private void addActionToMenu(final Action action) {
//
//		final ActionContributionItem item = new ActionContributionItem(action);
//		item.fill(fMenu, -1);
//	}
//
//	public void dispose() {
//		if (fMenu != null) {
//			fMenu.dispose();
//			fMenu = null;
//		}
//	}
//
//	public Menu getMenu(final Control parent) {
//		return null;
//	}
//
//	public Menu getMenu(final Menu parent) {
//
//		dispose();
//		fMenu = new Menu(parent);
//
//		// Add listener to repopulate the menu each time
//		fMenu.addMenuListener(new MenuAdapter() {
//			@Override
//			public void menuShown(final MenuEvent e) {
//
//				final Menu menu = (Menu) e.widget;
//
//				// dispose old items
//				for (final MenuItem menuItem : menu.getItems()) {
//					menuItem.dispose();
//				}
//
//				/*
//				 * create all expand types
//				 */
//				int typeIndex = 0;
//				for (final int expandType : SignManager.EXPAND_TYPES) {
//
//					final ActionSetTagStructure actionTagStructure = new ActionSetTagStructure(expandType,
//							SignManager.EXPAND_TYPE_NAMES[typeIndex++]);
//
//					addActionToMenu(actionTagStructure);
//				}
//			}
//		});
//
//		return fMenu;
//	}

}
