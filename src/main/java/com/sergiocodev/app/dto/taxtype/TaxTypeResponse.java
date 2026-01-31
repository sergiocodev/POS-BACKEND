package com.sergiocodev.app.dto.taxtype;

import com.sergiocodev.app.model.TaxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxTypeResponse {

    private Long id;
    private String name;
    private BigDecimal rate;
    private String codeSunat;
    private boolean active;

    public TaxTypeResponse(TaxType taxType) {
        this.id = taxType.getId();
        this.name = taxType.getName();
        this.rate = taxType.getRate();
        this.codeSunat = taxType.getCodeSunat();
        this.active = taxType.isActive();
    }
}
