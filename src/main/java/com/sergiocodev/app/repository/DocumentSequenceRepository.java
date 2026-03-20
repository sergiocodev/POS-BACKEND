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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DocumentSequence ds WHERE ds.establishment.id = :establishmentId " +
            "AND ds.documentType = :documentType AND ds.series = :series AND ds.deletedAt IS NULL")
    Optional<DocumentSequence> findForUpdate(
            @Param("establishmentId") Long establishmentId,
            @Param("documentType") DocumentSequence.DocumentType documentType,
            @Param("series") String series);
}
