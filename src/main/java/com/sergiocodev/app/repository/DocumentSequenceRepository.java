package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.DocumentSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    /**
     * Adquiere un bloqueo pesimista en escritura (PESSIMISTIC_WRITE) a nivel de base de datos para retener 
     * el consecutivo de la secuencia asociada de un determinado comprobante SUNAT (facturas o boletas).
     * Bloqueando el acceso concurrente se previene efectivamente que dos request que llegan idénticamente 
     * en el tiempo adquieran el mismo identificador de comprobante (correlativos duplicados).
     *
     * @param establishmentId ID del establecimiento asociado emisor.
     * @param documentType    Tipo de documento correlato (BOLETA, FACTURA, NOTA, etc.).
     * @param series          La serie específica unida al prefijo de comprobante.
     * @return El secuenciador para poder aumentar el ticket.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DocumentSequence ds WHERE ds.establishment.id = :establishmentId " +
            "AND ds.documentType = :documentType AND ds.series = :series AND ds.deletedAt IS NULL")
    Optional<DocumentSequence> findForUpdate(
            @Param("establishmentId") Long establishmentId,
            @Param("documentType") DocumentSequence.DocumentType documentType,
            @Param("series") String series);

    boolean existsByEstablishmentIdAndDocumentTypeAndSeries(Long establishmentId,
                                                             DocumentSequence.DocumentType documentType,
                                                             String series);

    boolean existsByEstablishmentIdAndDocumentTypeAndSeriesAndIdNot(Long establishmentId,
                                                                     DocumentSequence.DocumentType documentType,
                                                                     String series,
                                                                     Long id);
}
