package com.medhelp.common.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.medhelp.common.medhelp.Baseentity;

@Entity
@Table(name = "notification_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog extends Baseentity {

    @Column(name = "lab_id", nullable = false)
    private UUID labId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "patient_id")
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private LocalDateTime sentAt;

    public enum Channel { WHATSAPP, SMS, EMAIL }
    public enum NotificationType { ORDER_CONFIRM, REPORT_READY, PAYMENT_REMINDER }
    public enum NotificationStatus { PENDING, SENT, DELIVERED, FAILED }
}