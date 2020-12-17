package com.jacobsonmt.mags.server.entities;

import java.time.Instant;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "client", nullable = false)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "job_key", nullable = false)
    private String jobKey;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "input", nullable = false)
    private String inputFASTAContent;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = true;

    @Column(name = "submitted")
    private Instant submittedDate;

    @Column(name = "started")
    private Instant started;

    @Column(name = "finished")
    private Instant finished;

    @Column(name = "email")
    private String email;

    @Column(name = "external_link", nullable = false)
    private String externalLink;

    @Column(name = "email_on_job_submitted", nullable = false)
    private boolean emailOnJobSubmitted;

    @Column(name = "email_on_job_start", nullable = false)
    private boolean emailOnJobStart;

    @Column(name = "email_on_job_complete", nullable = false)
    private boolean emailOnJobComplete;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.SUBMITTED;

    @OneToOne(mappedBy = "job", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Result result;

    private transient Integer position;

    public enum Status {
        SUBMITTED,
        PENDING,
        QUEUED,
        PROCESSING,
        SUCCESS,
        ERROR
    }

}
