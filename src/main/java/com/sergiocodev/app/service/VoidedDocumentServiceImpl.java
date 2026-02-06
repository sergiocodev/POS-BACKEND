package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.voideddocument.VoidedDocumentRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentResponse;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.VoidedDocument;
import com.sergiocodev.app.model.VoidedDocumentItem;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.SaleRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.repository.VoidedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoidedDocumentServiceImpl implements VoidedDocumentService {

        private final VoidedDocumentRepository repository;
        private final SaleRepository saleRepository;
        private final EstablishmentRepository establishmentRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public VoidedDocumentResponse create(VoidedDocumentRequest request, Long userId) {
                VoidedDocument entity = new VoidedDocument();
                entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                                .orElseThrow(() -> new RuntimeException("Establishment not found")));
                entity.setUser(userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found")));
                entity.setIssueDate(request.issueDate());
                entity.setSunatStatus(VoidedDocument.VoidedSunatStatus.PENDING);

                // Create items and mark sales as voided
                for (Long saleId : request.saleIds()) {
                        Sale sale = saleRepository.findById(saleId)
                                        .orElseThrow(() -> new RuntimeException("Sale not found: " + saleId));

                        // Mark sale as voided
                        sale.setVoided(true);
                        sale.setVoidedAt(LocalDateTime.now());
                        sale.setVoidReason("Incluido en baja SUNAT");
                        saleRepository.save(sale);

                        // Create voided document item
                        VoidedDocumentItem item = new VoidedDocumentItem();
                        item.setVoidedDocument(entity);
                        item.setSale(sale);
                        item.setDescription(request.description() != null
                                        ? request.description()
                                        : "Anulación de " + sale.getDocumentType() + " " + sale.getSeries() + "-"
                                                        + sale.getNumber());
                        entity.getItems().add(item);
                }

                return new VoidedDocumentResponse(repository.save(entity));
        }

        @Override
        @Transactional(readOnly = true)
        public List<VoidedDocumentResponse> getAll() {
                return repository.findAll().stream()
                                .map(VoidedDocumentResponse::new)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public VoidedDocumentResponse getById(Long id) {
                return repository.findById(id)
                                .map(VoidedDocumentResponse::new)
                                .orElseThrow(() -> new RuntimeException("Voided document not found"));
        }

        @Override
        @Transactional
        public VoidedDocumentResponse updateSunatStatus(Long id, VoidedDocument.VoidedSunatStatus status,
                        String description) {
                VoidedDocument doc = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Voided document not found"));
                doc.setSunatStatus(status);
                doc.setSunatDescription(description);
                return new VoidedDocumentResponse(repository.save(doc));
        }

        @Override
        @Transactional(readOnly = true)
        public List<VoidedDocumentResponse> getByEstablishment(Long establishmentId) {
                return repository.findByEstablishmentId(establishmentId).stream()
                                .map(VoidedDocumentResponse::new)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void processDailyVoids(Long establishmentId) {
                List<VoidedDocument> pendings = repository.findByEstablishmentId(establishmentId).stream()
                                .filter(d -> d.getSunatStatus() == VoidedDocument.VoidedSunatStatus.PENDING)
                                .collect(Collectors.toList());

                for (VoidedDocument doc : pendings) {
                        doc.setSunatStatus(VoidedDocument.VoidedSunatStatus.ACCEPTED);
                        doc.setSunatDescription("Aceptado por SUNAT (Simulado)");
                        doc.setTicketSunat("TS-" + System.currentTimeMillis());
                        repository.save(doc);
                }
        }
}
