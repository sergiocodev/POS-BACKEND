package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.product.ProductIngredientResponse;
import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductIngredient;
import com.sergiocodev.app.model.TherapeuticAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "laboratoryName", source = "laboratory.name")
    @Mapping(target = "presentationDescription", source = "presentation.description")
    @Mapping(target = "pharmaceuticalFormName", source = "pharmaceuticalForm.name")
    @Mapping(target = "taxTypeName", source = "taxType.name")
    @Mapping(target = "tradeName", source = "tradeName")
    @Mapping(target = "genericName", source = "genericName")
    @Mapping(target = "unitType", source = "unitType", qualifiedByName = "enumToString")

    @Mapping(target = "isGeneric", source = "generic")
    @Mapping(target = "therapeuticActionNames", source = "therapeuticActions", qualifiedByName = "mapTherapeuticActions")
    @Mapping(target = "therapeuticActionIds", source = "therapeuticActions", qualifiedByName = "mapTherapeuticActionIds")
    ProductResponse toResponse(Product entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "laboratory", ignore = true)
    @Mapping(target = "pharmaceuticalForm", ignore = true)
    @Mapping(target = "presentation", ignore = true)
    @Mapping(target = "taxType", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "unitType", ignore = true)
    @Mapping(target = "generic", source = "isGeneric")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "therapeuticActions", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "laboratory", ignore = true)
    @Mapping(target = "pharmaceuticalForm", ignore = true)
    @Mapping(target = "presentation", ignore = true)
    @Mapping(target = "taxType", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "unitType", ignore = true)
    @Mapping(target = "generic", source = "isGeneric")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "therapeuticActions", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "activeIngredientId", source = "id.ingredientId")
    @Mapping(target = "activeIngredientName", source = "activeIngredient.name")
    ProductIngredientResponse toIngredientResponse(ProductIngredient entity);

    @Named("enumToString")
    default String enumToString(Product.UnitType unitType) {
        return unitType != null ? unitType.name() : null;
    }

    @Named("mapTherapeuticActions")
    default List<String> mapTherapeuticActions(Set<TherapeuticAction> therapeuticActions) {
        if (therapeuticActions == null) {
            return null;
        }
        return therapeuticActions.stream()
                .map(TherapeuticAction::getName)
                .collect(Collectors.toList());
    }

    @Named("mapTherapeuticActionIds")
    default List<Long> mapTherapeuticActionIds(Set<TherapeuticAction> therapeuticActions) {
        if (therapeuticActions == null) {
            return null;
        }
        return therapeuticActions.stream()
                .map(TherapeuticAction::getId)
                .collect(Collectors.toList());
    }
}
