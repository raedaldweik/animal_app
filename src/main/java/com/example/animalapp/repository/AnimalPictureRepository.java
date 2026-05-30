package com.example.animalapp.repository;

import com.example.animalapp.domain.AnimalPicture;
import com.example.animalapp.domain.AnimalType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimalPictureRepository extends JpaRepository<AnimalPicture, Long> {

    /**
     * Returns the most recently stored picture for the given animal type.
     * Ordering falls back to {@code id} so insertion order is honoured even when
     * several rows share the same {@code created_at} timestamp.
     */
    Optional<AnimalPicture> findFirstByAnimalTypeOrderByCreatedAtDescIdDesc(AnimalType animalType);
}
