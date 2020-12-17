package com.jacobsonmt.mags.ui.services.mail;

import com.jacobsonmt.mags.ui.settings.ApplicationSettings;
import com.jacobsonmt.mags.ui.settings.SiteSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Profile("mail")
@Service
public class JavaMailService implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    SiteSettings siteSettings;

    @Autowired
    ApplicationSettings applicationSettings;

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

    public void sendSupportMessage( String message, String name, String email, HttpServletRequest request,
                                    MultipartFile attachment ) throws MessagingException {
        StringBuilder content = new StringBuilder();
        content.append( "<p>Name: " + name + "</p>" );
        content.append( "<p>Email: " + email + "</p>" );
        content.append( "<p>User-Agent: " + request.getHeader( "User-Agent" ) + "</p>" );
        content.append( "<p>Message: " + message + "</p>" );
        boolean hasAttachment = (attachment != null && !Objects.equals( attachment.getOriginalFilename(), "" ));
        content.append( "<p>File Attached: " + hasAttachment + "</p>" );

        sendMessage( siteSettings.getTitle() + " Help - Contact Support", content.toString(), siteSettings.getContactEmail(), hasAttachment ? attachment : null );
    }

}