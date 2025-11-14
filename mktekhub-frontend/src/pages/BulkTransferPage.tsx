import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useToast } from "../contexts/ToastContext";
import { inventoryService } from "../services/inventoryService";
import { warehouseService } from "../services/warehouseService";
import api from "../services/api";
import type {
  StockTransferRequest,
  BulkStockTransferRequest,
  BulkStockTransferResponse,
  InventoryItem,
  Warehouse,
} from "../types";

interface TransferRow extends StockTransferRequest {
  id: string;
  itemName?: string;
  sourceWarehouseName?: string;
  destinationWarehouseName?: string;
  maxAvailable?: number;
}

export const BulkTransferPage = () => {
  const queryClient = useQueryClient();
  const toast = useToast();
  const [transfers, setTransfers] = useState<TransferRow[]>([]);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const { data: inventoryItems } = useQuery<InventoryItem[]>({
    queryKey: ["inventory"],
    queryFn: () => inventoryService.getAll(),
  });

  const { data: warehouses } = useQuery<Warehouse[]>({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const bulkTransferMutation = useMutation({
    mutationFn: async (data: BulkStockTransferRequest) => {
      const response = await api.post<BulkStockTransferResponse>(
        "/stock-transfer/bulk",
        data,
      );
      return response.data;
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      queryClient.invalidateQueries({ queryKey: ["stock-activity"] });

      if (data.failedTransfers === 0) {
        toast.success(
          `All ${data.successfulTransfers} transfers completed successfully!`,
        );
        setTransfers([]);
      } else {
        toast.warning(
          `${data.successfulTransfers} succeeded, ${data.failedTransfers} failed`,
        );
      }
      setShowConfirmation(false);
    },
    onError: (error: unknown) => {
      const message =
        (error as { response?: { data?: { message?: string } } })?.response
          ?.data?.message || "Failed to process bulk transfer";
      toast.error(message);
    },
  });

  const addTransferRow = () => {
    const newRow: TransferRow = {
      id: `transfer-${Date.now()}-${Math.random()}`,
      itemSku: "",
      sourceWarehouseId: 0,
      destinationWarehouseId: 0,
      quantity: 0,
      notes: "",
    };
    setTransfers([...transfers, newRow]);
  };

  const removeTransferRow = (id: string) => {
    setTransfers(transfers.filter((t) => t.id !== id));
  };

  const updateTransferRow = (id: string, updates: Partial<TransferRow>) => {
    setTransfers(
      transfers.map((t) => {
        if (t.id === id) {
          const updated = { ...t, ...updates };

          // Auto-populate item name and max available
          if (updates.itemSku && inventoryItems && warehouses) {
            const item = inventoryItems.find(
              (i) =>
                i.sku === updates.itemSku &&
                i.warehouseId === updated.sourceWarehouseId,
            );
            if (item) {
              updated.itemName = item.name;
              updated.maxAvailable = item.quantity;
            }
          }

          // Auto-populate warehouse names
          if (updates.sourceWarehouseId && warehouses) {
            const warehouse = warehouses.find(
              (w) => w.id === updates.sourceWarehouseId,
            );
            updated.sourceWarehouseName = warehouse?.name;
          }
          if (updates.destinationWarehouseId && warehouses) {
            const warehouse = warehouses.find(
              (w) => w.id === updates.destinationWarehouseId,
            );
            updated.destinationWarehouseName = warehouse?.name;
          }

          return updated;
        }
        return t;
      }),
    );
  };

  const validateTransfers = (): string[] => {
    const errors: string[] = [];

    if (transfers.length === 0) {
      errors.push("Add at least one transfer");
      return errors;
    }

    transfers.forEach((transfer, index) => {
      if (!transfer.itemSku) {
        errors.push(`Row ${index + 1}: Select an item`);
      }
      if (transfer.sourceWarehouseId === 0) {
        errors.push(`Row ${index + 1}: Select source warehouse`);
      }
      if (transfer.destinationWarehouseId === 0) {
        errors.push(`Row ${index + 1}: Select destination warehouse`);
      }
      if (transfer.sourceWarehouseId === transfer.destinationWarehouseId) {
        errors.push(
          `Row ${index + 1}: Source and destination must be different`,
        );
      }
      if (transfer.quantity <= 0) {
        errors.push(`Row ${index + 1}: Quantity must be greater than 0`);
      }
      if (transfer.maxAvailable && transfer.quantity > transfer.maxAvailable) {
        errors.push(
          `Row ${index + 1}: Quantity exceeds available stock (${transfer.maxAvailable})`,
        );
      }
    });

    return errors;
  };

  const handlePreview = () => {
    const errors = validateTransfers();
    if (errors.length > 0) {
      errors.forEach((error) => toast.error(error));
      return;
    }
    setShowConfirmation(true);
  };

  const handleConfirm = () => {
    const request: BulkStockTransferRequest = {
      transfers: transfers.map((t) => ({
        itemSku: t.itemSku,
        sourceWarehouseId: t.sourceWarehouseId,
        destinationWarehouseId: t.destinationWarehouseId,
        quantity: t.quantity,
        notes: t.notes,
      })),
    };
    bulkTransferMutation.mutate(request);
  };

  const getUniqueItemsInWarehouse = (warehouseId: number) => {
    if (!inventoryItems || warehouseId === 0) return [];
    return inventoryItems.filter((item) => item.warehouseId === warehouseId);
  };

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Bulk Stock Transfer
          </h1>
          <p className="mt-2 text-sm text-gray-600">
            Transfer multiple items in a single operation
          </p>
        </div>
        <button
          onClick={addTransferRow}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          + Add Transfer
        </button>
      </div>

      {/* Transfers Table */}
      {transfers.length === 0 ? (
        <div className="rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 p-12 text-center">
          <p className="text-gray-600">No transfers added yet</p>
          <p className="mt-1 text-sm text-gray-500">
            Click "Add Transfer" to get started
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-lg bg-white shadow">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Item (SKU)
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  From
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  To
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Quantity
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Notes
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {transfers.map((transfer) => (
                <tr key={transfer.id}>
                  <td className="px-4 py-4">
                    <select
                      value={transfer.itemSku}
                      onChange={(e) =>
                        updateTransferRow(transfer.id, {
                          itemSku: e.target.value,
                        })
                      }
                      disabled={transfer.sourceWarehouseId === 0}
                      className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none disabled:bg-gray-100"
                    >
                      <option value="">Select source first</option>
                      {getUniqueItemsInWarehouse(
                        transfer.sourceWarehouseId,
                      ).map((item) => (
                        <option key={item.sku} value={item.sku}>
                          {item.name} ({item.sku}) - Qty: {item.quantity}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="px-4 py-4">
                    <select
                      value={transfer.sourceWarehouseId}
                      onChange={(e) =>
                        updateTransferRow(transfer.id, {
                          sourceWarehouseId: parseInt(e.target.value),
                          itemSku: "", // Reset item when warehouse changes
                        })
                      }
                      className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                    >
                      <option value="0">Select warehouse</option>
                      {warehouses?.map((warehouse) => (
                        <option key={warehouse.id} value={warehouse.id}>
                          {warehouse.name}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="px-4 py-4">
                    <select
                      value={transfer.destinationWarehouseId}
                      onChange={(e) =>
                        updateTransferRow(transfer.id, {
                          destinationWarehouseId: parseInt(e.target.value),
                        })
                      }
                      className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                    >
                      <option value="0">Select warehouse</option>
                      {warehouses
                        ?.filter((w) => w.id !== transfer.sourceWarehouseId)
                        .map((warehouse) => (
                          <option key={warehouse.id} value={warehouse.id}>
                            {warehouse.name}
                          </option>
                        ))}
                    </select>
                  </td>
                  <td className="px-4 py-4">
                    <input
                      type="number"
                      min="1"
                      max={transfer.maxAvailable || undefined}
                      value={transfer.quantity || ""}
                      onChange={(e) =>
                        updateTransferRow(transfer.id, {
                          quantity: parseInt(e.target.value) || 0,
                        })
                      }
                      className="block w-24 rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      placeholder="0"
                    />
                    {transfer.maxAvailable && (
                      <p className="mt-1 text-xs text-gray-500">
                        Max: {transfer.maxAvailable}
                      </p>
                    )}
                  </td>
                  <td className="px-4 py-4">
                    <input
                      type="text"
                      value={transfer.notes || ""}
                      onChange={(e) =>
                        updateTransferRow(transfer.id, {
                          notes: e.target.value,
                        })
                      }
                      className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      placeholder="Optional"
                    />
                  </td>
                  <td className="px-4 py-4">
                    <button
                      onClick={() => removeTransferRow(transfer.id)}
                      className="text-red-600 hover:text-red-800"
                      title="Remove"
                    >
                      <svg
                        className="h-5 w-5"
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
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Action Buttons */}
      {transfers.length > 0 && (
        <div className="mt-6 flex justify-end space-x-3">
          <button
            onClick={() => setTransfers([])}
            className="rounded-md bg-gray-300 px-6 py-2 text-sm font-medium text-gray-700 hover:bg-gray-400"
          >
            Clear All
          </button>
          <button
            onClick={handlePreview}
            className="rounded-md bg-blue-600 px-6 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            Review & Confirm ({transfers.length} transfer
            {transfers.length > 1 ? "s" : ""})
          </button>
        </div>
      )}

      {/* Confirmation Dialog */}
      {showConfirmation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/25">
          <div className="relative max-h-[80vh] w-full max-w-3xl overflow-y-auto rounded-lg bg-white p-6 shadow-2xl border border-gray-200">
            <h2 className="mb-4 text-2xl font-bold text-gray-900">
              Confirm Bulk Transfer
            </h2>
            <p className="mb-4 text-sm text-gray-600">
              Review the following {transfers.length} transfer
              {transfers.length > 1 ? "s" : ""} before proceeding:
            </p>

            {/* Transfer Summary */}
            <div className="mb-6 space-y-3">
              {transfers.map((transfer, index) => {
                const item = inventoryItems?.find(
                  (i) =>
                    i.sku === transfer.itemSku &&
                    i.warehouseId === transfer.sourceWarehouseId,
                );
                const isExpired =
                  item?.expirationDate &&
                  new Date(item.expirationDate) < new Date();

                return (
                  <div
                    key={transfer.id}
                    className={`rounded-lg border p-4 ${
                      isExpired
                        ? "border-red-300 bg-red-50"
                        : "border-gray-200 bg-gray-50"
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-semibold text-gray-900">
                          {index + 1}. {transfer.itemName || transfer.itemSku}
                          {isExpired && (
                            <span className="ml-2 rounded-full bg-red-600 px-2 py-0.5 text-xs text-white">
                              EXPIRED
                            </span>
                          )}
                        </p>
                        <p className="mt-1 text-sm text-gray-600">
                          <span className="font-medium">
                            {transfer.quantity} units
                          </span>{" "}
                          from{" "}
                          <span className="font-medium">
                            {transfer.sourceWarehouseName}
                          </span>{" "}
                          to{" "}
                          <span className="font-medium">
                            {transfer.destinationWarehouseName}
                          </span>
                        </p>
                        {isExpired && (
                          <p className="mt-1 text-xs text-red-600">
                            ⚠ Expired on{" "}
                            {new Date(
                              item!.expirationDate!,
                            ).toLocaleDateString()}{" "}
                            - Consider disposing instead
                          </p>
                        )}
                        {transfer.notes && (
                          <p className="mt-1 text-sm italic text-gray-500">
                            Note: {transfer.notes}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Warning */}
            <div className="mb-6 rounded-lg bg-yellow-50 p-4">
              <div className="flex">
                <svg
                  className="h-5 w-5 text-yellow-400"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                    clipRule="evenodd"
                  />
                </svg>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-yellow-800">
                    Warning
                  </h3>
                  <p className="mt-1 text-sm text-yellow-700">
                    This action will transfer items across multiple warehouses.
                    Some transfers may fail if inventory or capacity constraints
                    are not met.
                  </p>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex space-x-3">
              <button
                onClick={() => setShowConfirmation(false)}
                className="flex-1 rounded-md bg-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-400"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirm}
                disabled={bulkTransferMutation.isPending}
                className="flex-1 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-gray-400"
              >
                {bulkTransferMutation.isPending
                  ? "Processing..."
                  : `Confirm ${transfers.length} Transfer${transfers.length > 1 ? "s" : ""}`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
