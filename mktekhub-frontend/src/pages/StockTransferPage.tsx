import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { inventoryService } from "../services/inventoryService";
import { warehouseService } from "../services/warehouseService";
import type { StockTransferRequest } from "../types";
import api from "../services/api";

export const StockTransferPage = () => {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<StockTransferRequest>({
    itemSku: "",
    sourceWarehouseId: 0,
    destinationWarehouseId: 0,
    quantity: 0,
    notes: "",
  });
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const { data: warehouses } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const { data: inventoryItems } = useQuery({
    queryKey: ["inventory"],
    queryFn: () => inventoryService.getAll(),
  });

  const transferMutation = useMutation({
    mutationFn: async (data: StockTransferRequest) => {
      const response = await api.post("/stock-transfer", data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      setSuccessMessage("Stock transfer completed successfully!");
      setErrorMessage("");
      setFormData({
        itemSku: "",
        sourceWarehouseId: 0,
        destinationWarehouseId: 0,
        quantity: 0,
        notes: "",
      });
      setTimeout(() => setSuccessMessage(""), 5000);
    },
    onError: (error: unknown) => {
      if (error && typeof error === "object" && "response" in error) {
        const err = error as {
          response?: { data?: { message?: string } };
        };
        setErrorMessage(
          err.response?.data?.message || "Transfer failed. Please try again.",
        );
      } else {
        setErrorMessage("Transfer failed. Please try again.");
      }
      setSuccessMessage("");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    if (formData.sourceWarehouseId === formData.destinationWarehouseId) {
      setErrorMessage("Source and destination warehouses must be different");
      return;
    }

    transferMutation.mutate(formData);
  };

  const getWarehouseName = (id: number) => {
    return warehouses?.find((w) => w.id === id)?.name || "Unknown";
  };

  const getItemInfo = (sku: string) => {
    const item = inventoryItems?.find((i) => i.sku === sku);
    if (!item) return null;
    return {
      name: item.name,
      quantity: item.quantity,
      warehouse: item.warehouseName,
    };
  };

  return (
    <div className="p-8">
      <h1 className="mb-8 text-3xl font-bold text-gray-900">Stock Transfer</h1>

      {/* Transfer Form */}
      <div className="mb-8 rounded-lg bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-semibold text-gray-900">
          New Transfer
        </h2>

        {successMessage && (
          <div className="mb-4 rounded-md bg-green-50 p-4">
            <p className="text-sm text-green-800">{successMessage}</p>
          </div>
        )}

        {errorMessage && (
          <div className="mb-4 rounded-md bg-red-50 p-4">
            <p className="text-sm text-red-800">{errorMessage}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Item SKU
              </label>
              <select
                required
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={formData.itemSku}
                onChange={(e) => {
                  setFormData({ ...formData, itemSku: e.target.value });
                }}
              >
                <option value="">Select an item</option>
                {inventoryItems?.map((item) => (
                  <option key={item.id} value={item.sku}>
                    {item.sku} - {item.name} (Qty: {item.quantity})
                  </option>
                ))}
              </select>
              {formData.itemSku && getItemInfo(formData.itemSku) && (
                <p className="mt-1 text-xs text-gray-500">
                  Current: {getItemInfo(formData.itemSku)?.quantity} units in{" "}
                  {getItemInfo(formData.itemSku)?.warehouse}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Quantity
              </label>
              <input
                type="number"
                required
                min="1"
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={formData.quantity}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    quantity: parseInt(e.target.value),
                  })
                }
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                From Warehouse
              </label>
              <select
                required
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={formData.sourceWarehouseId}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    sourceWarehouseId: parseInt(e.target.value),
                  })
                }
              >
                <option value="0">Select source warehouse</option>
                {warehouses?.map((warehouse) => (
                  <option key={warehouse.id} value={warehouse.id}>
                    {warehouse.name} - {warehouse.location}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                To Warehouse
              </label>
              <select
                required
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={formData.destinationWarehouseId}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    destinationWarehouseId: parseInt(e.target.value),
                  })
                }
              >
                <option value="0">Select destination warehouse</option>
                {warehouses?.map((warehouse) => (
                  <option key={warehouse.id} value={warehouse.id}>
                    {warehouse.name} - {warehouse.location}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Notes (Optional)
            </label>
            <textarea
              rows={3}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              value={formData.notes}
              onChange={(e) =>
                setFormData({ ...formData, notes: e.target.value })
              }
              placeholder="Add any notes about this transfer..."
            />
          </div>

          {formData.sourceWarehouseId > 0 &&
            formData.destinationWarehouseId > 0 && (
              <div className="rounded-md bg-blue-50 p-4">
                <p className="text-sm text-blue-800">
                  Transferring {formData.quantity} units from{" "}
                  <span className="font-medium">
                    {getWarehouseName(formData.sourceWarehouseId)}
                  </span>{" "}
                  to{" "}
                  <span className="font-medium">
                    {getWarehouseName(formData.destinationWarehouseId)}
                  </span>
                </p>
              </div>
            )}

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={transferMutation.isPending}
              className="rounded-md bg-blue-600 px-6 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-gray-400"
            >
              {transferMutation.isPending ? "Processing..." : "Transfer Stock"}
            </button>
          </div>
        </form>
      </div>

      {/* Transfer Guidelines */}
      <div className="rounded-lg bg-gray-50 p-6">
        <h3 className="mb-3 text-lg font-semibold text-gray-900">
          Transfer Guidelines
        </h3>
        <ul className="space-y-2 text-sm text-gray-700">
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            Ensure the item exists in the source warehouse with sufficient
            quantity
          </li>
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            The destination warehouse must have available capacity
          </li>
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            All transfers are logged in the stock activity history
          </li>
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            Source and destination warehouses cannot be the same
          </li>
        </ul>
      </div>
    </div>
  );
};
