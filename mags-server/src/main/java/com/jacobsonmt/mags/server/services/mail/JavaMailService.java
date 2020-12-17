package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.model.JobDO;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import com.jacobsonmt.mags.server.settings.ClientSettings;
import com.jacobsonmt.mags.server.settings.SiteSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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

    public void sendJobSubmittedMessage( JobDO job ) throws MessagingException {
        if ( job.getEmail() == null || job.getEmail().isEmpty() ) {
            return;
        }

        String clientName = clientSettings.getClients().get( job.getClientId() ).getName();
        String jobUrl = job.getEmailJobLinkPrefix() + job.getJobId();

        StringBuilder content = new StringBuilder();
        content.append( "<p>Your job has been submitted!</p>" );
        content.append( "<p>The job labelled <strong>" + job.getLabel() + "</strong> has been submitted to <strong>" + clientName + "</strong>.</p>" );
        content.append( "<p>You can view its progress and/or results here: <a href='" + jobUrl + "' target='_blank'>" + jobUrl + "</a>.</p>" );
        if ( job.isEmailOnJobStart() && job.isEmailOnJobComplete() ) {
            content.append( "<p>We will notify you when the job has begun processing and when it has completed.</p>" );
        } else if ( job.isEmailOnJobStart() ) {
            content.append( "<p>We will notify you when the job has begun processing.</p>" );
        } else if ( job.isEmailOnJobComplete() ) {
            content.append( "<p>We will notify you when the job has completed.</p>" );
        }
        content.append( "<hr style='margin-top: 50px;'><p><small>THIS IS AN AUTOMATED MESSAGE - PLEASE DO NOT REPLY DIRECTLY TO THIS EMAIL</small></p>" );
        sendMessage(  clientName + " - Job Submitted",
                content.toString(),
                job.getEmail() );
    }

    public void sendJobStartMessage( JobDO job ) throws MessagingException {
        if ( job.getEmail() == null || job.getEmail().isEmpty() ) {
            return;
        }

        String clientName = clientSettings.getClients().get( job.getClientId() ).getName();
        String jobUrl = job.getEmailJobLinkPrefix() + job.getJobId();

        StringBuilder content = new StringBuilder();
        content.append( "<p>Your job has started processing!</p>" );
        content.append( "<p>The job labelled <strong>" + job.getLabel() + "</strong> submitted on <strong>" + job.getSubmittedDate() + "</strong> has begun processing.</p>" );
        content.append( "<p>You can view its progress and/or results here: <a href='" + jobUrl + "' target='_blank'>" + jobUrl + "</a>.</p>" );
        if ( job.isEmailOnJobComplete() ) {
            content.append( "<p>We will notify you when the job has completed.</p>" );
        }
        content.append( "<hr style='margin-top: 50px;'><p><small>THIS IS AN AUTOMATED MESSAGE - PLEASE DO NOT REPLY DIRECTLY TO THIS EMAIL</small></p>" );
        sendMessage(  clientName + " - Job Started",
                content.toString(),
                job.getEmail() );
    }

    public void sendJobCompletionMessage( JobDO job ) throws MessagingException {
        if ( job.getEmail() == null || job.getEmail().isEmpty() ) {
            return;
        }

        String clientName = clientSettings.getClients().get( job.getClientId() ).getName();
        String jobUrl = job.getEmailJobLinkPrefix() + job.getJobId();

        StringBuilder content = new StringBuilder();
        content.append( "<p>Your job has completed!</p>" );
        content.append( "<p>The job labelled <strong>" + job.getLabel() + "</strong> submitted on <strong>" + job.getSubmittedDate() + "</strong> has completed.</p>" );
        content.append( "<p>You can view its results here: <a href='" + jobUrl + "' target='_blank'>" + jobUrl + "</a>.</p>" );
        content.append( "<hr style='margin-top: 50px;'><p><small>THIS IS AN AUTOMATED MESSAGE - PLEASE DO NOT REPLY DIRECTLY TO THIS EMAIL</small></p>" );
        sendMessage(  clientName + " - Job Completed",
                content.toString(),
                job.getEmail() );
    }

}