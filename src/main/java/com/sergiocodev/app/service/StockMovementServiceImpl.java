package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository repository;
    private final EstablishmentRepository establishmentRepository;
    private final ProductLotRepository lotRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StockMovementResponse create(StockMovementRequest request) {
        StockMovement entity = new StockMovement();
        entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Establishment not found: " + request.establishmentId())));
        entity.setLot(lotRepository.findById(request.lotId())
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found: " + request.lotId())));
        entity.setType(request.type());
        entity.setQuantity(request.quantity());
        entity.setBalanceAfter(request.balanceAfter());
        entity.setReferenceTable(request.referenceTable());
        entity.setReferenceId(request.referenceId());
        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId()).orElse(null));
        }

        return new StockMovementResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getAll() {
        return repository.findAll().stream()
                .map(StockMovementResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getByProduct(Long productId) {
        return repository.findByLotProductIdOrderByCreatedAtDesc(productId).stream()
                .map(StockMovementResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getByEstablishment(Long establishmentId) {
        return repository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId).stream()
                .map(StockMovementResponse::new)
                .collect(Collectors.toList());
    }
}
