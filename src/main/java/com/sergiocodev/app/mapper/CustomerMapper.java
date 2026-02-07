package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.customer.CustomerRequest;
import com.sergiocodev.app.dto.customer.CustomerResponse;
import com.sergiocodev.app.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "accumulatedPoints", ignore = true)
    Customer toEntity(CustomerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "accumulatedPoints", ignore = true)
    void updateEntity(CustomerRequest request, @MappingTarget Customer entity);
}
