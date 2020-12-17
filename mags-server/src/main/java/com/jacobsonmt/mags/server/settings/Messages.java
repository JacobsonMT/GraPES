package com.jacobsonmt.mags.server.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.messages")
@Getter
@Setter
public class Messages {

    @Getter
    @Setter
    public static class EmailMessages {
        private String submit;
        private String complete;
        private String fail;
    }

    private EmailMessages email;


    private String title;

}