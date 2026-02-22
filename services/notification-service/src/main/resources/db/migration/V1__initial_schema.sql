-- Notification Service Database Schema
-- Manages notifications, templates, and user preferences

-- Notification templates
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    channel VARCHAR(20) NOT NULL, -- EMAIL, SMS, PUSH, IN_APP
    subject VARCHAR(255),
    body_template TEXT NOT NULL,
    variables JSONB,  -- List of expected template variables
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User notification preferences
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    
    -- Event-specific preferences
    order_updates BOOLEAN DEFAULT TRUE,
    payment_notifications BOOLEAN DEFAULT TRUE,
    delivery_tracking BOOLEAN DEFAULT TRUE,
    booking_reminders BOOLEAN DEFAULT TRUE,
    promotional_messages BOOLEAN DEFAULT FALSE,
    
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications log
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    channel VARCHAR(20) NOT NULL, -- EMAIL, SMS, PUSH, IN_APP
    template_id UUID REFERENCES notification_templates(id),
    
    -- Content
    subject VARCHAR(255),
    body TEXT NOT NULL,
    
    -- Delivery info
    recipient VARCHAR(255) NOT NULL, -- Email address, phone number, or device token
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, DELIVERED, FAILED, READ
    
    -- Context
    event_type VARCHAR(100),
    reference_id UUID, -- Order ID, Booking ID, etc.
    reference_type VARCHAR(50), -- ORDER, BOOKING, DELIVERY, PAYMENT
    
    -- Metadata
    metadata JSONB,
    
    -- Tracking
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    retry_count INT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- In-app notification inbox
CREATE TABLE in_app_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    icon VARCHAR(50),
    action_url VARCHAR(500),
    
    -- Context
    event_type VARCHAR(100),
    reference_id UUID,
    reference_type VARCHAR(50),
    
    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    
    -- Priority
    priority VARCHAR(20) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification batches (for bulk sends)
CREATE TABLE notification_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    template_id UUID REFERENCES notification_templates(id),
    channel VARCHAR(20) NOT NULL,
    total_count INT DEFAULT 0,
    sent_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, FAILED
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_event ON notifications(event_type, reference_id);
CREATE INDEX idx_notifications_created ON notifications(created_at);
CREATE INDEX idx_in_app_user_unread ON in_app_notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_templates_channel ON notification_templates(channel) WHERE is_active = TRUE;
