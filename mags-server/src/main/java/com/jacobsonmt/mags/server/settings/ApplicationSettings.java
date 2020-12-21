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

    private int concurrentJobs = 1;
    private boolean disableEmails = false;
    private int jobPollSeconds = 5;

}
