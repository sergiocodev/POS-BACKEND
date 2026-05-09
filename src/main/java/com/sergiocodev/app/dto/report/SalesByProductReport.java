package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesByProductReport {
    private Long productId;
    private String productName;
    private String categoryName;
    private String laboratoryName;
    private String therapeuticAction;
    private Long quantitySold;
    private BigDecimal totalRevenue;
}
