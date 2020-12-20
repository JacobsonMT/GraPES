package com.jacobsonmt.mags.ui.model.result;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Distribution {
    private String label;
    private String title;
    private Number score;
    private Map<String, Number> markers;
    private List<Number> background;
}
