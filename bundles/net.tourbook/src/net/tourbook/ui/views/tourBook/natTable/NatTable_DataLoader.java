/*******************************************************************************
 * Copyright (C) 2020, 2024 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourBook.natTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.util.ColumnDefinition;
import net.tourbook.common.util.ColumnManager;
import net.tourbook.common.util.SQL;
import net.tourbook.common.util.SQLData;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.database.TourDatabase;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tag.tour.filter.TourTagFilterManager;
import net.tourbook.tag.tour.filter.TourTagFilterSqlJoinBuilder;
import net.tourbook.ui.SQLFilter;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.views.tourBook.LazyTourLoaderItem;
import net.tourbook.ui.views.tourBook.TVITourBookItem;
import net.tourbook.ui.views.tourBook.TVITourBookTour;
import net.tourbook.ui.views.tourBook.TourBookView;
import net.tourbook.ui.views.tourBook.TourBookView.TourCollectionFilter;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.swt.widgets.Display;

public class NatTable_DataLoader {

   private static final char                              NL                             = net.tourbook.common.UI.NEW_LINE;

   private static final IPreferenceStore                  _prefStore                     = TourbookPlugin.getPrefStore();

   private static final String                            SQL_ASCENDING                  = "ASC";                           //$NON-NLS-1$
   private static final String                            SQL_DESCENDING                 = "DESC";                          //$NON-NLS-1$

   private static final String                            SQL_DEFAULT_SORT_FIELD         = "TourStartTime";                 //$NON-NLS-1$

   /**
    * Dummy field name for fields which currently cannot be sorted in the NatTable.
    */
   public static final String                             FIELD_WITHOUT_SORTING          = "FIELD_WITHOUT_SORTING";         //$NON-NLS-1$

   private static int                                     FETCH_SIZE                     = 1_000;

   private static final SQLData                           EMPTY_SQL_DATA                 = new SQLData();

   private static final ExecutorService                   _loadingExecutor               = createExecuter_TourLoading();
   private static final ExecutorService                   _rowIndexExecutor              = createExecuter_TourId_RowIndex();

   private ConcurrentHashMap<Integer, Integer>            _pageNumbers_Fetched           = new ConcurrentHashMap<>();
   private ConcurrentHashMap<Integer, LazyTourLoaderItem> _pageNumbers_Loading           = new ConcurrentHashMap<>();

   /**
    * Relation between row position and tour item
    * <p>
    * Key: Row position<br>
    * Value: {@link TVITourBookTour}
    */
   private ConcurrentHashMap<Integer, TVITourBookTour>    _fetchedTourItems              = new ConcurrentHashMap<>();

   /**
    * Relation between tour id and row position
    * <p>
    * Key: Tour ID <br>
    * Value: Row position
    */
   private ConcurrentHashMap<Long, Integer>               _fetchedTourIndex              = new ConcurrentHashMap<>();
   private final LinkedBlockingDeque<LazyTourLoaderItem>  _loaderWaitingQueue            = new LinkedBlockingDeque<>();

   private String[]                                       _allSortColumnIds;
   private List<SortDirectionEnum>                        _allSortDirections;

   private ArrayList<String>                              _allSqlSortFields_OrderBy      = new ArrayList<>();
   private ArrayList<String>                              _allSqlSortFields_SelectFields = new ArrayList<>();
   private ArrayList<String>                              _allSqlSortDirections          = new ArrayList<>();

   /**
    * Contains all columns (also hidden columns), sorted in the order how they are displayed in the
    * UI.
    */
   public List<ColumnDefinition>                          allSortedColumns;

   /**
    * Number of all tours for the current lazy loader
    */
   private int                                            _numAllTourItems               = -1;
   private int                                            _numToursWithoutCollectionFilter;

   private TourBookView                                   _tourBookView;

   private ColumnManager                                  _columnManager;

   /**
    * Contains all tour id's for the current tour filter and tour sorting, this is used
    * to get the row index for a tour.
    */
   private long[]                                         _allSortedTourIds;

   private SQLData                                        _tourCollectionFilter          = EMPTY_SQL_DATA;

   public NatTable_DataLoader(final TourBookView tourBookView, final ColumnManager columnManager) {

      _tourBookView = tourBookView;
      _columnManager = columnManager;

      createColumnHeaderData();

      TourDatabase.getInstance().setupDerbyCustomFunctions_AvgSpeedPace();
   }

   private static ExecutorService createExecuter_TourId_RowIndex() {

      final ThreadFactory threadFactory = runnable -> {

         final Thread thread = new Thread(runnable, "NatTable_DataLoader: Loading row indices");//$NON-NLS-1$

         thread.setPriority(Thread.MIN_PRIORITY);
         thread.setDaemon(true);

         return thread;
      };

      return Executors.newSingleThreadExecutor(threadFactory);
   }

   private static ExecutorService createExecuter_TourLoading() {

      final ThreadFactory threadFactory = runnable -> {

         final Thread thread = new Thread(runnable, "NatTable_DataLoader: Loading tours");//$NON-NLS-1$

         thread.setPriority(Thread.MIN_PRIORITY);
         thread.setDaemon(true);

         return thread;
      };

// !!! newCachedThreadPool is not working, part of the view is not updated !!!
//
//      final ExecutorService loadingExecutor = Executors.newCachedThreadPool(threadFactory);
//
//      final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) loadingExecutor;
//
//      threadPoolExecutor.setKeepAliveTime(1, TimeUnit.SECONDS);
//      threadPoolExecutor.setMaximumPoolSize(Runtime.getRuntime().availableProcessors());
//
//      return loadingExecutor;

      return Executors.newSingleThreadExecutor(threadFactory);
   }

   private void createColumnHeaderData() {

      allSortedColumns = _columnManager.getVisibleAndSortedColumns();
   }

   private int[] createRowIndicesFromTourIds(final List<Long> allRequestedTourIds) {

      final int numRequestedTourIds = allRequestedTourIds.size();
      if (numRequestedTourIds == 0) {

         // nothing more to do

         return new int[0];
      }

      final IntArrayList allRowIndices = new IntArrayList();
      final int numAllAvailableTourIds = _allSortedTourIds.length;

      // loop: all requested tour id's
      for (final Long requestedTourId : allRequestedTourIds) {

         // loop: all available tour id's
         for (int rowPosition = 0; rowPosition < numAllAvailableTourIds; rowPosition++) {

            final long loadedTourId = _allSortedTourIds[rowPosition];

            if (loadedTourId == requestedTourId) {

               allRowIndices.add(rowPosition);

               // keep relation also for already fetched tour id's
               _fetchedTourIndex.put(requestedTourId, rowPosition);

               break;
            }
         }
      }

      return allRowIndices.toArray();
   }

   private String createSql_Sorting_OrderBy() {

      final int numOrderFields = _allSqlSortFields_OrderBy.size();

      if (numOrderFields == 0) {
         return UI.EMPTY_STRING;
      }

      final StringBuilder sb = new StringBuilder();

      sb.append(" ORDER BY ");//$NON-NLS-1$

      for (int fieldIndex = 0; fieldIndex < numOrderFields; fieldIndex++) {

         final String fieldName = _allSqlSortFields_OrderBy.get(fieldIndex);
         final String sortDirection = _allSqlSortDirections.get(fieldIndex);

         if (fieldIndex > 0) {

            // separate from previous order item
            sb.append(UI.COMMA_SPACE + NL);
         }

         sb.append(fieldName + UI.SPACE + sortDirection);
      }

      return sb.toString();
   }

   private String createSql_Sorting_SelectFields() {

      final int numSelectFields = _allSqlSortFields_SelectFields.size();

      if (numSelectFields == 0) {
         return UI.EMPTY_STRING;
      }

      final StringBuilder sb = new StringBuilder();

      for (int fieldIndex = 0; fieldIndex < numSelectFields; fieldIndex++) {

         final String fieldName = _allSqlSortFields_SelectFields.get(fieldIndex);

         if (fieldIndex > 0) {

            // separate from previous field
            sb.append(UI.COMMA_SPACE + NL);
         }

         sb.append(fieldName);
      }

      return sb.toString();
   }

   /**
    * Loads all tour id's for the current sort and tour filter
    *
    * @return
    */
   private void fetchAllTourIds() {

      String sql = null;
      final LongArrayList allTourIds = new LongArrayList();

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         PreparedStatement prepStmt;

         final String sqlSelectFields_Raw = createSql_Sorting_SelectFields();

         if (TourTagFilterManager.isNoTagsFilter_Or_CombineTagsWithOr()) {

            // tags are combined with OR

            final SQLFilter sqlFilter = new SQLFilter(SQLFilter.ANY_APP_FILTERS);

            String sqlSelectFields_1 = sqlSelectFields_Raw;
            if (sqlSelectFields_1.length() > 0) {
               sqlSelectFields_1 += UI.COMMA_SPACE + NL;
            }

            sql = NL

                  + " SELECT DISTINCT TourId," + NL //                                                      //$NON-NLS-1$
                  + "   " + sqlSelectFields_1 //                                                            //$NON-NLS-1$
                  + "   jTdataTtag.TourTag_tagId" + NL //                                                   //$NON-NLS-1$

                  + " FROM " + TourDatabase.TABLE_TOUR_DATA + NL //                                         //$NON-NLS-1$

                  // get tag id's, this is necessary that the tour filter works
                  + " LEFT JOIN " + TourDatabase.JOINTABLE__TOURDATA__TOURTAG + " AS jTdataTtag" + NL //    //$NON-NLS-1$ //$NON-NLS-2$
                  + " ON TourData.tourId = jTdataTtag.TourData_tourId" + NL //                              //$NON-NLS-1$

                  + " WHERE 1=1" + NL //                                                                    //$NON-NLS-1$
                  + "   " + sqlFilter.getWhereClause() + NL //                                              //$NON-NLS-1$
                  + "   " + _tourCollectionFilter.getSqlString() + NL //                                    //$NON-NLS-1$

                  + createSql_Sorting_OrderBy() + NL;

            prepStmt = conn.prepareStatement(sql);

            // set sql parameters
            sqlFilter.setParameters(prepStmt, 1);
            _tourCollectionFilter.setParameters(prepStmt, sqlFilter.getLastParameterIndex());

         } else {

            // tags are combined with AND

            final SQLFilter sqlFilter = new SQLFilter();
            final SQLData sqlCombineTagsWithAnd = TourTagFilterSqlJoinBuilder.createSql_CombineTagsWithAnd();

            String sqlSelectFields_2 = sqlSelectFields_Raw;
            if (sqlSelectFields_2.length() > 0) {
               sqlSelectFields_2 = UI.COMMA_SPACE + sqlSelectFields_2 + NL;
            }

            sql = NL

                  + " SELECT DISTINCT TourId" + NL //                                                    //$NON-NLS-1$
                  + "   " + sqlSelectFields_2 //                                                        //$NON-NLS-1$

                  + " FROM" + NL //                                                                      //$NON-NLS-1$
                  + " ( SELECT" + NL //                                                                  //$NON-NLS-1$
                  + "      TourId," + NL //                                                              //$NON-NLS-1$
                  + "      jTdataTtag.TourTag_tagId" + NL //                                             //$NON-NLS-1$
                  + "      " + sqlSelectFields_2 //                                                     //$NON-NLS-1$
                  + "   FROM " + TourDatabase.TABLE_TOUR_DATA + NL //                                    //$NON-NLS-1$
                  + "   " + sqlCombineTagsWithAnd.getSqlString() //                                      //$NON-NLS-1$
                  + "   AS jTdataTtag" + NL //                                                           //$NON-NLS-1$
                  + "   ON TourData.tourId = jTdataTtag.TourData_tourId" + NL //                         //$NON-NLS-1$

                  + "   WHERE 1=1" + NL //                                                               //$NON-NLS-1$
                  + "   " + sqlFilter.getWhereClause() + NL //                                           //$NON-NLS-1$
                  + "   " + _tourCollectionFilter.getSqlString() + NL //                                 //$NON-NLS-1$

                  + " ) AS DerivedTable" + NL //                                                         //$NON-NLS-1$

                  + createSql_Sorting_OrderBy() + NL;

            prepStmt = conn.prepareStatement(sql);

// SET_FORMATTING_OFF

            // set sql parameters
            sqlCombineTagsWithAnd   .setParameters(prepStmt, 1);
            sqlFilter               .setParameters(prepStmt, sqlCombineTagsWithAnd.getLastParameterIndex());
            _tourCollectionFilter   .setParameters(prepStmt, sqlFilter.getLastParameterIndex());

// SET_FORMATTING_ON
         }

         int rowIndex = 0;
         long prevTourId = -1;

         final ResultSet result = prepStmt.executeQuery();
         while (result.next()) {

            final long tourId = result.getLong(1);

            if (tourId == prevTourId) {

               // skip duplicated tour id's

            } else {

               allTourIds.add(tourId);

               _fetchedTourIndex.put(tourId, rowIndex++);
            }

            prevTourId = tourId;
         }

      } catch (final SQLException e) {

         SQL.showException(e, sql);
      }

      _allSortedTourIds = allTourIds.toArray();
   }

   private int fetchNumberOfTours() {

//    TourDatabase.enableRuntimeStatistics(conn);

      int numTours = 0;

      String sql = null;
      String sqlCollectionFilter = _tourCollectionFilter.getSqlString();

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         for (int runCounter = 0; runCounter < 2; runCounter++) {

            // first run is using the collection filter
            // second run is without the collection filter

            final boolean isFirstRun = runCounter == 0;

            if (isFirstRun == false) {
               sqlCollectionFilter = UI.EMPTY_STRING;
            }

            PreparedStatement prepStmt;

            if (TourTagFilterManager.isNoTagsFilter_Or_CombineTagsWithOr()) {

               final SQLFilter sqlFilter = new SQLFilter(SQLFilter.ANY_APP_FILTERS);

               sql = NL

                     + "SELECT" + NL //                                                                        //$NON-NLS-1$
                     + "   COUNT(*)" + NL //                                                                   //$NON-NLS-1$
                     + "FROM" + NL //                                                                          //$NON-NLS-1$
                     + "(  SELECT DISTINCT TourId" + NL //                                                     //$NON-NLS-1$
                     + "   FROM" + NL //                                                                       //$NON-NLS-1$
                     + "   (  SELECT" + NL //                                                                  //$NON-NLS-1$
                     + "         TourId," + NL //                                                              //$NON-NLS-1$
                     + "         jTdataTtag.TourTag_tagId" + NL //                                             //$NON-NLS-1$

                     + "      FROM " + TourDatabase.TABLE_TOUR_DATA + NL //                                    //$NON-NLS-1$
                     + "      LEFT JOIN " + TourDatabase.JOINTABLE__TOURDATA__TOURTAG + " jTdataTtag" //       //$NON-NLS-1$ //$NON-NLS-2$
                     + "      ON TourData.tourId = jTdataTtag.TourData_tourId" + NL //                         //$NON-NLS-1$

                     + "      WHERE 1=1" + NL //                                                               //$NON-NLS-1$
                     + "      " + sqlFilter.getWhereClause() + NL //                                           //$NON-NLS-1$
                     + "      " + sqlCollectionFilter + NL //                                                  //$NON-NLS-1$

                     // VERY IMPORTANT: "AS <name>" MUST be set, otherwise it DO NOT work
                     + "   ) AS DerivedTable1" + NL //                                                         //$NON-NLS-1$
                     + ") AS DerivedTable2" + NL //                                                            //$NON-NLS-1$
               ;

               prepStmt = conn.prepareStatement(sql);

               // set filter parameters
               sqlFilter.setParameters(prepStmt, 1);
               if (isFirstRun) {
                  _tourCollectionFilter.setParameters(prepStmt, sqlFilter.getLastParameterIndex());
               }

            } else {

               final SQLFilter sqlFilter = new SQLFilter();
               final SQLData sqlCombineTagsWithAnd = TourTagFilterSqlJoinBuilder.createSql_CombineTagsWithAnd();

               sql = NL

                     + " SELECT" + NL //                                                                       //$NON-NLS-1$
                     + "    COUNT(*)" + NL //                                                                  //$NON-NLS-1$
                     + " FROM" + NL //                                                                         //$NON-NLS-1$
                     + " ( SELECT DISTINCT TourId" + NL //                                                     //$NON-NLS-1$
                     + "   FROM" + NL //                                                                       //$NON-NLS-1$
                     + "   (  SELECT" + NL //                                                                  //$NON-NLS-1$
                     + "         TourId," + NL //                                                              //$NON-NLS-1$
                     + "         jTdataTtag.TourTag_tagId" + NL //                                             //$NON-NLS-1$

                     + "      FROM TOURDATA" + NL //                                                           //$NON-NLS-1$
                     + sqlCombineTagsWithAnd.getSqlString()
                     + "      AS jTdataTtag" + NL //                                                           //$NON-NLS-1$
                     + "      ON TourData.tourId = jTdataTtag.TourData_tourId" + NL //                         //$NON-NLS-1$

                     + "      WHERE 1=1" + NL //                                                               //$NON-NLS-1$
                     + "         " + sqlFilter.getWhereClause() + NL //                                        //$NON-NLS-1$
                     + "         " + sqlCollectionFilter + NL //                                               //$NON-NLS-1$

                     + "   ) AS DerivedTable1" + NL //                                                         //$NON-NLS-1$
                     + " ) AS DerivedTable2" + NL //                                                           //$NON-NLS-1$
               ;

               prepStmt = conn.prepareStatement(sql);

// SET_FORMATTING_OFF

               // set sql parameters
               sqlCombineTagsWithAnd   .setParameters(prepStmt, 1);
               sqlFilter               .setParameters(prepStmt, sqlCombineTagsWithAnd.getLastParameterIndex());

               if (isFirstRun) {
                  _tourCollectionFilter   .setParameters(prepStmt, sqlFilter.getLastParameterIndex());
               }

// SET_FORMATTING_ON
            }

            final ResultSet result = prepStmt.executeQuery();

            // get first result
            result.next();

            if (isFirstRun) {

               // 1st run

               numTours = result.getInt(1);

               if (sqlCollectionFilter.length() == 0) {

                  /**
                   * The 2st run would be the same as the 1nd run -> skip 2nd run otherwise this
                   * warning occurs:
                   * <p>
                   * [main] INFO com.mchange.v2.c3p0.stmt.GooGooStatementCache -
                   * Multiply-cached PreparedStatement:
                   */

                  _numToursWithoutCollectionFilter = numTours;

                  break;
               }

            } else {

               // 2nd run

               _numToursWithoutCollectionFilter = result.getInt(1);
            }

         }

      } catch (final SQLException e) {
         SQL.showException(e, sql);
      }

//    TourDatabase.disableRuntimeStatistic(conn);

      return numTours;
   }

   private boolean fetchPagedTourItems(final LazyTourLoaderItem loaderItem) {

      final SQLFilter sqlAppFilter = new SQLFilter(SQLFilter.ANY_APP_FILTERS);

      /**
       * Using this syntax from
       * https://stackoverflow.com/questions/38770349/get-rows-on-first-table-not-on-left-joins-result-set#38770491
       * <code>
       * SELECT ST1.Col1, T2.Col1 FROM
       * (
       *     SELECT * FROM Table1
       *     ORDER BY Col1
       *     OFFSET @offset ROWS
       *     FETCH NEXT @page ROWS ONLY
       * ) ST1
       * JOIN Table2 T2 ON ST1.Id=T2.FkToT1
       * </code>
       */

      String sql = null;

      try (Connection conn = TourDatabase.getInstance().getConnection()) {

         int rowIndex = loaderItem.sqlOffset;

         final TourTagFilterSqlJoinBuilder tagFilterSqlJoinBuilder = new TourTagFilterSqlJoinBuilder();
         final String orderBy = createSql_Sorting_OrderBy();

         /*
          * This synchronize may fix an issue, that number of sql parameter were wrong because of
          * concurrency
          */
         synchronized (_tourCollectionFilter) {

            sql = NL

                  // get all markers/tags for paged tours
                  + " SELECT" //                                                                             //$NON-NLS-1$
                  + "    " + TVITourBookItem.SQL_ALL_TOUR_FIELDS + "," + NL //                               //$NON-NLS-1$ //$NON-NLS-2$
                  + "    Tmarker.markerId," + NL //                                                          //$NON-NLS-1$
                  + "    jTdataTtag.TourTag_tagId," + NL //                                                  //$NON-NLS-1$
                  + "    TNutritionProduct.productId" + NL //                                                //$NON-NLS-1$

                  + " FROM" + NL //                                                                          //$NON-NLS-1$
                  + " (" + NL //                                                                             //$NON-NLS-1$

                  // get paged tours
                  + "   SELECT " + NL //                                                                     //$NON-NLS-1$
                  + "      " + TVITourBookItem.SQL_ALL_TOUR_FIELDS + NL //                                   //$NON-NLS-1$

                  + "   FROM" + NL //                                                                        //$NON-NLS-1$
                  + "   (" + NL //                                                                           //$NON-NLS-1$

                  // get sorted tours
                  + "      SELECT " + NL //                                                                  //$NON-NLS-1$
                  + "         DISTINCT " + TVITourBookItem.SQL_ALL_TOUR_FIELDS + NL //                       //$NON-NLS-1$

                  + "      FROM" + NL //                                                                     //$NON-NLS-1$
                  + "      (" + NL //                                                                        //$NON-NLS-1$

                  // get filtered tours
                  + "         SELECT " + NL //                                                               //$NON-NLS-1$
                  + "            " + TVITourBookItem.SQL_ALL_TOUR_FIELDS + "," + NL //                       //$NON-NLS-1$ //$NON-NLS-2$
                  + "            jTdataTtag.TourTag_tagId" + NL //                                           //$NON-NLS-1$
                  + "         FROM TOURDATA" + NL //                                                         //$NON-NLS-1$
                  + "         " + tagFilterSqlJoinBuilder.getSqlTagJoinTable() //                            //$NON-NLS-1$
                  + "         AS jTdataTtag" //                                                              //$NON-NLS-1$
                  + "         ON TourData.tourId = jTdataTtag.TourData_tourId" + NL //                       //$NON-NLS-1$

                  + "         WHERE 1=1" + NL //                                                             //$NON-NLS-1$
                  + "            " + sqlAppFilter.getWhereClause() + NL //                                   //$NON-NLS-1$
                  + "         " + _tourCollectionFilter.getSqlString() + NL //                               //$NON-NLS-1$

                  + "        " + orderBy + NL //                                                             //$NON-NLS-1$
                  + "      ) AS TourData " + NL //                                                           //$NON-NLS-1$

                  + "     " + orderBy + NL //                                                                //$NON-NLS-1$
                  + "   ) AS TourData " + NL //                                                              //$NON-NLS-1$

                  + "   OFFSET ? ROWS FETCH NEXT ? ROWS ONLY" + NL //                                        //$NON-NLS-1$

                  + " ) AS TourData" + NL //                                                                 //$NON-NLS-1$

                  + " LEFT JOIN " + TourDatabase.TABLE_TOUR_MARKER + " Tmarker" //                           //$NON-NLS-1$ //$NON-NLS-2$
                  + " ON TourData.tourId = Tmarker.TourData_tourId" + NL //                                  //$NON-NLS-1$

                  + " LEFT JOIN " + TourDatabase.JOINTABLE__TOURDATA__TOURTAG + " jTdataTtag" //             //$NON-NLS-1$ //$NON-NLS-2$
                  + " ON TourData.tourId = jTdataTtag.TourData_tourId" + NL //                               //$NON-NLS-1$

                  // get nutrition product ids
                  + "LEFT OUTER JOIN " + TourDatabase.TABLE_TOUR_NUTRITION_PRODUCT + " TNutritionProduct" // //$NON-NLS-1$ //$NON-NLS-2$
                  + " ON TourData.tourId = TNutritionProduct.TourData_tourId" + NL //                        //$NON-NLS-1$

                  + orderBy + NL;

            final PreparedStatement prepStmt = conn.prepareStatement(sql);

            int paramIndex = 1;

            paramIndex = tagFilterSqlJoinBuilder.setParameters(prepStmt, paramIndex);

            // set filter parameters
            sqlAppFilter.setParameters(prepStmt, paramIndex);
            paramIndex = _tourCollectionFilter.setParameters(prepStmt, sqlAppFilter.getLastParameterIndex());

            // set other parameters
            prepStmt.setInt(paramIndex++, rowIndex);
            prepStmt.setInt(paramIndex++, FETCH_SIZE);

            long prevTourId = -1;
            HashSet<Long> tagIds = null;
            HashSet<Long> markerIds = null;
            HashSet<Long> nutritionProductIds = null;

            final ResultSet result = prepStmt.executeQuery();
            while (result.next()) {

               final long result_TourId = result.getLong(1);

               final Object result_MarkerId = result.getObject(TVITourBookItem.SQL_ALL_OTHER_FIELDS__COLUMN_START_NUMBER);
               final Object result_TagId = result.getObject(TVITourBookItem.SQL_ALL_OTHER_FIELDS__COLUMN_START_NUMBER + 1);
               final Object result_NutritionProductId = result.getObject(TVITourBookItem.SQL_ALL_OTHER_FIELDS__COLUMN_START_NUMBER + 2);

               if (result_TourId == prevTourId) {

                  // these are additional result set's for the same tour

                  // get tags from left (outer) join
                  if (result_TagId instanceof final Long tagId) {
                     tagIds.add(tagId);
                  }

                  // get markers from left (outer) join
                  if (result_MarkerId instanceof final Long markerId) {
                     markerIds.add(markerId);
                  }

                  // get nutrition products from left (outer) join
                  if (result_NutritionProductId instanceof final Long nutritionProductId) {
                     nutritionProductIds.add(nutritionProductId);
                  }

               } else {

                  // first resultset for a new tour

                  final TVITourBookTour tourItem = new TVITourBookTour(null, null);

                  tourItem.tourId = result_TourId;

                  TVITourBookItem.getTourDataFields(result, tourItem);

                  // get first tag id
                  if (result_TagId instanceof final Long tagId) {

                     tagIds = new HashSet<>();
                     tagIds.add(tagId);

                     tourItem.setTagIds(tagIds);
                  }

                  // get first marker id
                  if (result_MarkerId instanceof final Long markerId) {

                     markerIds = new HashSet<>();
                     markerIds.add(markerId);

                     tourItem.setMarkerIds(markerIds);
                  }

                  // get first nutrition product id
                  if (result_NutritionProductId instanceof final Long nutritionProductId) {

                     nutritionProductIds = new HashSet<>();
                     nutritionProductIds.add(nutritionProductId);

                     tourItem.setNutritionProductsIds(nutritionProductIds);
                  }

                  // keep tour item
                  final int natTableRowIndex = rowIndex++;

                  _fetchedTourItems.put(natTableRowIndex, tourItem);
                  _fetchedTourIndex.put(tourItem.tourId, natTableRowIndex);
               }

               prevTourId = result_TourId;
            }
         }

      } catch (final SQLException sqlException) {

         final String sqlState = sqlException.getSQLState();

         if (TourDatabase.SQL_ERROR_XCL13.equals(sqlState)) {

            /**
             * ERROR XCL13: The parameter position '13' is out of range. The number of
             * parameters for this prepared statement is '12'.
             * <p>
             * net.tourbook.ui.views.tourBook.natTable.NatTable_DataLoader.fetchPagedTourItems(NatTable_DataLoader.java:648)
             * <p>
             * or
             * <p>
             * net.tourbook.ui.views.tourBook.natTable.NatTable_DataLoader.fetchPagedTourItems(NatTable_DataLoader.java:652)
             * <p>
             * This cannot be reproduced but it happened firstly, after fixing the tour collection
             * filter in commit
             * https://github.com/mytourbook/mytourbook/commit/b4c664f37caadfc45695e0ef806bad4c9a7fd4fc
             * and it occurred already several times
             * <p>
             */

            StatusUtil.log(sqlException);

         } else {

            SQL.showException(sqlException, sql);
         }

         return false;
      }

      return true;
   }

   /**
    * @param hoveredRow
    *
    * @return Returns the fetched tour by it's row index or <code>null</code> when tour is not yet
    *         fetched from the backend.
    */
   public TVITourBookTour getFetchedTour(final int hoveredRow) {

      return _fetchedTourItems.get(hoveredRow);
   }

   /**
    * @param tourId
    *
    * @return Returns the tour index but only when it was already fetched (which was done for
    *         displayed tour), otherwise -1.
    */
   public int getFetchedTourIndex(final long tourId) {

      final Integer rowIndex = _fetchedTourIndex.get(tourId);
      if (rowIndex == null) {
         return -1;
      } else {
         return rowIndex;
      }
   }

   /**
    * @return Returns number of all tours for the current lazy loader
    */
   int getNumberOfTours() {

      if (_numAllTourItems == -1) {
         _numAllTourItems = fetchNumberOfTours();
      }

      return _numAllTourItems;
   }

   /**
    * @return Returns number of all tours for the current lazy loader without the collection filter
    */
   public int getNumberOfToursWithoutCollectionFilter() {

      if (_numAllTourItems == -1) {

         // load number of tours
         getNumberOfTours();
      }

      return _numToursWithoutCollectionFilter;
   }

   /**
    * Number of columns which are visible in the natTable
    */
   int getNumberOfVisibleColumns() {

      return allSortedColumns.size();
   }

   /**
    * @param allRequestedTourIds
    *
    * @return Returns NatTable row indices from the requested tour id's.
    */
   public CompletableFuture<int[]> getRowIndexFromTourId(final List<Long> allRequestedTourIds) {

      if (_allSortedTourIds == null) {

         // firstly load all tour id's

         return CompletableFuture.supplyAsync(() -> {

            fetchAllTourIds();

            return createRowIndicesFromTourIds(allRequestedTourIds);

         }, _rowIndexExecutor);

      } else {

         return CompletableFuture.supplyAsync(() -> {

            return createRowIndicesFromTourIds(allRequestedTourIds);

         }, _rowIndexExecutor);
      }
   }

   public String[] getSortColumnIds() {
      return _allSortColumnIds;
   }

   public List<SortDirectionEnum> getSortDirections() {
      return _allSortDirections;
   }

   /**
    * Maps column field -> database field
    *
    * @param sortColumnId
    *
    * @return Returns database field
    */
   public String getSqlField_OrderBy(final String sortColumnId) {

      /*
       * Compute average speed/pace with derby custom functions
       */
      final String timeField = _prefStore.getBoolean(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME)
            ? "tourDeviceTime_Recorded" //$NON-NLS-1$
            : "tourComputedTime_Moving"; //$NON-NLS-1$

// SET_FORMATTING_OFF

      /*
       * These functions are stored procedures from net.tourbook.ext.apache.custom.DerbyCustomFunctions
       */
      final String sortByAvgSpeed = "avgSpeed(" + timeField + ", tourDistance)"; //$NON-NLS-1$ //$NON-NLS-2$
      final String sortByPace     = "avgPace("  + timeField + ", tourDistance)"; //$NON-NLS-1$ //$NON-NLS-2$

      switch (sortColumnId) {

      /**
       * These fields have a database index
       */

      // tour date
      case TableColumnFactory.TIME_DATE_ID:                          return SQL_DEFAULT_SORT_FIELD;

      // tour time, THERE IS CURRENTLY NO DATE ONLY FIELD
      case TableColumnFactory.TIME_TOUR_START_TIME_ID:               return FIELD_WITHOUT_SORTING;

      case TableColumnFactory.TOUR_TITLE_ID:                         return "TourTitle";              //$NON-NLS-1$
      case TableColumnFactory.DATA_IMPORT_FILE_NAME_ID:              return "TourImportFileName";     //$NON-NLS-1$

      case TableColumnFactory.TIME_WEEK_NO_ID:                       return "StartWeek";              //$NON-NLS-1$
      case TableColumnFactory.TIME_WEEKYEAR_ID:                      return "StartWeekYear";          //$NON-NLS-1$

// these fields are not yet displayed in tourbook view but are available in tour data indices
//
//    case TableColumnFactory.:                                      return "TourEndTime";            //$NON-NLS-1$
//    case TableColumnFactory.:                                      return "HasGeoData";             //$NON-NLS-1$

      /**
       * These fields have NO database index
       */

      /*
      * BODY
      */
      case TableColumnFactory.BODY_AVG_PULSE_ID:                     return "avgPulse";               //$NON-NLS-1$
      case TableColumnFactory.BODY_CALORIES_ID:                      return "calories";               //$NON-NLS-1$
      case TableColumnFactory.BODY_PULSE_MAX_ID:                     return "maxPulse";               //$NON-NLS-1$
      case TableColumnFactory.BODY_PERSON_ID:                        return "tourPerson_personId";    //$NON-NLS-1$
      case TableColumnFactory.BODY_RESTPULSE_ID:                     return "restPulse";              //$NON-NLS-1$
      case TableColumnFactory.BODY_WEIGHT_ID:                        return "bodyWeight";             //$NON-NLS-1$

      /*
       * DATA
       */
      case TableColumnFactory.DATA_DP_TOLERANCE_ID:                  return "dpTolerance";            //$NON-NLS-1$
      case TableColumnFactory.DATA_HAS_GEO_DATA_ID:                  return "hasGeoData";             //$NON-NLS-1$
//    case TableColumnFactory.DATA_IMPORT_FILE_NAME_ID:              // see indexed fields
      case TableColumnFactory.DATA_IMPORT_FILE_PATH_ID:              return "tourImportFilePath";     //$NON-NLS-1$
      case TableColumnFactory.DATA_NUM_TIME_SLICES_ID:               return "numberOfTimeSlices";     //$NON-NLS-1$
      case TableColumnFactory.DATA_TIME_INTERVAL_ID:                 return "deviceTimeInterval";     //$NON-NLS-1$
      case TableColumnFactory.DATA_TOUR_ID_ID:                       return "tourID";                 //$NON-NLS-1$

      /*
       * DEVICE
       */
      case TableColumnFactory.DEVICE_BATTERY_SOC_START_ID:           return "battery_Percentage_Start";  //$NON-NLS-1$
      case TableColumnFactory.DEVICE_BATTERY_SOC_END_ID:             return "battery_Percentage_End";    //$NON-NLS-1$
      case TableColumnFactory.DEVICE_DISTANCE_ID:                    return "startDistance";             //$NON-NLS-1$
      case TableColumnFactory.DEVICE_NAME_ID:                        return "devicePluginName";          //$NON-NLS-1$

      /*
       * ELEVATION
       */
      case TableColumnFactory.ALTITUDE_AVG_CHANGE_ID:                return "avgAltitudeChange";      //$NON-NLS-1$
      case TableColumnFactory.ALTITUDE_MAX_ID:                       return "maxAltitude";            //$NON-NLS-1$
      case TableColumnFactory.ALTITUDE_SUMMARIZED_BORDER_DOWN_ID:    return "tourAltDown";            //$NON-NLS-1$
      case TableColumnFactory.ALTITUDE_SUMMARIZED_BORDER_UP_ID:      return "tourAltUp";              //$NON-NLS-1$

      /*
       * MOTION
       */
      case TableColumnFactory.MOTION_AVG_PACE_ID:                    return sortByPace;
      case TableColumnFactory.MOTION_AVG_SPEED_ID:                   return sortByAvgSpeed;
      case TableColumnFactory.MOTION_DISTANCE_ID:                    return "tourDistance";           //$NON-NLS-1$
      case TableColumnFactory.MOTION_MAX_SPEED_ID:                   return "maxSpeed";               //$NON-NLS-1$

      /*
       * POWER
       */
      case TableColumnFactory.POWER_AVG_ID:                          return "power_Avg";              //$NON-NLS-1$
      case TableColumnFactory.POWER_MAX_ID:                          return "power_Max";              //$NON-NLS-1$
      case TableColumnFactory.POWER_NORMALIZED_ID:                   return "power_Normalized";       //$NON-NLS-1$
      case TableColumnFactory.POWER_TOTAL_WORK_ID:                   return "power_TotalWork";        //$NON-NLS-1$

      /*
       * POWERTRAIN
       */
      case TableColumnFactory.POWERTRAIN_AVG_CADENCE_ID:                            return "avgCadence";                          //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_AVG_LEFT_PEDAL_SMOOTHNESS_ID:              return "power_AvgLeftPedalSmoothness";        //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_AVG_LEFT_TORQUE_EFFECTIVENESS_ID:          return "power_AvgLeftTorqueEffectiveness";    //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_AVG_RIGHT_PEDAL_SMOOTHNESS_ID:             return "power_AvgRightPedalSmoothness";       //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_AVG_RIGHT_TORQUE_EFFECTIVENESS_ID:         return "power_AvgRightTorqueEffectiveness";   //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_CADENCE_MULTIPLIER_ID:                     return "cadenceMultiplier";                   //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_GEAR_FRONT_SHIFT_COUNT_ID:                 return "frontShiftCount";                     //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_GEAR_REAR_SHIFT_COUNT_ID:                  return "rearShiftCount";                      //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_PEDAL_LEFT_RIGHT_BALANCE_ID:               return "power_PedalLeftRightBalance";         //$NON-NLS-1$
      case TableColumnFactory.POWERTRAIN_SLOW_VS_FAST_CADENCE_PERCENTAGES_ID:       return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.POWERTRAIN_SLOW_VS_FAST_CADENCE_ZONES_DELIMITER_ID:   return FIELD_WITHOUT_SORTING;

      /*
       * RUNNING DYNAMICS
       */
      case TableColumnFactory.RUN_DYN_STANCE_TIME_AVG_ID:            return "runDyn_StanceTime_Avg";           //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STANCE_TIME_MIN_ID:            return "runDyn_StanceTime_Min";           //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STANCE_TIME_MAX_ID:            return "runDyn_StanceTime_Max";           //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STANCE_TIME_BALANCE_AVG_ID:    return "runDyn_StanceTimeBalance_Avg";    //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STANCE_TIME_BALANCE_MIN_ID:    return "runDyn_StanceTimeBalance_Min";    //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STANCE_TIME_BALANCE_MAX_ID:    return "runDyn_StanceTimeBalance_Max";    //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STEP_LENGTH_AVG_ID:            return "runDyn_StepLength_Avg";           //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STEP_LENGTH_MIN_ID:            return "runDyn_StepLength_Min";           //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_STEP_LENGTH_MAX_ID:            return "runDyn_StepLength_Max";           //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_VERTICAL_OSCILLATION_AVG_ID:   return "runDyn_VerticalOscillation_Avg";  //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_VERTICAL_OSCILLATION_MIN_ID:   return "runDyn_VerticalOscillation_Min";  //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_VERTICAL_OSCILLATION_MAX_ID:   return "runDyn_VerticalOscillation_Max";  //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_VERTICAL_RATIO_AVG_ID:         return "runDyn_VerticalRatio_Avg";        //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_VERTICAL_RATIO_MIN_ID:         return "runDyn_VerticalRatio_Min";        //$NON-NLS-1$
      case TableColumnFactory.RUN_DYN_VERTICAL_RATIO_MAX_ID:         return "runDyn_VerticalRatio_Max";        //$NON-NLS-1$

      /*
       * SURFING
       */
      case TableColumnFactory.SURFING_MIN_DISTANCE_ID:               return "surfing_MinDistance";             //$NON-NLS-1$
      case TableColumnFactory.SURFING_MIN_SPEED_START_STOP_ID:       return "surfing_MinSpeed_StartStop";      //$NON-NLS-1$
      case TableColumnFactory.SURFING_MIN_SPEED_SURFING_ID:          return "surfing_MinSpeed_Surfing";        //$NON-NLS-1$
      case TableColumnFactory.SURFING_MIN_TIME_DURATION_ID:          return "surfing_MinTimeDuration";         //$NON-NLS-1$
      case TableColumnFactory.SURFING_NUMBER_OF_EVENTS_ID:           return "surfing_NumberOfEvents";          //$NON-NLS-1$

      /*
       * TIME
       */
      case TableColumnFactory.TIME__DEVICE_ELAPSED_TIME_ID:          return "tourDeviceTime_Elapsed";          //$NON-NLS-1$
      case TableColumnFactory.TIME__DEVICE_RECORDED_TIME_ID:         return "tourDeviceTime_Recorded";         //$NON-NLS-1$
      case TableColumnFactory.TIME__DEVICE_PAUSED_TIME_ID:           return "tourDeviceTime_Paused";           //$NON-NLS-1$
      case TableColumnFactory.TIME__COMPUTED_MOVING_TIME_ID:         return "tourComputedTime_Moving";         //$NON-NLS-1$
      case TableColumnFactory.TIME__COMPUTED_BREAK_TIME_ID:          return "(tourDeviceTime_Elapsed - tourComputedTime_Moving)"; //$NON-NLS-1$
      case TableColumnFactory.TIME__COMPUTED_BREAK_TIME_RELATIVE_ID: return FIELD_WITHOUT_SORTING;

      case TableColumnFactory.TIME_TIME_ZONE_ID:                     return "TimeZoneId";                      //$NON-NLS-1$
      case TableColumnFactory.TIME_TIME_ZONE_DIFFERENCE_ID:          return FIELD_WITHOUT_SORTING;
//    case TableColumnFactory.TIME_TOUR_START_TIME_ID:               // see indexed fields
      case TableColumnFactory.TIME_TOUR_END_TIME_ID:                 return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.TIME_WEEK_DAY_ID:                      return FIELD_WITHOUT_SORTING;
//    case TableColumnFactory.TIME_WEEK_NO_ID:                       // see indexed fields
//    case TableColumnFactory.TIME_WEEKYEAR_ID:                      // see indexed fields

      /*
       * TOUR
       */
      case TableColumnFactory.TOUR_DESCRIPTION_ID:                   return "TourDescription";                             //$NON-NLS-1$
      case TableColumnFactory.TOUR_LOCATION_START_ID:                return "COALESCE(tourStartPlace, '')";                //$NON-NLS-1$
      case TableColumnFactory.TOUR_LOCATION_END_ID:                  return "COALESCE(tourEndPlace, '')";                  //$NON-NLS-1$
      case TableColumnFactory.TOUR_LOCATION_ID_START_ID:             return "tourLocationStart_LocationID";                //$NON-NLS-1$
      case TableColumnFactory.TOUR_LOCATION_ID_END_ID:               return "tourLocationEnd_LocationID";                  //$NON-NLS-1$
      case TableColumnFactory.TOUR_NUM_MARKERS_ID:                   return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.TOUR_NUM_PHOTOS_ID:                    return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.TOUR_TAGS_ID:                          return FIELD_WITHOUT_SORTING;
//    case TableColumnFactory.TOUR_TITLE_ID:                         // see indexed fields
      case TableColumnFactory.TOUR_TYPE_ID:                          return "tourType_typeId";  // an icon is displayed    //$NON-NLS-1$
      case TableColumnFactory.TOUR_TYPE_TEXT_ID:                     return FIELD_WITHOUT_SORTING;

      /*
       * TRAINING
       */
      case TableColumnFactory.TRAINING_EFFECT_AEROB_ID:              return "training_TrainingEffect_Aerob";               //$NON-NLS-1$
      case TableColumnFactory.TRAINING_EFFECT_ANAEROB_ID:            return "training_TrainingEffect_Anaerob";             //$NON-NLS-1$
      case TableColumnFactory.TRAINING_FTP_ID:                       return "power_FTP";                                   //$NON-NLS-1$
      case TableColumnFactory.TRAINING_INTENSITY_FACTOR_ID:          return "power_IntensityFactor";                       //$NON-NLS-1$
      case TableColumnFactory.TRAINING_POWER_TO_WEIGHT_ID:           return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.TRAINING_STRESS_SCORE_ID:              return "power_TrainingStressScore";                   //$NON-NLS-1$
      case TableColumnFactory.TRAINING_PERFORMANCE_LEVEL_ID:         return "training_TrainingPerformance";                //$NON-NLS-1$

      /*
       * WEATHER
       */
      case TableColumnFactory.WEATHER_AIR_QUALITY_ID:                return "weather_AirQuality"; //$NON-NLS-1$
      case TableColumnFactory.WEATHER_CLOUDS_ID:                     return "weather_Clouds";   // an icon is displayed     //$NON-NLS-1$
      case TableColumnFactory.WEATHER_TEMPERATURE_AVG_ID:            return "(DOUBLE(weather_Temperature_Average) / temperatureScale)"; //$NON-NLS-1$
      case TableColumnFactory.WEATHER_TEMPERATURE_AVG_COMBINED_ID:   return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.WEATHER_TEMPERATURE_AVG_DEVICE_ID:     return "(DOUBLE(weather_Temperature_Average_Device) / temperatureScale)"; //$NON-NLS-1$
      case TableColumnFactory.WEATHER_TEMPERATURE_MIN_ID:            return "weather_Temperature_Min";                     //$NON-NLS-1$
      case TableColumnFactory.WEATHER_TEMPERATURE_MIN_COMBINED_ID:   return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.WEATHER_TEMPERATURE_MIN_DEVICE_ID:     return "weather_Temperature_Min_Device";                     //$NON-NLS-1$
      case TableColumnFactory.WEATHER_TEMPERATURE_MAX_ID:            return "weather_Temperature_Max";                     //$NON-NLS-1$
      case TableColumnFactory.WEATHER_TEMPERATURE_MAX_COMBINED_ID:   return FIELD_WITHOUT_SORTING;
      case TableColumnFactory.WEATHER_TEMPERATURE_MAX_DEVICE_ID:     return "weather_Temperature_Max_Device";                     //$NON-NLS-1$
      case TableColumnFactory.WEATHER_WIND_DIRECTION_ID:             return "weather_Wind_Direction";                              //$NON-NLS-1$
      case TableColumnFactory.WEATHER_WIND_SPEED_ID:                 return "weather_Wind_Speed";                              //$NON-NLS-1$

      /*
       * NUTRITION
       */
      case TableColumnFactory.NUTRITION_NUM_PRODUCTS_ID:             return FIELD_WITHOUT_SORTING;


      default:

         // ensure a valid field is returned, this case should not happen but it helps during development

         System.out.println(UI.timeStampNano() + " [" + getClass().getSimpleName() + "] getSqlField_OrderBy()" //$NON-NLS-1$ //$NON-NLS-2$
               + "\tsortColumnId: \"" + sortColumnId + UI.SYMBOL_QUOTATION_MARK //$NON-NLS-1$
               + " has not a valid sql field" //$NON-NLS-1$
               );

         return SQL_DEFAULT_SORT_FIELD;
      }

   // SET_FORMATTING_ON
   }

   /**
    * Maps column field -> database field
    *
    * @param sortColumnId
    *
    * @return Returns database field
    */
   private String getSqlField_SelectFields(final String sortColumnId) {

      final String timeField = _prefStore.getBoolean(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME)
            ? "tourDeviceTime_Recorded" //$NON-NLS-1$
            : "tourComputedTime_Moving"; //$NON-NLS-1$

      final String speedPace_SelectFields = timeField + ", tourDistance"; //$NON-NLS-1$

// SET_FORMATTING_OFF

      switch (sortColumnId) {

      /*
       * MOTION
       */
      case TableColumnFactory.MOTION_AVG_PACE_ID:        return speedPace_SelectFields;
      case TableColumnFactory.MOTION_AVG_SPEED_ID:       return speedPace_SelectFields;

      }

// SET_FORMATTING_ON

      return null;
   }

   /**
    * @param rowIndex
    *
    * @return Returns tour item at the requested row index or <code>null</code> when not yet
    *         available. When tour is not yet loaded then the data will be fetched from the backend.
    */
   TVITourBookTour getTour(final int rowIndex) {

      final TVITourBookTour loadedTourItem = _fetchedTourItems.get(rowIndex);

      if (loadedTourItem != null) {

         // tour is loaded

         return loadedTourItem;
      }

      /*
       * Check if the tour is currently fetched
       */
      final int fetchKey = rowIndex / FETCH_SIZE;

      LazyTourLoaderItem lazyTourLoaderItem = _pageNumbers_Loading.get(fetchKey);

      if (lazyTourLoaderItem != null) {

         // tour is currently being loading -> wait until finished loading

         return null;
      }

      /*
       * Tour is not yet loaded or not yet loading -> load it now
       */

      lazyTourLoaderItem = new LazyTourLoaderItem();

      lazyTourLoaderItem.sqlOffset = fetchKey * FETCH_SIZE;
      lazyTourLoaderItem.fetchKey = fetchKey;

      _pageNumbers_Loading.put(fetchKey, lazyTourLoaderItem);

      _loaderWaitingQueue.add(lazyTourLoaderItem);

      _loadingExecutor.submit(() -> {

         // get last added loader item
         final LazyTourLoaderItem loaderItem = _loaderWaitingQueue.pollFirst();

         if (loaderItem == null) {
            return;
         }

         if (fetchPagedTourItems(loaderItem)) {

            // update UI

            final NatTable tourViewer_NatTable = _tourBookView.getTourViewer_NatTable();
            final Display display = tourViewer_NatTable.getDisplay();

            display.asyncExec(() -> {

               if (tourViewer_NatTable.isDisposed()) {
                  return;
               }

               // do a simple redraw as it retrieves values from the model
               tourViewer_NatTable.redraw();
            });
         }

         final int loaderItemFetchKey = loaderItem.fetchKey;

         _pageNumbers_Fetched.put(loaderItemFetchKey, loaderItemFetchKey);
         _pageNumbers_Loading.remove(loaderItemFetchKey);
      });

      return null;
   }

   long getTourId(final int rowIndex) {

      if (_allSortedTourIds == null) {

         return -1;

      } else {

         return _allSortedTourIds[rowIndex];
      }
   }

   /**
    * Cleanup all loaded data that the next time they are newly fetched when requested.
    *
    * @param isResetTourCollectionFilter
    */
   public void resetTourItems(final boolean isResetTourCollectionFilter) {

      for (final TVITourBookTour tourItem : _fetchedTourItems.values()) {
         tourItem.clearChildren();
      }

      _fetchedTourItems.clear();
      _fetchedTourIndex.clear();

      _pageNumbers_Fetched.clear();
      _pageNumbers_Loading.clear();

      _allSortedTourIds = null;

      _numAllTourItems = -1;

      if (isResetTourCollectionFilter) {
         _tourCollectionFilter = EMPTY_SQL_DATA;
      }
   }

   public void setTourCollectionFilter(final TourCollectionFilter tourCollectionFilter, final List<Long> allTourIds) {

      final int numIDs = allTourIds.size();

      if (numIDs < 1 || tourCollectionFilter == TourCollectionFilter.ALL_TOURS) {

         _tourCollectionFilter = EMPTY_SQL_DATA;

         return;
      }

      String sqlStatement;

      final ArrayList<Object> allSqlParameters = new ArrayList<>();
      allSqlParameters.addAll(allTourIds);

      final String parameterList = SQL.createParameterList(numIDs);

      switch (tourCollectionFilter) {

      case COLLECTED_TOURS:

         sqlStatement = "AND TourData.tourId IN (" + parameterList + ")"; //$NON-NLS-1$ //$NON-NLS-2$

         _tourCollectionFilter = new SQLData(sqlStatement, allSqlParameters);

         break;

      case NOT_COLLECTED_TOURS:

         sqlStatement = "AND TourData.tourId NOT IN (" + parameterList + ")"; //$NON-NLS-1$ //$NON-NLS-2$

         _tourCollectionFilter = new SQLData(sqlStatement, allSqlParameters);

         break;

      default:

         _tourCollectionFilter = EMPTY_SQL_DATA;

         break;
      }
   }

   /**
    * Sets sort column id/direction but first cleanup the previous loaded tours.
    *
    * @param allSortColumnIds
    * @param allSortDirections
    */
   public void setupSortColumns(final String[] allSortColumnIds, final List<SortDirectionEnum> allSortDirections) {

      // cleanup old fetched tours
      resetTourItems(false);

      _allSortColumnIds = allSortColumnIds;
      _allSortDirections = allSortDirections;

      _allSqlSortFields_OrderBy.clear();
      _allSqlSortFields_SelectFields.clear();
      _allSqlSortDirections.clear();

      boolean isSortedByDateTime = false;
      final int numSortColumns = allSortDirections.size();

      for (int columnIndex = 0; columnIndex < numSortColumns; columnIndex++) {

         final String sortColumnId = allSortColumnIds[columnIndex];

         final String sqlField_OrderBy = getSqlField_OrderBy(sortColumnId);

         /*
          * Ensure that the dummy field is not used in the sql statement, this should not happen but
          * it did during development
          */
         if (FIELD_WITHOUT_SORTING.equals(sqlField_OrderBy)) {

            // field is not sorted

            continue;
         }

         if (SQL_DEFAULT_SORT_FIELD.equals(sqlField_OrderBy)) {

            isSortedByDateTime = true;
         }

         final SortDirectionEnum sortDirectionEnum = allSortDirections.get(columnIndex);

         // skip field which are not sorted
         if (sortDirectionEnum.equals(SortDirectionEnum.NONE) == false) {

            /*
             * Set sort order
             */
            if (sortDirectionEnum == SortDirectionEnum.ASC) {
               _allSqlSortDirections.add(SQL_ASCENDING);
            } else {
               _allSqlSortDirections.add(SQL_DESCENDING);
            }

            /*
             * Set sort field(s)
             */
            _allSqlSortFields_OrderBy.add(sqlField_OrderBy);

            final String sqlField_SelectFields = getSqlField_SelectFields(sortColumnId);
            if (sqlField_SelectFields != null) {

               // use special "select" fields

               _allSqlSortFields_SelectFields.add(sqlField_SelectFields);

            } else {

               // use "order by" fields

               _allSqlSortFields_SelectFields.add(sqlField_OrderBy);
            }
         }
      }

      if (isSortedByDateTime == false) {

         /**
          * Add an additional sorting by date/time.
          * <p>
          * Because of the different tour ID retrievals in fetchPagedTourItems() and
          * fetchAllTourIds(), it happened, that the sorting of the not sorted tours were different
          * and a selected tour was not the selected display tour because of different tour ID
          * sortings !!!
          * <p>
          * It took me many hours to find/debug this simple workaround.
          */

         _allSqlSortDirections.add(SQL_ASCENDING);

         _allSqlSortFields_OrderBy.add(SQL_DEFAULT_SORT_FIELD);
         _allSqlSortFields_SelectFields.add(SQL_DEFAULT_SORT_FIELD);
      }
   }

}
