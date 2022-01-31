package com.jacobsonmt.mags.server.rest;

import com.jacobsonmt.mags.server.model.result.Graph;
import com.jacobsonmt.mags.server.model.result.MaGSResult;
import com.jacobsonmt.mags.server.model.search.SearchCriteria;
import com.jacobsonmt.mags.server.model.search.SearchResponse;
import com.jacobsonmt.mags.server.services.ResultService;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequestMapping("/api/results")
@RestController
public class ResultEndpoint {

    private final ResultService resultService;

    public ResultEndpoint(ResultService resultService) {
        this.resultService = resultService;
    }

    /**
     * @return Precomputed result for given accession.
     */
    @RequestMapping(value = "/precomputed/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MaGSResult> getPrecomputedResultForAccession( @PathVariable String accession ) {

        return resultService.getResultByAccession(accession)
            .map(ResponseEntity::ok).orElse(
                ResponseEntity.status( HttpStatus.NOT_FOUND ).body( null )
            );
    }

    /**
     * @return Precomputed feature graphs for given accession.
     */
    @RequestMapping(value = "/precomputed/{accession}/graphs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Graph>> getGraphsForAccession( @PathVariable String accession ) {

        return resultService.calculateDistributions(accession)
            .map(ResponseEntity::ok).orElse(
                ResponseEntity.status( HttpStatus.NOT_FOUND ).body( null )
            );
    }

    /**
     * @return Precomputed result for given accession.
     */
    @RequestMapping(value = "/precomputed/search", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<SearchResponse> search( @RequestBody SearchCriteria searchCriteria ) {
        return resultService.search(searchCriteria).map(ResponseEntity::ok).orElse(
            ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null )
        );
    }

    /* Jobs */

    /**
     * @return Jobs feature graphs for given id.
     */
    @RequestMapping(value = "/jobs/{id}/graphs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Graph>> getGraphsForAccession( @PathVariable long id ) {

        return resultService.calculateDistributionsForJobId(id)
            .map(ResponseEntity::ok).orElse(
                ResponseEntity.status( HttpStatus.NOT_FOUND ).body( null )
            );
    }

}
