-- Inventory Service - Initial Schema with Outbox Pattern
-- Migration: V1__initial_schema.sql
-- Description: Creates ingredients, stock_levels, reservations, and inventory_outbox tables

CREATE TABLE IF NOT EXISTS ingredients (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    minimum_stock_level DECIMAL(10, 3) NOT NULL,
    reorder_quantity DECIMAL(10, 3) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_levels (
    id UUID PRIMARY KEY,
    ingredient_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    current_quantity DECIMAL(10, 3) NOT NULL,
    reserved_quantity DECIMAL(10, 3) NOT NULL DEFAULT 0,
    available_quantity DECIMAL(10, 3) GENERATED ALWAYS AS (current_quantity - reserved_quantity) STORED,
    last_restocked_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_stock_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id),
    CONSTRAINT uk_stock_ingredient_restaurant UNIQUE (ingredient_id, restaurant_id),
    CONSTRAINT chk_positive_quantities CHECK (current_quantity >= 0 AND reserved_quantity >= 0)
);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    ingredient_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    scheduled_for TIMESTAMP,
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    released_at TIMESTAMP,
    CONSTRAINT fk_reservation_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
);

CREATE TABLE IF NOT EXISTS inventory_outbox (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_ingredients_category ON ingredients(category);
CREATE INDEX idx_stock_levels_restaurant ON stock_levels(restaurant_id);
CREATE INDEX idx_stock_levels_ingredient ON stock_levels(ingredient_id);
CREATE INDEX idx_stock_levels_low_stock ON stock_levels(available_quantity) WHERE available_quantity <= (SELECT minimum_stock_level FROM ingredients WHERE id = ingredient_id);
CREATE INDEX idx_reservations_order_id ON reservations(order_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_scheduled ON reservations(scheduled_for) WHERE scheduled_for IS NOT NULL;
CREATE INDEX idx_outbox_published ON inventory_outbox(published) WHERE published = FALSE;
CREATE INDEX idx_outbox_created_at ON inventory_outbox(created_at);

-- Comments
COMMENT ON TABLE ingredients IS 'Master list of all ingredients used in recipes';
COMMENT ON TABLE stock_levels IS 'Current and reserved stock per restaurant location';
COMMENT ON TABLE reservations IS 'Ingredient reservations for pending orders';
COMMENT ON TABLE inventory_outbox IS 'Transactional outbox for reliable event publishing';
COMMENT ON COLUMN stock_levels.available_quantity IS 'Computed column: current_quantity - reserved_quantity';
COMMENT ON COLUMN reservations.status IS 'PENDING, CONFIRMED, RELEASED, EXPIRED';
COMMENT ON COLUMN inventory_outbox.aggregate_type IS 'INGREDIENT, RESERVATION, STOCK_LEVEL';
