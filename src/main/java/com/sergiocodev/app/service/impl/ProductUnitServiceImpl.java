package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.ProductUnitService;

import com.sergiocodev.app.dto.productunit.ProductUnitRequest;
import com.sergiocodev.app.dto.productunit.ProductUnitResponse;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductUnit;
import com.sergiocodev.app.model.UnitOfMeasure;
import com.sergiocodev.app.repository.ProductRepository;
import com.sergiocodev.app.repository.ProductUnitRepository;
import com.sergiocodev.app.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductUnitServiceImpl implements ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional
    public ProductUnitResponse create(ProductUnitRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.isBaseUnit()) {
            productUnitRepository.findByProductIdAndIsBaseUnitTrue(product.getId())
                    .ifPresent(u -> {
                        u.setBaseUnit(false);
                        productUnitRepository.save(u);
                    });
        }

        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.unitOfMeasureId())
                .orElseThrow(() -> new RuntimeException("Unit of Measure not found"));

        ProductUnit unit = new ProductUnit();
        unit.setProduct(product);
        unit.setUnitOfMeasure(unitOfMeasure);
        unit.setFactor(request.factor());
        String barcode = request.barcode();
        if (barcode != null && barcode.trim().isEmpty()) {
            barcode = null;
        }
        unit.setBarcode(barcode);
        unit.setSunatCode(request.sunatCode());
        unit.setPrice(request.price());
        unit.setBaseUnit(request.isBaseUnit());

        return mapToResponse(productUnitRepository.save(unit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductUnitResponse> getByProductId(Long productId) {
        return productUnitRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductUnitResponse getById(Long id) {
        return productUnitRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductUnitResponse getByBarcode(String barcode) {
        return productUnitRepository.findByBarcode(barcode)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found with barcode: " + barcode));
    }

    @Override
    @Transactional
    public ProductUnitResponse update(Long id, ProductUnitRequest request) {
        ProductUnit unit = productUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found"));

        if (request.isBaseUnit() && !unit.isBaseUnit()) {
            productUnitRepository.findByProductIdAndIsBaseUnitTrue(unit.getProduct().getId())
                    .ifPresent(u -> {
                        u.setBaseUnit(false);
                        productUnitRepository.save(u);
                    });
        }

        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.unitOfMeasureId())
                .orElseThrow(() -> new RuntimeException("Unit of Measure not found"));

        unit.setUnitOfMeasure(unitOfMeasure);
        unit.setFactor(request.factor());
        String barcode = request.barcode();
        if (barcode != null && barcode.trim().isEmpty()) {
            barcode = null;
        }
        unit.setBarcode(barcode);
        unit.setSunatCode(request.sunatCode());
        unit.setPrice(request.price());
        unit.setBaseUnit(request.isBaseUnit());

        return mapToResponse(productUnitRepository.save(unit));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productUnitRepository.deleteById(id);
    }

    private ProductUnitResponse mapToResponse(ProductUnit unit) {
        return new ProductUnitResponse(
                unit.getId(),
                unit.getProduct().getId(),
                unit.getUnitOfMeasure().getId(),
                unit.getUnitOfMeasure().getName(),
                unit.getFactor(),
                unit.getBarcode(),
                unit.getSunatCode(),
                unit.getPrice(),
                unit.isBaseUnit());
    }
}
