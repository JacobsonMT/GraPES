package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class MaGSSeqResult {

    @Column(name="score")
    private Double score;

    @Column(name="z_score")
    private Double zScore;

    @Column(name="diso")
    private Double diso;

    @Column(name="len")
    private Integer len;

    @Column(name="run")
    private Integer run;

    @Column(name="max")
    private Integer max;

    @Column(name="chg")
    private Double chg;

    @Column(name="net")
    private Double net;

    @Column(name="gvy")
    private Double gvy;

    @Column(name="pip")
    private Double pip;

    @Column(name="tgo")
    private Double tgo;

    @Column(name="mfc")
    private Double mfc;

    @Column(name="sto")
    private Integer sto;

    @Column(name="stc")
    private Integer stc;

//    @Column(name="sft")
//    private varchar sft;

    @Column(name="scn")
    private Double scn;

    @Column(name="sbb")
    private Double sbb;

    @Column(name="pol")
    private Double pol;

    @Column(name="rbp")
    private Double rbp;

    @Column(name="sol")
    private Double sol;

    @Column(name="cat")
    private Double cat;

    @Column(name="r")
    private Double r;

    @Column(name="h")
    private Double h;

    @Column(name="k")
    private Double k;

    @Column(name="d")
    private Double d;

    @Column(name="e")
    private Double e;

    @Column(name="s")
    private Double s;

    @Column(name="t")
    private Double t;

    @Column(name="n")
    private Double n;

    @Column(name="q")
    private Double q;

    @Column(name="c")
    private Double c;

    @Column(name="g")
    private Double g;

    @Column(name="p")
    private Double p;

    @Column(name="a")
    private Double a;

    @Column(name="v")
    private Double v;

    @Column(name="i")
    private Double i;

    @Column(name="l")
    private Double l;

    @Column(name="m")
    private Double m;

    @Column(name="f")
    private Double f;

    @Column(name="y")
    private Double y;

    @Column(name="w")
    private Double w;

}
