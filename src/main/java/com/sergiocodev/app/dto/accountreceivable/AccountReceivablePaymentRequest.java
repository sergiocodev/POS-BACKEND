package com.sergiocodev.app.dto.accountreceivable;

import com.sergiocodev.app.model.AccountReceivablePayment;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountReceivablePaymentRequest(
        @NotNull(message = "Account Receivable ID is required") Long accountReceivableId,
        @NotNull(message = "Cash Session ID is required") Long cashSessionId,
        @NotNull(message = "Amount is required") BigDecimal amount,
        @NotNull(message = "Payment method is required") AccountReceivablePayment.PaymentMethod paymentMethod,
        String reference,
        String notes) {
}
