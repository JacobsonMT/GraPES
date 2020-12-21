package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import com.jacobsonmt.mags.server.settings.ClientSettings;
import com.jacobsonmt.mags.server.settings.SiteSettings;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Profile("mail")
@Service
public class JavaMailService implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    SiteSettings siteSettings;

    @Autowired
    ApplicationSettings applicationSettings;

    @Autowired
    ClientSettings clientSettings;

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
        if (StringUtils.isEmpty(job.getEmail())) {
            log.warn("Tried sending job start email with empty or null email");
            return;
        }

        String jobUrl = job.getExternalLink() + job.getId();

        StringBuilder content = new StringBuilder();
        content.append( "<p>Your job has started processing!</p>" );
        content.append( "<p>The job labelled <strong>" + job.getLabel() + "</strong> submitted on <strong>" + job.getCreatedDate() + "</strong> has begun processing.</p>" );
        content.append( "<p>You can view its progress and/or results here: <a href='" + jobUrl + "' target='_blank'>" + jobUrl + "</a>.</p>" );
        content.append( "<p>We will notify you when the job has completed.</p>" );

        content.append( "<hr style='margin-top: 50px;'><p><small>THIS IS AN AUTOMATED MESSAGE - PLEASE DO NOT REPLY DIRECTLY TO THIS EMAIL</small></p>" );
        sendMessage(  siteSettings.getTitle() + " - Job Started",
                content.toString(),
                job.getEmail() );
    }

    public void sendJobCompletionMessage( Job job ) throws MessagingException {
        if (StringUtils.isEmpty(job.getEmail())) {
            log.warn("Tried sending job complete email with empty or null email");
            return;
        }

        String jobUrl = job.getExternalLink() + job.getId();

        StringBuilder content = new StringBuilder();
        content.append( "<p>Your job has completed!</p>" );
        content.append( "<p>The job labelled <strong>" + job.getLabel() + "</strong> submitted on <strong>" + job.getCreatedDate() + "</strong> has completed.</p>" );
        content.append( "<p>You can view its results here: <a href='" +  jobUrl + "' target='_blank'>" +  jobUrl + "</a>.</p>" );
        content.append( "<hr style='margin-top: 50px;'><p><small>THIS IS AN AUTOMATED MESSAGE - PLEASE DO NOT REPLY DIRECTLY TO THIS EMAIL</small></p>" );
        sendMessage(  siteSettings.getTitle() + " - Job Completed",
                content.toString(),
                job.getEmail() );
    }

}