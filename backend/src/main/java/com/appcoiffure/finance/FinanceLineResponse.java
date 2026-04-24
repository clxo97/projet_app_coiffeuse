package com.appcoiffure.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinanceLineResponse(
        Long rendezVousId,
        Long clientId,
        String clientNom,
        LocalDateTime dateHeure,
        String prestation,
        BigDecimal prix
) {
}
