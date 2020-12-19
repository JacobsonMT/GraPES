package com.jacobsonmt.mags.server.rest;

import com.jacobsonmt.mags.server.dao.PrecomputedResultDao;
import com.jacobsonmt.mags.server.entities.PrecomputedResult;
import com.jacobsonmt.mags.server.model.search.FieldSearch;
import com.jacobsonmt.mags.server.model.search.SearchCriteria;
import com.jacobsonmt.mags.server.model.search.SearchResponse;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
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

    public ResultEndpoint(PrecomputedResultDao precomputedResultDao) {this.precomputedResultDao = precomputedResultDao;}

    /**
     * @return Precomputed result for given accession.
     */
    @RequestMapping(value = "/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PrecomputedResult> getPrecomputedResultForAccession( @PathVariable String accession ) {

        return precomputedResultDao.findById(accession)
            .map(ResponseEntity::ok).orElse(
                ResponseEntity.status( HttpStatus.NOT_FOUND ).body( null )
            );
    }

    /**
     * @return Precomputed result for given accession.
     */
    @RequestMapping(value = "/precomputed/search", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<SearchResponse> search( @RequestBody SearchCriteria searchCriteria ) {
        Specification<PrecomputedResult> spec = Specification.where(null);
        for (FieldSearch fieldSearch : searchCriteria.getFieldSearches()) {
            spec = spec.or((result, cq, cb) -> cb.like(
                cb.lower(result.get(fieldSearch.getField())), "%" + fieldSearch.getQuery().toLowerCase() + "%")
            );
        }

        Sort sort = Sort.by(
            searchCriteria.getFieldSorts().stream().map(
                fs -> new Order(fs.isAsc() ? Direction.ASC : Direction.DESC, fs.getField())).collect(Collectors.toList()
            )
        );
        PageRequest pageRequest = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize(), sort);

        Page<PrecomputedResult> res = precomputedResultDao.findAll(
            spec, pageRequest
        );

        SearchResponse searchResponse = new SearchResponse(
            res.getContent(),
            1000000,
            res.getTotalElements()
        );

        return ResponseEntity.ok(searchResponse);
    }

}
