package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.EmployeeRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;



/**
 * Métodos auxiliares reutilizables para los reportes de ventas.
 * Reduce la duplicación de patrones comunes como mapeo Sale→SalesReport,
 * agrupación por categorías, productos y series.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SalesReportHelper {

    // ────────────── Sale → SalesReport ──────────────

    /** Convierte una Sale a SalesReport usando el nombre de empleado proporcionado. */
    public static SalesReport toSalesReport(Sale s, String employeeName) {
        return new SalesReport(
                s.getId(),
                s.getCustomer() != null ? s.getCustomer().getName() : "Cliente General",
                employeeName,
                s.getDocumentType().name(),
                s.getSeries() + "-" + s.getNumber(),
                s.getDate(), s.getSubTotal(), s.getTax(), s.getTotal(),
                s.getStatus().name(),
                s.getSunatStatus() != null ? s.getSunatStatus().name() : null,
                s.isVoided());
    }

    /** Convierte una Sale a SalesReport usando el nombre completo del usuario. */
    public static SalesReport toSalesReport(Sale s) {
        return toSalesReport(s, s.getUser() != null ? s.getUser().getFullName() : "N/A");
    }

    // ────────────── Employee ID → User ID ──────────────

    /**
     * Convierte una lista de IDs de empleados a IDs de usuarios asociados.
     * Retorna null si no se debe filtrar (lista vacía o contiene 0L = "Todos").
     */
    public static List<Long> mapEmployeeIdsToUserIds(List<Long> sellerIds,
                                                      EmployeeRepository employeeRepository) {
        if (sellerIds == null || sellerIds.isEmpty() || sellerIds.contains(0L)) {
            return null;
        }
        List<Long> userIds = employeeRepository.findAllById(sellerIds).stream()
                .filter(e -> e.getUser() != null)
                .map(e -> e.getUser().getId())
                .collect(Collectors.toList());
        return userIds.isEmpty() ? null : userIds;
    }

    // ────────────── Customer filter ──────────────

    /** Filtra ventas por IDs de cliente. Si customerIds contiene 0L ("Todos"), no filtra. */
    public static List<Sale> filterByCustomerIds(List<Sale> sales, List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty() || customerIds.contains(0L)) {
            return sales;
        }
        return sales.stream()
                .filter(s -> s.getCustomer() != null && customerIds.contains(s.getCustomer().getId()))
                .collect(Collectors.toList());
    }

    // ─────────────️ Category grouping → SalesByCategoryDetailReport ─────────────

    /**
     * Construye reportes de categorías con detalle de productos a partir de items agrupados por categoría.
     *
     * @param itemsByCategory Items de venta ya agrupados por categoría
     * @return Lista de SalesByCategoryDetailReport ordenada por revenue descendente
     */
    public static List<SalesByCategoryDetailReport> buildCategoryDetailReports(
            Map<Category, List<SaleItem>> itemsByCategory) {

        return itemsByCategory.entrySet().stream()
                .map(entry -> {
                    Category cat = entry.getKey();
                    List<SaleItem> items = entry.getValue();

                    BigDecimal totalRevenue = items.stream().map(SaleItem::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalQty = items.stream().map(SaleItem::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<SalesByCategoryDetailReport.ProductDetail> products = items.stream()
                            .collect(Collectors.groupingBy(item -> item.getProduct().getId()))
                            .entrySet().stream()
                            .map(productEntry -> {
                                List<SaleItem> productItems = productEntry.getValue();
                                Product product = productItems.get(0).getProduct();
                                BigDecimal pQty = productItems.stream().map(SaleItem::getQuantity)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                BigDecimal pRevenue = productItems.stream().map(SaleItem::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                String labName = product.getLaboratory() != null
                                        ? product.getLaboratory().getName() : "N/A";
                                return new SalesByCategoryDetailReport.ProductDetail(
                                        product.getId(), product.getTradeName(), labName, pQty, pRevenue);
                            })
                            .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                            .collect(Collectors.toList());

                    return new SalesByCategoryDetailReport(cat.getId(), cat.getName(), totalRevenue, totalQty,
                            (long) products.size(), products);
                })
                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                .collect(Collectors.toList());
    }

    // ────────────── Product grouping → SalesByProductReport ─────────────

    /**
     * Construye reportes de productos a partir de items agrupados por producto.
     * Usa el extractor de nombre de laboratorio proporcionado (puede venir de Laboratory o Brand).
     */
    public static List<SalesByProductReport> buildProductReports(
            Map<Product, List<SaleItem>> itemsByProduct,
            Function<Product, String> labNameExtractor) {

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<SaleItem> items = entry.getValue();
                    BigDecimal totalRevenue = items.stream().map(SaleItem::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Long quantitySold = items.stream()
                            .map(i -> i.getQuantity().longValue()).reduce(0L, Long::sum);
                    String catName = product.getCategory() != null
                            ? product.getCategory().getName() : "Sin Categoría";
                    String labName = labNameExtractor.apply(product);
                    String therapeuticAction = product.getTherapeuticActions().stream()
                            .map(TherapeuticAction::getName).collect(Collectors.joining(", "));
                    return new SalesByProductReport(product.getId(), product.getTradeName(), catName, labName,
                            therapeuticAction, quantitySold, totalRevenue);
                })
                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                .collect(Collectors.toList());
    }

    /** Extractor de nombre de laboratorio desde Laboratory. */
    public static String labFromLaboratory(Product p) {
        return p.getLaboratory() != null ? p.getLaboratory().getName() : "Sin Laboratorio";
    }

    /** Extractor de nombre de laboratorio desde Brand. */
    public static String labFromBrand(Product p) {
        return p.getBrand() != null ? p.getBrand().getName() : "Sin Marca";
    }

    // ────────────── Series grouping → SalesBySeriesReport ─────────────

    /**
     * Agrupa ventas por tipo de documento + serie y construye reportes de series.
     *
     * @param sales  Lista de ventas (opcionalmente pre-filtrada)
     * @return Lista de SalesBySeriesReport ordenada por monto descendente
     */
    public static List<SalesBySeriesReport> buildSeriesReports(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(s -> s.getDocumentType().name() + "|" + s.getSeries()))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 2);
                    String docType = parts[0];
                    String seriesVal = parts.length > 1 ? parts[1] : "";
                    List<Sale> group = entry.getValue();

                    List<Sale> valid = group.stream().filter(s -> !s.isVoided()).collect(Collectors.toList());
                    List<Sale> voided = group.stream().filter(Sale::isVoided).collect(Collectors.toList());

                    BigDecimal totalSubTotal = valid.stream().map(Sale::getSubTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalTax = valid.stream().map(Sale::getTax)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalAmount = valid.stream().map(Sale::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal voidedAmount = voided.stream().map(Sale::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Long initialNum = group.stream().map(Sale::getNumber).map(Long::valueOf)
                            .min(Long::compare).orElse(0L);
                    Long actualNum = group.stream().map(Sale::getNumber).map(Long::valueOf)
                            .max(Long::compare).orElse(0L);

                    return new SalesBySeriesReport(docType, seriesVal, initialNum, actualNum,
                            (long) valid.size(), totalSubTotal, totalTax, totalAmount,
                            (long) voided.size(), voidedAmount);
                })
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .collect(Collectors.toList());
    }

    // ────────────── Fallback entities ─────────────

    public static Category createUncategorized() {
        Category c = new Category();
        c.setId(0L);
        c.setName("Sin Categoría");
        return c;
    }

    public static Laboratory createUnknownLaboratory() {
        Laboratory l = new Laboratory();
        l.setId(0L);
        l.setName("Sin Laboratorio");
        return l;
    }

    public static Customer createUnknownCustomer() {
        Customer c = new Customer();
        c.setId(0L);
        c.setName("Cliente General");
        return c;
    }

    public static Long toLong(Object[] row, int index) {
        if (row == null || row.length <= index || row[index] == null) {
            return 0L;
        }
        if (row[index] instanceof Number) {
            return ((Number) row[index]).longValue();
        }
        return Long.parseLong(row[index].toString());
    }

    public static BigDecimal toBigDecimal(Object[] row, int index) {
        if (row == null || row.length <= index || row[index] == null) {
            return BigDecimal.ZERO;
        }
        if (row[index] instanceof BigDecimal) {
            return (BigDecimal) row[index];
        }
        if (row[index] instanceof Number) {
            return BigDecimal.valueOf(((Number) row[index]).doubleValue());
        }
        return new BigDecimal(row[index].toString());
    }
}
