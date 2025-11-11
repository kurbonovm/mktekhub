import api from "./api";
import type { InventoryItem, InventoryItemRequest } from "../types";

export const inventoryService = {
  getAll: async (): Promise<InventoryItem[]> => {
    const response = await api.get<InventoryItem[]>("/inventory");
    return response.data;
  },

  getById: async (id: number): Promise<InventoryItem> => {
    const response = await api.get<InventoryItem>(`/inventory/${id}`);
    return response.data;
  },

  getBySku: async (sku: string): Promise<InventoryItem> => {
    const response = await api.get<InventoryItem>(`/inventory/sku/${sku}`);
    return response.data;
  },

  getByWarehouse: async (warehouseId: number): Promise<InventoryItem[]> => {
    const response = await api.get<InventoryItem[]>(`/inventory/warehouse/${warehouseId}`);
    return response.data;
  },

  getByCategory: async (category: string): Promise<InventoryItem[]> => {
    const response = await api.get<InventoryItem[]>(`/inventory/category/${category}`);
    return response.data;
  },

  getLowStock: async (): Promise<InventoryItem[]> => {
    const response = await api.get<InventoryItem[]>("/inventory/alerts/low-stock");
    return response.data;
  },

  getExpired: async (): Promise<InventoryItem[]> => {
    const response = await api.get<InventoryItem[]>("/inventory/alerts/expired");
    return response.data;
  },

  getExpiringSoon: async (days: number = 30): Promise<InventoryItem[]> => {
    const response = await api.get<InventoryItem[]>(`/inventory/alerts/expiring?days=${days}`);
    return response.data;
  },

  create: async (data: InventoryItemRequest): Promise<InventoryItem> => {
    const response = await api.post<InventoryItem>("/inventory", data);
    return response.data;
  },

  update: async (id: number, data: InventoryItemRequest): Promise<InventoryItem> => {
    const response = await api.put<InventoryItem>(`/inventory/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/inventory/${id}`);
  },

  adjustQuantity: async (id: number, quantityChange: number): Promise<InventoryItem> => {
    const response = await api.patch<InventoryItem>(`/inventory/${id}/adjust`, { quantityChange });
    return response.data;
  },
};
