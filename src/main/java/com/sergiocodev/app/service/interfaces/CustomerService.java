package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.customer.CustomerDashboardResponse;
import com.sergiocodev.app.dto.customer.CustomerRequest;
import com.sergiocodev.app.dto.customer.CustomerResponse;

import com.sergiocodev.app.dto.customer.CustomerSummaryResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    List<CustomerResponse> getAll();

    Page<CustomerResponse> getAllPaged(String name, String documentNumber, String email, String phone, Pageable pageable);

    CustomerResponse getById(Long id);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);

    CustomerResponse findByDocumentNumber(String documentNumber);

    List<CustomerSummaryResponse> getSummary(Long establishmentId);
}
