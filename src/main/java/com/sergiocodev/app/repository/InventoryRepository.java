package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
        Optional<Inventory> findByEstablishmentIdAndLotId(Long establishmentId, Long lotId);

        /**
         * Recupera el material en plaza cuyo stock remanente se haya tornado crítico o residualmente 
         * menor o equivalente a tan solo 10 elementos totales (umbral por defecto). 
         * Actúa de gatillo para reabastecimientos reactivos globalizados.
         * 
         * @return Colecciones de activos extinguiéndose en inventarios de todas las sedes.
         */
        @Query("SELECT i FROM Inventory i WHERE i.quantity <= 10")
        java.util.List<Inventory> findLowStock();

        /**
         * Encuentra remanentes biológicos y de mercancía vinculados a la dependencia de lotes
         * cuya vigencia expire anterior o estrictamente igual al parámetro temporal suministrado.
         * 
         * @param date Timestamp acotando la pesquisa de lotes o empaquetados perecibles.
         * @return Desglose de mermas, productos listos a rematar o caducados si son desfasajes.
         */
        @Query("SELECT i FROM Inventory i JOIN i.lot l WHERE l.expiryDate <= :date")
        java.util.List<Inventory> findExpiringSoon(
                        @Param("date") java.time.LocalDate date);

        /**
         * Analiza individualmente los límites predefinidos personalizables definidos según el control operativo 
         * devolviéndonos artículos que vulneraron el límite o "Stock Mínimo" estipulado expresamente por el regente.
         * 
         * @return Bienes y medicinas por debajo de su umbral de tolerancia preventivo en bodega.
         */
        @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStock")
        java.util.List<Inventory> findLowStockAlerts();

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i WHERE i.establishment.id = :establishmentId")
        List<Inventory> findAllByEstablishmentId(@Param("establishmentId") Long establishmentId);

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i WHERE i.establishment.id = :establishmentId " +
                        "AND i.quantity > 0 " +
                        "AND (LOWER(i.lot.product.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(i.lot.product.tradeName) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(i.lot.product.genericName) LIKE LOWER(CONCAT('%', :query, '%')))")
        java.util.List<Inventory> searchProductsForPOS(
                        @Param("query") String query,
                        @Param("establishmentId") Long establishmentId);

        java.util.Optional<Inventory> findFirstByEstablishmentIdAndLotProductIdAndQuantityGreaterThanOrderByLotExpiryDateAsc(
                        Long establishmentId, Long productId, java.math.BigDecimal quantity);

        @Query("SELECT i FROM Inventory i JOIN i.lot l WHERE l.expiryDate BETWEEN :startDate AND :endDate")
        List<Inventory> findExpiringLotsBetween(
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);

        // ──────────────────────────────────────────────────────────────
        // Dashboard & Report queries: replace findAll().stream().filter()
        // ──────────────────────────────────────────────────────────────

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i WHERE i.establishment.id = :establishmentId")
        List<Inventory> findSummaryByEstablishment(@Param("establishmentId") Long establishmentId);

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i JOIN i.lot l WHERE i.establishment.id = :establishmentId " +
                        "AND i.quantity > 0 AND l.expiryDate < :today")
        List<Inventory> findExpiredLots(@Param("establishmentId") Long establishmentId,
                        @Param("today") LocalDate today);

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i JOIN i.lot l WHERE i.establishment.id = :establishmentId " +
                        "AND i.quantity > 0 AND l.expiryDate >= :today AND l.expiryDate < :limitDate")
        List<Inventory> findExpiringLots(@Param("establishmentId") Long establishmentId,
                        @Param("today") LocalDate today,
                        @Param("limitDate") LocalDate limitDate);

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i WHERE i.establishment.id = :establishmentId " +
                        "AND i.quantity <= 0")
        List<Inventory> findOutOfStock(@Param("establishmentId") Long establishmentId);

        @EntityGraph(attributePaths = { "lot", "lot.product", "lot.product.category" })
        @Query("SELECT i FROM Inventory i JOIN i.lot l JOIN l.product p " +
                        "WHERE i.establishment.id = :establishmentId " +
                        "AND i.minStock > 0 AND i.quantity <= i.minStock " +
                        "ORDER BY (i.quantity / i.minStock) ASC")
        Page<Inventory> findLowStockItems(@Param("establishmentId") Long establishmentId,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "lot", "lot.product" })
        @Query("SELECT i FROM Inventory i JOIN i.lot l WHERE i.establishment.id = :establishmentId " +
                        "AND i.quantity > 0 AND l.expiryDate >= :today AND l.expiryDate < :limitDate " +
                        "ORDER BY l.expiryDate ASC")
        Page<Inventory> findExpiringLotsPaged(@Param("establishmentId") Long establishmentId,
                        @Param("today") LocalDate today,
                        @Param("limitDate") LocalDate limitDate,
                        Pageable pageable);

        @Query("SELECT COUNT(DISTINCT i.lot.product.id) FROM Inventory i " +
                        "WHERE i.establishment.id = :establishmentId AND i.quantity > 0")
        long countDistinctProductsInStock(@Param("establishmentId") Long establishmentId);

        @Query("SELECT COUNT(i) FROM Inventory i JOIN i.lot l " +
                        "WHERE i.establishment.id = :establishmentId AND i.quantity > 0 AND l.expiryDate < :today")
        long countExpiredLots(@Param("establishmentId") Long establishmentId,
                        @Param("today") LocalDate today);

        @Query("SELECT COUNT(i) FROM Inventory i JOIN i.lot l " +
                        "WHERE i.establishment.id = :establishmentId AND i.quantity > 0 " +
                        "AND l.expiryDate >= :today AND l.expiryDate < :limitDate")
        long countExpiringLots(@Param("establishmentId") Long establishmentId,
                        @Param("today") LocalDate today,
                        @Param("limitDate") LocalDate limitDate);

        @Query("SELECT COUNT(i) FROM Inventory i WHERE i.establishment.id = :establishmentId AND i.quantity <= 0")
        long countOutOfStock(@Param("establishmentId") Long establishmentId);

        // ──────────────────────────────────────────────────────────────
        // Locking for concurrent stock operations
        // ──────────────────────────────────────────────────────────────

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT i FROM Inventory i WHERE i.establishment.id = :establishmentId AND i.lot.id = :lotId")
        Optional<Inventory> findByEstablishmentIdAndLotIdForUpdate(
                        @Param("establishmentId") Long establishmentId,
                        @Param("lotId") Long lotId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT i FROM Inventory i JOIN i.lot l " +
                        "WHERE i.establishment.id = :establishmentId AND l.product.id = :productId " +
                        "AND i.quantity > :minQuantity AND l.expiryDate >= :today " +
                        "ORDER BY l.expiryDate ASC")
        Optional<Inventory> findAvailableLotForUpdate(
                        @Param("establishmentId") Long establishmentId,
                        @Param("productId") Long productId,
                        @Param("minQuantity") BigDecimal minQuantity,
                        @Param("today") LocalDate today);
}
