package com.medhelp.common.payment;

import com.medhelp.common.payment.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class PaymentDtos {

    public record RecordPaymentRequest(
            @NotNull UUID orderId,

            @NotNull @DecimalMin("0.01")
            BigDecimal amount,

            @NotNull
            Payment.PaymentMethod method
    ) {}

    public record PaymentResponse(
            UUID id,
            UUID orderId,
            BigDecimal amount,
            String method,
            String status,
            String paidAt,
            BigDecimal totalPaid    // total paid so far for this order
    ) {
        public static PaymentResponse from(Payment p, BigDecimal totalPaid) {
            return new PaymentResponse(
                    p.getId(), p.getOrderId(), p.getAmount(),
                    p.getMethod().name(), p.getStatus().name(),
                    p.getPaidAt() != null ? p.getPaidAt().toString() : null,
                    totalPaid
            );
        }
    }
}