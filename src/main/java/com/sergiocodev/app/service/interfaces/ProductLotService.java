package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.productlot.ProductLotRequest;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductLotService {
    ProductLotResponse create(ProductLotRequest request);

    List<ProductLotResponse> getAll();

    Page<ProductLotResponse> getAllPaged(String productName, String lotCode, Long establishmentId, Pageable pageable);

    List<ProductLotResponse> getByProductId(Long productId);

    ProductLotResponse getById(Long id);

    void delete(Long id);
}
