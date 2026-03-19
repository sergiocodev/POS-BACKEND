package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountPayableRepository extends JpaRepository<AccountPayable, Long> {
    List<AccountPayable> findBySupplierId(Long supplierId);

    List<AccountPayable> findByStatus(AccountPayable.PayableStatus status);

    Optional<AccountPayable> findByPurchaseId(Long purchaseId);
}
