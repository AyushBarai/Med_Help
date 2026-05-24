package com.medhelp.common.order;

import com.medhelp.common.order.OrderDtos.*;
import com.medhelp.common.order.TestOrder;
import com.medhelp.common.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Create and manage test orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new test order for a patient")
    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication auth) {
        UUID currentUserId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(request, currentUserId));
    }

    @Operation(summary = "List all orders — filter by status, paginated")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAll(
            @RequestParam(required = false) TestOrder.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAll(status, page, size));
    }

    @Operation(summary = "Get a single order with all items and results")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Advance order status — follows state machine rules")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
}