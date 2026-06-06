package com.sergiocodev.app.specification;

import com.sergiocodev.app.model.Sale;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleSpecification {

    public static Specification<Sale> filterSales(
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

        return (root, query, cb) -> buildPredicate(root, cb, startDate, endDate, documentType, series, number,
                customerName, customerDocument, vendedorName, status, sunatStatus,
                total, paymentMethod, columnDate, establishmentId);
    }

    public static Predicate buildPredicate(
            Root<Sale> root,
            CriteriaBuilder cb,
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

        List<Predicate> predicates = new ArrayList<>();

        if (establishmentId != null) {
            predicates.add(cb.equal(root.get("establishment").get("id"), establishmentId));
        }
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
        }
        if (columnDate != null && !columnDate.isEmpty()) {
            // Partial match on date as string
            predicates.add(cb.like(root.get("date").as(String.class), "%" + columnDate + "%"));
        }
        if (documentType != null && !documentType.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("documentType").as(String.class)),
                    "%" + documentType.toLowerCase() + "%"));
        }
        if (number != null && !number.isEmpty()) {
            // Search in series, number or combined "series-number"
            String pattern = "%" + number.toLowerCase() + "%";
            jakarta.persistence.criteria.Expression<String> combined = cb
                    .concat(cb.concat(cb.lower(root.get("series")), "-"), cb.lower(root.get("number")));
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("series")), pattern),
                    cb.like(cb.lower(root.get("number")), pattern),
                    cb.like(combined, pattern)));
        }
        if (customerName != null && !customerName.isEmpty()) {
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("customer").get("name")), "%" + customerName.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("customer").get("documentNumber")),
                            "%" + customerName.toLowerCase() + "%")));
        }
        if (customerDocument != null && !customerDocument.isEmpty()) {
            predicates.add(cb.equal(root.get("customer").get("documentNumber"), customerDocument));
        }
        if (vendedorName != null && !vendedorName.isEmpty()) {
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("user").get("fullName")), "%" + vendedorName.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("user").get("username")), "%" + vendedorName.toLowerCase() + "%")));
        }
        if (total != null && !total.isEmpty()) {
            predicates.add(cb.like(root.get("total").as(String.class), "%" + total + "%"));
        }
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            String s = paymentMethod.toLowerCase();
            // Search strictly in paymentCondition (CONTADO/CREDITO)
            jakarta.persistence.criteria.Predicate p = cb.like(cb.lower(root.get("paymentCondition").as(String.class)),
                    "%" + s + "%");
            if ("crédito".contains(s) || "credito".contains(s))
                p = cb.or(p, cb.equal(root.get("paymentCondition"),
                        com.sergiocodev.app.model.Sale.PaymentCondition.CREDITO));
            if ("contado".contains(s))
                p = cb.or(p, cb.equal(root.get("paymentCondition"),
                        com.sergiocodev.app.model.Sale.PaymentCondition.CONTADO));

            predicates.add(p);
        }
        if (status != null && !status.isEmpty()) {
            String s = status.toLowerCase();
            jakarta.persistence.criteria.Predicate p = cb.like(cb.lower(root.get("status").as(String.class)),
                    "%" + s + "%");
            if ("completado".contains(s))
                p = cb.or(p, cb.equal(root.get("status"), com.sergiocodev.app.model.Sale.SaleStatus.COMPLETED));
            if ("anulado".contains(s))
                p = cb.or(p, cb.equal(root.get("status"), com.sergiocodev.app.model.Sale.SaleStatus.CANCELED));
            predicates.add(p);
        }
        if (sunatStatus != null && !sunatStatus.isEmpty()) {
            String s = sunatStatus.toLowerCase();
            jakarta.persistence.criteria.Predicate p = cb.like(cb.lower(root.get("sunatStatus").as(String.class)),
                    "%" + s + "%");
            if ("si".contains(s) || "aceptado".contains(s))
                p = cb.or(p, cb.equal(root.get("sunatStatus"), com.sergiocodev.app.model.Sale.SunatStatus.ACCEPTED));
            if ("pendiente".contains(s))
                p = cb.or(p, cb.equal(root.get("sunatStatus"), com.sergiocodev.app.model.Sale.SunatStatus.PENDING));
            if ("rechazado".contains(s))
                p = cb.or(p, cb.equal(root.get("sunatStatus"), com.sergiocodev.app.model.Sale.SunatStatus.REJECTED));
            if ("no".contains(s))
                p = cb.or(p, cb.notEqual(root.get("sunatStatus"), com.sergiocodev.app.model.Sale.SunatStatus.ACCEPTED));
            predicates.add(p);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
