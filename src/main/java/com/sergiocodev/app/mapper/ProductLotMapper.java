package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.productlot.ProductLotRequest;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import com.sergiocodev.app.model.ProductLot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductLotMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    ProductLotResponse toResponse(ProductLot entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductLot toEntity(ProductLotRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ProductLotRequest request, @MappingTarget ProductLot entity);
}
