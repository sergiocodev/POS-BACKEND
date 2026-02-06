package com.sergiocodev.app.dto.dashboard;

import com.sergiocodev.app.model.SalePayment;
import java.math.BigDecimal;

public record PaymentMethodDistribution(
        SalePayment.PaymentMethod method,
        BigDecimal amount) {
}
