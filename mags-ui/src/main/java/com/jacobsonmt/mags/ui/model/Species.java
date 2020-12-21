package com.jacobsonmt.mags.ui.model;

import lombok.Getter;

@Getter
public enum Species {
    HUMAN("Human"),
    YEAST("Yeast");

    private String label;

    Species(String label) {
        this.label = label;
    }
}
