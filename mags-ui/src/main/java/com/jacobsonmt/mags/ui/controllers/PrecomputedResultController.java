package com.jacobsonmt.mags.ui.controllers;

import static com.jacobsonmt.mags.ui.controllers.Utils.downloadAsFile;
import static com.jacobsonmt.mags.ui.controllers.Utils.jsonToCsv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jacobsonmt.mags.ui.exceptions.ResultNotFoundException;
import com.jacobsonmt.mags.ui.model.datatable.DataTableRequest;
import com.jacobsonmt.mags.ui.model.datatable.DataTableResponse;
import com.jacobsonmt.mags.ui.model.result.Graph;
import com.jacobsonmt.mags.ui.model.result.MaGSResult;
import com.jacobsonmt.mags.ui.model.search.FieldSearch;
import com.jacobsonmt.mags.ui.model.search.FieldSort;
import com.jacobsonmt.mags.ui.model.search.SearchCriteria;
import com.jacobsonmt.mags.ui.model.search.SearchResponse;
import com.jacobsonmt.mags.ui.services.ResultService;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
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
@AllArgsConstructor
public class PrecomputedResultController {

    private final ResultService resultService;
    private final ObjectMapper objectMapper;


    @RequestMapping(value = "/precomputed/{accession}", method = RequestMethod.GET)
    public String precomputed( @PathVariable("accession") String accession, Model model) {
        if (accession == null) {
            throw new ResultNotFoundException();
        }

        ResponseEntity<MaGSResult> result = resultService.getPrecomputedResult(accession);
        ResponseEntity<List<Graph>> graphs = resultService.getPrecomputedResultGraphs(accession);

        model.addAttribute("result", result.getBody() );
        model.addAttribute("graphs", graphs.getBody() );

        return "result";
    }

    @GetMapping("/api/precomputed/{accession}/graphs")
    @ResponseBody
    public ResponseEntity<List<Graph>> getResultDistributions( @PathVariable("accession") String accession, Model model) {

        if (accession == null) {
            return ResponseEntity.notFound().build();
        }

        return resultService.getPrecomputedResultGraphs(accession);

    }

    @GetMapping("/api/precomputed/{accession}")
    @ResponseBody
    public ResponseEntity<MaGSResult> getPrecomputedResult( @PathVariable("accession") String accession, Model model) {

        if (accession == null) {
            return ResponseEntity.notFound().build();
        }

        return resultService.getPrecomputedResult(accession);

    }

    @GetMapping("/api/precomputed/{accession}/csv")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> getPrecomputedResultAsCSV( @PathVariable("accession") String accession, Model model)
        throws JsonProcessingException {

        if (accession == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<MaGSResult> result = resultService.getPrecomputedResult(accession);

        if (result.getStatusCode().is2xxSuccessful() && result.getBody() != null) {
            return downloadAsFile( accession + ".csv",  jsonToCsv(objectMapper.valueToTree(result.getBody())));
        }

        return ResponseEntity.status(result.getStatusCode()).build();
    }

    @RequestMapping( value = "/api/precomputed/datatable", method = RequestMethod.POST )
    public ResponseEntity<?> precomputedDataTable( @RequestBody final DataTableRequest dataTablesRequest ) {
        String query = dataTablesRequest.getSearch().getValue().toLowerCase();
        List<FieldSearch> searches = new ArrayList<>();
        if (!StringUtils.isEmpty(query)) {
            for ( DataTableRequest.Column col : dataTablesRequest.getColumns() ) {
                if ( col.isSearchable() ) {
                    switch ( col.getData() ) {
                        case "accession":
                        case "gene":
//                        case "species":
//                        case "v1":
                            searches.add(new FieldSearch(col.getData(), query));
                            searches.add(new FieldSearch("synonyms", query));
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
                    case "gene":
                        sorts.add(new FieldSort(col.getData(), order.getDir().equals("asc")));
                        break;
                    case "score":
                        sorts.add(new FieldSort("zScore", order.getDir().equals("asc")));
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

        ResponseEntity<SearchResponse> results = resultService.search(searchCriteria);

        if (!results.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(results.getStatusCode()).build();
        }

        if (results.getBody() == null) {
            return ResponseEntity.noContent().build();
        }

        DataTableResponse response = new DataTableResponse();
        response.setData(results.getBody().getData());
        response.setDraw(dataTablesRequest.getDraw());
        response.setRecordsTotal(results.getBody().getTotal());
        response.setRecordsFiltered(results.getBody().getMatched());

        return ResponseEntity.ok(response);
    }

}
