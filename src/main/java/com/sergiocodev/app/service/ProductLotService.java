package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.productlot.ProductLotRequest;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import java.util.List;

public interface ProductLotService {
    ProductLotResponse create(ProductLotRequest request);

    List<ProductLotResponse> getAll();

    List<ProductLotResponse> getByProductId(Long productId);

    ProductLotResponse getById(Long id);

    void delete(Long id);
}
