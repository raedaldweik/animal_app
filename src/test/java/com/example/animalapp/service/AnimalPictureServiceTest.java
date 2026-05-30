package com.example.animalapp.service;

import com.example.animalapp.domain.AnimalPicture;
import com.example.animalapp.domain.AnimalType;
import com.example.animalapp.provider.AnimalImageProvider;
import com.example.animalapp.provider.AnimalImageProviderRegistry;
import com.example.animalapp.provider.FetchedImage;
import com.example.animalapp.repository.AnimalPictureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalPictureServiceTest {

    @Mock
    AnimalImageProviderRegistry registry;

    @Mock
    AnimalPictureRepository repository;

    @Mock
    AnimalImageProvider catProvider;

    @Test
    void fetchAndStoreFetchesOncePerRequestedPictureAndSavesEach() {
        when(registry.get(AnimalType.CAT)).thenReturn(catProvider);
        when(catProvider.fetch())
                .thenReturn(new FetchedImage(new byte[]{1, 2, 3}, "image/jpeg", "http://example/cat"));
        // echo the entity back on save
        when(repository.save(any(AnimalPicture.class))).thenAnswer(inv -> inv.getArgument(0));

        AnimalPictureService service = new AnimalPictureService(registry, repository);
        List<AnimalPicture> result = service.fetchAndStore(AnimalType.CAT, 3);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(p -> p.getAnimalType() == AnimalType.CAT);
        verify(catProvider, times(3)).fetch();
        verify(repository, times(3)).save(any(AnimalPicture.class));
    }

    @Test
    void getLastPictureDelegatesToRepository() {
        AnimalPicture picture = new AnimalPicture(AnimalType.DOG, "image/png", "http://example/dog", new byte[]{9});
        when(repository.findFirstByAnimalTypeOrderByCreatedAtDescIdDesc(AnimalType.DOG))
                .thenReturn(Optional.of(picture));

        AnimalPictureService service = new AnimalPictureService(registry, repository);

        assertThat(service.getLastPicture(AnimalType.DOG)).contains(picture);
    }
}
