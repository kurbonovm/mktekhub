import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { warehouseService } from "../services/warehouseService";
import { useAuth } from "../contexts/AuthContext";
import { useToast } from "../contexts/ToastContext";
import { ConfirmDialog, CardSkeleton } from "../components/common";
import type { Warehouse, WarehouseRequest } from "../types";

export const WarehousesPage = () => {
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();
  const toast = useToast();
  const isAdminOrManager = hasRole("ADMIN") || hasRole("MANAGER");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(
    null,
  );
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [warehouseToDelete, setWarehouseToDelete] = useState<Warehouse | null>(
    null,
  );
  const [formData, setFormData] = useState<WarehouseRequest>({
    name: "",
    location: "",
    maxCapacity: 0,
    capacityAlertThreshold: 80,
  });

  const {
    data: warehouses,
    isLoading,
    error,
    isRefetching,
  } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const createMutation = useMutation({
    mutationFn: (data: WarehouseRequest) => warehouseService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      toast.success("Warehouse created successfully");
      closeModal();
    },
    onError: (error: unknown) => {
      const message =
        (error as { response?: { data?: { message?: string } } })?.response
          ?.data?.message || "Failed to create warehouse";
      toast.error(message);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: WarehouseRequest }) =>
      warehouseService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      toast.success("Warehouse updated successfully");
      closeModal();
    },
    onError: (error: unknown) => {
      const message =
        (error as { response?: { data?: { message?: string } } })?.response
          ?.data?.message || "Failed to update warehouse";
      toast.error(message);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => warehouseService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      toast.success("Warehouse deleted successfully");
    },
    onError: (error: unknown) => {
      const message =
        (error as { response?: { data?: { message?: string } } })?.response
          ?.data?.message || "Failed to delete warehouse";
      toast.error(message);
    },
  });

  const openCreateModal = () => {
    setEditingWarehouse(null);
    setFormData({
      name: "",
      location: "",
      maxCapacity: 0,
      capacityAlertThreshold: 80,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (warehouse: Warehouse) => {
    setEditingWarehouse(warehouse);
    setFormData({
      name: warehouse.name,
      location: warehouse.location,
      maxCapacity: warehouse.maxCapacity,
      capacityAlertThreshold: warehouse.capacityAlertThreshold,
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingWarehouse(null);
    setFormData({
      name: "",
      location: "",
      maxCapacity: 0,
      capacityAlertThreshold: 80,
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingWarehouse) {
      updateMutation.mutate({ id: editingWarehouse.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDeleteClick = (warehouse: Warehouse) => {
    setWarehouseToDelete(warehouse);
    setDeleteConfirmOpen(true);
  };

  const handleDeleteConfirm = () => {
    if (warehouseToDelete) {
      deleteMutation.mutate(warehouseToDelete.id);
      setDeleteConfirmOpen(false);
      setWarehouseToDelete(null);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteConfirmOpen(false);
    setWarehouseToDelete(null);
  };

  // Keyboard navigation - close modal with Escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        if (isModalOpen) closeModal();
        if (deleteConfirmOpen) handleDeleteCancel();
      }
    };

    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [isModalOpen, deleteConfirmOpen]);

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl">Loading warehouses...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl text-red-600">
          Error loading warehouses: {(error as Error).message}
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Warehouses</h1>
        {isAdminOrManager && (
          <button
            onClick={openCreateModal}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Add Warehouse
          </button>
        )}
      </div>

      {/* Warehouses Grid */}
      {isRefetching && !isLoading ? (
        <CardSkeleton cards={6} />
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {warehouses?.map((warehouse) => (
            <div
              key={warehouse.id}
              className="rounded-lg bg-white p-6 shadow hover:shadow-lg"
            >
              <div className="mb-4 flex items-start justify-between">
                <div>
                  <h3 className="text-xl font-semibold text-gray-900">
                    {warehouse.name}
                  </h3>
                  <p className="text-sm text-gray-600">{warehouse.location}</p>
                </div>
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${
                    warehouse.isActive
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
                  }`}
                >
                  {warehouse.isActive ? "Active" : "Inactive"}
                </span>
              </div>

              <div className="mb-4 space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Capacity:</span>
                  <span className="font-medium text-gray-900">
                    {warehouse.currentCapacity.toFixed(2)} /{" "}
                    {warehouse.maxCapacity.toFixed(2)} ft³
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Utilization:</span>
                  <span className="font-medium text-gray-900">
                    {warehouse.utilizationPercentage.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Alert Threshold:</span>
                  <span className="font-medium text-gray-900">
                    {warehouse.capacityAlertThreshold}%
                  </span>
                </div>
              </div>

              {/* Progress Bar */}
              <div className="mb-4 h-2 w-full overflow-hidden rounded-full bg-gray-200">
                <div
                  className={`h-full ${
                    warehouse.utilizationPercentage > 90
                      ? "bg-red-600"
                      : warehouse.utilizationPercentage > 75
                        ? "bg-yellow-600"
                        : "bg-green-600"
                  }`}
                  style={{
                    width: `${warehouse.utilizationPercentage}%`,
                  }}
                />
              </div>

              {isAdminOrManager && (
                <div className="flex flex-col space-y-2">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => openEditModal(warehouse)}
                      className="flex-1 rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDeleteClick(warehouse)}
                      disabled={warehouse.currentCapacity > 0}
                      className="flex-1 rounded-md bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:bg-gray-400"
                      title={
                        warehouse.currentCapacity > 0
                          ? "Cannot delete warehouse with inventory"
                          : "Delete warehouse"
                      }
                    >
                      Delete
                    </button>
                  </div>
                  {warehouse.currentCapacity > 0 && (
                    <p className="text-xs text-gray-600 text-center">
                      Remove all items before deleting
                    </p>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Create/Edit Modal */}
      {isModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-title"
        >
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h2
              id="modal-title"
              className="mb-4 text-2xl font-bold text-gray-900"
            >
              {editingWarehouse ? "Edit Warehouse" : "Add Warehouse"}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label
                  htmlFor="name"
                  className="block text-sm font-medium text-gray-700"
                >
                  Name
                </label>
                <input
                  type="text"
                  id="name"
                  required
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                />
              </div>

              <div>
                <label
                  htmlFor="location"
                  className="block text-sm font-medium text-gray-700"
                >
                  Location
                </label>
                <input
                  type="text"
                  id="location"
                  required
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.location}
                  onChange={(e) =>
                    setFormData({ ...formData, location: e.target.value })
                  }
                />
              </div>

              <div>
                <label
                  htmlFor="maxCapacity"
                  className="block text-sm font-medium text-gray-700"
                >
                  Max Capacity (cubic feet)
                </label>
                <input
                  type="number"
                  id="maxCapacity"
                  required
                  min="0.01"
                  step="0.01"
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.maxCapacity}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      maxCapacity: parseFloat(e.target.value),
                    })
                  }
                />
              </div>

              <div>
                <label
                  htmlFor="capacityAlertThreshold"
                  className="block text-sm font-medium text-gray-700"
                >
                  Capacity Alert Threshold (%)
                </label>
                <input
                  type="number"
                  id="capacityAlertThreshold"
                  required
                  min="0"
                  max="100"
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.capacityAlertThreshold}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      capacityAlertThreshold: parseFloat(e.target.value),
                    })
                  }
                />
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
                    : editingWarehouse
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

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={deleteConfirmOpen && warehouseToDelete !== null}
        title="Delete Warehouse"
        message={`Are you sure you want to delete "${warehouseToDelete?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="danger"
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
      />
    </div>
  );
};
