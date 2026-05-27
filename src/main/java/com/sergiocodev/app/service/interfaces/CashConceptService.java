package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.model.CashConcept;
import java.util.List;

public interface CashConceptService {

    CashConcept findOrCreateSaleConcept(String paymentMethodName);

    CashConcept findOrCreatePurchaseConcept(String paymentMethodName);

    CashConcept findOrCreateReceivableConcept(String paymentMethodName);

    CashConcept findOrCreatePayableConcept(String paymentMethodName);

    CashConcept findOrCreateByType(CashConcept.ConceptType type, String namePattern);

    List<CashConcept> findByType(CashConcept.ConceptType type);
}