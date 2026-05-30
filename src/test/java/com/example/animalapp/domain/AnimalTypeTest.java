package com.example.animalapp.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimalTypeTest {

    @Test
    void parsesCaseInsensitively() {
        assertThat(AnimalType.from("cat")).isEqualTo(AnimalType.CAT);
        assertThat(AnimalType.from("DOG")).isEqualTo(AnimalType.DOG);
        assertThat(AnimalType.from(" Bear ")).isEqualTo(AnimalType.BEAR);
    }

    @Test
    void rejectsUnknownType() {
        assertThatThrownBy(() -> AnimalType.from("dragon"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dragon");
    }

    @Test
    void rejectsBlankType() {
        assertThatThrownBy(() -> AnimalType.from("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
