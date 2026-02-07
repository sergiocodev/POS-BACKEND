package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.TherapeuticAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TherapeuticActionRepository extends JpaRepository<TherapeuticAction, Long> {

    Optional<TherapeuticAction> findByName(String name);

    boolean existsByName(String name);
}
