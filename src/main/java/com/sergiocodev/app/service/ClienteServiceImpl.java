package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cliente.ClienteRequest;
import com.sergiocodev.app.dto.cliente.ClienteResponse;
import com.sergiocodev.app.exception.ClienteNotFoundException;
import com.sergiocodev.app.exception.DniDuplicadoException;
import com.sergiocodev.app.model.Cliente;
import com.sergiocodev.app.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        // Validar que no exista un cliente con el mismo DNI
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new DniDuplicadoException(request.getDni());
        }

        // Crear nueva entidad Cliente
        Cliente cliente = new Cliente();
        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());

        // Guardar y retornar respuesta
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return new ClienteResponse(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        return new ClienteResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        // Verificar que el cliente existe
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        // Validar DNI duplicado solo si cambió
        if (!cliente.getDni().equals(request.getDni()) &&
                clienteRepository.existsByDni(request.getDni())) {
            throw new DniDuplicadoException(request.getDni());
        }

        // Actualizar campos
        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());

        // Guardar y retornar respuesta
        Cliente clienteActualizado = clienteRepository.save(cliente);
        return new ClienteResponse(clienteActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Verificar que el cliente existe
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException(id);
        }
        clienteRepository.deleteById(id);
    }
}
