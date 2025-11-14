import { useState, useMemo } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { inventoryService } from "../services/inventoryService";
import { warehouseService } from "../services/warehouseService";
import { Skeleton } from "../components/common";
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
  const [itemSearchQuery, setItemSearchQuery] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const { data: warehouses, isRefetching: isWarehousesRefetching } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const { data: inventoryItems, isRefetching: isInventoryRefetching } =
    useQuery({
      queryKey: ["inventory"],
      queryFn: () => inventoryService.getAll(),
    });

  // Group inventory items by SKU to show unique items with total quantities
  const uniqueItems = useMemo(() => {
    if (!inventoryItems) return [];

    const itemsMap = new Map<
      string,
      {
        sku: string;
        name: string;
        category: string;
        brand: string;
        totalQuantity: number;
        locations: Array<{
          warehouseId: number;
          warehouseName: string;
          quantity: number;
        }>;
      }
    >();

    inventoryItems.forEach((item) => {
      if (itemsMap.has(item.sku)) {
        const existing = itemsMap.get(item.sku)!;
        existing.totalQuantity += item.quantity;
        existing.locations.push({
          warehouseId: item.warehouseId,
          warehouseName: item.warehouseName,
          quantity: item.quantity,
        });
      } else {
        itemsMap.set(item.sku, {
          sku: item.sku,
          name: item.name,
          category: item.category || "",
          brand: item.brand || "",
          totalQuantity: item.quantity,
          locations: [
            {
              warehouseId: item.warehouseId,
              warehouseName: item.warehouseName,
              quantity: item.quantity,
            },
          ],
        });
      }
    });

    return Array.from(itemsMap.values());
  }, [inventoryItems]);

  // Filter items based on search query
  const filteredItems = useMemo(() => {
    if (!itemSearchQuery) return uniqueItems;
    const query = itemSearchQuery.toLowerCase();
    return uniqueItems.filter(
      (item) =>
        item.sku.toLowerCase().includes(query) ||
        item.name.toLowerCase().includes(query) ||
        item.category.toLowerCase().includes(query) ||
        item.brand.toLowerCase().includes(query),
    );
  }, [uniqueItems, itemSearchQuery]);

  // Get selected item details
  const selectedItem = useMemo(() => {
    return uniqueItems.find((item) => item.sku === formData.itemSku);
  }, [uniqueItems, formData.itemSku]);

  // Get available warehouses that have the selected item
  const availableSourceWarehouses = useMemo(() => {
    if (!selectedItem) return [];
    return selectedItem.locations;
  }, [selectedItem]);

  // Get max quantity available at source warehouse
  const maxAvailableQuantity = useMemo(() => {
    if (!formData.sourceWarehouseId || !selectedItem) return 0;
    const location = selectedItem.locations.find(
      (loc) => loc.warehouseId === formData.sourceWarehouseId,
    );
    return location?.quantity || 0;
  }, [formData.sourceWarehouseId, selectedItem]);

  // Check if selected item is expired
  const selectedItemDetails = useMemo(() => {
    if (!formData.itemSku || !formData.sourceWarehouseId || !inventoryItems)
      return null;
    return inventoryItems.find(
      (item) =>
        item.sku === formData.itemSku &&
        item.warehouseId === formData.sourceWarehouseId,
    );
  }, [formData.itemSku, formData.sourceWarehouseId, inventoryItems]);

  const isExpired = useMemo(() => {
    if (!selectedItemDetails?.expirationDate) return false;
    return new Date(selectedItemDetails.expirationDate) < new Date();
  }, [selectedItemDetails]);

  // Get filtered destination warehouses (exclude source)
  const availableDestinationWarehouses = useMemo(() => {
    if (!warehouses) return [];
    return warehouses.filter((w) => w.id !== formData.sourceWarehouseId);
  }, [warehouses, formData.sourceWarehouseId]);

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
      setItemSearchQuery("");
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

    if (formData.quantity > maxAvailableQuantity) {
      setErrorMessage(
        `Insufficient stock. Only ${maxAvailableQuantity} units available.`,
      );
      return;
    }

    transferMutation.mutate(formData);
  };

  const handleItemSelect = (sku: string) => {
    setFormData({
      itemSku: sku,
      sourceWarehouseId: 0,
      destinationWarehouseId: 0,
      quantity: 0,
      notes: "",
    });
    setItemSearchQuery("");
  };

  const handleSourceSelect = (warehouseId: number) => {
    setFormData({
      ...formData,
      sourceWarehouseId: warehouseId,
      destinationWarehouseId: 0,
    });
  };

  const getWarehouseName = (id: number) => {
    return warehouses?.find((w) => w.id === id)?.name || "Unknown";
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8">
      <h1 className="mb-6 text-2xl font-bold text-gray-900 sm:mb-8 sm:text-3xl">
        Stock Transfer
      </h1>

      {/* Transfer Form */}
      <div className="mb-8 rounded-lg bg-white p-4 shadow sm:p-6">
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

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Step 1: Item Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Step 1: Select Item to Transfer
            </label>
            {!formData.itemSku ? (
              <div className="mt-2 space-y-2">
                <input
                  type="text"
                  placeholder="Search by SKU, name, category, or brand..."
                  className="block w-full rounded-md border border-gray-300 px-3 py-3 text-base placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                  value={itemSearchQuery}
                  onChange={(e) => setItemSearchQuery(e.target.value)}
                />
                <div className="max-h-60 overflow-y-auto rounded-md border border-gray-300">
                  {isInventoryRefetching ? (
                    <div className="p-4 space-y-2">
                      <Skeleton variant="text" className="h-16" count={5} />
                    </div>
                  ) : filteredItems.length > 0 ? (
                    filteredItems.map((item) => (
                      <button
                        key={item.sku}
                        type="button"
                        onClick={() => handleItemSelect(item.sku)}
                        className="block w-full border-b border-gray-200 px-4 py-3 text-left hover:bg-blue-50 focus:bg-blue-50 focus:outline-none last:border-b-0"
                      >
                        <div className="font-medium text-gray-900">
                          {item.sku} - {item.name}
                        </div>
                        <div className="text-sm text-gray-500">
                          {item.category} • {item.brand} • Total:{" "}
                          {item.totalQuantity} units across{" "}
                          {item.locations.length} warehouse(s)
                        </div>
                      </button>
                    ))
                  ) : (
                    <div className="px-4 py-8 text-center text-sm text-gray-500">
                      {itemSearchQuery
                        ? "No items found matching your search"
                        : "Start typing to search for items"}
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="mt-2 flex items-center justify-between rounded-md border border-blue-200 bg-blue-50 p-4">
                <div>
                  <div className="font-medium text-gray-900">
                    {selectedItem?.sku} - {selectedItem?.name}
                  </div>
                  <div className="text-sm text-gray-600">
                    {selectedItem?.category} • {selectedItem?.brand} • Total:{" "}
                    {selectedItem?.totalQuantity} units
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setFormData({
                      itemSku: "",
                      sourceWarehouseId: 0,
                      destinationWarehouseId: 0,
                      quantity: 0,
                      notes: "",
                    })
                  }
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  Change
                </button>
              </div>
            )}
          </div>

          {/* Step 2: Stock Locations Table */}
          {formData.itemSku && selectedItem && (
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Step 2: Select Source Warehouse
              </label>
              <p className="mt-1 text-sm text-gray-500">
                Choose the warehouse to transfer from
              </p>
              <div className="mt-2 overflow-x-auto rounded-md border border-gray-300">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Warehouse
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Available Qty
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Action
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                    {availableSourceWarehouses.map((location) => (
                      <tr
                        key={location.warehouseId}
                        className={
                          formData.sourceWarehouseId === location.warehouseId
                            ? "bg-blue-50"
                            : ""
                        }
                      >
                        <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">
                          {location.warehouseName}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">
                          <span
                            className={`font-medium ${
                              location.quantity < 10
                                ? "text-red-600"
                                : location.quantity < 50
                                  ? "text-yellow-600"
                                  : "text-green-600"
                            }`}
                          >
                            {location.quantity} units
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-sm">
                          {formData.sourceWarehouseId ===
                          location.warehouseId ? (
                            <span className="text-blue-600">✓ Selected</span>
                          ) : (
                            <button
                              type="button"
                              onClick={() =>
                                handleSourceSelect(location.warehouseId)
                              }
                              className="text-blue-600 hover:text-blue-800"
                            >
                              Select
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Step 3: Destination & Quantity */}
          {formData.sourceWarehouseId > 0 && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Step 3: Select Destination Warehouse
                </label>
                {isWarehousesRefetching ? (
                  <div className="mt-1">
                    <Skeleton variant="text" className="h-12" />
                  </div>
                ) : (
                  <select
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                    value={formData.destinationWarehouseId}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        destinationWarehouseId: parseInt(e.target.value),
                      })
                    }
                  >
                    <option value="0">Select destination warehouse</option>
                    {availableDestinationWarehouses.map((warehouse) => (
                      <option key={warehouse.id} value={warehouse.id}>
                        {warehouse.name} - {warehouse.location}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Step 4: Enter Quantity
                </label>
                <div className="mt-1">
                  <input
                    type="number"
                    required
                    min="1"
                    max={maxAvailableQuantity}
                    className="block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                    value={formData.quantity || ""}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        quantity: parseInt(e.target.value) || 0,
                      })
                    }
                    placeholder="Enter quantity"
                  />
                  <p className="mt-1 text-sm text-gray-500">
                    Available: {maxAvailableQuantity} units at{" "}
                    {getWarehouseName(formData.sourceWarehouseId)}
                  </p>
                  {isExpired && (
                    <div className="mt-2 rounded-md bg-red-50 p-3">
                      <div className="flex">
                        <svg
                          className="h-5 w-5 text-red-400"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path
                            fillRule="evenodd"
                            d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                            clipRule="evenodd"
                          />
                        </svg>
                        <div className="ml-3">
                          <h3 className="text-sm font-medium text-red-800">
                            Expired Item Warning
                          </h3>
                          <p className="mt-1 text-sm text-red-700">
                            This item expired on{" "}
                            {new Date(
                              selectedItemDetails!.expirationDate!,
                            ).toLocaleDateString()}
                            . Consider disposing of expired inventory instead of
                            transferring.
                          </p>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Notes (Optional)
                </label>
                <textarea
                  rows={3}
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                  value={formData.notes}
                  onChange={(e) =>
                    setFormData({ ...formData, notes: e.target.value })
                  }
                  placeholder="Add any notes about this transfer..."
                />
              </div>
            </div>
          )}

          {/* Transfer Summary */}
          {formData.sourceWarehouseId > 0 &&
            formData.destinationWarehouseId > 0 &&
            formData.quantity > 0 && (
              <div className="rounded-md bg-blue-50 p-4">
                <h4 className="mb-2 font-medium text-blue-900">
                  Transfer Summary
                </h4>
                <p className="text-sm text-blue-800">
                  <span className="font-medium">Item:</span>{" "}
                  {selectedItem?.name} ({selectedItem?.sku})
                  <br />
                  <span className="font-medium">Quantity:</span>{" "}
                  {formData.quantity} units
                  <br />
                  <span className="font-medium">From:</span>{" "}
                  {getWarehouseName(formData.sourceWarehouseId)}
                  <br />
                  <span className="font-medium">To:</span>{" "}
                  {getWarehouseName(formData.destinationWarehouseId)}
                </p>
              </div>
            )}

          {/* Submit Button */}
          {formData.sourceWarehouseId > 0 &&
            formData.destinationWarehouseId > 0 && (
              <div className="flex justify-end">
                <button
                  type="submit"
                  disabled={transferMutation.isPending}
                  className="w-full rounded-md bg-blue-600 px-6 py-3 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-gray-400 sm:w-auto sm:py-2"
                >
                  {transferMutation.isPending
                    ? "Processing..."
                    : "Complete Transfer"}
                </button>
              </div>
            )}
        </form>
      </div>

      {/* Transfer Guidelines */}
      <div className="rounded-lg bg-gray-50 p-4 sm:p-6">
        <h3 className="mb-3 text-base font-semibold text-gray-900 sm:text-lg">
          Transfer Guidelines
        </h3>
        <ul className="space-y-2 text-sm text-gray-700">
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            Select an item and see all locations where it's available
          </li>
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            Choose the source warehouse with sufficient quantity
          </li>
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            All transfers are logged in the stock activity history
          </li>
          <li className="flex items-start">
            <span className="mr-2 text-blue-600">•</span>
            Color coding: <span className="text-green-600">Green</span> = high
            stock, <span className="text-yellow-600">Yellow</span> = medium,{" "}
            <span className="text-red-600">Red</span> = low
          </li>
        </ul>
      </div>
    </div>
  );
};
