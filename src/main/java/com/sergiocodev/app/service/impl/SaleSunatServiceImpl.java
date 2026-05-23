package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.SaleSunatService;
import com.sergiocodev.app.service.interfaces.DigitalSignatureService;
import com.sergiocodev.app.repository.SaleRepository;
import com.sergiocodev.app.repository.CompanyRepository;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.Sale.SunatStatus;
import com.sergiocodev.app.dto.sunat.EmitInvoiceResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.util.XmlUblGenerator;
import com.sergiocodev.app.util.SunatOseClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleSunatServiceImpl implements SaleSunatService {

    private final SaleRepository repository;
    private final CompanyRepository companyRepository;
    private final XmlUblGenerator xmlUblGenerator;
    private final DigitalSignatureService digitalSignatureService;
    private final SunatOseClient sunatOseClient;

    @Override
    public EmitInvoiceResponse emitInvoiceToOSE(Long saleId) {
        Sale sale = repository.findWithItemsById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (sale.getSunatStatus() == SunatStatus.ACCEPTED) {
            throw new RuntimeException("La venta ya fue aceptada por SUNAT");
        }

        try {
            com.sergiocodev.app.model.Company company = companyRepository.findMainCompany()
                    .orElseThrow(() -> new RuntimeException("Company not configured"));

            // 1. Generate XML
            String xml = xmlUblGenerator.generateInvoiceXml(sale, company);
            String fileName = sale.getSeries() + "-" + sale.getNumber() + ".xml";

            // 2. Sign XML
            String signedXml = digitalSignatureService.signXml(xml);

            // 3. Send to OSE
            SunatOseClient.SunatOseResponse oseResponse = sunatOseClient.sendInvoice(signedXml, fileName);

            // 4. Update Sale
            if ("0".equals(oseResponse.getStatusCode())) {
                sale.setSunatStatus(SunatStatus.ACCEPTED);
            } else {
                sale.setSunatStatus(SunatStatus.REJECTED);
            }

            sale.setSunatMessage(oseResponse.getStatusMessage());
            sale.setXmlUrl("mock/path/" + fileName);
            sale.setCdrUrl("mock/path/R-" + fileName);
            sale.setHashCpe("MOCK_HASH");
            sale.setSunatResponseJson("{\"ticket\": \"" + oseResponse.getTicket() + "\"}");

            repository.save(sale);

            return EmitInvoiceResponse.builder()
                    .saleId(sale.getId())
                    .sunatStatus(sale.getSunatStatus().name())
                    .sunatMessage(sale.getSunatMessage())
                    .xmlUrl(sale.getXmlUrl())
                    .cdrUrl(sale.getCdrUrl())
                    .hashCpe(sale.getHashCpe())
                    .build();

        } catch (Exception e) {
            sale.setSunatStatus(SunatStatus.REJECTED);
            sale.setSunatMessage(e.getMessage());
            repository.save(sale);
            throw new RuntimeException("Error emitiendo a SUNAT: " + e.getMessage());
        }
    }

    @Override
    public String getXml(Long id) {
        Sale sale = repository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Sale>\n");
        xml.append("  <Id>").append(sale.getId()).append("</Id>\n");
        xml.append("  <DocumentType>").append(sale.getDocumentType()).append("</DocumentType>\n");
        xml.append("  <Series>").append(sale.getSeries()).append("</Series>\n");
        xml.append("  <Number>").append(sale.getNumber()).append("</Number>\n");
        xml.append("  <Date>").append(sale.getDate()).append("</Date>\n");
        xml.append("  <Customer>").append(sale.getCustomer() != null ? sale.getCustomer().getName() : "")
                .append("</Customer>\n");
        xml.append("  <SubTotal>").append(sale.getSubTotal()).append("</SubTotal>\n");
        xml.append("  <Tax>").append(sale.getTax()).append("</Tax>\n");
        xml.append("  <Total>").append(sale.getTotal()).append("</Total>\n");
        xml.append("  <Status>").append(sale.getStatus()).append("</Status>\n");
        xml.append("  <SunatStatus>").append(sale.getSunatStatus()).append("</SunatStatus>\n");
        xml.append("</Sale>\n");
        return xml.toString();
    }

    @Override
    public String getCdr(Long id) {
        return "<cdr>Placeholder for Sale " + id + "</cdr>";
    }
}
