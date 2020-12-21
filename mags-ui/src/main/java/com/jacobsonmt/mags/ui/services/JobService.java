package com.jacobsonmt.mags.ui.services;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

import com.jacobsonmt.mags.ui.model.Job;
import com.jacobsonmt.mags.ui.model.Message;
import com.jacobsonmt.mags.ui.settings.ApplicationSettings;
import com.jacobsonmt.mags.ui.settings.SiteSettings;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Log4j2
@Service
public class JobService {

    @Autowired
    private ApplicationSettings applicationSettings;

    @Autowired
    private SiteSettings siteSettings;

    private Integer completionCount = 0;

    public ResponseEntity<JobSubmissionResponse> submitJob( String userId, String fasta, String email) {
        RestTemplate restTemplate = new RestTemplateBuilder().errorHandler(new NoOpResponseErrorHandler()).build();
        JobSubmission jobSubmission = new JobSubmission( userId,  fasta, email, siteSettings.getFullUrl() + "job/");
        HttpEntity<JobSubmission> request =
                new HttpEntity<>( jobSubmission, createHeaders() );
        return restTemplate.postForEntity( applicationSettings.getProcessServerURI() + "/job/submit", request, JobSubmissionResponse.class );
    }

    public ResponseEntity<Job> getJob(String jobId) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .errorHandler( new NoOpResponseErrorHandler() ).build();
        HttpEntity entity = new HttpEntity(createHeaders());
        // getForObject cannot specify headers so we use exchange

        log.info( "Client: (" + applicationSettings.getClientId() + "), Job: (" + jobId + ")" );
        ResponseEntity<Job> response
                = restTemplate.exchange(
                        applicationSettings.getProcessServerURI() + "/job/{jobId}",
                HttpMethod.GET,
                entity,
                Job.class,
                jobId
        );

        return response;

    }

    public ResponseEntity<String> deleteJob( String jobId) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .errorHandler( new RestTemplateResponseErrorHandler() )
                .build();
        HttpEntity entity = new HttpEntity(createHeaders());
        // getForObject cannot specify headers so we use exchange

        log.info( "Client: (" + applicationSettings.getClientId() + "), Job: (" + jobId + ")" );
        return restTemplate.exchange( applicationSettings.getProcessServerURI() + "/job/{jobId}/delete",
                HttpMethod.DELETE,
                entity,
                String.class,
                jobId
        );
    }

    public ResponseEntity<List<Job>> getJobsForUser( String userId ) {
        return getJobsForUser( userId, false );
    }

    public ResponseEntity<List<Job>> getJobsForUser( String userId, boolean withResults ) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .errorHandler( new RestTemplateResponseErrorHandler() ).build();
        HttpEntity entity = new HttpEntity(createHeaders());
        // getForObject cannot specify headers so we use exchange

        log.info( "Get Jobs for Client: (" + applicationSettings.getClientId() + "), User: (" + userId + ")" );
        ResponseEntity<List<Job>> response = restTemplate.exchange(
                applicationSettings.getProcessServerURI() + "/queue/client/{clientId}/user/{userId}?withResults={withResults}",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Job>>(){},
                applicationSettings.getClientId(),
                userId,
                withResults
                );

        return response;

    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set( "client", applicationSettings.getClientId() );
        headers.set( "auth_token", applicationSettings.getClientToken() );
        return headers;
    }

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class JobSubmission {
        private String userId;
        private String fastaContent;
        private String email;
        private String emailJobLinkPrefix;
    }

    @ToString
    @Setter
    @Getter
    @NoArgsConstructor
    public static class JobSubmissionResponse {
        private List<Message> messages;
        private List<Job> acceptedJobs;
        private List<String> rejectedJobHeaders;
        private int totalRequestedJobs;
    }

    private static class NoOpResponseErrorHandler extends
            DefaultResponseErrorHandler {

        @Override
        public void handleError( ClientHttpResponse response) throws IOException {
        }

    }

    public static class RestTemplateResponseErrorHandler
            implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse httpResponse)
                throws IOException {

            return (
                    httpResponse.getStatusCode().series() == CLIENT_ERROR
                            || httpResponse.getStatusCode().series() == SERVER_ERROR);
        }

        @Override
        public void handleError(ClientHttpResponse httpResponse)
                throws IOException {
            throw new ResponseStatusException( httpResponse.getStatusCode(), "", null );
        }
    }
}
