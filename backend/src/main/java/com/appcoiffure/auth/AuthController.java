package com.appcoiffure.auth;

import java.time.Instant;

import com.appcoiffure.config.JwtService;
import com.appcoiffure.config.PasswordService;
import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CoiffeuseRepository;
import com.appcoiffure.coiffeuse.SubscriptionStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CoiffeuseRepository coiffeuseRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthController(
            CoiffeuseRepository coiffeuseRepository,
            PasswordService passwordService,
            JwtService jwtService
    ) {
        this.coiffeuseRepository = coiffeuseRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.email() == null || request.motDePasse() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Coiffeuse coiffeuse = coiffeuseRepository.findByEmail(request.email())
                .filter(found -> passwordService.matches(request.motDePasse(), found.getMotDePasse()))
                .orElse(null);

        if (coiffeuse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(coiffeuse);

        return ResponseEntity.ok(new LoginResponse(
                token,
                "Bearer",
                coiffeuse.getId(),
                coiffeuse.getNom(),
                coiffeuse.getEmail(),
                coiffeuse.getSubscriptionStatus(),
                coiffeuse.getAbonnementActifJusquAu(),
                coiffeuse.hasActiveSubscription()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        if (isBlank(request.nom()) || isBlank(request.email()) || isBlank(request.motDePasse())) {
            return ResponseEntity.badRequest().build();
        }

        if (request.motDePasse().trim().length() < 8) {
            return ResponseEntity.badRequest().build();
        }

        String email = request.email().trim().toLowerCase();
        if (coiffeuseRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Coiffeuse coiffeuse = new Coiffeuse(
                request.nom().trim(),
                cleanOptional(request.nomSalon()),
                email,
                passwordService.hash(request.motDePasse()),
                SubscriptionStatus.TRIAL,
                Instant.now().plusSeconds(7L * 24 * 60 * 60)
        );

        Coiffeuse saved = coiffeuseRepository.save(coiffeuse);
        String token = jwtService.generateToken(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(
                token,
                "Bearer",
                saved.getId(),
                saved.getNom(),
                saved.getEmail(),
                saved.getSubscriptionStatus(),
                saved.getAbonnementActifJusquAu(),
                saved.hasActiveSubscription()
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String cleanOptional(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }
}
