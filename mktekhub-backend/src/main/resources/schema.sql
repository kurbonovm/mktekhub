-- MKTekHub Inventory Management System
-- PostgreSQL Database Schema
-- Version: 2.0 (Updated)

-- ============================================
-- Drop existing tables (for clean install)
-- ============================================
DROP TABLE IF EXISTS stock_activity CASCADE;
DROP TABLE IF EXISTS inventory_item CASCADE;
DROP TABLE IF EXISTS audit CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS warehouse CASCADE;

-- Drop custom types
DROP TYPE IF EXISTS activity_type CASCADE;
DROP TYPE IF EXISTS audit_action CASCADE;

-- ============================================
-- Create custom ENUM types
-- ============================================
CREATE TYPE activity_type AS ENUM ('RECEIVE', 'TRANSFER', 'SALE', 'ADJUSTMENT', 'DELETE');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE');

-- ============================================
-- Table: user
-- ============================================
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for user table
CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_is_active ON "user"(is_active);

-- ============================================
-- Table: role
-- ============================================
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for role table
CREATE INDEX idx_role_name ON role(name);

-- ============================================
-- Table: user_role (Junction Table)
-- ============================================
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- Indexes for user_role table
CREATE INDEX idx_user_role_user_id ON user_role(user_id);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);

-- ============================================
-- Table: warehouse
-- ============================================
CREATE TABLE warehouse (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    max_capacity INTEGER NOT NULL CHECK (max_capacity > 0),
    current_capacity INTEGER DEFAULT 0 CHECK (current_capacity >= 0),
    capacity_alert_threshold DECIMAL(5,2) DEFAULT 80.00 CHECK (capacity_alert_threshold BETWEEN 0 AND 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Indexes for warehouse table
CREATE INDEX idx_warehouse_name ON warehouse(name);
CREATE INDEX idx_warehouse_is_active ON warehouse(is_active);

-- Check constraint: current_capacity cannot exceed max_capacity
ALTER TABLE warehouse ADD CONSTRAINT chk_warehouse_capacity
    CHECK (current_capacity <= max_capacity);

-- ============================================
-- Table: inventory_item
-- ============================================
CREATE TABLE inventory_item (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    brand VARCHAR(100),
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    unit_price DECIMAL(10,2) CHECK (unit_price >= 0),
    reorder_level INTEGER CHECK (reorder_level >= 0),
    warehouse_id BIGINT NOT NULL,
    warranty_end_date DATE,
    expiration_date DATE,
    barcode VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT
);

-- Indexes for inventory_item table
CREATE INDEX idx_inventory_item_sku ON inventory_item(sku);
CREATE INDEX idx_inventory_item_warehouse_id ON inventory_item(warehouse_id);
CREATE INDEX idx_inventory_item_category ON inventory_item(category);
CREATE INDEX idx_inventory_item_name ON inventory_item(name);
CREATE INDEX idx_inventory_item_barcode ON inventory_item(barcode);

-- ============================================
-- Table: stock_activity
-- ============================================
CREATE TABLE stock_activity (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    item_sku VARCHAR(50) NOT NULL,
    activity_type activity_type NOT NULL,
    quantity_change INTEGER NOT NULL,
    previous_quantity INTEGER,
    new_quantity INTEGER,
    source_warehouse_id BIGINT,
    destination_warehouse_id BIGINT,
    performed_by BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (item_id) REFERENCES inventory_item(id) ON DELETE CASCADE,
    FOREIGN KEY (source_warehouse_id) REFERENCES warehouse(id) ON DELETE SET NULL,
    FOREIGN KEY (destination_warehouse_id) REFERENCES warehouse(id) ON DELETE SET NULL,
    FOREIGN KEY (performed_by) REFERENCES "user"(id) ON DELETE RESTRICT
);

-- Indexes for stock_activity table
CREATE INDEX idx_stock_activity_item_id ON stock_activity(item_id);
CREATE INDEX idx_stock_activity_activity_type ON stock_activity(activity_type);
CREATE INDEX idx_stock_activity_timestamp ON stock_activity(timestamp);
CREATE INDEX idx_stock_activity_performed_by ON stock_activity(performed_by);
CREATE INDEX idx_stock_activity_source_warehouse ON stock_activity(source_warehouse_id);
CREATE INDEX idx_stock_activity_destination_warehouse ON stock_activity(destination_warehouse_id);

-- Check constraint: For TRANSFER, source and destination must be different
ALTER TABLE stock_activity ADD CONSTRAINT chk_transfer_warehouses
    CHECK (
        activity_type != 'TRANSFER' OR
        (source_warehouse_id IS NOT NULL AND
         destination_warehouse_id IS NOT NULL AND
         source_warehouse_id != destination_warehouse_id)
    );

-- ============================================
-- Table: audit
-- ============================================
CREATE TABLE audit (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action audit_action NOT NULL,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changes JSONB,
    FOREIGN KEY (created_by_id) REFERENCES "user"(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by_id) REFERENCES "user"(id) ON DELETE SET NULL
);

-- Indexes for audit table
CREATE INDEX idx_audit_entity ON audit(entity_type, entity_id);
CREATE INDEX idx_audit_created_by ON audit(created_by_id);
CREATE INDEX idx_audit_updated_by ON audit(updated_by_id);
CREATE INDEX idx_audit_created_at ON audit(created_at);
CREATE INDEX idx_audit_changes ON audit USING GIN (changes);

-- ============================================
-- Functions and Triggers
-- ============================================

-- Function: Update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Update updated_at for user table
CREATE TRIGGER trigger_user_updated_at
    BEFORE UPDATE ON "user"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger: Update updated_at for warehouse table
CREATE TRIGGER trigger_warehouse_updated_at
    BEFORE UPDATE ON warehouse
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger: Update updated_at for inventory_item table
CREATE TRIGGER trigger_inventory_item_updated_at
    BEFORE UPDATE ON inventory_item
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function: Update warehouse current_capacity
CREATE OR REPLACE FUNCTION update_warehouse_capacity()
RETURNS TRIGGER AS $$
BEGIN
    -- Update capacity for old warehouse (if applicable)
    IF TG_OP = 'UPDATE' AND OLD.warehouse_id != NEW.warehouse_id THEN
        UPDATE warehouse
        SET current_capacity = (
            SELECT COALESCE(SUM(quantity), 0)
            FROM inventory_item
            WHERE warehouse_id = OLD.warehouse_id
        )
        WHERE id = OLD.warehouse_id;
    END IF;

    IF TG_OP = 'DELETE' THEN
        UPDATE warehouse
        SET current_capacity = (
            SELECT COALESCE(SUM(quantity), 0)
            FROM inventory_item
            WHERE warehouse_id = OLD.warehouse_id
        )
        WHERE id = OLD.warehouse_id;
        RETURN OLD;
    END IF;

    -- Update capacity for new/current warehouse
    UPDATE warehouse
    SET current_capacity = (
        SELECT COALESCE(SUM(quantity), 0)
        FROM inventory_item
        WHERE warehouse_id = NEW.warehouse_id
    )
    WHERE id = NEW.warehouse_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Update warehouse capacity on inventory changes
CREATE TRIGGER trigger_update_warehouse_capacity
    AFTER INSERT OR UPDATE OR DELETE ON inventory_item
    FOR EACH ROW
    EXECUTE FUNCTION update_warehouse_capacity();

-- Function: Create stock_activity on inventory quantity change
CREATE OR REPLACE FUNCTION log_inventory_change()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.quantity != NEW.quantity THEN
        INSERT INTO stock_activity (
            item_id,
            item_sku,
            activity_type,
            quantity_change,
            previous_quantity,
            new_quantity,
            destination_warehouse_id,
            performed_by,
            notes
        ) VALUES (
            NEW.id,
            NEW.sku,
            'ADJUSTMENT',
            NEW.quantity - OLD.quantity,
            OLD.quantity,
            NEW.quantity,
            NEW.warehouse_id,
            1, -- Default to user ID 1 (system user) - should be updated by application
            'Automatic adjustment logged by trigger'
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Log inventory changes
CREATE TRIGGER trigger_log_inventory_change
    AFTER UPDATE ON inventory_item
    FOR EACH ROW
    EXECUTE FUNCTION log_inventory_change();

-- ============================================
-- Seed Data (Initial Setup)
-- ============================================

-- Insert default roles
INSERT INTO role (name, description) VALUES
    ('ADMIN', 'Full system access with all privileges'),
    ('MANAGER', 'Warehouse and inventory management access'),
    ('VIEWER', 'Read-only access to view data');

-- Insert default admin user (password: 'admin123' - hashed with BCrypt)
-- NOTE: Change this password in production!
INSERT INTO "user" (username, password, email, is_active) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@mktekhub.com', true);

-- Assign ADMIN role to admin user
INSERT INTO user_role (user_id, role_id) VALUES
    (1, 1);

-- Insert sample warehouses
INSERT INTO warehouse (name, location, max_capacity, capacity_alert_threshold) VALUES
    ('Main Warehouse', '123 Industrial Blvd, New York, NY 10001', 10000, 85.00),
    ('West Coast Distribution Center', '456 Pacific Ave, Los Angeles, CA 90001', 15000, 80.00),
    ('East Coast Hub', '789 Atlantic Rd, Boston, MA 02101', 8000, 90.00);

-- Insert sample inventory items
INSERT INTO inventory_item (sku, name, description, category, brand, quantity, unit_price, reorder_level, warehouse_id, barcode) VALUES
    ('SKU-001', 'Laptop Computer', 'High-performance business laptop', 'Electronics', 'TechBrand', 50, 999.99, 10, 1, '1234567890123'),
    ('SKU-002', 'Office Chair', 'Ergonomic office chair with lumbar support', 'Furniture', 'ComfortPlus', 100, 249.99, 20, 1, '1234567890124'),
    ('SKU-003', 'Wireless Mouse', 'Bluetooth wireless mouse', 'Electronics', 'TechBrand', 200, 29.99, 50, 2, '1234567890125'),
    ('SKU-004', 'Standing Desk', 'Electric height-adjustable standing desk', 'Furniture', 'ComfortPlus', 30, 599.99, 5, 2, '1234567890126'),
    ('SKU-005', 'Monitor 27"', '27-inch 4K LED monitor', 'Electronics', 'ViewMaster', 75, 399.99, 15, 3, '1234567890127');

-- ============================================
-- Useful Views
-- ============================================

-- View: Warehouse utilization summary
CREATE VIEW warehouse_utilization AS
SELECT
    w.id,
    w.name,
    w.location,
    w.max_capacity,
    w.current_capacity,
    ROUND((w.current_capacity::DECIMAL / w.max_capacity * 100), 2) as utilization_percentage,
    w.capacity_alert_threshold,
    CASE
        WHEN (w.current_capacity::DECIMAL / w.max_capacity * 100) >= w.capacity_alert_threshold
        THEN true
        ELSE false
    END as is_alert_triggered
FROM warehouse w
WHERE w.is_active = true;

-- View: Low stock items
CREATE VIEW low_stock_items AS
SELECT
    i.id,
    i.sku,
    i.name,
    i.category,
    i.quantity,
    i.reorder_level,
    w.name as warehouse_name,
    w.location as warehouse_location
FROM inventory_item i
JOIN warehouse w ON i.warehouse_id = w.id
WHERE i.quantity <= i.reorder_level
    AND i.reorder_level IS NOT NULL
ORDER BY i.quantity ASC;

-- View: Expiring items (within 30 days)
CREATE VIEW expiring_items AS
SELECT
    i.id,
    i.sku,
    i.name,
    i.expiration_date,
    (i.expiration_date - CURRENT_DATE) as days_until_expiration,
    i.quantity,
    w.name as warehouse_name
FROM inventory_item i
JOIN warehouse w ON i.warehouse_id = w.id
WHERE i.expiration_date IS NOT NULL
    AND i.expiration_date <= CURRENT_DATE + INTERVAL '30 days'
    AND i.expiration_date >= CURRENT_DATE
ORDER BY i.expiration_date ASC;

-- View: Recent stock activity (last 30 days)
CREATE VIEW recent_stock_activity AS
SELECT
    sa.id,
    sa.timestamp,
    sa.activity_type,
    i.sku,
    i.name as item_name,
    sa.quantity_change,
    sa.previous_quantity,
    sa.new_quantity,
    sw.name as source_warehouse,
    dw.name as destination_warehouse,
    u.username as performed_by_user
FROM stock_activity sa
JOIN inventory_item i ON sa.item_id = i.id
LEFT JOIN warehouse sw ON sa.source_warehouse_id = sw.id
LEFT JOIN warehouse dw ON sa.destination_warehouse_id = dw.id
JOIN "user" u ON sa.performed_by = u.id
WHERE sa.timestamp >= CURRENT_DATE - INTERVAL '30 days'
ORDER BY sa.timestamp DESC;

-- ============================================
-- Grant Permissions (Optional - adjust as needed)
-- ============================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO your_app_user;

-- ============================================
-- Script Complete
-- ============================================
COMMENT ON DATABASE postgres IS 'MKTekHub Inventory Management System Database';
