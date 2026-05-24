package com.medhelp.common.catalog;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import com.medhelp.common.medhelp.Baseentity;

@Entity
@Table(name = "test_catalog")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestCatalog extends Baseentity {

    @Column(name = "lab_id", nullable = false)
    private UUID labId;

    @Column(nullable = false)
    private String name;           // "Complete Blood Count"

    private String code;           // "CBC"
    private String category;       // "Hematology"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer turnaroundHours = 24;

    private String sampleType;     // "Blood", "Urine", "Stool"

    // Stored as JSONB in PostgreSQL — free-form reference ranges per parameter
    // Example: {"Hemoglobin": {"min": 12, "max": 17, "unit": "g/dL"}}
    @Column(columnDefinition = "jsonb")
    private String referenceRanges;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
}