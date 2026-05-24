package com.medhelp.common.payment;

import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.tenant.TenantContext;
import com.medhelp.common.payment.PaymentDtos.*;
import com.medhelp.common.payment.Payment;
import com.medhelp.common.order.TestOrder;
import com.medhelp.common.order.TestOrderRepository;
import com.medhelp.common.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TestOrderRepository orderRepository;

    @Transactional
    public PaymentResponse record(RecordPaymentRequest request) {
        UUID labId = TenantContext.get();

        TestOrder order = orderRepository.findByIdAndLabId(request.orderId(), labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        // Don't allow over-payment
        BigDecimal alreadyPaid = paymentRepository.totalPaidForOrder(request.orderId());
        BigDecimal remaining = order.getNetAmount().subtract(alreadyPaid);

        if (request.amount().compareTo(remaining) > 0) {
            throw new BusinessException(
                    "Payment amount ₹" + request.amount() + " exceeds remaining balance ₹" + remaining);
        }

        Payment payment = Payment.builder()
                .labId(labId)
                .orderId(request.orderId())
                .amount(request.amount())
                .method(request.method())
                .status(Payment.PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        BigDecimal totalPaid = paymentRepository.totalPaidForOrder(request.orderId());

        return PaymentResponse.from(payment, totalPaid);
    }

    public List<PaymentResponse> getByOrder(UUID orderId) {
        UUID labId = TenantContext.get();
        orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        BigDecimal totalPaid = paymentRepository.totalPaidForOrder(orderId);
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(p -> PaymentResponse.from(p, totalPaid))
                .toList();
    }

    public List<PaymentResponse> getPending() {
        UUID labId = TenantContext.get();
        return paymentRepository.findAllByLabIdAndStatus(labId, Payment.PaymentStatus.PENDING)
                .stream()
                .map(p -> PaymentResponse.from(p, BigDecimal.ZERO))
                .toList();
    }
}