package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Establishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de locales, sucursales o establecimientos.
 */
@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {
}
