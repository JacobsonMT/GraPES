package com.jacobsonmt.mags.ui.model.datatable;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;

@Data
public class DataTableResponse {
    /**
     * The draw counter that this object is a response to - from the draw parameter sent as part of the data request. Note that it is strongly recommended for
     * security reasons that you cast this parameter to an integer, rather than simply echoing back to the client what it sent in the draw parameter, in order
     * to prevent Cross Site Scripting (XSS) attacks.
     */
    private int draw;

    /**
     * Total records, before filtering (i.e. the total number of records in the database)
     * <p/>
     */
    private long recordsTotal;

    /**
     * Total records, after filtering (i.e. the total number of records after filtering has been applied - not just the number of records being returned for this
     * page of data).
     * <p/>
     */
    private long recordsFiltered;

    /**
     * Optional: If an error occurs during the running of the server-side processing script, you can inform the user of this error by passing back the error message
     * to be displayed using this parameter. Do not include if there is no error.
     */
    private String error;

    /**
     * The data to be displayed in the table. This is an array of data source objects, one for each row, which will be used by DataTables. Note that this parameter's
     * name can be changed using the ajax option's dataSrc property.
     */
    private List<?> data = Lists.newArrayList();
}