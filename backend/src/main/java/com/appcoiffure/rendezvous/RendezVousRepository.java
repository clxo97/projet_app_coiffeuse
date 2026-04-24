package com.appcoiffure.rendezvous;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findTop100ByClientCoiffeuseIdAndDateHeureBetweenOrderByDateHeureAsc(
            Long coiffeuseId,
            LocalDateTime debut,
            LocalDateTime fin
    );

    List<RendezVous> findTop100ByClientCoiffeuseIdOrderByDateHeureAsc(Long coiffeuseId);

    List<RendezVous> findTop5ByClientCoiffeuseIdAndDateHeureGreaterThanEqualAndStatutNotOrderByDateHeureAsc(
            Long coiffeuseId,
            LocalDateTime dateHeure,
            StatutRendezVous statut
    );

    List<RendezVous> findTop5ByClientCoiffeuseIdAndDateHeureGreaterThanEqualAndStatutNotAndRappelEnvoyeFalseOrderByDateHeureAsc(
            Long coiffeuseId,
            LocalDateTime dateHeure,
            StatutRendezVous statut
    );

    List<RendezVous> findByClientCoiffeuseIdAndStatutAndDateHeureBetweenOrderByDateHeureAsc(
            Long coiffeuseId,
            StatutRendezVous statut,
            LocalDateTime debut,
            LocalDateTime fin
    );

    java.util.Optional<RendezVous> findByIdAndClientCoiffeuseId(Long id, Long coiffeuseId);

    boolean existsByIdAndClientCoiffeuseId(Long id, Long coiffeuseId);
}
