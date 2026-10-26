package com.pravinmali.orderprocessing.entity;

import java.util.Set;

/**
 * Represents the lifecycle states of an order.
 *
 * <p>Valid transitions:
 * <pre>
 *   PENDING → CONFIRMED
 *   PENDING → CANCELLED
 *   CONFIRMED → SHIPPED
 *   CONFIRMED → CANCELLED
 *   SHIPPED → DELIVERED
 * </pre>
 *
 * Any other transition is illegal and will be rejected by the service layer.
 * </p>
 */
public enum OrderStatus {

    PENDING {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of(CONFIRMED, CANCELLED);
        }
    },

    CONFIRMED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of(SHIPPED, CANCELLED);
        }
    },

    SHIPPED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of(DELIVERED);
        }
    },

    DELIVERED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            // Terminal state — no further transitions allowed
            return Set.of();
        }
    },

    CANCELLED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            // Terminal state — no further transitions allowed
            return Set.of();
        }
    };

    /**
     * Returns the set of statuses this status can legally transition to.
     */
    public abstract Set<OrderStatus> allowedTransitions();

    /**
     * Returns true if transitioning from this status to {@code target} is permitted.
     *
     * <p>Usage in service layer:
     * <pre>
     *   if (!order.getStatus().canTransitionTo(newStatus)) {
     *       throw new InvalidStatusTransitionException(...);
     *   }
     * </pre>
     * </p>
     */
    public boolean canTransitionTo(OrderStatus target) {
        return allowedTransitions().contains(target);
    }
}
