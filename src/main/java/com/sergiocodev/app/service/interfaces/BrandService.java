package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {

    BrandResponse createNewBrand(BrandRequest request);

    List<BrandResponse> getAllBrands();

    Page<BrandResponse> findAllPaged(String name, Pageable pageable);

    BrandResponse getBrandById(Long id);

    BrandResponse updateBrandById(Long id, BrandRequest request);

    void deleteBrandById(Long id);
}
