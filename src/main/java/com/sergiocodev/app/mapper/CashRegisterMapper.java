package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.cashregister.CashRegisterRequest;
import com.sergiocodev.app.dto.cashregister.CashRegisterResponse;
import com.sergiocodev.app.model.CashRegister;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CashRegisterMapper {

    @Mapping(target = "establishmentId", source = "establishment.id")
    @Mapping(target = "establishmentName", source = "establishment.name")
    CashRegisterResponse toResponse(CashRegister entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    CashRegister toEntity(CashRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    void updateEntity(CashRegisterRequest request, @MappingTarget CashRegister entity);
}
