package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.SalePayment;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SalePaymentRequest(
        @NotNull(message = "Payment method is required") SalePayment.PaymentMethod paymentMethod,

        @NotNull(message = "Amount is required") BigDecimal amount,

        String reference) {
}
