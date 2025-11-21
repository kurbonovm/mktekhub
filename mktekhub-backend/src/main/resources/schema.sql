-- MKTekHub Inventory Management System
-- PostgreSQL Database Schema
-- Version: 3.1 (Fixed: Trigger Redundancy & Initial Capacity Calculation)

-- ============================================
-- Drop existing objects (for clean install)
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

-- Drop functions (for clean re-creation)
DROP FUNCTION IF EXISTS update_updated_at_column CASCADE;
DROP FUNCTION IF EXISTS update_warehouse_capacity CASCADE;
DROP FUNCTION IF EXISTS log_inventory_change CASCADE;

-- ============================================
-- Create custom ENUM types
-- ============================================
CREATE TYPE activity_type AS ENUM ('RECEIVE', 'TRANSFER', 'SALE', 'ADJUSTMENT', 'UPDATE', 'DELETE');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE');

-- ============================================
-- Table: user
-- ============================================
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
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
    max_capacity NUMERIC(12,2) NOT NULL CHECK (max_capacity > 0),
    current_capacity NUMERIC(12,2) DEFAULT 0 CHECK (current_capacity >= 0),
    capacity_alert_threshold DECIMAL(5,2) DEFAULT 80.00 CHECK (capacity_alert_threshold BETWEEN 0 AND 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

COMMENT ON COLUMN warehouse.max_capacity IS 'Maximum capacity in cubic feet';
COMMENT ON COLUMN warehouse.current_capacity IS 'Current used capacity in cubic feet';

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
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    brand VARCHAR(100),
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    unit_price DECIMAL(10,2) CHECK (unit_price >= 0),
    volume_per_unit NUMERIC(10,2) DEFAULT 1.00 CHECK (volume_per_unit >= 0),
    reorder_level INTEGER CHECK (reorder_level >= 0),
    warehouse_id BIGINT NOT NULL,
    warranty_end_date DATE,
    expiration_date DATE,
    barcode VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT,
    CONSTRAINT inventory_item_sku_warehouse_key UNIQUE (sku, warehouse_id)
);

COMMENT ON COLUMN inventory_item.volume_per_unit IS 'Volume per unit in cubic feet';

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

-- Function: Update warehouse current_capacity (volume-based)
CREATE OR REPLACE FUNCTION update_warehouse_capacity()
RETURNS TRIGGER AS $$
BEGIN
    -- Update capacity for old warehouse (if applicable, e.g., if item is moved/deleted)
    IF TG_OP = 'UPDATE' AND OLD.warehouse_id != NEW.warehouse_id THEN
        UPDATE warehouse
        SET current_capacity = (
            SELECT COALESCE(SUM(quantity * COALESCE(volume_per_unit, 1.00)), 0)
            FROM inventory_item
            WHERE warehouse_id = OLD.warehouse_id
        )
        WHERE id = OLD.warehouse_id;
    END IF;

    IF TG_OP = 'DELETE' THEN
        UPDATE warehouse
        SET current_capacity = (
            SELECT COALESCE(SUM(quantity * COALESCE(volume_per_unit, 1.00)), 0)
            FROM inventory_item
            WHERE warehouse_id = OLD.warehouse_id
        )
        WHERE id = OLD.warehouse_id;
        RETURN OLD;
    END IF;

    -- Update capacity for new/current warehouse (volume = quantity * volumePerUnit)
    UPDATE warehouse
    SET current_capacity = (
        SELECT COALESCE(SUM(quantity * COALESCE(volume_per_unit, 1.00)), 0)
        FROM inventory_item
        WHERE warehouse_id = NEW.warehouse_id
    )
    WHERE id = NEW.warehouse_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_warehouse_capacity() IS 'Updates warehouse current_capacity based on volume (quantity × volume_per_unit) of all items';

-- Trigger: Update warehouse capacity on inventory changes
CREATE TRIGGER trigger_update_warehouse_capacity
    AFTER INSERT OR UPDATE OR DELETE ON inventory_item
    FOR EACH ROW
    EXECUTE FUNCTION update_warehouse_capacity();

-- Function: Create stock_activity on inventory quantity change (FIXED for transfers)
CREATE OR REPLACE FUNCTION log_inventory_change()
RETURNS TRIGGER AS $$
DECLARE
    transfer_in_progress TEXT;
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.quantity != NEW.quantity THEN
        -- Check for the custom session variable set by the application during transfers/bulk ops.
        -- If set to 'true', the application is manually logging the TRANSFER activity, so we skip the automatic ADJUSTMENT log.
        SELECT current_setting('inventory.transfer_in_progress', true) INTO transfer_in_progress;
        
        IF transfer_in_progress = 'true' THEN
            RETURN NEW;
        END IF;

        -- If not a transfer transaction, log the change as ADJUSTMENT
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
            1, -- Default to user ID 1 (system user/admin)
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

-- Insert default admin and manager users
INSERT INTO "user" (username, password, email, is_active) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@mktekhub.com', true),
    ('manager', '$2a$10$aX.1Z.2Y.3X.4W.5V.6U.7T.8S.9R.0Q.1P.2O.3N.4M.5L.6K.7J.8I.9H.0G', 'manager@mktekhub.com', true);

-- Assign roles
INSERT INTO user_role (user_id, role_id) VALUES
    (1, 1), -- admin is ADMIN
    (2, 2); -- manager is MANAGER

-- Insert sample warehouses (capacity in cubic feet)
INSERT INTO warehouse (name, location, max_capacity, capacity_alert_threshold) VALUES
    ('Main Warehouse', '123 Industrial Blvd, New York, NY 10001', 10000.00, 85.00),
    ('West Coast Distribution Center', '456 Pacific Ave, Los Angeles, CA 90001', 15000.00, 80.00),
    ('East Coast Hub', '789 Atlantic Rd, Boston, MA 02101', 8000.00, 90.00);

-- Insert sample inventory items (with volume per unit in cubic feet)
INSERT INTO inventory_item (sku, name, description, category, brand, quantity, unit_price, volume_per_unit, reorder_level, warehouse_id, barcode) VALUES
    ('APL-LAP-MBP16', 'MacBook Pro 16" M3 Max', 'High-performance laptop for professional creative work', 'Electronics', 'Apple', 50, 2499.99, 0.50, 10, 1, '1234567890123'),
    ('LOGI-MOU-MX3S', 'MX Master 3S Mouse', 'Advanced wireless mouse for power users', 'Electronics', 'Logitech', 200, 99.99, 0.10, 50, 2, '1234567890125'),
    ('AERO-MON-27G2', 'AOC 27" Gaming Monitor', '27-inch 144Hz 1ms gaming monitor', 'Electronics', 'AOC', 75, 299.99, 1.50, 15, 3, '1234567890127'),
    ('BOWL-FUR-ERGO', 'Ergonomic Mesh Office Chair', 'Breathable mesh chair with adjustable lumbar support', 'Furniture', 'Bowler', 100, 249.99, 8.00, 20, 1, '1234567890124'),
    ('VERT-FUR-DESK2', 'Electric Standing Desk (Mahogany)', 'Dual-motor electric standing desk, 60" wide', 'Furniture', 'Vertigo', 30, 599.99, 25.00, 5, 2, '1234567890126');

-- Initial warehouse capacity calculation (FIXED: replaces direct trigger function call)
UPDATE warehouse w
SET current_capacity = sub.total_volume
FROM (
    -- Calculate total volume for each warehouse
    SELECT
        warehouse_id,
        COALESCE(SUM(quantity * COALESCE(volume_per_unit, 1.00)), 0) AS total_volume
    FROM inventory_item
    GROUP BY warehouse_id
) AS sub
WHERE w.id = sub.warehouse_id;

-- Ensure warehouses without items are set to 0
UPDATE warehouse
SET current_capacity = 0
WHERE id NOT IN (SELECT DISTINCT warehouse_id FROM inventory_item);

-- ============================================
-- Useful Views
-- ============================================

-- View: Warehouse utilization summary (volume-based)
CREATE VIEW warehouse_utilization AS
SELECT
    w.id,
    w.name,
    w.location,
    w.max_capacity as max_capacity_cubic_feet,
    w.current_capacity as current_capacity_cubic_feet,
    (w.max_capacity - w.current_capacity) as available_capacity_cubic_feet,
    ROUND((w.current_capacity / NULLIF(w.max_capacity, 0) * 100), 2) as utilization_percentage,
    w.capacity_alert_threshold,
    CASE
        WHEN (w.current_capacity / NULLIF(w.max_capacity, 0) * 100) >= w.capacity_alert_threshold
        THEN true
        ELSE false
    END as is_alert_triggered
FROM warehouse w
WHERE w.is_active = true;

-- View: Inventory items with volume details
CREATE VIEW inventory_volume_details AS
SELECT
    i.id,
    i.sku,
    i.name,
    i.category,
    i.quantity,
    i.volume_per_unit,
    (i.quantity * COALESCE(i.volume_per_unit, 1.00)) as total_volume,
    w.name as warehouse_name,
    w.current_capacity as warehouse_current_capacity,
    w.max_capacity as warehouse_max_capacity,
    ROUND(((i.quantity * COALESCE(i.volume_per_unit, 1.00)) / NULLIF(w.max_capacity, 0) * 100), 2) as percentage_of_warehouse
FROM inventory_item i
JOIN warehouse w ON i.warehouse_id = w.id
ORDER BY total_volume DESC;

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
-- Script Complete
-- ============================================
COMMENT ON DATABASE postgres IS 'MKTekHub Inventory Management System Database';