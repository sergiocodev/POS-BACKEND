package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;
import com.sergiocodev.app.exception.BrandNotFoundException;
import com.sergiocodev.app.exception.DuplicateBrandException;
import com.sergiocodev.app.mapper.BrandMapper;
import com.sergiocodev.app.model.Brand;
import com.sergiocodev.app.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper mapper;

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByName(request.name())) {
            throw new DuplicateBrandException("Brand already exists with name: " + request.name());
        }

        Brand brand = mapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);
        return mapper.toResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAll() {
        return brandRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + id));
        return mapper.toResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + id));

        if (!brand.getName().equals(request.name()) &&
                brandRepository.existsByName(request.name())) {
            throw new DuplicateBrandException("Brand already exists with name: " + request.name());
        }

        mapper.updateEntity(request, brand);

        Brand updatedBrand = brandRepository.save(brand);
        return mapper.toResponse(updatedBrand);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new BrandNotFoundException("Brand not found with ID: " + id);
        }
        brandRepository.deleteById(id);
    }
}
