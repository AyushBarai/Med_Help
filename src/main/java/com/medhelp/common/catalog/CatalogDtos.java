package com.medhelp.common.catalog;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class CatalogDtos {

    public record TestCatalogRequest(
            @NotBlank(message = "Test name is required")
            String name,

            String code,
            String category,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.01", message = "Price must be greater than 0")
            BigDecimal price,

            @NotNull(message = "Turnaround hours is required")
            @Min(value = 1, message = "Turnaround must be at least 1 hour")
            Integer turnaroundHours,

            String sampleType,
            String referenceRanges   // raw JSON string
    ) {}

    public record TestCatalogResponse(
            UUID id,
            String name,
            String code,
            String category,
            BigDecimal price,
            Integer turnaroundHours,
            String sampleType,
            String referenceRanges,
            boolean isActive
    ) {
        public static TestCatalogResponse from(TestCatalog t) {
            return new TestCatalogResponse(
                    t.getId(), t.getName(), t.getCode(), t.getCategory(),
                    t.getPrice(), t.getTurnaroundHours(), t.getSampleType(),
                    t.getReferenceRanges(), t.isActive()
            );
        }
    }
}