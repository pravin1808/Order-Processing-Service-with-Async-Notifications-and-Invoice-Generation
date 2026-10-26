package com.pravinmali.orderprocessing.event;

import java.util.UUID;

/**
 * Published after an order status transitions to SHIPPED.
 */
public record OrderShippedEvent(
        UUID orderId,
        String customerName,
        String customerEmail
) {}
