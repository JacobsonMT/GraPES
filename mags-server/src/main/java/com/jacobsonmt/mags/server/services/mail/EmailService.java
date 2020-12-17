package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.model.JobDO;
import javax.mail.MessagingException;

public interface EmailService {

    void sendJobSubmittedMessage( JobDO job ) throws MessagingException;
    void sendJobStartMessage( JobDO job ) throws MessagingException;
    void sendJobCompletionMessage( JobDO job ) throws MessagingException;
}