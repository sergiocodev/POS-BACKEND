package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.ProductService;

import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.mapper.ProductMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

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
    private final PharmaceuticalFormRepository pharmaceuticalFormRepository;
    private final TherapeuticActionRepository therapeuticActionRepository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product entity = mapper.toEntity(request);

        mapBasicInfo(request, entity);
        mapTherapeuticActions(request, entity);
        entity = repository.save(entity);

        mapIngredients(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(Long categoryId, Long brandId) {
        return repository.findAllWithFilters(categoryId, brandId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllPaged(String code, String tradeName, String therapeuticActionNames, String categoryName, String brandName, String laboratoryName, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (code != null && !code.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (tradeName != null && !tradeName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tradeName")), "%" + tradeName.toLowerCase() + "%"));
            }
            if (categoryName != null && !categoryName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("category").get("name")), "%" + categoryName.toLowerCase() + "%"));
            }
            if (brandName != null && !brandName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("brand").get("name")), "%" + brandName.toLowerCase() + "%"));
            }
            if (laboratoryName != null && !laboratoryName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("laboratory").get("name")), "%" + laboratoryName.toLowerCase() + "%"));
            }
            if (therapeuticActionNames != null && !therapeuticActionNames.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("therapeuticActions").get("name")), "%" + therapeuticActionNames.toLowerCase() + "%"));
            }

            // Para evitar duplicados cuando hay múltiples acciones terapéuticas unidas
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        mapper.updateEntity(request, entity);

        mapBasicInfo(request, entity);
        mapTherapeuticActions(request, entity);
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
    @Transactional
    public ProductResponse toggleStatus(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        // status is managed via deleted_at or in product_units now (active was removed
        // from Product)
        return mapper.toResponse(repository.save(entity));
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
        entity.setPharmaceuticalForm(
                pharmaceuticalFormRepository.findById(request.pharmaceuticalFormId()).orElse(null));
        entity.setPresentation(presentationRepository.findById(request.presentationId()).orElse(null));
        entity.setTaxType(taxTypeRepository.findById(request.taxTypeId()).orElse(null));
    }

    private void mapIngredients(ProductRequest request, Product entity) {
        if (request.ingredients() != null) {
            entity.getIngredients().clear();
            request.ingredients().forEach(ir -> {
                ActiveIngredient activeIngredient = activeIngredientRepository.findById(ir.activeIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException(
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

    private void mapTherapeuticActions(ProductRequest request, Product entity) {
        if (request.therapeuticActionIds() != null) {
            entity.getTherapeuticActions().clear();
            List<TherapeuticAction> actions = therapeuticActionRepository.findAllById(request.therapeuticActionIds());
            if (actions.size() != request.therapeuticActionIds().size()) {
                throw new ResourceNotFoundException("Some therapeutic actions were not found");
            }
            entity.getTherapeuticActions().addAll(actions);
        }
    }

    @Override
    @Transactional
    public ProductResponse createNewProduct(ProductRequest request) {
        return create(request);
    }
}
