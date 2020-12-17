package com.jacobsonmt.mags.server.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ClientSettings {

    private Map<String, ApplicationClient> clients = new ConcurrentHashMap<>();

    @Getter
    @Setter
    @ToString
    public static class ApplicationClient {

        private String name;
        private String token;

        /**
         * Maximum number of jobs a client can have in the processing queue
         */
        private int processLimit = 2;

        /**
         * Maximum number of jobs a client can have in total (processing + client queue + user queue)
         */
        private int jobLimit = 100;

        /**
         * Maximum number of jobs a user can have in the client queue
         */
        private int userClientLimit = 1;

        /**
         * Maximum number of jobs a user can have in their user queue
         */
        private int userJobLimit = 20;

    }
}
