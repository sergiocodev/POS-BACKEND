package com.sergiocodev.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDashboard {
    private Long id;
    private String name;
    private BigDecimal quantity;
}
