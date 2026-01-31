package com.sergiocodev.app.dto.customer;

import com.sergiocodev.app.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    @Size(max = 20, message = "Document number cannot exceed 20 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Document number must contain only numbers")
    private String documentNumber;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 30, message = "Phone cannot exceed 30 characters")
    private String phone;

    @jakarta.validation.constraints.Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    private String address;
}
