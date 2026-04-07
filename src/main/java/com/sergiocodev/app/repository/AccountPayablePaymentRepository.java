package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountPayablePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountPayablePaymentRepository extends JpaRepository<AccountPayablePayment, Long>, JpaSpecificationExecutor<AccountPayablePayment> {
    List<AccountPayablePayment> findByAccountPayableIdAndDeletedAtIsNull(Long accountPayableId);

    List<AccountPayablePayment> findByCashSessionIdAndDeletedAtIsNull(Long cashSessionId);

    List<AccountPayablePayment> findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
            Long cashSessionId, AccountPayablePayment.PaymentMethod paymentMethod);
}
