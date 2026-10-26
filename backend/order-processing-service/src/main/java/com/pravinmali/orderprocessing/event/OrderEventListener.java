package com.pravinmali.orderprocessing.event;

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
 * <p>Currently logs events to the console. In a production system this would
 * send emails, push notifications, or messages to an external broker.</p>
 *
 * <p><b>Why not @TransactionalEventListener?</b> Our event objects carry a data
 * snapshot, not a JPA entity, so there is no risk of accessing lazy-loaded
 * fields after the transaction closes. Plain @EventListener is simpler and
 * sufficient here.</p>
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @Async("orderEventExecutor")
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[EVENT] ORDER_CREATED | orderId={} | customer={} <{}> | total={} | items={}",
                event.orderId(),
                event.customerName(),
                event.customerEmail(),
                event.total(),
                event.itemCount());
    }

    @Async("orderEventExecutor")
    @EventListener
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[EVENT] ORDER_CONFIRMED | orderId={} | customer={} <{}> | total={}",
                event.orderId(),
                event.customerName(),
                event.customerEmail(),
                event.total());
    }

    @Async("orderEventExecutor")
    @EventListener
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("[EVENT] ORDER_SHIPPED | orderId={} | customer={} <{}>",
                event.orderId(),
                event.customerName(),
                event.customerEmail());
    }
}
