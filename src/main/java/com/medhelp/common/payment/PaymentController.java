package com.medhelp.common.payment;

import com.medhelp.common.payment.PaymentDtos.*;
import com.medhelp.common.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Record and track payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Record a cash/UPI/card payment for an order")
    @PostMapping
    public ResponseEntity<PaymentResponse> record(@Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.record(request));
    }

    @Operation(summary = "Get all payments for an order")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getByOrder(orderId));
    }

    @Operation(summary = "List all pending payments across the lab")
    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPending() {
        return ResponseEntity.ok(paymentService.getPending());
    }
}