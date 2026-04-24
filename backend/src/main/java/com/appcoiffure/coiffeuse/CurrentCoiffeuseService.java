package com.appcoiffure.coiffeuse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentCoiffeuseService {

    private final CoiffeuseRepository coiffeuseRepository;

    public CurrentCoiffeuseService(CoiffeuseRepository coiffeuseRepository) {
        this.coiffeuseRepository = coiffeuseRepository;
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
}
