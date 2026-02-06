package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.purchase.PurchaseItemRequest;
import com.sergiocodev.app.dto.purchase.PurchaseItemResponse;
import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
import com.sergiocodev.app.model.Purchase;
import com.sergiocodev.app.model.PurchaseItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "establishmentName", source = "establishment.name")
    @Mapping(target = "username", source = "user.username")
    PurchaseResponse toResponse(Purchase entity);

    @Mapping(target = "productName", source = "product.name")
    PurchaseItemResponse toItemResponse(PurchaseItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "arrivalDate", ignore = true)
    @Mapping(target = "subTotal", ignore = true)
    @Mapping(target = "tax", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Purchase toEntity(PurchaseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "purchase", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    PurchaseItem toItemEntity(PurchaseItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "arrivalDate", ignore = true)
    @Mapping(target = "subTotal", ignore = true)
    @Mapping(target = "tax", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PurchaseRequest request, @MappingTarget Purchase entity);
}
