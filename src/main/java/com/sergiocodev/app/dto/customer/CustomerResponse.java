package com.sergiocodev.app.dto.customer;

import com.sergiocodev.app.model.DocumentType;
import java.time.LocalDateTime;

public record CustomerResponse(
                Long id,
                DocumentType documentType,
                String documentNumber,
                String name,
                String phone,
                String email,
                String address,
                Integer accumulatedPoints,
                LocalDateTime createdAt) {
}
