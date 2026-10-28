package com.pravinmali.orderprocessing.event;

import com.pravinmali.orderprocessing.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async consumer for order domain events.
 *
 * <p>Each handler method runs in the {@code orderEventExecutor} thread pool
 * defined in {@link com.pravinmali.orderprocessing.config.AsyncConfig}.
 * This ensures event processing never blocks the HTTP request thread.</p>
 *
 * <p>Logs events to the console and saves async notifications to the customer's
 * in-app notification center.</p>
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService notificationService;

    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("orderEventExecutor")
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[EVENT] ORDER_CREATED | orderId={} | customer={} <{}> | total={} | items={}",
                event.orderId(),
                event.customerName(),
                event.customerEmail(),
                event.total(),
                event.itemCount());

        notificationService.createNotification(
                event.customerEmail(),
                "Order Placed Successfully",
                "Your order #" + event.orderId().toString().substring(0, 8) + " for ₹" + event.total()
                        + " (" + event.itemCount() + " items) has been placed and is awaiting confirmation.",
                event.orderId(),
                "ORDER_CREATED"
        );
    }

    @Async("orderEventExecutor")
    @EventListener
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[EVENT] ORDER_CONFIRMED | orderId={} | customer={} <{}> | total={}",
                event.orderId(),
                event.customerName(),
                event.customerEmail(),
                event.total());

        notificationService.createNotification(
                event.customerEmail(),
                "Order Confirmed & Invoice Ready",
                "Your order #" + event.orderId().toString().substring(0, 8)
                        + " has been confirmed. Your official tax invoice PDF is ready to download.",
                event.orderId(),
                "ORDER_CONFIRMED"
        );
    }

    @Async("orderEventExecutor")
    @EventListener
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("[EVENT] ORDER_SHIPPED | orderId={} | customer={} <{}>",
                event.orderId(),
                event.customerName(),
                event.customerEmail());

        notificationService.createNotification(
                event.customerEmail(),
                "Order Shipped 🚚",
                "Great news! Your package for order #" + event.orderId().toString().substring(0, 8)
                        + " is on its way to your delivery address.",
                event.orderId(),
                "ORDER_SHIPPED"
        );
    }
}
