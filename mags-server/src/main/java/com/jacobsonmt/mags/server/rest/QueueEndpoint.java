package com.jacobsonmt.mags.server.rest;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.services.JobService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequestMapping("/api/queue")
@RestController
public class QueueEndpoint {

    @Autowired
    private JobService jobService;

    @RequestMapping(value = "/client/{clientId}/user/{userId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Job>> getJobs( @PathVariable String clientId, @PathVariable String userId,
                                            @RequestParam(value = "withResults", defaultValue = "false") boolean withResults  ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String client = authentication.getName();
        if ( !client.equals( clientId ) && !client.equals( "admin" ) ) {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body( new ArrayList<>() );
        }
        return ResponseEntity.ok( jobService.getJobs( userId ) );
    }

}
