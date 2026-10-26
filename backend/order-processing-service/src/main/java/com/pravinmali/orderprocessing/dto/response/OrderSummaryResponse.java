package com.pravinmali.orderprocessing.dto.response;

import com.pravinmali.orderprocessing.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight order view used in paginated list responses.
 * Does NOT include the full items list — avoids N+1 query overhead
 * when returning a page of 10–20 orders.
 */
public record OrderSummaryResponse(
        UUID id,
        String customerName,
        String customerEmail,
        OrderStatus status,
        BigDecimal total,
        int itemCount,
        LocalDateTime createdAt
) {}
