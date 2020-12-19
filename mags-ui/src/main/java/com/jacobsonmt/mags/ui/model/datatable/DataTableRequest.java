package com.jacobsonmt.mags.ui.model.datatable;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class DataTableRequest {

    @Data
    public static class Search {
        /**
         * Global search value. To be applied to all columns which have searchable as true.
         */
        private String value;

        /**
         * <code>true</code> if the global filter should be treated as a regular expression for advanced searching, false otherwise. Note that normally server-side
         * processing scripts will not perform regular expression searching for performance reasons on large data sets, but it is technically possible and at the
         * discretion of your script.
         */
        private boolean regex;
    }

    @Data
    public static class Order {
        /**
         * Column to which ordering should be applied. This is an index reference to the columns array of information that is also submitted to the server.
         */
        private int column;

        /**
         * Ordering direction for this column. It will be <code>asc</code> or <code>desc</code> to indicate ascending ordering or descending ordering,
         * respectively.
         */
        private String dir;
    }

    @Data
    public static class Column {
        /**
         * Column's data source, as defined by columns.data.
         */
        private String data;

        /**
         * Column's name, as defined by columns.name.
         */
        private String name;

        /**
         * Flag to indicate if this column is searchable (true) or not (false). This is controlled by columns.searchable.
         */
        private boolean searchable;


        /**
         * Flag to indicate if this column is orderable (true) or not (false). This is controlled by columns.orderable.
         */
        private boolean orderable;

        /**
         * Search value to apply to this specific column.
         */
        private Search search;

        /**
         * Flag to indicate if the search term for this column should be treated as regular expression (true) or not (false). As with global search, normally
         * server-side processing scripts will not perform regular expression searching for performance reasons on large data sets, but it is technically possible
         * and at the discretion of your script.
         */
        private boolean regex;
    }

    /**
     * Draw counter. This is used by DataTables to ensure that the Ajax returns from server-side processing requests are drawn in sequence by DataTables
     * (Ajax requests are asynchronous and thus can return out of sequence). This is used as part of the draw return parameter (see below).
     */
    private int draw;

    /**
     * Paging first record indicator. This is the start point in the current data set (0 index based - i.e. 0 is the first record).
     */
    private int start;

    /**
     * Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless
     * the server has fewer records to return. Note that this can be -1 to indicate that all records should be returned (although that negates any benefits of
     * server-side processing!)
     */
    private int length;

    /**
     * @see Search
     */
    private Search search;

    /**
     * @see Order
     */
    @JsonProperty("order")
    private List<Order> orders;

    /**
     * @see Column
     */
    private List<Column> columns;
}
