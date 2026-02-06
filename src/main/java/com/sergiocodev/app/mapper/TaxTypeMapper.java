package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.taxtype.TaxTypeRequest;
import com.sergiocodev.app.dto.taxtype.TaxTypeResponse;
import com.sergiocodev.app.model.TaxType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaxTypeMapper {

    TaxTypeResponse toResponse(TaxType entity);

    @Mapping(target = "id", ignore = true)
    TaxType toEntity(TaxTypeRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(TaxTypeRequest request, @MappingTarget TaxType entity);
}
