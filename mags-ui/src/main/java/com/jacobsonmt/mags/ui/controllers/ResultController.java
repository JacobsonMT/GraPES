package com.jacobsonmt.mags.ui.controllers;

import com.google.common.collect.Lists;
import com.jacobsonmt.mags.ui.exceptions.ResultNotFoundException;
import com.jacobsonmt.mags.ui.model.datatable.DataTableRequest;
import com.jacobsonmt.mags.ui.model.datatable.DataTableResponse;
import com.jacobsonmt.mags.ui.model.search.FieldSearch;
import com.jacobsonmt.mags.ui.model.search.FieldSort;
import com.jacobsonmt.mags.ui.model.search.SearchCriteria;
import com.jacobsonmt.mags.ui.model.search.SearchResponse;
import com.jacobsonmt.mags.ui.services.ResultSearchService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Log4j2
@Controller
public class ResultController {

    private final ResultSearchService resultSearchService;

    public ResultController(ResultSearchService resultSearchService) {this.resultSearchService = resultSearchService;}

    @RequestMapping(value = "/results/precomputed/{accession}", method = RequestMethod.GET)
    public String protein( @PathVariable("accession") String accession, Model model) {
        if (accession == null) {
            throw new ResultNotFoundException();
        }

        ResponseEntity<?> result = resultSearchService.getPrecomputedResult(accession);

        model.addAttribute("result", result );

        return "precomputed";
    }

    @GetMapping("/api/results/precomputed/{accession}")
    @ResponseBody
    public ResponseEntity<?> getPrecomputedResult( @PathVariable("accession") String accession, Model model) {

        if (accession == null) {
            return ResponseEntity.notFound().build();
        }

        return resultSearchService.getPrecomputedResult(accession);

    }

    @GetMapping("/api/results/precomputed/{accession}/download")
    @ResponseBody
    public ResponseEntity<?> download( @PathVariable("accession") String accession,
        Model model) throws IOException {
        // TODO

        if (accession == null) {
            return ResponseEntity.notFound().build();
        }

        return resultSearchService.getPrecomputedResult(accession);

    }

    @RequestMapping( value = "/api/results/precomputed/datatable", method = RequestMethod.POST )
    public ResponseEntity<?> precomputedDataTable( @RequestBody final DataTableRequest dataTablesRequest ) {
        String query = dataTablesRequest.getSearch().getValue().toLowerCase();
        List<FieldSearch> searches = new ArrayList<>();
        if (!StringUtils.isEmpty(query)) {
            for ( DataTableRequest.Column col : dataTablesRequest.getColumns() ) {
                if ( col.isSearchable() ) {
                    switch ( col.getData() ) {
                        case "accession":
//                        case "species":
//                        case "v1":
                            searches.add(new FieldSearch(col.getData(), query));
                            break;
                        default:
                            // Do Nothing
                    }
                }
            }
        }


        List<FieldSort> sorts = Lists.newArrayList();
        for ( DataTableRequest.Order order : dataTablesRequest.getOrders() ) {
            try {
                DataTableRequest.Column col = dataTablesRequest.getColumns().get( order.getColumn() );
                switch ( col.getData() ) {
                    case "accession":
                    case "species":
                    case "v1":
                        sorts.add(new FieldSort(col.getData(), order.getDir().equals("asc")));
                        break;
                    default:
                        // Do Nothing
                }

            } catch ( IndexOutOfBoundsException e ) {
                // Ignore
                log.warn( "Attempted to order by column that doesn't exist: " + order.getColumn() );
            }
        }

        SearchCriteria searchCriteria = new SearchCriteria(
            searches,
            sorts,
            dataTablesRequest.getStart() / dataTablesRequest.getLength(),
            dataTablesRequest.getLength()
        );

        ResponseEntity<SearchResponse> results = resultSearchService.search(searchCriteria);

        if (results.getBody() != null) {

            DataTableResponse response = new DataTableResponse();
            response.setData(results.getBody().getData());
            response.setDraw(dataTablesRequest.getDraw());
            response.setRecordsTotal(results.getBody().getTotal());
            response.setRecordsFiltered(results.getBody().getMatched());

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.noContent().build();
    }

}
