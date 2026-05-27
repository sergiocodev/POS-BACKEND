package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long>, JpaSpecificationExecutor<Purchase> {

        @EntityGraph(attributePaths = { "items", "items.product", "items.productUnit", "supplier", "establishment", "user" })
        java.util.List<Purchase> findAll();

        @Override
        @EntityGraph(attributePaths = { "items", "items.product", "items.productUnit", "supplier", "establishment", "user" })
        org.springframework.data.domain.Page<Purchase> findAll(org.springframework.data.jpa.domain.Specification<Purchase> spec, org.springframework.data.domain.Pageable pageable);

        /**
         * Recupera un historial paginado de compras realizadas en un establecimiento dentro de un rango de fechas.
         * Trae consigo grafos de entidades completas (proveedores, dueños) para construir dashboards.
         *
         * @param establishmentId ID local/sucursal (la restricción de sucursal previene sobre exposiciones de data entre franquicias).
         * @param startDate       Fecha de corte inicial de búsquedas.
         * @param endDate         Tope o límite de la franja buscada.
         * @param pageable        Configuración estandar para orden y límites offset del repositorio.
         * @return Lote de compras ya procesadas o completadas por un administrador.
         */
        @EntityGraph(attributePaths = { "items", "items.product", "items.productUnit", "supplier", "establishment", "user" })
        @Query("SELECT p FROM Purchase p WHERE p.establishment.id = :establishmentId " +
                        "AND p.issueDate >= :startDate AND p.issueDate <= :endDate " +
                        "ORDER BY p.issueDate DESC")
        Page<Purchase> findByEstablishmentAndDateRange(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        /**
         * Recupera una lista irrestricta o no paginada de compras para un reporte transaccional a imprimir.
         * No abusa de un volcado en memoria en rangos pequeños y abstrae las relaciones de los lotes internamente.
         *
         * @param establishmentId ID origen físico del pedido.
         * @param startDate       Acotación por fecha abajo.
         * @param endDate         Cota o techo límite.
         * @return Representación flat para construir los reportes tabulares o de PDF.
         */
        @EntityGraph(attributePaths = { "items", "items.product", "items.productUnit", "supplier", "establishment" })
        @Query("SELECT p FROM Purchase p WHERE p.establishment.id = :establishmentId " +
                        "AND p.issueDate >= :startDate AND p.issueDate <= :endDate " +
                        "ORDER BY p.issueDate DESC")
        List<Purchase> findByEstablishmentAndDateRangeList(
                        @Param("establishmentId") Long establishmentId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
