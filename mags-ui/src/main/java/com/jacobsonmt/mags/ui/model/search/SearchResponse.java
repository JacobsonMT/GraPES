package com.jacobsonmt.mags.ui.model.search;

import java.util.List;
import lombok.Data;

@Data
public class SearchResponse {
    private List<?> data;
    private long total;
    private long matched;
}
