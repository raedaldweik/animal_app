package com.example.animalapp.config;

import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Shared {@link RestClient} used by all image providers, with sensible
     * connect/read timeouts so a hanging upstream cannot block request threads
     * indefinitely.
     */
    @Bean
    public RestClient imageRestClient(RestClient.Builder builder) {
        var settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(15));
        return builder
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }
}
