package com.appcoiffure.coiffeuse;

public record ChangePasswordRequest(
        String motDePasseActuel,
        String nouveauMotDePasse
) {
}
