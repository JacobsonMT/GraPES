package com.jacobsonmt.mags.server.entities;

public enum Species {
    HUMAN("Human"),
    YEAST("Yeast");

    private String label;

    Species(String label) {
        this.label = label;
    }
}
