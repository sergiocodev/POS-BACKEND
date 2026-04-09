package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DashboardAlertsResponse(
        List<StockAlert> stock,
        List<SunatAlert> sunat) {

    public record StockAlert(
            @JsonProperty("product_name") String productName,
            @JsonProperty("lot_code") String lotCode,
            @JsonProperty("expiry_date") String expiryDate,
            int quantity,
            String status) {
    }

    public record SunatAlert(
            @JsonProperty("sale_id") Long saleId,
            @JsonProperty("document_type") String documentType,
            String series,
            String number,
            String status,
            String message) {
    }
}
