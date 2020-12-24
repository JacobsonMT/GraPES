package com.jacobsonmt.mags.ui.model.result;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Graph {

    private String label;
    private String title;
    private String description;
    private String unit;
    private Number score;
    private Distribution distribution;
}
