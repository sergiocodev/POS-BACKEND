package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.SalePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SalePaymentRepository extends JpaRepository<SalePayment, Long> {
    List<SalePayment> findBySaleId(Long saleId);

    List<SalePayment> findBySaleCashSessionIdAndPaymentMethod(Long cashSessionId,
            SalePayment.PaymentMethod paymentMethod);

    List<SalePayment> findBySaleCashSessionIdAndDeletedAtIsNull(Long cashSessionId);

    @Query("SELECT COALESCE(SUM(sp.amount), 0) FROM SalePayment sp " +
           "WHERE sp.sale.cashSession.id = :sessionId " +
           "AND sp.paymentMethod = :method " +
           "AND sp.deletedAt IS NULL")
    BigDecimal sumByCashSessionIdAndPaymentMethod(
            @Param("sessionId") Long sessionId,
            @Param("method") SalePayment.PaymentMethod method);
}
