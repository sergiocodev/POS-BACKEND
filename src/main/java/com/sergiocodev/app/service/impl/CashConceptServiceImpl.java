package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.repository.CashConceptRepository;
import com.sergiocodev.app.service.interfaces.CashConceptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashConceptServiceImpl implements CashConceptService {

    private final CashConceptRepository cashConceptRepository;

    @Override
    @Transactional
    public CashConcept findOrCreateSaleConcept(String paymentMethodName) {
        String methodStr = paymentMethodName.toUpperCase();
        return cashConceptRepository.findByType(CashConcept.ConceptType.IN).stream()
                .filter(c -> c.getName().toUpperCase().contains(methodStr) && c.getName().toLowerCase().contains("venta"))
                .findFirst()
                .orElseGet(() -> cashConceptRepository.findByType(CashConcept.ConceptType.IN).stream()
                        .filter(c -> c.getName().toUpperCase().contains(methodStr))
                        .findFirst()
                        .orElseGet(() -> {
                            log.info("Creating new CashConcept for sale income: VENTA {}", methodStr);
                            CashConcept newConcept = new CashConcept();
                            newConcept.setName("VENTA " + methodStr);
                            newConcept.setType(CashConcept.ConceptType.IN);
                            newConcept.setIsSystem(true);
                            return cashConceptRepository.save(newConcept);
                        }));
    }

    @Override
    @Transactional
    public CashConcept findOrCreatePurchaseConcept(String paymentMethodName) {
        String methodStr = paymentMethodName.toUpperCase();
        return cashConceptRepository.findByType(CashConcept.ConceptType.OUT).stream()
                .filter(c -> c.getName().toUpperCase().contains(methodStr) && c.getName().toLowerCase().contains("compra"))
                .findFirst()
                .orElseGet(() -> cashConceptRepository.findByType(CashConcept.ConceptType.OUT).stream()
                        .filter(c -> c.getName().toUpperCase().contains(methodStr))
                        .findFirst()
                        .orElseGet(() -> {
                            log.info("Creating new CashConcept for purchase expense: COMPRA {}", methodStr);
                            CashConcept newConcept = new CashConcept();
                            newConcept.setName("COMPRA " + methodStr);
                            newConcept.setType(CashConcept.ConceptType.OUT);
                            newConcept.setIsSystem(true);
                            return cashConceptRepository.save(newConcept);
                        }));
    }

    @Override
    @Transactional
    public CashConcept findOrCreateReceivableConcept(String paymentMethodName) {
        String methodStr = paymentMethodName.toUpperCase();
        return cashConceptRepository.findByType(CashConcept.ConceptType.IN).stream()
                .filter(c -> c.getName().toUpperCase().contains(methodStr) && c.getName().toLowerCase().contains("cobro"))
                .findFirst()
                .orElseGet(() -> cashConceptRepository.findByType(CashConcept.ConceptType.IN).stream()
                        .filter(c -> c.getName().toUpperCase().contains(methodStr))
                        .findFirst()
                        .orElseGet(() -> {
                            log.info("Creating new CashConcept for receivable: COBRO {}", methodStr);
                            CashConcept newConcept = new CashConcept();
                            newConcept.setName("COBRO " + methodStr);
                            newConcept.setType(CashConcept.ConceptType.IN);
                            newConcept.setIsSystem(true);
                            return cashConceptRepository.save(newConcept);
                        }));
    }

    @Override
    @Transactional
    public CashConcept findOrCreatePayableConcept(String paymentMethodName) {
        String methodStr = paymentMethodName.toUpperCase();
        String conceptName = "PAGO PROVEEDOR " + methodStr;

        return cashConceptRepository.findByType(CashConcept.ConceptType.OUT).stream()
                .filter(c -> c.getName().toUpperCase().contains(methodStr) && (c.getName().toUpperCase().contains("PAGO") || c.getName().toUpperCase().contains("PROVEEDOR")))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating new CashConcept for payable: {}", conceptName);
                    CashConcept newConcept = new CashConcept();
                    newConcept.setName(conceptName);
                    newConcept.setType(CashConcept.ConceptType.OUT);
                    newConcept.setIsSystem(true);
                    return cashConceptRepository.save(newConcept);
                });
    }

    @Override
    @Transactional
    public CashConcept findOrCreateByType(CashConcept.ConceptType type, String namePattern) {
        return cashConceptRepository.findByType(type).stream()
                .filter(c -> c.getName().toUpperCase().contains(namePattern.toUpperCase()))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating new CashConcept: {} - {}", type, namePattern);
                    CashConcept newConcept = new CashConcept();
                    newConcept.setName(namePattern.toUpperCase());
                    newConcept.setType(type);
                    newConcept.setIsSystem(true);
                    return cashConceptRepository.save(newConcept);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashConcept> findByType(CashConcept.ConceptType type) {
        return cashConceptRepository.findByType(type);
    }
}
