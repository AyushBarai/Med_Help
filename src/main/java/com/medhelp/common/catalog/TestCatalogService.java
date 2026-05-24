package com.medhelp.common.catalog;

import com.medhelp.common.catalog.CatalogDtos.*;
import com.medhelp.common.catalog.TestCatalog;
import com.medhelp.common.catalog.TestCatalogRepository;
import com.medhelp.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medhelp.common.tenant.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCatalogService {

    private final TestCatalogRepository repository;

    public List<TestCatalogResponse> getAll() {
        return repository.findAllByLabIdAndIsActiveTrue(TenantContext.get())
                .stream().map(TestCatalogResponse::from).toList();
    }

    public TestCatalogResponse getById(UUID id) {
        return TestCatalogResponse.from(
                repository.findByIdAndLabId(id, TenantContext.get())
                        .orElseThrow(() -> new ResourceNotFoundException("Test", id))
        );
    }

    @Transactional
    public TestCatalogResponse create(TestCatalogRequest request) {
        TestCatalog test = TestCatalog.builder()
                .labId(TenantContext.get())
                .name(request.name())
                .code(request.code())
                .category(request.category())
                .price(request.price())
                .turnaroundHours(request.turnaroundHours())
                .sampleType(request.sampleType())
                .referenceRanges(request.referenceRanges())
                .build();
        return TestCatalogResponse.from(repository.save(test));
    }

    @Transactional
    public TestCatalogResponse update(UUID id, TestCatalogRequest request) {
        TestCatalog test = repository.findByIdAndLabId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Test", id));
        test.setName(request.name());
        test.setCode(request.code());
        test.setCategory(request.category());
        test.setPrice(request.price());
        test.setTurnaroundHours(request.turnaroundHours());
        test.setSampleType(request.sampleType());
        test.setReferenceRanges(request.referenceRanges());
        return TestCatalogResponse.from(repository.save(test));
    }

    @Transactional
    public void deactivate(UUID id) {
        TestCatalog test = repository.findByIdAndLabId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Test", id));
        test.setActive(false);
        repository.save(test);
    }
}