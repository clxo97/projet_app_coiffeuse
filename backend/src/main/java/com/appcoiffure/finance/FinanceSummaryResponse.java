package com.appcoiffure.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FinanceSummaryResponse(
        LocalDate debut,
        LocalDate fin,
        BigDecimal chiffreAffaires,
        BigDecimal totalDepenses,
        BigDecimal resultatNet,
        long prestationsTerminees,
        BigDecimal ticketMoyen,
        List<FinanceLineResponse> lignes,
        List<DepenseResponse> depenses
) {
}
