package com.example.animalapp;

import com.example.animalapp.provider.ProviderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProviderProperties.class)
public class AnimalAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimalAppApplication.class, args);
    }
}
