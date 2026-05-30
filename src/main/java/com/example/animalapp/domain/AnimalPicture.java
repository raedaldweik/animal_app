package com.example.animalapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A single animal picture together with its raw bytes and metadata.
 *
 * <p>The image bytes are stored as a plain {@code byte[]} column (mapped to
 * {@code VARBINARY} on H2 and {@code bytea} on PostgreSQL). This avoids the
 * PostgreSQL large-object pitfalls that {@code @Lob byte[]} introduces, while
 * remaining portable across both databases.
 */
@Entity
@Table(
        name = "animal_picture",
        indexes = @Index(name = "idx_animal_picture_type_created", columnList = "animal_type, created_at")
)
public class AnimalPicture {

    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024; // 10 MB

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", nullable = false, length = 16)
    private AnimalType animalType;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "source_url", nullable = false, length = 1024)
    private String sourceUrl;

    @Column(name = "data", nullable = false, length = MAX_IMAGE_BYTES)
    private byte[] data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AnimalPicture() {
        // for JPA
    }

    public AnimalPicture(AnimalType animalType, String contentType, String sourceUrl, byte[] data) {
        this.animalType = animalType;
        this.contentType = contentType;
        this.sourceUrl = sourceUrl;
        this.data = data;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AnimalType getAnimalType() {
        return animalType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public byte[] getData() {
        return data;
    }

    public int getSizeBytes() {
        return data == null ? 0 : data.length;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
