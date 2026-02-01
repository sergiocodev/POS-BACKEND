package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveIngredientRepository extends JpaRepository<ActiveIngredient, Long> {
    Optional<ActiveIngredient> findByName(String name);

    boolean existsByName(String name);

    List<ActiveIngredient> findByNameContainingIgnoreCase(String name);
}
