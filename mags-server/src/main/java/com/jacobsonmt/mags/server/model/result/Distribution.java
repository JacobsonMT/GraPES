package com.jacobsonmt.mags.server.model.result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class Distribution {

    private final Map<String, Number> markers = new LinkedHashMap<>();
    private final List<Number> background = new ArrayList<>();

    public void add(Number data) {
        background.add(data);
    }
}
