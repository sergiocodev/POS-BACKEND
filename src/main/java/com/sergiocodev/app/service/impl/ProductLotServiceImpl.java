package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.ProductLotService;

import com.sergiocodev.app.dto.productlot.ProductLotRequest;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.repository.ProductLotRepository;
import com.sergiocodev.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductLotServiceImpl implements ProductLotService {

    private final ProductLotRepository repository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductLotResponse create(ProductLotRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductLot entity = new ProductLot();
        entity.setProduct(product);
        entity.setLotCode(request.lotCode());
        entity.setExpiryDate(request.expiryDate());
        return new ProductLotResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductLotResponse> getAll() {
        return repository.findAll().stream()
                .map(ProductLotResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductLotResponse> getAllPaged(String productName, String lotCode, Pageable pageable) {
        Specification<ProductLot> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productName != null && !productName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("product").get("tradeName")), "%" + productName.toLowerCase() + "%"));
            }
            if (lotCode != null && !lotCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lotCode")), "%" + lotCode.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(ProductLotResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductLotResponse> getByProductId(Long productId) {
        return repository.findByProductIdOrderByExpiryDateAsc(productId).stream()
                .map(ProductLotResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductLotResponse getById(Long id) {
        return repository.findById(id)
                .map(ProductLotResponse::new)
                .orElseThrow(() -> new RuntimeException("Product lot not found"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
