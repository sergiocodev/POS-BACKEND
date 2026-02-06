package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.sale.*;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.model.SalePayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "establishmentName", source = "establishment.name")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "relatedSaleId", source = "relatedSale.id")
    SaleResponse toResponse(Sale entity);

    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "lotCode", source = "lot.lotCode")
    SaleItemResponse toItemResponse(SaleItem item);

    SalePaymentResponse toPaymentResponse(SalePayment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "series", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sunatStatus", ignore = true)
    @Mapping(target = "pdfUrl", ignore = true)
    @Mapping(target = "cdrUrl", ignore = true)
    @Mapping(target = "voided", ignore = true)
    @Mapping(target = "voidedAt", ignore = true)
    @Mapping(target = "voidReason", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "subTotal", ignore = true)
    @Mapping(target = "tax", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "cashSession", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hashCpe", ignore = true)
    @Mapping(target = "sunatMessage", ignore = true)
    @Mapping(target = "xmlUrl", ignore = true)
    @Mapping(target = "relatedSale", ignore = true)
    Sale toEntity(SaleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "appliedTaxRate", ignore = true)
    @Mapping(target = "unitCost", ignore = true)
    SaleItem toItemEntity(SaleItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    SalePayment toPaymentEntity(SalePaymentRequest request);
}
