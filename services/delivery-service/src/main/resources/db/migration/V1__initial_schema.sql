-- Delivery Service Database Schema
-- Manages deliveries, couriers, and tracking

-- Couriers table
CREATE TABLE couriers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    vehicle_type VARCHAR(20) NOT NULL, -- BIKE, SCOOTER, CAR
    license_plate VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE', -- OFFLINE, AVAILABLE, ON_DELIVERY, BREAK
    current_latitude DECIMAL(10, 7),
    current_longitude DECIMAL(10, 7),
    last_location_update TIMESTAMP,
    rating DECIMAL(3, 2) DEFAULT 5.0,
    total_deliveries INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Deliveries table
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    courier_id UUID REFERENCES couriers(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, ARRIVED, DELIVERED, FAILED, CANCELLED
    
    -- Pickup location (restaurant)
    pickup_address VARCHAR(500) NOT NULL,
    pickup_latitude DECIMAL(10, 7) NOT NULL,
    pickup_longitude DECIMAL(10, 7) NOT NULL,
    pickup_instructions TEXT,
    
    -- Delivery location (customer)
    delivery_address VARCHAR(500) NOT NULL,
    delivery_latitude DECIMAL(10, 7) NOT NULL,
    delivery_longitude DECIMAL(10, 7) NOT NULL,
    delivery_instructions TEXT,
    
    -- Customer info
    customer_id UUID NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    
    -- Timing
    estimated_pickup_time TIMESTAMP,
    actual_pickup_time TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    
    -- Distance and ETA
    distance_km DECIMAL(6, 2),
    estimated_duration_minutes INT,
    
    -- Delivery details
    delivery_fee DECIMAL(10, 2),
    tip_amount DECIMAL(10, 2) DEFAULT 0,
    priority VARCHAR(20) DEFAULT 'NORMAL', -- NORMAL, EXPRESS, SCHEDULED
    
    -- Proof of delivery
    delivery_photo_url VARCHAR(500),
    signature_url VARCHAR(500),
    delivery_notes TEXT,
    
    -- Failure tracking
    failure_reason VARCHAR(500),
    retry_count INT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Delivery location history (for tracking route)
CREATE TABLE delivery_location_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    courier_id UUID NOT NULL REFERENCES couriers(id),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    speed_kmh DECIMAL(5, 2),
    heading DECIMAL(5, 2),
    accuracy_meters DECIMAL(6, 2),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Delivery status history (audit trail)
CREATE TABLE delivery_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    changed_by UUID,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Courier shifts
CREATE TABLE courier_shifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    courier_id UUID NOT NULL REFERENCES couriers(id),
    shift_start TIMESTAMP NOT NULL,
    shift_end TIMESTAMP,
    deliveries_completed INT DEFAULT 0,
    total_distance_km DECIMAL(8, 2) DEFAULT 0,
    total_tips DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Delivery zones (optional, for area-based assignments)
CREATE TABLE delivery_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    center_latitude DECIMAL(10, 7) NOT NULL,
    center_longitude DECIMAL(10, 7) NOT NULL,
    radius_km DECIMAL(6, 2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_couriers_status ON couriers(status) WHERE is_active = TRUE;
CREATE INDEX idx_couriers_location ON couriers(current_latitude, current_longitude) WHERE status = 'AVAILABLE';
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_courier_id ON deliveries(courier_id);
CREATE INDEX idx_deliveries_customer_id ON deliveries(customer_id);
CREATE INDEX idx_delivery_location_history_delivery ON delivery_location_history(delivery_id, recorded_at DESC);
CREATE INDEX idx_delivery_status_history_delivery ON delivery_status_history(delivery_id, created_at);
CREATE INDEX idx_courier_shifts_courier ON courier_shifts(courier_id, shift_start DESC);
