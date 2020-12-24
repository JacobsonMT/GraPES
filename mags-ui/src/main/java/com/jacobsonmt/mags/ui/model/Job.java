package com.jacobsonmt.mags.ui.model;

import com.jacobsonmt.mags.ui.model.result.MaGSSeqResult;
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
    private Species species;
    private String status;
    private String message;
    private Instant started;
    private Instant finished;
//    private String email;
//    private String externalLink;
    private Instant createdDate;;
    private MaGSSeqResult result;

}
