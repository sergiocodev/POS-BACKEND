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
        WHERE (:providerInfo IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :providerInfo, '%')) OR s.ruc LIKE CONCAT('%', :providerInfo, '%'))
        AND (:category IS NULL OR LOWER(s.category) LIKE LOWER(CONCAT('%', :category, '%')))
        AND (:contactInfo IS NULL OR LOWER(s.contactName) LIKE LOWER(CONCAT('%', :contactInfo, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :contactInfo, '%')))
        GROUP BY s.id, s.name, s.ruc, s.category, s.contactName, s.email, s.status, s.rating
    """)
    Page<SupplierDetailResponse> getSupplierDetailsPaged(
        @org.springframework.data.repository.query.Param("providerInfo") String providerInfo,
        @org.springframework.data.repository.query.Param("category") String category,
        @org.springframework.data.repository.query.Param("contactInfo") String contactInfo,
        Pageable pageable
    );

    @Query("SELECT COUNT(DISTINCT p.supplier.id) FROM Purchase p WHERE p.establishment.id = :establishmentId AND p.supplier.status = 'ACTIVO' AND p.arrivalDate >= :start AND p.arrivalDate <= :end")
    long countActiveSuppliersByEstablishment(@org.springframework.data.repository.query.Param("establishmentId") Long establishmentId, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.supplier.id) FROM Purchase p WHERE p.establishment.id = :establishmentId AND p.supplier.status = 'EN_EVALUACION' AND p.arrivalDate >= :start AND p.arrivalDate <= :end")
    long countEvaluatingSuppliersByEstablishment(@org.springframework.data.repository.query.Param("establishmentId") Long establishmentId, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.supplier.id) FROM Purchase p WHERE p.establishment.id = :establishmentId AND p.supplier.status = 'VENCIDO' AND p.arrivalDate >= :start AND p.arrivalDate <= :end")
    long countExpiredSuppliersByEstablishment(@org.springframework.data.repository.query.Param("establishmentId") Long establishmentId, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Purchase p WHERE p.establishment.id = :establishmentId AND p.status = 'RECEIVED' AND p.arrivalDate >= :start AND p.arrivalDate <= :end")
    java.math.BigDecimal sumTotalSpendByEstablishment(@org.springframework.data.repository.query.Param("establishmentId") Long establishmentId, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT COALESCE(AVG(s.rating), 0) FROM Supplier s WHERE EXISTS (SELECT 1 FROM Purchase p WHERE p.supplier = s AND p.establishment.id = :establishmentId AND p.arrivalDate >= :start AND p.arrivalDate <= :end)")
    java.math.BigDecimal calculateAverageRatingByEstablishment(@org.springframework.data.repository.query.Param("establishmentId") Long establishmentId, @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}
