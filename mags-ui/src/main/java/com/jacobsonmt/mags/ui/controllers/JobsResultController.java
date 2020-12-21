package com.jacobsonmt.mags.ui.controllers;

import com.jacobsonmt.mags.ui.exceptions.ResultNotFoundException;
import com.jacobsonmt.mags.ui.model.result.Graph;
import com.jacobsonmt.mags.ui.model.result.MaGSSeqResult;
import com.jacobsonmt.mags.ui.services.ResultService;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Log4j2
@Controller
public class JobsResultController {

    private final ResultService resultService;

    public JobsResultController(ResultService resultService) {this.resultService = resultService;}

    @RequestMapping(value = "/results/jobs/{id}", method = RequestMethod.GET)
    public String precomputed( @PathVariable("id") long id, Model model) {
        if (id == 0) {
            throw new ResultNotFoundException();
        }

        ResponseEntity<MaGSSeqResult> result = resultService.getJobsResult(id);
        ResponseEntity<List<Graph>> graphs = resultService.getJobsResultGraphs(id);

        model.addAttribute("result", result.getBody() );
        model.addAttribute("graphs", graphs.getBody() );

        return "job";
    }

    @GetMapping("/api/results/jobs/{id}/graphs")
    @ResponseBody
    public ResponseEntity<List<Graph>> getResultDistributions( @PathVariable("id") long id, Model model) {

        if (id == 0) {
            return ResponseEntity.notFound().build();
        }

        return resultService.getJobsResultGraphs(id);

    }

    @GetMapping("/api/results/jobs/{id}")
    @ResponseBody
    public ResponseEntity<MaGSSeqResult> getPrecomputedResult( @PathVariable("id") long id, Model model) {

        if (id == 0) {
            return ResponseEntity.notFound().build();
        }

        return resultService.getJobsResult(id);

    }

}
