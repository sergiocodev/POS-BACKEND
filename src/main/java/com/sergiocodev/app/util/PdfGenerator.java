package com.sergiocodev.app.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sergiocodev.app.dto.company.CompanyMinimalResponse;
import com.sergiocodev.app.dto.sale.SaleResponse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Generates sale receipts/invoices as PDF using OpenPDF.
 * Supports three formats: TICKET (80mm), BOLETA/FACTURA (A4), and standard receipt.
 */
public class PdfGenerator {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private PdfGenerator() {} // utility class

    /**
     * Generate a PDF receipt for a sale.
     * @param sale the sale data
     * @param company company information
     * @param format "TICKET" (80mm), "A4" (standard), or "80MM"
     * @return PDF bytes
     */
    public static byte[] generateSalePdf(SaleResponse sale, CompanyMinimalResponse company, String format) {
        try {
            if ("TICKET".equalsIgnoreCase(format) || "80MM".equalsIgnoreCase(format)) {
                return generateTicketPdf(sale, company);
            } else {
                return generateA4Pdf(sale, company);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private static byte[] generateTicketPdf(SaleResponse sale, CompanyMinimalResponse company) throws Exception {
        // 80mm thermal paper: ~302 points wide (80mm = ~227pts printable)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(227f, 99999f), 8, 8, 4, 4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

        // Company header
        if (company != null) {
            addCenteredParagraph(document, company.name(), headerFont);
            addCenteredParagraph(document, "RUC: " + company.ruc(), normalFont);
            if (company.address() != null) {
                addCenteredParagraph(document, company.address(), normalFont);
            }
            addSeparator(document, normalFont);
        }

        // Sale info
        addParagraph(document, "Documento: " + sale.documentType().name(), normalFont);
        addParagraph(document, "Serie: " + sale.series() + "-" + sale.number(), normalFont);
        addParagraph(document, "Fecha: " + sale.date().format(DT_FMT), normalFont);
        addParagraph(document, "Cliente: " + (sale.customerName() != null ? sale.customerName() : "Público en General"), normalFont);
        if (sale.customerDocumentNumber() != null) {
            addParagraph(document, "Doc: " + sale.customerDocumentType() + ": " + sale.customerDocumentNumber(), normalFont);
        }
        addSeparator(document, normalFont);

        // Items
        addParagraph(document, "CANT  DESCRIPCIÓN           P.UNIT  TOTAL", boldFont);
        addSeparator(document, normalFont);
        
        if (sale.items() != null) {
            for (var item : sale.items()) {
                String line = String.format("%-4s  %-20s  %8s  %8s",
                        item.quantity().toPlainString(),
                        truncate(item.productName(), 20),
                        item.unitPrice().toPlainString(),
                        item.amount().toPlainString());
                addParagraph(document, line, normalFont);
            }
        }
        
        addSeparator(document, normalFont);
        addParagraph(document, String.format("Subtotal: %s", sale.subTotal().toPlainString()), boldFont);
        addParagraph(document, String.format("IGV:      %s", sale.tax().toPlainString()), boldFont);
        addParagraph(document, String.format("TOTAL:    %s", sale.total().toPlainString()), totalFont);
        addParagraph(document, "Estado: " + sale.status(), normalFont);
        if (sale.sunatStatus() != null) {
            addParagraph(document, "SUNAT: " + sale.sunatStatus(), normalFont);
        }

        // Footer
        addSeparator(document, normalFont);
        addCenteredParagraph(document, "Gracias por su compra", normalFont);

        document.close();
        return baos.toByteArray();
    }

    private static byte[] generateA4Pdf(SaleResponse sale, CompanyMinimalResponse company) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(64, 64, 64));
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(64, 64, 64));

        // Title
        Paragraph title = new Paragraph("COMPROBANTE DE VENTA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Company info
        if (company != null) {
            PdfPTable companyTable = new PdfPTable(2);
            companyTable.setWidthPercentage(100);
            companyTable.addCell(createCell("Empresa:", headerFont));
            companyTable.addCell(createCell(company.name(), normalFont));
            companyTable.addCell(createCell("RUC:", headerFont));
            companyTable.addCell(createCell(company.ruc(), normalFont));
            if (company.address() != null) {
                companyTable.addCell(createCell("Dirección:", headerFont));
                companyTable.addCell(createCell(company.address(), normalFont));
            }
            document.add(companyTable);
            document.add(Chunk.NEWLINE);
        }

        // Sale info
        PdfPTable saleTable = new PdfPTable(2);
        saleTable.setWidthPercentage(100);
        saleTable.addCell(createCell("Documento:", headerFont));
        saleTable.addCell(createCell(sale.documentType().name(), normalFont));
        saleTable.addCell(createCell("Número:", headerFont));
        saleTable.addCell(createCell(sale.series() + "-" + sale.number(), normalFont));
        saleTable.addCell(createCell("Fecha:", headerFont));
        saleTable.addCell(createCell(sale.date().format(DT_FMT), normalFont));
        saleTable.addCell(createCell("Cliente:", headerFont));
        saleTable.addCell(createCell(sale.customerName() != null ? sale.customerName() : "Público en General", normalFont));
        document.add(saleTable);
        document.add(Chunk.NEWLINE);

        // Items table
        PdfPTable itemsTable = new PdfPTable(new float[]{1, 4, 1.5f, 1.5f, 1.5f});
        itemsTable.setWidthPercentage(100);
        itemsTable.setHeaderRows(1);

        String[] headers = {"Cant", "Descripción", "P. Unit", "Descuento", "Total"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(211, 211, 211));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        if (sale.items() != null) {
            for (var item : sale.items()) {
                itemsTable.addCell(createCell(item.quantity().toPlainString(), normalFont, Element.ALIGN_CENTER));
                itemsTable.addCell(createCell(item.productName(), normalFont));
                itemsTable.addCell(createCell(item.unitPrice().toPlainString(), normalFont, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(
                        item.discountAmount() != null && item.discountAmount().compareTo(BigDecimal.ZERO) > 0 
                                ? item.discountAmount().toPlainString() : "-", 
                        normalFont, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(item.amount().toPlainString(), normalFont, Element.ALIGN_RIGHT));
            }
        }
        document.add(itemsTable);
        document.add(Chunk.NEWLINE);

        // Totals
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.addCell(createCell("Subtotal:", headerFont));
        totalsTable.addCell(createCell(sale.subTotal().toPlainString(), normalFont, Element.ALIGN_RIGHT));
        totalsTable.addCell(createCell("IGV:", headerFont));
        totalsTable.addCell(createCell(sale.tax().toPlainString(), normalFont, Element.ALIGN_RIGHT));
        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        totalCell.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(totalCell);
        PdfPCell totalValueCell = new PdfPCell(new Phrase(sale.total().toPlainString(), totalFont));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(totalValueCell);
        document.add(totalsTable);

        document.close();
        return baos.toByteArray();
    }

    // Helper methods
    private static void addParagraph(Document doc, String text, Font font) throws Exception {
        doc.add(new Paragraph(text, font));
    }

    private static void addCenteredParagraph(Document doc, String text, Font font) throws Exception {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private static void addSeparator(Document doc, Font font) throws Exception {
        addParagraph(doc, "----------------------------------------", font);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 1) + "…" : s;
    }

    private static PdfPCell createCell(String text, Font font) {
        return createCell(text, font, Element.ALIGN_LEFT);
    }

    private static PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(2);
        return cell;
    }
}
