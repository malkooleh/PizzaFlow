-- Sample data for testing
-- Run manually or via test profile

-- Insert test couriers
INSERT INTO couriers (id, user_id, name, phone, email, vehicle_type, license_plate, status, current_latitude, current_longitude, rating, total_deliveries, is_active, created_at, updated_at)
VALUES
    ('770e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440001', 'Mike Johnson', '+1-555-1001', 'mike.j@courier.com', 'SCOOTER', 'ABC-123', 'AVAILABLE', 40.7128, -74.0060, 4.85, 523, true, NOW(), NOW()),
    ('770e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440002', 'Sarah Williams', '+1-555-1002', 'sarah.w@courier.com', 'BIKE', NULL, 'AVAILABLE', 40.7200, -74.0100, 4.92, 312, true, NOW(), NOW()),
    ('770e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440003', 'David Brown', '+1-555-1003', 'david.b@courier.com', 'CAR', 'XYZ-789', 'OFFLINE', NULL, NULL, 4.78, 847, true, NOW(), NOW()),
    ('770e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440004', 'Emily Davis', '+1-555-1004', 'emily.d@courier.com', 'SCOOTER', 'DEF-456', 'ON_DELIVERY', 40.7180, -74.0080, 4.95, 198, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert test delivery zone
INSERT INTO delivery_zones (id, name, description, center_latitude, center_longitude, radius_km, is_active, created_at)
VALUES
    ('990e8400-e29b-41d4-a716-446655440001', 'Manhattan Downtown', 'Core delivery area covering downtown Manhattan', 40.7128, -74.0060, 10.0, true, NOW()),
    ('990e8400-e29b-41d4-a716-446655440002', 'Brooklyn Heights', 'Brooklyn Heights and surrounding area', 40.6958, -73.9936, 8.0, true, NOW())
ON CONFLICT (id) DO NOTHING;
