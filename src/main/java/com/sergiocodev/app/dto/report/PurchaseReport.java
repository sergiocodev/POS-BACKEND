package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReport {
    private Long purchaseId;
    private String supplierName;
    private String documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDateTime arrivalDate;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String status;
    private Integer itemCount;
}
