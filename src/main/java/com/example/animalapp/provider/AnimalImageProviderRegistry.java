package com.example.animalapp.provider;

import com.example.animalapp.domain.AnimalType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the {@link AnimalImageProvider} responsible for a given
 * {@link AnimalType}. Providers self-register via Spring's component scanning,
 * so adding a new animal is as simple as adding a new provider bean.
 */
@Component
public class AnimalImageProviderRegistry {

    private final Map<AnimalType, AnimalImageProvider> providers = new EnumMap<>(AnimalType.class);

    public AnimalImageProviderRegistry(List<AnimalImageProvider> providerBeans) {
        for (AnimalImageProvider provider : providerBeans) {
            providers.put(provider.animalType(), provider);
        }
    }

    public AnimalImageProvider get(AnimalType animalType) {
        AnimalImageProvider provider = providers.get(animalType);
        if (provider == null) {
            throw new IllegalArgumentException("No image provider configured for animal type " + animalType);
        }
        return provider;
    }
}
