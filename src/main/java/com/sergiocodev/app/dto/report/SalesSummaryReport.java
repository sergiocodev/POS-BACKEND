package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalTransactions;
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private Long voidedCount;
    private BigDecimal voidedAmount;
    private Map<String, Long> countByDocumentType;
    private Map<String, BigDecimal> amountByDocumentType;
}
