package com.sergiocodev.app.dto.sale; // Updated to force IDE refresh

import com.sergiocodev.app.model.Sale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private Long id;
    private String establishmentName;
    private String customerName;
    private String username;
    private Sale.SaleDocumentType documentType;
    private String series;
    private String number;
    private LocalDateTime date;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Sale.SaleStatus status;
    private Sale.SunatStatus sunatStatus;
    private String pdfUrl;
    private String cdrUrl;

    // Campos de NC/ND
    private Long relatedSaleId;
    private String noteCode;
    private String noteReason;

    // Campos de anulación
    private boolean isVoided;
    private LocalDateTime voidedAt;
    private String voidReason;

    private List<SaleItemResponse> items;
    private List<SalePaymentResponse> payments;

    public SaleResponse(Sale sale) {
        this.id = sale.getId();
        this.establishmentName = sale.getEstablishment().getName();
        this.customerName = sale.getCustomer() != null ? sale.getCustomer().getName() : "PUBLICO GENERAL";
        this.username = sale.getUser().getUsername();
        this.documentType = sale.getDocumentType();
        this.series = sale.getSeries();
        this.number = sale.getNumber();
        this.date = sale.getDate();
        this.subTotal = sale.getSubTotal();
        this.tax = sale.getTax();
        this.total = sale.getTotal();
        this.status = sale.getStatus();
        this.sunatStatus = sale.getSunatStatus();
        this.pdfUrl = sale.getPdfUrl();
        this.cdrUrl = sale.getCdrUrl();
        this.relatedSaleId = sale.getRelatedSale() != null ? sale.getRelatedSale().getId() : null;
        this.noteCode = sale.getNoteCode();
        this.noteReason = sale.getNoteReason();
        this.isVoided = sale.isVoided();
        this.voidedAt = sale.getVoidedAt();
        this.voidReason = sale.getVoidReason();
        this.items = sale.getItems().stream().map(SaleItemResponse::new).collect(Collectors.toList());
        this.payments = sale.getPayments().stream().map(SalePaymentResponse::new).collect(Collectors.toList());
    }
}
