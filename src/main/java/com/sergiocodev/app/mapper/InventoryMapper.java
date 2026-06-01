package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.model.ProductUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "establishmentId", source = "establishment.id")
    @Mapping(target = "establishmentName", source = "establishment.name")
    @Mapping(target = "lotId", source = "lot.id")
    @Mapping(target = "lotCode", source = "lot.lotCode")
    @Mapping(target = "expiryDate", source = "lot.expiryDate")
    @Mapping(target = "productName", source = "lot.product.tradeName")
    @Mapping(target = "salesPrice", source = "lot", qualifiedByName = "mapSalesPrice")
    @Mapping(target = "unitName", source = "lot", qualifiedByName = "mapUnitName")
    @Mapping(target = "units", source = "lot", qualifiedByName = "mapUnits")
    InventoryResponse toResponse(Inventory entity);

    @Named("mapSalesPrice")
    default BigDecimal mapSalesPrice(ProductLot lot) {
        if (lot != null && lot.getProduct() != null && lot.getProduct().getUnits() != null) {
            return lot.getProduct().getUnits().stream()
                    .filter(ProductUnit::isBaseUnit)
                    .map(ProductUnit::getPrice)
                    .findFirst()
                    .orElse(
                        lot.getProduct().getUnits().stream()
                            .map(ProductUnit::getPrice)
                            .findFirst()
                            .orElse(null)
                    );
        }
        return null;
    }

    @Named("mapUnitName")
    default String mapUnitName(ProductLot lot) {
        if (lot != null && lot.getProduct() != null && lot.getProduct().getUnits() != null) {
            return lot.getProduct().getUnits().stream()
                    .filter(ProductUnit::isBaseUnit)
                    .map(ProductUnit::getUnitName)
                    .findFirst()
                    .orElse(
                        lot.getProduct().getUnits().stream()
                            .map(ProductUnit::getUnitName)
                            .findFirst()
                            .orElse(null)
                    );
        }
        return null;
    }

    @Named("mapUnits")
    default java.util.List<com.sergiocodev.app.dto.productunit.ProductUnitResponse> mapUnits(ProductLot lot) {
        if (lot != null && lot.getProduct() != null && lot.getProduct().getUnits() != null) {
            return lot.getProduct().getUnits().stream()
                    .map(u -> new com.sergiocodev.app.dto.productunit.ProductUnitResponse(
                            u.getId(),
                            u.getProduct().getId(),
                            u.getUnitName(),
                            u.getFactor(),
                            u.getBarcode(),
                            u.getSunatCode(),
                            u.getPrice(),
                            u.isBaseUnit()
                    )).toList();
        }
        return java.util.Collections.emptyList();
    }
}
