package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.dto.request.CreateOrderRequest;
import com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.pravinmali.orderprocessing.dto.response.OrderResponse;
import com.pravinmali.orderprocessing.dto.response.OrderSummaryResponse;
import com.pravinmali.orderprocessing.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for order management.
 *
 * <p>All endpoints are under {@code /api/v1/orders}.
 * Business logic is fully delegated to {@link OrderService} — this controller
 * only handles HTTP concerns (status codes, request binding, response wrapping).</p>
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order with one or more line items.
     * Returns 201 Created with the full order detail including calculated totals.
     */
    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve a single order by its UUID.
     * Returns 404 if the order does not exist.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * Paginated list of all orders.
     * Default: page 0, size 10, sorted by createdAt descending.
     * Supports ?page=&size=&sort= query parameters.
     */
    @GetMapping
    @Operation(summary = "List all orders (paginated)")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(pageable));
    }

    /**
     * Update an order's status.
     * Returns 422 if the transition is not permitted by business rules.
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
