package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowRotationReport {
    private Long productId;
    private String productName;
    private LocalDateTime lastSaleDate;
    private BigDecimal currentStock;
}
