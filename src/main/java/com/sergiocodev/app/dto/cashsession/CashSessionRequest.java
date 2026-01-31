package com.sergiocodev.app.dto.cashsession;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashSessionRequest {

    @NotNull(message = "Cash register ID is required")
    private Long cashRegisterId;

    @NotNull(message = "Opening balance is required")
    private BigDecimal openingBalance;

    private String notes;
}
