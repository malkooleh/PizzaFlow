-- V3__add_missing_columns.sql
-- Add missing columns to tables for Phase 2 completion

-- Add name column to table_configurations
ALTER TABLE table_configurations 
ADD COLUMN IF NOT EXISTS name VARCHAR(100);

-- Add actual_arrival_time column to bookings
ALTER TABLE bookings 
ADD COLUMN IF NOT EXISTS actual_arrival_time TIMESTAMP;
