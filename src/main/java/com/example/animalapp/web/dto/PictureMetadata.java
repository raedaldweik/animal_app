package com.example.animalapp.web.dto;

import com.example.animalapp.domain.AnimalPicture;
import com.example.animalapp.domain.AnimalType;

import java.time.Instant;

/**
 * JSON-friendly view of a stored picture (without the raw bytes). The {@code url}
 * field points at the endpoint that serves the actual image.
 */
public record PictureMetadata(
        Long id,
        AnimalType type,
        String contentType,
        String sourceUrl,
        int sizeBytes,
        Instant createdAt,
        String url) {

    public static PictureMetadata from(AnimalPicture picture) {
        return new PictureMetadata(
                picture.getId(),
                picture.getAnimalType(),
                picture.getContentType(),
                picture.getSourceUrl(),
                picture.getSizeBytes(),
                picture.getCreatedAt(),
                "/api/v1/pictures/" + picture.getId());
    }
}
