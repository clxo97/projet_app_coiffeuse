package com.appcoiffure.auth;

import java.time.Instant;

import com.appcoiffure.coiffeuse.SubscriptionStatus;

public record LoginResponse(
        String token,
        String tokenType,
        Long coiffeuseId,
        String nom,
        String email,
        SubscriptionStatus subscriptionStatus,
        Instant abonnementActifJusquAu,
        boolean abonnementActif
) {
}
