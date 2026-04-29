package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para los principios activos de los productos farmacéuticos.
 */
@Repository
public interface ActiveIngredientRepository extends JpaRepository<ActiveIngredient, Long> {
    /**
     * Busca un principio activo por su nombre exacto.
     * @param name Nombre del principio activo.
     * @return Optional con el resultado.
     */
    Optional<ActiveIngredient> findByName(String name);

    /**
     * Verifica la existencia de un principio activo por nombre.
     * @param name Nombre a verificar.
     * @return true si existe.
     */
    boolean existsByName(String name);

    /**
     * Búsqueda parcial de principios activos para autocompletados.
     * @param name Fragmento del nombre.
     * @return Lista de coincidencias ignorando mayúsculas.
     */
    List<ActiveIngredient> findByNameContainingIgnoreCase(String name);
}
