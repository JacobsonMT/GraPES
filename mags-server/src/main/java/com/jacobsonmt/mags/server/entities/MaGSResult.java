package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class MaGSResult {

    @Column(name="score")
    private Double score;

    @Column(name="z_score")
    private Double zScore;

    @Column(name="diso")
    private Double diso;

    @Column(name="abd")
    private Double abd;

    @Column(name="csl")
    private Double csl;

    @Column(name="int")
    private Integer inte;

    @Column(name="len")
    private Integer len;

    @Column(name="max")
    private Integer max;

    @Column(name="phs")
    private Integer phs;

    @Column(name="pip")
    private Double pip;

    @Column(name="rna")
    private Integer rna;

    @Column(name="sg")
    private Boolean sg;

    @Column(name="mrf")
    private Integer mrf;

    @Column(name="lps")
    private Double lps;

    @Column(name="cat")
    private Double cat;

    @Column(name="tgo")
    private Integer tgo;

    @Column(name="gvy")
    private Double gvy;

    @Column(name="a")
    private Double a;

    @Column(name="c")
    private Double c;

    @Column(name="d")
    private Double d;

    @Column(name="e")
    private Double e;

    @Column(name="f")
    private Double f;

    @Column(name="g")
    private Double g;

    @Column(name="h")
    private Double h;

    @Column(name="i")
    private Double i;

    @Column(name="k")
    private Double k;

    @Column(name="l")
    private Double l;

    @Column(name="m")
    private Double m;

    @Column(name="n")
    private Double n;

    @Column(name="p")
    private Double p;

    @Column(name="q")
    private Double q;

    @Column(name="r")
    private Double r;

    @Column(name="s")
    private Double s;

    @Column(name="t")
    private Double t;

    @Column(name="v")
    private Double v;

    @Column(name="w")
    private Double w;

    @Column(name="y")
    private Double y;

}
