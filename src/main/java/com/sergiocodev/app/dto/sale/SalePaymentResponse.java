package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.SalePayment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalePaymentResponse(
        Long id,
        SalePayment.PaymentMethod paymentMethod,
        BigDecimal amount,
        String reference,
        LocalDateTime createdAt) {
}
