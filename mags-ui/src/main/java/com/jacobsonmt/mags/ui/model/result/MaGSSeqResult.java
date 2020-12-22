package com.jacobsonmt.mags.ui.model.result;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class MaGSSeqResult {

    @JsonAlias("zscoreHuman")
    private Double scoreHuman;

    @JsonAlias("zscoreYeast")
    private Double scoreYeast;

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
