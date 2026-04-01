-- Sample data for testing
-- Run manually or via test profile

-- Insert test restaurant
INSERT INTO restaurants (id, name, address, phone, opening_time, closing_time, max_party_size, booking_slot_duration_minutes, is_active, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'PizzaFlow Downtown', '123 Main Street, Downtown', '+1-555-0100', '11:00:00', '23:00:00', 20, 90, true, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440002', 'PizzaFlow Waterfront', '456 Harbor Drive, Waterfront', '+1-555-0200', '12:00:00', '22:00:00', 16, 90, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert test tables for Downtown restaurant
INSERT INTO table_configurations (id, restaurant_id, table_number, capacity, min_capacity, table_type, location_description, is_active, created_at, updated_at)
VALUES
    -- Downtown Restaurant Tables
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Table 1', 2, 1, 'INDOOR', 'Near entrance', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Table 2', 2, 1, 'INDOOR', 'Near entrance', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 'Table 3', 4, 2, 'INDOOR', 'Center area', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'Table 4', 4, 2, 'INDOOR', 'Center area', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440001', 'Table 5', 6, 4, 'INDOOR', 'Window seat', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440001', 'Table 6', 8, 5, 'INDOOR', 'Large corner booth', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440001', 'Patio 1', 4, 2, 'OUTDOOR', 'Outdoor patio', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440001', 'Patio 2', 4, 2, 'OUTDOOR', 'Outdoor patio', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440001', 'Bar 1', 2, 1, 'BAR', 'Bar seating', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001', 'Bar 2', 2, 1, 'BAR', 'Bar seating', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', 'Private Room', 12, 8, 'PRIVATE', 'Private dining room', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440001', 'VIP Lounge', 10, 6, 'VIP', 'VIP area with premium service', true, NOW(), NOW()),
    
    -- Waterfront Restaurant Tables
    ('660e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440002', 'Table A', 2, 1, 'INDOOR', 'Harbor view', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440022', '550e8400-e29b-41d4-a716-446655440002', 'Table B', 4, 2, 'INDOOR', 'Harbor view', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440002', 'Table C', 4, 2, 'INDOOR', 'Interior', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440024', '550e8400-e29b-41d4-a716-446655440002', 'Table D', 6, 4, 'INDOOR', 'Interior booth', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440002', 'Deck 1', 4, 2, 'OUTDOOR', 'Waterfront deck', true, NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440026', '550e8400-e29b-41d4-a716-446655440002', 'Deck 2', 6, 3, 'OUTDOOR', 'Waterfront deck', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
