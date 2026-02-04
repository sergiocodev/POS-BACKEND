package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.external.ExternalConsultationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ExternalConsultationService {

    private final RestTemplate restTemplate;

    @Value("${apisnet.api.token:}")
    private String apiToken;

    @Value("${apisnet.api.url.dni:https://api.apis.net.pe/v2/reniec/dni}")
    private String apiUrlDni;

    @Value("${apisnet.api.url.ruc:https://api.apis.net.pe/v2/sunat/ruc}")
    private String apiUrlRuc;

    public ExternalConsultationResponse searchByDocument(String documentNumber) {
        String url;
        if (documentNumber.length() == 8) {
            url = apiUrlDni;
        } else if (documentNumber.length() == 11) {
            url = apiUrlRuc;
        } else {
            throw new IllegalArgumentException(
                    "Número de documento no válido (debe ser DNI 8 dígitos o RUC 11 dígitos)");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("numero", documentNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ExternalConsultationResponse> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    ExternalConsultationResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Error en la consulta externa: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con el servicio de consulta externa: " + e.getMessage());
        }
    }
}
