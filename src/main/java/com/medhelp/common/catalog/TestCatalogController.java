package com.medhelp.common.catalog;

import com.medhelp.common.catalog.CatalogDtos.*;
import com.medhelp.common.catalog.TestCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
@Tag(name = "Test Catalog", description = "Manage lab's test offerings and pricing")
public class TestCatalogController {

    private final TestCatalogService service;

    @Operation(summary = "List all active tests in this lab")
    @GetMapping
    public ResponseEntity<List<TestCatalogResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Get a single test by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TestCatalogResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Add a new test to the catalog")
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DOCTOR')")
    public ResponseEntity<TestCatalogResponse> create(@Valid @RequestBody TestCatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update an existing test")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'DOCTOR')")
    public ResponseEntity<TestCatalogResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TestCatalogRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Deactivate a test (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}