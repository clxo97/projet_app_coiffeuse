package com.appcoiffure.coiffeuse;

import java.time.Instant;

import com.appcoiffure.config.PasswordService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CoiffeuseInitializer implements CommandLineRunner {

    private final CoiffeuseRepository coiffeuseRepository;
    private final PasswordService passwordService;
    private final String nom;
    private final String email;
    private final String motDePasse;

    public CoiffeuseInitializer(
            CoiffeuseRepository coiffeuseRepository,
            PasswordService passwordService,
            @Value("${app.initial-coiffeuse.nom}") String nom,
            @Value("${app.initial-coiffeuse.email}") String email,
            @Value("${app.initial-coiffeuse.password}") String motDePasse
    ) {
        this.coiffeuseRepository = coiffeuseRepository;
        this.passwordService = passwordService;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    @Override
    public void run(String... args) {
        if (coiffeuseRepository.count() > 0) {
            return;
        }

        Coiffeuse coiffeuse = new Coiffeuse(
                nom,
                null,
                email,
                passwordService.hash(motDePasse),
                SubscriptionStatus.TRIAL,
                Instant.now().plusSeconds(7L * 24 * 60 * 60)
        );
        coiffeuseRepository.save(coiffeuse);
    }
}
