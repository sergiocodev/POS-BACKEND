package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.categoria.CategoriaRequest;
import com.sergiocodev.app.dto.categoria.CategoriaResponse;
import com.sergiocodev.app.exception.CategoriaNotFoundException;
import com.sergiocodev.app.exception.NombreCategoriaDuplicadoException;
import com.sergiocodev.app.model.Categoria;
import com.sergiocodev.app.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByName(request.getName())) {
            throw new NombreCategoriaDuplicadoException(request.getName());
        }

        Categoria categoria = new Categoria();
        categoria.setName(request.getName());
        if (request.getActive() != null) {
            categoria.setActive(request.getActive());
        }

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return new CategoriaResponse(categoriaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        return new CategoriaResponse(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        if (!categoria.getName().equals(request.getName()) &&
                categoriaRepository.existsByName(request.getName())) {
            throw new NombreCategoriaDuplicadoException(request.getName());
        }

        categoria.setName(request.getName());
        if (request.getActive() != null) {
            categoria.setActive(request.getActive());
        }

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return new CategoriaResponse(categoriaActualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new CategoriaNotFoundException(id);
        }
        categoriaRepository.deleteById(id);
    }
}
