package com.appcoiffure.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DepenseResponse(
        Long id,
        LocalDate dateDepense,
        String libelle,
        String categorie,
        BigDecimal montant,
        String notes,
        Instant creeLe,
        Instant modifieLe
) {
    public static DepenseResponse from(Depense depense) {
        return new DepenseResponse(
                depense.getId(),
                depense.getDateDepense(),
                depense.getLibelle(),
                depense.getCategorie(),
                depense.getMontant(),
                depense.getNotes(),
                depense.getCreeLe(),
                depense.getModifieLe()
        );
    }
}
