package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.Sale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    DailySalesReport getDailySales(LocalDateTime date, Long establishmentId);

    List<ProfitabilityReport> getProfitability(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SunatStatusReport> getSunatStatus(Long establishmentId);

    List<TopProductReport> getTopProducts(LocalDateTime start, LocalDateTime end, Long establishmentId, String sortBy,
            int limit);

    List<CategorySalesReport> getSalesByCategory(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<EmployeeSalesReport> getSalesByEmployee(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<HourlyHeatReport> getHourlyHeat(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<LowRotationReport> getLowRotation(int days, Long establishmentId);

    List<PurchaseReport> getPurchases(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SalesReport> getSales(LocalDateTime start, LocalDateTime end, Long establishmentId);

    SalesSummaryReport getSalesSummary(LocalDateTime start, LocalDateTime end, Long establishmentId);

    // ── Nuevos reportes para tabs de farmacia ──

    /** Tab Comprobantes: ventas filtradas por tipo de comprobante y serie */
    List<SalesReport> getSalesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
            Sale.SaleDocumentType documentType, String series, Long sellerId);

    /** Tab Series: ventas agrupadas por serie */
    List<SalesBySeriesReport> getSalesBySeries(LocalDateTime start, LocalDateTime end, Long establishmentId);

    /** Tab Categorías: ventas agrupadas por método de pago */
    List<SalesByPaymentMethodReport> getSalesByPaymentMethod(LocalDateTime start, LocalDateTime end, Long establishmentId);

    /** Tab Categorías: ventas agrupadas por laboratorio */
    List<SalesByLaboratoryReport> getSalesByLaboratory(LocalDateTime start, LocalDateTime end, Long establishmentId);

    /** Tab Vendedor: ventas cruzadas por vendedor y categoría */
    List<SalesByEmployeeCategoryReport> getSalesByEmployeeCategory(LocalDateTime start, LocalDateTime end,
            Long establishmentId);

    /** Tab Grupos: ventas por categoría con detalle de productos */
    List<SalesByCategoryDetailReport> getSalesByCategoryDetail(LocalDateTime start, LocalDateTime end, Long establishmentId);

    /** Tab Productos: ventas agrupadas por producto */
    List<com.sergiocodev.app.dto.report.SalesByProductReport> getSalesByProduct(LocalDateTime start, LocalDateTime end, Long establishmentId);

    /** Obtener series disponibles para un tipo de documento y establecimiento */
    List<String> getAvailableSeries(Long establishmentId, Sale.SaleDocumentType documentType);

    /** Reporte PDF: ventas filtradas por categorías */
    List<SalesByCategoryDetailReport> getSalesByCategories(LocalDateTime start, LocalDateTime end, Long establishmentId,
            List<Long> categoryIds, Long sellerId);

    /** Reporte PDF: ventas filtradas por producto, marca y acción terapéutica */
    List<SalesByProductReport> getSalesByProductFilters(LocalDateTime start, LocalDateTime end, Long establishmentId,
            List<Long> productIds, List<Long> brandIds, List<Long> therapeuticActionIds, Long sellerId);

    /** Reporte PDF: ventas agrupadas por serie con filtros */
    List<SalesBySeriesReport> getSalesBySeriesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
            List<String> seriesList);

    // ── Reportes por Vendedor ──

    /** PDF Vendedor: todas las ventas de un vendedor */
    List<SalesReport> getSalesBySeller(LocalDateTime start, LocalDateTime end, Long establishmentId, List<Long> sellerIds);

    /** PDF Vendedor: ventas de un vendedor agrupadas por categoría */
    List<SalesByCategoryDetailReport> getSalesBySellerCategories(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> sellerIds, List<Long> categoryIds);

    /** PDF Vendedor: ventas de un vendedor agrupadas por producto */
    List<SalesByProductReport> getSalesBySellerProducts(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> sellerIds, List<Long> productIds);

    /** Obtener el nombre completo de uno o más vendedores */
    String getSellerNames(List<Long> sellerIds);

    // ── Reportes por Cliente ──

    /** PDF Cliente: ventas agrupadas por cliente */
    List<SalesByCustomerReport> getSalesByCustomer(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> customerIds);

    /** PDF Cliente: detalle de ventas de un cliente */
    List<SalesReport> getSalesByCustomerDetail(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> customerIds);
}

