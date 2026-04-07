package com.sergiocodev.app.dto.company;

public record CompanyMinimalResponse(
    String ruc,
    String name,
    String address,
    String ubigeo,
    String urbanization,
    String phone,
    String email,
    String logoUrl
) {}
