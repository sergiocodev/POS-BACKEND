package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "establishmentId", source = "establishment.id")
    @Mapping(target = "establishmentName", source = "establishment.name")
    @Mapping(target = "lotId", source = "lot.id")
    @Mapping(target = "lotCode", source = "lot.lotCode")
    @Mapping(target = "productName", source = "lot.product.name")
    InventoryResponse toResponse(Inventory entity);
}
