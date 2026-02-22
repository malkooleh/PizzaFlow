-- Sample notification templates

-- Order templates
INSERT INTO notification_templates (id, name, channel, subject, body_template, is_active, created_at)
VALUES 
    ('a1b2c3d4-0001-4000-8000-000000000001', 'order-confirmation', 'EMAIL', 
     'Order Confirmation - #{{orderNumber}}', 
     'Thank you for your order #{{orderNumber}}! We''ve received your order and it''s being processed. You''ll receive updates as your order progresses.',
     true, NOW()),

    ('a1b2c3d4-0002-4000-8000-000000000002', 'order-ready', 'IN_APP',
     'Order Ready',
     'Your order #{{orderNumber}} is ready for pickup!',
     true, NOW());

-- Payment templates  
INSERT INTO notification_templates (id, name, channel, subject, body_template, is_active, created_at)
VALUES
    ('a1b2c3d4-0003-4000-8000-000000000003', 'payment-receipt', 'EMAIL',
     'Payment Confirmation - {{amount}}',
     'Your payment of {{amount}} has been processed successfully. Transaction Reference: {{transactionRef}}',
     true, NOW()),

    ('a1b2c3d4-0004-4000-8000-000000000004', 'payment-failed', 'EMAIL',
     'Payment Failed',
     'Unfortunately, your payment could not be processed. Reason: {{reason}}. Please try again or contact support.',
     true, NOW());

-- Booking templates
INSERT INTO notification_templates (id, name, channel, subject, body_template, is_active, created_at)
VALUES
    ('a1b2c3d4-0005-4000-8000-000000000005', 'booking-confirmation', 'EMAIL',
     'Booking Confirmation at {{restaurantName}}',
     'Your table for {{partySize}} at {{restaurantName}} has been booked for {{bookingTime}}. We look forward to seeing you!',
     true, NOW()),

    ('a1b2c3d4-0006-4000-8000-000000000006', 'booking-confirmed', 'EMAIL',
     'Booking Confirmed - {{restaurantName}}',
     'Great news! Your reservation at {{restaurantName}} for {{bookingTime}} is now confirmed. Your table: {{tableName}}.',
     true, NOW()),

    ('a1b2c3d4-0007-4000-8000-000000000007', 'booking-reminder', 'EMAIL',
     'Reminder: Reservation at {{restaurantName}}',
     'This is a reminder about your reservation at {{restaurantName}} on {{bookingTime}}. Address: {{restaurantAddress}}. See you soon!',
     true, NOW());

-- Delivery templates
INSERT INTO notification_templates (id, name, channel, subject, body_template, is_active, created_at)
VALUES
    ('a1b2c3d4-0008-4000-8000-000000000008', 'delivery-complete', 'EMAIL',
     'Order Delivered!',
     'Your order has been delivered! We hope you enjoy your meal. Thank you for ordering with PizzaFlow!',
     true, NOW()),

    ('a1b2c3d4-0009-4000-8000-000000000009', 'delivery-assigned', 'PUSH',
     'Driver on the way!',
     '{{courierName}} is on the way with your order. Estimated arrival: {{estimatedTime}}',
     true, NOW());

-- Promotional templates
INSERT INTO notification_templates (id, name, channel, subject, body_template, is_active, created_at)
VALUES
    ('a1b2c3d4-0010-4000-8000-000000000010', 'welcome-email', 'EMAIL',
     'Welcome to PizzaFlow! 🍕',
     'Welcome to PizzaFlow, {{userName}}! We''re excited to have you. Start exploring our menu and enjoy delicious pizzas!',
     true, NOW());
