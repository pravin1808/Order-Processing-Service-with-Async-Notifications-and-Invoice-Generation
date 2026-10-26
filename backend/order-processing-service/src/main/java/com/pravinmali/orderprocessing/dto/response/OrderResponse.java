package com.pravinmali.orderprocessing.dto.response;

import com.pravinmali.orderprocessing.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full order detail response including all line items.
 * Returned by GET /api/v1/orders/{id} and POST /api/v1/orders.
 */
public record OrderResponse(
        UUID id,
        CustomerResponse customer,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
