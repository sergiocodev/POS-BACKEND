package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.maintenance.UnitOfMeasureRequest;
import com.sergiocodev.app.dto.maintenance.UnitOfMeasureResponse;
import com.sergiocodev.app.service.interfaces.UnitOfMeasureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/units-of-measure")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;

    @PostMapping
    public ResponseEntity<ResponseApi<UnitOfMeasureResponse>> create(@Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasureResponse response = unitOfMeasureService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(response, "Unit of Measure created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<UnitOfMeasureResponse>> update(@PathVariable Long id, @Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasureResponse response = unitOfMeasureService.update(id, request);
        return ResponseEntity.ok(ResponseApi.success(response, "Unit of Measure updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        unitOfMeasureService.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Unit of Measure deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<UnitOfMeasureResponse>> getById(@PathVariable Long id) {
        UnitOfMeasureResponse response = unitOfMeasureService.getById(id);
        return ResponseEntity.ok(ResponseApi.success(response, "Unit of Measure retrieved successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseApi<List<UnitOfMeasureResponse>>> getAll() {
        List<UnitOfMeasureResponse> response = unitOfMeasureService.getAll();
        return ResponseEntity.ok(ResponseApi.success(response, "Units of Measure retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ResponseApi<Page<UnitOfMeasureResponse>>> getAllPaginated(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<UnitOfMeasureResponse> response = unitOfMeasureService.getAllPaginated(pageable, search);
        return ResponseEntity.ok(ResponseApi.success(response, "Units of Measure retrieved successfully"));
    }
}
