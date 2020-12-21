package com.jacobsonmt.mags.ui.model.result;

import lombok.Data;

@Data
public class MaGSSeqResult {


    private String label;
    private String species;


    /**
     * MaGS z score
     */
    private Double score;

    private Double disorder;
    private Double pScore;
    private Double rbpPred;
    private Double soluprot;
    private Integer length;
    private Double tango;

    private Double compositionG;
    private Double compositionR;
    private Double compositionL;
    private Double compositionD;
    private Double compositionP;
    private Double compositionS;

}
