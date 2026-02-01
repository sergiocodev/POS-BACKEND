package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.VoidedDocument;
import com.sergiocodev.app.model.VoidedDocument.VoidedSunatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoidedDocumentRepository extends JpaRepository<VoidedDocument, Long> {

    List<VoidedDocument> findByEstablishmentId(Long establishmentId);

    List<VoidedDocument> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);

    List<VoidedDocument> findBySunatStatus(VoidedSunatStatus status);

    List<VoidedDocument> findByEstablishmentIdAndSunatStatus(Long establishmentId, VoidedSunatStatus status);
}
