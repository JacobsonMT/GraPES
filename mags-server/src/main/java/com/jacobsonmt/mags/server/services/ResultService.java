package com.jacobsonmt.mags.server.services;

import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.Lists;
import com.jacobsonmt.mags.server.dao.PrecomputedResultDao;
import com.jacobsonmt.mags.server.entities.PrecomputedResult;
import com.jacobsonmt.mags.server.entities.PrecomputedResult.Species;
import com.jacobsonmt.mags.server.model.result.Distribution;
import com.jacobsonmt.mags.server.model.result.Result;
import com.jacobsonmt.mags.server.model.search.FieldSearch;
import com.jacobsonmt.mags.server.model.search.SearchCriteria;
import com.jacobsonmt.mags.server.model.search.SearchResponse;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final PrecomputedResultDao precomputedResultDao;

    private Map<Species, Map<Feature, List<Number>>> speciesBackgroundDistributions = new ConcurrentHashMap<>();
    private Map<Species, List<Marker>> markers = new ConcurrentHashMap<>();
    private Map<Species, List<Feature>> speciesFeatureSet = new ConcurrentHashMap<>();

    public enum Feature {
        abundance("Abundance", PrecomputedResult::getAbd),
        camsol("Camsol", PrecomputedResult::getCsl),
        annotatedPhosphorylationSites("Annotated Phosphorylation Sites", PrecomputedResult::getPhs),
        pScore("P Score", PrecomputedResult::getPip),
        disorder("Disorder", PrecomputedResult::getDiso),
        compositionD("% Composition D", PrecomputedResult::getD),
        compositionE("% Composition E", PrecomputedResult::getE),
        compositionL("% Composition L", PrecomputedResult::getL),
        compositionG("% Composition G", PrecomputedResult::getG);

        private final Function<PrecomputedResult, Number> extract;
        private final String title;

        Feature(String title, Function<PrecomputedResult, Number> extract) {
            this.extract = extract;
            this.title = title;
        }
    }

    @Value
    public static class Marker {
//        String name; // TODO
        String accession;
        Species species;
        Map<Feature, Number> features;
    }


    public ResultService(PrecomputedResultDao precomputedResultDao) {this.precomputedResultDao = precomputedResultDao;}

    @PostConstruct
    public void prepopulateBackgroundDistributions() {
        speciesFeatureSet.put(Species.HUMAN, Lists.newArrayList(
            Feature.abundance,
            Feature.camsol,
            Feature.annotatedPhosphorylationSites,
            Feature.pScore,
            Feature.disorder,
            Feature.compositionL,
            Feature.compositionG
        ));

        speciesFeatureSet.put(Species.YEAST, Lists.newArrayList(
            Feature.abundance,
            Feature.camsol,
            Feature.annotatedPhosphorylationSites,
            Feature.pScore,
            Feature.compositionD,
            Feature.compositionE
        ));

        Feature[] features = Feature.values();
        precomputedResultDao.findAll().forEach( result -> {
            Map<Feature, List<Number>> featureMap = speciesBackgroundDistributions.computeIfAbsent(
                result.getSpecies(),
                k -> new ConcurrentHashMap<>());

            for (Feature feature : speciesFeatureSet.get(result.getSpecies())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    featureMap.computeIfAbsent(feature, k -> new ArrayList<>()).add(val);
                }
            }
        });

        markers = precomputedResultDao.findByMarkerTrue().stream().map( result -> {
            Map<Feature, Number> markerFeatures = new HashMap<>();

            for (Feature feature : speciesFeatureSet.get(result.getSpecies())) {
                Number val = feature.extract.apply(result);
                if (val != null) {
                    markerFeatures.put(feature, val);
                }
            }
            return new Marker(result.getAccession(), result.getSpecies(), markerFeatures);
        }).collect(groupingBy(Marker::getSpecies));
    }

    public Optional<List<Distribution>> calculateDistributions(String accession) {
        return precomputedResultDao.findById(accession).map(this::calculateDistributions);
    }

    private List<Distribution> calculateDistributions(PrecomputedResult result) {
        List<Distribution> distributions = new ArrayList<>();

        for (Feature feature : speciesFeatureSet.get(result.getSpecies())) {
            try {
                Distribution dist = new Distribution(feature.name(), feature.title, feature.extract.apply(result));
                dist.setBackground(speciesBackgroundDistributions.get(result.getSpecies()).get(feature));
                dist.setMarkers(markers.get(result.getSpecies()).stream()
                    .collect(Collectors.toMap(Marker::getAccession, m-> m.getFeatures().get(feature)))
                );
                distributions.add(dist);
            } catch (Exception e) {
                log.error("Problem creating distribution data for feature: {} in species: {}", feature, result.getSpecies());
            }
        }

        return distributions;
    }

    public Optional<Result> getResultByAccession(String accession) {
        return precomputedResultDao.findById(accession).map(Result::fromPrecomputedResult);
    }

    public Optional<SearchResponse> search(SearchCriteria searchCriteria) {
        try {
            Specification<PrecomputedResult> spec = Specification.where(null);
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

            Page<PrecomputedResult> res = precomputedResultDao.findAll(
                spec, pageRequest
            );

            return Optional.of(new SearchResponse(
                res.getContent().stream().map(Result::fromPrecomputedResult).collect(Collectors.toList()),
                1000000,
                res.getTotalElements()
            ));
        } catch (Exception e) {
            log.error("Issue searching for precomputed result using search criteria: {}", searchCriteria);
            return Optional.empty();
        }
    }
}
