package com.pravinmali.orderprocessing.dto.response;

import java.util.List;

public record NotificationListResponse(
        long unreadCount,
        List<NotificationResponse> notifications
) {}
