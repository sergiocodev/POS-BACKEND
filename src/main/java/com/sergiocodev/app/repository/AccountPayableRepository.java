package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de cuentas por pagar (deudas a proveedores).
 */
@Repository
public interface AccountPayableRepository extends JpaRepository<AccountPayable, Long> {
    /**
     * Obtiene las cuentas por pagar asociadas a un proveedor específico.
     * @param supplierId ID del proveedor.
     * @return Lista de cuentas por pagar.
     */
    List<AccountPayable> findBySupplierId(Long supplierId);

    /**
     * Filtra las cuentas por pagar por su estado (Pendiente, Pagado, etc.).
     * @param status Estado de la cuenta.
     * @return Lista de cuentas con dicho estado.
     */
    List<AccountPayable> findByStatus(AccountPayable.PayableStatus status);

    /**
     * Busca la cuenta por pagar generada a partir de una compra específica.
     * @param purchaseId ID de la compra.
     * @return La cuenta por pagar envuelta en Optional.
     */
    Optional<AccountPayable> findByPurchaseId(Long purchaseId);
}
