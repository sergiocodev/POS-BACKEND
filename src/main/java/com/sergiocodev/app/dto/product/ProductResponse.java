package com.sergiocodev.app.dto.product;

import com.sergiocodev.app.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String code;
    private String digemidCode;
    private String name;
    private String description;
    private String brandName;
    private String categoryName;
    private String laboratoryName;
    private String presentationDescription;
    private String taxTypeName;
    private boolean requiresPrescription;
    private boolean isGeneric;
    private String unitType;
    private Integer purchaseFactor;
    private String fractionLabel;
    private boolean active;
    private List<ProductIngredientResponse> ingredients;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.code = product.getCode();
        this.digemidCode = product.getDigemidCode();
        this.name = product.getName();
        this.description = product.getDescription();
        this.brandName = product.getBrand() != null ? product.getBrand().getName() : null;
        this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        this.laboratoryName = product.getLaboratory() != null ? product.getLaboratory().getName() : null;
        this.presentationDescription = product.getPresentation() != null ? product.getPresentation().getDescription()
                : null;
        this.taxTypeName = product.getTaxType() != null ? product.getTaxType().getName() : null;
        this.requiresPrescription = product.isRequiresPrescription();
        this.isGeneric = product.isGeneric();
        this.unitType = product.getUnitType() != null ? product.getUnitType().name() : null;
        this.purchaseFactor = product.getPurchaseFactor();
        this.fractionLabel = product.getFractionLabel();
        this.active = product.isActive();
        this.ingredients = product.getIngredients() != null
                ? product.getIngredients().stream().map(ProductIngredientResponse::new).collect(Collectors.toList())
                : null;
    }
}
