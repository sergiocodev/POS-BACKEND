package com.sergiocodev.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePerformanceDashboard {
    private String fullName;
    private BigDecimal totalSold;
}
