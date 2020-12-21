package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.entities.Job;
import javax.mail.MessagingException;

public interface EmailService {

    void sendJobStartMessage( Job job ) throws MessagingException;
    void sendJobCompletionMessage( Job job ) throws MessagingException;
}