package com.jacobsonmt.mags.server.model.result;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Distribution {

    private final String label;
    private final String title;
    private final Number score;
    private Map<String, Number> markers;
    private List<Number> background;
}
