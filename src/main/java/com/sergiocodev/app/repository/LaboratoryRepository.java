package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    List<Laboratory> findByNameContainingIgnoreCase(String name);
}
