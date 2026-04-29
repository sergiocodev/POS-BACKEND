package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de clientes de la botica.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Busca un cliente por su número de documento (DNI/RUC).
     * @param documentNumber Número de identidad.
     * @return Optional con el cliente.
     */
    Optional<Customer> findByDocumentNumber(String documentNumber);

    /**
     * Comprueba si un número de documento ya está registrado.
     */
    boolean existsByDocumentNumber(String documentNumber);
}
