package com.jacobsonmt.mags.ui.model;

import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = {"id", "label"})
@EqualsAndHashCode(of = {"id"})
@Data
public class Job {
    private long id;
    private String session;
    private String label;
    private String input;
    private String species;
    private Status status;
    private String message;
    private Instant started;
    private Instant finished;
//    private String email;
    private String externalLink;
    private Instant createdDate;;
//    private JobResult result;

    public static String obfuscateEmail( String email ) {
        return email.replaceAll( "(\\w{0,3})(\\w+.*)(@.*)", "$1****$3" );
    }

    public enum Status {
        SUBMITTED,
        PROCESSING,
        SUCCESS,
        ERROR,
        STOPPED,
        VALIDATION_ERROR
    }

}
