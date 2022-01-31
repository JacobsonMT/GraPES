package com.jacobsonmt.mags.ui.model.result;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaGSSeqResult {

    @JsonAlias("zscoreHuman")
    private Double scoreHuman;

    @JsonAlias("zscoreYeast")
    private Double scoreYeast;

    @JsonAlias("net")
    private Integer charge;

    @JsonAlias("diso")
    private Double disorder;

    @JsonAlias("gvy")
    private Double gravy;

    @JsonAlias("len")
    private Integer length;

    @JsonAlias("pip")
    // For some reason jackson doesn't like to start with single letter lower case,
    // tos this is necessary otherwise pScore is null
    @JsonProperty("pScore")
    private Double pScore;

    @JsonAlias("rbp")
    private Double rbpPred;

    @JsonAlias("sol")
    private Double soluprot;

    @JsonAlias("tgo")
    private Double tango;

    @JsonAlias("a")
    private Double compositionA;
    @JsonAlias("d")
    private Double compositionD;
    @JsonAlias("f")
    private Double compositionF;
    @JsonAlias("g")
    private Double compositionG;
    @JsonAlias("i")
    private Double compositionI;
    @JsonAlias("l")
    private Double compositionL;
    @JsonAlias("m")
    private Double compositionM;
    @JsonAlias("p")
    private Double compositionP;
    @JsonAlias("r")
    private Double compositionR;
    @JsonAlias("s")
    private Double compositionS;
    @JsonAlias("v")
    private Double compositionV;
    @JsonAlias("w")
    private Double compositionW;

}
