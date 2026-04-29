package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.VoidedDocument;
import com.sergiocodev.app.model.VoidedDocument.VoidedSunatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la gestión de documentos de anulación (Comunicación de Baja) para SUNAT.
 */
@Repository
public interface VoidedDocumentRepository extends JpaRepository<VoidedDocument, Long> {

    /**
     * Lista anulaciones por establecimiento.
     */
    List<VoidedDocument> findByEstablishmentId(Long establishmentId);

    /**
     * Filtra anulaciones por un rango de fechas.
     */
    List<VoidedDocument> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Filtra anulaciones por su estado de respuesta de SUNAT.
     */
    List<VoidedDocument> findBySunatStatus(VoidedSunatStatus status);

    /**
     * Búsqueda combinada por establecimiento y estado SUNAT.
     */
    List<VoidedDocument> findByEstablishmentIdAndSunatStatus(Long establishmentId, VoidedSunatStatus status);
}
