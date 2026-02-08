package com.sergiocodev.app.util;

import com.sergiocodev.app.config.SunatConfig;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.VoidedDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class XmlUblGenerator {

        private final SunatConfig sunatConfig;

        public String generateInvoiceXml(Sale sale) {
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
                xml.append(
                                "<cbc:InvoiceTypeCode listAgencyName=\"PE:SUNAT\" listName=\"Tipo de Documento\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01\">01</cbc:InvoiceTypeCode>");

                // Emisor (Supplier)
                xml.append("<cac:AccountingSupplierParty>");
                xml.append("<cac:Party>");
                xml.append("<cac:PartyIdentification>");
                xml.append(
                                "<cbc:ID schemeID=\"6\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                                .append(sunatConfig.getRuc()).append("</cbc:ID>");
                xml.append("</cac:PartyIdentification>");
                xml.append("<cac:PartyName><cbc:Name>").append(sunatConfig.getRazonSocial())
                                .append("</cbc:Name></cac:PartyName>");
                xml.append("</cac:Party>");
                xml.append("</cac:AccountingSupplierParty>");

                // Receptor (Customer)
                xml.append("<cac:AccountingCustomerParty>");
                xml.append("<cac:Party>");
                xml.append("<cac:PartyIdentification>");
                xml.append(
                                "<cbc:ID schemeID=\"1\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                                .append(sale.getCustomer() != null ? sale.getCustomer().getDocumentNumber()
                                                : "00000000")
                                .append("</cbc:ID>");
                xml.append("</cac:PartyIdentification>");
                xml.append("<cac:PartyLegalEntity><cbc:RegistrationName>")
                                .append(sale.getCustomer() != null ? sale.getCustomer().getName() : "CLIENTE GENERICO")
                                .append("</cbc:RegistrationName></cac:PartyLegalEntity>");
                xml.append("</cac:Party>");
                xml.append("</cac:AccountingCustomerParty>");

                // Totals
                xml.append("<cac:LegalMonetaryTotal>");
                xml.append("<cbc:PayableAmount currencyID=\"PEN\">").append(sale.getTotal())
                                .append("</cbc:PayableAmount>");
                xml.append("</cac:LegalMonetaryTotal>");

                // Invoice Lines
                sale.getItems().forEach(item -> {
                        xml.append("<cac:InvoiceLine>");
                        xml.append("<cbc:ID>").append(item.getId()).append("</cbc:ID>");
                        xml.append("<cbc:InvoicedQuantity unitCode=\"NIU\">").append(item.getQuantity())
                                        .append("</cbc:InvoicedQuantity>");
                        xml.append("<cbc:LineExtensionAmount currencyID=\"PEN\">").append(item.getAmount())
                                        .append("</cbc:LineExtensionAmount>");
                        xml.append("<cac:Item>");
                        xml.append("<cbc:Description>").append(item.getLot().getProduct().getTradeName())
                                        .append("</cbc:Description>");
                        xml.append("</cac:Item>");
                        xml.append("<cac:Price>");
                        xml.append("<cbc:PriceAmount currencyID=\"PEN\">").append(item.getUnitPrice())
                                        .append("</cbc:PriceAmount>");
                        xml.append("</cac:Price>");
                        xml.append("</cac:InvoiceLine>");
                });

                xml.append("</Invoice>");
                return xml.toString();
        }

        public String generateVoidedDocumentXml(VoidedDocument voidedDoc) {
                // Placeholder for VoidedDocuments XML (Communication of cancellations)
                StringBuilder xml = new StringBuilder();
                xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                xml.append("<VoidedDocuments xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:VoidedDocuments-2\" ...>");
                // Implementation details...
                xml.append("<cbc:ID>").append(voidedDoc.getTicketSunat()).append("</cbc:ID>");
                xml.append("<cbc:IssueDate>").append(voidedDoc.getIssueDate()).append("</cbc:IssueDate>");

                xml.append("</VoidedDocuments>");
                return xml.toString();
        }
}
