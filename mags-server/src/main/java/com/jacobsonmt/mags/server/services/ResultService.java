package com.jacobsonmt.mags.server.services;

import com.google.common.collect.Lists;
import com.jacobsonmt.mags.server.dao.JobResultDao;
import com.jacobsonmt.mags.server.dao.PrecomputedMaGSResultDao;
import com.jacobsonmt.mags.server.dao.PrecomputedMaGSSeqResultDao;
import com.jacobsonmt.mags.server.entities.JobResult;
import com.jacobsonmt.mags.server.entities.MaGSSeqResult;
import com.jacobsonmt.mags.server.entities.PrecomputedMaGSResult;
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
    private final JobResultDao jobResultDao;

    private Map<Species, Map<MaGSFeature, Distribution>> backgroundMaGSDistributions = new ConcurrentHashMap<>();
    private Map<Species, Map<MaGSSeqFeature, Distribution>> backgroundMaGSSeqDistributions = new ConcurrentHashMap<>();

    public enum MaGSFeature {
        score("MaGS Z-Score", PrecomputedMaGSResult::getZScore),
        abundance("Abundance", PrecomputedMaGSResult::getAbd),
        camsol("Camsol", PrecomputedMaGSResult::getCsl),
        annotatedPhosphorylationSites("Annotated Phosphorylation Sites", PrecomputedMaGSResult::getPhs),
        propensityScore("Propensity Score", PrecomputedMaGSResult::getPip),
        disorder("Disorder", PrecomputedMaGSResult::getDiso),
        compositionD("% Composition D", PrecomputedMaGSResult::getD),
        compositionE("% Composition E", PrecomputedMaGSResult::getE),
        compositionL("% Composition L", PrecomputedMaGSResult::getL),
        compositionG("% Composition G", PrecomputedMaGSResult::getG);

        private final Function<PrecomputedMaGSResult, Number> extract;
        private final String title;

        MaGSFeature(String title, Function<PrecomputedMaGSResult, Number> extract) {
            this.extract = extract;
            this.title = title;
        }
    }

    public enum MaGSSeqFeature {
        score("MaGSeq Z-Score", MaGSSeqResult::getZScore),
        disorder("Disorder", MaGSSeqResult::getDiso),
        propensityScore("Propensity Score", MaGSSeqResult::getPip),
        rbpPred("RBP Pred", MaGSSeqResult::getRbp),
        soluprot("Soluprot", MaGSSeqResult::getSol),
        length("Length", MaGSSeqResult::getLen),
        tango("Tango", MaGSSeqResult::getTgo),
        compositionG("% Composition G", MaGSSeqResult::getG),
        compositionR("% Composition R", MaGSSeqResult::getR),
        compositionL("% Composition L", MaGSSeqResult::getL),
        compositionD("% Composition D", MaGSSeqResult::getD),
        compositionP("% Composition P", MaGSSeqResult::getP),
        compositionS("% Composition S", MaGSSeqResult::getS);

        private final Function<MaGSSeqResult, Number> extract;
        private final String title;

        MaGSSeqFeature(String title, Function<MaGSSeqResult, Number> extract) {
            this.extract = extract;
            this.title = title;
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
        JobResultDao jobResultDao) {
        this.precomputedMaGSResultDao = precomputedMaGSResultDao;
        this.precomputedMaGSSeqResultDao = precomputedMaGSSeqResultDao;
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
            MaGSFeature.propensityScore,
            MaGSFeature.disorder,
            MaGSFeature.compositionL,
            MaGSFeature.compositionG
        ));

        speciesFeatureSet.put(Species.YEAST, Lists.newArrayList(
            MaGSFeature.score,
            MaGSFeature.abundance,
            MaGSFeature.camsol,
            MaGSFeature.annotatedPhosphorylationSites,
            MaGSFeature.propensityScore,
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

        precomputedMaGSResultDao.findByMarkerTrue().forEach( result -> {
            Map<MaGSFeature, Distribution> featureMap = backgroundMaGSDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new LinkedHashMap<>());

            for (MaGSFeature feature : speciesFeatureSet.getOrDefault(result.getSpecies(), new ArrayList<>())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new Distribution()).getMarkers().put(result.getAccession(), val);
                }
            }
        });

        /* Jobs background */
        Map<Species, List<MaGSSeqFeature>> speciesMaGSSeqFeatureSet = new HashMap<>();
        speciesMaGSSeqFeatureSet.put(Species.HUMAN, Lists.newArrayList(
            MaGSSeqFeature.score,
            MaGSSeqFeature.disorder,
            MaGSSeqFeature.propensityScore,
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
            MaGSSeqFeature.score,
            MaGSSeqFeature.disorder,
            MaGSSeqFeature.propensityScore,
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

        precomputedMaGSSeqResultDao.findByMarkerTrue().forEach( result -> {
            Map<MaGSSeqFeature, Distribution> featureMap = backgroundMaGSSeqDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new LinkedHashMap<>());

            for (MaGSSeqFeature feature : speciesMaGSSeqFeatureSet.getOrDefault(result.getSpecies(), new ArrayList<>())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new Distribution()).getMarkers().put(result.getAccession(), val);
                }
            }
        });
    }

    public Optional<List<Graph>> calculateDistributions(String accession) {
        return precomputedMaGSResultDao.findById(accession).map(this::calculateDistributions);
    }

    private List<Graph> calculateDistributions(PrecomputedMaGSResult result) {
        List<Graph> graphs = new ArrayList<>();

        backgroundMaGSDistributions.getOrDefault(result.getSpecies(), new HashMap<>()).forEach( (feature, distribution) -> {
            try {
                graphs.add( new Graph(feature.name(), feature.title, feature.extract.apply(result), distribution) );
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

    public Optional<List<Graph>> calculateDistributionsForJobId(long id) {
        return jobResultDao.findById(id).map(this::calculateGraphs);
    }

    private List<Graph> calculateGraphs(JobResult result) {
        List<Graph> graphs = new ArrayList<>();

        backgroundMaGSSeqDistributions.getOrDefault(result.getSpecies(), new HashMap<>()).forEach( (feature, distribution) -> {
            try {
                graphs.add( new Graph(feature.name(), feature.title, feature.extract.apply(result), distribution) );
            } catch (Exception e) {
                log.error("Problem creating graph data for feature: {} in species: {}", feature, result.getSpecies(), e);
            }
        });

        return graphs;
    }

    public Optional<MaGSSeqResultVO> getResultByJobId(long id) {
        return jobResultDao.findById(id).map(MaGSSeqResultVO::fromPrecomputedResult);
    }
}
