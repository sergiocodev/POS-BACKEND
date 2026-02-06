package com.sergiocodev.app.dto.supplier;

import com.sergiocodev.app.model.Supplier;

public record SupplierResponse(
        Long id,
        String name,
        String ruc,
        String phone,
        String email,
        String address,
        boolean active) {
    public SupplierResponse(Supplier supplier) {
        this(
                supplier.getId(),
                supplier.getName(),
                supplier.getRuc(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.isActive());
    }
}
