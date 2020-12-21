package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of={"accession"}, callSuper = false)
@NoArgsConstructor
@Data
@Entity
@Table(name = "precomputed_mags")
public class PrecomputedMaGSResult extends MaGSResult {

    @Id
    @Column(name = "accession")
    private String accession;

    @Enumerated(EnumType.STRING)
    @Column(name = "species")
    private Species species;

    @Column(name = "marker")
    private boolean marker;

}
