package com.sergiocodev.app.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SunatOseClient {

    public SunatOseResponse sendInvoice(String signedXml, String fileName) {
        // Placeholder for SOAP/REST call to OSE
        // Simulate OSE response
        SunatOseResponse response = new SunatOseResponse();

        // Simulate success for now (mock)
        response.setStatusCode("0"); // 0 = Success in many OSEs
        response.setStatusMessage("La factura ha sido aceptada");
        response.setTicket(String.valueOf(new Random().nextInt(1000000)));
        // In real impl, this would return the base64 CDR
        response.setCdrXml("<CDR>Contenido del CDR</CDR>");
        return response;
    }

    public SunatOseResponse sendVoidedDocument(String signedXml, String fileName) {
        // Placeholder for SOAP/REST call to OSE for VoidedDocuments
        SunatOseResponse response = new SunatOseResponse();
        response.setStatusCode("0");
        response.setStatusMessage("La comunicación de baja ha sido aceptada");
        response.setTicket(String.valueOf(new Random().nextInt(1000000)));
        return response;
    }

    @Data
    public static class SunatOseResponse {
        private String statusCode;
        private String statusMessage;
        private String ticket;
        private String cdrXml;
        private String errorCode;
    }
}
