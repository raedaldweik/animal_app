package com.example.animalapp.provider;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import okio.Buffer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the provider HTTP logic against a {@link MockWebServer}, including the
 * dog provider's two-step (JSON -> image) fetch.
 */
class ImageProvidersTest {

    private MockWebServer server;
    private RestClient restClient;
    private ProviderProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        restClient = RestClient.builder().build();
        properties = new ProviderProperties();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private static Buffer imageBuffer(byte[] bytes) {
        Buffer buffer = new Buffer();
        buffer.write(bytes);
        return buffer;
    }

    @Test
    void catProviderReturnsImageBytesAndContentType() {
        byte[] bytes = {1, 2, 3, 4};
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "image/jpeg")
                .setBody(imageBuffer(bytes)));
        properties.setCatUrl(server.url("/cat").toString());

        FetchedImage image = new CatImageProvider(restClient, properties).fetch();

        assertThat(image.data()).containsExactly(bytes);
        assertThat(image.contentType()).contains("image/jpeg");
    }

    @Test
    void dogProviderResolvesJsonThenDownloadsImage() {
        byte[] bytes = {10, 20, 30};
        String imageUrl = server.url("/images/dog.png").toString();
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"" + imageUrl + "\",\"status\":\"success\"}"));
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "image/png")
                .setBody(imageBuffer(bytes)));
        properties.setDogApiUrl(server.url("/api/random").toString());

        FetchedImage image = new DogImageProvider(restClient, properties).fetch();

        assertThat(image.data()).containsExactly(bytes);
        assertThat(image.contentType()).contains("image/png");
        assertThat(image.sourceUrl()).isEqualTo(imageUrl);
    }

    @Test
    void providerThrowsImageFetchExceptionOnUpstreamError() {
        server.enqueue(new MockResponse().setResponseCode(503));
        properties.setBearUrl(server.url("/bear").toString());

        assertThatThrownBy(() -> new BearImageProvider(restClient, properties).fetch())
                .isInstanceOf(ImageFetchException.class);
    }

    @Test
    void dogProviderThrowsWhenJsonHasNoImageUrl() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"success\"}"));
        properties.setDogApiUrl(server.url("/api/random").toString());

        assertThatThrownBy(() -> new DogImageProvider(restClient, properties).fetch())
                .isInstanceOf(ImageFetchException.class);
    }
}
