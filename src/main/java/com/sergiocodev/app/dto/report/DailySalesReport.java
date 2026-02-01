package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesReport {
    private LocalDate date;
    private Long transactionCount;
    private BigDecimal totalSales;
    private BigDecimal totalTax;
    private BigDecimal totalDiscount;
}
