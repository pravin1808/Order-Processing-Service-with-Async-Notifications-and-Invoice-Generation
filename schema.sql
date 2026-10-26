-- =============================================================================
-- Order Processing Service — PostgreSQL Schema
-- =============================================================================
-- Run this script to create the database schema from scratch.
-- The application uses spring.jpa.hibernate.ddl-auto=update in dev, so this
-- script is provided for reference and for production deployments where
-- ddl-auto=validate is preferred.
--
-- Usage:
--   psql -U postgres -d orderdb -f schema.sql
-- =============================================================================

-- Create database (run as superuser if needed)
-- CREATE DATABASE orderdb;

-- =============================================================================
-- customers
-- =============================================================================
CREATE TABLE IF NOT EXISTS customers (
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(255),
    address       VARCHAR(500),
    password_hash VARCHAR(255),
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER'
                               CHECK (role IN ('ADMIN', 'CUSTOMER')),
    created_at    TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_customer_email ON customers (email);

-- =============================================================================
-- orders
-- =============================================================================
CREATE TABLE IF NOT EXISTS orders (
    id          UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_id UUID           NOT NULL REFERENCES customers (id),
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
                               CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    subtotal    NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_rate    NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    tax_amount  NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total       NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status       ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at   ON orders (created_at DESC);

-- =============================================================================
-- order_items
-- =============================================================================
CREATE TABLE IF NOT EXISTS order_items (
    id           UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id     UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INTEGER        NOT NULL,
    unit_price   NUMERIC(15, 2) NOT NULL,
    line_total   NUMERIC(15, 2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);
