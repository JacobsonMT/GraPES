package com.jacobsonmt.mags.ui.services.mail;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface EmailService {

    void sendSupportMessage( String message, String name, String email, HttpServletRequest request,
        MultipartFile attachment ) throws MessagingException;
}