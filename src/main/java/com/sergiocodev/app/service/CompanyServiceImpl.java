package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.company.CompanyRequest;
import com.sergiocodev.app.dto.company.CompanyResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.model.Company;
import com.sergiocodev.app.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompany() {
        Company company = companyRepository.findMainCompany()
                .orElseThrow(() -> new ResourceNotFoundException("Company not configured"));
        return mapToResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(CompanyRequest request) {
        Company company = companyRepository.findMainCompany()
                .orElse(new Company());

        company.setRuc(request.ruc());
        company.setName(request.name());
        company.setAddress(request.address());
        company.setUbigeo(request.ubigeo());
        company.setUrbanization(request.urbanization());
        company.setPhone(request.phone());
        company.setEmail(request.email());
        company.setLogoUrl(request.logoUrl());

        return mapToResponse(companyRepository.save(company));
    }

    @Override
    public boolean isConfigured() {
        return companyRepository.count() > 0;
    }

    private CompanyResponse mapToResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getRuc(),
                company.getName(),
                company.getAddress(),
                company.getUbigeo(),
                company.getUrbanization(),
                company.getPhone(),
                company.getEmail(),
                company.getLogoUrl(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
