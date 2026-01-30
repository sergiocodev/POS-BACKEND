package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.marca.MarcaRequest;
import com.sergiocodev.app.dto.marca.MarcaResponse;
import com.sergiocodev.app.exception.MarcaNotFoundException;
import com.sergiocodev.app.exception.NombreMarcaDuplicadoException;
import com.sergiocodev.app.model.Marca;
import com.sergiocodev.app.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;

    @Override
    @Transactional
    public MarcaResponse crear(MarcaRequest request) {
        if (marcaRepository.existsByName(request.getName())) {
            throw new NombreMarcaDuplicadoException(request.getName());
        }

        Marca marca = new Marca();
        marca.setName(request.getName());
        if (request.getActive() != null) {
            marca.setActive(request.getActive());
        }

        Marca marcaGuardada = marcaRepository.save(marca);
        return new MarcaResponse(marcaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaResponse> obtenerTodas() {
        return marcaRepository.findAll()
                .stream()
                .map(MarcaResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaResponse obtenerPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new MarcaNotFoundException(id));
        return new MarcaResponse(marca);
    }

    @Override
    @Transactional
    public MarcaResponse actualizar(Long id, MarcaRequest request) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new MarcaNotFoundException(id));

        if (!marca.getName().equals(request.getName()) &&
                marcaRepository.existsByName(request.getName())) {
            throw new NombreMarcaDuplicadoException(request.getName());
        }

        marca.setName(request.getName());
        if (request.getActive() != null) {
            marca.setActive(request.getActive());
        }

        Marca marcaActualizada = marcaRepository.save(marca);
        return new MarcaResponse(marcaActualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!marcaRepository.existsById(id)) {
            throw new MarcaNotFoundException(id);
        }
        marcaRepository.deleteById(id);
    }
}
