package com.sergiocodev.app.dto.accountpayable;

import com.sergiocodev.app.model.AccountPayable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountPayableResponse {
    private Long id;
    private Long purchaseId;
    private String supplierName;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal pendingBalance;
    private AccountPayable.PayableStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
