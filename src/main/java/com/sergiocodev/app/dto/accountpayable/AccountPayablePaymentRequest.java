package com.sergiocodev.app.dto.accountpayable;

import com.sergiocodev.app.model.AccountPayablePayment;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountPayablePaymentRequest(
        @NotNull(message = "Account Payable ID is required") Long accountPayableId,
        @NotNull(message = "Amount is required") BigDecimal amount,
        @NotNull(message = "Payment method is required") AccountPayablePayment.PaymentMethod paymentMethod,
        String reference,
        String notes) {
}
