package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {
    Optional<UnitOfMeasure> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
