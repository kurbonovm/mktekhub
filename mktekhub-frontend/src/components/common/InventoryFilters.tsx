import React from "react";
import type { Warehouse } from "../../types";
import type { InventoryFilterOptions } from "../../types/filters";

interface InventoryFiltersProps {
  /** Current filter values */
  filters: InventoryFilterOptions;
  /** Callback when filters change */
  onFilterChange: (filters: InventoryFilterOptions) => void;
  /** Callback to reset filters */
  onReset: () => void;
  /** Available warehouses for filtering */
  warehouses: Warehouse[];
  /** Available categories (extracted from items) */
  categories: string[];
  /** Available brands (extracted from items) */
  brands: string[];
  /** Whether filters are expanded */
  isExpanded: boolean;
  /** Callback to toggle filter panel */
  onToggle: () => void;
}

/**
 * InventoryFilters component for advanced filtering of inventory items
 *
 * @component
 * @example
 * ```tsx
 * <InventoryFilters
 *   filters={filters}
 *   onFilterChange={setFilters}
 *   onReset={handleResetFilters}
 *   warehouses={warehouses}
 *   categories={categories}
 *   brands={brands}
 *   isExpanded={showFilters}
 *   onToggle={() => setShowFilters(!showFilters)}
 * />
 * ```
 */
export const InventoryFilters: React.FC<InventoryFiltersProps> = ({
  filters,
  onFilterChange,
  onReset,
  warehouses,
  categories,
  brands,
  isExpanded,
  onToggle,
}) => {
  const handleChange = (
    key: keyof InventoryFilterOptions,
    value: string | number,
  ) => {
    onFilterChange({
      ...filters,
      [key]: value,
    });
  };

  const activeFiltersCount = Object.entries(filters).filter(([key, value]) => {
    if (key === "warehouseId") return value !== 0;
    if (typeof value === "string") return value !== "";
    return value !== "all";
  }).length;

  return (
    <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
      {/* Filter Header */}
      <div className="flex items-center justify-between border-b border-gray-200 p-4">
        <div className="flex items-center space-x-2">
          <svg
            className="h-5 w-5 text-gray-500"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
            />
          </svg>
          <h3 className="text-lg font-semibold text-gray-900">Filters</h3>
          {activeFiltersCount > 0 && (
            <span className="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800">
              {activeFiltersCount} active
            </span>
          )}
        </div>
        <div className="flex items-center space-x-2">
          {activeFiltersCount > 0 && (
            <button
              type="button"
              onClick={onReset}
              className="text-sm text-blue-600 hover:text-blue-800"
            >
              Clear all
            </button>
          )}
          <button
            type="button"
            onClick={onToggle}
            className="text-gray-500 hover:text-gray-700"
            aria-label={isExpanded ? "Collapse filters" : "Expand filters"}
          >
            <svg
              className={`h-5 w-5 transition-transform ${isExpanded ? "rotate-180" : ""}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
        </div>
      </div>

      {/* Filter Content */}
      {isExpanded && (
        <div className="p-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
            {/* Warehouse Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Warehouse
              </label>
              <select
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={filters.warehouseId}
                onChange={(e) =>
                  handleChange("warehouseId", parseInt(e.target.value))
                }
              >
                <option value={0}>All Warehouses</option>
                {warehouses.map((warehouse) => (
                  <option key={warehouse.id} value={warehouse.id}>
                    {warehouse.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Category Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Category
              </label>
              <select
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={filters.category}
                onChange={(e) => handleChange("category", e.target.value)}
              >
                <option value="">All Categories</option>
                {categories.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </div>

            {/* Brand Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Brand
              </label>
              <select
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={filters.brand}
                onChange={(e) => handleChange("brand", e.target.value)}
              >
                <option value="">All Brands</option>
                {brands.map((brand) => (
                  <option key={brand} value={brand}>
                    {brand}
                  </option>
                ))}
              </select>
            </div>

            {/* Stock Status Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Stock Status
              </label>
              <select
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={filters.stockStatus}
                onChange={(e) =>
                  handleChange(
                    "stockStatus",
                    e.target.value as InventoryFilterOptions["stockStatus"],
                  )
                }
              >
                <option value="all">All Items</option>
                <option value="in-stock">In Stock</option>
                <option value="low-stock">Low Stock</option>
                <option value="out-of-stock">Out of Stock</option>
              </select>
            </div>
          </div>

          {/* Advanced Filters - Second Row */}
          <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
            {/* Quantity Range */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Min Quantity
              </label>
              <input
                type="number"
                min="0"
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="Min"
                value={filters.minQuantity}
                onChange={(e) => handleChange("minQuantity", e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Max Quantity
              </label>
              <input
                type="number"
                min="0"
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="Max"
                value={filters.maxQuantity}
                onChange={(e) => handleChange("maxQuantity", e.target.value)}
              />
            </div>

            {/* Price Range */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Min Price ($)
              </label>
              <input
                type="number"
                min="0"
                step="0.01"
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="Min"
                value={filters.minPrice}
                onChange={(e) => handleChange("minPrice", e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Max Price ($)
              </label>
              <input
                type="number"
                min="0"
                step="0.01"
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="Max"
                value={filters.maxPrice}
                onChange={(e) => handleChange("maxPrice", e.target.value)}
              />
            </div>
          </div>

          {/* Expiration Status Filter */}
          <div className="mt-4">
            <label className="block text-sm font-medium text-gray-700">
              Expiration Status
            </label>
            <select
              className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 md:w-1/2 lg:w-1/4"
              value={filters.expirationStatus}
              onChange={(e) =>
                handleChange(
                  "expirationStatus",
                  e.target.value as InventoryFilterOptions["expirationStatus"],
                )
              }
            >
              <option value="all">All Items</option>
              <option value="expired">Expired</option>
              <option value="expiring-soon">Expiring Soon (30 days)</option>
              <option value="valid">Valid / No Expiration</option>
            </select>
          </div>
        </div>
      )}
    </div>
  );
};
