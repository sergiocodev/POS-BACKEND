package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyHeatReport {
    private int hour;
    private BigDecimal totalRevenue;
    private Long transactionCount;
}
