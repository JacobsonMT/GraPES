package com.jacobsonmt.mags.server.model.search;

import java.util.List;
import lombok.Value;

@Value
public class SearchCriteria {
    List<FieldSearch> fieldSearches;
    List<FieldSort> fieldSorts;
    Integer page;
    Integer size;
}
