import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { inventoryService } from "../services/inventoryService";
import { warehouseService } from "../services/warehouseService";
import { useAuth } from "../contexts/AuthContext";
import { useToast } from "../contexts/ToastContext";
import type { InventoryItem, InventoryItemRequest } from "../types";
import {
  SearchBar,
  InventoryFilters,
  ExpirationBadge,
  WarrantyBadge,
  Breadcrumb,
  ConfirmDialog,
  Tooltip,
  TableSkeleton,
  CardSkeleton,
} from "../components/common";
import { InventoryTable, InventoryMobileCard } from "../components/inventory";
import { defaultFilters, type InventoryFilterOptions } from "../types/filters";

export const InventoryPage = () => {
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();
  const toast = useToast();
  const isAdminOrManager = hasRole("ADMIN") || hasRole("MANAGER");

  // Search and Filter State
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] =
    useState<InventoryFilterOptions>(defaultFilters);
  const [showFilters, setShowFilters] = useState(false);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<InventoryItem | null>(null);
  const [adjustingItem, setAdjustingItem] = useState<InventoryItem | null>(
    null,
  );
  const [adjustQuantity, setAdjustQuantity] = useState(0);

  // Confirmation Dialog State
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState<number | null>(null);
  const [formData, setFormData] = useState<InventoryItemRequest>({
    sku: "",
    name: "",
    description: "",
    category: "",
    quantity: 0,
    reorderLevel: 0,
    unitPrice: 0,
    volumePerUnit: 1.0,
    warehouseId: 0,
    expirationDate: undefined,
    warrantyEndDate: undefined,
    barcode: undefined,
    brand: undefined,
  });

  const {
    data: inventoryItems,
    isLoading,
    error,
    isRefetching,
  } = useQuery({
    queryKey: ["inventory"],
    queryFn: () => inventoryService.getAll(),
  });

  const { data: warehouses } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehouseService.getAll(),
  });

  const createMutation = useMutation({
    mutationFn: (data: InventoryItemRequest) => inventoryService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      closeModal();
      toast.success("Item created successfully!");
    },
    onError: (error) => {
      toast.error(`Failed to create item: ${(error as Error).message}`);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: InventoryItemRequest }) =>
      inventoryService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      closeModal();
      toast.success("Item updated successfully!");
    },
    onError: (error) => {
      toast.error(`Failed to update item: ${(error as Error).message}`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => inventoryService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      setIsDeleteDialogOpen(false);
      setItemToDelete(null);
      toast.success("Item deleted successfully!");
    },
    onError: (error) => {
      toast.error(`Failed to delete item: ${(error as Error).message}`);
    },
  });

  const adjustMutation = useMutation({
    mutationFn: ({ id, quantity }: { id: number; quantity: number }) =>
      inventoryService.adjustQuantity(id, quantity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      closeAdjustModal();
      toast.success("Quantity adjusted successfully!");
    },
    onError: (error) => {
      toast.error(`Failed to adjust quantity: ${(error as Error).message}`);
    },
  });

  // Extract unique categories and brands from inventory
  const { categories, brands } = useMemo(() => {
    if (!inventoryItems) return { categories: [], brands: [] };

    const categoriesSet = new Set<string>();
    const brandsSet = new Set<string>();

    inventoryItems.forEach((item) => {
      if (item.category) categoriesSet.add(item.category);
      if (item.brand) brandsSet.add(item.brand);
    });

    return {
      categories: Array.from(categoriesSet).sort(),
      brands: Array.from(brandsSet).sort(),
    };
  }, [inventoryItems]);

  // Filter and search logic
  const filteredItems = useMemo(() => {
    if (!inventoryItems) return [];

    let filtered = [...inventoryItems];

    // Apply search filter
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (item) =>
          item.sku.toLowerCase().includes(searchLower) ||
          item.name.toLowerCase().includes(searchLower) ||
          item.description?.toLowerCase().includes(searchLower) ||
          item.category?.toLowerCase().includes(searchLower) ||
          item.brand?.toLowerCase().includes(searchLower) ||
          item.barcode?.toLowerCase().includes(searchLower),
      );
    }

    // Apply warehouse filter
    if (filters.warehouseId !== 0) {
      filtered = filtered.filter(
        (item) => item.warehouseId === filters.warehouseId,
      );
    }

    // Apply category filter
    if (filters.category) {
      filtered = filtered.filter((item) => item.category === filters.category);
    }

    // Apply brand filter
    if (filters.brand) {
      filtered = filtered.filter((item) => item.brand === filters.brand);
    }

    // Apply stock status filter
    if (filters.stockStatus !== "all") {
      filtered = filtered.filter((item) => {
        if (filters.stockStatus === "in-stock")
          return item.quantity > 0 && !item.isLowStock;
        if (filters.stockStatus === "low-stock") return item.isLowStock;
        if (filters.stockStatus === "out-of-stock") return item.quantity === 0;
        return true;
      });
    }

    // Apply expiration status filter
    if (filters.expirationStatus !== "all") {
      filtered = filtered.filter((item) => {
        if (filters.expirationStatus === "expired") return item.isExpired;
        if (filters.expirationStatus === "expiring-soon") {
          // Check if expiring within 30 days
          if (!item.expirationDate) return false;
          const expirationDate = new Date(item.expirationDate);
          const today = new Date();
          const thirtyDaysFromNow = new Date(
            today.getTime() + 30 * 24 * 60 * 60 * 1000,
          );
          return expirationDate <= thirtyDaysFromNow && expirationDate > today;
        }
        if (filters.expirationStatus === "valid") {
          return !item.isExpired && !item.expirationDate;
        }
        return true;
      });
    }

    // Apply quantity range filter
    if (filters.minQuantity) {
      const min = parseFloat(filters.minQuantity);
      filtered = filtered.filter((item) => item.quantity >= min);
    }
    if (filters.maxQuantity) {
      const max = parseFloat(filters.maxQuantity);
      filtered = filtered.filter((item) => item.quantity <= max);
    }

    // Apply price range filter
    if (filters.minPrice) {
      const min = parseFloat(filters.minPrice);
      filtered = filtered.filter((item) => (item.unitPrice || 0) >= min);
    }
    if (filters.maxPrice) {
      const max = parseFloat(filters.maxPrice);
      filtered = filtered.filter((item) => (item.unitPrice || 0) <= max);
    }

    // Sort by updatedAt descending (newest first)
    return filtered.sort((a, b) => {
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });
  }, [inventoryItems, searchTerm, filters]);

  // Reset filters handler
  const handleResetFilters = () => {
    setFilters(defaultFilters);
    setSearchTerm("");
  };

  const openCreateModal = () => {
    setEditingItem(null);
    setFormData({
      sku: "",
      name: "",
      description: "",
      category: "",
      brand: "",
      quantity: 0,
      reorderLevel: 0,
      unitPrice: 0,
      warehouseId: warehouses?.[0]?.id || 0,
      expirationDate: undefined,
      warrantyEndDate: undefined,
      barcode: "",
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
      brand: item.brand,
      quantity: item.quantity,
      reorderLevel: item.reorderLevel,
      unitPrice: item.unitPrice,
      volumePerUnit: item.volumePerUnit || 1.0,
      warehouseId: item.warehouseId,
      expirationDate: item.expirationDate,
      warrantyEndDate: item.warrantyEndDate,
      barcode: item.barcode,
    });
    setIsModalOpen(true);
  };

  const openAdjustModal = (item: InventoryItem) => {
    setAdjustingItem(item);
    setAdjustQuantity(0);
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
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingItem) {
      updateMutation.mutate({ id: editingItem.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleAdjustSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (adjustingItem) {
      adjustMutation.mutate({
        id: adjustingItem.id,
        quantity: adjustQuantity,
      });
    }
  };

  const handleDelete = (id: number) => {
    setItemToDelete(id);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (itemToDelete !== null) {
      deleteMutation.mutate(itemToDelete);
    }
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
    <div className="p-4 sm:p-6 lg:p-8">
      {/* Breadcrumb Navigation */}
      <Breadcrumb autoGenerate />

      <div className="mb-4 flex flex-col gap-4 sm:mb-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 sm:text-3xl">
            Inventory
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            {filteredItems?.length || 0} item
            {filteredItems?.length !== 1 ? "s" : ""} found
            {searchTerm ||
            filters.warehouseId !== 0 ||
            filters.category ||
            filters.brand
              ? ` (filtered from ${inventoryItems?.length || 0} total)`
              : ""}
          </p>
        </div>
        {isAdminOrManager && (
          <Tooltip content="Add a new inventory item" position="left">
            <button
              onClick={openCreateModal}
              className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:w-auto"
            >
              Add Item
            </button>
          </Tooltip>
        )}
      </div>

      {/* Search Bar */}
      <div className="mb-4">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Search by SKU, name, description, category, brand, or barcode..."
          className="w-full"
        />
      </div>

      {/* Filters */}
      <div className="mb-6">
        <InventoryFilters
          filters={filters}
          onFilterChange={setFilters}
          onReset={handleResetFilters}
          warehouses={warehouses || []}
          categories={categories}
          brands={brands}
          isExpanded={showFilters}
          onToggle={() => setShowFilters(!showFilters)}
        />
      </div>

      {/* Mobile Card View - Hidden on MD and larger */}
      <div className="block space-y-4 md:hidden">
        {isRefetching && !isLoading ? (
          <CardSkeleton cards={6} />
        ) : filteredItems.length === 0 ? (
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
              {searchTerm ||
              filters.warehouseId !== 0 ||
              filters.category ||
              filters.brand
                ? "Try adjusting your search or filters"
                : "Get started by adding a new inventory item"}
            </p>
          </div>
        ) : (
          filteredItems.map((item) => (
            <div key={item.id} className="rounded-lg bg-white p-4 shadow">
              <div className="mb-3 flex items-start justify-between">
                <div>
                  <h3 className="font-medium text-gray-900">{item.name}</h3>
                  <p className="mt-1 text-sm text-gray-500">SKU: {item.sku}</p>
                </div>
                {item.quantity <= (item.reorderLevel || 0) && (
                  <span className="rounded-full bg-red-100 px-2 py-1 text-xs font-medium text-red-800">
                    Low Stock
                  </span>
                )}
              </div>

              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Category:</span>
                  <span className="font-medium">{item.category || "N/A"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Warehouse:</span>
                  <span className="font-medium">
                    {warehouses?.find((w) => w.id === item.warehouseId)?.name ||
                      "N/A"}
                  </span>
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
                    onClick={() => openAdjustModal(item)}
                    className="flex-1 rounded-md bg-green-600 px-3 py-2 text-sm font-medium text-white hover:bg-green-700"
                  >
                    Adjust
                  </button>
                  <button
                    onClick={() => openEditModal(item)}
                    className="flex-1 rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(item.id)}
                    className="flex-1 rounded-md bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700"
                  >
                    Delete
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* Tablet/Desktop Table View - Hidden on small screens */}
      <div className="hidden overflow-x-auto rounded-lg bg-white shadow md:block">
        {isRefetching && !isLoading ? (
          <div className="p-4">
            <TableSkeleton rows={10} />
          </div>
        ) : (
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
                {filteredItems.length === 0 ? (
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
                          {searchTerm ||
                          filters.warehouseId !== 0 ||
                          filters.category ||
                          filters.brand
                            ? "Try adjusting your search or filters"
                            : "Get started by adding a new inventory item"}
                        </p>
                        {(searchTerm ||
                          filters.warehouseId !== 0 ||
                          filters.category ||
                          filters.brand) && (
                          <button
                            onClick={handleResetFilters}
                            className="mt-4 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
                          >
                            Clear filters
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ) : (
                  filteredItems?.map((item) => (
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
                        <div className="text-sm text-gray-900">
                          {item.quantity}
                        </div>
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
                                onClick={() => openAdjustModal(item)}
                                className="mr-2 text-green-600 hover:text-green-900"
                              >
                                Adjust
                              </button>
                            </Tooltip>
                            <Tooltip content="Edit item details" position="top">
                              <button
                                onClick={() => openEditModal(item)}
                                className="mr-2 text-blue-600 hover:text-blue-900"
                              >
                                Edit
                              </button>
                            </Tooltip>
                            <Tooltip content="Delete this item" position="top">
                              <button
                                onClick={() => handleDelete(item.id)}
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
        )}
      </div>

      {/* Create/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black bg-opacity-50 sm:items-center">
          <div className="max-h-[95vh] w-full overflow-y-auto rounded-t-lg bg-white p-4 shadow-xl sm:max-h-[90vh] sm:max-w-2xl sm:rounded-lg sm:p-6">
            <h2 className="mb-4 text-xl font-bold text-gray-900 sm:text-2xl">
              {editingItem ? "Edit Item" : "Add Item"}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
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
                    Brand
                  </label>
                  <input
                    type="text"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.brand || ""}
                    onChange={(e) =>
                      setFormData({ ...formData, brand: e.target.value })
                    }
                    placeholder="Optional"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Warehouse
                  </label>
                  <select
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
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

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Barcode
                  </label>
                  <input
                    type="text"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                    value={formData.barcode || ""}
                    onChange={(e) =>
                      setFormData({ ...formData, barcode: e.target.value })
                    }
                    placeholder="Optional"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Quantity
                  </label>
                  <input
                    type="number"
                    required
                    min="0"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
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
                    Reorder Level
                  </label>
                  <input
                    type="number"
                    min="0"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                    value={formData.reorderLevel || 0}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        reorderLevel: parseInt(e.target.value),
                      })
                    }
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Unit Price ($)
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.unitPrice || 0}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        unitPrice: parseFloat(e.target.value),
                      })
                    }
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Volume per Unit (ft³)
                  </label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.volumePerUnit || 1.0}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        volumePerUnit: parseFloat(e.target.value),
                      })
                    }
                    placeholder="1.00"
                  />
                </div>
              </div>

              {/* Date Fields */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Expiration Date
                  </label>
                  <input
                    type="date"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.expirationDate || ""}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        expirationDate: e.target.value || undefined,
                      })
                    }
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    Leave empty for non-perishable items
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Warranty End Date
                  </label>
                  <input
                    type="date"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={formData.warrantyEndDate || ""}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        warrantyEndDate: e.target.value || undefined,
                      })
                    }
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    Leave empty if no warranty
                  </p>
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
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black bg-opacity-50 sm:items-center">
          <div className="w-full max-w-md rounded-t-lg bg-white p-4 shadow-xl sm:rounded-lg sm:p-6">
            <h2 className="mb-4 text-xl font-bold text-gray-900 sm:text-2xl">
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
                  value={adjustQuantity || ""}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value === "" || value === "-") {
                      setAdjustQuantity(0);
                    } else {
                      setAdjustQuantity(parseInt(value, 10));
                    }
                  }}
                  placeholder="Enter adjustment amount"
                />
                <p className="mt-1 text-sm text-gray-500">
                  New quantity will be:{" "}
                  {Math.max(
                    0,
                    (adjustingItem?.quantity || 0) + (adjustQuantity || 0),
                  )}
                </p>
                {adjustingItem?.expirationDate &&
                  new Date(adjustingItem.expirationDate) < new Date() && (
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
                              adjustingItem.expirationDate,
                            ).toLocaleDateString()}
                            . Consider disposing of expired inventory instead of
                            adjusting quantity.
                          </p>
                        </div>
                      </div>
                    </div>
                  )}
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

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        title="Delete Item"
        message="Are you sure you want to delete this item? This action cannot be undone."
        confirmText="Delete"
        cancelText="Cancel"
        variant="danger"
        onConfirm={confirmDelete}
        onCancel={() => {
          setIsDeleteDialogOpen(false);
          setItemToDelete(null);
        }}
      />
    </div>
  );
};
