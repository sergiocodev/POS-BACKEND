package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.SalePayment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalePaymentRequest {

    @NotNull(message = "Payment method is required")
    private SalePayment.PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String reference;
}
