package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.entity.OrderItem;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import com.pravinmali.orderprocessing.exception.InvalidStatusTransitionException;
import com.pravinmali.orderprocessing.repository.CustomerRepository;
import com.pravinmali.orderprocessing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderService}.
 *
 * <p>No Spring context — pure JUnit 5 + Mockito.
 * Fast, isolated, and tests business logic directly.</p>
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .taxRate(new BigDecimal("18"))
                .build();
    }

    // =========================================================================
    // calculateTotals — pure business logic
    // =========================================================================

    @Test
    @DisplayName("calculateTotals: two items → correct subtotal, taxAmount, total")
    void calculateTotals_withTwoItems_computesCorrectly() {
        // Arrange
        OrderItem item1 = OrderItem.builder()
                .productName("Laptop")
                .quantity(1)
                .unitPrice(new BigDecimal("50000.00"))
                .lineTotal(new BigDecimal("50000.00"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .productName("Mouse")
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .lineTotal(new BigDecimal("1000.00"))
                .build();

        sampleOrder.addItem(item1);
        sampleOrder.addItem(item2);

        // Act
        orderService.calculateTotals(sampleOrder);

        // Assert
        assertThat(sampleOrder.getSubtotal()).isEqualByComparingTo("51000.00");
        assertThat(sampleOrder.getTaxAmount()).isEqualByComparingTo("9180.00");
        assertThat(sampleOrder.getTotal()).isEqualByComparingTo("60180.00");
    }

    @Test
    @DisplayName("calculateTotals: zero items → subtotal, tax, total all zero")
    void calculateTotals_withNoItems_allZero() {
        // Act
        orderService.calculateTotals(sampleOrder);

        // Assert
        assertThat(sampleOrder.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sampleOrder.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sampleOrder.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateTotals: fractional unit price → HALF_UP rounding applied")
    void calculateTotals_withFractionalPrice_roundsHalfUp() {
        // 3 × 33.33 = 99.99
        OrderItem item = OrderItem.builder()
                .productName("Widget")
                .quantity(3)
                .unitPrice(new BigDecimal("33.33"))
                .lineTotal(new BigDecimal("99.99"))
                .build();
        sampleOrder.addItem(item);

        orderService.calculateTotals(sampleOrder);

        // subtotal = 99.99, tax = 99.99 * 18 / 100 = 17.9982 → rounded to 18.00
        assertThat(sampleOrder.getSubtotal()).isEqualByComparingTo("99.99");
        assertThat(sampleOrder.getTaxAmount()).isEqualByComparingTo("18.00");
        assertThat(sampleOrder.getTotal()).isEqualByComparingTo("117.99");
    }

    // =========================================================================
    // Status transition validation
    // =========================================================================

    @Test
    @DisplayName("updateOrderStatus: PENDING → CONFIRMED is allowed")
    void updateStatus_validTransition_succeeds() {
        UUID orderId = UUID.randomUUID();

        Customer customer = Customer.builder()
                .name("Test User").email("test@example.com").build();

        Order order = Order.builder().taxRate(BigDecimal.ZERO).build();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Should not throw
        assertThatCode(() -> orderService.updateOrderStatus(
                orderId,
                new UpdateOrderStatusRequest(OrderStatus.CONFIRMED)
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("updateOrderStatus: SHIPPED → PENDING throws InvalidStatusTransitionException")
    void updateStatus_invalidTransition_throwsException() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().taxRate(BigDecimal.ZERO).build();
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(
                orderId,
                new com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest(OrderStatus.PENDING)
        )).isInstanceOf(InvalidStatusTransitionException.class)
          .hasMessageContaining("SHIPPED")
          .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("updateOrderStatus: non-existent order throws OrderNotFoundException")
    void updateStatus_orderNotFound_throwsException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(
                orderId,
                new com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest(OrderStatus.CONFIRMED)
        )).isInstanceOf(com.pravinmali.orderprocessing.exception.OrderNotFoundException.class);
    }
}
