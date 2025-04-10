/*******************************************************************************
 * Copyright (C) 2005, 2024 Wolfgang Schramm and Contributors
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
package net.tourbook.common.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.tourbook.common.Messages;
import net.tourbook.common.UI;
import net.tourbook.common.formatter.IValueFormatter;
import net.tourbook.common.formatter.ValueFormat;
import net.tourbook.common.formatter.ValueFormatter_Default;
import net.tourbook.common.formatter.ValueFormatter_Number_1_0;
import net.tourbook.common.formatter.ValueFormatter_Number_1_1;
import net.tourbook.common.formatter.ValueFormatter_Number_1_2;
import net.tourbook.common.formatter.ValueFormatter_Number_1_3;
import net.tourbook.common.formatter.ValueFormatter_Time_HH;
import net.tourbook.common.formatter.ValueFormatter_Time_HHMM;
import net.tourbook.common.formatter.ValueFormatter_Time_HHMMSS;
import net.tourbook.common.formatter.ValueFormatter_Time_SSS;
import net.tourbook.common.tooltip.AdvancedSlideoutShell;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.AbstractColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.UnFreezeGridCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * Manages the columns for a tree/table-viewer
 * <p>
 * Created: 2007-05-27 by Wolfgang Schramm
 */
public class ColumnManager {

   private static final String XML_STATE_COLUMN_MANAGER                  = "XML_STATE_COLUMN_MANAGER";          //$NON-NLS-1$
   //
   private static final String TAG_ROOT                                  = "ColumnProfiles";                    //$NON-NLS-1$
   //
   private static final String TAG_COLUMN                                = "Column";                            //$NON-NLS-1$
   private static final String TAG_PROFILE                               = "Profile";                           //$NON-NLS-1$
   //
   private static final String ATTR_IS_ACTIVE_PROFILE                    = "isActiveProfile";                   //$NON-NLS-1$
   private static final String ATTR_IS_SHOW_CATEGORY                     = "isShowCategory";                    //$NON-NLS-1$
   private static final String ATTR_IS_SHOW_COLUMN_ANNOTATION_FORMATTING = "isShowColumnAnnotation_Formatting"; //$NON-NLS-1$
   private static final String ATTR_IS_SHOW_COLUMN_ANNOTATION_SORTING    = "isShowColumnAnnotation_Sorting";    //$NON-NLS-1$
   //
   private static final String ATTR_COLUMN_ID                            = "columnId";                          //$NON-NLS-1$
   private static final String ATTR_COLUMN_ALIGNMENT                     = "alignment";                         //$NON-NLS-1$
   private static final String ATTR_COLUMN_FORMAT_CATEGORY               = "categoryFormat";                    //$NON-NLS-1$
   private static final String ATTR_COLUMN_FORMAT_DETAIL                 = "detailFormat";                      //$NON-NLS-1$
   private static final String ATTR_NAME                                 = "name";                              //$NON-NLS-1$
   private static final String ATTR_FROZEN_COLUMN_ID                     = "frozenColumnId";                    //$NON-NLS-1$

   private static final String ATTR_VISIBLE_COLUMN_IDS                   = "visibleColumnIds";                  //$NON-NLS-1$
   private static final String ATTR_VISIBLE_COLUMN_IDS_AND_WIDTH         = "visibleColumnIdsAndWidth";          //$NON-NLS-1$
   //
   private static final String COLUMN_CATEGORY_SEPARATOR                 = "   \u00bb   ";                      //$NON-NLS-1$
   static final String         COLUMN_TEXT_SEPARATOR                     = "   \u00B7   ";                      //$NON-NLS-1$
   //
   public static final String  CUSTOM_TRACKS_TIME_SLICES_ID              = "CUSTOM_TRACKS_TIME_SLICES";         //$NON-NLS-1$

   private static final String ALIGNMENT_LEFT                            = "left";                              //$NON-NLS-1$
   private static final String ALIGNMENT_CENTER                          = "center";                            //$NON-NLS-1$
   private static final String ALIGNMENT_RIGHT                           = "right";                             //$NON-NLS-1$
   //
   /**
    * Minimum column width, when the column width is 0, there was a bug that this happened.
    */
   private static final int    COLUMN_WIDTH_MINIMUM                      = 0;

   /**
    * There was a case when the column width in a NatTable was 393'515'928 which required a computer
    * restart to kill MT, it got frozen when scrolling horizontally.
    * <p>
    * 1000 would be too small on high-dpi displays
    */
   static final int            COLUMN_WIDTH_MAXIMUM                      = 5_000;

   /**
    * This is needed to adjust 4k display column widths, otherwise they get smaler and smaler every
    * time when they are retrieved from the NatTable
    */
   private static float        _dpiScaleFactor                           = -1;

   /*
    * Value formatter
    */
   private static IValueFormatter            _defaultDefaultValueFormatter = new ValueFormatter_Default();

   /**
    * Contains all column definitions which are defined for the table/tree viewer.
    * <p>
    * The sequence how they are added is the default.
    */
   private final ArrayList<ColumnDefinition> _allDefinedColumnDefinitions  = new ArrayList<>();

   /** All column profiles */
   private ArrayList<ColumnProfile>          _allProfiles                  = new ArrayList<>();

   /** Active column profile */
   private ColumnProfile                     _activeProfile;

   /**
    * When <code>true</code>, columns can be categorized which is displayed in the UI but it has no
    * other functions, default is <code>false</code>.
    * <p>
    * When only a view columns are contained in a view then it does not make sense to categorize
    * these columns.
    */
   private boolean                           _isCategoryAvailable          = false;

   /**
    * Flag which the user can set to show/hide column category in the column viewer and table/tree
    * header context menu.
    */
   private boolean                           _isShowCategory               = true;

   private boolean                           _isShowColumnAnnotation_Formatting;
   private boolean                           _isShowColumnAnnotation_Sorting;
   private boolean                           _isDoAResizeForAllColumnsToFit;

   private Comparator<ColumnProfile>         _profileSorter;

   private final ITourViewer                 _tourViewer;
   private AbstractColumnLayout              _columnLayout;

   /**
    * Viewer which is managed by this {@link ColumnManager}.
    */
   private ColumnViewer                      _columnViewer;

   private ColumnWrapper                     _headerColumnItem;

   private AdvancedSlideoutShell             _slideoutShell;

   /**
    * When {@link #_natTablePropertiesProvider} is not <code>null</code> then this
    * {@link ColumnManager} is used for a {@link NatTable}, it provides properties from a
    * {@link NatTable}.
    */
   private INatTable_PropertiesProvider      _natTablePropertiesProvider;

   /**
    * Context menu listener
    */
   private Listener                          _table_MenuDetect_Listener;
   private Listener                          _tree_MenuDetect_Listener;

   private final Listener                    _colMenuItem_Listener;

   private IValueFormatter                   _valueFormatter_Number_1_0    = new ValueFormatter_Number_1_0();
   private IValueFormatter                   _valueFormatter_Number_1_1    = new ValueFormatter_Number_1_1();
   private IValueFormatter                   _valueFormatter_Number_1_2    = new ValueFormatter_Number_1_2();
   private IValueFormatter                   _valueFormatter_Number_1_3    = new ValueFormatter_Number_1_3();
   private IValueFormatter                   _valueFormatter_Time_HH       = new ValueFormatter_Time_HH();
   private IValueFormatter                   _valueFormatter_Time_HHMM     = new ValueFormatter_Time_HHMM();
   private IValueFormatter                   _valueFormatter_Time_HHMMSS   = new ValueFormatter_Time_HHMMSS();
   private IValueFormatter                   _valueFormatter_Time_SSS      = new ValueFormatter_Time_SSS();

   {
      _colMenuItem_Listener = event -> onSelectColumnItem(event);

      _profileSorter = (colProfile1, colProfile2) -> colProfile1.name.compareTo(colProfile2.name);
   }

   /**
    * A column wrapper which contains a {@link TableColumn}, {@link TreeColumn} or {@link NatTable}
    */
   private class ColumnWrapper {

      Object columnItem;

//      int    columnLeftBorder;
      int columnRightBorder;

      public ColumnWrapper(final Object columnItem, final int columnLeftBorder, final int columnRightBorder) {

         this.columnItem = columnItem;

//         this.columnLeftBorder = columnLeftBorder;
         this.columnRightBorder = columnRightBorder;
      }
   }

   public ColumnManager(final ITourViewer tourViewer, final IDialogSettings viewState) {

      _tourViewer = tourViewer;

      restoreState(viewState);
   }

   static String getAlignmentText(final int alignment) {

      return switch (alignment) {

      case SWT.CENTER -> ALIGNMENT_CENTER;
      case SWT.TRAIL  -> ALIGNMENT_RIGHT;
      default         -> ALIGNMENT_LEFT;

      };
   }

   private static int getAlignmentValue(final String alignment) {

      return switch (alignment) {

      case ALIGNMENT_CENTER -> SWT.CENTER;
      case ALIGNMENT_RIGHT  -> SWT.TRAIL;
      default               -> SWT.LEAD;

      };
   }

   static IValueFormatter getDefaultDefaultValueFormatter() {

      return _defaultDefaultValueFormatter;
   }

   void action_AddColumn(final ColumnDefinition colDef) {

      setVisibleColumnIds_Column_Show(colDef, false);
   }

   public void action_FreezeColumn(final ColumnDefinition colDef) {

      // first unfreeze previous columns
      action_UnFreezeAllColumns();

      // update model
      colDef.setIsColumnFreezed(true);
      _activeProfile.frozenColumnId = colDef.getColumnId();

      // update UI
      final String columnId = colDef.getColumnId();
      int currentColumnIndex = -1;

      final String[] visibleColumnIds = _activeProfile.getVisibleColumnIds();
      for (int columnIndex = 0; columnIndex < visibleColumnIds.length; columnIndex++) {

         final String visibleColumnId = visibleColumnIds[columnIndex];

         if (columnId.equals(visibleColumnId)) {
            currentColumnIndex = columnIndex;
            break;
         }
      }

      final NatTable natTable = _natTablePropertiesProvider.getNatTable();

      // keep current viewport otherwise the freeze command will move the viewport to the top
      natTable.doCommand(new TurnViewportOffCommand());
      {
         natTable.doCommand(new FreezeColumnCommand(natTable, currentColumnIndex + 1));
      }
      natTable.doCommand(new TurnViewportOnCommand());

   }

   void action_SetColumnAlignment(final ColumnDefinition colDef, final int style) {

      /*
       * Update model
       */
      colDef.setStyle(style);

      final String columnId = colDef.getColumnId();
      for (final ColumnProperties columnProperties : _activeProfile.columnProperties) {

         if (columnId.equals(columnProperties.columnId)) {

            columnProperties.alignment = style;

            break;
         }
      }

      /*
       * Update UI
       */
      _tourViewer.recreateViewer(_tourViewer.getViewer());
   }

   void action_SetValueFormatter(final ColumnDefinition colDef,
                                 final ValueFormat valueFormat,
                                 final boolean isDetailFormat) {

      /*
       * Update model
       */

      final String columnId = colDef.getColumnId();
      for (final ColumnProperties columnProperties : _activeProfile.columnProperties) {

         if (columnId.equals(columnProperties.columnId)) {

            if (isDetailFormat) {
               columnProperties.valueFormat_Detail = valueFormat;
            } else {
               columnProperties.valueFormat_Category = valueFormat;
            }

            break;
         }
      }

      final IValueFormatter valueFormatter = getValueFormatter(valueFormat);

      if (isDetailFormat) {
         colDef.setValueFormatter_Detail(valueFormat, valueFormatter);
      } else {
         colDef.setValueFormatter_Category(valueFormat, valueFormatter);
      }

      /*
       * Update UI
       */
      if (isNatTableColumnManager()) {

         _natTablePropertiesProvider.getNatTable().redraw();

      } else {

         // allow the viewer to update the header
         _tourViewer.updateColumnHeader(colDef);

         _tourViewer.getViewer().refresh();
      }
   }

   private void action_ShowAllColumns() {

      if (MessageDialog.openConfirm(
            Display.getCurrent().getActiveShell(),
            Messages.Column_Profile_Dialog_Title,
            NLS.bind(Messages.Column_Profile_Dialog_ShowAllColumns_Message, _activeProfile.name))) {

         setVisibleColumnIds_All();

         recreateViewer();
      }
   }

   private void action_ShowDefaultColumns() {

      if (MessageDialog.openConfirm(
            Display.getCurrent().getActiveShell(),
            Messages.Column_Profile_Dialog_Title,
            NLS.bind(Messages.Column_Profile_Dialog_ShowDefaultColumns_Message, _activeProfile.name))) {

         setVisibleColumnIds_Default();

         recreateViewer();
      }
   }

   private void action_SizeAllColumnToFit() {

      if (isNatTableColumnManager()) {

         final NatTable natTable = _natTablePropertiesProvider.getNatTable();

         BusyIndicator.showWhile(natTable.getDisplay(), () -> {

            _isDoAResizeForAllColumnsToFit = true;

            natTable.redraw();
            natTable.update();
         });

      } else {

         // larger tables/trees are needing more time to resize

         BusyIndicator.showWhile(_columnViewer.getControl().getDisplay(), () -> {

            boolean isColumn0Visible = true;

            if (_tourViewer instanceof final ITourViewer2 tourViewer) {
               isColumn0Visible = tourViewer.isColumn0Visible(_columnViewer);
            }

            if (_columnViewer instanceof final TableViewer tableViewer) {

               final Table table = tableViewer.getTable();
               if (table.isDisposed()) {
                  return;
               }

               table.setRedraw(false);
               {
                  final TableColumn[] allColumns = table.getColumns();

                  for (int columnIndex = 0; columnIndex < allColumns.length; columnIndex++) {

                     final TableColumn tableColumn = allColumns[columnIndex];

                     if (columnIndex == 0) {

                        if (isColumn0Visible) {
                           tableColumn.pack();
                        } else {
                           tableColumn.setWidth(0);
                        }
                     } else {
                        tableColumn.pack();
                     }
                  }
               }
               table.setRedraw(true);

            } else if (_columnViewer instanceof final TreeViewer treeViewer) {

               final Tree tree = treeViewer.getTree();
               if (tree.isDisposed()) {
                  return;
               }

               tree.setRedraw(false);
               {
                  final TreeColumn[] allColumns = tree.getColumns();
                  for (final TreeColumn tableColumn : allColumns) {
                     tableColumn.pack();
                  }
               }
               tree.setRedraw(true);
            }
         });
      }

   }

   private void action_UnFreezeAllColumns() {

      /*
       * Update model
       */
      _activeProfile.frozenColumnId = null;

      boolean isAnyColumnFreezed = false;
      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {

         isAnyColumnFreezed |= colDef.isColumnFreezed();

         colDef.setIsColumnFreezed(false);
      }

      if (isAnyColumnFreezed == false) {

         // there is nothing to unfreeze in the UI
         return;
      }

      /*
       * Update UI
       */
      final ViewportLayer viewportLayer = _natTablePropertiesProvider.getNatTableLayer_Viewport();

      // keep current viewport otherwise the unfreeze command will move the viewport to the top
      viewportLayer.doCommand(new TurnViewportOffCommand());
      {
         _natTablePropertiesProvider.getNatTable().doCommand(new UnFreezeGridCommand());
      }
      viewportLayer.doCommand(new TurnViewportOnCommand());
   }

   void addColumn(final ColumnDefinition colDef) {
      _allDefinedColumnDefinitions.add(colDef);
   }

   /**
    * @return Returns <code>true</code> when at least one column is frozen.
    */
   private boolean areColumnFreezed() {

      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {
         if (colDef.isColumnFreezed()) {
            return true;
         }
      }

      return false;
   }

   /**
    * Removes all defined columns
    */
   public void clearColumns() {
      _allDefinedColumnDefinitions.clear();
   }

   // remove only the custom tracks columns from the overall list of defined column
   public void clearCustomTracksColumns() {
      final ArrayList<String> columnIds = new ArrayList<>();
      final Iterator<ColumnDefinition> iteratorColumnDefinition = _allDefinedColumnDefinitions.iterator();
      while (iteratorColumnDefinition.hasNext()) {
         final ColumnDefinition columnDefinition = iteratorColumnDefinition.next();
         final String columnId = columnDefinition.getColumnId();
         if (columnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
            iteratorColumnDefinition.remove();
         }
      }

      final Iterator<ColumnDefinition> iteratorVisibleColumnDefinition = _activeProfile.visibleColumnDefinitions.iterator();
      while (iteratorVisibleColumnDefinition.hasNext()) {
         final ColumnDefinition columnDefinition = iteratorVisibleColumnDefinition.next();
         final String columnId = columnDefinition.getColumnId();
         if (columnDefinition.getColumnId().startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
            iteratorVisibleColumnDefinition.remove();
         } else {
            columnIds.add(columnId);
         }
      }

      final ArrayList<String> visibleIdsAndWidth = new ArrayList<>();

      for (final ColumnDefinition columnDefinition : _activeProfile.visibleColumnDefinitions) {

         // set column id and width
         if (!columnDefinition.getColumnId().startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
            visibleIdsAndWidth.add(columnDefinition.getColumnId());
            visibleIdsAndWidth.add(Integer.toString(columnDefinition.getColumnWidth()));
         }
      }

      //TODO: need to manage cases where visible columns are now empty!!!
      _activeProfile.visibleColumnIdsAndWidth = visibleIdsAndWidth.toArray(new String[visibleIdsAndWidth.size()]);
      _activeProfile.setVisibleColumnIds(columnIds.toArray(new String[columnIds.size()]));
   }

   private String createColumnLabel(final ColumnDefinition colDef, final boolean isWithCategory) {

      final String category = colDef.getColumnCategory();
      final String label = colDef.getColumnLabel();
      final String unit = colDef.getColumnUnit();

      final StringBuilder sb = new StringBuilder();

      // add category
      if (isWithCategory && _isCategoryAvailable && _isShowCategory && category != null) {
         sb.append(category);
      }

      // add label
      if (label != null) {
         if (isWithCategory && sb.length() > 0) {
            sb.append(COLUMN_CATEGORY_SEPARATOR);
         }
         sb.append(label);
      }

      // add unit
      if (unit != null) {

         if (sb.length() > 0) {
            sb.append(COLUMN_TEXT_SEPARATOR);
         }

         sb.append(unit);
      }

      return sb.toString();
   }

   private void createColumnMenuItems(final Menu contextMenu, final ArrayList<ColumnDefinition> allColDef) {

      for (final ColumnDefinition colDef : allColDef) {

         final MenuItem colMenuItem = new MenuItem(contextMenu, SWT.CHECK);

         final String columnLabel = createColumnLabel(colDef, true);

         colMenuItem.setText(columnLabel);
         colMenuItem.setEnabled(colDef.canModifyVisibility());
         colMenuItem.setSelection(colDef.isColumnCheckedInContextMenu());

         colMenuItem.setData(colDef);
         colMenuItem.addListener(SWT.Selection, _colMenuItem_Listener);
      }
   }

   /**
    * Creates the columns in the tree/table for all visible columns.
    *
    * @param columnViewer
    */
   public void createColumns(final ColumnViewer columnViewer) {

      _columnViewer = columnViewer;

      setupColumns_01_VisibleColDefs(_activeProfile);
      setupColumns_02_ColumnAlignment(_activeProfile);

      if (columnViewer instanceof final TableViewer tableViewer) {

         // create all columns in the table

         for (final ColumnDefinition colDef : _activeProfile.visibleColumnDefinitions) {
            createColumns_Table((TableColumnDefinition) colDef, tableViewer);
         }

      } else if (columnViewer instanceof final TreeViewer treeViewer) {

         // create all columns in the tree

         for (final ColumnDefinition colDef : _activeProfile.visibleColumnDefinitions) {
            createColumns_Tree((TreeColumnDefinition) colDef, treeViewer);
         }
      }

      setupColumns_03_ValueFormatter(_activeProfile);
   }

   /**
    * Creates a column in a table viewer
    *
    * @param colDef
    * @param tableViewer
    */
   private void createColumns_Table(final TableColumnDefinition colDef, final TableViewer tableViewer) {

      TableViewerColumn tvc;
      TableColumn tc;

      tvc = new TableViewerColumn(tableViewer, colDef.getColumnStyle());

      final CellLabelProvider cellLabelProvider = colDef.getCellLabelProvider();
      if (cellLabelProvider != null) {
         tvc.setLabelProvider(cellLabelProvider);
      }

      tvc.setEditingSupport(colDef.getEditingSupport());

      // get column widget
      tc = tvc.getColumn();

      final String columnText = colDef.getColumnHeaderText(this);
      if (columnText != null) {
         tc.setText(columnText);
      }

      final String columnToolTipText = colDef.getColumnHeaderToolTipText();
      if (columnToolTipText != null) {
         tc.setToolTipText(columnToolTipText);
      }

      /*
       * set column width
       */
      if (_columnLayout == null) {

         // set the column width with pixels

         tc.setWidth(getColumnWidth(colDef));

      } else {

         // use the column layout to set the width of the columns

         final ColumnLayoutData columnLayoutData = colDef.getColumnWeightData();

         if (columnLayoutData == null) {
            try {
               throw new Exception("ColumnWeightData is not set for the column: " + colDef); //$NON-NLS-1$
            } catch (final Exception e) {
               e.printStackTrace();
            }
         }

         if (columnLayoutData instanceof final ColumnPixelData columnPixelData) {

            // overwrite the width
            columnPixelData.width = getColumnWidth(colDef);
            _columnLayout.setColumnData(tc, columnPixelData);
         } else {
            _columnLayout.setColumnData(tc, columnLayoutData);
         }
      }

      tc.setResizable(colDef.isColumnResizable());
      tc.setMoveable(colDef.isColumnMoveable());

      // keep reference to the column definition
      tc.setData(colDef);

      // keep tc ref
      colDef.setTableColumn(tc);

      // add selection listener
      final SelectionListener columnSelectionListener = colDef.getColumnSelectionListener();
      if (columnSelectionListener != null) {
         tc.addSelectionListener(columnSelectionListener);
      }

      // add resize/move listener
      final ControlListener columnControlListener = colDef.getColumnControlListener();
      if (columnControlListener != null) {
         tc.addControlListener(columnControlListener);
      }
   }

   /**
    * Creates a column in a tree viewer
    *
    * @param colDef
    * @param treeViewer
    */
   private void createColumns_Tree(final TreeColumnDefinition colDef, final TreeViewer treeViewer) {

      TreeViewerColumn tvc;
      TreeColumn tc;

      tvc = new TreeViewerColumn(treeViewer, colDef.getColumnStyle());

      final CellLabelProvider cellLabelProvider = colDef.getCellLabelProvider();
      if (cellLabelProvider != null) {
         tvc.setLabelProvider(cellLabelProvider);
      }

      tc = tvc.getColumn();

      final String columnText = colDef.getColumnHeaderText(this);
      if (columnText != null) {
         tc.setText(columnText);
      }

      final String columnToolTipText = colDef.getColumnHeaderToolTipText();
      if (columnToolTipText != null) {
         tc.setToolTipText(columnToolTipText);
      }

      /*
       * set column width
       */
      int columnWidth = colDef.getColumnWidth();
      if (colDef.isColumnHidden()) {
         columnWidth = 0;
      } else {
         columnWidth = columnWidth < COLUMN_WIDTH_MINIMUM
               ? colDef.getDefaultColumnWidth()
               : columnWidth;
      }
      tc.setWidth(columnWidth);

      tc.setResizable(colDef.isColumnResizable());
      tc.setMoveable(colDef.isColumnMoveable());

      // keep reference to the column definition
      tc.setData(colDef);
      colDef.setTreeColumn(tc);

      // add selection listener
      final SelectionListener columnSelectionListener = colDef.getColumnSelectionListener();
      if (columnSelectionListener != null) {
         tc.addSelectionListener(columnSelectionListener);
      }

      // add resize/move listener
      final ControlListener columnControlListener = colDef.getColumnControlListener();
      if (columnControlListener != null) {
         tc.addControlListener(columnControlListener);
      }
   }

   /**
    * Create (H)eader (C)ontext (M)enu which has the actions to modify columns
    *
    * @param composite
    * @param defaultContextMenuProvider
    *
    * @return
    */
   private Menu createHCM_0_Menu(final Composite composite, final Shell shell, final IContextMenuProvider defaultContextMenuProvider) {

      final Menu headerContextMenu = new Menu(shell, SWT.POP_UP);

      /*
       * IMPORTANT: Dispose the menus (only the current menu, when menu is set with setMenu() it
       * will be disposed automatically)
       */
      composite.addListener(SWT.Dispose, event -> {

         headerContextMenu.dispose();

         if (defaultContextMenuProvider != null) {
            defaultContextMenuProvider.disposeContextMenu();
         }
      });

      return headerContextMenu;
   }

   public void createHeaderContextMenu(final NatTable natTable,
                                       final IContextMenuProvider defaultContextMenuProvider,
                                       final ColumnHeaderLayer columnHeaderLayer) {

      // remove old listener
      if (_table_MenuDetect_Listener != null) {
         natTable.removeListener(SWT.MenuDetect, _table_MenuDetect_Listener);
      }

      final Shell contextMenuShell = natTable.getShell();
      final Menu[] headerContextMenu = { createHCM_0_Menu(natTable, contextMenuShell, defaultContextMenuProvider) };

      // add the context menu to the table
      _table_MenuDetect_Listener = event -> {

         final Display display = natTable.getShell().getDisplay();
         final Point mousePosition = display.map(null, natTable, new Point(event.x, event.y));

         final Rectangle clientArea = natTable.getClientArea();

         final int headerHeight = columnHeaderLayer.getHeight();
         final int headerBottom = clientArea.y + headerHeight;

         final boolean isTableHeaderHit = clientArea.y <= mousePosition.y
               && mousePosition.y < (clientArea.y + headerHeight);

         // !!! header column must be set BEFORE the menu is created !!!
         _headerColumnItem = getHeaderColumn(natTable, mousePosition, isTableHeaderHit, columnHeaderLayer);

         Menu contextMenu = getContextMenu(isTableHeaderHit, headerContextMenu[0], defaultContextMenuProvider);

         if (contextMenu != null) {

            // can be null when context menu is not set

            if (contextMenu == headerContextMenu[0] && contextMenu.getShell() != natTable.getShell()) {

               /**
                * java.lang.IllegalArgumentException: Widget has the wrong parent
                * <p>
                * When a view is minimized, then the context menu is already created
                * but has the wrong parent when the view is displayed later on.
                */

               headerContextMenu[0].dispose();

               headerContextMenu[0] = createHCM_0_Menu(natTable, natTable.getShell(), defaultContextMenuProvider);

               contextMenu = getContextMenu(isTableHeaderHit, headerContextMenu[0], defaultContextMenuProvider);

               StatusUtil.logInfo("Table header context menu has had the wrong parent and is recreated."); //$NON-NLS-1$

            } else if (defaultContextMenuProvider != null
                  && contextMenu == defaultContextMenuProvider.getContextMenu()
                  && contextMenu.getShell() != natTable.getShell()) {

               contextMenu = defaultContextMenuProvider.recreateContextMenu();

               StatusUtil.logInfo("Table context menu has had the wrong parent and is recreated."); //$NON-NLS-1$
            }
         }

         try {

            natTable.setMenu(contextMenu);

         } catch (final IllegalArgumentException e) {

            StatusUtil.showStatus(e);
         }

         /*
          * Set context menu position to the right border of the column
          */
         if (_headerColumnItem != null) {

            int posX = _headerColumnItem.columnRightBorder;
            int xOffset = 0;

            final ScrollBar hBar = natTable.getHorizontalBar();

            if (hBar != null) {
               xOffset = hBar.getSelection();
            }

            /*
             * It is possible that the context menu is outside of the tree, this occurs when the
             * column is very wide and horizontal scrolled.
             */
            if (posX - xOffset > clientArea.width) {
               posX = xOffset + clientArea.width;
            }

            final Point displayPosition = natTable.toDisplay(posX, headerBottom);

            // micro adjust position to show exactly on the header lines otherwise it looks ugly
            event.x = displayPosition.x - 1;
            event.y = displayPosition.y - 1;
         }
      };

      natTable.addListener(SWT.MenuDetect, _table_MenuDetect_Listener);
   }

   /**
    * set context menu depending on the position of the mouse
    *
    * @param table
    *           Table control
    * @param defaultContextMenuProvider
    *           Can be <code>null</code> when a default context menu is not available
    */
   public void createHeaderContextMenu(final Table table, final IContextMenuProvider defaultContextMenuProvider) {

      createHeaderContextMenu(table, defaultContextMenuProvider, table.getShell());
   }

   /**
    * Set context menu depending on the position of the mouse
    *
    * @param table
    *           Table control
    * @param defaultContextMenuProvider
    *           Can be <code>null</code> when a default context menu is not available
    * @param contextMenuShell
    *           Shell for the context menu. For reparented dialogs, the correct shell must be
    *           provided.
    */
   public void createHeaderContextMenu(final Table table, final IContextMenuProvider defaultContextMenuProvider, final Shell contextMenuShell) {

      // remove old listener
      if (_table_MenuDetect_Listener != null) {
         table.removeListener(SWT.MenuDetect, _table_MenuDetect_Listener);
      }

      final Menu[] headerContextMenu = { createHCM_0_Menu(table, contextMenuShell, defaultContextMenuProvider) };

      // add the context menu to the table
      _table_MenuDetect_Listener = event -> {

         final Display display = table.getShell().getDisplay();
         final Point mousePosition = display.map(null, table, new Point(event.x, event.y));

         final Rectangle clientArea = table.getClientArea();

         final int headerHeight = table.getHeaderHeight();
         final int headerBottom = clientArea.y + headerHeight;

         final boolean isTableHeaderHit = clientArea.y <= mousePosition.y
               && mousePosition.y < (clientArea.y + headerHeight);

         _headerColumnItem = getHeaderColumn(table, mousePosition, isTableHeaderHit);

         Menu contextMenu = getContextMenu(isTableHeaderHit, headerContextMenu[0], defaultContextMenuProvider);

         if (contextMenu != null) {

            // can be null when context menu is not set

            if (contextMenu == headerContextMenu[0] && contextMenu.getShell() != table.getShell()) {

               /**
                * java.lang.IllegalArgumentException: Widget has the wrong parent
                * <p>
                * When a view is minimized, then the context menu is already created
                * but has the wrong parent when the view is displayed later on.
                */

               headerContextMenu[0].dispose();

               headerContextMenu[0] = createHCM_0_Menu(table, table.getShell(), defaultContextMenuProvider);

               contextMenu = getContextMenu(isTableHeaderHit, headerContextMenu[0], defaultContextMenuProvider);

               StatusUtil.logInfo("Table header context menu has had the wrong parent and is recreated."); //$NON-NLS-1$

            } else if (defaultContextMenuProvider != null
                  && contextMenu == defaultContextMenuProvider.getContextMenu()
                  && contextMenu.getShell() != table.getShell()) {

               contextMenu = defaultContextMenuProvider.recreateContextMenu();

               StatusUtil.logInfo("Table context menu has had the wrong parent and is recreated."); //$NON-NLS-1$
            }
         }

         try {

            table.setMenu(contextMenu);

         } catch (final IllegalArgumentException e) {

            StatusUtil.showStatus(e);
         }

         /*
          * Set context menu position to the right border of the column
          */
         if (_headerColumnItem != null) {

            int posX = _headerColumnItem.columnRightBorder;
            int xOffset = 0;

            final ScrollBar hBar = table.getHorizontalBar();

            if (hBar != null) {
               xOffset = hBar.getSelection();
            }

            /*
             * It is possible that the context menu is outside of the tree, this occurs when the
             * column is very wide and horizontal scrolled.
             */
            if (posX - xOffset > clientArea.width) {
               posX = xOffset + clientArea.width;
            }

            final Point displayPosition = table.toDisplay(posX, headerBottom);

            event.x = displayPosition.x - 1;
            event.y = displayPosition.y - 2;
         }
      };

      table.addListener(SWT.MenuDetect, _table_MenuDetect_Listener);
   }

   /**
    * Set context menu depending on the position of the mouse
    *
    * @param tree
    *           Tree control
    * @param defaultContextMenu
    *           Can be <code>null</code> when a default context menu is not available
    */
   public void createHeaderContextMenu(final Tree tree, final IContextMenuProvider defaultContextMenuProvider) {

      this.createHeaderContextMenu(tree, defaultContextMenuProvider, tree.getShell());
   }

   /**
    * Set context menu depending on the position of the mouse
    *
    * @param tree
    *           Tree control
    * @param defaultContextMenu
    *           Can be <code>null</code> when a default context menu is not available
    * @param contextMenuShell
    *           Shell for the context menu. For reparented dialogs, the correct shell must be
    *           provided.
    */
   private void createHeaderContextMenu(final Tree tree, final IContextMenuProvider defaultContextMenuProvider, final Shell contextMenuShell) {

      // remove old listener
      if (_tree_MenuDetect_Listener != null) {
         tree.removeListener(SWT.MenuDetect, _tree_MenuDetect_Listener);
      }

      final Menu[] headerContextMenu = { createHCM_0_Menu(tree, contextMenuShell, defaultContextMenuProvider) };

      // add the context menu to the tree viewer
      _tree_MenuDetect_Listener = event -> {

         final Decorations shell = tree.getShell();
         final Display display = shell.getDisplay();

         final Point mousePosition = display.map(null, tree, new Point(event.x, event.y));

         final Rectangle clientArea = tree.getClientArea();

         final int headerHeight = tree.getHeaderHeight();
         final int headerBottom = clientArea.y + headerHeight;

         final boolean isTreeHeaderHit = clientArea.y <= mousePosition.y && mousePosition.y < headerBottom;

         _headerColumnItem = getHeaderColumn(tree, mousePosition, isTreeHeaderHit);

         Menu contextMenu = getContextMenu(isTreeHeaderHit, headerContextMenu[0], defaultContextMenuProvider);

         if (contextMenu != null) {

            // can be null when context menu is not set

            if (contextMenu == headerContextMenu[0] && contextMenu.getShell() != tree.getShell()) {

               /**
                * java.lang.IllegalArgumentException: Widget has the wrong parent
                * <p>
                * When a view is minimized, then the context menu is already created
                * but has the wrong parent when the view is displayed later on.
                */

               headerContextMenu[0].dispose();

               headerContextMenu[0] = createHCM_0_Menu(tree, tree.getShell(), defaultContextMenuProvider);

               contextMenu = getContextMenu(isTreeHeaderHit, headerContextMenu[0], defaultContextMenuProvider);

               StatusUtil.logInfo("Tree header context menu has had the wrong parent and is recreated."); //$NON-NLS-1$

            } else if (defaultContextMenuProvider != null
                  && contextMenu == defaultContextMenuProvider.getContextMenu()
                  && contextMenu.getShell() != tree.getShell()) {

               contextMenu = defaultContextMenuProvider.recreateContextMenu();

               StatusUtil.logInfo("Tree context menu has had the wrong parent and is recreated."); //$NON-NLS-1$
            }
         }

         try {

            tree.setMenu(contextMenu);

         } catch (final IllegalArgumentException e) {

            // This occurred: Widget has the wrong parent

            // after some debugging, could not find the reason, this view is very similar to the tourbook view

            /*
             * The problem can occur when tours are compared with 2 different perspectives (ref
             * tour and compare result), the system measurement is changed and the context menu
             * for the ref tours will be opened
             */

            StatusUtil.showStatus(e);
         }

         /*
          * Set context menu position to the right border of the column
          */
         if (_headerColumnItem != null) {

            int posX = _headerColumnItem.columnRightBorder;
            int xOffset = 0;

            final ScrollBar hBar = tree.getHorizontalBar();

            if (hBar != null) {
               xOffset = hBar.getSelection();
            }

            /*
             * It is possible that the context menu is outside of the tree, this occurs when the
             * column is very wide and horizontal scrolled.
             */
            if (posX - xOffset > clientArea.width) {
               posX = xOffset + clientArea.width;
            }

            final Point displayPosition = tree.toDisplay(posX, headerBottom);

            event.x = displayPosition.x - 1;
            event.y = displayPosition.y - 2;
         }
      };

      tree.addListener(SWT.MenuDetect, _tree_MenuDetect_Listener);
   }

   private void createMenuSeparator(final Menu contextMenu) {

      new MenuItem(contextMenu, SWT.SEPARATOR);
   }

   /**
    * Creates the (h)eader (c)ontext (m)enu.
    *
    * @param contextMenu
    */
   private void fillHeaderCtxMenu_0_MenuItems(final Menu contextMenu) {

      setVisibleColumnIds_FromViewer();

      fillHeaderCtxMenu_10_CurrentColumn(contextMenu);
      fillHeaderCtxMenu_20_AllColumns(contextMenu);
      fillHeaderCtxMenu_30_Profiles(contextMenu);
      fillHeaderCtxMenu_40_Columns(contextMenu);
   }

   private void fillHeaderCtxMenu_10_CurrentColumn(final Menu contextMenu) {

      if (_headerColumnItem == null) {
         // this is required
         return;
      }

      final ColumnDefinition colDef = getColDef_FromHeaderColumn();
      if (colDef == null) {
         // this should not occur
         return;
      }

      final String[] visibleIds = _activeProfile.getVisibleColumnIds();
      final boolean canColumnBeSetToHidden = visibleIds.length > 1;

      final ValueFormat[] availableFormatter = colDef.getAvailableFormatter();
      final boolean isValueFormatterAvailable = availableFormatter != null && availableFormatter.length > 0;

      final int columnStyle = colDef.getColumnStyle();

      if (isValueFormatterAvailable == false
            && canColumnBeSetToHidden == false
            && columnStyle == 0) {

         // nothing can be done
         return;
      }

      {
         /*
          * Menu title: > Column: ... <
          */

         // create menu item text
         final String menuItemText = NLS.bind(Messages.Action_ColumnManager_ColumnActions_Info, createColumnLabel(colDef, false));

         final MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
         menuItem.setText(menuItemText);
         menuItem.setEnabled(false);
      }
      {
         /*
          * Action: Hide current column
          */
         if (canColumnBeSetToHidden) {

            final MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
            menuItem.setText(Messages.Action_ColumnManager_HideCurrentColumn);
            menuItem.addListener(SWT.Selection, event -> setVisibleColumnIds_Column_Hide(colDef));

            if (colDef.canModifyVisibility() == false) {

               // column cannot be hidden, disable it
               menuItem.setEnabled(false);
            }
         }
      }

      // set action only for the NatTable
      if (isNatTableColumnManager()) {
         {
            /*
             * Action: Freeze current column
             */
            final MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
            menuItem.setText(Messages.Action_ColumnManager_FreezeCurrentColumn);
            menuItem.setToolTipText(Messages.Action_ColumnManager_FreezeCurrentColumn_Tooltip);
            menuItem.addListener(SWT.Selection, (event) -> {
               action_FreezeColumn(colDef);
            });

            menuItem.setEnabled(colDef.isColumnFreezed() == false);
         }
         {
            /*
             * Action: Unfreeze all columns
             */
            final MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
            menuItem.setText(Messages.Action_ColumnManager_UnFreezeAllColumns);
            menuItem.addListener(SWT.Selection, (event) -> {
               action_UnFreezeAllColumns();
            });

            menuItem.setEnabled(areColumnFreezed());
         }
      }
      {
         /*
          * Actions: Value Formatter
          */
         if (isValueFormatterAvailable) {

            new ColumnFormatSubMenu(contextMenu, colDef, this);
         }
      }
      {
         /*
          * Actions: Column alignment
          */
         if (columnStyle != 0) {

            new ColumnAlignmentSubMenu(contextMenu, colDef, this);
         }
      }

      createMenuSeparator(contextMenu);
   }

   private void fillHeaderCtxMenu_20_AllColumns(final Menu contextMenu) {

      {
         /*
          * Action: Size All Columns to Fit
          */
         final MenuItem fitMenuItem = new MenuItem(contextMenu, SWT.PUSH);
         fitMenuItem.setText(Messages.Action_App_SizeAllColumnsToFit);
         fitMenuItem.addListener(SWT.Selection, event -> action_SizeAllColumnToFit());
      }

      {
         /*
          * Action: Show all columns
          */
         final MenuItem allColumnsMenuItem = new MenuItem(contextMenu, SWT.PUSH);
         allColumnsMenuItem.setText(Messages.Action_ColumnManager_ShowAllColumns);
         allColumnsMenuItem.addListener(SWT.Selection, event -> action_ShowAllColumns());
      }

      {
         /*
          * Action: Show default columns
          */
         final MenuItem defaultColumnsMenuItem = new MenuItem(contextMenu, SWT.PUSH);
         defaultColumnsMenuItem.setText(Messages.Action_ColumnManager_ShowDefaultColumns);
         defaultColumnsMenuItem.addListener(SWT.Selection, event -> action_ShowDefaultColumns());
      }

      {
         /*
          * Action: &Customize Profiles/Columns...
          */
         final MenuItem configMenuItem = new MenuItem(contextMenu, SWT.PUSH);
         configMenuItem.setText(Messages.Action_App_CustomizeColumnsAndProfiles);
         configMenuItem.setImage(UI.IMAGE_REGISTRY.get(UI.IMAGE_CONFIGURE_COLUMNS));
         configMenuItem.addListener(SWT.Selection, event -> openColumnDialog());
      }

      createMenuSeparator(contextMenu);
   }

   /**
    * Action: Profiles
    */
   private void fillHeaderCtxMenu_30_Profiles(final Menu contextMenu) {

      {
         // menu title
         final MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
         menuItem.setText(Messages.Action_ColumnManager_Profile_Info);
         menuItem.setEnabled(false);
      }

      /*
       * Actions: All profiles
       */
      Collections.sort(_allProfiles, _profileSorter);

      for (int columnIndex = 0; columnIndex < _allProfiles.size(); columnIndex++) {

         final ColumnProfile columnProfile = _allProfiles.get(columnIndex);
         final boolean isChecked = columnProfile == _activeProfile;

         String menuText = columnProfile.name

               + COLUMN_TEXT_SEPARATOR

               // show number of visible columns
               + Integer.toString(columnProfile.getVisibleColumnIds().length);

         // add a mnemonic to select a profile easier when debugging
         if (columnIndex < 10) {
            menuText = UI.SYMBOL_MNEMONIC + Integer.toString(columnIndex + 1) + UI.SPACE + menuText;
         }

         final MenuItem menuItem = new MenuItem(contextMenu, SWT.CHECK);

         menuItem.setText(menuText);
         menuItem.setSelection(isChecked);

         menuItem.setData(columnProfile);
         menuItem.addListener(SWT.Selection, _colMenuItem_Listener);
      }

      createMenuSeparator(contextMenu);
   }

   /**
    * Action: Columns
    */
   private void fillHeaderCtxMenu_40_Columns(final Menu contextMenu) {

      /*
       * Header: > Columns <
       */
      {
         final MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
         menuItem.setText(Messages.Action_ColumnManager_Column_Info);
         menuItem.setEnabled(false);
      }

      /*
       * Actions: All columns
       */
      final ArrayList<ColumnDefinition> allColumns = getRearrangedColumns();

      final ArrayList<ColumnDefinition> displayedColumns = new ArrayList<>();
      final ArrayList<ColumnDefinition> notDisplyedColumns = new ArrayList<>();
      final HashSet<String> categorizedNames = new HashSet<>();

      for (final ColumnDefinition colDef : allColumns) {

         if (colDef.isColumnCheckedInContextMenu()) {

            displayedColumns.add(colDef);

         } else {

            notDisplyedColumns.add(colDef);

            final String columnCategory = colDef.getColumnCategory();

            if (columnCategory != null) {
               categorizedNames.add(columnCategory);
            }
         }
      }

      // create menu items for each visible column
      createColumnMenuItems(contextMenu, displayedColumns);

      if (_isCategoryAvailable) {

         // create submenus for each category

         // sort by category name
         final ArrayList<String> categories = new ArrayList<>(categorizedNames);
         Collections.sort(categories);

         new ColumnContextMenu(contextMenu, categories, notDisplyedColumns, this);

      } else {

         // create not categorized menu items
         createColumnMenuItems(contextMenu, notDisplyedColumns);
      }
   }

   public ColumnProfile getActiveProfile() {
      return _activeProfile;
   }

   /**
    * @param columnId
    *           column id
    *
    * @return Returns the column definition for the column id, or <code>null</code> when the column
    *         for the column id is not available
    */
   private ColumnDefinition getColDef_ByColumnId(final String columnId) {

      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {

         if (colDef.getColumnId().compareTo(columnId) == 0) {
            return colDef;
         }
      }

      return null;
   }

   /**
    * @param createIndex
    *           column create id
    *
    * @return Returns the column definition for the column create index, or <code>null</code> when
    *         the column is not available
    */
   private ColumnDefinition getColDef_ByCreateIndex(final int createIndex) {

      for (final ColumnDefinition colDef : _activeProfile.visibleColumnDefinitions) {
         if (colDef.getCreateIndex() == createIndex) {
            return colDef;
         }
      }

      return null;
   }

   private ColumnDefinition getColDef_FromHeaderColumn() {

      if (_headerColumnItem == null) {
         return null;
      }

      ColumnDefinition colDef = null;

      final Object columnItem = _headerColumnItem.columnItem;

      if (columnItem instanceof final TableColumn tableColumn) {

         colDef = (ColumnDefinition) tableColumn.getData();

      } else if (columnItem instanceof final TreeColumn treeColumn) {

         colDef = (ColumnDefinition) treeColumn.getData();

      } else if (columnItem instanceof final ColumnDefinition columnDefinition) {

         colDef = columnDefinition;
      }

      return colDef;
   }

   /**
    * @return Returns the columns in the format: id/width ...
    */
   private String[] getColumns_FromViewer_IdAndWidth() {

      final ArrayList<String> allColumnIdsAndWidth = new ArrayList<>();

      if (isNatTableColumnManager()) {

         final DataLayer dataLayer = _natTablePropertiesProvider.getNatTableLayer_Data();
         final ColumnHideShowLayer columnHideShowLayer = _natTablePropertiesProvider.getNatTableLayer_ColumnHideShow();
         final ColumnReorderLayer columnReorderLayer = _natTablePropertiesProvider.getNatTableLayer_ColumnReorder();

         final int numColumns = dataLayer.getColumnCount();

         for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {

            /*
             * This looks a bit complicated, it is. It respects reordered and hidden columns. This
             * solution was found with trial and error until it worked.
             */

            // the reorder layer contains the correct column order for cases when columns are moved with drag&drop
            final int reorderColIndex = columnReorderLayer.getColumnIndexByPosition(columnIndex);

            final int colIndexByPos = dataLayer.getColumnIndexByPosition(reorderColIndex);

            // the column hide show layer has the info if a column was set to hidden by the user
            final boolean isColumnHidden = columnHideShowLayer.isColumnIndexHidden(colIndexByPos);
            if (isColumnHidden) {
               continue;
            }

            final ColumnDefinition colDef = getColDef_ByCreateIndex(colIndexByPos);
            if (colDef != null) {

               final int columnWidth = getColumnWidth_NatTable(dataLayer, reorderColIndex);
               final String columnId = colDef.getColumnId();

               setColumnIdAndWidth(allColumnIdsAndWidth, columnId, columnWidth);
            }
         }

      } else if (_columnViewer instanceof final TableViewer tableViewer) {

         final Table table = tableViewer.getTable();
         if (table.isDisposed()) {
            return null;
         }

         for (final TableColumn column : table.getColumns()) {

            final String columnId = ((ColumnDefinition) column.getData()).getColumnId();
            final int columnWidth = column.getWidth();

            setColumnIdAndWidth(allColumnIdsAndWidth, columnId, columnWidth);
         }

      } else if (_columnViewer instanceof final TreeViewer treeViewer) {

         final Tree tree = treeViewer.getTree();
         if (tree.isDisposed()) {
            return null;
         }

         for (final TreeColumn column : tree.getColumns()) {

            final String columnId = ((TreeColumnDefinition) column.getData()).getColumnId();
            final int columnWidth = column.getWidth();

            setColumnIdAndWidth(allColumnIdsAndWidth, columnId, columnWidth);
         }
      }

      return allColumnIdsAndWidth.toArray(new String[allColumnIdsAndWidth.size()]);
   }

   /**
    * Read the column order from a swt table/tree or nattable
    *
    * @return Returns <code>null</code> when table/tree cannot be accessed.
    */
   private String[] getColumns_FromViewer_Ids() {

      final ArrayList<String> orderedColumnIds = new ArrayList<>();

      int[] columnOrder = null;

      if (isNatTableColumnManager()) {

         final DataLayer dataLayer = _natTablePropertiesProvider.getNatTableLayer_Data();
         final ColumnHideShowLayer columnHideShowLayer = _natTablePropertiesProvider.getNatTableLayer_ColumnHideShow();
         final ColumnReorderLayer columnReorderLayer = _natTablePropertiesProvider.getNatTableLayer_ColumnReorder();

         final int numColumns = dataLayer.getColumnCount();

         final ArrayList<String> allOrderedColumnIds = new ArrayList<>();

         for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {

            /*
             * This looks a bit complicated, it is. It respects reordered and hidden columns. This
             * solution was found with trial and error until it worked.
             */

            // the reorder layer contains the correct column order for cases when columns are moved with drag&drop
            final int reorderColIndex = columnReorderLayer.getColumnIndexByPosition(columnIndex);

            final int colIndexByPos = dataLayer.getColumnIndexByPosition(reorderColIndex);

            // the column hide show layer has the info if a column was set to hidden by the user
            final boolean isColumnHidden = columnHideShowLayer.isColumnIndexHidden(colIndexByPos);
            if (isColumnHidden) {
               continue;
            }

            final ColumnDefinition colDef = getColDef_ByCreateIndex(colIndexByPos);
            if (colDef != null) {

               final String columnId = colDef.getColumnId();

               allOrderedColumnIds.add(columnId);
            }
         }

         return allOrderedColumnIds.toArray(new String[allOrderedColumnIds.size()]);

      } else {

         if (_columnViewer instanceof final TableViewer tableViewer) {

            final Table table = tableViewer.getTable();
            if (table.isDisposed()) {
               return null;
            }
            columnOrder = table.getColumnOrder();

         } else if (_columnViewer instanceof final TreeViewer treeViewer) {

            final Tree tree = treeViewer.getTree();
            if (tree.isDisposed()) {
               return null;
            }
            columnOrder = tree.getColumnOrder();
         }

         if (columnOrder == null) {
            return null;
         }

         // create column id'ss with the visible sort order
         for (final int createIndex : columnOrder) {

            final ColumnDefinition colDef = getColDef_ByCreateIndex(createIndex);

            if (colDef != null) {
               orderedColumnIds.add(colDef.getColumnId());
            }
         }
      }

      return orderedColumnIds.toArray(new String[orderedColumnIds.size()]);
   }

   ColumnViewer getColumnViewer() {
      return _columnViewer;
   }

   private int getColumnWidth(final String columnWidthId) {

      final String[] values = _activeProfile.visibleColumnIdsAndWidth;

      if (values == null) {

         // may need another value
         return 88;
      }

      for (int columnIndex = 0; columnIndex < values.length; columnIndex++) {

         final String columnId = values[columnIndex];

         if (columnWidthId.equals(columnId)) {
            try {
               return Integer.parseInt(values[++columnIndex]);
            } catch (final Exception e) {
               // ignore format exception
            }
         }

         // skip width, advance to next id
         columnIndex++;
      }

      return 0;
   }

   private int getColumnWidth(final TableColumnDefinition colDef) {

      int columnWidth = colDef.getColumnWidth();

      if (colDef.isColumnHidden()) {
         columnWidth = 0;
      } else {
         columnWidth = columnWidth < COLUMN_WIDTH_MINIMUM
               ? colDef.getDefaultColumnWidth()
               : columnWidth;
      }

      return columnWidth;
   }

   private int getColumnWidth_NatTable(final DataLayer dataLayer, final int createdColumnIndex) {

      int colWidthByPos = dataLayer.getColumnWidthByPosition(createdColumnIndex);

      if (UI.IS_4K_DISPLAY) {

         if (_dpiScaleFactor == -1) {

            final int displayDpi = Display.getDefault().getDPI().x;

            _dpiScaleFactor = GUIHelper.getDpiFactor(displayDpi);
         }

         colWidthByPos = (int) (colWidthByPos / _dpiScaleFactor);
      }

      return colWidthByPos;
   }

   /**
    * @param isHeaderHit
    * @param headerContextMenu
    * @param defaultContextMenuProvider
    *
    * @return
    */
   private Menu getContextMenu(final boolean isHeaderHit,
                               final Menu headerContextMenu,
                               final IContextMenuProvider defaultContextMenuProvider) {

      Menu contextMenu;

      if (isHeaderHit) {

         contextMenu = headerContextMenu;

         // recreate all menu items because the column order can be changed
         for (final MenuItem menuItem : contextMenu.getItems()) {
            menuItem.dispose();
         }

         fillHeaderCtxMenu_0_MenuItems(headerContextMenu);

      } else {

         contextMenu = defaultContextMenuProvider != null
               ? defaultContextMenuProvider.getContextMenu()
               : null;
      }

      return contextMenu;
   }

   /**
    * @param natTable
    * @param mousePosition
    * @param isTableHeaderHit
    * @param columnHeaderLayer
    *
    * @return Returns a column item or <code>null</code> when the header is not hit or the columns
    *         is not found
    */
   private ColumnWrapper getHeaderColumn(final NatTable natTable,
                                         final Point mousePosition,
                                         final boolean isTableHeaderHit,
                                         final ColumnHeaderLayer columnHeaderLayer) {

      if (isTableHeaderHit) {

         final int gridColumnPosition = natTable.getColumnPositionByX(mousePosition.x);
         final int colIndexByPos = natTable.getColumnIndexByPosition(gridColumnPosition);

         if (colIndexByPos == -1) {

            // a column is not hit
            return null;
         }

         final ColumnDefinition colDef = _activeProfile.visibleColumnDefinitions.get(colIndexByPos);
         if (colDef != null) {

            // column found

            final int gridColumnStartX = natTable.getStartXOfColumnPosition(gridColumnPosition);
            final int columnWidth = natTable.getColumnWidthByPosition(gridColumnPosition);

            final int columnLeftBorder = gridColumnStartX;
            final int columnRightBorder = gridColumnStartX + columnWidth;

            return new ColumnWrapper(colDef, columnLeftBorder, columnRightBorder);
         }
      }

      return null;
   }

   private ColumnWrapper getHeaderColumn(final Table table, final Point mousePosition, final boolean isTableHeaderHit) {

      if (isTableHeaderHit) {

         int columnWidths = 0;

         final TableColumn[] columns = table.getColumns();
         final int[] columnOrder = table.getColumnOrder();

         for (final int creationIndex : columnOrder) {

            final TableColumn tc = columns[creationIndex];

            final int columnWidth = tc.getWidth();

            if (columnWidths < mousePosition.x && mousePosition.x < columnWidths + columnWidth) {

               final int columnLeftBorder = columnWidths;
               final int columnRightBorder = columnWidths + columnWidth;

               // column found
               return new ColumnWrapper(tc, columnLeftBorder, columnRightBorder);
            }

            columnWidths += columnWidth;
         }
      }

      return null;
   }

   private ColumnWrapper getHeaderColumn(final Tree tree, final Point mousePosition, final boolean isTreeHeaderHit) {

      if (isTreeHeaderHit) {

         int columnWidths = 0;

         final TreeColumn[] columns = tree.getColumns();
         final int[] columnOrder = tree.getColumnOrder();

         for (final int creationIndex : columnOrder) {

            final TreeColumn tc = columns[creationIndex];

            final int columnWidth = tc.getWidth();

            if (columnWidths < mousePosition.x && mousePosition.x < columnWidths + columnWidth) {

               final int columnLeftBorder = columnWidths;
               final int columnRightBorder = columnWidths + columnWidth;

               // column found
               return new ColumnWrapper(tc, columnLeftBorder, columnRightBorder);
            }

            columnWidths += columnWidth;
         }
      }

      return null;
   }

   /**
    * Read the order/width for the columns, this is necessary because the user can have rearranged
    * the columns and/or resized the columns with the mouse.
    *
    * @return Returns ALL columns, first the visible then the hidden columns.
    */
   public ArrayList<ColumnDefinition> getRearrangedColumns() {

      /*
       * Get column order from viewer
       */
      int[] columnOrder = null;

      if (isNatTableColumnManager()) {

         final DataLayer dataLayer = _natTablePropertiesProvider.getNatTableLayer_Data();
         final ColumnHideShowLayer columnHideShowLayer = _natTablePropertiesProvider.getNatTableLayer_ColumnHideShow();
         final ColumnReorderLayer columnReorderLayer = _natTablePropertiesProvider.getNatTableLayer_ColumnReorder();

         final int numColumns = dataLayer.getColumnCount();

         final IntArrayList allOrderedColumns = new IntArrayList();

         for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {

            /*
             * This looks a bit complicated, it is. It respects reordered and hidden columns. This
             * solution was found with trial and error until it worked.
             */

            // the reorder layer contains the correct column order for cases when columns are moved with drag&drop
            final int reorderColIndex = columnReorderLayer.getColumnIndexByPosition(columnIndex);

            final int colIndexByPos = dataLayer.getColumnIndexByPosition(reorderColIndex);

            // the column hide show layer has the info if a column was set to hidden by the user
            final boolean isColumnHidden = columnHideShowLayer.isColumnIndexHidden(colIndexByPos);
            if (isColumnHidden) {
               continue;
            }

            allOrderedColumns.add(colIndexByPos);
         }

         columnOrder = allOrderedColumns.toArray();

      } else {

         if (_columnViewer instanceof final TableViewer tableViewer) {

            final Table table = tableViewer.getTable();
            if (table.isDisposed()) {
               return null;
            }
            columnOrder = table.getColumnOrder();

         } else if (_columnViewer instanceof final TreeViewer treeViewer) {

            final Tree tree = treeViewer.getTree();
            if (tree.isDisposed()) {
               return null;
            }
            columnOrder = tree.getColumnOrder();
         }
      }

      /*
       * Clone all columns
       */
      final ArrayList<ColumnDefinition> allColDefClone = new ArrayList<>();

      try {
         for (final ColumnDefinition definedColDef : _allDefinedColumnDefinitions) {
            allColDefClone.add((ColumnDefinition) definedColDef.clone());
         }
      } catch (final CloneNotSupportedException e) {
         StatusUtil.log(e);
      }

      /*
       * Add visible columns in the current sort order
       */
      final ArrayList<ColumnDefinition> allRearrangedColumns = new ArrayList<>();

      for (final int createIndex : columnOrder) {

         final ColumnDefinition colDef = getColDef_ByCreateIndex(createIndex);

         if (colDef != null) {

            // check all visible columns in the dialog
            colDef.setIsColumnChecked(true);

            // set column width
            colDef.setColumnWidth(getColumnWidth(colDef.getColumnId()));

            // keep the column
            allRearrangedColumns.add(colDef);

            allColDefClone.remove(colDef);
         }
      }

      /*
       * Add remaining columns which are defined but not visible
       */
      for (final ColumnDefinition colDef : allColDefClone) {

         // uncheck hidden columns
         colDef.setIsColumnChecked(false);

         // set column default width
         colDef.setColumnWidth(colDef.getDefaultColumnWidth());

         allRearrangedColumns.add(colDef);
      }

      return allRearrangedColumns;
   }

   /**
    * Get a value formatter for a {@link ValueFormat}.
    *
    * @param valueFormat_Category
    *
    * @return Returns the {@link IValueFormatter} or <code>null</code> when not available.
    */
   IValueFormatter getValueFormatter(final ValueFormat valueFormat) {

      if (valueFormat == null) {
         return null;
      }

      switch (valueFormat) {

      case NUMBER_1_0:
         return _valueFormatter_Number_1_0;

      case NUMBER_1_1:
         return _valueFormatter_Number_1_1;

      case NUMBER_1_2:
         return _valueFormatter_Number_1_2;

      case NUMBER_1_3:
         return _valueFormatter_Number_1_3;

      case TIME_HH:
         return _valueFormatter_Time_HH;

      case TIME_HH_MM:
         return _valueFormatter_Time_HHMM;

      case TIME_HH_MM_SS:
         return _valueFormatter_Time_HHMMSS;

      case TIME_SSS:
         return _valueFormatter_Time_SSS;

      default:
         return null;
      }
   }

   /**
    * @return Returns column definitions which are visible in the table/tree in the sort order of
    *         the table/tree.
    */
   public ArrayList<ColumnDefinition> getVisibleAndSortedColumns() {
      return _activeProfile.visibleColumnDefinitions;
   }

   public boolean isCategoryAvailable() {
      return _isCategoryAvailable;
   }

   /**
    * @return Returns <code>true</code> when this {@link ColumnManager} is used for a
    *         {@link NatTable}.
    */
   public boolean isNatTableColumnManager() {
      return _natTablePropertiesProvider != null;
   }

   public boolean isShowCategory() {
      return _isShowCategory;
   }

   public boolean isShowColumnAnnotation_Formatting() {
      return _isShowColumnAnnotation_Formatting;
   }

   public boolean isShowColumnAnnotation_Sorting() {
      return _isShowColumnAnnotation_Sorting;
   }

   private void onSelectColumnItem(final Event event) {

      if (event.widget instanceof final MenuItem menuItem) {

         final Object data = menuItem.getData();

         if (data instanceof ColumnDefinition) {

            updateColumns(menuItem.getParent().getItems());

         } else if (data instanceof final ColumnProfile profile) {

            updateColumns(profile);
         }
      }
   }

   public void openColumnDialog() {

      setVisibleColumnIds_FromViewer();

      final DialogModifyColumns columnDialog = new DialogModifyColumns(
            Display.getCurrent().getActiveShell(),
            this,
            getRearrangedColumns(),
            _allDefinedColumnDefinitions,
            _activeProfile,
            _allProfiles);

      if (_slideoutShell != null) {

         // prevent that the column dialog will freeze the app
         _slideoutShell.setIsAnotherDialogOpened(true);
      }

      columnDialog.open();

      if (_slideoutShell != null) {

         _slideoutShell.setIsAnotherDialogOpened(false);
      }
   }

   private void recreateViewer() {

      _columnViewer = _tourViewer.recreateViewer(_columnViewer);

      // restore frozen column to the frozen column id in the active profile
      final String frozenColumnId = _activeProfile.frozenColumnId;

      if (frozenColumnId != null) {
         action_FreezeColumn(getColDef_ByColumnId(frozenColumnId));
      }
   }

   /**
    * Restore the column order and width from a memento
    * except for CUSTOM TRACKS (no restore for them) !!
    *
    * @param state
    */
   private void restoreState(final IDialogSettings state) {

      ColumnProfile activeProfile = null;
      final ArrayList<ColumnProfile> allProfiles = new ArrayList<>();

      final String stateValue = Util.getStateString(state, XML_STATE_COLUMN_MANAGER, null);
      if (stateValue != null) {

         try {

            final Reader reader = new StringReader(stateValue);
            final XMLMemento xmlMemento = XMLMemento.createReadRoot(reader);

            boolean isDumpXML = false;
            isDumpXML = false;
//          isDumpXML = true;
            if (isDumpXML) {
               System.out.println();
               System.out.println(StringEscapeUtils.unescapeHtml4(xmlMemento.toString()));
               System.out.println();
            }

            // get category column state
            final Boolean xmlIsShowCategory = xmlMemento.getBoolean(ATTR_IS_SHOW_CATEGORY);
            if (xmlIsShowCategory != null) {
               _isShowCategory = xmlIsShowCategory;
            }

            // get annotation states
            final Boolean xmlIsShowAnnotation_Formatting = xmlMemento.getBoolean(ATTR_IS_SHOW_COLUMN_ANNOTATION_FORMATTING);
            if (xmlIsShowAnnotation_Formatting != null) {
               _isShowColumnAnnotation_Formatting = xmlIsShowAnnotation_Formatting;
            }
            final Boolean xmlIsShowAnnotation_Sorting = xmlMemento.getBoolean(ATTR_IS_SHOW_COLUMN_ANNOTATION_SORTING);
            if (xmlIsShowAnnotation_Sorting != null) {
               _isShowColumnAnnotation_Sorting = xmlIsShowAnnotation_Sorting;
            }

            // get profiles
            for (final IMemento memento : xmlMemento.getChildren()) {

               final XMLMemento xmlProfile = (XMLMemento) memento;

               if (TAG_PROFILE.equals(xmlProfile.getType())) {

                  final ColumnProfile currentProfile = new ColumnProfile();

                  // name
                  final String xmlName = xmlProfile.getString(ATTR_NAME);
                  if (xmlName != null) {
                     currentProfile.name = xmlName;
                  }

                  // active profile
                  final Boolean xmlIsActive = xmlProfile.getBoolean(ATTR_IS_ACTIVE_PROFILE);
                  if (xmlIsActive != null && xmlIsActive) {
                     activeProfile = currentProfile;
                  }

                  // frozen column id
                  final String xmlFrozenColumnId = xmlProfile.getString(ATTR_FROZEN_COLUMN_ID);

                  if (xmlFrozenColumnId != null && !xmlFrozenColumnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
                     currentProfile.frozenColumnId = xmlFrozenColumnId;
                  }

                  // visible column id's
                  final String xmlColumnIds = xmlProfile.getString(ATTR_VISIBLE_COLUMN_IDS);
                  if (xmlColumnIds != null) {
                     final String[] xmlColumnIdsArray = StringToArrayConverter.convertStringToArray(xmlColumnIds);
                     final ArrayList<String> xmlColumnIdsArrayList = new ArrayList<>();
                     for (final String xmlColumnId : xmlColumnIdsArray) {
                        if (!xmlColumnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
                           xmlColumnIdsArrayList.add(xmlColumnId);
                        }
                     }
                     if (xmlColumnIdsArrayList.size() > 0) {
                        currentProfile.setVisibleColumnIds(xmlColumnIdsArrayList.toArray(new String[xmlColumnIdsArrayList.size()]));
                     }
                  }

                  // visible column id's and width
                  final String xmlColumnIdsAndWidth = xmlProfile.getString(ATTR_VISIBLE_COLUMN_IDS_AND_WIDTH);
                  if (xmlColumnIdsAndWidth != null) {
                     final String[] xmlColumnIdsAndWidthArray = StringToArrayConverter.convertStringToArray(xmlColumnIdsAndWidth);
                     final ArrayList<String> xmlColumnIdsAndWidthArrayList = new ArrayList<>();
                     for (int j = 0; j < xmlColumnIdsAndWidthArray.length; j++) {
                        final String xmlColumnIdAndWidth = xmlColumnIdsAndWidthArray[j++];
                        if (!xmlColumnIdAndWidth.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
                           xmlColumnIdsAndWidthArrayList.add(xmlColumnIdAndWidth);
                           xmlColumnIdsAndWidthArrayList.add(xmlColumnIdsAndWidthArray[j]);
                        }
                     }
                     if (xmlColumnIdsAndWidthArrayList.size() > 0) {
                        currentProfile.visibleColumnIdsAndWidth = xmlColumnIdsAndWidthArrayList.toArray(new String[xmlColumnIdsAndWidthArrayList.size()]);
                     }
                  }

                  /*
                   * Column properties, all properties are optional
                   */
                  final ArrayList<ColumnProperties> allColumnProperties = new ArrayList<>();
                  currentProfile.columnProperties = allColumnProperties;

                  for (final IMemento memento2 : xmlProfile.getChildren()) {

                     final XMLMemento xmlColumn = (XMLMemento) memento2;

                     if (TAG_COLUMN.equals(xmlColumn.getType())) {

                        final String columnId = xmlColumn.getString(ATTR_COLUMN_ID);

                        if (columnId == null ||
                              columnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
                           continue;
                        }

                        final ColumnProperties columnProperties = new ColumnProperties();

                        columnProperties.columnId = columnId;

                        final Enum<ValueFormat> valueFormat_Category = Util.getXmlEnum(
                              xmlColumn,
                              ATTR_COLUMN_FORMAT_CATEGORY,
                              ValueFormat.DUMMY_VALUE);
                        final Enum<ValueFormat> valueFormat_Detail = Util.getXmlEnum(
                              xmlColumn,
                              ATTR_COLUMN_FORMAT_DETAIL,
                              ValueFormat.DUMMY_VALUE);

                        final String alignment = Util.getXmlString(xmlColumn, ATTR_COLUMN_ALIGNMENT, null);

                        if (valueFormat_Category != ValueFormat.DUMMY_VALUE) {
                           columnProperties.valueFormat_Category = (ValueFormat) valueFormat_Category;
                        }

                        if (valueFormat_Detail != ValueFormat.DUMMY_VALUE) {
                           columnProperties.valueFormat_Detail = (ValueFormat) valueFormat_Detail;
                        }

                        if (alignment != null) {
                           columnProperties.alignment = getAlignmentValue(alignment);
                        }

                        allColumnProperties.add(columnProperties);
                     }
                  }

                  allProfiles.add(currentProfile);
               }
            }

         } catch (final WorkbenchException e) {
            // ignore
         }
      }

      // ensure 1 profile is available
      if (allProfiles.isEmpty()) {

         // create default profile
         final ColumnProfile defaultProfile = new ColumnProfile();
         defaultProfile.name = Messages.Column_Profile_Name_Default;

         allProfiles.add(defaultProfile);
      }

      if (activeProfile == null) {

         // use 1st profile as default
         activeProfile = allProfiles.get(0);
      }

      _activeProfile = activeProfile;
      _allProfiles = allProfiles;
   }

   /**
    * Save the column order and width into a memento
    * except for CUSTOM TRACKS (no save for them) !!
    *
    * @param state
    */
   public void saveState(final IDialogSettings state) {

      /*
       * Update state for the active profile
       */

      // save column sort order
      final String[] visibleColumnIds = getColumns_FromViewer_Ids();
      if (visibleColumnIds != null) {
         _activeProfile.setVisibleColumnIds(visibleColumnIds);
      }

      // save columns width
      final String[] visibleColumnIdsAndWidth = getColumns_FromViewer_IdAndWidth();
      if (visibleColumnIdsAndWidth != null) {
         _activeProfile.visibleColumnIdsAndWidth = visibleColumnIdsAndWidth;
      }

      saveState_All(state);
   }

   public void saveState(final IDialogSettings state,
                         final DataLayer dataLayer,
                         final ColumnReorderLayer columnReorderLayer,
                         final ColumnHideShowLayer columnHideShowLayer) {

      if (dataLayer == null) {
         return;
      }

      /*
       * Update state for the active profile
       */

      final int numColumns = dataLayer.getColumnCount();

      final ArrayList<String> allOrderedColumnIds = new ArrayList<>();
      final ArrayList<String> allColumnIdsAndWidth = new ArrayList<>();

//// TODO
//      System.out.println("All columns in _allDefinedColumnDefinitions, number of entries: " + _allDefinedColumnDefinitions.size()); //$NON-NLS-1$
//      System.out.println();
//
//      for (final ColumnDefinition colDefLog : _allDefinedColumnDefinitions) {
//         System.out.println("  \"" + colDefLog.getColumnId() + UI.SYMBOL_QUOTATION_MARK); //$NON-NLS-1$ //$NON-NLS-2$
//      }
//
//      System.out.println();
//      System.out.println();

      for (int uiColumnPos = 0; uiColumnPos < numColumns; uiColumnPos++) {

         /*
          * This looks a bit complicated, it is. It respects reordered and hidden columns. This
          * solution was found with trial and error until it worked.
          */

         // the reorder layer contains the correct column order for cases when columns are moved with drag&drop
         final int createdColumnIndex = columnReorderLayer.getColumnIndexByPosition(uiColumnPos);

         final int colIndexByPos = dataLayer.getColumnIndexByPosition(createdColumnIndex);

         // the column hide show layer has the info if a column was set to hidden by the user
         final boolean isColumnHidden = columnHideShowLayer.isColumnIndexHidden(colIndexByPos);
         if (isColumnHidden) {
            continue;
         }

         final ColumnDefinition colDef = getColDef_ByCreateIndex(colIndexByPos);
         if (colDef != null) {

            final int columnWidth = getColumnWidth_NatTable(dataLayer, createdColumnIndex);
            final String columnId = colDef.getColumnId();

            if (columnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
               continue;
            }
            allOrderedColumnIds.add(columnId);
            setColumnIdAndWidth(allColumnIdsAndWidth, columnId, columnWidth);
         }
      }

      _activeProfile.setVisibleColumnIds(allOrderedColumnIds.toArray(new String[allOrderedColumnIds.size()]));
      _activeProfile.visibleColumnIdsAndWidth = allColumnIdsAndWidth.toArray(new String[allColumnIdsAndWidth.size()]);

      saveState_All(state);
   }

   /**
    * Save states for all profiles
    *
    * @param state
    */
   private void saveState_All(final IDialogSettings state) {

      // Build the XML block for writing the bindings and active scheme.
      final XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);

      // save other states
      xmlMemento.putBoolean(ATTR_IS_SHOW_CATEGORY, _isShowCategory);
      xmlMemento.putBoolean(ATTR_IS_SHOW_COLUMN_ANNOTATION_FORMATTING, _isShowColumnAnnotation_Formatting);
      xmlMemento.putBoolean(ATTR_IS_SHOW_COLUMN_ANNOTATION_SORTING, _isShowColumnAnnotation_Sorting);

      // save profiles
      saveState_Profiles(xmlMemento);

      // Write the XML block to the state store.
      try (final Writer writer = new StringWriter()) {

         xmlMemento.save(writer);
         state.put(XML_STATE_COLUMN_MANAGER, writer.toString());

      } catch (final IOException e) {
         StatusUtil.log(e);
      }
   }

   private void saveState_Profiles(final XMLMemento xmlMemento) {

      for (final ColumnProfile profile : _allProfiles) {

         final IMemento xmlProfile = xmlMemento.createChild(TAG_PROFILE);

         xmlProfile.putString(ATTR_NAME, profile.name);

         /*
          * Set flag which is the active profile
          */
         if (profile == _activeProfile) {
            xmlProfile.putBoolean(ATTR_IS_ACTIVE_PROFILE, true);
         }

         /*
          * frozenColumnId
          */
         if (profile.frozenColumnId != null && !profile.frozenColumnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
            xmlProfile.putString(ATTR_FROZEN_COLUMN_ID, profile.frozenColumnId);
         }

         /*
          * visibleColumnIds
          */
         final String[] visibleColumnIds = profile.getVisibleColumnIds();

         if (visibleColumnIds != null) {
            //CUSTOM TRACKS visible columns must not be saved
            final ArrayList<String> visibleColumnIdsNoCustomTracksList = new ArrayList<>();
            for (final String visibleColumnId : visibleColumnIds) {
               if (!visibleColumnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
                  visibleColumnIdsNoCustomTracksList.add(visibleColumnId);
               }
            }
            if (visibleColumnIdsNoCustomTracksList.size() > 0) {
               final String[] visibleColumnIdsNoCustomTracks = visibleColumnIdsNoCustomTracksList.toArray(
                     new String[visibleColumnIdsNoCustomTracksList.size()]);
               xmlProfile.putString(ATTR_VISIBLE_COLUMN_IDS, StringToArrayConverter.convertArrayToString(visibleColumnIdsNoCustomTracks));
            }
         }

         /*
          * visibleColumnIdsAndWidth
          */
         final String[] visibleColumnIdsAndWidth = profile.visibleColumnIdsAndWidth;
         if (visibleColumnIdsAndWidth != null) {
            //CUSTOM TRACKS visibleColumnIdsAndWidth must not be saved on disk
            final ArrayList<String> visibleColumnIdsAndWidthNoCustomTracksList = new ArrayList<>();
            for (int j = 0; j < visibleColumnIdsAndWidth.length; j++) {
               final String visibleColumnId = visibleColumnIdsAndWidth[j++];
               if (!visibleColumnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
                  visibleColumnIdsAndWidthNoCustomTracksList.add(visibleColumnId);
                  visibleColumnIdsAndWidthNoCustomTracksList.add(visibleColumnIdsAndWidth[j]);
               }
            }
            if (visibleColumnIdsAndWidthNoCustomTracksList.size() > 0) {
               final String[] visibleColumnIdsAndWidthNoCustomTracks = visibleColumnIdsAndWidthNoCustomTracksList.toArray(new String[visibleColumnIdsAndWidthNoCustomTracksList
                     .size()]);
               xmlProfile.putString(ATTR_VISIBLE_COLUMN_IDS_AND_WIDTH, StringToArrayConverter.convertArrayToString(visibleColumnIdsAndWidthNoCustomTracks));
            }
         }

         /*
          * Column properties
          */
         for (final ColumnProperties columnProperty : profile.columnProperties) {

            if (!columnProperty.columnId.startsWith(CUSTOM_TRACKS_TIME_SLICES_ID)) {
               final IMemento xmlColumn = xmlProfile.createChild(TAG_COLUMN);

               xmlColumn.putString(ATTR_COLUMN_ID, columnProperty.columnId);

               final Enum<ValueFormat> columnFormat = columnProperty.valueFormat_Category;
               if (columnFormat != null) {
                  xmlColumn.putString(ATTR_COLUMN_FORMAT_CATEGORY, columnFormat.name());
               }

               final Enum<ValueFormat> columnFormat_Detail = columnProperty.valueFormat_Detail;
               if (columnFormat_Detail != null) {
                  xmlColumn.putString(ATTR_COLUMN_FORMAT_DETAIL, columnFormat_Detail.name());
               }

               final int alignment = columnProperty.alignment;
               if (alignment != 0) {
                  xmlColumn.putString(ATTR_COLUMN_ALIGNMENT, getAlignmentText(alignment));
               }
            }
         }
      }
   }

   /**
    * @param columnIdsAndWidth
    *           Columns are in the format: id/width ...
    * @param columnId
    * @param columnWidth
    */
   private void setColumnIdAndWidth(final ArrayList<String> columnIdsAndWidth, final String columnId, int columnWidth) {

      final ColumnDefinition colDef = getColDef_ByColumnId(columnId);

      if (colDef == null) {

         final String errorMessage = String.format("\"colDef\" is null for columnId = \"%s\"", columnId); //$NON-NLS-1$

         System.out.println(errorMessage);
// TODO remove SYSTEM.OUT.PRINTLN

//       StatusUtil.log(new Throwable(errorMessage));

         return;
      }

      if (colDef.isColumnHidden()) {

         // column is hidden

         columnWidth = 0;

      } else {

         // column is visible

         if (columnWidth == 0) {

            // there is somewhere an error that the column width is 0,

            columnWidth = colDef.getDefaultColumnWidth();
            columnWidth = Math.max(COLUMN_WIDTH_MINIMUM, columnWidth);
         }
      }

      columnWidth = Math.min(columnWidth, COLUMN_WIDTH_MAXIMUM);

      columnIdsAndWidth.add(columnId);
      columnIdsAndWidth.add(Integer.toString(columnWidth));
   }

   /**
    * Sets the column layout for the viewer which is managed by the {@link ColumnManager}.
    * <p>
    * When the columnLayout is set, all columns must have a {@link ColumnWeightData}, otherwise it
    * will fail
    *
    * @param columnLayout
    */
   public void setColumnLayout(final AbstractColumnLayout columnLayout) {
      _columnLayout = columnLayout;
   }

   /**
    * Show or hide a column in the viewer.
    *
    * @param columnDefinition
    * @param isVisible
    */
   public void setColumnVisible(final TableColumnDefinition columnDefinition, final boolean isVisible) {

      // save current column widths into the model
      setVisibleColumnIds_FromViewer();

      // update model from the model -> highly complicated but is seems to work
      getRearrangedColumns();

      if (isVisible) {

         // show column

         // even more complicated -> update model otherwise column is not painted
         columnDefinition.setIsColumnChecked(true);

         setVisibleColumnIds_Column_Show(columnDefinition, true);

      } else {

         // hide column

         columnDefinition.setIsColumnChecked(false);

         setVisibleColumnIds_Column_Hide(columnDefinition);
      }
   }

   /**
    * @param isCategoryAvailable
    *           When <code>true</code>, columns can be categorized which is displayed in the UI but
    *           it has no
    *           other functions, default is <code>false</code>.
    *           <p>
    *           When only a view columns are contained in a view then it does not make sense to
    *           categorize
    *           these columns.
    */
   public void setIsCategoryAvailable(final boolean isCategoryAvailable) {
      _isCategoryAvailable = isCategoryAvailable;
   }

   public void setIsShowCategory(final boolean isShowCategory) {
      _isShowCategory = isShowCategory;
   }

   void setIsShowColumnAnnotation_Formatting(final boolean isShowColumnAnnotation_Formatting) {
      _isShowColumnAnnotation_Formatting = isShowColumnAnnotation_Formatting;
   }

   public void setIsShowColumnAnnotation_Sorting(final boolean isShowColumnAnnotation_Sorting) {
      _isShowColumnAnnotation_Sorting = isShowColumnAnnotation_Sorting;
   }

   public void setSlideoutShell(final AdvancedSlideoutShell slideoutShell) {
      _slideoutShell = slideoutShell;
   }

   /**
    * Sync column definitions in the {@link ColumnProfile} from the visible id's.
    *
    * @param columnProfile
    */
   void setupColumns_01_VisibleColDefs(final ColumnProfile columnProfile) {

      final List<ColumnDefinition> allVisibleColDefs = columnProfile.visibleColumnDefinitions;

      allVisibleColDefs.clear();

      final String[] visibleColumnIds = columnProfile.getVisibleColumnIds();
      if (visibleColumnIds != null) {

         // fill columns with the visible order

         int createIndex = 0;

         for (final String columnId : visibleColumnIds) {

            final ColumnDefinition colDef = getColDef_ByColumnId(columnId);
            if (colDef != null) {

               colDef.setCreateIndex(createIndex++);

               allVisibleColDefs.add(colDef);
            }
         }
      }

      final String[] visibleColumnIdsAndWidth = columnProfile.visibleColumnIdsAndWidth;
      if (visibleColumnIdsAndWidth != null) {

         // set the width for all columns

         for (int dataIdx = 0; dataIdx < visibleColumnIdsAndWidth.length; dataIdx++) {

            final String columnId = visibleColumnIdsAndWidth[dataIdx++];
            final int columnWidth = Integer.valueOf(visibleColumnIdsAndWidth[dataIdx]);

            final ColumnDefinition colDef = getColDef_ByColumnId(columnId);
            if (colDef != null) {
               colDef.setColumnWidth(columnWidth);
            }
         }
      }

      /*
       * When no columns are visible (which is the first time), show only the default columns
       * because every column reduces performance
       */
      if ((allVisibleColDefs.isEmpty()) && (_allDefinedColumnDefinitions.size() > 0)) {

         final ArrayList<String> columnIds = new ArrayList<>();
         int createIndex = 0;

         for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {
            if (colDef.isDefaultColumn()) {

               colDef.setCreateIndex(createIndex++);

               allVisibleColDefs.add(colDef);
               columnIds.add(colDef.getColumnId());
            }
         }

         columnProfile.setVisibleColumnIds(columnIds.toArray(new String[columnIds.size()]));
      }

      /*
       * When no default columns are set, use the first column
       */
      if ((allVisibleColDefs.isEmpty()) && (_allDefinedColumnDefinitions.size() > 0)) {

         final ColumnDefinition firstColumn = _allDefinedColumnDefinitions.get(0);
         firstColumn.setCreateIndex(0);

         allVisibleColDefs.add(firstColumn);

         columnProfile.setVisibleColumnIds(new String[1]);
         visibleColumnIds[0] = firstColumn.getColumnId();
      }

      /*
       * Ensure that all columns which must be visible, are also displayed. This case can happen
       * when new columns are added.
       */
      final ArrayList<ColumnDefinition> notAddedColumns = new ArrayList<>();

      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {

         if (colDef.canModifyVisibility() == false) {

            if (allVisibleColDefs.contains(colDef) == false) {
               notAddedColumns.add(colDef);
            }
         }
      }

      if (notAddedColumns.size() > 0) {

         allVisibleColDefs.addAll(notAddedColumns);

         /*
          * Set create index, otherwise save/restore do not work!!!
          */
         int createIndex = 0;
         for (final ColumnDefinition colDef : allVisibleColDefs) {
            colDef.setCreateIndex(createIndex++);
         }

         /*
          * Set visible id's
          */
         final ArrayList<String> columnIds = new ArrayList<>();

         for (final ColumnDefinition colDef : allVisibleColDefs) {
            columnIds.add(colDef.getColumnId());
         }

         columnProfile.setVisibleColumnIds(columnIds.toArray(new String[columnIds.size()]));
      }

      /*
       * Ensure that each visible column has also set it's column width
       */
      int numCheckedColumns = 0;
      for (final ColumnDefinition colDef : allVisibleColDefs) {

         if (colDef.getColumnWidth() == 0) {
            colDef.setColumnWidth(colDef.getDefaultColumnWidth());
         }

         if (colDef.isColumnCheckedInContextMenu()) {
            numCheckedColumns++;
         }
      }
      if (numCheckedColumns == 0) {

         // nothing is displayed -> show all columns

         for (final ColumnDefinition colDef : allVisibleColDefs) {
            colDef.setIsColumnChecked(true);
         }
      }
   }

   /**
    * Setup alignment for newly created columns
    *
    * @param activeProfile
    */
   private void setupColumns_02_ColumnAlignment(final ColumnProfile activeProfile) {

      final ArrayList<ColumnProperties> allProfileColumnProperties = activeProfile.columnProperties;

      // loop: all defined columns
      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {

         final String colDefID = colDef.getColumnId();

         /*
          * Reset current style to the default style which will be overwritten when profile
          * properties are available
          */
         colDef.setStyle(colDef.getDefaultColumnStyle());

         // loop: all column properties in a profile -> find column and it's properties
         for (final ColumnProperties profileColumnProperties : allProfileColumnProperties) {

            if (colDefID.equals(profileColumnProperties.columnId)) {

               // column properties are available

               if (profileColumnProperties.alignment != 0) {

                  colDef.setStyle(profileColumnProperties.alignment);
               }

               break;
            }
         }
      }
   }

   private void setupColumns_03_ValueFormatter(final ColumnProfile activeProfile) {

      final ArrayList<ColumnProperties> profileColumnProperties = new ArrayList<>();

      for (final ColumnDefinition colDef : activeProfile.visibleColumnDefinitions) {

         final String columnId = colDef.getColumnId();

         final ValueFormat defaultFormat_Category = colDef.getDefaultValueFormat_Category();
         final ValueFormat defaultFormat_Detail = colDef.getDefaultValueFormat_Detail();

         ValueFormat valueFormat_Category = null;
         ValueFormat valueFormat_Detail = null;
         IValueFormatter valueFormatter_Category = null;
         IValueFormatter valueFormatter_Detail = null;

         ColumnProperties currentColumnProperties = null;

         for (final ColumnProperties columnProperties : activeProfile.columnProperties) {

            if (columnId.equals(columnProperties.columnId)) {

               currentColumnProperties = columnProperties;

               /*
                * Set format ONLY when default is set, this prevents that old saved settings are
                * still used.
                */

               if (defaultFormat_Category != null) {

                  valueFormat_Category = columnProperties.valueFormat_Category;
                  valueFormatter_Category = getValueFormatter(valueFormat_Category);
               }

               if (defaultFormat_Detail != null) {

                  valueFormat_Detail = columnProperties.valueFormat_Detail;
                  valueFormatter_Detail = getValueFormatter(valueFormat_Detail);
               }

               break;
            }
         }

         if (valueFormatter_Category == null && defaultFormat_Category != null) {

            valueFormat_Category = defaultFormat_Category;
            valueFormatter_Category = getValueFormatter(defaultFormat_Category);
         }

         if (valueFormatter_Detail == null && defaultFormat_Detail != null) {

            valueFormat_Detail = defaultFormat_Detail;
            valueFormatter_Detail = getValueFormatter(defaultFormat_Detail);
         }

         colDef.setValueFormatter_Category(valueFormat_Category, valueFormatter_Category);
         colDef.setValueFormatter_Detail(valueFormat_Detail, valueFormatter_Detail);

         // ensure all column properties for all visible columns are created
         if (currentColumnProperties == null) {

            // column properties are not available

            final ColumnProperties columnProperties = new ColumnProperties();

            columnProperties.columnId = columnId;
            columnProperties.valueFormat_Category = valueFormat_Category;
            columnProperties.valueFormat_Detail = valueFormat_Detail;

            profileColumnProperties.add(columnProperties);

         } else {

            profileColumnProperties.add(currentColumnProperties);
         }
      }

      // update model
      activeProfile.columnProperties.clear();
      activeProfile.columnProperties.addAll(profileColumnProperties);
   }

   public void setupNatTable(final INatTable_PropertiesProvider natTablePropertiesProvider) {

      _natTablePropertiesProvider = natTablePropertiesProvider;

      setupColumns_01_VisibleColDefs(_activeProfile);
      setupColumns_02_ColumnAlignment(_activeProfile);
      setupColumns_03_ValueFormatter(_activeProfile);
   }

   /**
    * Setup {@link NatTable} for autoresizing all columns after the table was created.
    */
   public void setupNatTable_PostCreate() {

      final NatTable natTable = _natTablePropertiesProvider.getNatTable();

      /**
       * Found this solution in https://www.eclipse.org/nattable/documentation.php?page=faq
       */
      natTable.addOverlayPainter((gc, layer) -> {

         if (!_isDoAResizeForAllColumnsToFit) {
            return;
         }

         // reset flag, resizing is done only once when the corresponding action is selected
         _isDoAResizeForAllColumnsToFit = false;

         final int numColumns = natTable.getColumnCount();

         final IConfigRegistry configRegistry = natTable.getConfigRegistry();
         final GCFactory gcFactory = new GCFactory(natTable);

         for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {

            if (natTable.isColumnPositionResizable(columnIndex) == false) {
               continue;
            }

            final InitializeAutoResizeColumnsCommand columnCommand = new InitializeAutoResizeColumnsCommand(
                  natTable,
                  columnIndex,
                  configRegistry,
                  gcFactory);

            natTable.doCommand(columnCommand);
         }
      });
   }

   private void setVisibleColumnIds_All() {

      final ArrayList<String> visibleColumnIds = new ArrayList<>();
      final ArrayList<String> visibleIdsAndWidth = new ArrayList<>();

      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {

         // set visible columns
         visibleColumnIds.add(colDef.getColumnId());

         // set column id and width
         visibleIdsAndWidth.add(colDef.getColumnId());
         visibleIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));

         colDef.setIsColumnChecked(true);
      }

      _activeProfile.setVisibleColumnIds(visibleColumnIds.toArray(new String[visibleColumnIds.size()]));
      _activeProfile.visibleColumnIdsAndWidth = visibleIdsAndWidth.toArray(new String[visibleIdsAndWidth.size()]);
   }

   private void setVisibleColumnIds_Column_Hide(final ColumnDefinition colDef_HeaderHit) {

      final String headerHitColId = colDef_HeaderHit.getColumnId();
      final String[] visibleIds = _activeProfile.getVisibleColumnIds();

      final ArrayList<String> visibleColumnIds = new ArrayList<>();
      final ArrayList<String> visibleIdsAndWidth = new ArrayList<>();

      for (final String columnId : visibleIds) {

         if (columnId.equals(headerHitColId)) {

            // set state that column is hidden

            colDef_HeaderHit.setIsColumnChecked(false);

         } else {

            // column is still displayed

            final ColumnDefinition colDef = getColDef_ByColumnId(columnId);

            // set visible columns
            visibleColumnIds.add(colDef.getColumnId());

            // set column id and width
            visibleIdsAndWidth.add(colDef.getColumnId());
            visibleIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));
         }
      }

      _activeProfile.setVisibleColumnIds(visibleColumnIds.toArray(new String[visibleColumnIds.size()]));
      _activeProfile.visibleColumnIdsAndWidth = visibleIdsAndWidth.toArray(new String[visibleIdsAndWidth.size()]);

      recreateViewer();
   }

   private void setVisibleColumnIds_Column_Show(final ColumnDefinition colDef_New, final boolean isFirstColumn) {

      final ArrayList<String> allNewVisibleColumnIds = new ArrayList<>();
      final ArrayList<String> allNewVisibleIdsAndWidth = new ArrayList<>();

      boolean isNewColumnAdded = false;

      final String columnId_New = colDef_New.getColumnId();

      if (isFirstColumn) {

         // ensure the column is also displayed, it could be hidden when it was previously displayed and then set to hidden !!!
         final ColumnDefinition colDef = getColDef_ByColumnId(columnId_New);
         colDef.setIsColumnChecked(true);

         // set visible columns
         allNewVisibleColumnIds.add(columnId_New);

         // set column id and width
         allNewVisibleIdsAndWidth.add(columnId_New);
         allNewVisibleIdsAndWidth.add(Integer.toString(colDef_New.getColumnWidth()));

         isNewColumnAdded = true;
      }

      for (final String columnId : _activeProfile.getVisibleColumnIds()) {

         final ColumnDefinition colDef = getColDef_ByColumnId(columnId);

         // ensure the column is also displayed, it could be hidden when it was previously displayed and then set to hidden !!!
         colDef.setIsColumnChecked(true);

         if (columnId_New.equals(colDef.getColumnId()) && isNewColumnAdded) {

            // column is already added
            continue;
         }

         // set visible columns
         allNewVisibleColumnIds.add(columnId);

         // set column id and width
         allNewVisibleIdsAndWidth.add(columnId);
         allNewVisibleIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));

         if (columnId_New.equals(colDef.getColumnId())) {
            isNewColumnAdded = true;
         }
      }

      /*
       * Insert new column at the current mouse position, after many years of frustration to move a
       * new column to the correct position
       */
      int newColumnPosition = -1;
      if (isNewColumnAdded == false) {

         final ColumnDefinition headerColDef = getColDef_FromHeaderColumn();
         if (headerColDef != null) {

            // find current header column position

            final String headerColumnId = headerColDef.getColumnId();

            for (int visibleIndex = 0; visibleIndex < allNewVisibleColumnIds.size(); visibleIndex++) {

               if (allNewVisibleColumnIds.get(visibleIndex).equals(headerColumnId)) {

                  // set position at the current column
                  newColumnPosition = visibleIndex;
                  break;
               }
            }
         }

         // ensure the column is also displayed, it could be hidden when it was previously displayed and then set to hidden !!!
         final ColumnDefinition colDef = getColDef_ByColumnId(columnId_New);
         colDef.setIsColumnChecked(true);

         if (newColumnPosition == -1) {

            // a new column position could not be found (should not happen), add column to the end

            // set visible columns
            allNewVisibleColumnIds.add(columnId_New);

            // set column id and width
            allNewVisibleIdsAndWidth.add(columnId_New);
            allNewVisibleIdsAndWidth.add(Integer.toString(colDef_New.getColumnWidth()));

         } else {

            // set visible columns
            allNewVisibleColumnIds.add(newColumnPosition, columnId_New);

            // set column id and width
            allNewVisibleIdsAndWidth.add(newColumnPosition * 2, columnId_New);
            allNewVisibleIdsAndWidth.add(newColumnPosition * 2 + 1, Integer.toString(colDef_New.getColumnWidth()));
         }
      }

      /*
       * Update model
       */
      _activeProfile.setVisibleColumnIds(allNewVisibleColumnIds.toArray(new String[allNewVisibleColumnIds.size()]));
      _activeProfile.visibleColumnIdsAndWidth = allNewVisibleIdsAndWidth.toArray(new String[allNewVisibleIdsAndWidth.size()]);

      /*
       * Update UI
       */
      recreateViewer();
   }

   private void setVisibleColumnIds_Default() {

      final ArrayList<String> visibleColumnIds = new ArrayList<>();
      final ArrayList<String> visibleIdsAndWidth = new ArrayList<>();

      for (final ColumnDefinition colDef : _allDefinedColumnDefinitions) {

         if (colDef.isDefaultColumn()) {

            // set visible columns
            visibleColumnIds.add(colDef.getColumnId());

            // set column id and width
            visibleIdsAndWidth.add(colDef.getColumnId());
            visibleIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));

            colDef.setIsColumnChecked(true);

         } else {

            colDef.setIsColumnChecked(false);
         }
      }

      _activeProfile.setVisibleColumnIds(visibleColumnIds.toArray(new String[visibleColumnIds.size()]));
      _activeProfile.visibleColumnIdsAndWidth = visibleIdsAndWidth.toArray(new String[visibleIdsAndWidth.size()]);
   }

   private void setVisibleColumnIds_FromMenu(final MenuItem[] menuItems) {

      final ArrayList<String> visibleColumnIds = new ArrayList<>();
      final ArrayList<String> columnIdsAndWidth = new ArrayList<>();

      // recreate columns in the correct sort order
      for (final MenuItem menuItem : menuItems) {

         final Object itemData = menuItem.getData();

         // data in the table item contains the input items for the viewer
         if (itemData instanceof final ColumnDefinition colDef) {

            final boolean isChecked = menuItem.getSelection();

            if (isChecked) {

               // set the visible columns
               visibleColumnIds.add(colDef.getColumnId());

               // set column id and width
               columnIdsAndWidth.add(colDef.getColumnId());
               columnIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));

               colDef.setIsColumnChecked(true);

            } else {

               colDef.setIsColumnChecked(false);
            }
         }
      }

      _activeProfile.setVisibleColumnIds(visibleColumnIds.toArray(new String[visibleColumnIds.size()]));
      _activeProfile.visibleColumnIdsAndWidth = columnIdsAndWidth.toArray(new String[columnIdsAndWidth.size()]);
   }

   /**
    * Set the columns in {@link #_activeProfile._visibleColumnDefinitions} to the order of the
    * <code>tableItems</code> in the {@link DialogModifyColumns}
    *
    * @param columnViewerModel
    */
   void setVisibleColumnIds_FromModel(final ColumnProfile profile, final List<ColumnDefinition> columnViewerModel) {

      final ArrayList<String> visibleColumnIds = new ArrayList<>();
      final ArrayList<String> columnIdsAndWidth = new ArrayList<>();

      // recreate columns in the correct sort order
      for (final ColumnDefinition colDef : columnViewerModel) {

         final String columnId = colDef.getColumnId();

         final boolean isColumnVisible = colDef.isColumnCheckedInContextMenu();

         // update original model, otherwise it could be hidden when it was previously displayed and then set to hidden !!!
         final ColumnDefinition colDef_Original = getColDef_ByColumnId(columnId);
         colDef_Original.setIsColumnChecked(isColumnVisible);

         if (isColumnVisible) {

            // set the visible columns
            visibleColumnIds.add(columnId);

            // set column id and width
            columnIdsAndWidth.add(columnId);
            columnIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));
         }
      }

      profile.setVisibleColumnIds(visibleColumnIds.toArray(new String[visibleColumnIds.size()]));
      profile.visibleColumnIdsAndWidth = columnIdsAndWidth.toArray(new String[columnIdsAndWidth.size()]);
   }

   /**
    * Set the columns in {@link #_activeProfile._visibleColumnDefinitions} to the order of the
    * <code>tableItems</code> in the {@link DialogModifyColumns}
    *
    * @param tableItems
    */
   void setVisibleColumnIds_FromModifyDialog(final ColumnProfile profile, final TableItem[] tableItems) {

      final ArrayList<String> visibleColumnIds = new ArrayList<>();
      final ArrayList<String> columnIdsAndWidth = new ArrayList<>();

      // recreate columns in the correct sort order
      for (final TableItem tableItem : tableItems) {

         // data in the table item contains the input items for the viewer
         final ColumnDefinition colDef = (ColumnDefinition) tableItem.getData();

         final String columnId = colDef.getColumnId();

         final boolean isColumnVisible = tableItem.getChecked();

         // update original model, otherwise it could be hidden when it was previously displayed and then set to hidden !!!
         final ColumnDefinition colDef_Original = getColDef_ByColumnId(columnId);
         colDef_Original.setIsColumnChecked(isColumnVisible);

         if (isColumnVisible) {

            // set the visible columns
            visibleColumnIds.add(columnId);

            // set column id and width
            columnIdsAndWidth.add(columnId);
            columnIdsAndWidth.add(Integer.toString(colDef.getColumnWidth()));
         }
      }

      profile.setVisibleColumnIds(visibleColumnIds.toArray(new String[visibleColumnIds.size()]));
      profile.visibleColumnIdsAndWidth = columnIdsAndWidth.toArray(new String[columnIdsAndWidth.size()]);
   }

   /**
    * Read the sorting order and column width from the viewer/nattable.
    */
   private void setVisibleColumnIds_FromViewer() {

      final String[] visibleColumnIds = getColumns_FromViewer_Ids();

      _activeProfile.setVisibleColumnIds(visibleColumnIds);
      _activeProfile.visibleColumnIdsAndWidth = getColumns_FromViewer_IdAndWidth();

//      // sync with visible columm definitions
//      final ArrayList<ColumnDefinition> visibleColDefs = _activeProfile.visibleColumnDefinitions;
//      visibleColDefs.clear();
//
//      for (final String columnId : visibleColumnIds) {
//
//         final ColumnDefinition colDef = getColDef_ByColumnId(columnId);
//
//         if (colDef != null) {
//
//            visibleColDefs.add(colDef);
//
//            // update column width
//            colDef.setColumnWidth(getColumnWidth(colDef.getColumnId()));
//         }
//      }
//
//      int a = 0;
//      a++;
   }

   private void updateColumns(final ColumnProfile profile) {

      _activeProfile = profile;

      recreateViewer();
   }

   /**
    * Update the viewer with the columns from the {@link DialogModifyColumns}
    *
    * @param dialogActiveProfile
    * @param tableItems
    *           table item in the {@link DialogModifyColumns}
    */
   void updateColumns(final ColumnProfile dialogActiveProfile, final TableItem[] tableItems) {

      _activeProfile = dialogActiveProfile;

      setVisibleColumnIds_FromModifyDialog(_activeProfile, tableItems);

      recreateViewer();
   }

   private void updateColumns(final MenuItem[] menuItems) {

      setVisibleColumnIds_FromMenu(menuItems);

      recreateViewer();

   }

}
