package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "precomputed")
public class PrecomputedResult {

    @Id
    @Column(name = "nam")
    private String accession;

    @Column(name = "species")
    private String species;

    /**
     * Model score
     */
    @Column(name = "v1")
    private Double v1;

    /**
     * Model score average
     */
    @Column(name = "avg")
    private Double avg;

    @Column(name = "dis")
    private Double disorder;

    @Column(name = "len")
    private Integer len;

    /**
     * has 30 consecutive disorder AA
     */
    @Column(name = "run")
    private Integer run;

    @Column(name = "max")
    private Integer max;

    /**
     * % charged AA
     */
    @Column(name = "chg")
    private Double chargedAAPercentage;

    /**
     * net charge
     */
    @Column(name = "net")
    private Double netCharged;

    /**
     * GRAVY score
     */
    @Column(name = "gvy")
    private Double gravyScore;

    /**
     * Pi-Pi Pscore
     */
    @Column(name = "pip")
    private Double piPiScore;

    /**
     * TANGO Score
     */
    @Column(name = "tgo")
    private Double tangoScore;

    /**
     * number of MoRFs
     */
    @Column(name = "mfc")
    private Integer morfCount;

    /**
     * number of tango sections
     */
    @Column(name = "sto")
    private Integer tangoSectionCount;

    /**
     * number AA in TANGO sections
     */
    @Column(name = "stc")
    private Integer aaInTangoSection;

    /**
     * RBPPred score
     */
    @Column(name = "rbp")
    private String rbp;

    /**
     * soluprot score
     */
    @Column(name = "sol")
    private Double sol;

    /**
     * catgrangule score
     */
    @Column(name = "cat")
    private Double cat;

    /* Columns that showed up in yeast only */

    @Column(name = "sft")
    private String sft;

    @Column(name = "scn")
    private Double scn;

    @Column(name = "sbb")
    private Double sbb;

    @Column(name = "pol")
    private Double pol;

    /* %composition of given AAs Below*/

    @Column(name = "r")
    private Double r;

    @Column(name = "h")
    private Double h;

    @Column(name = "k")
    private Double k;

    @Column(name = "d")
    private Double d;

    @Column(name = "e")
    private Double e;

    @Column(name = "s")
    private Double s;

    @Column(name = "t")
    private Double t;

    @Column(name = "n")
    private Double n;

    @Column(name = "q")
    private Double q;

    @Column(name = "c")
    private Double c;

    @Column(name = "g")
    private Double g;

    @Column(name = "p")
    private Double p;

    @Column(name = "a")
    private Double a;

    @Column(name = "v")
    private Double v;

    @Column(name = "i")
    private Double i;

    @Column(name = "l")
    private Double l;

    @Column(name = "m")
    private Double m;

    @Column(name = "f")
    private Double f;

    @Column(name = "y")
    private Double y;

    @Column(name = "w")
    private Double w;

}
