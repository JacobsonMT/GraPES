package com.jacobsonmt.mags.server.rest;

import com.jacobsonmt.mags.server.dao.PrecomputedResultDao;
import com.jacobsonmt.mags.server.model.result.Distribution;
import com.jacobsonmt.mags.server.model.result.Result;
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

    private final PrecomputedResultDao precomputedResultDao;
    private final ResultService resultService;

    public ResultEndpoint(PrecomputedResultDao precomputedResultDao,
        ResultService resultService) {this.precomputedResultDao = precomputedResultDao;
        this.resultService = resultService;
    }

    /**
     * @return Precomputed result for given accession.
     */
    @RequestMapping(value = "/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Result> getPrecomputedResultForAccession( @PathVariable String accession ) {

        return resultService.getResultByAccession(accession)
            .map(ResponseEntity::ok).orElse(
                ResponseEntity.status( HttpStatus.NOT_FOUND ).body( null )
            );
    }

    /**
     * @return Precomputed feature distributions for given accession.
     */
    @RequestMapping(value = "/{accession}/distributions", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Distribution>> getDistributionForAccession( @PathVariable String accession ) {

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

}
