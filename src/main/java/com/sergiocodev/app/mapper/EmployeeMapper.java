package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.employee.EmployeeRequest;
import com.sergiocodev.app.dto.employee.EmployeeResponse;
import com.sergiocodev.app.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "username", source = "user.username")
    EmployeeResponse toResponse(Employee entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Employee toEntity(EmployeeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(EmployeeRequest request, @MappingTarget Employee entity);
}
