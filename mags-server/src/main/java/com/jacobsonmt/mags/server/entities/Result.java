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
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = "job")
@NoArgsConstructor
@Data
@Entity
@Table(name = "result")
public class Result {

    @Id
    private long id;

    // Not yet lazy, see https://hibernate.atlassian.net/browse/HHH-10771
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id")
    private Job job;

    @Column(name = "executionTime", nullable = false)
    private long executionTime;

    @Column(name = "accession", nullable = false)
    private String accession;

    @Column(name = "result_file_path", nullable = false)
    private String resultFilePath;

}
