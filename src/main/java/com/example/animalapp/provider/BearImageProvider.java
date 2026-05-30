package com.example.animalapp.provider;

import com.example.animalapp.domain.AnimalType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Fetches bear pictures. The configured endpoint (loremflickr.com by default)
 * returns image bytes directly.
 */
@Component
public class BearImageProvider extends AbstractHttpImageProvider {

    private final ProviderProperties properties;

    public BearImageProvider(RestClient imageRestClient, ProviderProperties properties) {
        super(imageRestClient);
        this.properties = properties;
    }

    @Override
    public AnimalType animalType() {
        return AnimalType.BEAR;
    }

    @Override
    public FetchedImage fetch() {
        return download(properties.getBearUrl());
    }
}
