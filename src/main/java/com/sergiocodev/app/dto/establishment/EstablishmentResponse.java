package com.sergiocodev.app.dto.establishment;

import com.sergiocodev.app.model.Establishment;

public record EstablishmentResponse(
        Long id,
        String name,
        String address,
        String codeSunat) {
    public EstablishmentResponse(Establishment establishment) {
        this(
                establishment.getId(),
                establishment.getName(),
                establishment.getAddress(),
                establishment.getCodeSunat());
    }
}
