package com.example.animalapp.web.dto;

import java.util.List;

/**
 * Response body for the "fetch and store" endpoint.
 *
 * @param type     the requested animal type
 * @param count    how many pictures were stored
 * @param pictures metadata for each stored picture
 */
public record FetchResponse(String type, int count, List<PictureMetadata> pictures) {
}
