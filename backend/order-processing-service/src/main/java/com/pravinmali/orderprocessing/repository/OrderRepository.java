package com.pravinmali.orderprocessing.repository;

import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
