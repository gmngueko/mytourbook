/*******************************************************************************
 * Copyright (C) 2022 Gervais-Martial Ngueko
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
package net.tourbook.ui.views.tourCustomTracks;

import java.util.ArrayList;
import java.util.Arrays;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.util.ColumnDefinition;
import net.tourbook.common.util.ColumnManager;
import net.tourbook.common.util.IContextMenuProvider;
import net.tourbook.common.util.ITourViewer;
import net.tourbook.common.util.PostSelectionProvider;
import net.tourbook.data.CustomTrackDefinition;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.SelectionDeletedTours;
import net.tourbook.tour.SelectionTourData;
import net.tourbook.tour.SelectionTourId;
import net.tourbook.tour.SelectionTourIds;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.action.ActionEditCustomTracks;
import net.tourbook.ui.views.referenceTour.SelectionReferenceTourView;
import net.tourbook.ui.views.referenceTour.TVIElevationCompareResult_ComparedTour;
import net.tourbook.ui.views.referenceTour.TVIRefTour_ComparedTour;
import net.tourbook.ui.views.referenceTour.TVIRefTour_RefTourItem;

import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class TourCustomTracksView extends ViewPart implements ITourProvider, ITourViewer {
   public static final String      ID                = "net.tourbook.views.TourCustomTracksView"; //$NON-NLS-1$

   private final IPreferenceStore  _prefStore        = TourbookPlugin.getPrefStore();
   private final IPreferenceStore  _prefStore_Common = CommonActivator.getPrefStore();
   private final IDialogSettings   _state            = TourbookPlugin.getState(ID);

   private TourData                _tourData;

   private PostSelectionProvider   _postSelectionProvider;
   private ISelectionListener      _postSelectionListener;
   private IPropertyChangeListener _prefChangeListener;
   private IPropertyChangeListener _prefChangeListener_Common;
   private ITourEventListener      _tourPropertyListener;
   private IPartListener2          _partListener;

   private PixelConverter          _pc;

   private ActionEditCustomTracks _action_EditCustomTracksData;

   /*
    * UI controls
    */
   private PageBook    _pageBook;

   private TableViewer _wpViewer;

   private Composite   _pageNoData;
   private Composite   _viewerContainer;

   /*
    * none UI
    */
   private ColumnManager _columnManager;
   private MenuManager   _viewerMenuManager;
   private Menu          _tableContextMenu;
   private IContextMenuProvider _tableViewerContextMenuProvider = new TableContextMenuProvider();

   private static class CustomTracksComparator extends ViewerComparator {

      @Override
      public int compare(final Viewer viewer, final Object e1, final Object e2) {

         final CustomTrackDefinition ct1 = (CustomTrackDefinition) e1;
         final CustomTrackDefinition ct2 = (CustomTrackDefinition) e2;

         /*
          * sort by name
          */
         final String ct1Name = ct1.getName();
         final String ct2Name = ct2.getName();

         if (ct1Name != null && ct2Name != null) {
            return ct1Name.compareTo(ct2Name);
         }

         return ct1Name != null ? 1 : -1;

      }
   }

   class CustomTracksViewerContentProvider implements IStructuredContentProvider {

      @Override
      public void dispose() {}

      @Override
      public Object[] getElements(final Object inputElement) {
         if (_tourData == null) {
            return new Object[0];
         } else {
            return _tourData.getCustomTracksDefinitionFromDataSerie().values().toArray();
         }
      }

      @Override
      public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
   }

   public class RowNumberLabelProvider extends CellLabelProvider {

      private TableViewer viewer;

      @Override
      protected void initialize(final ColumnViewer viewer, final ViewerColumn column) {
        super.initialize(viewer, column);
        this.viewer = null;
        if (viewer instanceof TableViewer) {
          this.viewer = (TableViewer) viewer;
        }
      }

      @Override
      public void update(final ViewerCell cell) {
        //super.update(cell);
        if (viewer != null) {
          final int index = Arrays.asList(viewer.getTable().getItems()).indexOf(cell.getItem());
          cell.setText("" + (index + 1));
        }
      }
    }

   public class TableContextMenuProvider implements IContextMenuProvider {

      @Override
      public void disposeContextMenu() {

         if (_tableContextMenu != null) {
            _tableContextMenu.dispose();
         }
      }

      @Override
      public Menu getContextMenu() {
         return _tableContextMenu;
      }

      @Override
      public Menu recreateContextMenu() {

         disposeContextMenu();

         _tableContextMenu = createUI_22_CreateViewerContextMenu();

         return _tableContextMenu;
      }

   }

   public TourCustomTracksView() {
      super();
   }

   private void addPartListener() {

      _partListener = new IPartListener2() {

         @Override
         public void partActivated(final IWorkbenchPartReference partRef) {}

         @Override
         public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

         @Override
         public void partClosed(final IWorkbenchPartReference partRef) {}

         @Override
         public void partDeactivated(final IWorkbenchPartReference partRef) {}

         @Override
         public void partHidden(final IWorkbenchPartReference partRef) {}

         @Override
         public void partInputChanged(final IWorkbenchPartReference partRef) {}

         @Override
         public void partOpened(final IWorkbenchPartReference partRef) {}

         @Override
         public void partVisible(final IWorkbenchPartReference partRef) {}
      };
      getViewSite().getPage().addPartListener(_partListener);
   }

   private void addPrefListener() {

      _prefChangeListener = new IPropertyChangeListener() {
         @Override
         public void propertyChange(final PropertyChangeEvent event) {

            final String property = event.getProperty();

            if (property.equals(ITourbookPreferences.VIEW_LAYOUT_CHANGED)) {

               _wpViewer.getTable().setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));
               _wpViewer.refresh();
            }
         }
      };

      _prefChangeListener_Common = new IPropertyChangeListener() {
         @Override
         public void propertyChange(final PropertyChangeEvent event) {

            final String property = event.getProperty();

            if (property.equals(ICommonPreferences.MEASUREMENT_SYSTEM)) {

               // measurement system has changed

               //updateInternalUnitValues();

               _columnManager.saveState(_state);
               _columnManager.clearColumns();
               defineAllColumns(_viewerContainer);

               _wpViewer = (TableViewer) recreateViewer(_wpViewer);
            }
         }
      };

      _prefStore.addPropertyChangeListener(_prefChangeListener);
      _prefStore_Common.addPropertyChangeListener(_prefChangeListener_Common);
   }

   /**
    * listen for events when a tour is selected
    */
   private void addSelectionListener() {

      _postSelectionListener = new ISelectionListener() {
         @Override
         public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
            if (part == TourCustomTracksView.this) {
               return;
            }
            onSelectionChanged(selection);
         }
      };
      getViewSite().getPage().addPostSelectionListener(_postSelectionListener);
   }

   private void addTourEventListener() {

      _tourPropertyListener = new ITourEventListener() {
         @Override
         public void tourChanged(final IWorkbenchPart part, final TourEventId eventId, final Object eventData) {

            if ((_tourData == null) || (part == TourCustomTracksView.this)) {
               return;
            }
            if (eventId == TourEventId.TOUR_SELECTION && eventData instanceof ISelection) {

               onSelectionChanged((ISelection) eventData);

            } else {
               if (eventId == TourEventId.TOUR_CHANGED || eventId == TourEventId.UPDATE_UI) {

                  // check if a tour must be updated

                  final long viewTourId = _tourData.getTourId();

                  if (net.tourbook.ui.UI.containsTourId(eventData, viewTourId) != null) {

                     // reload tour data
                     _tourData = TourManager.getInstance().getTourData(viewTourId);

                     _wpViewer.setInput(new Object[0]);
                     enableActions();
                     // removed old tour data from the selection provider
                     _postSelectionProvider.clearSelection();

                  } else {
                     clearView();
                  }

               } else if (eventId == TourEventId.CLEAR_DISPLAYED_TOUR) {

                  clearView();
               }
            }
         }
      };

      TourManager.getInstance().addTourEventListener(_tourPropertyListener);
   }

   private void clearView() {

      _tourData = null;

      _wpViewer.setInput(new Object[0]);

      _postSelectionProvider.clearSelection();
      enableActions();

      _pageBook.showPage(_pageNoData);
   }

   private void createActions() {
      _action_EditCustomTracksData = new ActionEditCustomTracks(this, true);
   }

   private void createMenuManager() {
      _viewerMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
      _viewerMenuManager.setRemoveAllWhenShown(true);
      _viewerMenuManager.addMenuListener(this::fillContextMenu);
   }

   @Override
   public void createPartControl(final Composite parent) {

      _pc = new PixelConverter(parent);

      //updateInternalUnitValues();
      createMenuManager();

      _columnManager = new ColumnManager(this, _state);
      _columnManager.setIsCategoryAvailable(true);
      defineAllColumns(parent);

      createUI(parent);

      createActions();
      fillToolbar();

//    _actionEditTourWaypoints = new ActionOpenMarkerDialog(this, true);

      addSelectionListener();
      addTourEventListener();
      addPrefListener();
      addPartListener();

      // this part is a selection provider
      getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider(ID));

      // show default page
      _pageBook.showPage(_pageNoData);

      // show custom tracks from last selection
      onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

      if (_tourData == null) {
         showTourFromTourProvider();
      }
   }

   private void createUI(final Composite parent) {

      _pageBook = new PageBook(parent, SWT.NONE);

      _pageNoData = net.tourbook.common.UI.createUI_PageNoData(_pageBook, Messages.UI_Label_no_chart_is_selected);

      _viewerContainer = new Composite(_pageBook, SWT.NONE);
      GridLayoutFactory.fillDefaults().applyTo(_viewerContainer);
      {
         createUI_10_CustomTracksViewer(_viewerContainer);
      }
   }

   private void createUI_10_CustomTracksViewer(final Composite parent) {

      final Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

      table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      table.setHeaderVisible(true);
      table.setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

      table.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(final KeyEvent e) {

            if (isTourSavedInDb() == false) {
               return;
            }

            final IStructuredSelection selection = (IStructuredSelection) _wpViewer.getSelection();
            if ((selection.size() > 0) && (e.keyCode == SWT.CR)) {

            }
         }
      });

      /*
       * create table viewer
       */
      _wpViewer = new TableViewer(table);
      _columnManager.createColumns(_wpViewer);

      _wpViewer.setContentProvider(new CustomTracksViewerContentProvider());
      _wpViewer.setComparator(new CustomTracksComparator());

      _wpViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(final SelectionChangedEvent event) {
            final StructuredSelection selection = (StructuredSelection) event.getSelection();
            if (selection != null) {
               fireWaypointPosition(selection);
            }
         }
      });

      _wpViewer.addDoubleClickListener(new IDoubleClickListener() {
         @Override
         public void doubleClick(final DoubleClickEvent event) {

            if (isTourSavedInDb() == false) {
               return;
            }
            // edit selected custom tracks
            final IStructuredSelection selection = (IStructuredSelection) _wpViewer.getSelection();
            if (selection.size() > 0) {
               //TODO set selected custom track's in dialog
               _action_EditCustomTracksData.run();
            }
         }
      });

      // the context menu must be created in this method otherwise it will not work when the viewer is recreated
      createUI_20_ContextMenu();
   }

   /**
    * create the views context menu
    */
   private void createUI_20_ContextMenu() {

      _tableContextMenu = createUI_22_CreateViewerContextMenu();

      final Table table = (Table) _wpViewer.getControl();

      _columnManager.createHeaderContextMenu(table, _tableViewerContextMenuProvider);
   }

   private Menu createUI_22_CreateViewerContextMenu() {

      final Table table = (Table) _wpViewer.getControl();
      final Menu tableContextMenu = _viewerMenuManager.createContextMenu(table);

      return tableContextMenu;
   }

   private void defineAllColumns(final Composite parent) {

      defineColumn_Index();
      defineColumn_Name();
      defineColumn_Unit();
      defineColumn_Size();
      defineColumn_Id();
   }

   /**
    * column: id
    */
   private void defineColumn_Id() {

      final ColumnDefinition colDef = TableColumnFactory.CUSTOM_TRACKS_ID.createColumn(_columnManager, _pc);
      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final CustomTrackDefinition ct = (CustomTrackDefinition) cell.getElement();
            cell.setText(ct.getId());
            final float[] ctData = _tourData.getCustomTracks(ct.getId());
            final int ctSize = ctData == null ? 0 : ctData.length;
            if (ctSize == 0) {
               cell.setForeground(_wpViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
            }
         }
      });
   }

   /**
    * column: index
    */
   private void defineColumn_Index() {

      final ColumnDefinition colDef = TableColumnFactory.CUSTOM_TRACKS_INDEX.createColumn(_columnManager, _pc);
      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new RowNumberLabelProvider());
   }

   /**
    * column: name
    */
   private void defineColumn_Name() {

      final ColumnDefinition colDef = TableColumnFactory.CUSTOM_TRACKS_NAME.createColumn(_columnManager, _pc);
      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final CustomTrackDefinition ct = (CustomTrackDefinition) cell.getElement();
            cell.setText(ct.getName());
            final float[] ctData = _tourData.getCustomTracks(ct.getId());
            final int ctSize = ctData == null ? 0 : ctData.length;
            if (ctSize == 0) {
               cell.setForeground(_wpViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
            }

         }
      });
   }

   /**
    * column: id
    */
   private void defineColumn_Size() {

      final ColumnDefinition colDef = TableColumnFactory.CUSTOM_TRACKS_SIZE.createColumn(_columnManager, _pc);
      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final CustomTrackDefinition ct = (CustomTrackDefinition) cell.getElement();
            final float[] ctData = _tourData.getCustomTracks(ct.getId());
            final int ctSize = ctData==null ? 0:ctData.length;
            cell.setText(String.valueOf(ctSize));
            if (ctSize == 0) {
               cell.setForeground(_wpViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
            }
         }
      });
   }

   /**
    * column: unit
    */
   private void defineColumn_Unit() {

      final ColumnDefinition colDef = TableColumnFactory.CUSTOM_TRACKS_UNIT.createColumn(_columnManager, _pc);
      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final CustomTrackDefinition ct = (CustomTrackDefinition) cell.getElement();
            cell.setText(ct.getUnit());
            final float[] ctData = _tourData.getCustomTracks(ct.getId());
            final int ctSize = ctData == null ? 0 : ctData.length;
            if (ctSize == 0) {
               cell.setForeground(_wpViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
            }
         }
      });
   }

   @Override
   public void dispose() {

      final IWorkbenchPage page = getViewSite().getPage();

      TourManager.getInstance().removeTourEventListener(_tourPropertyListener);
      page.removePostSelectionListener(_postSelectionListener);
      page.removePartListener(_partListener);

      _prefStore.removePropertyChangeListener(_prefChangeListener);
      _prefStore_Common.removePropertyChangeListener(_prefChangeListener_Common);

      super.dispose();
   }

   /**
    * enable actions
    */
   private void enableActions() {

      final boolean isTourInDb = isTourSavedInDb();
      //final boolean isSingleTour = _tourData != null && _tourData.isMultipleTours() == false;
      _action_EditCustomTracksData.setEnabled(isTourInDb);
   }

   private void fillContextMenu(final IMenuManager menuMgr) {

      menuMgr.add(_action_EditCustomTracksData);

      // add standard group which allows other plug-ins to contribute here
      menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//
//    // set the marker which should be selected in the marker dialog
//    final IStructuredSelection selection = (IStructuredSelection) _wpViewer.getSelection();
//    _actionEditTourWaypoints.setSelectedMarker((TourMarker) selection.getFirstElement());
//
      enableActions();
   }

   private void fillToolbar() {

      /*
       * View toolbar
       */
      final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
      toolBarManager.add(_action_EditCustomTracksData);
      /*
       * fill view menu
       */
//      final IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
//
//      menuMgr.add(new Separator());
   }

   /**
    * fire waypoint position
    */
   private void fireWaypointPosition(final StructuredSelection selection) {
      _postSelectionProvider.setSelection(selection);
   }

   @Override
   public ColumnManager getColumnManager() {
      return _columnManager;
   }

   @Override
   public ArrayList<TourData> getSelectedTours() {

      final ArrayList<TourData> selectedTours = new ArrayList<>();
      selectedTours.add(_tourData);

      return selectedTours;
   }

   @Override
   public ColumnViewer getViewer() {
      return _wpViewer;
   }

   /**
    * @return Returns <code>true</code> when the tour is saved in the database
    */
   private boolean isTourSavedInDb() {

      if ((_tourData != null) && (_tourData.getTourPerson() != null)) {
         return true;
      }

      return false;
   }

   private void onSelectionChanged(final ISelection selection) {

      long tourId = TourDatabase.ENTITY_IS_NOT_SAVED;

      if (selection instanceof SelectionTourData) {

         // a tour was selected, get the chart and update the custom tracks viewer

         final SelectionTourData tourDataSelection = (SelectionTourData) selection;
         _tourData = tourDataSelection.getTourData();

         if (_tourData != null) {
            tourId = _tourData.getTourId();
         }

      } else if (selection instanceof SelectionTourId) {

         tourId = ((SelectionTourId) selection).getTourId();

      } else if (selection instanceof SelectionTourIds) {

         final ArrayList<Long> tourIds = ((SelectionTourIds) selection).getTourIds();

         if (tourIds != null && tourIds.size() > 0) {

            if (tourIds.size() == 1) {
               tourId = tourIds.get(0);
            } else {
               _tourData = TourManager.createJoinedTourData(tourIds);
            }
         }
      } else if (selection instanceof SelectionReferenceTourView) {

         final SelectionReferenceTourView tourCatalogSelection = (SelectionReferenceTourView) selection;

         final TVIRefTour_RefTourItem refItem = tourCatalogSelection.getRefItem();
         if (refItem != null) {
            tourId = refItem.getTourId();
         }

      } else if (selection instanceof StructuredSelection) {

         final Object firstElement = ((StructuredSelection) selection).getFirstElement();
         if (firstElement instanceof TVIRefTour_ComparedTour) {
            tourId = ((TVIRefTour_ComparedTour) firstElement).getTourId();
         } else if (firstElement instanceof TVIElevationCompareResult_ComparedTour) {
            tourId = ((TVIElevationCompareResult_ComparedTour) firstElement).getTourId();
         }

      } else if (selection instanceof SelectionDeletedTours) {

         clearView();
      }

      if (tourId > TourDatabase.ENTITY_IS_NOT_SAVED) {

         final TourData tourData = TourManager.getInstance().getTourData(tourId);
         if (tourData != null) {
            _tourData = tourData;
         }
      }

      final boolean isTour = (_tourData != null);//(tourId >= 0) && (_tourData != null);

      if (isTour) {
         _wpViewer.setInput(new Object[0]);
         _pageBook.showPage(_viewerContainer);
      }

//    _actionEditTourWaypoints.setEnabled(isTour);
   }

   @Override
   public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

      _viewerContainer.setRedraw(false);
      {
         _wpViewer.getTable().dispose();

         createUI_10_CustomTracksViewer(_viewerContainer);
         _viewerContainer.layout();

         // update the viewer
         reloadViewer();
      }
      _viewerContainer.setRedraw(true);

      return _wpViewer;
   }

   @Override
   public void reloadViewer() {
      _wpViewer.setInput(new Object[0]);
      enableActions();
   }

   @PersistState
   private void saveState() {

      // check if UI is disposed
      final Table table = _wpViewer.getTable();
      if (table.isDisposed()) {
         return;
      }

      _columnManager.saveState(_state);
   }

   @Override
   public void setFocus() {
      _wpViewer.getTable().setFocus();
   }

   private void showTourFromTourProvider() {

      _pageBook.showPage(_pageNoData);

      // a tour is not displayed, find a tour provider which provides a tour
      Display.getCurrent().asyncExec(new Runnable() {
         @Override
         public void run() {

            // validate widget
            if (_pageBook.isDisposed()) {
               return;
            }

            /*
             * check if tour was set from a selection provider
             */
            if (_tourData != null) {
               return;
            }

            final ArrayList<TourData> selectedTours = TourManager.getSelectedTours();
            if ((selectedTours != null) && (selectedTours.size() > 0)) {
               onSelectionChanged(new SelectionTourData(selectedTours.get(0)));
            }
         }
      });
   }

   @Override
   public void updateColumnHeader(final ColumnDefinition colDef) {
      // TODO Auto-generated method stub

   }

}
