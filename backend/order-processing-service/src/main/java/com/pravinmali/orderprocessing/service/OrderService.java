package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.dto.request.CreateOrderRequest;
import com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.pravinmali.orderprocessing.dto.response.CustomerResponse;
import com.pravinmali.orderprocessing.dto.response.OrderItemResponse;
import com.pravinmali.orderprocessing.dto.response.OrderResponse;
import com.pravinmali.orderprocessing.dto.response.OrderSummaryResponse;
import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.entity.OrderItem;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import com.pravinmali.orderprocessing.event.OrderConfirmedEvent;
import com.pravinmali.orderprocessing.event.OrderCreatedEvent;
import com.pravinmali.orderprocessing.event.OrderShippedEvent;
import com.pravinmali.orderprocessing.exception.InvalidStatusTransitionException;
import com.pravinmali.orderprocessing.exception.OrderNotFoundException;
import com.pravinmali.orderprocessing.repository.CustomerRepository;
import com.pravinmali.orderprocessing.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Core business logic for order management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Customer upsert (find by email or create new)</li>
 *   <li>Order creation with automatic monetary calculations</li>
 *   <li>Status transition validation and enforcement</li>
 *   <li>Domain event publishing after state changes</li>
 *   <li>Entity ↔ DTO mapping</li>
 * </ul>
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final int MONETARY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.order.tax-rate:18}")
    private BigDecimal taxRate;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    // CREATE ORDER
    // =========================================================================

    /**
     * Creates a new order from the given request.
     *
     * <p>Flow:
     * <ol>
     *   <li>Upsert the customer (find by email or create)</li>
     *   <li>Build the Order entity with all line items</li>
     *   <li>Calculate line totals, subtotal, tax, and total</li>
     *   <li>Persist and publish OrderCreatedEvent</li>
     * </ol>
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = upsertCustomer(request);

        Order order = Order.builder()
                .customer(customer)
                .taxRate(taxRate)
                .build();

        // Build and attach each line item
        request.items().forEach(itemRequest -> {
            BigDecimal lineTotal = BigDecimal.valueOf(itemRequest.quantity())
                    .multiply(itemRequest.unitPrice())
                    .setScale(MONETARY_SCALE, ROUNDING);

            OrderItem item = OrderItem.builder()
                    .productName(itemRequest.productName())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice().setScale(MONETARY_SCALE, ROUNDING))
                    .lineTotal(lineTotal)
                    .build();

            order.addItem(item);
        });

        calculateTotals(order);

        Order savedOrder = orderRepository.save(order);
        log.debug("Order created: id={}", savedOrder.getId());

        // Publish event with a data snapshot — not the entity reference
        eventPublisher.publishEvent(new OrderCreatedEvent(
                savedOrder.getId(),
                customer.getName(),
                customer.getEmail(),
                savedOrder.getTotal(),
                savedOrder.getItems().size()
        ));

        return toOrderResponse(savedOrder);
    }

    // =========================================================================
    // READ OPERATIONS
    // =========================================================================

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toOrderSummaryResponse);
    }

    // =========================================================================
    // UPDATE STATUS
    // =========================================================================

    /**
     * Updates the order status after validating the transition is legal.
     * Publishes OrderConfirmedEvent or OrderShippedEvent if applicable.
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = findOrderOrThrow(orderId);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.status();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        log.debug("Order {} status updated: {} → {}", orderId, currentStatus, newStatus);

        publishStatusChangeEvent(savedOrder, newStatus);

        return toOrderResponse(savedOrder);
    }

    // =========================================================================
    // CALCULATION LOGIC (package-private for unit testing)
    // =========================================================================

    /**
     * Calculates and sets subtotal, taxAmount, and total on the given order.
     * All rounding uses HALF_UP to 2 decimal places (monetary standard).
     */
    void calculateTotals(Order order) {
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONETARY_SCALE, ROUNDING);

        BigDecimal taxAmount = subtotal
                .multiply(order.getTaxRate())
                .divide(BigDecimal.valueOf(100), MONETARY_SCALE, ROUNDING);

        BigDecimal total = subtotal.add(taxAmount).setScale(MONETARY_SCALE, ROUNDING);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Finds an existing customer by email or creates a new one.
     * This is the standard e-commerce "guest checkout that links to your account" pattern.
     */
    private Customer upsertCustomer(CreateOrderRequest request) {
        return customerRepository.findByEmail(request.customerEmail())
                .map(existing -> {
                    // Update mutable fields if the customer placed an order with new details
                    existing.setName(request.customerName());
                    existing.setPhone(request.customerPhone());
                    existing.setAddress(request.customerAddress());
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> customerRepository.save(
                        Customer.builder()
                                .name(request.customerName())
                                .email(request.customerEmail())
                                .phone(request.customerPhone())
                                .address(request.customerAddress())
                                .build()
                ));
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void publishStatusChangeEvent(Order order, OrderStatus newStatus) {
        String customerName = order.getCustomer().getName();
        String customerEmail = order.getCustomer().getEmail();

        switch (newStatus) {
            case CONFIRMED -> eventPublisher.publishEvent(new OrderConfirmedEvent(
                    order.getId(), customerName, customerEmail, order.getTotal()));
            case SHIPPED -> eventPublisher.publishEvent(new OrderShippedEvent(
                    order.getId(), customerName, customerEmail));
            default -> { /* No event for DELIVERED or CANCELLED */ }
        }
    }

    // =========================================================================
    // MAPPING — Entity → DTO
    // =========================================================================

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()))
                .toList();

        CustomerResponse customerResponse = new CustomerResponse(
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getCustomer().getPhone(),
                order.getCustomer().getAddress());

        return new OrderResponse(
                order.getId(),
                customerResponse,
                order.getStatus(),
                items,
                order.getSubtotal(),
                order.getTaxRate(),
                order.getTaxAmount(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private OrderSummaryResponse toOrderSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getStatus(),
                order.getTotal(),
                order.getItems().size(),
                order.getCreatedAt());
    }
}
