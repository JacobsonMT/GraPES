package com.jacobsonmt.mags.server.model.search;

import lombok.Value;

@Value
public class FieldSearch {
    String field;
    String query;
}
