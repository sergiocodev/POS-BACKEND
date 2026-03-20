package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.documentsequence.DocumentSequenceRequest;
import com.sergiocodev.app.dto.documentsequence.DocumentSequenceResponse;
import com.sergiocodev.app.service.DocumentSequenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/document-sequences")
@RequiredArgsConstructor
public class DocumentSequenceController {

    private final DocumentSequenceService service;

    @PostMapping
    public ResponseEntity<DocumentSequenceResponse> create(@Valid @RequestBody DocumentSequenceRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DocumentSequenceResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentSequenceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentSequenceResponse> update(@PathVariable Long id,
            @Valid @RequestBody DocumentSequenceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
