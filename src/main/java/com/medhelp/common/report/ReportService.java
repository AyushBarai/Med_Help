package com.medhelp.common.report;

import com.medhelp.common.catalog.TestCatalog;
import com.medhelp.common.catalog.TestCatalogRepository;
import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.tenant.TenantContext;
import com.medhelp.common.lab.Lab;
import com.medhelp.common.lab.LabRepository;
import com.medhelp.common.notification.NotificationService;
import com.medhelp.common.order.OrderItem;
import com.medhelp.common.order.TestOrder;
import com.medhelp.common.order.OrderItemRepository;
import com.medhelp.common.order.TestOrderRepository;
import com.medhelp.common.patient.Patient;
import com.medhelp.common.patient.PatientRepository;
import com.medhelp.common.report.ReportDtos.DeliverRequest;
import com.medhelp.common.report.ReportDtos.ReportResponse;
import com.medhelp.common.report.Report;
import com.medhelp.common.report.ReportRepository;
import com.medhelp.common.report.PdfReportBuilder.TestItemData;
import com.medhelp.common.result.TestResult;
import com.medhelp.common.result.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository      reportRepository;
    private final TestOrderRepository   orderRepository;
    private final OrderItemRepository   itemRepository;
    private final PatientRepository     patientRepository;
    private final LabRepository         labRepository;
    private final TestCatalogRepository catalogRepository;
    private final TestResultRepository  resultRepository;
    private final PdfReportBuilder      pdfBuilder;
    private final StorageService        storageService;
    private final NotificationService   notificationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // =====================================================================
    //  GENERATE REPORT
    //  Fetches all data → builds PDF → saves → creates DB record
    // =====================================================================

    @Transactional
    public ReportResponse generate(UUID orderId, UUID generatedBy) throws IOException {
        UUID labId = TenantContext.get();

        // 1. Fetch and validate the order
        TestOrder order = orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Results can only be generated once the order is REPORT_READY
        if (order.getStatus() == TestOrder.OrderStatus.REGISTERED ||
            order.getStatus() == TestOrder.OrderStatus.SAMPLE_COLLECTED) {
            throw new BusinessException("Results must be entered before generating a report");
        }

        // 2. Fetch patient and lab (for letterhead)
        Patient patient = patientRepository.findById(order.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", order.getPatientId()));

        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));

        // 3. Fetch order items + their results and build TestItemData list
        List<OrderItem> orderItems = itemRepository.findAllByOrderId(orderId);

        // Build a map: testId → TestCatalog (to get test name, category, sampleType)
        List<UUID> testIds = orderItems.stream().map(OrderItem::getTestId).toList();
        Map<UUID, TestCatalog> catalogMap = catalogRepository.findAllById(testIds)
                .stream().collect(Collectors.toMap(TestCatalog::getId, t -> t));

        // For each order item, fetch its result parameters
        List<TestItemData> testItems = orderItems.stream().map(item -> {
            TestCatalog catalog = catalogMap.get(item.getTestId());
            List<TestResult> results = resultRepository.findAllByOrderItemId(item.getId());

            return new TestItemData(
                    catalog != null ? catalog.getName() : "Unknown Test",
                    catalog != null ? catalog.getCategory() : null,
                    catalog != null ? catalog.getSampleType() : null,
                    results
            );
        }).toList();

        // 4. Generate the PDF bytes
        log.info("Generating PDF for order: {} (lab: {})", order.getOrderNumber(), lab.getSlug());
        byte[] pdfBytes = pdfBuilder.build(lab, patient, order, testItems);

        // 5. Determine version number (allows regeneration = new version)
        int version = reportRepository.countByOrderId(orderId) + 1;

        // 6. Create a unique filename
        String fileName = String.format("%s-%s-v%d.pdf",
                lab.getSlug(), order.getOrderNumber(), version);

        // 7. Save PDF to local disk / S3
        String pdfUrl = storageService.save(fileName, pdfBytes);

        // 8. Generate a random access token for the public shareable link
        String accessToken = UUID.randomUUID().toString().replace("-", "");

        // 9. Save Report record to DB
        Report report = Report.builder()
                .orderId(orderId)
                .version(version)
                .pdfUrl(pdfUrl)
                .accessToken(accessToken)
                .isFinal(true)
                .generatedAt(LocalDateTime.now())
                .generatedBy(generatedBy)
                .build();
        report = reportRepository.save(report);

        // 10. Auto-advance order to REPORT_READY if not already
        if (order.getStatus() == TestOrder.OrderStatus.PROCESSING) {
            order.setStatus(TestOrder.OrderStatus.REPORT_READY);
            orderRepository.save(order);
        }

        log.info("Report generated: {} (version {}, token: {})", fileName, version, accessToken);
        return ReportResponse.from(report, baseUrl);
    }

    // =====================================================================
    //  DELIVER REPORT — sends to patient via WhatsApp/SMS/email
    // =====================================================================

    @Transactional
    public ReportResponse deliver(UUID orderId, DeliverRequest request) {
        UUID labId = TenantContext.get();

        TestOrder order = orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Report report = reportRepository
                .findTopByOrderIdAndIsFinalTrueOrderByVersionDesc(orderId)
                .orElseThrow(() -> new BusinessException("No report generated yet. Generate report first."));

        Patient patient = patientRepository.findById(order.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", order.getPatientId()));

        String reportLink = baseUrl + "/api/v1/reports/public/" + report.getAccessToken();

        // Send via requested channels
        if (request.sendWhatsApp()) {
            notificationService.sendReportReady(
                    labId, orderId, patient.getId(), patient.getPhone(), reportLink);
        }

        // Mark order as DELIVERED
        if (order.getStatus().canTransitionTo(TestOrder.OrderStatus.DELIVERED)) {
            order.setStatus(TestOrder.OrderStatus.DELIVERED);
            order.setDeliveredAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        log.info("Report delivered for order {} to patient {}", order.getOrderNumber(), patient.getPhone());
        return ReportResponse.from(report, baseUrl);
    }

    // =====================================================================
    //  PUBLIC REPORT — no auth — accessed via shareable link
    // =====================================================================

    /**
     * Returns PDF bytes for a given access token.
     * This endpoint is PUBLIC — no JWT needed.
     * The access token acts as a password.
     */
    public byte[] getPublicPdf(String accessToken) throws IOException {
        Report report = reportRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found or link has expired"));

        return storageService.load(report.getPdfUrl());
    }

    // =====================================================================
    //  LIST REPORTS FOR AN ORDER
    // =====================================================================

    public List<ReportResponse> getByOrder(UUID orderId) {
        UUID labId = TenantContext.get();
        // Verify order belongs to this lab
        orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        return reportRepository.findAllByOrderIdOrderByVersionDesc(orderId)
                .stream()
                .map(r -> ReportResponse.from(r, baseUrl))
                .toList();
    }
}