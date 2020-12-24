package com.jacobsonmt.mags.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(of = "key")
@Data
@Entity
@Table(name = "job_config")
public class JobConfig {

    public static final String DEFAULT_STATUS = "DEFAULT_STATUS";
    public static final String DEFAULT_MESSAGE = "DEFAULT_MESSAGE";

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private String value;

}
