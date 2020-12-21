package com.jacobsonmt.mags.ui.controllers;

import com.jacobsonmt.mags.ui.model.Job;
import com.jacobsonmt.mags.ui.model.Job.Status;
import com.jacobsonmt.mags.ui.services.JobService;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;

@Log4j2
@Controller
public class MainController {

    @Autowired
    private JobService jobService;

    @GetMapping("/")
    public String index() {
        return "precomputed";
    }

    @GetMapping("/submit")
    public String submit() {
        String session = RequestContextHolder.currentRequestAttributes().getSessionId();
        return "redirect:/submit?session="+session;
    }

    @GetMapping(value = "/submit", params = "session")
    public String submitWithSession( Model model, @RequestParam(value = "session") String session ) {
        model.addAttribute("jobs", jobService.getJobsForUser( session ).getBody());
        model.addAttribute("sessionId", session);
        return "submit";
    }

    @GetMapping("/job-table")
    public String getJobTable( Model model, @RequestParam(value = "session", required = false) String session ) {
        if (session == null || session.isEmpty()) {
            session = RequestContextHolder.currentRequestAttributes().getSessionId();
        }

        model.addAttribute("jobs", jobService.getJobsForUser( session ).getBody());

        return "submit :: #job-table";
    }

    @GetMapping("/queue")
    public String queue( Model model, @RequestParam(value = "session", required = false) String session ) {
        if (session == null || session.isEmpty()) {
            session = RequestContextHolder.currentRequestAttributes().getSessionId();
        }
        model.addAttribute("jobs", jobService.getJobsForUser( session ).getBody());

        return "queue?session=" + session;
    }

    @GetMapping("/pending")
    public ResponseEntity<Long> pendingCount(@RequestParam(value = "session", required = false) String session ) {
        if (session == null || session.isEmpty()) {
            session = RequestContextHolder.currentRequestAttributes().getSessionId();
        }
        List<Job> jobs = jobService.getJobsForUser( session ).getBody();
        if (jobs == null) {
            return ResponseEntity.status( 500 ).body( 0L );
        }

        return ResponseEntity.ok().body(jobs.stream()
            .filter( j -> j.getStatus() == Status.SUBMITTED || j.getStatus() == Status.PROCESSING ).count());
    }

    @GetMapping("/precomputed")
    public String search( Model model) {
        return "precomputed";
    }

    @GetMapping("/documentation")
    public String documentation( Model model) {
        return "documentation";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/maintenance")
    public String maintenance() {
        return "maintenance";
    }
}
