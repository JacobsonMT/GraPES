package com.jacobsonmt.mags.ui.model.result;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class Distribution {

    private Map<String, Number> markers;
    private List<Number> background;
}
