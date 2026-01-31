package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByDocumentNumber(String documentNumber);

    Optional<Employee> findByUserId(Long userId);
}
