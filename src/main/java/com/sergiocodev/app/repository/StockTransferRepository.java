package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de traslados de mercadería entre establecimientos.
 */
@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    /**
     * Lista traslados enviados desde un establecimiento de origen.
     */
    List<StockTransfer> findBySourceEstablishmentId(Long sourceEstablishmentId);

    /**
     * Lista traslados recibidos en un establecimiento de destino.
     */
    List<StockTransfer> findByTargetEstablishmentId(Long targetEstablishmentId);

    /**
     * Filtra traslados por su estado (Pendiente, Enviado, Recibido, Cancelado).
     */
    List<StockTransfer> findByStatus(StockTransfer.TransferStatus status);
}
