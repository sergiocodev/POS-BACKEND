package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountReceivablePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para el manejo de cobros o pagos de cuentas por cobrar.
 */
@Repository
public interface AccountReceivablePaymentRepository extends JpaRepository<AccountReceivablePayment, Long>, JpaSpecificationExecutor<AccountReceivablePayment> {
    /**
     * Busca los cobros realizados sobre una cuenta por cobrar específica.
     * @param accountReceivableId ID de la cuenta por cobrar.
     * @return Lista de cobros activos.
     */
    List<AccountReceivablePayment> findByAccountReceivableIdAndDeletedAtIsNull(Long accountReceivableId);

    /**
     * Recupera los ingresos por cobranza vinculados a una sesión de caja.
     * @param cashSessionId ID de la sesión de caja.
     * @return Lista de pagos recibidos.
     */
    List<AccountReceivablePayment> findByCashSessionIdAndDeletedAtIsNull(Long cashSessionId);

    /**
     * Filtra los cobros de una sesión de caja por su método de pago.
     * @param cashSessionId ID de la sesión de caja.
     * @param paymentMethod Método de pago.
     * @return Lista de cobros filtrados.
     */
    List<AccountReceivablePayment> findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
            Long cashSessionId, AccountReceivablePayment.PaymentMethod paymentMethod);
}
