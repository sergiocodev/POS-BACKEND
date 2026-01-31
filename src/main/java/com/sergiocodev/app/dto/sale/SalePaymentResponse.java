package com.sergiocodev.app.dto.sale; // Updated to force IDE refresh

import com.sergiocodev.app.model.SalePayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalePaymentResponse {

    private Long id;
    private SalePayment.PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String reference;
    private LocalDateTime createdAt;

    public SalePaymentResponse(SalePayment payment) {
        this.id = payment.getId();
        this.paymentMethod = payment.getPaymentMethod();
        this.amount = payment.getAmount();
        this.reference = payment.getReference();
        this.createdAt = payment.getCreatedAt();
    }
}
