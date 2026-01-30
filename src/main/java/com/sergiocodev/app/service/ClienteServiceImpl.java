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
        // Validar que no exista un cliente con el mismo DNI (si se proporciona)
        if (request.getDni() != null && !request.getDni().isEmpty()
                && clienteRepository.existsByDni(request.getDni())) {
            throw new DniDuplicadoException(request.getDni());
        }

        // Validar que no exista un cliente con el mismo RUC (si se proporciona)
        if (request.getRuc() != null && !request.getRuc().isEmpty()
                && clienteRepository.existsByRuc(request.getRuc())) {
            throw new RuntimeException("El RUC '" + request.getRuc() + "' ya existe");
        }

        // Crear nueva entidad Cliente
        Cliente cliente = new Cliente();
        cliente.setDni(request.getDni());
        cliente.setRuc(request.getRuc());
        cliente.setFirstName(request.getFirstName());
        cliente.setLastName(request.getLastName());
        cliente.setPhone(request.getPhone());
        cliente.setEmail(request.getEmail());
        cliente.setAddress(request.getAddress());

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
        if (request.getDni() != null && !request.getDni().isEmpty() &&
                !request.getDni().equals(cliente.getDni()) &&
                clienteRepository.existsByDni(request.getDni())) {
            throw new DniDuplicadoException(request.getDni());
        }

        // Validar RUC duplicado solo si cambió
        if (request.getRuc() != null && !request.getRuc().isEmpty() &&
                !request.getRuc().equals(cliente.getRuc()) &&
                clienteRepository.existsByRuc(request.getRuc())) {
            throw new RuntimeException("El RUC '" + request.getRuc() + "' ya existe");
        }

        // Actualizar campos
        cliente.setDni(request.getDni());
        cliente.setRuc(request.getRuc());
        cliente.setFirstName(request.getFirstName());
        cliente.setLastName(request.getLastName());
        cliente.setPhone(request.getPhone());
        cliente.setEmail(request.getEmail());
        cliente.setAddress(request.getAddress());

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
