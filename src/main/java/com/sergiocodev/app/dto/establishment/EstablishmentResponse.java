package com.sergiocodev.app.dto.establishment;

import com.sergiocodev.app.model.Establishment;

public record EstablishmentResponse(
        Long id,
        String name,
        String address,
        String codeSunat,
        boolean active) {
    public EstablishmentResponse(Establishment establishment) {
        this(
                establishment.getId(),
                establishment.getName(),
                establishment.getAddress(),
                establishment.getCodeSunat(),
                establishment.isActive());
    }
}
