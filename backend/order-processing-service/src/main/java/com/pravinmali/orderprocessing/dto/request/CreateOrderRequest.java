package com.pravinmali.orderprocessing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating a new order.
 * Customer details are included here — if the email matches an existing customer,
 * the service reuses that customer record; otherwise a new one is created.
 */
public record CreateOrderRequest(

        @NotBlank(message = "Customer name must not be blank")
        String customerName,

        @NotBlank(message = "Customer email must not be blank")
        @Email(message = "Customer email must be a valid email address")
        String customerEmail,

        String customerPhone,

        @NotBlank(message = "Customer address must not be blank")
        @Size(max = 500, message = "Address must not exceed 500 characters")
        String customerAddress,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {}
