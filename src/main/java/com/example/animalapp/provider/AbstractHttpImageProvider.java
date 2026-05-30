package com.example.animalapp.provider;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Base class with the shared logic for downloading raw image bytes from a URL.
 */
abstract class AbstractHttpImageProvider implements AnimalImageProvider {

    private static final String DEFAULT_CONTENT_TYPE = MediaType.IMAGE_JPEG_VALUE;

    protected final RestClient restClient;

    protected AbstractHttpImageProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Downloads the bytes at {@code url} and wraps them in a {@link FetchedImage},
     * preserving the upstream {@code Content-Type} when present.
     */
    protected FetchedImage download(String url) {
        try {
            var response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                throw new ImageFetchException("Empty image body returned from " + url);
            }

            MediaType mediaType = response.getHeaders().getContentType();
            String contentType = mediaType != null ? mediaType.toString() : DEFAULT_CONTENT_TYPE;
            return new FetchedImage(body, contentType, url);
        } catch (RestClientException ex) {
            throw new ImageFetchException("Failed to fetch image from " + url + ": " + ex.getMessage(), ex);
        }
    }
}
