package com.jacobsonmt.mags.ui.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;

@Component
@ConfigurationProperties(prefix = "application.site")
@Getter
@Setter
public class SiteSettings {

    private String title;
    private String subtitle;

    private String host;
    private String context;

    private String apiSite;
    private String precomputedSite;

    @Email
    private String contactEmail;
    @Email
    private String fromEmail;

    public String getFullUrl() {
        return host + context + (context.endsWith( "/" ) ? "" : "/");
    }

}
