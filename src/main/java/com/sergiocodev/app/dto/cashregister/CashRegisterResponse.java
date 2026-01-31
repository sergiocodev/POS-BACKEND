package com.sergiocodev.app.dto.cashregister;

import com.sergiocodev.app.model.CashRegister;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterResponse {

    private Long id;
    private String name;
    private Long establishmentId;
    private String establishmentName;
    private boolean active;

    public CashRegisterResponse(CashRegister register) {
        this.id = register.getId();
        this.name = register.getName();
        this.establishmentId = register.getEstablishment().getId();
        this.establishmentName = register.getEstablishment().getName();
        this.active = register.isActive();
    }
}
