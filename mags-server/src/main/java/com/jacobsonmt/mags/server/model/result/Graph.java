package com.jacobsonmt.mags.server.model.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Graph {

    private final String label;
    private final String title;
    private final String description;
    private final Number score;
    private final Distribution distribution;
}
