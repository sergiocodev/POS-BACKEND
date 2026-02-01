package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalesReport {
    private Long userId;
    private String userName;
    private BigDecimal totalRevenue;
    private BigDecimal quantitySold;
    private Long transactionCount;
}
