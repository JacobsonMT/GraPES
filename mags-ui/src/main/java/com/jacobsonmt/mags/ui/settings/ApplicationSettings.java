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

    private String processServerHost;
    private String processServerURI;
    private String clientId;
    private String clientToken;

    private boolean disableEmails = false;


}