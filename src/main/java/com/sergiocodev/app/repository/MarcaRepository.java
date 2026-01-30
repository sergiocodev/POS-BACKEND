package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    Optional<Marca> findByNombreMarca(String nombreMarca);

    boolean existsByNombreMarca(String nombreMarca);
}
