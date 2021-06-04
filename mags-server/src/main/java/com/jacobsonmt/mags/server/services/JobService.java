package com.jacobsonmt.mags.server.services;


import com.google.common.collect.Sets;
import com.jacobsonmt.mags.server.dao.JobConfigDao;
import com.jacobsonmt.mags.server.dao.JobDao;
import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.entities.Job.Status;
import com.jacobsonmt.mags.server.entities.JobConfig;
import com.jacobsonmt.mags.server.entities.JobResult;
import com.jacobsonmt.mags.server.entities.Species;
import com.jacobsonmt.mags.server.model.FASTASequence;
import com.jacobsonmt.mags.server.services.mail.EmailService;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class JobService {

    private final JobDao jobDao;
    private final JobConfigDao jobConfigDao;
    private final JobRunner jobRunner;
    private final EmailService emailService;

    private final Set<String> recentlyProcessedSessions = new HashSet<>();
    private final Set<Status> pendingStatuses = Sets.newHashSet(Status.SUBMITTED, Status.PROCESSING);

    public JobService(
        JobDao jobDao,
        JobConfigDao jobConfigDao,
        JobRunner jobRunner,
        EmailService emailService) {
        this.jobDao = jobDao;
        this.jobConfigDao = jobConfigDao;
        this.jobRunner = jobRunner;
        this.emailService = emailService;
    }

    public void startJob() {
        try {
            findNextJob().ifPresent(job -> {
                log.info("Start job: {}", job.getId());
                try {
                    job.setStatus(Status.PROCESSING);
                    job.setStarted(Instant.now());

                    jobDao.save(job);
                    this.onJobStart(job);

                    JobResult result = jobRunner.run(job);
                    job.setResult(result);
                    result.setJob(job);

                    job.setStatus(Status.SUCCESS);
                    job.setFinished(Instant.now());
                    job.setMessage(
                        "Completed after " + (job.getFinished().getEpochSecond() - job.getStarted().getEpochSecond())
                            + "s");
                    log.info("Job {}: {}", job.getId(), job.getMessage());
                } catch (Exception e) {
                    log.error("Job failed: {}", job.getId(), e);
                    job.setStatus(Status.ERROR);
                    job.setFinished(Instant.now());
                    job.setMessage("Unknown Internal Error");
                } finally {
                    jobDao.save(job);
                    this.onJobFinish(job);
                }
            });
        } catch (Exception e) {
            log.error("Exception: ", e);
        }
    }

    /**
     * Get oldest job that hasn't been run
     *
     * @return Next job to run
     */
    public synchronized Optional<Job> findNextJob() {
        try {
            Optional<Job> job = jobDao
                .findFirstBySessionNotInAndStatusIsAndDeletedFalseOrderByCreatedDateAsc(
                    // There must be at least one element in the comma separated list that defines the set of values for the IN expression.
                    recentlyProcessedSessions.isEmpty() ? Sets.newHashSet(""): recentlyProcessedSessions,
                    Status.SUBMITTED);

            if (job.isPresent()) {
                log.info("Found job: {}", job.get().getId());
                recentlyProcessedSessions.add(job.get().getSession());
            } else {
                // No jobs available with restrictions on session
                log.debug("No jobs from new sessions");
                recentlyProcessedSessions.clear();
            }

            return job;
        } catch (Exception e) {
            log.error("Issue finding next job", e);
            return Optional.empty();
        }
    }

    @Transactional
    public Job submit( String user, String email, FASTASequence sequence, String emailExternalLink, Species species) {
        Job job = createJob(user, email, sequence, emailExternalLink, species);

        // Check for existing job with exact match that hasn't been invalidated
        Optional<Job> existingJob = jobDao.findFirstByInputAndStatusAndInvalidatedFalseOrderByCreatedDateDesc(job.getInput(), Status.SUCCESS);

        if (existingJob.isPresent() && existingJob.get().getResult() != null) {
            // Copy so that we store a new record in the db
            JobResult result = JobResult.fromJobResult(existingJob.get().getResult());
            result.setJob(job);
            job.setResult(result);
            job.setStatus(Status.SUCCESS);
            job.setMessage("Cached Result");
            log.info("Returning cached result for job submitted for user: {}, length: {}", user, sequence.getSequence().length());
        } else {
            log.info("Job submitted for user: {}, length: {}", user, sequence.getSequence().length());
        }

        return jobDao.save(job);
    }

    public Optional<Job> getJob( long jobId ) {
        return jobDao.findByIdAndDeletedFalse(jobId);
    }

    @Transactional
    public void stopJob( long jobId ) {
        log.info("Stop job: {}", jobId);
        getJob(jobId).ifPresent(job -> {
            job.setDeleted(true);
            if (job.getStatus() == Status.SUBMITTED) {
                job.setStatus(Status.STOPPED);
            }
            jobDao.save(job);
        });
    }

    public List<Job> getJobs(String session) {
        return jobDao.findBySessionAndDeletedFalseOrderByCreatedDateDesc(session);
    }

    public long getPendingJobCount() {
        return jobDao.countJobByDeletedFalseAndStatusIn(pendingStatuses);
    }

    public long getPendingJobCount(String session) {
        return jobDao.countJobBySessionIsAndDeletedFalseAndStatusIn(session, pendingStatuses);
    }

    private Job createJob(String user, String email, FASTASequence sequence, String emailExternalLink, Species species) {
        Job job = new Job();
        job.setSession(user);
        job.setSpecies(species != null ? species : Species.HUMAN);

        job.setLabel(sequence.getHeader());
        job.setInput(sequence.getSequence());

        if ( !sequence.getValidationStatus().isEmpty() ) {
            job.setStatus(Status.VALIDATION_ERROR);
            job.setMessage(sequence.getValidationStatus());
        } else {
            Status status = jobConfigDao.findById(JobConfig.DEFAULT_STATUS).map(JobConfig::getValue)
                .map(Status::getValueOf).orElse(Status.SUBMITTED);
            if (status != Status.SUBMITTED) {
                String message = jobConfigDao.findById(JobConfig.DEFAULT_MESSAGE).map(JobConfig::getValue).orElse("");
                job.setMessage(message);
            }
            job.setStatus(status);
        }

        job.setEmail(email);
        job.setExternalLink(emailExternalLink);

        return job;
    }

    private void onJobStart( Job job ) {
        if (StringUtils.isNotEmpty(job.getEmail())) {
            try {
                emailService.sendJobStartMessage(job);
            } catch (Exception e) {
                log.error("Exception sending job start email for job: {}", job.getId(),  e);
            }
        }
    }

    private void onJobFinish( Job job ) {
        if (StringUtils.isNotEmpty(job.getEmail())) {
            try {
                emailService.sendJobCompletionMessage(job);
            } catch (Exception e) {
                log.error("Exception sending job finished email for job: {}", job.getId(),  e);
            }
        }
    }
}
