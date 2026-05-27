package com.sergiocodev.app.specification;

import com.sergiocodev.app.model.Purchase;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseSpecification {

    public static Specification<Purchase> filterPurchases(
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

        return (root, query, cb) -> buildPredicate(root, cb, startDate, endDate, documentType, series, number,
                supplierName, supplierDocument, userName, status,
                total, paymentMethod, columnDate);
    }

    public static Predicate buildPredicate(
            Root<Purchase> root,
            CriteriaBuilder cb,
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

        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), startDate.toLocalDate()));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), endDate.toLocalDate()));
        }
        if (columnDate != null && !columnDate.isEmpty()) {
            predicates.add(cb.like(root.get("issueDate").as(String.class), "%" + columnDate + "%"));
        }
        if (documentType != null && !documentType.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("documentType").as(String.class)),
                    "%" + documentType.toLowerCase() + "%"));
        }
        if (number != null && !number.isEmpty()) {
            String pattern = "%" + number.toLowerCase() + "%";
            jakarta.persistence.criteria.Expression<String> combined = cb
                    .concat(cb.concat(cb.lower(root.get("series")), "-"), cb.lower(root.get("number")));
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("series")), pattern),
                    cb.like(cb.lower(root.get("number")), pattern),
                    cb.like(combined, pattern)));
        }
        if (supplierName != null && !supplierName.isEmpty()) {
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("supplier").get("name")), "%" + supplierName.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("supplier").get("ruc")),
                            "%" + supplierName.toLowerCase() + "%")));
        }
        if (supplierDocument != null && !supplierDocument.isEmpty()) {
            predicates.add(cb.equal(root.get("supplier").get("ruc"), supplierDocument));
        }
        if (userName != null && !userName.isEmpty()) {
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("user").get("fullName")), "%" + userName.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("user").get("username")), "%" + userName.toLowerCase() + "%")));
        }
        if (total != null && !total.isEmpty()) {
            predicates.add(cb.like(root.get("total").as(String.class), "%" + total + "%"));
        }
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            String s = paymentMethod.toLowerCase();
            jakarta.persistence.criteria.Predicate p = cb.like(cb.lower(root.get("paymentCondition").as(String.class)),
                    "%" + s + "%");
            if ("crédito".contains(s) || "credito".contains(s))
                p = cb.or(p, cb.equal(root.get("paymentCondition"),
                        com.sergiocodev.app.model.Purchase.PaymentCondition.CREDITO));
            if ("contado".contains(s))
                p = cb.or(p, cb.equal(root.get("paymentCondition"),
                        com.sergiocodev.app.model.Purchase.PaymentCondition.CONTADO));

            predicates.add(p);
        }
        if (status != null && !status.isEmpty()) {
            String s = status.toLowerCase();
            jakarta.persistence.criteria.Predicate p = cb.like(cb.lower(root.get("status").as(String.class)),
                    "%" + s + "%");
            if ("recibido".contains(s))
                p = cb.or(p, cb.equal(root.get("status"), com.sergiocodev.app.model.Purchase.PurchaseStatus.RECEIVED));
            if ("pendiente".contains(s))
                p = cb.or(p, cb.equal(root.get("status"), com.sergiocodev.app.model.Purchase.PurchaseStatus.PENDING));
            if ("anulado".contains(s))
                p = cb.or(p, cb.equal(root.get("status"), com.sergiocodev.app.model.Purchase.PurchaseStatus.CANCELED));
            predicates.add(p);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
