package com.jacobsonmt.mags.ui.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.jacobsonmt.mags.ui.settings.ApplicationSettings;
import java.time.Duration;
import javax.annotation.PostConstruct;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("downstream")
public class BackendHealthIndicator implements HealthIndicator {

    private final ApplicationSettings applicationSettings;

    private final RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    public BackendHealthIndicator(ApplicationSettings applicationSettings,
        RestTemplateBuilder restTemplateBuilder) {
        this.applicationSettings = applicationSettings;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @PostConstruct
    private void postConstruct() {
        restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(2))
            .build();
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                applicationSettings.getProcessServerHost() + "/actuator/health",
                HttpMethod.GET,
                new HttpEntity<>( createHeaders() ),
                JsonNode.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String status = responseEntity.getBody().get("status").textValue();
                if (status.equals("UP")) {
                    return Health.up().withDetail("status", status).build();
                } else {
                    return Health.down().build();
                }
            } else {
                return Health.down().build();
            }
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set( "client", applicationSettings.getClientId() );
        headers.set( "auth_token", applicationSettings.getClientToken() );
        return headers;
    }

}