package com.jacobsonmt.mags.ui.model.result;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class MaGSSeqResult {


    private String species;

    /**
     * MaGS z score
     */
    @JsonAlias("zscore")
    private Double zScore;

    @JsonAlias("diso")
    private Double disorder;

    @JsonAlias("pip")
    private Double propensityScore;

    @JsonAlias("rbp")
    private Double rbpPred;

    @JsonAlias("sol")
    private Double soluprot;

    @JsonAlias("len")
    private Integer length;

    @JsonAlias("tgo")
    private Double tango;

    @JsonAlias("g")
    private Double compositionG;
    @JsonAlias("r")
    private Double compositionR;
    @JsonAlias("l")
    private Double compositionL;
    @JsonAlias("d")
    private Double compositionD;
    @JsonAlias("p")
    private Double compositionP;
    @JsonAlias("s")
    private Double compositionS;

}
