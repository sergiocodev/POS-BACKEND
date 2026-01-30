package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.categoria.CategoriaRequest;
import com.sergiocodev.app.dto.categoria.CategoriaResponse;

import java.util.List;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);

    List<CategoriaResponse> obtenerTodas();

    CategoriaResponse obtenerPorId(Long id);

    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    void eliminar(Long id);
}
