package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import com.sergiocodev.app.mapper.ProductMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final PresentationRepository presentationRepository;
    private final TaxTypeRepository taxTypeRepository;
    private final ActiveIngredientRepository activeIngredientRepository;
    private final ProductLotRepository productLotRepository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product entity = mapper.toEntity(request);
        mapBasicInfo(request, entity);
        entity = repository.save(entity); // Save first to get the ID

        mapIngredients(request, entity);
        return mapper.toResponse(repository.save(entity)); // Save again to persist ingredients
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(Long categoryId, Long brandId, Boolean active) {
        return repository.findAllWithFilters(categoryId, brandId, active).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        mapper.updateEntity(request, entity);
        mapBasicInfo(request, entity);
        mapIngredients(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query) {
        return repository.searchByQuery(query).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductLotResponse> getLots(Long productId) {
        return productLotRepository.findByProductIdOrderByExpiryDateAsc(productId).stream()
                .map(ProductLotResponse::new)
                .collect(Collectors.toList());
    }

    private void mapBasicInfo(ProductRequest request, Product entity) {
        entity.setBrand(brandRepository.findById(request.brandId()).orElse(null));
        entity.setCategory(categoryRepository.findById(request.categoryId()).orElse(null));
        entity.setLaboratory(laboratoryRepository.findById(request.laboratoryId()).orElse(null));
        entity.setPresentation(presentationRepository.findById(request.presentationId()).orElse(null));
        entity.setTaxType(taxTypeRepository.findById(request.taxTypeId()).orElse(null));
        if (request.unitType() != null) {
            entity.setUnitType(Product.UnitType.valueOf(request.unitType()));
        }
    }

    private void mapIngredients(ProductRequest request, Product entity) {
        if (request.ingredients() != null) {
            entity.getIngredients().clear();
            request.ingredients().forEach(ir -> {
                ActiveIngredient activeIngredient = activeIngredientRepository.findById(ir.activeIngredientId())
                        .orElseThrow(() -> new RuntimeException(
                                "Active ingredient not found with id: " + ir.activeIngredientId()));

                ProductIngredient pi = new ProductIngredient();
                pi.setProduct(entity);
                pi.setActiveIngredient(activeIngredient);
                pi.setConcentration(ir.concentration());

                // Initialize the composite ID explicitly now that we have the product ID
                pi.getId().setProductId(entity.getId());
                pi.getId().setIngredientId(activeIngredient.getId());

                entity.getIngredients().add(pi);
            });
        }
    }
}
