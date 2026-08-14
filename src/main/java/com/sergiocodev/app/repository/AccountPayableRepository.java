package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.AccountPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de cuentas por pagar (deudas a proveedores).
 */
@Repository
public interface AccountPayableRepository extends JpaRepository<AccountPayable, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<AccountPayable> {
    /**
     * Obtiene las cuentas por pagar asociadas a un proveedor específico.
     * @param supplierId ID del proveedor.
     * @return Lista de cuentas por pagar.
     */
    List<AccountPayable> findBySupplierId(Long supplierId);

    /**
     * Filtra las cuentas por pagar por su estado (Pendiente, Pagado, etc.).
     * @param status Estado de la cuenta.
     * @return Lista de cuentas con dicho estado.
     */
    List<AccountPayable> findByStatus(AccountPayable.PayableStatus status);

    /**
     * Busca la cuenta por pagar generada a partir de una compra específica.
     * @param purchaseId ID de la compra.
     * @return La cuenta por pagar envuelta en Optional.
     */
    Optional<AccountPayable> findByPurchaseId(Long purchaseId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AccountPayable a WHERE a.status IN :statuses AND a.purchase.establishment.id = :establishmentId ORDER BY a.dueDate ASC")
    org.springframework.data.domain.Page<AccountPayable> findUpcomingPayables(
            @org.springframework.data.repository.query.Param("statuses") List<AccountPayable.PayableStatus> statuses,
            @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountPayable a WHERE a.status NOT IN :excludedStatuses AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getTotalPendingBalance(@org.springframework.data.repository.query.Param("excludedStatuses") List<AccountPayable.PayableStatus> excludedStatuses, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountPayable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate < CURRENT_DATE AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getOverdueBalance(@org.springframework.data.repository.query.Param("excludedStatuses") List<AccountPayable.PayableStatus> excludedStatuses, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountPayable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate >= CURRENT_DATE AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getAmountUpcomingDue(@org.springframework.data.repository.query.Param("excludedStatuses") List<AccountPayable.PayableStatus> excludedStatuses, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AccountPayable a WHERE a.status != :canceledStatus AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getTotalExpectedAmount(@org.springframework.data.repository.query.Param("canceledStatus") AccountPayable.PayableStatus canceledStatus, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.amountPaid), 0) FROM AccountPayable a WHERE a.status != :canceledStatus AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getTotalCollectedAmount(@org.springframework.data.repository.query.Param("canceledStatus") AccountPayable.PayableStatus canceledStatus, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountPayable a WHERE a.status NOT IN :excludedStatuses AND a.createdAt >= :start AND a.createdAt <= :end AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getPendingBalanceCreatedBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("excludedStatuses") List<AccountPayable.PayableStatus> excludedStatuses, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountPayable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate >= :start AND a.dueDate <= :end AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getOverdueBalanceDueBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDate start, @org.springframework.data.repository.query.Param("end") java.time.LocalDate end, @org.springframework.data.repository.query.Param("excludedStatuses") List<AccountPayable.PayableStatus> excludedStatuses, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.pendingBalance), 0) FROM AccountPayable a WHERE a.status NOT IN :excludedStatuses AND a.dueDate >= :start AND a.dueDate <= :end AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getAmountDueBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDate start, @org.springframework.data.repository.query.Param("end") java.time.LocalDate end, @org.springframework.data.repository.query.Param("excludedStatuses") List<AccountPayable.PayableStatus> excludedStatuses, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AccountPayable a WHERE a.status != :canceledStatus AND a.createdAt >= :start AND a.createdAt <= :end AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getExpectedAmountCreatedBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("canceledStatus") AccountPayable.PayableStatus canceledStatus, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.amountPaid), 0) FROM AccountPayable a WHERE a.status != :canceledStatus AND a.createdAt >= :start AND a.createdAt <= :end AND a.purchase.establishment.id = :establishmentId")
    java.math.BigDecimal getCollectedAmountCreatedBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("canceledStatus") AccountPayable.PayableStatus canceledStatus, @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId);
}
