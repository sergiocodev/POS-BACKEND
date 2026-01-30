package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByName(String name);

    boolean existsByName(String name);
}
