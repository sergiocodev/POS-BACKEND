package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.Sale;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesReportService {
    DailySalesReport getDailySales(LocalDateTime date, Long establishmentId);

    List<ProfitabilityReport> getProfitability(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SunatStatusReport> getSunatStatus(Long establishmentId);

    List<TopProductReport> getTopProducts(LocalDateTime start, LocalDateTime end, Long establishmentId, String sortBy, int limit);

    List<CategorySalesReport> getSalesByCategory(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<EmployeeSalesReport> getSalesByEmployee(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<HourlyHeatReport> getHourlyHeat(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<LowRotationReport> getLowRotation(int days, Long establishmentId);

    List<SalesReport> getSales(LocalDateTime start, LocalDateTime end, Long establishmentId);

    SalesSummaryReport getSalesSummary(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SalesReport> getSalesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
            Sale.SaleDocumentType documentType, String series, Long sellerId);

    List<SalesBySeriesReport> getSalesBySeries(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SalesByPaymentMethodReport> getSalesByPaymentMethod(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SalesByLaboratoryReport> getSalesByLaboratory(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SalesByEmployeeCategoryReport> getSalesByEmployeeCategory(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<SalesByCategoryDetailReport> getSalesByCategoryDetail(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<com.sergiocodev.app.dto.report.SalesByProductReport> getSalesByProduct(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<String> getAvailableSeries(Long establishmentId, Sale.SaleDocumentType documentType);

    List<SalesByCategoryDetailReport> getSalesByCategories(LocalDateTime start, LocalDateTime end, Long establishmentId,
            List<Long> categoryIds, Long sellerId);

    List<SalesByProductReport> getSalesByProductFilters(LocalDateTime start, LocalDateTime end, Long establishmentId,
            List<Long> productIds, List<Long> brandIds, List<Long> therapeuticActionIds, Long sellerId);

    List<SalesBySeriesReport> getSalesBySeriesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
            List<String> seriesList);

    // Seller-specific
    List<SalesReport> getSalesBySeller(LocalDateTime start, LocalDateTime end, Long establishmentId, List<Long> sellerIds);

    List<SalesByCategoryDetailReport> getSalesBySellerCategories(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> sellerIds, List<Long> categoryIds);

    List<SalesByProductReport> getSalesBySellerProducts(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> sellerIds, List<Long> productIds);

    String getSellerNames(List<Long> sellerIds);

    // Customer-specific
    List<SalesByCustomerReport> getSalesByCustomer(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> customerIds);

    List<SalesReport> getSalesByCustomerDetail(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> customerIds);
}
