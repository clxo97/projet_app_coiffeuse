package com.appcoiffure.client;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findTop50ByCoiffeuseIdAndNomContainingIgnoreCaseOrCoiffeuseIdAndTelephoneContainingIgnoreCaseOrCoiffeuseIdAndEmailContainingIgnoreCaseOrderByNomAsc(
            Long coiffeuseIdNom,
            String nom,
            Long coiffeuseIdTelephone,
            String telephone,
            Long coiffeuseIdEmail,
            String email
    );

    List<Client> findTop50ByCoiffeuseIdOrderByNomAsc(Long coiffeuseId);

    java.util.Optional<Client> findByIdAndCoiffeuseId(Long id, Long coiffeuseId);

    boolean existsByIdAndCoiffeuseId(Long id, Long coiffeuseId);

    List<Client> findByCoiffeuseIsNull();
}
