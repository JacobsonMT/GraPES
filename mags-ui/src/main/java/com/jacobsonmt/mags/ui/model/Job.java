package com.jacobsonmt.mags.ui.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.util.Date;

@Log4j2
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"jobId", "label", "hidden"})
@EqualsAndHashCode(of = {"jobId"})
public class Job {

    // Information on creation of job
    private String jobId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private String clientId;
    private String label;
    private String status;
    private boolean running;
    private boolean failed;
    private boolean complete;
    private Integer position;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private boolean hidden;
    private Date submittedDate;
    private Date startedDate;
    private Date finishedDate;
    private String inputFASTAContent;
    private JobResult result;
    private long executionTime;

    public static String obfuscateEmail( String email ) {
        return email.replaceAll( "(\\w{0,3})(\\w+.*)(@.*)", "$1****$3" );
    }

}
