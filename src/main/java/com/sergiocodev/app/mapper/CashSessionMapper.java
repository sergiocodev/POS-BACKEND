package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.model.CashSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CashSessionMapper {

    @Mapping(target = "cashRegisterName", source = "cashRegister.name")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "establishmentId", source = "cashRegister.establishment.id")
    CashSessionResponse toResponse(CashSession entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cashRegister", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "closingBalance", ignore = true)
    @Mapping(target = "calculatedBalance", ignore = true)
    @Mapping(target = "diffAmount", ignore = true)
    @Mapping(target = "openedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    CashSession toEntity(CashSessionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cashRegister", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "openedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "closingBalance", ignore = true)
    @Mapping(target = "calculatedBalance", ignore = true)
    @Mapping(target = "diffAmount", ignore = true)
    void updateEntity(CashSessionRequest request, @MappingTarget CashSession entity);
}
