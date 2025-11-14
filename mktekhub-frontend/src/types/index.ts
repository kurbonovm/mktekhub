// Auth Types
export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

// Warehouse Types
export interface Warehouse {
  id: number;
  name: string;
  location: string;
  maxCapacity: number; // Volume in cubic feet
  currentCapacity: number; // Volume in cubic feet
  capacityAlertThreshold: number;
  utilizationPercentage: number;
  isAlertTriggered: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface WarehouseRequest {
  name: string;
  location: string;
  maxCapacity: number; // Volume in cubic feet
  capacityAlertThreshold?: number;
}

// Inventory Types
export interface InventoryItem {
  id: number;
  sku: string;
  name: string;
  description?: string;
  category?: string;
  brand?: string;
  quantity: number;
  unitPrice?: number;
  volumePerUnit?: number; // Volume per unit in cubic feet
  totalVolume?: number; // Total volume in cubic feet
  reorderLevel?: number;
  warrantyEndDate?: string;
  expirationDate?: string;
  barcode?: string;
  warehouseId: number;
  warehouseName: string;
  totalValue?: number;
  isLowStock: boolean;
  isExpired: boolean;
  isWarrantyValid: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryItemRequest {
  sku: string;
  name: string;
  description?: string;
  category?: string;
  brand?: string;
  quantity: number;
  unitPrice?: number;
  volumePerUnit?: number; // Volume per unit in cubic feet
  reorderLevel?: number;
  warehouseId: number;
  warrantyEndDate?: string;
  expirationDate?: string;
  barcode?: string;
}

// Stock Transfer Types
export interface StockTransferRequest {
  itemSku: string;
  sourceWarehouseId: number;
  destinationWarehouseId: number;
  quantity: number;
  notes?: string;
}

export interface StockTransferResponse {
  activityId: number;
  itemSku: string;
  itemName: string;
  sourceWarehouseName: string;
  destinationWarehouseName: string;
  quantityTransferred: number;
  previousQuantity: number;
  newQuantity: number;
  timestamp: string;
  performedBy: string;
  notes: string;
}

export interface BulkStockTransferRequest {
  transfers: StockTransferRequest[];
}

export interface BulkStockTransferResponse {
  totalTransfers: number;
  successfulTransfers: number;
  failedTransfers: number;
  successResults: StockTransferResponse[];
  errors: TransferError[];
}

export interface TransferError {
  transferIndex: number;
  itemSku: string;
  errorMessage: string;
}

// Stock Activity Types
export type ActivityType =
  | "RECEIVE"
  | "TRANSFER"
  | "SALE"
  | "ADJUSTMENT"
  | "UPDATE"
  | "DELETE";

export interface StockActivity {
  id: number;
  itemSku: string;
  itemName: string;
  activityType: ActivityType;
  quantityChange: number;
  previousQuantity: number;
  newQuantity: number;
  timestamp: string;
  performedBy: string;
  sourceWarehouseName?: string;
  destinationWarehouseName?: string;
  notes: string;
}

// Dashboard Types
export interface DashboardSummary {
  warehouseSummary: WarehouseSummary;
  inventorySummary: InventorySummary;
  alertsSummary: AlertsSummary;
}

export interface WarehouseSummary {
  totalWarehouses: number;
  activeWarehouses: number;
  totalCapacity: number;
  usedCapacity: number;
  averageUtilization: number;
  warehousesWithAlerts: number;
}

export interface InventorySummary {
  totalItems: number;
  totalQuantity: number;
  totalValue: number;
  uniqueSkus: number;
  categoriesCount: number;
}

export interface AlertsSummary {
  lowStockItems: number;
  expiredItems: number;
  expiringSoonItems: number;
  capacityAlerts: number;
}

export interface CombinedAlerts {
  lowStockItems: InventoryItem[];
  expiredItems: InventoryItem[];
  expiringSoonItems: InventoryItem[];
  capacityAlerts: Warehouse[];
}

// Error Types
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
  errors?: string[];
}
