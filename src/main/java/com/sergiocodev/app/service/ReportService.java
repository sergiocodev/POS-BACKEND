package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.Sale;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    DailySalesReport getDailySales(LocalDate date, Long establishmentId);

    List<ProfitabilityReport> getProfitability(LocalDate start, LocalDate end, Long establishmentId);

    List<SunatStatusReport> getSunatStatus(Long establishmentId);

    List<TopProductReport> getTopProducts(LocalDate start, LocalDate end, Long establishmentId, String sortBy,
            int limit);

    List<CategorySalesReport> getSalesByCategory(LocalDate start, LocalDate end, Long establishmentId);

    List<EmployeeSalesReport> getSalesByEmployee(LocalDate start, LocalDate end, Long establishmentId);

    List<HourlyHeatReport> getHourlyHeat(LocalDate start, LocalDate end, Long establishmentId);

    List<LowRotationReport> getLowRotation(int days, Long establishmentId);

    List<PurchaseReport> getPurchases(LocalDate start, LocalDate end, Long establishmentId);

    List<SalesReport> getSales(LocalDate start, LocalDate end, Long establishmentId);

    SalesSummaryReport getSalesSummary(LocalDate start, LocalDate end, Long establishmentId);

    // ── Nuevos reportes para tabs de farmacia ──

    /** Tab Comprobantes: ventas filtradas por tipo de comprobante y serie */
    List<SalesReport> getSalesFiltered(LocalDate start, LocalDate end, Long establishmentId,
            Sale.SaleDocumentType documentType, String series);

    /** Tab Series: ventas agrupadas por serie */
    List<SalesBySeriesReport> getSalesBySeries(LocalDate start, LocalDate end, Long establishmentId);

    /** Tab Categorías: ventas agrupadas por método de pago */
    List<SalesByPaymentMethodReport> getSalesByPaymentMethod(LocalDate start, LocalDate end, Long establishmentId);

    /** Tab Categorías: ventas agrupadas por laboratorio */
    List<SalesByLaboratoryReport> getSalesByLaboratory(LocalDate start, LocalDate end, Long establishmentId);

    /** Tab Vendedor: ventas cruzadas por vendedor y categoría */
    List<SalesByEmployeeCategoryReport> getSalesByEmployeeCategory(LocalDate start, LocalDate end,
            Long establishmentId);

    /** Tab Grupos: ventas por categoría con detalle de productos */
    List<SalesByCategoryDetailReport> getSalesByCategoryDetail(LocalDate start, LocalDate end, Long establishmentId);

    /** Tab Productos: ventas agrupadas por producto */
    List<com.sergiocodev.app.dto.report.SalesByProductReport> getSalesByProduct(LocalDate start, LocalDate end, Long establishmentId);

    /** Obtener series disponibles para un tipo de documento y establecimiento */
    List<String> getAvailableSeries(Long establishmentId, Sale.SaleDocumentType documentType);
}

