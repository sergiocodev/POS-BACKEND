package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import java.util.List;

import com.sergiocodev.app.dto.productlot.ProductLotResponse;

public interface ProductService {
    ProductResponse create(ProductRequest request);

    List<ProductResponse> getAll(Long categoryId, Long brandId, Boolean active);

    ProductResponse getById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    List<ProductResponse> search(String query);

    List<ProductLotResponse> getLots(Long productId);

    ProductResponse toggleStatus(Long id);

    ProductResponse createNewProduct(ProductRequest request);
}
