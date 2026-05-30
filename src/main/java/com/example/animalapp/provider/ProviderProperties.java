package com.example.animalapp.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised configuration for the upstream image providers.
 *
 * <p>The suggested placeholder services in the challenge (placekitten.com,
 * place.dog, placebear.com) have become unreliable, so every endpoint is
 * configurable and defaults to a service that is currently healthy. Override
 * any of these via {@code application.yml} or environment variables, e.g.
 * {@code ANIMALAPP_PROVIDERS_CAT_URL}.
 */
@ConfigurationProperties(prefix = "animalapp.providers")
public class ProviderProperties {

    /** Cat endpoint that returns image bytes directly. */
    private String catUrl = "https://cataas.com/cat";

    /** Dog endpoint returning JSON of the form {@code {"message": "<url>", "status": "success"}}. */
    private String dogApiUrl = "https://dog.ceo/api/breeds/image/random";

    /** Bear endpoint that returns image bytes directly. */
    private String bearUrl = "https://loremflickr.com/640/480/bear";

    public String getCatUrl() {
        return catUrl;
    }

    public void setCatUrl(String catUrl) {
        this.catUrl = catUrl;
    }

    public String getDogApiUrl() {
        return dogApiUrl;
    }

    public void setDogApiUrl(String dogApiUrl) {
        this.dogApiUrl = dogApiUrl;
    }

    public String getBearUrl() {
        return bearUrl;
    }

    public void setBearUrl(String bearUrl) {
        this.bearUrl = bearUrl;
    }
}
