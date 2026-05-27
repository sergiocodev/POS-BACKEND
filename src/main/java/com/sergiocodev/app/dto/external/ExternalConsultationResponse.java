package com.sergiocodev.app.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalConsultationResponse(
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    String tipoDocumento,
    String numeroDocumento,
    String razonSocial,
    String estado,
    String condicion,
    String direccion,
    String ubigeo,
    String viaTipo,
    String viaNombre,
    String zonaCodigo,
    String zonaTipo,
    String numero,
    String interior,
    String lote,
    String dpto,
    String manzana,
    String kilometro,
    String distrito,
    String provincia,
    String departamento
) {}
