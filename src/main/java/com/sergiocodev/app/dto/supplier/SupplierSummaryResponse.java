package com.sergiocodev.app.dto.supplier;

import java.math.BigDecimal;

public record SupplierSummaryResponse(
        long activeSuppliers,
        long evaluatingSuppliers,
        long expiredSuppliers,
        BigDecimal totalSpendYear,
        BigDecimal averageRating
) {}
