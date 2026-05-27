package com.sergiocodev.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientId implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "ingredient_id")
    private Long ingredientId;
}
