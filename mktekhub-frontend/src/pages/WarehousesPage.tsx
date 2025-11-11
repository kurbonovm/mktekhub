import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { warehouseService } from "../services/warehouseService";
import type {
  Warehouse,
  WarehouseCreateRequest,
  WarehouseUpdateRequest,
} from "../types";

export const WarehousesPage = () => {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(
    null,
  );
  const [formData, setFormData] = useState<
    WarehouseCreateRequest | WarehouseUpdateRequest
  >({
    name: "",
    location: "",
    capacity: 0,
    managerId: 0,
  });

  const {
    data: warehouses,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const createMutation = useMutation({
    mutationFn: (data: WarehouseCreateRequest) => warehouseService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      closeModal();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: WarehouseUpdateRequest }) =>
      warehouseService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      closeModal();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => warehouseService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
    },
  });

  const openCreateModal = () => {
    setEditingWarehouse(null);
    setFormData({
      name: "",
      location: "",
      capacity: 0,
      managerId: 0,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (warehouse: Warehouse) => {
    setEditingWarehouse(warehouse);
    setFormData({
      name: warehouse.name,
      location: warehouse.location,
      capacity: warehouse.capacity,
      managerId: warehouse.managerId,
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingWarehouse(null);
    setFormData({
      name: "",
      location: "",
      capacity: 0,
      managerId: 0,
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingWarehouse) {
      updateMutation.mutate({ id: editingWarehouse.id, data: formData });
    } else {
      createMutation.mutate(formData as WarehouseCreateRequest);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm("Are you sure you want to delete this warehouse?")) {
      deleteMutation.mutate(id);
    }
  };

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
        <button
          onClick={openCreateModal}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          Add Warehouse
        </button>
      </div>

      {/* Warehouses Grid */}
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
                  warehouse.active
                    ? "bg-green-100 text-green-800"
                    : "bg-red-100 text-red-800"
                }`}
              >
                {warehouse.active ? "Active" : "Inactive"}
              </span>
            </div>

            <div className="mb-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Capacity:</span>
                <span className="font-medium text-gray-900">
                  {warehouse.currentOccupancy} / {warehouse.capacity}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Utilization:</span>
                <span className="font-medium text-gray-900">
                  {(
                    (warehouse.currentOccupancy / warehouse.capacity) *
                    100
                  ).toFixed(1)}
                  %
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Manager ID:</span>
                <span className="font-medium text-gray-900">
                  {warehouse.managerId}
                </span>
              </div>
            </div>

            {/* Progress Bar */}
            <div className="mb-4 h-2 w-full overflow-hidden rounded-full bg-gray-200">
              <div
                className={`h-full ${
                  (warehouse.currentOccupancy / warehouse.capacity) * 100 > 90
                    ? "bg-red-600"
                    : (warehouse.currentOccupancy / warehouse.capacity) * 100 >
                        75
                      ? "bg-yellow-600"
                      : "bg-green-600"
                }`}
                style={{
                  width: `${(warehouse.currentOccupancy / warehouse.capacity) * 100}%`,
                }}
              />
            </div>

            <div className="flex space-x-2">
              <button
                onClick={() => openEditModal(warehouse)}
                className="flex-1 rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                Edit
              </button>
              <button
                onClick={() => handleDelete(warehouse.id)}
                className="flex-1 rounded-md bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h2 className="mb-4 text-2xl font-bold text-gray-900">
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
                  htmlFor="capacity"
                  className="block text-sm font-medium text-gray-700"
                >
                  Capacity
                </label>
                <input
                  type="number"
                  id="capacity"
                  required
                  min="1"
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.capacity}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      capacity: parseInt(e.target.value),
                    })
                  }
                />
              </div>

              <div>
                <label
                  htmlFor="managerId"
                  className="block text-sm font-medium text-gray-700"
                >
                  Manager ID
                </label>
                <input
                  type="number"
                  id="managerId"
                  required
                  min="1"
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  value={formData.managerId}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      managerId: parseInt(e.target.value),
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
    </div>
  );
};
