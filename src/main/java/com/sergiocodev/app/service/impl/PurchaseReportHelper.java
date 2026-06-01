package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport;
import com.sergiocodev.app.model.Category;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.PurchaseItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Métodos auxiliares reutilizables para los reportes de compras.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PurchaseReportHelper {

    /**
     * Normaliza una lista de IDs: si es null o vacía retorna null (sin filtro).
     */
    public static <T> List<T> toNullableList(List<T> ids) {
        return (ids != null && ids.isEmpty()) ? null : ids;
    }

    /**
     * Construye los ProductDetail para un reporte de compras por categoría
     * a partir de items ya agrupados por ID de producto.
     */
    public static List<PurchasesByCategoryDetailReport.ProductDetail> buildPurchaseProductDetails(
            Map<Long, List<PurchaseItem>> itemsByProductId) {

        return itemsByProductId.entrySet().stream()
                .map(productEntry -> {
                    List<PurchaseItem> productItems = productEntry.getValue();
                    Product product = productItems.get(0).getProduct();
                    BigDecimal pQty = productItems.stream()
                            .map(i -> BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 0))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pSpent = productItems.stream()
                            .map(PurchaseItem::getTotalCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String labName = product.getLaboratory() != null
                            ? product.getLaboratory().getName() : "N/A";
                    return new PurchasesByCategoryDetailReport.ProductDetail(
                            product.getId(), product.getTradeName(), labName, pQty, pSpent);
                })
                .sorted((a, b) -> b.spent().compareTo(a.spent()))
                .collect(Collectors.toList());
    }

    /**
     * Calcula el precio unitario promedio a partir de cantidad y total gastado.
     */
    public static BigDecimal calculateUnitPrice(BigDecimal totalSpent, BigDecimal quantity) {
        return quantity.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(quantity, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    /**
     * Calcula la variación porcentual entre dos precios.
     */
    public static String calculateVariation(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return "—";
        }
        BigDecimal diff = current.subtract(previous);
        BigDecimal percent = diff.divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        String sign = percent.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return sign + percent.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    public static Category createUncategorized() {
        Category c = new Category();
        c.setId(0L);
        c.setName("Sin Categoría");
        return c;
    }
}
