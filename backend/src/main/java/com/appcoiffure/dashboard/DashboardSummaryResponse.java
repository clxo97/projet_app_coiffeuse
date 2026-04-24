package com.appcoiffure.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.appcoiffure.rendezvous.RendezVousResponse;

public record DashboardSummaryResponse(
        long rendezVousDuJour,
        long rendezVousTerminesDuJour,
        BigDecimal chiffreAffairesDuJour,
        List<RendezVousResponse> prochainsRendezVous,
        List<RendezVousResponse> rappelsAEnvoyer
) {
}
