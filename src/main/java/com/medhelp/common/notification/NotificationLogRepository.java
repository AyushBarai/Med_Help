package com.medhelp.common.notification;

import com.medhelp.common.notification.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findAllByOrderId(UUID orderId);
}