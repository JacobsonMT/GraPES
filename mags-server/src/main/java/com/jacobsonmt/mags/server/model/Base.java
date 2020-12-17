package com.jacobsonmt.mags.server.model;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"reference"})
@ToString
public class Base {
    private final String reference;
    private final int depth;
    private final double conservation;
    private List<Double> list = new ArrayList<>();
}
