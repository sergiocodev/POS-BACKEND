package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.PharmaceuticalForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmaceuticalFormRepository extends JpaRepository<PharmaceuticalForm, Long> {

    Optional<PharmaceuticalForm> findByName(String name);

    boolean existsByName(String name);
}
