package com.sergiocodev.app.repository;

import com.sergiocodev.app.dto.sale.SaleSummaryResponse;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.specification.SaleSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleRepositoryCustomImpl implements SaleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SaleSummaryResponse getSaleSummary(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String documentType,
            String series,
            String number,
            String customerName,
            String customerDocument,
            String vendedorName,
            String status,
            String sunatStatus,
            String total,
            String paymentMethod,
            String columnDate,
            Long establishmentId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Sale> root = query.from(Sale.class);
        query.distinct(true);

        Predicate predicate = SaleSpecification.buildPredicate(
                root, cb, startDate, endDate, documentType, series, number,
                customerName, customerDocument, vendedorName, status, sunatStatus,
                total, paymentMethod, columnDate, establishmentId);

        query.where(predicate);
        query.multiselect(root.get("documentType"), cb.sum(root.get("total")));
        query.groupBy(root.get("documentType"));

        List<Object[]> results = entityManager.createQuery(query).getResultList();

        BigDecimal totalFacturas = BigDecimal.ZERO;
        BigDecimal totalBoletas = BigDecimal.ZERO;
        BigDecimal totalNotaCredito = BigDecimal.ZERO;
        BigDecimal totalNotaDebito = BigDecimal.ZERO;
        BigDecimal totalNotaVenta = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        for (Object[] result : results) {
            Sale.SaleDocumentType type = (Sale.SaleDocumentType) result[0];
            BigDecimal sum = (BigDecimal) result[1];
            if (sum == null)
                sum = BigDecimal.ZERO;

            if (type != null) {
                switch (type) {
                    case FACTURA -> totalFacturas = sum;
                    case BOLETA -> totalBoletas = sum;
                    case NOTA_CREDITO -> totalNotaCredito = sum;
                    case NOTA_DEBITO -> totalNotaDebito = sum;
                    case NOTA_DE_VENTA, TICKET -> totalNotaVenta = totalNotaVenta.add(sum);
                }
            }
            totalNeto = totalNeto.add(sum);
        }

        return new SaleSummaryResponse(
                totalFacturas, totalBoletas, totalNotaCredito, totalNotaDebito, totalNotaVenta,
                totalNeto);
    }
}
