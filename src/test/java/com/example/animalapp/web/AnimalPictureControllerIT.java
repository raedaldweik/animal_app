package com.example.animalapp.web;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack test: real Spring context + embedded H2 + a {@link MockWebServer}
 * standing in for the upstream cat provider. Verifies the fetch/store and
 * "last picture" flows end to end, plus validation error handling.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnimalPictureControllerIT {

    private static final byte[] CAT_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01, 0x02};

    static MockWebServer upstream;

    @Autowired
    TestRestTemplate rest;

    @BeforeAll
    static void startServer() throws IOException {
        upstream = new MockWebServer();
        upstream.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        upstream.shutdown();
    }

    @DynamicPropertySource
    static void providerProps(DynamicPropertyRegistry registry) {
        registry.add("animalapp.providers.cat-url", () -> upstream.url("/cat").toString());
    }

    private static Buffer catImage() {
        Buffer buffer = new Buffer();
        buffer.write(CAT_BYTES);
        return buffer;
    }

    @Test
    void fetchStoresPicturesAndLastReturnsLatestImage() {
        upstream.enqueue(new MockResponse().setHeader("Content-Type", "image/jpeg").setBody(catImage()));
        upstream.enqueue(new MockResponse().setHeader("Content-Type", "image/jpeg").setBody(catImage()));

        ResponseEntity<String> post =
                rest.postForEntity("/api/v1/pictures?type=cat&count=2", null, String.class);

        assertThat(post.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(post.getBody()).contains("\"count\":2").contains("\"type\":\"cat\"");

        ResponseEntity<byte[]> last = rest.getForEntity("/api/v1/pictures/cat/last", byte[].class);
        assertThat(last.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(last.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(last.getBody()).containsExactly(CAT_BYTES);
    }

    @Test
    void lastReturns404WhenNothingStored() {
        ResponseEntity<String> response = rest.getForEntity("/api/v1/pictures/bear/last", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unknownTypeReturns400() {
        ResponseEntity<String> response =
                rest.postForEntity("/api/v1/pictures?type=dragon&count=1", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void invalidCountReturns400() {
        ResponseEntity<String> response =
                rest.postForEntity("/api/v1/pictures?type=cat&count=0", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
