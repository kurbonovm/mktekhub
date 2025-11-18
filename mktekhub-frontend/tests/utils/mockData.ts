import {
  InventoryItem,
  Warehouse,
  StockActivity,
  DashboardSummary,
  AuthResponse,
} from "@/types";

// Mock Users (using AuthResponse type which has all fields needed by Navbar)
export const mockUsers = {
  admin: {
    id: 1,
    username: "admin",
    email: "admin@mktekhub.com",
    firstName: "Admin",
    lastName: "User",
    roles: ["ADMIN"],
    token: "mock-admin-token",
  },
  manager: {
    id: 2,
    username: "manager",
    email: "manager@mktekhub.com",
    firstName: "Manager",
    lastName: "User",
    roles: ["MANAGER"],
    token: "mock-manager-token",
  },
  employee: {
    id: 3,
    username: "employee",
    email: "employee@mktekhub.com",
    firstName: "Employee",
    lastName: "User",
    roles: ["EMPLOYEE"],
    token: "mock-employee-token",
  },
};

// Mock Warehouses
export const mockWarehouses: Warehouse[] = [
  {
    id: 1,
    name: "Main Warehouse",
    location: "123 Main St, New York, NY",
    capacity: 10000,
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
  },
  {
    id: 2,
    name: "Secondary Warehouse",
    location: "456 Second Ave, Los Angeles, CA",
    capacity: 5000,
    createdAt: "2024-01-02T00:00:00Z",
    updatedAt: "2024-01-02T00:00:00Z",
  },
];

// Mock Inventory Items
export const mockInventoryItems: InventoryItem[] = [
  {
    id: 1,
    name: "Laptop Dell XPS 15",
    description: "High-performance laptop",
    sku: "LAP-DELL-001",
    category: "Electronics",
    brand: "Dell",
    quantity: 50,
    unitPrice: 1299.99,
    warehouseId: 1,
    warehouseName: "Main Warehouse",
    expirationDate: "2025-12-31",
    warrantyEndDate: "2026-12-31",
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-15T00:00:00Z",
  },
  {
    id: 2,
    name: "Office Chair Premium",
    description: "Ergonomic office chair",
    sku: "FUR-CHAIR-001",
    category: "Furniture",
    brand: "ErgoMax",
    quantity: 5,
    unitPrice: 299.99,
    warehouseId: 1,
    warehouseName: "Main Warehouse",
    createdAt: "2024-01-02T00:00:00Z",
    updatedAt: "2024-01-02T00:00:00Z",
  },
  {
    id: 3,
    name: "Expired Medicine",
    description: "Medical supplies - expired",
    sku: "MED-SUPPLY-001",
    category: "Medical",
    brand: "PharmaCorp",
    quantity: 10,
    unitPrice: 49.99,
    warehouseId: 2,
    warehouseName: "Secondary Warehouse",
    expirationDate: "2024-01-01",
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
  },
  {
    id: 4,
    name: "Expiring Soon Item",
    description: "Item expiring within 30 days",
    sku: "FOOD-ITEM-001",
    category: "Food",
    brand: "FreshCo",
    quantity: 100,
    unitPrice: 9.99,
    warehouseId: 1,
    warehouseName: "Main Warehouse",
    expirationDate: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split("T")[0], // 15 days from now
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
  },
];

// Mock Stock Activities
export const mockStockActivities: StockActivity[] = [
  {
    id: 1,
    itemId: 1,
    itemName: "Laptop Dell XPS 15",
    itemSku: "LAP-DELL-001",
    activityType: "RECEIVE",
    quantityChange: 50,
    warehouseId: 1,
    warehouseName: "Main Warehouse",
    performedBy: "admin",
    notes: "Initial stock",
    timestamp: "2024-01-01T10:00:00Z",
  },
  {
    id: 2,
    itemId: 1,
    itemName: "Laptop Dell XPS 15",
    itemSku: "LAP-DELL-001",
    activityType: "SALE",
    quantityChange: -10,
    warehouseId: 1,
    warehouseName: "Main Warehouse",
    performedBy: "manager",
    notes: "Sold to customer",
    timestamp: "2024-01-05T14:30:00Z",
  },
];

// Mock Dashboard Summary
export const mockDashboardSummary: DashboardSummary = {
  warehouseSummary: {
    totalWarehouses: 5,
    activeWarehouses: 3,
    totalCapacity: 10000,
    usedCapacity: 6500,
    averageUtilization: 65.0,
    warehousesWithAlerts: 1,
  },
  inventorySummary: {
    totalItems: 150,
    totalQuantity: 5000,
    totalValue: 45000.0,
    uniqueSkus: 120,
    categoriesCount: 8,
  },
  alertsSummary: {
    lowStockItems: 12,
    expiredItems: 3,
    expiringSoonItems: 7,
    capacityAlerts: 2,
  },
};

// Helper functions to create custom mock data
export const createMockInventoryItem = (
  overrides?: Partial<InventoryItem>,
): InventoryItem => ({
  id: 999,
  name: "Test Item",
  description: "Test Description",
  sku: "TEST-SKU-001",
  category: "Test Category",
  brand: "Test Brand",
  quantity: 100,
  unitPrice: 99.99,
  warehouseId: 1,
  warehouseName: "Main Warehouse",
  createdAt: "2024-01-01T00:00:00Z",
  updatedAt: "2024-01-01T00:00:00Z",
  ...overrides,
});

export const createMockWarehouse = (
  overrides?: Partial<Warehouse>,
): Warehouse => ({
  id: 999,
  name: "Test Warehouse",
  location: "Test Location",
  capacity: 1000,
  createdAt: "2024-01-01T00:00:00Z",
  updatedAt: "2024-01-01T00:00:00Z",
  ...overrides,
});

export const createMockUser = (
  overrides?: Partial<AuthResponse>,
): AuthResponse => ({
  id: 999,
  username: "testuser",
  email: "test@example.com",
  firstName: "Test",
  lastName: "User",
  roles: ["EMPLOYEE"],
  token: "mock-token",
  ...overrides,
});
