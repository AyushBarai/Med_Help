package com.medhelp.common.order;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.medhelp.common.medhelp.Baseentity;

@Entity
@Table(name = "test_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestOrder extends Baseentity {

    @Column(name = "lab_id", nullable = false)
    private UUID labId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(nullable = false, unique = true)
    private String orderNumber;      // "LAB-20240115-0001"

    private String referredBy;       // doctor who referred the patient

    @Column(name = "collected_by")
    private UUID collectedBy;        // staff member who collected sample

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CollectionType collectionType = CollectionType.WALK_IN;

    private String collectionAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.REGISTERED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime expectedAt;   // based on max TAT of selected tests
    private LocalDateTime deliveredAt;

    public enum CollectionType { WALK_IN, HOME_COLLECTION }

    public enum OrderStatus {
        REGISTERED,
        SAMPLE_COLLECTED,
        PROCESSING,
        REPORT_READY,
        DELIVERED;

        // State machine: defines which transitions are allowed
        public boolean canTransitionTo(OrderStatus next) {
            return switch (this) {
                case REGISTERED       -> next == SAMPLE_COLLECTED;
                case SAMPLE_COLLECTED -> next == PROCESSING;
                case PROCESSING       -> next == REPORT_READY;
                case REPORT_READY     -> next == DELIVERED;
                case DELIVERED        -> false;
            };
        }
    }
}