package com.appcoiffure.admin;

import java.time.Instant;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.SubscriptionStatus;

public record AdminCoiffeuseResponse(
        Long id,
        String nom,
        String nomSalon,
        String email,
        SubscriptionStatus subscriptionStatus,
        Instant abonnementActifJusquAu,
        boolean abonnementActif,
        Instant creeLe
) {
    public static AdminCoiffeuseResponse from(Coiffeuse coiffeuse) {
        return new AdminCoiffeuseResponse(
                coiffeuse.getId(),
                coiffeuse.getNom(),
                coiffeuse.getNomSalon(),
                coiffeuse.getEmail(),
                coiffeuse.getSubscriptionStatus(),
                coiffeuse.getAbonnementActifJusquAu(),
                coiffeuse.hasActiveSubscription(),
                coiffeuse.getCreeLe()
        );
    }
}
