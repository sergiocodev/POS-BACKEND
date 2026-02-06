package com.sergiocodev.app.dto.cashregister;

import com.sergiocodev.app.model.CashRegister;

public record CashRegisterResponse(
        Long id,
        String name,
        Long establishmentId,
        String establishmentName,
        boolean active) {
    public CashRegisterResponse(CashRegister register) {
        this(
                register.getId(),
                register.getName(),
                register.getEstablishment().getId(),
                register.getEstablishment().getName(),
                register.isActive());
    }
}
