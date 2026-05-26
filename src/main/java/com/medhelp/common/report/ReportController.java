package com.medhelp.common.report;

import com.medhelp.common.report.ReportDtos.DeliverRequest;
import com.medhelp.common.report.ReportDtos.ReportResponse;
import com.medhelp.common.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Generate and deliver lab reports as PDF")
public class ReportController {

    private final ReportService reportService;

    // ---- Protected endpoints (require JWT) ----

    @Operation(summary = "Generate a PDF report for an order")
    @PostMapping("/api/v1/orders/{orderId}/report/generate")
    public ResponseEntity<ReportResponse> generate(
            @PathVariable UUID orderId,
            Authentication auth) throws IOException {

        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.generate(orderId, userId));
    }

    @Operation(summary = "Deliver the report to patient via WhatsApp/SMS/email")
    @PostMapping("/api/v1/orders/{orderId}/report/deliver")
    public ResponseEntity<ReportResponse> deliver(
            @PathVariable UUID orderId,
            @Valid @RequestBody DeliverRequest request) {

        return ResponseEntity.ok(reportService.deliver(orderId, request));
    }

    @Operation(summary = "List all report versions for an order")
    @GetMapping("/api/v1/orders/{orderId}/reports")
    public ResponseEntity<List<ReportResponse>> listByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(reportService.getByOrder(orderId));
    }

    // ---- PUBLIC endpoint — no JWT — accessed by patient via WhatsApp link ----

    /**
     * Streams the PDF directly to the browser.
     *
     * CONTENT-DISPOSITION: inline → browser opens PDF preview
     * CONTENT-DISPOSITION: attachment → browser downloads the file
     *
     * The access token in the URL acts as a one-time password.
     * No login required — the patient just clicks the WhatsApp link.
     */
    @Operation(summary = "View report PDF via shareable link — public, no login needed")
    @GetMapping("/api/v1/reports/public/{accessToken}")
    public ResponseEntity<byte[]> viewPublic(@PathVariable String accessToken) throws IOException {
        byte[] pdfBytes = reportService.getPublicPdf(accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.pdf\"")
                .body(pdfBytes);
    }
}