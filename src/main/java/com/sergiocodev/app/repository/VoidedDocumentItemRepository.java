package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.VoidedDocumentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoidedDocumentItemRepository extends JpaRepository<VoidedDocumentItem, Long> {

    List<VoidedDocumentItem> findByVoidedDocumentId(Long voidedDocumentId);

    List<VoidedDocumentItem> findBySaleId(Long saleId);
}
