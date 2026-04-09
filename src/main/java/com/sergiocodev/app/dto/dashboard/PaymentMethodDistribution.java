package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record PaymentMethodDistribution(
        @JsonProperty("payment_method") String paymentMethod,
        BigDecimal amount,
        long count,
        double percentage) {
}
