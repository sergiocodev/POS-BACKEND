package com.sergiocodev.app.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.sergiocodev.app.dto.productunit.ProductUnitResponse;

public record InventoryResponse(
                Long id,
                Long establishmentId,
                String establishmentName,
                Long lotId,
                String lotCode,
                String productName,
                BigDecimal quantity,
                Integer minStock,
                Integer maxStock,
                String locationShelf,
                BigDecimal costPrice,
                BigDecimal salesPrice,
                String unitName,
                List<ProductUnitResponse> units,
                LocalDateTime lastMovement,
                LocalDate expiryDate) {
}
