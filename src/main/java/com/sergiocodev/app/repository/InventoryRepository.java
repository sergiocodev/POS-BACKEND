package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByEstablishmentIdAndLotId(Long establishmentId, Long lotId);

    // Low stock: quantity <= 10 (Assuming you might want to add minStock to Product
    // or Inventory)
    // For now showing items with quantity < 10
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inventory i WHERE i.quantity <= 10")
    java.util.List<Inventory> findLowStock();

    // Expirations: expiryDate between now and date
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inventory i JOIN i.lot l WHERE l.expiryDate <= :date")
    java.util.List<Inventory> findExpiringSoon(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);
}
