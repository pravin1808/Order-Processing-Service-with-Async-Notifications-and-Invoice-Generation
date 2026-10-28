package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.dto.response.NotificationListResponse;
import com.pravinmali.orderprocessing.dto.response.NotificationResponse;
import com.pravinmali.orderprocessing.entity.Notification;
import com.pravinmali.orderprocessing.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(String recipientEmail,
                                           String title,
                                           String message,
                                           UUID orderId,
                                           String eventType) {
        Notification notification = Notification.builder()
                .recipientEmail(recipientEmail)
                .title(title)
                .message(message)
                .orderId(orderId)
                .eventType(eventType)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("[NOTIFICATION SAVED] id={} | recipient={} | eventType={}",
                saved.getId(), recipientEmail, eventType);
        return saved;
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotificationsForUser(String email, String role) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
        PageRequest pageRequest = PageRequest.of(0, 30);

        List<Notification> list = isAdmin
                ? notificationRepository.findAllByOrderByCreatedAtDesc(pageRequest)
                : notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email, pageRequest);

        long unreadCount = isAdmin
                ? notificationRepository.countByReadFalse()
                : notificationRepository.countByRecipientEmailAndReadFalse(email);

        List<NotificationResponse> dtos = list.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getRecipientEmail(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getOrderId(),
                        n.getEventType(),
                        n.isRead(),
                        n.getCreatedAt()
                ))
                .toList();

        return new NotificationListResponse(unreadCount, dtos);
    }

    @Transactional
    public boolean markAsRead(UUID id, String email, String role) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
        return notificationRepository.findById(id).map(n -> {
            if (isAdmin || n.getRecipientEmail().equalsIgnoreCase(email)) {
                n.setRead(true);
                notificationRepository.save(n);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Transactional
    public void markAllAsRead(String email, String role) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
        if (isAdmin) {
            notificationRepository.markAllAsRead();
        } else {
            notificationRepository.markAllAsReadForRecipient(email);
        }
    }
}
