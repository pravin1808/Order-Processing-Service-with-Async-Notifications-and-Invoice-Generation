package com.pravinmali.orderprocessing.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Published after an order status transitions to CONFIRMED.
 */
public record OrderConfirmedEvent(
        UUID orderId,
        String customerName,
        String customerEmail,
        BigDecimal total
) {}
