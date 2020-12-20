package com.jacobsonmt.mags.ui.services;

import com.jacobsonmt.mags.ui.model.result.Distribution;
import com.jacobsonmt.mags.ui.model.result.Result;
import com.jacobsonmt.mags.ui.model.search.SearchCriteria;
import com.jacobsonmt.mags.ui.model.search.SearchResponse;
import com.jacobsonmt.mags.ui.settings.ApplicationSettings;
import java.io.IOException;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
public class ResultService {

    private final ApplicationSettings applicationSettings;

    public ResultService(
        ApplicationSettings applicationSettings) {this.applicationSettings = applicationSettings;}


    public ResponseEntity<Result> getPrecomputedResult(String accession) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .errorHandler( new NoOpResponseErrorHandler() ).build();
        HttpEntity entity = new HttpEntity(createHeaders());
        // getForObject cannot specify headers so we use exchange

        log.info( "Get Result: {}", accession );
        return restTemplate.exchange(
                        applicationSettings.getProcessServerURI() + "/results/{accession}",
                HttpMethod.GET,
                entity,
                Result.class,
                accession
        );
    }

    public ResponseEntity<List<Distribution>> getResultDistributions(String accession) {
        RestTemplate restTemplate = new RestTemplateBuilder()
            .errorHandler( new NoOpResponseErrorHandler() ).build();
        HttpEntity entity = new HttpEntity(createHeaders());
        // getForObject cannot specify headers so we use exchange

        log.info( "Get Result: {}", accession );
        return restTemplate.exchange(
            applicationSettings.getProcessServerURI() + "/results/{accession}/distributions",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<Distribution>>(){},
            accession
        );
    }

    public ResponseEntity<SearchResponse> search(SearchCriteria searchCriteria) {
        RestTemplate restTemplate = new RestTemplateBuilder()
            .errorHandler( new NoOpResponseErrorHandler() ).build();
        HttpEntity entity = new HttpEntity(searchCriteria, createHeaders());

        return restTemplate.exchange(
            applicationSettings.getProcessServerURI() + "/results/precomputed/search",
            HttpMethod.POST,
            entity,
            SearchResponse.class
        );
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set( "client", applicationSettings.getClientId() );
        headers.set( "auth_token", applicationSettings.getClientToken() );
        return headers;
    }

    private static class NoOpResponseErrorHandler extends
            DefaultResponseErrorHandler {

        @Override
        public void handleError( ClientHttpResponse response) throws IOException {
        }

    }
}
