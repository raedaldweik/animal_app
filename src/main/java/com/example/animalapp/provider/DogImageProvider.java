package com.example.animalapp.provider;

import com.example.animalapp.domain.AnimalType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Fetches dog pictures. The configured endpoint (dog.ceo by default) returns a
 * JSON envelope pointing at the actual image, so this provider performs a
 * two-step fetch: resolve the URL, then download the bytes.
 */
@Component
public class DogImageProvider extends AbstractHttpImageProvider {

    private final ProviderProperties properties;

    public DogImageProvider(RestClient imageRestClient, ProviderProperties properties) {
        super(imageRestClient);
        this.properties = properties;
    }

    @Override
    public AnimalType animalType() {
        return AnimalType.DOG;
    }

    @Override
    public FetchedImage fetch() {
        String apiUrl = properties.getDogApiUrl();
        DogApiResponse response;
        try {
            response = restClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .body(DogApiResponse.class);
        } catch (RestClientException ex) {
            throw new ImageFetchException("Failed to query dog API at " + apiUrl + ": " + ex.getMessage(), ex);
        }

        if (response == null || response.message() == null || response.message().isBlank()) {
            throw new ImageFetchException("Dog API at " + apiUrl + " did not return an image URL");
        }
        return download(response.message());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DogApiResponse(String message, String status) {
    }
}
