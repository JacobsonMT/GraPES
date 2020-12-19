package com.jacobsonmt.mags.ui.model.search;

import java.util.List;
import lombok.Value;

@Value
public class SearchCriteria {
    List<FieldSearch> fieldSearches;
    List<FieldSort> fieldSorts;
    Integer page;
    Integer size;
}
