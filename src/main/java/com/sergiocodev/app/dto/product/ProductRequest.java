package com.sergiocodev.app.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    private String code;

    @Size(max = 50, message = "DIGEMID code cannot exceed 50 characters")
    private String digemidCode;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Laboratory ID is required")
    private Long laboratoryId;

    @NotNull(message = "Presentation ID is required")
    private Long presentationId;

    @NotNull(message = "Tax type ID is required")
    private Long taxTypeId;

    private boolean requiresPrescription = false;
    private boolean isGeneric = false;

    @NotBlank(message = "Unit type is required")
    private String unitType = "UNI";

    @NotNull(message = "Purchase factor is required")
    private Integer purchaseFactor = 1;

    private String fractionLabel;
    private boolean active = true;

    private List<ProductIngredientRequest> ingredients;
}
