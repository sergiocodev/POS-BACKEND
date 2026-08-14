package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.CustomerService;

import com.sergiocodev.app.dto.customer.CustomerDashboardResponse;
import com.sergiocodev.app.dto.customer.CustomerDashboardResponse.*;
import com.sergiocodev.app.dto.customer.CustomerRequest;
import com.sergiocodev.app.dto.customer.CustomerResponse;
import com.sergiocodev.app.exception.CustomerNotFoundException;
import com.sergiocodev.app.exception.DuplicateDocumentException;
import com.sergiocodev.app.mapper.CustomerMapper;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

        private final CustomerRepository customerRepository;
        private final CustomerMapper customerMapper;

        private static final String[] AVATAR_COLORS = {
                        "#10b981", "#3b82f6", "#8b5cf6", "#f59e0b",
                        "#ec4899", "#14b8a6", "#f87171", "#6366f1"
        };

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
        public Page<CustomerResponse> getAllPaged(String name, String documentNumber, String email, String phone,
                        Pageable pageable) {
                Specification<Customer> spec = (root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        if (name != null && !name.isBlank()) {
                                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                        }
                        if (documentNumber != null && !documentNumber.isBlank()) {
                                predicates.add(cb.like(root.get("documentNumber"), "%" + documentNumber + "%"));
                        }
                        if (email != null && !email.isBlank()) {
                                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
                        }
                        if (phone != null && !phone.isBlank()) {
                                predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
                        }

                        return cb.and(predicates.toArray(new Predicate[0]));
                };

                return customerRepository.findAll(spec, pageable)
                                .map(customerMapper::toResponse);
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

        @Override
        @Transactional(readOnly = true)
        public CustomerResponse findByDocumentNumber(String documentNumber) {
                Customer customer = customerRepository.findByDocumentNumber(documentNumber)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with document number: " + documentNumber));
                return customerMapper.toResponse(customer);
        }

        @Override
        @Transactional(readOnly = true)
        public List<com.sergiocodev.app.dto.customer.CustomerSummaryResponse> getSummary(Long establishmentId) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime prevMonthStart = monthStart.minusMonths(1);
                LocalDateTime prevMonthEnd = monthStart.minusSeconds(1);
                
                // Current KPIs
                long totalCustomers = customerRepository.countTotalCustomersByEstablishment(establishmentId);
                long activeCustomers = customerRepository.countActiveCustomersByEstablishment(establishmentId, monthStart, now);
                BigDecimal totalSalesAmount = customerRepository.sumTotalSalesByEstablishment(establishmentId, monthStart, now);
                long salesCount = customerRepository.countTotalSalesByEstablishment(establishmentId, monthStart, now);
                BigDecimal averageTicket = salesCount > 0 ? totalSalesAmount.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                // Previous KPIs
                long prevTotalCustomers = customerRepository.countTotalCustomersByEstablishmentUpTo(establishmentId, prevMonthEnd);
                long prevActiveCustomers = customerRepository.countActiveCustomersByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
                BigDecimal prevTotalSalesAmount = customerRepository.sumTotalSalesByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
                long prevSalesCount = customerRepository.countTotalSalesByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
                BigDecimal prevAverageTicket = prevSalesCount > 0 ? prevTotalSalesAmount.divide(BigDecimal.valueOf(prevSalesCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        
                return List.of(
                        new com.sergiocodev.app.dto.customer.CustomerSummaryResponse(
                                "CLIENTES TOTALES",
                                String.valueOf(totalCustomers),
                                null, null,
                                calculateTrend(BigDecimal.valueOf(totalCustomers), BigDecimal.valueOf(prevTotalCustomers)),
                                calculateDirection(BigDecimal.valueOf(totalCustomers), BigDecimal.valueOf(prevTotalCustomers)),
                                "vs. mes ant."
                        ),
                        new com.sergiocodev.app.dto.customer.CustomerSummaryResponse(
                                "CLIENTES ACTIVOS",
                                String.valueOf(activeCustomers),
                                null, null,
                                calculateTrend(BigDecimal.valueOf(activeCustomers), BigDecimal.valueOf(prevActiveCustomers)),
                                calculateDirection(BigDecimal.valueOf(activeCustomers), BigDecimal.valueOf(prevActiveCustomers)),
                                "vs. mes ant."
                        ),
                        new com.sergiocodev.app.dto.customer.CustomerSummaryResponse(
                                "VENTAS A CLIENTES",
                                totalSalesAmount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                                "S/ ", null,
                                calculateTrend(totalSalesAmount, prevTotalSalesAmount),
                                calculateDirection(totalSalesAmount, prevTotalSalesAmount),
                                "vs. mes ant."
                        ),
                        new com.sergiocodev.app.dto.customer.CustomerSummaryResponse(
                                "TICKET PROMEDIO",
                                averageTicket.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                                "S/ ", null,
                                calculateTrend(averageTicket, prevAverageTicket),
                                calculateDirection(averageTicket, prevAverageTicket),
                                "vs. mes ant."
                        )
                );
        }

        private String calculateTrend(BigDecimal current, BigDecimal previous) {
                if (previous.compareTo(BigDecimal.ZERO) == 0) {
                        return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0.0%";
                }
                BigDecimal diff = current.subtract(previous);
                BigDecimal percent = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                String sign = percent.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                return sign + percent.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
        }

        private String calculateDirection(BigDecimal current, BigDecimal previous) {
                int cmp = current.compareTo(previous);
                if (cmp == 0) return "neutral";
                return cmp > 0 ? "up" : "down";
        }
}
