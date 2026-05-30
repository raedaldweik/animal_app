package com.example.animalapp.provider;

/**
 * An image fetched from an external provider, ready to be persisted.
 *
 * @param data        the raw image bytes
 * @param contentType the MIME type reported by the provider (e.g. {@code image/jpeg})
 * @param sourceUrl   the URL the image was ultimately fetched from
 */
public record FetchedImage(byte[] data, String contentType, String sourceUrl) {
}
