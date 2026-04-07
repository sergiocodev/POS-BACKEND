package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;
import com.sergiocodev.app.model.AccountReceivable;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.repository.AccountReceivableRepository;
import com.sergiocodev.app.repository.CustomerRepository;
import com.sergiocodev.app.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountReceivableServiceImpl implements AccountReceivableService {

    private final AccountReceivableRepository repository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public AccountReceivableResponse create(AccountReceivableRequest request) {
        Sale sale = saleRepository.findById(request.saleId())
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (repository.findBySaleId(sale.getId()).isPresent()) {
            throw new RuntimeException("Account Receivable already exists for this sale");
        }

        AccountReceivable receivable = new AccountReceivable();
        receivable.setSale(sale);
        receivable.setCustomer(customer);
        receivable.setTotalAmount(request.totalAmount());
        receivable.setAmountPaid(BigDecimal.ZERO);
        receivable.setPendingBalance(request.totalAmount());
        receivable.setStatus(AccountReceivable.ReceivableStatus.PENDING);
        receivable.setDueDate(request.dueDate());
        receivable.setNotes(request.notes());

        return mapToResponse(repository.save(receivable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountReceivableResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountReceivableResponse> getByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountReceivableResponse getById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Account Receivable not found"));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        AccountReceivable receivable = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account Receivable not found"));

        receivable.setStatus(AccountReceivable.ReceivableStatus.CANCELED);
        repository.save(receivable);
    }

    private AccountReceivableResponse mapToResponse(AccountReceivable entity) {
        return new AccountReceivableResponse(
                entity.getId(),
                entity.getSale().getId(),
                entity.getCustomer().getName(),
                entity.getTotalAmount(),
                entity.getAmountPaid(),
                entity.getPendingBalance(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
