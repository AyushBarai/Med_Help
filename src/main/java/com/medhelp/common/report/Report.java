package com.medhelp.common.report;

import com.medhelp.common.medhelp.Baseentity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report extends Baseentity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    // Each regeneration increments version — allows corrected reports
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    // S3 URL (or local path in dev): where the PDF file lives
    @Column(length = 500)
    private String pdfUrl;

    // Random UUID token — used in the public shareable link
    // e.g. reports.medhelp.com/view/{accessToken}
    @Column(unique = true, length = 255)
    private String accessToken;

    // false = draft, true = final (cannot be regenerated without incrementing version)
    @Column(nullable = false)
    @Builder.Default
    private boolean isFinal = false;

    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private UUID generatedBy;
}