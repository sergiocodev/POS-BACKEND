package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.product.ProductIngredientResponse;
import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "laboratoryName", source = "laboratory.name")
    @Mapping(target = "presentationDescription", source = "presentation.description")
    @Mapping(target = "taxTypeName", source = "taxType.name")
    @Mapping(target = "unitType", source = "unitType", qualifiedByName = "enumToString")
    @Mapping(target = "isGeneric", source = "generic")
    ProductResponse toResponse(Product entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "laboratory", ignore = true)
    @Mapping(target = "presentation", ignore = true)
    @Mapping(target = "taxType", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "unitType", ignore = true)
    @Mapping(target = "generic", source = "isGeneric")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "laboratory", ignore = true)
    @Mapping(target = "presentation", ignore = true)
    @Mapping(target = "taxType", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "unitType", ignore = true)
    @Mapping(target = "generic", source = "isGeneric")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "activeIngredientId", source = "id.ingredientId")
    @Mapping(target = "activeIngredientName", source = "activeIngredient.name")
    ProductIngredientResponse toIngredientResponse(ProductIngredient entity);

    @Named("enumToString")
    default String enumToString(Product.UnitType unitType) {
        return unitType != null ? unitType.name() : null;
    }
}
