package com.appcoiffure.notification;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSmsRepository extends JpaRepository<NotificationSms, Long> {

    List<NotificationSms> findTop100ByRendezVousClientCoiffeuseIdOrderByCreeLeDesc(Long coiffeuseId);

    List<NotificationSms> findTop20ByStatutAndDateEnvoiPrevueLessThanEqualOrderByDateEnvoiPrevueAsc(
            NotificationStatus statut,
            Instant dateEnvoiPrevue
    );

    void deleteByRendezVousIdAndStatut(Long rendezVousId, NotificationStatus statut);

    void deleteByRendezVousId(Long rendezVousId);
}
