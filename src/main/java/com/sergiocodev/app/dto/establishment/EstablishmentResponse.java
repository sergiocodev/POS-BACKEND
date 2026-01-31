package com.sergiocodev.app.dto.establishment;

import com.sergiocodev.app.model.Establishment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentResponse {

    private Long id;
    private String name;
    private String address;
    private String codeSunat;
    private boolean active;

    public EstablishmentResponse(Establishment establishment) {
        this.id = establishment.getId();
        this.name = establishment.getName();
        this.address = establishment.getAddress();
        this.codeSunat = establishment.getCodeSunat();
        this.active = establishment.isActive();
    }
}
