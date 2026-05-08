package com.sergiocodev.app.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sergiocodev.app.dto.company.CompanyResponse;
import com.sergiocodev.app.dto.report.SalesReport;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportPdfGenerator {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

        // Add page numbers
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                Phrase footer = new Phrase("Pg. " + writer.getPageNumber(), NORMAL_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footer, document.right(), document.top() + 10, 0);
            }
        });

        document.open();

        // Company Header
        if (company != null) {
            Paragraph companyName = new Paragraph(company.name(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(companyName);
            document.add(Chunk.NEWLINE);
        }

        // Title
        Paragraph title = new Paragraph("REPORTE POR COMPROBANTE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Dates
        Paragraph dates = new Paragraph("Desde: " + startDate + "          Hasta: " + endDate, BOLD_FONT);
        dates.setAlignment(Element.ALIGN_CENTER);
        dates.setSpacingBefore(10);
        dates.setSpacingAfter(15);
        document.add(dates);

        // Grouping data (Filter out voided sales for the entire report)
        Map<String, List<SalesReport>> salesByType = sales.stream()
                .filter(s -> !s.isVoided())
                .collect(Collectors.groupingBy(s -> formatDocString(s.documentType())));

        // Section 1: Summary
        addSectionTitle(document, "RESUMEN POR COMPROBANTES");

        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(5);
        summaryTable.setSpacingAfter(20);

        addTableHeader(summaryTable, new String[] { "Comprobante", "Cantidad", "Importe" });

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

        // Total row
        PdfPCell totalLabel = createCell("TOTAL", BOLD_FONT, Element.ALIGN_CENTER);
        totalLabel.setColspan(1);
        summaryTable.addCell(totalLabel);
        summaryTable.addCell(createCell(String.valueOf(totalQty), BOLD_FONT, Element.ALIGN_CENTER));
        summaryTable.addCell(createCell(totalAmount.toPlainString(), BOLD_FONT, Element.ALIGN_CENTER));

        document.add(summaryTable);

        // Section 2: Details
        addSectionTitle(document, "VENTAS CONFIRMADAS POR COMPROBANTE");

        for (Map.Entry<String, List<SalesReport>> entry : salesByType.entrySet()) {
            String docType = entry.getKey();
            List<SalesReport> typeSales = entry.getValue();

            if (typeSales.isEmpty())
                continue;

            PdfPTable detailsTable = new PdfPTable(new float[] { 1.5f, 2f, 4f, 1.5f });
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10);
            detailsTable.setSpacingAfter(15);

            // Header with Document Type
            PdfPCell typeCell = new PdfPCell(new Phrase(docType, HEADER_FONT));
            typeCell.setColspan(4);
            typeCell.setBackgroundColor(new Color(33, 37, 41));
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            typeCell.setPadding(5);
            detailsTable.addCell(typeCell);

            addTableHeader(detailsTable, new String[] { "Fecha", "Documento", "Cliente", "Importe" },
                    new Color(240, 240, 240), BOLD_FONT);

            for (SalesReport sale : typeSales) {
                detailsTable.addCell(createCell(sale.date().format(DT_FMT), NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(sale.documentNumber(), NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable
                        .addCell(createCell(sale.customerName() != null ? sale.customerName() : "Público en General",
                                NORMAL_FONT, Element.ALIGN_CENTER));
                detailsTable.addCell(createCell(sale.total().toPlainString(), NORMAL_FONT, Element.ALIGN_RIGHT));
            }

            document.add(detailsTable);
        }

        document.close();
        return baos.toByteArray();
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
        cell.setBackgroundColor(new Color(108, 117, 125)); // Gray secondary
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
}
