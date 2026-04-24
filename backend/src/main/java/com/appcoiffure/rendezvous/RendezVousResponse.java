package com.appcoiffure.rendezvous;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record RendezVousResponse(
        Long id,
        Long clientId,
        String clientNom,
        String clientTelephone,
        String clientInstagram,
        LocalDateTime dateHeure,
        Integer dureeMinutes,
        String prestation,
        BigDecimal prix,
        StatutRendezVous statut,
        String notes,
        String messageRappel,
        Boolean rappelEnvoye,
        Instant creeLe,
        Instant modifieLe
) {
    public static RendezVousResponse from(RendezVous rendezVous) {
        return from(rendezVous, null);
    }

    public static RendezVousResponse from(RendezVous rendezVous, String messageRappel) {
        return new RendezVousResponse(
                rendezVous.getId(),
                rendezVous.getClient().getId(),
                rendezVous.getClient().getNom(),
                rendezVous.getClient().getTelephone(),
                rendezVous.getClient().getInstagram(),
                rendezVous.getDateHeure(),
                rendezVous.getDureeMinutes(),
                rendezVous.getPrestation(),
                rendezVous.getPrix(),
                rendezVous.getStatut(),
                rendezVous.getNotes(),
                messageRappel,
                rendezVous.getRappelEnvoye(),
                rendezVous.getCreeLe(),
                rendezVous.getModifieLe()
        );
    }
}
