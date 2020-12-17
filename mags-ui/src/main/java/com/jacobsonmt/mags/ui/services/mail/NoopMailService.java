package com.jacobsonmt.mags.ui.services.mail;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Profile("!mail")
@Service
public class NoopMailService implements EmailService {

    @Override
    public void sendSupportMessage(String message, String name, String email, HttpServletRequest request,
        MultipartFile attachment) {
        log.debug("Dummy implementation, no e-mail is being sent");
    }
}