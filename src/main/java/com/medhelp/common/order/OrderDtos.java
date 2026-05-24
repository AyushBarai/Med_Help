package com.medhelp.common.order;

import com.medhelp.common.catalog.CatalogDtos.TestCatalogResponse;
import com.medhelp.common.order.*;
import com.medhelp.common.patient.PatientDtos.PatientResponse;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderDtos {

    // ---- CREATE ORDER ----

    public record CreateOrderRequest(
            @NotNull(message = "Patient ID is required")
            UUID patientId,

            @NotEmpty(message = "At least one test must be selected")
            List<UUID> testIds,

            String referredBy,

            TestOrder.CollectionType collectionType,
            String collectionAddress,

            BigDecimal discountAmount,
            String notes
    ) {}

    // ---- UPDATE STATUS ----

    public record UpdateStatusRequest(
            @NotNull TestOrder.OrderStatus status
    ) {}

    // ---- RESPONSES ----

    public record OrderItemResponse(
            UUID id,
            UUID testId,
            String testName,
            BigDecimal price,
            String status
    ) {
        public static OrderItemResponse from(OrderItem item, String testName) {
            return new OrderItemResponse(
                    item.getOrderId(), item.getTestId(), testName,
                    item.getPrice(), item.getStatus().name()
            );
        }
    }

    public record OrderResponse(
            UUID id,
            String orderNumber,
            String status,
            String collectionType,
            String referredBy,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal netAmount,
            String expectedAt,
            String deliveredAt,
            String notes,
            String createdAt,
            PatientResponse patient,
            List<OrderItemResponse> items
    ) {}
}