package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión del personal de la empresa.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Busca un empleado por su número de documento de identidad.
     */
    Optional<Employee> findByDocumentNumber(String documentNumber);

    /**
     * Localiza el perfil de empleado vinculado a un usuario del sistema.
     */
    Optional<Employee> findByUserId(Long userId);
}
