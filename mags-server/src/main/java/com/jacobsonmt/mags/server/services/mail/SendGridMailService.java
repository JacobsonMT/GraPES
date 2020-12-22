package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import com.jacobsonmt.mags.server.settings.SiteSettings;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import javax.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Profile("sendgridmail")
@Service
public class SendGridMailService implements EmailService {

    private final SendGrid sendGrid;

    private final SiteSettings siteSettings;

    private final ApplicationSettings applicationSettings;

    private final TemplateService templateService;

    public SendGridMailService(
        SendGrid sendGrid,
        SiteSettings siteSettings,
        ApplicationSettings applicationSettings,
        TemplateService templateService) {
        this.sendGrid = sendGrid;
        this.siteSettings = siteSettings;
        this.applicationSettings = applicationSettings;
        this.templateService = templateService;
    }

    private void sendMail(Mail mail) throws MessagingException {
        Request request = new Request();
        Response response;
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = sendGrid.api(request);
        } catch (IOException e) {
            throw new MessagingException("Issue sending SendGrid mail", e);
        }

        if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
            throw new MessagingException("Received Invalid response from SendGrid API: "
                + response.getBody());
        }
    }

    @Override
    public void sendJobStartMessage(Job job) throws MessagingException {
        if (applicationSettings.isDisableStartEmails() || applicationSettings.isDisableEmails()) {
            log.warn("Tried sending job start email but job start emails are disabled");
            return;
        }
        if (StringUtils.isEmpty(job.getEmail())) {
            log.warn("Tried sending job start email with empty or null email");
            return;
        }

        Email from = new Email(siteSettings.getFromEmail(), siteSettings.getTitle());
        String subject = siteSettings.getTitle() + " - Job Started";
        Email to = new Email(job.getEmail());
        Content content = new Content("text/html", templateService.generateJobStartedContent(job));
        sendMail(new Mail(from, subject, to, content));
        log.info("Job start email sent for job: {}", job.getId());
    }

    @Override
    public void sendJobCompletionMessage(Job job) throws MessagingException {
        if (applicationSettings.isDisableEmails()) {
            log.warn("Tried sending job finish email but emails are disabled");
            return;
        }
        if (StringUtils.isEmpty(job.getEmail())) {
            log.warn("Tried sending job complete email with empty or null email");
            return;
        }

        Email from = new Email(siteSettings.getFromEmail(), siteSettings.getTitle());
        String subject = siteSettings.getTitle() + " - Job Completed";
        Email to = new Email(job.getEmail());
        Content content = new Content("text/html", templateService.generateJobFinishedContent(job));
        sendMail(new Mail(from, subject, to, content));
        log.info("Job Finish email sent for job: {}", job.getId());
    }
}
