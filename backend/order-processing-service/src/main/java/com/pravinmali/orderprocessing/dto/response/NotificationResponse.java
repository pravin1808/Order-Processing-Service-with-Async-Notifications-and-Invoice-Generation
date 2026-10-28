package com.pravinmali.orderprocessing.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String recipientEmail,
        String title,
        String message,
        UUID orderId,
        String eventType,
        boolean read,
        LocalDateTime createdAt
) {}
