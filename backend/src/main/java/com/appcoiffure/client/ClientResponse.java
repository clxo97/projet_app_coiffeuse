package com.appcoiffure.client;

import java.time.Instant;

public record ClientResponse(
        Long id,
        String nom,
        String telephone,
        String email,
        String instagram,
        String notes,
        Boolean smsActif,
        Instant creeLe,
        Instant modifieLe
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getNom(),
                client.getTelephone(),
                client.getEmail(),
                client.getInstagram(),
                client.getNotes(),
                client.getSmsActif(),
                client.getCreeLe(),
                client.getModifieLe()
        );
    }
}
