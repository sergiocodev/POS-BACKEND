package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
        Optional<Product> findByCode(String code);

        /**
         * Busca productos en el Punto de Venta (POS) coincidiendo por código corporativo, nombre comercial, o principio activo de los componentes.
         * Retorna un grafo de entidades que incluye categoría, marca, laboratorio e ingredientes para agilizar la carga perdiendo el problema N+1.
         *
         * @param query El término de búsqueda (nombre, ingredientes o código).
         * @return Lista de productos que coinciden con el criterio (sin paginación, asume listado del POS).
         */
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {
                        "category", "brand", "laboratory", "presentation", "taxType", "ingredients",
                        "ingredients.activeIngredient"
        })
        @Query("SELECT DISTINCT p FROM Product p " +
                        "LEFT JOIN p.ingredients pi " +
                        "LEFT JOIN pi.activeIngredient ai " +
                        "WHERE LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(p.tradeName) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(ai.name) LIKE LOWER(CONCAT('%', :query, '%'))")
        java.util.List<Product> searchByQuery(@org.springframework.data.repository.query.Param("query") String query);

        /**
         * Recupera todos los productos filtrados de forma optativa por categoría o marca,
         * forzando fetch de entidades relacionadas para pintar eficientemente los catálogos en el frontend.
         *
         * @param categoryId ID de la clasificación categorica (opcional).
         * @param brandId    ID de la procedencia/marca (opcional).
         * @return Fichas completas de productos filtrados y pre-hidratados.
         */
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {
                        "category", "brand", "laboratory", "presentation", "taxType"
        })
        @Query("SELECT p FROM Product p WHERE " +
                        "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                        "(:brandId IS NULL OR p.brand.id = :brandId)")
        java.util.List<Product> findAllWithFilters(
                        @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
                        @org.springframework.data.repository.query.Param("brandId") Long brandId);
}
