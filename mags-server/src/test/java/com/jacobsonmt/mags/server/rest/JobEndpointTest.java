package com.jacobsonmt.mags.server.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacobsonmt.mags.server.dao.JobDao;
import com.jacobsonmt.mags.server.dao.PrecomputedResultDao;
import com.jacobsonmt.mags.server.model.FASTASequence;
import com.jacobsonmt.mags.server.model.JobDO;
import com.jacobsonmt.mags.server.model.JobResult;
import com.jacobsonmt.mags.server.rest.JobEndpoint.JobSubmissionContent;
import com.jacobsonmt.mags.server.services.JobManager;
import com.jacobsonmt.mags.server.services.ResultService;
import com.jacobsonmt.mags.server.settings.ClientSettings;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith( SpringRunner.class )
@WebMvcTest
@AutoConfigureMockMvc
@TestPropertySource(
        locations = {"classpath:application.properties"},
        properties = {"spring.jackson.date-format=yyyy-MM-dd HH:mm:ss z", "spring.jackson.time-zone=UTC"})
@ContextConfiguration(
        initializers={ConfigFileApplicationContextInitializer.class}
)
public class JobEndpointTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ClientSettings clientSettings;

    @MockBean
    private JobManager jobManager;

    @MockBean
    private ResultService resultService;

    @MockBean
    private JobDao jobDao;

    @MockBean
    private PrecomputedResultDao precomputedResultDao;

    @Autowired private ApplicationContext ctx;

    private JobDO commonJob;

    private SimpleDateFormat jacksonDateFormat;

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Before
    public void setUp() {

        JobDO.JobDOBuilder builder = JobDO.builder();

        builder.jobId( "testJobId" );
        builder.clientId( "testClient" );
        builder.label( "testLabel" );
        builder.status( "testStatus" );
        builder.running( false );
        builder.failed( false );
        builder.complete( true );
        builder.position( null );
        builder.email( "email@email.com" );
        builder.hidden( true );
        builder.submittedDate( new Date() );
        builder.startedDate( new Date() );
        builder.finishedDate( new Date() );
        builder.inputFASTAContent( ">Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" );
        String resultCSV = "OX\t9749\tHomo Sapiens Test\n" + "AC\tPos\tRef\tDepth\tConservation\tA\tR\tN\tD\tC\tQ\tE\tG\tH\tI\tL\tK\tM\tF\tP\tS\tT\tW\tY\tV\n" +
                "sp|P07766|CD3E_\t1\tM\t41\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\n" +
                "sp|P07766|CD3E_\t2\tQ\t7\t0.253307\t0.244276\t0.233177\t0.244276\t0.244276\t0.244276\t0\t0.317168\t0.244276\t0.281372\t0.244276\t0.232177\t0.30097\t0.244276\t0.244276\t0.272373\t0.244276\t0.244276\t0.244276\t0.244276\t0.244276";
        builder.result( JobResult.parseResultCSVStream( new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 )) ));
        builder.executionTime( 17 );

        commonJob = builder.build();

        ClientSettings.ApplicationClient client = new ClientSettings.ApplicationClient();
        client.setToken( "testclienttoken" );
        given( clientSettings.getClients() ).willReturn( Maps.newHashMap("testclient", client ));

        given( jobManager.getSavedJob( commonJob.getJobId() ) ).willReturn( commonJob );

        when(jobManager.createJobs(anyString(), anyString(), anyString(), anySet(), anyString(), anyBoolean(), anyString(),
            anyBoolean(), anyBoolean(), anyBoolean())).thenAnswer((Answer<List<JobDO>>) invocation -> {
            Object[] args = invocation.getArguments();
            Set<FASTASequence> sequences = (Set< FASTASequence >) args[3];
            List<JobDO> ans = Lists.newArrayList();
            for (int i = 0; i < sequences.size(); i++) {
                ans.add(mock(JobDO.class));
            }
            return ans;
        });

        jacksonDateFormat = new SimpleDateFormat( ctx.getEnvironment().getProperty( "spring.jackson.date-format" ) );
        jacksonDateFormat.setTimeZone( TimeZone.getTimeZone( ctx.getEnvironment().getProperty( "spring.jackson.time-zone" ) ));

    }

    @Test
    public void givenJobExists_whenGetJob_thenReturnJson() throws Exception {

        mvc.perform( get( "/api/job/" + commonJob.getJobId() )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttoken" )
                .header( "client", "testclient" ))
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.jobId", is( commonJob.getJobId())))
                .andExpect( jsonPath( "$.clientId", is( commonJob.getClientId())))
                .andExpect( jsonPath( "$.label", is( commonJob.getLabel())))
                .andExpect( jsonPath( "$.status", is( commonJob.getStatus())))
                .andExpect( jsonPath( "$.running", is( commonJob.isRunning())))
                .andExpect( jsonPath( "$.failed", is( commonJob.isFailed())))
                .andExpect( jsonPath( "$.complete", is( commonJob.isComplete())))
                .andExpect( jsonPath( "$.position", is( commonJob.getPosition())))
                .andExpect( jsonPath( "$.email", is( JobDO.obfuscateEmail(commonJob.getEmail()))))
                .andExpect( jsonPath( "$.hidden", is( commonJob.isHidden())))
                .andExpect( jsonPath( "$.submittedDate", is( jacksonDateFormat.format(commonJob.getSubmittedDate()))))
                .andExpect( jsonPath( "$.startedDate", is( jacksonDateFormat.format(commonJob.getStartedDate()))))
                .andExpect( jsonPath( "$.finishedDate", is( jacksonDateFormat.format(commonJob.getFinishedDate()))))
                .andExpect( jsonPath( "$.inputFASTAContent", is( commonJob.getInputFASTAContent())))
                .andExpect( jsonPath( "$.result.resultCSV", is( commonJob.getResult().getResultCSV())))
                .andExpect( jsonPath( "$.result.taxa.key", is( commonJob.getResult().getTaxa().getKey())))
                .andExpect( jsonPath( "$.result.taxa.id", is( commonJob.getResult().getTaxa().getId())))
                .andExpect( jsonPath( "$.result.taxa.name", is( commonJob.getResult().getTaxa().getName())));

    }

    @Test
    public void givenJobNotExists_whenGetJob_thenReturn404() throws Exception {
        mvc.perform( get( "/api/job/" + commonJob.getJobId() + "wrong" )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttoken" )
                .header( "client", "testclient" ))
                .andExpect( status().isNotFound() );
    }

    @Test
    public void givenWrongCredentials_whenGetJob_thenReturn403() throws Exception {
        mvc.perform( get( "/api/job/" + commonJob.getJobId() )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttokenwrong" )
                .header( "client", "testclientwrong" ))
                .andExpect( status().isForbidden() );
    }

    /* Status */

    @Test
    public void givenJobExists_whenGetJobStatus_thenReturnString() throws Exception {

        mvc.perform( get( "/api/job/" + commonJob.getJobId() + "/status" )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttoken" )
                .header( "client", "testclient" ))
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$", is( commonJob.getStatus())));
    }

    @Test
    public void givenJobNotExists_whenGetJobStatus_thenReturn404() throws Exception {
        mvc.perform( get( "/api/job/" + commonJob.getJobId() + "wrong" + "/status" )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttoken" )
                .header( "client", "testclient" ))
                .andExpect( status().isNotFound() );
    }

    @Test
    public void givenWrongCredentials_whenGetJobStatus_thenReturn403() throws Exception {
        mvc.perform( get( "/api/job/" + commonJob.getJobId() + "/status" )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttokenwrong" )
                .header( "client", "testclientwrong" ))
                .andExpect( status().isForbidden() );
    }

/* Submit */

    @Test
    public void whenSubmitSingleValidJob_thenReturnValidResponse() throws Exception {
        JobSubmissionContent jobSubmissionContent =  new JobSubmissionContent(
        "label",
        "userId",
        ">P07766 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n",
        false,
        "",
        "emailJobLinkPrefix"
        );

        doReturn("").when(jobManager)
            .submit(any());

        mvc.perform( post( "/api/job/submit" )
                .contentType( MediaType.APPLICATION_JSON )
                .header( "auth_token", "testclienttoken" )
                .header( "client", "testclient" )
                .content( OBJECT_MAPPER.writeValueAsString(jobSubmissionContent) ))
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.totalRequestedJobs", is(1)))
                .andExpect( jsonPath( "$.acceptedJobs", hasSize(1)))
                .andExpect( jsonPath( "$.rejectedJobHeaders", hasSize(0)));
    }

    @Test
    public void whenSubmitMultipleValidJobs_thenReturnValidResponse() throws Exception {
        StringBuilder fastaBuilder = new StringBuilder();
        fastaBuilder.append(">P07766-1 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-2 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-3 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        fastaBuilder.append(">P07766-4 OX=9606\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWQHNDKNI\n");
        JobSubmissionContent jobSubmissionContent =  new JobSubmissionContent(
            "label",
            "userId",
            fastaBuilder.toString(),
            false,
            "",
            "emailJobLinkPrefix"
        );

        doReturn("").when(jobManager)
            .submit(any());

        mvc.perform( post( "/api/job/submit" )
            .contentType( MediaType.APPLICATION_JSON )
            .header( "auth_token", "testclienttoken" )
            .header( "client", "testclient" )
            .content( OBJECT_MAPPER.writeValueAsString(jobSubmissionContent) ))
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.totalRequestedJobs", is(4)))
            .andExpect( jsonPath( "$.acceptedJobs", hasSize(4)))
            .andExpect( jsonPath( "$.rejectedJobHeaders", hasSize(0)));
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

        doReturn("").when(jobManager)
            .submit(any());

        mvc.perform( post( "/api/job/submit" )
            .contentType( MediaType.APPLICATION_JSON )
            .header( "auth_token", "testclienttoken" )
            .header( "client", "testclient" )
            .content( OBJECT_MAPPER.writeValueAsString(jobSubmissionContent) ))
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.totalRequestedJobs", is(4)))
            .andExpect( jsonPath( "$.rejectedJobHeaders", hasSize(2)))
            .andExpect( jsonPath( "$.rejectedJobHeaders", containsInAnyOrder("P07766-1 OX=9606", "P07766-3 OX=9606")));
    }


}
