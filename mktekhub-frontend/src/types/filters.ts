/**
 * Filter types and constants for inventory filtering
 */

/**
 * Filter criteria for inventory items
 */
export interface InventoryFilterOptions {
  /** Filter by warehouse ID (0 = all warehouses) */
  warehouseId: number;
  /** Filter by category (empty = all categories) */
  category: string;
  /** Filter by brand (empty = all brands) */
  brand: string;
  /** Filter by stock status */
  stockStatus: "all" | "in-stock" | "low-stock" | "out-of-stock";
  /** Filter by expiration status */
  expirationStatus: "all" | "expired" | "expiring-soon" | "valid";
  /** Minimum quantity filter */
  minQuantity: string;
  /** Maximum quantity filter */
  maxQuantity: string;
  /** Minimum price filter */
  minPrice: string;
  /** Maximum price filter */
  maxPrice: string;
}

/**
 * Default filter values
 */
export const defaultFilters: InventoryFilterOptions = {
  warehouseId: 0,
  category: "",
  brand: "",
  stockStatus: "all",
  expirationStatus: "all",
  minQuantity: "",
  maxQuantity: "",
  minPrice: "",
  maxPrice: "",
};
