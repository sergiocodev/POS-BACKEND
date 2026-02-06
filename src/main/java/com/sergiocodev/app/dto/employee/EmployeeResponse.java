package com.sergiocodev.app.dto.employee;

import com.sergiocodev.app.model.Employee;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String documentNumber,
        String username,
        boolean active) {
    public EmployeeResponse(Employee employee) {
        this(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDocumentNumber(),
                employee.getUser() != null ? employee.getUser().getUsername() : null,
                employee.isActive());
    }
}
