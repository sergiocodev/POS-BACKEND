package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.kardex.KardexResponse;
import java.util.List;

public interface KardexService {
    List<KardexResponse> getByProductId(Long productId);
}
