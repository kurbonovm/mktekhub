import { ExpirationBadge, WarrantyBadge, Tooltip } from "../common";
import type { InventoryItem, Warehouse } from "../../types";

interface InventoryTableProps {
  items: InventoryItem[];
  warehouses: Warehouse[];
  isAdminOrManager: boolean;
  onAdjust: (item: InventoryItem) => void;
  onEdit: (item: InventoryItem) => void;
  onDelete: (id: number) => void;
  onResetFilters: () => void;
  hasActiveFilters: boolean;
}

export const InventoryTable = ({
  items,
  warehouses,
  isAdminOrManager,
  onAdjust,
  onEdit,
  onDelete,
  onResetFilters,
  hasActiveFilters,
}: InventoryTableProps) => {
  return (
    <div className="hidden overflow-x-auto rounded-lg bg-white shadow md:block">
      <div className="max-h-[calc(100vh-300px)] overflow-y-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="sticky top-0 z-10 bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                SKU
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Name
              </th>
              <th className="hidden px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:table-cell">
                Category
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Warehouse
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Quantity
              </th>
              <th className="hidden px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:table-cell">
                Price
              </th>
              <th className="hidden px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:table-cell">
                Volume
              </th>
              <th className="hidden px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 xl:table-cell">
                Total Value
              </th>
              <th className="hidden px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 xl:table-cell">
                Expiration
              </th>
              <th className="hidden px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 xl:table-cell">
                Warranty
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {items.length === 0 ? (
              <tr>
                <td colSpan={11} className="px-6 py-12 text-center">
                  <div className="flex flex-col items-center justify-center">
                    <svg
                      className="h-12 w-12 text-gray-400"
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
                </td>
              </tr>
            ) : (
              items.map((item) => (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                    {item.sku}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium text-gray-900">
                      {item.name}
                    </div>
                    <div className="text-sm text-gray-500">
                      {item.description}
                    </div>
                  </td>
                  <td className="hidden whitespace-nowrap px-6 py-4 text-sm text-gray-500 lg:table-cell">
                    {item.category || "N/A"}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                    {item.warehouseName}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4">
                    <div className="text-sm text-gray-900">{item.quantity}</div>
                    {item.isLowStock && (
                      <span className="inline-flex rounded-full bg-red-100 px-2 text-xs font-semibold leading-5 text-red-800">
                        Low Stock
                      </span>
                    )}
                  </td>
                  <td className="hidden whitespace-nowrap px-6 py-4 text-sm text-gray-900 lg:table-cell">
                    ${item.unitPrice?.toFixed(2) || "0.00"}
                  </td>
                  <td className="hidden whitespace-nowrap px-6 py-4 text-sm text-gray-500 lg:table-cell">
                    {item.totalVolume?.toFixed(2) || "0.00"} ft³
                  </td>
                  <td className="hidden whitespace-nowrap px-6 py-4 text-sm text-gray-900 xl:table-cell">
                    ${item.totalValue?.toFixed(2) || "0.00"}
                  </td>
                  <td className="hidden px-6 py-4 xl:table-cell">
                    <ExpirationBadge
                      expirationDate={item.expirationDate}
                      isExpired={item.isExpired}
                    />
                  </td>
                  <td className="hidden px-6 py-4 xl:table-cell">
                    <WarrantyBadge
                      warrantyEndDate={item.warrantyEndDate}
                      isWarrantyValid={item.isWarrantyValid}
                    />
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm font-medium">
                    {isAdminOrManager ? (
                      <>
                        <Tooltip content="Adjust quantity" position="top">
                          <button
                            onClick={() => onAdjust(item)}
                            className="mr-2 text-green-600 hover:text-green-900"
                          >
                            Adjust
                          </button>
                        </Tooltip>
                        <Tooltip content="Edit item details" position="top">
                          <button
                            onClick={() => onEdit(item)}
                            className="mr-2 text-blue-600 hover:text-blue-900"
                          >
                            Edit
                          </button>
                        </Tooltip>
                        <Tooltip content="Delete this item" position="top">
                          <button
                            onClick={() => onDelete(item.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Delete
                          </button>
                        </Tooltip>
                      </>
                    ) : (
                      <span className="text-gray-400">View Only</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
