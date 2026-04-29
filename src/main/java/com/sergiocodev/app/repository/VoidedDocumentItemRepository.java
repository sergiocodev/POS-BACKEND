package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.VoidedDocumentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para los ítems contenidos en un resumen de anulaciones SUNAT.
 */
@Repository
public interface VoidedDocumentItemRepository extends JpaRepository<VoidedDocumentItem, Long> {

    /**
     * Lista los ítems de un documento de anulación específico.
     */
    List<VoidedDocumentItem> findByVoidedDocumentId(Long voidedDocumentId);

    /**
     * Busca si una venta específica ya ha sido incluida en un proceso de anulación.
     */
    List<VoidedDocumentItem> findBySaleId(Long saleId);
}
