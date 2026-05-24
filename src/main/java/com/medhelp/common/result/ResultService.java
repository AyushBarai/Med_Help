package com.medhelp.common.result;

import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.tenant.TenantContext;
import com.medhelp.common.order.OrderItem;
import com.medhelp.common.order.TestOrder;
import com.medhelp.common.order.OrderItemRepository;
import com.medhelp.common.order.TestOrderRepository;
import com.medhelp.common.result.ResultDtos.*;
import com.medhelp.common.result.TestResult;
import com.medhelp.common.result.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultService {

    private final TestResultRepository resultRepository;
    private final OrderItemRepository itemRepository;
    private final TestOrderRepository orderRepository;

    @Transactional
    public List<ResultResponse> saveResults(UUID orderId, BulkResultRequest request, UUID enteredBy) {
        UUID labId = TenantContext.get();

        // Verify order belongs to this lab
        TestOrder order = orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Only allow result entry when order is in SAMPLE_COLLECTED or PROCESSING
        if (order.getStatus() == TestOrder.OrderStatus.REGISTERED) {
            throw new BusinessException("Collect sample before entering results");
        }
        if (order.getStatus() == TestOrder.OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot modify results of a delivered order");
        }

        // Save each result parameter
        List<TestResult> saved = request.results().stream().map(param -> {
            TestResult result = TestResult.builder()
                    .orderItemId(param.orderItemId())
                    .parameterName(param.parameterName())
                    .value(param.value())
                    .unit(param.unit())
                    .referenceRange(param.referenceRange())
                    .flag(autoFlag(param.value(), param.referenceRange()))
                    .enteredBy(enteredBy)
                    .build();
            return resultRepository.save(result);
        }).toList();

        // Mark order items as COMPLETED and update order status to PROCESSING
        request.results().stream()
                .map(ParameterRequest::orderItemId)
                .distinct()
                .forEach(itemId -> {
                    itemRepository.findById(itemId).ifPresent(item -> {
                        item.setStatus(OrderItem.ItemStatus.COMPLETED);
                        itemRepository.save(item);
                    });
                });

        // If all items completed → move order to REPORT_READY
        List<OrderItem> allItems = itemRepository.findAllByOrderId(orderId);
        boolean allComplete = allItems.stream()
                .allMatch(i -> i.getStatus() == OrderItem.ItemStatus.COMPLETED);

        if (allComplete && order.getStatus() != TestOrder.OrderStatus.REPORT_READY) {
            order.setStatus(TestOrder.OrderStatus.REPORT_READY);
            orderRepository.save(order);
            log.info("All results entered for order {} — marked REPORT_READY", order.getOrderNumber());
        }

        return saved.stream().map(ResultResponse::from).toList();
    }

    public List<ResultResponse> getByOrderItem(UUID orderItemId) {
        return resultRepository.findAllByOrderItemId(orderItemId)
                .stream().map(ResultResponse::from).toList();
    }

    // ---- Auto-flag: compare numeric value to "min - max" reference range ----
    // e.g. value="11.0", referenceRange="12.0 - 17.0" → LOW
    private TestResult.ResultFlag autoFlag(String value, String referenceRange) {
        try {
            if (value == null || referenceRange == null) return TestResult.ResultFlag.NORMAL;

            double val = Double.parseDouble(value.trim());
            String[] parts = referenceRange.split("-");
            if (parts.length != 2) return TestResult.ResultFlag.NORMAL;

            double min = Double.parseDouble(parts[0].trim());
            double max = Double.parseDouble(parts[1].trim());

            if (val < min * 0.7 || val > max * 1.3) return TestResult.ResultFlag.CRITICAL;
            if (val < min) return TestResult.ResultFlag.LOW;
            if (val > max) return TestResult.ResultFlag.HIGH;
            return TestResult.ResultFlag.NORMAL;
        } catch (NumberFormatException e) {
            return TestResult.ResultFlag.NORMAL; // text values (e.g. "Positive") — no auto-flag
        }
    }
}