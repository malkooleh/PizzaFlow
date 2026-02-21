-- Order Service - Initial Schema
-- Migration: V1__initial_schema.sql
-- Description: Creates orders, order_items, and order_saga_state tables

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    scheduled_time TIMESTAMP,
    booking_id UUID,
    delivery_address_street VARCHAR(255),
    delivery_address_city VARCHAR(100),
    delivery_address_postal_code VARCHAR(20),
    delivery_address_latitude DECIMAL(10, 8),
    delivery_address_longitude DECIMAL(11, 8),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    item_id VARCHAR(100) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_saga_state (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    saga_status VARCHAR(50) NOT NULL,
    current_step VARCHAR(100) NOT NULL,
    compensation_required BOOLEAN NOT NULL DEFAULT FALSE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saga_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_scheduled_time ON orders(scheduled_time) WHERE scheduled_time IS NOT NULL;
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_saga_order_id ON order_saga_state(order_id);

-- Comments for documentation
COMMENT ON TABLE orders IS 'Main orders table containing all order types (delivery, takeaway, dine-in, scheduled)';
COMMENT ON TABLE order_items IS 'Order line items with product details and quantities';
COMMENT ON TABLE order_saga_state IS 'Saga orchestration state for distributed transaction management';
COMMENT ON COLUMN orders.order_type IS 'DELIVERY, TAKEAWAY, DINE_IN, SCHEDULED';
COMMENT ON COLUMN orders.status IS 'PENDING, PAYMENT_PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED, FAILED';
