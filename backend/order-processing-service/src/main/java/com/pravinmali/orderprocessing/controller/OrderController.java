package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.dto.request.CreateOrderRequest;
import com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.pravinmali.orderprocessing.dto.response.OrderResponse;
import com.pravinmali.orderprocessing.dto.response.OrderSummaryResponse;
import com.pravinmali.orderprocessing.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for order management.
 *
 * <p>RBAC enforcement:
 * <ul>
 *   <li>ADMIN — full access to all orders</li>
 *   <li>CUSTOMER — can create orders and view only their own orders</li>
 * </ul>
 * Endpoint-level rules are in {@link com.pravinmali.orderprocessing.config.SecurityConfig}.
 * Ownership filtering (CUSTOMER sees own orders only) is enforced here via
 * {@link Authentication} principal.</p>
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order. ADMIN and CUSTOMER roles allowed.
     * For CUSTOMER, the order's customer email is taken from the JWT principal
     * to ensure they can only create orders for themselves.
     */
    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    /**
     * Get a single order. ADMIN sees any; CUSTOMER may only see their own.
     * Ownership is enforced in the service layer.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrderById(id, authentication));
    }

    /**
     * Paginated list of orders. ADMIN sees all; endpoint is ADMIN-only per SecurityConfig.
     */
    @GetMapping
    @Operation(summary = "List all orders — ADMIN only")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(pageable));
    }

    /**
     * Update order status — ADMIN only (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status — ADMIN only")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
