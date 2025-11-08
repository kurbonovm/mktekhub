# MKTekHub - Updated Database Schema (ERD)

## Overview

This document provides the updated Entity-Relationship Diagram (ERD) schema for the MKTekHub Inventory Management System, incorporating best practices and addressing referential integrity issues.

---

## Entities and Attributes

### 1. User
**Purpose:** Stores user account information for authentication and authorization.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | User's login name |
| password | VARCHAR(255) | NOT NULL | Hashed password (BCrypt) |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User's email address |
| is_active | BOOLEAN | DEFAULT TRUE | Account active status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation timestamp |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last modification timestamp |

**Indexes:**
- PRIMARY KEY (id)
- UNIQUE INDEX (username)
- UNIQUE INDEX (email)

---

### 2. Role
**Purpose:** Defines user roles/permissions in the system.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(50) | UNIQUE, NOT NULL | Role name (e.g., ADMIN, USER, MANAGER) |
| description | VARCHAR(255) | NULL | Role description |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Role creation timestamp |

**Indexes:**
- PRIMARY KEY (id)
- UNIQUE INDEX (name)

**Common Roles:**
- ADMIN - Full system access
- MANAGER - Warehouse and inventory management
- VIEWER - Read-only access

---

### 3. User_Role
**Purpose:** Junction table for many-to-many relationship between Users and Roles.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| user_id | BIGINT | PK, FK → User.id | Reference to User |
| role_id | BIGINT | PK, FK → Role.id | Reference to Role |
| assigned_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | When role was assigned |

**Indexes:**
- PRIMARY KEY (user_id, role_id)
- FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
- FOREIGN KEY (role_id) REFERENCES Role(id) ON DELETE CASCADE

---

### 4. Warehouse
**Purpose:** Stores warehouse location and capacity information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Warehouse name |
| location | VARCHAR(255) | NOT NULL | Physical address/location |
| max_capacity | INTEGER | NOT NULL, CHECK > 0 | Maximum storage capacity |
| current_capacity | INTEGER | DEFAULT 0, CHECK >= 0 | Current usage (auto-calculated) |
| capacity_alert_threshold | DECIMAL(5,2) | DEFAULT 80.00, CHECK 0-100 | Alert threshold percentage |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Warehouse creation timestamp |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last modification timestamp |
| is_active | BOOLEAN | DEFAULT TRUE | Warehouse active status |

**Indexes:**
- PRIMARY KEY (id)
- INDEX (name)

**Business Logic:**
- `current_capacity` should be calculated/updated via triggers or application logic
- Alert when: `(current_capacity / max_capacity * 100) >= capacity_alert_threshold`

---

### 5. InventoryItem
**Purpose:** Stores individual inventory item information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| sku | VARCHAR(50) | UNIQUE, NOT NULL | Stock Keeping Unit |
| name | VARCHAR(255) | NOT NULL | Product name |
| description | TEXT | NULL | Detailed description |
| category | VARCHAR(100) | NULL | Product category |
| brand | VARCHAR(100) | NULL | Product brand/manufacturer |
| quantity | INTEGER | DEFAULT 0, CHECK >= 0 | Current stock quantity |
| unit_price | DECIMAL(10,2) | NULL, CHECK >= 0 | Unit price/cost |
| reorder_level | INTEGER | NULL, CHECK >= 0 | Minimum quantity threshold |
| warehouse_id | BIGINT | FK → Warehouse.id, NOT NULL | Current warehouse location |
| warranty_end_date | DATE | NULL | Warranty expiration date |
| expiration_date | DATE | NULL | Product expiration date (optional) |
| barcode | VARCHAR(100) | NULL | Barcode/UPC code |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Item creation timestamp |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last modification timestamp |

**Indexes:**
- PRIMARY KEY (id)
- UNIQUE INDEX (sku)
- INDEX (warehouse_id)
- INDEX (category)
- INDEX (name)
- FOREIGN KEY (warehouse_id) REFERENCES Warehouse(id) ON DELETE RESTRICT

**Business Rules:**
- Cannot delete warehouse if it contains inventory items (RESTRICT)
- Low stock alert when: `quantity <= reorder_level`
- Expiration alert when: `expiration_date <= CURRENT_DATE + 30 days`

---

### 6. Stock_Activity
**Purpose:** Audit trail for all inventory movements and changes.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| item_id | BIGINT | FK → InventoryItem.id, NOT NULL | Reference to inventory item |
| item_sku | VARCHAR(50) | NOT NULL | SKU snapshot (for historical reference) |
| activity_type | ENUM | NOT NULL | RECEIVE, TRANSFER, SALE, ADJUSTMENT, DELETE |
| quantity_change | INTEGER | NOT NULL | Quantity changed (+/-) |
| previous_quantity | INTEGER | NULL | Quantity before change |
| new_quantity | INTEGER | NULL | Quantity after change |
| source_warehouse_id | BIGINT | FK → Warehouse.id, NULL | Source warehouse (for transfers) |
| destination_warehouse_id | BIGINT | FK → Warehouse.id, NULL | Destination warehouse (for transfers/receives) |
| performed_by | BIGINT | FK → User.id, NOT NULL | User who performed action |
| timestamp | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | When action occurred |
| notes | TEXT | NULL | Additional context/comments |

**Indexes:**
- PRIMARY KEY (id)
- INDEX (item_id)
- INDEX (activity_type)
- INDEX (timestamp)
- INDEX (performed_by)
- FOREIGN KEY (item_id) REFERENCES InventoryItem(id) ON DELETE CASCADE
- FOREIGN KEY (source_warehouse_id) REFERENCES Warehouse(id) ON DELETE SET NULL
- FOREIGN KEY (destination_warehouse_id) REFERENCES Warehouse(id) ON DELETE SET NULL
- FOREIGN KEY (performed_by) REFERENCES User(id) ON DELETE RESTRICT

**Activity Types:**
- **RECEIVE** - New inventory added to warehouse
- **TRANSFER** - Movement between warehouses
- **SALE** - Inventory sold/removed
- **ADJUSTMENT** - Manual quantity correction
- **DELETE** - Item removed from system

**Validation Rules:**
- For TRANSFER: both `source_warehouse_id` and `destination_warehouse_id` must be set and different
- For RECEIVE: only `destination_warehouse_id` is set
- For SALE/ADJUSTMENT: neither source nor destination required

---

### 7. Audit
**Purpose:** Generic audit trail for all database operations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| entity_type | VARCHAR(50) | NOT NULL | Table/entity name |
| entity_id | BIGINT | NOT NULL | ID of affected record |
| action | ENUM | NOT NULL | CREATE, UPDATE, DELETE |
| created_by_id | BIGINT | FK → User.id, NULL | User who created record |
| updated_by_id | BIGINT | FK → User.id, NULL | User who updated record |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Audit entry creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last modification time |
| changes | JSON | NULL | JSON object with before/after values |

**Indexes:**
- PRIMARY KEY (id)
- INDEX (entity_type, entity_id)
- INDEX (created_by_id)
- INDEX (updated_by_id)
- INDEX (created_at)
- FOREIGN KEY (created_by_id) REFERENCES User(id) ON DELETE SET NULL
- FOREIGN KEY (updated_by_id) REFERENCES User(id) ON DELETE SET NULL

---

## Relationships

### One-to-Many Relationships

1. **Warehouse → InventoryItem** (1:N)
   - One warehouse contains many inventory items
   - FK: `InventoryItem.warehouse_id` → `Warehouse.id`
   - ON DELETE: RESTRICT (cannot delete warehouse with items)

2. **User → Stock_Activity** (1:N)
   - One user performs many stock activities
   - FK: `Stock_Activity.performed_by` → `User.id`
   - ON DELETE: RESTRICT (preserve audit trail)

3. **InventoryItem → Stock_Activity** (1:N)
   - One inventory item has many stock activities
   - FK: `Stock_Activity.item_id` → `InventoryItem.id`
   - ON DELETE: CASCADE (delete activities when item deleted)

4. **Warehouse → Stock_Activity (source)** (1:N)
   - One warehouse is source for many transfers
   - FK: `Stock_Activity.source_warehouse_id` → `Warehouse.id`
   - ON DELETE: SET NULL (preserve history)

5. **Warehouse → Stock_Activity (destination)** (1:N)
   - One warehouse is destination for many receives/transfers
   - FK: `Stock_Activity.destination_warehouse_id` → `Warehouse.id`
   - ON DELETE: SET NULL (preserve history)

### Many-to-Many Relationships

1. **User ↔ Role** (M:N via User_Role)
   - Users can have multiple roles
   - Roles can be assigned to multiple users
   - Junction table: `User_Role`

---

## Database Triggers

### 1. Update Warehouse Current Capacity
```sql
-- Trigger on InventoryItem INSERT/UPDATE/DELETE
-- Automatically recalculate Warehouse.current_capacity
-- Based on SUM of all InventoryItem.quantity in that warehouse
```

### 2. Create Stock Activity on Inventory Change
```sql
-- Trigger on InventoryItem UPDATE (quantity change)
-- Automatically create Stock_Activity record
-- Capture previous and new quantity values
```

### 3. Prevent Warehouse Overcapacity
```sql
-- Trigger on InventoryItem INSERT/UPDATE
-- Check if total quantity would exceed Warehouse.max_capacity
-- RAISE EXCEPTION if capacity exceeded
```

### 4. Populate Audit Table
```sql
-- Triggers on all major tables (INSERT/UPDATE/DELETE)
-- Automatically create Audit records
-- Capture before/after state in JSON format
```

---

## Key Changes from Original ERD

### Critical Fixes
✅ **Stock_Activity.item_id** - Changed from `item_sku` (VARCHAR) to `item_id` (FK) for referential integrity
- Still includes `item_sku` as VARCHAR for historical snapshot
- Prevents orphaned records if items are deleted
- Enables proper cascade/restrict behavior

### Important Additions

#### Timestamps
✅ Added `created_at`, `updated_at` to:
- User
- Warehouse
- InventoryItem

#### Business Fields
✅ **InventoryItem** enhancements:
- `unit_price` - For inventory valuation
- `reorder_level` - For low stock alerts
- `expiration_date` - For stretch goal (item expiration tracking)
- `description` - For detailed product info

✅ **Warehouse** enhancements:
- `is_active` - Soft delete/disable warehouses

✅ **Stock_Activity** enhancements:
- `previous_quantity`, `new_quantity` - Better audit trail
- `notes` - Additional context for activities

✅ **User** enhancements:
- `created_at`, `updated_at` - Account tracking

---

## Implementation Notes

### PostgreSQL Specific Features

1. **ENUM Types** - Create custom types:
```sql
CREATE TYPE activity_type AS ENUM ('RECEIVE', 'TRANSFER', 'SALE', 'ADJUSTMENT', 'DELETE');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE');
```

2. **JSON Support** - Use JSONB for Audit.changes:
```sql
ALTER TABLE audit ALTER COLUMN changes TYPE JSONB USING changes::JSONB;
CREATE INDEX idx_audit_changes ON audit USING GIN (changes);
```

3. **Check Constraints**:
```sql
ALTER TABLE warehouse ADD CONSTRAINT chk_capacity
  CHECK (current_capacity <= max_capacity);

ALTER TABLE warehouse ADD CONSTRAINT chk_threshold
  CHECK (capacity_alert_threshold BETWEEN 0 AND 100);
```

### Spring Boot JPA Entities

1. Use `@Entity`, `@Table`, `@Column` annotations
2. Implement `@CreatedDate`, `@LastModifiedDate` with JPA Auditing
3. Use `@Enumerated(EnumType.STRING)` for ENUM fields
4. Implement soft deletes with `@Where(clause = "is_active = true")`

---

## Testing Considerations

### Data Integrity Tests
- Verify FK constraints prevent invalid references
- Test cascade deletes work correctly
- Verify warehouse capacity calculations
- Test duplicate SKU prevention

### Business Logic Tests
- Warehouse overcapacity prevention
- Transfer validation (source ≠ destination)
- Low stock alerts trigger correctly
- Expiration date warnings work

### Performance Tests
- Index effectiveness on large datasets
- Query performance for dashboard metrics
- Trigger performance impact
- JSON query performance (Audit table)

---

## Future Enhancements (Stretch Goals)

### Additional Tables to Consider

1. **Category** - Separate table for inventory categories
2. **Supplier** - Track item suppliers/vendors
3. **PurchaseOrder** - Track incoming inventory orders
4. **StockAlert** - Persistent alerts table
5. **WarehouseZone** - Sub-locations within warehouses
6. **InventoryBatch** - Track inventory by batch/lot number

### Additional Fields

1. **InventoryItem**:
   - `image_url` - Product images
   - `weight`, `dimensions` - For shipping calculations
   - `min_stock_level`, `max_stock_level` - Stock range management

2. **Warehouse**:
   - `manager_id` - FK to User (warehouse manager)
   - `operating_hours` - Business hours
   - `timezone` - Warehouse timezone

---

## Summary

This updated schema addresses all critical issues identified in the original ERD and adds essential fields for a production-ready inventory management system. The schema now supports:

✅ Complete audit trail with referential integrity
✅ Proper timestamp tracking across all entities
✅ Business-critical fields (pricing, reorder levels, expiration)
✅ Flexible user/role management
✅ Warehouse capacity tracking and alerts
✅ Comprehensive stock activity logging

The schema is ready for implementation with Spring Boot and PostgreSQL.
