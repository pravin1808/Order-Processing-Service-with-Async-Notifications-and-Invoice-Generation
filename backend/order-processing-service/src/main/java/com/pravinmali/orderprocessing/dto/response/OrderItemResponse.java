package com.pravinmali.orderprocessing.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single line item inside an order response.
 */
public record OrderItemResponse(
        UUID id,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {}
