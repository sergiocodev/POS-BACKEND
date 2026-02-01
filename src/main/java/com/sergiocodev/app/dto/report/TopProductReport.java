package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductReport {
    private Long productId;
    private String productName;
    private BigDecimal value; // Quantity or Amount
    private BigDecimal percentage;
    private BigDecimal cumulativePercentage;
}
