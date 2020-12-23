package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
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

@EqualsAndHashCode(of = {"accession"})
@ToString(exclude = "result")
@NoArgsConstructor
@Data
@Entity
@Table(name = "marker_mags")
public class MaGSMarker {

    @Id
    @Column(name = "accession")
    private String accession;

    @Column(name = "label")
    private String label;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "accession")
    private PrecomputedMaGSResult result;

}
