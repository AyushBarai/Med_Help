package com.medhelp.common.order;

import com.medhelp.common.catalog.TestCatalog;
import com.medhelp.common.catalog.TestCatalogRepository;
import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.tenant.TenantContext;
import com.medhelp.common.order.OrderDtos.*;
import com.medhelp.common.order.OrderItem;
import com.medhelp.common.order.OrderItemRepository;
import com.medhelp.common.order.TestOrderRepository;
import com.medhelp.common.patient.Patient;
import com.medhelp.common.patient.PatientDtos.PatientResponse;
import com.medhelp.common.patient.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final TestOrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PatientRepository patientRepository;
    private final TestCatalogRepository catalogRepository;

    // ---- CREATE ----

    @Transactional
    public OrderResponse create(CreateOrderRequest request, UUID currentUserId) {
        UUID labId = TenantContext.get();

        // 1. Validate patient belongs to this lab
        Patient patient = patientRepository.findByIdAndLabId(request.patientId(), labId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));

        // 2. Fetch selected tests — validate all belong to this lab
        List<TestCatalog> tests = catalogRepository.findAllByIdInAndLabId(request.testIds(), labId);
        if (tests.size() != request.testIds().size()) {
            throw new BusinessException("One or more selected tests do not belong to this lab");
        }

        // 3. Calculate amounts
        BigDecimal total = tests.stream()
                .map(TestCatalog::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal net = total.subtract(discount);

        if (net.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Discount cannot exceed total amount");
        }

        // 4. Calculate expected completion time (max TAT across selected tests)
        int maxTatHours = tests.stream()
                .mapToInt(TestCatalog::getTurnaroundHours)
                .max()
                .orElse(24);

        // 5. Generate order number — sequential per lab: LAB-YYYYMMDD-XXXX
        String orderNumber = generateOrderNumber(labId);

        // 6. Create order
        TestOrder order = TestOrder.builder()
                .labId(labId)
                .patientId(patient.getLabId())
                .orderNumber(orderNumber)
                .referredBy(request.referredBy())
                .collectedBy(currentUserId)
                .collectionType(request.collectionType() != null
                        ? request.collectionType() : TestOrder.CollectionType.WALK_IN)
                .collectionAddress(request.collectionAddress())
                .totalAmount(total)
                .discountAmount(discount)
                .netAmount(net)
                .notes(request.notes())
                .expectedAt(LocalDateTime.now().plusHours(maxTatHours))
                .build();

        order = orderRepository.save(order);

        // 7. Create order items (one per test, with price snapshot)
        final UUID orderId = order.getLabId();
        List<OrderItem> items = tests.stream()
                .map(test -> OrderItem.builder()
                        .orderId(orderId)
                        .testId(test.getId())
                        .price(test.getPrice())
                        .build())
                .collect(Collectors.toList());
        itemRepository.saveAll(items);

        log.info("Order created: {} for patient {} (lab: {})", orderNumber, patient.getName(), labId);

        return buildResponse(order, patient, items, tests);
    }

    // ---- GET ORDERS ----

    public Page<OrderResponse> getAll(TestOrder.OrderStatus status, int page, int size) {
        UUID labId = TenantContext.get();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<TestOrder> orders = (status != null)
                ? orderRepository.findAllByLabIdAndStatus(labId, status, pageable)
                : orderRepository.findAllByLabId(labId, pageable);

        return orders.map(order -> {
            Patient patient = patientRepository.findById(order.getPatientId()).orElse(null);
            List<OrderItem> items = itemRepository.findAllByOrderId(order.getLabId());
            List<TestCatalog> tests = catalogRepository.findAllByIdInAndLabId(
                    items.stream().map(OrderItem::getTestId).toList(), labId);
            return buildResponse(order, patient, items, tests);
        });
    }

    public OrderResponse getById(UUID orderId) {
        UUID labId = TenantContext.get();
        TestOrder order = orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Patient patient = patientRepository.findById(order.getPatientId()).orElse(null);
        List<OrderItem> items = itemRepository.findAllByOrderId(orderId);
        List<TestCatalog> tests = catalogRepository.findAllByIdInAndLabId(
                items.stream().map(OrderItem::getTestId).toList(), labId);
        return buildResponse(order, patient, items, tests);
    }

    // ---- UPDATE STATUS ----

    @Transactional
    public OrderResponse updateStatus(UUID orderId, TestOrder.OrderStatus newStatus) {
        UUID labId = TenantContext.get();
        TestOrder order = orderRepository.findByIdAndLabId(orderId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // State machine check
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessException(
                    "Cannot move order from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);

        if (newStatus == TestOrder.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        orderRepository.save(order);

        Patient patient = patientRepository.findById(order.getPatientId()).orElse(null);
        List<OrderItem> items = itemRepository.findAllByOrderId(orderId);
        List<TestCatalog> tests = catalogRepository.findAllByIdInAndLabId(
                items.stream().map(OrderItem::getTestId).toList(), labId);

        return buildResponse(order, patient, items, tests);
    }

    // ---- HELPERS ----

    private String generateOrderNumber(UUID labId) {
        long count = orderRepository.countByLabId(labId) + 1;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("LAB-%s-%04d", date, count);
    }

    private OrderResponse buildResponse(TestOrder order, Patient patient,
                                        List<OrderItem> items, List<TestCatalog> tests) {
        // Map testId → testName for item responses
        Map<UUID, String> testNames = tests.stream()
                .collect(Collectors.toMap(TestCatalog::getId, TestCatalog::getName));

        List<OrderItemResponse> itemResponses = items.stream()
                .map(i -> OrderItemResponse.from(i, testNames.getOrDefault(i.getTestId(), "Unknown")))
                .toList();

        PatientResponse patientResponse = patient != null ? PatientResponse.from(patient) : null;

        return new OrderResponse(
                order.getLabId(), order.getOrderNumber(), order.getStatus().name(),
                order.getCollectionType().name(), order.getReferredBy(),
                order.getTotalAmount(), order.getDiscountAmount(), order.getNetAmount(),
                order.getExpectedAt() != null ? order.getExpectedAt().toString() : null,
                order.getDeliveredAt() != null ? order.getDeliveredAt().toString() : null,
                order.getNotes(),
                order.getExpectedAt() != null ? order.getExpectedAt().toString() : null,
                patientResponse, itemResponses
        );
    }
}