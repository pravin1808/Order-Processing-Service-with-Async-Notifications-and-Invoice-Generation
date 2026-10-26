package com.pravinmali.orderprocessing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Root aggregate entity representing a customer order.
 *
 * <p>References {@link Customer} via a proper foreign key so that
 * RBAC filtering ("a CUSTOMER can only see their own orders") is a simple
 * {@code findByCustomer(...)} query rather than string comparison.</p>
 *
 * <p>Monetary totals are stored (not computed on read) so historical
 * invoice amounts never change if the tax rate changes later.</p>
 */
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    // -------------------------------------------------------------------------
    // Customer — proper FK relationship for RBAC and order ownership queries
    // -------------------------------------------------------------------------

    /**
     * The customer who placed this order.
     * LAZY fetch — we don't need customer data for every order list query.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // -------------------------------------------------------------------------
    // Monetary totals — all stored as NUMERIC(15,2)
    // -------------------------------------------------------------------------

    /** Sum of all line totals before tax. */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Tax rate applied to this order (percentage, e.g. 18 for 18% GST).
     * Stored so historical invoices show the rate that was in effect at order time.
     */
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    /** Calculated as: subtotal × taxRate / 100 */
    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** Calculated as: subtotal + taxAmount */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    // -------------------------------------------------------------------------
    // Line items
    // -------------------------------------------------------------------------

    /**
     * CascadeType.ALL — persisting/updating/deleting an Order cascades to its items.
     * orphanRemoval = true — removing an item from this list deletes it from the DB.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Audit timestamps
    // -------------------------------------------------------------------------

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Convenience helper
    // -------------------------------------------------------------------------

    /**
     * Adds a line item and wires its back-reference to this order.
     * Always use this instead of {@code getItems().add(item)} directly.
     */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}
