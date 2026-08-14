package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.employee.EmployeeRequest;
import com.sergiocodev.app.dto.employee.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);

    List<EmployeeResponse> getAll();

    org.springframework.data.domain.Page<EmployeeResponse> getAllPaged(String fullName, String documentNumber, String username, org.springframework.data.domain.Pageable pageable);

    EmployeeResponse getById(Long id);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);
}
