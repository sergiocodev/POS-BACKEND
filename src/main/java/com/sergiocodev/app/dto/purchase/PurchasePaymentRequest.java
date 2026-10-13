package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.AccountPayablePayment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record PurchasePaymentRequest(
        @NotNull(message = "Payment method is required")
        AccountPayablePayment.PaymentMethod method,

        @NotNull(message = "Amount is required")
        @PositiveOrZero(message = "Amount must be zero or positive")
        BigDecimal amount,

        String reference
) {}
