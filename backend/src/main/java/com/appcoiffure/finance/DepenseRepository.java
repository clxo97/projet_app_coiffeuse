package com.appcoiffure.finance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    List<Depense> findByCoiffeuseIdAndDateDepenseBetweenOrderByDateDepenseAsc(Long coiffeuseId, LocalDate debut, LocalDate fin);

    java.util.Optional<Depense> findByIdAndCoiffeuseId(Long id, Long coiffeuseId);

    boolean existsByIdAndCoiffeuseId(Long id, Long coiffeuseId);

    List<Depense> findByCoiffeuseIsNull();
}
