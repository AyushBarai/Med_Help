package com.medhelp.common.report;

import java.util.UUID;

public class ReportDtos {

    public record ReportResponse(
            UUID id,
            UUID orderId,
            int version,
            String pdfUrl,
            String publicLink,      // the full shareable URL for the patient
            String accessToken,
            boolean isFinal,
            String generatedAt
    ) {
        public static ReportResponse from(Report r, String baseUrl) {
            return new ReportResponse(
                    r.getId(),
                    r.getOrderId(),
                    r.getVersion(),
                    r.getPdfUrl(),
                    baseUrl + "/api/v1/reports/public/" + r.getAccessToken(),
                    r.getAccessToken(),
                    r.isFinal(),
                    r.getGeneratedAt() != null ? r.getGeneratedAt().toString() : null
            );
        }
    }

    public record DeliverRequest(
            boolean sendWhatsApp,
            boolean sendSms,
            boolean sendEmail
    ) {}
}