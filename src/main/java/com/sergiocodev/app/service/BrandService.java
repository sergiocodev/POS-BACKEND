package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;

import java.util.List;

public interface BrandService {

    BrandResponse create(BrandRequest request);

    List<BrandResponse> getAll();

    BrandResponse getById(Long id);

    BrandResponse update(Long id, BrandRequest request);

    void delete(Long id);
}
