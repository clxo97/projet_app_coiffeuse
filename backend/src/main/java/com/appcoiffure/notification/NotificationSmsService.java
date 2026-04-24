package com.appcoiffure.notification;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.rendezvous.RendezVous;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationSmsService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DEFAULT_CONFIRMATION_TEMPLATE = "Votre rendez-vous coiffure est confirme le {date}. Prestation : {prestation}. Duree : {duree} min. A bientot.";
    private static final String DEFAULT_MODIFICATION_TEMPLATE = "Votre rendez-vous coiffure a ete modifie le {date}. Prestation : {prestation}. Duree : {duree} min. A bientot.";
    private static final String DEFAULT_RAPPEL_TEMPLATE = "Rappel : votre rendez-vous coiffure est prevu le {date}. Prestation : {prestation}. Duree : {duree} min. A bientot.";

    private final NotificationSmsRepository notificationSmsRepository;
    private final SmsSender smsSender;

    public NotificationSmsService(
            NotificationSmsRepository notificationSmsRepository,
            SmsSender smsSender
    ) {
        this.notificationSmsRepository = notificationSmsRepository;
        this.smsSender = smsSender;
    }

    @Transactional
    public void programmerNotificationsCreation(RendezVous rendezVous) {
        programmerNotification(rendezVous, NotificationType.CONFIRMATION_RDV, Instant.now(), confirmationTemplate(rendezVous));
        programmerRappel24h(rendezVous);
    }

    @Transactional
    public void programmerNotificationsModification(RendezVous rendezVous) {
        notificationSmsRepository.deleteByRendezVousIdAndStatut(rendezVous.getId(), NotificationStatus.A_ENVOYER);
        programmerNotification(rendezVous, NotificationType.CONFIRMATION_RDV, Instant.now(), modificationTemplate(rendezVous));
        programmerRappel24h(rendezVous);
    }

    @Transactional
    public void supprimerNotifications(Long rendezVousId) {
        notificationSmsRepository.deleteByRendezVousId(rendezVousId);
    }

    public String construireMessageRappel(RendezVous rendezVous) {
        return buildMessage(rendezVous, rappelTemplate(rendezVous));
    }

    @Scheduled(fixedDelayString = "${app.sms.scheduler-delay-ms}")
    @Transactional
    public void envoyerNotificationsEnAttente() {
        List<NotificationSms> notifications = notificationSmsRepository
                .findTop20ByStatutAndDateEnvoiPrevueLessThanEqualOrderByDateEnvoiPrevueAsc(
                        NotificationStatus.A_ENVOYER,
                        Instant.now()
        );

        for (NotificationSms notification : notifications) {
            if (!notification.getRendezVous().getClient().getCoiffeuse().hasActiveSubscription()) {
                continue;
            }

            try {
                smsSender.send(notification.getTelephone(), notification.getMessage());
                notification.marquerEnvoyee();

                if (notification.getType() == NotificationType.RAPPEL_24H) {
                    notification.getRendezVous().marquerRappelEnvoye();
                }
            } catch (Exception exception) {
                notification.marquerEchec(exception.getMessage());
            }
        }
    }

    private void programmerNotification(
            RendezVous rendezVous,
            NotificationType type,
            Instant scheduledAt,
            String template
    ) {
        if (!rendezVous.getClient().getSmsActif()) {
            return;
        }

        String phone = cleanPhone(rendezVous.getClient().getTelephone());

        if (phone == null) {
            return;
        }

        notificationSmsRepository.save(new NotificationSms(
                rendezVous,
                type,
                scheduledAt,
                phone,
                buildMessage(rendezVous, template)
        ));
    }

    private void programmerRappel24h(RendezVous rendezVous) {
        Instant scheduledAt = rendezVous.getDateHeure()
                .minusHours(24)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        if (scheduledAt.isBefore(Instant.now())) {
            scheduledAt = Instant.now();
        }

        programmerNotification(rendezVous, NotificationType.RAPPEL_24H, scheduledAt, rappelTemplate(rendezVous));
    }

    private String buildMessage(RendezVous rendezVous, String template) {
        String date = rendezVous.getDateHeure().format(DATE_FORMATTER);
        return template
                .replace("{client}", rendezVous.getClient().getNom())
                .replace("{date}", date)
                .replace("{prestation}", rendezVous.getPrestation())
                .replace("{duree}", rendezVous.getDureeMinutes().toString());
    }

    private String confirmationTemplate(RendezVous rendezVous) {
        return configuredTemplate(rendezVous, Coiffeuse::getModeleSmsConfirmation, DEFAULT_CONFIRMATION_TEMPLATE);
    }

    private String modificationTemplate(RendezVous rendezVous) {
        return configuredTemplate(rendezVous, Coiffeuse::getModeleSmsModification, DEFAULT_MODIFICATION_TEMPLATE);
    }

    private String rappelTemplate(RendezVous rendezVous) {
        return configuredTemplate(rendezVous, Coiffeuse::getModeleSmsRappel, DEFAULT_RAPPEL_TEMPLATE);
    }

    private String configuredTemplate(
            RendezVous rendezVous,
            java.util.function.Function<Coiffeuse, String> getter,
            String fallback
    ) {
        return Optional.ofNullable(rendezVous.getClient().getCoiffeuse())
                .map(getter)
                .filter(value -> value != null && !value.isBlank())
                .orElse(fallback);
    }

    private String cleanPhone(String phone) {
        if (phone == null || phone.trim().isBlank()) {
            return null;
        }

        return phone.replaceAll("[^0-9+]", "");
    }
}
