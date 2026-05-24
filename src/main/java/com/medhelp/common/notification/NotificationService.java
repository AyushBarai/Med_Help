package com.medhelp.common.notification;

import com.medhelp.common.notification.*;
import com.medhelp.common.notification.NotificationLog.Channel;
import com.medhelp.common.notification.NotificationLog.NotificationStatus;
import com.medhelp.common.notification.NotificationLog.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification service — currently a stub that logs notifications to DB.
 *
 * TO ADD WHATSAPP: inject Interakt/Meta API client and call it in sendWhatsApp().
 * TO ADD SMS:      inject MSG91 client and call it in sendSms().
 * TO ADD EMAIL:    inject JavaMailSender and call it in sendEmail().
 *
 * The DB log tracks every notification so you can show delivery status
 * in the lab dashboard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository logRepository;

    public void sendOrderConfirmation(UUID labId, UUID orderId, UUID patientId,
                                      String patientPhone, String orderNumber) {
        log.info("[NOTIFY] Order confirmation → {} (order: {})", patientPhone, orderNumber);

        saveLog(labId, orderId, patientId, Channel.WHATSAPP, NotificationType.ORDER_CONFIRM);
    }

    public void sendReportReady(UUID labId, UUID orderId, UUID patientId,
                                String patientPhone, String reportLink) {
        log.info("[NOTIFY] Report ready → {} (link: {})", patientPhone, reportLink);

        saveLog(labId, orderId, patientId, Channel.WHATSAPP, NotificationType.REPORT_READY);
    }

    // ---- INTERNAL ----

    private void saveLog(UUID labId, UUID orderId, UUID patientId,
                         Channel channel, NotificationType type) {
        NotificationLog log = NotificationLog.builder()
                .labId(labId)
                .orderId(orderId)
                .patientId(patientId)
                .channel(channel)
                .type(type)
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
}