package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cliente.ClienteRequest;
import com.sergiocodev.app.dto.cliente.ClienteResponse;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    List<ClienteResponse> obtenerTodos();

    ClienteResponse obtenerPorId(Long id);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    void eliminar(Long id);
}
