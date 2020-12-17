package com.jacobsonmt.mags.ui.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"reference"})
@ToString
public class Base {
    private String reference;
    private int depth;
    private double conservation;
    private List<Double> list = new ArrayList<>();
}
