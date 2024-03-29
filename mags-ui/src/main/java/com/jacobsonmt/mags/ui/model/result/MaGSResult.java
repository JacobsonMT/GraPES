package com.jacobsonmt.mags.ui.model.result;

import lombok.Data;

@Data
public class MaGSResult {

    private String accession;
    private String gene;
    private String species;


    /**
     * MaGS z score
     */
    private Double score;

    private Double abundance;
    private Double camsol;
    private Integer annotatedPhosphorylationSites;
    private Double pScore;

    private Double disorder;

    private Double compositionD;
    private Double compositionE;

    private Double compositionL;
    private Double compositionG;

    private Boolean rna;
    private Boolean sg;

}
