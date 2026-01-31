package com.sergiocodev.app.dto.activeingredient;

import com.sergiocodev.app.model.ActiveIngredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveIngredientResponse {

    private Long id;
    private String name;
    private String description;
    private boolean active;

    public ActiveIngredientResponse(ActiveIngredient activeIngredient) {
        this.id = activeIngredient.getId();
        this.name = activeIngredient.getName();
        this.description = activeIngredient.getDescription();
        this.active = activeIngredient.isActive();
    }
}
