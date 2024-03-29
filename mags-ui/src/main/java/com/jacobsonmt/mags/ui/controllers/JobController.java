package com.jacobsonmt.mags.ui.controllers;

import static com.jacobsonmt.mags.ui.controllers.Utils.downloadAsFile;
import static com.jacobsonmt.mags.ui.controllers.Utils.jsonToCsv;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacobsonmt.mags.ui.exceptions.JobNotFoundException;
import com.jacobsonmt.mags.ui.exceptions.ResultNotFoundException;
import com.jacobsonmt.mags.ui.model.Job;
import com.jacobsonmt.mags.ui.model.Species;
import com.jacobsonmt.mags.ui.model.result.Graph;
import com.jacobsonmt.mags.ui.model.result.MaGSSeqResult;
import com.jacobsonmt.mags.ui.services.JobService;
import com.jacobsonmt.mags.ui.services.JobService.JobSubmissionResponse;
import com.jacobsonmt.mags.ui.utils.InputStreamUtils;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class JobController {

    private final JobService jobService;
    private final ObjectMapper objectMapper;

    public JobController(JobService jobService, ObjectMapper objectMapper) {
        this.jobService = jobService;

        // We want to flatten result to simplify conversion to CSV
        // We copy the object mapper so as not to flatten for all other serializations of Job
        this.objectMapper = objectMapper.copy().addMixIn(Job.class, JobUnwrappedMixIn.class);
    }

    @PostMapping("/submit")
    public String submitJob(@RequestParam(value = "fasta", required = false, defaultValue = "") String fasta,
                           @RequestParam(value = "fastaFile", required = false) MultipartFile fastaFile,
                           @RequestParam(value = "email", required = false, defaultValue = "") String email,
                           @RequestParam(value = "session", required = false) String session,
                           @RequestParam(value = "species", defaultValue = "HUMAN") Species species,
                           RedirectAttributes redirectAttributes) throws IOException {

        if (session == null || session.isEmpty()) {
            session = RequestContextHolder.currentRequestAttributes().getSessionId();
        }

        if ( fasta.isEmpty() ) {
            if (fastaFile != null) {
                fasta = InputStreamUtils.inputStreamToString( fastaFile.getInputStream() );
            } else {
                redirectAttributes.addFlashAttribute( "errorMessage", "FASTA Not Found" );
                return "redirect:/submit?session="+session;
            }
        }

        ResponseEntity<JobSubmissionResponse> jobSubmissionResponse = jobService.submitJob(
            session,
            fasta,
            email,
            species);

        if (jobSubmissionResponse.getBody() != null) {
            redirectAttributes.addFlashAttribute( "messages", jobSubmissionResponse.getBody().getMessages() );
        } else {
            redirectAttributes.addFlashAttribute( "errorMessage", "Server Error" );
        }

        return "redirect:/submit?session="+session;
    }

    @GetMapping("/job/{id}")
    public String jobPage( @PathVariable("id") long id, Model model) {
        if (id == 0) {
            throw new ResultNotFoundException();
        }

        ResponseEntity<Job> entity = jobService.getJob( id );

        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
            throw new JobNotFoundException();
        }

        Job job = entity.getBody();
        model.addAttribute("job", job );

        if (job.getResult() != null) {
            ResponseEntity<List<Graph>> graphs = jobService.getJobsResultGraphs(id);
            model.addAttribute("graphs", graphs.getBody());
        }

        return "job";
    }

    @GetMapping("/job/{id}/content")
    public String getJobViewContent( @PathVariable("id") long id, Model model) {
        jobPage(id, model);
        return "job :: #job-view-content";
    }

    @GetMapping("/api/job/{id}")
    @ResponseBody
    public Job job( @PathVariable("id") long id) {
        if (id == 0) {
            throw new ResultNotFoundException();
        }

        ResponseEntity<Job> entity = jobService.getJob( id );

        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
            throw new JobNotFoundException();
        }

        return entity.getBody();
    }

    @GetMapping("/api/job/{id}/csv")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> jobAsCSV( @PathVariable("id") long id) throws JsonProcessingException {
        if (id == 0) {
            throw new ResultNotFoundException();
        }

        ResponseEntity<Job> entity = jobService.getJob( id );

        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
            throw new JobNotFoundException();
        }

        return downloadAsFile( id + ".csv",  jsonToCsv(objectMapper.valueToTree(entity.getBody())));
    }

    interface JobUnwrappedMixIn {
        @JsonUnwrapped
        MaGSSeqResult getResult();
    }

    //TODO
//    @GetMapping(value = "/job/{jobId}/bases", produces = "application/json")
//    @ResponseBody
//    public List<Base> getJobResultBases( @PathVariable("jobId") String jobId) {
//        ResponseEntity<Job> entity = jobService.getJob( jobId );
//
//        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
//            throw new JobNotFoundException();
//        }
//
//        Job job = entity.getBody();
//
//        if ( job.isComplete() && !job.isFailed() ) {
//            return job.getResult().getBases();
//        }
//
//        return new ArrayList<>();
//    }

    @GetMapping("/job/{jobId}/delete")
    public ResponseEntity<String> deleteJob( @PathVariable("jobId") String jobId ) {
        return jobService.deleteJob( jobId );
    }

    @GetMapping("/queue/pending")
    public ResponseEntity<Long> queuePending() {
        return jobService.countPendingJobs();
    }

    @GetMapping("/api/job/{id}/graphs")
    @ResponseBody
    public ResponseEntity<List<Graph>> getResultDistributions(  @PathVariable("id") long id) {
        if (id == 0) {
            throw new ResultNotFoundException();
        }

        return jobService.getJobsResultGraphs(id);
    }

}
