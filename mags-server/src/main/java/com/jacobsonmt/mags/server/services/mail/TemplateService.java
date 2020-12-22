package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.settings.SiteSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class TemplateService {

    @Autowired
    SiteSettings siteSettings;

    @Autowired
    private TemplateEngine templateEngine;

    public String generateJobStartedContent(Job job) {
        Context context = createBaseContent(job);
        context.setVariable("title", "Job Started");
        context.setVariable("state", "started");

        return templateEngine.process("job", context);
    }

    public String generateJobFinishedContent(Job job) {
        Context context = createBaseContent(job);
        context.setVariable("title", "Job Complete");
        context.setVariable("state", "finished");

        return templateEngine.process("job", context);
    }

    private Context createBaseContent(Job job) {
        Context context = new Context();
        context.setVariable("job", job);
        context.setVariable("createdDate", job.getCreatedDate());
        context.setVariable("jobLabel", job.getLabel());
        context.setVariable("jobLink", job.getExternalLink() + job.getId());
        context.setVariable("logo", siteSettings.getLogoUrl());

        return context;
    }

}
