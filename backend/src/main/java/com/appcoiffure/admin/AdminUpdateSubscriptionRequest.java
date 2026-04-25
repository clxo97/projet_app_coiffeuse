package com.appcoiffure.admin;

import java.time.Instant;

import com.appcoiffure.coiffeuse.SubscriptionStatus;

public record AdminUpdateSubscriptionRequest(
        SubscriptionStatus subscriptionStatus,
        Instant abonnementActifJusquAu
) {
}
