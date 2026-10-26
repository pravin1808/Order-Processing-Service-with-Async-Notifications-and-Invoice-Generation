package com.pravinmali.orderprocessing.entity;

/**
 * Roles for RBAC.
 * ADMIN — full access to all orders and management operations.
 * CUSTOMER — can only view and place their own orders.
 */
public enum Role {
    ADMIN,
    CUSTOMER
}
