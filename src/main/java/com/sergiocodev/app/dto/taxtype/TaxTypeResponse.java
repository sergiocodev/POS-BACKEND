package com.sergiocodev.app.dto.taxtype;

import java.math.BigDecimal;

public record TaxTypeResponse(
        Long id,
        String name,
        BigDecimal rate,
        String codeSunat,
        boolean active) {
}
