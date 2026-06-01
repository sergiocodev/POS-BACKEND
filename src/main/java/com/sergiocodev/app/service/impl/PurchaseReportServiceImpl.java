package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.AccountPayableRepository;
import com.sergiocodev.app.repository.PurchaseRepository;
import com.sergiocodev.app.service.interfaces.PurchaseReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseReportServiceImpl implements PurchaseReportService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseReportServiceImpl.class);

    private final PurchaseRepository purchaseRepository;
    private final AccountPayableRepository accountPayableRepository;

    public PurchaseReportServiceImpl(PurchaseRepository purchaseRepository,
                                      AccountPayableRepository accountPayableRepository) {
        this.purchaseRepository = purchaseRepository;
        this.accountPayableRepository = accountPayableRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseReport> getPurchases(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Purchase> purchases = purchaseRepository.findByEstablishmentAndDateRangeList(
                establishmentId, start.toLocalDate(), end.toLocalDate());
        return mapPurchasesToReport(purchases);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseDebtReport> getPurchaseDebtStatus(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Purchase> purchases = purchaseRepository.findByEstablishmentAndDateRangeList(
                establishmentId, start.toLocalDate(), end.toLocalDate());

        return purchases.stream().map(p -> {
            String group = "CANCELADO";
            BigDecimal pending = BigDecimal.ZERO;
            LocalDate dueDate = null;
            Integer overdueDays = 0;
            Integer creditDays = 0;
            LocalDate paymentDate = p.getIssueDate();

            if (p.getPaymentCondition() == Purchase.PaymentCondition.CREDITO) {
                AccountPayable account = accountPayableRepository.findByPurchaseId(p.getId()).orElse(null);
                if (account != null) {
                    pending = account.getPendingBalance();
                    dueDate = account.getDueDate();
                    if (dueDate != null && dueDate.isAfter(p.getIssueDate())) {
                        creditDays = (int) java.time.temporal.ChronoUnit.DAYS.between(p.getIssueDate(), dueDate);
                    }
                    if (account.getStatus() == AccountPayable.PayableStatus.PAID) {
                        group = "CANCELADO";
                        paymentDate = account.getUpdatedAt().toLocalDate();
                    } else {
                        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                            group = "VENCIDO";
                            overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                        } else {
                            group = "CREDITO";
                        }
                    }
                } else {
                    group = "CREDITO";
                    pending = p.getTotal();
                }
            }

            return new PurchaseDebtReport(
                    p.getId(), p.getIssueDate(),
                    p.getSupplier() != null ? p.getSupplier().getName() : "Proveedor",
                    p.getDocumentType() != null ? p.getDocumentType().name() : "Comprobante",
                    (p.getSeries() != null ? p.getSeries() + "-" : "") + (p.getNumber() != null ? p.getNumber() : ""),
                    p.getPaymentCondition() != null ? p.getPaymentCondition().name() : "CONTADO",
                    "Efectivo", paymentDate, creditDays, dueDate, overdueDays, p.getTotal(), pending, group);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchasesByCategoryDetailReport> getPurchasesByCategoryDetail(LocalDateTime start, LocalDateTime end,
                                                                                Long establishmentId, List<Long> categoryIds) {
        final List<Long> finalCategoryIds = (categoryIds != null && categoryIds.isEmpty()) ? null : categoryIds;

        List<Purchase> purchases = purchaseRepository.findByCategoryFilters(
                establishmentId, start.toLocalDate(), end.toLocalDate(), finalCategoryIds);

        Map<Category, List<PurchaseItem>> itemsByCategory = purchases.stream()
                .filter(p -> !"VOIDED".equals(p.getStatus().name()))
                .flatMap(p -> p.getItems().stream())
                .filter(item -> finalCategoryIds == null || (item.getProduct().getCategory() != null
                        && finalCategoryIds.contains(item.getProduct().getCategory().getId())))
                .collect(Collectors.groupingBy(item -> {
                    Category cat = item.getProduct().getCategory();
                    return cat != null ? cat : PurchaseReportHelper.createUncategorized();
                }));

        return itemsByCategory.entrySet().stream()
                .map(entry -> {
                    Category cat = entry.getKey();
                    List<PurchaseItem> items = entry.getValue();

                    BigDecimal totalSpent = items.stream()
                            .map(PurchaseItem::getTotalCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalQty = items.stream()
                            .map(i -> BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 0))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<PurchasesByCategoryDetailReport.ProductDetail> products =
                            PurchaseReportHelper.buildPurchaseProductDetails(
                                    items.stream().collect(Collectors.groupingBy(item -> item.getProduct().getId())));

                    return new PurchasesByCategoryDetailReport(
                            cat.getId(), cat.getName(), totalSpent, totalQty,
                            (long) products.size(), products);
                })
                .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchasesBySupplierReport> getPurchasesBySupplier(LocalDateTime start, LocalDateTime end,
                                                                    Long establishmentId, List<Long> supplierIds) {
        final List<Long> finalSupplierIds = (supplierIds != null && supplierIds.isEmpty()) ? null : supplierIds;

        List<Purchase> purchases = purchaseRepository.findByFilters(
                establishmentId, start.toLocalDate(), end.toLocalDate(), finalSupplierIds, null, null, null);

        Map<Supplier, List<Purchase>> purchasesBySupplier = purchases.stream()
                .filter(p -> !"VOIDED".equals(p.getStatus().name()))
                .filter(p -> p.getSupplier() != null)
                .collect(Collectors.groupingBy(Purchase::getSupplier));

        return purchasesBySupplier.entrySet().stream()
                .map(entry -> {
                    Supplier supplier = entry.getKey();
                    List<Purchase> supplierPurchases = entry.getValue();

                    long purchaseCount = supplierPurchases.size();
                    BigDecimal totalSpent = supplierPurchases.stream()
                            .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    LocalDate lastPurchaseDate = supplierPurchases.stream()
                            .map(Purchase::getIssueDate).max(LocalDate::compareTo).orElse(null);

                    List<PurchasesBySupplierReport.ProductDetail> products = supplierPurchases.stream()
                            .flatMap(p -> p.getItems().stream())
                            .collect(Collectors.groupingBy(item -> item.getProduct().getId()))
                            .entrySet().stream()
                            .map(productEntry -> {
                                List<PurchaseItem> productItems = productEntry.getValue();
                                Product product = productItems.get(0).getProduct();
                                BigDecimal pQty = productItems.stream()
                                        .map(i -> BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 0))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                BigDecimal pSpent = productItems.stream()
                                        .map(PurchaseItem::getTotalCost)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                BigDecimal unitPrice = PurchaseReportHelper.calculateUnitPrice(pSpent, pQty);
                                String labName = product.getLaboratory() != null
                                        ? product.getLaboratory().getName() : "N/A";
                                return new PurchasesBySupplierReport.ProductDetail(
                                        product.getTradeName(), labName, pQty, unitPrice, pSpent);
                            })
                            .sorted((a, b) -> b.total().compareTo(a.total()))
                            .collect(Collectors.toList());

                    return new PurchasesBySupplierReport(
                            supplier.getId(), supplier.getName(), purchaseCount, totalSpent,
                            lastPurchaseDate,
                            supplier.getStatus() != null ? supplier.getStatus().name() : "N/A", products);
                })
                .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountsPayableSupplierReport> getAccountsPayableBySupplier(LocalDateTime start, LocalDateTime end,
                                                                              Long establishmentId, List<Long> supplierIds) {
        final List<Long> finalSupplierIds = (supplierIds != null && supplierIds.isEmpty()) ? null : supplierIds;

        List<AccountPayable> allPayables = accountPayableRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            jakarta.persistence.criteria.Join<AccountPayable, Purchase> purchaseJoin = root.join("purchase");
            predicates.add(cb.equal(purchaseJoin.get("establishment").get("id"), establishmentId));
            predicates.add(cb.between(purchaseJoin.get("issueDate"), start.toLocalDate(), end.toLocalDate()));
            if (finalSupplierIds != null && !finalSupplierIds.isEmpty()) {
                predicates.add(root.get("supplier").get("id").in(finalSupplierIds));
            }
            predicates.add(root.get("status").in(AccountPayable.PayableStatus.PENDING, AccountPayable.PayableStatus.PARTIAL));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });

        Map<Supplier, List<AccountPayable>> payablesBySupplier = allPayables.stream()
                .filter(ap -> ap.getSupplier() != null)
                .collect(Collectors.groupingBy(AccountPayable::getSupplier));

        return payablesBySupplier.entrySet().stream()
                .map(entry -> {
                    Supplier supplier = entry.getKey();
                    List<AccountPayable> supplierPayables = entry.getValue();

                    BigDecimal totalPending = supplierPayables.stream()
                            .map(ap -> ap.getPendingBalance() != null ? ap.getPendingBalance() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal overdueDebt = supplierPayables.stream()
                            .filter(ap -> ap.getDueDate() != null && ap.getDueDate().isBefore(LocalDate.now()))
                            .map(ap -> ap.getPendingBalance() != null ? ap.getPendingBalance() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<AccountsPayableSupplierReport.InvoiceDetail> invoices = supplierPayables.stream()
                            .map(ap -> {
                                Purchase p = ap.getPurchase();
                                String invoiceNumber = (p.getSeries() != null ? p.getSeries() + "-" : "")
                                        + (p.getNumber() != null ? p.getNumber() : "");
                                BigDecimal pendingAmt = ap.getPendingBalance() != null ? ap.getPendingBalance() : BigDecimal.ZERO;
                                String status = "Por vencer";
                                if (ap.getDueDate() != null) {
                                    if (ap.getDueDate().isBefore(LocalDate.now())) {
                                        status = "Vencido";
                                    } else if (ap.getDueDate().isBefore(LocalDate.now().plusDays(5))) {
                                        status = "Próximo vencer";
                                    }
                                }
                                return new AccountsPayableSupplierReport.InvoiceDetail(
                                        invoiceNumber, p.getIssueDate(), ap.getDueDate(), pendingAmt, status);
                            })
                            .sorted((a, b) -> {
                                if (a.dueDate() == null && b.dueDate() == null) return 0;
                                if (a.dueDate() == null) return 1;
                                if (b.dueDate() == null) return -1;
                                return a.dueDate().compareTo(b.dueDate());
                            })
                            .collect(Collectors.toList());

                    return new AccountsPayableSupplierReport(
                            supplier.getId(), supplier.getName(), totalPending,
                            supplierPayables.size(), overdueDebt, invoices);
                })
                .sorted((a, b) -> b.totalPending().compareTo(a.totalPending()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPriceHistoryReport> getProductPriceHistory(LocalDateTime start, LocalDateTime end,
                                                                    Long establishmentId, Long productId) {
        List<Purchase> purchases = purchaseRepository.findByProductFilters(
                establishmentId, start.toLocalDate(), end.toLocalDate(), productId);

        List<PurchaseItem> allItems = purchases.stream()
                .filter(p -> !"VOIDED".equals(p.getStatus().name()))
                .flatMap(p -> p.getItems().stream())
                .filter(item -> productId == null || item.getProduct().getId().equals(productId))
                .collect(Collectors.toList());

        Map<Product, List<PurchaseItem>> itemsByProduct = allItems.stream()
                .collect(Collectors.groupingBy(PurchaseItem::getProduct));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<PurchaseItem> productItems = entry.getValue();
                    productItems.sort(Comparator.comparing(i -> i.getPurchase().getIssueDate()));

                    List<ProductPriceHistoryReport.PriceHistoryDetail> history = new ArrayList<>();
                    BigDecimal lowestPrice = null;
                    BigDecimal highestPrice = null;
                    BigDecimal currentPrice = BigDecimal.ZERO;
                    BigDecimal previousPrice = null;

                    for (PurchaseItem item : productItems) {
                        BigDecimal qty = item.getQuantity() != null ? BigDecimal.valueOf(item.getQuantity()) : BigDecimal.ZERO;
                        BigDecimal totalCost = item.getTotalCost() != null ? item.getTotalCost() : BigDecimal.ZERO;
                        BigDecimal unitPrice = PurchaseReportHelper.calculateUnitPrice(totalCost, qty);

                        if (lowestPrice == null || unitPrice.compareTo(lowestPrice) < 0) lowestPrice = unitPrice;
                        if (highestPrice == null || unitPrice.compareTo(highestPrice) > 0) highestPrice = unitPrice;
                        currentPrice = unitPrice;

                        String variation = previousPrice != null
                                ? PurchaseReportHelper.calculateVariation(unitPrice, previousPrice)
                                : "—";

                        history.add(new ProductPriceHistoryReport.PriceHistoryDetail(
                                item.getPurchase().getIssueDate(), product.getTradeName(),
                                item.getPurchase().getSupplier() != null ? item.getPurchase().getSupplier().getName() : "N/A",
                                qty, unitPrice, variation));
                        previousPrice = unitPrice;
                    }

                    history.sort((a, b) -> b.date().compareTo(a.date()));

                    String totalVariation = "—";
                    if (productItems.size() > 1) {
                        BigDecimal firstQty = productItems.get(0).getQuantity() != null
                                ? BigDecimal.valueOf(productItems.get(0).getQuantity()) : BigDecimal.ZERO;
                        BigDecimal firstTotal = productItems.get(0).getTotalCost() != null
                                ? productItems.get(0).getTotalCost() : BigDecimal.ZERO;
                        BigDecimal firstPrice = PurchaseReportHelper.calculateUnitPrice(firstTotal, firstQty);
                        if (firstPrice.compareTo(BigDecimal.ZERO) > 0) {
                            totalVariation = PurchaseReportHelper.calculateVariation(currentPrice, firstPrice);
                        }
                    }

                    return new ProductPriceHistoryReport(
                            product.getId(), product.getTradeName(), currentPrice,
                            lowestPrice != null ? lowestPrice : BigDecimal.ZERO,
                            highestPrice != null ? highestPrice : BigDecimal.ZERO,
                            totalVariation, history);
                })
                .sorted(Comparator.comparing(ProductPriceHistoryReport::productName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchasesByBuyerReport> getPurchasesByBuyer(LocalDateTime start, LocalDateTime end,
                                                              Long establishmentId, List<Long> buyerIds) {
        final List<Long> finalBuyerIds = (buyerIds != null && buyerIds.isEmpty()) ? null : buyerIds;

        List<Purchase> purchases = purchaseRepository.findByFilters(
                establishmentId, start.toLocalDate(), end.toLocalDate(), null, null, finalBuyerIds, null);

        Map<User, List<Purchase>> purchasesByUser = purchases.stream()
                .filter(p -> p.getUser() != null)
                .collect(Collectors.groupingBy(Purchase::getUser));

        return purchasesByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<Purchase> userPurchases = entry.getValue();

                    int totalPurchases = userPurchases.size();
                    BigDecimal totalSpent = userPurchases.stream()
                            .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgPurchase = totalPurchases > 0
                            ? totalSpent.divide(BigDecimal.valueOf(totalPurchases), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    LocalDate lastPurchaseDate = userPurchases.stream()
                            .map(Purchase::getIssueDate).filter(Objects::nonNull)
                            .max(LocalDate::compareTo).orElse(null);

                    List<PurchasesByBuyerReport.PurchaseDetail> details = userPurchases.stream()
                            .map(p -> {
                                String document = (p.getSeries() != null ? p.getSeries() + "-" : "")
                                        + (p.getNumber() != null ? p.getNumber() : "");
                                String supplierName = p.getSupplier() != null ? p.getSupplier().getName() : "N/A";
                                return new PurchasesByBuyerReport.PurchaseDetail(
                                        p.getIssueDate(), user.getFullName(), supplierName, document,
                                        p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO);
                            })
                            .sorted((a, b) -> {
                                if (a.date() == null && b.date() == null) return 0;
                                if (a.date() == null) return 1;
                                if (b.date() == null) return -1;
                                return b.date().compareTo(a.date());
                            })
                            .collect(Collectors.toList());

                    return new PurchasesByBuyerReport(
                            user.getId(), user.getFullName(), totalPurchases, totalSpent,
                            lastPurchaseDate, avgPurchase, details);
                })
                .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
                .collect(Collectors.toList());
    }

    private List<PurchaseReport> mapPurchasesToReport(List<Purchase> purchases) {
        return purchases.stream()
                .map(p -> new PurchaseReport(
                        p.getId(),
                        p.getSupplier() != null ? p.getSupplier().getName() : "Proveedor General",
                        p.getDocumentType() != null ? p.getDocumentType().name() : "N/A",
                        (p.getSeries() != null ? p.getSeries() + "-" : "") + (p.getNumber() != null ? p.getNumber() : ""),
                        p.getIssueDate(), p.getArrivalDate(), p.getSubTotal(), p.getTax(), p.getTotal(),
                        p.getStatus() != null ? p.getStatus().name() : "N/A",
                        p.getItems() != null ? p.getItems().size() : 0))
                .collect(Collectors.toList());
    }

    private Category createUncategorized() {
        return PurchaseReportHelper.createUncategorized();
    }
}
