package com.sergiocodev.app.dto.employee;

import com.sergiocodev.app.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String username;
    private boolean active;

    public EmployeeResponse(Employee employee) {
        this.id = employee.getId();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.documentNumber = employee.getDocumentNumber();
        this.username = employee.getUser() != null ? employee.getUser().getUsername() : null;
        this.active = employee.isActive();
    }
}
