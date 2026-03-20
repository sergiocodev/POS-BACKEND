package com.sergiocodev.app.dto.accountreceivable;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountReceivableRequest(
                @NotNull(message = "Sale ID is required") Long saleId,
                @NotNull(message = "Customer ID is required") Long customerId,
                @NotNull(message = "Total amount is required") BigDecimal totalAmount,
                LocalDate dueDate,
                String notes) {
}
