package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de cuentas por cobrar (créditos a clientes).
 */
@Repository
public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, Long> {
    /**
     * Localiza la cuenta por cobrar originada de una venta específica.
     * @param saleId ID de la venta.
     * @return Optional con la cuenta por cobrar.
     */
    Optional<AccountReceivable> findBySaleId(Long saleId);

    /**
     * Obtiene todas las deudas activas de un cliente.
     * @param customerId ID del cliente.
     * @return Lista de cuentas por cobrar.
     */
    List<AccountReceivable> findByCustomerId(Long customerId);

    /**
     * Filtra los créditos por su estado actual.
     * @param status Estado (Pendiente, Parcial, Cancelado).
     * @return Lista de cuentas filtradas.
     */
    List<AccountReceivable> findByStatus(AccountReceivable.ReceivableStatus status);
}
