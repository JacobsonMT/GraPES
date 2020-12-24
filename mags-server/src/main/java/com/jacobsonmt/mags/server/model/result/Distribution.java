package com.jacobsonmt.mags.server.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import weka.estimators.KernelEstimator;

@Data
public class Distribution {

    private static final int KERNEL_STEPS = 100;

    private final Map<String, Number> markers = new LinkedHashMap<>();

    @JsonIgnore
    private final List<Number> background = new ArrayList<>();

    public void add(Number data) {
        background.add(data);
    }

    private List<Double[]> kde;

    public void buildKernelDensityEstimate() {

        Double[] iqr = iqr(background);
        double q1 = iqr[0];
        double q3 = iqr[1];

        // Remove outliers
        double outlierMax = q3 + (q3 - q1) * 1.5;
        double outlierMin = q1 - (q3 - q1) * 1.5;
        List<Double> filteredBackground = background.stream().map(Number::doubleValue).filter(v ->
            v < outlierMax && v > outlierMin).collect(Collectors.toList());

        KernelEstimator kernelEstimator = new KernelEstimator();
        for (Double aDouble : filteredBackground) {
            kernelEstimator.addValue(aDouble, 1);
        }


        List<Double[]> data = new ArrayList<>();

        double min = Collections.min(filteredBackground);
        double max = Collections.max(filteredBackground);

        double stepWidth = (max - min) / KERNEL_STEPS;

        for (int i = 0; i < KERNEL_STEPS; i++) {
            double xi = min + i * stepWidth;

            data.add(new Double[]{xi, kernelEstimator.getProbability(xi)});
        }

        this.kde = data;
    }
    private Double[] iqr(List<? extends Number> values) {
        values.sort((o1, o2) -> {
            Double d1 = (o1 == null) ? Double.POSITIVE_INFINITY : o1.doubleValue();
            Double d2 = (o2 == null) ? Double.POSITIVE_INFINITY : o2.doubleValue();
            return d1.compareTo(d2);
        });
        return new Double[] {values.get(values.size() / 4).doubleValue(), values.get(3 * values.size() / 4).doubleValue()};
    }
}
