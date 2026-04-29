package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de categorías de productos.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca una categoría por su nombre.
     * @param name Nombre de la categoría.
     * @return Optional con la categoría encontrada.
     */
    Optional<Category> findByName(String name);

    /**
     * Verifica si existe una categoría con el nombre especificado.
     * @param name Nombre a verificar.
     * @return true si existe.
     */
    boolean existsByName(String name);
}
