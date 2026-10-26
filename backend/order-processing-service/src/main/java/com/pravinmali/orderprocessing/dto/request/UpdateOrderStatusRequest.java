package com.pravinmali.orderprocessing.dto.request;

import com.pravinmali.orderprocessing.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating an order's status.
 */
public record UpdateOrderStatusRequest(

        @NotNull(message = "Status must not be null")
        OrderStatus status
) {}
