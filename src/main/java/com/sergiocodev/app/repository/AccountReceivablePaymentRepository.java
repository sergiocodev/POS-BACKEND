package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountReceivablePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountReceivablePaymentRepository extends JpaRepository<AccountReceivablePayment, Long>, JpaSpecificationExecutor<AccountReceivablePayment> {
    List<AccountReceivablePayment> findByAccountReceivableIdAndDeletedAtIsNull(Long accountReceivableId);

    List<AccountReceivablePayment> findByCashSessionIdAndDeletedAtIsNull(Long cashSessionId);

    List<AccountReceivablePayment> findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
            Long cashSessionId, AccountReceivablePayment.PaymentMethod paymentMethod);
}
