package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import com.jacobsonmt.mags.server.settings.SiteSettings;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Profile("javamail")
@Service
public class JavaMailService implements EmailService {

    private final JavaMailSender emailSender;

    private final SiteSettings siteSettings;

    private final ApplicationSettings applicationSettings;

    private final TemplateService templateService;

    public JavaMailService(JavaMailSender emailSender,
        SiteSettings siteSettings, ApplicationSettings applicationSettings, TemplateService templateService) {
        this.emailSender = emailSender;
        this.siteSettings = siteSettings;
        this.applicationSettings = applicationSettings;
        this.templateService = templateService;
    }

    private void sendMessage( String subject, String content, String to ) throws MessagingException {
        sendMessage( subject, content, to, null );
    }

    private void sendMessage( String subject, String content, String to, MultipartFile attachment ) throws MessagingException {

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper( message, true );

        helper.setSubject( subject );
        helper.setText( content, true );
        helper.setTo( to );
        helper.setFrom( siteSettings.getFromEmail() );

        if ( attachment != null ) {
            helper.addAttachment( attachment.getOriginalFilename(), attachment );
        }

        if ( !applicationSettings.isDisableEmails() ) {
            emailSender.send( message );
        }

    }

    public void sendJobStartMessage( Job job ) throws MessagingException {
        if (applicationSettings.isDisableStartEmails() || applicationSettings.isDisableEmails()) {
            log.warn("Tried sending job start email but job start emails are disabled");
            return;
        }
        if (StringUtils.isEmpty(job.getEmail())) {
            log.warn("Tried sending job start email with empty or null email");
            return;
        }

        sendMessage(
            siteSettings.getTitle() + " - Job Started",
            templateService.generateJobStartedContent(job),
            job.getEmail() );
    }


    public void sendJobCompletionMessage( Job job ) throws MessagingException {
        if (applicationSettings.isDisableEmails()) {
            log.warn("Tried sending job finish email but emails are disabled");
            return;
        }
        if (StringUtils.isEmpty(job.getEmail())) {
            log.warn("Tried sending job complete email with empty or null email");
            return;
        }


        sendMessage(
            siteSettings.getTitle() + " - Job Completed",
            templateService.generateJobFinishedContent(job),
            job.getEmail() );
    }


}