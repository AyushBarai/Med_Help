package com.medhelp.common.order;

import com.medhelp.common.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findAllByOrderId(UUID orderId);
    long countByOrderIdAndStatus(UUID orderId, OrderItem.ItemStatus status);
}