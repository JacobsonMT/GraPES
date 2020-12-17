package com.jacobsonmt.mags.ui.controllers;

import com.jacobsonmt.mags.ui.exceptions.JobNotFoundException;
import com.jacobsonmt.mags.ui.model.Base;
import com.jacobsonmt.mags.ui.model.Job;
import com.jacobsonmt.mags.ui.services.JobService;
import com.jacobsonmt.mags.ui.utils.InputStreamUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Controller
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/")
    public String submitJob(       @RequestParam(value = "fasta", required = false, defaultValue = "") String fasta,
                                   @RequestParam(value = "fastaFile", required = false) MultipartFile fastaFile,
//                                   @RequestParam(value = "label", required = false, defaultValue = "") String label,
                                   @RequestParam(value = "email", required = false, defaultValue = "") String email,
//                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) throws IOException {

//        String ipAddress = request.getHeader( "X-FORWARDED-FOR" );
//        if ( ipAddress == null ) {
//            ipAddress = request.getRemoteAddr();
//        }

        String userId = RequestContextHolder.currentRequestAttributes().getSessionId();

        if ( fasta.isEmpty() ) {
            if (fastaFile != null) {
                fasta = InputStreamUtils.inputStreamToString( fastaFile.getInputStream() );
            } else {
                redirectAttributes.addFlashAttribute( "errorMessage", "FASTA Not Found" );
                return "redirect:/";
            }
        }

        ResponseEntity<JobService.JobSubmissionResponse> jobSubmissionResponse = jobService.submitJob( userId,
                "",
                fasta,
                email,
                true );

        if (jobSubmissionResponse.getBody() != null) {
            redirectAttributes.addFlashAttribute( "messages", jobSubmissionResponse.getBody().getMessages() );
        } else {
            redirectAttributes.addFlashAttribute( "errorMessage", "Server Error" );
        }

        return "redirect:/";
    }

    @GetMapping("/job/{jobId}")
    public String job( @PathVariable("jobId") String jobId,
                       Model model) throws IOException {

        ResponseEntity<Job> entity = jobService.getJob( jobId );

        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
            throw new JobNotFoundException();
        }

        model.addAttribute("job", entity.getBody() );

        return "job";
    }

    @GetMapping("/job/{jobId}/content")
    public String getJobViewContent( @PathVariable("jobId") String jobId,
                                     Model model) {
        ResponseEntity<Job> entity = jobService.getJob( jobId );

        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
            throw new JobNotFoundException();
        }

        model.addAttribute("job", entity.getBody() );

        return "job :: #job-view-content";
    }

    @GetMapping(value = "/job/{jobId}/bases", produces = "application/json")
    @ResponseBody
    public List<Base> getJobResultBases( @PathVariable("jobId") String jobId) {
        ResponseEntity<Job> entity = jobService.getJob( jobId );

        if (entity.getStatusCode().equals( HttpStatus.NOT_FOUND ) || entity.getBody() == null ) {
            throw new JobNotFoundException();
        }

        Job job = entity.getBody();

        if ( job.isComplete() && !job.isFailed() ) {
            return job.getResult().getBases();
        }

        return new ArrayList<>();
    }

    @GetMapping("/job/{jobId}/delete")
    public ResponseEntity<String> deleteJob( @PathVariable("jobId") String jobId ) {
        return jobService.deleteJob( jobId );
    }

    @GetMapping("/job/{jobId}/resultCSV")
    public ResponseEntity<String> jobResultCSV( @PathVariable("jobId") String jobId) {
        return jobService.downloadJobResultContent( jobId );
    }

    @GetMapping("/job/{jobId}/inputFASTA")
    public ResponseEntity<String> jobInputFASTA( @PathVariable("jobId") String jobId) {
        return jobService.downloadJobInputFASTA( jobId );
    }


}
