package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.model.AccountPayable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountPayableMapper {

    @Mapping(target = "purchaseId", source = "purchase.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    AccountPayableResponse toResponse(AccountPayable entity);

}
