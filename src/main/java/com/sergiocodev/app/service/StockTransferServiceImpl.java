package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.stocktransfer.StockTransferItemRequest;
import com.sergiocodev.app.dto.stocktransfer.StockTransferItemResponse;
import com.sergiocodev.app.dto.stocktransfer.StockTransferRequest;
import com.sergiocodev.app.dto.stocktransfer.StockTransferResponse;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {

        private final StockTransferRepository repository;
        private final EstablishmentRepository establishmentRepository;
        private final ProductRepository productRepository;
        private final ProductLotRepository lotRepository;
        private final UserRepository userRepository;
        private final InventoryRepository inventoryRepository;
        private final StockMovementRepository stockMovementRepository;

        @Override
        @Transactional
        public StockTransferResponse create(StockTransferRequest request, Long userId) {
                Establishment source = establishmentRepository.findById(request.sourceEstablishmentId())
                                .orElseThrow(() -> new RuntimeException("Source establishment not found"));
                Establishment target = establishmentRepository.findById(request.targetEstablishmentId())
                                .orElseThrow(() -> new RuntimeException("Target establishment not found"));
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (source.getId().equals(target.getId())) {
                        throw new RuntimeException("Source and target establishment cannot be the same");
                }

                StockTransfer transfer = new StockTransfer();
                transfer.setSourceEstablishment(source);
                transfer.setTargetEstablishment(target);
                transfer.setUser(user);

                for (StockTransferItemRequest ir : request.items()) {
                        Product product = productRepository.findById(ir.productId())
                                        .orElseThrow(() -> new RuntimeException("Product not found"));
                        ProductLot lot = lotRepository.findById(ir.lotId())
                                        .orElseThrow(() -> new RuntimeException("Lot not found"));

                        // Validate source inventory
                        Inventory sourceInventory = inventoryRepository
                                        .findByEstablishmentIdAndLotId(source.getId(), lot.getId())
                                        .orElseThrow(() -> new RuntimeException("Product not in source inventory"));

                        if (sourceInventory.getQuantity().compareTo(ir.quantity()) < 0) {
                                throw new RuntimeException(
                                                "Insufficient stock in source establishment for product: "
                                                                + product.getTradeName());
                        }

                        StockTransferItem item = new StockTransferItem();
                        item.setStockTransfer(transfer);
                        item.setProduct(product);
                        item.setLot(lot);
                        item.setQuantity(ir.quantity());
                        transfer.getItems().add(item);
                }

                return mapToResponse(repository.save(transfer));
        }

        @Override
        @Transactional(readOnly = true)
        public List<StockTransferResponse> getBySourceEstablishmentId(Long establishmentId) {
                return repository.findBySourceEstablishmentId(establishmentId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<StockTransferResponse> getByTargetEstablishmentId(Long establishmentId) {
                return repository.findByTargetEstablishmentId(establishmentId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public StockTransferResponse getById(Long id) {
                return repository.findById(id)
                                .map(this::mapToResponse)
                                .orElseThrow(() -> new RuntimeException("Stock transfer not found"));
        }

        @Override
        @Transactional
        public StockTransferResponse dispatchTransfer(Long id, Long userId) {
                StockTransfer transfer = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Stock transfer not found"));
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (transfer.getStatus() != StockTransfer.TransferStatus.PENDING) {
                        throw new RuntimeException("Only PENDING transfers can be dispatched");
                }

                // Deduct from source
                for (StockTransferItem item : transfer.getItems()) {
                        Inventory sourceInventory = inventoryRepository.findByEstablishmentIdAndLotId(
                                        transfer.getSourceEstablishment().getId(), item.getLot().getId())
                                        .orElseThrow(() -> new RuntimeException("Inventory not found"));

                        if (sourceInventory.getQuantity().compareTo(item.getQuantity()) < 0) {
                                throw new RuntimeException("Insufficient stock in source establishment");
                        }

                        sourceInventory.setQuantity(sourceInventory.getQuantity().subtract(item.getQuantity()));
                        inventoryRepository.save(sourceInventory);

                        createStockMovement(transfer.getSourceEstablishment(), item.getLot(),
                                        StockMovement.MovementType.TRANSFER_OUT, item.getQuantity().negate(),
                                        sourceInventory.getQuantity(), transfer, user);
                }

                transfer.setStatus(StockTransfer.TransferStatus.IN_TRANSIT);
                transfer.setSentAt(LocalDateTime.now());
                return mapToResponse(repository.save(transfer));
        }

        @Override
        @Transactional
        public StockTransferResponse receiveTransfer(Long id, Long userId) {
                StockTransfer transfer = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Stock transfer not found"));
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (transfer.getStatus() != StockTransfer.TransferStatus.IN_TRANSIT) {
                        throw new RuntimeException("Only IN_TRANSIT transfers can be received");
                }

                // Add to target
                for (StockTransferItem item : transfer.getItems()) {
                        Inventory targetInventory = inventoryRepository.findByEstablishmentIdAndLotId(
                                        transfer.getTargetEstablishment().getId(), item.getLot().getId())
                                        .orElseGet(() -> {
                                                Inventory newInv = new Inventory();
                                                newInv.setEstablishment(transfer.getTargetEstablishment());
                                                newInv.setLot(item.getLot());
                                                newInv.setQuantity(BigDecimal.ZERO);
                                                // Copiamos el costo del source
                                                Inventory sourceInv = inventoryRepository.findByEstablishmentIdAndLotId(
                                                                transfer.getSourceEstablishment().getId(),
                                                                item.getLot().getId()).orElse(null);
                                                newInv.setCostPrice(sourceInv != null ? sourceInv.getCostPrice()
                                                                : BigDecimal.ZERO);
                                                return newInv;
                                        });

                        targetInventory.setQuantity(targetInventory.getQuantity().add(item.getQuantity()));
                        inventoryRepository.save(targetInventory);

                        createStockMovement(transfer.getTargetEstablishment(), item.getLot(),
                                        StockMovement.MovementType.TRANSFER_IN, item.getQuantity(),
                                        targetInventory.getQuantity(), transfer, user);
                }

                transfer.setStatus(StockTransfer.TransferStatus.COMPLETED);
                transfer.setReceivedAt(LocalDateTime.now());
                return mapToResponse(repository.save(transfer));
        }

        @Override
        @Transactional
        public StockTransferResponse cancelTransfer(Long id, Long userId) {
                StockTransfer transfer = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Stock transfer not found"));

                if (transfer.getStatus() == StockTransfer.TransferStatus.COMPLETED) {
                        throw new RuntimeException("Cannot cancel COMPLETED transfers");
                }

                if (transfer.getStatus() == StockTransfer.TransferStatus.IN_TRANSIT) {
                        // Revert deduction from source
                        User user = userRepository.findById(userId)
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        for (StockTransferItem item : transfer.getItems()) {
                                Inventory sourceInventory = inventoryRepository.findByEstablishmentIdAndLotId(
                                                transfer.getSourceEstablishment().getId(), item.getLot().getId())
                                                .orElseThrow(() -> new RuntimeException("Inventory not found"));

                                sourceInventory.setQuantity(sourceInventory.getQuantity().add(item.getQuantity()));
                                inventoryRepository.save(sourceInventory);

                                createStockMovement(transfer.getSourceEstablishment(), item.getLot(),
                                                StockMovement.MovementType.TRANSFER_IN, item.getQuantity(),
                                                sourceInventory.getQuantity(), transfer, user);
                        }
                }

                transfer.setStatus(StockTransfer.TransferStatus.CANCELED);
                return mapToResponse(repository.save(transfer));
        }

        private void createStockMovement(Establishment establishment, ProductLot lot,
                        StockMovement.MovementType type, BigDecimal amount, BigDecimal balanceAfter,
                        StockTransfer transfer, User user) {
                StockMovement movement = new StockMovement();
                movement.setEstablishment(establishment);
                movement.setLot(lot);
                movement.setType(type);
                movement.setQuantity(amount);
                movement.setBalanceAfter(balanceAfter);
                movement.setReferenceTable("stock_transfers");
                movement.setReferenceId(transfer.getId());
                movement.setUser(user);
                movement.setCreatedAt(LocalDateTime.now());
                stockMovementRepository.save(movement);
        }

        private StockTransferResponse mapToResponse(StockTransfer transfer) {
                return new StockTransferResponse(
                                transfer.getId(),
                                String.format("TR-%08d", transfer.getId()), // Pseudo transferNumber
                                transfer.getSourceEstablishment().getId(),
                                transfer.getSourceEstablishment().getName(),
                                transfer.getTargetEstablishment().getId(),
                                transfer.getTargetEstablishment().getName(),
                                transfer.getStatus(),
                                transfer.getUser().getId(),
                                transfer.getUser().getUsername(),
                                null,
                                null,
                                null,
                                null,
                                transfer.getCreatedAt(),
                                transfer.getSentAt(),
                                transfer.getReceivedAt(),
                                transfer.getNotes(),
                                transfer.getItems().stream()
                                                .map(i -> new StockTransferItemResponse(
                                                                i.getId(),
                                                                transfer.getId(),
                                                                i.getProduct().getId(),
                                                                i.getProduct().getTradeName(),
                                                                i.getLot().getId(),
                                                                i.getLot().getLotCode(),
                                                                i.getQuantity()))
                                                .collect(Collectors.toList()));
        }
}
