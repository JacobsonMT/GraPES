package com.jacobsonmt.mags.server.services;

import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobScheduler {

    private final JobService jobService;
    private final ApplicationSettings applicationSettings;

    private ScheduledExecutorService scheduler;

    public JobScheduler(JobService jobService,
        ApplicationSettings applicationSettings) {
        this.jobService = jobService;
        this.applicationSettings = applicationSettings;
    }

    /**
     * Initialize JobScheduler:
     */
    @PostConstruct
    private void initialize() {
        log.info( "Job JobScheduler Initialize" );
        scheduler = Executors.newScheduledThreadPool( applicationSettings.getConcurrentJobs() );
        for (int i = 0; i < applicationSettings.getConcurrentJobs(); i++) {
            scheduler.scheduleAtFixedRate(jobService::startJob, 0, 1, TimeUnit.SECONDS );
        }
    }

    @PreDestroy
    public void destroy() {
        log.info( "JobScheduler destroyed" );
        scheduler.shutdownNow();
    }


}
