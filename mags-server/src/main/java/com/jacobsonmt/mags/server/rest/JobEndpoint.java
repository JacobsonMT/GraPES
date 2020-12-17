package com.jacobsonmt.mags.server.rest;

import com.jacobsonmt.mags.server.exceptions.FASTAValidationException;
import com.jacobsonmt.mags.server.model.JobDO;
import com.jacobsonmt.mags.server.model.FASTASequence;
import com.jacobsonmt.mags.server.model.Message;
import com.jacobsonmt.mags.server.services.JobManager;
import com.jacobsonmt.mags.server.model.JobDO.JobVO;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints to access and submit jobs.
 *
 * TODO: Consider isolating clients so that they cannot view each others jobs.
 */
@Log4j2
@RequestMapping("/api/job")
@RestController
public class JobEndpoint {

    private final JobManager jobManager;

    public JobEndpoint(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    protected String getClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @RequestMapping(value = "/{jobId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<JobVO> getJob( @PathVariable String jobId,
                                                     @RequestParam(value = "withResults", defaultValue = "true")
                                                             boolean withResults ) {
        JobDO job = jobManager.getSavedJob( jobId );

        if ( job == null ) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok( createJobValueObject( jobManager.getSavedJob( jobId ), withResults ) );
    }

    @RequestMapping(value = "/{jobId}/status", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> getJobStatus(@PathVariable String jobId) {
        JobDO job = jobManager.getSavedJob( jobId );

        if ( job == null ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Not Found" );
        }

        return ResponseEntity.ok( job.getStatus() );
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<JobSubmissionResponse> submitJob( @Valid @RequestBody JobSubmissionContent jobSubmissionContent, BindingResult errors ) {
        // NOTE: You must declare an Errors, or BindingResult argument immediately after the validated method argument.
        String client = getClient();

        JobSubmissionResponse result = new JobSubmissionResponse();

        if (errors.hasErrors()) {
            for ( ObjectError error : errors.getAllErrors() ) {
                result.addMessage( new Message( Message.MessageLevel.ERROR, error.getDefaultMessage() ) );
            }
        } else {

            try {
                Set<FASTASequence> sequences = FASTASequence.parseFASTAContent( jobSubmissionContent.fastaContent );
                result.setTotalRequestedJobs( sequences.size() );

                sequences.stream().filter( s -> !s.getValidationStatus().isEmpty() ).forEach(
                        s -> {
                            result.addRejectedHeader( s.getHeader() );
                            result.addMessage( new Message( Message.MessageLevel.WARNING, s.getValidationStatus() + " for '" + s.getHeader() + "'" ) );
                        }
                );

                List<JobDO> jobs = jobManager.createJobs( client,
                        jobSubmissionContent.userId,
                        jobSubmissionContent.label,
                        sequences,
                        jobSubmissionContent.email,
                        jobSubmissionContent.hidden,
                        jobSubmissionContent.emailJobLinkPrefix,
                        jobSubmissionContent.emailOnJobSubmitted,
                        jobSubmissionContent.emailOnJobStart,
                        jobSubmissionContent.emailOnJobComplete
                );

                for ( JobDO job : jobs ) {
                    if (!job.isFailed()) {
                        String rejectedMsg = jobManager.submit(job);
                        if (rejectedMsg.isEmpty()) {
                            result.addAcceptedJob(job);
                        } else {
                            result.addRejectedHeader( job.getLabel() );
                            result.addMessage( new Message( Message.MessageLevel.WARNING, rejectedMsg + " for '" + job.getLabel() + "'" ) );
                        }
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
    public ResponseEntity<String> stopJob( @PathVariable("jobId") String jobId) {
        if ( jobId.equals( "example" ) ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Not Found" );
        }

        JobDO job = jobManager.getSavedJob( jobId );

        if ( job == null ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Not Found" );
        }

        jobManager.stopJob( job );
        return ResponseEntity.accepted().body( "Job Delete: " + jobId ); // Could be 'OK' as well, this seems semantically safer
    }

    @GetMapping("/{jobId}/resultCSV")
    public ResponseEntity<String> jobResultCSV( @PathVariable("jobId") String jobId) {
        JobDO job = jobManager.getSavedJob( jobId );

        if ( job == null ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Not Found" );
        }

        if ( !job.isComplete() ) {
            return ResponseEntity.status( HttpStatus.PROCESSING ).body( "Not Yet Complete");
        }

        if ( job.isFailed() ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Failed" );
        }

        return createStreamingResponse(job.getResult().getResultCSV(), job.getLabel() + ".list");
    }

    @GetMapping("/{jobId}/inputFASTA")
    public ResponseEntity<String> jobInputFASTA( @PathVariable("jobId") String jobId) {
        JobDO job = jobManager.getSavedJob( jobId );

        if ( job == null ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Job Not Found" );
        }

        return createStreamingResponse(job.getInputFASTAContent(), job.getLabel() + ".fasta");
    }

    private ResponseEntity<String> createStreamingResponse( String content, String filename ) {
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType("application/octet-stream"))
                .header( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(content);
    }

    private JobVO createJobValueObject( JobDO job, boolean withResults ) {
        if ( job == null ) {
            return null;
        }
        return job.toValueObject(true, withResults);
    }

    @Getter
    @AllArgsConstructor
    protected static final class JobSubmissionContent {
        private final String label;
        @NotBlank(message = "User missing!")
        private final String userId;
        @NotBlank(message = "FASTA content missing!")
        private final String fastaContent;
        private final Boolean hidden;
        @Email(message = "Not a valid email address")
        private final String email;
        private final String emailJobLinkPrefix;
        private final Boolean emailOnJobSubmitted = false;
        private final Boolean emailOnJobStart = false;
        private final Boolean emailOnJobComplete = true;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    static class JobSubmissionResponse {
        private List<Message> messages = new ArrayList<>();
        private List<JobVO> acceptedJobs = new ArrayList<>();;
        private List<String> rejectedJobHeaders = new ArrayList<>();;
        private int totalRequestedJobs;

        private void addMessage( Message message) {
            messages.add( message );
        }

        private void addAcceptedJob(JobDO job) {
            acceptedJobs.add( job.toValueObject( false, false ) );
        }

        private void addRejectedHeader(String label) {
            rejectedJobHeaders.add( label );
        }
    }


}
