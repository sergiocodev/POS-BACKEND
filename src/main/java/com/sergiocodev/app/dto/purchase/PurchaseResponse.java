package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.Purchase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    private Long id;
    private String supplierName;
    private String establishmentName;
    private String username;
    private Purchase.PurchaseDocumentType documentType;
    private String series;
    private String number;
    private LocalDate issueDate;
    private LocalDateTime arrivalDate;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Purchase.PurchaseStatus status;
    private String notes;
    private List<PurchaseItemResponse> items;

    public PurchaseResponse(Purchase purchase) {
        this.id = purchase.getId();
        this.supplierName = purchase.getSupplier().getName();
        this.establishmentName = purchase.getEstablishment().getName();
        this.username = purchase.getUser().getUsername();
        this.documentType = purchase.getDocumentType();
        this.series = purchase.getSeries();
        this.number = purchase.getNumber();
        this.issueDate = purchase.getIssueDate();
        this.arrivalDate = purchase.getArrivalDate();
        this.subTotal = purchase.getSubTotal();
        this.tax = purchase.getTax();
        this.total = purchase.getTotal();
        this.status = purchase.getStatus();
        this.notes = purchase.getNotes();
        this.items = purchase.getItems().stream().map(PurchaseItemResponse::new).collect(Collectors.toList());
    }
}
