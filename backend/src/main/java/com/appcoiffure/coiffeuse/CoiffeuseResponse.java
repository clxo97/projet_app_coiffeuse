package com.appcoiffure.coiffeuse;

import java.time.Instant;

public record CoiffeuseResponse(
        Long id,
        String nom,
        String nomSalon,
        String email,
        SubscriptionStatus subscriptionStatus,
        Instant abonnementActifJusquAu,
        boolean abonnementActif,
        String modeleSmsConfirmation,
        String modeleSmsModification,
        String modeleSmsRappel
) {
    public static CoiffeuseResponse from(Coiffeuse coiffeuse) {
        return new CoiffeuseResponse(
                coiffeuse.getId(),
                coiffeuse.getNom(),
                coiffeuse.getNomSalon(),
                coiffeuse.getEmail(),
                coiffeuse.getSubscriptionStatus(),
                coiffeuse.getAbonnementActifJusquAu(),
                coiffeuse.hasActiveSubscription(),
                coiffeuse.getModeleSmsConfirmation(),
                coiffeuse.getModeleSmsModification(),
                coiffeuse.getModeleSmsRappel()
        );
    }
}
