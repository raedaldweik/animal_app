package com.example.animalapp.domain;

import java.util.Locale;

/**
 * Supported animal types. Parsing is case-insensitive so the REST API accepts
 * {@code cat}, {@code CAT}, {@code Cat}, etc.
 */
public enum AnimalType {
    CAT,
    DOG,
    BEAR;

    public static AnimalType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Animal type must not be empty");
        }
        try {
            return AnimalType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown animal type '" + value + "'. Supported types: cat, dog, bear");
        }
    }
}
