/*******************************************************************************
 * Copyright (C) 2005, 2011  Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourCatalog;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.TourData;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourType;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tag.TagMenuManager;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.TourDoubleClickState;
import net.tourbook.tour.TourEvent;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.tour.TourTypeMenuManager;
import net.tourbook.ui.IReferenceTourProvider;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.TreeColumnFactory;
import net.tourbook.ui.TreeViewerItem;
import net.tourbook.ui.UI;
import net.tourbook.ui.action.ActionCollapseAll;
import net.tourbook.ui.action.ActionCollapseOthers;
import net.tourbook.ui.action.ActionEditQuick;
import net.tourbook.ui.action.ActionEditTour;
import net.tourbook.ui.action.ActionExpandSelection;
import net.tourbook.ui.action.ActionModifyColumns;
import net.tourbook.ui.action.ActionOpenTour;
import net.tourbook.ui.action.ActionRefreshView;
import net.tourbook.ui.action.ActionSetTourTypeMenu;
import net.tourbook.ui.views.TourInfoToolTipCellLabelProvider;
import net.tourbook.ui.views.TourInfoToolTipStyledCellLabelProvider;
import net.tourbook.ui.views.TreeViewerTourInfoToolTip;
import net.tourbook.util.ColumnManager;
import net.tourbook.util.ITourViewer;
import net.tourbook.util.PixelConverter;
import net.tourbook.util.PostSelectionProvider;
import net.tourbook.util.TreeColumnDefinition;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

public class TourCatalogView extends ViewPart implements ITourViewer, ITourProvider, IReferenceTourProvider {

	public static final String			ID									= "net.tourbook.views.tourCatalog.TourCatalogView"; //$NON-NLS-1$

	public static final int				COLUMN_LABEL						= 0;
	public static final int				COLUMN_SPEED						= 1;

	private static final String			MEMENTO_TOUR_CATALOG_ACTIVE_REF_ID	= "tour.catalog.active.ref.id";					//$NON-NLS-1$
	private static final String			MEMENTO_TOUR_CATALOG_LINK_TOUR		= "tour.catalog.link.tour";						//$NON-NLS-1$

	private final IPreferenceStore		_prefStore							= TourbookPlugin
																					.getDefault()
																					.getPreferenceStore();
	private final IDialogSettings		_state								= TourbookPlugin
																					.getDefault()
																					.getDialogSettingsSection(ID);

	private TVICatalogRootItem			_rootItem;

	private final NumberFormat			_nf									= NumberFormat.getNumberInstance();

	private PostSelectionProvider		_postSelectionProvider;

	private ISelectionListener			_postSelectionListener;
	private IPartListener2				_partListener;
	private ITourEventListener			_compareTourPropertyListener;
	private IPropertyChangeListener		_prefChangeListener;
	private ITourEventListener			_tourEventListener;

	/**
	 * tour item which is selected by the link tour action
	 */
	protected TVICatalogComparedTour	_linkedTour;

	/**
	 * ref id which is currently selected in the tour viewer
	 */
	private long						_activeRefId;

	/**
	 * flag if actions are added to the toolbar
	 */
	private boolean						_isToolbarCreated					= false;

	private ColumnManager				_columnManager;

	private boolean						_isToolTipInRefTour;
	private boolean						_isToolTipInTitle;
	private boolean						_isToolTipInTags;

	private TagMenuManager				_tagMenuMgr;
	private TourDoubleClickState		_tourDoubleClickState				= new TourDoubleClickState();

	/*
	 * UI controls
	 */
	private Composite					_viewerContainer;
	private TreeViewer					_tourViewer;

	private ActionRemoveComparedTours	_actionRemoveComparedTours;
	private ActionRenameRefTour			_actionRenameRefTour;
	private ActionLinkTour				_actionLinkTour;
	private ActionCollapseAll			_actionCollapseAll;
	private ActionCollapseOthers		_actionCollapseOthers;
	private ActionExpandSelection		_actionExpandSelection;
	private ActionRefreshView			_actionRefreshView;
	private ActionModifyColumns			_actionModifyColumns;
	private ActionEditQuick				_actionEditQuick;
	private ActionEditTour				_actionEditTour;
	private ActionSetTourTypeMenu		_actionSetTourType;

	private ActionTourCompareWizard		_actionTourCompareWizard;
	private ActionOpenTour				_actionOpenTour;

	class TourContentProvider implements ITreeContentProvider {

		public void dispose() {}

		public Object[] getChildren(final Object parentElement) {
			return ((TreeViewerItem) parentElement).getFetchedChildrenAsArray();
		}

		public Object[] getElements(final Object inputElement) {
			return _rootItem.getFetchedChildrenAsArray();
		}

		public Object getParent(final Object element) {
			return ((TreeViewerItem) element).getParentItem();
		}

		public boolean hasChildren(final Object element) {
			return ((TreeViewerItem) element).hasChildren();
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
	}

	public TourCatalogView() {}

	/**
	 * !!! Recursive !!!<br>
	 * <br>
	 * Find the compared tours in the tour map tree viewer<br>
	 * <br>
	 * !!! Recursive !!!<br>
	 * 
	 * @param comparedTours
	 * @param parentItem
	 * @param findCompIds
	 *            comp id's which should be found
	 */
	private static void getComparedTours(	final ArrayList<TVICatalogComparedTour> comparedTours,
											final TreeViewerItem parentItem,
											final ArrayList<Long> findCompIds) {

		final ArrayList<TreeViewerItem> unfetchedChildren = parentItem.getUnfetchedChildren();

		if (unfetchedChildren != null) {

			// children are available

			for (final TreeViewerItem tourTreeItem : unfetchedChildren) {

				if (tourTreeItem instanceof TVICatalogComparedTour) {

					final TVICatalogComparedTour ttiCompResult = (TVICatalogComparedTour) tourTreeItem;
					final long ttiCompId = ttiCompResult.getCompId();

					for (final Long compId : findCompIds) {
						if (ttiCompId == compId) {
							comparedTours.add(ttiCompResult);
						}
					}

				} else {
					// this is a child which can be the parent for other childs
					getComparedTours(comparedTours, tourTreeItem, findCompIds);
				}
			}
		}
	}

	private void addCompareTourPropertyListener() {

		_compareTourPropertyListener = new ITourEventListener() {
			public void tourChanged(final IWorkbenchPart part, final TourEventId propertyId, final Object propertyData) {

				if (propertyId == TourEventId.COMPARE_TOUR_CHANGED
						&& propertyData instanceof TourPropertyCompareTourChanged) {

					final TourPropertyCompareTourChanged compareTourProperty = (TourPropertyCompareTourChanged) propertyData;

					// check if the compared tour was saved in the database
					if (compareTourProperty.isDataSaved) {

						final ArrayList<Long> compareIds = new ArrayList<Long>();
						compareIds.add(compareTourProperty.compareId);

						// find the compared tour in the viewer
						final ArrayList<TVICatalogComparedTour> comparedTours = new ArrayList<TVICatalogComparedTour>();

						getComparedTours(comparedTours, _rootItem, compareIds);

						if (comparedTours.size() > 0) {

							final TVICatalogComparedTour comparedTour = comparedTours.get(0);

							// update entity
							comparedTour.setStartIndex(compareTourProperty.startIndex);
							comparedTour.setEndIndex(compareTourProperty.endIndex);
							comparedTour.setTourSpeed(compareTourProperty.speed);

							// update the viewer
							_tourViewer.update(comparedTour, null);
						}
					}
				}
			}
		};

		TourManager.getInstance().addTourEventListener(_compareTourPropertyListener);
	}

	private void addPartListener() {

		_partListener = new IPartListener2() {
			public void partActivated(final IWorkbenchPartReference partRef) {}

			public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

			public void partClosed(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourCatalogView.this) {

					saveState();

//					TourManager.fireEvent(TourEventId.CLEAR_DISPLAYED_TOUR, null, TourCatalogView.this);
				}
			}

			public void partDeactivated(final IWorkbenchPartReference partRef) {}

			public void partHidden(final IWorkbenchPartReference partRef) {}

			public void partInputChanged(final IWorkbenchPartReference partRef) {}

			public void partOpened(final IWorkbenchPartReference partRef) {
				/*
				 * add the actions in the part open event so they are appended AFTER the actions
				 * which are defined in the plugin.xml
				 */
				fillToolbar();
			}

			public void partVisible(final IWorkbenchPartReference partRef) {}
		};

		getViewSite().getPage().addPartListener(_partListener);
	}

	private void addPostSelectionListener() {

		// this view part is a selection listener
		_postSelectionListener = new ISelectionListener() {

			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {

				// update the view when a new tour reference was created
				if (selection instanceof SelectionPersistedCompareResults) {

					final SelectionPersistedCompareResults selectionPersisted = (SelectionPersistedCompareResults) selection;

					final ArrayList<TVICompareResultComparedTour> persistedCompareResults = selectionPersisted.persistedCompareResults;

					if (persistedCompareResults.size() > 0) {
						updateTourViewer(persistedCompareResults);
					}

				} else if (selection instanceof SelectionNewRefTours) {

					reloadViewer();

				} else if (selection instanceof SelectionRemovedComparedTours) {

					final SelectionRemovedComparedTours removedCompTours = (SelectionRemovedComparedTours) selection;

					/*
					 * find/remove the removed compared tours in the viewer
					 */

					final ArrayList<TVICatalogComparedTour> comparedTours = new ArrayList<TVICatalogComparedTour>();

					getComparedTours(comparedTours, _rootItem, removedCompTours.removedComparedTours);

					// remove compared tour from the data model
					for (final TVICatalogComparedTour comparedTour : comparedTours) {
						comparedTour.remove();
					}

					// remove compared tour from the tree viewer
					_tourViewer.remove(comparedTours.toArray());
					reloadViewer();

				} else if (selection instanceof StructuredSelection) {

					final StructuredSelection structuredSelection = (StructuredSelection) selection;

					final Object firstElement = structuredSelection.getFirstElement();

					if (firstElement instanceof TVICatalogComparedTour) {

						// select the compared tour in the tour viewer

						final TVICatalogComparedTour linkedTour = (TVICatalogComparedTour) firstElement;

						// check if the linked tour is already set, prevent recursion
						if (_linkedTour != linkedTour) {
							_linkedTour = linkedTour;
							selectLinkedTour();
						}
					}
				}
			}
		};

		// register selection listener in the page
		getSite().getPage().addPostSelectionListener(_postSelectionListener);
	}

	private void addPrefListener() {

		_prefChangeListener = new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {

				final String property = event.getProperty();

				if (property.equals(ITourbookPreferences.MEASUREMENT_SYSTEM)) {

					// measurement system has changed

					UI.updateUnits();

					_columnManager.saveState(_state);
					_columnManager.clearColumns();
					defineAllColumns(_viewerContainer);

					_tourViewer = (TreeViewer) recreateViewer(_tourViewer);

				} else if (property.equals(ITourbookPreferences.VIEW_TOOLTIP_IS_MODIFIED)) {

					updateToolTipState();

				} else if (property.equals(ITourbookPreferences.TOUR_TYPE_LIST_IS_MODIFIED)) {

					// update tourbook viewer
					_tourViewer.refresh();

				} else if (property.equals(ITourbookPreferences.VIEW_LAYOUT_CHANGED)) {

					_tourViewer.getTree().setLinesVisible(
							_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

					_tourViewer.refresh();

					/*
					 * the tree must be redrawn because the styled text does not show with the new
					 * color
					 */
					_tourViewer.getTree().redraw();
				}
			}
		};
		_prefStore.addPropertyChangeListener(_prefChangeListener);
	}

	private void addTourEventListener() {

		_tourEventListener = new ITourEventListener() {
			public void tourChanged(final IWorkbenchPart part, final TourEventId eventId, final Object eventData) {

				if (part == TourCatalogView.this) {
					return;
				}

				if (eventId == TourEventId.TOUR_CHANGED && eventData instanceof TourEvent) {

					// get a clone of the modified tours because the tours are removed from the list
					final ArrayList<TourData> modifiedTours = ((TourEvent) eventData).getModifiedTours();
					if (modifiedTours != null) {
						updateTourViewer(_rootItem, modifiedTours);
					}

				} else if (eventId == TourEventId.TAG_STRUCTURE_CHANGED
						|| eventId == TourEventId.REFERENCE_TOUR_IS_CREATED) {

					reloadViewer();
				}
			}
		};
		TourManager.getInstance().addTourEventListener(_tourEventListener);
	}

	private void createActions() {

		_actionRemoveComparedTours = new ActionRemoveComparedTours(this);
		_actionRenameRefTour = new ActionRenameRefTour(this);
		_actionTourCompareWizard = new ActionTourCompareWizard(this);
		_actionLinkTour = new ActionLinkTour(this);
		_actionRefreshView = new ActionRefreshView(this);
		_actionModifyColumns = new ActionModifyColumns(this);

		_actionCollapseOthers = new ActionCollapseOthers(this);
		_actionExpandSelection = new ActionExpandSelection(this);
		_actionCollapseAll = new ActionCollapseAll(this);

		_actionEditQuick = new ActionEditQuick(this);
		_actionEditTour = new ActionEditTour(this);
		_actionOpenTour = new ActionOpenTour(this);

		_actionSetTourType = new ActionSetTourTypeMenu(this);
		_tagMenuMgr = new TagMenuManager(this, true);
	}

	/**
	 * create the views context menu
	 */
	private void createContextMenu() {

		final Control controlMenuParent = _tourViewer.getControl();

		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		final Menu contextMenu = menuMgr.createContextMenu(controlMenuParent);
		contextMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuHidden(final MenuEvent e) {
				_tagMenuMgr.onHideMenu();
			}

			@Override
			public void menuShown(final MenuEvent menuEvent) {
				_tagMenuMgr.onShowMenu(menuEvent, controlMenuParent, Display.getCurrent().getCursorLocation());
			}
		});

		// add the context menu to the table viewer
		controlMenuParent.setMenu(contextMenu);
	}

	@Override
	public void createPartControl(final Composite parent) {

		// define all columns for the viewer
		_columnManager = new ColumnManager(this, _state);
		defineAllColumns(parent);

		_viewerContainer = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(_viewerContainer);

		createTourViewer(_viewerContainer);

		createActions();
		fillViewMenu();

		addPartListener();
		addPostSelectionListener();
		addCompareTourPropertyListener();
		addTourEventListener();
		addPrefListener();

		// set selection provider
		getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider());

		_tourViewer.setInput(_rootItem = new TVICatalogRootItem(this));

		restoreState();

		// move the horizontal scrollbar to the left border
		final ScrollBar horizontalBar = _tourViewer.getTree().getHorizontalBar();
		if (horizontalBar != null) {
			horizontalBar.setSelection(0);
		}
	}

	private void createTourViewer(final Composite parent) {

		// tour tree
		final Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FLAT | SWT.MULTI | SWT.FULL_SELECTION);

		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tree.setHeaderVisible(true);
		tree.setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

		_tourViewer = new TreeViewer(tree);
		_columnManager.createColumns(_tourViewer);

		_tourViewer.setContentProvider(new TourContentProvider());
		_tourViewer.setUseHashlookup(true);

		_tourViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				onSelectionChanged((IStructuredSelection) event.getSelection());
			}
		});

		_tourViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {

				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();

				final Object tourItem = selection.getFirstElement();

				/*
				 * get tour id
				 */
				long tourId = -1;
				if (tourItem instanceof TVICatalogComparedTour) {
					tourId = ((TVICatalogComparedTour) tourItem).getTourId();
				}

				if (tourId != -1) {
					TourManager.getInstance().tourDoubleClickAction(TourCatalogView.this, _tourDoubleClickState);
				} else {
					// expand/collapse current item
					if (_tourViewer.getExpandedState(tourItem)) {
						_tourViewer.collapseToLevel(tourItem, 1);
					} else {
						_tourViewer.expandToLevel(tourItem, 1);
					}
				}
			}
		});

		createContextMenu();

		// set tour info tooltip provider
		new TreeViewerTourInfoToolTip(_tourViewer);
	}

	private void defineAllColumns(final Composite parent) {

		final PixelConverter pc = new PixelConverter(parent);

		defineColumn1stColumn(pc);
		defineColumnTourType(pc);
		defineColumnTitle(pc);
		defineColumnTags(pc);
		defineColumnSpeed(pc);

	}

	/**
	 * first column: ref tour name/compare tour name /year
	 */
	private void defineColumn1stColumn(final PixelConverter pc) {

		final TreeColumnDefinition colDef = TreeColumnFactory.REF_TOUR.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setCanModifyVisibility(false);
		colDef.setLabelProvider(new TourInfoToolTipStyledCellLabelProvider() {

			@Override
			public Long getTourId(final ViewerCell cell) {

				if (_isToolTipInRefTour == false) {
					return null;
				}

				final Object element = cell.getElement();

				if ((element instanceof TVICatalogRefTourItem)) {

					// ref tour item

					return ((TVICatalogRefTourItem) element).getTourId();

				} else if (element instanceof TVICatalogComparedTour) {

					// compared tour item

					return ((TVICatalogComparedTour) element).getTourId();

				}

				return null;
			}

			@Override
			public void update(final ViewerCell cell) {

				final Object element = cell.getElement();

				if ((element instanceof TVICatalogRefTourItem)) {

					// ref tour item

					final TVICatalogRefTourItem refItem = (TVICatalogRefTourItem) element;

					final StyledString styledString = new StyledString();
					styledString.append(refItem.label, UI.TAG_STYLER);

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());

				} else if (element instanceof TVICatalogYearItem) {

					// year item

					final TVICatalogYearItem yearItem = (TVICatalogYearItem) element;
					final StyledString styledString = new StyledString();
					styledString.append(Integer.toString(yearItem.year), UI.TAG_SUB_STYLER);
					styledString.append("   " + yearItem.tourCounter, StyledString.QUALIFIER_STYLER); //$NON-NLS-1$

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());

				} else if (element instanceof TVICatalogComparedTour) {

					// compared tour item

					cell.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(
							((TVICatalogComparedTour) element).getTourDate()));

				}
			}
		});
	}

	/**
	 * column: speed
	 */
	private void defineColumnSpeed(final PixelConverter pc) {

		final TreeColumnDefinition colDef = TreeColumnFactory.SPEED.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof TVICatalogComparedTour) {

					_nf.setMinimumFractionDigits(1);
					_nf.setMaximumFractionDigits(1);

					final float speed = ((TVICatalogComparedTour) element).getTourSpeed();
					if (speed > 0) {
						cell.setText(_nf.format(speed / UI.UNIT_VALUE_DISTANCE));
					}
				}
			}
		});
	}

	/**
	 * column: tags
	 */
	private void defineColumnTags(final PixelConverter pc) {

		final TreeColumnDefinition colDef = TreeColumnFactory.TOUR_TAGS.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new TourInfoToolTipCellLabelProvider() {

			@Override
			public Long getTourId(final ViewerCell cell) {

				if (_isToolTipInTags == false) {
					return null;
				}

				final Object element = cell.getElement();
				if (element instanceof TVICatalogComparedTour) {
					return ((TVICatalogComparedTour) element).getTourId();
				}

				return null;
			}

			@Override
			public void update(final ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof TVICatalogComparedTour) {
					cell.setText(TourDatabase.getTagNames(((TVICatalogComparedTour) element).tagIds));
				}
			}
		});
	}

	/**
	 * column: title
	 */
	private void defineColumnTitle(final PixelConverter pc) {

		final TreeColumnDefinition colDef = TreeColumnFactory.TITLE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new TourInfoToolTipCellLabelProvider() {

			@Override
			public Long getTourId(final ViewerCell cell) {

				if (_isToolTipInTitle == false) {
					return null;
				}

				final Object element = cell.getElement();
				if (element instanceof TVICatalogComparedTour) {
					return ((TVICatalogComparedTour) element).getTourId();
				}

				return null;
			}

			@Override
			public void update(final ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof TVICatalogComparedTour) {
					cell.setText(((TVICatalogComparedTour) element).tourTitle);
				}
			}
		});
	}

	/**
	 * column: tour type
	 */
	private void defineColumnTourType(final PixelConverter pc) {

		final TreeColumnDefinition colDef = TreeColumnFactory.TOUR_TYPE.createColumn(_columnManager, pc);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof TVICatalogComparedTour) {
					cell.setImage(UI.getInstance().getTourTypeImage(((TVICatalogComparedTour) element).tourTypeId));
				}
			}
		});
	}

	@Override
	public void dispose() {

		getSite().getPage().removePostSelectionListener(_postSelectionListener);
		getViewSite().getPage().removePartListener(_partListener);

		TourManager.getInstance().removeTourEventListener(_compareTourPropertyListener);
		TourManager.getInstance().removeTourEventListener(_tourEventListener);

		_prefStore.removePropertyChangeListener(_prefChangeListener);

		super.dispose();
	}

	private void enableActions() {

		final ITreeSelection selection = (ITreeSelection) _tourViewer.getSelection();

		int refItems = 0;
		int yearItems = 0;
		int tourItems = 0;
		TVICatalogComparedTour firstTourItem = null;

		// count number of items
		for (final Iterator<?> iter = selection.iterator(); iter.hasNext();) {

			final Object treeItem = iter.next();

			if (treeItem instanceof TVICatalogRefTourItem) {
				refItems++;
			} else if (treeItem instanceof TVICatalogComparedTour) {
				if (tourItems == 0) {
					firstTourItem = (TVICatalogComparedTour) treeItem;
				}
				tourItems++;
			} else if (treeItem instanceof TVICatalogYearItem) {
				yearItems++;
			}
		}

		final boolean isTourSelected = tourItems > 0;
		final boolean isOneTour = tourItems == 1 && refItems == 0 && yearItems == 0;
		final boolean isOneRefTour = refItems == 1 && yearItems == 0 && tourItems == 0;
		final boolean isEditableTour = isOneTour || isOneRefTour;

		final int selectedItems = selection.size();
		final TreeViewerItem firstElement = (TreeViewerItem) selection.getFirstElement();
		final boolean firstElementHasChildren = firstElement == null ? false : firstElement.hasChildren();

		_tourDoubleClickState.canEditTour = isEditableTour;
		_tourDoubleClickState.canOpenTour = isEditableTour;
		_tourDoubleClickState.canQuickEditTour = isEditableTour;
		_tourDoubleClickState.canEditMarker = isEditableTour;
		_tourDoubleClickState.canAdjustAltitude = isEditableTour;

		_actionRemoveComparedTours.setEnabled(isOneTour);

		_actionTourCompareWizard.setEnabled(refItems > 0);

		// enable remove button when only one type of item is selected
		if (yearItems == 0 && ((refItems > 0 && tourItems == 0) || (refItems == 0 & tourItems > 0))) {
			_actionRemoveComparedTours.setEnabled(true);
		} else {
			_actionRemoveComparedTours.setEnabled(false);
		}

		_actionEditQuick.setEnabled(isEditableTour);
		_actionEditTour.setEnabled(isEditableTour);
		_actionOpenTour.setEnabled(isEditableTour);

		_actionRenameRefTour.setEnabled(refItems == 1 && tourItems == 0 && yearItems == 0);

		final ArrayList<TourType> tourTypes = TourDatabase.getAllTourTypes();
		_actionSetTourType.setEnabled(isTourSelected && tourTypes.size() > 0);

		_actionExpandSelection.setEnabled(firstElement == null ? false : //
				selectedItems == 1 ? firstElementHasChildren : //
						true);

		_actionCollapseOthers.setEnabled(selectedItems == 1 && firstElementHasChildren);

		_tagMenuMgr.enableTagActions(isTourSelected, isOneTour, firstTourItem == null ? null : firstTourItem.tagIds);

		TourTypeMenuManager.enableRecentTourTypeActions(isTourSelected, isOneTour
				? firstTourItem.tourTypeId
				: TourDatabase.ENTITY_IS_NOT_SAVED);
	}

	private void fillContextMenu(final IMenuManager menuMgr) {

		menuMgr.add(_actionCollapseOthers);
		menuMgr.add(_actionExpandSelection);
		menuMgr.add(_actionCollapseAll);

		menuMgr.add(new Separator());
		menuMgr.add(_actionTourCompareWizard);
		menuMgr.add(_actionRenameRefTour);

		menuMgr.add(new Separator());
		menuMgr.add(_actionEditQuick);
		menuMgr.add(_actionEditTour);
		menuMgr.add(_actionOpenTour);

		// tour tag actions
		_tagMenuMgr.fillTagMenu(menuMgr);

		// tour type actions
		menuMgr.add(new Separator());
		menuMgr.add(_actionSetTourType);
		TourTypeMenuManager.fillMenuWithRecentTourTypes(menuMgr, this, true);

		menuMgr.add(new Separator());
		menuMgr.add(_actionRemoveComparedTours);

		enableActions();
	}

	private void fillToolbar() {

		// check if toolbar is created
		if (_isToolbarCreated) {
			return;
		}
		_isToolbarCreated = true;

		final IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();

		tbm.add(_actionLinkTour);
		tbm.add(_actionCollapseAll);
		tbm.add(_actionRefreshView);

		tbm.update(true);
	}

	private void fillViewMenu() {

		/*
		 * fill view menu
		 */
		final IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();

		menuMgr.add(_actionModifyColumns);
	}

	void fireSelection(final ISelection selection) {
		_postSelectionProvider.setSelection(selection);
	}

	public ColumnManager getColumnManager() {
		return _columnManager;
	}

	public ArrayList<Long> getSelectedReferenceTours() {

		final IStructuredSelection selectedItems = ((IStructuredSelection) _tourViewer.getSelection());
		final ArrayList<Long> selectedReferenceTour = new ArrayList<Long>();

		// loop: all selected items
		for (final Iterator<?> iter = selectedItems.iterator(); iter.hasNext();) {
			final Object treeItem = iter.next();

			if (treeItem instanceof TVICatalogRefTourItem) {
				selectedReferenceTour.add(((TVICatalogRefTourItem) treeItem).refId);
			}
		}

		return selectedReferenceTour;
	}

	public ArrayList<TourData> getSelectedTours() {

		// get selected tours

		final IStructuredSelection selectedTours = ((IStructuredSelection) _tourViewer.getSelection());
		final ArrayList<TourData> selectedTourData = new ArrayList<TourData>();

		// loop: all selected items
		for (final Iterator<?> iter = selectedTours.iterator(); iter.hasNext();) {

			final Object treeItem = iter.next();
			if (treeItem instanceof TVICatalogComparedTour) {

				final TVICatalogComparedTour tourItem = ((TVICatalogComparedTour) treeItem);

				final TourData tourData = TourManager.getInstance().getTourData(tourItem.getTourId());
				if (tourData != null) {
					selectedTourData.add(tourData);
				}

			} else if (treeItem instanceof TVICatalogRefTourItem) {

				final TVICatalogRefTourItem refItem = (TVICatalogRefTourItem) treeItem;

				final TourData tourData = TourManager.getInstance().getTourData(refItem.getTourId());
				if (tourData != null) {
					selectedTourData.add(tourData);
				}
			}
		}

		return selectedTourData;
	}

	public TreeViewer getTourViewer() {
		return _tourViewer;
	}

	public ColumnViewer getViewer() {
		return _tourViewer;
	}

	/**
	 * Selection changes in the tour map viewer
	 * 
	 * @param selection
	 */
	private void onSelectionChanged(final IStructuredSelection selection) {

		// show the reference tour chart
		final Object item = selection.getFirstElement();

		if (item instanceof TVICatalogRefTourItem) {

			// reference tour is selected

			final TVICatalogRefTourItem refItem = (TVICatalogRefTourItem) item;

			_activeRefId = refItem.refId;

			// fire selection for the selected tour catalog item
			_postSelectionProvider.setSelection(new SelectionTourCatalogView(refItem));

		} else if (item instanceof TVICatalogYearItem) {

			// year item is selected

			final TVICatalogYearItem yearItem = (TVICatalogYearItem) item;

			_activeRefId = yearItem.refId;

			// fire selection for the selected tour catalog item
			_postSelectionProvider.setSelection(new SelectionTourCatalogView(yearItem));

			/*
			 * get selection from year statistic view, this selection is set from the previous fired
			 * selection
			 */
			final ISelection selectionInTourChart = getSite()
					.getWorkbenchWindow()
					.getSelectionService()
					.getSelection(YearStatisticView.ID);

			_postSelectionProvider.setSelection(selectionInTourChart);

		} else if (item instanceof TVICatalogComparedTour) {

			// compared tour is selected

			final TVICatalogComparedTour compItem = (TVICatalogComparedTour) item;

			_activeRefId = compItem.getRefId();

			// fire selection for the selected tour catalog item
			_postSelectionProvider.setSelection(new StructuredSelection(compItem));
		}
	}

	public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

		final Object[] expandedElements = _tourViewer.getExpandedElements();
		final ISelection selection = _tourViewer.getSelection();

		_viewerContainer.setRedraw(false);
		{
			_tourViewer.getTree().dispose();

			createTourViewer(_viewerContainer);
			_viewerContainer.layout();

			_tourViewer.setInput(_rootItem = new TVICatalogRootItem(this));

			_tourViewer.setExpandedElements(expandedElements);
			_tourViewer.setSelection(selection);
		}
		_viewerContainer.setRedraw(true);

		return _tourViewer;
	}

	public void reloadViewer() {

		final Tree tree = _tourViewer.getTree();
		tree.setRedraw(false);
		{
			final Object[] expandedElements = _tourViewer.getExpandedElements();
			final ISelection selection = _tourViewer.getSelection();

			_tourViewer.setInput(_rootItem = new TVICatalogRootItem(this));

			_tourViewer.setExpandedElements(expandedElements);
			_tourViewer.setSelection(selection);
		}
		tree.setRedraw(true);
	}

	private void restoreState() {

		// selected ref tour in tour viewer
		final String mementoRefId = _state.get(MEMENTO_TOUR_CATALOG_ACTIVE_REF_ID);
		if (mementoRefId != null) {
			try {
				selectRefTour(Long.parseLong(mementoRefId));
			} catch (final NumberFormatException e) {
				// do nothing
			}
		}

		// action: link tour with statistics
		_actionLinkTour.setChecked(_state.getBoolean(MEMENTO_TOUR_CATALOG_LINK_TOUR));

		updateToolTipState();
	}

	public void saveState() {

		_state.put(MEMENTO_TOUR_CATALOG_ACTIVE_REF_ID, Long.toString(_activeRefId));
		_state.put(MEMENTO_TOUR_CATALOG_LINK_TOUR, _actionLinkTour.isChecked());

		_columnManager.saveState(_state);
	}

	/**
	 * select the tour which was selected in the year chart
	 */
	void selectLinkedTour() {
		if (_linkedTour != null && _actionLinkTour.isChecked()) {
			_tourViewer.setSelection(new StructuredSelection((_linkedTour)), true);
		}
	}

	/**
	 * Select the reference tour in the tour viewer
	 * 
	 * @param refId
	 */
	private void selectRefTour(final long refId) {

		final Object[] refTourItems = _rootItem.getFetchedChildrenAsArray();

		// search ref tour
		for (final Object refTourItem : refTourItems) {
			if (refTourItem instanceof TVICatalogRefTourItem) {

				final TVICatalogRefTourItem tvtiRefTour = (TVICatalogRefTourItem) refTourItem;
				if (tvtiRefTour.refId == refId) {

					// select ref tour
					_tourViewer.setSelection(new StructuredSelection(tvtiRefTour), true);
					break;
				}
			}
		}
	}

	@Override
	public void setFocus() {
//		fTourViewer.getTree().setFocus();
	}

	private void updateToolTipState() {

		_isToolTipInRefTour = _prefStore.getBoolean(ITourbookPreferences.VIEW_TOOLTIP_TOURCATALOG_REFTOUR);
		_isToolTipInTitle = _prefStore.getBoolean(ITourbookPreferences.VIEW_TOOLTIP_TOURCATALOG_TITLE);
		_isToolTipInTags = _prefStore.getBoolean(ITourbookPreferences.VIEW_TOOLTIP_TOURCATALOG_TAGS);
	}

	/**
	 * Update viewer with new saved compared tours
	 * 
	 * @param persistedCompareResults
	 */
	private void updateTourViewer(final ArrayList<TVICompareResultComparedTour> persistedCompareResults) {

		// ref id's which hast new children
		final HashMap<Long, Long> viewRefIds = new HashMap<Long, Long>();

		// get all ref tours which needs to be updated
		for (final TVICompareResultComparedTour compareResult : persistedCompareResults) {

			if (compareResult.getParentItem() instanceof TVICompareResultReferenceTour) {

				final long compResultRefId = ((TVICompareResultReferenceTour) compareResult.getParentItem()).refTour
						.getRefId();

				viewRefIds.put(compResultRefId, compResultRefId);
			}
		}

		// clear selection
		persistedCompareResults.clear();

		// loop: all ref tours where children have been added
		for (final Long refId : viewRefIds.values()) {

			final ArrayList<TreeViewerItem> unfetchedChildren = _rootItem.getUnfetchedChildren();
			if (unfetchedChildren != null) {

				for (final TreeViewerItem rootChild : unfetchedChildren) {
					final TVICatalogRefTourItem mapRefTour = (TVICatalogRefTourItem) rootChild;

					if (mapRefTour.refId == refId) {

						// reload the children for the reference tour
						mapRefTour.fetchChildren();
						_tourViewer.refresh(mapRefTour, true);

						break;
					}
				}
			}
		}
	}

	/**
	 * !!!Recursive !!! update all tour items with new data
	 * 
	 * @param rootItem
	 * @param modifiedTours
	 */
	private void updateTourViewer(final TreeViewerItem parentItem, final ArrayList<TourData> modifiedTours) {

		final ArrayList<TreeViewerItem> children = parentItem.getUnfetchedChildren();

		if (children == null) {
			return;
		}

		// loop: all children
		for (final Object object : children) {
			if (object instanceof TreeViewerItem) {

				final TreeViewerItem treeItem = (TreeViewerItem) object;
				if (treeItem instanceof TVICatalogComparedTour) {

					final TVICatalogComparedTour tourItem = (TVICatalogComparedTour) treeItem;
					final long tourItemId = tourItem.getTourId();

					for (final TourData modifiedTourData : modifiedTours) {
						if (modifiedTourData.getTourId().longValue() == tourItemId) {

							// update tree item

							final TourType tourType = modifiedTourData.getTourType();
							if (tourType != null) {
								tourItem.tourTypeId = tourType.getTypeId();
							}

							// update item title
							tourItem.tourTitle = modifiedTourData.getTourTitle();

							// update item tags
							final Set<TourTag> tourTags = modifiedTourData.getTourTags();
							final ArrayList<Long> tagIds;

							if (tourItem.tagIds != null) {
								tourItem.tagIds.clear();
							}

							tourItem.tagIds = tagIds = new ArrayList<Long>();
							for (final TourTag tourTag : tourTags) {
								tagIds.add(tourTag.getTagId());
							}

							// update item in the viewer
							_tourViewer.update(tourItem, null);

							break;
						}
					}

				} else {
					// update children
					updateTourViewer(treeItem, modifiedTours);
				}
			}
		}
	}

}
