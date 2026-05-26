package com.medhelp.common.report;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    // Used by patients to access their report via shareable link (no auth)
    Optional<Report> findByAccessToken(String token);

    // Get all versions of a report for an order
    List<Report> findAllByOrderIdOrderByVersionDesc(UUID orderId);

    // Get the latest final report for an order
    Optional<Report> findTopByOrderIdAndIsFinalTrueOrderByVersionDesc(UUID orderId);

    // Count existing reports for an order — used to set next version number
    int countByOrderId(UUID orderId);
}