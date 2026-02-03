package com.sergiocodev.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReport {
    private Long saleId;
    private String customerName;
    private String employeeName;
    private String documentType;
    private String documentNumber;
    private LocalDateTime date;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String status;
    private String sunatStatus;
    private boolean isVoided;
}
