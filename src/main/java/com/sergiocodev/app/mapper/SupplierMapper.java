package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.supplier.SupplierRequest;
import com.sergiocodev.app.dto.supplier.SupplierResponse;
import com.sergiocodev.app.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier entity);

    @Mapping(target = "id", ignore = true)
    Supplier toEntity(SupplierRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(SupplierRequest request, @MappingTarget Supplier entity);
}
