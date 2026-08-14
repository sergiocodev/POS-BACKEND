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

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Employee e LEFT JOIN e.user u WHERE " +
            "(:fullName IS NULL OR LOWER(CONCAT(e.firstName, ' ', COALESCE(e.lastName, ''))) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND " +
            "(:documentNumber IS NULL OR e.documentNumber LIKE CONCAT('%', :documentNumber, '%')) AND " +
            "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))")
    org.springframework.data.domain.Page<Employee> findAllFiltered(
            @org.springframework.data.repository.query.Param("fullName") String fullName,
            @org.springframework.data.repository.query.Param("documentNumber") String documentNumber,
            @org.springframework.data.repository.query.Param("username") String username,
            org.springframework.data.domain.Pageable pageable);
}
