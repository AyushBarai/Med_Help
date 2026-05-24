package com.medhelp.common.result;

import com.medhelp.common.result.ResultDtos.BulkResultRequest;
import com.medhelp.common.result.ResultDtos.ResultResponse;
import com.medhelp.common.result.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/results")
@RequiredArgsConstructor
@Tag(name = "Results", description = "Enter and view test results")
public class ResultController {

    private final ResultService resultService;

    @Operation(summary = "Bulk save results for all tests in an order")
    @PostMapping
    public ResponseEntity<List<ResultResponse>> save(
            @PathVariable UUID orderId,
            @Valid @RequestBody BulkResultRequest request,
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(resultService.saveResults(orderId, request, userId));
    }

    @Operation(summary = "Get results for a specific order item")
    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<List<ResultResponse>> getByItem(@PathVariable UUID orderItemId) {
        return ResponseEntity.ok(resultService.getByOrderItem(orderItemId));
    }
}