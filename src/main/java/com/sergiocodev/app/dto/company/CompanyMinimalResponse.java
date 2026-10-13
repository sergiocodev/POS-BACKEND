package com.sergiocodev.app.dto.company;

public record CompanyMinimalResponse(
        String ruc,
        String name,
        String address,
        String ubigeo,
        String urbanization,
        String department,
        String city,
        String district,
        String phone,
        String email,
        String logoUrl) {
}
