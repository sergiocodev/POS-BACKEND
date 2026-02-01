package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesReport {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalRevenue;
    private BigDecimal quantitySold;
}
