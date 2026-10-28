package com.pravinmali.orderprocessing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Detailed report response containing totals and per-status breakdown.
 */
public record DailyRevenueReportResponse(
        LocalDate date,
        Long totalOrders,
        BigDecimal totalSubtotal,
        BigDecimal totalTax,
        BigDecimal totalRevenue,
        List<DailyRevenueSummary> breakdown
) {}
