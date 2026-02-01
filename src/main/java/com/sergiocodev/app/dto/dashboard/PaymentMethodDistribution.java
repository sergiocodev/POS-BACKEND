package com.sergiocodev.app.dto.dashboard;

import com.sergiocodev.app.model.SalePayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDistribution {
    private SalePayment.PaymentMethod method;
    private BigDecimal amount;
}
