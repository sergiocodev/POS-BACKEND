package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import com.sergiocodev.app.dto.supplier.SupplierDetailResponse;

/**
 * Repositorio para la gestión de proveedores de la botica.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    /**
     * Busca un proveedor por su número de RUC.
     */
    Optional<Supplier> findByRuc(String ruc);

    @Query("""
        SELECT new com.sergiocodev.app.dto.supplier.SupplierDetailResponse(
            s.id, s.name, s.ruc, s.category, s.contactName, s.email, 
            s.status, s.rating, MAX(p.arrivalDate), SUM(p.total)
        )
        FROM Supplier s
        LEFT JOIN Purchase p ON p.supplier.id = s.id AND p.status = 'RECEIVED'
        GROUP BY s.id, s.name, s.ruc, s.category, s.contactName, s.email, s.status, s.rating
    """)
    Page<SupplierDetailResponse> getSupplierDetailsPaged(Pageable pageable);

    @Query("""
        SELECT COUNT(s) FROM Supplier s WHERE s.status = 'ACTIVO'
    """)
    long countActiveSuppliers();

    @Query("""
        SELECT COUNT(s) FROM Supplier s WHERE s.status = 'EN_EVALUACION'
    """)
    long countEvaluatingSuppliers();

    @Query("""
        SELECT COUNT(s) FROM Supplier s WHERE s.status = 'VENCIDO'
    """)
    long countExpiredSuppliers();

    @Query("""
        SELECT COALESCE(SUM(p.total), 0) FROM Purchase p 
        WHERE p.status = 'RECEIVED' AND YEAR(p.arrivalDate) = YEAR(CURRENT_DATE)
    """)
    java.math.BigDecimal calculateTotalSpendCurrentYear();

    @Query("""
        SELECT COALESCE(AVG(s.rating), 0) FROM Supplier s
    """)
    java.math.BigDecimal calculateAverageRating();
}
