package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.SupplierService;

import com.sergiocodev.app.dto.supplier.SupplierRequest;
import com.sergiocodev.app.dto.supplier.SupplierResponse;
import com.sergiocodev.app.model.Supplier;
import com.sergiocodev.app.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier entity = new Supplier();
        entity.setName(request.name());
        entity.setRuc(request.ruc());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setAddress(request.address());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.contactName() != null) entity.setContactName(request.contactName());
        if (request.status() != null) entity.setStatus(request.status());
        if (request.rating() != null) entity.setRating(request.rating());
        return new SupplierResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {
        return repository.findAll().stream()
                .map(SupplierResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        return repository.findById(id)
                .map(SupplierResponse::new)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        entity.setName(request.name());
        entity.setRuc(request.ruc());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setAddress(request.address());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.contactName() != null) entity.setContactName(request.contactName());
        if (request.status() != null) entity.setStatus(request.status());
        if (request.rating() != null) entity.setRating(request.rating());
        return new SupplierResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.sergiocodev.app.dto.supplier.SupplierDetailResponse> getSupplierDetailsPaged(String providerInfo, String category, String contactInfo, org.springframework.data.domain.Pageable pageable) {
        return repository.getSupplierDetailsPaged(providerInfo, category, contactInfo, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.sergiocodev.app.dto.supplier.SupplierSummaryResponse> getSummary(Long establishmentId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime prevMonthStart = monthStart.minusMonths(1);
        java.time.LocalDateTime prevMonthEnd = monthStart.minusSeconds(1);

        // Current KPIs
        long activeSuppliers = repository.countActiveSuppliersByEstablishment(establishmentId, monthStart, now);
        long evalSuppliers = repository.countEvaluatingSuppliersByEstablishment(establishmentId, monthStart, now);
        long expSuppliers = repository.countExpiredSuppliersByEstablishment(establishmentId, monthStart, now);
        java.math.BigDecimal totalSpend = repository.sumTotalSpendByEstablishment(establishmentId, monthStart, now);
        java.math.BigDecimal averageRating = repository.calculateAverageRatingByEstablishment(establishmentId, monthStart, now);

        // Previous KPIs
        long prevActiveSuppliers = repository.countActiveSuppliersByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
        long prevEvalSuppliers = repository.countEvaluatingSuppliersByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
        long prevExpSuppliers = repository.countExpiredSuppliersByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
        java.math.BigDecimal prevTotalSpend = repository.sumTotalSpendByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);
        java.math.BigDecimal prevAverageRating = repository.calculateAverageRatingByEstablishment(establishmentId, prevMonthStart, prevMonthEnd);

        return java.util.List.of(
            new com.sergiocodev.app.dto.supplier.SupplierSummaryResponse(
                "PROVEEDORES ACTIVOS",
                String.valueOf(activeSuppliers),
                null, null,
                calculateTrend(java.math.BigDecimal.valueOf(activeSuppliers), java.math.BigDecimal.valueOf(prevActiveSuppliers)),
                calculateDirection(java.math.BigDecimal.valueOf(activeSuppliers), java.math.BigDecimal.valueOf(prevActiveSuppliers), false),
                "vs. mes ant."
            ),
            new com.sergiocodev.app.dto.supplier.SupplierSummaryResponse(
                "EN EVALUACIÓN",
                String.valueOf(evalSuppliers),
                null, null,
                calculateTrend(java.math.BigDecimal.valueOf(evalSuppliers), java.math.BigDecimal.valueOf(prevEvalSuppliers)),
                calculateDirection(java.math.BigDecimal.valueOf(evalSuppliers), java.math.BigDecimal.valueOf(prevEvalSuppliers), true),
                "vs. mes ant."
            ),
            new com.sergiocodev.app.dto.supplier.SupplierSummaryResponse(
                "VENCIDOS",
                String.valueOf(expSuppliers),
                null, null,
                calculateTrend(java.math.BigDecimal.valueOf(expSuppliers), java.math.BigDecimal.valueOf(prevExpSuppliers)),
                calculateDirection(java.math.BigDecimal.valueOf(expSuppliers), java.math.BigDecimal.valueOf(prevExpSuppliers), true),
                "vs. mes ant."
            ),
            new com.sergiocodev.app.dto.supplier.SupplierSummaryResponse(
                "GASTO MENSUAL",
                totalSpend.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                "S/ ", null,
                calculateTrend(totalSpend, prevTotalSpend),
                calculateDirection(totalSpend, prevTotalSpend, false),
                "vs. mes ant."
            ),
            new com.sergiocodev.app.dto.supplier.SupplierSummaryResponse(
                "RATING PROMEDIO",
                averageRating.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString(),
                null, " / 5",
                calculateTrend(averageRating, prevAverageRating),
                calculateDirection(averageRating, prevAverageRating, false),
                "vs. mes ant."
            )
        );
    }

    private String calculateTrend(java.math.BigDecimal current, java.math.BigDecimal previous) {
        if (previous.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return current.compareTo(java.math.BigDecimal.ZERO) > 0 ? "+100%" : "0.0%";
        }
        java.math.BigDecimal diff = current.subtract(previous);
        java.math.BigDecimal percent = diff.divide(previous, 4, java.math.RoundingMode.HALF_UP).multiply(new java.math.BigDecimal("100"));
        String sign = percent.compareTo(java.math.BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + percent.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String calculateDirection(java.math.BigDecimal current, java.math.BigDecimal previous, boolean invert) {
        int cmp = current.compareTo(previous);
        if (cmp == 0) return "neutral";
        if (invert) return cmp > 0 ? "down" : "up"; // Increased expired/eval is bad
        return cmp > 0 ? "up" : "down";
    }
}
