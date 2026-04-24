package com.appcoiffure.auth;

public record RegisterRequest(
        String nom,
        String nomSalon,
        String email,
        String motDePasse
) {
}
