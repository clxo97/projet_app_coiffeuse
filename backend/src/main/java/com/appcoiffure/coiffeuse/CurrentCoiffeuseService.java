package com.appcoiffure.coiffeuse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentCoiffeuseService {

    private final CoiffeuseRepository coiffeuseRepository;
    private final String adminEmail;

    public CurrentCoiffeuseService(
            CoiffeuseRepository coiffeuseRepository,
            @Value("${app.admin.email}") String adminEmail
    ) {
        this.coiffeuseRepository = coiffeuseRepository;
        this.adminEmail = normalizeEmail(adminEmail);
    }

    public Coiffeuse requireCurrent(HttpServletRequest request) {
        Object email = request.getAttribute("coiffeuseEmail");

        if (!(email instanceof String coiffeuseEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }

        return coiffeuseRepository.findByEmail(coiffeuseEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Compte introuvable"));
    }

    public Coiffeuse requireActive(HttpServletRequest request) {
        Coiffeuse coiffeuse = requireCurrent(request);

        if (!coiffeuse.hasActiveSubscription()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Abonnement actif requis");
        }

        return coiffeuse;
    }

    public Coiffeuse requireAdmin(HttpServletRequest request) {
        Coiffeuse coiffeuse = requireCurrent(request);

        if (!isAdmin(coiffeuse)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces administrateur requis");
        }

        return coiffeuse;
    }

    public boolean isAdmin(Coiffeuse coiffeuse) {
        return normalizeEmail(coiffeuse.getEmail()).equals(adminEmail);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
