package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.marca.MarcaRequest;
import com.sergiocodev.app.dto.marca.MarcaResponse;

import java.util.List;

public interface MarcaService {

    MarcaResponse crear(MarcaRequest request);

    List<MarcaResponse> obtenerTodas();

    MarcaResponse obtenerPorId(Long id);

    MarcaResponse actualizar(Long id, MarcaRequest request);

    void eliminar(Long id);
}
