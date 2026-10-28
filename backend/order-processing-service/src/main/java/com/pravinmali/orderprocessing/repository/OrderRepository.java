package com.pravinmali.orderprocessing.repository;

import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pravinmali.orderprocessing.dto.response.DailyRevenueSummary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Order} entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /** Paginated list of all orders — used by the ADMIN role. */
    Page<Order> findAll(Pageable pageable);

    /**
     * Paginated list of a specific customer's orders.
     * Used for CUSTOMER role RBAC: only return orders owned by the authenticated customer.
     */
    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    /** Filter by status — used for admin dashboards and the nightly CSV export. */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Groups orders by status within a date-time window and calculates total revenue,
     * tax, and order count for nightly CSV summary exports.
     */
    @Query("""
        SELECT new com.pravinmali.orderprocessing.dto.response.DailyRevenueSummary(
            o.status,
            COUNT(o),
            SUM(o.subtotal),
            SUM(o.taxAmount),
            SUM(o.total)
        )
        FROM Order o
        WHERE o.createdAt >= :startDate AND o.createdAt < :endDate
        GROUP BY o.status
        ORDER BY o.status
    """)
    List<DailyRevenueSummary> findDailyRevenueGroupedByStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Groups all historical orders by status and calculates total revenue,
     * tax, and order count for dashboard summary.
     */
    @Query("""
        SELECT new com.pravinmali.orderprocessing.dto.response.DailyRevenueSummary(
            o.status,
            COUNT(o),
            SUM(o.subtotal),
            SUM(o.taxAmount),
            SUM(o.total)
        )
        FROM Order o
        GROUP BY o.status
        ORDER BY o.status
    """)
    List<DailyRevenueSummary> findOverallRevenueGroupedByStatus();
}
