package com.sergiocodev.app.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sergiocodev.app.dto.company.CompanyResponse;
import com.sergiocodev.app.dto.report.SalesReport;
import com.sergiocodev.app.dto.report.SalesByCategoryDetailReport;
import com.sergiocodev.app.dto.report.SalesByCustomerReport;
import com.sergiocodev.app.dto.report.SalesByProductReport;
import com.sergiocodev.app.dto.report.SalesBySeriesReport;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportPdfGenerator {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 51, 153));
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

    public static byte[] generateComprobantesReport(List<SalesReport> sales, CompanyResponse company, String startDate,
            String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE POR COMPROBANTE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        Map<String, List<SalesReport>> salesByType = sales.stream()
                .filter(s -> !s.isVoided())
                .collect(Collectors.groupingBy(s -> formatDocString(s.documentType())));

        addSectionTitle(document, "RESUMEN POR COMPROBANTES");

        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(5);
        summaryTable.setSpacingAfter(20);

        addTableHeader(summaryTable, new String[] { "COMPROBANTE", "CANTIDAD", "TOTAL INGRESOS" });

        long totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<String, List<SalesReport>> entry : salesByType.entrySet()) {
            String docType = entry.getKey();
            List<SalesReport> typeSales = entry.getValue();

            long qty = typeSales.size();
            BigDecimal amount = typeSales.stream()
                    .map(SalesReport::total)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            summaryTable.addCell(createCell(docType, NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(String.valueOf(qty), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(amount.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));

            totalQty += qty;
            totalAmount = totalAmount.add(amount);
        }

        PdfPCell totalLabel = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setColspan(1);
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(totalQty), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(totalAmount.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        addSectionTitle(document, "VENTAS CONFIRMADAS POR COMPROBANTE");

        for (Map.Entry<String, List<SalesReport>> entry : salesByType.entrySet()) {
            String docType = entry.getKey();
            List<SalesReport> typeSales = entry.getValue();

            if (typeSales.isEmpty())
                continue;

            PdfPTable detailsTable = new PdfPTable(new float[] { 2.2f, 2f, 4f, 1.5f });
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10);
            detailsTable.setSpacingAfter(15);

            PdfPCell typeCell = new PdfPCell(new Phrase(docType, HEADER_FONT));
            typeCell.setColspan(4);
            typeCell.setBackgroundColor(new Color(33, 37, 41));
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            typeCell.setPadding(5);
            detailsTable.addCell(typeCell);

            addTableHeader(detailsTable, new String[] { "FECHA", "DOCUMENTO", "CLIENTE", "SUBTOTAL" },
                    new Color(240, 240, 240), BOLD_FONT);

            for (SalesReport sale : typeSales) {
                detailsTable.addCell(createCell(sale.date().format(DT_FMT), NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(sale.documentNumber(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable
                        .addCell(createCell(sale.customerName() != null ? sale.customerName() : "PUBLICO EN GENERAL",
                                NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(sale.total().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
            }

            // Subtotal row for the voucher type
            BigDecimal typeTotal = typeSales.stream()
                    .map(SalesReport::total)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPCell subtotalLabel = createCell("TOTAL", BOLD_FONT, Element.ALIGN_CENTER);
            subtotalLabel.setColspan(3);
            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
            detailsTable.addCell(subtotalLabel);
            detailsTable.addCell(createCell(typeTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

            document.add(detailsTable);
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generateCategoriesReport(List<SalesByCategoryDetailReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE VENTAS POR CATEGORÍA", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        BigDecimal grandTotal = reports.stream().map(SalesByCategoryDetailReport::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfPTable summaryTable = new PdfPTable(new float[] { 4f, 2f, 2f });
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        // Main Table Header (Dark)
        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN POR CATEGORÍAS", HEADER_FONT));
        summaryHeaderCell.setColspan(3);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "CATEGORÍA", "CANTIDAD PRODUCTOS", "TOTAL INGRESOS" });

        for (SalesByCategoryDetailReport report : reports) {
            summaryTable.addCell(createCell(report.categoryName(), NORMAL_FONT, Element.ALIGN_LEFT));
            summaryTable.addCell(createCell(String.valueOf(report.productCount()), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.totalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        long totalProductCount = reports.stream().mapToLong(SalesByCategoryDetailReport::productCount).sum();

        PdfPCell totalLabel = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setColspan(1);
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(totalProductCount), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(grandTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        for (SalesByCategoryDetailReport report : reports) {
            PdfPTable detailTable = new PdfPTable(new float[] { 5f, 2f, 2f, 2f });
            detailTable.setWidthPercentage(100);
            detailTable.setSpacingBefore(10);
            detailTable.setSpacingAfter(15);

            // Group header row (Full width, dark background)
            PdfPCell categoryHeaderCell = new PdfPCell(new Phrase(report.categoryName().toUpperCase(), HEADER_FONT));
            categoryHeaderCell.setColspan(4);
            categoryHeaderCell.setBackgroundColor(new Color(33, 37, 41)); // Dark grey
            categoryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            categoryHeaderCell.setPadding(6);
            detailTable.addCell(categoryHeaderCell);

            addTableHeader(detailTable, new String[] { "PRODUCTO", "LABORATORIO", "CANTIDAD VENDIDA", "SUBTOTAL" },
                    new Color(240, 240, 240), BOLD_FONT);

            for (SalesByCategoryDetailReport.ProductDetail detail : report.products()) {
                detailTable.addCell(createCell(detail.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(detail.laboratoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable
                        .addCell(createCell(detail.quantitySold().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(detail.revenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
            }

            // Subtotal row for the category
            BigDecimal totalQty = report.products().stream()
                    .map(SalesByCategoryDetailReport.ProductDetail::quantitySold)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPCell subtotalLabel = createCell("TOTAL", BOLD_FONT, Element.ALIGN_CENTER);
            subtotalLabel.setColspan(2);
            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
            detailTable.addCell(subtotalLabel);
            detailTable.addCell(createCell(totalQty.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(report.totalRevenue().toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

            document.add(detailTable);
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generateProductsReport(List<SalesByProductReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE POR ACCIÓN TERAPÉUTICA, MARCA Y PRODUCTO", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        // Group by Therapeutic Action
        Map<String, List<SalesByProductReport>> groupedByAction = reports.stream()
                .collect(Collectors.groupingBy(
                        r -> (r.getTherapeuticAction() == null || r.getTherapeuticAction().isEmpty())
                                ? "SIN ACCIÓN TERAPÉUTICA"
                                : r.getTherapeuticAction(),
                        java.util.TreeMap::new,
                        Collectors.toList()));

        BigDecimal grandTotalRevenue = BigDecimal.ZERO;
        long grandTotalQty = 0;

        // Calculate totals first for the summary
        for (List<SalesByProductReport> actionProducts : groupedByAction.values()) {
            grandTotalQty += actionProducts.stream().mapToLong(SalesByProductReport::getQuantitySold).sum();
            grandTotalRevenue = grandTotalRevenue.add(actionProducts.stream()
                    .map(SalesByProductReport::getTotalRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        // Summary Table (Now at the beginning)
        PdfPTable summaryTable = new PdfPTable(new float[] { 8f, 2f, 2.5f });
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        // Main Table Header (Dark)
        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN GENERAL", HEADER_FONT));
        summaryHeaderCell.setColspan(3);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "ACCIÓN TERAPÉUTICA", "CANTIDAD", "TOTAL INGRESOS" },
                new Color(230, 230, 230), BOLD_FONT);

        for (Map.Entry<String, List<SalesByProductReport>> entry : groupedByAction.entrySet()) {
            String actionName = entry.getKey();
            long qty = entry.getValue().stream().mapToLong(SalesByProductReport::getQuantitySold).sum();
            BigDecimal total = entry.getValue().stream()
                    .map(SalesByProductReport::getTotalRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            summaryTable.addCell(createCell(actionName, NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(String.valueOf(qty), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(total.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        PdfPCell totalLabelCell = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabelCell.setPadding(8);
        totalLabelCell.setBackgroundColor(new Color(245, 245, 245));
        summaryTable.addCell(totalLabelCell);
        summaryTable.addCell(createCell(String.valueOf(grandTotalQty), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(grandTotalRevenue.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        // Detailed Sections
        for (Map.Entry<String, List<SalesByProductReport>> actionEntry : groupedByAction.entrySet()) {
            PdfPTable table = new PdfPTable(new float[] { 5f, 3f, 2f, 2.5f });
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(15);

            // Group header row (Full width, dark background)
            PdfPCell groupHeaderCell = new PdfPCell(new Phrase(actionEntry.getKey().toUpperCase(), HEADER_FONT));
            groupHeaderCell.setColspan(4);
            groupHeaderCell.setBackgroundColor(new Color(33, 37, 41)); // Dark grey
            groupHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            groupHeaderCell.setPadding(6);
            table.addCell(groupHeaderCell);

            addTableHeader(table, new String[] { "PRODUCTO", "MARCA", "CANTIDAD", "SUBTOTAL" },
                    new Color(240, 240, 240), BOLD_FONT);

            BigDecimal actionTotalRevenue = BigDecimal.ZERO;
            long actionTotalQty = 0;

            // Sort products by lab and name within action
            List<SalesByProductReport> sortedProducts = actionEntry.getValue().stream()
                    .sorted(java.util.Comparator.comparing(SalesByProductReport::getLaboratoryName)
                            .thenComparing(SalesByProductReport::getProductName))
                    .collect(Collectors.toList());

            for (SalesByProductReport report : sortedProducts) {
                table.addCell(createCell(report.getProductName(), NORMAL_FONT, Element.ALIGN_CENTER));
                table.addCell(createCell(report.getLaboratoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
                table.addCell(createCell(String.valueOf(report.getQuantitySold()), NORMAL_FONT, Element.ALIGN_CENTER));
                table.addCell(createCell(report.getTotalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));

                actionTotalRevenue = actionTotalRevenue.add(report.getTotalRevenue());
                actionTotalQty += report.getQuantitySold();
            }

            // Subtotal row for the action
            PdfPCell subtotalLabel = createCell("TOTAL", BOLD_FONT, Element.ALIGN_CENTER);
            subtotalLabel.setColspan(2);
            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
            table.addCell(subtotalLabel);
            table.addCell(createCell(String.valueOf(actionTotalQty), BOLD_FONT, Element.ALIGN_CENTER));
            table.addCell(createCell(actionTotalRevenue.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generateSeriesReport(List<SalesBySeriesReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE POR ESTABLECIMIENTO Y SERIE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        PdfPTable table = new PdfPTable(new float[] { 2f, 2f, 2f, 4f, 2f, 2f });
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        // Main Table Header (Dark)
        PdfPCell mainHeaderCell = new PdfPCell(new Phrase("RESUMEN POR SERIE CONFIRMADAS", HEADER_FONT));
        mainHeaderCell.setColspan(6);
        mainHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        mainHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainHeaderCell.setPadding(6);
        table.addCell(mainHeaderCell);

        addTableHeader(table,
                new String[] { "SERIE", "NUM. INICIAL", "NUM. ACTUAL", "COMPROBANTE", "CANT. VEND.", "TOTAL VEND." });

        BigDecimal grandTotal = BigDecimal.ZERO;
        long totalQty = 0;

        // Group and sort reports
        java.util.Map<String, List<SalesBySeriesReport>> groupedReports = reports.stream()
                .sorted(java.util.Comparator.comparing(SalesBySeriesReport::documentType)
                        .thenComparing(SalesBySeriesReport::series))
                .collect(java.util.stream.Collectors.groupingBy(SalesBySeriesReport::documentType,
                        java.util.LinkedHashMap::new, java.util.stream.Collectors.toList()));

        for (java.util.Map.Entry<String, List<SalesBySeriesReport>> entry : groupedReports.entrySet()) {
            String docTypeLabel = formatDocString(entry.getKey());
            List<SalesBySeriesReport> group = entry.getValue();

            boolean isFirstInGroup = true;

            for (SalesBySeriesReport report : group) {
                // Column 1: SERIE
                table.addCell(createCell(report.series(), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 2: NUM. INICIAL
                table.addCell(createCell(String.valueOf(report.initialNumber()), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 3: NUM. ACTUAL
                table.addCell(createCell(String.valueOf(report.currentNumber()), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 4: COMPROBANTE (merged and centered)
                if (isFirstInGroup) {
                    PdfPCell docCell = createCell(docTypeLabel, NORMAL_FONT, Element.ALIGN_CENTER);
                    docCell.setRowspan(group.size());
                    docCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    docCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(docCell);
                    isFirstInGroup = false;
                }

                // Column 5: CANTIDAD
                table.addCell(createCell(String.valueOf(report.transactionCount()), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 6: TOTAL
                table.addCell(createCell(report.totalAmount().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));

                grandTotal = grandTotal.add(report.totalAmount());
                totalQty += report.transactionCount();
            }
        }

        PdfPCell totalLabel = createCell("TOTALES CONFIRMADOS", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setColspan(4);
        table.addCell(totalLabel);
        table.addCell(createCell(String.valueOf(totalQty), BOLD_FONT, Element.ALIGN_CENTER));
        table.addCell(createCell(grandTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(table);

        // --- SECOND TABLE: VOIDED ---
        PdfPTable voidedTable = new PdfPTable(new float[] { 2f, 2f, 2f, 4f, 2f, 2f });
        voidedTable.setWidthPercentage(100);
        voidedTable.setSpacingBefore(10);

        // Voided Table Header (Dark)
        PdfPCell voidedHeaderCell = new PdfPCell(new Phrase("RESUMEN POR SERIE ANULADAS", HEADER_FONT));
        voidedHeaderCell.setColspan(6);
        voidedHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        voidedHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        voidedHeaderCell.setPadding(6);
        voidedTable.addCell(voidedHeaderCell);

        addTableHeader(voidedTable,
                new String[] { "SERIE", "NUM. INICIAL", "NUM. ACTUAL", "COMPROBANTE", "CANT. ANUL.", "IMP. ANUL." });

        BigDecimal grandVoidedAmount = BigDecimal.ZERO;
        long totalVoided = 0;

        for (java.util.Map.Entry<String, List<SalesBySeriesReport>> entry : groupedReports.entrySet()) {
            String docTypeLabel = formatDocString(entry.getKey());
            List<SalesBySeriesReport> group = entry.getValue();

            boolean isFirstInGroup = true;

            for (SalesBySeriesReport report : group) {
                // Column 1: SERIE
                voidedTable.addCell(createCell(report.series(), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 2: NUM. INICIAL
                voidedTable
                        .addCell(createCell(String.valueOf(report.initialNumber()), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 3: NUM. ACTUAL
                voidedTable
                        .addCell(createCell(String.valueOf(report.currentNumber()), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 4: COMPROBANTE (merged and centered)
                if (isFirstInGroup) {
                    PdfPCell docCell = createCell(docTypeLabel, NORMAL_FONT, Element.ALIGN_CENTER);
                    docCell.setRowspan(group.size());
                    docCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    docCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    voidedTable.addCell(docCell);
                    isFirstInGroup = false;
                }

                // Column 5: CANT. ANUL.
                voidedTable
                        .addCell(createCell(String.valueOf(report.voidedCount()), NORMAL_FONT, Element.ALIGN_CENTER));

                // Column 6: IMP. ANUL.
                voidedTable
                        .addCell(createCell(report.voidedAmount().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));

                grandVoidedAmount = grandVoidedAmount.add(report.voidedAmount());
                totalVoided += report.voidedCount();
            }
        }

        PdfPCell voidedTotalLabel = createCell("TOTALES ANULADOS", BOLD_FONT, Element.ALIGN_CENTER);
        voidedTotalLabel.setColspan(4);
        voidedTable.addCell(voidedTotalLabel);
        voidedTable.addCell(createCell(String.valueOf(totalVoided), BOLD_FONT, Element.ALIGN_CENTER));
        voidedTable.addCell(createCell(grandVoidedAmount.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
        document.add(voidedTable);

        document.close();
        return baos.toByteArray();
    }

    // ── SELLER-SPECIFIC REPORTS ──

    /**
     * PDF: Reporte de ventas por vendedor (comprobantes del vendedor).
     */
    public static byte[] generateSellerReport(List<SalesReport> sales, CompanyResponse company,
            String startDate, String endDate, String sellerName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE VENTAS POR VENDEDOR", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addSellerSubtitle(document, sellerName);
        addDateHeader(document, startDate, endDate);

        // Summary calculations
        List<SalesReport> confirmed = sales.stream().filter(s -> !s.isVoided()).collect(Collectors.toList());
        List<SalesReport> voided = sales.stream().filter(SalesReport::isVoided).collect(Collectors.toList());

        BigDecimal totalConfirmed = confirmed.stream().map(SalesReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVoided = voided.stream().map(SalesReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Summary table grouped by seller
        Map<String, List<SalesReport>> salesBySeller = confirmed.stream()
                .collect(Collectors.groupingBy(s -> s.employeeName() != null ? s.employeeName() : "N/A"));

        PdfPTable summaryTable = new PdfPTable(new float[] { 4f, 2f, 2f });
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN DE VENTAS POR VENDEDOR", HEADER_FONT));
        summaryHeaderCell.setColspan(3);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "VENDEDOR", "CANTIDAD", "MONTO" });

        salesBySeller.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String seller = entry.getKey();
                    List<SalesReport> sellerSales = entry.getValue();
                    BigDecimal sellerTotal = sellerSales.stream().map(SalesReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);

                    summaryTable.addCell(createCell(seller, NORMAL_FONT, Element.ALIGN_CENTER));
                    summaryTable.addCell(createCell(String.valueOf(sellerSales.size()), NORMAL_FONT, Element.ALIGN_CENTER));
                    summaryTable.addCell(createCell(sellerTotal.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                });

        PdfPCell totalLabel = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setBackgroundColor(new Color(245, 245, 245));
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(confirmed.size()), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(totalConfirmed.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        // Detail table: confirmed sales grouped by seller
        if (!confirmed.isEmpty()) {
            salesBySeller.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String seller = entry.getKey();
                        List<SalesReport> sellerSales = entry.getValue();

                        try {
                            PdfPTable detailTable = new PdfPTable(new float[] { 2.2f, 2f, 4f, 1.5f, 1.5f });
                            detailTable.setWidthPercentage(100);
                            detailTable.setSpacingBefore(10);
                            detailTable.setSpacingAfter(15);

                            PdfPCell detailHeader = new PdfPCell(new Phrase("DETALLE DE VENTAS: " + seller.toUpperCase(), HEADER_FONT));
                            detailHeader.setColspan(5);
                            detailHeader.setBackgroundColor(new Color(33, 37, 41));
                            detailHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                            detailHeader.setPadding(6);
                            detailTable.addCell(detailHeader);

                            addTableHeader(detailTable, new String[] { "FECHA", "DOCUMENTO", "CLIENTE", "COMPROBANTE", "TOTAL" },
                                    new Color(240, 240, 240), BOLD_FONT);

                            for (SalesReport sale : sellerSales) {
                                detailTable.addCell(createCell(sale.date().format(DT_FMT), NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(sale.documentNumber(), NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(sale.customerName() != null ? sale.customerName() : "PUBLICO EN GENERAL",
                                        NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(formatDocString(sale.documentType()), NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(sale.total().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                            }

                            // Seller subtotal
                            BigDecimal sellerTotal = sellerSales.stream().map(SalesReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);
                            PdfPCell subtotalLabel = createCell("TOTAL " + seller.toUpperCase(), BOLD_FONT, Element.ALIGN_CENTER);
                            subtotalLabel.setColspan(4);
                            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
                            detailTable.addCell(subtotalLabel);
                            detailTable.addCell(createCell(sellerTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

                            document.add(detailTable);
                        } catch (Exception e) {
                            throw new RuntimeException("Error generating seller PDF table", e);
                        }
                    });
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * PDF: Reporte de ventas por vendedor agrupado por categoría.
     */
    public static byte[] generateSellerCategoriesReport(List<SalesByCategoryDetailReport> reports,
            CompanyResponse company, String startDate, String endDate, String sellerName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE VENTAS POR CATEGORÍA - VENDEDOR", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addSellerSubtitle(document, sellerName);
        addDateHeader(document, startDate, endDate);

        BigDecimal grandTotal = reports.stream().map(SalesByCategoryDetailReport::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Summary
        PdfPTable summaryTable = new PdfPTable(new float[] { 4f, 2f, 2f });
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN POR CATEGORÍAS", HEADER_FONT));
        summaryHeaderCell.setColspan(3);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "CATEGORÍA", "CANTIDAD PRODUCTOS", "TOTAL INGRESOS" });

        for (SalesByCategoryDetailReport report : reports) {
            summaryTable.addCell(createCell(report.categoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(String.valueOf(report.productCount()), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.totalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        long totalProductCount = reports.stream().mapToLong(SalesByCategoryDetailReport::productCount).sum();
        PdfPCell totalLabel = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(totalProductCount), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(grandTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        // Detail by category
        for (SalesByCategoryDetailReport report : reports) {
            PdfPTable detailTable = new PdfPTable(new float[] { 5f, 2f, 2f, 2f });
            detailTable.setWidthPercentage(100);
            detailTable.setSpacingBefore(10);
            detailTable.setSpacingAfter(15);

            PdfPCell categoryHeaderCell = new PdfPCell(new Phrase(report.categoryName().toUpperCase(), HEADER_FONT));
            categoryHeaderCell.setColspan(4);
            categoryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
            categoryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            categoryHeaderCell.setPadding(6);
            detailTable.addCell(categoryHeaderCell);

            addTableHeader(detailTable, new String[] { "PRODUCTO", "LABORATORIO", "CANTIDAD VENDIDA", "SUBTOTAL" },
                    new Color(240, 240, 240), BOLD_FONT);

            for (SalesByCategoryDetailReport.ProductDetail detail : report.products()) {
                detailTable.addCell(createCell(detail.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(detail.laboratoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(detail.quantitySold().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(detail.revenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
            }

            BigDecimal totalQty = report.products().stream()
                    .map(SalesByCategoryDetailReport.ProductDetail::quantitySold)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPCell subtotalLabel = createCell("TOTAL", BOLD_FONT, Element.ALIGN_CENTER);
            subtotalLabel.setColspan(2);
            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
            detailTable.addCell(subtotalLabel);
            detailTable.addCell(createCell(totalQty.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(report.totalRevenue().toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

            document.add(detailTable);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * PDF: Reporte de ventas por vendedor agrupado por producto.
     */
    public static byte[] generateSellerProductsReport(List<SalesByProductReport> reports,
            CompanyResponse company, String startDate, String endDate, String sellerName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE VENTAS POR PRODUCTO - VENDEDOR", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addSellerSubtitle(document, sellerName);
        addDateHeader(document, startDate, endDate);

        // Summary
        BigDecimal grandTotalRevenue = reports.stream().map(SalesByProductReport::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long grandTotalQty = reports.stream().mapToLong(SalesByProductReport::getQuantitySold).sum();

        PdfPTable summaryTable = new PdfPTable(new float[] { 5f, 3f, 2f, 2.5f });
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN DE PRODUCTOS VENDIDOS", HEADER_FONT));
        summaryHeaderCell.setColspan(4);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "PRODUCTO", "CATEGORÍA", "CANTIDAD", "TOTAL INGRESOS" },
                new Color(240, 240, 240), BOLD_FONT);

        List<SalesByProductReport> sortedProducts = reports.stream()
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .collect(Collectors.toList());

        for (SalesByProductReport report : sortedProducts) {
            summaryTable.addCell(createCell(report.getProductName(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.getCategoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(String.valueOf(report.getQuantitySold()), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.getTotalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        PdfPCell totalLabelCell = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabelCell.setColspan(2);
        totalLabelCell.setBackgroundColor(new Color(245, 245, 245));
        summaryTable.addCell(totalLabelCell);
        summaryTable.addCell(createCell(String.valueOf(grandTotalQty), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(grandTotalRevenue.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        document.close();
        return baos.toByteArray();
    }

    private static void addSellerSubtitle(Document document, String sellerName) throws Exception {
        Font sellerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(0, 102, 51));
        Paragraph seller = new Paragraph("VENDEDOR: " + sellerName.toUpperCase(), sellerFont);
        seller.setAlignment(Element.ALIGN_CENTER);
        seller.setSpacingBefore(5);
        seller.setSpacingAfter(5);
        document.add(seller);
    }

    private static void addPageNumbers(PdfWriter writer, Document document) {
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                Phrase footer = new Phrase("Pg. " + writer.getPageNumber(), NORMAL_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footer, document.right(), document.top() + 10, 0);
            }
        });
    }

    private static void addCompanyHeader(Document document, CompanyResponse company) throws Exception {
        if (company != null) {
            Paragraph companyName = new Paragraph(company.name(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(companyName);
            document.add(Chunk.NEWLINE);
        }
    }

    private static void addDateHeader(Document document, String startDate, String endDate) throws Exception {
        Paragraph dates = new Paragraph("DESDE: " + startDate + "          HASTA: " + endDate, BOLD_FONT);
        dates.setAlignment(Element.ALIGN_CENTER);
        dates.setSpacingBefore(10);
        dates.setSpacingAfter(15);
        document.add(dates);
    }

    private static String formatDocString(String type) {
        if (type == null)
            return "DESCONOCIDO";
        switch (type) {
            case "FACTURA":
                return "FACTURA ELECTRÓNICA";
            case "BOLETA":
                return "BOLETA ELECTRÓNICA";
            case "NOTA_CREDITO":
                return "NOTA DE CRÉDITO";
            case "NOTA_DEBITO":
                return "NOTA DE DÉBITO";
            case "NOTA_DE_VENTA":
                return "NOTA DE VENTA";
            default:
                return type;
        }
    }

    private static void addSectionTitle(Document document, String title) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(title, SECTION_FONT));
        cell.setBackgroundColor(new Color(108, 117, 125));
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        document.add(table);
    }

    private static void addTableHeader(PdfPTable table, String[] headers) {
        addTableHeader(table, headers, Color.WHITE, BOLD_FONT);
    }

    private static void addTableHeader(PdfPTable table, String[] headers, Color bgColor, Font font) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(bgColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String content, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        return cell;
    }

    /**
     * PDF: Reporte de ventas por cliente.
     */
    public static byte[] generateCustomerReport(List<SalesByCustomerReport> customers,
            List<SalesReport> salesDetail, CompanyResponse company,
            String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE VENTAS POR CLIENTE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        // ── Summary Table ──
        BigDecimal grandTotal = customers.stream()
                .map(SalesByCustomerReport::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long grandTransactions = customers.stream()
                .mapToLong(SalesByCustomerReport::transactionCount)
                .sum();

        PdfPTable summaryTable = new PdfPTable(new float[] { 4f, 2f, 2f, 2f });
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeader = new PdfPCell(new Phrase("RESUMEN DE VENTAS POR CLIENTE", HEADER_FONT));
        summaryHeader.setColspan(4);
        summaryHeader.setBackgroundColor(new Color(33, 37, 41));
        summaryHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeader.setPadding(6);
        summaryTable.addCell(summaryHeader);

        addTableHeader(summaryTable, new String[] { "CLIENTE", "DOCUMENTO", "TRANSACCIONES", "MONTO TOTAL" },
                new Color(240, 240, 240), BOLD_FONT);

        for (SalesByCustomerReport customer : customers) {
            summaryTable.addCell(createCell(customer.customerName(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(customer.documentNumber(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(String.valueOf(customer.transactionCount()), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(customer.totalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        PdfPCell totalLabel = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setColspan(2);
        totalLabel.setBackgroundColor(new Color(245, 245, 245));
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(grandTransactions), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(grandTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        // ── Detail Tables grouped by customer ──
        if (salesDetail != null && !salesDetail.isEmpty()) {
            Map<String, List<SalesReport>> detailByCustomer = salesDetail.stream()
                    .collect(Collectors.groupingBy(s -> s.customerName() != null ? s.customerName() : "PUBLICO EN GENERAL"));

            detailByCustomer.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String customerName = entry.getKey();
                        List<SalesReport> customerSales = entry.getValue();

                        try {
                            PdfPTable detailTable = new PdfPTable(new float[] { 2.2f, 2f, 3f, 1.5f, 1.5f });
                            detailTable.setWidthPercentage(100);
                            detailTable.setSpacingBefore(10);
                            detailTable.setSpacingAfter(15);

                            PdfPCell detailHeader = new PdfPCell(new Phrase("DETALLE: " + customerName.toUpperCase(), HEADER_FONT));
                            detailHeader.setColspan(5);
                            detailHeader.setBackgroundColor(new Color(33, 37, 41));
                            detailHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                            detailHeader.setPadding(6);
                            detailTable.addCell(detailHeader);

                            addTableHeader(detailTable, new String[] { "FECHA", "DOCUMENTO", "VENDEDOR", "COMPROBANTE", "TOTAL" },
                                    new Color(240, 240, 240), BOLD_FONT);

                            for (SalesReport sale : customerSales) {
                                detailTable.addCell(createCell(sale.date().format(DT_FMT), NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(sale.documentNumber(), NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(sale.employeeName() != null ? sale.employeeName() : "N/A",
                                        NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(formatDocString(sale.documentType()), NORMAL_FONT, Element.ALIGN_CENTER));
                                detailTable.addCell(createCell(sale.total().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                            }

                            BigDecimal customerTotal = customerSales.stream()
                                    .map(SalesReport::total)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            PdfPCell subtotalLabel = createCell("TOTAL " + customerName.toUpperCase(), BOLD_FONT, Element.ALIGN_CENTER);
                            subtotalLabel.setColspan(4);
                            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
                            detailTable.addCell(subtotalLabel);
                            detailTable.addCell(createCell(customerTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

                            document.add(detailTable);
                        } catch (Exception e) {
                            throw new RuntimeException("Error generating customer PDF table", e);
                        }
                    });
        }

        document.close();
        return baos.toByteArray();
    }
}
