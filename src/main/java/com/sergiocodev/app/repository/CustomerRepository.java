package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de clientes de la botica.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * Busca un cliente por su número de documento (DNI/RUC).
     * @param documentNumber Número de identidad.
     * @return Optional con el cliente.
     */
    Optional<Customer> findByDocumentNumber(String documentNumber);

    /**
     * Comprueba si un número de documento ya está registrado.
     */
    boolean existsByDocumentNumber(String documentNumber);

    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.establishment.id = :establishmentId AND s.customer IS NOT NULL AND s.isVoided = false")
    long countTotalCustomersByEstablishment(@Param("establishmentId") Long establishmentId);

    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.establishment.id = :establishmentId AND s.customer IS NOT NULL AND s.isVoided = false AND s.date <= :end")
    long countTotalCustomersByEstablishmentUpTo(@Param("establishmentId") Long establishmentId, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.establishment.id = :establishmentId AND s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end")
    long countActiveCustomersByEstablishment(@Param("establishmentId") Long establishmentId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.establishment.id = :establishmentId AND s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end")
    BigDecimal sumTotalSalesByEstablishment(@Param("establishmentId") Long establishmentId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.establishment.id = :establishmentId AND s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end")
    long countTotalSalesByEstablishment(@Param("establishmentId") Long establishmentId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
