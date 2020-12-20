package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "precomputed")
public class PrecomputedResult {

    public enum Species {
        HUMAN("Human"),
        YEAST("Yeast");

        private String label;

        Species(String label) {
            this.label = label;
        }
    }

    @Id
    @Column(name = "accession")
    private String accession;

    @Enumerated(EnumType.STRING)
    @Column(name = "species")
    private Species species;

    @Column(name = "marker")
    private boolean marker;

    @Column(name = "mags_score")
    private Double magsScore;

    @Column(name = "mags_z_score")
    private Double magsZScore;

    @Column(name = "diso")
    private Double diso;

    @Column(name = "abd")
    private Double abd;

    @Column(name = "csl")
    private Double csl;

//    @Column(name = "int")
//    private Integer int;

    @Column(name = "len")
    private Integer len;

    @Column(name = "max")
    private Integer max;

    @Column(name = "phs")
    private Integer phs;

    @Column(name = "pip")
    private Double pip;

    @Column(name = "rna")
    private Integer rna;

    @Column(name = "mrf")
    private Integer mrf;

    @Column(name = "lps")
    private Double lps;

    @Column(name = "cat")
    private Double cat;

    @Column(name = "tgo")
    private Double tgo;

    @Column(name = "gvy")
    private Double gvy;

    @Column(name = "a")
    private Double a;

    @Column(name = "c")
    private Double c;

    @Column(name = "d")
    private Double d;

    @Column(name = "e")
    private Integer e;

    @Column(name = "f")
    private Double f;

    @Column(name = "g")
    private Double g;

    @Column(name = "h")
    private Integer h;

    @Column(name = "i")
    private Double i;

    @Column(name = "k")
    private Double k;

    @Column(name = "l")
    private Double l;

    @Column(name = "m")
    private Double m;

    @Column(name = "n")
    private Double n;

    @Column(name = "p")
    private Double p;

    @Column(name = "q")
    private Double q;

    @Column(name = "r")
    private Double r;

    @Column(name = "s")
    private Double s;

    @Column(name = "t")
    private Double t;

    @Column(name = "v")
    private Double v;

    @Column(name = "w")
    private Double w;

    @Column(name = "y")
    private Integer y;

}
