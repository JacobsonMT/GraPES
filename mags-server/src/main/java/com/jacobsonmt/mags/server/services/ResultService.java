package com.jacobsonmt.mags.server.services;

import com.google.common.collect.Lists;
import com.jacobsonmt.mags.server.dao.JobResultDao;
import com.jacobsonmt.mags.server.dao.MaGSMarkerDao;
import com.jacobsonmt.mags.server.dao.MaGSeqMarkerDao;
import com.jacobsonmt.mags.server.dao.PrecomputedMaGSResultDao;
import com.jacobsonmt.mags.server.dao.PrecomputedMaGSSeqResultDao;
import com.jacobsonmt.mags.server.entities.JobResult;
import com.jacobsonmt.mags.server.entities.MaGSSeqResult;
import com.jacobsonmt.mags.server.entities.PrecomputedMaGSResult;
import com.jacobsonmt.mags.server.entities.PrecomputedMaGSSeqResult;
import com.jacobsonmt.mags.server.entities.Species;
import com.jacobsonmt.mags.server.model.result.Distribution;
import com.jacobsonmt.mags.server.model.result.Graph;
import com.jacobsonmt.mags.server.model.result.MaGSResult;
import com.jacobsonmt.mags.server.model.result.MaGSSeqResultVO;
import com.jacobsonmt.mags.server.model.search.FieldSearch;
import com.jacobsonmt.mags.server.model.search.SearchCriteria;
import com.jacobsonmt.mags.server.model.search.SearchResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ResultService {

    private final PrecomputedMaGSResultDao precomputedMaGSResultDao;
    private final PrecomputedMaGSSeqResultDao precomputedMaGSSeqResultDao;
    private final MaGSMarkerDao maGSMarkerDao;
    private final MaGSeqMarkerDao maGSeqMarkerDao;
    private final JobResultDao jobResultDao;

    private Map<Species, Map<MaGSFeature, Distribution>> backgroundMaGSDistributions = new ConcurrentHashMap<>();
    private Map<Species, Map<MaGSSeqFeature, Distribution>> backgroundMaGSSeqDistributions = new ConcurrentHashMap<>();

    public enum MaGSFeature {
        score("MaGS Z-Score", PrecomputedMaGSResult::getZScore, "This is the MaGS z-score, the higher the value the more likely the protein is predicted to be in a biological condensate.  However, other complications, like cell localization could play a role."),
        abundance("Abundance", PrecomputedMaGSResult::getAbd, "The amount of protein contained within the cell, as reported in the PAXdb integrated proteomes.  Units are in ppm."),
        camsol("Camsol", PrecomputedMaGSResult::getCsl, "Protein solubility as determined by the Camsol method.  Higher numbers indicate proteins that tend to remain in solution while lower numbers indicate that a protein is aggregation-prone."),
        annotatedPhosphorylationSites("Annotated Phosphorylation Sites", PrecomputedMaGSResult::getPhs, "The total number of experimentally observed phosphorylation sites on a protein.  This number indicates the potential for modification."),
        pScore("PScore", PrecomputedMaGSResult::getPip, "A metric to indicate the amount of π-π interactions within a protein.  Indicates that a protein is more likely to phase separate in vitro."),
        disorder("Disorder", PrecomputedMaGSResult::getDiso, "The percent of residues within the protein that are predicted to be disordered by DISOPRED3. Many hypotheses suggest that disordered regions in proteins can modulate protein solubility."),
        compositionD("% Composition D", PrecomputedMaGSResult::getD, "% Composition D"),
        compositionE("% Composition E", PrecomputedMaGSResult::getE, "% Composition E"),
        compositionL("% Composition L", PrecomputedMaGSResult::getL, "% Composition L"),
        compositionG("% Composition G", PrecomputedMaGSResult::getG, "% Composition G");

        private final Function<PrecomputedMaGSResult, Number> extract;
        private final String title;
        private final String description;

        MaGSFeature(String title, Function<PrecomputedMaGSResult, Number> extract, String description) {
            this.extract = extract;
            this.title = title;
            this.description = description;
        }
    }

    public enum MaGSSeqFeature {
        scoreHuman("MaGSeq Z-Score", MaGSSeqResult::getZScoreHuman, "This is the MaGSeq z-score, the higher the value the more likely the protein is predicted to be in a biological condensate.  However, other complications, like cell localization could play a role."),
        scoreYeast("MaGSeq Z-Score", MaGSSeqResult::getZScoreYeast, "This is the MaGSeq z-score, the higher the value the more likely the protein is predicted to be in a biological condensate.  However, other complications, like cell localization could play a role."),
        disorder("Disorder", MaGSSeqResult::getDiso, "The percent of residues within the protein that are predicted to be disordered by DISOPRED3. Many hypotheses suggest that disordered regions in proteins can modulate protein solubility."),
        pScore("PScore", MaGSSeqResult::getPip, "A metric to indicate the amount of π-π interactions within a protein.  Indicates that a protein is more likely to phase separate in vitro."),
        rbpPred("RBP Pred", MaGSSeqResult::getRbp, "Likelihood prediction for a protein to be an RNA-binding protein.  If score is over 0.5, then it is considered to interact with RNA."),
        soluprot("Soluprot", MaGSSeqResult::getSol, "A protein solubility score where higher numbers indicate higher solubility."),
        length("Length", MaGSSeqResult::getLen, "Number of amino acids."),
        tango("Tango", MaGSSeqResult::getTgo, "A score which indicates a likelihood for cross-beta protein aggregation. Higher scores indicate that the protein has regions which are aggregation-prone."),
        compositionG("% Composition G", MaGSSeqResult::getG, "% Composition G"),
        compositionR("% Composition R", MaGSSeqResult::getR, "% Composition R"),
        compositionL("% Composition L", MaGSSeqResult::getL, "% Composition L"),
        compositionD("% Composition D", MaGSSeqResult::getD, "% Composition D"),
        compositionP("% Composition P", MaGSSeqResult::getP, "% Composition P"),
        compositionS("% Composition S", MaGSSeqResult::getS, "% Composition S");

        private final Function<MaGSSeqResult, Number> extract;
        private final String title;
        private final String description;

        MaGSSeqFeature(String title, Function<MaGSSeqResult, Number> extract, String description) {
            this.extract = extract;
            this.title = title;
            this.description = description;
        }
    }

    @Value
    public static class Marker {
//        String name; // TODO
        String accession;
        Species species;
        Map<MaGSFeature, Number> features;
    }


    public ResultService(
        PrecomputedMaGSResultDao precomputedMaGSResultDao,
        PrecomputedMaGSSeqResultDao precomputedMaGSSeqResultDao,
        MaGSMarkerDao maGSMarkerDao, MaGSeqMarkerDao maGSeqMarkerDao,
        JobResultDao jobResultDao) {
        this.precomputedMaGSResultDao = precomputedMaGSResultDao;
        this.precomputedMaGSSeqResultDao = precomputedMaGSSeqResultDao;
        this.maGSMarkerDao = maGSMarkerDao;
        this.maGSeqMarkerDao = maGSeqMarkerDao;
        this.jobResultDao = jobResultDao;
    }

    @PostConstruct
    public void prepopulateBackgroundDistributions() {
        Map<Species, List<MaGSFeature>> speciesFeatureSet = new HashMap<>();
        speciesFeatureSet.put(Species.HUMAN, Lists.newArrayList(
            MaGSFeature.score,
            MaGSFeature.abundance,
            MaGSFeature.camsol,
            MaGSFeature.annotatedPhosphorylationSites,
            MaGSFeature.pScore,
            MaGSFeature.disorder,
            MaGSFeature.compositionL,
            MaGSFeature.compositionG
        ));

        speciesFeatureSet.put(Species.YEAST, Lists.newArrayList(
            MaGSFeature.score,
            MaGSFeature.abundance,
            MaGSFeature.camsol,
            MaGSFeature.annotatedPhosphorylationSites,
            MaGSFeature.pScore,
            MaGSFeature.compositionD,
            MaGSFeature.compositionE
        ));

        precomputedMaGSResultDao.findAll().forEach( result -> {
            Map<MaGSFeature, Distribution> featureMap = backgroundMaGSDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new LinkedHashMap<>());

            for (MaGSFeature feature : speciesFeatureSet.getOrDefault(result.getSpecies(), new ArrayList<>())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new Distribution()).add(val);
                }
            }
        });

        maGSMarkerDao.findAll().forEach( marker -> {
            PrecomputedMaGSResult result = marker.getResult();
            Map<MaGSFeature, Distribution> featureMap = backgroundMaGSDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new LinkedHashMap<>());

            for (MaGSFeature feature : speciesFeatureSet.getOrDefault(result.getSpecies(), new ArrayList<>())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new Distribution()).getMarkers().put(marker.getLabel(), val);
                }
            }
        });

        /* Jobs background */
        Map<Species, List<MaGSSeqFeature>> speciesMaGSSeqFeatureSet = new HashMap<>();
        speciesMaGSSeqFeatureSet.put(Species.HUMAN, Lists.newArrayList(
            MaGSSeqFeature.scoreHuman,
            MaGSSeqFeature.disorder,
            MaGSSeqFeature.pScore,
            MaGSSeqFeature.soluprot,
            MaGSSeqFeature.length,
            MaGSSeqFeature.tango,
            MaGSSeqFeature.compositionG,
            MaGSSeqFeature.compositionR,
            MaGSSeqFeature.compositionL,
            MaGSSeqFeature.compositionD,
            MaGSSeqFeature.compositionP,
            MaGSSeqFeature.compositionS
        ));

        speciesMaGSSeqFeatureSet.put(Species.YEAST, Lists.newArrayList(
            MaGSSeqFeature.scoreYeast,
            MaGSSeqFeature.disorder,
            MaGSSeqFeature.pScore,
            MaGSSeqFeature.rbpPred,
            MaGSSeqFeature.soluprot,
            MaGSSeqFeature.length,
            MaGSSeqFeature.tango,
            MaGSSeqFeature.compositionG,
            MaGSSeqFeature.compositionR,
            MaGSSeqFeature.compositionL,
            MaGSSeqFeature.compositionD,
            MaGSSeqFeature.compositionP,
            MaGSSeqFeature.compositionS
        ));
        precomputedMaGSSeqResultDao.findAll().forEach( result -> {
            Map<MaGSSeqFeature, Distribution> featureMap = backgroundMaGSSeqDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new LinkedHashMap<>());

            for (MaGSSeqFeature feature : speciesMaGSSeqFeatureSet.getOrDefault(result.getSpecies(), new ArrayList<>())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new Distribution()).add(val);
                }
            }
        });

        maGSeqMarkerDao.findAll().forEach( marker -> {
            PrecomputedMaGSSeqResult result = marker.getResult();
            Map<MaGSSeqFeature, Distribution> featureMap = backgroundMaGSSeqDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new LinkedHashMap<>());

            for (MaGSSeqFeature feature : speciesMaGSSeqFeatureSet.getOrDefault(result.getSpecies(), new ArrayList<>())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new Distribution()).getMarkers().put(marker.getLabel(), val);
                }
            }
        });

        log.info("Building kernel density distributions");
        /* Build all kernel density distributions and clear background data to free memory*/
        backgroundMaGSDistributions.values().stream().flatMap(m -> m.values().stream()).forEach(d -> {
            d.buildKernelDensityEstimate();
            d.getBackground().clear();
        });
        backgroundMaGSSeqDistributions.values().stream().flatMap(m -> m.values().stream()).forEach(d -> {
            d.buildKernelDensityEstimate();
            d.getBackground().clear();
        });
        log.info("Finished building kernel density distributions");
    }

    public Optional<List<Graph>> calculateDistributions(String accession) {
        return precomputedMaGSResultDao.findById(accession).map(this::calculateDistributions);
    }

    private List<Graph> calculateDistributions(PrecomputedMaGSResult result) {
        List<Graph> graphs = new ArrayList<>();

        backgroundMaGSDistributions.getOrDefault(result.getSpecies(), new HashMap<>()).forEach( (feature, distribution) -> {
            try {
                graphs.add( new Graph(feature.name(), feature.title, feature.description, feature.extract.apply(result), distribution) );
            } catch (Exception e) {
                log.error("Problem creating graph data for feature: {} in species: {}", feature, result.getSpecies(), e);
            }
        });

        return graphs;
    }

    public Optional<MaGSResult> getResultByAccession(String accession) {
        return precomputedMaGSResultDao.findById(accession).map(MaGSResult::fromPrecomputedResult);
    }

    public Optional<SearchResponse> search(SearchCriteria searchCriteria) {
        try {
            Specification<PrecomputedMaGSResult> spec = Specification.where(null);
            for (FieldSearch fieldSearch : searchCriteria.getFieldSearches()) {
                spec = spec.or((result, cq, cb) -> cb.like(
                    cb.lower(result.get(fieldSearch.getField())), "%" + fieldSearch.getQuery().toLowerCase() + "%")
                );
            }

            Sort sort = Sort.by(
                searchCriteria.getFieldSorts().stream().map(
                    fs -> new Order(fs.isAsc() ? Direction.ASC : Direction.DESC, fs.getField()))
                    .collect(Collectors.toList()
                    )
            );
            PageRequest pageRequest = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize(), sort);

            Page<PrecomputedMaGSResult> res = precomputedMaGSResultDao.findAll(
                spec, pageRequest
            );

            return Optional.of(new SearchResponse(
                res.getContent().stream().map(MaGSResult::fromPrecomputedResult).collect(Collectors.toList()),
                1000000,
                res.getTotalElements()
            ));
        } catch (Exception e) {
            log.error("Issue searching for precomputed result using search criteria: {}", searchCriteria);
            return Optional.empty();
        }
    }

    /* Jobs */

    /**
     * Use species selected when job was ran
     */
    public Optional<List<Graph>> calculateDistributionsForJobId(long id) {
        return jobResultDao.findById(id).map(JobResult::getJob).map(job ->
            calculateGraphs(job.getResult(), job.getSpecies()));
    }

    /**
     * Use alternate species
     */
    public Optional<List<Graph>> calculateDistributionsForJobId(long id, Species species) {
        return jobResultDao.findById(id).map(jr -> calculateGraphs(jr, species));
    }

    private List<Graph> calculateGraphs(JobResult result, Species species) {
        List<Graph> graphs = new ArrayList<>();

        backgroundMaGSSeqDistributions.getOrDefault(species, new HashMap<>()).forEach( (feature, distribution) -> {
            try {
                graphs.add( new Graph(feature.name(), feature.title, feature.description, feature.extract.apply(result), distribution) );
            } catch (Exception e) {
                log.error("Problem creating graph data for feature: {} in species: {}", feature, species, e);
            }
        });

        return graphs;
    }

    /**
     * Use species selected when job was ran
     */
    public Optional<MaGSSeqResultVO> getResultByJobId(long id) {
        return jobResultDao.findById(id).map(JobResult::getJob).map(job ->
            MaGSSeqResultVO.fromPrecomputedResult(job.getResult(), job.getSpecies()));
    }

    /**
     * Use alternate species
     */
    public Optional<MaGSSeqResultVO> getResultByJobId(long id, Species species) {
        return jobResultDao.findById(id).map(jr ->
            MaGSSeqResultVO.fromPrecomputedResult(jr, species));
    }
}
