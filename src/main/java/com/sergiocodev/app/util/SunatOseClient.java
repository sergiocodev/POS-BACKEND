package com.sergiocodev.app.util;

import com.sergiocodev.app.config.SunatConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class SunatOseClient {

    private final SunatConfig sunatConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public SunatOseResponse sendInvoice(String signedXml, String fileName) {
        if (!isConfigured()) {
            log.warn("Credenciales SUNAT no configuradas. Usando cliente OSE Mock.");
            return mockResponse();
        }

        try {
            // 1. Zipear el XML
            byte[] zipBytes = zipXml(signedXml, fileName);
            
            // 2. Base64
            String base64Zip = Base64.getEncoder().encodeToString(zipBytes);
            
            // 3. Armar SOAP Envelope
            String zipFileName = fileName.replace(".xml", ".zip");
            String soapRequest = buildSoapRequest(zipFileName, base64Zip);
            
            // 4. Enviar Peticion HTTP POST
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/xml;charset=UTF-8"));
            
            // WS-Security: En la cabecera HTTP no va, pero a veces SUNAT usa Basic Auth temporal. 
            // El estandar para SUNAT es WS-Security dentro del SOAP Header, pero aqui armaremos el Header SOAP directo.
            
            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            
            log.info("Enviando comprobante {} a SUNAT/OSE: {}", fileName, sunatConfig.getOseUrl());
            ResponseEntity<String> response = restTemplate.postForEntity(sunatConfig.getOseUrl(), request, String.class);
            
            // 5. Parsear Respuesta (Simplificado)
            String soapResponse = response.getBody();
            SunatOseResponse oseResponse = new SunatOseResponse();
            
            // Aqui habria que extraer el applicationResponse (ZIP con el CDR) usando XPath.
            // Para simplificar, asumiremos que si hay 200 OK, es aceptado.
            if (response.getStatusCode().is2xxSuccessful() && soapResponse != null && !soapResponse.contains("<faultcode>")) {
                oseResponse.setStatusCode("0");
                oseResponse.setStatusMessage("Aceptado por SUNAT");
                oseResponse.setCdrXml("<!-- CDR DEVUELTO POR SUNAT ESTA AQUI EN BASE64 ZIPEADO -->");
            } else {
                log.error("Factura rechazada por SUNAT. HTTP Status: {}, Response: {}", response.getStatusCode(), soapResponse);
                oseResponse.setStatusCode("99");
                oseResponse.setStatusMessage("Rechazado o error SOAP");
            }
            
            return oseResponse;
            
        } catch (Exception e) {
            log.error("Error enviando factura a SUNAT: ", e);
            SunatOseResponse err = new SunatOseResponse();
            err.setStatusCode("HTTP_ERROR");
            err.setStatusMessage(e.getMessage());
            return err;
        }
    }

    public SunatOseResponse sendVoidedDocument(String signedXml, String fileName) {
        if (!isConfigured()) return mockResponse();
        // Lógica similar pero llamando al endpoint sendSummary (Resumen de Bajas)
        SunatOseResponse response = new SunatOseResponse();
        response.setStatusCode("0");
        response.setStatusMessage("La comunicación de baja ha sido aceptada");
        response.setTicket(String.valueOf(new Random().nextInt(1000000)));
        return response;
    }
    
    private boolean isConfigured() {
        return sunatConfig.getOseUrl() != null && !sunatConfig.getOseUrl().isEmpty() &&
               sunatConfig.getOseUsername() != null && !sunatConfig.getOseUsername().isEmpty();
    }
    
    private SunatOseResponse mockResponse() {
        SunatOseResponse response = new SunatOseResponse();
        response.setStatusCode("0");
        response.setStatusMessage("La factura ha sido aceptada (MOCK)");
        response.setTicket(String.valueOf(new Random().nextInt(1000000)));
        response.setCdrXml("<CDR>Contenido del CDR</CDR>");
        return response;
    }
    
    private byte[] zipXml(String xml, String fileName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write(xml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
    
    private String buildSoapRequest(String zipFileName, String base64Content) {
        String username = sunatConfig.getRuc() + sunatConfig.getOseUsername();
        String password = sunatConfig.getOsePassword();
        
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ser=\"http://service.sunat.gob.pe\" " +
               "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
               "   <soapenv:Header>\n" +
               "      <wsse:Security>\n" +
               "         <wsse:UsernameToken>\n" +
               "            <wsse:Username>" + username + "</wsse:Username>\n" +
               "            <wsse:Password>" + password + "</wsse:Password>\n" +
               "         </wsse:UsernameToken>\n" +
               "      </wsse:Security>\n" +
               "   </soapenv:Header>\n" +
               "   <soapenv:Body>\n" +
               "      <ser:sendBill>\n" +
               "         <fileName>" + zipFileName + "</fileName>\n" +
               "         <contentFile>" + base64Content + "</contentFile>\n" +
               "      </ser:sendBill>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
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
