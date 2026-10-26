package com.pravinmali.orderprocessing.exception;

import com.pravinmali.orderprocessing.entity.OrderStatus;

/**
 * Thrown when a requested status transition is not permitted by the business rules.
 * For example: SHIPPED → PENDING, or DELIVERED → CANCELLED.
 * Mapped to HTTP 422 Unprocessable Entity by {@link GlobalExceptionHandler}.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Cannot transition order status from " + from + " to " + to);
    }
}
