package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.VoidedDocumentService;

import com.sergiocodev.app.service.interfaces.DigitalSignatureService;
import com.sergiocodev.app.service.interfaces.InventoryService;
import com.sergiocodev.app.dto.sunat.VoidInvoiceRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.VoidedDocument;
import com.sergiocodev.app.model.VoidedDocumentItem;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.SaleRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.repository.VoidedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sergiocodev.app.util.XmlUblGenerator;
import com.sergiocodev.app.util.SunatOseClient;
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
        private final InventoryService inventoryService;
        private final com.sergiocodev.app.repository.CompanyRepository companyRepository;
        private final XmlUblGenerator xmlUblGenerator;
        private final DigitalSignatureService digitalSignatureService;
        private final SunatOseClient sunatOseClient;

        @Override
        @Transactional
        public VoidedDocumentResponse create(VoidedDocumentRequest request, Long userId) {
                VoidedDocument entity = new VoidedDocument();
                entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Establishment not found")));
                entity.setUser(userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
                entity.setIssueDate(request.issueDate());
                entity.setSunatStatus(VoidedDocument.VoidedSunatStatus.PENDING);

                for (Long saleId : request.saleIds()) {
                        Sale sale = saleRepository.findById(saleId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + saleId));

                        sale.setVoided(true);
                        sale.setVoidedAt(LocalDateTime.now());
                        sale.setVoidReason("Incluido en baja SUNAT");
                        saleRepository.save(sale);

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
                                .orElseThrow(() -> new ResourceNotFoundException("Voided document not found"));
        }

        @Override
        @Transactional
        public VoidedDocumentResponse updateSunatStatus(Long id, VoidedDocument.VoidedSunatStatus status,
                        String description) {
                VoidedDocument doc = repository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Voided document not found"));
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
                List<VoidedDocument> pendings = repository.findByEstablishmentIdAndSunatStatus(
                                establishmentId, VoidedDocument.VoidedSunatStatus.PENDING);

                for (VoidedDocument doc : pendings) {
                        doc.setSunatStatus(VoidedDocument.VoidedSunatStatus.ACCEPTED);
                        doc.setSunatDescription("Aceptado por SUNAT (Simulado)");
                        doc.setTicketSunat("TS-" + System.currentTimeMillis());
                        repository.save(doc);
                }
        }

        @Override
        @Transactional
        public VoidedDocumentResponse voidInvoice(VoidInvoiceRequest request,
                        Long userId) {
                Sale sale = saleRepository.findById(request.saleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + request.saleId()));

                if (sale.isVoided()) {
                        throw new BadRequestException("La venta ya está anulada");
                }

                VoidedDocument entity = new VoidedDocument();
                entity.setEstablishment(sale.getEstablishment());
                entity.setUser(userRepository.findById(userId).orElse(sale.getUser()));
                entity.setIssueDate(java.time.LocalDate.now());
                entity.setSunatStatus(VoidedDocument.VoidedSunatStatus.PENDING);

                String ticket = "RA-" + System.currentTimeMillis();
                entity.setTicketSunat(ticket);

                VoidedDocumentItem item = new VoidedDocumentItem();
                item.setVoidedDocument(entity);
                item.setSale(sale);
                item.setDescription(request.reason());
                entity.getItems().add(item);

                entity = repository.save(entity);

                try {
                        com.sergiocodev.app.model.Company company = companyRepository.findMainCompany()
                                        .orElseThrow(() -> new ResourceNotFoundException("Company not configured"));

                        String xml = xmlUblGenerator.generateVoidedDocumentXml(entity, company);
                        String fileName = ticket + ".xml";

                        String signedXml = digitalSignatureService.signXml(xml);
                        SunatOseClient.SunatOseResponse oseResponse = sunatOseClient.sendVoidedDocument(signedXml,
                                        fileName);

                        if ("0".equals(oseResponse.getStatusCode())) {
                                entity.setSunatStatus(VoidedDocument.VoidedSunatStatus.ACCEPTED);
                                entity.setSunatDescription(oseResponse.getStatusMessage());
                                entity.setXmlUrl("mock/voided/" + fileName);
                                entity.setCdrUrl("mock/voided/R-" + fileName);

                                inventoryService.reverseStockForSale(sale.getId());

                                sale.setVoided(true);
                                sale.setVoidedAt(LocalDateTime.now());
                                sale.setVoidReason(request.reason());
                                sale.setSunatStatus(Sale.SunatStatus.VOIDED);
                                saleRepository.save(sale);

                        } else {
                                entity.setSunatStatus(VoidedDocument.VoidedSunatStatus.REJECTED);
                                entity.setSunatDescription(oseResponse.getStatusMessage());
                        }

                        return new VoidedDocumentResponse(repository.save(entity));

                } catch (Exception e) {
                        entity.setSunatStatus(VoidedDocument.VoidedSunatStatus.REJECTED);
                        entity.setSunatDescription("Error interno: " + e.getMessage());
                        return new VoidedDocumentResponse(repository.save(entity));
                }
        }
}
