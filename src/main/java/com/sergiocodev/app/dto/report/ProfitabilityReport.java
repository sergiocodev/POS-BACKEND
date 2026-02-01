package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitabilityReport {
    private Long productId;
    private String productName;
    private BigDecimal quantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal marginPercentage;
}
