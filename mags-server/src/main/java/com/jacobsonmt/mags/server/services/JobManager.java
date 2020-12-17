package com.jacobsonmt.mags.server.services;


import com.jacobsonmt.mags.server.model.FASTASequence;
import com.jacobsonmt.mags.server.model.JobDO;
import com.jacobsonmt.mags.server.model.JobDO.JobVO;
import com.jacobsonmt.mags.server.model.JobResult;
import com.jacobsonmt.mags.server.repositories.JobRepository;
import com.jacobsonmt.mags.server.services.mail.EmailService;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import com.jacobsonmt.mags.server.settings.ClientSettings;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class JobManager {

    private final ApplicationSettings applicationSettings;

    private final ClientSettings clientSettings;

    private final EmailService emailService;

    private final JobRepository jobRepository;

    // Main executor to process jobs
    @Setter
    private ExecutorService executor;

    // Contains a copy of the processing queue of jobs internal to executor.
    // It is non-trivial to extract a list of running/waiting jobs in the executor
    // so we maintain a copy in sync with the real thing.
    private final Set<JobDO> jobQueueMirror = new LinkedHashSet<>();

    // Secondary client queues or waiting lines. One specific to each client.
    private Map<String, Queue<JobDO>> clientQueues = new ConcurrentHashMap<>();

    // Tertiary user queues or waiting lines. One specific to each user in each client.
    private Map<String, Queue<JobDO>> userQueues = new ConcurrentHashMap<>();

    public JobManager(
        ApplicationSettings applicationSettings, ClientSettings clientSettings, EmailService emailService,
        JobRepository jobRepository) {
        this.applicationSettings = applicationSettings;
        this.clientSettings = clientSettings;
        this.emailService = emailService;
        this.jobRepository = jobRepository;
    }

    private static String userQueueKey(JobDO job) {
        return job.getClientId() + "-" + job.getUserId();
    }

    // Stored approximate number of completed jobs for each clientId. Can be used to test when to update during polling.
    private final Map<String, AtomicInteger> completionCounts = new ConcurrentHashMap<>();

    // Used to periodically purge the old saved jobs
    private ScheduledExecutorService scheduler;

    private static final int MAX_DEFAULT_LABEL_SIZE = 20;

    /**
     * Initialize Job Manager:
     *
     * Create and schedule process queue executor.
     * Load previously completed jobs from disk if enabled.
     */
    @PostConstruct
    private void initialize() {
        log.info( "Job Manager Initialize" );
        executor = Executors.newFixedThreadPool( applicationSettings.getConcurrentJobs() );
    }

    @PreDestroy
    public void destroy() {
        log.info( "JobManager destroyed" );
        executor.shutdownNow();
        scheduler.shutdownNow();
    }


    /**
     * Create job with specified parameters but do not submit it to any queue.
     *
     * @param clientId Client of the job.
     * @param userId User from client.
     * @param label Short label/name for the job.
     * @param sequence Input sequence data used to run the job.
     * @param email Email to be used for notifications if enabled.
     * @param hidden Is job private.
     * @param emailJobLinkPrefix Link prefix used to create the job url in client
     * @param emailOnJobSubmitted Send email when job is submitted to the queueing system.
     * @param emailOnJobStart Send email when job is started.
     * @param emailOnJobComplete Send email when job is complete.
     * @return Created job.
     */
    public JobDO createJob( String clientId,
                              String userId,
                              String label,
                              FASTASequence sequence,
                              String email,
                              boolean hidden,
                              String emailJobLinkPrefix,
                              boolean emailOnJobSubmitted,
                              boolean emailOnJobStart,
                              boolean emailOnJobComplete ) {
        JobDO.JobDOBuilder jobBuilder = JobDO.builder();

        // Generated
        String jobId = UUID.randomUUID().toString();
        jobBuilder.jobId( jobId );

        // Static Resources
        jobBuilder.command( applicationSettings.getCommand() );
        jobBuilder.jobsDirectory( Paths.get( applicationSettings.getJobsDirectory(), jobId) );
        jobBuilder.outputCSVFilename( applicationSettings.getOutputCSVFilename() );
        jobBuilder.inputFASTAFilename( applicationSettings.getInputFASTAFilename() );
        jobBuilder.jobSerializationFilename( applicationSettings.getJobSerializationFilename() );

        // User Inputs
        jobBuilder.clientId( clientId );
        jobBuilder.userId( userId );
        jobBuilder.label( Strings.isNotBlank( label ) ? label :
                sequence.getHeader().substring( 0, Math.min( sequence.getHeader().length(), MAX_DEFAULT_LABEL_SIZE ) ) );
        jobBuilder.inputFASTAContent( sequence.getFASTAContent() );
        jobBuilder.hidden( hidden );
        jobBuilder.email( email );
        jobBuilder.emailJobLinkPrefix( emailJobLinkPrefix );
        jobBuilder.emailOnJobSubmitted( emailOnJobSubmitted );
        jobBuilder.emailOnJobStart( emailOnJobStart );
        jobBuilder.emailOnJobComplete( emailOnJobComplete );

        return jobBuilder.build();

    }

    public List<JobDO> createJobs( String clientId,
                                     String userId,
                                     String label,
                                     Set<FASTASequence> sequences,
                                     String email,
                                     boolean hidden,
                                     String emailJobLinkPrefix,
                                     boolean emailOnJobSubmitted,
                                     boolean emailOnJobStart,
                                     boolean emailOnJobComplete ) {

        List<JobDO> jobs = new ArrayList<>();


        for ( FASTASequence sequence : sequences ) {
            JobDO job = createJob(
                    clientId,
                    userId,
                    label,
                    sequence,
                    email,
                    hidden,
                    emailJobLinkPrefix,
                    emailOnJobSubmitted,
                    emailOnJobStart,
                    emailOnJobComplete );

            if ( !sequence.getValidationStatus().isEmpty() ) {
                job.setComplete( true );
                job.setFailed( true );
                job.setPosition( null ); // Mainly for sort order in tables
                job.setStatus( sequence.getValidationStatus() );
                jobRepository.cacheJob( job ); // So the job's validation status can be queried
                log.info( "Validation error: " + job.getJobId() + " - " + job.getStatus() );
            }

            jobs.add( job );

        }

        return jobs;
    }

    /**
     * Submit job to executor for process queue.
     *
     * Synchronized on jobQueueMirror.
     *
     * @param job
     */
    private void submitToProcessQueue( JobDO job ) {
        synchronized ( jobQueueMirror ) {
            log.info( "Submitting job (" + job.getJobId() + ") for client-user: (" + userQueueKey(job) + ") to process queue" );
            job.setSubmittedDate( new Date() );
            job.setOnJobStart(this::onJobStart);
            job.setOnJobComplete(this::onJobComplete);

            jobQueueMirror.add( job );
            job.setPosition( (int) jobQueueMirror.stream().filter( j -> !j.isRunning() ).count() );
            job.setStatus( "Position: " + job.getPosition() );

            Future<JobResult> future = executor.submit( job );
            job.setFuture( future );

        }
    }

    /**
     * For a specific client, submit the job at the top of their personal queue to the process queue if there is room left in their
     * allocated process queue limit.
     *
     * @param clientId specific client
     */
    private void submitTopOfClientQueue( String clientId ) {
        int cnt = 0;
        synchronized ( jobQueueMirror ) {

            for ( JobDO job : jobQueueMirror ) {
                if ( job.getClientId().equals( clientId ) ) cnt++;
            }
            log.debug( "Found {} existing jobs for client {} in processing queue", cnt, clientId);
        }

        if ( cnt < clientSettings.getClients().get( clientId ).getProcessLimit() ) {

            Queue<JobDO> jobs = clientQueues.get( clientId );

            if ( jobs != null ) {
                JobDO job;
                synchronized ( jobs ) {
                    job = jobs.poll();
                }
                if ( job != null ) {
                    submitToProcessQueue( job );
                }
            }
        }

    }

    /**
     * Add job to personal queue of owning client if there is room in their total job allocation limit.
     *
     * @param job
     */
    private void submitToClientQueue( JobDO job ) {

        Queue<JobDO> jobs = clientQueues.computeIfAbsent( job.getClientId(), k -> new LinkedList<>() );

        log.debug( "Found {} existing jobs for client {} in client queue", jobs.size(), job.getClientId());

        if ( jobs.size() >= clientSettings.getClients().get( job.getClientId() ).getJobLimit() ) {
            log.info( "Too many jobs in client queue, failed to submit job ({}) for client-user: ({})",
                    job.getJobId(), userQueueKey(job));
            return;
        }

        synchronized ( jobs ) {

            if ( !jobs.contains( job ) ) {
                log.info( "Submitting job (" + job.getJobId() + ") for client-user: (" + userQueueKey(job) + ") to client queue" );
                jobs.add( job );
                job.setStatus( "Queued..." );
                submitTopOfClientQueue( job.getClientId() );
            }
        }
    }

    /**
     * For a specific user, submit the job at the top of their personal queue to their owning client queue
     * if there is room left in their allocated client queue limit.
     *
     * @param userQueueKey key to user queue map for user
     */
    private void submitTopOfUserQueue( String userQueueKey ) {

        Queue<JobDO> jobs = userQueues.get( userQueueKey );

        if ( jobs != null ) {
            JobDO job = jobs.peek();

            if ( job != null ) {

                Queue<JobDO> clientQueue = clientQueues.computeIfAbsent( job.getClientId(), k -> new LinkedList<>() );

                int cnt = 0;
                synchronized ( clientQueue ) {

                    for ( JobDO j : clientQueue ) {
                        if ( j.getUserId().equals( job.getUserId() ) ) cnt++;
                    }
                    log.debug( "Found {} existing jobs for user {} in client queue {}", cnt, job.getUserId(),
                            job.getClientId() );
                }

                if ( cnt < clientSettings.getClients().get( job.getClientId() ).getUserClientLimit() ) {
                    synchronized ( jobs ) {
                        job = jobs.poll();
                    }
                    submitToClientQueue( job );
                } else {
                    log.debug( "Too many jobs in client queue for user ({}), failed to promote job ({}) for " +
                                    "client-user: ({})", job.getUserId(), job.getJobId(), userQueueKey(job));
                }
            }
        }
    }

    /**
     * Add job to personal queue of owning user if there is room in their total job allocation limit.
     *
     * @param job
     */
    private String submitToUserQueue( JobDO job ) {
        Queue<JobDO> jobs = userQueues.computeIfAbsent( userQueueKey(job), k -> new LinkedList<>() );

        log.debug( "Found {} existing jobs for user {} in user queue", jobs.size(), job.getUserId());

        if ( jobs.size() >= clientSettings.getClients().get( job.getClientId() ).getUserJobLimit() ) {
            log.info( "Too many jobs in user queue, failed to submit job ({}) for client-user: ({})",
                    job.getJobId(), userQueueKey(job));
            return "Too many jobs";
        }

        synchronized ( jobs ) {

            if ( !jobs.contains( job ) ) {
                log.info( "Submitting job (" + job.getJobId() + ") for client-user: (" + userQueueKey(job) + ") to user queue" );
                jobs.add( job );
                job.setStatus( "Pending..." );
                jobRepository.cacheJob( job );
                submitTopOfUserQueue( userQueueKey(job) );
            }
        }
        return "";
    }

    /**
     * Begin process of submitting a job to the queueing system.
     *
     * @param job
     * @return Message for things like validation failure.
     */
    public String submit( JobDO job ) {

        if ( job.isComplete() || job.isFailed() || job.isRunning() ) {
            return "Job already submitted.";
        }

        if ( job.isEmailOnJobSubmitted() && job.getEmail() != null && !job.getEmail().isEmpty() ) {
            try {
                emailService.sendJobSubmittedMessage( job );
            } catch ( Exception e ) {
                log.warn( e );
            }
        }

        return submitToUserQueue( job );
    }

    public JobDO getSavedJob( String jobId ) {
        return jobRepository.getById( jobId );
    }

    public String stopJobs( String clientId, String userId ) {
        List<JobDO> jobs = jobRepository.allJobsForClientAndUser( clientId, userId ).collect( Collectors.toList() );

        if ( jobs.isEmpty() ) {
            return "No jobs found for: " + userId;
        }

        for ( JobDO job : jobs ) {
            stopJob( job );
        }
        return "Jobs deleted for: " + userId;
    }

    public void stopJob( JobDO job ) {
        log.info( "Requesting job stop (" + job.getJobId() + ") for client-user: (" + userQueueKey(job) + ")" );
        // Start at the lowest queue and work our way up to minimize race conditions

        Queue<JobDO> jobs = userQueues.get( userQueueKey(job) );
        if ( jobs != null ) {
            synchronized ( jobs ) {
                if ( jobs.contains( job ) ) {
                    // Not yet submitted, just remove it from user queue
                    jobs.remove( job );
                }
            }
        }

        jobs = clientQueues.get( job.getClientId() );
        if ( jobs != null ) {
            synchronized ( jobs ) {
                if ( jobs.contains( job ) ) {
                    // Not yet submitted, just remove it from client queue
                    jobs.remove( job );
                }
            }
        }

        synchronized ( jobQueueMirror ) {
            if ( jobQueueMirror.contains( job ) ) {
                if ( job.isComplete() ) {
                    jobQueueMirror.remove( job );
                } else if ( job.isRunning() ) {
                    // Disabled for now as it doesn't currently work if job is running
//                    job.getFuture().cancel( true );
//                    jobQueueMirror.remove( job );
                } else {
                    job.getFuture().cancel( true );
                    jobQueueMirror.remove( job );
                }
            }
        }

        // Remove the job from saved cache and disk no matter what so that it becomes inaccessible
        jobRepository.delete( job );

        // Fix any gaps created in the queues, if there are no gaps these do nothing so safe to run in either case
        submitTopOfClientQueue( job.getClientId() );
        submitTopOfUserQueue( userQueueKey( job ) );
    }

    public void onJobStart( JobDO job ) {
        if ( job.isEmailOnJobStart() && job.getEmail() != null && !job.getEmail().isEmpty() ) {
            try {
                emailService.sendJobStartMessage( job );
            } catch ( Exception e ) {
                log.warn( e );
            }
        }

        // Update positions
        synchronized ( jobQueueMirror ) {
            int idx = 1;
            for ( JobDO j : jobQueueMirror ) {
                if ( !j.isRunning() ) {
                    j.setPosition( idx );
                    j.setStatus( "Position: " + idx );
                    idx++;
                }
            }
        }
    }

    public void onJobComplete( JobDO job ) {
        job.setSaveExpiredDate( System.currentTimeMillis() + applicationSettings.getPurgeAfterHours() * 60 * 60 * 1000 );
        if ( job.isEmailOnJobComplete() && job.getEmail() != null && !job.getEmail().isEmpty() ) {
            try {
                emailService.sendJobCompletionMessage( job );
            } catch ( Exception e ) {
                log.warn( e );
            }
        }
        // Remove job from queue mirror
        job.setPosition( null );
        synchronized ( jobQueueMirror ) {
            jobQueueMirror.remove( job );
        }

        if ( !job.isFailed() ) {
            jobRepository.persistJob( job );
        }

        // Increment counts
        completionCounts.putIfAbsent( job.getClientId(), new AtomicInteger( 0 ) );
        completionCounts.get( job.getClientId() ).incrementAndGet();

        // Add new job for given client
        submitTopOfClientQueue( job.getClientId() );
        submitTopOfUserQueue( userQueueKey( job ) );
        log.info( String.format( "Jobs in queue: %d", jobQueueMirror.size() ) );
    }

    public int getCompletionCount(String clientId) {
        AtomicInteger count = completionCounts.get( clientId );
        return count == null ? 0 : count.get();
    }

    public List<JobVO> listJobsForClientAndUser(String clientId, String userId, boolean withResults) {
        return jobRepository.allJobsForClientAndUser( clientId, userId )
                .map( j -> j.toValueObject( true, withResults ) )
                .sorted(
                        Comparator.comparing(JobVO::isComplete, Comparator.nullsLast(Boolean::compareTo))
                                .thenComparing(JobVO::getPosition, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(JobVO::getSubmittedDate, Comparator.nullsLast(Date::compareTo).reversed())
                                .thenComparing(JobVO::getStatus, String::compareToIgnoreCase)
                )
                .collect( Collectors.toList() );
    }



}
