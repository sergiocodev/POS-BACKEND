package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashConceptRepository extends JpaRepository<CashConcept, Long> {
    List<CashConcept> findByType(CashConcept.ConceptType type);
}
