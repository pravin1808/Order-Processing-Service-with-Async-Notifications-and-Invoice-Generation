package com.pravinmali.orderprocessing.exception;

import java.util.UUID;

/**
 * Thrown when an order with the given ID does not exist in the database.
 * Mapped to HTTP 404 Not Found by {@link GlobalExceptionHandler}.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("Order not found with id: " + orderId);
    }
}
