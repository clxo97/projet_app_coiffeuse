package com.appcoiffure.auth;

public record LoginRequest(
        String email,
        String motDePasse
) {
}
