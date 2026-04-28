package com.sergiocodev.app.util;

import com.sergiocodev.app.model.Company;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.VoidedDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class XmlUblGenerator {

    public String generateInvoiceXml(Sale sale, Company company) {
        // Placeholder for UBL 2.1 XML generation
        // specific to SUNAT requirements (Invoice/Boleta)
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" ");
        xml.append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ");
        xml.append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ");
        xml.append("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" ");
        xml.append("xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">");

        // Add UBL extensions (signature placeholder)
        xml.append("<ext:UBLExtensions>");
        xml.append("<ext:UBLExtension>");
        xml.append("<ext:ExtensionContent>");
        xml.append("</ext:ExtensionContent>");
        xml.append("</ext:UBLExtension>");
        xml.append("</ext:UBLExtensions>");

        // Basic Invoice Data
        xml.append("<cbc:UBLVersionID>2.1</cbc:UBLVersionID>");
        xml.append("<cbc:CustomizationID>2.0</cbc:CustomizationID>");
        xml.append("<cbc:ID>").append(sale.getSeries()).append("-").append(sale.getNumber())
                .append("</cbc:ID>");
        xml.append("<cbc:IssueDate>").append(sale.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .append("</cbc:IssueDate>");
        xml.append("<cbc:IssueTime>").append(sale.getDate().format(DateTimeFormatter.ISO_LOCAL_TIME))
                .append("</cbc:IssueTime>");
        
        String invoiceTypeCode = sale.getDocumentType() == Sale.SaleDocumentType.FACTURA ? "01" : "03";
        xml.append("<cbc:InvoiceTypeCode listAgencyName=\"PE:SUNAT\" listName=\"Tipo de Documento\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01\">")
                .append(invoiceTypeCode).append("</cbc:InvoiceTypeCode>");

        // Emisor (Supplier)
        xml.append("<cac:AccountingSupplierParty>");
        xml.append("<cac:Party>");
        xml.append("<cac:PartyIdentification>");
        xml.append(
                "<cbc:ID schemeID=\"6\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                .append(company.getRuc()).append("</cbc:ID>");
        xml.append("</cac:PartyIdentification>");
        xml.append("<cac:PartyName><cbc:Name>").append(company.getName())
                .append("</cbc:Name></cac:PartyName>");
        
        // Address Info
        xml.append("<cac:PartyLegalEntity>");
        xml.append("<cbc:RegistrationName>").append(company.getName()).append("</cbc:RegistrationName>");
        xml.append("<cac:RegistrationAddress>");
        xml.append("<cbc:AddressTypeCode>").append(sale.getEstablishment().getCodeSunat()).append("</cbc:AddressTypeCode>");
        xml.append("<cbc:CitySubdivisionName>").append(company.getUrbanization() != null ? company.getUrbanization() : "-").append("</cbc:CitySubdivisionName>");
        xml.append("<cbc:CityName>Lima</cbc:CityName>"); // Should be dynamic based on ubigeo
        xml.append("<cbc:CountrySubentity>Lima</cbc:CountrySubentity>"); // Should be dynamic based on ubigeo
        xml.append("<cbc:District>Lima</cbc:District>"); // Should be dynamic based on ubigeo
        xml.append("<cac:AddressLine><cbc:Line>").append(company.getAddress()).append("</cbc:Line></cac:AddressLine>");
        xml.append("<cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country>");
        xml.append("</cac:RegistrationAddress>");
        xml.append("</cac:PartyLegalEntity>");

        xml.append("</cac:Party>");
        xml.append("</cac:AccountingSupplierParty>");

        // Receptor (Customer)
        xml.append("<cac:AccountingCustomerParty>");
        xml.append("<cac:Party>");
        xml.append("<cac:PartyIdentification>");
        
        String customerDocType = "1"; // DNI by default
        if (sale.getCustomer() != null && sale.getCustomer().getDocumentType() == com.sergiocodev.app.model.DocumentType.RUC) {
            customerDocType = "6";
        }

        xml.append(
                "<cbc:ID schemeID=\"").append(customerDocType).append("\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                .append(sale.getCustomer() != null ? sale.getCustomer().getDocumentNumber()
                        : "00000000")
                .append("</cbc:ID>");
        xml.append("</cac:PartyIdentification>");
        xml.append("<cac:PartyLegalEntity><cbc:RegistrationName>")
                .append(sale.getCustomer() != null ? sale.getCustomer().getName() : "CLIENTE GENERICO")
                .append("</cbc:RegistrationName></cac:PartyLegalEntity>");
        xml.append("</cac:Party>");
        xml.append("</cac:AccountingCustomerParty>");

        // Payment Terms
        xml.append("<cac:PaymentTerms>");
        xml.append("<cbc:ID>FormaPago</cbc:ID>");
        xml.append("<cbc:PaymentMeansID>").append(sale.getPaymentCondition() == Sale.PaymentCondition.CREDITO ? "Credito" : "Contado").append("</cbc:PaymentMeansID>");
        if (sale.getPaymentCondition() == Sale.PaymentCondition.CONTADO) {
            xml.append("<cbc:Amount currencyID=\"PEN\">").append(sale.getTotal()).append("</cbc:Amount>");
        }
        xml.append("</cac:PaymentTerms>");

        // Totals
        xml.append("<cac:LegalMonetaryTotal>");
        xml.append("<cbc:PayableAmount currencyID=\"PEN\">").append(sale.getTotal())
                .append("</cbc:PayableAmount>");
        xml.append("</cac:LegalMonetaryTotal>");

        // Invoice Lines
        java.util.concurrent.atomic.AtomicInteger lineIndex = new java.util.concurrent.atomic.AtomicInteger(1);
        sale.getItems().forEach(item -> {
            xml.append("<cac:InvoiceLine>");
            xml.append("<cbc:ID>").append(lineIndex.getAndIncrement()).append("</cbc:ID>");
            xml.append("<cbc:InvoicedQuantity unitCode=\"NIU\">").append(item.getQuantity())
                    .append("</cbc:InvoicedQuantity>");
            xml.append("<cbc:LineExtensionAmount currencyID=\"PEN\">").append(item.getAmount())
                    .append("</cbc:LineExtensionAmount>");
            xml.append("<cac:Item>");
            xml.append("<cbc:Description>")
                .append(item.getLot() != null && item.getLot().getProduct() != null ? item.getLot().getProduct().getTradeName() : "Producto")
                .append("</cbc:Description>");
            xml.append("</cac:Item>");
            xml.append("<cac:Price>");
            java.math.BigDecimal effectivePrice = item.getAmount().divide(item.getQuantity(), 2, java.math.RoundingMode.HALF_UP);
            xml.append("<cbc:PriceAmount currencyID=\"PEN\">").append(effectivePrice)
                    .append("</cbc:PriceAmount>");
            xml.append("</cac:Price>");
            xml.append("</cac:InvoiceLine>");
        });

        xml.append("</Invoice>");
        return xml.toString();
    }

    public String generateVoidedDocumentXml(VoidedDocument voidedDoc, Company company) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        xml.append("<VoidedDocuments xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:VoidedDocuments-2\">");
        xml.append("<cbc:ID>").append(voidedDoc.getTicketSunat()).append("</cbc:ID>");
        xml.append("<cbc:IssueDate>").append(voidedDoc.getIssueDate()).append("</cbc:IssueDate>");
        
        // Emisor details would go here too
        
        xml.append("</VoidedDocuments>");
        return xml.toString();
    }
}
