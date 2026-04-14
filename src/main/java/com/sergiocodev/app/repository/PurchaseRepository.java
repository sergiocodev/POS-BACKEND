package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

        @EntityGraph(attributePaths = { "items", "supplier", "establishment", "user" })
        java.util.List<Purchase> findAll();

        @EntityGraph(attributePaths = { "items", "supplier", "establishment", "user" })
        @Query("SELECT p FROM Purchase p WHERE p.establishment.id = :establishmentId " +
                        "AND p.issueDate >= :startDate AND p.issueDate <= :endDate " +
                        "ORDER BY p.issueDate DESC")
        Page<Purchase> findByEstablishmentAndDateRange(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "items", "supplier", "establishment" })
        @Query("SELECT p FROM Purchase p WHERE p.establishment.id = :establishmentId " +
                        "AND p.issueDate >= :startDate AND p.issueDate <= :endDate " +
                        "ORDER BY p.issueDate DESC")
        List<Purchase> findByEstablishmentAndDateRangeList(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
