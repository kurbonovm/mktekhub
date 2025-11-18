import { ExpirationBadge, WarrantyBadge, StockStatusBadge } from "../common";
import type { InventoryItem } from "../../types";

interface InventoryMobileCardProps {
  items: InventoryItem[];
  isAdminOrManager: boolean;
  onAdjust: (item: InventoryItem) => void;
  onEdit: (item: InventoryItem) => void;
  onDelete: (id: number) => void;
  onResetFilters: () => void;
  hasActiveFilters: boolean;
}

export const InventoryMobileCard = ({
  items,
  isAdminOrManager,
  onAdjust,
  onEdit,
  onDelete,
  onResetFilters,
  hasActiveFilters,
}: InventoryMobileCardProps) => {
  return (
    <div className="block space-y-4 md:hidden">
      {items.length === 0 ? (
        <div className="rounded-lg bg-white p-12 text-center shadow">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
            />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            No items found
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            {hasActiveFilters
              ? "Try adjusting your search or filters"
              : "Get started by adding a new inventory item"}
          </p>
          {hasActiveFilters && (
            <button
              onClick={onResetFilters}
              className="mt-4 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        items.map((item) => (
          <div key={item.id} className="rounded-lg bg-white p-4 shadow">
            <div className="mb-3 flex items-start justify-between">
              <div>
                <h3 className="font-medium text-gray-900">{item.name}</h3>
                <p className="mt-1 text-sm text-gray-500">SKU: {item.sku}</p>
              </div>
              <StockStatusBadge
                quantity={item.quantity}
                reorderLevel={item.reorderLevel}
              />
            </div>

            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Category:</span>
                <span className="font-medium">{item.category || "N/A"}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Warehouse:</span>
                <span className="font-medium">{item.warehouseName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Quantity:</span>
                <span className="font-medium">{item.quantity}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Unit Price:</span>
                <span className="font-medium">
                  ${(item.unitPrice || 0).toFixed(2)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Volume per Unit:</span>
                <span className="font-medium">
                  {(item.volumePerUnit || 0).toFixed(2)} ft³
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Total Volume:</span>
                <span className="font-medium">
                  {(item.totalVolume || 0).toFixed(2)} ft³
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Total Value:</span>
                <span className="font-medium">
                  ${(item.quantity * (item.unitPrice || 0)).toFixed(2)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Expiration:</span>
                <ExpirationBadge
                  expirationDate={item.expirationDate}
                  isExpired={item.isExpired}
                />
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Warranty:</span>
                <WarrantyBadge
                  warrantyEndDate={item.warrantyEndDate}
                  isWarrantyValid={item.isWarrantyValid}
                />
              </div>
            </div>

            {isAdminOrManager && (
              <div className="mt-4 flex gap-2 border-t pt-3">
                <button
                  onClick={() => onAdjust(item)}
                  className="flex flex-1 items-center justify-center gap-2 rounded-md bg-green-600 px-3 py-2 text-sm font-medium text-white hover:bg-green-700 transition-colors"
                  aria-label="Adjust quantity"
                >
                  <svg
                    className="h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4"
                    />
                  </svg>
                  Adjust
                </button>
                <button
                  onClick={() => onEdit(item)}
                  className="flex flex-1 items-center justify-center gap-2 rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
                  aria-label="Edit item"
                >
                  <svg
                    className="h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                    />
                  </svg>
                  Edit
                </button>
                <button
                  onClick={() => onDelete(item.id)}
                  className="flex flex-1 items-center justify-center gap-2 rounded-md bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700 transition-colors"
                  aria-label="Delete item"
                >
                  <svg
                    className="h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                  Delete
                </button>
              </div>
            )}
          </div>
        ))
      )}
    </div>
  );
};
