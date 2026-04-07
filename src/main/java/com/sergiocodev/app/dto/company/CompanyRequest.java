package com.sergiocodev.app.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
    @NotBlank(message = "RUC is required")
    @Size(min = 11, max = 11, message = "RUC must be 11 digits")
    String ruc,

    @NotBlank(message = "Name is required")
    String name,

    String address,
    String ubigeo,
    String urbanization,
    String phone,
    String email,
    String logoUrl
) {}
