package com.jacobsonmt.mags.server.model.result;

import com.jacobsonmt.mags.server.entities.JobResult;
import com.jacobsonmt.mags.server.entities.Species;
import lombok.Data;

@Data
public class MaGSSeqResultVO {

    public static MaGSSeqResultVO fromPrecomputedResult(JobResult jobResult) {
        MaGSSeqResultVO result = new MaGSSeqResultVO();
        result.setLabel(jobResult.getJob().getLabel());
        result.setSpecies(jobResult.getSpecies());

        result.setScore(jobResult.getZScore());

        result.setDisorder(jobResult.getDiso());
        result.setPropensityScore(jobResult.getPip());
        result.setRbpPred(jobResult.getRbp());
        result.setSoluprot(jobResult.getSol());
        result.setLength(jobResult.getLen());
        result.setTango(jobResult.getTgo());
        result.setCompositionG(jobResult.getG());
        result.setCompositionR(jobResult.getR());
        result.setCompositionL(jobResult.getL());
        result.setCompositionD(jobResult.getD());
        result.setCompositionP(jobResult.getP());
        result.setCompositionS(jobResult.getS());

        return result;
    }

    private String label;
    private Species species;


    /**
     * MaGS z score
     */
    private Double score;

    private Double disorder;
    private Double propensityScore;
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
