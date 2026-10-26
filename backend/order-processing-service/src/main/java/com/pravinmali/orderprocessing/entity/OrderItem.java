package com.pravinmali.orderprocessing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single line item within an order.
 *
 * <p>Each OrderItem belongs to exactly one {@link Order} and captures:
 * <ul>
 *   <li>The product being purchased</li>
 *   <li>How many units were ordered</li>
 *   <li>The price per unit at the time of ordering (snapshot — does not change if catalog price changes)</li>
 *   <li>The pre-calculated line total (quantity × unitPrice)</li>
 * </ul>
 *
 * <p>lineTotal is stored in the database (not computed on read) so that historical
 * invoices remain accurate even if unit prices change later.</p>
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * The parent order. Mapped by Order.items — OrderItem does not own the join column.
     * FetchType.LAZY avoids loading the entire order when we only need the line item.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Price per unit at time of order — stored as NUMERIC(15,2).
     * BigDecimal is mandatory for monetary values to avoid floating-point rounding errors.
     */
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Pre-calculated: quantity × unitPrice.
     * Stored to keep invoice totals accurate regardless of future price changes.
     */
    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    /**
     * Explicit setter for the back-reference to the parent Order.
     * Defined explicitly (rather than relying on Lombok) to avoid annotation
     * processing order issues in circular JPA relationships.
     */
    public void setOrder(Order order) {
        this.order = order;
    }
}
