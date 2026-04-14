package com.sergiocodev.app.dto.customer;

import com.sergiocodev.app.model.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotNull(message = "Document type is required") DocumentType documentType,

        @Size(max = 20, message = "Document number max 20 characters") String documentNumber,

        @NotBlank(message = "Name is required") @Size(max = 200, message = "Name max 200 characters") String name,

        @Size(max = 20, message = "Phone max 20 characters") String phone,

        @Email(message = "Email must be valid") @Size(max = 100, message = "Email max 100 characters") String email,

        @Size(max = 200, message = "Address max 200 characters") String address) {
}
