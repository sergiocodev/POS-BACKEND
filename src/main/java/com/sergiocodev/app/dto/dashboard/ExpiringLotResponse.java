package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record ExpiringLotResponse(
        @JsonProperty("inventory_id") Long inventoryId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("lot_code") String lotCode,
        int quantity,
        @JsonProperty("expiry_date") LocalDate expiryDate,
        @JsonProperty("days_until_expiry") long daysUntilExpiry,
        boolean urgent) {
}
