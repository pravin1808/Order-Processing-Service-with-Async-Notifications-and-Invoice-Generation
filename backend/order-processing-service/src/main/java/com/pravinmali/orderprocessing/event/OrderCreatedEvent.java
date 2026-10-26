package com.pravinmali.orderprocessing.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Published after a new order is successfully persisted with status PENDING.
 *
 * <p>Carries a data snapshot — not the JPA entity — so the async listener
 * can safely access fields without a live JPA session or risking
 * LazyInitializationException.</p>
 */
public record OrderCreatedEvent(
        UUID orderId,
        String customerName,
        String customerEmail,
        BigDecimal total,
        int itemCount
) {}
