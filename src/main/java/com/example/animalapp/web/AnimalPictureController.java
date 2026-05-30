package com.example.animalapp.web;

import com.example.animalapp.domain.AnimalPicture;
import com.example.animalapp.domain.AnimalType;
import com.example.animalapp.service.AnimalPictureService;
import com.example.animalapp.web.dto.FetchResponse;
import com.example.animalapp.web.dto.PictureMetadata;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pictures")
@Validated
public class AnimalPictureController {

    private final AnimalPictureService service;

    public AnimalPictureController(AnimalPictureService service) {
        this.service = service;
    }

    /**
     * Fetches {@code count} pictures of the given animal {@code type} from the
     * upstream provider, stores them, and returns their metadata.
     *
     * <p>Example: {@code POST /api/v1/pictures?type=cat&count=3}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FetchResponse fetchAndStore(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") @Min(1) @Max(50) int count) {

        AnimalType animalType = AnimalType.from(type);
        List<PictureMetadata> stored = service.fetchAndStore(animalType, count).stream()
                .map(PictureMetadata::from)
                .toList();
        return new FetchResponse(animalType.name().toLowerCase(), stored.size(), stored);
    }

    /**
     * Returns the raw bytes of the most recently stored picture for the given
     * animal {@code type}. Example: {@code GET /api/v1/pictures/cat/last}
     */
    @GetMapping("/{type}/last")
    public ResponseEntity<byte[]> getLastPicture(@PathVariable String type) {
        AnimalType animalType = AnimalType.from(type);
        AnimalPicture picture = service.getLastPicture(animalType)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No stored picture for animal type " + type));
        return imageResponse(picture);
    }

    /**
     * Returns metadata (without bytes) for the most recently stored picture of
     * the given animal {@code type}. Example: {@code GET /api/v1/pictures/cat/last/info}
     */
    @GetMapping("/{type}/last/info")
    public PictureMetadata getLastPictureInfo(@PathVariable String type) {
        AnimalType animalType = AnimalType.from(type);
        return service.getLastPicture(animalType)
                .map(PictureMetadata::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No stored picture for animal type " + type));
    }

    /**
     * Returns the raw bytes of a stored picture by its id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getById(@PathVariable Long id) {
        AnimalPicture picture = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No picture with id " + id));
        return imageResponse(picture);
    }

    private ResponseEntity<byte[]> imageResponse(AnimalPicture picture) {
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(picture.getContentType());
        } catch (RuntimeException ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(picture.getData());
    }
}
