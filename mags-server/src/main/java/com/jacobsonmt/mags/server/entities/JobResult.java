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

}
