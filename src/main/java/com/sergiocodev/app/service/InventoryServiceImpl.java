package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.inventory.InventoryRequest;
import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.InventoryRepository;
import com.sergiocodev.app.repository.ProductLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final EstablishmentRepository establishmentRepository;
    private final ProductLotRepository lotRepository;

    @Override
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        Inventory entity = repository.findByEstablishmentIdAndLotId(request.getEstablishmentId(), request.getLotId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setEstablishment(
                            establishmentRepository.findById(request.getEstablishmentId()).orElse(null));
                    newInv.setLot(lotRepository.findById(request.getLotId()).orElse(null));
                    return newInv;
                });

        entity.setQuantity(request.getQuantity());
        if (request.getCostPrice() != null)
            entity.setCostPrice(request.getCostPrice());
        if (request.getSalesPrice() != null)
            entity.setSalesPrice(request.getSalesPrice());
        entity.setLastMovement(LocalDateTime.now());

        return new InventoryResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return repository.findAll().stream()
                .map(InventoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getByEstablishment(Long establishmentId) {
        // This would need a repo method findByEstablishmentId, but I'll use findAll and
        // filter for now
        // or just create the repo method if needed.
        return repository.findAll().stream()
                .filter(i -> i.getEstablishment().getId().equals(establishmentId))
                .map(InventoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        return repository.findById(id)
                .map(InventoryResponse::new)
                .orElseThrow(() -> new RuntimeException("Inventory record not found"));
    }
}
