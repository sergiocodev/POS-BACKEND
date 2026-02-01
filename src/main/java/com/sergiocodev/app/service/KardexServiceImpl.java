package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.kardex.KardexResponse;
import com.sergiocodev.app.repository.KardexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KardexServiceImpl implements KardexService {

    private final KardexRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponse> getByProductId(Long productId) {
        return repository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(KardexResponse::new)
                .collect(Collectors.toList());
    }
}
