package com.sergiocodev.app.util;

import com.sergiocodev.app.model.Company;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.model.DocumentType;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.model.VoidedDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class XmlUblGenerator {

    /**
     * Genera el XML UBL 2.1 para Facturas y Boletas de Venta.
     */
    public String generateInvoiceXml(Sale sale, Company company) {
        validateSale(sale, company);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" ");
        xml.append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ");
        xml.append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ");
        xml.append("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" ");
        xml.append("xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">\n");

        // 14. Firma digital (Placeholder para XMLDSig)
        xml.append("    <ext:UBLExtensions>\n");
        xml.append("        <ext:UBLExtension>\n");
        xml.append("            <ext:ExtensionContent></ext:ExtensionContent>\n");
        xml.append("        </ext:UBLExtension>\n");
        xml.append("    </ext:UBLExtensions>\n");

        // 1. Encabezado
        xml.append("    <cbc:UBLVersionID>2.1</cbc:UBLVersionID>\n");
        xml.append("    <cbc:CustomizationID schemeAgencyName=\"PE:SUNAT\">2.0</cbc:CustomizationID>\n");
        xml.append("    <cbc:ID>").append(sale.getSeries()).append("-").append(sale.getNumber()).append("</cbc:ID>\n");
        xml.append("    <cbc:IssueDate>").append(sale.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</cbc:IssueDate>\n");
        xml.append("    <cbc:IssueTime>").append(sale.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("</cbc:IssueTime>\n");

        String invoiceTypeCode = sale.getDocumentType() == Sale.SaleDocumentType.FACTURA ? "01" : "03";
        // El tipo de operación (Catálogo 51, p.e. 0101 Venta Interna) debe ir en el listID de InvoiceTypeCode
        xml.append("    <cbc:InvoiceTypeCode listID=\"0101\" listAgencyName=\"PE:SUNAT\" listName=\"Tipo de Documento\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01\">")
                .append(invoiceTypeCode).append("</cbc:InvoiceTypeCode>\n");
        xml.append("    <cbc:DocumentCurrencyCode listID=\"ISO 4217 Alpha\" listName=\"Currency\" listAgencyName=\"United Nations Economic Commission for Europe\">PEN</cbc:DocumentCurrencyCode>\n");

        // 2. Identificación del Emisor (Supplier)
        xml.append("    <cac:AccountingSupplierParty>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyIdentification>\n");
        xml.append("                <cbc:ID schemeID=\"6\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                .append(company.getRuc()).append("</cbc:ID>\n");
        xml.append("            </cac:PartyIdentification>\n");
        xml.append("            <cac:PartyName>\n");
        xml.append("                <cbc:Name>").append(cdata(company.getName())).append("</cbc:Name>\n");
        xml.append("            </cac:PartyName>\n");
        xml.append("            <cac:PartyLegalEntity>\n");
        xml.append("                <cbc:RegistrationName>").append(cdata(company.getName())).append("</cbc:RegistrationName>\n");
        xml.append("                <cac:RegistrationAddress>\n");
        xml.append("                    <cbc:ID>").append(escapeXml(company.getUbigeo() != null ? company.getUbigeo() : "150101")).append("</cbc:ID>\n");
        xml.append("                    <cbc:AddressTypeCode>0000</cbc:AddressTypeCode>\n");
        xml.append("                    <cbc:CityName>").append(cdata(company.getCity() != null ? company.getCity() : "LIMA")).append("</cbc:CityName>\n");
        xml.append("                    <cbc:CountrySubentity>").append(cdata(company.getDepartment() != null ? company.getDepartment() : "LIMA")).append("</cbc:CountrySubentity>\n");
        xml.append("                    <cbc:District>").append(cdata(company.getDistrict() != null ? company.getDistrict() : "LIMA")).append("</cbc:District>\n");
        xml.append("                    <cac:AddressLine><cbc:Line>").append(cdata(company.getAddress())).append("</cbc:Line></cac:AddressLine>\n");
        xml.append("                    <cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country>\n");
        xml.append("                </cac:RegistrationAddress>\n");
        xml.append("            </cac:PartyLegalEntity>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingSupplierParty>\n");

        // 3. Identificación del Receptor (Customer)
        String customerDocType = getCustomerDocTypeCode(sale.getCustomer(), sale.getDocumentType());
        String customerDocNumber = getCustomerDocNumber(sale.getCustomer(), sale.getDocumentType());
        String customerName = getCustomerName(sale.getCustomer());

        xml.append("    <cac:AccountingCustomerParty>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyIdentification>\n");
        xml.append("                <cbc:ID schemeID=\"").append(customerDocType).append("\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                .append(escapeXml(customerDocNumber)).append("</cbc:ID>\n");
        xml.append("            </cac:PartyIdentification>\n");
        xml.append("            <cac:PartyLegalEntity>\n");
        xml.append("                <cbc:RegistrationName>").append(cdata(customerName)).append("</cbc:RegistrationName>\n");
        if (sale.getCustomer() != null && sale.getCustomer().getAddress() != null && !sale.getCustomer().getAddress().isEmpty()) {
             xml.append("                <cac:RegistrationAddress>\n");
             xml.append("                    <cac:AddressLine><cbc:Line>").append(cdata(sale.getCustomer().getAddress())).append("</cbc:Line></cac:AddressLine>\n");
             xml.append("                </cac:RegistrationAddress>\n");
        }
        xml.append("            </cac:PartyLegalEntity>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingCustomerParty>\n");

        // 13. Forma y Condición de Pago
        boolean isCredito = sale.getPaymentCondition() == Sale.PaymentCondition.CREDITO;
        xml.append("    <cac:PaymentTerms>\n");
        xml.append("        <cbc:ID>FormaPago</cbc:ID>\n");
        xml.append("        <cbc:PaymentMeansID>").append(isCredito ? "Credito" : "Contado").append("</cbc:PaymentMeansID>\n");
        if (isCredito) {
            xml.append("        <cbc:Amount currencyID=\"PEN\">").append(sale.getTotal().setScale(2, RoundingMode.HALF_UP)).append("</cbc:Amount>\n");
            // Para Crédito, SUNAT pide adicionalmente cuotas. Para un caso genérico, si no hay detalle de cuotas,
            // agregamos una única cuota al final de la vida de la factura a modo de ejemplo. 
            // Si el modelo maneja Payment, se debe iterar ahí.
            xml.append("    </cac:PaymentTerms>\n");
            xml.append("    <cac:PaymentTerms>\n");
            xml.append("        <cbc:ID>Cuota001</cbc:ID>\n");
            xml.append("        <cbc:PaymentMeansID>Cuota001</cbc:PaymentMeansID>\n");
            xml.append("        <cbc:Amount currencyID=\"PEN\">").append(sale.getTotal().setScale(2, RoundingMode.HALF_UP)).append("</cbc:Amount>\n");
            xml.append("        <cbc:PaymentDueDate>").append(sale.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</cbc:PaymentDueDate>\n");
        }
        xml.append("    </cac:PaymentTerms>\n");

        // Preparar cálculos de impuestos globales
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalTaxableAmountS = BigDecimal.ZERO; // Gravadas
        BigDecimal totalTaxAmountS = BigDecimal.ZERO;
        BigDecimal totalTaxableAmountE = BigDecimal.ZERO; // Exoneradas
        BigDecimal totalTaxableAmountO = BigDecimal.ZERO; // Inafectas

        // Calcular primero los totales para el bloque global
        for (SaleItem item : sale.getItems()) {
            BigDecimal qty = item.getQuantity();
            BigDecimal unitPriceIncIgv = item.getUnitPrice();
            
            String codeSunat = (item.getProduct().getTaxType() != null) ? item.getProduct().getTaxType().getCodeSunat() : "10";
            boolean isGravado = codeSunat.equals("10"); // Simplificado
            
            BigDecimal rate = isGravado ? new BigDecimal("0.18") : BigDecimal.ZERO;
            BigDecimal divisor = BigDecimal.ONE.add(rate); // 1.18 o 1.00
            
            BigDecimal unitPriceExcIgv = unitPriceIncIgv.divide(divisor, 5, RoundingMode.HALF_UP);
            BigDecimal lineExtensionAmount = unitPriceExcIgv.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            
            // Impuesto exacto por línea
            BigDecimal lineTax = lineExtensionAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            
            totalTaxAmount = totalTaxAmount.add(lineTax);
            
            String taxCategory = getTaxCategory(codeSunat);
            if (taxCategory.equals("S")) {
                totalTaxableAmountS = totalTaxableAmountS.add(lineExtensionAmount);
                totalTaxAmountS = totalTaxAmountS.add(lineTax);
            } else if (taxCategory.equals("E")) {
                totalTaxableAmountE = totalTaxableAmountE.add(lineExtensionAmount);
            } else if (taxCategory.equals("O")) {
                totalTaxableAmountO = totalTaxableAmountO.add(lineExtensionAmount);
            }
        }

        // 7. Impuestos globales (TaxTotal)
        xml.append("    <cac:TaxTotal>\n");
        xml.append("        <cbc:TaxAmount currencyID=\"PEN\">").append(totalTaxAmount).append("</cbc:TaxAmount>\n");
        
        if (totalTaxableAmountS.compareTo(BigDecimal.ZERO) > 0) {
            appendTaxSubtotal(xml, totalTaxableAmountS, totalTaxAmountS, "S", "1000", "IGV", "VAT");
        }
        if (totalTaxableAmountE.compareTo(BigDecimal.ZERO) > 0) {
            appendTaxSubtotal(xml, totalTaxableAmountE, BigDecimal.ZERO, "E", "9997", "EXO", "VAT");
        }
        if (totalTaxableAmountO.compareTo(BigDecimal.ZERO) > 0) {
            appendTaxSubtotal(xml, totalTaxableAmountO, BigDecimal.ZERO, "O", "9998", "INA", "FRE");
        }
        xml.append("    </cac:TaxTotal>\n");

        // 12. Legal Monetary Total
        BigDecimal lineExtensionAmountGlobal = totalTaxableAmountS.add(totalTaxableAmountE).add(totalTaxableAmountO);
        BigDecimal taxInclusiveAmountGlobal = lineExtensionAmountGlobal.add(totalTaxAmount);
        
        xml.append("    <cac:LegalMonetaryTotal>\n");
        xml.append("        <cbc:LineExtensionAmount currencyID=\"PEN\">").append(lineExtensionAmountGlobal).append("</cbc:LineExtensionAmount>\n");
        xml.append("        <cbc:TaxInclusiveAmount currencyID=\"PEN\">").append(taxInclusiveAmountGlobal).append("</cbc:TaxInclusiveAmount>\n");
        xml.append("        <cbc:PayableAmount currencyID=\"PEN\">").append(taxInclusiveAmountGlobal).append("</cbc:PayableAmount>\n");
        xml.append("    </cac:LegalMonetaryTotal>\n");

        // 9. Invoice Lines
        AtomicInteger lineIndex = new AtomicInteger(1);
        for (SaleItem item : sale.getItems()) {
            BigDecimal qty = item.getQuantity();
            BigDecimal unitPriceIncIgv = item.getUnitPrice(); 
            
            String codeSunat = (item.getProduct().getTaxType() != null) ? item.getProduct().getTaxType().getCodeSunat() : "10";
            boolean isGravado = codeSunat.equals("10");
            
            BigDecimal rate = isGravado ? new BigDecimal("0.18") : BigDecimal.ZERO;
            BigDecimal divisor = BigDecimal.ONE.add(rate); // 1.18 o 1.00
            
            BigDecimal unitPriceExcIgv = unitPriceIncIgv.divide(divisor, 5, RoundingMode.HALF_UP);
            BigDecimal lineExtensionAmount = unitPriceExcIgv.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTax = lineExtensionAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            
            String taxCatId = getTaxCategory(codeSunat);
            String taxSchemeId = getTaxSchemeId(codeSunat);
            String taxSchemeName = getTaxSchemeName(codeSunat);
            String taxSchemeTypeCode = getTaxTypeCode(codeSunat);

            xml.append("    <cac:InvoiceLine>\n");
            xml.append("        <cbc:ID>").append(lineIndex.getAndIncrement()).append("</cbc:ID>\n");
            xml.append("        <cbc:InvoicedQuantity unitCode=\"NIU\">").append(qty).append("</cbc:InvoicedQuantity>\n");
            xml.append("        <cbc:LineExtensionAmount currencyID=\"PEN\">").append(lineExtensionAmount).append("</cbc:LineExtensionAmount>\n");
            
            // 11. Pricing Reference
            xml.append("        <cac:PricingReference>\n");
            xml.append("            <cac:AlternativeConditionPrice>\n");
            xml.append("                <cbc:PriceAmount currencyID=\"PEN\">").append(unitPriceIncIgv.setScale(2, RoundingMode.HALF_UP)).append("</cbc:PriceAmount>\n");
            xml.append("                <cbc:PriceTypeCode listName=\"Tipo de Precio\" listAgencyName=\"PE:SUNAT\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16\">01</cbc:PriceTypeCode>\n");
            xml.append("            </cac:AlternativeConditionPrice>\n");
            xml.append("        </cac:PricingReference>\n");

            // Impuesto por Línea
            xml.append("        <cac:TaxTotal>\n");
            xml.append("            <cbc:TaxAmount currencyID=\"PEN\">").append(lineTax).append("</cbc:TaxAmount>\n");
            xml.append("            <cac:TaxSubtotal>\n");
            xml.append("                <cbc:TaxableAmount currencyID=\"PEN\">").append(lineExtensionAmount).append("</cbc:TaxableAmount>\n");
            xml.append("                <cbc:TaxAmount currencyID=\"PEN\">").append(lineTax).append("</cbc:TaxAmount>\n");
            xml.append("                <cac:TaxCategory>\n");
            xml.append("                    <cbc:ID schemeID=\"UN/ECE 5305\" schemeName=\"Tax Category Identifier\" schemeAgencyName=\"United Nations Economic Commission for Europe\">").append(taxCatId).append("</cbc:ID>\n");
            xml.append("                    <cbc:Percent>").append(rate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)).append("</cbc:Percent>\n");
            xml.append("                    <cbc:TaxExemptionReasonCode listAgencyName=\"PE:SUNAT\" listName=\"Afectacion del IGV\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07\">").append(escapeXml(codeSunat)).append("</cbc:TaxExemptionReasonCode>\n");
            xml.append("                    <cac:TaxScheme>\n");
            xml.append("                        <cbc:ID schemeID=\"UN/ECE 5153\" schemeName=\"Codigo de tributos\" schemeAgencyName=\"PE:SUNAT\">").append(taxSchemeId).append("</cbc:ID>\n");
            xml.append("                        <cbc:Name>").append(taxSchemeName).append("</cbc:Name>\n");
            xml.append("                        <cbc:TaxTypeCode>").append(taxSchemeTypeCode).append("</cbc:TaxTypeCode>\n");
            xml.append("                    </cac:TaxScheme>\n");
            xml.append("                </cac:TaxCategory>\n");
            xml.append("            </cac:TaxSubtotal>\n");
            xml.append("        </cac:TaxTotal>\n");

            xml.append("        <cac:Item>\n");
            String prodDesc = (item.getProduct() != null) ? item.getProduct().getTradeName() : "Producto";
            xml.append("            <cbc:Description>").append(cdata(prodDesc)).append("</cbc:Description>\n");
            if (item.getProduct() != null && item.getProduct().getCode() != null) {
                xml.append("            <cac:SellersItemIdentification>\n");
                xml.append("                <cbc:ID>").append(escapeXml(item.getProduct().getCode())).append("</cbc:ID>\n");
                xml.append("            </cac:SellersItemIdentification>\n");
            }
            xml.append("        </cac:Item>\n");
            xml.append("        <cac:Price>\n");
            xml.append("            <cbc:PriceAmount currencyID=\"PEN\">").append(unitPriceExcIgv.setScale(2, RoundingMode.HALF_UP)).append("</cbc:PriceAmount>\n");
            xml.append("        </cac:Price>\n");
            xml.append("    </cac:InvoiceLine>\n");
        }

        xml.append("</Invoice>");
        return xml.toString();
    }

    /**
     * Genera el XML UBL 2.1 para Notas de Crédito.
     */
    public String generateCreditNoteXml(Sale creditNoteSale, Company company) {
        validateSale(creditNoteSale, company);
        if (creditNoteSale.getRelatedSale() == null) {
            throw new IllegalStateException("La nota de crédito debe referenciar a un comprobante (Factura o Boleta).");
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<CreditNote xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2\" ");
        xml.append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ");
        xml.append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ");
        xml.append("xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">\n");

        xml.append("    <ext:UBLExtensions>\n");
        xml.append("        <ext:UBLExtension>\n");
        xml.append("            <ext:ExtensionContent></ext:ExtensionContent>\n");
        xml.append("        </ext:UBLExtension>\n");
        xml.append("    </ext:UBLExtensions>\n");

        xml.append("    <cbc:UBLVersionID>2.1</cbc:UBLVersionID>\n");
        xml.append("    <cbc:CustomizationID schemeAgencyName=\"PE:SUNAT\">2.0</cbc:CustomizationID>\n");
        xml.append("    <cbc:ID>").append(creditNoteSale.getSeries()).append("-").append(creditNoteSale.getNumber()).append("</cbc:ID>\n");
        xml.append("    <cbc:IssueDate>").append(creditNoteSale.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</cbc:IssueDate>\n");
        xml.append("    <cbc:IssueTime>").append(creditNoteSale.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("</cbc:IssueTime>\n");
        xml.append("    <cbc:DocumentCurrencyCode listID=\"ISO 4217 Alpha\" listName=\"Currency\" listAgencyName=\"United Nations Economic Commission for Europe\">PEN</cbc:DocumentCurrencyCode>\n");

        xml.append("    <cac:DiscrepancyResponse>\n");
        xml.append("        <cbc:ReferenceID>").append(creditNoteSale.getRelatedSale().getSeries()).append("-").append(creditNoteSale.getRelatedSale().getNumber()).append("</cbc:ReferenceID>\n");
        xml.append("        <cbc:ResponseCode>").append(escapeXml(creditNoteSale.getNoteCode() != null ? creditNoteSale.getNoteCode() : "01")).append("</cbc:ResponseCode>\n");
        xml.append("        <cbc:Description>").append(cdata(creditNoteSale.getNoteReason() != null ? creditNoteSale.getNoteReason() : "Anulacion de la operacion")).append("</cbc:Description>\n");
        xml.append("    </cac:DiscrepancyResponse>\n");

        String relatedDocTypeCode = creditNoteSale.getRelatedSale().getDocumentType() == Sale.SaleDocumentType.FACTURA ? "01" : "03";
        xml.append("    <cac:BillingReference>\n");
        xml.append("        <cac:InvoiceDocumentReference>\n");
        xml.append("            <cbc:ID>").append(creditNoteSale.getRelatedSale().getSeries()).append("-").append(creditNoteSale.getRelatedSale().getNumber()).append("</cbc:ID>\n");
        xml.append("            <cbc:DocumentTypeCode>").append(relatedDocTypeCode).append("</cbc:DocumentTypeCode>\n");
        xml.append("        </cac:InvoiceDocumentReference>\n");
        xml.append("    </cac:BillingReference>\n");

        // Emisor
        xml.append("    <cac:AccountingSupplierParty>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyIdentification>\n");
        xml.append("                <cbc:ID schemeID=\"6\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                .append(company.getRuc()).append("</cbc:ID>\n");
        xml.append("            </cac:PartyIdentification>\n");
        xml.append("            <cac:PartyName>\n");
        xml.append("                <cbc:Name>").append(cdata(company.getName())).append("</cbc:Name>\n");
        xml.append("            </cac:PartyName>\n");
        xml.append("            <cac:PartyLegalEntity>\n");
        xml.append("                <cbc:RegistrationName>").append(cdata(company.getName())).append("</cbc:RegistrationName>\n");
        xml.append("                <cac:RegistrationAddress>\n");
        xml.append("                    <cbc:ID>").append(escapeXml(company.getUbigeo() != null ? company.getUbigeo() : "150101")).append("</cbc:ID>\n");
        xml.append("                    <cbc:AddressTypeCode>0000</cbc:AddressTypeCode>\n");
        xml.append("                    <cbc:CityName>").append(cdata(company.getCity() != null ? company.getCity() : "LIMA")).append("</cbc:CityName>\n");
        xml.append("                    <cbc:CountrySubentity>").append(cdata(company.getDepartment() != null ? company.getDepartment() : "LIMA")).append("</cbc:CountrySubentity>\n");
        xml.append("                    <cbc:District>").append(cdata(company.getDistrict() != null ? company.getDistrict() : "LIMA")).append("</cbc:District>\n");
        xml.append("                    <cac:AddressLine><cbc:Line>").append(cdata(company.getAddress())).append("</cbc:Line></cac:AddressLine>\n");
        xml.append("                    <cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country>\n");
        xml.append("                </cac:RegistrationAddress>\n");
        xml.append("            </cac:PartyLegalEntity>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingSupplierParty>\n");

        // Receptor
        String customerDocType = getCustomerDocTypeCode(creditNoteSale.getCustomer(), creditNoteSale.getDocumentType());
        String customerDocNumber = getCustomerDocNumber(creditNoteSale.getCustomer(), creditNoteSale.getDocumentType());
        String customerName = getCustomerName(creditNoteSale.getCustomer());
        xml.append("    <cac:AccountingCustomerParty>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyIdentification>\n");
        xml.append("                <cbc:ID schemeID=\"").append(customerDocType).append("\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">")
                .append(escapeXml(customerDocNumber)).append("</cbc:ID>\n");
        xml.append("            </cac:PartyIdentification>\n");
        xml.append("            <cac:PartyLegalEntity>\n");
        xml.append("                <cbc:RegistrationName>").append(cdata(customerName)).append("</cbc:RegistrationName>\n");
        if (creditNoteSale.getCustomer() != null && creditNoteSale.getCustomer().getAddress() != null && !creditNoteSale.getCustomer().getAddress().isEmpty()) {
             xml.append("                <cac:RegistrationAddress>\n");
             xml.append("                    <cac:AddressLine><cbc:Line>").append(cdata(creditNoteSale.getCustomer().getAddress())).append("</cbc:Line></cac:AddressLine>\n");
             xml.append("                </cac:RegistrationAddress>\n");
        }
        xml.append("            </cac:PartyLegalEntity>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingCustomerParty>\n");

        // Cálculos de impuestos
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalTaxableAmountS = BigDecimal.ZERO;
        BigDecimal totalTaxAmountS = BigDecimal.ZERO;
        BigDecimal totalTaxableAmountE = BigDecimal.ZERO;
        BigDecimal totalTaxableAmountO = BigDecimal.ZERO;

        for (SaleItem item : creditNoteSale.getItems()) {
            BigDecimal qty = item.getQuantity();
            BigDecimal unitPriceIncIgv = item.getUnitPrice();
            String codeSunat = (item.getProduct().getTaxType() != null) ? item.getProduct().getTaxType().getCodeSunat() : "10";
            boolean isGravado = codeSunat.equals("10");
            BigDecimal rate = isGravado ? new BigDecimal("0.18") : BigDecimal.ZERO;
            BigDecimal divisor = BigDecimal.ONE.add(rate);
            BigDecimal unitPriceExcIgv = unitPriceIncIgv.divide(divisor, 5, RoundingMode.HALF_UP);
            BigDecimal lineExtensionAmount = unitPriceExcIgv.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTax = lineExtensionAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            totalTaxAmount = totalTaxAmount.add(lineTax);
            
            String taxCategory = getTaxCategory(codeSunat);
            if (taxCategory.equals("S")) {
                totalTaxableAmountS = totalTaxableAmountS.add(lineExtensionAmount);
                totalTaxAmountS = totalTaxAmountS.add(lineTax);
            } else if (taxCategory.equals("E")) {
                totalTaxableAmountE = totalTaxableAmountE.add(lineExtensionAmount);
            } else if (taxCategory.equals("O")) {
                totalTaxableAmountO = totalTaxableAmountO.add(lineExtensionAmount);
            }
        }

        xml.append("    <cac:TaxTotal>\n");
        xml.append("        <cbc:TaxAmount currencyID=\"PEN\">").append(totalTaxAmount).append("</cbc:TaxAmount>\n");
        if (totalTaxableAmountS.compareTo(BigDecimal.ZERO) > 0) {
            appendTaxSubtotal(xml, totalTaxableAmountS, totalTaxAmountS, "S", "1000", "IGV", "VAT");
        }
        if (totalTaxableAmountE.compareTo(BigDecimal.ZERO) > 0) {
            appendTaxSubtotal(xml, totalTaxableAmountE, BigDecimal.ZERO, "E", "9997", "EXO", "VAT");
        }
        if (totalTaxableAmountO.compareTo(BigDecimal.ZERO) > 0) {
            appendTaxSubtotal(xml, totalTaxableAmountO, BigDecimal.ZERO, "O", "9998", "INA", "FRE");
        }
        xml.append("    </cac:TaxTotal>\n");

        BigDecimal lineExtensionAmountGlobal = totalTaxableAmountS.add(totalTaxableAmountE).add(totalTaxableAmountO);
        BigDecimal taxInclusiveAmountGlobal = lineExtensionAmountGlobal.add(totalTaxAmount);
        
        xml.append("    <cac:LegalMonetaryTotal>\n");
        xml.append("        <cbc:LineExtensionAmount currencyID=\"PEN\">").append(lineExtensionAmountGlobal).append("</cbc:LineExtensionAmount>\n");
        xml.append("        <cbc:TaxInclusiveAmount currencyID=\"PEN\">").append(taxInclusiveAmountGlobal).append("</cbc:TaxInclusiveAmount>\n");
        xml.append("        <cbc:PayableAmount currencyID=\"PEN\">").append(taxInclusiveAmountGlobal).append("</cbc:PayableAmount>\n");
        xml.append("    </cac:LegalMonetaryTotal>\n");

        AtomicInteger lineIndex = new AtomicInteger(1);
        for (SaleItem item : creditNoteSale.getItems()) {
            BigDecimal qty = item.getQuantity();
            BigDecimal unitPriceIncIgv = item.getUnitPrice(); 
            String codeSunat = (item.getProduct().getTaxType() != null) ? item.getProduct().getTaxType().getCodeSunat() : "10";
            boolean isGravado = codeSunat.equals("10");
            BigDecimal rate = isGravado ? new BigDecimal("0.18") : BigDecimal.ZERO;
            BigDecimal divisor = BigDecimal.ONE.add(rate);
            BigDecimal unitPriceExcIgv = unitPriceIncIgv.divide(divisor, 5, RoundingMode.HALF_UP);
            BigDecimal lineExtensionAmount = unitPriceExcIgv.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTax = lineExtensionAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            String taxCatId = getTaxCategory(codeSunat);
            String taxSchemeId = getTaxSchemeId(codeSunat);
            String taxSchemeName = getTaxSchemeName(codeSunat);
            String taxSchemeTypeCode = getTaxTypeCode(codeSunat);

            xml.append("    <cac:CreditNoteLine>\n");
            xml.append("        <cbc:ID>").append(lineIndex.getAndIncrement()).append("</cbc:ID>\n");
            xml.append("        <cbc:CreditedQuantity unitCode=\"NIU\">").append(qty).append("</cbc:CreditedQuantity>\n");
            xml.append("        <cbc:LineExtensionAmount currencyID=\"PEN\">").append(lineExtensionAmount).append("</cbc:LineExtensionAmount>\n");
            xml.append("        <cac:PricingReference>\n");
            xml.append("            <cac:AlternativeConditionPrice>\n");
            xml.append("                <cbc:PriceAmount currencyID=\"PEN\">").append(unitPriceIncIgv.setScale(2, RoundingMode.HALF_UP)).append("</cbc:PriceAmount>\n");
            xml.append("                <cbc:PriceTypeCode listName=\"Tipo de Precio\" listAgencyName=\"PE:SUNAT\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16\">01</cbc:PriceTypeCode>\n");
            xml.append("            </cac:AlternativeConditionPrice>\n");
            xml.append("        </cac:PricingReference>\n");
            xml.append("        <cac:TaxTotal>\n");
            xml.append("            <cbc:TaxAmount currencyID=\"PEN\">").append(lineTax).append("</cbc:TaxAmount>\n");
            xml.append("            <cac:TaxSubtotal>\n");
            xml.append("                <cbc:TaxableAmount currencyID=\"PEN\">").append(lineExtensionAmount).append("</cbc:TaxableAmount>\n");
            xml.append("                <cbc:TaxAmount currencyID=\"PEN\">").append(lineTax).append("</cbc:TaxAmount>\n");
            xml.append("                <cac:TaxCategory>\n");
            xml.append("                    <cbc:ID schemeID=\"UN/ECE 5305\" schemeName=\"Tax Category Identifier\" schemeAgencyName=\"United Nations Economic Commission for Europe\">").append(taxCatId).append("</cbc:ID>\n");
            xml.append("                    <cbc:Percent>").append(rate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)).append("</cbc:Percent>\n");
            xml.append("                    <cbc:TaxExemptionReasonCode listAgencyName=\"PE:SUNAT\" listName=\"Afectacion del IGV\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07\">").append(escapeXml(codeSunat)).append("</cbc:TaxExemptionReasonCode>\n");
            xml.append("                    <cac:TaxScheme>\n");
            xml.append("                        <cbc:ID schemeID=\"UN/ECE 5153\" schemeName=\"Codigo de tributos\" schemeAgencyName=\"PE:SUNAT\">").append(taxSchemeId).append("</cbc:ID>\n");
            xml.append("                        <cbc:Name>").append(taxSchemeName).append("</cbc:Name>\n");
            xml.append("                        <cbc:TaxTypeCode>").append(taxSchemeTypeCode).append("</cbc:TaxTypeCode>\n");
            xml.append("                    </cac:TaxScheme>\n");
            xml.append("                </cac:TaxCategory>\n");
            xml.append("            </cac:TaxSubtotal>\n");
            xml.append("        </cac:TaxTotal>\n");
            xml.append("        <cac:Item>\n");
            String prodDesc = (item.getProduct() != null) ? item.getProduct().getTradeName() : "Producto";
            xml.append("            <cbc:Description>").append(cdata(prodDesc)).append("</cbc:Description>\n");
            if (item.getProduct() != null && item.getProduct().getCode() != null) {
                xml.append("            <cac:SellersItemIdentification>\n");
                xml.append("                <cbc:ID>").append(escapeXml(item.getProduct().getCode())).append("</cbc:ID>\n");
                xml.append("            </cac:SellersItemIdentification>\n");
            }
            xml.append("        </cac:Item>\n");
            xml.append("        <cac:Price>\n");
            xml.append("            <cbc:PriceAmount currencyID=\"PEN\">").append(unitPriceExcIgv.setScale(2, RoundingMode.HALF_UP)).append("</cbc:PriceAmount>\n");
            xml.append("        </cac:Price>\n");
            xml.append("    </cac:CreditNoteLine>\n");
        }

        xml.append("</CreditNote>");
        return xml.toString();
    }

    /**
     * Genera el XML UBL 2.0 para Anulación de Comprobantes (Comunicaciones de Baja).
     */
    public String generateVoidedDocumentXml(VoidedDocument voidedDoc, Company company) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<VoidedDocuments xmlns=\"urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1\" ");
        xml.append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ");
        xml.append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ");
        xml.append("xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">\n");

        xml.append("    <ext:UBLExtensions>\n");
        xml.append("        <ext:UBLExtension>\n");
        xml.append("            <ext:ExtensionContent></ext:ExtensionContent>\n");
        xml.append("        </ext:UBLExtension>\n");
        xml.append("    </ext:UBLExtensions>\n");

        xml.append("    <cbc:UBLVersionID>2.0</cbc:UBLVersionID>\n");
        xml.append("    <cbc:CustomizationID>1.0</cbc:CustomizationID>\n");
        
        String dateIdentifier = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ticketSunat = voidedDoc.getTicketSunat() != null ? voidedDoc.getTicketSunat() : ("RA-" + dateIdentifier + "-1");
        xml.append("    <cbc:ID>").append(escapeXml(ticketSunat)).append("</cbc:ID>\n");
        
        xml.append("    <cbc:ReferenceDate>").append(voidedDoc.getIssueDate()).append("</cbc:ReferenceDate>\n");
        xml.append("    <cbc:IssueDate>").append(java.time.LocalDate.now()).append("</cbc:IssueDate>\n");

        xml.append("    <cac:AccountingSupplierParty>\n");
        xml.append("        <cbc:CustomerAssignedAccountID>").append(company.getRuc()).append("</cbc:CustomerAssignedAccountID>\n");
        xml.append("        <cbc:AdditionalAccountID>6</cbc:AdditionalAccountID>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyLegalEntity>\n");
        xml.append("                <cbc:RegistrationName>").append(cdata(company.getName())).append("</cbc:RegistrationName>\n");
        xml.append("            </cac:PartyLegalEntity>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingSupplierParty>\n");

        AtomicInteger lineIndex = new AtomicInteger(1);
        for (var item : voidedDoc.getItems()) {
            xml.append("    <cac:VoidedDocumentsLine>\n");
            xml.append("        <cbc:LineID>").append(lineIndex.getAndIncrement()).append("</cbc:LineID>\n");
            xml.append("        <cbc:DocumentTypeCode>").append(item.getSale().getDocumentType() == Sale.SaleDocumentType.FACTURA ? "01" : "03").append("</cbc:DocumentTypeCode>\n");
            xml.append("        <cac:DocumentSerialID>").append(item.getSale().getSeries()).append("</cac:DocumentSerialID>\n");
            xml.append("        <cac:DocumentNumberID>").append(item.getSale().getNumber()).append("</cac:DocumentNumberID>\n");
            xml.append("        <cbc:VoidReasonDescription>").append(cdata(item.getDescription() != null ? item.getDescription() : "Error en emision")).append("</cbc:VoidReasonDescription>\n");
            xml.append("    </cac:VoidedDocumentsLine>\n");
        }

        xml.append("</VoidedDocuments>");
        return xml.toString();
    }
    
    // --- MÉTODOS DE APOYO Y VALIDACIONES ---

    private void appendTaxSubtotal(StringBuilder xml, BigDecimal taxableAmt, BigDecimal taxAmt, String catId, String schemeId, String name, String typeCode) {
        xml.append("        <cac:TaxSubtotal>\n");
        xml.append("            <cbc:TaxableAmount currencyID=\"PEN\">").append(taxableAmt).append("</cbc:TaxableAmount>\n");
        xml.append("            <cbc:TaxAmount currencyID=\"PEN\">").append(taxAmt).append("</cbc:TaxAmount>\n");
        xml.append("            <cac:TaxCategory>\n");
        xml.append("                <cbc:ID schemeID=\"UN/ECE 5305\" schemeName=\"Tax Category Identifier\" schemeAgencyName=\"United Nations Economic Commission for Europe\">").append(catId).append("</cbc:ID>\n");
        xml.append("                <cac:TaxScheme>\n");
        xml.append("                    <cbc:ID schemeID=\"UN/ECE 5153\" schemeAgencyID=\"6\">").append(schemeId).append("</cbc:ID>\n");
        xml.append("                    <cbc:Name>").append(name).append("</cbc:Name>\n");
        xml.append("                    <cbc:TaxTypeCode>").append(typeCode).append("</cbc:TaxTypeCode>\n");
        xml.append("                </cac:TaxScheme>\n");
        xml.append("            </cac:TaxCategory>\n");
        xml.append("        </cac:TaxSubtotal>\n");
    }

    /**
     * Valida reglas estrictas antes de emitir a SUNAT.
     */
    private void validateSale(Sale sale, Company company) {
        if (company == null || company.getRuc() == null || company.getRuc().length() != 11) {
            throw new IllegalArgumentException("Emisor inválido: Se requiere RUC de 11 dígitos");
        }
        
        if (sale.getDocumentType() == Sale.SaleDocumentType.FACTURA) {
            if (sale.getCustomer() == null || sale.getCustomer().getDocumentType() != DocumentType.RUC) {
                throw new IllegalStateException("No se puede emitir factura: El receptor debe estar identificado con RUC");
            }
            if (sale.getCustomer().getDocumentNumber() == null || sale.getCustomer().getDocumentNumber().length() != 11) {
                throw new IllegalStateException("No se puede emitir factura: El RUC del receptor es inválido");
            }
        }
        
        if (sale.getDocumentType() == Sale.SaleDocumentType.BOLETA) {
            // Validaciones para boleta de venta (DNI, CE, Pasaporte o monto máximo sin ID)
            if (sale.getCustomer() != null && sale.getCustomer().getDocumentType() == DocumentType.DNI) {
                if (sale.getCustomer().getDocumentNumber() == null || sale.getCustomer().getDocumentNumber().length() != 8) {
                    throw new IllegalStateException("DNI del receptor es inválido");
                }
            }
            // Regla de SUNAT: si la boleta supera los 700 soles, se exige cliente identificado
            if (sale.getTotal().compareTo(new BigDecimal("700")) >= 0) {
                if (sale.getCustomer() == null || sale.getCustomer().getDocumentNumber() == null || sale.getCustomer().getDocumentNumber().isEmpty()) {
                    throw new IllegalStateException("Boleta de Venta supera 700 Soles: Receptor debe estar identificado (DNI, RUC, CE o Pasaporte)");
                }
            }
        }
        
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalStateException("El comprobante debe tener al menos un ítem");
        }
    }

    private String getCustomerDocTypeCode(Customer customer, Sale.SaleDocumentType docType) {
        if (customer != null && customer.getDocumentType() != null) {
            return switch (customer.getDocumentType()) {
                case DNI -> "1";
                case CE -> "4";
                case RUC -> "6";
                case PASAPORTE -> "7";
                default -> "-";
            };
        }
        return docType == Sale.SaleDocumentType.FACTURA ? "6" : "-";
    }

    private String getCustomerDocNumber(Customer customer, Sale.SaleDocumentType docType) {
        if (customer != null && customer.getDocumentNumber() != null && !customer.getDocumentNumber().isEmpty()) {
            return customer.getDocumentNumber();
        }
        return docType == Sale.SaleDocumentType.FACTURA ? "00000000000" : "00000000";
    }

    private String getCustomerName(Customer customer) {
        if (customer != null && customer.getName() != null && !customer.getName().isEmpty()) {
            return customer.getName();
        }
        return "CLIENTE GENERICO";
    }

    private String getTaxCategory(String codeSunat) {
        if (codeSunat == null) return "S";
        if (codeSunat.startsWith("2")) return "E";
        if (codeSunat.startsWith("3")) return "O";
        return "S";
    }

    private String getTaxSchemeId(String codeSunat) {
        if (codeSunat == null) return "1000";
        if (codeSunat.startsWith("2")) return "9997";
        if (codeSunat.startsWith("3")) return "9998";
        return "1000";
    }

    private String getTaxSchemeName(String codeSunat) {
        if (codeSunat == null) return "IGV";
        if (codeSunat.startsWith("2")) return "EXO";
        if (codeSunat.startsWith("3")) return "INA";
        return "IGV";
    }

    private String getTaxTypeCode(String codeSunat) {
        if (codeSunat == null) return "VAT";
        if (codeSunat.startsWith("2")) return "VAT";
        if (codeSunat.startsWith("3")) return "FRE";
        return "VAT";
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    private String cdata(String input) {
        if (input == null) return "<![CDATA[]]>";
        return "<![CDATA[" + input.replace("]]>", "]]]]><![CDATA[>") + "]]>";
    }
}
