package com.jacobsonmt.mags.server.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.jacobsonmt.mags.server.repositories.JobRepository;
import com.jacobsonmt.mags.server.rest.JobEndpoint.JobSubmissionContent;
import com.jacobsonmt.mags.server.rest.JobEndpoint.JobSubmissionResponse;
import com.jacobsonmt.mags.server.services.mail.EmailService;
import com.jacobsonmt.mags.server.services.mail.JavaMailService;
import com.jacobsonmt.mags.server.services.JobManager;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import com.jacobsonmt.mags.server.settings.ClientSettings;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public class JobEndpointUnitTest {

    @Autowired
    private ApplicationContext ctx;

    JobEndpoint jobEndpoint;

    private JobManager jobManager;

    @Mock
    private ApplicationSettings applicationSettings;

    private ClientSettings clientSettings;

    @Mock
    private EmailService emailService;

    @Mock
    private JobRepository jobRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ClientSettings.ApplicationClient client = new ClientSettings.ApplicationClient();
        clientSettings = new ClientSettings();
        clientSettings.getClients().put("testclient", client);
        jobManager = new JobManager(applicationSettings, clientSettings, emailService, jobRepository);
        jobManager.setExecutor(mock(ExecutorService.class));
        jobEndpoint = spy(new JobEndpoint(jobManager));

//        ClientSettings.ApplicationClient client = new ClientSettings.ApplicationClient();
//        client.setToken( "testclienttoken" );
//        given( clientSettings.getClients() ).willReturn( Maps.newHashMap("testclient", client ));
        doReturn("testclient").when(jobEndpoint).getClient();
    }

    @Test
    public void whenSubmitMixedValidJobs_thenReturnValidResponse() throws Exception {
        StringBuilder fastaBuilder = new StringBuilder();
        fastaBuilder.append(">P07766-1 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-1 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-3 OX=9606\nMQSGTHWRVLG\n");
        fastaBuilder.append(">P07766-4 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        JobSubmissionContent jobSubmissionContent =  new JobSubmissionContent(
            "label",
            "userId",
            fastaBuilder.toString(),
            false,
            "",
            "emailJobLinkPrefix"
        );

//        doReturn("").when(jobManager)
//            .submit(any());

        ResponseEntity<JobSubmissionResponse> response = jobEndpoint
            .submitJob(jobSubmissionContent, mock(BindingResult.class, RETURNS_DEEP_STUBS));

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTotalRequestedJobs(), is(4));
        assertThat(response.getBody().getAcceptedJobs(), hasSize(2));
        assertThat(response.getBody().getRejectedJobHeaders(), hasSize(2));
        assertThat(response.getBody().getRejectedJobHeaders(), containsInAnyOrder("P07766-1 OX=9606", "P07766-3 OX=9606"));
    }

    @Test
    public void whenSubmitMixedValidJobsAndExceedUserLimit_thenReturnValidResponse() throws Exception {
        StringBuilder fastaBuilder = new StringBuilder();
        fastaBuilder.append(">P07766-1 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-1 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-3 OX=9606\nMQSGTHWRVLG\n");
        fastaBuilder.append(">P07766-4 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-5 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-6 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-7 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-8 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-9 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        JobSubmissionContent jobSubmissionContent =  new JobSubmissionContent(
            "",
            "userId",
            fastaBuilder.toString(),
            false,
            "",
            "emailJobLinkPrefix"
        );

        clientSettings.getClients().get("testclient").setUserJobLimit(1);
        clientSettings.getClients().get("testclient").setUserClientLimit(1);
        clientSettings.getClients().get("testclient").setProcessLimit(1);
        clientSettings.getClients().get("testclient").setJobLimit(1);

//        doReturn("").when(jobManager)
//            .submit(any());

        ResponseEntity<JobSubmissionResponse> response = jobEndpoint
            .submitJob(jobSubmissionContent, mock(BindingResult.class, RETURNS_DEEP_STUBS));

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTotalRequestedJobs(), is(9));
        assertThat(response.getBody().getAcceptedJobs(), hasSize(3));
        assertThat(response.getBody().getRejectedJobHeaders(), hasSize(6));
        assertThat(response.getBody().getRejectedJobHeaders(),
            containsInAnyOrder(
                "P07766-1 OX=9606",
                "P07766-3 OX=9606",
                "P07766-6 OX=9606",
                "P07766-7 OX=9606",
                "P07766-8 OX=9606",
                "P07766-9 OX=9606"
            ));
        assertThat(response.getBody().getMessages().get(0).getMessage().toLowerCase(), containsString("duplicate"));
        assertThat(response.getBody().getMessages().get(1).getMessage().toLowerCase(), containsString("minimum"));
    }


}
