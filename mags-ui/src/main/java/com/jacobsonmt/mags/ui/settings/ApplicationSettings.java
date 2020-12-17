package com.jacobsonmt.mags.ui.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.settings")
@Getter
@Setter
public class ApplicationSettings {

    private String processServerURI;
    private String clientId;
    private String clientToken;

    private int userProcessLimit = 2;
    private int userJobLimit = 200;

    private boolean disableEmails = false;
    private boolean emailOnJobSubmitted = true;
    private boolean emailOnJobStart = true;
    private boolean emailOnJobComplete = true;

}