-- Payment Service - Initial Schema
-- Migration: V1__initial_schema.sql
-- Description: Creates transactions, payment_methods, and refunds tables

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,
    payment_method_type VARCHAR(50) NOT NULL,
    payment_gateway VARCHAR(50),
    gateway_transaction_id VARCHAR(255),
    gateway_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(20),
    card_expiry_month INTEGER,
    card_expiry_year INTEGER,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refunds (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    order_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    gateway_refund_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_refund_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

-- Indexes
CREATE INDEX idx_transactions_order_id ON transactions(order_id);
CREATE INDEX idx_transactions_customer_id ON transactions(customer_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_payment_methods_customer_id ON payment_methods(customer_id);
CREATE INDEX idx_refunds_transaction_id ON refunds(transaction_id);
CREATE INDEX idx_refunds_order_id ON refunds(order_id);

-- Comments
COMMENT ON TABLE transactions IS 'Payment transactions linked to orders';
COMMENT ON TABLE payment_methods IS 'Customer saved payment methods';
COMMENT ON TABLE refunds IS 'Refund requests and processing';
COMMENT ON COLUMN transactions.status IS 'PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED';
COMMENT ON COLUMN transactions.payment_method_type IS 'CREDIT_CARD, DEBIT_CARD, PAYPAL, CASH_ON_DELIVERY';
