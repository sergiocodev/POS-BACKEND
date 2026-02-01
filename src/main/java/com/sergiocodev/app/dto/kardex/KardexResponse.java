package com.sergiocodev.app.dto.kardex;

import com.sergiocodev.app.model.Kardex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String movementType;
    private Integer quantity;
    private Integer balance;
    private String notes;
    private LocalDateTime createdAt;
    private String establishmentName;

    public KardexResponse(Kardex kardex) {
        this.id = kardex.getId();
        this.productId = kardex.getProduct().getId();
        this.productName = kardex.getProduct().getName();
        this.movementType = kardex.getMovementType().name();
        this.quantity = kardex.getQuantity();
        this.balance = kardex.getBalance();
        this.notes = kardex.getNotes();
        this.createdAt = kardex.getCreatedAt();
        if (kardex.getEstablishment() != null) {
            this.establishmentName = kardex.getEstablishment().getName();
        }
    }
}
