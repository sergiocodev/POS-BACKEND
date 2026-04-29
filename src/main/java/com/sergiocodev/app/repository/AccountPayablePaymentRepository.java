package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountPayablePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de pagos de cuentas por pagar.
 * Proporciona métodos para buscar pagos realizados sobre deudas a proveedores.
 */
@Repository
public interface AccountPayablePaymentRepository extends JpaRepository<AccountPayablePayment, Long>, JpaSpecificationExecutor<AccountPayablePayment> {
    /**
     * Busca todos los pagos asociados a una cuenta por pagar específica que no hayan sido eliminados.
     * @param accountPayableId ID de la cuenta por pagar.
     * @return Lista de pagos activos.
     */
    List<AccountPayablePayment> findByAccountPayableIdAndDeletedAtIsNull(Long accountPayableId);

    /**
     * Recupera los pagos efectuados durante una sesión de caja específica.
     * @param cashSessionId ID de la sesión de caja.
     * @return Lista de pagos vinculados a la sesión.
     */
    List<AccountPayablePayment> findByCashSessionIdAndDeletedAtIsNull(Long cashSessionId);

    /**
     * Filtra los pagos de una sesión de caja por el método de pago utilizado.
     * @param cashSessionId ID de la sesión de caja.
     * @param paymentMethod Método de pago (Efectivo, Tarjeta, etc.).
     * @return Lista de pagos filtrados.
     */
    List<AccountPayablePayment> findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
            Long cashSessionId, AccountPayablePayment.PaymentMethod paymentMethod);
}
