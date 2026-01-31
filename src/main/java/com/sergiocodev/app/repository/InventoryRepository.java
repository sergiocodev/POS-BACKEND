package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByEstablishmentIdAndLotId(Long establishmentId, Long lotId);
}
