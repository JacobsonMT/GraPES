package com.jacobsonmt.mags.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(of = {"id"}, callSuper = false)
@ToString(exclude = "job")
@NoArgsConstructor
@Data
@Entity
@Table(name = "job_result")
public class JobResult extends MaGSSeqResult {

    @Id
    private long id;

    // Not yet lazy, see https://hibernate.atlassian.net/browse/HHH-10771
    @JsonIgnore
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id")
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "species")
    private Species species;

    public static JobResult fromJobResult(JobResult jobResult) {
        JobResult result = new JobResult();
        result.setSpecies(jobResult.getSpecies());

        result.setScore(jobResult.getScore());
        result.setZScore(jobResult.getZScore());
        result.setDiso(jobResult.getDiso());
        result.setLen(jobResult.getLen());
        result.setRun(jobResult.getRun());
        result.setMax(jobResult.getMax());
        result.setChg(jobResult.getChg());
        result.setNet(jobResult.getNet());
        result.setGvy(jobResult.getGvy());
        result.setPip(jobResult.getPip());
        result.setTgo(jobResult.getTgo());
        result.setMfc(jobResult.getMfc());
        result.setSto(jobResult.getSto());
        result.setStc(jobResult.getStc());
//        result.setSft(jobResult.getSft());
        result.setScn(jobResult.getScn());
        result.setSbb(jobResult.getSbb());
        result.setPol(jobResult.getPol());
        result.setRbp(jobResult.getRbp());
        result.setSol(jobResult.getSol());
        result.setCat(jobResult.getCat());
        result.setR(jobResult.getR());
        result.setH(jobResult.getH());
        result.setK(jobResult.getK());
        result.setD(jobResult.getD());
        result.setE(jobResult.getE());
        result.setS(jobResult.getS());
        result.setT(jobResult.getT());
        result.setN(jobResult.getN());
        result.setQ(jobResult.getQ());
        result.setC(jobResult.getC());
        result.setG(jobResult.getG());
        result.setP(jobResult.getP());
        result.setA(jobResult.getA());
        result.setV(jobResult.getV());
        result.setI(jobResult.getI());
        result.setL(jobResult.getL());
        result.setM(jobResult.getM());
        result.setF(jobResult.getF());
        result.setY(jobResult.getY());
        result.setW(jobResult.getW());

        return result;
    }

}
