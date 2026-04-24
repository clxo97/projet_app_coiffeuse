package com.appcoiffure.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseRequest(
        LocalDate dateDepense,
        String libelle,
        String categorie,
        BigDecimal montant,
        String notes
) {
}
