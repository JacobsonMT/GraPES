package com.jacobsonmt.mags.server.model.result;

import com.jacobsonmt.mags.server.entities.PrecomputedResult;
import com.jacobsonmt.mags.server.entities.PrecomputedResult.Species;
import lombok.Data;

@Data
public class Result {

    public static Result fromPrecomputedResult(PrecomputedResult precomputedResult) {
        Result result = new Result();
        result.setAccession(precomputedResult.getAccession());
        result.setSpecies(precomputedResult.getSpecies());

        result.setScore(precomputedResult.getMagsZScore());

        result.setAbundance(precomputedResult.getAbd());
        result.setCamsol(precomputedResult.getCsl());
        result.setAnnotatedPhosphorylationSites(precomputedResult.getPhs());
        result.setPScore(precomputedResult.getPip());
        result.setDisorder(precomputedResult.getDiso());
        result.setCompositionD(precomputedResult.getD());
        result.setCompositionE(precomputedResult.getE());
        result.setCompositionL(precomputedResult.getL());
        result.setCompositionG(precomputedResult.getG());

        result.setRna(precomputedResult.getRna() != null && precomputedResult.getRna().equals(1));

        return result;
    }

    private String accession;
    private Species species;


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
    private Integer compositionE;

    private Double compositionL;
    private Double compositionG;

    private Boolean rna;

}
