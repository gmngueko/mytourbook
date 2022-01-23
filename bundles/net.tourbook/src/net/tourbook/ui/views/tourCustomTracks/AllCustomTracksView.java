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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.util.ColumnDefinition;
import net.tourbook.common.util.ColumnManager;
import net.tourbook.common.util.IContextMenuProvider;
import net.tourbook.common.util.ITourViewer;
import net.tourbook.common.util.PostSelectionProvider;
import net.tourbook.data.DataSerie;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.TourEvent;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.action.ActionEditCustomTracksAll;

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
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

public class AllCustomTracksView extends ViewPart implements ITourProvider, ITourViewer {
   public static final String     ID                = "net.tourbook.views.AllCustomTracksView"; //$NON-NLS-1$
   //
   private static final String COLUMN_REFID     = "ReferenceId";                            //$NON-NLS-1$
   private static final String COLUMN_NAME      = "Name";                                   //$NON-NLS-1$
   private static final String COLUMN_UNIT      = "Unit";                                   //$NON-NLS-1$
   private static final String COLUMN_SIZE      = "NumberOfTours";                          //$NON-NLS-1$

   //
   private final IPreferenceStore  _prefStore        = TourbookPlugin.getPrefStore();
   private final IPreferenceStore  _prefStore_Common = CommonActivator.getPrefStore();
   private final IDialogSettings   _state            = TourbookPlugin.getState(ID);

   //

   private PostSelectionProvider   _postSelectionProvider;
   private ISelectionListener      _postSelectionListener;
   private IPropertyChangeListener _prefChangeListener;
   private IPropertyChangeListener _prefChangeListener_Common;
   private ITourEventListener      _tourEventListener;
   private IPartListener2          _partListener;
   //
   private MenuManager             _viewerMenuManager;
   private IContextMenuProvider    _tableViewerContextMenuProvider = new TableContextMenuProvider();
   private CustomTracksComparator  _ctComparator                   = new CustomTracksComparator();
   //UI controls
   private PixelConverter          _pc;
   private Menu                   _tableContextMenu;
   private TableViewer             _ctViewer;
   private ColumnManager           _columnManager;
   private Composite               _viewerContainer;
   private Composite               _uiParent;

   private ActionEditCustomTracksAll _action_EditCustomTracksData;

   private ArrayList<DataSerie>    _allDataSeries = new ArrayList<>();
   private HashMap<String, DataSerieViewItem> _allDataSeriesView_ByRefId      = null;
   private boolean                 _isInUpdate;

   private final NumberFormat      _nf1                            = NumberFormat.getNumberInstance();
   private final NumberFormat      _nf3                            = NumberFormat.getNumberInstance();
   private final NumberFormat      _nfLatLon                       = NumberFormat.getNumberInstance();
   {
      _nf1.setMinimumFractionDigits(1);
      _nf1.setMaximumFractionDigits(1);
      _nf3.setMinimumFractionDigits(3);
      _nf3.setMaximumFractionDigits(3);
   }

   private class CustomTracksComparator extends ViewerComparator {

      @Override
      public int compare(final Viewer viewer, final Object e1, final Object e2) {

         final DataSerie ct1 = (DataSerie) e1;
         final DataSerie ct2 = (DataSerie) e2;

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

         return _allDataSeries.toArray();

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

   public AllCustomTracksView() {
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

      _prefChangeListener = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ITourbookPreferences.VIEW_LAYOUT_CHANGED)) {

            _ctViewer.getTable().setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

            _ctViewer.refresh();

            /*
             * the tree must be redrawn because the styled text does not show with the new color
             */
            _ctViewer.getTable().redraw();
         }
      };

      _prefChangeListener_Common = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ICommonPreferences.MEASUREMENT_SYSTEM)) {

            // measurement system has changed

            _columnManager.saveState(_state);
            _columnManager.clearColumns();

            defineAllColumns();

            _ctViewer = (TableViewer) recreateViewer(_ctViewer);
         }
      };

      _prefStore.addPropertyChangeListener(_prefChangeListener);
      _prefStore_Common.addPropertyChangeListener(_prefChangeListener_Common);
   }

   private void addTourEventListener() {

      _tourEventListener = (workbenchPart, tourEventId, eventData) -> {

         if (workbenchPart == AllCustomTracksView.this) {
            return;
         }

         if (_isInUpdate) {
            return;
         }

         if ((tourEventId == TourEventId.TOUR_CHANGED) && (eventData instanceof TourEvent)) {

            final ArrayList<TourData> modifiedTours = ((TourEvent) eventData).getModifiedTours();
            if (modifiedTours != null) {

               // update modified tour

               reloadViewer();
            }

         } else if (tourEventId == TourEventId.DATA_SERIE_IS_MODIFIED) {
            reloadViewer();

         } else if (tourEventId == TourEventId.CLEAR_DISPLAYED_TOUR) {

            reloadViewer();
         }
      };

      TourManager.getInstance().addTourEventListener(_tourEventListener);
   }

   private void createActions() {
      _action_EditCustomTracksData = new ActionEditCustomTracksAll(this, true);
   }

   private void createMenuManager() {

      _viewerMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
      _viewerMenuManager.setRemoveAllWhenShown(true);
      _viewerMenuManager.addMenuListener(this::fillContextMenu);
   }

   @Override
   public void createPartControl(final Composite parent) {

      _uiParent = parent;

      initUI(parent);
      createMenuManager();

      restoreState_BeforeUI();

      // define all columns for the viewer
      _columnManager = new ColumnManager(this, _state);
      defineAllColumns();

      createUI(parent);

      addTourEventListener();
      addPrefListener();
      addPartListener();

      // set selection provider
      getSite().setSelectionProvider(_postSelectionProvider = new PostSelectionProvider(ID));

      createActions();
      fillToolbar();

      // load marker and display them
//      parent.getDisplay().asyncExec(new Runnable() {
//         public void run() {
//
//         }
//      });
      BusyIndicator.showWhile(parent.getDisplay(), () -> {

         loadAllDataSeries();

         updateUI_SetViewerInput();

         restoreState_WithUI();
      });
   }

   private void createUI(final Composite parent) {

      _viewerContainer = new Composite(parent, SWT.NONE);
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

            final IStructuredSelection selection = (IStructuredSelection) _ctViewer.getSelection();
            if ((selection.size() > 0) && (e.keyCode == SWT.CR)) {

            }
         }
      });

      /*
       * create table viewer
       */
      _ctViewer = new TableViewer(table);
      _columnManager.createColumns(_ctViewer);
      _ctViewer.setUseHashlookup(true);

      _ctViewer.setContentProvider(new CustomTracksViewerContentProvider());
      _ctViewer.setComparator(new CustomTracksComparator());

      _ctViewer.addDoubleClickListener(new IDoubleClickListener() {
         @Override
         public void doubleClick(final DoubleClickEvent event) {

            //if (isTourSavedInDb() == false) {
            //   return;
            //}
            // edit selected custom tracks
            final IStructuredSelection selection = (IStructuredSelection) _ctViewer.getSelection();
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

      final Table table = (Table) _ctViewer.getControl();

      _columnManager.createHeaderContextMenu(table, _tableViewerContextMenuProvider);
   }

   private Menu createUI_22_CreateViewerContextMenu() {

      final Table table = (Table) _ctViewer.getControl();
      final Menu tableContextMenu = _viewerMenuManager.createContextMenu(table);

      return tableContextMenu;
   }

   private void defineAllColumns() {

      defineColumn_Index();
      defineColumn_Name();
      defineColumn_Unit();
      defineColumn_Tours_Count();
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

            final DataSerie ct = (DataSerie) cell.getElement();
            cell.setText(ct.getRefId());
            //Hibernate.initialize(ct.getTourData());
            //final Set<TourData> listofTours = ct.getTourData();
            final long ctSize = _allDataSeriesView_ByRefId.get(ct.getRefId()).getColTourCounter();//(listofTours == null ? 0 : listofTours.size());
            if (ctSize == 0) {
               cell.setForeground(_ctViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
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

            final DataSerie ct = (DataSerie) cell.getElement();
            cell.setText(ct.getName());
            //Hibernate.initialize(ct.getTourData());
            //final Set<TourData> listofTours = ct.getTourData();
            //final int ctSize = (listofTours == null ? 0 : listofTours.size());
            final long ctSize = _allDataSeriesView_ByRefId.get(ct.getRefId()).getColTourCounter();
            if (ctSize == 0) {
               cell.setForeground(_ctViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
            }

         }
      });
   }

   /**
    * column: id
    */
   private void defineColumn_Tours_Count() {

      final ColumnDefinition colDef = TableColumnFactory.CUSTOM_TRACKS_COUNT_TOURS.createColumn(_columnManager, _pc);
      colDef.setIsDefaultColumn();
      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final DataSerie ct = (DataSerie) cell.getElement();
            //final Set<TourData> listofTours = ct.getTourData();
            //Hibernate.initialize(ct.getTourData());
            //final int ctSize = (listofTours == null ? 0 : listofTours.size());
            final long ctSize = _allDataSeriesView_ByRefId.get(ct.getRefId()).getColTourCounter();
            cell.setText(String.valueOf(ctSize));
            if (ctSize == 0) {
               cell.setForeground(_ctViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
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

            final DataSerie ct = (DataSerie) cell.getElement();
            cell.setText(ct.getUnit());
            //Hibernate.initialize(ct.getTourData());
            //final Set<TourData> listofTours = ct.getTourData();
            //final int ctSize = (listofTours == null ? 0 : listofTours.size());
            final long ctSize = _allDataSeriesView_ByRefId.get(ct.getRefId()).getColTourCounter();
            if (ctSize == 0) {
               cell.setForeground(_ctViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
            }
         }
      });
   }

   @Override
   public void dispose() {

      TourManager.getInstance().removeTourEventListener(_tourEventListener);

      getViewSite().getPage().removePartListener(_partListener);

      _prefStore.removePropertyChangeListener(_prefChangeListener);
      _prefStore_Common.removePropertyChangeListener(_prefChangeListener_Common);

      super.dispose();
   }

   /**
    * enable actions
    */
   private void enableActions() {

      _action_EditCustomTracksData.setEnabled(true);
   }

   private void fillContextMenu(final IMenuManager menuMgr) {

      menuMgr.add(_action_EditCustomTracksData);

      // add standard group which allows other plug-ins to contribute here
      menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      enableActions();
   }

   private void fillToolbar() {

      /*
       * View toolbar
       */
      final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
      toolBarManager.add(_action_EditCustomTracksData);
   }

   @Override
   public ColumnManager getColumnManager() {
      return _columnManager;
   }

   public Object getCtViewer() {
      return _ctViewer;
   }

   @Override
   public ArrayList<TourData> getSelectedTours() {
      // TODO Auto-generated method stub
      return (new ArrayList<>());
   }

   @Override
   public ColumnViewer getViewer() {
      return _ctViewer;
   }

   private StructuredSelection getViewerSelection() {

      return (StructuredSelection) _ctViewer.getSelection();
   }

   private void initUI(final Composite parent) {

      _pc = new PixelConverter(parent);

   }

   private void loadAllDataSeries() {
      //TODO rework this for performance
      //TODO replace by a sql fetching the sum of distnace , time, etc...
      _allDataSeries.clear();
      TourDatabase.clearDataSeries();
      _allDataSeries.addAll(TourDatabase.getAllDataSeriesWithTourData());
      //_allDataSeriesView_ByRefId.clear();
      //_allDataSeriesView_ByRefId.putAll(TourDatabase.getAllDataSeriesView_ByRefId());
      _allDataSeriesView_ByRefId = TourDatabase.getAllDataSeriesView_ByRefId();
   }

   @Override
   public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

      _viewerContainer.setRedraw(false);
      {
         // keep selection
         final ISelection selectionBackup = getViewerSelection();
         //final Object[] checkedElements = _ctViewer.getCheckedElements();
         {
            _ctViewer.getTable().dispose();

            createUI_10_CustomTracksViewer(_viewerContainer);

            // update UI
            _viewerContainer.layout();

            // update the viewer
            updateUI_SetViewerInput();
         }
         //updateUI_SelectTourMarker(selectionBackup, checkedElements);
      }
      _viewerContainer.setRedraw(true);

      _ctViewer.getTable().setFocus();

      return _ctViewer;
   }

   @Override
   public void reloadViewer() {

      loadAllDataSeries();

      _viewerContainer.setRedraw(false);
      {
         // keep selection
         final ISelection selectionBackup = getViewerSelection();
         //final Object[] checkedElements = _markerViewer.getCheckedElements();
         {
            updateUI_SetViewerInput();
         }
         //updateUI_SelectTourMarker(selectionBackup, checkedElements);
      }
      _viewerContainer.setRedraw(true);

   }

   private void restoreState_BeforeUI() {

   }

   private void restoreState_WithUI() {
      enableActions();
   }

   @PersistState
   private void saveState() {

      _columnManager.saveState(_state);

//      _state.put(STATE_SORT_COLUMN_ID, _markerComparator.__sortColumnId);
//      _state.put(STATE_SORT_COLUMN_DIRECTION, _markerComparator.__sortDirection);
//
//      _state.put(STATE_GPS_FILTER, _gpsMarkerFilter);

      /*
       * selected marker item
       */
//      long markerId = TourDatabase.ENTITY_IS_NOT_SAVED;
//      final StructuredSelection selection = getViewerSelection();
//      final Object firstItem = selection.getFirstElement();
//
//      if (firstItem instanceof TourMarkerItem) {
//         final TourMarkerItem markerItem = (TourMarkerItem) firstItem;
//         markerId = markerItem.markerId;
//      }
//      _state.put(STATE_SELECTED_MARKER_ITEM, markerId);
   }

   @Override
   public void setFocus() {
      _ctViewer.getTable().setFocus();
   }

   @Override
   public void updateColumnHeader(final ColumnDefinition colDef) {
      // TODO Auto-generated method stub

   }

   private void updateUI_SetViewerInput() {

      _isInUpdate = true;
      {
         _ctViewer.setInput(new Object[0]);
         enableActions();
      }
      _isInUpdate = false;

   }

}
