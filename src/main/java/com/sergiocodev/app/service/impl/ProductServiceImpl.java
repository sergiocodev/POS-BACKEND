package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.ProductService;

import com.sergiocodev.app.dto.product.ProductLookupResponse;
import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.mapper.ProductMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
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

        String code = request.code() != null ? request.code().trim() : "";
        if (code.isBlank()) {
            code = generateNextCode(request.categoryId());
        } else if (repository.existsByCode(code)) {
            throw new BadRequestException("El código interno '" + code + "' ya se encuentra registrado.");
        }
        entity.setCode(code);

        cleanOptionalFields(entity);

        mapBasicInfo(request, entity);
        mapTherapeuticActions(request, entity);
        entity = repository.save(entity);

        mapIngredients(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(Long categoryId) {
        return repository.findAllWithFilters(categoryId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductLookupResponse> getLookup() {
        List<Object[]> actionRows = repository.findAllProductTherapeuticActionIds();
        Map<Long, List<Long>> actionMap = new HashMap<>();
        for (Object[] row : actionRows) {
            Long productId = ((Number) row[0]).longValue();
            Long actionId = ((Number) row[1]).longValue();
            actionMap.computeIfAbsent(productId, k -> new ArrayList<>()).add(actionId);
        }

        List<Object[]> productRows = repository.findActiveProductBasicInfo();
        List<ProductLookupResponse> result = new ArrayList<>(productRows.size());
        for (Object[] row : productRows) {
            Long id = ((Number) row[0]).longValue();
            String code = (String) row[1];
            String tradeName = (String) row[2];
            List<Long> actions = actionMap.getOrDefault(id, Collections.emptyList());
            result.add(new ProductLookupResponse(id, code, tradeName, actions));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllPaged(
            String code,
            String digemidCode,
            String sanitaryRegistration,
            String tradeName,
            String therapeuticActionNames,
            String categoryName,
            String laboratoryName,
            Boolean hasUnits,
            Boolean isActive,
            Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (code != null && !code.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (digemidCode != null && !digemidCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("digemidCode")), "%" + digemidCode.toLowerCase() + "%"));
            }
            if (sanitaryRegistration != null && !sanitaryRegistration.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("sanitaryRegistration")), "%" + sanitaryRegistration.toLowerCase() + "%"));
            }
            if (tradeName != null && !tradeName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tradeName")), "%" + tradeName.toLowerCase() + "%"));
            }
            if (categoryName != null && !categoryName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("category").get("name")), "%" + categoryName.toLowerCase() + "%"));
            }
            if (laboratoryName != null && !laboratoryName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("laboratory").get("name")), "%" + laboratoryName.toLowerCase() + "%"));
            }
            if (therapeuticActionNames != null && !therapeuticActionNames.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("therapeuticActions").get("name")), "%" + therapeuticActionNames.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (hasUnits != null) {
                if (Boolean.TRUE.equals(hasUnits)) {
                    predicates.add(cb.isNotEmpty(root.get("units")));
                } else {
                    predicates.add(cb.isEmpty(root.get("units")));
                }
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

        String code = request.code() != null ? request.code().trim() : "";
        if (code.isBlank()) {
            throw new BadRequestException("El código interno es obligatorio.");
        }
        if (repository.existsByCodeAndIdNot(code, id)) {
            throw new BadRequestException("El código interno '" + code + "' ya está en uso por otro producto.");
        }

        mapper.updateEntity(request, entity);
        entity.setCode(code);
        cleanOptionalFields(entity);

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
        return repository.searchByQuery(query, org.springframework.data.domain.PageRequest.of(0, 50)).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse toggleStatus(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        entity.setActive(!entity.isActive());
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

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode(Long categoryId) {
        if (categoryId == null) {
            throw new BadRequestException("Debe seleccionar una categoría para generar el código interno.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + categoryId));

        String prefix = category.getPrefix();
        if (prefix == null || prefix.isBlank()) {
            String base = category.getName().trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            prefix = base.substring(0, Math.min(base.length(), 4));
            if (prefix.isBlank()) {
                prefix = "PRD";
            }
        }
        prefix = prefix.trim().toUpperCase();

        Long maxNumber = repository.findMaxCodeNumberByPrefix(prefix);
        long nextNumber = (maxNumber == null) ? 1L : maxNumber + 1L;

        String nextCode = String.format("%s-%06d", prefix, nextNumber);
        while (repository.existsByCode(nextCode)) {
            nextNumber++;
            nextCode = String.format("%s-%06d", prefix, nextNumber);
        }

        return nextCode;
    }

    private void cleanOptionalFields(Product entity) {
        if (entity.getDigemidCode() != null && entity.getDigemidCode().trim().isEmpty()) {
            entity.setDigemidCode(null);
        } else if (entity.getDigemidCode() != null) {
            entity.setDigemidCode(entity.getDigemidCode().trim());
        }

        if (entity.getSanitaryRegistration() != null && entity.getSanitaryRegistration().trim().isEmpty()) {
            entity.setSanitaryRegistration(null);
        } else if (entity.getSanitaryRegistration() != null) {
            entity.setSanitaryRegistration(entity.getSanitaryRegistration().trim());
        }

        if (entity.getGenericName() != null && entity.getGenericName().trim().isEmpty()) {
            entity.setGenericName(null);
        } else if (entity.getGenericName() != null) {
            entity.setGenericName(entity.getGenericName().trim());
        }

        if (entity.getDescription() != null && entity.getDescription().trim().isEmpty()) {
            entity.setDescription(null);
        } else if (entity.getDescription() != null) {
            entity.setDescription(entity.getDescription().trim());
        }
    }
}
