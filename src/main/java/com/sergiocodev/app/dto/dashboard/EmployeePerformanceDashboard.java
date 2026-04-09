package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record EmployeePerformanceDashboard(
        @JsonProperty("user_id") Long userId,
        String username,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("sales_count") long salesCount,
        @JsonProperty("total_amount") BigDecimal totalAmount) {
}
