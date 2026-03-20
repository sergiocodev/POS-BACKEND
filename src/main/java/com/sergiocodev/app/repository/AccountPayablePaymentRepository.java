package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountPayablePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountPayablePaymentRepository extends JpaRepository<AccountPayablePayment, Long> {
    List<AccountPayablePayment> findByAccountPayableIdAndDeletedAtIsNull(Long accountPayableId);
}
