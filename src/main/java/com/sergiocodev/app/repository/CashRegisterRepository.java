package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de puntos físicos de venta o cajas registradoras.
 */
@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    /**
     * Lista las cajas registradoras de un establecimiento específico.
     * @param establishmentId ID del establecimiento.
     * @return Lista de cajas.
     */
    List<CashRegister> findByEstablishmentId(Long establishmentId);
}
