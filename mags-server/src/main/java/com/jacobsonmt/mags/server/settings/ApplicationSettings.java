package com.jacobsonmt.mags.server.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.settings")
@Getter
@Setter
public class ApplicationSettings {

    private String command;
    private String jobsDirectory;
    private String outputCSVFilename;
    private String inputFASTAFilename;
    private String jobSerializationFilename;
    private boolean loadJobsFromDisk;

    private int maxCachedJobs = 1000;
    private int concurrentJobs = 1;
    private boolean purgeSavedJobs = true;
    private int purgeSavedJobsTimeHours = 1;
    private int purgeAfterHours = 24;
    private boolean disableEmails = false;

}
