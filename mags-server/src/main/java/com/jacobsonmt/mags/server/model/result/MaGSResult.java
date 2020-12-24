package com.jacobsonmt.mags.server.model.result;

import com.jacobsonmt.mags.server.entities.PrecomputedMaGSResult;
import com.jacobsonmt.mags.server.entities.Species;
import lombok.Data;

@Data
public class MaGSResult {

    public static MaGSResult fromPrecomputedResult(PrecomputedMaGSResult precomputedResult) {
        MaGSResult result = new MaGSResult();
        result.setAccession(precomputedResult.getAccession());
        result.setGene(precomputedResult.getGene());
        result.setSpecies(precomputedResult.getSpecies());

        result.setScore(precomputedResult.getZScore());

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
    private String gene;
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
    private Double compositionE;

    private Double compositionL;
    private Double compositionG;

    private Boolean rna;

}
