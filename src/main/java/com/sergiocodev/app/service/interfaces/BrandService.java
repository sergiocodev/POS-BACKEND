package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;

import java.util.List;

public interface BrandService {

    BrandResponse createNewBrand(BrandRequest request);

    List<BrandResponse> getAllBrands();

    BrandResponse getBrandById(Long id);

    BrandResponse updateBrandById(Long id, BrandRequest request);

    void deleteBrandById(Long id);
}
