package com.sergiocodev.app.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sergiocodev.app.dto.company.CompanyResponse;
import com.sergiocodev.app.dto.report.SalesReport;
import com.sergiocodev.app.dto.report.SalesByCategoryDetailReport;
import com.sergiocodev.app.dto.report.SalesByCustomerReport;
import com.sergiocodev.app.dto.report.SalesByProductReport;
import com.sergiocodev.app.dto.report.SalesBySeriesReport;
import com.sergiocodev.app.dto.report.PurchaseReport;

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
                        r -> (r.therapeuticAction() == null || r.therapeuticAction().isEmpty())
                                ? "SIN ACCIÓN TERAPÉUTICA"
                                : r.therapeuticAction(),
                        java.util.TreeMap::new,
                        Collectors.toList()));

        BigDecimal grandTotalRevenue = BigDecimal.ZERO;
        long grandTotalQty = 0;

        // Calculate totals first for the summary
        for (List<SalesByProductReport> actionProducts : groupedByAction.values()) {
            grandTotalQty += actionProducts.stream().mapToLong(SalesByProductReport::quantitySold).sum();
            grandTotalRevenue = grandTotalRevenue.add(actionProducts.stream()
                    .map(SalesByProductReport::totalRevenue)
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
            long qty = entry.getValue().stream().mapToLong(SalesByProductReport::quantitySold).sum();
            BigDecimal total = entry.getValue().stream()
                    .map(SalesByProductReport::totalRevenue)
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
                    .sorted(java.util.Comparator.comparing(SalesByProductReport::laboratoryName)
                            .thenComparing(SalesByProductReport::productName))
                    .collect(Collectors.toList());

            for (SalesByProductReport report : sortedProducts) {
                table.addCell(createCell(report.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
                table.addCell(createCell(report.laboratoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
                table.addCell(createCell(String.valueOf(report.quantitySold()), NORMAL_FONT, Element.ALIGN_CENTER));
                table.addCell(createCell(report.totalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));

                actionTotalRevenue = actionTotalRevenue.add(report.totalRevenue());
                actionTotalQty += report.quantitySold();
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
        BigDecimal grandTotalRevenue = reports.stream().map(SalesByProductReport::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long grandTotalQty = reports.stream().mapToLong(SalesByProductReport::quantitySold).sum();

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
                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                .collect(Collectors.toList());

        for (SalesByProductReport report : sortedProducts) {
            summaryTable.addCell(createCell(report.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.categoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(String.valueOf(report.quantitySold()), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.totalRevenue().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
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

    public static byte[] generatePurchaseComprobantesReport(List<PurchaseReport> purchases, CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40); // Portrait A4
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE POR COMPROBANTE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        // Group by document type
        Map<String, List<PurchaseReport>> purchasesByType = purchases.stream()
                .filter(p -> !"VOIDED".equals(p.status())) // skip voided if needed, or keep them
                .collect(Collectors.groupingBy(p -> p.documentType() != null ? p.documentType() : "OTROS"));

        addSectionTitle(document, "RESUMEN POR COMPROBANTES");

        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(5);
        summaryTable.setSpacingAfter(20);

        addTableHeader(summaryTable, new String[] { "COMPROBANTE", "CANTIDAD", "TOTAL (S/)" });

        long totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<String, List<PurchaseReport>> entry : purchasesByType.entrySet()) {
            String docType = entry.getKey();
            List<PurchaseReport> typePurchases = entry.getValue();

            long qty = typePurchases.size();
            BigDecimal amount = typePurchases.stream()
                    .map(p -> p.total() != null ? p.total() : BigDecimal.ZERO)
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

        addSectionTitle(document, "COMPRAS CONFIRMADAS POR COMPROBANTE");

        for (Map.Entry<String, List<PurchaseReport>> entry : purchasesByType.entrySet()) {
            String docType = entry.getKey();
            List<PurchaseReport> typePurchases = entry.getValue();

            PdfPTable detailsTable = new PdfPTable(new float[] { 2f, 3f, 4f, 2f });
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10);
            detailsTable.setSpacingAfter(15);

            PdfPCell typeCell = new PdfPCell(new Phrase(docType, HEADER_FONT));
            typeCell.setColspan(4);
            typeCell.setBackgroundColor(new Color(33, 37, 41));
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            typeCell.setPadding(5);
            detailsTable.addCell(typeCell);

            addTableHeader(detailsTable, new String[] { "FECHA", "DOCUMENTO", "PROVEEDOR", "TOTAL" },
                    new Color(240, 240, 240), BOLD_FONT);

            for (PurchaseReport purchase : typePurchases) {
                detailsTable.addCell(createCell(purchase.issueDate() != null ? purchase.issueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(purchase.documentNumber() != null ? purchase.documentNumber() : "", NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(purchase.supplierName() != null ? purchase.supplierName() : "PROVEEDOR", NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(purchase.total() != null ? purchase.total().toPlainString() : "0.00", NORMAL_FONT, Element.ALIGN_CENTER));
            }

            // Subtotal row for the voucher type
            BigDecimal typeTotal = typePurchases.stream()
                    .map(p -> p.total() != null ? p.total() : BigDecimal.ZERO)
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

    public static byte[] generatePurchaseCategoriesReport(List<com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE COMPRAS POR CATEGORÍA", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        BigDecimal grandTotal = reports.stream()
                .map(r -> r.totalSpent() != null ? r.totalSpent() : BigDecimal.ZERO)
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

        addTableHeader(summaryTable, new String[] { "CATEGORÍA", "CANTIDAD PRODUCTOS", "TOTAL" });

        for (com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport report : reports) {
            summaryTable.addCell(createCell(report.categoryName() != null ? report.categoryName() : "SIN CATEGORÍA", NORMAL_FONT, Element.ALIGN_LEFT));
            summaryTable.addCell(createCell(String.valueOf(report.productCount()), NORMAL_FONT, Element.ALIGN_CENTER));
            summaryTable.addCell(createCell(report.totalSpent() != null ? report.totalSpent().toPlainString() : "0.00", NORMAL_FONT, Element.ALIGN_CENTER));
        }

        long totalProductCount = reports.stream().mapToLong(com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport::productCount).sum();

        PdfPCell totalLabel = createCell("TOTAL GENERAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setColspan(1);
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(totalProductCount), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(grandTotal.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        for (com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport report : reports) {
            PdfPTable detailTable = new PdfPTable(new float[] { 4f, 2f, 2f, 2f, 2f });
            detailTable.setWidthPercentage(100);
            detailTable.setSpacingBefore(10);
            detailTable.setSpacingAfter(15);

            // Group header row (Full width, dark background)
            PdfPCell categoryHeaderCell = new PdfPCell(new Phrase(report.categoryName() != null ? report.categoryName().toUpperCase() : "SIN CATEGORÍA", HEADER_FONT));
            categoryHeaderCell.setColspan(5);
            categoryHeaderCell.setBackgroundColor(new Color(33, 37, 41)); // Dark grey
            categoryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            categoryHeaderCell.setPadding(6);
            detailTable.addCell(categoryHeaderCell);

            addTableHeader(detailTable, new String[] { "PRODUCTO", "LABORATORIO", "PRECIO UNIT.", "CANTIDAD", "SUBTOTAL" },
                    new Color(240, 240, 240), BOLD_FONT);

            for (com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport.ProductDetail detail : report.products()) {
                BigDecimal qty = detail.quantityPurchased() != null ? detail.quantityPurchased() : BigDecimal.ZERO;
                BigDecimal spent = detail.spent() != null ? detail.spent() : BigDecimal.ZERO;
                BigDecimal unitPrice = qty.compareTo(BigDecimal.ZERO) > 0 ? spent.divide(qty, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

                detailTable.addCell(createCell(detail.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(detail.laboratoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(unitPrice.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(qty.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailTable.addCell(createCell(spent.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
            }

            // Subtotal row for the category
            BigDecimal totalQty = report.products().stream()
                    .map(p -> p.quantityPurchased() != null ? p.quantityPurchased() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPCell subtotalLabel = createCell("TOTAL", BOLD_FONT, Element.ALIGN_CENTER);
            subtotalLabel.setColspan(3);
            subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
            detailTable.addCell(subtotalLabel);
            detailTable.addCell(createCell(totalQty.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(report.totalSpent() != null ? report.totalSpent().toPlainString() : "0.00", BOLD_FONT, Element.ALIGN_CENTER));

            document.add(detailTable);
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generatePurchasesBySupplierReport(List<com.sergiocodev.app.dto.report.PurchasesBySupplierReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE COMPRAS POR PROVEEDOR", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        // Calculate summary metrics
        BigDecimal totalSpent = reports.stream()
                .map(r -> r.totalSpent() != null ? r.totalSpent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalSuppliers = reports.size();
        String mainSupplier = reports.isEmpty() ? "N/A" : reports.get(0).supplierName();
        BigDecimal avgPurchases = totalSuppliers > 0 ? totalSpent.divide(BigDecimal.valueOf(totalSuppliers), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // SUMMARY TABLE
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN GENERAL", HEADER_FONT));
        summaryHeaderCell.setColspan(2);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "Indicador", "Valor" }, new Color(245, 245, 245), BOLD_FONT);

        summaryTable.addCell(createCell("Total Comprado", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell("S/ " + totalSpent.toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Total Proveedores", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(String.valueOf(totalSuppliers), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Proveedor Principal", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(mainSupplier, NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Compras Promedio", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell("S/ " + avgPurchases.toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

        document.add(summaryTable);

        // DETAIL TABLE
        PdfPTable detailTable = new PdfPTable(new float[] { 4f, 1.5f, 2.5f, 2f, 1.5f });
        detailTable.setWidthPercentage(100);
        detailTable.setSpacingBefore(10);
        detailTable.setSpacingAfter(15);

        addTableHeader(detailTable, new String[] { "Proveedor", "Compras", "Total Gastado", "Última Compra", "Estado" },
                new Color(240, 240, 240), BOLD_FONT);

        for (com.sergiocodev.app.dto.report.PurchasesBySupplierReport report : reports) {
            detailTable.addCell(createCell(report.supplierName(), NORMAL_FONT, Element.ALIGN_LEFT));
            detailTable.addCell(createCell(String.valueOf(report.purchaseCount()), NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell("S/ " + (report.totalSpent() != null ? report.totalSpent().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
            String dateStr = report.lastPurchaseDate() != null ? report.lastPurchaseDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            detailTable.addCell(createCell(dateStr, NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(report.status(), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        document.add(detailTable);

        // PRODUCT DETAILS PER SUPPLIER
        for (com.sergiocodev.app.dto.report.PurchasesBySupplierReport report : reports) {
            if (report.products() != null && !report.products().isEmpty()) {
                PdfPTable productTable = new PdfPTable(new float[] { 4f, 2f, 2f, 2f, 2f });
                productTable.setWidthPercentage(100);
                productTable.setSpacingBefore(15);
                productTable.setSpacingAfter(15);

                PdfPCell supplierHeaderCell = new PdfPCell(new Phrase("PRODUCTOS COMPRADOS A: " + (report.supplierName() != null ? report.supplierName().toUpperCase() : "N/A"), HEADER_FONT));
                supplierHeaderCell.setColspan(5);
                supplierHeaderCell.setBackgroundColor(new Color(33, 37, 41)); // Dark grey
                supplierHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                supplierHeaderCell.setPadding(6);
                productTable.addCell(supplierHeaderCell);

                addTableHeader(productTable, new String[] { "PRODUCTO", "LABORATORIO", "PRECIO UNIT.", "CANTIDAD", "SUBTOTAL" },
                        new Color(240, 240, 240), BOLD_FONT);

                for (com.sergiocodev.app.dto.report.PurchasesBySupplierReport.ProductDetail detail : report.products()) {
                    productTable.addCell(createCell(detail.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    productTable.addCell(createCell(detail.laboratoryName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    productTable.addCell(createCell("S/ " + (detail.unitPrice() != null ? detail.unitPrice().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
                    productTable.addCell(createCell(detail.quantity() != null ? detail.quantity().toPlainString() : "0", NORMAL_FONT, Element.ALIGN_CENTER));
                    productTable.addCell(createCell("S/ " + (detail.total() != null ? detail.total().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
                }
                
                // Subtotal
                PdfPCell subtotalLabel = createCell("TOTAL COMPRADO", BOLD_FONT, Element.ALIGN_CENTER);
                subtotalLabel.setColspan(3);
                subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
                productTable.addCell(subtotalLabel);

                BigDecimal totalQty = report.products().stream()
                        .map(p -> p.quantity() != null ? p.quantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                productTable.addCell(createCell(totalQty.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
                productTable.addCell(createCell("S/ " + (report.totalSpent() != null ? report.totalSpent().toPlainString() : "0.00"), BOLD_FONT, Element.ALIGN_CENTER));

                document.add(productTable);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generateAccountsPayableBySupplierReport(List<com.sergiocodev.app.dto.report.AccountsPayableSupplierReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE CUENTAS POR PAGAR (POR PROVEEDOR)", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        // Calculate summary metrics
        BigDecimal grandTotalPending = reports.stream()
                .map(r -> r.totalPending() != null ? r.totalPending() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalInvoices = reports.stream().mapToInt(com.sergiocodev.app.dto.report.AccountsPayableSupplierReport::pendingInvoicesCount).sum();
        int totalSuppliers = reports.size();
        BigDecimal totalOverdue = reports.stream()
                .map(r -> r.overdueDebt() != null ? r.overdueDebt() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // SUMMARY TABLE
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN GENERAL", HEADER_FONT));
        summaryHeaderCell.setColspan(2);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "Indicador", "Valor" }, new Color(245, 245, 245), BOLD_FONT);

        summaryTable.addCell(createCell("Total Pendiente", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell("S/ " + grandTotalPending.toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Facturas Pendientes", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(String.valueOf(totalInvoices), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Proveedores con Deuda", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(String.valueOf(totalSuppliers), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Deuda Vencida", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell("S/ " + totalOverdue.toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

        document.add(summaryTable);

        // INVOICES TABLES PER SUPPLIER
        for (com.sergiocodev.app.dto.report.AccountsPayableSupplierReport report : reports) {
            if (report.invoices() != null && !report.invoices().isEmpty()) {
                PdfPTable invoiceTable = new PdfPTable(new float[] { 3f, 2f, 2f, 2f, 2f, 2f });
                invoiceTable.setWidthPercentage(100);
                invoiceTable.setSpacingBefore(15);
                invoiceTable.setSpacingAfter(15);

                PdfPCell supplierHeaderCell = new PdfPCell(new Phrase("DEUDAS CON: " + (report.supplierName() != null ? report.supplierName().toUpperCase() : "N/A"), HEADER_FONT));
                supplierHeaderCell.setColspan(6);
                supplierHeaderCell.setBackgroundColor(new Color(33, 37, 41)); // Dark grey
                supplierHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                supplierHeaderCell.setPadding(6);
                invoiceTable.addCell(supplierHeaderCell);

                addTableHeader(invoiceTable, new String[] { "Proveedor", "Factura", "Fecha Compra", "Vencimiento", "Monto Pendiente", "Estado" },
                        new Color(240, 240, 240), BOLD_FONT);

                for (com.sergiocodev.app.dto.report.AccountsPayableSupplierReport.InvoiceDetail inv : report.invoices()) {
                    invoiceTable.addCell(createCell(report.supplierName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    invoiceTable.addCell(createCell(inv.invoiceNumber(), NORMAL_FONT, Element.ALIGN_CENTER));
                    String purchaseDateStr = inv.purchaseDate() != null ? inv.purchaseDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                    String dueDateStr = inv.dueDate() != null ? inv.dueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                    invoiceTable.addCell(createCell(purchaseDateStr, NORMAL_FONT, Element.ALIGN_CENTER));
                    invoiceTable.addCell(createCell(dueDateStr, NORMAL_FONT, Element.ALIGN_CENTER));
                    invoiceTable.addCell(createCell("S/ " + (inv.pendingAmount() != null ? inv.pendingAmount().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
                    
                    Font statusFont = NORMAL_FONT;
                    if ("Vencido".equals(inv.status())) {
                        statusFont = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(220, 53, 69)); // Red
                    } else if ("Próximo vencer".equals(inv.status())) {
                        statusFont = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(253, 126, 20)); // Orange
                    }
                    invoiceTable.addCell(createCell(inv.status(), statusFont, Element.ALIGN_CENTER));
                }
                
                // Subtotal
                PdfPCell subtotalLabel = createCell("TOTAL DEUDA", BOLD_FONT, Element.ALIGN_CENTER);
                subtotalLabel.setColspan(4);
                subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
                invoiceTable.addCell(subtotalLabel);

                invoiceTable.addCell(createCell("S/ " + (report.totalPending() != null ? report.totalPending().toPlainString() : "0.00"), BOLD_FONT, Element.ALIGN_CENTER));
                
                PdfPCell emptyCell = createCell("", NORMAL_FONT, Element.ALIGN_CENTER);
                emptyCell.setBackgroundColor(new Color(245, 245, 245));
                invoiceTable.addCell(emptyCell);

                document.add(invoiceTable);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generateProductPriceHistoryReport(List<com.sergiocodev.app.dto.report.ProductPriceHistoryReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE HISTORIAL DE PRECIOS", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        for (com.sergiocodev.app.dto.report.ProductPriceHistoryReport report : reports) {
            
            Paragraph prodTitle = new Paragraph("PRODUCTO: " + report.productName().toUpperCase(), HEADER_FONT);
            prodTitle.setSpacingBefore(15);
            prodTitle.setSpacingAfter(10);
            document.add(prodTitle);

            // SUMMARY TABLE
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(15);

            addTableHeader(summaryTable, new String[] { "Indicador", "Valor" }, new Color(245, 245, 245), BOLD_FONT);

            summaryTable.addCell(createCell("Precio Actual", NORMAL_FONT, Element.ALIGN_LEFT));
            summaryTable.addCell(createCell("S/ " + report.currentPrice().toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

            summaryTable.addCell(createCell("Precio Más Bajo", NORMAL_FONT, Element.ALIGN_LEFT));
            summaryTable.addCell(createCell("S/ " + report.lowestPrice().toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

            summaryTable.addCell(createCell("Precio Más Alto", NORMAL_FONT, Element.ALIGN_LEFT));
            summaryTable.addCell(createCell("S/ " + report.highestPrice().toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

            summaryTable.addCell(createCell("Variación Total", NORMAL_FONT, Element.ALIGN_LEFT));
            summaryTable.addCell(createCell(report.totalVariation(), NORMAL_FONT, Element.ALIGN_LEFT));

            document.add(summaryTable);

            // HISTORY TABLE
            if (report.history() != null && !report.history().isEmpty()) {
                PdfPTable historyTable = new PdfPTable(new float[] { 2f, 3f, 3f, 1.5f, 2f, 1.5f });
                historyTable.setWidthPercentage(100);
                historyTable.setSpacingAfter(20);

                addTableHeader(historyTable, new String[] { "Fecha", "Producto", "Proveedor", "Cantidad", "Precio Unitario", "Variación" },
                        new Color(240, 240, 240), BOLD_FONT);

                for (com.sergiocodev.app.dto.report.ProductPriceHistoryReport.PriceHistoryDetail det : report.history()) {
                    String dateStr = det.date() != null ? det.date().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                    historyTable.addCell(createCell(dateStr, NORMAL_FONT, Element.ALIGN_CENTER));
                    historyTable.addCell(createCell(det.productName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    historyTable.addCell(createCell(det.supplierName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    historyTable.addCell(createCell(det.quantity().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                    historyTable.addCell(createCell("S/ " + det.unitPrice().toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
                    historyTable.addCell(createCell(det.variation(), NORMAL_FONT, Element.ALIGN_CENTER));
                }

                document.add(historyTable);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    public static byte[] generatePurchasesByBuyerReport(List<com.sergiocodev.app.dto.report.PurchasesByBuyerReport> reports,
            CompanyResponse company, String startDate, String endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE COMPRAS POR COMPRADOR", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        addDateHeader(document, startDate, endDate);

        // Calculate summary metrics
        int grandTotalPurchases = reports.stream().mapToInt(com.sergiocodev.app.dto.report.PurchasesByBuyerReport::totalPurchases).sum();
        int activeUsersCount = reports.size();
        BigDecimal grandTotalSpent = reports.stream()
                .map(r -> r.totalSpent() != null ? r.totalSpent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String topBuyer = reports.isEmpty() ? "N/A" : reports.get(0).buyerName();

        // SUMMARY TABLE
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN GENERAL", HEADER_FONT));
        summaryHeaderCell.setColspan(2);
        summaryHeaderCell.setBackgroundColor(new Color(33, 37, 41));
        summaryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryHeaderCell.setPadding(6);
        summaryTable.addCell(summaryHeaderCell);

        addTableHeader(summaryTable, new String[] { "Indicador", "Valor" }, new Color(245, 245, 245), BOLD_FONT);

        summaryTable.addCell(createCell("Total Compras", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(String.valueOf(grandTotalPurchases), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Usuarios Activos", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(String.valueOf(activeUsersCount), NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Mayor Registrador", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell(topBuyer, NORMAL_FONT, Element.ALIGN_LEFT));

        summaryTable.addCell(createCell("Total Comprado", NORMAL_FONT, Element.ALIGN_LEFT));
        summaryTable.addCell(createCell("S/ " + grandTotalSpent.toPlainString(), NORMAL_FONT, Element.ALIGN_LEFT));

        document.add(summaryTable);

        // BUYERS SUMMARY TABLE
        PdfPTable buyersTable = new PdfPTable(new float[] { 3f, 2f, 2f, 2f, 2f });
        buyersTable.setWidthPercentage(100);
        buyersTable.setSpacingBefore(10);
        buyersTable.setSpacingAfter(20);

        addTableHeader(buyersTable, new String[] { "Usuario", "Compras Registradas", "Monto Total", "Última Compra", "Promedio" },
                new Color(240, 240, 240), BOLD_FONT);

        for (com.sergiocodev.app.dto.report.PurchasesByBuyerReport report : reports) {
            buyersTable.addCell(createCell(report.buyerName(), NORMAL_FONT, Element.ALIGN_CENTER));
            buyersTable.addCell(createCell(String.valueOf(report.totalPurchases()), NORMAL_FONT, Element.ALIGN_CENTER));
            buyersTable.addCell(createCell("S/ " + (report.totalSpent() != null ? report.totalSpent().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
            String lastPurchaseDateStr = report.lastPurchaseDate() != null ? report.lastPurchaseDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            buyersTable.addCell(createCell(lastPurchaseDateStr, NORMAL_FONT, Element.ALIGN_CENTER));
            buyersTable.addCell(createCell("S/ " + (report.averagePurchase() != null ? report.averagePurchase().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
        }

        document.add(buyersTable);

        // DETAILED PURCHASES PER BUYER
        for (com.sergiocodev.app.dto.report.PurchasesByBuyerReport report : reports) {
            if (report.purchases() != null && !report.purchases().isEmpty()) {
                PdfPTable detailsTable = new PdfPTable(new float[] { 2f, 3f, 3f, 2f, 2f });
                detailsTable.setWidthPercentage(100);
                detailsTable.setSpacingBefore(15);
                detailsTable.setSpacingAfter(15);

                PdfPCell buyerHeaderCell = new PdfPCell(new Phrase("DETALLE DE COMPRAS: " + (report.buyerName() != null ? report.buyerName().toUpperCase() : "N/A"), HEADER_FONT));
                buyerHeaderCell.setColspan(5);
                buyerHeaderCell.setBackgroundColor(new Color(33, 37, 41)); // Dark grey
                buyerHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                buyerHeaderCell.setPadding(6);
                detailsTable.addCell(buyerHeaderCell);

                addTableHeader(detailsTable, new String[] { "Fecha", "Usuario", "Proveedor", "Documento", "Total" },
                        new Color(240, 240, 240), BOLD_FONT);

                for (com.sergiocodev.app.dto.report.PurchasesByBuyerReport.PurchaseDetail det : report.purchases()) {
                    String dateStr = det.date() != null ? det.date().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                    detailsTable.addCell(createCell(dateStr, NORMAL_FONT, Element.ALIGN_CENTER));
                    detailsTable.addCell(createCell(det.buyerName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    detailsTable.addCell(createCell(det.supplierName(), NORMAL_FONT, Element.ALIGN_CENTER));
                    detailsTable.addCell(createCell(det.document(), NORMAL_FONT, Element.ALIGN_CENTER));
                    detailsTable.addCell(createCell("S/ " + (det.total() != null ? det.total().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_CENTER));
                }
                
                // Subtotal row
                PdfPCell subtotalLabel = createCell("TOTAL COMPRAS DE " + report.buyerName(), BOLD_FONT, Element.ALIGN_RIGHT);
                subtotalLabel.setColspan(4);
                subtotalLabel.setBackgroundColor(new Color(245, 245, 245));
                subtotalLabel.setPaddingRight(10);
                detailsTable.addCell(subtotalLabel);

                detailsTable.addCell(createCell("S/ " + (report.totalSpent() != null ? report.totalSpent().toPlainString() : "0.00"), BOLD_FONT, Element.ALIGN_CENTER));

                document.add(detailsTable);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    // ── CASH BOX REPORTS ────────────────────────────────────────────────────────

    /**
     * PDF: Historial de sesiones de caja por período.
     */
    public static byte[] generateCashSessionsReport(
            List<com.sergiocodev.app.dto.report.CashSessionReport> sessions,
            CompanyResponse company,
            String startDate, String endDate) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("HISTORIAL DE SESIONES DE CAJA", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        addDateHeader(document, startDate, endDate);

        // Summary counts
        long openCount = sessions.stream().filter(s -> "OPEN".equals(s.status())).count();
        long closedCount = sessions.stream().filter(s -> "CLOSED".equals(s.status())).count();

        PdfPTable summaryTable = new PdfPTable(new float[]{4f, 2f, 2f, 2f, 2f});
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell headerCell = new PdfPCell(new Phrase("RESUMEN DE SESIONES", HEADER_FONT));
        headerCell.setColspan(5);
        headerCell.setBackgroundColor(new Color(33, 37, 41));
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(6);
        summaryTable.addCell(headerCell);

        addTableHeader(summaryTable,
                new String[]{"TOTAL SESIONES", "ABIERTAS", "CERRADAS", "INGRESOS TOTALES", "EGRESOS TOTALES"});

        java.math.BigDecimal totalOpen = sessions.stream()
                .filter(s -> s.openingBalance() != null)
                .map(s -> s.openingBalance())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalDiff = sessions.stream()
                .filter(s -> s.diffAmount() != null)
                .map(s -> s.diffAmount().abs())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        summaryTable.addCell(createCell(String.valueOf(sessions.size()), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(String.valueOf(openCount), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(String.valueOf(closedCount), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell("S/ " + totalOpen.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell("S/ " + totalDiff.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        document.add(summaryTable);

        // Detail table
        PdfPTable detailTable = new PdfPTable(new float[]{1.5f, 2.5f, 2f, 2f, 2f, 2f, 2f, 1.8f});
        detailTable.setWidthPercentage(100);
        detailTable.setSpacingBefore(5);
        detailTable.setSpacingAfter(10);

        PdfPCell detailHeader = new PdfPCell(new Phrase("DETALLE DE SESIONES", HEADER_FONT));
        detailHeader.setColspan(8);
        detailHeader.setBackgroundColor(new Color(33, 37, 41));
        detailHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        detailHeader.setPadding(6);
        detailTable.addCell(detailHeader);

        addTableHeader(detailTable,
                new String[]{"ID", "CAJA", "CAJERO", "APERTURA", "CIERRE", "S. INICIAL", "DIFERENCIA", "ESTADO"},
                new Color(240, 240, 240), BOLD_FONT);

        java.time.format.DateTimeFormatter dtFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        for (com.sergiocodev.app.dto.report.CashSessionReport s : sessions) {
            detailTable.addCell(createCell(s.sessionId().toString(), NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(s.cashRegisterName(), NORMAL_FONT, Element.ALIGN_LEFT));
            detailTable.addCell(createCell(s.username(), NORMAL_FONT, Element.ALIGN_LEFT));
            detailTable.addCell(createCell(s.openedAt() != null ? s.openedAt().format(dtFmt) : "-", NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(s.closedAt() != null ? s.closedAt().format(dtFmt) : "Activa", NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell("S/ " + (s.openingBalance() != null ? s.openingBalance().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_RIGHT));
            detailTable.addCell(createCell("S/ " + (s.diffAmount() != null ? s.diffAmount().toPlainString() : "-"), NORMAL_FONT, Element.ALIGN_RIGHT));

            String statusLabel = "OPEN".equals(s.status()) ? "ABIERTA" : "CERRADA";
            PdfPCell statusCell = createCell(statusLabel, BOLD_FONT, Element.ALIGN_CENTER);
            statusCell.setBackgroundColor("OPEN".equals(s.status()) ? new Color(198, 239, 206) : new Color(242, 242, 242));
            detailTable.addCell(statusCell);
        }
        document.add(detailTable);

        document.close();
        return baos.toByteArray();
    }

    /**
     * PDF: Movimientos de caja en un período.
     */
    public static byte[] generateCashMovementsReport(
            List<com.sergiocodev.app.dto.report.CashMovementReport> movements,
            CompanyResponse company,
            String startDate, String endDate) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("REPORTE DE MOVIMIENTOS DE CAJA", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        addDateHeader(document, startDate, endDate);

        // Summary
        java.math.BigDecimal totalIngresos = movements.stream()
                .filter(m -> "INGRESO".equals(m.type()))
                .map(com.sergiocodev.app.dto.report.CashMovementReport::amount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalEgresos = movements.stream()
                .filter(m -> "EGRESO".equals(m.type()))
                .map(com.sergiocodev.app.dto.report.CashMovementReport::amount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        PdfPTable summaryTable = new PdfPTable(new float[]{4f, 3f, 3f, 3f});
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        PdfPCell sumHeader = new PdfPCell(new Phrase("RESUMEN DE MOVIMIENTOS", HEADER_FONT));
        sumHeader.setColspan(4);
        sumHeader.setBackgroundColor(new Color(33, 37, 41));
        sumHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        sumHeader.setPadding(6);
        summaryTable.addCell(sumHeader);

        addTableHeader(summaryTable, new String[]{"TOTAL MOVIMIENTOS", "TOTAL INGRESOS", "TOTAL EGRESOS", "NETO"});

        java.math.BigDecimal neto = totalIngresos.subtract(totalEgresos);
        summaryTable.addCell(createCell(String.valueOf(movements.size()), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell("S/ " + totalIngresos.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell("S/ " + totalEgresos.toPlainString(), NORMAL_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell("S/ " + neto.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
        document.add(summaryTable);

        // Detail table
        PdfPTable detailTable = new PdfPTable(new float[]{1.5f, 3f, 1.8f, 2f, 2f, 2.5f});
        detailTable.setWidthPercentage(100);
        detailTable.setSpacingBefore(5);

        PdfPCell detHeader = new PdfPCell(new Phrase("DETALLE DE MOVIMIENTOS", HEADER_FONT));
        detHeader.setColspan(6);
        detHeader.setBackgroundColor(new Color(33, 37, 41));
        detHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        detHeader.setPadding(6);
        detailTable.addCell(detHeader);

        addTableHeader(detailTable,
                new String[]{"ID", "CONCEPTO", "TIPO", "MONTO", "USUARIO", "FECHA"},
                new Color(240, 240, 240), BOLD_FONT);

        java.time.format.DateTimeFormatter dtFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        for (com.sergiocodev.app.dto.report.CashMovementReport m : movements) {
            detailTable.addCell(createCell(m.id().toString(), NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(m.conceptName(), NORMAL_FONT, Element.ALIGN_LEFT));

            PdfPCell typeCell = createCell(m.type(), BOLD_FONT, Element.ALIGN_CENTER);
            typeCell.setBackgroundColor("INGRESO".equals(m.type()) ? new Color(198, 239, 206) : new Color(255, 213, 213));
            detailTable.addCell(typeCell);

            detailTable.addCell(createCell("S/ " + (m.amount() != null ? m.amount().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_RIGHT));
            detailTable.addCell(createCell(m.username(), NORMAL_FONT, Element.ALIGN_CENTER));
            detailTable.addCell(createCell(m.createdAt() != null ? m.createdAt().format(dtFmt) : "-", NORMAL_FONT, Element.ALIGN_CENTER));
        }
        document.add(detailTable);

        document.close();
        return baos.toByteArray();
    }

    /**
     * PDF: Arqueo detallado de una sesión de caja.
     */
    public static byte[] generateCashArqueoReport(
            com.sergiocodev.app.dto.report.CashSessionReport session,
            List<com.sergiocodev.app.dto.report.CashMovementReport> movements,
            CompanyResponse company) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        addPageNumbers(writer, document);
        document.open();
        addCompanyHeader(document, company);

        Paragraph title = new Paragraph("ARQUEO DE CAJA", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        java.time.format.DateTimeFormatter dtFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Session header info
        PdfPTable sessionTable = new PdfPTable(new float[]{3f, 3f, 3f, 3f});
        sessionTable.setWidthPercentage(100);
        sessionTable.setSpacingBefore(15);
        sessionTable.setSpacingAfter(20);

        PdfPCell sessionHeader = new PdfPCell(new Phrase("DATOS DE LA SESIÓN #" + session.sessionId(), HEADER_FONT));
        sessionHeader.setColspan(4);
        sessionHeader.setBackgroundColor(new Color(33, 37, 41));
        sessionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        sessionHeader.setPadding(6);
        sessionTable.addCell(sessionHeader);

        addTableHeader(sessionTable, new String[]{"CAJA", "CAJERO", "APERTURA", "CIERRE"}, new Color(240, 240, 240), BOLD_FONT);
        sessionTable.addCell(createCell(session.cashRegisterName(), NORMAL_FONT, Element.ALIGN_CENTER));
        sessionTable.addCell(createCell(session.username(), NORMAL_FONT, Element.ALIGN_CENTER));
        sessionTable.addCell(createCell(session.openedAt() != null ? session.openedAt().format(dtFmt) : "-", NORMAL_FONT, Element.ALIGN_CENTER));
        sessionTable.addCell(createCell(session.closedAt() != null ? session.closedAt().format(dtFmt) : "Sesión Activa", NORMAL_FONT, Element.ALIGN_CENTER));
        document.add(sessionTable);

        // Balance summary
        PdfPTable balanceTable = new PdfPTable(new float[]{4f, 3f});
        balanceTable.setWidthPercentage(60);
        balanceTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        balanceTable.setSpacingBefore(5);
        balanceTable.setSpacingAfter(20);

        PdfPCell balHeader = new PdfPCell(new Phrase("RESUMEN DE SALDOS", HEADER_FONT));
        balHeader.setColspan(2);
        balHeader.setBackgroundColor(new Color(33, 37, 41));
        balHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        balHeader.setPadding(6);
        balanceTable.addCell(balHeader);

        String[][] balanceRows = {
                {"Saldo Inicial", "S/ " + (session.openingBalance() != null ? session.openingBalance().toPlainString() : "0.00")},
                {"Saldo Teórico (calculado)", "S/ " + (session.calculatedBalance() != null ? session.calculatedBalance().toPlainString() : "0.00")},
                {"Saldo Cierre (contado)", "S/ " + (session.closingBalance() != null ? session.closingBalance().toPlainString() : "Pendiente")},
                {"Diferencia", "S/ " + (session.diffAmount() != null ? session.diffAmount().toPlainString() : "0.00")}
        };

        for (String[] row : balanceRows) {
            balanceTable.addCell(createCell(row[0], BOLD_FONT, Element.ALIGN_LEFT));
            balanceTable.addCell(createCell(row[1], NORMAL_FONT, Element.ALIGN_RIGHT));
        }
        document.add(balanceTable);

        // Movements breakdown
        java.math.BigDecimal totalIngresos = movements.stream()
                .filter(m -> "INGRESO".equals(m.type()))
                .map(com.sergiocodev.app.dto.report.CashMovementReport::amount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalEgresos = movements.stream()
                .filter(m -> "EGRESO".equals(m.type()))
                .map(com.sergiocodev.app.dto.report.CashMovementReport::amount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        if (!movements.isEmpty()) {
            PdfPTable movTable = new PdfPTable(new float[]{1.5f, 3f, 1.8f, 2f, 2f, 2.5f});
            movTable.setWidthPercentage(100);
            movTable.setSpacingBefore(5);

            PdfPCell movHeader = new PdfPCell(new Phrase("MOVIMIENTOS DE LA SESIÓN (" + movements.size() + " registros)", HEADER_FONT));
            movHeader.setColspan(6);
            movHeader.setBackgroundColor(new Color(33, 37, 41));
            movHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            movHeader.setPadding(6);
            movTable.addCell(movHeader);

            addTableHeader(movTable, new String[]{"ID", "CONCEPTO", "TIPO", "MONTO", "USUARIO", "FECHA"}, new Color(240, 240, 240), BOLD_FONT);

            for (com.sergiocodev.app.dto.report.CashMovementReport m : movements) {
                movTable.addCell(createCell(m.id().toString(), NORMAL_FONT, Element.ALIGN_CENTER));
                movTable.addCell(createCell(m.conceptName(), NORMAL_FONT, Element.ALIGN_LEFT));

                PdfPCell typeCell = createCell(m.type(), BOLD_FONT, Element.ALIGN_CENTER);
                typeCell.setBackgroundColor("INGRESO".equals(m.type()) ? new Color(198, 239, 206) : new Color(255, 213, 213));
                movTable.addCell(typeCell);

                movTable.addCell(createCell("S/ " + (m.amount() != null ? m.amount().toPlainString() : "0.00"), NORMAL_FONT, Element.ALIGN_RIGHT));
                movTable.addCell(createCell(m.username(), NORMAL_FONT, Element.ALIGN_CENTER));
                movTable.addCell(createCell(m.createdAt() != null ? m.createdAt().format(dtFmt) : "-", NORMAL_FONT, Element.ALIGN_CENTER));
            }

            // Total row
            PdfPCell totalLabel = createCell("TOTALES", BOLD_FONT, Element.ALIGN_CENTER);
            totalLabel.setColspan(2);
            totalLabel.setBackgroundColor(new Color(245, 245, 245));
            movTable.addCell(totalLabel);
            movTable.addCell(createCell("", NORMAL_FONT, Element.ALIGN_CENTER));
            movTable.addCell(createCell("(+) S/ " + totalIngresos.toPlainString() + "  (-) S/ " + totalEgresos.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));
            movTable.addCell(createCell("", NORMAL_FONT, Element.ALIGN_CENTER));
            movTable.addCell(createCell("", NORMAL_FONT, Element.ALIGN_CENTER));

            document.add(movTable);
        }

        document.close();
        return baos.toByteArray();
    }
}
