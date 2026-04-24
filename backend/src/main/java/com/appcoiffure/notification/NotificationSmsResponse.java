package com.appcoiffure.notification;

import java.time.Instant;
import java.time.LocalDateTime;

public record NotificationSmsResponse(
        Long id,
        Long rendezVousId,
        String clientNom,
        String prestation,
        LocalDateTime dateHeure,
        String clientInstagram,
        NotificationType type,
        NotificationStatus statut,
        Instant dateEnvoiPrevue,
        Instant dateEnvoi,
        String telephone,
        String message,
        String erreur,
        Instant creeLe
) {
    public static NotificationSmsResponse from(NotificationSms notification) {
        return new NotificationSmsResponse(
                notification.getId(),
                notification.getRendezVous().getId(),
                notification.getRendezVous().getClient().getNom(),
                notification.getRendezVous().getPrestation(),
                notification.getRendezVous().getDateHeure(),
                notification.getRendezVous().getClient().getInstagram(),
                notification.getType(),
                notification.getStatut(),
                notification.getDateEnvoiPrevue(),
                notification.getDateEnvoi(),
                notification.getTelephone(),
                notification.getMessage(),
                notification.getErreur(),
                notification.getCreeLe()
        );
    }
}
