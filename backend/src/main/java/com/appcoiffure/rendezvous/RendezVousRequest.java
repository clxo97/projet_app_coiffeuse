package com.appcoiffure.rendezvous;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RendezVousRequest(
        Long clientId,
        LocalDateTime dateHeure,
        Integer dureeMinutes,
        String prestation,
        BigDecimal prix,
        StatutRendezVous statut,
        String notes
) {
}
