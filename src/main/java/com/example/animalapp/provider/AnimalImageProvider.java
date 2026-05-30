package com.example.animalapp.provider;

import com.example.animalapp.domain.AnimalType;

/**
 * Strategy for fetching a single random image of a particular {@link AnimalType}
 * from some external source.
 *
 * <p>Each implementation isolates the quirks of one upstream API (direct image
 * bytes vs. a JSON indirection step, etc.). Endpoints are configuration-driven
 * (see {@link ProviderProperties}) so a flaky placeholder service can be swapped
 * out without code changes.
 */
public interface AnimalImageProvider {

    AnimalType animalType();

    FetchedImage fetch();
}
