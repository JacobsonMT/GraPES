package com.jacobsonmt.mags.server.model.search;

import java.util.List;
import lombok.Value;

@Value
public class SearchResponse {
    List<?> data;
    long total;
    long matched;
}
