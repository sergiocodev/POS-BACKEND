package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, Long> {
    Optional<AccountReceivable> findBySaleId(Long saleId);

    List<AccountReceivable> findByCustomerId(Long customerId);

    List<AccountReceivable> findByStatus(AccountReceivable.ReceivableStatus status);
}
