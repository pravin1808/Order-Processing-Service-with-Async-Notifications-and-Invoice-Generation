package com.pravinmali.orderprocessing.exception;

import com.pravinmali.orderprocessing.entity.OrderStatus;

/**
 * Thrown when a PDF invoice is requested for an order whose status does not
 * qualify for invoice generation (i.e. PENDING or CANCELLED).
 * Mapped to HTTP 409 Conflict by {@link GlobalExceptionHandler}.
 *
 * <p>Invoice generation is only permitted for: CONFIRMED, SHIPPED, DELIVERED.</p>
 */
public class InvoiceNotEligibleException extends RuntimeException {

    public InvoiceNotEligibleException(OrderStatus currentStatus) {
        super("Invoice cannot be generated for an order with status: " + currentStatus
                + ". Order must be CONFIRMED, SHIPPED, or DELIVERED.");
    }
}
