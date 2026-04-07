package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    // Usually there is only one company, but we provide a way to find by RUC just in case
    Optional<Company> findByRuc(String ruc);
    
    // Helper to get the first (and usually only) company
    default Optional<Company> findMainCompany() {
        return findAll().stream().findFirst();
    }
}
