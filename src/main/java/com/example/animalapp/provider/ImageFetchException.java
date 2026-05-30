package com.example.animalapp.provider;

/**
 * Raised when an image cannot be fetched from an upstream provider.
 */
public class ImageFetchException extends RuntimeException {

    public ImageFetchException(String message) {
        super(message);
    }

    public ImageFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
