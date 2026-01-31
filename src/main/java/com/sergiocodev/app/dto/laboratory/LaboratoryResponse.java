package com.sergiocodev.app.dto.laboratory;

import com.sergiocodev.app.model.Laboratory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryResponse {

    private Long id;
    private String name;
    private boolean active;

    public LaboratoryResponse(Laboratory laboratory) {
        this.id = laboratory.getId();
        this.name = laboratory.getName();
        this.active = laboratory.isActive();
    }
}
