package com.example.animalapp.service;

import com.example.animalapp.domain.AnimalPicture;
import com.example.animalapp.domain.AnimalType;
import com.example.animalapp.provider.AnimalImageProvider;
import com.example.animalapp.provider.AnimalImageProviderRegistry;
import com.example.animalapp.provider.FetchedImage;
import com.example.animalapp.repository.AnimalPictureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AnimalPictureService {

    private static final Logger log = LoggerFactory.getLogger(AnimalPictureService.class);

    private final AnimalImageProviderRegistry providerRegistry;
    private final AnimalPictureRepository repository;

    public AnimalPictureService(AnimalImageProviderRegistry providerRegistry,
                                AnimalPictureRepository repository) {
        this.providerRegistry = providerRegistry;
        this.repository = repository;
    }

    /**
     * Fetches {@code count} pictures of the given {@code type} from the upstream
     * provider and stores them. Returns the persisted entities in fetch order.
     */
    @Transactional
    public List<AnimalPicture> fetchAndStore(AnimalType type, int count) {
        AnimalImageProvider provider = providerRegistry.get(type);
        List<AnimalPicture> saved = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            FetchedImage image = provider.fetch();
            AnimalPicture picture = new AnimalPicture(
                    type, image.contentType(), image.sourceUrl(), image.data());
            saved.add(repository.save(picture));
        }

        log.info("Stored {} {} picture(s)", saved.size(), type);
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<AnimalPicture> getLastPicture(AnimalType type) {
        return repository.findFirstByAnimalTypeOrderByCreatedAtDescIdDesc(type);
    }

    @Transactional(readOnly = true)
    public Optional<AnimalPicture> getById(Long id) {
        return repository.findById(id);
    }
}
