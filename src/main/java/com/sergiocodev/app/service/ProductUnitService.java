package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.productunit.ProductUnitRequest;
import com.sergiocodev.app.dto.productunit.ProductUnitResponse;

import java.util.List;

public interface ProductUnitService {
    ProductUnitResponse create(ProductUnitRequest request);

    List<ProductUnitResponse> getByProductId(Long productId);

    ProductUnitResponse getById(Long id);

    ProductUnitResponse getByBarcode(String barcode);

    ProductUnitResponse update(Long id, ProductUnitRequest request);

    void delete(Long id);
}
