package com.sergiocodev.app.dto.cashmovement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CashMovementRequest(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal amount,
        
        @NotNull(message = "El concepto es obligatorio")
        Long conceptId,
        
        String reference,
        String description,
        
        @NotNull(message = "El ID de usuario es obligatorio")
        Long userId
) {}
