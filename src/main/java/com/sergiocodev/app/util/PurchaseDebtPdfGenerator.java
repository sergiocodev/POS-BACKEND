package com.sergiocodev.app.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sergiocodev.app.dto.report.PurchaseDebtReport;
import com.sergiocodev.app.dto.company.CompanyResponse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PurchaseDebtPdfGenerator {

    private static final Font COMPANY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0, 51, 153)); // Blue Title like sales
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    
    private static final Font SECTION_TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
    private static final Font SUBHEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    
    private static final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font BOLD_DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    
    private static final Color SECTION_BG = new Color(134, 142, 150); // Light Gray
    private static final Color SUBHEADER_BG = new Color(33, 37, 41); // Dark Gray / Black
    private static final Color BORDER_COLOR = Color.BLACK;

    public static byte[] generateDebtReport(List<PurchaseDebtReport> debts, CompanyResponse company, String startDate, String endDate) throws Exception {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        
        // Add Page Numbers
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                Phrase footer = new Phrase("Pg. " + writer.getPageNumber(), DATA_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footer, document.right(), document.top() + 10, 0);
            }
        });

        document.open();

        // Company Name Header (Top Left)
        if (company != null) {
            Paragraph comp = new Paragraph(company.name(), COMPANY_FONT);
            comp.setAlignment(Element.ALIGN_LEFT);
            comp.setSpacingAfter(20);
            document.add(comp);
        } else {
            document.add(new Paragraph("\n"));
        }

        // Title
        Paragraph title = new Paragraph("ESTADO DE PAGOS (DEUDAS)", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        // Date Range
        Paragraph dateRange = new Paragraph("DESDE: " + startDate + "        HASTA: " + endDate, SUBTITLE_FONT);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        dateRange.setSpacingAfter(20);
        document.add(dateRange);

        // Agrupamos la data
        List<PurchaseDebtReport> cancelados = debts.stream().filter(d -> "CANCELADO".equals(d.reportGroup())).toList();
        List<PurchaseDebtReport> creditos = debts.stream().filter(d -> "CREDITO".equals(d.reportGroup())).toList();
        List<PurchaseDebtReport> vencidos = debts.stream().filter(d -> "VENCIDO".equals(d.reportGroup())).toList();

        BigDecimal totalCancelado = cancelados.stream().map(PurchaseDebtReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredito = creditos.stream().map(PurchaseDebtReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVencido = vencidos.stream().map(PurchaseDebtReport::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGeneral = totalCancelado.add(totalCredito).add(totalVencido);

        // SECTION 1: RESUMEN POR ESTADO
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);
        
        // Header Section
        PdfPCell summaryHeaderCell = new PdfPCell(new Phrase("RESUMEN POR ESTADO", SECTION_TITLE_FONT));
        summaryHeaderCell.setBackgroundColor(SECTION_BG);
        summaryHeaderCell.setColspan(3);
        summaryHeaderCell.setPadding(8);
        summaryHeaderCell.setBorderColor(BORDER_COLOR);
        summaryTable.addCell(summaryHeaderCell);

        // Columns
        addTableHeader(summaryTable, "ESTADO");
        addTableHeader(summaryTable, "CANTIDAD");
        addTableHeader(summaryTable, "TOTAL (S/)");

        // Rows
        addSummaryRow(summaryTable, "CANCELADO", cancelados.size(), totalCancelado);
        addSummaryRow(summaryTable, "CRÉDITO", creditos.size(), totalCredito);
        addSummaryRow(summaryTable, "VENCIDO", vencidos.size(), totalVencido);
        
        // Total Row
        PdfPCell totalLblCell = new PdfPCell(new Phrase("TOTAL GENERAL", BOLD_DATA_FONT));
        totalLblCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalLblCell.setPadding(6);
        summaryTable.addCell(totalLblCell);
        
        PdfPCell totalQtyCell = new PdfPCell(new Phrase(String.valueOf(debts.size()), BOLD_DATA_FONT));
        totalQtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalQtyCell.setPadding(6);
        summaryTable.addCell(totalQtyCell);

        PdfPCell totalAmtCell = new PdfPCell(new Phrase(String.format("%.2f", totalGeneral), BOLD_DATA_FONT));
        totalAmtCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalAmtCell.setPadding(6);
        summaryTable.addCell(totalAmtCell);

        document.add(summaryTable);

        // SECTION 2: DETALLES POR ESTADO
        PdfPTable detailsMainTable = new PdfPTable(1);
        detailsMainTable.setWidthPercentage(100);
        detailsMainTable.setSpacingAfter(10);
        
        PdfPCell detailsHeaderCell = new PdfPCell(new Phrase("COMPRAS POR ESTADO", SECTION_TITLE_FONT));
        detailsHeaderCell.setBackgroundColor(SECTION_BG);
        detailsHeaderCell.setPadding(8);
        detailsHeaderCell.setBorderColor(BORDER_COLOR);
        detailsMainTable.addCell(detailsHeaderCell);
        document.add(detailsMainTable);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 1. CANCELADO
        if (!cancelados.isEmpty()) {
            PdfPTable t1 = new PdfPTable(5);
            t1.setWidthPercentage(100);
            t1.setSpacingAfter(15);
            t1.setWidths(new float[]{2f, 3f, 4f, 2f, 2f});
            
            addGroupHeader(t1, "CANCELADO", 5);
            addTableHeader(t1, "FECHA");
            addTableHeader(t1, "DOCUMENTO");
            addTableHeader(t1, "PROVEEDOR");
            addTableHeader(t1, "F. PAGO");
            addTableHeader(t1, "TOTAL");
            
            for (PurchaseDebtReport d : cancelados) {
                t1.addCell(createCell(d.issueDate() != null ? d.issueDate().format(dtf) : ""));
                t1.addCell(createCell(d.documentType() + " " + d.documentNumber()));
                t1.addCell(createCell(d.supplierName()));
                t1.addCell(createCell(d.paymentDate() != null ? d.paymentDate().format(dtf) : ""));
                t1.addCell(createCell(String.format("%.2f", d.total())));
            }
            document.add(t1);
        }

        // 2. CREDITO
        if (!creditos.isEmpty()) {
            PdfPTable t2 = new PdfPTable(6);
            t2.setWidthPercentage(100);
            t2.setSpacingAfter(15);
            t2.setWidths(new float[]{2f, 3f, 4f, 2f, 2f, 2f});
            
            addGroupHeader(t2, "CRÉDITO", 6);
            addTableHeader(t2, "FECHA");
            addTableHeader(t2, "DOCUMENTO");
            addTableHeader(t2, "PROVEEDOR");
            addTableHeader(t2, "VENCE");
            addTableHeader(t2, "TOTAL");
            addTableHeader(t2, "PENDIENTE");
            
            for (PurchaseDebtReport d : creditos) {
                t2.addCell(createCell(d.issueDate() != null ? d.issueDate().format(dtf) : ""));
                t2.addCell(createCell(d.documentType() + " " + d.documentNumber()));
                t2.addCell(createCell(d.supplierName()));
                t2.addCell(createCell(d.dueDate() != null ? d.dueDate().format(dtf) : ""));
                t2.addCell(createCell(String.format("%.2f", d.total())));
                t2.addCell(createCell(String.format("%.2f", d.pending())));
            }
            document.add(t2);
        }

        // 3. VENCIDO
        if (!vencidos.isEmpty()) {
            PdfPTable t3 = new PdfPTable(6);
            t3.setWidthPercentage(100);
            t3.setSpacingAfter(15);
            t3.setWidths(new float[]{2f, 3f, 4f, 2f, 2f, 2f});
            
            addGroupHeader(t3, "VENCIDO", 6);
            addTableHeader(t3, "FECHA");
            addTableHeader(t3, "DOCUMENTO");
            addTableHeader(t3, "PROVEEDOR");
            addTableHeader(t3, "MORA (Días)");
            addTableHeader(t3, "TOTAL");
            addTableHeader(t3, "PENDIENTE");
            
            for (PurchaseDebtReport d : vencidos) {
                t3.addCell(createCell(d.issueDate() != null ? d.issueDate().format(dtf) : ""));
                t3.addCell(createCell(d.documentType() + " " + d.documentNumber()));
                t3.addCell(createCell(d.supplierName()));
                t3.addCell(createCell(String.valueOf(d.overdueDays())));
                t3.addCell(createCell(String.format("%.2f", d.total())));
                t3.addCell(createCell(String.format("%.2f", d.pending())));
            }
            document.add(t3);
        }

        document.close();
        return out.toByteArray();
    }

    private static void addSummaryRow(PdfPTable table, String state, int quantity, BigDecimal amount) {
        PdfPCell c1 = new PdfPCell(new Phrase(state, DATA_FONT));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setPadding(6);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(quantity), DATA_FONT));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setPadding(6);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(String.format("%.2f", amount), DATA_FONT));
        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
        c3.setPadding(6);
        table.addCell(c3);
    }

    private static void addGroupHeader(PdfPTable table, String groupName, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(groupName, SUBHEADER_FONT));
        cell.setBackgroundColor(SUBHEADER_BG);
        cell.setColspan(colspan);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private static void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header, TABLE_HEADER_FONT));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private static PdfPCell createCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "", DATA_FONT));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }
}
