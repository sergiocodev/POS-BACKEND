package com.sergiocodev.app.dto.company;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
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
        String logoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
