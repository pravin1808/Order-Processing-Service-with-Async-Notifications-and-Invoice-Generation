package com.pravinmali.orderprocessing.dto.response;

import com.pravinmali.orderprocessing.entity.OrderStatus;

import java.math.BigDecimal;

/**
 * Data projection for revenue aggregation grouped by {@link OrderStatus}.
 */
public record DailyRevenueSummary(
        OrderStatus status,
        Long orderCount,
        BigDecimal totalSubtotal,
        BigDecimal totalTax,
        BigDecimal totalRevenue
) {
    public DailyRevenueSummary {
        orderCount = orderCount != null ? orderCount : 0L;
        totalSubtotal = totalSubtotal != null ? totalSubtotal : BigDecimal.ZERO;
        totalTax = totalTax != null ? totalTax : BigDecimal.ZERO;
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
}
