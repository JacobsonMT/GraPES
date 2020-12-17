package com.jacobsonmt.mags.server.services.mail;

import com.jacobsonmt.mags.server.model.JobDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Profile("!mail")
@Service
public class NoopMailService implements EmailService {


    @Override
    public void sendJobSubmittedMessage(JobDO job) {
        log.debug("Dummy implementation, no e-mail is being sent");
    }

    @Override
    public void sendJobStartMessage(JobDO job) {
        log.debug("Dummy implementation, no e-mail is being sent");
    }

    @Override
    public void sendJobCompletionMessage(JobDO job) {
        log.debug("Dummy implementation, no e-mail is being sent");
    }
}