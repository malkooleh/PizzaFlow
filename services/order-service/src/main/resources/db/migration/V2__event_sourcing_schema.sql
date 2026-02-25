-- Order Service - Event Sourcing Schema
-- Migration: V2__event_sourcing_schema.sql
-- Description: Creates event store and snapshot tables for CQRS/Event Sourcing

-- Event Store Table
-- Stores all domain events for order aggregates in append-only fashion
CREATE TABLE IF NOT EXISTS event_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL DEFAULT 'Order',
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    triggered_by VARCHAR(255),
    correlation_id VARCHAR(100),
    
    -- Ensure events for same aggregate are ordered and unique per version
    CONSTRAINT uk_aggregate_version UNIQUE (aggregate_id, version)
);

-- Snapshot Table
-- Stores periodic snapshots of aggregate state for performance optimization
CREATE TABLE IF NOT EXISTS aggregate_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL DEFAULT 'Order',
    version BIGINT NOT NULL,
    snapshot_data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Each aggregate has only one snapshot per version
    CONSTRAINT uk_snapshot_aggregate_version UNIQUE (aggregate_id, version)
);

-- Read Model Table: order_read_model
-- Denormalized view of orders optimized for queries (CQRS read side)
CREATE TABLE IF NOT EXISTS order_read_model (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    
    -- Pricing
    subtotal DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2),
    total_amount DECIMAL(10, 2) NOT NULL,
    
    -- Delivery details
    delivery_address_street VARCHAR(255),
    delivery_address_city VARCHAR(100),
    delivery_address_postal_code VARCHAR(20),
    delivery_address_latitude DECIMAL(10, 8),
    delivery_address_longitude DECIMAL(11, 8),
    
    -- Scheduling
    scheduled_time TIMESTAMPTZ,
    table_number VARCHAR(20),
    booking_id UUID,
    
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,
    preparing_at TIMESTAMPTZ,
    ready_at TIMESTAMPTZ,
    picked_up_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    
    -- Cancellation
    cancellation_reason TEXT,
    cancelled_by VARCHAR(50),
    
    -- Delivery tracking
    courier_id UUID,
    courier_name VARCHAR(100),
    estimated_delivery_time TIMESTAMPTZ,
    
    -- Version for optimistic locking in read model
    version BIGINT NOT NULL DEFAULT 0,
    last_event_id UUID,
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Read Model Table: order_items_read_model
-- Order items for the read model
CREATE TABLE IF NOT EXISTS order_items_read_model (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    item_id VARCHAR(100) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    
    CONSTRAINT fk_order_items_read_order 
        FOREIGN KEY (order_id) REFERENCES order_read_model(id) ON DELETE CASCADE
);

-- Indexes for Event Store
CREATE INDEX idx_event_store_aggregate_id ON event_store(aggregate_id);
CREATE INDEX idx_event_store_aggregate_type ON event_store(aggregate_type);
CREATE INDEX idx_event_store_event_type ON event_store(event_type);
CREATE INDEX idx_event_store_timestamp ON event_store(timestamp);
CREATE INDEX idx_event_store_correlation_id ON event_store(correlation_id) WHERE correlation_id IS NOT NULL;
CREATE INDEX idx_event_store_aggregate_version ON event_store(aggregate_id, version);

-- Indexes for Snapshots
CREATE INDEX idx_snapshots_aggregate_id ON aggregate_snapshots(aggregate_id);
CREATE INDEX idx_snapshots_created_at ON aggregate_snapshots(created_at);

-- Indexes for Read Model - Orders
CREATE INDEX idx_order_read_customer_id ON order_read_model(customer_id);
CREATE INDEX idx_order_read_restaurant_id ON order_read_model(restaurant_id);
CREATE INDEX idx_order_read_status ON order_read_model(status);
CREATE INDEX idx_order_read_order_type ON order_read_model(order_type);
CREATE INDEX idx_order_read_created_at ON order_read_model(created_at);
CREATE INDEX idx_order_read_scheduled_time ON order_read_model(scheduled_time) WHERE scheduled_time IS NOT NULL;
CREATE INDEX idx_order_read_courier_id ON order_read_model(courier_id) WHERE courier_id IS NOT NULL;

-- Indexes for Read Model - Order Items
CREATE INDEX idx_order_items_read_order_id ON order_items_read_model(order_id);

-- Comments for documentation
COMMENT ON TABLE event_store IS 'Append-only event store for order aggregate events';
COMMENT ON TABLE aggregate_snapshots IS 'Periodic snapshots of order aggregate state for performance';
COMMENT ON TABLE order_read_model IS 'Denormalized read model for order queries (CQRS read side)';
COMMENT ON TABLE order_items_read_model IS 'Order items for the read model';
COMMENT ON COLUMN event_store.aggregate_id IS 'The order ID this event belongs to';
COMMENT ON COLUMN event_store.version IS 'Sequential version number for optimistic concurrency';
COMMENT ON COLUMN event_store.event_data IS 'JSON payload of the event';
COMMENT ON COLUMN order_read_model.last_event_id IS 'ID of the last event applied to this read model';
