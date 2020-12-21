package com.jacobsonmt.mags.server.rest;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.entities.Job.Status;
import com.jacobsonmt.mags.server.exceptions.FASTAValidationException;
import com.jacobsonmt.mags.server.model.FASTASequence;
import com.jacobsonmt.mags.server.model.Message;
import com.jacobsonmt.mags.server.services.JobService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints to access and submit jobs.
 */
@Slf4j
@RequestMapping("/api/job")
@RestController
public class JobEndpoint {

    private final JobService jobService;

    public JobEndpoint(JobService jobService) {
        this.jobService = jobService;
    }

    protected String getClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @RequestMapping(value = "/{jobId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Job> getJob( @PathVariable int jobId,
        @RequestParam(value = "withResults", defaultValue = "true") boolean withResults ) {
        return jobService.getJob( jobId ).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @RequestMapping(value = "/{jobId}/status", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> getJobStatus(@PathVariable int jobId) {
        return jobService.getJob( jobId )
            .map(Job::getStatus)
            .map(Status::name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<JobSubmissionResponse> submitJob( @Valid @RequestBody JobSubmissionContent jobSubmissionContent, BindingResult errors ) {
        // NOTE: You must declare an Errors, or BindingResult argument immediately after the validated method argument.
        JobSubmissionResponse result = new JobSubmissionResponse();

        if (errors.hasErrors()) {
            for ( ObjectError error : errors.getAllErrors() ) {
                result.addMessage( new Message( Message.MessageLevel.ERROR, error.getDefaultMessage() ) );
            }
        } else {

            try {
                Set<FASTASequence> sequences = FASTASequence.parseFASTAContent( jobSubmissionContent.fastaContent );
                result.setTotalRequestedJobs( sequences.size() );

                for (FASTASequence s : sequences) {

                    Job job = jobService.submit(
                        jobSubmissionContent.userId,
                        jobSubmissionContent.email,
                        s,
                        jobSubmissionContent.emailJobLinkPrefix);

                    if (job.getStatus() != Status.SUBMITTED) {
                        result.addRejectedHeader( job.getLabel() );
                        result.addMessage( new Message( Message.MessageLevel.WARNING, job.getMessage() + " for '" + job.getLabel() + "'" ) );
                    } else {
                        result.addAcceptedJob(job);
                    }
                }

                if ( result.getAcceptedJobs().isEmpty() ) {
                    result.addMessage( new Message( Message.MessageLevel.WARNING, "No jobs were submitted." ) );
                } else {
                    result.addMessage( new Message( Message.MessageLevel.INFO, "Submitted " + result.getAcceptedJobs().size() + " jobs." ) );
                }

            } catch ( FASTAValidationException e ) {
                result.addMessage( new Message( Message.MessageLevel.ERROR, e.getMessage() ) );
            }
        }

        HttpStatus status = result.getMessages().stream()
                .anyMatch( m -> m.getLevel().equals( Message.MessageLevel.ERROR ) ) ?
                HttpStatus.BAD_REQUEST :
                HttpStatus.OK;

        return ResponseEntity.status( status ).body( result );

    }

    @DeleteMapping("/{jobId}/delete")
    public ResponseEntity<String> stopJob( @PathVariable("jobId") long jobId) {
        Job job = jobService.getJob( jobId ).orElse(null);

        if ( job == null ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Not Found" );
        }

        jobService.stopJob( jobId );
        return ResponseEntity.accepted().body( "Job Delete: " + jobId ); // Could be 'OK' as well, this seems semantically safer
    }

    @Getter
    @AllArgsConstructor
    protected static final class JobSubmissionContent {
        @NotBlank(message = "User missing!")
        private final String userId;
        @NotBlank(message = "FASTA content missing!")
        private final String fastaContent;
        @Email(message = "Not a valid email address")
        private final String email;
        private final String emailJobLinkPrefix;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    static class JobSubmissionResponse {
        private List<Message> messages = new ArrayList<>();
        private List<Job> acceptedJobs = new ArrayList<>();;
        private List<String> rejectedJobHeaders = new ArrayList<>();;
        private int totalRequestedJobs;

        private void addMessage( Message message) {
            messages.add( message );
        }

        private void addAcceptedJob(Job job) {
            acceptedJobs.add( job );
        }

        private void addRejectedHeader(String label) {
            rejectedJobHeaders.add( label );
        }
    }


}
