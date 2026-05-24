package com.medhelp.common.order;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import com.medhelp.common.medhelp.Baseentity;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem extends Baseentity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    // Price is snapshotted at order time — catalog price changes won't affect past orders
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ItemStatus status = ItemStatus.PENDING;

    public enum ItemStatus { PENDING, PROCESSING, COMPLETED }
}