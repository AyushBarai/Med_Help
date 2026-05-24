package com.medhelp.common.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByOrderId(UUID orderId);
    List<Payment> findAllByLabIdAndStatus(UUID labId, Payment.PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.orderId = :orderId AND p.status = 'PAID'")
    BigDecimal totalPaidForOrder(UUID orderId);
}