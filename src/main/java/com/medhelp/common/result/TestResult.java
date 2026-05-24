package com.medhelp.common.result;

import com.medhelp.common.medhelp.Baseentity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "test_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestResult extends Baseentity {

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(nullable = false)
    private String parameterName;   // "Hemoglobin"

    private String value;           // "13.5"
    private String unit;            // "g/dL"
    private String referenceRange;  // "12.0 - 17.0"

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResultFlag flag = ResultFlag.NORMAL;

    @Column(name = "entered_by")
    private UUID enteredBy;

    public enum ResultFlag { NORMAL, LOW, HIGH, CRITICAL }
}