package com.jacobsonmt.mags.server.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.settings.jobs")
@Getter
@Setter
public class JobSettings {

    private String rootPath;
    private String inputPath;
    private String outputPath;
    private String outputFeatureFile;
    private String outputScoreFile;
    private String command;

}
