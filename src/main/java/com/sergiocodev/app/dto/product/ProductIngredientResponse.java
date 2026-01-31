package com.sergiocodev.app.dto.product;

import com.sergiocodev.app.model.ProductIngredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientResponse {

    private Long activeIngredientId;
    private String activeIngredientName;
    private String concentration;

    public ProductIngredientResponse(ProductIngredient pi) {
        this.activeIngredientId = pi.getId().getIngredientId();
        this.activeIngredientName = pi.getActiveIngredient() != null ? pi.getActiveIngredient().getName() : null;
        this.concentration = pi.getConcentration();
    }
}
