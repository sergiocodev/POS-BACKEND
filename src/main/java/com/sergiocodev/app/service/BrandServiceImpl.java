package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;
import com.sergiocodev.app.exception.BrandNotFoundException;
import com.sergiocodev.app.exception.DuplicateBrandException;
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

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateBrandException("Brand already exists with name: " + request.getName());
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }

        Brand savedBrand = brandRepository.save(brand);
        return new BrandResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAll() {
        return brandRepository.findAll()
                .stream()
                .map(BrandResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + id));
        return new BrandResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + id));

        if (!brand.getName().equals(request.getName()) &&
                brandRepository.existsByName(request.getName())) {
            throw new DuplicateBrandException("Brand already exists with name: " + request.getName());
        }

        brand.setName(request.getName());
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }

        Brand updatedBrand = brandRepository.save(brand);
        return new BrandResponse(updatedBrand);
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
