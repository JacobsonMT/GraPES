package com.jacobsonmt.mags.ui.controllers;

import com.jacobsonmt.mags.ui.model.ContactForm;
import com.jacobsonmt.mags.ui.model.Job;
import com.jacobsonmt.mags.ui.services.JobService;
import com.jacobsonmt.mags.ui.services.mail.EmailService;
import java.util.List;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;

@Log4j2
@Controller
public class MainController {

    @Autowired
    private JobService jobService;

    @Autowired
    private EmailService emailService;


    @GetMapping("/")
    public String index() {
        String session = RequestContextHolder.currentRequestAttributes().getSessionId();
        return "redirect:/?session="+session;
    }

    @GetMapping(value = "/", params = "session")
    public String indexWithSession( Model model, @RequestParam(value = "session") String session ) {
        model.addAttribute("jobs", jobService.getJobsForUser( session ).getBody());
        model.addAttribute("sessionId", session);
        return "index";
    }

    @GetMapping("/job-table")
    public String getJobTable( Model model, @RequestParam(value = "session", required = false) String session ) {
        if (session == null || session.isEmpty()) {
            session = RequestContextHolder.currentRequestAttributes().getSessionId();
        }

        model.addAttribute("jobs", jobService.getJobsForUser( session ).getBody());

        return "index :: #job-table";
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
        return ResponseEntity.ok().body(jobs.stream().filter( j -> j.getResult() == null ).count());
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
    public String contact( Model model) {
        model.addAttribute("contactForm", new ContactForm());
        return "contact";
    }

    @PostMapping("/contact")
    public String contact( Model model,
                           HttpServletRequest request,
                           @Valid ContactForm contactForm,
                           BindingResult bindingResult ) {
        if (bindingResult.hasErrors()) {
            return "contact";
        }

        log.info( contactForm );
        try {
            emailService.sendSupportMessage( contactForm.getMessage(), contactForm.getName(), contactForm.getEmail(), request, contactForm.getAttachment() );
            model.addAttribute( "message", "Sent. We will get back to you shortly." );
            model.addAttribute( "success", true );
        } catch ( MessagingException | MailSendException e) {
            log.error(e);
            model.addAttribute( "message", "There was a problem sending the support request. Please try again later." );
            model.addAttribute( "success", false );
        }

        return "contact";
    }

    @GetMapping("/maintenance")
    public String contact() {
        return "maintenance";
    }
}
