package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de cuentas por cobrar (créditos a clientes).
 */
@Repository
public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, Long>, JpaSpecificationExecutor<AccountReceivable> {
    /**
     * Localiza la cuenta por cobrar originada de una venta específica.
     * @param saleId ID de la venta.
     * @return Optional con la cuenta por cobrar.
     */
    Optional<AccountReceivable> findBySaleId(Long saleId);

    /**
     * Obtiene todas las deudas activas de un cliente.
     * @param customerId ID del cliente.
     * @return Lista de cuentas por cobrar.
     */
    List<AccountReceivable> findByCustomerId(Long customerId);

    /**
     * Filtra los créditos por su estado actual.
     * @param status Estado (Pendiente, Parcial, Cancelado).
     * @return Lista de cuentas filtradas.
     */
    List<AccountReceivable> findByStatus(AccountReceivable.ReceivableStatus status);

    @Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountReceivable a WHERE a.status NOT IN :excludedStatuses AND a.sale.establishment.id = :establishmentId")
    BigDecimal getTotalPendingBalance(@Param("excludedStatuses") List<AccountReceivable.ReceivableStatus> excludedStatuses, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountReceivable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate < CURRENT_DATE AND a.sale.establishment.id = :establishmentId")
    BigDecimal getOverdueBalance(@Param("excludedStatuses") List<AccountReceivable.ReceivableStatus> excludedStatuses, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountReceivable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate >= CURRENT_DATE AND a.sale.establishment.id = :establishmentId")
    BigDecimal getAmountUpcomingDue(@Param("excludedStatuses") List<AccountReceivable.ReceivableStatus> excludedStatuses, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AccountReceivable a WHERE a.status != :canceledStatus AND a.sale.establishment.id = :establishmentId")
    BigDecimal getTotalExpectedAmount(@Param("canceledStatus") AccountReceivable.ReceivableStatus canceledStatus, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.amountPaid), 0) FROM AccountReceivable a WHERE a.status != :canceledStatus AND a.sale.establishment.id = :establishmentId")
    BigDecimal getTotalCollectedAmount(@Param("canceledStatus") AccountReceivable.ReceivableStatus canceledStatus, @Param("establishmentId") Long establishmentId);

    // Queries for dynamic trends (Comparing periods)
    @Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountReceivable a WHERE a.status NOT IN :excludedStatuses AND a.createdAt >= :start AND a.createdAt <= :end AND a.sale.establishment.id = :establishmentId")
    BigDecimal getPendingBalanceCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludedStatuses") List<AccountReceivable.ReceivableStatus> excludedStatuses, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountReceivable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate >= :start AND a.dueDate <= :end AND a.sale.establishment.id = :establishmentId")
    BigDecimal getOverdueBalanceDueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("excludedStatuses") List<AccountReceivable.ReceivableStatus> excludedStatuses, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountReceivable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate >= :start AND a.dueDate <= :end AND a.sale.establishment.id = :establishmentId")
    BigDecimal getAmountDueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("excludedStatuses") List<AccountReceivable.ReceivableStatus> excludedStatuses, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AccountReceivable a WHERE a.status != :canceledStatus AND a.createdAt >= :start AND a.createdAt <= :end AND a.sale.establishment.id = :establishmentId")
    BigDecimal getExpectedAmountCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("canceledStatus") AccountReceivable.ReceivableStatus canceledStatus, @Param("establishmentId") Long establishmentId);

    @Query("SELECT COALESCE(SUM(a.amountPaid), 0) FROM AccountReceivable a WHERE a.status != :canceledStatus AND a.createdAt >= :start AND a.createdAt <= :end AND a.sale.establishment.id = :establishmentId")
    BigDecimal getCollectedAmountCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("canceledStatus") AccountReceivable.ReceivableStatus canceledStatus, @Param("establishmentId") Long establishmentId);
}
