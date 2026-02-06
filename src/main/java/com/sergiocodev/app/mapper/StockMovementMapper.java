package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import com.sergiocodev.app.model.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(target = "establishmentName", source = "establishment.name")
    @Mapping(target = "productName", source = "lot.product.name")
    @Mapping(target = "lotCode", source = "lot.lotCode")
    @Mapping(target = "userName", source = "user.username")
    StockMovementResponse toResponse(StockMovement entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    StockMovement toEntity(StockMovementRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(StockMovementRequest request, @MappingTarget StockMovement entity);
}
