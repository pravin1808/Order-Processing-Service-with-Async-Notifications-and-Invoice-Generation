package com.pravinmali.orderprocessing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a customer in the system.
 *
 * <p>Kept as a separate entity (rather than denormalised fields on Order) so that:
 * <ul>
 *   <li>A customer can own multiple orders — enabling "view only my orders" RBAC filtering</li>
 *   <li>When JWT auth is added, the authenticated User maps cleanly to a Customer</li>
 *   <li>Customer profile updates don't retroactively alter historical order data</li>
 * </ul>
 */
@Entity
@Table(
    name = "customers",
    uniqueConstraints = @UniqueConstraint(name = "uk_customer_email", columnNames = "email")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    /**
     * Email is unique per customer — will serve as the username/login identity
     * when JWT authentication is added.
     */
    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column(length = 500)
    private String address;

    /**
     * A customer can place many orders.
     * mappedBy = "customer" — Order owns the foreign key column.
     * Lazy fetched — we rarely need all orders when loading a customer.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
