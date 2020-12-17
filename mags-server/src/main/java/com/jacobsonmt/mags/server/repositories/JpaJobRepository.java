package com.jacobsonmt.mags.server.repositories;

import com.jacobsonmt.mags.server.dao.JobDao;
import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.entities.Job.Status;
import com.jacobsonmt.mags.server.entities.Result;
import com.jacobsonmt.mags.server.model.JobDO;
import com.jacobsonmt.mags.server.model.JobResult;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.time.Instant;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Log4j2
@Primary
@Component
public class JpaJobRepository implements JobRepository{

    private final ApplicationSettings applicationSettings;

    private final JobDao jobDao;

    public JpaJobRepository(ApplicationSettings applicationSettings, JobDao jobDao) {
        this.applicationSettings = applicationSettings;
        this.jobDao = jobDao;}

    @Override
    public JobDO getById(String id) {
        Job job = jobDao.findByJobKey(id);
        return convertJob(job);
    }

    @Override
    public String getRawResultFileById(String id) {
        Job job = jobDao.findByJobKey(id);
        if (job == null) {
            return null;
        }

        Result result = job.getResult();
        if (result == null) {
            return null;
        }
        Path path = Paths.get( applicationSettings.getJobsDirectory(), result.getResultFilePath() );

        if ( !Files.exists( path ) ) {
            return null;
        }

        try {
            return inputStreamToString( Files.newInputStream( path ) );
        } catch ( IOException e ) {
            log.error( e );
        }

        return null;
    }

    @Override
    public Stream<JobDO> allJobsForClientAndUser(String clientId, String userId) {
        return jobDao.findByClientIdAndUserId(clientId, userId).stream().map(this::convertJob);
    }

    @Override
    public void delete(JobDO jobDO) {
        jobDao.deleteByJobKey(jobDO.getJobId());
    }

    @Override
    public void persistJob(JobDO jobDO) {
        Job newJob = convertJob(jobDO);
        Long id = jobDao.findIdByJobKey(jobDO.getJobId());
        if (id != null) {
            newJob.setId(id);
        }

        jobDao.save(newJob);
    }

    @Override
    public void cacheJob(JobDO jobDO) {
        Job newJob = convertJob(jobDO);
        Long id = jobDao.findIdByJobKey(jobDO.getJobId());
        if (id != null) {
            newJob.setId(id);
        }

        jobDao.save(newJob);
    }

    private static String inputStreamToString( InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try ( Reader reader = new BufferedReader(new InputStreamReader
            (inputStream, Charset.forName( StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    private Job convertJob(JobDO jobDO) {
        Job job = new Job();
        job.setClientId(jobDO.getClientId());
        job.setUserId(jobDO.getUserId());
        job.setJobKey(jobDO.getJobId());
        job.setLabel(jobDO.getLabel());
        job.setInputFASTAContent(jobDO.getInputFASTAContent());
        job.setHidden(jobDO.isHidden());

        job.setSubmittedDate(dateToInstant(jobDO.getSubmittedDate()));
        job.setStarted(dateToInstant(jobDO.getStartedDate()));
        job.setFinished(dateToInstant(jobDO.getFinishedDate()));

        job.setEmail(jobDO.getEmail());
        job.setExternalLink(jobDO.getEmailJobLinkPrefix() + jobDO.getJobId());
        job.setEmailOnJobSubmitted(jobDO.isEmailOnJobSubmitted());
        job.setEmailOnJobStart(jobDO.isEmailOnJobStart());
        job.setEmailOnJobComplete(jobDO.isEmailOnJobComplete());

        if (jobDO.isComplete() && !jobDO.isFailed()) {
            job.setStatus(Status.SUCCESS);
        } else if (jobDO.isFailed()) {
            job.setStatus(Status.ERROR);
        } else {
            job.setStatus(Status.SUBMITTED);
        }

        if (jobDO.getResult() != null) {
            Result result = new Result();
            result.setJob(job);
            result.setAccession(jobDO.getResult().getAccession());
            result.setExecutionTime(jobDO.getExecutionTime());
            result.setResultFilePath(Paths.get(job.getJobKey(), applicationSettings.getOutputCSVFilename() ).toString());
            job.setResult(result);
        }

        job.setPosition(jobDO.getPosition());

        return job;
    }

    private Instant dateToInstant(Date date) {
        if (date == null) {
            return null;
        }

        return date.toInstant();
    }

    private JobDO convertJob(Job job) {
        if (job == null){
            return null;
        }

        JobDO.JobDOBuilder builder = JobDO.builder();

        builder.command(applicationSettings.getCommand());
        builder.jobsDirectory(Paths.get(applicationSettings.getJobsDirectory()));
        builder.outputCSVFilename(applicationSettings.getOutputCSVFilename());
        builder.inputFASTAFilename(applicationSettings.getInputFASTAFilename());
//        builder.jobSerializationFilename();
        builder.clientId(job.getClientId());
        builder.userId(job.getUserId());
        builder.jobId(job.getJobKey());
        builder.label(job.getLabel());
        builder.inputFASTAContent(job.getInputFASTAContent());
        builder.hidden(job.isHidden());
        builder.submittedDate(instantToDate(job.getSubmittedDate()));
        builder.startedDate(instantToDate(job.getStarted()));
        builder.finishedDate(instantToDate(job.getFinished()));
        builder.email(job.getEmail());
        builder.emailJobLinkPrefix(job.getExternalLink());
        builder.emailOnJobSubmitted(job.isEmailOnJobSubmitted());
        builder.emailOnJobStart(job.isEmailOnJobStart());
        builder.emailOnJobComplete(job.isEmailOnJobComplete());
        builder.running(false);
        builder.failed(job.getStatus() == Status.ERROR);
        builder.complete(job.getStatus() == Status.ERROR || job.getStatus() == Status.SUCCESS);
        builder.position(null);
        builder.status(job.getStatus().name()); //TODO:


        builder.saved(true);
//        builder.saveExpiredDate();
//        builder.onJobStart();
//        builder.onJobComplete();
//        builder.future();

        if (job.getResult() != null) {
            // TODO: set from db?
            Path path = Paths.get( applicationSettings.getJobsDirectory(), job.getResult().getResultFilePath() );
            try {
                builder.result( JobResult.parseResultCSVStream(
                    Files.newInputStream( path) ) );
            } catch ( IOException e ) {
                builder.result( JobResult.createNullResult() );
            }

            builder.executionTime(job.getResult().getExecutionTime());
        }

        return builder.build();
    }

    private Date instantToDate(Instant instant) {
        if (instant == null) {
            return null;
        }

        return Date.from(instant);
    }
}
