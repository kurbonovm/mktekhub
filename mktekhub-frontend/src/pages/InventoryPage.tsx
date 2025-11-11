import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { inventoryService } from "../services/inventoryService";
import { warehouseService } from "../services/warehouseService";
import type {
  InventoryItem,
  InventoryItemCreateRequest,
  InventoryItemUpdateRequest,
} from "../types";

export const InventoryPage = () => {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<InventoryItem | null>(null);
  const [adjustingItem, setAdjustingItem] = useState<InventoryItem | null>(
    null,
  );
  const [adjustQuantity, setAdjustQuantity] = useState(0);
  const [adjustReason, setAdjustReason] = useState("");
  const [formData, setFormData] = useState<
    InventoryItemCreateRequest | InventoryItemUpdateRequest
  >({
    sku: "",
    name: "",
    description: "",
    category: "",
    quantity: 0,
    minimumStockLevel: 0,
    price: 0,
    warehouseId: 0,
  });

  const {
    data: inventoryItems,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["inventory"],
    queryFn: () => inventoryService.getAll(),
  });

  const { data: warehouses } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const createMutation = useMutation({
    mutationFn: (data: InventoryItemCreateRequest) =>
      inventoryService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      closeModal();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: number;
      data: InventoryItemUpdateRequest;
    }) => inventoryService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      closeModal();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => inventoryService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
    },
  });

  const adjustMutation = useMutation({
    mutationFn: ({
      id,
      quantity,
      reason,
    }: {
      id: number;
      quantity: number;
      reason: string;
    }) => inventoryService.adjustQuantity(id, { quantity, reason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      closeAdjustModal();
    },
  });

  const openCreateModal = () => {
    setEditingItem(null);
    setFormData({
      sku: "",
      name: "",
      description: "",
      category: "",
      quantity: 0,
      minimumStockLevel: 0,
      price: 0,
      warehouseId: warehouses?.[0]?.id || 0,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (item: InventoryItem) => {
    setEditingItem(item);
    setFormData({
      sku: item.sku,
      name: item.name,
      description: item.description,
      category: item.category,
      quantity: item.quantity,
      minimumStockLevel: item.minimumStockLevel,
      price: item.price,
      warehouseId: item.warehouseId,
    });
    setIsModalOpen(true);
  };

  const openAdjustModal = (item: InventoryItem) => {
    setAdjustingItem(item);
    setAdjustQuantity(0);
    setAdjustReason("");
    setIsAdjustModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingItem(null);
  };

  const closeAdjustModal = () => {
    setIsAdjustModalOpen(false);
    setAdjustingItem(null);
    setAdjustQuantity(0);
    setAdjustReason("");
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingItem) {
      updateMutation.mutate({ id: editingItem.id, data: formData });
    } else {
      createMutation.mutate(formData as InventoryItemCreateRequest);
    }
  };

  const handleAdjustSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (adjustingItem) {
      adjustMutation.mutate({
        id: adjustingItem.id,
        quantity: adjustQuantity,
        reason: adjustReason,
      });
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm("Are you sure you want to delete this item?")) {
      deleteMutation.mutate(id);
    }
  };

  const getWarehouseName = (warehouseId: number) => {
    return (
      warehouses?.find((w) => w.id === warehouseId)?.name || "Unknown"
    );
  };

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl">Loading inventory...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl text-red-600">
          Error loading inventory: {(error as Error).message}
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Inventory</h1>
        <button
          onClick={openCreateModal}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          Add Item
        </button>
      </div>

      {/* Inventory Table */}
      <div className="overflow-hidden rounded-lg bg-white shadow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                SKU
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Category
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Warehouse
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Quantity
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Price
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Total Value
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {inventoryItems?.map((item) => (
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
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                  {item.category}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                  {getWarehouseName(item.warehouseId)}
                </td>
                <td className="whitespace-nowrap px-6 py-4">
                  <div className="text-sm text-gray-900">{item.quantity}</div>
                  {item.quantity <= item.minimumStockLevel && (
                    <span className="inline-flex rounded-full bg-red-100 px-2 text-xs font-semibold leading-5 text-red-800">
                      Low Stock
                    </span>
                  )}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  ${item.price.toFixed(2)}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  ${(item.quantity * item.price).toFixed(2)}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm font-medium">
                  <button
                    onClick={() => openAdjustModal(item)}
                    className="mr-2 text-green-600 hover:text-green-900"
                  >
                    Adjust
                  </button>
                  <button
                    onClick={() => openEditModal(item)}
                    className="mr-2 text-blue-600 hover:text-blue-900"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(item.id)}
                    className="text-red-600 hover:text-red-900"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Create/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-lg bg-white p-6 shadow-xl">
            <h2 className="mb-4 text-2xl font-bold text-gray-900">
              {editingItem ? "Edit Item" : "Add Item"}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    SKU
                  </label>
                  <input
                    type="text"
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.sku}
                    onChange={(e) =>
                      setFormData({ ...formData, sku: e.target.value })
                    }
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Name
                  </label>
                  <input
                    type="text"
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.name}
                    onChange={(e) =>
                      setFormData({ ...formData, name: e.target.value })
                    }
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Description
                </label>
                <textarea
                  required
                  rows={3}
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Category
                  </label>
                  <input
                    type="text"
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.category}
                    onChange={(e) =>
                      setFormData({ ...formData, category: e.target.value })
                    }
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Warehouse
                  </label>
                  <select
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.warehouseId}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        warehouseId: parseInt(e.target.value),
                      })
                    }
                  >
                    {warehouses?.map((warehouse) => (
                      <option key={warehouse.id} value={warehouse.id}>
                        {warehouse.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Quantity
                  </label>
                  <input
                    type="number"
                    required
                    min="0"
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

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Min Stock Level
                  </label>
                  <input
                    type="number"
                    required
                    min="0"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.minimumStockLevel}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        minimumStockLevel: parseInt(e.target.value),
                      })
                    }
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Price
                  </label>
                  <input
                    type="number"
                    required
                    min="0"
                    step="0.01"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.price}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        price: parseFloat(e.target.value),
                      })
                    }
                  />
                </div>
              </div>

              <div className="flex space-x-3">
                <button
                  type="submit"
                  disabled={
                    createMutation.isPending || updateMutation.isPending
                  }
                  className="flex-1 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-gray-400"
                >
                  {createMutation.isPending || updateMutation.isPending
                    ? "Saving..."
                    : editingItem
                      ? "Update"
                      : "Create"}
                </button>
                <button
                  type="button"
                  onClick={closeModal}
                  className="flex-1 rounded-md bg-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-400"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Adjust Quantity Modal */}
      {isAdjustModalOpen && adjustingItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h2 className="mb-4 text-2xl font-bold text-gray-900">
              Adjust Quantity
            </h2>
            <div className="mb-4 rounded-md bg-blue-50 p-4">
              <p className="text-sm text-gray-700">
                <span className="font-medium">Item:</span> {adjustingItem.name}
              </p>
              <p className="text-sm text-gray-700">
                <span className="font-medium">Current Quantity:</span>{" "}
                {adjustingItem.quantity}
              </p>
            </div>
            <form onSubmit={handleAdjustSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Adjustment (use negative for decrease)
                </label>
                <input
                  type="number"
                  required
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={adjustQuantity}
                  onChange={(e) => setAdjustQuantity(parseInt(e.target.value))}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Reason
                </label>
                <textarea
                  required
                  rows={3}
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={adjustReason}
                  onChange={(e) => setAdjustReason(e.target.value)}
                  placeholder="Reason for adjustment..."
                />
              </div>

              <div className="flex space-x-3">
                <button
                  type="submit"
                  disabled={adjustMutation.isPending}
                  className="flex-1 rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:bg-gray-400"
                >
                  {adjustMutation.isPending ? "Adjusting..." : "Adjust"}
                </button>
                <button
                  type="button"
                  onClick={closeAdjustModal}
                  className="flex-1 rounded-md bg-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-400"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
