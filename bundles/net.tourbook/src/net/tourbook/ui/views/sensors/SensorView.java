/*******************************************************************************
 * Copyright (C) 2021, 2025 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.sensors;

import static org.eclipse.swt.events.KeyListener.keyPressedAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.tourbook.Images;
import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.CommonActivator;
import net.tourbook.common.UI;
import net.tourbook.common.preferences.ICommonPreferences;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.ColumnDefinition;
import net.tourbook.common.util.ColumnManager;
import net.tourbook.common.util.IContextMenuProvider;
import net.tourbook.common.util.ITourViewer;
import net.tourbook.common.util.SQL;
import net.tourbook.common.util.Util;
import net.tourbook.data.DeviceSensor;
import net.tourbook.data.DeviceSensorType;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.SelectionDeletedTours;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.TableColumnFactory;

import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.part.ViewPart;

public class SensorView extends ViewPart implements ITourViewer {

   public static final String      ID                              = "net.tourbook.ui.views.sensors.SensorView.ID"; //$NON-NLS-1$

   private static final char       NL                              = UI.NEW_LINE;

   private static final String     STATE_SELECTED_SENSOR_INDICES   = "STATE_SELECTED_SENSOR_INDICES";               //$NON-NLS-1$
   private static final String     STATE_SORT_COLUMN_DIRECTION     = "STATE_SORT_COLUMN_DIRECTION";                 //$NON-NLS-1$
   private static final String     STATE_SORT_COLUMN_ID            = "STATE_SORT_COLUMN_ID";                        //$NON-NLS-1$

   private final IPreferenceStore  _prefStore                      = TourbookPlugin.getPrefStore();
   private final IPreferenceStore  _prefStore_Common               = CommonActivator.getPrefStore();
   private final IDialogSettings   _state                          = TourbookPlugin.getState(ID);

   private ISelectionListener      _postSelectionListener;
   private IPropertyChangeListener _prefChangeListener;
   private IPropertyChangeListener _prefChangeListener_Common;
   private ITourEventListener      _tourPropertyListener;

   private TableViewer             _sensorViewer;
   private SensorComparator        _sensorComparator               = new SensorComparator();
   private ColumnManager           _columnManager;
   private SelectionAdapter        _columnSortListener;

   private List<SensorItem>        _allSensorItems                 = new ArrayList<>();

   private MenuManager             _viewerMenuManager;
   private IContextMenuProvider    _tableViewerContextMenuProvider = new TableContextMenuProvider();

   private boolean                 _isInUIUpdate;

   private final NumberFormat      _nf1                            = NumberFormat.getNumberInstance();
   private final NumberFormat      _nf3                            = NumberFormat.getNumberInstance();
   {
      _nf1.setMinimumFractionDigits(1);
      _nf1.setMaximumFractionDigits(1);
      _nf3.setMinimumFractionDigits(3);
      _nf3.setMaximumFractionDigits(3);
   }

   private Action_DeleteSensor    _action_DeleteSensor;
   private Action_EditSensor      _action_EditSensor;
   private Action_OpenSensorChart _action_OpenSensorChartView;

   /*
    * UI controls
    */
   private PixelConverter _pc;
   private Composite      _viewerContainer;

   private Menu           _tableContextMenu;

   private class Action_DeleteSensor extends Action {

      public Action_DeleteSensor() {

         setText(Messages.Sensor_View_Action_DeleteSensor);

         setImageDescriptor(TourbookPlugin.getImageDescriptor(Images.App_Delete));
      }

      @Override
      public void run() {

         onAction_DeleteSensor();
      }
   }

   private class Action_EditSensor extends Action {

      public Action_EditSensor() {

         setText(Messages.Sensor_View_Action_EditSensor);

         setImageDescriptor(TourbookPlugin.getImageDescriptor(Images.Sensor));
      }

      @Override
      public void run() {

         onAction_EditSensor();
      }
   }

   private class Action_OpenSensorChart extends Action {

      public Action_OpenSensorChart() {

         setText(Messages.Sensor_View_Action_OpenSensorChart);

         setImageDescriptor(TourbookPlugin.getImageDescriptor(Images.SensorChart));
      }

      @Override
      public void run() {

         onAction_OpenSensorChart();
      }
   }

   private class SensorComparator extends ViewerComparator {

      private static final int ASCENDING       = 0;
      private static final int DESCENDING      = 1;

      private String           __sortColumnId  = TableColumnFactory.SENSOR_NAME_ID;
      private int              __sortDirection = ASCENDING;

      @Override
      public int compare(final Viewer viewer, final Object e1, final Object e2) {

         final SensorItem item1 = (SensorItem) e1;
         final SensorItem item2 = (SensorItem) e2;

         double rc = 0;

         // Determine which column and do the appropriate sort
         switch (__sortColumnId) {

         case TableColumnFactory.SENSOR_MANUFACTURER_NAME_ID:
            rc = item1.sensor.getManufacturerName().compareTo(item2.sensor.getManufacturerName());
            break;

         case TableColumnFactory.SENSOR_MANUFACTURER_NUMBER_ID:

            rc = item1.sensor.getManufacturerNumber() - item2.sensor.getManufacturerNumber();

            if (rc == 0) {
               rc = item1.sensor.getProductNumber() - item2.sensor.getProductNumber();
            }

            break;

         case TableColumnFactory.SENSOR_PRODUCT_NAME_ID:
            rc = item1.sensor.getProductName().compareTo(item2.sensor.getProductName());
            break;

         case TableColumnFactory.SENSOR_PRODUCT_NUMBER_ID:

            rc = item1.sensor.getProductNumber() - item2.sensor.getProductNumber();

            if (rc == 0) {
               rc = item1.sensor.getManufacturerNumber() - item2.sensor.getManufacturerNumber();
            }

            break;

         case TableColumnFactory.SENSOR_SERIAL_NUMBER_ID:

            final DeviceSensor sensor1 = item1.sensor;
            final DeviceSensor sensor2 = item2.sensor;

            final long serialNumber1AsLong = sensor1.getSerialNumberAsLong();
            final long serialNumber2AsLong = sensor2.getSerialNumberAsLong();

            if (serialNumber1AsLong != Long.MIN_VALUE && serialNumber2AsLong != Long.MIN_VALUE) {

               // firstly sort by serial number

               rc = serialNumber1AsLong - serialNumber2AsLong;

            } else if (serialNumber1AsLong != Long.MIN_VALUE) {

               // must be set otherwise: java.lang.IllegalArgumentException: Comparison method violates its general contract!

               rc = 1;

            } else if (serialNumber2AsLong != Long.MIN_VALUE) {

               // must be set otherwise: java.lang.IllegalArgumentException: Comparison method violates its general contract!

               rc = -1;

            } else {

               // secondly sort by sensor key

               rc = sensor1.getSensorKey_WithDevType().compareTo(sensor2.getSensorKey_WithDevType());
            }

            break;

         case TableColumnFactory.SENSOR_STATE_BATTERY_LEVEL_ID: // %

            final int isBatteryLevelAvailable1 = item1.isBatteryLevelAvailable ? 1 : 0;
            final int isBatteryLevelAvailable2 = item2.isBatteryLevelAvailable ? 1 : 0;

            rc = isBatteryLevelAvailable1 - isBatteryLevelAvailable2;

            if (rc == 0) {

               final int isBatteryVoltageAvailable1 = item1.isBatteryVoltageAvailable ? 1 : 0;
               final int isBatteryVoltageAvailable2 = item2.isBatteryVoltageAvailable ? 1 : 0;

               rc = isBatteryVoltageAvailable1 - isBatteryVoltageAvailable2;
            }

            if (rc == 0) {

               final int isBatteryStatusAvailable1 = item1.isBatteryStatusAvailable ? 1 : 0;
               final int isBatteryStatusAvailable2 = item2.isBatteryStatusAvailable ? 1 : 0;

               rc = isBatteryStatusAvailable1 - isBatteryStatusAvailable2;
            }

            break;

         case TableColumnFactory.SENSOR_STATE_BATTERY_VOLTAGE_ID: // V

            final int isBatteryVoltageAvailable1 = item1.isBatteryVoltageAvailable ? 1 : 0;
            final int isBatteryVoltageAvailable2 = item2.isBatteryVoltageAvailable ? 1 : 0;

            rc = isBatteryVoltageAvailable1 - isBatteryVoltageAvailable2;

            if (rc == 0) {

               final int isBatteryLevelAvailable1a = item1.isBatteryLevelAvailable ? 1 : 0;
               final int isBatteryLevelAvailable2a = item2.isBatteryLevelAvailable ? 1 : 0;

               rc = isBatteryLevelAvailable1a - isBatteryLevelAvailable2a;
            }

            if (rc == 0) {

               final int isBatteryStatusAvailable1 = item1.isBatteryStatusAvailable ? 1 : 0;
               final int isBatteryStatusAvailable2 = item2.isBatteryStatusAvailable ? 1 : 0;

               rc = isBatteryStatusAvailable1 - isBatteryStatusAvailable2;
            }

            break;

         case TableColumnFactory.SENSOR_STATE_BATTERY_STATUS_ID: // OK, Low, ...

            final int isBatteryStatusAvailable1 = item1.isBatteryStatusAvailable ? 1 : 0;
            final int isBatteryStatusAvailable2 = item2.isBatteryStatusAvailable ? 1 : 0;

            rc = isBatteryStatusAvailable1 - isBatteryStatusAvailable2;

            if (rc == 0) {

               final int isBatteryLevelAvailable1a = item1.isBatteryLevelAvailable ? 1 : 0;
               final int isBatteryLevelAvailable2a = item2.isBatteryLevelAvailable ? 1 : 0;

               rc = isBatteryLevelAvailable1a - isBatteryLevelAvailable2a;
            }

            if (rc == 0) {

               final int isBatteryVoltageAvailable1a = item1.isBatteryVoltageAvailable ? 1 : 0;
               final int isBatteryVoltageAvailable2a = item2.isBatteryVoltageAvailable ? 1 : 0;

               rc = isBatteryVoltageAvailable1a - isBatteryVoltageAvailable2a;
            }

            break;

         case TableColumnFactory.SENSOR_NUMBER_OF_TOURS_ID:
            rc = item1.numTours - item2.numTours;
            break;

         case TableColumnFactory.SENSOR_TIME_FIRST_USED_ID:
            rc = item1.usedFirstTime - item2.usedFirstTime;
            break;

         case TableColumnFactory.SENSOR_TIME_LAST_USED_ID:
            rc = item1.usedLastTime - item2.usedLastTime;
            break;

         case TableColumnFactory.SENSOR_TYPE_ID:

            final DeviceSensorType sensorType1 = item1.sensor.getSensorType();
            final DeviceSensorType sensorType2 = item2.sensor.getSensorType();

            if (sensorType1 != null && sensorType2 != null) {
               final String sensorTypeName1 = SensorManager.getSensorTypeName(sensorType1);
               final String sensorTypeName2 = SensorManager.getSensorTypeName(sensorType2);
               rc = sensorTypeName1.compareToIgnoreCase(sensorTypeName2);
            }

            break;

         case TableColumnFactory.SENSOR_NAME_KEY_ID:
            rc = item1.sensor.getSensorKey_WithDevType().compareTo(item2.sensor.getSensorKey_WithDevType());
            break;

         case TableColumnFactory.SENSOR_NAME_ID:
         default:
            rc = item1.sensor.getSensorName().compareTo(item2.sensor.getSensorName());
         }

         // 2nd sort by sensor custom name
         if (rc == 0) {
            rc = item1.sensor.getSensorName().compareTo(item2.sensor.getSensorName());
         }

         // 3nd sort by manufacturer name
         if (rc == 0) {
            rc = item1.sensor.getManufacturerName().compareTo(item2.sensor.getManufacturerName());
         }

         // 4nd sort by product name
         if (rc == 0) {
            rc = item1.sensor.getProductName().compareTo(item2.sensor.getProductName());
         }

         // If descending order, flip the direction
         if (__sortDirection == DESCENDING) {
            rc = -rc;
         }

         /*
          * MUST return 1 or -1 otherwise long values are not sorted correctly
          */
         return rc > 0
               ? 1
               : rc < 0
                     ? -1
                     : 0;
      }

      public void setSortColumn(final Widget widget) {

         final ColumnDefinition columnDefinition = (ColumnDefinition) widget.getData();
         final String columnId = columnDefinition.getColumnId();

         if (columnId.equals(__sortColumnId)) {

            // Same column as last sort; toggle the direction

            __sortDirection = 1 - __sortDirection;

         } else {

            // New column; do an ascent sorting

            __sortColumnId = columnId;
            __sortDirection = ASCENDING;
         }

         updateUI_SetSortDirection(__sortColumnId, __sortDirection);
      }
   }

   private class SensorContentProvider implements IStructuredContentProvider {

      @Override
      public void dispose() {}

      @Override
      public Object[] getElements(final Object inputElement) {
         return _allSensorItems.toArray();
      }

      @Override
      public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
   }

   private class SensorItem {

      DeviceSensor sensor;

      long         usedFirstTime;
      long         usedLastTime;

      boolean      isBatteryLevelAvailable;
      boolean      isBatteryStatusAvailable;
      boolean      isBatteryVoltageAvailable;

      int          numTours;

      @Override
      public boolean equals(final Object obj) {

         if (this == obj) {
            return true;
         }
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }

         final SensorItem other = (SensorItem) obj;
         if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
            return false;
         }

         return sensor.getSensorId() == other.sensor.getSensorId();
      }

      private SensorView getEnclosingInstance() {
         return SensorView.this;
      }

      @Override
      public int hashCode() {

         final int prime = 31;
         int result = 1;
         result = prime * result + getEnclosingInstance().hashCode();
         result = prime * result + Objects.hash(sensor.getSensorId());

         return result;
      }

      @Override
      public String toString() {

         return UI.EMPTY_STRING

               + "SensorItem" + NL //                                                  //$NON-NLS-1$

               + "[" + NL //                                                           //$NON-NLS-1$

               + "sensor                     = " + sensor + NL //                      //$NON-NLS-1$
               + "usedFirstTime              = " + usedFirstTime + NL //               //$NON-NLS-1$
               + "usedLastTime               = " + usedLastTime + NL //                //$NON-NLS-1$
               + "isBatteryLevelAvailable    = " + isBatteryLevelAvailable + NL //     //$NON-NLS-1$
               + "isBatteryStatusAvailable   = " + isBatteryStatusAvailable + NL //    //$NON-NLS-1$
               + "isBatteryVoltageAvailable  = " + isBatteryVoltageAvailable + NL //   //$NON-NLS-1$

               + "]" + NL //                                                           //$NON-NLS-1$
         ;
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

   private void addPrefListener() {

      _prefChangeListener = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ITourbookPreferences.VIEW_LAYOUT_CHANGED)) {

            _sensorViewer.getTable().setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

            _sensorViewer.refresh();

            /*
             * the tree must be redrawn because the styled text does not show with the new color
             */
            _sensorViewer.getTable().redraw();

         } else if (property.equals(ITourbookPreferences.APP_DATA_FILTER_IS_MODIFIED)) {

            // reselect current sensor that the sensor chart (when opened) is reloaded

            final StructuredSelection selection = getViewerSelection();

            _sensorViewer.setSelection(selection, true);

            final Table table = _sensorViewer.getTable();
            table.showSelection();
         }
      };

      _prefChangeListener_Common = propertyChangeEvent -> {

         final String property = propertyChangeEvent.getProperty();

         if (property.equals(ICommonPreferences.MEASUREMENT_SYSTEM)) {

            // measurement system has changed

            _columnManager.saveState(_state);
            _columnManager.clearColumns();

            defineAllColumns();

            _sensorViewer = (TableViewer) recreateViewer(_sensorViewer);
         }
      };

      _prefStore.addPropertyChangeListener(_prefChangeListener);
      _prefStore_Common.addPropertyChangeListener(_prefChangeListener_Common);
   }

   /**
    * Listen for events when a selection is fired
    */
   private void addSelectionListener() {

      _postSelectionListener = (part, selection) -> {

         if (part == SensorView.this) {
            return;
         }

         onSelectionChanged(selection);
      };

      getSite().getPage().addPostSelectionListener(_postSelectionListener);
   }

   private void addTourEventListener() {

      _tourPropertyListener = (part, eventId, eventData) -> {

         if (part == SensorView.this) {
            return;
         }

         if (eventId == TourEventId.UPDATE_UI
               || eventId == TourEventId.ALL_TOURS_ARE_MODIFIED

         // this event is fired when tours are imported, this could change the sensor values
               || eventId == TourEventId.CLEAR_DISPLAYED_TOUR

         ) {

            // new tours could be imported with new sensors

            reloadViewer();
         }
      };

      TourManager.getInstance().addTourEventListener(_tourPropertyListener);
   }

   private void createActions() {

      _action_DeleteSensor = new Action_DeleteSensor();
      _action_EditSensor = new Action_EditSensor();
      _action_OpenSensorChartView = new Action_OpenSensorChart();
   }

   private void createMenuManager() {

      _viewerMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
      _viewerMenuManager.setRemoveAllWhenShown(true);
      _viewerMenuManager.addMenuListener(menuManager -> fillContextMenu(menuManager));
   }

   @Override
   public void createPartControl(final Composite parent) {

      initUI(parent);
      createMenuManager();

      restoreState_BeforeUI();

      // define all columns for the viewer
      _columnManager = new ColumnManager(this, _state);
      defineAllColumns();

      createUI(parent);

      addPrefListener();
      addTourEventListener();
      addSelectionListener();

      createActions();
      fillToolbar();

      BusyIndicator.showWhile(parent.getDisplay(), () -> {

         loadAllSensors();

         updateUI_SetViewerInput();

         restoreState_WithUI();
      });
   }

   private void createUI(final Composite parent) {

      _viewerContainer = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().applyTo(_viewerContainer);
      {
         createUI_10_SensorViewer(_viewerContainer);
      }
   }

   private void createUI_10_SensorViewer(final Composite parent) {

      /*
       * Create table
       */
      final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

      table.setHeaderVisible(true);
      table.setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

      table.addKeyListener(keyPressedAdapter(keyEvent -> {

         if (keyEvent.keyCode == SWT.DEL) {
            onAction_DeleteSensor();
         }
      }));

      /*
       * Create table viewer
       */
      _sensorViewer = new TableViewer(table);

      _columnManager.createColumns(_sensorViewer);

      _sensorViewer.setUseHashlookup(true);
      _sensorViewer.setContentProvider(new SensorContentProvider());
      _sensorViewer.setComparator(_sensorComparator);

      _sensorViewer.addSelectionChangedListener(selectionChangedEvent -> onSelectSensor());
      _sensorViewer.addDoubleClickListener(doubleClickEvent -> onAction_OpenSensorChart());

      updateUI_SetSortDirection(
            _sensorComparator.__sortColumnId,
            _sensorComparator.__sortDirection);

      createUI_20_ContextMenu();
   }

   /**
    * create the views context menu
    */
   private void createUI_20_ContextMenu() {

      _tableContextMenu = createUI_22_CreateViewerContextMenu();

      final Table table = (Table) _sensorViewer.getControl();

      _columnManager.createHeaderContextMenu(table, _tableViewerContextMenuProvider);
   }

   private Menu createUI_22_CreateViewerContextMenu() {

      final Table table = (Table) _sensorViewer.getControl();
      final Menu tableContextMenu = _viewerMenuManager.createContextMenu(table);

      return tableContextMenu;
   }

   private void defineAllColumns() {

      defineColumn_Sensor_Name();
      defineColumn_Sensor_Type();
      defineColumn_NumberOfTours();
      defineColumn_DeviceType();
      defineColumn_BatteryState_Level();
      defineColumn_BatteryState_Status();
      defineColumn_BatteryState_Voltage();
      defineColumn_Sensor_Description();
      defineColumn_Manufacturer_Name();
      defineColumn_Manufacturer_Number();
      defineColumn_Product_Name();
      defineColumn_Product_Number();
      defineColumn_SerialNumber();
      defineColumn_Time_FirstUsed();
      defineColumn_Time_LastUsed();
      defineColumn_NameKey();
   }

   /**
    * Column: Battery state: 0...100 %
    */
   private void defineColumn_BatteryState_Level() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_STATE_BATTERY_LEVEL.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.isBatteryLevelAvailable
                  ? UI.SYMBOL_BOX
                  : UI.EMPTY_STRING);
         }
      });
   }

   /**
    * Column: Battery state: OK, Low, ...
    */
   private void defineColumn_BatteryState_Status() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_STATE_BATTERY_STATUS.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.isBatteryStatusAvailable
                  ? UI.SYMBOL_BOX
                  : UI.EMPTY_STRING);
         }
      });
   }

   /**
    * Column: Battery voltage state: 3.x Volt
    */
   private void defineColumn_BatteryState_Voltage() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_STATE_BATTERY_VOLTAGE.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.isBatteryVoltageAvailable
                  ? UI.SYMBOL_BOX
                  : UI.EMPTY_STRING);
         }
      });
   }

   /**
    * Column: Fit data: device type
    */
   private void defineColumn_DeviceType() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_DEVICE_TYPE.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(Short.toString(sensorItem.sensor.getDeviceType()));
         }
      });
   }

   /**
    * Column: Manufacturer name
    */
   private void defineColumn_Manufacturer_Name() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_MANUFACTURER_NAME.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.sensor.getManufacturerName());
         }
      });
   }

   /**
    * Column: Manufacturer number
    */
   private void defineColumn_Manufacturer_Number() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_MANUFACTURER_NUMBER.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(Integer.toString(sensorItem.sensor.getManufacturerNumber()));
         }
      });
   }

   /**
    * Column: Name key
    */
   private void defineColumn_NameKey() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_NAME_KEY.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.sensor.getSensorKey_WithDevType());
         }
      });
   }

   /**
    * Column: Number of tours
    */
   private void defineColumn_NumberOfTours() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_NUMBER_OF_TOURS.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(Integer.toString(sensorItem.numTours));
         }
      });
   }

   /**
    * Column: Product name
    */
   private void defineColumn_Product_Name() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_PRODUCT_NAME.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.sensor.getProductName());
         }
      });
   }

   /**
    * Column: Product number
    */
   private void defineColumn_Product_Number() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_PRODUCT_NUMBER.createColumn(_columnManager, _pc);

      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(Integer.toString(sensorItem.sensor.getProductNumber()));
         }
      });
   }

   /**
    * Column: Sensor description
    */
   private void defineColumn_Sensor_Description() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_DESCRIPTION.createColumn(_columnManager, _pc);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.sensor.getDescription());
         }
      });
   }

   /**
    * Column: Sensor name
    */
   private void defineColumn_Sensor_Name() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_NAME.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.sensor.getSensorName());
         }
      });
   }

   /**
    * Column: Sensor type
    */
   private void defineColumn_Sensor_Type() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_TYPE.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            final DeviceSensorType sensorType = sensorItem.sensor.getSensorType();

            cell.setText(sensorType == null
                  ? UI.EMPTY_STRING
                  : SensorManager.getSensorTypeName(sensorType));
         }
      });
   }

   /**
    * Column: Serial number
    */
   private void defineColumn_SerialNumber() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_SERIAL_NUMBER.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(sensorItem.sensor.getSerialNumber());
         }
      });
   }

   /**
    * Column: Used start time
    */
   private void defineColumn_Time_FirstUsed() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_TIME_FIRST_USED.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(TimeTools.Formatter_Date_S.format(TimeTools.toLocalDateTime(sensorItem.usedFirstTime)));
         }
      });
   }

   /**
    * Column: Used end time
    */
   private void defineColumn_Time_LastUsed() {

      final ColumnDefinition colDef = TableColumnFactory.SENSOR_TIME_LAST_USED.createColumn(_columnManager, _pc);

      colDef.setIsDefaultColumn();
      colDef.setColumnSelectionListener(_columnSortListener);

      colDef.setLabelProvider(new CellLabelProvider() {
         @Override
         public void update(final ViewerCell cell) {

            final SensorItem sensorItem = (SensorItem) cell.getElement();
            cell.setText(TimeTools.Formatter_Date_S.format(TimeTools.toLocalDateTime(sensorItem.usedLastTime)));
         }
      });
   }

   @Override
   public void dispose() {

      _prefStore.removePropertyChangeListener(_prefChangeListener);
      _prefStore_Common.removePropertyChangeListener(_prefChangeListener_Common);

      TourManager.getInstance().removeTourEventListener(_tourPropertyListener);

      getSite().getPage().removePostSelectionListener(_postSelectionListener);

      super.dispose();
   }

   private void enableActions() {

      final SensorItem selectedSensorItem = getSelectedSensorItem();
      final boolean isSensorSelected = selectedSensorItem != null;

      _action_OpenSensorChartView.setEnabled(isSensorSelected);
      _action_DeleteSensor.setEnabled(isSensorSelected);
      _action_EditSensor.setEnabled(isSensorSelected);
   }

   private void fillContextMenu(final IMenuManager menuMgr) {

      /*
       * Fill menu
       */

      menuMgr.add(_action_EditSensor);
      menuMgr.add(_action_OpenSensorChartView);
      menuMgr.add(_action_DeleteSensor);

      enableActions();
   }

   private void fillToolbar() {

//      final IActionBars actionBars = getViewSite().getActionBars();

      /*
       * Fill view menu
       */
//      final IMenuManager menuMgr = actionBars.getMenuManager();

      /*
       * Fill view toolbar
       */
//      final IToolBarManager tbm = actionBars.getToolBarManager();

   }

   @Override
   public ColumnManager getColumnManager() {
      return _columnManager;
   }

   private int getNumberOfTours(final DeviceSensor selectedSensor) {

      String sql = null;
      int numberOfTours = 0;

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         sql = UI.EMPTY_STRING

               // get number of tours
               + " SELECT COUNT(*)" + NL //                                         //$NON-NLS-1$
               + " FROM" + NL //                                                    //$NON-NLS-1$

               // get all device values which contain the selected device
               + " (" + NL //                                                       //$NON-NLS-1$

               + " SELECT " + NL //                                                 //$NON-NLS-1$
               + "  DISTINCT TOURDATA_TourID," + NL //                              //$NON-NLS-1$
               + "  DEVICESENSOR_SensorID" + NL //                                  //$NON-NLS-1$

               + "  FROM " + TourDatabase.TABLE_DEVICE_SENSOR_VALUE + NL //         //$NON-NLS-1$
               + "  WHERE DEVICESENSOR_SensorID = ?" + NL //                        //$NON-NLS-1$

               + " ) TourId" + NL //                                                //$NON-NLS-1$
         ;

         final PreparedStatement stmt = conn.prepareStatement(sql);

         stmt.setLong(1, selectedSensor.getSensorId());

         final ResultSet result = stmt.executeQuery();

         // get first result
         result.next();

         // get first value
         numberOfTours = result.getInt(1);

      } catch (final SQLException e) {
         SQL.showException(e, sql);
      }

      return numberOfTours;
   }

   private SensorItem getSelectedSensorItem() {

      final IStructuredSelection selection = _sensorViewer.getStructuredSelection();
      final Object firstElement = selection.getFirstElement();

      if (firstElement != null) {

         return ((SensorItem) firstElement);
      }

      return null;
   }

   /**
    * @param sortColumnId
    *
    * @return Returns the column widget by it's column id, when column id is not found then the
    *         first column is returned.
    */
   private TableColumn getSortColumn(final String sortColumnId) {

      final TableColumn[] allColumns = _sensorViewer.getTable().getColumns();

      for (final TableColumn column : allColumns) {

         final String columnId = ((ColumnDefinition) column.getData()).getColumnId();

         if (columnId.equals(sortColumnId)) {
            return column;
         }
      }

      return allColumns[0];
   }

   @Override
   public ColumnViewer getViewer() {
      return _sensorViewer;
   }

   private StructuredSelection getViewerSelection() {

      return (StructuredSelection) _sensorViewer.getSelection();
   }

   private void initUI(final Composite parent) {

      _pc = new PixelConverter(parent);

      _columnSortListener = new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            onColumn_Select(e);
         }
      };
   }

   private void loadAllSensors() {

      /*
       * Create sensor items for all sensors
       */
      final Map<Long, DeviceSensor> allDbDeviceSensors = TourDatabase.getAllDeviceSensors_BySensorID();
      final HashMap<Long, SensorItem> allSensorItems = new HashMap<>();

      for (final DeviceSensor sensor : allDbDeviceSensors.values()) {

         final SensorItem sensorItem = new SensorItem();
         sensorItem.sensor = sensor;

         allSensorItems.put(sensor.getSensorId(), sensorItem);
      }

      PreparedStatement statementMinMax = null;
      ResultSet resultMinMax = null;

      String sql;

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         /*
          * Set used start/end time
          */
         sql = UI.EMPTY_STRING

               + "SELECT" + NL //                                                   //$NON-NLS-1$

               + "   DEVICESENSOR_SensorID         ," + NL //                    1  //$NON-NLS-1$

               + "   Min(TourStartTime)            ," + NL //                    2  //$NON-NLS-1$
               + "   Max(TourStartTime)            ," + NL //                    3  //$NON-NLS-1$

               + "   Max(BatteryLevel_Start)       ," + NL //                    4  //$NON-NLS-1$
               + "   Max(BatteryLevel_End)         ," + NL //                    5  //$NON-NLS-1$
               + "   Max(BatteryStatus_Start)      ," + NL //                    6  //$NON-NLS-1$
               + "   Max(BatteryStatus_End)        ," + NL //                    7  //$NON-NLS-1$
               + "   Max(BatteryVoltage_Start)     ," + NL //                    8  //$NON-NLS-1$
               + "   Max(BatteryVoltage_End)       ," + NL //                    9  //$NON-NLS-1$

               + "   COUNT(DEVICESENSOR_SensorID)  " + NL //                     10 //$NON-NLS-1$

               + "FROM " + TourDatabase.TABLE_DEVICE_SENSOR_VALUE + NL //           //$NON-NLS-1$
               + "GROUP BY DEVICESENSOR_SensorID" + NL //                           //$NON-NLS-1$
         ;

         statementMinMax = conn.prepareStatement(sql);
         resultMinMax = statementMinMax.executeQuery();

         while (resultMinMax.next()) {

            final long sensorId = resultMinMax.getLong(1);

            final SensorItem sensorItem = allSensorItems.get(sensorId);
            if (sensorItem == null) {

               // this should not happen

            } else {

// SET_FORMATTING_OFF

               final long dbUsedFirstTime       = resultMinMax.getLong(2);
               final long dbUsedLastTime        = resultMinMax.getLong(3);

               final float dbMaxLevel_Start     = resultMinMax.getFloat(4);
               final float dbMaxLevel_End       = resultMinMax.getFloat(5);
               final float dbMaxStatus_Start    = resultMinMax.getFloat(6);
               final float dbMaxStatus_End      = resultMinMax.getFloat(6);
               final float dbMaxVoltage_Start   = resultMinMax.getFloat(8);
               final float dbMaxVoltage_End     = resultMinMax.getFloat(9);

               final int dbNumSensorValues      = resultMinMax.getInt(10);

               sensorItem.usedFirstTime               = dbUsedFirstTime;
               sensorItem.usedLastTime                = dbUsedLastTime;

               sensorItem.isBatteryLevelAvailable     = dbMaxLevel_Start > 0 || dbMaxLevel_End > 0;
               sensorItem.isBatteryStatusAvailable    = dbMaxStatus_Start > 0 || dbMaxStatus_End > 0;
               sensorItem.isBatteryVoltageAvailable   = dbMaxVoltage_Start > 0 || dbMaxVoltage_End > 0;

               sensorItem.numTours                    = dbNumSensorValues;

// SET_FORMATTING_ON
            }
         }

         _allSensorItems.clear();
         _allSensorItems.addAll(allSensorItems.values());

      } catch (final SQLException e) {

         SQL.showException(e);

      } finally {

         SQL.close(statementMinMax);
         SQL.close(resultMinMax);
      }
   }

   private void onAction_DeleteSensor() {

      final SensorItem selectedSensorItem = getSelectedSensorItem();
      final DeviceSensor selectedSensor = selectedSensorItem.sensor;
      final int numSensorTours = getNumberOfTours(selectedSensor);

      if (numSensorTours > 0) {

         // only sensors with 0 tours can be deleted

         MessageDialog.openInformation(_viewerContainer.getShell(),

               Messages.Sensor_View_Dialog_DeleteSensor_Title,

               NLS.bind(Messages.Sensor_View_Dialog_CannotDeleteSensor_Message,
                     selectedSensor.getLabel(),
                     numSensorTours));

         return;
      }

      final int returnCode = new MessageDialog(
            _viewerContainer.getShell(),
            Messages.Sensor_View_Dialog_DeleteSensor_Title,

            null, // image

            NLS.bind(Messages.Sensor_View_Dialog_DeleteSensor_Message, selectedSensor.getLabel()),
            MessageDialog.QUESTION,

            1, // default index

            Messages.App_Action_Delete,
            IDialogConstants.CANCEL_LABEL

      ).open();

      if (returnCode != Window.OK) {
         return;
      }

      /*
       * Delete sensor
       */

      final String sql = "DELETE FROM " + TourDatabase.TABLE_DEVICE_SENSOR + " WHERE sensorId=?"; //$NON-NLS-1$ //$NON-NLS-2$

      try (Connection conn = TourDatabase.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql); //
      ) {

         stmt.setLong(1, selectedSensor.getSensorId());
         stmt.execute();

      } catch (final SQLException e) {

         SQL.showException(e, sql);
      }

      /*
       * Update UI
       */

      final Table table = _sensorViewer.getTable();

      // get index for selected sensor
      final int lastSensorIndex = table.getSelectionIndex();

      reloadSensorViewer();

      // get next sensor
      SensorItem nextSensorItem = (SensorItem) _sensorViewer.getElementAt(lastSensorIndex);

      if (nextSensorItem == null) {
         nextSensorItem = (SensorItem) _sensorViewer.getElementAt(lastSensorIndex - 1);
      }

      // select next sensor
      if (nextSensorItem != null) {
         _sensorViewer.setSelection(new StructuredSelection(nextSensorItem), true);
      }

      table.setFocus();
   }

   private void onAction_EditSensor() {

      final Object firstElement = _sensorViewer.getStructuredSelection().getFirstElement();

      if (firstElement != null) {

         final SensorItem sensorItem = (SensorItem) firstElement;

         final long sensorId = sensorItem.sensor.getSensorId();
         final DeviceSensor sensor = TourDatabase.getAllDeviceSensors_BySensorID().get(sensorId);

         if (new DialogSensor(_viewerContainer.getShell(), sensor).open() != Window.OK) {

            _sensorViewer.getTable().setFocus();

            return;
         }

         // update model
         final DeviceSensor savedSensor = TourDatabase.saveEntity(sensor, sensorId, DeviceSensor.class);
         sensorItem.sensor = savedSensor;

         // update UI - resort and preserve selection
         _viewerContainer.setRedraw(false);
         {
            // keep selection
            final ISelection selectionBackup = getViewerSelection();
            {
               _sensorViewer.refresh();
            }
            updateUI_SelectSensor(selectionBackup);
         }
         _viewerContainer.setRedraw(true);

         /*
          * When a sensor is modified, all tours with sensor values do contain the old sensor
          * instance
          */
         TourManager.getInstance().clearTourDataCache();
         TourManager.fireEvent(TourEventId.ALL_TOURS_ARE_MODIFIED, this);
      }
   }

   private void onAction_OpenSensorChart() {

      Util.showView(SensorChartView.ID, true);

      // reselect current sensor to update the sensor chart
      final IStructuredSelection structuredSelection = _sensorViewer.getStructuredSelection();
      if (structuredSelection.getFirstElement() != null) {
         _sensorViewer.setSelection(structuredSelection);
      }
   }

   private void onColumn_Select(final SelectionEvent e) {

      _viewerContainer.setRedraw(false);
      {
         // keep selection
         final ISelection selectionBackup = getViewerSelection();
         {
            // update viewer with new sorting
            _sensorComparator.setSortColumn(e.widget);
            _sensorViewer.refresh();
         }
         updateUI_SelectSensor(selectionBackup);
      }
      _viewerContainer.setRedraw(true);
   }

   private void onSelectionChanged(final ISelection selection) {

      if (selection instanceof SelectionDeletedTours) {

         reloadSensorViewer();
      }
   }

   private void onSelectSensor() {

      if (_isInUIUpdate) {
         return;
      }

      final IStructuredSelection selection = _sensorViewer.getStructuredSelection();
      final Object firstElement = selection.getFirstElement();

      if (firstElement == null) {
         return;
      }

      final DeviceSensor selectedSensor = ((SensorItem) firstElement).sensor;

      // this view could be inactive -> selection is not fired with the SelectionProvider interface
      TourManager.fireEventWithCustomData(
            TourEventId.SELECTION_SENSOR,
            new SelectionSensor(selectedSensor, null),
            this);
   }

   @Override
   public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

      _viewerContainer.setRedraw(false);
      {
         // keep selection
         final ISelection selectionBackup = getViewerSelection();
         {
            _sensorViewer.getTable().dispose();

            createUI_10_SensorViewer(_viewerContainer);

            // update UI
            _viewerContainer.layout();

            // update the viewer
            updateUI_SetViewerInput();
         }
         updateUI_SelectSensor(selectionBackup);
      }
      _viewerContainer.setRedraw(true);

      _sensorViewer.getTable().setFocus();

      return _sensorViewer;
   }

   private void reloadSensorViewer() {

      // update model
      TourDatabase.clearDeviceSensors();
      loadAllSensors();

      // update the viewer
      updateUI_SetViewerInput();
   }

   @Override
   public void reloadViewer() {

      loadAllSensors();

      _viewerContainer.setRedraw(false);
      {
         // keep selection
         final ISelection selectionBackup = getViewerSelection();
         {
            updateUI_SetViewerInput();
         }
         updateUI_SelectSensor(selectionBackup);
      }
      _viewerContainer.setRedraw(true);
   }

   private void restoreState_BeforeUI() {

      // sorting
      final String sortColumnId = Util.getStateString(_state, STATE_SORT_COLUMN_ID, TableColumnFactory.SENSOR_NAME_ID);
      final int sortDirection = Util.getStateInt(_state, STATE_SORT_COLUMN_DIRECTION, SensorComparator.ASCENDING);

      // update comparator
      _sensorComparator.__sortColumnId = sortColumnId;
      _sensorComparator.__sortDirection = sortDirection;
   }

   private void restoreState_WithUI() {

      /*
       * Restore selected sensor
       */
      final String[] allViewerIndices = _state.getArray(STATE_SELECTED_SENSOR_INDICES);

      if (allViewerIndices != null) {

         final ArrayList<Object> allSensors = new ArrayList<>();

         for (final String viewerIndex : allViewerIndices) {

            Object sensor = null;

            try {
               final int index = Integer.parseInt(viewerIndex);
               sensor = _sensorViewer.getElementAt(index);
            } catch (final NumberFormatException e) {
               // just ignore
            }

            if (sensor != null) {
               allSensors.add(sensor);
            }
         }

         if (allSensors.size() > 0) {

            _viewerContainer.getDisplay().timerExec(

                  /*
                   * When this value is too small, then the chart axis could not be painted
                   * correctly with the dark theme during the app startup
                   */
                  1000,

                  () -> {

                     _sensorViewer.setSelection(new StructuredSelection(allSensors.toArray()), true);

                     enableActions();
                  });
         }
      }

      enableActions();
   }

   @PersistState
   private void saveState() {

      _columnManager.saveState(_state);

      _state.put(STATE_SORT_COLUMN_ID, _sensorComparator.__sortColumnId);
      _state.put(STATE_SORT_COLUMN_DIRECTION, _sensorComparator.__sortDirection);

      // keep selected tours
      Util.setState(_state, STATE_SELECTED_SENSOR_INDICES, _sensorViewer.getTable().getSelectionIndices());
   }

   @Override
   public void setFocus() {
      _sensorViewer.getTable().setFocus();
   }

   @Override
   public void updateColumnHeader(final ColumnDefinition colDef) {}

   /**
    * Select and reveal tour marker item.
    *
    * @param selection
    * @param checkedElements
    */
   private void updateUI_SelectSensor(final ISelection selection) {

      _isInUIUpdate = true;
      {
         _sensorViewer.setSelection(selection, true);

         final Table table = _sensorViewer.getTable();
         table.showSelection();
      }
      _isInUIUpdate = false;
   }

   /**
    * Set the sort column direction indicator for a column.
    *
    * @param sortColumnId
    * @param isAscendingSort
    */
   private void updateUI_SetSortDirection(final String sortColumnId, final int sortDirection) {

      final Table table = _sensorViewer.getTable();
      final TableColumn tc = getSortColumn(sortColumnId);

      table.setSortColumn(tc);
      table.setSortDirection(sortDirection == SensorComparator.ASCENDING ? SWT.UP : SWT.DOWN);
   }

   private void updateUI_SetViewerInput() {

      _isInUIUpdate = true;
      {
         _sensorViewer.setInput(new Object[0]);
      }
      _isInUIUpdate = false;
   }
}
