package com.sergiocodev.app.repository;

import com.sergiocodev.app.dto.purchase.PurchaseSummaryResponse;
import com.sergiocodev.app.model.Purchase;
import com.sergiocodev.app.specification.PurchaseSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseRepositoryCustomImpl implements PurchaseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PurchaseSummaryResponse getPurchaseSummary(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String documentType,
            String series,
            String number,
            String supplierName,
            String supplierDocument,
            String userName,
            String status,
            String total,
            String paymentMethod,
            String columnDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Purchase> root = query.from(Purchase.class);
        query.distinct(true);

        Predicate predicate = PurchaseSpecification.buildPredicate(
                root, cb, startDate, endDate, documentType, series, number,
                supplierName, supplierDocument, userName, status,
                total, paymentMethod, columnDate);

        query.where(predicate);
        query.multiselect(root.get("documentType"), cb.sum(root.get("total")));
        query.groupBy(root.get("documentType"));

        List<Object[]> results = entityManager.createQuery(query).getResultList();

        BigDecimal totalFacturas = BigDecimal.ZERO;
        BigDecimal totalBoletas = BigDecimal.ZERO;
        BigDecimal totalGuiaRemision = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        for (Object[] result : results) {
            Purchase.PurchaseDocumentType type = (Purchase.PurchaseDocumentType) result[0];
            BigDecimal sum = (BigDecimal) result[1];
            if (sum == null)
                sum = BigDecimal.ZERO;

            if (type != null) {
                switch (type) {
                    case FACTURA -> totalFacturas = sum;
                    case BOLETA -> totalBoletas = sum;
                    case GUIA -> totalGuiaRemision = sum;
                }
            }
            totalNeto = totalNeto.add(sum);
        }

        return new PurchaseSummaryResponse(
                totalFacturas, totalBoletas, totalGuiaRemision, totalNeto);
    }
}
