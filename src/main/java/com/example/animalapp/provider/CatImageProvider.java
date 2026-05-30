package com.example.animalapp.provider;

import com.example.animalapp.domain.AnimalType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Fetches cat pictures. The configured endpoint (cataas.com by default) returns
 * image bytes directly.
 */
@Component
public class CatImageProvider extends AbstractHttpImageProvider {

    private final ProviderProperties properties;

    public CatImageProvider(RestClient imageRestClient, ProviderProperties properties) {
        super(imageRestClient);
        this.properties = properties;
    }

    @Override
    public AnimalType animalType() {
        return AnimalType.CAT;
    }

    @Override
    public FetchedImage fetch() {
        return download(properties.getCatUrl());
    }
}
