package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para la información legal y comercial de la empresa.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Busca la empresa por su número de RUC.
     * @param ruc Registro Único de Contribuyentes.
     * @return Optional con la empresa.
     */
    Optional<Company> findByRuc(String ruc);
    
    /**
     * Método de conveniencia para obtener la empresa principal configurada.
     * @return La primera empresa encontrada en el sistema.
     */
    default Optional<Company> findMainCompany() {
        return findAll().stream().findFirst();
    }
}
