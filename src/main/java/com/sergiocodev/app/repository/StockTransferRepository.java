package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    List<StockTransfer> findBySourceEstablishmentId(Long sourceEstablishmentId);

    List<StockTransfer> findByTargetEstablishmentId(Long targetEstablishmentId);

    List<StockTransfer> findByStatus(StockTransfer.TransferStatus status);
}
