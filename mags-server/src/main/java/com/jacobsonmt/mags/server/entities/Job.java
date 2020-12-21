package com.jacobsonmt.mags.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Data
@Entity
@Where(clause="deleted=false")
@Table(name = "job")
public class Job extends Auditable {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "session", nullable = false)
    private String session;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "input", nullable = false)
    private String input;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false)
    private Species species = Species.HUMAN;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.SUBMITTED;

    @Column(name = "message")
    private String message;

    @Column(name = "started")
    private Instant started;

    @Column(name = "finished")
    private Instant finished;

    /* Options */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "email")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "external_link", nullable = false)
    private String externalLink;

//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
//    @JoinColumn(name="result_id")
//    private Result result;

    @OneToOne(mappedBy = "job", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "result_id", referencedColumnName = "id")
    private JobResult result;

    public enum Status {
        SUBMITTED,
        PROCESSING,
        SUCCESS,
        ERROR,
        STOPPED,
        VALIDATION_ERROR
    }

    @JsonIgnore
    public String recreateFASTA() {
        return ">" + label + System.lineSeparator() + input + System.lineSeparator();
    }

}
