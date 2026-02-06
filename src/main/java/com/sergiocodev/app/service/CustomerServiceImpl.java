package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.customer.CustomerRequest;
import com.sergiocodev.app.dto.customer.CustomerResponse;
import com.sergiocodev.app.exception.CustomerNotFoundException;
import com.sergiocodev.app.exception.DuplicateDocumentException;
import com.sergiocodev.app.mapper.CustomerMapper;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new DuplicateDocumentException(
                    "Document number '" + request.documentNumber() + "' already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        if (!request.documentNumber().equals(customer.getDocumentNumber()) &&
                customerRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new DuplicateDocumentException(
                    "Document number '" + request.documentNumber() + "' already exists");
        }

        customerMapper.updateEntity(request, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }
}
