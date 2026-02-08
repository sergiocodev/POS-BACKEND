package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashConceptRepository extends JpaRepository<CashConcept, Long> {
}
