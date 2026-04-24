package com.appcoiffure.coiffeuse;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoiffeuseRepository extends JpaRepository<Coiffeuse, Long> {

    Optional<Coiffeuse> findByEmail(String email);

    boolean existsByEmail(String email);
}
