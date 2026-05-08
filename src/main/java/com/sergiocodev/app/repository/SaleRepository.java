package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

        /**
         * Cuenta rápidamente de forma algorítmica las ventas exitosamente completadas
         * (no anuladas) y procesadas.
         * Si provee un establishmentId solo cuenta de ese establecimiento,
         * si el pase es nulo se contará transversalmente en toda la cadena globalmente
         * operativa (Admin scope).
         * 
         * @param establishmentId Origen o sucursal.
         * @param start           El comienzo de la evaluación temporal.
         * @param end             El tope o límite comparativo a la fecha actual del
         *                        rango.
         * @return Total de ventas sin cancelar producidas en la organización.
         */
        @Query("SELECT COUNT(s) FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) AND s.isVoided = false AND s.date >= :start AND s.date <= :end")
        long countByEstablishmentAndDateBetween(
                        @Param("establishmentId") Long establishmentId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Suma el valor adquisitivo subtotal neto e ingresos generados por operaciones
         * de las cajas.
         * Provee una radiografía económica fundamental para elaborar arqueos,
         * presupuestos directos o cierres.
         * 
         * @param establishmentId La sucursal o sede comercial recaudadora.
         * @param start           El timestamp de comienzo.
         * @param end             La liquidación a consultar final.
         * @return Dinero nominal devengado transaccionalmente o un campo nulo/0
         *         internamente al omitir anulaciones.
         */
        @Query("SELECT SUM(s.total) FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) AND s.isVoided = false AND s.date >= :start AND s.date <= :end")
        BigDecimal sumTotalByEstablishmentAndDateBetween(
                        @Param("establishmentId") Long establishmentId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @EntityGraph(attributePaths = { "customer", "establishment", "user" })
        List<Sale> findByCustomerId(Long customerId);

        @EntityGraph(attributePaths = { "customer", "establishment", "user" })
        List<Sale> findByCashSessionId(Long cashSessionId);

        @EntityGraph(attributePaths = { "customer", "establishment", "user" })
        List<Sale> findAllByOrderByDateDesc();

        @EntityGraph(attributePaths = { "customer", "establishment", "user" })
        List<Sale> findByDateBetweenOrderByDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);

        @EntityGraph(attributePaths = { "items", "items.productLot",
                        "items.productLot.product", "establishment", "customer" })
        java.util.Optional<Sale> findWithItemsById(Long id);

        // ──────────────────────────────────────────────────────────────
        // Dashboard & Report queries: replace findAll().stream().filter()
        // ──────────────────────────────────────────────────────────────

        /** Count sales pending SUNAT submission (PENDING or REJECTED) before a date */
        @Query("SELECT COUNT(s) FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) "
                        +
                        "AND s.isVoided = false AND s.sunatStatus IN (:statuses) AND s.date < :beforeDate")
        long countPendingSunat(@Param("establishmentId") Long establishmentId,
                        @Param("statuses") List<Sale.SunatStatus> statuses,
                        @Param("beforeDate") LocalDateTime beforeDate);

        /** Sales for dashboard/chart: paginated with eager loading */
        @EntityGraph(attributePaths = { "customer", "establishment", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate " +
                        "ORDER BY s.date DESC")
        Page<Sale> findByEstablishmentAndDateRangeOrderByDateDesc(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /** Sales for reports: paginated (no EntityGraph for aggregate queries) */
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate " +
                        "ORDER BY s.date DESC")
        Page<Sale> findForReports(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /** SUNAT alerts: pending or rejected sales before a date */
        @EntityGraph(attributePaths = { "establishment", "customer", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.sunatStatus IN (:statuses) AND s.date < :beforeDate " +
                        "ORDER BY s.date DESC")
        List<Sale> findSunatAlerts(@Param("establishmentId") Long establishmentId,
                        @Param("statuses") List<Sale.SunatStatus> statuses,
                        @Param("beforeDate") LocalDateTime beforeDate);

        /** Sales grouped by employee for a given date */
        @EntityGraph(attributePaths = { "items", "establishment", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate " +
                        "ORDER BY s.user.id, s.date DESC")
        List<Sale> findByEstablishmentAndDateRangeForEmployee(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** Recent sales for today, limited via Pageable */
        @EntityGraph(attributePaths = { "items", "customer", "establishment", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate " +
                        "ORDER BY s.date DESC")
        Page<Sale> findRecentSales(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /** Sales for payment method analysis */
        @EntityGraph(attributePaths = { "payments", "establishment" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate")
        List<Sale> findForPaymentAnalysis(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** Sales for category analysis */
        @EntityGraph(attributePaths = { "items", "items.product", "establishment" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate")
        List<Sale> findForCategoryAnalysis(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** All sales for report export (paginated, with date filtering) */
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.date >= :startDate AND s.date <= :endDate " +
                        "ORDER BY s.date DESC")
        Page<Sale> findAllForReport(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /** Top products: aggregate query via GROUP BY (most efficient) */
        @Query("SELECT si.product.id, SUM(si.quantity), SUM(si.amount) " +
                        "FROM SaleItem si JOIN si.sale s " +
                        "WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) AND s.isVoided = false "
                        +
                        "AND s.date >= :startDate AND s.date <= :endDate " +
                        "GROUP BY si.product.id ORDER BY SUM(si.quantity) DESC")
        List<Object[]> findTopProductsByQuantity(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /** Sales with payments for payment method reports */
        @EntityGraph(attributePaths = { "payments", "establishment", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.date >= :startDate AND s.date <= :endDate " +
                        "ORDER BY s.date DESC")
        List<Sale> findWithPaymentsForReport(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** Sales with items, product and category for category detail reports */
        @EntityGraph(attributePaths = { "items", "items.product", "items.product.category",
                        "items.product.laboratory", "establishment", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.isVoided = false AND s.date >= :startDate AND s.date <= :endDate")
        List<Sale> findForCategoryDetailAnalysis(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** Sales filtered by documentType and optionally series */
        @EntityGraph(attributePaths = { "customer", "establishment", "user" })
        @Query("SELECT s FROM Sale s WHERE (:establishmentId IS NULL OR s.establishment.id = :establishmentId) " +
                        "AND s.date >= :startDate AND s.date <= :endDate " +
                        "AND (:documentType IS NULL OR s.documentType = :documentType) " +
                        "AND (:series IS NULL OR s.series = :series) " +
                        "ORDER BY s.date DESC")
        List<Sale> findByFilters(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("documentType") Sale.SaleDocumentType documentType,
                        @Param("series") String series);
}
