-- V1__initial_schema.sql
-- Booking Service: Table reservation and capacity management

-- Restaurant table (for booking context - may reference external restaurant service)
CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),
    opening_time TIME NOT NULL DEFAULT '10:00:00',
    closing_time TIME NOT NULL DEFAULT '22:00:00',
    max_party_size INTEGER NOT NULL DEFAULT 10,
    booking_slot_duration_minutes INTEGER NOT NULL DEFAULT 120,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table configurations
CREATE TABLE table_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    table_number VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL,
    min_capacity INTEGER NOT NULL DEFAULT 1,
    table_type VARCHAR(50) NOT NULL DEFAULT 'INDOOR',
    location_description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_table_number_restaurant UNIQUE (restaurant_id, table_number)
);

-- Bookings table
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    table_id UUID REFERENCES table_configurations(id),
    reservation_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    party_size INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    pre_order_id UUID,
    special_requests TEXT,
    internal_notes TEXT,
    reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for booking queries
CREATE INDEX idx_bookings_customer_id ON bookings(customer_id);
CREATE INDEX idx_bookings_restaurant_id ON bookings(restaurant_id);
CREATE INDEX idx_bookings_reservation_time ON bookings(reservation_time);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_table_id ON bookings(table_id);
CREATE INDEX idx_bookings_pre_order ON bookings(pre_order_id) WHERE pre_order_id IS NOT NULL;

-- Booking history/audit
CREATE TABLE booking_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255),
    change_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Blocked time slots (for restaurant closures, private events, etc.)
CREATE TABLE blocked_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    table_id UUID REFERENCES table_configurations(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blocked_slots_restaurant ON blocked_slots(restaurant_id, start_time, end_time);
CREATE INDEX idx_blocked_slots_table ON blocked_slots(table_id, start_time, end_time) WHERE table_id IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE restaurants IS 'Restaurant configuration for booking context';
COMMENT ON TABLE table_configurations IS 'Physical table setup with capacity information';
COMMENT ON TABLE bookings IS 'Customer reservations with status tracking';
COMMENT ON TABLE booking_history IS 'Audit trail for booking status changes';
COMMENT ON TABLE blocked_slots IS 'Temporarily unavailable time slots';
COMMENT ON COLUMN bookings.table_id IS 'NULL until table is assigned (can be auto-assigned at confirmation)';
COMMENT ON COLUMN bookings.pre_order_id IS 'Links to Order Service for hybrid orders';
