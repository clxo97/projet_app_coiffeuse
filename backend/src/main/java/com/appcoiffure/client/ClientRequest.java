package com.appcoiffure.client;

public record ClientRequest(
        String nom,
        String telephone,
        String email,
        String instagram,
        String notes,
        Boolean smsActif
) {
}
