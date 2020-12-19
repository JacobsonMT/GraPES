package com.jacobsonmt.mags.server.model.search;

import lombok.Value;

@Value
public class FieldSort {
    String field;
    boolean asc;
}
